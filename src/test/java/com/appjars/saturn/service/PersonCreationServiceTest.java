package com.appjars.saturn.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Person;

public class PersonCreationServiceTest {

	@Test
	public void basicTests() {
		Person p = new Person();
		PersonCreationService personService = new PersonCreationService();
		Integer id = personService.saveOrUpdate(p, new Errors());
		Assertions.assertNotNull(id);
	}
	
	
}
