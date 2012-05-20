package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class NotifierUntilDeath extends MonitorableThread {

	Object lock;
	private boolean shouldShutdown;
	private long timeBetweenNotifications;
	
	
	public NotifierUntilDeath(Object lock) {
		super("NotifierUntilDeathThread");
		this.lock = lock;
		this.timeBetweenNotifications = 100;
	}
	public void run(){
		synchronized (lock){
			while (!shouldShutdown){
				System.out.println("will execute monitorable");
				lock.notify();
				try {
					lock.wait(timeBetweenNotifications);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		hasExecuted = true;
	}
	/**
	 * 
	 */
	public synchronized void shutdown(){
		this.shouldShutdown = true;
	}
	public long getTimeBetweenNotifications() {
		return timeBetweenNotifications;
	}
	public void setTimeBetweenNotifications(long timeBetweenNotifications) {
		this.timeBetweenNotifications = timeBetweenNotifications;
	}

}
