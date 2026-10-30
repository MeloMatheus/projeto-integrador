package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Column(name = "id_status_pagamento")
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nome;
}
