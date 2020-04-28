package com.appjars.saturn.model;

import java.io.Serializable;

/**
 * Description of an information message
 * 
 * @author mlopez
 * 
 */
public class InfoDescription extends BaseMessage {

	private static final long serialVersionUID = 1L;

	public InfoDescription(String messageKey) {
		super(messageKey);
	}

	public InfoDescription(String messageKey, Serializable[] messageKeyValues) {
		super(messageKey, messageKeyValues);
	}

	public InfoDescription(String field, String messageKey, Serializable[] messageKeyValues) {
		super(field, messageKey, messageKeyValues);
	}

}
