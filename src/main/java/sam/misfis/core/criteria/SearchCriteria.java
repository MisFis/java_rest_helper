package sam.misfis.core.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static sam.misfis.core.utils.WrapperResponseUtils.getFieldInTarget;

@Data
@ToString
@EqualsAndHashCode
public class SearchCriteria<T extends Comparable> {
    @Getter
    private final QueryContext queryContext;
    private List<String> join = new ArrayList<>();
    private String key;
    private SearchOperation operation;
    private T value;
    private Class type;
    private boolean orPredicate;

    public SearchCriteria(String key, String operation, String prefix, T value, Class clazz, String suffix, QueryContext queryContext) {
        this.queryContext = queryContext;
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
            if (join == null) join = new ArrayList<>();
            Map<String, Field> fieldKeys = getFieldInTarget(clazz);
            for (int i = 0; i < split.length - 1; i++) {
                String joinValue = split[i];
                join.add(joinValue);
                Field field = fieldKeys.get(joinValue);

                if (field.getType().isAssignableFrom(List.class)) {
                    ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                    Class<?> genericListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
                    fieldKeys = getFieldInTarget(genericListClass);
                } else {
                    fieldKeys = getFieldInTarget(field.getType());
                }
            }

            this.key = split[split.length - 1];
            this.type = fieldKeys.get(this.key).getType();
        } else {
            this.key = key;
            this.type = fieldInTarget.get(key).getType();
        }
    }
}
