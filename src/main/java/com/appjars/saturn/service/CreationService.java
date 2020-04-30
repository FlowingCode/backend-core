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
import com.appjars.saturn.service.dao.CreationDaoSupport;
import com.appjars.saturn.service.validation.CreationValidator;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

/**
 * A special kind of service that allows entities creation
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CreationService<T extends IdentifiableObject<K>, K extends Serializable> {

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default K saveOrUpdate(T entity, Errors errors) throws ValidationException {
		Objects.requireNonNull(errors, "Errors cannot be null");
		if (this instanceof ValidationSupport) {
			List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators().stream()
					.filter(item -> (item instanceof CreationValidator)).collect(Collectors.toList());
			validators.stream().forEach(validator -> validator.validate(entity, errors));
			if (errors.hasErrors()) {
				throw new ValidationException(errors);
			}
		}
		if (this instanceof CreationDaoSupport) {
			return ((CreationDaoSupport<T, K>) this).getCreationDao().save(entity);
		} else {
			throw new ClassCastException("Class implementing CreationService must also implement CreationDaoSupport");
		}
	}

}
