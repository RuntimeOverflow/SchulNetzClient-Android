package com.runtimeoverflow.SchulNetzClient.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.util.ArrayList;
import java.util.Objects;

public class User {
	public transient Student self;

	public boolean balanceConfirmed;
	public ArrayList<Teacher> teachers = new ArrayList<>();
	public ArrayList<Student> students = new ArrayList<>();
	public ArrayList<Subject> subjects = new ArrayList<>();
	public ArrayList<Transaction> transactions = new ArrayList<>();
	public ArrayList<Lesson> lessons = new ArrayList<>();

	public static User load(){
		SharedPreferences prefs = Variables.get().currentContext.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);

		if(!prefs.getAll().containsKey("user")) return new User();

		Gson gson = new GsonBuilder().create();
		User user = gson.fromJson(prefs.getString("user", null), User.class);
		
		user.processConnections();
		return user;
	}

	public void save(){
		SharedPreferences prefs = Variables.get().currentContext.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(this);
		editor.putString("user", json);

		editor.apply();
	}
	
	public void processConnections(){
		for(Teacher t : teachers) t.subjects = new ArrayList<>();
		
		for(Subject s : subjects){
			if(s.identifier.split("-").length == 3){
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
			s.self = false;
			
			if(s.self){
				self = s;
			}
		}
	}

	public Teacher teacherForShortName(String shortName){
		for(Teacher t : teachers){
			if(Objects.equals(t.shortName.toLowerCase(), shortName.toLowerCase())) return t;
		}

		return null;
	}

	public Subject subjectForShortName(String shortName){
		for(Subject s : subjects){
			if(Objects.equals(s.shortName.toLowerCase(), shortName.toLowerCase())) return s;
		}

		return null;
	}

	public Subject subjectForIdentifier(String identifier){
		for(Subject s : subjects){
			if(Objects.equals(s.identifier.toLowerCase(), identifier.toLowerCase())) return s;
		}

		return null;
	}
}
