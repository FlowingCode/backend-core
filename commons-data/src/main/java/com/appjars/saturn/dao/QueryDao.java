package com.appjars.saturn.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.appjars.saturn.model.QuerySpec;

public interface QueryDao<T extends Serializable, K extends Serializable> {

	Optional<T> findById(K id);

	List<T> findAll();

	List<T> filter(QuerySpec<K> filter);

	long count(QuerySpec<K> filter);

}
