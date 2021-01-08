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
package com.appjars.saturn.validation;

import com.appjars.saturn.exception.BaseException;
import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.model.Errors;

/**
 * Exception representing business rules validations fails
 * 
 * @author mlopez
 * 
 */
public class ValidationException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ValidationException() {
		super();
	}

	public ValidationException(Errors errors) {
		super(errors);
	}

	public ValidationException(String messageKey, Object... messageKeyValues) {
		super(messageKey, messageKeyValues);
	}

	public ValidationException(String messageKey) {
		super(messageKey);
	}

	public ValidationException(Throwable cause, Errors errors) {
		super(cause, errors);
	}

	public ValidationException(Throwable cause, String messageKey, Object... messageKeyValues) {
		super(cause, messageKey, messageKeyValues);
	}

	public <T extends ErrorDescription> ValidationException(Throwable cause, T error) {
		super(cause, error);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

}