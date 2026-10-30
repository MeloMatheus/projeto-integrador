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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cupom")
@Getter
@Setter
@NoArgsConstructor
public class CupomModel extends AuditoriaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_cupom", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_desconto", nullable = false)
    private TipoDescontoModel tipoDesconto;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(length = 255)
    private String descricao;

    @Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(name = "limite_utilizacoes")
    private Integer limiteUtilizacoes;

    @Column(name = "valor_minimo_pedido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMinimoPedido = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "cupom", fetch = FetchType.LAZY)
    private List<UsoCupomModel> usos = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void normalizeCode() {
        codigo = codigo.trim().toUpperCase(Locale.ROOT);
    }
}
