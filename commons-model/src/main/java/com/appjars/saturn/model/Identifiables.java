/*-
 * #%L
 * Commons Backend - Model
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
package com.appjars.saturn.model;

import lombok.experimental.UtilityClass;

/**
 * Utility methods for {@link Identifiable} objects. 
 * 
 * @author Javier Godoy / Flowing Code
 *
 */
@UtilityClass
public class Identifiables {

	/** Return the hash code of an {@code Identifiable}*/
	public static <K> int hashCode(Identifiable<K> o) {
		if (o != null) {
			K id = o.getId();
			if (id != null) {
				return id.hashCode();
			} else {
				return System.identityHashCode(o);
			}
		} else {
			return 0;
		}
	}
	
	/** Compare two {@code Identifiable} objects for equality. 
	 * 
	 * The result is {@code true} iff: <ul>
	 * <li>Both arguments are instances of the same class, and <ul>
	 * <li>Either both arguments refer to the same object ({@code a==b}), or
	 * they both have non-null {@link Identifiable#getId() id} such that 
	 * {@code a.id} {@link #equals(Object) is equal to} {@code b.id}.
	 */
	public static boolean equals(Identifiable<?> a, Object b) {
		
		if (a == b) {
			return true;
		}
		if (b == null) {
			return false;
		}
		if (a == null || a.getId() == null) {
			return false;
		}
		if (a.getClass() != b.getClass()) {
			return false;
		}		
		Identifiable<?> other = (Identifiable<?>) b;
		return a.getId().equals(other.getId());
	}

	/** Return a string representation of the {@code Identifiable}.*/
	public static String toString(Identifiable<?> obj) {
		Object id = obj.getId();
		if (id!=null) {
			return obj.getClass().getName()+"@"+id;
		} else {
			return obj.getClass().getName()+"@transient:"+Integer.toHexString(System.identityHashCode(obj));
		}
	}
	
}
