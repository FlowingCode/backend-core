package com.appjars.saturn.model.constraints;

import com.appjars.saturn.model.Constraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

public interface RelationalConstraint extends Constraint {

	String EQ = "=";
	String NE = "<>";
	String LT = "<";
	String LE = "<=";
	String GT = ">";
	String GE = ">=";
		
}
