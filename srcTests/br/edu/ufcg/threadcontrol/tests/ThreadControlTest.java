package br.edu.ufcg.threadcontrol.tests;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableLoopThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath2;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread2;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread3;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread4;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterClass;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterClass2;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterNotTimed;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterNotTimed2;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SleeperThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.StopperThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.TemporaryWaiterThread;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.WaiterLoopThread;

public class ThreadControlTest extends TestCase {
	private static final int NUMBER_OF_TESTS = 15;
	private static long[] TEST_TIMES;
	private long timestamp;
	private static int TEST_COUNTER = 0;

	public ThreadControlTest() {
		super();
		TEST_TIMES = new long[NUMBER_OF_TESTS];
	}
	
	// TODO Implement tests with two prepares and a reset call between them.

	// TODO: Implement test case in which the main application has a
	// sleep call, that will begin to be monitored, but there is no
	// association before.
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (TEST_COUNTER == NUMBER_OF_TESTS){
			TEST_COUNTER = 0;
		}
		this.timestamp = System.currentTimeMillis();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		TEST_TIMES[TEST_COUNTER] = (System.currentTimeMillis() - timestamp);
		System.out.println("Test number#" + (TEST_COUNTER + 1) + "#"
				+ TEST_TIMES[TEST_COUNTER]);
		TEST_COUNTER++;
	}

	public void printTimes() {
		System.out.println("+++++++++++++++++FINALIZING TEST");
		try {
			FileWriter fw = new FileWriter("testTimes.txt", true);
			for (int k = 0; k < TEST_COUNTER; k++) {
				fw.write("Test number#" + (k + 1) + "#" + TEST_TIMES[k] + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void testSimpleThreadhasExecutedxecution()
			throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		assertFalse(mt.hasExecuted());
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		mt.start();
		tcs.waitUntilStateIsReached();
		assertTrue(mt.hasExecuted());
		tcs.proceed();
	}
	// TODO: Implement test case in which the main application has a
	// sleep call, that will begin to be monitored, but there is no
	// association before.
	public void testSimpleThreadhasStartedRunning() throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		ThreadLateStarter tls = new ThreadLateStarter(mt);
		tls.start();
		tcs.waitUntilStateIsReached();
		Thread.sleep(2000);
		assertFalse(mt.hasExecuted());
		tcs.proceed();
		mt.join();
		assertTrue(mt.hasExecuted());
	}

	public void testSimpleThreadhasStartedRunningWithoutLaterStart()
			throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		Thread.sleep(1000);
		mt.start();
		Thread.sleep(2000);
		tcs.waitUntilStateIsReached();
		assertFalse(mt.hasExecuted());
		tcs.proceed();
		mt.join();
		assertTrue(mt.hasExecuted());
	}

	public void testThreadTryingToRunAndBlocked() throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		MonitorableLoopThread mlt = new MonitorableLoopThread();
		long timeToSleep = 1000;
		mlt.setTimeToSleep(timeToSleep);
		assertFalse(mt.hasExecuted());
		assertEquals(0, mlt.getExecutionTimes());
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		mlt.start();
		mt.start();
		tcs.waitUntilStateIsReached();
		int executionTimes = mlt.getExecutionTimes();
		Thread.sleep(3 * timeToSleep);
		assertFalse(mt.hasExecuted());
		assertEquals(executionTimes, mlt.getExecutionTimes());
		tcs.proceed();
		mt.join();
		assertTrue(mt.hasExecuted());
		boolean hasLoopThreadExecuted = false;
		int maxTries = 100;
		for (int k = 0; k < maxTries && !hasLoopThreadExecuted; k++) {
			hasLoopThreadExecuted = (mlt.getExecutionTimes() > executionTimes);
			Thread.sleep(timeToSleep / 2);
		}
		mlt.shutdown();
		mlt.join();
	}

	// TODO: verify this case. See notes in my paper notebook...
	public void testOneWaitingTwoFinished() throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		WaiterLoopThread wlt = new WaiterLoopThread();
		NotifierThread nt = new NotifierThread(wlt);
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.WaiterLoopThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);

		tcs.prepare(threadsConfiguration);
		mt.start();
		wlt.start();
		Thread.sleep(2000);
		assertTrue(wlt.getExecutionTimes() == 0);
		nt.start();
		tcs.waitUntilStateIsReached();
		assertTrue(wlt.getExecutionTimes() > 0);
		assertTrue(nt.hasExecuted());
		assertTrue(mt.hasExecuted());
		tcs.proceed();
		wlt.shutdown();
		wlt.join();
		assertTrue(wlt.hasExecuted());
	}

	public void testFourFinished() throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		WaiterLoopThread wlt = new WaiterLoopThread();
		NotifierThread nt = new NotifierThread(wlt);
		StopperThread st = new StopperThread(wlt);
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.WaiterLoopThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);
		ThreadConfiguration conf4 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.StopperThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf4);

		tcs.prepare(threadsConfiguration);
		mt.start();
		wlt.start();
		Thread.sleep(2000);
		assertTrue(wlt.getExecutionTimes() == 0);
		nt.start();
		st.start();
		tcs.waitUntilStateIsReached();
		assertTrue(wlt.getExecutionTimes() > 0);
		assertTrue(nt.hasExecuted());
		assertTrue(mt.hasExecuted());

		tcs.proceed();
		wlt.shutdown();
		wlt.join();
		assertTrue(wlt.hasExecuted());
		st.join();
	}

	public void testOneSleepingThreeFinished() throws InterruptedException {
		SleeperThread slept = new SleeperThread();
		WaiterLoopThread wlt = new WaiterLoopThread();
		NotifierThread nt = new NotifierThread(wlt);
		StopperThread st = new StopperThread(wlt);
		assertFalse(slept.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SleeperThread",
				ThreadState.SLEEPING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.WaiterLoopThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);
		ThreadConfiguration conf4 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.StopperThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf4);

		tcs.prepare(threadsConfiguration);
		slept.start();
		wlt.start();
		assertTrue(wlt.getExecutionTimes() == 0);
		nt.start();
		st.start();
		tcs.waitUntilStateIsReached();
		assertTrue(wlt.getExecutionTimes() > 0);
		assertTrue(nt.hasExecuted());
		int sleepTimes = slept.getExecutionTimes();
		assertTrue(sleepTimes > 0);
		Thread.sleep(1500);
		assertTrue(sleepTimes == slept.getExecutionTimes());
		// only to garantee it won't do anything

		tcs.proceed();
		wlt.shutdown();
		slept.shutdown();
		slept.join();
		wlt.join();
		st.join();
		assertTrue(wlt.hasExecuted());

	}

	public void testOneTemporarilyWaitingTwoFinished()
			throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		TemporaryWaiterThread twt = new TemporaryWaiterThread();
		NotifierThread nt = new NotifierThread(twt);
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.TemporaryWaiterThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);

		tcs.prepare(threadsConfiguration);
		mt.start();
		twt.start();
		assertTrue(twt.getExecutionTimes() == 0);
		nt.start();
		tcs.waitUntilStateIsReached();
		int executionTimes = twt.getExecutionTimes();
		assertTrue(executionTimes == 1);
		assertTrue(nt.hasExecuted());
		assertTrue(mt.hasExecuted());
		tcs.proceed();
		twt.shutdown();
		twt.join();
		assertTrue(twt.getExecutionTimes() > 0);
	}

	public void testOneTemporarilyWaitingOneFinishedOneToBeInterrupted()
			throws InterruptedException {
		MonitorableThread mt = new MonitorableThread();
		WaiterLoopThread wlt = new WaiterLoopThread();
		NotifierThread nt = new NotifierThread(wlt);
		assertFalse(mt.hasExecuted());
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.WaiterLoopThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		tcs.prepare(threadsConfiguration);
		mt.start();
		mt.join();
		assertEquals(0, wlt.getExecutionTimes());
		wlt.start();
		nt.start();		
		Thread.sleep(2000);
		tcs.waitUntilStateIsReached();
		int executionTimes = wlt.getExecutionTimes();
		assertEquals(0, wlt.getNumberOfWaitExecutions());
		assertTrue(mt.hasExecuted());
		tcs.proceed();
		wlt.shutdown();
		wlt.join();
		assertTrue(wlt.getExecutionTimes() > 0);
		System.out.println("ExecutionTimes:" + executionTimes);
	}

	public void testTwoWaitingAndNotify() throws InterruptedException {
		// TODO: Test to verify how the system must handle when just
		// a notify is performed and more than one thread was waiting.
		//
		System.out.println("+++++TEST TWO WAITING AND NOTIFY");
		Object lock = new Object();
		SimpleNotifiableThread snt1 = new SimpleNotifiableThread(lock);

		NotifierUntilDeath nt = new NotifierUntilDeath(lock);
		SimpleNotifiableThread2 snt2 = new SimpleNotifiableThread2(lock, nt);

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread2",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);
		tcs.prepare(threadsConfiguration);
		snt1.start();
		snt2.start();
		nt.start();
		tcs.waitUntilStateIsReached();
		assertFalse(snt1.hasExecuted());
		assertTrue(snt2.hasExecuted());
		assertTrue(nt.hasExecuted());
		tcs.proceed();
		snt1.shutdown();
		snt1.join();

	}

	public void testOneWaitingOneTimedWaitingAndOneNotify()
			throws InterruptedException {
		// TODO: Test to verify how the system must handle when just
		// a notify is performed and more than one thread was waiting.
		//

		Object lock = new Object();
		NotifierUntilDeath2 nud2 = new NotifierUntilDeath2(lock);
		SimpleNotifiableThread3 snt3 = new SimpleNotifiableThread3(lock, nud2);

		SimpleNotifiableThread4 snt4 = new SimpleNotifiableThread4(lock, nud2);

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread3",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleNotifiableThread4",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);
		// ####
		// ThreadConfiguration conf3 = new
		// ThreadConfiguration("br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath2",
		// ThreadState.FINISHED,ThreadConfiguration.AT_LEAST_ONCE);
		// threadsConfiguration.add(conf3);
		// ####
		tcs.prepare(threadsConfiguration);
		snt4.start();
		snt3.start();

		tcs.waitUntilStateIsReached();
		tcs.proceed();

		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath2",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);
		tcs.prepare(threadsConfiguration);
		nud2.start();

		tcs.waitUntilStateIsReached();
		assertFalse(snt3.hasExecuted());
		assertFalse(snt4.hasExecuted());
		assertTrue(nud2.hasExecuted());
		System.out.println("++++>" + snt3.getState().toString());
		System.out.println("++++>" + snt4.getState().toString());

		tcs.proceed();
		snt3.shutdown();
		snt4.shutdown();
		snt3.join();
		snt4.join();

	}

	// FIXME: Verify this case with two threads of the same class
	public void testTwoWaitingSameClassAndOneNotify()
			throws InterruptedException {
		// TODO: Test to verify how the system must handle when just
		// a notify is performed and more than one thread was waiting.
		//	

		Object lock = new Object();
		NotifierUntilDeath2 nud2 = new NotifierUntilDeath2(lock);
		SimpleWaiterNotTimed swc = new SimpleWaiterNotTimed(lock, nud2);
		SimpleWaiterNotTimed swc2 = new SimpleWaiterNotTimed(lock, nud2);

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterNotTimed",
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		swc.start();
		swc2.start();

		tcs.waitUntilStateIsReached();
		assertFalse(swc.hasExecuted());
		assertFalse(swc.hasExecuted());
		tcs.proceed();

		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath2",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);
		tcs.prepare(threadsConfiguration);
		nud2.start();

		tcs.waitUntilStateIsReached();
		assertFalse(swc.hasExecuted());
		assertFalse(swc.hasExecuted());
		assertTrue(nud2.hasExecuted());
		System.out.println("++++>" + swc.getState().toString());
		System.out.println("++++>" + swc2.getState().toString());

		tcs.proceed();
		swc.shutdown();
		swc2.shutdown();
		swc.join();
		swc2.join();
		assertTrue(swc.hasExecuted());
		assertTrue(swc.hasExecuted());
	}

	// This is the case in which both are waiting but in fact one is marked as
	// possibly notified due to the timedwait
	// In this case we use two possible states.
	public void testTwoTimedWaitingAndOneNotifyAndOnePossiblyNotified()
			throws InterruptedException {
		// TODO: Test to verify how the system must handle when just
		// a notify is performed and more than one thread was waiting.
		//	

		Object lock = new Object();
		NotifierUntilDeath2 nud2 = new NotifierUntilDeath2(lock);
		SimpleWaiterClass swc = new SimpleWaiterClass(lock, nud2);
		SimpleWaiterClass2 swc2 = new SimpleWaiterClass2(lock, nud2);

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadState[] possibleStates = new ThreadState[] {
				ThreadState.POSSIBLY_NOTIFIED, ThreadState.WAITING };
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterClass",
				possibleStates, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterClass2",
				possibleStates, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf2);

		tcs.prepare(threadsConfiguration);
		swc.start();
		swc2.start();

		tcs.waitUntilStateIsReached();
		tcs.proceed();

		ThreadConfiguration conf3 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath2",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf3);
		tcs.prepare(threadsConfiguration);

		nud2.start();

		tcs.waitUntilStateIsReached();
		assertFalse(swc.hasExecuted());
		assertFalse(swc.hasExecuted());
		assertTrue(nud2.hasExecuted());

		tcs.proceed();
		swc.shutdown();
		swc2.shutdown();
		swc.join();
		swc2.join();
		assertTrue(swc.hasExecuted());
		assertTrue(swc.hasExecuted());
	}

	public void utestWaitingForTwoTimesSeveralTimes() throws Exception {
		int cont = 0;
		while (true){
			cont++;
			System.out.println("======================");
			System.out.println("TESTING AGAIN:"+cont);
			this.setUp();
			this.testWaitingForTwoTimes();
			this.tearDown();
			System.out.println("======================");
			Thread.sleep(1000);
		}
	}
	public void testWaitingForTwoTimes() throws InterruptedException {
		Object lock = new Object();
		NotifierUntilDeath2 nud2 = new NotifierUntilDeath2(lock);
		SimpleWaiterNotTimed2 swc2 = new SimpleWaiterNotTimed2(lock, nud2);
		System.out.println("----will start test");
		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterNotTimed2",
				ThreadState.WAITING, 1);
		threadsConfiguration.add(conf1);
		System.out.println("----will prepare");
		tcs.prepare(threadsConfiguration);
		System.out.println("----will start");
		swc2.start();
		System.out.println("----will wait until");
		tcs.waitUntilStateIsReached();
		System.out.println("waited ...");
		assertFalse(swc2.hasExecuted());
		assertFalse(nud2.hasExecuted());
		tcs.proceed();
		System.out.println("proceeded");
		List<ThreadConfiguration> threadsConfiguration2 = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf2 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterNotTimed2",
				ThreadState.WAITING, 3);
		threadsConfiguration2.add(conf2);
		System.out.println("will prepare for new configuration...");
		tcs.prepare(threadsConfiguration2);
		System.out.println("Prepared");
		nud2.start();
		System.out.println("started nud2");
		tcs.waitUntilStateIsReached();
		System.out.println("waited until");
		assertFalse(swc2.hasExecuted());
		assertFalse(nud2.hasExecuted());
		assertEquals(3, swc2.getExecutionTimes());
		tcs.proceed();
		swc2.shutdown();
		nud2.shutdown();
		swc2.join();
		nud2.join();
		assertTrue(swc2.hasExecuted());
		assertTrue(nud2.hasExecuted());
	}

	// FIXME: This is the case in which both are waiting but in fact one is
	// marked as possibly notified due to the timedwait
	/*
	 * public void testTwoWaitingTwoTimedWaitingAndOneNotify() throws
	 * InterruptedException{ //TODO: Test to verify how the system must handle
	 * when just //a notify is performed and more than one thread was waiting. //
	 * 
	 * Object lock = new Object(); NotifierUntilDeath2 nud2 = new
	 * NotifierUntilDeath2(lock); SimpleWaiterClass swc = new
	 * SimpleWaiterClass(lock, nud2); SimpleWaiterClass2 swc2 = new
	 * SimpleWaiterClass2(lock, nud2);
	 * 
	 * ThreadControl tcs = new ThreadControl(); List <ThreadConfiguration>
	 * threadsConfiguration = new LinkedList<ThreadConfiguration>();
	 * ThreadConfiguration conf1 = new
	 * ThreadConfiguration("br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterClass",
	 * ThreadState.WAITING,ThreadConfiguration.AT_LEAST_ONCE); threadsConfiguration.add(conf1);
	 * ThreadConfiguration conf2 = new
	 * ThreadConfiguration("br.edu.ufcg.threadcontrol.tests.auxiliarythreads.SimpleWaiterClass2",
	 * ThreadState.WAITING,ThreadConfiguration.AT_LEAST_ONCE); threadsConfiguration.add(conf2);
	 * 
	 * tcs.prepare(threadsConfiguration); swc.start(); swc2.start();
	 * 
	 * tcs.waitUntilStateIsReached();
	 * System.out.println("++++>"+swc.getState().toString());
	 * System.out.println("++++>"+swc2.getState().toString()); tcs.proceed();
	 * 
	 * ThreadConfiguration conf3 = new
	 * ThreadConfiguration("br.edu.ufcg.threadcontrol.tests.auxiliarythreads.NotifierUntilDeath2",
	 * ThreadState.FINISHED,0); threadsConfiguration.add(conf3);
	 * tcs.prepare(threadsConfiguration);
	 * 
	 * nud2.start();
	 * 
	 * 
	 * tcs.waitUntilStateIsReached(); assertFalse(swc.hasExecuted());
	 * assertFalse(swc.hasExecuted()); assertTrue(nud2.hasExecuted());
	 * System.out.println("++++>"+swc.getState().toString());
	 * System.out.println("++++>"+swc2.getState().toString());
	 * 
	 * tcs.proceed(); swc.shutdown(); swc2.shutdown(); swc.join(); swc2.join();
	 * assertTrue(swc.hasExecuted()); assertTrue(swc.hasExecuted()); }
	 */

	public void testJustForWriteTimes() {
		this.printTimes();
	}
}

class ThreadLateStarter extends Thread {
	Thread internalThread;

	ThreadLateStarter(Thread t) {
		super("ThreadLateStarter");
		internalThread = t;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		internalThread.start();
	}
}