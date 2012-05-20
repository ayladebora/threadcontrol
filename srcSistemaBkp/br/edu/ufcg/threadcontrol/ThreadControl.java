/*
 * Copyright (c) 2008 Universidade Federal de Campina Grande
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package br.edu.ufcg.threadcontrol;

import java.util.List;

/**
 * Services offered for tests in order to obtain a better control of their
 * execution and avoid false negatives. The real services for this class are
 * specified by the <code>ThreadControlAspect</code>, which should be weaved
 * with this class before its operations are invoked. <br>
 * For each state to be expected in a test, this sequence should be followed:
 * <br>
 * 1)ThreadControl.prepare(configuration)<br>
 * 2)Stimulate the system<br>
 * 3)ThreadControl.waitUntilStateIsReached() <br>
 * 4)Perform assertions <br>
 * 5) ThreadControl.proceed()
 */
public class ThreadControl {

	private SystemConfiguration systemConfiguration;

	/**
	 * Default constructor.
	 */
	public ThreadControl() {
		super();
		this.reset();
	}


	/**
	 * Indicates the expected state for the system.
	 * 
	 * @param threadsConfiguration
	 *            a list of ThreadConfiguration objects indicating the state in
	 *            which should be some application threads considering the names
	 *            of Runnable classes.
	 */
	public void prepare(List<ThreadConfiguration> threadsConfiguration) {
		throwNotUsingAspectsException();
	}

	/**
	 * Indicates the expected state for the system.
	 * 
	 * @param threadsConfiguration
	 *            a list of ThreadConfiguration objects indicating the state in
	 *            which should be some application threads considering the names
	 *            of Runnable classes.
	 */
	public void prepare(SystemConfiguration sysConfiguration) {
		throwNotUsingAspectsException();
	}
	/**
	 * Waits until the specified threads configuration is obtained and then
	 * stops all threads trying to modify the achieved system state.
	 * 
	 */
	public void waitUntilStateIsReached() {
		throwNotUsingAspectsException();
	}

	/**
	 * Throws a RuntimeException indicating that the classes that use this
	 * package should be compiled using AspectJ compiler.
	 */
	private static void throwNotUsingAspectsException() {
		throw new RuntimeException(
				"This class should be used only after compiling with aspects.");
	}

	/**
	 * Makes the system proceed normally after assertions have been performed.
	 * Any thread blocked because the expected system configuration has been
	 * reached will be unblocked when this method is invoked.
	 */
	public void proceed() {
		throwNotUsingAspectsException();
	}

	/**
	 * Resets the control of threads removing any expected system configuration
	 * and history of previous threads state transitions.
	 */
	public void reset() {
		throwNotUsingAspectsException();
	}
}
