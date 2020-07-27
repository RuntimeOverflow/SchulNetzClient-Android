package com.runtimeoverflow.SchulNetzClient;

import android.content.Context;

import com.runtimeoverflow.SchulNetzClient.Data.User;

import java.util.Calendar;

public class Variables {
	private static Variables instance;
	
	public Context currentContext;
	
	public Account account;
	public User user;
	
	public Calendar timetableDate = Calendar.getInstance();
	
	public Object activityParameter = null;
	
	private Variables(){}
	
	public static Variables get() {
		if(instance == null){
			instance = new Variables();
		}
		
		return instance;
	}
}
