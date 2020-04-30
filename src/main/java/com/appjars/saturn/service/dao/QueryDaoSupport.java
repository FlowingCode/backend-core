package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.model.IdentifiableObject;

public interface QueryDaoSupport<T extends IdentifiableObject<K>, K extends Serializable> {

	QueryDao<T,K> getQueryDao();
	
}
