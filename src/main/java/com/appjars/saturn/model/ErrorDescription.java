package com.appjars.saturn.model;

import java.io.Serializable;

/**
 * Description of a given error
 * 
 * @author mlopez
 * 
 */
public class ErrorDescription extends BaseMessage {

	private static final long serialVersionUID = 1L;

	public ErrorDescription(String messageKey) {
		super(messageKey);
	}

	public ErrorDescription(String messageKey, Serializable[] messageKeyValues) {
		super(messageKey, messageKeyValues);
	}

	public ErrorDescription(String field, String messageKey, Serializable[] messageKeyValues) {
		super(field, messageKey, messageKeyValues);
	}

}
