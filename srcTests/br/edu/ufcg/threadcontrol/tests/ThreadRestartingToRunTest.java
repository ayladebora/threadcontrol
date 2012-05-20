package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import br.edu.ufcg.threadcontrol.tests.external.RunnableExecutorThread;

public class ThreadRestartingToRunTest extends TestCase {
	
	public void testThreadRestartingToRun() {
		boolean [] sharedArray = new boolean [2];
		SimpleRunnable run1 = new SimpleRunnable(0, sharedArray);
		SimpleRunnable run2 = new SimpleRunnable(1, sharedArray);
		Runnable [] runnables = new Runnable [] {run1, run2};		
		RunnableExecutorThread runExec = new RunnableExecutorThread(runnables);
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.external.RunnableExecutorThread",
				ThreadState.RUNNING, 2);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		runExec.start();
		tcs.waitUntilStateIsReached();
		assertTrue(sharedArray[0]);
		assertFalse(sharedArray[1]);
		
		threadsConfiguration.remove(0);
		List<ThreadConfiguration> threadsConfigurationFinished = new LinkedList<ThreadConfiguration>();
		threadsConfigurationFinished.add(new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.external.RunnableExecutorThread",
				ThreadState.FINISHED, 2));
		tcs.prepare(threadsConfigurationFinished);
		tcs.proceed();
		tcs.waitUntilStateIsReached();
		assertTrue(sharedArray[0]);
		assertTrue(sharedArray[1]);
		tcs.proceed();
	}

	class SimpleRunnable implements Runnable {
		private int positionToSet;
		private boolean[] sharedArray;
		
		public SimpleRunnable(int positionToSet, boolean [] sharedArray){
			this.positionToSet = positionToSet;
			this.sharedArray = sharedArray;
		}
		public void run() {
			System.out.println("will set position");
			this.sharedArray [positionToSet] = true;
			System.out.println("has set position");
		}
	}
}

