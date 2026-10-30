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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_status_pedido")
@Getter
@Setter
@NoArgsConstructor
public class HistoricoStatusPedidoModel extends AuditoriaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_historico", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private PedidoModel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_status_pedido", nullable = false)
    private StatusPedidoModel statusPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsavel")
    private UsuarioModel usuarioResponsavel;

    @Column(name = "data_alteracao", nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;

    @Column(length = 500)
    private String observacao;

    @PrePersist
    void prePersist() {
        dataAlteracao = LocalDateTime.now();
    }
}
