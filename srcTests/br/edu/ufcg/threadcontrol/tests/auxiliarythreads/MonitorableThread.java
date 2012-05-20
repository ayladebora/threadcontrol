package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class MonitorableThread extends Thread {
	protected boolean hasExecuted;
	
	public MonitorableThread(String name) {
		super(name);
		hasExecuted = false;
	}
	public MonitorableThread(){
		this("MonitorableThread");
	}

	public boolean hasExecuted(){
		return hasExecuted;
	}

	public void run(){
		System.out.println("will execute monitorable");
		hasExecuted = true;
		System.out.println("executed MonitorableThread.");
	}

}
