package com.flowingcode.backendcore.validation;

import java.util.List;

import com.flowingcode.backendcore.exception.BaseException;
import com.flowingcode.backendcore.model.ErrorDescription;

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
