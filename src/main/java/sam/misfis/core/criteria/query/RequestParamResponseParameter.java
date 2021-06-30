package sam.misfis.core.criteria.query;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;
import sam.misfis.core.dto.WrapperResponse;

@Getter
@Setter
public class RequestParamResponseParameter<T> implements ResponseParameter<T> {

    private String include = "default";

    /**
     * example: ( number:67890 AND organization.fullName:Орган* )
     * description: найти сущность содержащию номер 67890 И имя организации начинающиеся на Орган
     * sql WHERE number = 67890 AND o.fullName like 'Орган%'
     */
    private String condition = "";


    @Override
    public Specification<T> toSpec(Class<T> clazz) {
        return new SpecQueryImpl<T>(condition).toSpec(clazz);
    }

    @Override
    public WrapperResponse<T> toDTO(T value) {
        return new WrapperResponse<>(include, value);
    }

    public void appendCondition(String condition, String operator) {
        if (this.condition != null && !this.condition.isEmpty()) {
            this.condition += " " +  operator + " " + condition;
        } else {
            this.condition = condition;
        }
    }
}
