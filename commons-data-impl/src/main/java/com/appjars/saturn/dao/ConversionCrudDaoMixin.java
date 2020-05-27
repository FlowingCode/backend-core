package com.appjars.saturn.dao;

import java.io.Serializable;

import com.appjars.saturn.conversion.ConversionSupport;
import com.appjars.saturn.model.Identifiable;

public interface ConversionCrudDaoMixin<S extends Serializable, T extends Identifiable<K>, K extends Serializable>
		extends CrudDao<S, K>, ConversionSupport<S, T> {

}
