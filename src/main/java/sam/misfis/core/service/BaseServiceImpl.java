package sam.misfis.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import sam.misfis.core.criteria.query.ResponseDTO;
import sam.misfis.core.form.Form;
import sam.misfis.core.criteria.query.ResponseParameter;
import sam.misfis.core.dto.WrapperResponse;
import sam.misfis.core.model.DefaultEntity;
import sam.misfis.core.repository.BaseRepository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

import static sam.misfis.core.utils.WrapperResponseConverter.toList;
import static sam.misfis.core.utils.WrapperResponseConverter.toPage;

public class BaseServiceImpl<T extends DefaultEntity<ID>, ID extends Serializable> implements BaseService<T, ID> {

    protected final BaseRepository<T, ID> baseRepository;
    private final Class<T> modelClazz;

    public BaseServiceImpl(BaseRepository<T, ID> baseRepository) {
        this.baseRepository = baseRepository;
        modelClazz = (Class) (((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    @Override
    @Transactional
    public List<T> saveAll(List<T> entity) {
        return baseRepository.saveAll(entity);
    }

    @Override
    @Transactional
    public T save(T entity) {
        return baseRepository.save(entity);
    }

    @Override
    public T saveAndFlush(T entity) {
        return baseRepository.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public T save(Form<T> form) {
        return baseRepository.save(form.toEntity());
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        baseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        baseRepository.delete(entity);
    }

    @Override
    @Transactional
    public T update(ID id, Form<T> form) {
        T entity = form.toEntity();
        entity.setId(id);
        return baseRepository.save(entity);
    }

    @Override
    @Transactional
    public List<WrapperResponse<T>> saveAll(List<T> entity, ResponseDTO<T> parameter) {
       return toList(saveAll(entity), parameter);
    }

    @Override
    @Transactional
    public WrapperResponse<T> save(T entity, ResponseDTO<T> parameter) {
        T t = save(entity);
        return parameter.toDTO(t);
    }

    @Override
    @Transactional
    public WrapperResponse<T> saveAndFlush(T entity, ResponseDTO<T> parameter) {
        T t = saveAndFlush(entity);
        return parameter.toDTO(t);
    }

    @Override
    @Transactional
    public WrapperResponse<T> save(Form<T> form, ResponseDTO<T> parameter) {
        T t = save(form);
        return parameter.toDTO(t);
    }

    @Override
    @Transactional
    public WrapperResponse<T> update(ID id, Form<T> form, ResponseDTO<T> parameter) {
        T t = update(id, form);
        return parameter.toDTO(t);
    }

    @Override
    @Transactional(readOnly = true)
    public WrapperResponse<T> findById(ID id, ResponseDTO<T> parameter) {
        T entity = baseRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("Entity not found"));
        return parameter.toDTO(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return baseRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WrapperResponse<T>> findAllWithPageable(ResponseParameter<T> responseParameter, Pageable pageable) {
        return toPage(baseRepository.findAll(responseParameter.toSpec(modelClazz), pageable), responseParameter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WrapperResponse<T>> findAll(ResponseParameter<T> responseParameter) {
        return toList(baseRepository.findAll(responseParameter.toSpec(modelClazz)), responseParameter);
    }
}
