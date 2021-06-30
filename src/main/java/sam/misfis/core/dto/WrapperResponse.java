package sam.misfis.core.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import sam.misfis.core.utils.WrapperResponseUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class WrapperResponse<T> implements EntityPrototype<T>{

    private static final String DEFAULT_INCLUDE = "default";
    @JsonIgnore
    private final String include;

    @JsonIgnore
    private final T value;

    private Map<String, Object> properties;

    public WrapperResponse(String include, T value) {
        this.include = include;
        this.value = value;

        this.toProto();
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void toProto() {
        String includeField = this.include;
        if (includeField.equals(DEFAULT_INCLUDE)) {
            Set<String> allPrimitive = WrapperResponseUtils.getAllPrimitive(this.value);
            properties = WrapperResponseUtils.toMap(allPrimitive.toArray(new String[0]), this.value);
        } else {
            Set<String> fields = new HashSet<>();
            if (includeField.contains(DEFAULT_INCLUDE)) {
                fields = WrapperResponseUtils.getAllPrimitive(this.value);
                includeField = includeField.replace(DEFAULT_INCLUDE, "");
            }
            String[] split = includeField.split("[,;]");
            for (String field: split) {
                if (field != null && !field.trim().equals(""))
                    fields.add(field.trim());
            }
            properties = WrapperResponseUtils.toMap(fields.toArray(new String[0]), this.value);
        }
    }
}
