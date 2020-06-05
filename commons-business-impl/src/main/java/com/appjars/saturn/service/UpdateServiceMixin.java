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
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.dao.UpdateDao;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.service.validation.UpdateValidator;
import com.appjars.saturn.validation.ValidationException;
import com.appjars.saturn.validation.ValidationSupport;
import com.appjars.saturn.validation.Validator;

/**
 * A special kind of service that allows entities update
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface UpdateServiceMixin<T extends Serializable, K extends Serializable> extends UpdateService<T, K> {

	UpdateDao<T> getUpdateDao();

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
