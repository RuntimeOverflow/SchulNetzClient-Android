package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class StudentActivity extends AppCompatActivity {
	private Student currentStudent = null;
	
	private Button classButton;
	private Button homeButton;
	private Button phoneButton;
	private Button birthdayButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_activity);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(Variables.get().activityParameter.getClass() == Student.class){
			currentStudent = (Student)Variables.get().activityParameter;
			Variables.get().activityParameter = null;
		} else {
			finish();
			return;
		}
		
		getSupportActionBar().setTitle(currentStudent.firstName + " " + currentStudent.lastName);
		
		classButton = findViewById(R.id.classButton);
		homeButton = findViewById(R.id.homeButton);
		phoneButton = findViewById(R.id.phoneButton);
		birthdayButton = findViewById(R.id.birthdayButton);
		
		classButton.setText(currentStudent.className + (currentStudent.bilingual ? " (" + getString(R.string.bilingual) + ")" : ""));
		homeButton.setText(currentStudent.address + "\n" + currentStudent.zipCode + " " + currentStudent.city);
		phoneButton.setText(!currentStudent.phone.isEmpty() ? currentStudent.phone : "[" + getString(R.string.noPhoneNumber) + "]");
		
		if(currentStudent.phone.isEmpty()) phoneButton.setTextColor(Color.parseColor("#ff000000"));
		
		Calendar birthday = currentStudent.dateOfBirth;
		SimpleDateFormat sdf = new SimpleDateFormat("d. MMMM yyyy");
		
		int age = Calendar.getInstance().get(Calendar.YEAR) - currentStudent.dateOfBirth.get(Calendar.YEAR);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentStudent.dateOfBirth.getTimeInMillis());
		c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		age -= c.before(Calendar.getInstance()) ? 0 : 1;
		
		birthdayButton.setText(sdf.format(birthday.getTime()) + " (" + age + " " + getString(R.string.age) + ")");
		
		homeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri uri = Uri.parse("geo:0,0?q=" + URLEncoder.encode(currentStudent.address + " " + currentStudent.zipCode + " " + currentStudent.city, StandardCharsets.UTF_8.toString()));
					intent.setData(uri);
					
					if(intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
					else Toast.makeText(Variables.get().currentContext, getString(R.string.noMaps), Toast.LENGTH_LONG).show();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
		
		if(!currentStudent.phone.isEmpty()) phoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					Intent intent = new Intent(Intent.ACTION_DIAL);
					Uri uri = Uri.parse("tel:" + URLEncoder.encode(currentStudent.phone, StandardCharsets.UTF_8.toString()));
					intent.setData(uri);
					
					if(intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
					else Toast.makeText(Variables.get().currentContext, getString(R.string.noPhone), Toast.LENGTH_LONG).show();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
		
		birthdayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_EDIT);
				intent.setType("vnd.android.cursor.item/event");
				intent.putExtra("title", currentStudent.firstName + "'s " + getString(R.string.birthday));
				intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
				intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, currentStudent.dateOfBirth.getTimeInMillis());
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(currentStudent.dateOfBirth.getTimeInMillis());
				c.add(Calendar.DAY_OF_YEAR, 1);
				intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, c.getTimeInMillis() - 1);
				
				if(intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
				else Toast.makeText(Variables.get().currentContext, getString(R.string.noCalendar), Toast.LENGTH_LONG).show();
			}
		});
		
		View.OnLongClickListener copyListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if(Button.class.isAssignableFrom(view.getClass())){
					Toast.makeText(Variables.get().currentContext, getString(R.string.copied), Toast.LENGTH_SHORT).show();
					
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("text", ((Button)view).getText());
					clipboard.setPrimaryClip(clip);
				}
				
				return true;
			}
		};
		
		classButton.setOnLongClickListener(copyListener);
		homeButton.setOnLongClickListener(copyListener);
		phoneButton.setOnLongClickListener(copyListener);
		birthdayButton.setOnLongClickListener(copyListener);
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