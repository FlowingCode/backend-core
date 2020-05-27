package com.appjars.saturn.dao;

import java.io.Serializable;

public interface CreationDao<T extends Serializable, K extends Serializable> {

	K save(T entity);

}
