package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.CarrinhoModel;
import br.com.cupcake.srv.model.CupomModel;
import br.com.cupcake.srv.model.EnderecoModel;
import br.com.cupcake.srv.model.HistoricoStatusPedidoModel;
import br.com.cupcake.srv.model.ItemCarrinhoModel;
import br.com.cupcake.srv.model.ItemPedidoModel;
import br.com.cupcake.srv.model.PagamentoModel;
import br.com.cupcake.srv.model.PedidoModel;
import br.com.cupcake.srv.model.StatusPagamentoModel;
import br.com.cupcake.srv.model.StatusCarrinho;
import br.com.cupcake.srv.model.StatusPedidoModel;
import br.com.cupcake.srv.model.UsoCupomModel;
import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.CarrinhoRepository;
import br.com.cupcake.srv.repository.CupomRepository;
import br.com.cupcake.srv.repository.HistoricoStatusPedidoRepository;
import br.com.cupcake.srv.repository.ItemCarrinhoRepository;
import br.com.cupcake.srv.repository.ItemPedidoRepository;
import br.com.cupcake.srv.repository.PagamentoRepository;
import br.com.cupcake.srv.repository.PedidoRepository;
import br.com.cupcake.srv.repository.StatusPagamentoRepository;
import br.com.cupcake.srv.repository.StatusPedidoRepository;
import br.com.cupcake.srv.repository.UsoCupomRepository;
import br.com.cupcake.srv.request.pedido.CriarPedidoRequest;
import br.com.cupcake.srv.response.pedido.PedidoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final StatusPedidoRepository statusRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final StatusPagamentoRepository statusPagamentoRepository;
    private final HistoricoStatusPedidoRepository historicoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final CupomRepository cupomRepository;
    private final UsoCupomRepository usoCupomRepository;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final EnderecoService enderecoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            StatusPedidoRepository statusRepository,
            ItemPedidoRepository itemPedidoRepository,
            PagamentoRepository pagamentoRepository,
            StatusPagamentoRepository statusPagamentoRepository,
            HistoricoStatusPedidoRepository historicoRepository,
            CarrinhoRepository carrinhoRepository,
            ItemCarrinhoRepository itemCarrinhoRepository,
            CupomRepository cupomRepository,
            UsoCupomRepository usoCupomRepository,
            UsuarioService usuarioService,
            ProdutoService produtoService,
            EnderecoService enderecoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.statusRepository = statusRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.statusPagamentoRepository = statusPagamentoRepository;
        this.historicoRepository = historicoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.cupomRepository = cupomRepository;
        this.usoCupomRepository = usoCupomRepository;
        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
        this.enderecoService = enderecoService;
    }

    @Transactional
    public PedidoResponse criar(String email, CriarPedidoRequest request) {
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        EnderecoModel endereco = enderecoService.buscarDoUsuario(request.idEndereco(), usuario.getId());
        List<CarrinhoModel> openCarts = carrinhoRepository.findOpenByUsuarioIdForUpdate(
                usuario.getId(), StatusCarrinho.ATIVO
        );
        if (openCarts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O usuário não possui um carrinho aberto.");
        }
        CarrinhoModel carrinho = openCarts.getFirst();
        List<ItemCarrinhoModel> cartItems = itemCarrinhoRepository.findByCarrinhoId(carrinho.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível criar um pedido sem itens.");
        }

        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
        Map<UUID, BigDecimal> unitPrices = new HashMap<>();
        for (ItemCarrinhoModel item : cartItems) {
            var produto = produtoService.buscarDisponivel(item.getProduto().getId());
            BigDecimal unitPrice = produto.getPreco().setScale(2, RoundingMode.HALF_UP);
            unitPrices.put(produto.getId(), unitPrice);
            BigDecimal lineSubtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        CupomModel cupom = null;
        BigDecimal desconto = BigDecimal.ZERO.setScale(2);
        if (request.codigoCupom() != null && !request.codigoCupom().isBlank()) {
            cupom = validarCupom(request.codigoCupom(), subtotal);
            desconto = calcularDesconto(cupom, subtotal);
        }

        BigDecimal total = subtotal.subtract(desconto).setScale(2, RoundingMode.HALF_UP);
        boolean semCobranca = total.signum() == 0;
        PedidoModel pedido = new PedidoModel();
        pedido.setUsuario(usuario);
        pedido.setStatusPedido(statusByName(semCobranca ? "PAGO" : "PENDENTE_PAGAMENTO"));
        pedido.setNumeroPedido("CP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        pedido.setSubtotal(subtotal);
        pedido.setValorDesconto(desconto);
        pedido.setValorFrete(BigDecimal.ZERO.setScale(2));
        pedido.setValorTotal(total);
        pedido.setDestinatario(endereco.getDestinatario());
        pedido.setCepEntrega(endereco.getCep());
        pedido.setLogradouroEntrega(endereco.getLogradouro());
        pedido.setNumeroEntrega(endereco.getNumero());
        pedido.setComplementoEntrega(endereco.getComplemento());
        pedido.setBairroEntrega(endereco.getBairro());
        pedido.setCidadeEntrega(endereco.getCidade());
        pedido.setUfEntrega(endereco.getUf());
        pedido.setReferenciaEntrega(endereco.getReferencia());
        pedido = pedidoRepository.save(pedido);

        for (ItemCarrinhoModel cartItem : cartItems) {
            BigDecimal unitPrice = unitPrices.get(cartItem.getProduto().getId());
            ItemPedidoModel itemPedido = new ItemPedidoModel();
            itemPedido.setPedido(pedido);
            itemPedido.setProduto(cartItem.getProduto());
            itemPedido.setNomeProdutoSnapshot(cartItem.getProduto().getNome());
            itemPedido.setQuantidade(cartItem.getQuantidade());
            itemPedido.setPrecoUnitario(unitPrice);
            itemPedidoRepository.save(itemPedido);
        }

        registrarHistorico(
                pedido,
                pedido.getStatusPedido(),
                usuario,
                semCobranca
                        ? "Pedido coberto integralmente por descontos; nenhuma cobrança é necessária."
                        : "Pedido criado."
        );
        if (cupom != null) {
            UsoCupomModel uso = new UsoCupomModel();
            uso.setCupom(cupom);
            uso.setPedido(pedido);
            uso.setUsuario(usuario);
            uso.setValorDescontoAplicado(desconto);
            usoCupomRepository.save(uso);
        }
        carrinho.setStatus(StatusCarrinho.FINALIZADO);
        return response(pedido);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(String email, boolean admin, Pageable pageable) {
        Page<PedidoModel> pedidos;
        if (admin) {
            pedidos = pedidoRepository.findAll(pageable);
        } else {
            UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
            pedidos = pedidoRepository.findByUsuarioId(usuario.getId(), pageable);
        }
        return pedidos.map(this::response);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(String email, boolean admin, UUID id) {
        PedidoModel pedido = findPedido(email, admin, id);
        return response(pedido);
    }

    @Transactional
    public PedidoResponse alterarStatus(UUID id, String novoNome, String email, String observacao) {
        PedidoModel pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        String current = pedido.getStatusPedido().getNome();
        String next = novoNome.trim().toUpperCase();
        Map<String, Set<String>> transitions = Map.of(
                "PENDENTE_PAGAMENTO", Set.of("CANCELADO"),
                "PAGO", Set.of("EM_PREPARACAO"),
                "EM_PREPARACAO", Set.of("ENVIADO"),
                "ENVIADO", Set.of("CONCLUIDO")
        );
        if (current.equals(next)) {
            return response(pedido);
        }
        if (!transitions.getOrDefault(current, Set.of()).contains(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transição de status inválida.");
        }
        UsuarioModel responsavel = usuarioService.buscarAtivoPorEmail(email);
        changeStatus(pedido, statusByName(next), responsavel, observacao);
        if (next.equals("CANCELADO")) {
            StatusPagamentoModel cancelled = statusPagamentoRepository.findByNome("CANCELADO")
                    .orElseThrow(() -> new IllegalStateException("Status de pagamento ausente: CANCELADO"));
            for (PagamentoModel payment : pagamentoRepository.findByPedidoId(id)) {
                if (payment.getStatusPagamento().getNome().equals("PENDENTE")) {
                    payment.setStatusPagamento(cancelled);
                    payment.setDataConfirmacao(LocalDateTime.now());
                }
            }
        }
        return response(pedido);
    }

    PedidoModel findPedido(String email, boolean admin, UUID id) {
        if (admin) {
            return pedidoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        }
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        return pedidoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
    }

    void changeStatus(PedidoModel pedido, StatusPedidoModel status, UsuarioModel responsavel, String observacao) {
        pedido.setStatusPedido(status);
        registrarHistorico(pedido, status, responsavel, observacao);
    }

    private CupomModel validarCupom(String codigo, BigDecimal subtotal) {
        CupomModel cupom = cupomRepository.findByCodigoForUpdate(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Cupom inválido."));
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(cupom.getAtivo())
                || now.isBefore(cupom.getDataInicio())
                || now.isAfter(cupom.getDataFim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cupom inativo ou fora do período de vigência.");
        }
        if (subtotal.compareTo(cupom.getValorMinimoPedido()) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O subtotal não atingiu o valor mínimo do cupom.");
        }
        if (cupom.getLimiteUtilizacoes() != null
                && usoCupomRepository.countByCupomId(cupom.getId()) >= cupom.getLimiteUtilizacoes()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O limite de utilizações do cupom foi atingido.");
        }
        return cupom;
    }

    private BigDecimal calcularDesconto(CupomModel cupom, BigDecimal subtotal) {
        BigDecimal discount;
        if (cupom.getTipoDesconto().getNome().equals("PERCENTUAL")) {
            if (cupom.getValorDesconto().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Percentual de desconto inválido.");
            }
            discount = subtotal.multiply(cupom.getValorDesconto())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (cupom.getTipoDesconto().getNome().equals("VALOR_FIXO")) {
            discount = cupom.getValorDesconto().min(subtotal);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tipo de desconto inválido.");
        }
        return discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    StatusPedidoModel statusByName(String name) {
        return statusRepository.findByNome(name)
                .orElseThrow(() -> new IllegalStateException("Status de pedido ausente: " + name));
    }

    private void registrarHistorico(
            PedidoModel pedido,
            StatusPedidoModel status,
            UsuarioModel responsavel,
            String observacao
    ) {
        HistoricoStatusPedidoModel historico = new HistoricoStatusPedidoModel();
        historico.setPedido(pedido);
        historico.setStatusPedido(status);
        historico.setUsuarioResponsavel(responsavel);
        historico.setObservacao(observacao);
        historicoRepository.save(historico);
    }

    private PedidoResponse response(PedidoModel pedido) {
        List<ItemPedidoModel> items = itemPedidoRepository.findByPedidoId(pedido.getId());
        List<HistoricoStatusPedidoModel> history =
                historicoRepository.findByPedidoIdOrderByDataAlteracaoAsc(pedido.getId());
        return PedidoResponse.from(pedido, items, history);
    }
}
