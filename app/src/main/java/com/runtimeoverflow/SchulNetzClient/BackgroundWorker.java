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
		
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		if(prefs == null || prefs.getString("host", "").length() <= 0 || prefs.getString("username", "").length() <= 0 || prefs.getString("password", "").length() <= 0) return Result.success();
		
		User user = new User();
		Account account = new Account(prefs.getString("host", null), prefs.getString("username", null), prefs.getString("password", null), false);
		
		account.signIn();
		Object doc = account.loadPage("22352");
		if(doc.getClass() == Document.class) Parser.parseTeachers((Document)doc, user);
		doc = account.loadPage("22326");
		if(doc.getClass() == Document.class) Parser.parseSubjects((Document)doc, user);
		if(doc.getClass() == Document.class) Parser.parseStudents((Document)doc, user);
		doc = account.loadPage("21311");
		if(doc.getClass() == Document.class) Parser.parseGrades((Document)doc, user);
		doc = account.loadPage("21411");
		if(doc.getClass() == Document.class) Parser.parseSelf((Document)doc, user);
		if(doc.getClass() == Document.class) Parser.parseTransactions((Document)doc, user);
		doc = account.loadPage("21111");
		if(doc.getClass() == Document.class) Parser.parseAbsences((Document)doc, user);
		doc = account.loadPage("22202");
		if(doc.getClass() == Document.class) Parser.parseSchedulePage((Document)doc, user);
		
		doc = account.loadSchedule(Calendar.getInstance(), Calendar.getInstance());
		if(doc.getClass() == Document.class) user.lessons = Parser.parseSchedule((Document)doc);
		
		user.processConnections();
		account.signOut();
		
		ArrayList<Change<?>> changes = Change.getChanges(Variables.get().user, user);
		
		Variables.get().user = user;
		user.save();
		
		if(prefs.getBoolean("notificationsEnabled", true)) for(Change<?> change : changes){
			Class<?> c = null;
			
			if(change.previous != null) c = change.previous.getClass();
			else if(change.current != null) c = change.current.getClass();
			else continue;
			
			if(c == Grade.class){
				if(change.type == Change.ChangeType.ADDED || (change.type == Change.ChangeType.MODIFIED && change.varName.equals("grade") && ((Grade)change.previous).grade == 0)){
					Utilities.sendNotifications(getApplicationContext().getString(R.string.newGrade), "[" + ((Grade)change.current).subject.name + "] " + ((Grade)change.current).content + ": " + Double.toString(((Grade)change.current).grade));
				} else if(change.type == Change.ChangeType.MODIFIED && change.varName.equals("grade") && ((Grade)change.current).grade != 0){
					Utilities.sendNotifications(getApplicationContext().getString(R.string.modifiedGrade), "[" + ((Grade)change.current).subject.name + "] " + ((Grade)change.current).content + ": " + Double.toString(((Grade)change.previous).grade) + " -> " + Double.toString(((Grade)change.current).grade));
				}
			} else if(c == Absence.class){
				if(change.type == Change.ChangeType.ADDED){
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					
					String body = sdf.format(((Absence)change.current).startDate.getTime()) + (((Absence)change.current).startDate.getTimeInMillis() != ((Absence)change.current).endDate.getTimeInMillis() ? " - " + sdf.format(((Absence)change.current).endDate.getTime()) : "");
					body += " (" + Integer.toString(((Absence)change.current).lessonCount) + " " + getApplicationContext().getString(R.string.lessons) + ")";
					Utilities.sendNotifications(((Absence)change.current).excused ? getApplicationContext().getString(R.string.newExcusedAbsence) : getApplicationContext().getString(R.string.newAbsence), body);
				} else if(change.type == Change.ChangeType.MODIFIED && change.varName.equals("excused") && ((Absence)change.current).excused){
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					
					String body = sdf.format(((Absence)change.current).startDate.getTime()) + (((Absence)change.current).startDate.getTimeInMillis() != ((Absence)change.current).endDate.getTimeInMillis() ? " - " + sdf.format(((Absence)change.current).endDate.getTime()) : "");
					body += " (" + Integer.toString(((Absence)change.current).lessonCount) + " " + getApplicationContext().getString(R.string.lessons) + ")";
					Utilities.sendNotifications(getApplicationContext().getString(R.string.excusedAbsence), body);
				}
			} else if(c == Transaction.class){
				if(change.type == Change.ChangeType.ADDED){
					Utilities.sendNotifications(getApplicationContext().getString(R.string.newTransaction), ((Transaction)change.current).reason + " -> " + String.format("%.2f", ((Transaction)change.current).amount));
				}
			}
		}
		
		return Result.success();
	}
}
