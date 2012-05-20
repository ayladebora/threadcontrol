package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SimpleWaiterClass extends MonitorableThread implements
		NotifiableThread {
	private static final long WAIT_TIME = 3600000;

	private Object lock;

	private boolean shouldShutdown = false;

	private NotifierUntilDeath2 nud2;

	public SimpleWaiterClass(String name, Object lock, NotifierUntilDeath2 nud2) {
		super(name);
		this.lock = lock;
		this.nud2 = nud2;
	}

	public SimpleWaiterClass(Object lock, NotifierUntilDeath2 nud2) {
		this("SimpleWaiterClass", lock, nud2);

	}

	public void run() {
		synchronized (lock) {
			while (!shouldShutdown) {
				// System.out.println("{{inside_lock");
				try {
					// System.out.println("&&&WILL WAIT
					// SimpleNotifiableThread4");
					lock.wait(WAIT_TIME);
					// System.out.println("&&&WAITED SimpleNotifiableThread4");
					this.nud2.shutdown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		hasExecuted = true;
	}

	public void wakeUp() {
		synchronized (lock) {
			lock.notify();
		}
	}

	public synchronized void shutdown() {
		this.shouldShutdown = true;
		synchronized (lock) {
			lock.notifyAll();
		}
	}
}
