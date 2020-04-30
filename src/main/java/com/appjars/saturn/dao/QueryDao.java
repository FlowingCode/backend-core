package com.appjars.saturn.dao;

import java.io.Serializable;
import java.util.List;

import com.appjars.saturn.model.IdentifiableObject;

public interface QueryDao<T extends IdentifiableObject<K>, K extends Serializable> {
	
	T findById(K id);
	
	List<T> findAll();

}
