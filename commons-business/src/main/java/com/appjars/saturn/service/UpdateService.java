package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;

/**
 * A special kind of service that allows entities update
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface UpdateService<T extends Serializable, K extends Serializable> {

	void saveOrUpdate(T entity, Errors errors);

}
