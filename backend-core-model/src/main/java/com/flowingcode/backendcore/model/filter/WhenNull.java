/*-
 * #%L
 * Commons Backend - Model
 * %%
 * Copyright (C) 2020 - 2026 Flowing Code
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
package com.flowingcode.backendcore.model.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides how a {@code null} value on the annotated filter field is handled.
 *
 * <p>The default policy for any field carrying {@link Attribute} is
 * {@link Policy#SKIP}: a null field contributes no predicate. Use this
 * annotation with {@link Policy#IS_NULL} to instead emit a
 * {@code attribute IS NULL} predicate.
 *
 * <p>{@code @WhenNull} requires {@link Attribute} on the same field and is not
 * allowed alongside {@link From} or {@link To}, since range bounds have no
 * sensible {@code IS_NULL} semantics.
 *
 * @see Attribute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WhenNull {

	Policy value();

	/** Policy applied to a null filter field. */
	enum Policy {

		/** Emit no predicate for this field when its value is {@code null}. */
		SKIP,

		/** Emit an {@code IS NULL} predicate when the field value is {@code null}. */
		IS_NULL
	}
}
