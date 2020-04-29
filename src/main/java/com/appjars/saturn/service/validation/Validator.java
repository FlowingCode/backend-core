package com.appjars.saturn.service.validation;

import java.io.Serializable;

import com.appjars.saturn.model.Errors;

/**
 * 
 * Generic validation service
 * 
 * @author mlopez
 * 
 * @param <T>
 */
@FunctionalInterface
public interface Validator<T extends Serializable> {

	public void validate(T target, Errors errors);
	
}
