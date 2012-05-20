package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SimpleNotifiableThread2 extends MonitorableThread implements
		NotifiableThread {
	private Object lock;
	private NotifierUntilDeath nud;

	public SimpleNotifiableThread2(String name, Object lock, NotifierUntilDeath nud) {
		super(name);
		this.lock = lock;
		this.nud = nud;
	}

	public SimpleNotifiableThread2(Object lock, NotifierUntilDeath nud) {
		this("SimpleNotifiableThread2", lock, nud);

	}

	public void run() {
		System.out.println("{{before_lock");
		synchronized (lock) {
			System.out.println("{{inside_lock");
			try {
				System.out.println("&&&WILL WAIT SimpleNotifiableThread2");
				lock.wait();
				System.out.println("&&&WAITED SimpleNotifiableThread2");
				this.nud.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		hasExecuted = true;
	}

	public void wakeUp() {
		synchronized (lock) {
			lock.notify();
		}
	}

}
