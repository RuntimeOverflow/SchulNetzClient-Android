package com.runtimeoverflow.SchulNetzClient;

public class SessionManager implements Runnable {
	private Thread thread = null;
	private Account account = null;

	public SessionManager(Account account){
		this.account = account;
	}

	public void start(){
		thread = new Thread(this);
		thread.start();
	}

	public void stop(){
		account = null;
	}

	@Override
	public void run() {
		while(account != null){
			account.resetTimeout();

			try {
				Thread.sleep(20 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				start();
			}
		}
	}
}
