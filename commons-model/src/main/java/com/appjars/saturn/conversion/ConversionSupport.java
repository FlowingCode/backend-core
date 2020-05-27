package com.appjars.saturn.conversion;

/**
 * Service for generic conversion logic
 * 
 * @author mlopez
 * 
 */
public interface ConversionSupport<S, T> {

	T convertTo(S source);

	S convertFrom(T source);

}
