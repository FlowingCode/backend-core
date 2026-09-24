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
package com.flowingcode.backendcore.model.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Base class for annotation-driven filters consumed by the DAO layer.
 *
 * <p>Filter subclasses declare typed fields annotated with {@link Attribute}
 * and (optionally) {@link From} / {@link To} / {@link WhenNull}. The DAO base
 * reflects on the populated instance to build a JPA {@code CriteriaQuery}.
 *
 * <p>Two construction styles are supported and produce equivalent state:
 *
 * <pre>{@code
 * // POJO style
 * PersonFilter f = new PersonFilter()
 *         .setName("Ada")
 *         .setBirthDateFrom(LocalDate.of(1990, 1, 1));
 * f.addOrder("name");
 * f.setMaxResult(50);
 *
 * // Builder style (Lombok @SuperBuilder)
 * PersonFilter f = PersonFilter.builder()
 *         .name("Ada")
 *         .birthDateFrom(LocalDate.of(1990, 1, 1))
 *         .addOrder("name", BaseFilter.Order.ASC)
 *         .maxResult(50)
 *         .build();
 * }</pre>
 *
 * <p>Pagination validators ({@link #setFirstResult(Integer)} /
 * {@link #setMaxResult(Integer)}) reject negative values from the POJO path,
 * and the builder's {@code firstResult(...)} / {@code maxResult(...)} methods
 * apply the same checks before {@code build()} returns, so both styles enforce
 * the same invariants.
 *
 * <p>The sort orders are never shared: maps passed to {@link #setOrders(Map)} or
 * to the builder are copied, {@link #addOrder(String, Order)} copies before
 * writing, and {@link #getOrders()} returns an unmodifiable view. A filter
 * obtained through {@code toBuilder()} is therefore independent of its source.
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class BaseFilter {

	/** Sort direction for an ordering entry. */
	public enum Order {
		ASC, DESC
	}

	private Map<String, Order> orders;

	private Integer firstResult;

	private Integer maxResult;

	/** Adds an ascending order on {@code attribute}. */
	public BaseFilter addOrder(String attribute) {
		return addOrder(attribute, Order.ASC);
	}

	/** Adds an order on {@code attribute} with the given {@code direction}. */
	public BaseFilter addOrder(String attribute, Order direction) {
		// Copy before writing: the map may be shared with the builder this filter
		// was built from, or with another filter built from that same builder.
		this.orders = copyOf(this.orders);
		this.orders.put(attribute, direction);
		return this;
	}

	/**
	 * Returns the configured sort orders, preserving insertion order. Never
	 * {@code null}; an empty map indicates no ordering.
	 *
	 * @return an unmodifiable view of the sort orders
	 */
	public Map<String, Order> getOrders() {
		return orders == null ? Collections.emptyMap() : Collections.unmodifiableMap(orders);
	}

	/**
	 * Replaces the sort orders with a copy of {@code orders}, so later changes to
	 * the argument do not affect this filter.
	 *
	 * @param orders the sort orders in application order, or {@code null} to clear
	 */
	public BaseFilter setOrders(Map<String, Order> orders) {
		this.orders = orders == null ? null : copyOf(orders);
		return this;
	}

	private static Map<String, Order> copyOf(Map<String, Order> orders) {
		return orders == null ? new LinkedHashMap<>() : new LinkedHashMap<>(orders);
	}

	/**
	 * Sets the position of the first result to retrieve (numbered from 0).
	 *
	 * @param firstResult the position, or {@code null} to clear
	 * @throws IllegalArgumentException if the argument is negative
	 */
	public BaseFilter setFirstResult(Integer firstResult) {
		validateNonNegative(firstResult, "firstResult");
		this.firstResult = firstResult;
		return this;
	}

	/**
	 * Sets the maximum number of results to retrieve.
	 *
	 * @param maxResult the cap, or {@code null} for no cap
	 * @throws IllegalArgumentException if the argument is negative
	 */
	public BaseFilter setMaxResult(Integer maxResult) {
		validateNonNegative(maxResult, "maxResult");
		this.maxResult = maxResult;
		return this;
	}

	private static void validateNonNegative(Integer value, String name) {
		if (value != null && value < 0) {
			throw new IllegalArgumentException(name + " must be >= 0");
		}
	}

	/**
	 * Inner builder declared explicitly so that paging validation and the
	 * per-entry order adder are available on the builder API. Lombok's
	 * {@code @SuperBuilder} fills in the rest (field accumulation, {@code self()},
	 * {@code build()}, the subclass plumbing).
	 */
	public abstract static class BaseFilterBuilder<C extends BaseFilter, B extends BaseFilterBuilder<C, B>> {

		/** Adds an ascending order on {@code attribute}. */
		public B addOrder(String attribute) {
			return addOrder(attribute, Order.ASC);
		}

		/** Adds an order on {@code attribute} with the given {@code direction}. */
		public B addOrder(String attribute, Order direction) {
			// Always copy: a builder obtained via toBuilder() shares the map reference
			// with the source filter, so mutating in place would leak into it.
			this.orders = copyOf(this.orders);
			this.orders.put(attribute, direction);
			return self();
		}

		/**
		 * Replaces the sort orders with a copy of {@code orders}, so later changes
		 * to the argument do not affect the built filter.
		 *
		 * @param orders the sort orders in application order, or {@code null} to
		 *        clear
		 */
		public B orders(Map<String, Order> orders) {
			this.orders = orders == null ? null : copyOf(orders);
			return self();
		}

		/**
		 * @throws IllegalArgumentException if {@code firstResult} is negative
		 */
		public B firstResult(Integer firstResult) {
			validateNonNegative(firstResult, "firstResult");
			this.firstResult = firstResult;
			return self();
		}

		/**
		 * @throws IllegalArgumentException if {@code maxResult} is negative
		 */
		public B maxResult(Integer maxResult) {
			validateNonNegative(maxResult, "maxResult");
			this.maxResult = maxResult;
			return self();
		}
	}
}
