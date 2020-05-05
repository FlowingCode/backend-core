package com.appjars.saturn.model;

import java.time.LocalDate;

@SuppressWarnings("serial")
public class Person extends BaseEntity<Integer> {

	private String name;
	private String lastName;
	private LocalDate birthDay;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public LocalDate getBirthDay() {
		return birthDay;
	}
	public void setBirthDay(LocalDate birthDay) {
		this.birthDay = birthDay;
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	
}
