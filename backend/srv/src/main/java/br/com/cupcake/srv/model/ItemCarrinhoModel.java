package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "item_carrinho",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_item_carrinho_produto",
                columnNames = {"id_carrinho", "id_produto"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ItemCarrinhoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_item_carrinho", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrinho", nullable = false)
    private CarrinhoModel carrinho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private ProdutoModel produto;

    @Column(nullable = false)
    private Long quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @jakarta.persistence.PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }
}
