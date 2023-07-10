package com.flowingcode.backendcore.validation;


public interface ValidationKind {

	@SuppressWarnings("rawtypes")
	Class<? extends Validator> getValidatorType();
	
}
