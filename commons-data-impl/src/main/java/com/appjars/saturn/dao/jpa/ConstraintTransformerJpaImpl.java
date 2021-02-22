/*-
 * #%L
 * Commons Backend - Data Access Layer Implementations
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
package com.appjars.saturn.dao.jpa;

import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.appjars.saturn.model.ConstraintTransformer;
import com.appjars.saturn.model.constraints.AttributeBetweenConstraint;
import com.appjars.saturn.model.constraints.AttributeConstraint;
import com.appjars.saturn.model.constraints.AttributeInConstraint;
import com.appjars.saturn.model.constraints.AttributeLikeConstraint;
import com.appjars.saturn.model.constraints.AttributeRelationalConstraint;
import com.appjars.saturn.model.constraints.NegatedConstraint;
import com.appjars.saturn.model.constraints.RelationalConstraint;

public class ConstraintTransformerJpaImpl extends ConstraintTransformer<Predicate> {

	private final CriteriaBuilder criteriaBuilder;
	private Root<?> root;
	
	public ConstraintTransformerJpaImpl(EntityManager em, Root<?> root) {
		this.criteriaBuilder = em.getCriteriaBuilder();
		this.root = Objects.requireNonNull(root);
	}

	private Expression<?> getExpression(AttributeConstraint c) {
		return root.get(c.getAttribute());
	}
	
	@SuppressWarnings("unchecked")
	private <T> Expression<T> getExpression(AttributeConstraint c, Class<T> type) {
		Expression<?> expression = root.get(c.getAttribute());
		expression.getJavaType().asSubclass(type);
		return (Expression<T>) expression;
	}
		
	@Override
	protected Predicate transformNegatedConstraint(NegatedConstraint c) {		
		return criteriaBuilder.not(transform(c.getConstraint()));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Predicate transformRelationalConstraint(AttributeRelationalConstraint c) {
		c.getAttribute();
		Expression<Comparable> x = getExpression(c, Comparable.class);
		Comparable y = c.getValue();
		
		switch (c.getOperator()) {
			case RelationalConstraint.EQ: return criteriaBuilder.equal(x, y);
			case RelationalConstraint.NE: return criteriaBuilder.notEqual(x, y);
			case RelationalConstraint.LE: return criteriaBuilder.lessThanOrEqualTo(x, y);
			case RelationalConstraint.LT: return criteriaBuilder.lessThan(x, y);
			case RelationalConstraint.GE: return criteriaBuilder.greaterThan(x, y);
			case RelationalConstraint.GT: return criteriaBuilder.greaterThanOrEqualTo(x, y);
			default: return null;
		}
	}

	@Override
	protected Predicate transformLikeConstraint(AttributeLikeConstraint c) {
		return criteriaBuilder.like(getExpression(c, String.class), c.getPattern());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Predicate transformBetweenConstraint(AttributeBetweenConstraint c) {
		return criteriaBuilder.between(getExpression(c, Comparable.class), (Comparable)c.getLower(), (Comparable)c.getUpper());
	}

	@Override
	protected Predicate transformInConstraint(AttributeInConstraint c) {
		return getExpression(c).in(c.getValues());
	}

}
