package com.flowingcode.backendcore.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import com.flowingcode.backendcore.dao.jpa.ConstraintTransformerJpaImpl;
import com.flowingcode.backendcore.model.Constraint;
import com.flowingcode.backendcore.model.QuerySpec;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Spring Data JPA {@code Specification} that applies a model
 * {@code Constraint}.
 *
 * @param <T> the entity type
 * @author jgodoy
 */
@SuppressWarnings("serial")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ConstraintSpecification<T> implements Specification<T> {

	@NonNull
	private final Constraint constraint;

	private static <T> Specification<T> newInstance(Constraint constraint) {
		return new ConstraintSpecification<>(constraint);
	}

	@Override
	public jakarta.persistence.criteria.Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		return new ConstraintTransformerJpaImpl(criteriaBuilder, root).apply(constraint);
	}

	static <T> Specification<T> buildSpecification(QuerySpec querySpec) {
		return querySpec.getConstraints().stream().map(ConstraintSpecification::<T>newInstance).reduce(Specification<T>::and).orElse(null);
	}

}
