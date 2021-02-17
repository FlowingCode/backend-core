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

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for entities
 * 
 * @author mlopez
 *
 * @param <K>
 */
@SuppressWarnings("serial")
public abstract class BaseEntity<K extends Serializable> implements Identifiable<K> {

	@Override
	public abstract void setId(K id);
	
	@Override
	public abstract K getId();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(getId());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identifiable<K> other = (Identifiable<K>) obj;
		return Objects.equals(getId(), other.getId());
	}
	
	@Override
	public String toString() {
		K id = getId();
		if (id!=null) {
			return this.getClass().getName()+"@"+id;
		} else {
			return this.getClass().getName()+"@transient:"+Integer.toHexString(System.identityHashCode(this));
		}
	}

}
