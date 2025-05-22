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
package com.flowingcode.backendcore.model.constraints;

import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * Constraint that checks if an attribute's value falls between two inclusive bounds.
 *
 * @author jgodoy
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeBetweenConstraint implements AttributeConstraint {

	@NonNull String attribute;
	@NonNull Comparable<?> lower;
	@NonNull Comparable<?> upper;

	public <T extends Comparable<T>> AttributeBetweenConstraint(String attribute, T lower, T upper) {
		this.attribute=Objects.requireNonNull(attribute);
		this.lower=Objects.requireNonNull(lower);
		this.upper=Objects.requireNonNull(upper);
	}
	
}
