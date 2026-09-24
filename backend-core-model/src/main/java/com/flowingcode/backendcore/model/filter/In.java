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
 * Matches the {@link Attribute} of a {@code Collection} filter field with an
 * {@code IN} predicate: the attribute must equal one of the collection's
 * elements.
 *
 * <p>A {@code null} field value contributes no predicate. An empty collection is
 * handled according to {@link #whenEmpty()}, which defaults to
 * {@link EmptyPolicy#SKIP}: an empty selection is treated as "no criterion", the
 * same as {@code null}, rather than as "match nothing".
 *
 * <p>Must be paired with {@link Attribute}, and cannot combine with
 * {@link From}, {@link To}, {@link Like} or {@link WhenNull}.
 *
 * @see Attribute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface In {

	/** How an empty collection is handled. Defaults to {@link EmptyPolicy#SKIP}. */
	EmptyPolicy whenEmpty() default EmptyPolicy.SKIP;

	/** Policy applied to an empty collection. */
	enum EmptyPolicy {

		/** Emit no predicate, as for a {@code null} field value. */
		SKIP,

		/** Emit a predicate that matches no rows. */
		MATCH_NONE
	}
}
