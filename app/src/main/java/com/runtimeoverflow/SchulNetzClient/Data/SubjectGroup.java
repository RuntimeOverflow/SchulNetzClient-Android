package com.runtimeoverflow.SchulNetzClient.Data;

import java.util.ArrayList;

public class SubjectGroup {
	public transient ArrayList<Subject> subjects = new ArrayList<>();
	
	public ArrayList<String> subjectIdentifiers = new ArrayList<>();
	public int roundOption = 1;
	public String name = "";
	
	public double getGrade(){
		if(roundOption == 0){
			return Double.NaN;
		} else if(roundOption == 1){
			double total = 0;
			int count = 0;
			for(Subject s : subjects) {
				double average = s.getAverage();
				if(!Double.isNaN(average) && average >= 1){
					total += average;
					count++;
				}
			}
			
			return total / count;
		} else if(roundOption == 2){
			double total = 0;
			int count = 0;
			for(Subject s : subjects) {
				double average = s.getAverage();
				if(!Double.isNaN(average) && average >= 1){
					total += Math.round(average * 2.0) / 2.0;
					count++;
				}
			}
			
			return total / count;
		}
		
		return Double.NaN;
	}
}
