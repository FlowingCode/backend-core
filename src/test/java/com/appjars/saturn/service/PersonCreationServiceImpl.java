package com.appjars.saturn.service;

import java.util.Arrays;
import java.util.List;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.dao.PersonCreationDao;
import com.appjars.saturn.model.Person;
import com.appjars.saturn.service.dao.CreationDaoSupport;
import com.appjars.saturn.service.validation.CreationValidation;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

public class PersonCreationServiceImpl
		implements PersonCreationService, CreationDaoSupport<Person, Integer>, ValidationSupport<Person> {

	@Override
	public List<Validator<Person>> getValidators() {
		return Arrays.asList(CreationValidation.of((person, errors) -> {
			if (person.getName() == null)
				errors.addError("name.not.null");
		}));
	}

	@Override
	public CreationDao<Person, Integer> getCreationDao() {
		return new PersonCreationDao();
	}

}
