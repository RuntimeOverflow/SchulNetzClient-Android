package com.runtimeoverflow.SchulNetzClient.Activities;

import android.graphics.Color;
import android.icu.number.NumberFormatter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.AsyncAction;
import com.runtimeoverflow.SchulNetzClient.Data.Absence;
import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;

public class AbsencesFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.absences_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		reloadList();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Utilities.runAsynchronous(new AsyncAction() {
			@Override
			public void runAsync() {
				if(Utilities.hasWifi()){
					Object result = Variables.get().account.loadPage("21111");
					
					if(result != null && result.getClass() == Document.class){
						Parser.parseAbsences((Document) result, Variables.get().user);
						Variables.get().user.processConnections();
					}
				}
			}
			
			@Override
			public void runSyncWhenDone() {
				reloadList();
			}
		});
	}
	
	public void reloadList(){
		if(getView() == null) return;
		
		LinearLayout absencesList = getView().findViewById(R.id.absencesList);
		if(absencesList.getChildCount() > 1) absencesList.removeViews(1, absencesList.getChildCount() - 1);
		
		int lessonCount = 0;
		for(Absence a : Variables.get().user.absences){
			final LinearLayout row = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.absence_cell, null);
			
			lessonCount += a.lessonCount;
			
			((Button)row.findViewById(R.id.absenceButton)).setText(!a.reason.isEmpty() ? a.reason : "[" + getText(R.string.noDesc) + "]");
			((Button)row.findViewById(R.id.absenceButton)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int targetVisibility = ((TextView)row.findViewById(R.id.timespanLabel)).getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
					
					if(targetVisibility == View.VISIBLE) ((Button)row.findViewById(R.id.absenceButton)).setMaxLines(Integer.MAX_VALUE);
					else ((Button)row.findViewById(R.id.absenceButton)).setMaxLines(1);
					
					row.findViewById(R.id.timespanLabel).setVisibility(targetVisibility);
					row.findViewById(R.id.reportsButton).setVisibility(targetVisibility);
					
					row.findViewById(R.id.reportsList).setVisibility(View.GONE);
					((Button)row.findViewById(R.id.reportsButton)).setText(R.string.showReports);
				}
			});
			
			((TextView)row.findViewById(R.id.lessonCountLabel)).setText(Integer.toString(a.lessonCount) + " " + (a.lessonCount != 1 ? getString(R.string.lessons) : getString(R.string.lesson)));
			if(!a.excused) ((TextView)row.findViewById(R.id.lessonCountLabel)).setTextColor(Color.parseColor("#ffff0000"));
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy");
			((TextView)row.findViewById(R.id.timespanLabel)).setText(sdf.format(a.startDate.getTime()) + (a.startDate.getTimeInMillis() != a.endDate.getTimeInMillis() ? " - " + sdf.format(a.endDate.getTime()) : ""));
			
			((Button)row.findViewById(R.id.reportsButton)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int targetVisibility = row.findViewById(R.id.reportsList).getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
					row.findViewById(R.id.reportsList).setVisibility(targetVisibility);
					
					if(targetVisibility != View.GONE) ((Button)row.findViewById(R.id.reportsButton)).setText(R.string.hideReports);
					else ((Button)row.findViewById(R.id.reportsButton)).setText(R.string.showReports);
				}
			});
			
			int index = 0;
			for(Subject s : a.subjects){
				if(s == null) continue;
				
				TextView report = new TextView(getContext());
				report.setText(Integer.toString(index + 1) + ". " + (s != null ? s.name : s.shortName));
				report.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin = (int)getResources().getDisplayMetrics().density * 25;
				report.setLayoutParams(params);
				
				((LinearLayout)row.findViewById(R.id.reportsList)).addView(report);
				
				index++;
			}
			
			absencesList.addView(row);
			
			if(Variables.get().user.absences.indexOf(a) != Variables.get().user.absences.size() - 1){
				View divider = new View(getContext());
				divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density * 1));
				divider.setBackgroundColor(Color.LTGRAY);
				absencesList.addView(divider);
			}
		}
		
		((TextView)getView().findViewById(R.id.titleLabel)).setText(getString(R.string.missedLessons) + ": " + Integer.toString(lessonCount));
		
		if(Variables.get().user.absences.size() <= 0) getView().findViewById(R.id.noAbsencesLayout).setVisibility(View.VISIBLE);
		else getView().findViewById(R.id.noAbsencesLayout).setVisibility(View.GONE);
	}
}