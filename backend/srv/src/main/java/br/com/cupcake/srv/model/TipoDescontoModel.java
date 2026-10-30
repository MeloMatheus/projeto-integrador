package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_desconto")
@Getter
@Setter
@NoArgsConstructor
public class TipoDescontoModel {
    @Id
    @Column(name = "id_tipo_desconto")
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String nome;
}
