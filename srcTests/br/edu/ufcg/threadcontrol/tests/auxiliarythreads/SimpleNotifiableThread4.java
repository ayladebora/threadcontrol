package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SimpleNotifiableThread4 extends MonitorableThread implements
		NotifiableThread {
	private Object lock;
	private Object internalLock;
	private NotifierUntilDeath2 nud;

	public SimpleNotifiableThread4(String name, Object lock, NotifierUntilDeath2 nud) {
		super(name);
		this.lock = lock;
		this.nud = nud;
		this.internalLock = new Object();
	}

	public SimpleNotifiableThread4(Object lock, NotifierUntilDeath2 nud) {
		this("SimpleNotifiableThread4", lock, nud);

	}

	public void run() {
		synchronized (lock) {
			try {
				lock.wait();
				this.nud.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (internalLock){
			try {
				internalLock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		hasExecuted = true;
	}

	public void shutdown() {
		synchronized (internalLock) {
			internalLock.notify();
		}
	}
	public void wakeUp() {
		this.shutdown();
	}

}
