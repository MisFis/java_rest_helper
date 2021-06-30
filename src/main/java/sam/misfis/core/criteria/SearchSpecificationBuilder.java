package sam.misfis.core.criteria;

import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.function.Function;

public class SearchSpecificationBuilder<T> {
    private final List<SearchCriteria> params;

    public SearchSpecificationBuilder() {
        params = new ArrayList<>();
    }

    public final SearchSpecificationBuilder<T> with(final String key, final String operation, final Object value, final String prefix, final String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public SearchSpecificationBuilder<T> with(final String orPredicate, final String key, final String operation, final Object value, final String prefix, final String suffix) {
        SearchOperation op = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            if (op == SearchOperation.EQUALITY) { // the operation may be complex operation
                final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    op = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    op = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    op = SearchOperation.STARTS_WITH;
                }
            }
            params.add(new SearchCriteria(orPredicate, key, op, (Comparable) value));
        }
        return this;
    }

    public Specification<T> build(Deque<?> postFixedExprStack, Function<SearchCriteria, Specification<T>> converter) {

        Deque<Specification<T>> specStack = new LinkedList<>();

        Collections.reverse((List<?>) postFixedExprStack);

        while (!postFixedExprStack.isEmpty()) {
            Object mayBeOperand = postFixedExprStack.pop();

            if (!(mayBeOperand instanceof String)) {
                specStack.push(converter.apply((SearchCriteria) mayBeOperand));
            } else {
                Specification<T> operand1 = specStack.pop();
                Specification<T> operand2 = specStack.pop();
                if (mayBeOperand.equals(SearchOperation.AND_OPERATOR))
                    specStack.push(Specification.where(operand1)
                            .and(operand2));
                else if (mayBeOperand.equals(SearchOperation.OR_OPERATOR))
                    specStack.push(Specification.where(operand1)
                            .or(operand2));
            }

        }
        return specStack.pop();
    }

}