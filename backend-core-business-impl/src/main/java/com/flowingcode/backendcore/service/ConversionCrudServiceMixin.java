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
package com.flowingcode.backendcore.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import com.flowingcode.backendcore.dao.CreationDao;
import com.flowingcode.backendcore.dao.CrudDao;
import com.flowingcode.backendcore.dao.DeletionDao;
import com.flowingcode.backendcore.dao.QueryDao;
import com.flowingcode.backendcore.dao.UpdateDao;
import com.flowingcode.backendcore.model.ErrorDescription;
import com.flowingcode.backendcore.service.CrudService;
import com.flowingcode.backendcore.service.validation.DeletionValidator;
import com.flowingcode.backendcore.validation.DeletionValidationException;
import com.flowingcode.backendcore.validation.ValidationSupport;
import com.flowingcode.backendcore.validation.Validator;

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
		ConversionUpdateServiceMixin<B, P>, 
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
            List<Validator<B>> validators = ((ValidationSupport<B>) this).getValidators(DeletionValidator.class);
			List<ErrorDescription> errors = validators.stream().flatMap(val->val.validate(entity).stream()).collect(Collectors.toList());
			if (!errors.isEmpty()) {
				throw new DeletionValidationException(errors);
			}
			getDeletionDao().delete(persistentEntity);
		}
	}

}
