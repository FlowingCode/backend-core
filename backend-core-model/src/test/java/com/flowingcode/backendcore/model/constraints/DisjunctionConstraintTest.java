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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flowingcode.backendcore.model.Constraint;
import com.flowingcode.backendcore.model.ConstraintBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class DisjunctionConstraintTest {

  private static final Constraint A = ConstraintBuilder.of("a").equal(1);
  private static final Constraint B = ConstraintBuilder.of("b").equal(2);
  private static final Constraint C = ConstraintBuilder.of("c").equal(3);

  @Test
  void testOfProducesCorrectMembers() {
    DisjunctionConstraint d = DisjunctionConstraint.of(A, B, C);
    assertEquals(List.of(A, B, C), d.getConstraints());
  }

  @Test
  void testChainedOrFlattens() {
    // a.or(b).or(c) must produce OR(a, b, c), not OR(OR(a, b), c)
    Constraint chained = A.or(B).or(C);
    DisjunctionConstraint d = (DisjunctionConstraint) chained;
    assertEquals(3, d.getConstraints().size());
    assertSame(A, d.getConstraints().get(0));
    assertSame(B, d.getConstraints().get(1));
    assertSame(C, d.getConstraints().get(2));
  }

  @Test
  void testOrWithExistingDisjunctionInRestFlattens() {
    // DisjunctionConstraint passed as a rest element is also flattened
    DisjunctionConstraint ab = DisjunctionConstraint.of(A, B);
    DisjunctionConstraint d = DisjunctionConstraint.of(C, ab);
    assertEquals(List.of(C, A, B), d.getConstraints());
  }

  @Test
  void testNullFirstThrows() {
    assertThrows(NullPointerException.class, () -> DisjunctionConstraint.of(null, B));
  }

  @Test
  void testNullRestArrayThrows() {
    assertThrows(NullPointerException.class, () -> DisjunctionConstraint.of(A, (Constraint[]) null));
  }

  @Test
  void testNullElementInRestThrows() {
    assertThrows(NullPointerException.class, () -> DisjunctionConstraint.of(A, B, null));
  }

}
