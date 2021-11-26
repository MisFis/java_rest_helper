package sam.misfis.core.criteria;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class SearchSpecification<T> implements Specification<T> {
    private final QueryContext queryContext;
    private final SearchCriteria criteria;
    private final Map<String, Join> context;

    public SearchSpecification(SearchCriteria criteria) {
        this(criteria, null);
    }

    public SearchSpecification(SearchCriteria criteria, Map<String, Join> context) {
        this.criteria = criteria;
        queryContext = criteria.getQueryContext();
        this.context = context;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        Path<?> queryModel = root;
        Comparable value = castToParameter(criteria.getType());
        if (criteria.getJoin() != null && !criteria.getJoin().isEmpty()) queryModel = join(root, query, builder);
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

                if (value.equals("null")) return builder.isNull(queryModel.get(criteria.getKey()));
                return builder.equal(queryModel.get(criteria.getKey()), value);
            }
            case NEGATION: {
                if (value.equals("null")) return builder.isNotNull(queryModel.get(criteria.getKey()));
                return builder.notEqual(queryModel.get(criteria.getKey()), value);
            }
            case GREATER_THAN:
                return builder.greaterThan(queryModel.get(criteria.getKey()), value);
            case LESS_THAN:
                return builder.lessThan(queryModel.get(criteria.getKey()), value);
            case STARTS_WITH:
                return builder.like(builder.lower(queryModel.get(criteria.getKey())), (value + "%").toLowerCase(Locale.ROOT));
            case ENDS_WITH:
                return builder.like(builder.lower(queryModel.get(criteria.getKey())), ("%" + value).toLowerCase(Locale.ROOT));
            case CONTAINS:
                return builder.like(builder.lower(queryModel.get(criteria.getKey())), ("%" + value + "%").toLowerCase(Locale.ROOT));
            case IN: {
                return queryModel.get(criteria.getKey()).in(toArray(criteria.getType()));
            }

            default:
                return null;
        }
    }

    private Join join(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        Join joinedEntity = null;
        if (!criteria.getJoin().isEmpty()) {
            query.distinct(true);
        }
        From entity = root;
        Map<String, Join> joinContext = queryContext.getJoinContext();
        for (Object o : criteria.getJoin()) {
            String join = (String) o;
            if (joinContext.containsKey(join)) {
                return joinContext.get(join);
            }

            if (context != null) {
                joinedEntity = context.putIfAbsent(criteria.getKey(), entity.join(join));
                entity = joinedEntity;
            } else {
                Iterator<Join<T, ?>> iterator = entity.getJoins().iterator();
                while (iterator.hasNext()) {
                    Join<T, ?> next = iterator.next();
                    if (next.getAttribute().getName().equals(criteria.getJoin())) {
                        joinedEntity = next;
                        entity = joinedEntity;
                        break;
                    }
                }
                joinedEntity = entity.join(join, JoinType.LEFT);
                queryContext.putToContext(join, joinedEntity);
                entity = joinedEntity;

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


    private List<?> toArray(Class toCast) {
        String value = (String) this.criteria.getValue();
        if (toCast.isAssignableFrom(List.class)) return Arrays.stream(value.split(",")).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
