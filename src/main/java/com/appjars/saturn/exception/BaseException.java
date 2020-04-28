package com.appjars.saturn.exception;

import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.model.Errors;

/**
 * Base Exception with support for I18N
 * 
 * @author mlopez
 * 
 */
public abstract class BaseException extends RuntimeException {

	private static final String DEFAULT_MESSAGE_KEY = "errors.service.default";

	private static final long serialVersionUID = 1L;

	private final String messageKey;

	private final Object[] messageKeyValues;

	private Errors errors;

	public BaseException() {
		super();
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
	}

	public BaseException(Throwable cause) {
		super(cause);
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
	}

	public BaseException(String messageKey) {
		super();
		this.messageKey = messageKey;
		messageKeyValues = null;
	}

	public BaseException(Throwable cause, String messageKey, Object... messageKeyValues) {
		super(cause);
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public BaseException(String messageKey, Object... messageKeyValues) {
		super();
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public <T extends ErrorDescription> BaseException(Throwable cause, T error) {
		super(cause);
		this.messageKey = error.getMessageKey();
		this.messageKeyValues = error.getMessageKeyValues();
	}

	public BaseException(Throwable cause, Errors errors) {
		super(cause);
		this.errors = errors;
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
	}

	public BaseException(Errors errors) {
		super();
		this.errors = errors;
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
	}

	public Object[] getMessageKeyValues() {
		return messageKeyValues;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Errors getErrors() {
		return errors;
	}

	public <T extends ErrorDescription> void addError(T error) {
		if (error != null) {
			this.errors.addError(error);
		}
	}
}
