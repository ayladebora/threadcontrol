package br.edu.ufcg.threadcontrol.tests.auxiliaryadvancedthreads;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread;

public class SimpleLockExampleThread extends MonitorableThread{

	private Lock lock;
	private boolean shouldShutdown;
	public SimpleLockExampleThread(Lock lock) {
		super("SimpleLockExampleThread");
		this.lock = lock;
	}

	public Condition condition;
	public void run() {

			while (!shouldShutdown) {
					lock.lock();
			}
			lock.unlock();
		hasExecuted = true;
	}
	
	public void shutdown() {
		this.shouldShutdown = true;
		lock.unlock();
	}

}
