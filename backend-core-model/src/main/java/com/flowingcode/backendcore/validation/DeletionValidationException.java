package com.flowingcode.backendcore.validation;

import java.util.List;

import com.flowingcode.backendcore.exception.BaseException;
import com.flowingcode.backendcore.model.ErrorDescription;

/**
 * Thrown to indicate validation failures during delete operations.
 *
 * @author mlopez
 */
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
