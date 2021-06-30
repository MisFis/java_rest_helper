package sam.misfis.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

@MappedSuperclass
@Data
@EqualsAndHashCode(of = {}, callSuper = true)
public class VersionEntity<T extends Serializable> extends AuditEntity<T> {

    @Version
    private Long version;
}
