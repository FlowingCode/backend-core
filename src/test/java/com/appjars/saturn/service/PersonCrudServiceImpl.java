package com.appjars.saturn.service;

import java.util.Arrays;
import java.util.List;

import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.model.Person;
import com.appjars.saturn.service.dao.HasCrudDao;
import com.appjars.saturn.service.validation.CreationValidator;
import com.appjars.saturn.service.validation.DeletionValidator;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

public class PersonCrudServiceImpl
		implements PersonCrudService, HasCrudDao<Person, Integer>, ValidationSupport<Person> {

	@Override
	public List<Validator<Person>> getValidators() {
		return Arrays.asList(
				CreationValidator.of(person -> person.getName() != null,
						(person, errors) -> errors.addError("name.not.null")),
				DeletionValidator.of(person -> person.getName() != null,
						(person, errors) -> errors.addError("name.not.null")));
	}

	@Override
	public CrudDao<Person, Integer> getCrudDao() {
		return null;
	}

}
