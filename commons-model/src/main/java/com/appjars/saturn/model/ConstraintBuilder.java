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

import java.util.Collection;

import com.appjars.saturn.model.constraints.AttributeBetweenConstraint;
import com.appjars.saturn.model.constraints.AttributeInConstraint;
import com.appjars.saturn.model.constraints.AttributeLikeConstraint;
import com.appjars.saturn.model.constraints.AttributeRelationalConstraint;
import com.appjars.saturn.model.constraints.NegatedConstraint;
import com.appjars.saturn.model.constraints.RelationalConstraint;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConstraintBuilder {

	public Constraint not(Constraint c) {
		return new NegatedConstraint(c);	
	}
	
	public Constraint equal(String attribute, Object value) {
		return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.EQ);	
	}
	
	public Constraint notEqual(String attribute, Object value) {
		return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.NE);	
	}
	
	public <T  extends Comparable<T>> Constraint between(String attribute, T lower, T upper) {
		return new AttributeBetweenConstraint(attribute, lower, upper);	
	}
	
	public Constraint like(String attribute, String pattern) {
		return new AttributeLikeConstraint(attribute, pattern);	
	}
	
	public Constraint in(String attribute, Collection<?> values) {
		return new AttributeInConstraint(attribute, values);	
	}
}
