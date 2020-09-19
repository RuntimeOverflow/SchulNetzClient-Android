package com.runtimeoverflow.SchulNetzClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.runtimeoverflow.SchulNetzClient.Data.Absence;
import com.runtimeoverflow.SchulNetzClient.Data.Change;
import com.runtimeoverflow.SchulNetzClient.Data.Grade;
import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Transaction;
import com.runtimeoverflow.SchulNetzClient.Data.User;

import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BackgroundWorker extends Worker {
	public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}
	
	@NonNull
	@Override
	public Result doWork() {
		if(Utilities.isInForeground(getApplicationContext())) return Result.retry();
		
		Variables.get().currentContext = getApplicationContext();
		
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		if(prefs == null || prefs.getString("host", "").length() <= 0 || prefs.getString("username", "").length() <= 0 || prefs.getString("password", "").length() <= 0) return Result.success();
		
		User previous = User.load();
		User user = new User();
		Account account = new Account(prefs.getString("host", null), prefs.getString("username", null), prefs.getString("password", null), false);
		
		Object res = account.signIn();
		if(res == null || !((res.getClass() == boolean.class && (boolean)res) || (res.getClass() == Boolean.class && (Boolean)res))) return Result.retry();
		
		Object doc = account.loadPage("22352");
		if(doc != null && doc.getClass() == Document.class) Parser.parseTeachers((Document)doc, user);
		else user.teachers = previous.teachers;
		doc = account.loadPage("22326");
		if(doc != null && doc.getClass() == Document.class) Parser.parseSubjects((Document)doc, user);
		else user.subjects = previous.subjects;
		if(doc != null && doc.getClass() == Document.class) Parser.parseStudents((Document)doc, user);
		else user.students = previous.students;
		doc = account.loadPage("21311");
		if(doc != null && doc.getClass() == Document.class) Parser.parseGrades((Document)doc, user);
		else user.subjects = previous.subjects;
		doc = account.loadPage("21411");
		if(doc != null && doc.getClass() == Document.class) Parser.parseSelf((Document)doc, user);
		else{
			for(Student s : user.students){
				if(s.firstName.toLowerCase().equals(previous.self.firstName.toLowerCase()) && s.lastName.toLowerCase().equals(previous.self.lastName.toLowerCase())){
					s.self = true;
					break;
				}
			}
		}
		if(doc != null && doc.getClass() == Document.class) Parser.parseTransactions((Document)doc, user);
		else user.transactions = previous.transactions;
		doc = account.loadPage("21111");
		if(doc != null && doc.getClass() == Document.class) Parser.parseAbsences((Document)doc, user);
		else user.absences = previous.absences;
		doc = account.loadPage("22202");
		if(doc != null && doc.getClass() == Document.class) Parser.parseSchedulePage((Document)doc, user);
		else{
			user.lessonTypeMap = previous.lessonTypeMap;
			user.roomMap = previous.roomMap;
		}
		
		doc = account.loadSchedule(Calendar.getInstance(), Calendar.getInstance());
		if(doc != null && doc.getClass() == Document.class) user.lessons = Parser.parseSchedule((Document)doc);
		
		user.processConnections();
		account.signOut();
		
		ArrayList<Change<?>> changes = Change.getChanges(previous, user);
		user.save();
		
		Change.publishNotifications(changes);
		
		return Result.success();
	}
}
