package com.appjars.saturn.service.validation;

import java.io.Serializable;

public interface RemovalValidation<T extends Serializable> extends Validator<T> {

	static <T extends Serializable> RemovalValidation<T> of(Validator<T> validator) {
		return validator::validate;
	}
	
}
