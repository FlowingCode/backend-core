package com.appjars.saturn.service.validation;

import java.io.Serializable;

public interface CreationValidation<T extends Serializable> extends Validator<T> {

	static <T extends Serializable> CreationValidation<T> of(Validator<T> validator) {
		return validator::validate;
	}
	
}
