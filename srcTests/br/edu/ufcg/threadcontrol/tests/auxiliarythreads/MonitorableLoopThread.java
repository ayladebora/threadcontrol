package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class MonitorableLoopThread extends Thread{

	private static final long DEFAULT_TIME_TO_SLEEP = 10000;
	private int executionTimes;
	private boolean shouldShutdown;
	private long timeToSleep = DEFAULT_TIME_TO_SLEEP;
	
	public MonitorableLoopThread() {
		super("MonitorableLoopThread");
		executionTimes = 0;
		shouldShutdown = false;
	}

	public void run(){
		while (!shouldShutdown){
			this.executionTimes++;
			try {
				Thread.sleep(timeToSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void shutdown(){
		this.shouldShutdown = true;
	}
	
	public int getExecutionTimes(){
		return this.executionTimes;
	}

	public long getTimeToSleep() {
		return timeToSleep;
	}

	public void setTimeToSleep(long timeToSleep) {
		this.timeToSleep = timeToSleep;
	}


}
