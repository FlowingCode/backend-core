package com.appjars.saturn.model.constraints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeRelationalConstraint implements AttributeConstraint, RelationalConstraint {
	
	@NonNull String attribute;
	@NonNull Comparable<?> value;
	@NonNull String operator;
		
}
