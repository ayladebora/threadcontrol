package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;

import br.edu.ufcg.threadcontrol.ListOfThreadConfigurations;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import junit.framework.TestCase;

public class SchedulerAndExecutorTest extends TestCase {

	public void testSchedulerAndExecutor() throws InterruptedException{
		System.out.println("will start");
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SchedulerAndExecutorTest.SchedulerThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SchedulerAndExecutorTest.ExecutorThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		ExecutorThread executor = new ExecutorThread();
		SchedulerThread sched = new SchedulerThread(executor);
		executor.start();
		while (!Thread.State.WAITING.equals( executor.getState() )) {
			Thread.sleep( 100 );
		}
		sched.start();
		tcs.waitUntilStateIsReached();
		assertEquals(0, executor.getExecutionTimes());
		System.out.println("first assert");
		tcs.proceed();
		tcs.prepare(threadsConfiguration);
		System.out.println("===>before sleep");
		synchronized(this){
			this.wait(1000);
		}
		System.out.println("second assert");
		sched.addJob();
		System.out.println("third");
		tcs.waitUntilStateIsReached();
		assertEquals(1, executor.getExecutionTimes());
		tcs.proceed();
		System.out.println("first shutdown");
		sched.shutdown();
		System.out.println("second shutdown");
		executor.shutdown();
		System.out.println("will proceed");
		
		sched.join();
		executor.join();
	}
	
	public void testSchedulerAndExecutorExcludingThreads() throws InterruptedException{
		System.out.println("will start");
		ThreadControl tcs = new ThreadControl();
		ListOfThreadConfigurations sysConfig = new ListOfThreadConfigurations();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SchedulerAndExecutorTest.SchedulerThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		sysConfig.addThreadConfiguration(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SchedulerAndExecutorTest.ExecutorThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		sysConfig.addThreadConfiguration(conf2);
		sysConfig.addToClassesToBeIgnored(ClassToExclude.class);
		tcs.prepare(sysConfig);
		ExecutorThread executor = new ExecutorThread();
		SchedulerThread sched = new SchedulerThread(executor);
		ClassToExclude classToExclude = new ClassToExclude(executor);
		executor.start();
		classToExclude.start();
		while (!Thread.State.WAITING.equals( executor.getState() )) {
			Thread.sleep( 100 );
		}
		sched.start();
		tcs.waitUntilStateIsReached();
		assertEquals(0, executor.getExecutionTimes());
		System.out.println("first assert");
		tcs.proceed();
		tcs.prepare(sysConfig);
		System.out.println("===>before sleep");
		synchronized(this){
			this.wait(1000);
		}
		System.out.println("second assert");
		sched.addJob();
		System.out.println("third");
		tcs.waitUntilStateIsReached();
		assertEquals(1, executor.getExecutionTimes());
		tcs.proceed();
		System.out.println("first shutdown");
		sched.shutdown();
		System.out.println("second shutdown");
		executor.shutdown();
		System.out.println("will proceed");
		classToExclude.shutdown();
		sched.join();
		executor.join();
		classToExclude.join();
	}
	
	class SchedulerThread extends Thread {
		private ExecutorThread execThread;
		private boolean shouldShutdown;
		SchedulerThread(ExecutorThread execThread){
			this.execThread = execThread;
		}
		public void run(){
			synchronized (this){
				while (!shouldShutdown){
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!shouldShutdown){
						execThread.execJob();
					}
				}
			}
		}
		
		public void addJob(){
			synchronized(this){
				this.notify();
			}
		}
		
		public void shutdown() {
			synchronized(this){
				this.shouldShutdown = true;
				this.notify();
			}
		}
	}

	class ExecutorThread extends Thread {
		private boolean shouldShutdown;
		private int executionTimes = 0;
		public void run(){
			synchronized(this){
				while(!shouldShutdown){
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!shouldShutdown){
						System.out.println("Executing my job");
						executionTimes++;
					}
				}
			}
		}
		public int getExecutionTimes(){
			return executionTimes;
		}
		public void shutdown() {
			synchronized(this){
				this.shouldShutdown = true;
				this.notify();
			}
		}
		public void execJob(){
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	class ClassToExclude extends Thread{
		private ExecutorThread execThread;
		boolean shouldShutdown;
		public ClassToExclude(ExecutorThread et){
			this.execThread = et;
		}
		public void run(){
			synchronized(this){
					while (!shouldShutdown){
						synchronized(execThread){
							try {
								this.wait(1000);
								System.out.println("liberou");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
			}
		}
		
		public void shutdown(){
			synchronized(this){
				this.shouldShutdown = true;
				this.notify();
			}
		}
	}
}


