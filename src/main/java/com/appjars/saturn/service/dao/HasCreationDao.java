package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.CreationDao;
import com.appjars.saturn.model.Identifiable;

public interface HasCreationDao<T extends Identifiable<K>, K extends Serializable> {
	
	CreationDao<T,K> getCreationDao();

}
