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
 * Specifies the contract of a generic deletion service.
 *
 * @param <T> the type of entity (e.g., {@code User})
 *
 * @author mlopez
 *
 */
public interface DeletionService<T> {

    /**
     * Deletes a given entity from the system.
     *
     * @param entity the entity to be deleted
     */
    void delete(T entity);

}
