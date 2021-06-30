package sam.misfis.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sam.misfis.core.criteria.query.ResponseDTO;
import sam.misfis.core.form.Form;
import sam.misfis.core.criteria.query.ResponseParameter;
import sam.misfis.core.dto.WrapperResponse;
import sam.misfis.core.model.DefaultEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface BaseService<T extends DefaultEntity<ID>, ID extends Serializable> {

    List<T> saveAll(List<T> entity);

    T save(T entity);

    T saveAndFlush(T entity);

    T save(Form<T> form);

    T update(ID id, Form<T> form);

    List<WrapperResponse<T>> saveAll(List<T> entity, ResponseDTO<T> parameter);

    WrapperResponse<T> save(T entity, ResponseDTO<T> parameter);

    WrapperResponse<T> saveAndFlush(T entity, ResponseDTO<T> parameter);

    WrapperResponse<T> save(Form<T> form, ResponseDTO<T> parameter);

    WrapperResponse<T> update(ID id, Form<T> form, ResponseDTO<T> parameter);

    void deleteById(ID id);

    void delete(T entity);

    WrapperResponse<T> findById(ID id, ResponseDTO<T> parameter);

    Optional<T> findById(ID id);

    Page<WrapperResponse<T>> findAllWithPageable(ResponseParameter<T> responseParameter, Pageable pageable);

    List<WrapperResponse<T>> findAll(ResponseParameter<T> responseParameter);
}
