package br.com.cupcake.srv.response.pagamento;

import br.com.cupcake.srv.model.PagamentoModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID idPagamento,
        UUID idPedido,
        String status,
        String metodo,
        BigDecimal valor,
        String transacaoExterna,
        LocalDateTime dataCriacao,
        LocalDateTime dataConfirmacao
) {
    public static PagamentoResponse from(PagamentoModel pagamento) {
        return new PagamentoResponse(
                pagamento.getId(), pagamento.getPedido().getId(), pagamento.getStatusPagamento().getNome(),
                pagamento.getMetodo(), pagamento.getValor(), pagamento.getTransacaoExterna(),
                pagamento.getDataCriacao(), pagamento.getDataConfirmacao()
        );
    }
}
