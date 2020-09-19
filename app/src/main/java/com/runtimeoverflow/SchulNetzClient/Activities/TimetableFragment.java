package com.runtimeoverflow.SchulNetzClient.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.AsyncAction;
import com.runtimeoverflow.SchulNetzClient.Data.Change;
import com.runtimeoverflow.SchulNetzClient.Data.Lesson;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.TimetableView;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TimetableFragment extends Fragment {
	private TimetableView tt;
	private ArrayList<Lesson> lessons = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timetable_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		tt = getView().findViewById(R.id.timeTableView);
		
		getView().findViewById(R.id.dateLabel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(Utilities.hasWifi()){
					Variables.get().timetableDate = Calendar.getInstance();
					
					SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
					((TextView)getView().findViewById(R.id.dateLabel)).setText(sdf.format(Variables.get().timetableDate.getTime()));
					
					fetchAndReloadSchedule();
				} else {
					resetToTodayAndReload();
				}
			}
		});
		
		getView().findViewById(R.id.previousButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(Utilities.hasWifi()){
					Variables.get().timetableDate.add(Calendar.DAY_OF_YEAR, -1);
					
					SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
					((TextView)getView().findViewById(R.id.dateLabel)).setText(sdf.format(Variables.get().timetableDate.getTime()));
					
					fetchAndReloadSchedule();
				} else {
					resetToTodayAndReload();
				}
			}
		});
		
		getView().findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(Utilities.hasWifi()){
					Variables.get().timetableDate.add(Calendar.DAY_OF_YEAR, 1);
					
					SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
					((TextView)getView().findViewById(R.id.dateLabel)).setText(sdf.format(Variables.get().timetableDate.getTime()));
					
					fetchAndReloadSchedule();
				} else {
					resetToTodayAndReload();
				}
			}
		});
		
		reloadSchedule();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Object result = Variables.get().account.loadPage("22202");
				
				if(result != null && result.getClass() == Document.class){
					User copy = Variables.get().user.copy();
					
					Parser.parseSchedulePage((Document) result, Variables.get().user);
					
					Change.publishNotifications(Change.getChanges(copy, Variables.get().user));
					Variables.get().user.save();
				}
			}
		});
		t.start();
		
		SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
		((TextView)getView().findViewById(R.id.dateLabel)).setText(sdf.format(Variables.get().timetableDate.getTime()));
		
		if(!Utilities.hasWifi()){
			getView().findViewById(R.id.previousButton).setVisibility(View.GONE);
			getView().findViewById(R.id.nextButton).setVisibility(View.GONE);
			
			resetToTodayAndReload();
		} else {
			getView().findViewById(R.id.previousButton).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
			
			fetchAndReloadSchedule();
		}
	}
	
	public void resetToTodayAndReload(){
		if(!Utilities.hasWifi()){
			getView().findViewById(R.id.previousButton).setVisibility(View.GONE);
			getView().findViewById(R.id.nextButton).setVisibility(View.GONE);
		} else {
			getView().findViewById(R.id.previousButton).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
		}
		
		Variables.get().timetableDate = Calendar.getInstance();
		lessons = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
		((TextView)getView().findViewById(R.id.dateLabel)).setText(sdf.format(Variables.get().timetableDate.getTime()));
		
		reloadSchedule();
	}
	
	private boolean running = false;
	public void fetchAndReloadSchedule(){
		final Calendar original = Calendar.getInstance();
		original.setTimeInMillis(Variables.get().timetableDate.getTimeInMillis());
		
		tt.setVisibility(View.GONE);
		getView().findViewById(R.id.statusLabel).setVisibility(View.GONE);
		getView().findViewById(R.id.loadingIcon).setVisibility(View.VISIBLE);
		
		Utilities.runAsynchronous(new AsyncAction() {
			@Override
			public void runAsync() {
				if(Utilities.hasWifi()){
					if(running){
						while(running && original.compareTo(Variables.get().timetableDate) == 0) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						if(running || original.compareTo(Variables.get().timetableDate) != 0) return;
					}
					
					running = true;
					
					Object result = Variables.get().account.loadSchedule(original, original);
					
					if(result != null && result.getClass() == Document.class && original.compareTo(Variables.get().timetableDate) == 0){
						lessons = Parser.parseSchedule((Document)result);
						if(lessons == null) {
							resetToTodayAndReload();
							running = false;
							return;
						} else{
							Variables.get().user.processLessons(lessons);
						}
					}
					
					running = false;
				}
			}
			
			@Override
			public void runSyncWhenDone() {
				if(original.compareTo(Variables.get().timetableDate) == 0) {
					if(getView() != null) getView().findViewById(R.id.loadingIcon).setVisibility(View.GONE);
					reloadSchedule();
				}
			}
		});
	}
	
	public void reloadSchedule(){
		if(getView() == null) return;
		
		ArrayList<Lesson.ScheduleLesson> sl = calculateLayout(lessons);
		tt.setLessons(sl);
		
		if(lessons == null){
			((TextView)getView().findViewById(R.id.statusLabel)).setText(R.string.timetableUnavailable);
			getView().findViewById(R.id.statusLabel).setVisibility(View.VISIBLE);
			tt.setVisibility(View.GONE);
		} else if(sl.size() <= 0){
			((TextView)getView().findViewById(R.id.statusLabel)).setText(R.string.noLessons);
			getView().findViewById(R.id.statusLabel).setVisibility(View.VISIBLE);
			tt.setVisibility(View.GONE);
		} else{
			getView().findViewById(R.id.statusLabel).setVisibility(View.GONE);
			tt.setVisibility(View.VISIBLE);
		}
	}
	
	private ArrayList<Lesson.ScheduleLesson> calculateLayout(ArrayList<Lesson> lessons){
		if(lessons == null) return new ArrayList<>();
		
		ArrayList<Lesson.ScheduleLesson> result = new ArrayList<>();
		ArrayList<Lesson> sorted = Lesson.orderByStartTime(lessons);
		
		for(int i = 0; i < sorted.size(); i++){
			if(sorted.get(i).longerThanOrEqualToOneDay()){
				sorted.remove(i);
				i--;
			}
		}
		
		ArrayList<Lesson.ScheduleLesson> active = new ArrayList<>();
		int currentSplits = 0;
		for(int i = 0; i < sorted.size(); i++){
			Lesson l = sorted.get(i);
			active = Lesson.ScheduleLesson.orderByEndTime(active);
			
			for(int i2 = 0; i2 < active.size(); i2++){
				Lesson.ScheduleLesson al = active.get(i2);
				if(al.lesson.endDate.after(l.startDate)) break;
				
				al.end = al.lesson.endDate;
				result.add(al);
				
				ArrayList<Lesson.ScheduleLesson> newActive = new ArrayList<>();
				int index = 0;
				for(int i3 = i2 + 1; i3 < active.size(); i3++){
					Lesson.ScheduleLesson al2 = active.get(i3);
					if(al == al2) continue;
					
					if(al.lesson.endDate.getTimeInMillis() == al2.lesson.endDate.getTimeInMillis()){
						al2.end = al.lesson.endDate;
						result.add(al2);
					} else{
						Lesson.ScheduleLesson newSplitLesson = new Lesson.ScheduleLesson();
						al2.end = al.lesson.endDate;
						result.add(al2);
						
						newSplitLesson.lesson = al2.lesson;
						newSplitLesson.start = al.lesson.endDate;
						newSplitLesson.index = index;
						newActive.add(newSplitLesson);
						index++;
					}
				}
				
				for(Lesson.ScheduleLesson al2 : newActive) al2.total = index;
				currentSplits = index;
				
				active = Lesson.ScheduleLesson.orderByEndTime(newActive);
				i2 = -1;
			}
			
			Lesson.ScheduleLesson scheduleLesson = new Lesson.ScheduleLesson();
			scheduleLesson.lesson = l;
			scheduleLesson.start = l.startDate;
			
			ArrayList<Lesson.ScheduleLesson> newActive = new ArrayList<>();
			int index = 0;
			for(int i2 = 0; i2 < active.size(); i2++){
				Lesson.ScheduleLesson al = active.get(i2);
				
				Lesson.ScheduleLesson newSplitLesson = new Lesson.ScheduleLesson();
				al.end = l.startDate;
				if(al.start.getTimeInMillis() != al.end.getTimeInMillis()) result.add(al);
				
				newSplitLesson.lesson = al.lesson;
				newSplitLesson.start = l.startDate;
				
				newSplitLesson.index = index;
				newActive.add(newSplitLesson);
				index++;
			}
			
			for(Lesson.ScheduleLesson al : newActive) al.total = index + 1;
			currentSplits = index;
			
			active = Lesson.ScheduleLesson.orderByEndTime(newActive);
			
			scheduleLesson.index = currentSplits;
			currentSplits++;
			scheduleLesson.total = currentSplits;
			active.add(scheduleLesson);
			
			Calendar c1 = Calendar.getInstance();
			c1.setTimeInMillis(l.startDate.getTimeInMillis());
			c1.set(Calendar.MINUTE, 0);
			c1.set(Calendar.HOUR, 0);
			c1.set(Calendar.AM_PM, Calendar.AM);
			c1.set(Calendar.HOUR_OF_DAY, 0);
			
			Calendar c2 = Calendar.getInstance();
			if(i + 1 < sorted.size()) c2.setTimeInMillis(sorted.get(i + 1).startDate.getTimeInMillis());
			else c2.setTimeInMillis(0);
			c2.set(Calendar.MINUTE, 0);
			c2.set(Calendar.HOUR, 0);
			c2.set(Calendar.AM_PM, Calendar.AM);
			c2.set(Calendar.HOUR_OF_DAY, 0);
			
			if(i + 1 < sorted.size() && c2.after(c1)){
				ArrayList<Lesson.ScheduleLesson> newActive2 = new ArrayList<>();
				int index2 = 0;
				for(int i2 = 0; i2 < active.size(); i2++){
					Lesson.ScheduleLesson al = active.get(i2);
					
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(l.startDate.getTimeInMillis());
					c.add(Calendar.DAY_OF_YEAR, 1);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.HOUR, 0);
					c.set(Calendar.AM_PM, Calendar.AM);
					c.set(Calendar.HOUR_OF_DAY, 0);
					
					Lesson.ScheduleLesson newSplitLesson = new Lesson.ScheduleLesson();
					al.end = c;
					if(al.start.getTimeInMillis() == al.end.getTimeInMillis()) result.add(al);
					
					newSplitLesson.lesson = al.lesson;
					newSplitLesson.start = c;
					newSplitLesson.index = index2;
					newActive2.add(newSplitLesson);
					index2++;
				}
				
				for(Lesson.ScheduleLesson al : newActive2) al.total = index2;
				currentSplits = index2;
				
				active = Lesson.ScheduleLesson.orderByEndTime(newActive2);
			}
		}
		
		active = Lesson.ScheduleLesson.orderByEndTime(active);
		
		for(int i2 = 0; i2 < active.size(); i2++){
			Lesson.ScheduleLesson al = active.get(i2);
			
			al.end = al.lesson.endDate;
			result.add(al);
			
			ArrayList<Lesson.ScheduleLesson> newActive = new ArrayList<>();
			int index = 0;
			for(int i3 = i2 + 1; i3 < active.size(); i3++){
				Lesson.ScheduleLesson al2 = active.get(i3);
				if(al == al2) continue;
				
				if(al.lesson.endDate.getTimeInMillis() == al2.lesson.endDate.getTimeInMillis()){
					al2.end = al.lesson.endDate;
					result.add(al2);
				} else{
					Lesson.ScheduleLesson newSplitLesson = new Lesson.ScheduleLesson();
					al2.end = al.lesson.endDate;
					result.add(al2);
					
					newSplitLesson.lesson = al2.lesson;
					newSplitLesson.start = al.lesson.endDate;
					newSplitLesson.index = index;
					newActive.add(newSplitLesson);
					index++;
				}
			}
			
			for(Lesson.ScheduleLesson al2 : newActive) al2.total = index;
			currentSplits = index;
			
			active = Lesson.ScheduleLesson.orderByEndTime(newActive);
			i2 = -1;
		}
		
		return result;
	}
}