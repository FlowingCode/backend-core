package com.appjars.saturn.model.constraints;

import com.appjars.saturn.model.Constraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NegatedConstraint implements Constraint {

	@NonNull Constraint constraint;
		
}
