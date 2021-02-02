/*-
 * #%L
 * Commons Backend - Data Access Interfaces
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

import java.util.List;
import java.util.Optional;

import com.appjars.saturn.model.QuerySpec;
import com.appjars.saturn.model.Person;

public class PersonCrudDao implements CrudDao<Person, Integer> {

	@Override
	public Integer save(Person entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Person entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Person> findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Person> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Person entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Person> filter(QuerySpec<Integer> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(QuerySpec<Integer> filter) {
		// TODO Auto-generated method stub
		return 0;
	}

}
