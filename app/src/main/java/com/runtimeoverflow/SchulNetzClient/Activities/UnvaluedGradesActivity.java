package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

public class UnvaluedGradesActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unvalued_grades_activity);
    
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.unvaluedGrades));
    
        Variables.get().currentContext = this;
        if(Variables.get().user == null){
            Variables.get().user = User.load();
        
            if(Variables.get().user == null){
                startActivity(new Intent(Variables.get().currentContext, StartActivity.class));
                return;
            }
        }
        
        for(final Subject s : Variables.get().user.subjects){
            if(s.group != null) continue;
            
            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
            separator.setBackgroundColor(Color.LTGRAY);
            ((LinearLayout)findViewById(R.id.unvaluedGradesList)).addView(separator);
            
            final ConstraintLayout row = (ConstraintLayout) LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.unvalued_grade_cell, null);
    
            TextView subjectLabel = row.findViewById(R.id.subjectLabel);
            SwitchCompat subjectSwitch = row.findViewById(R.id.subjectSwitch);
            
            subjectLabel.setText(s.name != null ? s.name : s.shortName);
            subjectSwitch.setChecked(s.unvalued);
            
            subjectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    s.unvalued = b;
                    Variables.get().user.save();
                }
            });
            
            ((LinearLayout)findViewById(R.id.unvaluedGradesList)).addView(row);
        }
        
        if(Variables.get().user.subjects.size() > 0){
            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
            separator.setBackgroundColor(Color.LTGRAY);
            ((LinearLayout)findViewById(R.id.unvaluedGradesList)).addView(separator);
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