package br.com.cupcake.srv.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        long usuariosAtivos,
        long produtosAtivos,
        long cuponsAtivos,
        long pedidosPendentes,
        long totalPedidos,
        BigDecimal faturamentoConfirmado,
        String periodo,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal faturamentoRecebido,
        long pedidosNoPeriodo,
        BigDecimal ticketMedioRecebido,
        List<FaturamentoDia> faturamentoPorDia,
        List<PedidosPorStatus> pedidosPorStatus,
        List<PedidoRecente> ultimosPedidos
) {
    public record FaturamentoDia(LocalDate data, BigDecimal faturamento) {
    }

    public record PedidosPorStatus(String status, long quantidade) {
    }

    public record PedidoRecente(
            UUID idPedido,
            String numeroPedido,
            String status,
            LocalDateTime dataPedido,
            BigDecimal valorTotal
    ) {
    }
}
