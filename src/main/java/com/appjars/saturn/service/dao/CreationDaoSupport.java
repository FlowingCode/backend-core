package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.model.IdentifiableObject;

public interface CreationDaoSupport<T extends IdentifiableObject<K>, K extends Serializable> {
	
	CreationDao<T,K> getDao();

}
