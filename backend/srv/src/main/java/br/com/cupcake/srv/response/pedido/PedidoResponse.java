package br.com.cupcake.srv.response.pedido;

import br.com.cupcake.srv.model.PedidoModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID idPedido,
        String numeroPedido,
        String status,
        LocalDateTime dataPedido,
        BigDecimal subtotal,
        BigDecimal valorDesconto,
        BigDecimal valorFrete,
        BigDecimal valorTotal,
        String destinatario,
        String cepEntrega,
        String logradouroEntrega,
        String numeroEntrega,
        String complementoEntrega,
        String bairroEntrega,
        String cidadeEntrega,
        String ufEntrega,
        String referenciaEntrega,
        List<ItemPedidoResponse> itens,
        List<HistoricoPedidoResponse> historico
) {
    public static PedidoResponse from(
            PedidoModel pedido,
            List<br.com.cupcake.srv.model.ItemPedidoModel> itens,
            List<br.com.cupcake.srv.model.HistoricoStatusPedidoModel> historicos
    ) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getStatusPedido().getNome(),
                pedido.getDataPedido(),
                pedido.getSubtotal(),
                pedido.getValorDesconto(),
                pedido.getValorFrete(),
                pedido.getValorTotal(),
                pedido.getDestinatario(),
                pedido.getCepEntrega(),
                pedido.getLogradouroEntrega(),
                pedido.getNumeroEntrega(),
                pedido.getComplementoEntrega(),
                pedido.getBairroEntrega(),
                pedido.getCidadeEntrega(),
                pedido.getUfEntrega(),
                pedido.getReferenciaEntrega(),
                itens.stream().map(ItemPedidoResponse::from).toList(),
                historicos.stream().map(HistoricoPedidoResponse::from).toList()
        );
    }
}
