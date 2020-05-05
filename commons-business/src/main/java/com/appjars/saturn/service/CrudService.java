package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Identifiable;

/**
 * A special kind of service that allows entities CRUD operations
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CrudService<T extends Identifiable<K>, K extends Serializable>
		extends CreationService<T, K>, UpdateService<T, K>, DeletionService<T, K>, QueryService<T, K> {

	void deleteById(K id, Errors errors);
}
