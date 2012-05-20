package br.edu.ufcg.threadcontrol.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for br.edu.ufcg.threadcontrol.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ThreadControlGenericScenario2Test.class);
		suite.addTestSuite(ThreadControlTest.class);
		suite.addTestSuite(ThreadControlScenario3Test.class);
		suite.addTestSuite(ThreadControlScenario1Test.class);
		suite.addTestSuite(SchedulerAndExecutorTest.class);
		suite.addTestSuite(RunnablesOfRunnablesTest.class);
		suite.addTestSuite(ThreadRestartingToRunTest.class);
		suite.addTestSuite(BlockingQueueTest.class);
		suite.addTestSuite(SemaphoreForTwoThreadsTest.class);
		suite.addTestSuite(SemaphoreTwoAcquiresAndOnePermitTest.class);
		suite.addTestSuite(SemaphoreTest.class);
		//$JUnit-END$
		return suite;
	}

}
