package br.com.cupcake.srv.response.pedido;

import br.com.cupcake.srv.model.HistoricoStatusPedidoModel;

import java.time.LocalDateTime;

public record HistoricoPedidoResponse(String status, String responsavel, LocalDateTime dataAlteracao, String observacao) {
    public static HistoricoPedidoResponse from(HistoricoStatusPedidoModel historico) {
        return new HistoricoPedidoResponse(
                historico.getStatusPedido().getNome(),
                historico.getUsuarioResponsavel() == null ? null : historico.getUsuarioResponsavel().getName(),
                historico.getDataAlteracao(),
                historico.getObservacao()
        );
    }
}
