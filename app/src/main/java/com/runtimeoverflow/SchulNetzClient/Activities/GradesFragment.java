package com.runtimeoverflow.SchulNetzClient.Activities;

import android.content.Intent;
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
import com.runtimeoverflow.SchulNetzClient.Data.Grade;
import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

public class GradesFragment extends Fragment {
	private TableLayout gradeTable;
	private int cellsPerRow = 2;
	
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
					Object result = Variables.get().account.loadPage("22326");
					
					if(result != null && result.getClass() == Document.class){
						Parser.parseSubjects((Document) result, Variables.get().user);
					}
					
					result = Variables.get().account.loadPage("21311");
					
					if(result != null && result.getClass() == Document.class){
						Parser.parseGrades((Document) result, Variables.get().user);
						Variables.get().user.processConnections();
					}
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
		for(final Subject s : Variables.get().user.subjects){
			if(s.name == null) {
				absoluteIndex++;
				continue;
			}
			
			if(!Double.isNaN(s.getAverage())){
				if(Math.round(s.getAverage() * 2.0) / 2.0 - 4.0 > 0) positive += Math.round(s.getAverage() * 2.0) / 2.0 - 4.0;
				else negative = Math.round(s.getAverage() * 2.0) / 2.0 - 4.0;
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
		
		((TextView)getView().findViewById(R.id.negativeLabel)).setText((negative == 0 ? "-" : "") + Double.toString(Utilities.roundToDecimalPlaces(negative, 3)));
		((TextView)getView().findViewById(R.id.positiveLabel)).setText("+" + Double.toString(Utilities.roundToDecimalPlaces(positive, 3)));
		
		((TextView)getView().findViewById(R.id.differenceLabel)).setText(Double.toString(Utilities.roundToDecimalPlaces(positive + 2 * negative, 3)));
	}
}