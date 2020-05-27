package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.model.Identifiable;

public interface EntityConversionCrudDaoMixin<T extends Identifiable<K>, K extends Serializable>
		extends ConversionCrudDaoMixin<T, T, K> {

	@Override
	default T convertTo(T source) {
		return source;
	}

	@Override
	default T convertFrom(T source) {
		return source;
	}

}
