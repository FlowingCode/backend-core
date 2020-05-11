package com.appjars.saturn.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.impl.Person;

public class PersonCrudServiceTest {

	@Test
	public void basicTests() {
		Person p = new Person();
		p.setName("Some Name");
		PersonCrudService personService = new PersonCrudServiceImpl();
		Integer id = personService.save(p, new Errors());
		Assertions.assertNotNull(id);
	}

}
