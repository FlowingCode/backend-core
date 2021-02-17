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

/**
 * Thrown by {@link ConstraintTransformer} when the {@link QuerySpec} contains an unsupported {@link Constraint}.
 * 
 * @author Javier Godoy / Flowing Code
 */
public class ConstraintTransformerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConstraintTransformerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstraintTransformerException(String message) {
		super(message);
	}

	public ConstraintTransformerException(Throwable cause) {
		super(cause);
	}

}
