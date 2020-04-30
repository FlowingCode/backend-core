package com.appjars.saturn.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.appjars.saturn.exception.ValidationException;
import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Person;

public class PersonCreationServiceTest {

	@Test
	public void basicTests() {
		Person p = new Person();
		PersonCreationServiceImpl personService = new PersonCreationServiceImpl();
		Assertions.assertThrows(ValidationException.class, () -> personService.save(p, new Errors()));
	}

}
