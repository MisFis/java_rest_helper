package sam.misfis.core.criteria;

import com.google.common.base.Joiner;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CriteriaParser {

    private static Map<String, Operator> ops;

    private static Pattern oldRegExp;

    static {
        Map<String, Operator> tempMap = new HashMap<>();
        tempMap.put("AND", Operator.AND);
        tempMap.put("OR", Operator.OR);
        tempMap.put("or", Operator.OR);
        tempMap.put("and", Operator.AND);

        ops = Collections.unmodifiableMap(tempMap);

        String operationSetExper = Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile("(\\p{Punct}?)([\\.\\w]+?)(" + operationSetExper + ")(\\p{Punct}?)([А-я\\w,: -]+)(\\p{Punct}?)");
        oldRegExp = pattern;
    }

    private enum Operator {
        OR(1), AND(2);
        final int precedence;

        Operator(int p) {
            precedence = p;
        }
    }


    public Deque<?> parse(String searchParam, Class clazz) {
        Deque<Object> output = new LinkedList<>();
        Deque<String> stack = new LinkedList<>();
        QueryContext queryContext = new QueryContext();

        String[] split = searchParam.split("\\s+");
        for (int i = 0; i < split.length; i++) {
            String token = split[i];
            if (ops.containsKey(token)) {
                while (!stack.isEmpty() && isHigerPrecedenceOperator(token, stack.peek())) {
                    output.push(stack.pop().equalsIgnoreCase(SearchOperation.OR_OPERATOR)
                            ? SearchOperation.OR_OPERATOR : SearchOperation.AND_OPERATOR);
                }
                stack.push(token.equalsIgnoreCase(SearchOperation.OR_OPERATOR)
                        ? SearchOperation.OR_OPERATOR : SearchOperation.AND_OPERATOR);

            } else if (token.equals(SearchOperation.LEFT_PARANTHESIS)) {
                stack.push(SearchOperation.LEFT_PARANTHESIS);
            } else if (token.equals(SearchOperation.RIGHT_PARANTHESIS)) {
                while (!stack.peek().equals(SearchOperation.LEFT_PARANTHESIS)) {
                    output.push(stack.pop());
                }
                stack.pop();
            } else {
                if (split.length > i + 1 && !isRequired(split[i + 1])) {
                    token += " " + split[i + 1];
                };
                Matcher matcher = oldRegExp.matcher(token);

                while (matcher.find()) {
                    output.push(new SearchCriteria(
                            matcher.group(2),
                            matcher.group(3),
                            matcher.group(4),
                            matcher.group(5),
                            clazz,
                            matcher.group(6),
                            queryContext));
                }
            }
        }

        while (!stack.isEmpty()) {
            output.push(stack.pop());
        }

        return output;
    }

    private static boolean isHigerPrecedenceOperator(String currOp, String prevOp) {
        return (ops.containsKey(prevOp) && ops.get(prevOp).precedence >= ops.get(currOp).precedence);
    }


    private boolean isRequired(String value) {
        return SearchOperation.OR_OPERATOR.equals(value) || SearchOperation.AND_OPERATOR.equals(value) || SearchOperation.LEFT_PARANTHESIS.equals(value)
                || SearchOperation.RIGHT_PARANTHESIS.equals(value);
    }
}
