package br.edu.ufcg.threadcontrol.tests;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class BlockingQueue2Test extends TestCase implements UncaughtExceptionHandler {

	private List<Throwable> failures;
	
	private static int exec;

	public static synchronized void addExec() {
		exec++;
	}
	
	@Override
	public void setUp() {
		this.failures = new LinkedList<Throwable>();
		this.exec = 0;
	}
	
	@Override
	public void tearDown() throws Exception {
		//Thread.sleep( 10000 );
	}
	
	public void testSchedulerAndExecutorWithBlockingQueue() throws InterruptedException{
		ThreadControl tcs = new ThreadControl();

		int numberOfThreads = 200;

		List<Thread> threads = new LinkedList<Thread>();
		
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				Producer.class.getCanonicalName(),
				ThreadState.FINISHED, numberOfThreads);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				Consumer.class.getCanonicalName(),
				ThreadState.FINISHED, numberOfThreads);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(5);
		for (int k=0; k < numberOfThreads; k++) {
			Producer p = new Producer(10, queue);
			p.setUncaughtExceptionHandler( this );
			p.start();
			Consumer c = new Consumer(10, queue);
			c.start();
			c.setUncaughtExceptionHandler( this );
			threads.add( c );
			threads.add( p );
		}
		tcs.waitUntilStateIsReached();
		assertEquals(0, queue.size());
		assertEquals(2 * numberOfThreads * 10, exec);
		assertEquals(0, failures.size());
		tcs.proceed();
		
		for ( Thread thread : threads ) {
			thread.join();
		}
		tcs.reset();
	}
	
	public void donttestSeveralTimes() throws Exception {
		int cont = 0;
		while (true) {
			System.out.println("====> Test:" + ++cont);
			this.setUp();
			this.testSchedulerAndExecutorWithBlockingQueue();
			this.tearDown();
		}
	}
	
	public static class Producer extends Thread {
		
		private int size;
		private BlockingQueue<Object> queue;
		
		public Producer(int size, BlockingQueue<Object> queue) {
			this.size = size;
			this.queue = queue;
		}
		
		@Override
		public void run() {
			for ( int i = 0; i < size; i++ ) {
				try {
					this.queue.put( new Object() );
					addExec();
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class Consumer extends Thread {
		
		private int size;
		private BlockingQueue<Object> queue;

		public Consumer(int size, BlockingQueue<Object> queue) {
			this.size = size;
			this.queue = queue;
		}
		
		@Override
		public void run() {
			for ( int i = 0; i < size; i++ ) {
				try {
					this.queue.take();
					addExec();
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
	}

	public void uncaughtException( Thread arg0, Throwable arg1 ) {
		System.out.println("A failure!!! " + arg1);
		this.failures.add(arg1);
	}
	
}
