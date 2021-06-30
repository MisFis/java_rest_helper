package sam.misfis.core.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static sam.misfis.core.utils.WrapperResponseUtils.getFieldInTarget;

@Data
@ToString
@EqualsAndHashCode
public class SearchCriteria<T extends Comparable> {
    private String join;
    private String key;
    private SearchOperation operation;
    private T value;
    private Class type;
    private boolean orPredicate;

    public SearchCriteria(final String orPredicate, final String key, final SearchOperation operation, final T value) {
        this.orPredicate = orPredicate != null && orPredicate.equals(SearchOperation.OR_PREDICATE_FLAG);
        setKey(key, null);
        this.operation = operation;
        this.value = value;
    }

    public SearchCriteria(String key, String operation, String prefix, T value, Class clazz, String suffix) {
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
        }
        setKey(key, clazz);
        this.operation = op;
        this.value = value;
    }

    private void setKey(String key, Class clazz) {
        Map<String, Field> fieldInTarget = getFieldInTarget(clazz);
        if (key.contains(".")) {
            String[] split = key.split("\\.");
            join = split[0];
            Field field = fieldInTarget.get(join);
            Map<String, Field> fieldKeys = getFieldInTarget(field.getType());
            this.key = split[1];
            this.type = fieldKeys.get(this.key).getType();
        } else {
            this.key = key;
            this.type = fieldInTarget.get(key).getType();
        }
    }
}
