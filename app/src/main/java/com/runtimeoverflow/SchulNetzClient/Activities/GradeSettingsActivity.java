package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.runtimeoverflow.SchulNetzClient.R;

public class GradeSettingsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grade_settings_activity);
    
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.gradeSettings));
        
        final Context context = this;
        final SharedPreferences prefs = getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        
        ((SwitchCompat)findViewById(R.id.doubleNegativePointsSwitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("doubleNegativePointsEnabled", b);
                editor.commit();
            }
        });
    
        ((SwitchCompat)findViewById(R.id.doubleNegativePointsSwitch)).setChecked(prefs.getBoolean("doubleNegativePointsEnabled", true));
        
        findViewById(R.id.unvaluedGradesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, UnvaluedGradesActivity.class));
            }
        });
    
        findViewById(R.id.gradeGroupsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, GradeGroupsActivity.class));
            }
        });
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