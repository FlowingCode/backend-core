package com.appjars.saturn.model.constraints;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeInConstraint implements AttributeConstraint {

	@NonNull String attribute;
	@NonNull Collection<?> values;
	
}
