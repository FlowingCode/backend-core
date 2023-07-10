/*-
 * #%L
 * Commons Backend - Model
 * %%
 * Copyright (C) 2020 - 2021 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.backendcore.model;

import java.io.Serializable;
import java.util.Objects;

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

	protected static final String FIELD_CANNOT_BE_NULL = "field cannot be null";

	protected static final String MESSAGE_KEY_CANNOT_BE_NULL = "messageKey cannot be null";

	private static final long serialVersionUID = 1L;

	private String field;

	private String messageKey;

	private Serializable[] messageKeyValues;

	protected BaseMessage(String messageKey) {
		Objects.requireNonNull(messageKey, MESSAGE_KEY_CANNOT_BE_NULL);
		this.messageKey = messageKey;
	}

	protected BaseMessage(String messageKey, Serializable[] messageKeyValues) {
		Objects.requireNonNull(messageKey, MESSAGE_KEY_CANNOT_BE_NULL);
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	protected BaseMessage(String field, String messageKey, Serializable... messageKeyValues) {
		Objects.requireNonNull(field, FIELD_CANNOT_BE_NULL);
		Objects.requireNonNull(messageKey, MESSAGE_KEY_CANNOT_BE_NULL);
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
