package sam.misfis.core.criteria.query;

import sam.misfis.core.dto.WrapperResponse;

public interface ResponseDTO<T> {
    WrapperResponse<T> toDTO(T value);
}
