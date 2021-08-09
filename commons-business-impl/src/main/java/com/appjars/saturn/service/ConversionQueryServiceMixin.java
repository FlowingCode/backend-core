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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.model.QuerySpec;

/**
 * A special kind of service that allows entities querying
 * 
 * @author mlopez
 * @author jgodoy
 *
 * @param <B> The type of the business layer entity
 * @param <P> The type of the persistence layer entity
 * @param <K> The type of the entity identifier 
 */
public interface ConversionQueryServiceMixin<B, P, K> extends QueryService<B, K>, BusinessConversionSupport<B, P> {

	QueryDao<P, K> getQueryDao();

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default Optional<B> findById(K id) {
		return Optional.ofNullable(id).flatMap(getQueryDao()::findById).map(this::convertToBusiness);
	}

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default List<B> findAll() {
		return getQueryDao().findAll().stream().map(this::convertToBusiness).collect(Collectors.toList());
	}

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default List<B> filter(QuerySpec filter) {
		return getQueryDao().filter(filter).stream().map(this::convertToBusiness).collect(Collectors.toList());
	}

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default long count(QuerySpec filter) {
		return getQueryDao().count(filter);
	}

}
