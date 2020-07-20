package com.runtimeoverflow.SchulNetzClient.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.runtimeoverflow.SchulNetzClient.Account;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		//AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.grades_navigation, R.id.absences_navigation, R.id.timetable_navigation, R.id.people_navigation, R.id.settings_navigation).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		//NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(navView, navController);
		
		SharedPreferences prefs = getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		Variables.get().account = new Account(prefs.getString("host", null), prefs.getString("username", null), prefs.getString("password", null));
		Variables.get().user = User.load();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i("NESA", Variables.get().account.signIn().toString());
			}
		});
		t.start();
		
		Variables.get().currentContext = this;
	}
}