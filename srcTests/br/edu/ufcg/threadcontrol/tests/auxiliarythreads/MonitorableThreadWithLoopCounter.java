package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

import umontreal.iro.lecuyer.probdist.ExponentialDist;
import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.WELL607;

public class MonitorableThreadWithLoopCounter extends Thread {
	protected boolean hasExecuted;
	int numLoopsExecuted = 0;
	private static final int MAX_LOOPS =4;
	
	public MonitorableThreadWithLoopCounter(String name) {
		super(name);
		hasExecuted = false;
	}
	public MonitorableThreadWithLoopCounter(){
		this("MonitorableThread");
	}
	
	public int getNumLoopsExecuted(){
		return numLoopsExecuted;
	}

	public boolean hasExecuted(){
		return hasExecuted;
	}

	public void run(){
		System.out.println("will execute monitorable");
		sleepBefore();
//		long randomInterval = (long)((5)*Math.random());
//		System.out.println("Rand:"+randomInterval);
//		waitForACertainTime(randomInterval);
		for (; numLoopsExecuted<MAX_LOOPS;){
			numLoopsExecuted++;
			hasExecuted = true;
			try {
				ExponentialDist expDis = new ExponentialDist(0.6);
				ExponentialGen expGen = new ExponentialGen(new WELL607(),expDis);
				long randomInterval = 0;
				while (randomInterval == 0){
					randomInterval = (long)(expGen.nextDouble());
					System.out.println("rand-tmp:"+randomInterval);
				}
				System.out.println("rand:"+randomInterval+ " numLoops:"+numLoopsExecuted);
            	synchronized(this){
    				System.out.println("will wait:"+randomInterval);
            		this.wait(randomInterval);
            		System.out.println("waited");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
		System.out.println("Finished execution");
	}
	private void sleepBefore() {
		ExponentialDist expDis = new ExponentialDist(0.5);
		ExponentialGen expGen = new ExponentialGen(new WELL607(),expDis);
		long randomInterval = (long)(expGen.nextDouble());
//		System.out.println("rand:"+randomInterval);
		try {
			Thread.sleep(randomInterval);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
