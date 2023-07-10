package com.flowingcode.backendcore.validation;

import java.util.List;

import com.flowingcode.backendcore.exception.BaseException;
import com.flowingcode.backendcore.model.ErrorDescription;

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
