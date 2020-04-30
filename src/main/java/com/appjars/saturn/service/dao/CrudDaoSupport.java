package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.dao.CrudDao;
import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.dao.RemovalDao;
import com.appjars.saturn.model.IdentifiableObject;

public interface CrudDaoSupport<T extends IdentifiableObject<K>, K extends Serializable>
		extends CreationDaoSupport<T, K>, RemovalDaoSupport<T, K>, QueryDaoSupport<T, K> {

	default CreationDao<T, K> getCreationDao() {
		return getCrudDao();
	}

	default RemovalDao<T, K> getRemovalDao() {
		return getCrudDao();
	}

	default QueryDao<T, K> getQueryDao() {
		return getCrudDao();
	}

	CrudDao<T, K> getCrudDao();

}
