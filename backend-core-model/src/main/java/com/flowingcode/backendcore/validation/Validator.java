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
package com.flowingcode.backendcore.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.flowingcode.backendcore.model.ErrorDescription;

/**
 * 
 * Generic validation service
 * 
 * @author mlopez, jgodoy
 * 
 * @param <T>
 */
@FunctionalInterface
public interface Validator<T> {

	public static List<ErrorDescription> success() {
		return Collections.emptyList();
	}
	
	public List<ErrorDescription> validate(T target);
		
	@SafeVarargs
	static <T> ValidatorBuilder<T> on(ValidationKind... kinds) {
		return new ValidatorBuilder<T>(kinds);
	}
		
	static <T> Validator<T> forCondition(Predicate<T> predicate, Function<T, ErrorDescription> errorSupplier) {
		Objects.requireNonNull(predicate);
		Objects.requireNonNull(errorSupplier);
		return t->predicate.test(t)?Validator.success():Collections.singletonList(errorSupplier.apply(t));
	}
	
}
