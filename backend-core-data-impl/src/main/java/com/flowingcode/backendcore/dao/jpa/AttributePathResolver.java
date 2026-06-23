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
 * {@code Expression}, auto-joining associations along the way and reusing
 * existing joins when one is already present on the same attribute and join
 * type.
 *
 * <p>Instances are not thread-safe: a new resolver should be created per
 * {@code CriteriaQuery}.
 */
public class AttributePathResolver {

	private final From<?, ?> root;

	private JoinType currentJoinType = JoinType.INNER;

	public AttributePathResolver(From<?, ?> root) {
		this.root = Objects.requireNonNull(root, "root");
	}

	/** Returns the join type currently used when creating new joins. */
	public JoinType getCurrentJoinType() {
		return currentJoinType;
	}

	/** Sets the join type used for newly created joins by subsequent resolutions. */
	public void setCurrentJoinType(JoinType joinType) {
		this.currentJoinType = Objects.requireNonNull(joinType, "joinType");
	}

	/**
	 * Resolves {@code attributePath} into an {@code Expression} of the leaf
	 * attribute on the root, auto-joining as needed.
	 */
	public Expression<?> resolve(String attributePath) {
		return resolve(attributePath, Object.class);
	}

	/**
	 * Resolves {@code attributePath} and verifies the leaf attribute's Java type
	 * is assignable to {@code expectedType}.
	 *
	 * @throws ClassCastException if the leaf attribute type isn't compatible
	 */
	@SuppressWarnings("unchecked")
	public <V> Expression<V> resolve(String attributePath, Class<V> expectedType) {
		Objects.requireNonNull(attributePath, "attributePath");
		String[] path = attributePath.split("\\.");
		String attributeName = path[path.length - 1];
		String[] joinPath = Arrays.copyOf(path, path.length - 1);
		Expression<?> expression = traverse(root, joinPath).get(attributeName);
		boxed(expression.getJavaType()).asSubclass(expectedType);
		return (Expression<V>) expression;
	}

	private From<?, ?> traverse(From<?, ?> source, String[] path) {
		From<?, ?> from = source;
		for (String name : path) {
			from = join(from, name);
		}
		return from;
	}

	@SuppressWarnings("rawtypes")
	private From<?, ?> join(From<?, ?> source, String attributeName) {
		Optional<Join> existing = source.getJoins().stream()
				.map(j -> (Join) j)
				.filter(j -> j.getAttribute().getName().equals(attributeName))
				.filter(j -> j.getJoinType() == currentJoinType)
				.findFirst();
		return existing.orElseGet(() -> source.join(attributeName, currentJoinType));
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
