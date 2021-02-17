/*-
 * #%L
 * Commons Backend - Business Implementations
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
package com.appjars.saturn.service.validation;

import java.io.Serializable;
import java.util.List;

import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.validation.Validator;

public interface DeletionValidator<T extends Serializable> extends Validator<T> {

	default DeletionValidator<T> and(DeletionValidator<T> then) {
		return t -> {
			List<ErrorDescription> result = this.validate(t);
			if (result.isEmpty()) {
				result = then.validate(t); 
			}
			return result;
		};
	}

}
