package br.edu.ufcg.threadcontrol.tests;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThreadWithLoopCounter;

public class ThreadControlGenericScenario2Test extends TestCase {

	public void testSimpleThreadhasExecutedxecutionWithLoopCounter()
			throws InterruptedException {
		MonitorableThreadWithLoopCounter mt = new MonitorableThreadWithLoopCounter();
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThreadWithLoopCounter",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		long time = System.currentTimeMillis();
		mt.start();
		tcs.waitUntilStateIsReached();
		time = (System.currentTimeMillis() - time);
		assertTrue(mt.hasExecuted());
		assertEquals(1, mt.getNumLoopsExecuted());
		tcs.proceed();
		mt.join();
		System.out.println("TIWA: " + time);
	}
}
