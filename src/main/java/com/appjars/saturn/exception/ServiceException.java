package com.appjars.saturn.exception;

import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.model.Errors;

/**
 * Exception thrown at Service Layer
 *
 * @author mlopez
 *
 */
public class ServiceException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ServiceException() {
		super();
	}

	public ServiceException(Errors errors) {
		super(errors);
	}

	public ServiceException(String messageKey, Object... messageKeyValues) {
		super(messageKey, messageKeyValues);
	}

	public ServiceException(String messageKey) {
		super(messageKey);
	}

	public ServiceException(Throwable cause, Errors errors) {
		super(cause, errors);
}

	public ServiceException(Throwable cause, String messageKey, Object... messageKeyValues) {
		super(cause, messageKey, messageKeyValues);
	}

	public <T extends ErrorDescription> ServiceException(Throwable cause, T error) {
		super(cause, error);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

}
