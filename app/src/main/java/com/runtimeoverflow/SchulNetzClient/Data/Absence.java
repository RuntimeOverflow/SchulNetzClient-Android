package com.runtimeoverflow.SchulNetzClient.Data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class Absence {
	public Calendar startDate;
	public Calendar endDate;
	public String reason;
	public String additionalInformation;
	public int lessonCount;
	public boolean excused;
	
	public ArrayList<String> subjectIdentifiers = new ArrayList<>();
	
	public transient ArrayList<Subject> subjects = new ArrayList<>();
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Absence absence = (Absence) o;
		return Objects.equals(reason, absence.reason);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(reason);
	}
}
