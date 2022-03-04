package com.appjars.saturn.service.validation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.appjars.saturn.model.ErrorDescription;
import com.appjars.saturn.service.validation.CreationValidator;
import com.appjars.saturn.service.validation.UpdateValidator;
import com.appjars.saturn.validation.Validator;

public interface InvariantValidator<T> extends CreationValidator<T>, UpdateValidator<T> {

  default InvariantValidator<T> and(InvariantValidator<T> then) {
    return t -> {
      List<ErrorDescription> result = this.validate(t);
      if (result.isEmpty()) {
        result = then.validate(t);
      }
      return result;
    };
  }

  static <T> InvariantValidator<T> forCondition(Predicate<T> predicate, Function<T, ErrorDescription> errorSupplier) {
    Objects.requireNonNull(predicate);
    Objects.requireNonNull(errorSupplier);
    return t->predicate.test(t)?Validator.success():Collections.singletonList(errorSupplier.apply(t));
  }

}
