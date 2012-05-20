package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SimpleNotifiableThread extends MonitorableThread implements
		NotifiableThread {
	private Object lock;
	private boolean shouldShutdown = false;
	public SimpleNotifiableThread(String name, Object lock) {
		super(name);
		this.lock = lock;
	}

	public SimpleNotifiableThread(Object lock) {
		this("SimpleNotifiableThread", lock);
		
	}

	public void run(){
		synchronized (lock){
			try {
				while (!shouldShutdown ){
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		hasExecuted = true;
	}

	public void wakeUp() {
		synchronized (lock){
			lock.notify();
		}
	}
	
	public synchronized void shutdown(){
		this.shouldShutdown = true;
		synchronized(lock){
			lock.notify();
		}
	}

}
