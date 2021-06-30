package sam.misfis.core.criteria.query;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;
import sam.misfis.core.criteria.CriteriaParser;
import sam.misfis.core.criteria.SearchSpecification;
import sam.misfis.core.criteria.SearchSpecificationBuilder;
import sam.misfis.core.dto.WrapperResponse;

@Getter
@Setter
public class ResponseDTOImpl<T> implements ResponseDTO<T> {

    private String include = "default";

    @Override
    public WrapperResponse<T> toDTO(T value) {
        return new WrapperResponse<>(include, value);
    }
}
