package com.appjars.saturn.validation;

import java.util.List;
import com.appjars.saturn.exception.BaseException;
import com.appjars.saturn.model.ErrorDescription;

@SuppressWarnings("serial")
public class CreationValidationException extends ValidationException {

  private CreationValidationException(ErrorDescription error) {
    super(null, error);
  }
  
  public CreationValidationException(List<ErrorDescription> errors) {
    super(errors);
  }

  @Override
  protected BaseException newInstance(ErrorDescription error) {
      return new CreationValidationException(error);
  }

}
