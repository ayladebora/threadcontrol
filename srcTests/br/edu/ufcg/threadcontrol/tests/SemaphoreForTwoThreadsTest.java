package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class SemaphoreForTwoThreadsTest extends TestCase {

	public void testSchedulerAndExecutorWithSemaphore() throws InterruptedException {

		ExecutorThread.resetExecutionTimes();
		ThreadControl tcs = new ThreadControl();

		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreForTwoThreadsTest.SchedulerThread", ThreadState.WAITING,
				ThreadConfiguration.AT_LEAST_ONCE );
		threadsConfiguration.add( conf1 );
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.SemaphoreForTwoThreadsTest.ExecutorThread", ThreadState.WAITING,
				ThreadConfiguration.AT_LEAST_ONCE );
		threadsConfiguration.add( conf2 );
		tcs.prepare( threadsConfiguration );

		int expectedNumberOfExecutionTimes = 5;
		
		SchedulerThread sched = new SchedulerThread( expectedNumberOfExecutionTimes );
		sched.start();
		System.out.println("===>will wait until...");
		tcs.waitUntilStateIsReached();
		System.out.println("===>waited until...");
		assertEquals( expectedNumberOfExecutionTimes, ExecutorThread.EXECUTION_TIMES );
		tcs.proceed();

		sched.shutdown();
		sched.join();
	}


	// TODO Do this to all tests. (Stress test)
	public void donttestSeveralTimes() throws Exception {

		int cont = 0;
		while ( true ) {
			System.out.println( "====> Test:" + cont );
			this.setUp();
			this.testSchedulerAndExecutorWithSemaphore();
			this.tearDown();
		}
	}

	static class SchedulerThread extends Thread {

		private volatile boolean shouldShutdown;

		private int numberOfJobsToScheduleOnStart;

		private Semaphore semaphore;

		private ExecutorThread et;


		SchedulerThread( int numberOfJobsToScheduleOnStart ) {

			this.semaphore = new Semaphore( 0 );
			this.numberOfJobsToScheduleOnStart = numberOfJobsToScheduleOnStart;
			et = new ExecutorThread( semaphore );
			et.start();
		}


		@Override
		public void run() {

			for ( int i = 0; i < numberOfJobsToScheduleOnStart; i++ ) {
				addJob( );
			}

			while ( !shouldShutdown ) {
				synchronized ( this ) {
					try {
						this.wait();
					} catch ( InterruptedException e ) {
						// TODO Auto-generated catch block
					}
				}
			}
		}


		private void addJob() {

			synchronized ( this ) {
				this.notify();
			}
			System.out.println("->calling release");
			semaphore.release();
			System.out.println("->has called release");
		}


		public void shutdown() {

			shouldShutdown = true;
			et.shutdown();
			try {
				et.join();
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
			synchronized(this){
				this.notify();
			}
		}
	}

	static class ExecutorThread extends Thread {

		static int EXECUTION_TIMES = 0;

		private Semaphore semaphore;

		private boolean shouldShutdown;


		public ExecutorThread( Semaphore semaphore ) {

			this.semaphore = semaphore;
		}


		@Override
		public void run() {

			try {
				while ( !shouldShutdown ) {
					this.semaphore.acquire();
					System.out.println( "**Processing job:" + EXECUTION_TIMES );
					if (!shouldShutdown) {
						EXECUTION_TIMES++;
					}
				}
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}


		public void shutdown() {

			shouldShutdown = true;
			semaphore.release();
		}


		public static void resetExecutionTimes() {

			EXECUTION_TIMES = 0;
		}


		public static int getExecutionTimes() {

			return EXECUTION_TIMES;
		}

	}
}
