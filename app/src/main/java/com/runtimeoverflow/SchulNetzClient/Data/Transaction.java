package com.runtimeoverflow.SchulNetzClient.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Transaction {
	public Calendar date;
	public String reason;
	public double amount;
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Transaction that = (Transaction) o;
		return Objects.equals(reason, that.reason);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(reason);
	}
}
