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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
public class EnderecoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_endereco", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @Column(length = 50)
    private String identificacao;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 10)
    private String cep;

    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, columnDefinition = "char(2)")
    private String uf;

    @Column(length = 200)
    private String referencia;

    @Column(nullable = false)
    private Boolean principal = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }
}
