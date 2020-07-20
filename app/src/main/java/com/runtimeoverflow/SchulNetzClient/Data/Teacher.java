package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Teacher {
	public transient ArrayList<Subject> subjects = new ArrayList<>();
	public transient ArrayList<Lesson> lessons = new ArrayList<>();

	public String firstName;
	public String lastName;
	public String shortName;
	public String mail;
}
