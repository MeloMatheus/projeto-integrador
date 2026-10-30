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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
public class PagamentoModel extends AuditoriaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pagamento", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private PedidoModel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_status_pagamento", nullable = false)
    private StatusPagamentoModel statusPagamento;

    @Column(nullable = false, length = 40)
    private String metodo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "transacao_externa", length = 150, unique = true)
    private String transacaoExterna;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @PrePersist
    void prePersist() {
        dataCriacao = LocalDateTime.now();
    }
}
