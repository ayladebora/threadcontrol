package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class NotifierThread extends MonitorableThread {
	NotifiableThread wlt;
	public NotifierThread(NotifiableThread wlt) {
		super("NotifierThread");
		this.wlt = wlt;
	}
	public void run(){
		System.out.println("will execute monitorable");
		this.wlt.wakeUp();
		hasExecuted = true;
	}

}
