package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.model.IdentifiableObject;

public interface RemovalDao<T extends IdentifiableObject<K>, K extends Serializable> {
	
	void remove(T entity);

}
