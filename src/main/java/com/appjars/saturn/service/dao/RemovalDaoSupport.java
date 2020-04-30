package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.RemovalDao;
import com.appjars.saturn.model.IdentifiableObject;

public interface RemovalDaoSupport<T extends IdentifiableObject<K>, K extends Serializable> {

	RemovalDao<T,K> getRemovalDao();
	
}
