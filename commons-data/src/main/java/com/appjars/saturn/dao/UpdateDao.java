package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.model.Identifiable;

public interface UpdateDao<T extends Identifiable<K>, K extends Serializable> {

	void saveOrUpdate(T entity);

}