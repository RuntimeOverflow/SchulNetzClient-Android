package com.runtimeoverflow.SchulNetzClient;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.runtimeoverflow.SchulNetzClient.Activities.StartActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Utilities {
	public static boolean hasWifi(){
		ConnectivityManager connectivityManager = (ConnectivityManager)Variables.get().currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
			if(connectivityManager.getActiveNetwork() == null) return false;
			else return connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()).hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
		} else {
			if(connectivityManager.getActiveNetworkInfo() == null) return false;
			else return connectivityManager.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
		}
	}
	
	public static boolean isInForeground(Context context){
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null) {
			return false;
		}
		
		final String packageName = context.getPackageName();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
				return true;
			}
		}
		
		return false;
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
	
	public static void sendNotification(String title, String content){
		SharedPreferences prefs = Variables.get().currentContext.getSharedPreferences("com.runtimeoverflow.SchulNetzClient", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("com.runtimeoverflow.SchulNetzClient", Variables.get().currentContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
			NotificationManager notificationManager = Variables.get().currentContext.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
		
		Intent intent = new Intent(Variables.get().currentContext, StartActivity.class);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setAction(Intent.ACTION_MAIN);
		PendingIntent pendingIntent = PendingIntent.getActivity(Variables.get().currentContext, 0, intent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(Variables.get().currentContext, "com.runtimeoverflow.SchulNetzClient");
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setContentTitle(title);
		builder.setContentText(content).setStyle(new NotificationCompat.BigTextStyle().bigText(content));
		builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
		builder.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
		builder.setContentIntent(pendingIntent).setAutoCancel(true);
		
		int id = prefs.getInt("notificationId", 0);
		editor.putInt("notificationId", id + 1);
		editor.apply();
		editor.commit();
		
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Variables.get().currentContext);
		notificationManager.notify(id, builder.build());
	}
	
	public static double roundToDecimalPlaces(double value, int places){
		return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
}
