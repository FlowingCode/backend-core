package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.model.Identifiable;

public interface CreationDao<T extends Identifiable<K>, K extends Serializable> {

	K save(T entity);
	
}
