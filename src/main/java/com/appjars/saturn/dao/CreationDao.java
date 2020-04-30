package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.model.IdentifiableObject;

public interface CreationDao<T extends IdentifiableObject<K>, K extends Serializable> {

	K save(T entity);
	
}
