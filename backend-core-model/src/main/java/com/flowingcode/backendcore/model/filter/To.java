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
 * Marks a filter field as the upper bound of a range comparison on its
 * {@link Attribute}.
 *
 * <p>When {@link #inclusive()} is {@code true} (default) the predicate is
 * {@code attribute <= value}; when {@code false}, it becomes
 * {@code attribute < value}.
 *
 * <p>If another field on the same filter declares the same {@code @Attribute}
 * value and carries a {@link From} annotation, the two fields form a range.
 * With both bounds non-null and both inclusive, the processor emits a single
 * {@code BETWEEN} predicate; otherwise two ANDed comparison predicates are
 * emitted honoring each side's {@code inclusive} setting.
 *
 * <p>Must be paired with {@link Attribute}; otherwise the processor reports a
 * configuration error.
 *
 * @see Attribute
 * @see From
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface To {

	/**
	 * When {@code true} (default), the upper bound is inclusive ({@code <=}).
	 * When {@code false}, the bound is strict ({@code <}).
	 */
	boolean inclusive() default true;
}
