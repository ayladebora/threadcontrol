package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class SemaphoreTwoAcquiresAndOnePermitTest extends TestCase {

	public void testSchedulerAndExecutorWithSemaphore() throws InterruptedException{
		ExecutorThread.resetExecutionTimes();
		ThreadControl tcs = new ThreadControl();
		
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreTwoAcquiresAndOnePermitTest.SchedulerThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		
		int numberOfThreadsToBeInState = 1;
		
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreTwoAcquiresAndOnePermitTest.ExecutorThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE, numberOfThreadsToBeInState);
		threadsConfiguration.add(conf2);
		
		//The following configuration was added because without it, it was possible
		//to achieve the expected state before one of the threads has executed.
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreTwoAcquiresAndOnePermitTest.ExecutorThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE, numberOfThreadsToBeInState);
		threadsConfiguration.add(conf3);
		
		tcs.prepare(threadsConfiguration);

		int numberOfExecutorsToStart = 2;
		
		SchedulerThread sched = new SchedulerThread(numberOfExecutorsToStart);
		sched.start();
		System.out.println("--will wait");
		tcs.waitUntilStateIsReached();
		System.out.println("--waited");
		assertEquals(1, ExecutorThread.EXECUTION_TIMES);
		
		tcs.proceed();
		System.out.println("--proceeded");
		sched.shutdown();
		System.out.println("--shutdown called");
		sched.join();
		System.out.println("--finished");
	}
	
	// TODO Do this to all tests. (Stress test)
	public void dontTestSeveralTimes() throws Exception {
		int cont = 0;
		while (true) {
			System.out.println("====> Test:"+cont);
			this.setUp();
			this.testSchedulerAndExecutorWithSemaphore();
			this.tearDown();
			System.out.println("=========================");
		}
	}

	static class SchedulerThread extends Thread {

		private Semaphore semaphore;

		private ExecutorThread[ ] executors;
		
		SchedulerThread(int numberOfExecutorsToStart){
			this.semaphore = new Semaphore(0);
			executors = new ExecutorThread[numberOfExecutorsToStart];
			for ( int i = 0; i < numberOfExecutorsToStart; i++ ) {
				executors[i] = new ExecutorThread(semaphore);
				executors[i].start();
			}
		}

		@Override
		public void run(){
			semaphore.release();	
		}
		
		public void addJob(int jobNumber){
			synchronized ( this ) {
				this.notify();	
			}
		
		}
		
		public void shutdown() throws InterruptedException {
			for ( int i = 0; i < executors.length; i++ ) {
				ExecutorThread executor = executors[i];
				System.out.println();
				executor.shutdown();
				executor.join();
			}
		}
	}

	static class ExecutorThread extends Thread {
		static int EXECUTION_TIMES = 0;
		private Semaphore semaphore;
		boolean wasStopped = false;
		
		public ExecutorThread(Semaphore semaphore){
			this.semaphore = semaphore;
		}
		
		@Override
		public void run(){
			try {
				this.semaphore.acquire();
				if (!wasStopped){
					System.out.println("**Processing one job."+this);
					EXECUTION_TIMES++;
				}
			} catch (Exception e) {
				if (e instanceof InterruptedException){
					System.out.println("--interrupted "+this);
				} else {
					e.printStackTrace();
				}
			}
		}

		public void shutdown() {
			wasStopped = true;
			this.interrupt();
			this.semaphore.release();
			System.out.println("--shutdown was called on "+this);
		}
		
		public static void resetExecutionTimes(){
			EXECUTION_TIMES = 0;
		}

		public static int getExecutionTimes(){
			return EXECUTION_TIMES;
		}
		
	}
}
