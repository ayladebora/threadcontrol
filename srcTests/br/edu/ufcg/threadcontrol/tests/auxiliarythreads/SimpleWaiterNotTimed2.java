package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SimpleWaiterNotTimed2   extends MonitorableThread implements
				NotifiableThread {

			private Object lock;

			private boolean shouldShutdown = false;
			private int executionTimes = 0;

			private NotifierUntilDeath2 nud2;

			public SimpleWaiterNotTimed2(String name, Object lock, NotifierUntilDeath2 nud2) {
				super(name);
				this.lock = lock;
				this.nud2 = nud2;
			}

			public SimpleWaiterNotTimed2(Object lock, NotifierUntilDeath2 nud2) {
				this("SimpleWaiterNotTimed2", lock, nud2);

			}

			public void run() {
				synchronized (lock) {
					while (!shouldShutdown) {
						try {
							executionTimes++;
							lock.wait();
							this.nud2.makeNotify();
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
					lock.notify();
				}
			}
			
			public int getExecutionTimes(){
				return this.executionTimes;
			}
		}

