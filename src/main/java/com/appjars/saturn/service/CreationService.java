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
import com.appjars.saturn.service.validation.CreationValidation;
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
public interface CreationService<T extends IdentifiableObject<K>, K extends Serializable> extends CreationDaoSupport<T,K>, ValidationSupport<T> {
	
	@Transactional(value=TxType.REQUIRED,rollbackOn=Exception.class)
	default K saveOrUpdate(T entity, Errors errors) {
		Objects.requireNonNull(errors, "errors no puede ser nulo");
		List<Validator<T>> validators = getValidators().stream().filter(item->(item instanceof CreationValidation)).collect(Collectors.toList());
		validators.stream().forEach(validator->validator.validate(entity, errors));
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		return getDao().save(entity);
	}

}
