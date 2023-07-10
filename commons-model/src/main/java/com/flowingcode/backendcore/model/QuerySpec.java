/*-
 * #%L
 * Commons Backend - Model
 * %%
 * Copyright (C) 2020 - 2021 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.backendcore.model;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class QuerySpec {

	public enum Order {
		ASC, DESC;
	}

	@Getter
	@Setter
	private String[] returnedAttributes;

	@Getter
	@Setter
	private Map<String, Order> orders;

	private Integer firstResult;

	private Integer maxResult;

	@Getter
	private final Collection<Constraint> constraints = new ArrayList<>();

	public void addOrder(String property, Order order) {
		if (this.orders == null) {
			this.orders = new LinkedHashMap<>();
		}
		this.orders.put(property, order);
	}

	public void addOrder(String property) {
		addOrder(property, Order.ASC);
	}
	
	public void addConstraint(Constraint constraint) {
		this.constraints.add(constraint);
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

	/**
     * Return the position of the first result to retrieve.
     * @return the position of the first result to retrieve (numbered from 0) or null.
     * @see {@link #setFirstResult(Integer)}
     */
	public Integer getFirstResult() {
      return firstResult;
    }
	
	/**
     * Return the maximum number of results to retrieve.
     * @return the maximum number of results to retrieve, or null.
     */
	public Integer getMaxResult() {
      return maxResult;
    }

    /**
     * Set the position of the first result to retrieve.
     * @param startPosition position of the first result, 
     *        numbered from 0 (may be null)
     * @return the same query instance
     * @throws IllegalArgumentException if the argument is negative
     */
	public void setFirstResult(Integer firstResult) {
	  if (firstResult!=null && firstResult<0) throw new IllegalArgumentException();
      this.firstResult = firstResult;
    }
	
    /**
     * Set the maximum number of results to retrieve.
     * @param maxResult  maximum number of results to retrieve (may be null)
     * @throws IllegalArgumentException if the argument is negative
     */
	public void setMaxResult(Integer maxResult) {
	  if (maxResult!=null && maxResult<0) throw new IllegalArgumentException();
      this.maxResult = maxResult;
    }
}
