package sam.misfis.core.criteria;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
public class SearchSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;
    private final Map<String, Join> context;

    public SearchSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
        context = null;
    }

    public SearchSpecification(SearchCriteria criteria, Map<String, Join> context) {
        this.criteria = criteria;
        this.context = context;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        Path<?> queryModel = root;
        Comparable value = castToParameter(criteria.getType());
        if (criteria.getJoin() != null) queryModel = join(root, query, builder);
        switch (criteria.getOperation()) {
            case EQUALITY: {
                if (value instanceof Boolean) {
                    boolean val = (Boolean) value;
                    if (val) {
                        return builder.isTrue(queryModel.get(criteria.getKey()));
                    } else {
                        return builder.isFalse(queryModel.get(criteria.getKey()));
                    }
                }

                return builder.equal(queryModel.get(criteria.getKey()), value);
            }
            case NEGATION:
                return builder.notEqual(queryModel.get(criteria.getKey()), value);
            case GREATER_THAN:
                return builder.greaterThan(queryModel.get(criteria.getKey()), value);
            case LESS_THAN:
                return builder.lessThan(queryModel.get(criteria.getKey()), value);
            case LIKE:
                return builder.like(queryModel.get(criteria.getKey()), value.toString());
            case STARTS_WITH:
                return builder.like(builder.lower(queryModel.get(criteria.getKey())), (value + "%").toLowerCase(Locale.ROOT));
            case ENDS_WITH:
                return builder.like(builder.lower(queryModel.get(criteria.getKey())), ("%" + value).toLowerCase(Locale.ROOT));
            case CONTAINS:
                return builder.like(builder.lower(queryModel.get(criteria.getKey())), ("%" + value + "%").toLowerCase(Locale.ROOT));
            default:
                return null;
        }
    }

    private Join join(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        Join joinedEntity = null;
        if (context != null) {
            joinedEntity = context.putIfAbsent(criteria.getKey(), root.join(criteria.getJoin()));
        } else {
            Iterator<Join<T, ?>> iterator = root.getJoins().iterator();
            while (iterator.hasNext()) {
                Join<T, ?> next = iterator.next();
                if (next.getAttribute().getName().equals(criteria.getJoin())) {
                    joinedEntity = next;
                    break;
                }
            }
            if (joinedEntity == null) {
                joinedEntity = root.join(criteria.getJoin());
            }
        }

        return joinedEntity;
    }


    private Comparable castToParameter(Class toCast) {
        String value = (String) this.criteria.getValue();

        if (toCast == null) return value;
        if (toCast.isEnum()) return Enum.valueOf(toCast, value);
        if (toCast.isAssignableFrom(UUID.class)) return UUID.fromString(value);
        if (toCast.isAssignableFrom(Boolean.class)) return value.equals("true");
        if (toCast.isAssignableFrom(LocalDateTime.class)) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return LocalDateTime.parse(value, formatter);
        }
        if (toCast.isAssignableFrom(LocalDate.class)) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return LocalDate.parse(value, formatter);
        }
        return value;
    }
}
