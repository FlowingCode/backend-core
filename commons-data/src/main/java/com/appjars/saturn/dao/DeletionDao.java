package com.appjars.saturn.dao;

import java.io.Serializable;

public interface DeletionDao<T extends Serializable> {

	void delete(T entity);

}
