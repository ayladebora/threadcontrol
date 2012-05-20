package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class TemporaryWaiterThread extends MonitorableThread implements LooperThread {
	private int executionTimes;
	private boolean shouldShutdown;
	private boolean wasWakedUp;

	public TemporaryWaiterThread(String name) {
		super(name);
	}

	public TemporaryWaiterThread() {
		this("TemporaryWaiterThread");
	}


	
	public void run(){
		boolean firstRun = true;
		synchronized (this) {
			while (!shouldShutdown || firstRun){
					try {
						while (!this.wasWakedUp){
							this.wait(500);
						}		
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					increaseExecutionTimes();
					firstRun = false;
					try {
						this.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		
		}
		hasExecuted = true;
	}
	
	public void increaseExecutionTimes(){
		this.executionTimes++;
	}
	public synchronized void wakeUp(){
		this.wasWakedUp = true;
		this.notifyAll();
	}
	public synchronized void shutdown(){
		this.shouldShutdown = true;
		this.notifyAll();
	}
	
	public int getExecutionTimes(){
		return this.executionTimes;
	}

}
