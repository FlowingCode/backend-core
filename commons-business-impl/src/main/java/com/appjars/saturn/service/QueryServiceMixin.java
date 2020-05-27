package com.appjars.saturn.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.appjars.saturn.dao.QueryDao;
import com.appjars.saturn.model.QuerySpec;

/**
 * A special kind of service that allows entities querying
 * 
 * @author mlopez
 *
 * @param <T>
 * @param <K>
 */
public interface QueryServiceMixin<T extends Serializable, K extends Serializable> extends QueryService<T, K> {

	QueryDao<T, K> getQueryDao();

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default Optional<T> findById(K id) {
		return getQueryDao().findById(id);
	}

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default List<T> findAll() {
		return getQueryDao().findAll();
	}

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default List<T> filter(QuerySpec<K> filter) {
		return getQueryDao().filter(filter);
	}

	@Transactional(value = TxType.REQUIRED, rollbackOn = Exception.class)
	@Override
	default long count(QuerySpec<K> filter) {
		return getQueryDao().count(filter);
	}

}
