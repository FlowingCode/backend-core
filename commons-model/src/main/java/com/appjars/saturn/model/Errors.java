package com.appjars.saturn.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An Error representation
 * 
 * @author mlopez
 * 
 */
public class Errors implements Serializable {

	private static final String FIELD_CANNOT_BE_NULL = "field cannot be null";

	private static final String MESSAGE_KEY_CANNOT_BE_NULL = "messageKey cannot be null";

	private static final long serialVersionUID = 1L;

	private List<ErrorDescription> errorsList = new ArrayList<>();

	public Errors() {
	}

	public Errors(List<ErrorDescription> errors) {
		this.errorsList = errors;
	}

	public Errors addError(String messageKey) {
		Objects.requireNonNull(messageKey, MESSAGE_KEY_CANNOT_BE_NULL);
		return this.addError(new ErrorDescription(messageKey));
	}

	public Errors addError(String messageKey, Serializable[] messageKeyValues) {
		Objects.requireNonNull(messageKey, MESSAGE_KEY_CANNOT_BE_NULL);
		return this.addError(new ErrorDescription(messageKey, messageKeyValues));
	}

	public Errors addError(String field, String messageKey, Serializable... messageKeyValues) {
		Objects.requireNonNull(field, FIELD_CANNOT_BE_NULL);
		Objects.requireNonNull(messageKey, MESSAGE_KEY_CANNOT_BE_NULL);
		return this.addError(new ErrorDescription(field, messageKey, messageKeyValues));
	}

	public Errors addError(ErrorDescription error) {
		if (error != null) {
			this.errorsList.add(error);
		}
		return this;
	}

	public boolean hasErrors() {
		return !errorsList.isEmpty();
	}

	public List<ErrorDescription> getAllErrors() {
		return errorsList;
	}

}
