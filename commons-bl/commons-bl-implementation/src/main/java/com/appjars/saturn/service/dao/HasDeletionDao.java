package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.DeletionDao;
import com.appjars.saturn.model.Identifiable;

public interface HasDeletionDao<T extends Identifiable<K>, K extends Serializable> {

	DeletionDao<T, K> getDeletionDao();

}
