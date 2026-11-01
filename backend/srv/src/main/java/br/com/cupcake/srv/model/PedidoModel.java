package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class PedidoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pedido", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_status_pedido", nullable = false)
    private StatusPedidoModel statusPedido;

    @Column(name = "numero_pedido", nullable = false, unique = true, length = 30)
    private String numeroPedido;

    @Column(name = "data_pedido", nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(name = "cep_entrega", nullable = false, length = 10)
    private String cepEntrega;

    @Column(name = "logradouro_entrega", nullable = false, length = 150)
    private String logradouroEntrega;

    @Column(name = "numero_entrega", nullable = false, length = 20)
    private String numeroEntrega;

    @Column(name = "complemento_entrega", length = 100)
    private String complementoEntrega;

    @Column(name = "bairro_entrega", nullable = false, length = 100)
    private String bairroEntrega;

    @Column(name = "cidade_entrega", nullable = false, length = 100)
    private String cidadeEntrega;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf_entrega", nullable = false, columnDefinition = "char(2)")
    private String ufEntrega;

    @Column(name = "referencia_entrega", length = 200)
    private String referenciaEntrega;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
    private List<ItemPedidoModel> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
    private List<PagamentoModel> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
    private List<HistoricoStatusPedidoModel> historicos = new ArrayList<>();

    @PrePersist
    void prePersist() {
        dataPedido = LocalDateTime.now();
    }
}
