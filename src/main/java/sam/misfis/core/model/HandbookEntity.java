package sam.misfis.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@Data
@EqualsAndHashCode(of = {"key"}, callSuper = true)
public class HandbookEntity<T extends Serializable> extends AuditEntity<T> {

    private String name;

    private String key;

    public HandbookEntity() {
    }

    public HandbookEntity(T id) {
        super(id);
    }
}
