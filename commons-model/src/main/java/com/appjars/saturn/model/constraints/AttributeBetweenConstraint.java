package com.appjars.saturn.model.constraints;

import java.util.Objects;

import com.appjars.saturn.model.Constraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeBetweenConstraint implements AttributeConstraint {

	@NonNull String attribute;
	@NonNull Comparable<?> lower;
	@NonNull Comparable<?> upper;

	public <T extends Comparable<T>> AttributeBetweenConstraint(String attribute, T lower, T upper) {
		this.attribute=Objects.requireNonNull(attribute);
		this.lower=Objects.requireNonNull(lower);
		this.upper=Objects.requireNonNull(upper);
	}
	
}
