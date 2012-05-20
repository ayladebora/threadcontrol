package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class NotifierUntilDeath2 extends MonitorableThread {

	Object lock;
	Object internalLock;
	private boolean shouldShutdown;
	private long timeBetweenNotifications;
	private boolean makeNotifyReceived;
	
	
	public NotifierUntilDeath2(Object lock) {
		super("NotifierUntilDeathThread2");
		this.lock = lock;
		this.timeBetweenNotifications = 100;
		this.internalLock = new Object();
	}
	public void run(){

			boolean firstTime = true;
			while (!shouldShutdown && (firstTime  || makeNotifyReceived)){
				synchronized (lock){
					if ((!shouldShutdown && (firstTime  || makeNotifyReceived))){			
						synchronized (internalLock){
							if (makeNotifyReceived){
								lock.notify();
								this.makeNotifyReceived = false;
							} else if (firstTime){
								lock.notify();
							}
							firstTime = false;
						}
					}
				}
				synchronized (internalLock){
					if ((!shouldShutdown && (! makeNotifyReceived))){
						try {
							internalLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		hasExecuted = true;
	}
	public void makeNotify(){
		synchronized (internalLock){
			this.makeNotifyReceived = true;
			internalLock.notify();
		}
	}
	/**
	 * 
	 */
	public void shutdown(){
		if (!shouldShutdown){
			synchronized (lock){
				  synchronized (internalLock){
					  this.shouldShutdown = true;
					  this.internalLock.notify();
				  }
			}
		}
	}
	public long getTimeBetweenNotifications() {
		return timeBetweenNotifications;
	}
	public void setTimeBetweenNotifications(long timeBetweenNotifications) {
		this.timeBetweenNotifications = timeBetweenNotifications;
	}

}
