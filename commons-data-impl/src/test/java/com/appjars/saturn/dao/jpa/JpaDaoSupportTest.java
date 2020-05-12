package com.appjars.saturn.dao.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
		pf.setExcludeIds(new Integer[] { persistedPerson.getId() });
		long allMinusFirst = dao.count(pf);
		assertEquals(10, allMinusFirst);
	}

	@Test
	void testCount() {
		PersonFilter pf = new PersonFilter();
		pf.setExcludeIds(new Integer[] { persistedPerson.getId() });
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
