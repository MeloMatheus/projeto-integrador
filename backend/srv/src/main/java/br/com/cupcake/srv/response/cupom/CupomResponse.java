package br.com.cupcake.srv.response.cupom;

import br.com.cupcake.srv.model.CupomModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CupomResponse(
        UUID idCupom,
        String codigo,
        String descricao,
        String tipoDesconto,
        BigDecimal valorDesconto,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Long limiteUtilizacoes,
        BigDecimal valorMinimoPedido,
        boolean ativo
) {
    public static CupomResponse from(CupomModel cupom) {
        return new CupomResponse(
                cupom.getId(), cupom.getCodigo(), cupom.getDescricao(), cupom.getTipoDesconto().getNome(),
                cupom.getValorDesconto(), cupom.getDataInicio(), cupom.getDataFim(),
                cupom.getLimiteUtilizacoes(), cupom.getValorMinimoPedido(), cupom.getAtivo()
        );
    }
}
