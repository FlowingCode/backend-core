package com.appjars.saturn.service.validation;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.appjars.saturn.model.Errors;
import com.appjars.saturn.validation.Validator;

public interface DeletionValidator<T extends Serializable> extends Validator<T> {

	static <T extends Serializable> DeletionValidator<T> of(Validator<T> validator) {
		return validator::validate;
	}

	default CreationValidator<T> and(DeletionValidator<T> then) {
		return (t, errors) -> this.validate(t, errors) && then.validate(t, errors);
	}

	static <T extends Serializable> DeletionValidator<T> of(Predicate<T> predicate,
			BiConsumer<T, Errors> errorHandler) {
		return (t, errors) -> {
			if (predicate.test(t))
				return true;
			else {
				errorHandler.accept(t, errors);
				return false;
			}
		};
	}

}
