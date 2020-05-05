package com.appjars.saturn.dao;

import java.io.Serializable;
import java.util.List;

import com.appjars.saturn.model.BaseFilter;
import com.appjars.saturn.model.Identifiable;

public interface QueryDao<T extends Identifiable<K>, K extends Serializable> {

	T findById(K id);

	List<T> findAll();

	List<T> filter(BaseFilter<K> filter);

	long count(BaseFilter<K> filter);

}
