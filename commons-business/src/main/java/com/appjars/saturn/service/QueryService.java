/*-
 * #%L
 * Commons Backend - Business Interfaces
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

import com.appjars.saturn.model.QuerySpec;

/**
 * Specifies the contract of a generic service that handles querying operations on entities.
 *
 * @param <T> the type of the entity to be queried
 * @param <K> the type of the entity's identifying key
 *
 * @author mlopez
 */
public interface QueryService<T, K> {

  /**
   * Find an entity by its identifying key.
   *
   * @param id the entity's identifying key
   * @return an {@code Optional} instance containing the entity if it exists; otherwise empty
   */
  Optional<T> findById(K id);

  /**
   * Find all entities.
   *
   * @return a {@code List} containing all entities in the system
   */
  List<T> findAll();

  /**
   * Apply filtering to entities in the system based on a certain query specification.
   *
   * @param filter the query specification used for filtering
   * @return a {@code List} containing all entities matching the query specification
   */
  List<T> filter(QuerySpec filter);

  /**
   * Count the number of entities that match a certain query specification.
   *
   * @param filter the query specification used for filtering
   * @return the number of entities that match the query specification
   */
  long count(QuerySpec filter);

}
