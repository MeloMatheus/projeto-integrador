package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uso_cupom", uniqueConstraints = @UniqueConstraint(name = "uq_uso_cupom_pedido", columnNames = "id_pedido"))
@Getter
@Setter
@NoArgsConstructor
public class UsoCupomModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_uso_cupom", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cupom", nullable = false)
    private CupomModel cupom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private PedidoModel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @Column(name = "data_utilizacao", nullable = false, updatable = false)
    private LocalDateTime dataUtilizacao;

    @Column(name = "valor_desconto_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDescontoAplicado;

    @PrePersist
    void prePersist() {
        dataUtilizacao = LocalDateTime.now();
    }
}
