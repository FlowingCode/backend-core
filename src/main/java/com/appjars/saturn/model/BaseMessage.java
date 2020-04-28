package com.appjars.saturn.model;

import java.io.Serializable;

/**
 * A message representation
 * 
 * @author mlopez
 * 
 */
public abstract class BaseMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String field;

	protected String messageKey;

	protected Serializable[] messageKeyValues;

	protected String defaultMessage;

	public BaseMessage(String messageKey) {
		this.messageKey = messageKey;
	}

	public BaseMessage(String messageKey, Serializable[] messageKeyValues) {
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public BaseMessage(String field, String messageKey, Serializable... messageKeyValues) {
		this.field = field;
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public Object[] getMessageKeyValues() {
		return messageKeyValues;
	}

	public void setMessageKeyValues(Serializable[] messageKeyValues) {
		this.messageKeyValues = messageKeyValues;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	public void setDefaultMessage(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((messageKey == null) ? 0 : messageKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BaseMessage))
			return false;
		BaseMessage other = (BaseMessage) obj;
		if (messageKey == null) {
			if (other.messageKey != null)
				return false;
		} else if (!messageKey.equals(other.messageKey)) {
			return false;
		}
		return true;
	}

}
