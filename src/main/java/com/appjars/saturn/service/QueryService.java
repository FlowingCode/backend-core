package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.model.IdentifiableObject;
import com.appjars.saturn.service.dao.QueryDaoSupport;
import com.appjars.saturn.service.validation.ValidationSupport;

/**
 * A special kind of service that allows entities querying
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface QueryService<T extends IdentifiableObject<K>, K extends Serializable> extends ValidationSupport<T> {

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default T findById(K id) {
		if (this instanceof QueryDaoSupport) {
			return ((QueryDaoSupport<T, K>) this).getQueryDao().findById(id);
		} else {
			throw new ClassCastException("Class implementing QueryService must also implement QueryDaoSupport");
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default List<T> findAll() {
		if (this instanceof QueryDaoSupport) {
			return ((QueryDaoSupport<T, K>) this).getQueryDao().findAll();
		} else {
			throw new ClassCastException("Class implementing QueryService must also implement QueryDaoSupport");
		}
	}

}
