package sam.misfis.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@Data
@EqualsAndHashCode
public class DefaultEntity<T extends Serializable> implements Serializable {

    @Id
    @GeneratedValue
    private T id;

    public DefaultEntity() {
    }

    public DefaultEntity(T id) {
        this.id = id;
    }
}
