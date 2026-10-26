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
 * Places the predicate of the annotated filter field in the filter's
 * disjunction instead of its conjunction.
 *
 * <p>The predicates of all {@code @Or} fields are combined with {@code OR}, and
 * the result is combined with {@code AND} with the predicates of every other
 * field:
 *
 * <pre>
 * WHERE &lt;other fields&gt; AND (&lt;@Or fields&gt;)
 * </pre>
 *
 * <p>There is a single disjunction per filter: every {@code @Or} field joins
 * the same one, including fields inherited from a superclass. Fields whose value
 * contributes no predicate (for instance, a {@code null} value) are left out of
 * it, and when no {@code @Or} field contributes a predicate the disjunction is
 * omitted, so it never restricts the results on its own.
 *
 * <p>Associations traversed by an {@code @Or} field are joined with a left
 * join, so rows with a {@code null} association can still match through
 * another branch of the disjunction.
 *
 * <p>Must be paired with {@link Attribute} and cannot be used on a
 * {@link Attribute#manual() manual} field. On a {@link From} / {@link To} pair,
 * either both fields or neither carry {@code @Or}. Groups of disjunctions, or
 * matching one value against several attributes, are built in a DAO hook.
 *
 * @see Attribute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Or {
}
