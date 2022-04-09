package com.appjars.saturn.validation;

import java.util.List;
import com.appjars.saturn.exception.BaseException;
import com.appjars.saturn.model.ErrorDescription;

@SuppressWarnings("serial")
public class UpdateValidationException extends ValidationException {

  private UpdateValidationException(ErrorDescription error) {
    super(null, error);
  }
  
  public UpdateValidationException(List<ErrorDescription> errors) {
    super(errors);
  }

  @Override
  protected BaseException newInstance(ErrorDescription error) {
      return new UpdateValidationException(error);
  }

}
