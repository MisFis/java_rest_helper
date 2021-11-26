package sam.misfis.core.criteria;

import lombok.Getter;

import javax.persistence.criteria.Join;
import java.util.HashMap;
import java.util.Map;

public class QueryContext {
    @Getter
    Map<String, Join> joinContext = new HashMap<>();

    public void putToContext(String key, Join join) {
        if (!joinContext.containsKey(key))
            joinContext.put(key, join);
    }
}
