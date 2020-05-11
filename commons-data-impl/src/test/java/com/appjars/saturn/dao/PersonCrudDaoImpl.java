package com.appjars.saturn.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.appjars.saturn.dao.jpa.JpaDaoSupport;
import com.appjars.saturn.model.impl.Person;

public class PersonCrudDaoImpl implements CrudDao<Person, Integer>, JpaDaoSupport<Person, Integer> {

	EntityManagerFactory entityManagerFactory;

	public PersonCrudDaoImpl(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

}
