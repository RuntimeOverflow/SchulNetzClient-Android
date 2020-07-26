package com.runtimeoverflow.SchulNetzClient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Transaction;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Variables;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transactions_activity);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.transactions));
		
		double balance = 0;
		for(final Transaction t : Variables.get().user.transactions){
			final ConstraintLayout row = (ConstraintLayout)LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.transaction_cell, null);
			
			((Button)row.findViewById(R.id.reasonButton)).setText(t.reason);
			((TextView)row.findViewById(R.id.amountLabel)).setText((t.amount >= 0 ? "+" : "") + String.format(Locale.ENGLISH, "%.2f", t.amount));
			((TextView)row.findViewById(R.id.amountLabel)).setTextColor(Color.parseColor(t.amount >= 0 ? "#ff00ff00" : "#ffff0000"));
			
			balance += t.amount;
			
			((Button)row.findViewById(R.id.reasonButton)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int maxLines = ((Button)row.findViewById(R.id.reasonButton)).getMaxLines();
					
					if(maxLines == 1) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy");
						
						((Button)row.findViewById(R.id.reasonButton)).setMaxLines(Integer.MAX_VALUE);
						((Button)row.findViewById(R.id.reasonButton)).setText(t.reason + " (" + sdf.format(t.date.getTime()) + ")");
					} else{
						((Button)row.findViewById(R.id.reasonButton)).setMaxLines(1);
						((Button)row.findViewById(R.id.reasonButton)).setText(t.reason);
					}
				}
			});
			
			((LinearLayout)findViewById(R.id.transactionsList)).addView(row);
		}
		
		((TextView)findViewById(R.id.balanceLabel)).setText(String.format(Locale.ENGLISH, "%.2f", balance));
		((TextView)findViewById(R.id.balanceLabel)).setTextColor(Color.parseColor(balance >= 0 ? "#ff00ff00" : "#ffff0000"));
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