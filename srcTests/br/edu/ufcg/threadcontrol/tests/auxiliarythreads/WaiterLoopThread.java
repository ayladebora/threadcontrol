package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class WaiterLoopThread extends MonitorableThread implements LooperThread{

	private boolean wasWakedUp = false;
	public WaiterLoopThread() {
		super("WaiterLoopThread");
		executionTimes = 0;
		shouldShutdown = false;
	}

		private int executionTimes;
		private int waitExecutions = 0;
		private boolean shouldShutdown;

		public void run(){
			boolean firstRun = true;
			synchronized (this) {
				while (!shouldShutdown || firstRun){
						try {
							while (!this.wasWakedUp){
								this.wait();
								this.waitExecutions++;
							}						
							increaseExecutionTimes();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (!shouldShutdown){
							try {
								this.wait();
								this.waitExecutions++;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					firstRun = false;
				}
			
			}
			hasExecuted = true;
		}
		
		public void increaseExecutionTimes(){
			this.executionTimes++;
		}
		/**
		 * @see br.edu.ufcg.threadcontrol.tests.LooperThread#wakeUp()
		 */
		public synchronized void wakeUp(){
			this.wasWakedUp = true;
			this.notifyAll();
		}
		
		
		/**
		 * @see br.edu.ufcg.threadcontrol.tests.LooperThread#shutdown()
		 */
		public synchronized void shutdown(){
			this.shouldShutdown = true;
			this.notifyAll();
		}
		
		/**
		 * @see br.edu.ufcg.threadcontrol.tests.LooperThread#getExecutionTimes()
		 */
		public int getExecutionTimes(){
			return this.executionTimes;
		}

		public int getNumberOfWaitExecutions(){
			return this.waitExecutions;
		}


	}

