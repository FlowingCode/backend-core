package com.appjars.saturn.validation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public interface ValidationSupport<T extends Serializable> {

	default List<Validator<T>> getValidators() {
		return Collections.emptyList();
	};

}
