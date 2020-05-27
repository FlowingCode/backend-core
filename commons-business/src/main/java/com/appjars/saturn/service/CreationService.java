package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;

/**
 * A special kind of service that allows entities creation
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CreationService<T extends Serializable, K extends Serializable> {

	K save(T entity, Errors errors);

}
