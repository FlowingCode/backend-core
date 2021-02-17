package com.appjars.saturn.model;

import com.appjars.saturn.model.constraints.AttributeBetweenConstraint;
import com.appjars.saturn.model.constraints.AttributeInConstraint;
import com.appjars.saturn.model.constraints.AttributeLikeConstraint;
import com.appjars.saturn.model.constraints.AttributeRelationalConstraint;
import com.appjars.saturn.model.constraints.NegatedConstraint;

/**
 * @author Javier Godoy / Flowing Code
 * @param <T>
 */
public abstract class ConstraintTransformer<T> {


	public T transform(Constraint c) {

		if (c instanceof AttributeBetweenConstraint) {
			return transformBetweenConstraint((AttributeBetweenConstraint) c);
		}

		if (c instanceof AttributeLikeConstraint) {
			return transformLikeConstraint((AttributeLikeConstraint) c);
		}

		if (c instanceof AttributeRelationalConstraint) {
			return transformRelationalConstraint((AttributeRelationalConstraint) c);
		}
		
		if (c instanceof AttributeInConstraint) {
			return transformInConstraint((AttributeInConstraint) c);
		}
		
		if (c instanceof NegatedConstraint) {
			return transformNegatedConstraint((NegatedConstraint) c);
		}

		return null;
	}

	protected T transformRelationalConstraint(AttributeRelationalConstraint c) {
		return null;
	}

	protected T transformLikeConstraint(AttributeLikeConstraint c) {
		return null;
	}

	protected T transformBetweenConstraint(AttributeBetweenConstraint c) {
		return null;
	}
	
	protected T transformInConstraint(AttributeInConstraint c) {
		return null;
	}

	protected T transformNegatedConstraint(NegatedConstraint c) {
		return null;
	}
	
}
