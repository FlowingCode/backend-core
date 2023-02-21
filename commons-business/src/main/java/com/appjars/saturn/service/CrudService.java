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
 * A special kind of service that allows entities CRUD operations
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CrudService<T, K>
		extends CreationService<T, K>, UpdateService<T>, DeletionService<T>, QueryService<T, K> {

	void deleteById(K id);

}
