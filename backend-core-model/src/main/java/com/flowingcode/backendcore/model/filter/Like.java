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
 * Matches the {@link Attribute} of a {@code String} filter field with a
 * {@code LIKE} predicate instead of equality.
 *
 * <p>With the default {@link Match#CONTAINS}, the field value is searched
 * anywhere in the attribute. The {@code CONTAINS}, {@code STARTS_WITH} and
 * {@code ENDS_WITH} modes escape the {@code %} and {@code _} wildcards (and the
 * escape character itself) in the field value, so the value is always matched
 * literally. Use {@link Match#RAW} to pass the field value through as the
 * pattern, wildcards included.
 *
 * <p>Set {@link #ignoreCase()} to compare case-insensitively; both sides are
 * lower-cased before comparing.
 *
 * <p>A {@code null} field value contributes no predicate. Must be paired with
 * {@link Attribute}, and cannot combine with {@link From}, {@link To},
 * {@link In} or {@link WhenNull}.
 *
 * @see Attribute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Like {

	/** When {@code true}, the comparison ignores case. Defaults to {@code false}. */
	boolean ignoreCase() default false;

	/** Where the field value must occur in the attribute. Defaults to {@link Match#CONTAINS}. */
	Match match() default Match.CONTAINS;

	/** Position of the field value within the matched attribute. */
	enum Match {

		/** The field value is used as the pattern verbatim, wildcards included. */
		RAW,

		/** The attribute contains the field value. */
		CONTAINS,

		/** The attribute starts with the field value. */
		STARTS_WITH,

		/** The attribute ends with the field value. */
		ENDS_WITH
	}
}
