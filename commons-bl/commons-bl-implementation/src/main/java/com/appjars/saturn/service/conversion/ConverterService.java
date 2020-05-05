package com.appjars.saturn.service.conversion;

/**
 * Service for generic conversion logic
 * 
 * @author mlopez
 * 
 */
public interface ConverterService {

	<S, T> T convertTo(S source, Class<T> targetClass);
	<S, T> boolean canConvertTo(Class<S> sourceClass, Class<T> targetClass);
	
}
