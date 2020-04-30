package com.appjars.saturn.service;

import java.io.Serializable;

import com.appjars.saturn.model.IdentifiableObject;

public interface CrudService<T extends IdentifiableObject<K>, K extends Serializable> extends CreationService<T,K>, RemovalService<T,K>, QueryService<T,K> {

}
