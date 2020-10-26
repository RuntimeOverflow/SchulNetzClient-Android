package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Objects;

public class Subject {
	public transient Teacher teacher;
	public transient ArrayList<Lesson> lessons = new ArrayList<>();
	public transient SubjectGroup group = null;

	public ArrayList<Grade> grades = new ArrayList<>();
	public String identifier;
	public String name;
	public String shortName;
	public boolean confirmed;
	public boolean hiddenGrades;
	public boolean unvalued = false;

	public double getAverage(){
		double total = 0;
		double count = 0;
		for(Grade g : grades){
			if(g.grade > 0){
				count += g.weight;
				total += g.weight * g.grade;
			}
		}

		return total / count;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Subject subject = (Subject) o;
		return Objects.equals(shortName, subject.shortName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(shortName);
	}
}
