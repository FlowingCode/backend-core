package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.appjars.saturn.model.BaseFilter;
import com.appjars.saturn.model.Identifiable;

/**
 * A special kind of service that allows entities querying
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface QueryService<T extends Identifiable<K>, K extends Serializable> {

	Optional<T> findById(K id);

	List<T> findAll();

	List<T> filter(BaseFilter<K> filter);

	long count(BaseFilter<K> filter);

}
