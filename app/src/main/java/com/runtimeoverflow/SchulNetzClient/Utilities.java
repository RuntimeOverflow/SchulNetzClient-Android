package com.runtimeoverflow.SchulNetzClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Utilities {
	public static boolean hasWifi(){
		ConnectivityManager connectivityManager = (ConnectivityManager)Variables.get().currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
	}
	
	public static void runAsynchronous(final AsyncAction action){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				action.runAsync();
				
				Handler mainHandler = new Handler(Looper.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						action.runSyncWhenDone();
					}
				});
			}
		});
		t.start();
	}
}
