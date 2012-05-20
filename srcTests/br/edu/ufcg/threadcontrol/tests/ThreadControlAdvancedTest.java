package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import br.edu.ufcg.threadcontrol.tests.auxiliaryadvancedthreads.SimpleLockExampleThread;

public class ThreadControlAdvancedTest extends TestCase {
	//TODO: Include this test when we have support for the 
	//operations involving the Lock class.
	public void testWaitingForTwoTimes() throws InterruptedException{
		Lock lock = new ReentrantLock();
		SimpleLockExampleThread nud2 = new SimpleLockExampleThread(lock);	
		
		ThreadControl tcs = new ThreadControl();
		List <ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration("br.edu.ufcg.threadcontrol.tests.auxiliaryadvancedthreads.SimpleLockExampleThread", ThreadState.WAITING,ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		nud2.start();		

		tcs.waitUntilStateIsReached();
		assertFalse(nud2.hasExecuted());
		tcs.proceed();
		
		ThreadConfiguration conf2 = new ThreadConfiguration("br.edu.ufcg.threadcontrol.tests.auxiliaryadvancedthreads.SimpleLockExampleThread", ThreadState.FINISHED,ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		
		nud2.shutdown();
		nud2.join();
		assertTrue(nud2.hasExecuted());		
	}
}
