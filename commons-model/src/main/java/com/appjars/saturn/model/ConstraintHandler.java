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
public abstract class ConstraintHandler<T> {


	public T handle(Constraint c) {

		if (c instanceof AttributeBetweenConstraint) {
			return handleBetweenConstraint((AttributeBetweenConstraint) c);
		}

		if (c instanceof AttributeLikeConstraint) {
			return handleLikeConstraint((AttributeLikeConstraint) c);
		}

		if (c instanceof AttributeRelationalConstraint) {
			return handleRelationalConstraint((AttributeRelationalConstraint) c);
		}
		
		if (c instanceof AttributeInConstraint) {
			return handleInConstraint((AttributeInConstraint) c);
		}
		
		if (c instanceof NegatedConstraint) {
			return handleNegatedConstraint((NegatedConstraint) c);
		}

		return null;
	}

	protected T handleRelationalConstraint(AttributeRelationalConstraint c) {
		return null;
	}

	protected T handleLikeConstraint(AttributeLikeConstraint c) {
		return null;
	}

	protected T handleBetweenConstraint(AttributeBetweenConstraint c) {
		return null;
	}
	
	protected T handleInConstraint(AttributeInConstraint c) {
		return null;
	}

	protected T handleNegatedConstraint(NegatedConstraint c) {
		return null;
	}
	
}
