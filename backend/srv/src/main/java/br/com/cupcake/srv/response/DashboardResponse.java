package br.com.cupcake.srv.response;

import java.math.BigDecimal;

public record DashboardResponse(
        long usuariosAtivos,
        long produtosAtivos,
        long cuponsAtivos,
        long pedidosPendentes,
        long totalPedidos,
        BigDecimal faturamentoConfirmado
) {
}
