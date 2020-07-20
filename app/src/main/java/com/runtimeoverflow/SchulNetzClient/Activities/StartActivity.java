package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.runtimeoverflow.SchulNetzClient.Account;
import com.runtimeoverflow.SchulNetzClient.Activities.MainActivity;
import com.runtimeoverflow.SchulNetzClient.Activities.SigninActivity;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

public class StartActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
		
		Variables.get().currentContext = this;
		
		SharedPreferences prefs = this.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		if(prefs != null && prefs.getString("host", null) != null && prefs.getString("host", null).length() > 0 && prefs.getString("username", null) != null && prefs.getString("username", null).length() > 0 && prefs.getString("password", null) != null && prefs.getString("password", null).length() > 0){
			Variables.get().account = new Account(prefs.getString("host", null), prefs.getString("username", null), prefs.getString("password", null));
			Variables.get().user = User.load();
			
			final StartActivity sa = this;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					if(Utilities.hasWifi()) Variables.get().account.signIn();
					
					Intent i = new Intent(sa, MainActivity.class);
					startActivity(i);
				}
			});
			t.start();
		} else {
			Intent i = new Intent(this, SigninActivity.class);
			startActivity(i);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Variables.get().currentContext = this;
	}
}