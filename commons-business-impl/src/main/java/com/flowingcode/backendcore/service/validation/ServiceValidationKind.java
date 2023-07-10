package com.flowingcode.backendcore.service.validation;

import com.flowingcode.backendcore.validation.ValidationKind;
import com.flowingcode.backendcore.validation.Validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceValidationKind implements ValidationKind {

	CREATE(CreationValidator.class),
	UPDATE(UpdateValidator.class),
	DELETE(DeletionValidator.class);

	@SuppressWarnings("rawtypes")
	@Getter
	final Class<? extends Validator> validatorType;
	
}
