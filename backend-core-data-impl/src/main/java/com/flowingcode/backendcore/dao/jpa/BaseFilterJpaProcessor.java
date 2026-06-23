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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flowingcode.backendcore.model.Identifiable;
import com.flowingcode.backendcore.model.filter.Attribute;
import com.flowingcode.backendcore.model.filter.BaseFilter;
import com.flowingcode.backendcore.model.filter.From;
import com.flowingcode.backendcore.model.filter.To;
import com.flowingcode.backendcore.model.filter.WhenNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Builds and executes JPA {@code CriteriaQuery} from a {@link BaseFilter}.
 *
 * <p>The processor reflects on the filter class once (per JVM), caches the
 * resulting metadata, and produces predicates honoring {@link Attribute},
 * {@link com.flowingcode.backendcore.model.filter.From} /
 * {@link com.flowingcode.backendcore.model.filter.To} ranges and
 * {@link WhenNull} null-handling policies. DAO subclasses contribute
 * non-declarative predicates and other {@code CriteriaQuery} mutations through
 * the supplied {@link Hooks}.
 *
 * <p><b>Instances are not thread-safe.</b> Create a new instance per query
 * invocation.
 */
class BaseFilterJpaProcessor<T extends Identifiable<K>, K extends Serializable> {

	/** DAO-supplied callbacks that augment the declaratively built criteria. */
	interface Hooks<T> {
		Collection<Predicate> customizePredicates(BaseFilter filter, CriteriaBuilder cb,
				CriteriaQuery<?> cq, Root<T> root);

		void customizeCriteria(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
				Root<T> root);
	}

	private static final ConcurrentMap<Class<? extends BaseFilter>, FilterMetadata> METADATA_CACHE =
			new ConcurrentHashMap<>();

	private final EntityManager em;
	private final Class<T> persistentClass;
	private final Hooks<T> hooks;

	static <T extends Identifiable<K>, K extends Serializable> BaseFilterJpaProcessor<T, K> of(
			EntityManager em, Class<T> persistentClass, Hooks<T> hooks) {
		return new BaseFilterJpaProcessor<>(em, persistentClass, hooks);
	}

	private BaseFilterJpaProcessor(EntityManager em, Class<T> persistentClass, Hooks<T> hooks) {
		this.em = em;
		this.persistentClass = persistentClass;
		this.hooks = hooks;
	}

