package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Teacher;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.util.StringJoiner;

public class TeacherActivity extends AppCompatActivity {
    private Teacher currentTeacher = null;
    
    private Button mailButton;
    private Button subjectsButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_activity);
    
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    
        if(Variables.get().activityParameter.getClass() == Teacher.class){
            currentTeacher = (Teacher)Variables.get().activityParameter;
            Variables.get().activityParameter = null;
        } else {
            finish();
            return;
        }
    
        getSupportActionBar().setTitle(currentTeacher.firstName + " " + currentTeacher.lastName + " (" + currentTeacher.shortName + ")");
        
        mailButton = findViewById(R.id.mailButton);
        subjectsButton = findViewById(R.id.subjectsButton);
        
        mailButton.setText(currentTeacher.mail);
    
        String subjects = "";
        for(int i = 0; i < currentTeacher.subjects.size(); i++){
            if(i != 0) subjects += "\n";
            subjects += currentTeacher.subjects.get(i).name != null ? currentTeacher.subjects.get(i).name : currentTeacher.subjects.get(i).shortName;
        }
        
        subjectsButton.setText(subjects);
        
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{currentTeacher.mail});
    
                if(intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
                else Toast.makeText(Variables.get().currentContext, getString(R.string.noMail), Toast.LENGTH_LONG).show();
            }
        });
    
        View.OnLongClickListener copyListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(Button.class.isAssignableFrom(view.getClass())){
                    Toast.makeText(Variables.get().currentContext, "Copied!", Toast.LENGTH_SHORT).show();
                
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", ((Button)view).getText());
                    clipboard.setPrimaryClip(clip);
                }
            
                return true;
            }
        };
    
        mailButton.setOnLongClickListener(copyListener);
        subjectsButton.setOnLongClickListener(copyListener);
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