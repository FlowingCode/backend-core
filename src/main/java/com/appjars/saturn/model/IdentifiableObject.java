package com.appjars.saturn.model;

import java.io.Serializable;

/**
 * Represents an object that can be identified
 * 
 * @author mlopez
 *
 * @param <K>
 */
public interface IdentifiableObject<K extends Serializable> extends Serializable {
	
	K getId();
	void setId(K id);
	
}
