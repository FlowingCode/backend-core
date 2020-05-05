package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.dao.UpdateDao;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Identifiable;
import com.appjars.saturn.service.validation.UpdateValidator;
import com.appjars.saturn.service.validation.ValidationException;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

/**
 * A special kind of service that allows entities update
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface UpdateServiceMixin<T extends Identifiable<K>, K extends Serializable> extends UpdateService<T, K> {

	UpdateDao<T, K> getUpdateDao();

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = { Exception.class, ValidationException.class })
	@Override
	default void saveOrUpdate(T entity, Errors errors) {
		Objects.requireNonNull(errors, "Errors cannot be null");
		if (this instanceof ValidationSupport) {
			List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators().stream()
					.filter(item -> (item instanceof UpdateValidator)).collect(Collectors.toList());
			validators.stream().forEach(validator -> validator.validate(entity, errors));
			if (errors.hasErrors()) {
				throw new ValidationException(errors);
			}
		}
		getUpdateDao().saveOrUpdate(entity);
	}

}
