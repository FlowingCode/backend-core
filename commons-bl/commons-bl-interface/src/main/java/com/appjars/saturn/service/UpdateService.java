package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Identifiable;

/**
 * A special kind of service that allows entities update
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface UpdateService<T extends Identifiable<K>, K extends Serializable> {

	void saveOrUpdate(T entity, Errors errors);

}
