package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.exception.ValidationException;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Identifiable;
import com.appjars.saturn.service.dao.HasDeletionDao;
import com.appjars.saturn.service.validation.DeletionValidator;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

/**
 * A special kind of service that allows entities deletion
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface DeletionService<T extends Identifiable<K>, K extends Serializable> {

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default void delete(T entity, Errors errors) {
		Objects.requireNonNull(errors, "errors cannot be null");
		if (this instanceof ValidationSupport) {
			List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators().stream()
					.filter(item -> (item instanceof DeletionValidator)).collect(Collectors.toList());
			validators.stream().forEach(validator -> validator.validate(entity, errors));
			if (errors.hasErrors()) {
				throw new ValidationException(errors);
			}
		}
		if (this instanceof HasDeletionDao) {
			((HasDeletionDao<T, K>) this).getDeletionDao().delete(entity);
		} else {
			throw new ClassCastException("Class implementing DeletionService must also implement HasDeletionDao");
		}
	}

}
