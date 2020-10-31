package com.runtimeoverflow.SchulNetzClient.Activities;

import android.content.Context;
import android.content.Intent;
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
		
		Variables.get().currentContext = this;
		if(Variables.get().user == null){
			Variables.get().user = User.load();
			
			if(Variables.get().user == null){
				startActivity(new Intent(Variables.get().currentContext, StartActivity.class));
				return;
			}
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		BottomNavigationView navView = findViewById(R.id.nav_view);
		
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupWithNavController(navView, navController);
		
		SharedPreferences prefs = this.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		int pageId = prefs.getInt("defaultPage", R.id.grades_navigation);
		navView.setSelectedItemId(pageId);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Variables.get().currentContext = this;
	}
	
	@Override
	public void onBackPressed() {}
}