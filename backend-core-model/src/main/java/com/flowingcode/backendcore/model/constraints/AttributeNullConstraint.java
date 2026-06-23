package com.flowingcode.backendcore.model.constraints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Deprecated(since = "1.2.0", forRemoval = false)
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeNullConstraint implements AttributeConstraint {

	@NonNull
	String attribute;
}
