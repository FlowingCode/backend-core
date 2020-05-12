package com.appjars.saturn.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Constraint {

	public enum Type {
		EQUALS, NOTEQUALS, LIKE, BETWEEN
	}

	private String attribute;
	private Type type;
	private Object value;
	private Object valueEnd;

	public Constraint(String attribute, Type type, Object value) {
		super();
		this.attribute = attribute;
		this.type = type;
		this.value = value;
	}

	public <T> Constraint(String attribute, T valueStart, T valueEnd) {
		super();
		this.attribute = attribute;
		this.type = Type.BETWEEN;
		this.value = valueStart;
		this.valueEnd = valueEnd;
	}

}