	List<T> filter(BaseFilter filter) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(persistentClass);
		Root<T> root = cq.from(persistentClass);
		cq.select(root);
		applyPredicates(filter, cb, cq, root);
		applyOrders(filter, cb, cq, root);
		hooks.customizeCriteria(filter, cb, cq, root);
		TypedQuery<T> query = em.createQuery(cq);
		applyPaging(query, filter);
		return query.getResultList();
	}

	Optional<T> filterWithSingleResult(BaseFilter filter) {
		List<T> result = filter(filter);
		if (result.size() > 1) {
			throw new IllegalStateException("Current filter returned more than one result");
		}
		return result.stream().findFirst();
	}

	long count(BaseFilter filter) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(persistentClass);
		cq.select(cb.count(root));
		applyPredicates(filter, cb, cq, root);
		hooks.customizeCriteria(filter, cb, cq, root);
		return em.createQuery(cq).getSingleResult();
	}

	// ----- query assembly -----

	private void applyPredicates(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
			Root<T> root) {
		FilterMetadata metadata = metadataFor(filter.getClass());
		AttributePathResolver resolver = new AttributePathResolver(root);

		List<Predicate> predicates = new ArrayList<>();
		for (FieldHandler handler : metadata.handlers) {
			Predicate predicate;
			try {
				predicate = handler.toPredicate(filter, cb, resolver);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
						"Cannot read field " + handler.describe() + " on " + filter.getClass(), e);
			}
			if (predicate != null) {
				predicates.add(predicate);
			}
		}

		Collection<Predicate> extra = hooks.customizePredicates(filter, cb, cq, root);
		if (extra != null && !extra.isEmpty()) {
			predicates.addAll(extra);
		}

		if (!predicates.isEmpty()) {
			cq.where(predicates.toArray(new Predicate[0]));
		}
	}

	private void applyOrders(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<T> cq,
			Root<T> root) {
		Map<String, BaseFilter.Order> orders = filter.getOrders();
		if (orders == null || orders.isEmpty()) {
			return;
		}
		AttributePathResolver resolver = new AttributePathResolver(root);
		List<jakarta.persistence.criteria.Order> jpaOrders = new ArrayList<>(orders.size());
		for (Entry<String, BaseFilter.Order> e : orders.entrySet()) {
			Expression<?> expr = resolver.resolve(e.getKey());
			jpaOrders.add(e.getValue() == BaseFilter.Order.ASC ? cb.asc(expr) : cb.desc(expr));
		}
		cq.orderBy(jpaOrders);
	}

	private void applyPaging(TypedQuery<T> query, BaseFilter filter) {
		if (filter.getFirstResult() != null) {
			query.setFirstResult(filter.getFirstResult());
		}
		if (filter.getMaxResult() != null) {
			query.setMaxResults(filter.getMaxResult());
		}
	}

	// ----- metadata reflection + caching -----

	private static FilterMetadata metadataFor(Class<? extends BaseFilter> filterClass) {
		return METADATA_CACHE.computeIfAbsent(filterClass, BaseFilterJpaProcessor::buildMetadata);
	}

	private static FilterMetadata buildMetadata(Class<? extends BaseFilter> filterClass) {
		// Index every declared field in the hierarchy by name so the value-accessor
		// helper can read any of them, including unannotated ones. Closer-to-leaf
		// declarations win on name shadowing.
		Map<String, Field> fieldsByName = new LinkedHashMap<>();
		List<Field> annotated = new ArrayList<>();
		Class<?> c = filterClass;
		while (c != null && c != Object.class) {
			for (Field f : c.getDeclaredFields()) {
				if (!fieldsByName.containsKey(f.getName())) {
					f.setAccessible(true);
					fieldsByName.put(f.getName(), f);
				}
				if (c != BaseFilter.class && (f.isAnnotationPresent(Attribute.class)
						|| f.isAnnotationPresent(From.class) || f.isAnnotationPresent(To.class)
						|| f.isAnnotationPresent(WhenNull.class))) {
					annotated.add(f);
				}
			}
			c = c.getSuperclass();
		}

		// Validate per-field constraints and group declarative fields by attribute path.
		Map<String, List<Field>> byPath = new HashMap<>();
		for (Field f : annotated) {
			Attribute attr = f.getAnnotation(Attribute.class);
			From from = f.getAnnotation(From.class);
			To to = f.getAnnotation(To.class);
			WhenNull whenNull = f.getAnnotation(WhenNull.class);

			if ((from != null || to != null || whenNull != null) && attr == null) {
				throw new IllegalStateException("Field " + describe(f)
						+ " has @From/@To/@WhenNull but no @Attribute");
			}
			if (attr != null && attr.manual()
					&& (from != null || to != null || whenNull != null)) {
				throw new IllegalStateException("Field " + describe(f)
						+ " is @Attribute(manual=true); it cannot combine with @From, @To or @WhenNull");
			}
			if (from != null && to != null) {
				throw new IllegalStateException("Field " + describe(f)
						+ " cannot be both @From and @To");
			}
			if (whenNull != null && (from != null || to != null)) {
				throw new IllegalStateException("Field " + describe(f)
						+ " cannot combine @WhenNull with @From or @To");
			}
			// Manual fields are tracked in fieldsByName already; skip declarative grouping.
			if (attr.manual()) {
				continue;
			}
			byPath.computeIfAbsent(attr.value(), k -> new ArrayList<>()).add(f);
		}

		List<FieldHandler> handlers = new ArrayList<>();
		for (Entry<String, List<Field>> entry : byPath.entrySet()) {
			String path = entry.getKey();
			List<Field> group = entry.getValue();
			handlers.add(buildHandler(path, group));
		}
		return new FilterMetadata(handlers, Collections.unmodifiableMap(fieldsByName));
	}

	private static FieldHandler buildHandler(String path, List<Field> group) {
		if (group.size() == 1) {
			Field f = group.get(0);
			From from = f.getAnnotation(From.class);
			To to = f.getAnnotation(To.class);
			if (from != null) {
				return new LowerBoundHandler(path, f, from.inclusive());
			}
			if (to != null) {
				return new UpperBoundHandler(path, f, to.inclusive());
			}
			WhenNull whenNull = f.getAnnotation(WhenNull.class);
			WhenNull.Policy policy = whenNull != null ? whenNull.value() : WhenNull.Policy.SKIP;
			return new EqualityHandler(path, f, policy);
		}
		if (group.size() == 2) {
			Field a = group.get(0);
			Field b = group.get(1);
			From fromA = a.getAnnotation(From.class);
			To toA = a.getAnnotation(To.class);
			From fromB = b.getAnnotation(From.class);
			To toB = b.getAnnotation(To.class);
			Field lower = null;
			Field upper = null;
			boolean lowerInclusive = true;
			boolean upperInclusive = true;
			if (fromA != null && toB != null) {
				lower = a;
				upper = b;
				lowerInclusive = fromA.inclusive();
				upperInclusive = toB.inclusive();
			} else if (fromB != null && toA != null) {
				lower = b;
				upper = a;
				lowerInclusive = fromB.inclusive();
				upperInclusive = toA.inclusive();
			}
			if (lower != null) {
				return new RangePairHandler(path, lower, upper, lowerInclusive, upperInclusive);
			}
		}
		throw new IllegalStateException("Attribute path \"" + path
				+ "\" is mapped by multiple filter fields that do not form a @From/@To pair: "
				+ describe(group));
	}

	private static String describe(Field f) {
		return f.getDeclaringClass().getSimpleName() + "." + f.getName();
	}

	private static String describe(List<Field> fields) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(describe(fields.get(i)));
		}
		return sb.toString();
	}

	// ----- handler types -----

	/**
	 * Reads {@code fieldName} on the given {@code filter} using the cached
	 * reflection metadata. Intended to be called from DAO hooks that need to
	 * consume the value of a field marked {@code @Attribute(manual=true)} (or any
	 * other field on the filter) without re-doing reflection.
	 *
	 * @throws IllegalArgumentException if the filter class has no field with that
	 *         name
	 */
	static Object readField(BaseFilter filter, String fieldName) {
		FilterMetadata metadata = metadataFor(filter.getClass());
		Field f = metadata.fieldsByName.get(fieldName);
		if (f == null) {
			throw new IllegalArgumentException("Filter " + filter.getClass().getSimpleName()
					+ " has no field named: " + fieldName);
		}
		try {
			return f.get(filter);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(
					"Cannot read field " + describe(f) + " on " + filter.getClass(), e);
		}
	}

	private static final class FilterMetadata {
		final List<FieldHandler> handlers;
		final Map<String, Field> fieldsByName;

		FilterMetadata(List<FieldHandler> handlers, Map<String, Field> fieldsByName) {
			this.handlers = List.copyOf(handlers);
			this.fieldsByName = fieldsByName;
		}
	}

	private abstract static class FieldHandler {
		final String attributePath;

		FieldHandler(String attributePath) {
			this.attributePath = attributePath;
		}

		abstract Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb,
				AttributePathResolver resolver) throws IllegalAccessException;

		abstract String describe();
	}

	private static final class EqualityHandler extends FieldHandler {
		private final Field field;
		private final WhenNull.Policy nullPolicy;

		EqualityHandler(String path, Field field, WhenNull.Policy nullPolicy) {
			super(path);
			this.field = field;
			this.nullPolicy = nullPolicy;
		}

		@Override
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object value = field.get(filter);
			if (value == null) {
				return nullPolicy == WhenNull.Policy.IS_NULL ? cb.isNull(resolver.resolve(attributePath))
						: null;
			}
			return cb.equal(resolver.resolve(attributePath), value);
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(field);
		}
	}

	private static final class LowerBoundHandler extends FieldHandler {
		private final Field field;
		private final boolean inclusive;

		LowerBoundHandler(String path, Field field, boolean inclusive) {
			super(path);
			this.field = field;
			this.inclusive = inclusive;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object value = field.get(filter);
			if (value == null) return null;
			Expression<Comparable> expr = resolver.resolve(attributePath, Comparable.class);
			return inclusive ? cb.greaterThanOrEqualTo(expr, (Comparable) value)
					: cb.greaterThan(expr, (Comparable) value);
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(field);
		}
	}

	private static final class UpperBoundHandler extends FieldHandler {
		private final Field field;
		private final boolean inclusive;

		UpperBoundHandler(String path, Field field, boolean inclusive) {
			super(path);
			this.field = field;
			this.inclusive = inclusive;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object value = field.get(filter);
			if (value == null) return null;
			Expression<Comparable> expr = resolver.resolve(attributePath, Comparable.class);
			return inclusive ? cb.lessThanOrEqualTo(expr, (Comparable) value)
					: cb.lessThan(expr, (Comparable) value);
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(field);
		}
	}

	private static final class RangePairHandler extends FieldHandler {
		private final Field lower;
		private final Field upper;
		private final boolean lowerInclusive;
		private final boolean upperInclusive;

		RangePairHandler(String path, Field lower, Field upper, boolean lowerInclusive,
				boolean upperInclusive) {
			super(path);
			this.lower = lower;
			this.upper = upper;
			this.lowerInclusive = lowerInclusive;
			this.upperInclusive = upperInclusive;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object lo = lower.get(filter);
			Object hi = upper.get(filter);
			if (lo == null && hi == null) return null;
			Expression<Comparable> expr = resolver.resolve(attributePath, Comparable.class);

			if (lo != null && hi != null && lowerInclusive && upperInclusive) {
				return cb.between(expr, (Comparable) lo, (Comparable) hi);
			}

			List<Predicate> parts = new ArrayList<>(2);
			if (lo != null) {
				parts.add(lowerInclusive ? cb.greaterThanOrEqualTo(expr, (Comparable) lo)
						: cb.greaterThan(expr, (Comparable) lo));
			}
			if (hi != null) {
				parts.add(upperInclusive ? cb.lessThanOrEqualTo(expr, (Comparable) hi)
						: cb.lessThan(expr, (Comparable) hi));
			}
			return parts.size() == 1 ? parts.get(0) : cb.and(parts.toArray(new Predicate[0]));
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(lower) + " + "
					+ BaseFilterJpaProcessor.describe(upper);
		}
	}
}
