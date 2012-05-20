package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SimpleNotifiableThread3 extends MonitorableThread implements
		NotifiableThread {
	private static final long WAIT_TIME = 360000;
	private Object lock;
	private boolean shouldShutdown = false;
	private NotifierUntilDeath2 nud2;
	public SimpleNotifiableThread3(String name, Object lock, NotifierUntilDeath2 nud2) {
		super(name);
		this.lock = lock;
		this.nud2 = nud2;
	}

	public SimpleNotifiableThread3(Object lock, NotifierUntilDeath2 nud2) {
		this("SimpleNotifiableThread3", lock, nud2);
		
	}

	public void run(){
		synchronized (lock){
			try {
				while (!shouldShutdown ){
					lock.wait(WAIT_TIME);
					nud2.makeNotify();
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
