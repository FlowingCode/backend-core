package com.appjars.saturn.service;

import com.appjars.saturn.model.Errors;

/**
 * 
 * Generic validator object
 * 
 * @author mlopez
 * 
 * @param <T>
 */
public interface Validator<T> {

	public void validate(T target, Errors errors);
}
