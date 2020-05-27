package com.appjars.saturn.dao;

import java.io.Serializable;

public interface UpdateDao<T extends Serializable> {

	void saveOrUpdate(T entity);

}
