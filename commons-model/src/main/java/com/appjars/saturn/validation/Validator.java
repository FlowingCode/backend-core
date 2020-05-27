package com.appjars.saturn.validation;

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

	public boolean validate(T target, Errors errors);

}
