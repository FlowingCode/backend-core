package com.appjars.saturn.model;

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

	public Constraint(String attribute, Object valueStart, Object valueEnd) {
		super();
		this.attribute = attribute;
		this.type = Type.BETWEEN;
		this.value = valueStart;
		this.valueEnd = valueEnd;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValueEnd() {
		return valueEnd;
	}

	public void setValueEnd(Object valueEnd) {
		this.valueEnd = valueEnd;
	}

}
