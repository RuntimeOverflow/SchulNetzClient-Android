package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Data.SubjectGroup;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

public class GradeGroupsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grade_groups_activity);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.gradeGroups));
		
		Variables.get().currentContext = this;
		if(Variables.get().user == null){
			Variables.get().user = User.load();
			
			if(Variables.get().user == null){
				startActivity(new Intent(Variables.get().currentContext, StartActivity.class));
				return;
			}
		}
		
		final Context context = this;
		
		((Button)findViewById(R.id.createGradeGroupButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SubjectGroup g = new SubjectGroup();
				g.name = getString(R.string.newGroup);
				g.roundOption = 1;
				Variables.get().user.subjectGroups.add(g);
				
				Variables.get().activityParameter = g;
				startActivity(new Intent(context, EditGradeGroupActivity.class));
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		final Context context = this;
		
		((LinearLayout)findViewById(R.id.gradeGroupsList)).removeAllViews();
		for(final SubjectGroup g : Variables.get().user.subjectGroups){
			View separator = new View(this);
			separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
			separator.setBackgroundColor(Color.LTGRAY);
			((LinearLayout)findViewById(R.id.gradeGroupsList)).addView(separator);
			
			final ConstraintLayout row = (ConstraintLayout) LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.grade_group_cell, null);
			
			Button gradeGroupButton = row.findViewById(R.id.gradeGroupButton);
			
			gradeGroupButton.setText(g.name);
			gradeGroupButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Variables.get().activityParameter = g;
					startActivity(new Intent(context, EditGradeGroupActivity.class));
				}
			});
			
			((LinearLayout)findViewById(R.id.gradeGroupsList)).addView(row);
		}
		
		if(Variables.get().user.subjectGroups.size() > 0){
			View separator = new View(this);
			separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
			separator.setBackgroundColor(Color.LTGRAY);
			((LinearLayout)findViewById(R.id.gradeGroupsList)).addView(separator);
		}
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