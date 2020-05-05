package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;
import com.appjars.saturn.model.Identifiable;

/**
 * A special kind of service that allows entities creation
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface CreationService<T extends Identifiable<K>, K extends Serializable> {

	K save(T entity, Errors errors);

}
