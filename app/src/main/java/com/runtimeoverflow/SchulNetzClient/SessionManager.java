package com.runtimeoverflow.SchulNetzClient;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SessionManager implements Runnable, Application.ActivityLifecycleCallbacks {
	private Thread thread = null;
	private Account account = null;
	
	private boolean running = false;

	public SessionManager(Account account){
		this.account = account;
	}

	public void start(){
		thread = new Thread(this);
		thread.start();
	}

	public void stop(){
		running = false;
	}

	@Override
	public void run() {
		if(running) return;
		
		running = true;
		
		while(running){
			account.resetTimeout();

			try {
				Thread.sleep(20 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				start();
			}
		}
	}
	
	@Override
	public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}
	
	@Override
	public void onActivityStarted(@NonNull Activity activity) {
		if(!account.signedIn){
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					account.signIn();
				}
			});
			t.start();
		}
	}
	
	@Override
	public void onActivityResumed(@NonNull Activity activity) {}
	
	@Override
	public void onActivityPaused(@NonNull Activity activity) {}
	
	@Override
	public void onActivityStopped(@NonNull Activity activity) {
		if(!Utilities.isInForeground(activity) && account.signedIn){
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					account.signOut();
				}
			});
			t.start();
		}
	}
	
	@Override
	public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}
	
	@Override
	public void onActivityDestroyed(@NonNull Activity activity) {}
}
