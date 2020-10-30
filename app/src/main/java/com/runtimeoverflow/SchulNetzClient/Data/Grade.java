package com.runtimeoverflow.SchulNetzClient.Data;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Objects;

public class Grade {
	public transient Subject subject;

	public Calendar date;
	public String content;
	public double grade;
	public String details;
	public double weight;
	
	public static int colorForGrade(double grade){
		int r = 0;
		int g = 0;
		int b = 0;
		
		if(grade <= 6 && grade >= 4){
			double positiveImpact = (grade - 4) / 2;
			double negativeImpact = 1 - positiveImpact;
			
			r = (int)Math.round(negativeImpact * 255);
			g = (int)Math.round(negativeImpact * 255 + positiveImpact * 255);
		} else if(grade <= 4 && grade >= 1){
			double positiveImpact = (grade - 1) / 3;
			double negativeImpact = 1 - positiveImpact;
			
			r = (int)Math.round(negativeImpact * 255 + positiveImpact * 255);
			g = (int)Math.round(positiveImpact * 127);
		}
		
		return Color.argb(255, r, g, b);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Grade grade = (Grade) o;
		return Objects.equals(content, grade.content) && date.getTimeInMillis() == grade.date.getTimeInMillis();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(content);
	}
}
