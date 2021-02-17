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
