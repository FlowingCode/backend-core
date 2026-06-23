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
 * Maps a filter field to an entity attribute.
 *
 * <p>The value is a dotted attribute path on the target entity (e.g.
 * {@code "city.state.name"}). Path traversal auto-joins associations using
 * inner joins, reusing existing joins when one is already present on the same
 * attribute and join type.
 *
 * <p>A filter field carrying only {@code @Attribute} is interpreted as an
 * equality predicate against the resolved attribute. Pair it with {@link From}
 * or {@link To} to express range comparisons, or with {@link WhenNull} to
 * control how a null field value is handled.
 *
 * <p>Set {@link #manual()} to {@code true} when the predicate for the field is
 * built by hand in a DAO hook (e.g.
 * {@code ConversionJpaDaoSupport#customizePredicates}). The processor will skip
 * declarative predicate building for the field but still track it so the
 * field's value can be retrieved via
 * {@code ConversionJpaDaoSupport#getFilterFieldValue}. Manual fields cannot
 * combine with {@link From}, {@link To} or {@link WhenNull}, since none of
 * those have meaning when the predicate is hand-built.
 *
 * @see From
 * @see To
 * @see WhenNull
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute {

	/**
	 * Dotted attribute path on the target entity (e.g. {@code "city.state.name"}).
	 * On a {@link #manual() manual} field the value is informational — the
	 * processor never resolves it — and is still useful as documentation of
	 * which entity attribute the hook is expected to target.
	 */
	String value();

	/**
	 * When {@code true}, the processor records the field for value lookup but
	 * does not generate a declarative predicate. The caller is responsible for
	 * producing the predicate in a DAO hook.
	 */
	boolean manual() default false;
}
