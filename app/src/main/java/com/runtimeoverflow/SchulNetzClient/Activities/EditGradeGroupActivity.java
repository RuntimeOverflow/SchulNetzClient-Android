package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Data.SubjectGroup;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

public class EditGradeGroupActivity extends AppCompatActivity {
    private SubjectGroup currentGroup = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_grade_group_activity);
    
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.editGradeGroup));
    
        Variables.get().currentContext = this;
        if(Variables.get().user == null){
            Variables.get().user = User.load();
        
            if(Variables.get().user == null){
                startActivity(new Intent(Variables.get().currentContext, StartActivity.class));
                return;
            }
        }
        
        if(Variables.get().activityParameter != null && Variables.get().activityParameter.getClass() == SubjectGroup.class){
            currentGroup = (SubjectGroup) Variables.get().activityParameter;
            Variables.get().activityParameter = null;
        } else {
            finish();
            return;
        }
        
        ((EditText)findViewById(R.id.gradeGroupNameField)).setText(currentGroup.name);
        ((EditText)findViewById(R.id.gradeGroupNameField)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentGroup.name = ((EditText)findViewById(R.id.gradeGroupNameField)).getText().toString();
            }
    
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        
        final String[] roundOptions = new String[] {getString(R.string.roundOptionUnvalued), getString(R.string.roundOptionAverage), getString(R.string.roundOptionRoundedAverage)};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, roundOptions);
        ((Spinner)findViewById(R.id.roundOptionSpinner)).setAdapter(adapter);
    
        ((Spinner)findViewById(R.id.roundOptionSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentGroup.roundOption = i;
            }
        
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    
        ((Spinner)findViewById(R.id.roundOptionSpinner)).setSelection(currentGroup.roundOption);
        
        findViewById(R.id.gradeGroupDeleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Subject s : currentGroup.subjects) s.group = null;
                
                Variables.get().user.subjectGroups.remove(currentGroup);
                Variables.get().user.processConnections();
                
                finish();
            }
        });
        
        reloadSubjects();
    }
    
    private void reloadSubjects(){
        ((LinearLayout)findViewById(R.id.addedSubjectsList)).removeAllViews();
        ((LinearLayout)findViewById(R.id.freeSubjectsList)).removeAllViews();
        
        for(final Subject s : Variables.get().user.subjects){
            if(s.group == currentGroup){
                View separator = new View(this);
                separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
                separator.setBackgroundColor(Color.LTGRAY);
                ((LinearLayout)findViewById(R.id.addedSubjectsList)).addView(separator);
    
                final ConstraintLayout row = (ConstraintLayout) LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.grade_group_subject_cell, null);
    
                ((TextView)row.findViewById(R.id.subjectLabel)).setText(s.name != null ? s.name : s.shortName);
                
                Button addRemoveButton = row.findViewById(R.id.addRemoveButton);
    
                addRemoveButton.setText(getString(R.string.subjectGroupRemove));
                addRemoveButton.setTextColor(Color.RED);
                addRemoveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        s.group = null;
    
                        currentGroup.subjects.remove(s);
                        currentGroup.subjectIdentifiers.remove(s.identifier);
    
                        reloadSubjects();
                    }
                });
    
                ((LinearLayout)findViewById(R.id.addedSubjectsList)).addView(row);
            } else if(s.group == null){
                View separator = new View(this);
                separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
                separator.setBackgroundColor(Color.LTGRAY);
                ((LinearLayout)findViewById(R.id.freeSubjectsList)).addView(separator);
    
                final ConstraintLayout row = (ConstraintLayout) LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.grade_group_subject_cell, null);
                
                ((TextView)row.findViewById(R.id.subjectLabel)).setText(s.name != null ? s.name : s.shortName);
                
                Button addRemoveButton = row.findViewById(R.id.addRemoveButton);
    
                addRemoveButton.setText(getString(R.string.subjectGroupAdd));
                addRemoveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        s.group = currentGroup;
                        
                        currentGroup.subjects.add(s);
                        currentGroup.subjectIdentifiers.add(s.identifier);
                        
                        reloadSubjects();
                    }
                });
    
                ((LinearLayout)findViewById(R.id.freeSubjectsList)).addView(row);
            }
        }
        
        if(currentGroup.subjects.size() <= 0) findViewById(R.id.subjectsSeparator).setVisibility(View.GONE);
        else findViewById(R.id.subjectsSeparator).setVisibility(View.VISIBLE);
        
        if(((LinearLayout)findViewById(R.id.addedSubjectsList)).getChildCount() > 0){
            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
            separator.setBackgroundColor(Color.LTGRAY);
            ((LinearLayout)findViewById(R.id.addedSubjectsList)).addView(separator);
        }
    
        if(((LinearLayout)findViewById(R.id.freeSubjectsList)).getChildCount() > 0){
            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)getResources().getDisplayMetrics().density));
            separator.setBackgroundColor(Color.LTGRAY);
            ((LinearLayout)findViewById(R.id.freeSubjectsList)).addView(separator);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    
        Variables.get().user.save();
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