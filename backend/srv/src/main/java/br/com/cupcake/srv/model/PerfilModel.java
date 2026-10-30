package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfil")
@Getter
@Setter
@NoArgsConstructor
public class PerfilModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_perfil", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String nome;

    @OneToMany(mappedBy = "perfil", fetch = jakarta.persistence.FetchType.LAZY)
    private List<UsuarioModel> usuarios = new ArrayList<>();
}
