package com.appjars.saturn.dao;

import java.util.List;
import java.util.Optional;

import com.appjars.saturn.model.BaseFilter;
import com.appjars.saturn.model.Person;

public class PersonCrudDao implements CrudDao<Person, Integer> {

	@Override
	public Integer save(Person entity) {
		return 1;
	}

	@Override
	public void delete(Person entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Person> findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Person> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveOrUpdate(Person entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Person> filter(BaseFilter<Integer> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(BaseFilter<Integer> filter) {
		// TODO Auto-generated method stub
		return 0;
	}

}
