package sam.misfis.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import sam.misfis.core.model.DefaultEntity;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseRepository<T extends DefaultEntity<ID>, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
