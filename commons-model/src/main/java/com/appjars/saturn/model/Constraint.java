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
package com.appjars.saturn.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Constraint {

	public enum Type {
		EQUALS, NOTEQUALS, LIKE, BETWEEN
	}

	private String attribute;
	private Type type;
	private Object value;
	private Object valueEnd;

	public Constraint(String attribute, Type type, Object value) {
		super();
		this.attribute = attribute;
		this.type = type;
		this.value = value;
	}

	public <T> Constraint(String attribute, T valueStart, T valueEnd) {
		super();
		this.attribute = attribute;
		this.type = Type.BETWEEN;
		this.value = valueStart;
		this.valueEnd = valueEnd;
	}

}
