package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.model.BaseFilter;
import com.appjars.saturn.model.Identifiable;
import com.appjars.saturn.service.dao.HasQueryDao;
import com.appjars.saturn.service.validation.ValidationSupport;

/**
 * A special kind of service that allows entities querying
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface QueryService<T extends Identifiable<K>, K extends Serializable> extends ValidationSupport<T> {

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default T findById(K id) {
		if (this instanceof HasQueryDao) {
			return ((HasQueryDao<T, K>) this).getQueryDao().findById(id);
		} else {
			throw new ClassCastException("Class implementing QueryService must also implement HasQueryDao");
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default List<T> findAll() {
		if (this instanceof HasQueryDao) {
			return ((HasQueryDao<T, K>) this).getQueryDao().findAll();
		} else {
			throw new ClassCastException("Class implementing QueryService must also implement HasQueryDao");
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default List<T> filter(BaseFilter<K> filter) {
		if (this instanceof HasQueryDao) {
			return ((HasQueryDao<T, K>) this).getQueryDao().filter(filter);
		} else {
			throw new ClassCastException("Class implementing QueryService must also implement HasQueryDao");
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	default long count(BaseFilter<K> filter) {
		if (this instanceof HasQueryDao) {
			return ((HasQueryDao<T, K>) this).getQueryDao().count(filter);
		} else {
			throw new ClassCastException("Class implementing QueryService must also implement HasQueryDao");
		}
	}

}
