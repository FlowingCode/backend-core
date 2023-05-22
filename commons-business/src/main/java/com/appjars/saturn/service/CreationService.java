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

/**
 * Specifies the contract of a generic service that is responsible for saving new entities to the database.
 *
 * @param <T> Represents the entity type
 * @param <K> Represents the key type
 *
 * @author mlopez
 */
public interface CreationService<T, K> {

    /**
     * Saves a new entity to a database.
     *
     * @param entity the created entity to be saved
     * @return an instance of key of entity this is saved to the database
     */
    K save(T entity);

}
