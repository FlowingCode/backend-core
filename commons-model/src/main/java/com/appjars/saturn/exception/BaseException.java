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
package com.appjars.saturn.exception;

import java.io.Serializable;
import java.util.List;

import com.appjars.saturn.model.ErrorDescription;

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

	private final Serializable[] messageKeyValues;

	private List<ErrorDescription> errors;

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
		super(messageKey);
		this.messageKey = messageKey;
		messageKeyValues = null;
	}

	public BaseException(Throwable cause, String messageKey, Serializable... messageKeyValues) {
		super(cause);
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public BaseException(String messageKey, Serializable... messageKeyValues) {
		super(messageKey);
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public BaseException(Throwable cause, ErrorDescription error) {
		super(error.getMessageKey(), cause);
		this.messageKey = error.getMessageKey();
		this.messageKeyValues = error.getMessageKeyValues();
	}

	public BaseException(Throwable cause, List<ErrorDescription> errors) {
		super(cause);
		this.errors = errors;
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
		fillSuppressed();
	}

	public BaseException(List<ErrorDescription> errors) {
		super();
		this.errors = errors;
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
		fillSuppressed();
	}

	private void fillSuppressed() {
		if (errors!=null) {
			for (ErrorDescription error : errors) {
				BaseException e = newInstance(error);
				e.setStackTrace(new StackTraceElement[0]);
				addSuppressed(e);
			}
		}
	}
	
	protected abstract BaseException newInstance(ErrorDescription error);

	public Object[] getMessageKeyValues() {
		return messageKeyValues;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public List<ErrorDescription> getErrors() {
		return errors;
	}

	public <T extends ErrorDescription> void addError(T error) {
		if (error != null) {
			this.errors.add(error);
		}
	}
}
