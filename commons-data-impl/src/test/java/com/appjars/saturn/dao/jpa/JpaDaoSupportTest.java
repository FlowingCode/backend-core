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
package com.appjars.saturn.dao.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.appjars.saturn.dao.PersonCrudDaoImpl;
import com.appjars.saturn.model.QuerySpec;
import com.appjars.saturn.model.impl.Person;
import com.github.javafaker.Faker;

class JpaDaoSupportTest {

	private PersonCrudDaoImpl dao;
	private Person persistedPerson;

	@BeforeEach
	void setUp() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("person");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		dao = new PersonCrudDaoImpl(emf);
		Faker faker = Faker.instance();
		persistedPerson = new Person();
		persistedPerson.setName("John");
		persistedPerson.setLastName("Doe");
		persistedPerson.setBirthDay(faker.date().birthday());

		em.persist(persistedPerson);

		IntStream.range(0, 10).forEach(i -> {
			Person aPerson = new Person();
			aPerson.setName(faker.address().firstName());
			aPerson.setLastName(faker.address().lastName());
			aPerson.setBirthDay(faker.date().birthday());
			em.persist(aPerson);
		});
		em.getTransaction().commit();

	}

	@Test
	void testFindById() {
		Optional<Person> retrievedPerson = dao.findById(persistedPerson.getId());
		assertTrue(retrievedPerson.isPresent());
	}

	@Test
	void testFindAll() {
		long all = dao.findAll().size();
		assertEquals(11, all);
	}

	@Test
	void testFilter() {
		PersonFilter pf = new PersonFilter();
		pf.setExcludeIds(Arrays.asList(new Integer[] { persistedPerson.getId() }));
		long allMinusFirst = dao.count(pf);
		assertEquals(10, allMinusFirst);
	}

	@Test
	void testCount() {
		PersonFilter pf = new PersonFilter();
		pf.setExcludeIds(Arrays.asList(new Integer[] { persistedPerson.getId() }));
		long allMinusFirst = dao.count(pf);
		assertEquals(10, allMinusFirst);
	}

	@Test
	void testDelete() {
		fail("Not yet implemented");
	}

	@Test
	void testSaveOrUpdate() {
		fail("Not yet implemented");
	}

	@Test
	void testSave() {
		fail("Not yet implemented");
	}

	private static class PersonFilter extends QuerySpec<Integer> {

	}

}
