package com.flowingcode.backendcore.service;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import com.flowingcode.backendcore.model.ErrorDescription;
import com.flowingcode.backendcore.model.QuerySpec;
import com.flowingcode.backendcore.service.CrudService;
import com.flowingcode.backendcore.service.validation.CreationValidator;
import com.flowingcode.backendcore.service.validation.DeletionValidator;
import com.flowingcode.backendcore.service.validation.UpdateValidator;
import com.flowingcode.backendcore.validation.CreationValidationException;
import com.flowingcode.backendcore.validation.DeletionValidationException;
import com.flowingcode.backendcore.validation.UpdateValidationException;
import com.flowingcode.backendcore.validation.ValidationException;
import com.flowingcode.backendcore.validation.ValidationSupport;
import com.flowingcode.backendcore.validation.Validator;

public abstract class JpaCrudService<T, K> implements CrudService<T, K> {

	protected abstract CrudRepository<T, K> getCrudRepository();

	protected abstract JpaSpecificationExecutor<T> getExecutor();

	protected Specification<T> buildSpecification(QuerySpec spec) {
		return ConstraintSpecification.buildSpecification(spec);
	}

	/**
	 * Default implementation for obtaining an id from an entity by calling method getId() by reflection
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected K getId(T entity) {
		K id;
		try {
			Method m = entity.getClass().getMethod("getId");
			id = (K) m.invoke(entity);
		} catch (Exception e) {
            throw new UndeclaredThrowableException(e, String.format(
                "Problem when trying to obtain id of entity of type %s by assuming that its name is 'id'",
                entity.getClass().getName()));
		}
		return id;
	}

	private Sort buildSort(QuerySpec filter) {
		if (filter.getOrders()==null || filter.getOrders().isEmpty()) {
			return Sort.unsorted();
		} else {
			return Sort.by(filter.getOrders().entrySet().stream().map(e->{
				switch (e.getValue()) {
				case ASC:
					return Sort.Order.asc(e.getKey());
				case DESC:
					return Sort.Order.desc(e.getKey());
				}
				throw new AssertionError();
			}).collect(Collectors.toList()));
		}
	}

	private Pageable buildPageable(QuerySpec filter) {
		if (filter.getMaxResult()==null) {
			throw new IllegalArgumentException("QuerySpec is not pageable");
		}

		int firstResult = Optional.ofNullable(filter.getFirstResult()).orElse(0);
		int firstPage = firstResult/filter.getMaxResult();
		if (firstResult%filter.getMaxResult() !=0) {
			throw new IllegalArgumentException("QuerySpec is not pageable");
		}

		Sort sort = buildSort(filter);
		return PageRequest.of(firstPage, filter.getMaxResult(), sort);
	}

	@Override
	public Optional<T> findById(K id) {
		return getCrudRepository().findById(id);
	}

	@Override
	public List<T> findAll() {
		return Streamable.of(getCrudRepository().findAll()).toList();
	}

	@Override
	public List<T> filter(QuerySpec filter) {
		if (filter.getFirstResult()==null  && filter.getMaxResult()==null) {
			return getExecutor().findAll(buildSpecification(filter), buildSort(filter));
		} else if (filter.getMaxResult().equals(0)) {
			return Collections.emptyList();
		} else {
			return getExecutor().findAll(buildSpecification(filter), buildPageable(filter)).toList();
		}
	}

	@Override
	public long count(QuerySpec filter) {
		return getExecutor().count(buildSpecification(filter));
	}

	private List<Validator<T>> getValidators(@SuppressWarnings("rawtypes") Class<? extends Validator> validatorType) {
		if (this instanceof ValidationSupport) {
			@SuppressWarnings("unchecked")
			List<Validator<T>> validators = ((ValidationSupport<T>)this).getValidators(validatorType);
			return validators;
		} else {
			return Collections.emptyList();
		}
	}

	private void validate(@SuppressWarnings("rawtypes") Class<? extends Validator> validatorType, T entity, Function<List<ErrorDescription>,ValidationException> newException) {
		List<Validator<T>> validators = getValidators(validatorType);
		if (!validators.isEmpty()) {
			List<ErrorDescription> errors = validators.stream().flatMap(val->val.validate(entity).stream()).collect(Collectors.toList());
			if (!errors.isEmpty()) {
				throw newException.apply(errors);
			}
		}
	}

	@Override
	public K save(T entity) {
		validate(CreationValidator.class, entity, CreationValidationException::new);
		return getId(getCrudRepository().save(entity));
	}

	@Override
	public void update(T entity) {
		validate(UpdateValidator.class, entity, UpdateValidationException::new);
		getCrudRepository().save(entity);
	}

	@Override
	public void delete(T entity) {
		validate(DeletionValidator.class, entity, DeletionValidationException::new);
		getCrudRepository().delete(entity);
	}

	@Override
	public void deleteById(K id) {
		if (getValidators(DeletionValidator.class).isEmpty()) {
			getCrudRepository().deleteById(id);
		} else {
			findById(id).ifPresent(entity->{
				validate(DeletionValidator.class, entity, DeletionValidationException::new);
				getCrudRepository().delete(entity);
			});
		}
	}

}
