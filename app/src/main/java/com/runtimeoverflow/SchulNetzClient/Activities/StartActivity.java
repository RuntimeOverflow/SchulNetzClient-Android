package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.runtimeoverflow.SchulNetzClient.Account;
import com.runtimeoverflow.SchulNetzClient.Activities.MainActivity;
import com.runtimeoverflow.SchulNetzClient.Activities.SigninActivity;
import com.runtimeoverflow.SchulNetzClient.BackgroundWorker;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.util.concurrent.TimeUnit;

public class StartActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
		
		Variables.get().currentContext = this;
		
		SharedPreferences prefs = this.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		if(prefs != null && prefs.getString("host", null) != null && prefs.getString("host", "").length() > 0 && prefs.getString("username", null) != null && prefs.getString("username", null).length() > 0 && prefs.getString("password", null) != null && prefs.getString("password", null).length() > 0 && User.load() != null){
			Variables.get().account = new Account(prefs.getString("host", null), prefs.getString("username", null), prefs.getString("password", null));
			Variables.get().user = User.load();
			
			startActivity(new Intent(this, MainActivity.class));
		} else {
			startActivity(new Intent(this, SigninActivity.class));
		}
		
		PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(BackgroundWorker.class, 15, TimeUnit.MINUTES).setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresBatteryNotLow(true).build()).setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES).build();
		WorkManager manager = WorkManager.getInstance(this);
		manager.enqueueUniquePeriodicWork("com.runtimeoverflow.SchulNetzClient", ExistingPeriodicWorkPolicy.REPLACE, request);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Variables.get().currentContext = this;
	}
}