package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class RunnablesOfRunnablesTest extends TestCase {

	public void testRunCall() throws InterruptedException {
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnable",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		SimpleRunnable simpleRunnable = new SimpleRunnable();
		SimpleRunnableInvoker runInvoker 
		= new SimpleRunnableInvoker(simpleRunnable);
		Thread thread = new Thread(runInvoker);
		thread.start();
		tcs.waitUntilStateIsReached();
		assertFalse(runInvoker.hasExecuted());
		assertTrue(simpleRunnable.hasExecuted());
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		List<ThreadConfiguration> threadsConfigurationInvokerFinished = new LinkedList<ThreadConfiguration>();
		threadsConfigurationInvokerFinished.add(conf3);
		System.out.println("==>will prepare");
		tcs.prepare(threadsConfigurationInvokerFinished);
		System.out.println("==>will proceed");
		tcs.proceed();
		System.out.println("==>will conclude");
		runInvoker.concludeExecution();
		Thread.sleep(2000);
		tcs.waitUntilStateIsReached();
		assertFalse(runInvoker.hasExecuted());
		tcs.proceed();
		thread.join();
		assertTrue(runInvoker.hasExecuted());

	}
	
	public void testSameRunnableNameTwoWaiting() throws InterruptedException{
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnable",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		
		threadsConfiguration.add(conf1);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		SimpleRunnable simpleRunnable = new SimpleRunnable();
		SimpleRunnableInvoker runInvoker 
		= new SimpleRunnableInvoker(simpleRunnable);
		SimpleRunnableInvoker runInvoker2 
		= new SimpleRunnableInvoker(runInvoker);
		Thread thread = new Thread(runInvoker2);
		thread.start();
		System.out.println("===>will wait until");
		tcs.waitUntilStateIsReached();
		System.out.println("===>waited until");
		assertFalse(runInvoker.hasExecuted());
		assertFalse(runInvoker2.hasExecuted());
		assertTrue(simpleRunnable.hasExecuted());
		ThreadConfiguration conf4 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		List<ThreadConfiguration> threadsConfigurationInvokerFinished = new LinkedList<ThreadConfiguration>();
		threadsConfigurationInvokerFinished.add(conf4);
		tcs.prepare(threadsConfigurationInvokerFinished);
		System.out.println("===>will proceed");
		tcs.proceed();
		System.out.println("===>proceeded");
		runInvoker2.concludeExecution();
		runInvoker.concludeExecution();
		tcs.waitUntilStateIsReached();
		System.out.println("===>waited2");
		assertTrue(runInvoker.hasExecuted());
		assertTrue(runInvoker2.hasExecuted());
		tcs.proceed();
		thread.join();

	}
	public void dontTestSeveralTimes() throws Exception {
		while (true){
			System.out.println("==================");
			this.setUp();
			this.testSameRunnableName();
			this.tearDown();
			System.out.println("==================");
		}
	}
	public void testSameRunnableName() throws InterruptedException{
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE, 1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnable",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE, 1);
		
		threadsConfiguration.add(conf1);
		threadsConfiguration.add(conf2);
		threadsConfiguration.add(conf3);
		tcs.prepare(threadsConfiguration);
		SimpleRunnable simpleRunnable = new SimpleRunnable();
		SimpleRunnableInvoker runInvoker 
		= new SimpleRunnableInvoker(simpleRunnable);
		SimpleRunnableInvoker runInvoker2 
		= new SimpleRunnableInvoker(runInvoker);
		Thread thread = new Thread(runInvoker2);
		thread.start();
		runInvoker.concludeExecution();
		System.out.println("**>will wait until");
		tcs.waitUntilStateIsReached();
		System.out.println("**>waited until");
		assertFalse(runInvoker2.hasExecuted());
		assertTrue(runInvoker.hasExecuted());
		assertTrue(simpleRunnable.hasExecuted());
		ThreadConfiguration conf4 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.RunnablesOfRunnablesTest.SimpleRunnableInvoker",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		List<ThreadConfiguration> threadsConfigurationInvokerFinished = new LinkedList<ThreadConfiguration>();
		threadsConfigurationInvokerFinished.add(conf4);
		tcs.prepare(threadsConfigurationInvokerFinished);
		tcs.proceed();
		runInvoker2.concludeExecution();
		tcs.waitUntilStateIsReached();
		assertTrue(runInvoker.hasExecuted());
		assertTrue(runInvoker2.hasExecuted());
		tcs.proceed();
		thread.join();

	}

	class SimpleRunnableInvoker implements Runnable {
		Runnable internalRunnable;
		boolean hasExecuted = false;
		boolean concludeExecution = false;
		
		public SimpleRunnableInvoker(Runnable r){
			internalRunnable = r;
		}
		public void run() {
			System.out.println("will invoke runnable");
			this.internalRunnable.run();
			synchronized(this){
				try {
					if (!concludeExecution){
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			hasExecuted = true;
		}
		public void concludeExecution(){
			synchronized (this) {
				concludeExecution = true;
				this.notify();
			}
		}
		public boolean hasExecuted(){
			return this.hasExecuted;
		}
		
	}
	
	class SimpleRunnable implements Runnable {
		private boolean hasExecuted = false;
		public void run() {
			System.out.println("internal runnable executed");
			hasExecuted = true;
		}
		public boolean hasExecuted(){
			return this.hasExecuted;
		}
		
	}
}



