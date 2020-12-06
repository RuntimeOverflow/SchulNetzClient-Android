package com.runtimeoverflow.SchulNetzClient.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.AsyncAction;
import com.runtimeoverflow.SchulNetzClient.Data.Change;
import com.runtimeoverflow.SchulNetzClient.Data.Grade;
import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Data.SubjectGroup;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GradesFragment extends Fragment {
	private TableLayout gradeTable;
	private int cellsPerRow = 2;
	private ArrayList<Subject> current = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.grades_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				
				reloadTable();
			}
		});
		
		current = Variables.get().user.subjects;
		
		reloadTable();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		reloadTable();
		
		Utilities.runAsynchronous(new AsyncAction() {
			@Override
			public void runAsync() {
				if(Utilities.hasWifi()){
					User copy = Variables.get().user.copy();
					copy.subjects = current;
					
					ArrayList<Subject> previous = Variables.get().user.subjects;
					
					Object result = Variables.get().account.loadPage("22348");
					boolean success = false;
					
					if(result != null && result.getClass() == Document.class){
						success = Parser.parseSubjects((Document) result, Variables.get().user);
					}
					
					if(!success){
						Variables.get().user.subjects = previous;
						Variables.get().user.processConnections();
						return;
					}
					
					Variables.get().user.processConnections();
					
					result = Variables.get().account.loadPage("21311");
					
					success = false;
					if(result != null && result.getClass() == Document.class){
						success = Parser.parseGrades((Document) result, Variables.get().user);
					}
					
					if(!success) Variables.get().user.subjects = previous;
					
					Variables.get().user.processConnections();
					
					Change.publishNotifications(Change.getChanges(copy, Variables.get().user));
					Variables.get().user.save();
					
					if(success) current = Variables.get().user.subjects;
				}
			}
			
			@Override
			public void runSyncWhenDone() {
				reloadTable();
			}
		});
	}
	
	public void reloadTable(){
		if(getView() == null) return;
		
		final SharedPreferences prefs = getContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		current = Variables.get().user.subjects;
		
		gradeTable = getView().findViewById(R.id.gradeTable);
		
		TableRow overviewRow = gradeTable.findViewById(R.id.overviewRow);
		gradeTable.removeAllViews();
		gradeTable.addView(overviewRow);
		
		int width = gradeTable.getWidth() / cellsPerRow;
		
		double positive = 0;
		double negative = 0;
		
		int index = 0;
		int absoluteIndex = 0;
		TableRow row = null;
		for(final Subject s : current){
			if(s.name == null) {
				absoluteIndex++;
				continue;
			}
			
			if(!Double.isNaN(s.getAverage()) && !s.unvalued && s.group == null){
				if(Math.round(s.getAverage() * 2.0) / 2.0 - 4.0 > 0) positive += Math.round(s.getAverage() * 2.0) / 2.0 - 4.0;
				else negative += Math.round(s.getAverage() * 2.0) / 2.0 - 4.0;
			}
			
			if(index % cellsPerRow == 0){
				row = new TableRow(getContext());
			}
			
			LinearLayout gradeCell = (LinearLayout)LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.subject_grade_cell, null);
			((TextView)gradeCell.findViewById(R.id.subjectLabel)).setText(s.name);
			
			if(!s.confirmed) ((TextView)gradeCell.findViewById(R.id.subjectLabel)).setTypeface(null, Typeface.BOLD);
			((TextView)gradeCell.findViewById(R.id.gradeLabel)).setText((!Double.isNaN(s.getAverage()) ? Double.toString(Utilities.roundToDecimalPlaces(s.getAverage(), 3)) : "-") + (s.hiddenGrades ? "*" : ""));
			((TextView)gradeCell.findViewById(R.id.gradeLabel)).setTextColor(Grade.colorForGrade(s.getAverage()));
			gradeCell.setLayoutParams(new TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT));
			
			gradeCell.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Variables.get().activityParameter = s;
					startActivity(new Intent(getContext(), GradesActivity.class));
				}
			});
			
			row.addView(gradeCell);
			
			if((index + 1) % cellsPerRow == 0 || absoluteIndex == Variables.get().user.subjects.size() - 1){
				gradeTable.addView(row);
			}
			
			absoluteIndex++;
			index++;
		}
		
		for(SubjectGroup g : Variables.get().user.subjectGroups){
			double grade = g.getGrade();
			if(!Double.isNaN(grade) && grade >= 1){
				if(Math.round(grade * 2.0) / 2.0 - 4.0 > 0) positive += Math.round(grade * 2.0) / 2.0 - 4.0;
				else negative += Math.round(grade * 2.0) / 2.0 - 4.0;
			}
		}
		
		((TextView)getView().findViewById(R.id.negativeLabel)).setText((negative == 0 ? "-" : "") + Double.toString(Utilities.roundToDecimalPlaces((prefs.getBoolean("doubleNegativePointsEnabled", true) ? 2 : 1) * negative, 3)));
		((TextView)getView().findViewById(R.id.positiveLabel)).setText("+" + Double.toString(Utilities.roundToDecimalPlaces(positive, 3)));
		
		((TextView)getView().findViewById(R.id.differenceLabel)).setText(Double.toString(Utilities.roundToDecimalPlaces(positive + (prefs.getBoolean("doubleNegativePointsEnabled", true) ? 2 : 1) * negative, 3)));
	}
}