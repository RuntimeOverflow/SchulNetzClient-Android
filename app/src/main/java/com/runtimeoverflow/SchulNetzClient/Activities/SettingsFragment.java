package com.runtimeoverflow.SchulNetzClient.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.runtimeoverflow.SchulNetzClient.Account;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

public class SettingsFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.settings_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getView().findViewById(R.id.refreshButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onRefreshPressed();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void onRefreshPressed(){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				String response = "";

				try{
					SharedPreferences prefs = getContext().getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
					
					//Variables.get().account.signIn();
					Document doc = (Document)Variables.get().account.loadPage("22352");
					Parser.parseTeachers(doc, Variables.get().user);
					doc = (Document)Variables.get().account.loadPage("22326");
					Parser.parseSubjects(doc, Variables.get().user);
					Parser.parseStudents(doc, Variables.get().user);
					doc = (Document)Variables.get().account.loadPage("21311");
					Parser.parseGrades(doc, Variables.get().user);
					doc = (Document)Variables.get().account.loadPage("21411");
					Parser.parseSelf(doc, Variables.get().user);
					Parser.parseTransactions(doc, Variables.get().user);
					Variables.get().account.signOut();
					Variables.get().user.processConnections();
					
					Variables.get().user.save();
					response = "Successfully refreshed";
					Log.i("NESA", "Refreshed");
				} catch(Exception e){
					e.printStackTrace();
					response = e.getLocalizedMessage();
				} finally {
					Handler mainHandler = new Handler(Looper.getMainLooper());
					final String finalResponse = response;
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getContext(), finalResponse, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
		t.start();
	}
}