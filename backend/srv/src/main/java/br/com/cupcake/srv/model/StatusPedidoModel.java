package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status_pedido")
@Getter
@Setter
@NoArgsConstructor
public class StatusPedidoModel {
    @Id
    @Column(name = "id_status_pedido")
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nome;

    @Column(length = 150)
    private String descricao;
}
