package com.appjars.saturn.model;

import java.util.Optional;
import java.util.function.Function;

import com.appjars.saturn.model.constraints.AttributeBetweenConstraint;
import com.appjars.saturn.model.constraints.AttributeInConstraint;
import com.appjars.saturn.model.constraints.AttributeLikeConstraint;
import com.appjars.saturn.model.constraints.AttributeRelationalConstraint;
import com.appjars.saturn.model.constraints.NegatedConstraint;

/**
 * Transform a {@link Constraint} into an implementation-specific representation (such as a JPA {@code Expression}).
 * Subclasses are expected to override the {@link #transform(Constraint)} method in order to support additional constraint types, 
 * and one or more of the {@code transform*Constraint} methods for providing the actual representations for the underlying database technology. 
 *  
 * @param <T> The type of the implementation-specific representation of the constraint.
 * @author Javier Godoy / Flowing Code 
 */
public abstract class ConstraintTransformer<T> implements Function<Constraint, T> {
	
	/**Return an implementation-specific representation of the constraint. 
	 * 
	 * @throws ConstraintTransformerException if the {@code Constraint} is not supported by this implementation.
	 */
	public final T apply(Constraint c) {
		return Optional.ofNullable(transform(c)).orElseThrow(() -> new ConstraintTransformerException("Unsupported constraint: " + c));
	}
	
	/** Return an implementation-specific representation of the constraint, or {@code null} if it cannot be transformed.
	 * This method delegated into one of the of the {@code transform*Constraint} methods, depending on the actual constraint class.*/
	protected T transform(Constraint c) {

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

	/** Return an implementation-specific representation of an {@code AttributeRelationalConstraint} constraint.
	 * @return an implementation-specific representation of the constraint, or {@code null} if it cannot be transformed.*/
	protected T transformRelationalConstraint(AttributeRelationalConstraint c) {
		return null;
	}

	/** Return an implementation-specific representation of an {@code AttributeLikeConstraint} constraint.
	 * @return an implementation-specific representation of the constraint, or {@code null} if it cannot be transformed.*/
	protected T transformLikeConstraint(AttributeLikeConstraint c) {
		return null;
	}

	/** Return an implementation-specific representation of an {@code AttributeBetweenConstraint} constraint.
	 * @return an implementation-specific representation of the constraint, or {@code null} if it cannot be transformed.*/
	protected T transformBetweenConstraint(AttributeBetweenConstraint c) {
		return null;
	}
	
	/** Return an implementation-specific representation of an {@code AttributeInConstraint} constraint.
	 * @return an implementation-specific representation of the constraint, or {@code null} if it cannot be transformed.*/
	protected T transformInConstraint(AttributeInConstraint c) {
		return null;
	}

	/** Return an implementation-specific representation of a {@code NegatedConstraint} constraint.
	 * @return an implementation-specific representation of the constraint, or {@code null} if it cannot be transformed.*/
	protected T transformNegatedConstraint(NegatedConstraint c) {
		return null;
	}

}
