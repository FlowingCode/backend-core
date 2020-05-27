package com.appjars.saturn.validation;

import com.appjars.saturn.exception.BaseException;
import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.model.Errors;

/**
 * Exception representing business rules validations fails
 * 
 * @author mlopez
 * 
 */
public class ValidationException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ValidationException() {
		super();
	}

	public ValidationException(Errors errors) {
		super(errors);
	}

	public ValidationException(String messageKey, Object... messageKeyValues) {
		super(messageKey, messageKeyValues);
	}

	public ValidationException(String messageKey) {
		super(messageKey);
	}

	public ValidationException(Throwable cause, Errors errors) {
		super(cause, errors);
	}

	public ValidationException(Throwable cause, String messageKey, Object... messageKeyValues) {
		super(cause, messageKey, messageKeyValues);
	}

	public <T extends ErrorDescription> ValidationException(Throwable cause, T error) {
		super(cause, error);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

}
