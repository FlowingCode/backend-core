package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.Objects;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.exception.ValidationException;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.IdentifiableObject;

/**
 * A special kind of service that allows entities creation
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CreationService<T extends IdentifiableObject<K>, K extends Serializable> extends DaoBasedService<T,K> {
	
//	@Transactional(value=TxType.REQUIRED,rollbackOn=Exception.class)
//	default K save(T entity, Errors errors) {
//		Objects.requireNonNull(errors, "errors no puede ser nulo");
//		if (saveValidator != null) {
//			saveValidator.validate(entity, errors);
//		}
//		if (errors.hasErrors()) {
//			throw new ValidationException(errors);
//		}
//		return getDao().save(entity);
//	}
//
//	@Transactional(value=TxType.REQUIRED,rollbackOn=Exception.class)
//	default void saveOrUpdate(T entity, Errors errors) {
//		Objects.requireNonNull(errors, "errors no puede ser nulo");
//		if (saveOrUpdateValidator != null) {
//			saveOrUpdateValidator.validate(entity, errors);
//		}
//		if (errors.hasErrors()) {
//			throw new ValidationException(errors);
//		}
//		dao.saveOrUpdate(entity);
//	}

}
