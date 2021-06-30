package sam.misfis.core.criteria.query;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import sam.misfis.core.criteria.CriteriaParser;
import sam.misfis.core.criteria.SearchSpecification;
import sam.misfis.core.criteria.SearchSpecificationBuilder;

@AllArgsConstructor
public class SpecQueryImpl<T> implements SpecQuery<T> {

    private final String condition;

    @Override
    public Specification<T> toSpec(Class<T> clazz) {
        if (condition == null || condition.equals("")) return null;
        SearchSpecificationBuilder<T> builder = new SearchSpecificationBuilder<>();
        var parser = new CriteriaParser();
        return builder.build(parser.parse(condition, clazz), SearchSpecification::new);
    }
}
