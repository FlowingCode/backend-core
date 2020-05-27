package com.appjars.saturn.dao;

import java.io.Serializable;

public interface CrudDao<T extends Serializable, K extends Serializable>
		extends CreationDao<T, K>, UpdateDao<T>, DeletionDao<T>, QueryDao<T, K> {

}
