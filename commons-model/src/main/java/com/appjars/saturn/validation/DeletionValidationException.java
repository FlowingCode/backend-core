package com.appjars.saturn.validation;

import java.util.List;
import com.appjars.saturn.exception.BaseException;
import com.appjars.saturn.model.ErrorDescription;

@SuppressWarnings("serial")
public class DeletionValidationException extends ValidationException {

  private DeletionValidationException(ErrorDescription error) {
    super(null, error);
  }
  
  public DeletionValidationException(List<ErrorDescription> errors) {
    super(errors);
  }

  @Override
  protected BaseException newInstance(ErrorDescription error) {
      return new DeletionValidationException(error);
  }

}
