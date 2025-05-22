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
package com.flowingcode.backendcore.model;

import com.flowingcode.backendcore.model.constraints.DisjunctionConstraint;
import com.flowingcode.backendcore.model.constraints.NegatedConstraint;

/**
 * General constraint interface representing a filtering criterion for queries.
 *
 * @author jgodoy
 */
public interface Constraint {

  default Constraint not() {
    return new NegatedConstraint(this);
  }

  /**
   * Returns a constraint that is satisfied when this constraint or any of the given constraints is
   * satisfied (logical OR).
   *
   * @param first the first additional constraint
   * @param rest  optional additional constraints
   * @return a {@link DisjunctionConstraint} combining this and the given constraints
   */
  default Constraint or(Constraint first, Constraint... rest) {
    return DisjunctionConstraint.of(this, prepend(first, rest));
  }

  private static Constraint[] prepend(Constraint first, Constraint[] rest) {
    Constraint[] result = new Constraint[1 + rest.length];
    result[0] = first;
    System.arraycopy(rest, 0, result, 1, rest.length);
    return result;
  }

}
