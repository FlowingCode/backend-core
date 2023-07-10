/*-
 * #%L
 * Commons Backend - Business Implementations
 * %%
 * Copyright (C) 2020 - 2021 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.backendcore.service;

/**
 * Provides conversion logic between business and persistence layers
 * 
 * @author jgodoy
 * 
 * @param <B> The type of the business layer entity
 * @param <P> The type of the persistence layer entity
 */
public interface BusinessConversionSupport<B, P> {

	P convertToPersistence(B source);

	B convertToBusiness(P source);

	
	/**Specialization of {@code ConversionSupport} that performs an identity conversion
	 * @param <T> The type of the business layer and persistence layer entity */
	public interface Identity<T> extends BusinessConversionSupport<T, T> {
		
		@Override
		default T convertToBusiness(T source) {
			return source;
		}
		
		@Override
		default T convertToPersistence(T source) {		
			return source;
		}
	}
	
	
}
