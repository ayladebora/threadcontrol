package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

import umontreal.iro.lecuyer.probdist.ExponentialDist;
import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.WELL607;

public class MonitorableThread_sleepingBefore extends Thread {
	protected boolean hasExecuted;
	
	public MonitorableThread_sleepingBefore(String name) {
		super(name);
		hasExecuted = false;
	}
	public MonitorableThread_sleepingBefore(){
		this("MonitorableThread");
	}

	public boolean hasExecuted(){
		return hasExecuted;
	}

	public void run(){
		ExponentialDist expDis = new ExponentialDist(0.5);
		ExponentialGen expGen = new ExponentialGen(new WELL607(),expDis);
		double nextDouble = expGen.nextDouble();
		long randomInterval = (long)(nextDouble);
		System.out.println("rand:"+randomInterval+ " double:"+nextDouble);
		try {
			Thread.sleep(randomInterval);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("will execute monitorable");
		hasExecuted = true;
	}

}
