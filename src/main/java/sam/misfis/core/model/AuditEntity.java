package sam.misfis.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = {}, callSuper = true)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditEntity<T extends Serializable> extends DefaultEntity<T> {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    @JsonIgnore
    protected LocalDateTime createdWhen;

    @CreatedBy
    @JsonIgnore
    protected String createdBy;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false, updatable = false)
    @JsonIgnore
    protected LocalDateTime updatedWhen;

    @LastModifiedBy
    @JsonIgnore
    protected String updatedBy;

    public AuditEntity() {
    }

    public AuditEntity(T id) {
        super(id);
    }
}
