package com.appjars.saturn.service;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.dao.PersonCreationDao;
import com.appjars.saturn.model.Person;

public class PersonCreationService implements CreationService<Person,Integer> {

	@Override
	public CreationDao<Person, Integer> getDao() {
		return new PersonCreationDao();
	}

}
