package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.DeletionDao;
import com.appjars.saturn.model.IdentifiableObject;

public interface DeletionDaoSupport<T extends IdentifiableObject<K>, K extends Serializable> {

	DeletionDao<T, K> getDeletionDao();

}
