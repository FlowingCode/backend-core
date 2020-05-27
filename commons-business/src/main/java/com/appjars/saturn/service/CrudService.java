package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;

/**
 * A special kind of service that allows entities CRUD operations
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CrudService<T extends Serializable, K extends Serializable>
		extends CreationService<T, K>, UpdateService<T, K>, DeletionService<T>, QueryService<T, K> {

	void deleteById(K id, Errors errors);

}
