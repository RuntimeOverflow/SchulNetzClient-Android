package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runtimeoverflow.SchulNetzClient.Account;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

public class SigninActivity extends AppCompatActivity {
	private Spinner hostList;
	private EditText hostField;
	private EditText usernameField;
	private EditText passwordField;
	private Button signinButton;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		context = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin_activity);

		hostList = findViewById(R.id.hostList);
		String[] hosts = new String[]{(String)getApplicationContext().getResources().getText(R.string.selectHost), "ksw.nesa-sg.ch", "Other..."};
		SpinnerAdapter sa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, hosts){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0) return false;
				else return super.isEnabled(position);
			}

			@Override
			public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				TextView view = (TextView) super.getDropDownView(position, convertView, parent);
				if(position == 0) view.setTextColor(Color.GRAY);
				return view;
			}
		};
		hostList.setAdapter(sa);

		hostField = findViewById(R.id.hostField);
		usernameField = findViewById(R.id.usernameField);
		passwordField = findViewById(R.id.passwordField);
		signinButton = findViewById(R.id.signinButton);

		TextWatcher tw = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void afterTextChanged(Editable editable) {
				onChange();
			}
		};

		hostField.addTextChangedListener(tw);
		usernameField.addTextChangedListener(tw);
		passwordField.addTextChangedListener(tw);
		hostList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if(i == 0) ((TextView)adapterView.getChildAt(0)).setTextColor(Color.GRAY);

				if(i == hostList.getCount() - 1) hostField.setVisibility(View.VISIBLE);
				else hostField.setVisibility(View.GONE);

				onChange();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				onChange();
			}
		});

		onChange();

		signinButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((TextView)findViewById(R.id.errorLabel)).setText("");
				signinButton.setVisibility(View.INVISIBLE);
				findViewById(R.id.loadingIcon).setVisibility(View.VISIBLE);

			    final Thread t = new Thread(new Runnable() {
				    @Override
				    public void run() {
				    	String host = (String)hostList.getSelectedItem();
				    	if(hostList.getSelectedItemPosition() == hostList.getCount() - 1) host = hostField.getText().toString();
					    final Account account = new Account(host, usernameField.getText().toString(), passwordField.getText().toString());
					    final Object res = account.signIn();

					    if((res.getClass() == boolean.class || res.getClass() == Boolean.class) && (Boolean)res) account.signOut();

					    Handler mainHandler = new Handler(Looper.getMainLooper());
					    Runnable r = new Runnable() {
						    @Override
						    public void run() {
							    signinButton.setVisibility(View.VISIBLE);
							    findViewById(R.id.loadingIcon).setVisibility(View.INVISIBLE);

							    if(Exception.class.isAssignableFrom(res.getClass())){
								    Exception e = (Exception)res;
								    ((TextView)findViewById(R.id.errorLabel)).setText(e.getLocalizedMessage());
							    } else if(res.getClass() == boolean.class || res.getClass() == Boolean.class){
								    if(!(Boolean)res){
									    ((TextView)findViewById(R.id.errorLabel)).setText(getString(R.string.invalidCreds));
								    } else {
									    SharedPreferences prefs = context.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
										SharedPreferences.Editor editor = prefs.edit();

										editor.putString("host", account.host);
									    editor.putString("username", account.username);
									    editor.putString("password", account.password);

									    editor.apply();
										
										startActivity(new Intent(Variables.get().currentContext, StartActivity.class));
								    }
							    }
						    }
					    };
					    mainHandler.post(r);
				    }
			    });
			    t.start();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		Variables.get().currentContext = this;
	}

	private void onChange(){
		if(hostList.getSelectedItem() != null && hostList.getSelectedItemPosition() != 0 && usernameField.getText().length() > 0 && passwordField.getText().length() > 0 && (hostList.getSelectedItemPosition() < hostList.getCount() - 1 || hostField.getText().length() > 0)){
			signinButton.setEnabled(true);
			signinButton.setAlpha(1f);
		} else {
			signinButton.setEnabled(false);
			signinButton.setAlpha(0.5f);
		}
	}
}