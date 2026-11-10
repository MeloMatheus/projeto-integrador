package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.PagamentoModel;
import br.com.cupcake.srv.model.PedidoModel;
import br.com.cupcake.srv.model.StatusPagamentoModel;
import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.PagamentoRepository;
import br.com.cupcake.srv.repository.PedidoRepository;
import br.com.cupcake.srv.repository.StatusPagamentoRepository;
import br.com.cupcake.srv.request.pagamento.CriarPagamentoRequest;
import br.com.cupcake.srv.response.pagamento.PagamentoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PagamentoService {
    private final PagamentoRepository pagamentoRepository;
    private final StatusPagamentoRepository statusRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            StatusPagamentoRepository statusRepository,
            PedidoRepository pedidoRepository,
            PedidoService pedidoService,
            UsuarioService usuarioService
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.statusRepository = statusRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public PagamentoResponse criarTentativa(String email, UUID pedidoId, CriarPagamentoRequest request) {
        PedidoModel pedido = pedidoService.findPedido(email, false, pedidoId);
        pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        String method = request.metodo().trim().toUpperCase(Locale.ROOT);
        if (!List.of("PIX", "CARTAO", "BOLETO").contains(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Método de pagamento não suportado.");
        }
        String idempotencyKey = request.chaveIdempotencia().trim();
        PagamentoModel existingWithKey = pagamentoRepository.findByChaveIdempotencia(idempotencyKey)
                .orElse(null);
        if (existingWithKey != null) {
            if (existingWithKey.getPedido().getId().equals(pedidoId)
                    && existingWithKey.getMetodo().equals(method)) {
                return PagamentoResponse.from(existingWithKey);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A chave de idempotência já foi usada.");
        }
        if (!pedido.getStatusPedido().getNome().equals("PENDENTE_PAGAMENTO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O pedido não está aguardando pagamento.");
        }
        List<PagamentoModel> existing = pagamentoRepository.findByPedidoId(pedidoId);
        if (existing.stream().anyMatch(payment -> payment.getStatusPagamento().getNome().equals("PENDENTE"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma tentativa de pagamento pendente.");
        }
        PagamentoModel payment = new PagamentoModel();
        payment.setPedido(pedido);
        payment.setStatusPagamento(statusByName("PENDENTE"));
        payment.setMetodo(method);
        payment.setValor(pedido.getValorTotal());
        payment.setChaveIdempotencia(idempotencyKey);
        return PagamentoResponse.from(pagamentoRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar(String email, boolean admin, UUID pedidoId) {
        pedidoService.findPedido(email, admin, pedidoId);
        return pagamentoRepository.findByPedidoId(pedidoId).stream().map(PagamentoResponse::from).toList();
    }

    @Transactional
    public PagamentoResponse confirmar(UUID pagamentoId, String resultado, String transacaoExterna, String adminEmail) {
        PagamentoModel initialPayment = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
        PedidoModel pedido = pedidoRepository.findByIdForUpdate(initialPayment.getPedido().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        PagamentoModel pagamento = pagamentoRepository.findByIdForUpdate(pagamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
        pagamento.setPedido(pedido);
        String outcome = resultado.trim().toUpperCase(Locale.ROOT);
        if (!List.of("APROVADO", "RECUSADO", "CANCELADO").contains(outcome)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resultado de confirmação inválido.");
        }
        String current = pagamento.getStatusPagamento().getNome();
        if (!current.equals("PENDENTE")) {
            if (current.equals(outcome) && (
                    transacaoExterna == null || transacaoExterna.equals(pagamento.getTransacaoExterna())
            )) {
                return PagamentoResponse.from(pagamento);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tentativa de pagamento já foi processada.");
        }
        if (outcome.equals("APROVADO")) {
            if (transacaoExterna == null || transacaoExterna.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A confirmação aprovada exige uma referência de transação.");
            }
            if (!pagamento.getPedido().getStatusPedido().getNome().equals("PENDENTE_PAGAMENTO")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "O pedido não pode mais ser pago.");
            }
            pagamentoRepository.findByTransacaoExterna(transacaoExterna).ifPresent(existing -> {
                if (!existing.getId().equals(pagamentoId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A transação externa já foi processada.");
                }
            });
            if (pagamentoRepository.findByPedidoId(pagamento.getPedido().getId()).stream()
                    .anyMatch(other -> !other.getId().equals(pagamentoId)
                            && other.getStatusPagamento().getNome().equals("APROVADO"))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "O pedido já possui pagamento aprovado.");
            }
        }

        StatusPagamentoModel status = statusByName(outcome);
        pagamento.setStatusPagamento(status);
        pagamento.setDataConfirmacao(LocalDateTime.now());
        if (transacaoExterna != null && !transacaoExterna.isBlank()) {
            pagamento.setTransacaoExterna(transacaoExterna.trim());
        }
        if (outcome.equals("APROVADO")) {
            UsuarioModel admin = usuarioService.buscarAtivoPorEmail(adminEmail);
            pedidoService.changeStatus(
                    pagamento.getPedido(),
                    pedidoService.statusByName("PAGO"),
                    admin,
                    "Pagamento confirmado manualmente; integração real de provedor não configurada."
            );
        }
        return PagamentoResponse.from(pagamento);
    }

    private StatusPagamentoModel statusByName(String name) {
        return statusRepository.findByNome(name)
                .orElseThrow(() -> new IllegalStateException("Status de pagamento ausente: " + name));
    }

}
