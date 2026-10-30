package br.com.cupcake.srv.response.produto;

import br.com.cupcake.srv.model.ProdutoModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoResponse(
        UUID idProduto,
        String nome,
        String descricao,
        String imagemUrl,
        BigDecimal preco,
        boolean disponivel,
        LocalDateTime criadoEm
) {
    public static ProdutoResponse from(ProdutoModel produto) {
        return new ProdutoResponse(
                produto.getId(), produto.getNome(), produto.getDescricao(), produto.getImagemUrl(),
                produto.getPreco(), produto.getDisponivel(), produto.getCriadoEm()
        );
    }
}
