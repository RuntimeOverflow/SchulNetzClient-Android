package com.runtimeoverflow.SchulNetzClient.Data;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Lesson {
	public transient Subject subject;
	public transient Teacher teacher;
	public transient String room = "";
	
	public Calendar startDate;
	public Calendar endDate;
	public String lessionIdentifier;
	public int roomNumber;
	public int color;
	public String type;
	public String marking;
	public String replacementTeacher;
	
	public boolean longerThanOrEqualToOneDay(){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(startDate.getTimeInMillis());
		c.add(Calendar.DAY_OF_YEAR, 1);
		
		return !endDate.before(c);
	}
	
	public static ArrayList<Lesson> orderByStartTime(ArrayList<Lesson> lessons){
		if(lessons == null) return new ArrayList<>();
		ArrayList<Lesson> sorted = new ArrayList<>();
		
		for(Lesson lesson : lessons){
			if(lesson.startDate == null) continue;
			
			int index = sorted.size();
			for(int i = 0; i < sorted.size(); i++){
				if(sorted.get(i).startDate.after(lesson.startDate)){
					index = i;
					break;
				}
			}
			
			sorted.add(index, lesson);
		}
		
		return sorted;
	}
	
	public static ArrayList<Lesson> orderByEndTime(ArrayList<Lesson> lessons){
		if(lessons == null) return new ArrayList<>();
		ArrayList<Lesson> sorted = new ArrayList<>();
		
		for(Lesson lesson : lessons){
			if(lesson.endDate == null) continue;
			
			int index = sorted.size();
			for(int i = 0; i < sorted.size(); i++){
				if(sorted.get(i).endDate.after(lesson.endDate)){
					index = i;
					break;
				}
			}
			
			sorted.add(index, lesson);
		}
		
		return sorted;
	}
	
	public static class ScheduleLesson {
		public Lesson lesson;
		public Calendar start;
		public Calendar end;
		public int index;
		public int total;
		
		public static ArrayList<Lesson.ScheduleLesson> orderByStartTime(ArrayList<Lesson.ScheduleLesson> lessons){
			if(lessons == null) return new ArrayList<>();
			ArrayList<Lesson.ScheduleLesson> sorted = new ArrayList<>();
			
			for(Lesson.ScheduleLesson lesson : lessons){
				if(lesson.lesson.startDate == null) continue;
				
				int index = sorted.size();
				for(int i = 0; i < sorted.size(); i++){
					if(sorted.get(i).lesson.startDate.after(lesson.lesson.startDate)){
						index = i;
						break;
					}
				}
				
				sorted.add(index, lesson);
			}
			
			return sorted;
		}
		
		public static ArrayList<Lesson.ScheduleLesson> orderByEndTime(ArrayList<Lesson.ScheduleLesson> lessons){
			if(lessons == null) return new ArrayList<>();
			ArrayList<Lesson.ScheduleLesson> sorted = new ArrayList<>();
			
			for(Lesson.ScheduleLesson lesson : lessons){
				if(lesson.lesson.endDate == null) continue;
				
				int index = sorted.size();
				for(int i = 0; i < sorted.size(); i++){
					if(sorted.get(i).lesson.endDate.after(lesson.lesson.endDate)){
						index = i;
						break;
					}
				}
				
				sorted.add(index, lesson);
			}
			
			return sorted;
		}
	}
}
