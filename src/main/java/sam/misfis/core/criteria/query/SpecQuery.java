package sam.misfis.core.criteria.query;

import org.springframework.data.jpa.domain.Specification;

public interface SpecQuery<T> {
    Specification<T> toSpec(Class<T> clazz);
}
