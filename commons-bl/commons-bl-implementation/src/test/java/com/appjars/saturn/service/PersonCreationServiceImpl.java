package com.appjars.saturn.service;

import java.util.Arrays;
import java.util.List;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.dao.PersonCreationDao;
import com.appjars.saturn.model.Person;
import com.appjars.saturn.service.dao.HasCreationDao;
import com.appjars.saturn.service.validation.CreationValidator;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

public class PersonCreationServiceImpl
		implements PersonCreationService, HasCreationDao<Person, Integer>, ValidationSupport<Person> {

	@Override
	public List<Validator<Person>> getValidators() {
		return Arrays.asList(CreationValidator.of(person -> person.getName() != null,
				(person, errors) -> errors.addError("name.not.null")));
	}

	@Override
	public CreationDao<Person, Integer> getCreationDao() {
		return new PersonCreationDao();
	}

}
