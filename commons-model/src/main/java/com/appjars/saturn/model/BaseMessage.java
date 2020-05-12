package com.appjars.saturn.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * A message representation
 * 
 * @author mlopez
 * 
 */
@Getter
@Setter
public abstract class BaseMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String field;

	private String messageKey;

	private Serializable[] messageKeyValues;

	private String defaultMessage;

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
