package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.model.IdentifiableObject;

public interface CrudDao<T extends IdentifiableObject<K>, K extends Serializable>
		extends CreationDao<T, K>, UpdateDao<T, K>, DeletionDao<T, K>, QueryDao<T, K> {

}
