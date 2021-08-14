package sam.misfis.core.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageJson<T> extends PageImpl<T> implements Page<T> {

    public PageJson(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PageJson(List<T> content) {
        super(content);
    }

    public PageJson(List<T> content, Page old) {
        super(content, old.getPageable(), old.getTotalElements());
    }
}
