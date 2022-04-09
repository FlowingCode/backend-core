/*-
 * #%L
 * Commons Backend - Business Implementations
 * %%
 * Copyright (C) 2020 - 2021 Flowing Code
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
import com.appjars.saturn.validation.DeletionValidationException;
import com.appjars.saturn.validation.ValidationException;
import com.appjars.saturn.validation.ValidationSupport;
import com.appjars.saturn.validation.Validator;

/**
 * A special kind of service that allows entities CRUD operations
 * 
 * @author mlopez
 * @author jgodoy
 *
 * @param <B> The type of the business layer entity
 * @param <P> The type of the persistence layer entity
 * @param <K> The type of the entity identifier 
 */
public interface ConversionCrudServiceMixin<B, P, K> 
	extends ConversionCreationServiceMixin<B, P, K>,
		ConversionUpdateServiceMixin<B, P, K>, 
		ConversionDeletionServiceMixin<B, P>, 
		ConversionQueryServiceMixin<B, P, K>,
		CrudService<B, K> {

	default CreationDao<P, K> getCreationDao() {
		return getCrudDao();
	}

	default UpdateDao<P> getUpdateDao() {
		return getCrudDao();
	}

	default DeletionDao<P> getDeletionDao() {
		return getCrudDao();
	}

	default QueryDao<P, K> getQueryDao() {
		return getCrudDao();
	}

	CrudDao<P, K> getCrudDao();

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default void deleteById(K id) {
		P persistentEntity = getQueryDao().findById(id).orElse(null);
		if (persistentEntity!=null && this instanceof ValidationSupport) {
			B entity = convertToBusiness(persistentEntity); 
			List<Validator<B>> validators = ((ValidationSupport<B>) this).getValidators().stream()
				.filter(item -> (item instanceof DeletionValidator)).collect(Collectors.toList());
			List<ErrorDescription> errors = validators.stream().flatMap(val->val.validate(entity).stream()).collect(Collectors.toList());
			if (!errors.isEmpty()) {
				throw new DeletionValidationException(errors);
			}
			getDeletionDao().delete(persistentEntity);
		}
	}

}
