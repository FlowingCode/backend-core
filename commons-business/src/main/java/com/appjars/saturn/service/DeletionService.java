package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;

/**
 * A special kind of service that allows entities deletion
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface DeletionService<T extends Serializable> {

	void delete(T entity, Errors errors);

}
