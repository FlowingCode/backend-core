/*-
 * #%L
 * Commons Backend - Business Implementations
 * %%
 * Copyright (C) 2020 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.dao.DeletionDao;
import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.dao.UpdateDao;
import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.service.validation.DeletionValidator;
import com.appjars.saturn.validation.ValidationException;
import com.appjars.saturn.validation.ValidationSupport;
import com.appjars.saturn.validation.Validator;

/**
 * A special kind of service that allows entities CRUD operations
 * 
 * @author mlopez
 * @author jgodoy
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
	default void deleteById(K id) {
		getQueryDao().findById(id).ifPresent(entity -> {
			if (this instanceof ValidationSupport) {
				List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators().stream()
						.filter(item -> (item instanceof DeletionValidator)).collect(Collectors.toList());
				List<ErrorDescription> errors = validators.stream().flatMap(val->val.validate(entity).stream()).collect(Collectors.toList());
				if (!errors.isEmpty()) {
					throw new ValidationException(errors);
				}
			}
			getDeletionDao().delete(entity);
		});
	}

}
