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
package com.flowingcode.backendcore.model;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.flowingcode.backendcore.model.constraints.AttributeBetweenConstraint;
import com.flowingcode.backendcore.model.constraints.AttributeILikeConstraint;
import com.flowingcode.backendcore.model.constraints.AttributeInConstraint;
import com.flowingcode.backendcore.model.constraints.AttributeLikeConstraint;
import com.flowingcode.backendcore.model.constraints.AttributeNullConstraint;
import com.flowingcode.backendcore.model.constraints.AttributeRelationalConstraint;
import com.flowingcode.backendcore.model.constraints.NegatedConstraint;
import com.flowingcode.backendcore.model.constraints.RelationalConstraint;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConstraintBuilder {

  public static ConstraintBuilder of(String attribute0, String... attributes) {
    String attribute = Stream.concat(Stream.of(attribute0), Stream.of(attributes)).collect(Collectors.joining("."));
    return new ConstraintBuilder(attribute);
  }

  @NonNull
  private final String attribute;

  /**
   * @deprecated Use {@link Constraint#not()}
   */
  @Deprecated
  public static Constraint not(Constraint c) {
    return new NegatedConstraint(c);	
  }

  /** @deprecated Use {@link #equal(String)} */
  @Deprecated
  public static Constraint equal(String attribute, Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.EQ);	
  }

  /** @deprecated Use {@link #notEqual(String)} */
  @Deprecated
  public static Constraint notEqual(String attribute, Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.NE);	
  }

  /** @deprecated Use {@link #between(String)} */
  @Deprecated
  public <T  extends Comparable<T>> Constraint between(String attribute, T lower, T upper) {
    return new AttributeBetweenConstraint(attribute, lower, upper);	
  }

  /** @deprecated Use {@link #like(String)} */
  @Deprecated
  public static Constraint like(String attribute, String pattern) {
    return new AttributeLikeConstraint(attribute, pattern);	
  }

  /** @deprecated Use {@link #in(String)} */
  @Deprecated
  public static Constraint in(String attribute, Collection<?> values) {
    return new AttributeInConstraint(attribute, values);	
  }

  /** @deprecated Use {@link #isNull(String)} */
  @Deprecated
  public static Constraint isNull(String attribute) {
    return new AttributeNullConstraint(attribute);
  }

  /** @deprecated Use {@link #iLike(String)} */
  @Deprecated
  public static Constraint iLike(String attribute, String pattern) {
    return new AttributeILikeConstraint(attribute, pattern); 
  }

  public Constraint equal(Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.EQ);
  }

  public Constraint notEqual(Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.NE);
  }

  public Constraint lessThan(Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.LT);
  }

  public Constraint greaterThan(Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.GT);
  }

  public Constraint greaterOrEqualThan(Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.GE);
  }

  public Constraint lessOrEqualThan(Object value) {
    return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.LE);
  }

  public <T extends Comparable<T>> Constraint between(T lower, T upper) {
    return new AttributeBetweenConstraint(attribute, lower, upper);
  }

  public Constraint like(String pattern) {
    return new AttributeLikeConstraint(attribute, pattern);
  }

  public Constraint in(Collection<?> values) {
    return new AttributeInConstraint(attribute, values);
  }

  public Constraint isNull() {
    return new AttributeNullConstraint(attribute);
  }
  public Constraint isNotNull() {
    return ConstraintBuilder.not(new AttributeNullConstraint(attribute));
  }

  public Constraint iLike(String pattern) {
    return new AttributeILikeConstraint(attribute, pattern);
  }

}
