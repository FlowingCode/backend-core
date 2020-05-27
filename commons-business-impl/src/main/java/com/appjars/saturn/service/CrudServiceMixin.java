package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.dao.DeletionDao;
import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.dao.UpdateDao;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.service.validation.DeletionValidator;
import com.appjars.saturn.validation.ValidationException;
import com.appjars.saturn.validation.ValidationSupport;
import com.appjars.saturn.validation.Validator;

/**
 * A special kind of service that allows entities CRUD operations
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CrudServiceMixin<T extends Serializable, K extends Serializable> extends CreationServiceMixin<T, K>,
		UpdateServiceMixin<T, K>, DeletionServiceMixin<T>, QueryServiceMixin<T, K>, CrudService<T, K> {

	default CreationDao<T, K> getCreationDao() {
		return getCrudDao();
	}

	default UpdateDao<T> getUpdateDao() {
		return getCrudDao();
	}

	default DeletionDao<T> getDeletionDao() {
		return getCrudDao();
	}

	default QueryDao<T, K> getQueryDao() {
		return getCrudDao();
	}

	CrudDao<T, K> getCrudDao();

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default void deleteById(K id, Errors errors) {
		Objects.requireNonNull(errors, "errors cannot be null");
		Optional<T> entity = getQueryDao().findById(id);

		entity.ifPresent(ent -> {
			if (this instanceof ValidationSupport) {
				List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators().stream()
						.filter(item -> (item instanceof DeletionValidator)).collect(Collectors.toList());
				validators.stream().forEach(validator -> validator.validate(ent, errors));
				if (errors.hasErrors()) {
					throw new ValidationException(errors);
				}
			}
			getDeletionDao().delete(ent);
		});
	}

}
