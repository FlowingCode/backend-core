package com.appjars.saturn.validation;


public interface ValidationKind {

	@SuppressWarnings("rawtypes")
	Class<? extends Validator> getValidatorType();
	
}
