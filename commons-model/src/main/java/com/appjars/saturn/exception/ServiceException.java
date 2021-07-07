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

import java.util.List;

import com.appjars.saturn.model.ErrorDescription;

/**
 * Exception thrown at Service Layer
 *
 * @author mlopez
 *
 */
public class ServiceException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ServiceException() {
		super();
	}

	public ServiceException(List<ErrorDescription> errors) {
		super(errors);
	}

	public ServiceException(String messageKey, Object... messageKeyValues) {
		super(messageKey, messageKeyValues);
	}

	public ServiceException(String messageKey) {
		super(messageKey);
	}

	public ServiceException(Throwable cause, List<ErrorDescription> errors) {
		super(cause, errors);
}

	public ServiceException(Throwable cause, String messageKey, Object... messageKeyValues) {
		super(cause, messageKey, messageKeyValues);
	}

	public ServiceException(Throwable cause, ErrorDescription error) {
		super(cause, error);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}
	
	@Override
	protected BaseException newInstance(ErrorDescription error) {
		return new ServiceException(null, error);
	}

}
