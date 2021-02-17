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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.model.Identifiable;
import com.appjars.saturn.model.QuerySpec;

public interface ConversionJpaDaoSupport<S, T extends Identifiable<K>, K extends Serializable>
		extends CrudDao<S, K> {

	EntityManager getEntityManager();

	T convertTo(S source);

	S convertFrom(T source);
		
	@SuppressWarnings("unchecked")
	default Class<T> getPersistentClass() {
		Type[] interfaces = getClass().getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType) type;
				if (ptype.getRawType().equals(ConversionJpaDaoSupport.class)) {
					return (Class<T>) ptype.getActualTypeArguments()[1];
				}
			}
		}
		throw new UnsupportedOperationException("Couldn't find entity type, probably " + ConversionJpaDaoSupport.class
				+ " is not being implemented, try overriding this method");
	}

	@Override
	default K save(S entity) {
		T persistentEntity = convertTo(entity);
		persistentEntity = getEntityManager().merge(persistentEntity);
		K id = persistentEntity.getId();
		return Objects.requireNonNull(id, "id");
	}

	@Override
	default void update(S entity) {
		T persistentEntity = convertTo(entity);
		Objects.requireNonNull(persistentEntity.getId(), "id");
		getEntityManager().merge(persistentEntity);
	}

	@Override
	default void delete(S entity) {
		T persistentEntity = convertTo(entity);
		EntityManager em = getEntityManager();
		persistentEntity = em.getReference(getPersistentClass(), persistentEntity.getId());
		em.remove(persistentEntity);
	}

	@Override
	default Optional<S> findById(K id) {
		return FilterProcesor.<T, K>of(getEntityManager(), getPersistentClass()).findById(id).map(this::convertFrom);
	}

	@Override
	default List<S> findAll() {
		return FilterProcesor.<T, K>of(getEntityManager(), getPersistentClass()).findAll().stream()
				.map(this::convertFrom).collect(Collectors.toList());
	}

	@Override
	default long count(QuerySpec<K> filter) {
		return FilterProcesor.<T, K>of(getEntityManager(), getPersistentClass()).count(filter);
	}

	@Override
	default List<S> filter(QuerySpec<K> filter) {
		return FilterProcesor.<T, K>of(getEntityManager(), getPersistentClass()).filter(filter).stream()
				.map(this::convertFrom).collect(Collectors.toList());
	}

	static class FilterProcesor<T extends Identifiable<K>, K extends Serializable> {

		private Class<T> persistentClass;
		private EntityManager entityManager;

		private static <T extends Identifiable<K>, K extends Serializable> FilterProcesor<T, K> of(
				EntityManager entityManager, Class<T> persistentClass) {
			return new FilterProcesor<>(entityManager, persistentClass);
		}

		long count(QuerySpec<K> filter) {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<T> root = cq.from(persistentClass);
			cq.select(cb.count(root));
			addWhere(filter, cb, cq, root);
			return entityManager.createQuery(cq).getSingleResult();
		}

		List<T> findAll() {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(persistentClass);
			Root<T> rootEntry = cq.from(persistentClass);
			CriteriaQuery<T> all = cq.select(rootEntry);
			TypedQuery<T> allQuery = entityManager.createQuery(all);
			return allQuery.getResultList();
		}

		Optional<T> findById(K id) {
			return Optional.ofNullable(entityManager.find(persistentClass, id));
		}

		private FilterProcesor(EntityManager entityManager, Class<T> persistentClass) {
			this.entityManager = entityManager;
			this.persistentClass = persistentClass;
		}

		public List<T> filter(QuerySpec<K> baseFilter) {
			if (baseFilter.getReturnedAttributes() != null && baseFilter.getReturnedAttributes().length > 0) {
				return filterNotFullData(baseFilter);
			} else {
				return filterFullData(baseFilter);
			}
		}

		protected CriteriaQuery<T> createFilterCriteria(final QuerySpec<K> baseFilter, CriteriaBuilder cb) {
			CriteriaQuery<T> crit = cb.createQuery(persistentClass);
			Root<T> root = crit.from(persistentClass);
			crit = addWhere(baseFilter, cb, crit, root);
			if (baseFilter.getOrders() != null) {
				for (Entry<String, QuerySpec.Order> entry : baseFilter.getOrders().entrySet()) {
					if (QuerySpec.Order.ASC.equals(entry.getValue())) {
						crit.orderBy(cb.asc(root.get(entry.getKey())));
//						crit.addOrder(Order.asc(entry.getKey()));
					} else {
						crit.orderBy(cb.desc(root.get(entry.getKey())));
//						crit.addOrder(Order.desc(entry.getKey()));
					}

				}
			}
//			// si el objeto admite borrado lógico
//			if (Auditable.class.isAssignableFrom(getPersistentClass())
//					&& AuditableFilter.class.isAssignableFrom(baseFilter.getClass())) {
//				AuditableFilter<PK> filter = (AuditableFilter<PK>) baseFilter;
//				if (filter.getActivo() != null) {
//					crit.add(Restrictions.eq(Auditable.Attribute.ACTIVO, filter.getActivo()));
//				}
//				if (filter.getFechaAlta() != null) {
//					crit.add(Restrictions.eq(Auditable.Attribute.FECHA_ALTA, filter.getFechaAlta()));
//				}
//			}
			return crit;
		}

		private <U> CriteriaQuery<U> addWhere(final QuerySpec<K> baseFilter, CriteriaBuilder cb, CriteriaQuery<U> cq, Root<T> root) {			
			if (!baseFilter.getConstraints().isEmpty()) {
				return cq.where(baseFilter.getConstraints().stream()
						.map(new ConstraintTransformerImpl(entityManager, root))
						.toArray(Predicate[]::new));				
			} else {
				return cq;
			}
		}

		protected List<T> filterFullData(QuerySpec<K> filter) {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<T> criteria = createFilterCriteria(filter, cb);
			return addPagination(criteria, filter).getResultList();
		}

		protected List<T> filterNotFullData(QuerySpec<K> filter) {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<T> criteria = createFilterCriteria(filter, cb);
//			ProjectionList projections = null;
//			if (filter.getReturnedAttributes() != null) {
//				projections = Projections.projectionList();
//				for (String attr : filter.getReturnedAttributes()) {
//					projections.add(Projections.alias(Projections.property(attr), attr));
//				}
//			}
//			criteria.setProjection(Projections.distinct(projections));
//			criteria.setResultTransformer(new AliasToBeanResultTransformer(persistentClass));
			return addPagination(criteria, filter).getResultList();
		}

		protected List<T> executeCriteria(CriteriaQuery<T> criteria) {
			return this.entityManager.createQuery(criteria).getResultList();
		}

		protected T executeCriteriaForUniqueResult(CriteriaQuery<T> criteria) {
			return this.entityManager.createQuery(criteria).getSingleResult();
		}

		protected TypedQuery<T> addPagination(CriteriaQuery<T> detachedCriteria, QuerySpec<K> filter) {
			TypedQuery<T> criteria = this.entityManager.createQuery(detachedCriteria);
			if (filter.getFirstResult() != null) {
				criteria.setFirstResult(filter.getFirstResult());
			}
			if (filter.getMaxResult() != null) {
				criteria.setMaxResults(filter.getMaxResult());
			}
			return criteria;
		}
	}

}
