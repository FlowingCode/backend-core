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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import com.appjars.saturn.model.ConstraintTransformer;
import com.appjars.saturn.model.constraints.AttributeBetweenConstraint;
import com.appjars.saturn.model.constraints.AttributeConstraint;
import com.appjars.saturn.model.constraints.AttributeILikeConstraint;
import com.appjars.saturn.model.constraints.AttributeInConstraint;
import com.appjars.saturn.model.constraints.AttributeLikeConstraint;
import com.appjars.saturn.model.constraints.AttributeNullConstraint;
import com.appjars.saturn.model.constraints.AttributeRelationalConstraint;
import com.appjars.saturn.model.constraints.NegatedConstraint;
import com.appjars.saturn.model.constraints.RelationalConstraint;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConstraintTransformerJpaImpl extends ConstraintTransformer<Predicate> {

	@NonNull
	private final CriteriaBuilder criteriaBuilder;
	
	@NonNull
	private final From<?,?> root;
	
	public ConstraintTransformerJpaImpl(EntityManager em, From<?,?> root) {
		this.criteriaBuilder = em.getCriteriaBuilder();
		this.root = Objects.requireNonNull(root);
	}

	private Expression<?> getExpression(AttributeConstraint c) {
		return getExpression(c, Object.class);
	}
	
	@SuppressWarnings("unchecked")
	private <T> Expression<T> getExpression(AttributeConstraint c, Class<T> type) {
		String path[] = c.getAttribute().split("\\.");
		String attributeName = path[path.length-1];
		path = Arrays.copyOf(path, path.length-1);
		Expression<?> expression = join(root,path).get(attributeName);
		boxed(expression.getJavaType()).asSubclass(type);
		return (Expression<T>) expression;
	}
		
	private From<?,?> join(From<?,?> root, String[] path) {
		From<?,?> from = root;
		for (String attributeName : path) {
			from = join(from, attributeName);
		}
		return from;
	}
	
	@SuppressWarnings("rawtypes")
	private From<?,?> join(From<?,?> source, String attributeName) {
		Optional<Join> existingJoin = source.getJoins().stream().filter(join->join.getAttribute().getName().equals(attributeName)).map(join->(Join)join).findFirst();
		return existingJoin.orElseGet(()->source.join(attributeName, JoinType.INNER));
	}
	
	private static Class<?> boxed(Class<?> type) {
		if (type.isPrimitive()) {
			if (type==boolean.class) return Boolean.class;
			if (type==int.class) return Integer.class;
			if (type==long.class) return Long.class;
			if (type==byte.class) return Byte.class;
			if (type==short.class) return Short.class;
			if (type==char.class) return Character.class;
			if (type==float.class) return Float.class;
			if (type==double.class) return Double.class;
		}
		return type;
	}
	
	@Override
	protected Predicate transformNegatedConstraint(NegatedConstraint c) {		
		return criteriaBuilder.not(transform(c.getConstraint()));
	}

	@Override
	protected Predicate transformRelationalConstraint(AttributeRelationalConstraint c) {
		switch (c.getOperator()) {
			case RelationalConstraint.EQ: 
			case RelationalConstraint.NE: 
				return transformEqualityConstraint(c);
			default: 
				return transformComparisonConstraint(c);
		}
	}
	
	private Predicate transformEqualityConstraint(AttributeRelationalConstraint c) {
		Expression<?> x = getExpression(c);
		Object y = c.getValue();
		
		switch (c.getOperator()) {
			case RelationalConstraint.EQ: return criteriaBuilder.equal(x, y);
			case RelationalConstraint.NE: return criteriaBuilder.notEqual(x, y);
			default: return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Predicate transformComparisonConstraint(AttributeRelationalConstraint c) {
		Expression<Comparable> x = getExpression(c, Comparable.class);
		Comparable y = (Comparable<?>) c.getValue();
		
		switch (c.getOperator()) {
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

	@Override
	protected Predicate transformNullConstraint(AttributeNullConstraint c) {
		return getExpression(c).isNull();
	}
	
	@Override
    protected Predicate transformILikeConstraint(AttributeILikeConstraint c) {
        return criteriaBuilder.like(criteriaBuilder.lower(getExpression(c, String.class)), c.getPattern().toLowerCase());
    }
}
