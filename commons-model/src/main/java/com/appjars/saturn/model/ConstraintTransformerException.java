package com.appjars.saturn.model;

/**
 * Thrown by {@link ConstraintTransformer} when the {@link QuerySpec} contains an unsupported {@link Constraint}.
 * 
 * @author Javier Godoy / Flowing Code
 */
public class ConstraintTransformerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConstraintTransformerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstraintTransformerException(String message) {
		super(message);
	}

	public ConstraintTransformerException(Throwable cause) {
		super(cause);
	}

}
