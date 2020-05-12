package com.appjars.saturn.model;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuerySpec<K extends Serializable> {

	public enum Order {
		ASC, DESC;
	}

	private Collection<K> excludeIds;

	private String[] returnedAttributes;

	private Map<String, Order> orders;

	private Integer firstResult;

	private Integer maxResult;

	private Set<Constraint> constraints = new HashSet<>();

	public void addOrder(String property, Order order) {
		if (this.orders == null) {
			this.orders = new LinkedHashMap<>();
		}
		this.orders.put(property, order);
	}

	public void addOrder(String property) {
		addOrder(property, Order.ASC);
	}
	
	public void addEqualsConstraint(String attribute, Object value) {
		this.constraints.add(new Constraint(attribute, Constraint.Type.EQUALS, value));
	}

	public void addNotEqualsConstraint(String attribute, Object value) {
		this.constraints.add(new Constraint(attribute, Constraint.Type.EQUALS, value));
	}

	public void addLikeConstraint(String attribute, String value) {
		this.constraints.add(new Constraint(attribute, Constraint.Type.LIKE, value));
	}

	public void addBetweenConstraint(String attribute, Object valueStart, Object valueEnd) {
		this.constraints.add(new Constraint(attribute, valueStart, valueEnd));
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getName());
		try {
			buffer.append("{ ");
			BeanInfo info = Introspector.getBeanInfo(getClass(), Object.class);
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				String value = String.valueOf((pd.getReadMethod() != null ? pd.getReadMethod().invoke(this) : "?"));
				buffer.append(" [" + pd.getName() + "=" + value + "]");
			}
			buffer.append(" }");
		} catch (Exception e) {
			buffer.append("Error: " + e.getMessage());
		}
		return buffer.toString();
	}

}
