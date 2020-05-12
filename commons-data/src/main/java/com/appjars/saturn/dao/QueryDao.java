package com.appjars.saturn.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.appjars.saturn.model.QuerySpec;
import com.appjars.saturn.model.Identifiable;

public interface QueryDao<T extends Identifiable<K>, K extends Serializable> {

	Optional<T> findById(K id);

	List<T> findAll();

	List<T> filter(QuerySpec<K> filter);

	long count(QuerySpec<K> filter);

}
