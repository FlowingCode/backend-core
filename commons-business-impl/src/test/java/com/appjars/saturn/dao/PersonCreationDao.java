package com.appjars.saturn.dao;

import com.appjars.saturn.model.Person;

public class PersonCreationDao implements CreationDao<Person,Integer> {

	@Override
	public Integer save(Person entity) {
		entity.setId(((int)Math.random() * 10000));
		return entity.getId();
	}

}
