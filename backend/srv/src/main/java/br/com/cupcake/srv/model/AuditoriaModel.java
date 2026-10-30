package br.com.cupcake.srv.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditoriaModel {
    @CreatedBy
    @Column(name = "created_by", length = 180)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 180)
    private String updatedBy;
}
