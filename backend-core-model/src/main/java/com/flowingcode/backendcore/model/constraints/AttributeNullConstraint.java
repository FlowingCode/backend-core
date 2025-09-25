package com.flowingcode.backendcore.model.constraints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Constraint indicating that the specified attribute's value must be null.
 *
 * @author jgodoy
 */
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeNullConstraint implements AttributeConstraint {

	@NonNull
	String attribute;
}
