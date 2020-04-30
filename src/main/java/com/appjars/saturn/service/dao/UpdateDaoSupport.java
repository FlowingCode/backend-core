package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.UpdateDao;
import com.appjars.saturn.model.IdentifiableObject;

public interface UpdateDaoSupport<T extends IdentifiableObject<K>, K extends Serializable> {

	UpdateDao<T, K> getUpdateDao();

}
