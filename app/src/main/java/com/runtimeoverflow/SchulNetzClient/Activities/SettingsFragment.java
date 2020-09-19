package com.runtimeoverflow.SchulNetzClient.Activities;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.runtimeoverflow.SchulNetzClient.Account;
import com.runtimeoverflow.SchulNetzClient.AsyncAction;
import com.runtimeoverflow.SchulNetzClient.Data.Change;
import com.runtimeoverflow.SchulNetzClient.Data.Lesson;
import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsFragment extends Fragment {
	private final int[] resIds = new int[]{R.id.grades_navigation, R.id.absences_navigation, R.id.timetable_navigation, R.id.people_navigation, R.id.settings_navigation};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.settings_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		final SharedPreferences prefs = getContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		
		getView().findViewById(R.id.signoutButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSignOutPressed();
			}
		});
		
		getView().findViewById(R.id.transactionsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getContext(), TransactionsActivity.class));
			}
		});
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{getString(R.string.grades), getString(R.string.absences), getString(R.string.timetable), getString(R.string.people), getString(R.string.settings)});
		((Spinner)getView().findViewById(R.id.startPageSelector)).setAdapter(adapter);
		
		((Spinner)getView().findViewById(R.id.startPageSelector)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				editor.putInt("defaultPage", resIds[adapterView.getSelectedItemPosition()]);
				editor.apply();
				editor.commit();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {}
		});
		
		((Switch)getView().findViewById(R.id.notificationsSwitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				editor.putBoolean("notificationsEnabled", compoundButton.isChecked());
				editor.apply();
				editor.commit();
			}
		});
		
		getView().findViewById(R.id.sourceCodeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RuntimeOverflow/SchulNetz-Client-Android")));
			}
		});
		
		reloadSettingsPage();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Utilities.runAsynchronous(new AsyncAction() {
			@Override
			public void runAsync() {
				if(Utilities.hasWifi()){
					Object result = Variables.get().account.loadPage("21411");
					
					if(result != null && result.getClass() == Document.class){
						User copy = Variables.get().user.copy();
						
						Parser.parseSelf((Document) result, Variables.get().user);
						Parser.parseTransactions((Document) result, Variables.get().user);
						
						Variables.get().user.processConnections();
						
						Change.publishNotifications(Change.getChanges(copy, Variables.get().user));
						Variables.get().user.save();
					}
				}
			}
			
			@Override
			public void runSyncWhenDone() {
				reloadSettingsPage();
			}
		});
	}
	
	public void reloadSettingsPage(){
		if(getView() == null) return;
		
		final SharedPreferences prefs = getContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		
		if(Variables.get().user.self != null){
			((TextView)getView().findViewById(R.id.nameLabel)).setText(Variables.get().user.self.firstName + " " + Variables.get().user.self.lastName);
			((TextView)getView().findViewById(R.id.classLabel)).setText(Variables.get().user.self.className);
		} else{
			((TextView)getView().findViewById(R.id.nameLabel)).setText(Variables.get().account.username.replace(".", " ").toUpperCase());
			((TextView)getView().findViewById(R.id.classLabel)).setVisibility(View.GONE);
		}
		
		int currentPageId = prefs.getInt("defaultPage", R.id.grades_navigation);
		
		for(int i = 0; i < resIds.length; i++) if(resIds[i] == currentPageId){
			((Spinner)getView().findViewById(R.id.startPageSelector)).setSelection(i);
			break;
		}
		
		((Switch)getView().findViewById(R.id.notificationsSwitch)).setChecked(prefs.getBoolean("notificationsEnabled", true));
	}
	
	private void onSignOutPressed(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setCancelable(true).setTitle(R.string.signout).setMessage(R.string.signoutConfirmation);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				SharedPreferences prefs = getContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				
				Variables.get().account.close();
				
				editor.remove("host");
				editor.remove("username");
				editor.remove("password");
				editor.remove("user");
				
				editor.apply();
				editor.commit();
				
				startActivity(new Intent(getContext(), StartActivity.class));
			}
		});
		builder.setNegativeButton(R.string.no, null);
		
		builder.create().show();
	}
}