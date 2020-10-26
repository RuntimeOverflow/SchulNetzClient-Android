package com.runtimeoverflow.SchulNetzClient.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class User {
	public transient Student self;
	
	public HashMap<String, String> lessonTypeMap = new HashMap<>();
	public HashMap<Integer, String> roomMap = new HashMap<>();
	
	public boolean balanceConfirmed = false;
	public ArrayList<Teacher> teachers = new ArrayList<>();
	public ArrayList<Student> students = new ArrayList<>();
	public ArrayList<Subject> subjects = new ArrayList<>();
	public ArrayList<Transaction> transactions = new ArrayList<>();
	public ArrayList<Absence> absences = new ArrayList<>();
	public ArrayList<Lesson> lessons = new ArrayList<>();
	public ArrayList<SubjectGroup> subjectGroups = new ArrayList<>();

	public static User load(){
		SharedPreferences prefs = Variables.get().currentContext.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);

		if(prefs.getString("user", null) == null) return null;

		Gson gson = new GsonBuilder().create();
		User user = gson.fromJson(prefs.getString("user", ""), User.class);
		
		if(user.teachers == null) user.teachers = new ArrayList<>();
		if(user.students == null) user.students = new ArrayList<>();
		if(user.subjects == null) user.subjects = new ArrayList<>();
		if(user.transactions == null) user.transactions = new ArrayList<>();
		if(user.absences == null) user.absences = new ArrayList<>();
		if(user.lessons == null) user.lessons = new ArrayList<>();
		if(user.subjectGroups == null) user.subjectGroups = new ArrayList<>();
		
		user.processConnections();
		return user;
	}

	public void save(){
		SharedPreferences prefs = Variables.get().currentContext.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(this);
		editor.putString("user", json);
		
		editor.commit();
	}
	
	public User copy(){
		Gson gson = new GsonBuilder().create();
		User copy = gson.fromJson(gson.toJson(this), User.class);
		copy.processConnections();
		return copy;
	}
	
	public void processConnections(){
		try{
			for(Teacher t : teachers) t.subjects = new ArrayList<>();
			
			for(Subject s : subjects){
				s.group = null;
				
				if(s.identifier != null && s.identifier.split("-").length == 3){
					Teacher t = teacherForShortName(s.identifier.split("-")[2]);
					
					if(t != null){
						t.subjects.add(s);
						s.teacher = t;
					}
				}
				
				for(Grade g : s.grades){
					g.subject = s;
				}
			}
			
			for(Student s : students){
				if(s.self){
					self = s;
					break;
				}
			}
			
			for(Absence a : absences){
				a.subjects = new ArrayList<>();
				
				for(String subjectIdentifier : a.subjectIdentifiers){
					if(subjectIdentifier == null) continue;
					
					Subject s = subjectForShortName(subjectIdentifier.split("-")[0]);
					
					if(s != null) a.subjects.add(s);
				}
			}
			
			for(Lesson l : lessons){
				if(l.lessonIdentifier != null && l.lessonIdentifier.split("-").length >= 3){
					Subject s = subjectForShortName(l.lessonIdentifier.split("-")[0]);
					if(s != null){
						//s.lessons.add(l);
						l.subject = s;
					}
					
					Teacher t = teacherForShortName(l.lessonIdentifier.split("-")[2]);
					if(t != null){
						//t.lessons.add(l);
						l.teacher = t;
					}
				}
				
				if(roomMap.containsKey(l.roomNumber)) l.room = roomMap.get(l.roomNumber);
			}
			
			for(SubjectGroup g : subjectGroups){
				g.subjects.clear();
				
				for(String identifier : g.subjectIdentifiers){
					Subject s = subjectForIdentifier(identifier);
					if(s != null){
						g.subjects.add(s);
						s.group = g;
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public Teacher teacherForShortName(String shortName){
		if(shortName == null) return null;
		for(Teacher t : teachers) if(t.shortName != null && Objects.equals(t.shortName.toLowerCase(), shortName.toLowerCase())) return t;

		return null;
	}

	public Subject subjectForShortName(String shortName){
		if(shortName == null) return null;
		for(Subject s : subjects) if(s.shortName != null && Objects.equals(s.shortName.toLowerCase(), shortName.toLowerCase())) return s;

		return null;
	}

	public Subject subjectForIdentifier(String identifier){
		if(identifier == null) return null;
		for(Subject s : subjects) if(s.identifier != null && Objects.equals(s.identifier.toLowerCase(), identifier.toLowerCase())) return s;

		return null;
	}
	
	public void processLessons(ArrayList<Lesson> lessons){
		for(Lesson l : lessons){
			if(l.lessonIdentifier != null && l.lessonIdentifier.split("-").length >= 3){
				Subject s = subjectForShortName(l.lessonIdentifier.split("-")[0]);
				if(s != null){
					l.subject = s;
				}
				
				Teacher t = teacherForShortName(l.lessonIdentifier.split("-")[2]);
				if(t != null){
					l.teacher = t;
				}
			}
			
			if(roomMap.containsKey(l.roomNumber)) l.room = roomMap.get(l.roomNumber);
		}
	}
}
