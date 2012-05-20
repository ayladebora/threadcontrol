package br.edu.ufcg.threadcontrol.tests;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread;

public class ThreadControlScenario1Test extends TestCase {
	private static final int NUMBER_OF_TESTS = 1;
	private static long[] TEST_TIMES;
	private long timestamp;
	private static int TEST_COUNTER = 0;

	public ThreadControlScenario1Test() {
		super();
		TEST_TIMES = new long[NUMBER_OF_TESTS];
	}

	public void setUp() throws Exception {
		super.setUp();
		this.timestamp = System.currentTimeMillis();
	}

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
		TEST_COUNTER = 0;
		MonitorableThread mt = new MonitorableThread();
		assertFalse(mt.hasExecuted());

		ThreadControl tcs = new ThreadControl();
		List<ThreadConfiguration> threadsConfiguration = new LinkedList<ThreadConfiguration>();
		ThreadConfiguration conf1 = new ThreadConfiguration(
				"br.edu.ufcg.threadcontrol.tests.auxiliarythreads.MonitorableThread",
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		threadsConfiguration.add(conf1);
		tcs.prepare(threadsConfiguration);
		long time = System.currentTimeMillis();
		mt.start();
		tcs.waitUntilStateIsReached();
		System.out.println(System.currentTimeMillis() - time);
		tcs.proceed();
		mt.join();
	}
	
}