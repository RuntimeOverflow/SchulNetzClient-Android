package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Objects;

public class Student {
	public String firstName;
	public String lastName;
	public boolean gender;
	public String degree;
	public boolean bilingual;
	public String className;
	public String address;
	public int zipCode;
	public String city;
	public String phone;
	public Calendar dateOfBirth;
	public String additionalClasses;
	public String status;
	public String placeOfWork;
	public boolean self;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Student student = (Student) o;
		return Objects.equals(firstName, student.firstName) &&
				Objects.equals(lastName, student.lastName) &&
				dateOfBirth.getTimeInMillis() == student.dateOfBirth.getTimeInMillis();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, dateOfBirth);
	}
}
