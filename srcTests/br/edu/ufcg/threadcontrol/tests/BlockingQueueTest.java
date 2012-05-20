package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class BlockingQueueTest extends TestCase {

	public void testSchedulerAndExecutorWithBlockingQueue() throws InterruptedException{
		ExecutorThread.resetExecutionTimes();
		System.out.println("will start");
		ThreadControl tcs = new ThreadControl();
		
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.BlockingQueueTest.SchedulerThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.BlockingQueueTest.ExecutorThread",
				ThreadState.FINISHED, 7);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		BlockingQueue<Integer> jobsQueue = new LinkedBlockingQueue<Integer>();
		for (int k=1; k<= 5; k++) {
			jobsQueue.put(k);
		}
		SchedulerThread sched = new SchedulerThread(jobsQueue);
		sched.start();
		sched.addJob(6);
		sched.addJob(7);
		System.out.println("==>will wait until");
		tcs.waitUntilStateIsReached();
		System.out.println("==>waited");
		assertEquals(0, jobsQueue.size());
		assertEquals(7, ExecutorThread.EXECUTION_TIMES);
		System.out.println("==>will proceed");
		tcs.proceed();
		System.out.println("==>proceeded");
		sched.shutdown();
		System.out.println("==>shutdown was called");
		sched.join();
	}
	
	// TODO Do this to all tests. (Stress test)
	public void dontTestSeveralTimes() throws Exception {
		int cont = 0;
		while (true) {
			System.out.println("====> Test:"+cont);
			this.setUp();
			this.testSchedulerAndExecutorWithBlockingQueue();
			this.tearDown();
		}
	}
	
	
	class SchedulerThread extends Thread {
		private BlockingQueue<Integer> jobsQueue;
		private boolean shouldShutdown;
		private LinkedList<ExecutorThread> list;
		
		SchedulerThread(BlockingQueue<Integer> jobsQueue){
			this.jobsQueue = jobsQueue;
			this.list = new LinkedList<ExecutorThread>();
		}
		
		@Override
		public void run(){
			while (!shouldShutdown){
				int jobNumber;
				try {
					System.out.println("will process job:");
					jobNumber = jobsQueue.take();
					if (!shouldShutdown) {
						System.out.println("creating executor for:"+jobNumber);
						ExecutorThread et = new ExecutorThread(jobNumber);
						et.start();
						this.list.add( et );
					}
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted. Maybe it has finished");
				}
			}
		}
		
		public void addJob(int jobNumber){
			try {
				this.jobsQueue.put(jobNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void shutdown() {
			shouldShutdown = true;
			try {
				this.jobsQueue.put( new Integer(0) );
				for (ExecutorThread et : list) {
					et.join();
				}
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}

		}
	}

	static class ExecutorThread extends Thread {
		static int EXECUTION_TIMES = 0;
		private int jobNumber;
		private boolean shouldShutdown = false;
		
		public ExecutorThread(int jobNumber){
			this.jobNumber = jobNumber;
		}
		
		@Override
		public void run(){
			try {
				synchronized ( this ) {
					this.wait( 1000 );
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!shouldShutdown) {
				System.out.println("**Processing job:"+jobNumber);
				EXECUTION_TIMES++;
			}
		}
		
		public void shutdown() {
			this.shouldShutdown = true;
			synchronized ( this ) {
				this.notify();
			}
		}
		
		public static void resetExecutionTimes(){
			EXECUTION_TIMES = 0;
		}

		public static int getExecutionTimes(){
			return EXECUTION_TIMES;
		}
		
	}
}
