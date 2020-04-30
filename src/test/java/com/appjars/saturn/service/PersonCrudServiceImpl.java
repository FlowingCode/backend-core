package com.appjars.saturn.service;

import java.util.Arrays;
import java.util.List;

import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.model.Person;
import com.appjars.saturn.service.dao.CrudDaoSupport;
import com.appjars.saturn.service.validation.CreationValidation;
import com.appjars.saturn.service.validation.RemovalValidation;
import com.appjars.saturn.service.validation.ValidationSupport;
import com.appjars.saturn.service.validation.Validator;

public class PersonCrudServiceImpl
		implements PersonCrudService, CrudDaoSupport<Person, Integer>, ValidationSupport<Person> {

	@Override
	public List<Validator<Person>> getValidators() {
		return Arrays.asList(CreationValidation.of((person, errors) -> {
			if (person.getName() == null)
				errors.addError("name.not.null");
		}), RemovalValidation.of((person, errors) -> {
			if (person.getId() == null)
				errors.addError("id.not.null");
		}));
	}

	@Override
	public CrudDao<Person, Integer> getCrudDao() {
		return null;
	}

}
