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
package com.appjars.saturn.model;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuerySpec {

	public enum Order {
		ASC, DESC;
	}

	private String[] returnedAttributes;

	private Map<String, Order> orders;

	private Integer firstResult;

	private Integer maxResult;

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

}
