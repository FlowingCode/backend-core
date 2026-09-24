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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.flowingcode.backendcore.model.Identifiable;
import com.flowingcode.backendcore.model.filter.Attribute;
import com.flowingcode.backendcore.model.filter.BaseFilter;
import com.flowingcode.backendcore.model.filter.From;
import com.flowingcode.backendcore.model.filter.In;
import com.flowingcode.backendcore.model.filter.Like;
import com.flowingcode.backendcore.model.filter.Or;
import com.flowingcode.backendcore.model.filter.To;
import com.flowingcode.backendcore.model.filter.WhenNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

/**
 * Builds and executes JPA {@code CriteriaQuery} from a {@link BaseFilter}.
 *
 * <p>The processor reflects on the filter class once (per JVM), caches the
 * resulting metadata, and produces predicates honoring {@link Attribute},
 * {@link From} / {@link To} ranges, {@link Like} and {@link In} matching,
 * {@link WhenNull} null-handling policies and the {@link Or} disjunction. DAO
 * subclasses contribute non-declarative predicates and other
 * {@code CriteriaQuery} mutations through the supplied {@link Hooks}.
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

	/** Annotations that choose the operator of a field's predicate; at most one per field. */
	private static final List<Class<? extends Annotation>> OPERATORS =
			List.of(From.class, To.class, Like.class, In.class);

	/** Annotations that are only meaningful on a declarative {@code @Attribute} field. */
	private static final List<Class<? extends Annotation>> MODIFIERS =
			List.of(From.class, To.class, Like.class, In.class, WhenNull.class, Or.class);

	private static final ConcurrentMap<Class<? extends BaseFilter>, List<FieldHandler>> HANDLER_CACHE =
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
		requireSelection(cq, root);
		TypedQuery<T> query = em.createQuery(cq);
		applyPaging(query, filter);
		return query.getResultList();
	}

	Optional<T> filterWithSingleResult(BaseFilter filter) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(persistentClass);
		Root<T> root = cq.from(persistentClass);
		cq.select(root);
		applyPredicates(filter, cb, cq, root);
		hooks.customizeCriteria(filter, cb, cq, root);
		requireSelection(cq, root);
		try {
			return Optional.of(em.createQuery(cq).getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		} catch (NonUniqueResultException e) {
			throw new IllegalStateException("Current filter returned more than one result", e);
		}
	}

	long count(BaseFilter filter) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(persistentClass);
		Expression<Long> count = cb.count(root);
		cq.select(count);
		applyPredicates(filter, cb, cq, root);
		hooks.customizeCriteria(filter, cb, cq, root);
		requireSelection(cq, count);
		if (!cq.getGroupList().isEmpty()) {
			throw new IllegalStateException(
					"customizeCriteria must not add a GROUP BY to a count query, which would return"
							+ " one count per group; check CriteriaQuery.getResultType() to skip it");
		}
		if (cq.isDistinct()) {
			// SELECT DISTINCT COUNT(x) still counts duplicates; count distinct roots instead.
			cq.distinct(false);
			cq.select(cb.countDistinct(root));
		}
		return em.createQuery(cq).getSingleResult();
	}

	private static void requireSelection(CriteriaQuery<?> cq, Selection<?> expected) {
		if (cq.getSelection() != expected) {
			throw new IllegalStateException("customizeCriteria must not replace the query selection");
		}
	}

	// ----- query assembly -----

	private void applyPredicates(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<?> cq,
			Root<T> root) {
		AttributePathResolver resolver = new AttributePathResolver(root);

		List<Predicate> conjunction = new ArrayList<>();
		List<Predicate> disjunction = new ArrayList<>();
		for (FieldHandler handler : handlersFor(filter.getClass())) {
			Predicate predicate;
			try {
				predicate = handler.toPredicate(filter, cb, resolver);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
						"Cannot read field " + handler.describe() + " on " + filter.getClass(), e);
			}
			if (predicate != null) {
				(handler.or ? disjunction : conjunction).add(predicate);
			}
		}
		// A disjunction without disjuncts is false, so it is only added when some
		// @Or field contributed a predicate; otherwise it would match no rows.
		if (!disjunction.isEmpty()) {
			conjunction.add(cb.or(disjunction.toArray(new Predicate[0])));
		}

		Collection<Predicate> extra = hooks.customizePredicates(filter, cb, cq, root);
		if (extra != null && !extra.isEmpty()) {
			conjunction.addAll(extra);
		}

		if (!conjunction.isEmpty()) {
			cq.where(conjunction.toArray(new Predicate[0]));
		}
	}

	private void applyOrders(BaseFilter filter, CriteriaBuilder cb, CriteriaQuery<T> cq,
			Root<T> root) {
		Map<String, BaseFilter.Order> orders = filter.getOrders();
		if (orders.isEmpty()) {
			return;
		}
		AttributePathResolver resolver = new AttributePathResolver(root);
		List<jakarta.persistence.criteria.Order> jpaOrders = new ArrayList<>(orders.size());
		for (Entry<String, BaseFilter.Order> e : orders.entrySet()) {
			// Left join: sorting must not drop rows whose association is null.
			Expression<?> expr = resolver.resolve(e.getKey(), JoinType.LEFT);
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

	private static List<FieldHandler> handlersFor(Class<? extends BaseFilter> filterClass) {
		return HANDLER_CACHE.computeIfAbsent(filterClass, BaseFilterJpaProcessor::buildHandlers);
	}

	private static List<FieldHandler> buildHandlers(Class<? extends BaseFilter> filterClass) {
		// Collect the annotated fields of every class between the filter and
		// BaseFilter. A field shadowed by a subclass field of the same name is still
		// a filter field of its own.
		List<Field> annotated = new ArrayList<>();
		for (Class<?> c = filterClass; c != BaseFilter.class; c = c.getSuperclass()) {
			for (Field f : c.getDeclaredFields()) {
				if (f.isAnnotationPresent(Attribute.class) || !annotationsOn(f, MODIFIERS).isEmpty()) {
					annotated.add(f);
				}
			}
		}

		// Validate per-field constraints and group declarative fields by attribute path.
		Map<String, List<Field>> byPath = new HashMap<>();
		for (Field f : annotated) {
			validate(f);
			Attribute attr = f.getAnnotation(Attribute.class);
			// Manual fields are read by the DAO hook through the filter's accessors.
			if (attr.manual()) {
				continue;
			}
			f.setAccessible(true);
			byPath.computeIfAbsent(attr.value(), k -> new ArrayList<>()).add(f);
		}

		List<FieldHandler> handlers = new ArrayList<>();
		for (Entry<String, List<Field>> entry : byPath.entrySet()) {
			String path = entry.getKey();
			List<Field> group = entry.getValue();
			handlers.add(buildHandler(path, group));
		}
		return List.copyOf(handlers);
	}

	private static void validate(Field f) {
		Attribute attr = f.getAnnotation(Attribute.class);
		List<String> modifiers = annotationsOn(f, MODIFIERS);
		List<String> operators = annotationsOn(f, OPERATORS);

		if (attr == null) {
			throw new IllegalStateException("Field " + describe(f) + " has "
					+ String.join(", ", modifiers) + " but no @Attribute");
		}
		if (attr.manual() && !modifiers.isEmpty()) {
			throw new IllegalStateException("Field " + describe(f)
					+ " is @Attribute(manual=true); it cannot combine with "
					+ String.join(", ", modifiers));
		}
		if (operators.size() > 1) {
			throw new IllegalStateException("Field " + describe(f) + " cannot combine "
					+ String.join(" and ", operators) + "; use at most one of @From, @To, @Like or @In");
		}
		if (f.isAnnotationPresent(WhenNull.class) && !operators.isEmpty()) {
			throw new IllegalStateException("Field " + describe(f)
					+ " cannot combine @WhenNull with " + operators.get(0));
		}
		if (f.isAnnotationPresent(Like.class) && f.getType() != String.class) {
			throw new IllegalStateException("Field " + describe(f)
					+ " is @Like, so its type must be String; found " + f.getType().getName());
		}
		if (f.isAnnotationPresent(In.class) && !Collection.class.isAssignableFrom(f.getType())) {
			throw new IllegalStateException("Field " + describe(f)
					+ " is @In, so its type must be a Collection; found " + f.getType().getName());
		}
	}

	private static List<String> annotationsOn(Field f, List<Class<? extends Annotation>> types) {
		return types.stream()
				.filter(f::isAnnotationPresent)
				.map(type -> "@" + type.getSimpleName())
				.collect(Collectors.toList());
	}

	private static FieldHandler buildHandler(String path, List<Field> group) {
		if (group.size() == 1) {
			Field f = group.get(0);
			boolean or = f.isAnnotationPresent(Or.class);
			From from = f.getAnnotation(From.class);
			if (from != null) {
				return new LowerBoundHandler(path, or, f, from.inclusive());
			}
			To to = f.getAnnotation(To.class);
			if (to != null) {
				return new UpperBoundHandler(path, or, f, to.inclusive());
			}
			Like like = f.getAnnotation(Like.class);
			if (like != null) {
				return new LikeHandler(path, or, f, like.ignoreCase(), like.match());
			}
			In in = f.getAnnotation(In.class);
			if (in != null) {
				return new InHandler(path, or, f, in.whenEmpty());
			}
			WhenNull whenNull = f.getAnnotation(WhenNull.class);
			WhenNull.Policy policy = whenNull != null ? whenNull.value() : WhenNull.Policy.SKIP;
			return new EqualityHandler(path, or, f, policy);
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
				boolean or = lower.isAnnotationPresent(Or.class);
				if (or != upper.isAnnotationPresent(Or.class)) {
					throw new IllegalStateException("Range fields " + describe(group)
							+ " on attribute path \"" + path + "\" must either both carry @Or or neither");
				}
				return new RangePairHandler(path, or, lower, upper, lowerInclusive, upperInclusive);
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

	private abstract static class FieldHandler {
		final String attributePath;
		final boolean or;

		FieldHandler(String attributePath, boolean or) {
			this.attributePath = attributePath;
			this.or = or;
		}

		/**
		 * Join type for the path of a predicate that rejects {@code null}: a left
		 * join inside the disjunction, so that a row with a {@code null}
		 * association can still match through another disjunct, and an inner join
		 * otherwise.
		 */
		JoinType joinType() {
			return or ? JoinType.LEFT : JoinType.INNER;
		}

		abstract Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb,
				AttributePathResolver resolver) throws IllegalAccessException;

		abstract String describe();
	}

	private static final class EqualityHandler extends FieldHandler {
		private final Field field;
		private final WhenNull.Policy nullPolicy;

		EqualityHandler(String path, boolean or, Field field, WhenNull.Policy nullPolicy) {
			super(path, or);
			this.field = field;
			this.nullPolicy = nullPolicy;
		}

		@Override
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object value = field.get(filter);
			if (value == null) {
				// IS NULL must also match rows whose association is null, which an
				// inner join would drop before the predicate is evaluated.
				return nullPolicy == WhenNull.Policy.IS_NULL
						? cb.isNull(resolver.resolve(attributePath, JoinType.LEFT))
						: null;
			}
			return cb.equal(resolver.resolve(attributePath, joinType()), value);
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(field);
		}
	}

	private static final class LowerBoundHandler extends FieldHandler {
		private final Field field;
		private final boolean inclusive;

		LowerBoundHandler(String path, boolean or, Field field, boolean inclusive) {
			super(path, or);
			this.field = field;
			this.inclusive = inclusive;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object value = field.get(filter);
			if (value == null) return null;
			Expression<Comparable> expr = resolver.resolve(attributePath, Comparable.class, joinType());
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

		UpperBoundHandler(String path, boolean or, Field field, boolean inclusive) {
			super(path, or);
			this.field = field;
			this.inclusive = inclusive;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Object value = field.get(filter);
			if (value == null) return null;
			Expression<Comparable> expr = resolver.resolve(attributePath, Comparable.class, joinType());
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

		RangePairHandler(String path, boolean or, Field lower, Field upper, boolean lowerInclusive,
				boolean upperInclusive) {
			super(path, or);
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
			Expression<Comparable> expr = resolver.resolve(attributePath, Comparable.class, joinType());

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

	private static final class LikeHandler extends FieldHandler {
		private static final char ESCAPE = '\\';

		private final Field field;
		private final boolean ignoreCase;
		private final Like.Match match;

		LikeHandler(String path, boolean or, Field field, boolean ignoreCase, Like.Match match) {
			super(path, or);
			this.field = field;
			this.ignoreCase = ignoreCase;
			this.match = match;
		}

		@Override
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			String value = (String) field.get(filter);
			if (value == null) return null;
			Expression<String> expr = resolver.resolve(attributePath, String.class, joinType());
			if (ignoreCase) {
				expr = cb.lower(expr);
				value = value.toLowerCase(Locale.ROOT);
			}
			if (match == Like.Match.RAW) {
				return cb.like(expr, value);
			}
			return cb.like(expr, pattern(value), ESCAPE);
		}

		private String pattern(String value) {
			String escaped = escape(value);
			return switch (match) {
				case STARTS_WITH -> escaped + "%";
				case ENDS_WITH -> "%" + escaped;
				case CONTAINS, RAW -> "%" + escaped + "%";
			};
		}

		/** Escapes the wildcards so that {@code value} is matched literally. */
		private static String escape(String value) {
			StringBuilder sb = new StringBuilder(value.length());
			for (char ch : value.toCharArray()) {
				if (ch == ESCAPE || ch == '%' || ch == '_') {
					sb.append(ESCAPE);
				}
				sb.append(ch);
			}
			return sb.toString();
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(field);
		}
	}

	private static final class InHandler extends FieldHandler {
		private final Field field;
		private final In.EmptyPolicy whenEmpty;

		InHandler(String path, boolean or, Field field, In.EmptyPolicy whenEmpty) {
			super(path, or);
			this.field = field;
			this.whenEmpty = whenEmpty;
		}

		@Override
		Predicate toPredicate(BaseFilter filter, CriteriaBuilder cb, AttributePathResolver resolver)
				throws IllegalAccessException {
			Collection<?> values = (Collection<?>) field.get(filter);
			if (values == null) return null;
			if (values.isEmpty()) {
				// A disjunction without disjuncts is false: it matches no rows.
				return whenEmpty == In.EmptyPolicy.MATCH_NONE ? cb.disjunction() : null;
			}
			return resolver.resolve(attributePath, joinType()).in(values);
		}

		@Override
		String describe() {
			return BaseFilterJpaProcessor.describe(field);
		}
	}
}
