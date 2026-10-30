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
public class PedidoModel extends AuditoriaModel {
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
