package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status_pagamento")
@Getter
@Setter
@NoArgsConstructor
public class StatusPagamentoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_status_pagamento")
    private java.util.UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String nome;

    @Column(length = 150)
    private String descricao;
}
