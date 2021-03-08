package com.appjars.saturn.validation;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.appjars.saturn.model.ErrorDescription;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Javier Godoy
 */
@RequiredArgsConstructor
public class ValidatorBuilder<T> {

	@RequiredArgsConstructor
	private final static class ValidatorDelegate implements InvocationHandler, Serializable {
		
		final Validator<?> delegate;
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(delegate, args);
		}
	};
	
	@NonNull
	private final ValidationKind[] validationKinds;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Validator<T> forCondition(Predicate<T> predicate, Function<T, ErrorDescription> errorSupplier) {
		Validator<T> validator = Validator.forCondition(predicate, errorSupplier);
		Class<?>[] interfaces = Stream.of(validationKinds)
				.map(ValidationKind::getValidatorType)
				.map(clazz->((Class)clazz).asSubclass(Validator.class))
				.distinct().toArray(Class[]::new); 
		return (Validator<T>) Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, new ValidatorDelegate(validator));
	}
	
}
