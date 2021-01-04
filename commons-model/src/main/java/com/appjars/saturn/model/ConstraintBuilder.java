package com.appjars.saturn.model;

import java.util.Collection;

import com.appjars.saturn.model.constraints.AttributeBetweenConstraint;
import com.appjars.saturn.model.constraints.AttributeInConstraint;
import com.appjars.saturn.model.constraints.AttributeLikeConstraint;
import com.appjars.saturn.model.constraints.AttributeRelationalConstraint;
import com.appjars.saturn.model.constraints.NegatedConstraint;
import com.appjars.saturn.model.constraints.RelationalConstraint;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConstraintBuilder {

	public Constraint not(Constraint c) {
		return new NegatedConstraint(c);	
	}
	
	public Constraint equal(String attribute, Object value) {
		return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.EQ);	
	}
	
	public Constraint notEqual(String attribute, Object value) {
		return new AttributeRelationalConstraint(attribute, value, RelationalConstraint.NE);	
	}
	
	public <T  extends Comparable<T>> Constraint between(String attribute, T lower, T upper) {
		return new AttributeBetweenConstraint(attribute, lower, upper);	
	}
	
	public Constraint like(String attribute, String value) {
		return new AttributeLikeConstraint(attribute, value);	
	}
	
	public Constraint in(String attribute, Collection<?> values) {
		return new AttributeInConstraint(attribute, values);	
	}
}
