package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.model.Identifiable;

public interface HasQueryDao<T extends Identifiable<K>, K extends Serializable> {

	QueryDao<T,K> getQueryDao();
	
}
