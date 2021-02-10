/*-
 * #%L
 * Commons Backend - Model
 * %%
 * Copyright (C) 2020 Flowing Code
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
		super();
		this.messageKey = messageKey;
		messageKeyValues = null;
	}

	public BaseException(Throwable cause, String messageKey, Serializable... messageKeyValues) {
		super(cause);
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public BaseException(String messageKey, Serializable... messageKeyValues) {
		super();
		this.messageKey = messageKey;
		this.messageKeyValues = messageKeyValues;
	}

	public <T extends ErrorDescription> BaseException(Throwable cause, T error) {
		super(cause);
		this.messageKey = error.getMessageKey();
		this.messageKeyValues = error.getMessageKeyValues();
	}

	public BaseException(Throwable cause, List<ErrorDescription> errors) {
		super(cause);
		this.errors = errors;
		messageKey = DEFAULT_MESSAGE_KEY;
		messageKeyValues = null;
	}

	public BaseException(List<ErrorDescription> errors) {
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

	public List<ErrorDescription> getErrors() {
		return errors;
	}

	public <T extends ErrorDescription> void addError(T error) {
		if (error != null) {
			this.errors.add(error);
		}
	}
}
