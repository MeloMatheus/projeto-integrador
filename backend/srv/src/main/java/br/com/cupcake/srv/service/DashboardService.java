package br.com.cupcake.srv.service;

import br.com.cupcake.srv.repository.CupomRepository;
import br.com.cupcake.srv.repository.PagamentoRepository;
import br.com.cupcake.srv.repository.PedidoRepository;
import br.com.cupcake.srv.repository.ProdutoRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import br.com.cupcake.srv.response.DashboardResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final int ULTIMOS_PEDIDOS_LIMITE = 10;

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomRepository cupomRepository;
    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;

    public DashboardService(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            CupomRepository cupomRepository,
            PedidoRepository pedidoRepository,
            PagamentoRepository pagamentoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.cupomRepository = cupomRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse carregar(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        PeriodoDashboard filtro = resolverPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime inicio = filtro.dataInicio().atStartOfDay();
        LocalDateTime fim = filtro.dataFim().plusDays(1).atStartOfDay();
        BigDecimal faturamentoRecebido = pagamentoRepository.totalAprovadoNoPeriodo(inicio, fim);
        long pedidosComPagamento = pagamentoRepository.contarPedidosComPagamentoAprovadoNoPeriodo(inicio, fim);
        BigDecimal ticketMedio = pedidosComPagamento == 0
                ? ZERO
                : faturamentoRecebido.divide(BigDecimal.valueOf(pedidosComPagamento), 2, RoundingMode.HALF_UP);

        return new DashboardResponse(
                usuarioRepository.countByAtivoTrue(),
                produtoRepository.countByDisponivelTrue(),
                cupomRepository.countByAtivoTrue(),
                pedidoRepository.countByStatusPedidoNome("PENDENTE_PAGAMENTO"),
                pedidoRepository.count(),
                pagamentoRepository.totalAprovado(),
                filtro.nome(),
                filtro.dataInicio(),
                filtro.dataFim(),
                faturamentoRecebido,
                pedidoRepository.countByDataPedidoGreaterThanEqualAndDataPedidoLessThan(inicio, fim),
                ticketMedio,
                faturamentoPorDia(filtro.dataInicio(), filtro.dataFim(), inicio, fim),
                pedidoRepository.contarPorStatusNoPeriodo(inicio, fim).stream()
                        .map(status -> new DashboardResponse.PedidosPorStatus(status.getStatus(), status.getQuantidade()))
                        .toList(),
                pedidoRepository.findByDataPedidoGreaterThanEqualAndDataPedidoLessThanOrderByDataPedidoDesc(
                                inicio, fim, PageRequest.of(0, ULTIMOS_PEDIDOS_LIMITE))
                        .stream()
                        .map(pedido -> new DashboardResponse.PedidoRecente(
                                pedido.getId(),
                                pedido.getNumeroPedido(),
                                pedido.getStatusPedido().getNome(),
                                pedido.getDataPedido(),
                                pedido.getValorTotal()
                        ))
                        .toList()
        );
    }

    private List<DashboardResponse.FaturamentoDia> faturamentoPorDia(
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        Map<LocalDate, BigDecimal> faturamentoPorData = new HashMap<>();
        pagamentoRepository.buscarFaturamentosAprovadosNoPeriodo(inicio, fim).forEach(pagamento -> {
            LocalDate data = pagamento.getDataConfirmacao().toLocalDate();
            faturamentoPorData.merge(data, pagamento.getValor(), BigDecimal::add);
        });

        List<DashboardResponse.FaturamentoDia> resultado = new ArrayList<>();
        for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
            resultado.add(new DashboardResponse.FaturamentoDia(
                    data,
                    faturamentoPorData.getOrDefault(data, ZERO)
            ));
        }
        return resultado;
    }

    private PeriodoDashboard resolverPeriodo(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        String nome = periodo == null ? "" : periodo.trim().toUpperCase(Locale.ROOT);
        LocalDate hoje = LocalDate.now();
        return switch (nome) {
            case "HOJE" -> new PeriodoDashboard(nome, hoje, hoje);
            case "ULTIMOS_7_DIAS" -> new PeriodoDashboard(nome, hoje.minusDays(6), hoje);
            case "ULTIMOS_30_DIAS" -> new PeriodoDashboard(nome, hoje.minusDays(29), hoje);
            case "PERSONALIZADO" -> {
                if (dataInicio == null || dataFim == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Os parâmetros dataInicio e dataFim são obrigatórios para o período personalizado."
                    );
                }
                if (dataInicio.isAfter(dataFim)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "A dataInicio deve ser anterior ou igual à dataFim."
                    );
                }
                yield new PeriodoDashboard(nome, dataInicio, dataFim);
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Período inválido. Use HOJE, ULTIMOS_7_DIAS, ULTIMOS_30_DIAS ou PERSONALIZADO."
            );
        };
    }

    private record PeriodoDashboard(String nome, LocalDate dataInicio, LocalDate dataFim) {
    }
}
