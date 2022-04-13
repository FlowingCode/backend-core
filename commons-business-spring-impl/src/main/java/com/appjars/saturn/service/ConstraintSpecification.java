package com.appjars.saturn.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import com.appjars.saturn.dao.jpa.ConstraintTransformerJpaImpl;
import com.appjars.saturn.model.Constraint;
import com.appjars.saturn.model.QuerySpec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ConstraintSpecification<T> implements Specification<T> {

	@NonNull
	private final Constraint constraint;

	private static <T> Specification<T> newInstance(Constraint constraint) {
		return new ConstraintSpecification<>(constraint);
	}

	@Override
	public javax.persistence.criteria.Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		return new ConstraintTransformerJpaImpl(criteriaBuilder, root).apply(constraint);
	}

	static <T> Specification<T> buildSpecification(QuerySpec querySpec) {
		return querySpec.getConstraints().stream().map(ConstraintSpecification::<T>newInstance).reduce(Specification<T>::and).orElse(null);
	}

}
