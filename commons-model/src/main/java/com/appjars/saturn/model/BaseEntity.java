package com.appjars.saturn.model;

import java.io.Serializable;

/**
 * Base class for entities
 * 
 * @author mlopez
 *
 * @param <K>
 */
@SuppressWarnings("serial")
public abstract class BaseEntity<K extends Serializable> implements Identifiable<K> {

	protected K id;

	@Override
	public void setId(K id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}

}
