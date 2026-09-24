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
 * {@code "city.state.name"}). Path traversal auto-joins associations, reusing
 * an existing join when one is already present on the same attribute. Joins are
 * inner joins, except where an inner join would drop rows the predicate is meant
 * to match: paths of {@link Or} fields, {@code IS NULL} predicates from
 * {@link WhenNull}, and sort orders use left joins. Consequently, an equality or
 * comparison on a nested path never matches rows whose association is
 * {@code null}.
 *
 * <p>A filter field carrying only {@code @Attribute} is interpreted as an
 * equality predicate against the resolved attribute. Pair it with {@link From}
 * or {@link To} to express range comparisons, with {@link Like} or {@link In}
 * for pattern and membership matching, with {@link WhenNull} to control how a
 * null field value is handled, or with {@link Or} to place the predicate in the
 * filter's disjunction.
 *
 * <p>Set {@link #manual()} to {@code true} when the predicate for the field is
 * built by hand in a DAO hook (e.g.
 * {@code ConversionJpaDaoSupport#customizePredicates}). The processor skips
 * the field, and the hook reads its value through the filter's accessor, so a
 * manual field must have one. Manual fields cannot
 * combine with {@link From}, {@link To}, {@link Like}, {@link In},
 * {@link WhenNull} or {@link Or}, since none of those have meaning when the
 * predicate is hand-built.
 *
 * @see From
 * @see To
 * @see Like
 * @see In
 * @see WhenNull
 * @see Or
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
	 * When {@code true}, the processor does not generate a declarative predicate
	 * for the field. The caller is responsible for producing the predicate in a
	 * DAO hook, reading the value through the filter's accessor.
	 */
	boolean manual() default false;
}
