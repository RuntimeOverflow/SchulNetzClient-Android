package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.Data.Grade;
import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GradesActivity extends AppCompatActivity {
	private Subject currentSubject;
	private ArrayList<LinearLayout> items = new ArrayList<>();
	private LinearLayout root;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grades_activity);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(Variables.get().activityParameter != null && Variables.get().activityParameter.getClass() == Subject.class){
			currentSubject = (Subject) Variables.get().activityParameter;
			Variables.get().activityParameter = null;
		} else {
			finish();
			return;
		}
		
		getSupportActionBar().setTitle(currentSubject.name + (!Double.isNaN(currentSubject.getAverage()) ? " (" + Utilities.roundToDecimalPlaces(currentSubject.getAverage(), 3) + (currentSubject.hiddenGrades ? "*" : "") + ")" : ""));
		
		root = findViewById(R.id.examsList);
		
		for(int i = 0; i < currentSubject.grades.size(); i++){
			final Grade g = currentSubject.grades.get(i);
			
			final LinearLayout item = (LinearLayout)LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.grade_item, null);
			items.add(item);
			
			((Button)item.findViewById(R.id.examButton)).setText(g.content);
			((TextView)item.findViewById(R.id.gradeLabel)).setText((g.weight != 1 ? "(" + g.weight + "x) " : "") + (g.grade >= 1 && !Double.isNaN(g.grade) ? Double.toString(Utilities.roundToDecimalPlaces(g.grade, 3)) : "-"));
			((TextView)item.findViewById(R.id.gradeLabel)).setTextColor(Grade.colorForGrade(g.grade));
			
			SimpleDateFormat sdf = new SimpleDateFormat("d. MMMM yyyy");
			((TextView)item.findViewById(R.id.dateLabel)).setText(g.date != null ? sdf.format(g.date.getTime()) : "");
			
			((TextView)item.findViewById(R.id.detailsLabel)).setText(g.details != null ? g.details : "");
			
			((Button)item.findViewById(R.id.examButton)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(g.date != null){
						item.findViewById(R.id.dateLabel).setVisibility(item.findViewById(R.id.dateLabel).getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
					}
					
					if(g.details != null){
						item.findViewById(R.id.detailsLabel).setVisibility(item.findViewById(R.id.detailsLabel).getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
					}
				}
			});
			
			root.addView(item);
		}
		
		if(currentSubject.grades.size() <= 0){
			findViewById(R.id.examsList).setVisibility(View.GONE);
			findViewById(R.id.noGradesLabel).setVisibility(View.VISIBLE);
		} else{
			findViewById(R.id.examsList).setVisibility(View.VISIBLE);
			findViewById(R.id.noGradesLabel).setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Variables.get().currentContext = this;
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}