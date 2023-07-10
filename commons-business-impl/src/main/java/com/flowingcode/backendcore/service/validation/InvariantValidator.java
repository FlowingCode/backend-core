package com.flowingcode.backendcore.service.validation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.flowingcode.backendcore.model.ErrorDescription;
import com.flowingcode.backendcore.service.validation.CreationValidator;
import com.flowingcode.backendcore.service.validation.UpdateValidator;
import com.flowingcode.backendcore.validation.Validator;

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

  static <T> InvariantValidator<T> forCondition(Predicate<T> predicate, String messageKey) {
    Objects.requireNonNull(messageKey);
    return forCondition(predicate, t->new ErrorDescription(messageKey));
  }

}
