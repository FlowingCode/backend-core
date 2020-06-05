/*-
 * #%L
 * Commons Backend - Data Access Layer Implementations
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
package com.appjars.saturn.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.appjars.saturn.dao.jpa.JpaDaoSupport;
import com.appjars.saturn.model.impl.Person;

public class PersonCrudDaoImpl
		implements JpaDaoSupport<Person, Person, Integer>, EntityConversionCrudDaoMixin<Person, Integer> {

	EntityManagerFactory entityManagerFactory;

	public PersonCrudDaoImpl(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

}
