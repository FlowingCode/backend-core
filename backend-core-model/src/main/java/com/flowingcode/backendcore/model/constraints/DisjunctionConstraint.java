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
package com.flowingcode.backendcore.model.constraints;

import com.flowingcode.backendcore.model.Constraint;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/** A constraint that is satisfied when any of its member constraints is satisfied (logical OR). */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DisjunctionConstraint implements Constraint {

  @NonNull List<Constraint> constraints;

  public static DisjunctionConstraint of(Constraint first, Constraint... rest) {
    List<Constraint> list = new java.util.ArrayList<>();
    Objects.requireNonNull(rest, "constraints must not be null");
    add(list, Objects.requireNonNull(first, "constraint must not be null"));
    for (Constraint c : rest) {
      add(list, Objects.requireNonNull(c, "constraint must not be null"));
    }
    return new DisjunctionConstraint(List.copyOf(list));
  }

  private static void add(List<Constraint> list, Constraint c) {
    if (c instanceof DisjunctionConstraint) {
      list.addAll(((DisjunctionConstraint) c).constraints);
    } else {
      list.add(c);
    }
  }

}
