package com.appjars.saturn.service.dao;

import java.io.Serializable;

import com.appjars.saturn.dao.UpdateDao;
import com.appjars.saturn.model.Identifiable;

public interface HasUpdateDao<T extends Identifiable<K>, K extends Serializable> {

	UpdateDao<T, K> getUpdateDao();

}
