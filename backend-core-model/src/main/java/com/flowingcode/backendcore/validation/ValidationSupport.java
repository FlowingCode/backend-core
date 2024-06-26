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

import com.flowingcode.backendcore.model.ErrorDescription;
import java.util.List;
import java.util.stream.Collectors;

public interface ValidationSupport<T> {

  List<Validator<T>> getValidators();

  default List<Validator<T>> getValidators(
      @SuppressWarnings("rawtypes") Class<? extends Validator> validatorType) {
    return getValidators().stream().filter(validatorType::isInstance).collect(Collectors.toList());
  }

  default List<ErrorDescription> validate(Class<Validator<T>> validatorType, T t) {
      List<Validator<T>> validators = ((ValidationSupport<T>) this).getValidators(validatorType);
      return validators.stream().flatMap(val -> val.validate(t).stream()).collect(Collectors.toList());
  }

}
