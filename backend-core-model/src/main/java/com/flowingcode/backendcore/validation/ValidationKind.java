package com.flowingcode.backendcore.validation;


/**
 * Defines the type of validator used for service validation steps.
 *
 * @author mlopez
 */
public interface ValidationKind {

	@SuppressWarnings("rawtypes")
	Class<? extends Validator> getValidatorType();
	
}
