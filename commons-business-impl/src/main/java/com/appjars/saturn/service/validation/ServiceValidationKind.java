package com.appjars.saturn.service.validation;

import com.appjars.saturn.validation.ValidationKind;
import com.appjars.saturn.validation.Validator;

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
