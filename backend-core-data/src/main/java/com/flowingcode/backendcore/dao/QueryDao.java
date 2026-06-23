/*-
 * #%L
 * Commons Backend - Data Access Interfaces
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
package com.flowingcode.backendcore.dao;

import java.util.List;
import java.util.Optional;

import com.flowingcode.backendcore.model.QuerySpec;
import com.flowingcode.backendcore.model.filter.BaseFilter;

public interface QueryDao<T, K> {

	Optional<T> findById(K id);

	List<T> findAll();

	/**
	 * @deprecated Use {@link #filter(BaseFilter)} with a {@link BaseFilter}
	 *             subclass.
	 */
	@Deprecated(since = "1.2.0", forRemoval = false)
	List<T> filter(QuerySpec filter);

	/**
	 * @deprecated Use {@link #filterWithSingleResult(BaseFilter)} with a
	 *             {@link BaseFilter} subclass.
	 */
	@Deprecated(since = "1.2.0", forRemoval = false)
	Optional<T> filterWithSingleResult(QuerySpec filter);

	/**
	 * @deprecated Use {@link #count(BaseFilter)} with a {@link BaseFilter}
	 *             subclass.
	 */
	@Deprecated(since = "1.2.0", forRemoval = false)
	long count(QuerySpec filter);

	/**
	 * Returns the entities that match the given {@code filter}. Annotations on
	 * the filter fields drive the generated JPA Criteria.
	 */
	List<T> filter(BaseFilter filter);

	/**
	 * Returns the single entity matching the given {@code filter}, if any.
	 *
	 * @throws IllegalStateException if more than one entity matches
	 */
	Optional<T> filterWithSingleResult(BaseFilter filter);

	/** Returns the number of entities matching the given {@code filter}. */
	long count(BaseFilter filter);

}
