package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class SleeperThread extends MonitorableThread {
	private int executionTimes;
	private boolean shouldShutdown;
	public SleeperThread(String name) {
		super(name);
		this.executionTimes = 0;
	}
	public SleeperThread() {
		this("SleeperThread");
	}

	public void run(){
		while (!shouldShutdown){
			System.out.println("Will sleep");
			try {
				executionTimes++;
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		hasExecuted = true;
	}
	
	public synchronized void shutdown(){
		this.shouldShutdown = true;
	}
	
	public int getExecutionTimes(){
		return this.executionTimes;
	}

}
