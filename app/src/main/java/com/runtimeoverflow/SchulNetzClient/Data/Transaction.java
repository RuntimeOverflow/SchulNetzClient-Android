package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

public class Transaction {
	public Calendar date;
	public String reason;
	public double amount;
}
