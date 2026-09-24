/*-
 * #%L
 * Commons Backend - Data Access Layer Implementations
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
package com.flowingcode.backendcore.dao.jpa;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

/**
 * Resolves a dotted attribute path on a JPA {@code From} root into a leaf
 * {@code Expression}, auto-joining associations along the way.
 *
 * <p>The caller chooses the join type of each resolution. A join that already
 * exists on the same attribute is reused whatever its type, so an association
 * is joined at most once per query. Reuse is safe for the declarative filter
 * predicates, which are always combined as a conjunction: an inner join is only
 * requested for a conjunct that rejects {@code null}, so every result row has
 * that association anyway, and a left join behaves as an inner join under such
 * a conjunct.
 *
 * <p>Instances are not thread-safe: a new resolver should be created per
 * {@code CriteriaQuery}.
 */
class AttributePathResolver {

	private final From<?, ?> root;

	AttributePathResolver(From<?, ?> root) {
		this.root = Objects.requireNonNull(root, "root");
	}

	/**
	 * Resolves {@code attributePath} into an {@code Expression} of the leaf
	 * attribute on the root, creating missing joins with {@code joinType}.
	 */
	Expression<?> resolve(String attributePath, JoinType joinType) {
		return resolve(attributePath, Object.class, joinType);
	}

	/**
	 * Resolves {@code attributePath}, creating missing joins with
	 * {@code joinType}, and verifies the leaf attribute's Java type is assignable
	 * to {@code expectedType}.
	 *
	 * @throws IllegalArgumentException if {@code attributePath} is blank, has a
	 *         leading or trailing dot, or contains empty segments
	 * @throws ClassCastException if the leaf attribute type isn't compatible
	 */
	@SuppressWarnings("unchecked")
	<V> Expression<V> resolve(String attributePath, Class<V> expectedType, JoinType joinType) {
		Objects.requireNonNull(attributePath, "attributePath");
		Objects.requireNonNull(joinType, "joinType");
		if (attributePath.isBlank() || attributePath.startsWith(".")
				|| attributePath.endsWith(".") || attributePath.contains("..")) {
			throw new IllegalArgumentException("Invalid attributePath: \"" + attributePath + "\"");
		}
		String[] path = attributePath.split("\\.");
		String attributeName = path[path.length - 1];
		String[] joinPath = Arrays.copyOf(path, path.length - 1);
		Expression<?> expression = traverse(root, joinPath, joinType).get(attributeName);
		boxed(expression.getJavaType()).asSubclass(expectedType);
		return (Expression<V>) expression;
	}

	private From<?, ?> traverse(From<?, ?> source, String[] path, JoinType joinType) {
		From<?, ?> from = source;
		for (String name : path) {
			from = join(from, name, joinType);
		}
		return from;
	}

	@SuppressWarnings("rawtypes")
	private From<?, ?> join(From<?, ?> source, String attributeName, JoinType joinType) {
		Optional<Join> existing = source.getJoins().stream()
				.map(j -> (Join) j)
				.filter(j -> j.getAttribute().getName().equals(attributeName))
				.findFirst();
		return existing.orElseGet(() -> source.join(attributeName, joinType));
	}

	private static Class<?> boxed(Class<?> type) {
		if (type.isPrimitive()) {
			if (type == boolean.class) return Boolean.class;
			if (type == int.class) return Integer.class;
			if (type == long.class) return Long.class;
			if (type == byte.class) return Byte.class;
			if (type == short.class) return Short.class;
			if (type == char.class) return Character.class;
			if (type == float.class) return Float.class;
			if (type == double.class) return Double.class;
		}
		return type;
	}
}
