package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.exception.ValidationException;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.IdentifiableObject;
import com.appjars.saturn.service.dao.RemovalDaoSupport;
import com.appjars.saturn.service.validation.RemovalValidation;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

public interface RemovalService<T extends IdentifiableObject<K>, K extends Serializable> {

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default void remove(T entity, Errors errors) {
		Objects.requireNonNull(errors, "errors cannot be null");
		if (this instanceof ValidationSupport) {
			List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators().stream()
					.filter(item -> (item instanceof RemovalValidation)).collect(Collectors.toList());
			validators.stream().forEach(validator -> validator.validate(entity, errors));
			if (errors.hasErrors()) {
				throw new ValidationException(errors);
			}
		}
		if (this instanceof RemovalDaoSupport) {
			((RemovalDaoSupport<T, K>) this).getRemovalDao().remove(entity);
		} else {
			throw new ClassCastException("Class implementing RemovalService must also implement RemovalDaoSupport");
		}
	}

}
