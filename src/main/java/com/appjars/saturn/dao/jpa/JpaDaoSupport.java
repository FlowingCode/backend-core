package com.appjars.saturn.dao.jpa;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.model.BaseFilter;
import com.appjars.saturn.model.Identifiable;

public interface JpaDaoSupport<T extends Identifiable<K>, K extends Serializable> extends CrudDao<T, K> {

	EntityManager getEntityManager();

	default K save(T entity) {
		getEntityManager().persist(entity);
		return entity.getId();
	}

	@Override
	default void saveOrUpdate(T entity) {
		getEntityManager().persist(entity);
	}

	@Override
	default void delete(T entity) {
		getEntityManager().remove(entity);
	}

	@Override
	default List<T> filter(BaseFilter<K> filter) {
		return FilterProcesor.<T, K>of(getEntityManager()).filter(filter);
	}

	static class FilterProcesor<T extends Identifiable<K>, K extends Serializable> {

		@SuppressWarnings("unchecked")
		private Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		private EntityManager entityManager;

		private static <T extends Identifiable<K>, K extends Serializable> FilterProcesor<T, K> of(
				EntityManager entityManager) {
			return new FilterProcesor<>(entityManager);
		}

		private FilterProcesor(EntityManager entityManager) {
			this.entityManager = entityManager;
		}

		public List<T> filter(BaseFilter<K> baseFilter) {
			if (baseFilter.getReturnedAttributes() != null && baseFilter.getReturnedAttributes().length > 0) {
				return filterNotFullData(baseFilter);
			} else {
				return filterFullData(baseFilter);
			}
		}

		protected CriteriaQuery<T> createFilterCriteria(final BaseFilter<K> baseFilter) {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<T> crit = cb.createQuery(persistentClass);
// TODO: add support for excludeIds
//			if (baseFilter.getExcludeIds() != null && baseFilter.getExcludeIds().length > 0) {
//				crit.add(Restrictions.not(Restrictions.in("id", baseFilter.getExcludeIds())));
//			}
			for (Entry<String, String> alias : baseFilter.getAliases().entrySet()) {
				Root<T> root = crit.from(persistentClass);
				crit = crit.where(cb.equal(root.join(alias.getKey()), alias.getValue()));
//				crit.createAlias(alias.getKey(), alias.getValue());
			}
			for (Entry<String, String> alias : baseFilter.getLeftAliases().entrySet()) {
				Root<T> root = crit.from(persistentClass);
				crit = crit.where(cb.equal(root.join(alias.getKey(), JoinType.LEFT), alias.getValue()));
//				crit.createAlias(alias.getKey(), alias.getValue(), JoinType.LEFT_OUTER_JOIN);
			}
			if (baseFilter.getOrders() != null) {
				for (Entry<String, BaseFilter.Order> entry : baseFilter.getOrders().entrySet()) {
					Root<T> root = crit.from(persistentClass);
					if (BaseFilter.Order.ASC.equals(entry.getValue())) {
						crit.orderBy(cb.asc(root.get(entry.getKey())));
//						crit.addOrder(Order.asc(entry.getKey()));
					} else {
						crit.orderBy(cb.desc(root.get(entry.getKey())));
//						crit.addOrder(Order.desc(entry.getKey()));
					}

				}
			}
			if (baseFilter.getEagerRelationShips() != null) {
				for (String relation : baseFilter.getEagerRelationShips()) {
					Root<T> root = crit.from(persistentClass);
					root.fetch(relation);
//					crit.setFetchMode(relation, FetchMode.JOIN);
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

		@SuppressWarnings("unchecked")
		protected List<T> filterFullData(BaseFilter<K> filter) {
			CriteriaQuery<T> criteria = createFilterCriteria(filter);
			return addPagination(criteria, filter).getResultList();
		}

		@SuppressWarnings("unchecked")
		protected List<T> filterNotFullData(BaseFilter<K> filter) {
			CriteriaQuery<T> criteria = createFilterCriteria(filter);
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

		@SuppressWarnings("unchecked")
		protected List<T> executeCriteria(CriteriaQuery<T> criteria) {
			return this.entityManager.createQuery(criteria).getResultList();
		}

		protected T executeCriteriaForUniqueResult(CriteriaQuery<T> criteria) {
			return this.entityManager.createQuery(criteria).getSingleResult();
		}

		protected TypedQuery<T> addPagination(CriteriaQuery<T> detachedCriteria, BaseFilter<K> filter) {
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
