package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Objects;

public class Teacher {
	public transient ArrayList<Subject> subjects = new ArrayList<>();
	public transient ArrayList<Lesson> lessons = new ArrayList<>();

	public String firstName;
	public String lastName;
	public String shortName;
	public String mail;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Teacher teacher = (Teacher) o;
		return Objects.equals(shortName, teacher.shortName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(shortName);
	}
}
