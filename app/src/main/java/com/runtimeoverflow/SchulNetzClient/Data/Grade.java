package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class Grade {
	public transient Subject subject;

	public Calendar date;
	public String content;
	public double grade;
	public String details;
	public double weight;
}
