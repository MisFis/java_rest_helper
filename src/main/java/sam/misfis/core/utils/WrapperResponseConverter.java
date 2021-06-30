package sam.misfis.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import sam.misfis.core.criteria.query.ResponseDTO;
import sam.misfis.core.dto.PageJson;
import sam.misfis.core.dto.WrapperResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class WrapperResponseConverter {

    public static <T> Page<WrapperResponse<T>> toPage(Page<T> data, ResponseDTO<T> responseDTO) {
        List<WrapperResponse<T>> collect = data.getContent()
                .stream()
                .map(t -> toWrapper(responseDTO, t))
                .collect(Collectors.toList());
        return new PageJson<>(collect, data);
    }

    public static <T> List<WrapperResponse<T>> toList(List<T> data, ResponseDTO<T> responseDTO) {
        return data.stream()
                .map(t -> toWrapper(responseDTO, t))
                .collect(Collectors.toList());
    }


    protected <T> WrapperResponse<T> toWrapper(ResponseDTO<T> responseDTO, T value) {
        return responseDTO.toDTO(value);
    }
}
