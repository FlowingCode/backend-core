package com.appjars.saturn.model;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseFilter<K extends Serializable> {

	public enum Order {
		ASC, DESC;
	}

	private K[] excludeIds;

	private String[] returnedAttributes;

	private String[] eagerRelationShips;

	private Map<String, Order> orders;

	private Integer firstResult;

	private Integer maxResult;

	private Map<String, String> aliases = new HashMap<>();

	private Map<String, String> leftAliases = new HashMap<>();

	public K[] getExcludeIds() {
		return excludeIds;
	}

	public void setExcludeIds(K[] excludeIds) {
		this.excludeIds = excludeIds;
	}

	public String[] getReturnedAttributes() {
		return returnedAttributes;
	}

	public void setReturnedAttributes(String[] returnedAttributes) {
		this.returnedAttributes = returnedAttributes;
	}

	public Map<String, Order> getOrders() {
		return orders;
	}

	public void setOrders(Map<String, Order> orders) {
		this.orders = orders;
	}

	public void addOrder(String property, Order order) {
		if (this.orders == null) {
			this.orders = new LinkedHashMap<String, Order>();
		}
		this.orders.put(property, order);
	}

	public void addOrder(String property) {
		addOrder(property, Order.ASC);
	}

	public String[] getEagerRelationShips() {
		return eagerRelationShips;
	}

	public void setEagerRelationShips(String[] eagerRelationShips) {
		this.eagerRelationShips = eagerRelationShips;
	}

	public Integer getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
	}

	public Integer getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(Integer maxResult) {
		this.maxResult = maxResult;
	}

	public BaseFilter<K> addAlias(String associationPath, String alias) {
		this.aliases.put(associationPath, alias);
		return this;
	}

	public Map<String, String> getAliases() {
		return aliases;
	}

	public BaseFilter<K> addLeftAlias(String associationPath, String alias) {
		this.leftAliases.put(associationPath, alias);
		return this;
	}

	public Map<String, String> getLeftAliases() {
		return leftAliases;
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
