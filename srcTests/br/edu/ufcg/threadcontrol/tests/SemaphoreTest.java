package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class SemaphoreTest extends TestCase {

	/**
	* This test should be ignored. We dont know why we did this...
	*/
	public void testSchedulerAndExecutorWithSemaphore() throws InterruptedException{
		ExecutorThread.resetExecutionTimes();
		ThreadControl tcs = new ThreadControl();
		
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreTest.SchedulerThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreTest.ExecutorThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreTest.JobAdderThread",
				ThreadState.FINISHED, 2);
		threadsConfiguration.add(conf3);
		tcs.prepare(threadsConfiguration);

		SchedulerThread sched = new SchedulerThread(5);
		sched.start();
		
		JobAdderThread jobAdderThread6 = new JobAdderThread(sched, 6);
		JobAdderThread jobAdderThread7 = new JobAdderThread(sched, 7);

		jobAdderThread6.start();
		jobAdderThread7.start();
		
		tcs.waitUntilStateIsReached();
		assertEquals(7, ExecutorThread.getExecutionTimes());
		tcs.proceed();

		sched.shutdown();
		sched.join();
	}
	
	// TODO Do this to all tests. (Stress test)
	public void donttestSeveralTimes() throws Exception {
		int cont = 0;
		while (true) {
			cont++;
			System.out.println("====> Test:"+cont);
			this.setUp();
			this.testSchedulerAndExecutorWithSemaphore();
			this.tearDown();
		}
	}
	
	static class JobAdderThread extends Thread {
		
		private SchedulerThread schedulerThread;
		private int job;

		public JobAdderThread(SchedulerThread schedulerThread, int job) {
			this.schedulerThread = schedulerThread;
			this.job = job;
		}
		
		@Override
		public void run(){
			schedulerThread.addJob( this.job );
		}
	}
	
	static class SchedulerThread extends Thread {

		private volatile boolean shouldShutdown;

		private int numberOfJobsToScheduleOnStart;
		private Semaphore semaphore;
		
		SchedulerThread(int numberOfJobsToScheduleOnStart){
			this.semaphore = new Semaphore(0);
			this.numberOfJobsToScheduleOnStart = numberOfJobsToScheduleOnStart;
		}

		@Override
		public void run(){

			for ( int i = 0; i < numberOfJobsToScheduleOnStart; i++ ) {
				addJob( i );
			}

			while (!shouldShutdown){
				synchronized ( this ) {
					try {
						this.wait();
					} catch ( Exception e ) {
						e.printStackTrace();
					}			
				}
			}
		}
		
		public void addJob(int jobNumber){
			ExecutorThread et = new ExecutorThread(jobNumber, semaphore);
			et.start();
			synchronized ( this ) {
				this.notify();	
				try {
					semaphore.release();
				} catch(Exception e){
					e.printStackTrace();
				}
			}			
		}
		
		public synchronized void shutdown() {
			shouldShutdown = true;
			this.notify();
		}
	}

	static class ExecutorThread extends Thread {
		static AtomicInteger EXECUTION_TIMES = new AtomicInteger(0);
		private int jobNumber;
		private Semaphore semaphore;
		
		public ExecutorThread(int jobNumber, Semaphore semaphore){
			this.jobNumber = jobNumber;
			this.semaphore = semaphore;
		}
		
		@Override
		public void run(){
			try {
				this.semaphore.acquire();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("**Processing job:"+jobNumber);
			EXECUTION_TIMES.incrementAndGet();
		}
		
		public static void resetExecutionTimes(){
			EXECUTION_TIMES.set(0);
		}

		public static int getExecutionTimes(){
			return EXECUTION_TIMES.get();
		}
		
	}
}
