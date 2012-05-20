package br.edu.ufcg.threadcontrol.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class StressTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for br.edu.ufcg.threadcontrol.tests");
		//$JUnit-BEGIN$
		for (int k=0; k<1000;k++){
			suite.addTest(AllTests.suite());
		}
		//$JUnit-END$
		return suite;
	}

}
