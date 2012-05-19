/*
 * Copyright (c) 2010 Universidade Federal de Campina Grande
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Defines the expected state configuration for threads or associated Runnables.
 * 
 */
public class ThreadConfiguration {

	public static final int AT_LEAST_ONCE = -2;
	public static final int ALL_THREADS_TO_BE_IN_STATE = -1;

	private String threadClassName;
	private HashSet<ThreadState> expectedStates;
	private int timesToBeInState;
	private int numberOfThreadsToBeInState;

	/**
	 * Constructor that defines the state in which all classes with
	 * threadClassName should be.
	 * 
	 * @param threadClassName
	 *            The Thread or Runnable canonical class name.
	 * @param expectedState
	 *            The expected state for Runnables with a given name.
	 * @param timesToBeInState
	 *            The number of times the state has been reached by instances of
	 *            this class or <code>AT_LEAST_ONCE</code> if the state has
	 *            been reached at least once.
	 */
	public ThreadConfiguration(String threadClassName,
			ThreadState expectedState, int timesToBeInState) {
		this(threadClassName, new ThreadState[] { expectedState },
				timesToBeInState, ALL_THREADS_TO_BE_IN_STATE);
	}

	/**
	 * Constructor that defines the state in which some instances of classes
	 * with threadClassName should be.
	 * 
	 * @param threadClassName
	 *            The Thread or Runnable canonical class name.
	 * @param expectedState
	 *            The expected state for Runnables with a given name.
	 * @param timesToBeInState
	 *            The number of times the state has been reached by instances of
	 *            this class or <code>AT_LEAST_ONCE</code> if the state has
	 *            been reached at least once.
	 * @param numberOfThreadsToBeInState
	 *            The number of instances of threadClassName that should be in
	 *            <code>expectedState</code>.
	 */
	public ThreadConfiguration(String threadClassName,
			ThreadState expectedState, int timesToBeInState,
			int numberOfThreadsToBeInState) {
		this(threadClassName, new ThreadState[] { expectedState },
				timesToBeInState, numberOfThreadsToBeInState);
	}

	/**
	 * Constructor that defines the states in which all classes with
	 * threadClassName should be.
	 * 
	 * @param threadClassName
	 *            The Thread or Runnable canonical class name.
	 * @param expectedStates
	 *            The possible states in which Runnables with a given name
	 *            should be.
	 * @param timesToBeInState
	 *            The number of times the state has been reached by instances of
	 *            this class or <code>AT_LEAST_ONCE</code> if the state has
	 *            been reached at least once.
	 */
	public ThreadConfiguration(String threadClassName,
			ThreadState[] expectedStates, int timesToBeInState) {
		this(threadClassName, expectedStates, timesToBeInState,
				ALL_THREADS_TO_BE_IN_STATE);
	}

	/**
	 * Constructor that defines the states in which some classes with
	 * threadClassName should be.
	 * 
	 * @param threadClassName
	 *            The Thread or Runnable canonical class name.
	 * @param expectedStates
	 *            The possible states in which Runnables with a given name
	 *            should be.
	 * @param timesToBeInState
	 *            The number of times the state has been reached by instances of
	 *            this class or <code>AT_LEAST_ONCE</code> if the state has
	 *            been reached at least once.
	 * @param numberOfThreadsToBeInState
	 *            The number of instances of threadClassName that should be in
	 *            <code>expectedState</code>.
	 */
	public ThreadConfiguration(String threadClassName,
			ThreadState[] expectedStates, int timesToBeInState,
			int numberOfThreadsToBeInState) {
		this.threadClassName = threadClassName;
		this.expectedStates = new HashSet<ThreadState>();
		this.setExpectedStates(expectedStates);
		this.timesToBeInState = timesToBeInState;
		this.numberOfThreadsToBeInState = numberOfThreadsToBeInState;
	}

	/**
	 * Indicates if this configuration defines that all threads associated with
	 * a given class name should be in a certain state or if just some instances
	 * should be.
	 * 
	 * @return true if all threads associated with a given class name should be
	 *         in a certain state or set of states, and false, otherwise.
	 */
	public boolean shouldAllThreadsBeInState() {
		return this.numberOfThreadsToBeInState == ALL_THREADS_TO_BE_IN_STATE;
	}

	/**
	 * Returns the number of threads associated with a given class name that
	 * should be in one of the expected states or in a specific state.
	 * 
	 * @return the number of threads to be in the configured state(s).
	 */
	public int getNumberOfThreadsToBeInState() {
		return this.numberOfThreadsToBeInState;
	}

	/**
	 * Gets the possible state(s) for threads associated with a given class
	 * name.
	 * 
	 * @return a collection with one or more expected states.
	 */
	public Collection<ThreadState> getExpectedStates() {
		return expectedStates;
	}

	/**
	 * Configured the expected state for a class of threads.
	 * 
	 * @param expectedState
	 *            The expected state.
	 */
	public void setExpectedState(ThreadState expectedState) {
		this.setExpectedStates(expectedState);
	}

	/**
	 * Configures the expected states for threads associated with the class name
	 * of this configuration.
	 * 
	 * @param states
	 *            The expected states.
	 */
	public void setExpectedStates(ThreadState... states) {
		this.expectedStates = new HashSet<ThreadState>();
		for (ThreadState s : states) {
			this.expectedStates.add(s);
		}
	}

	/**
	 * The class name associated with threads. Usually corresponds to a Runnable
	 * class name.
	 * 
	 * @return the class name associated with some application threads.
	 */
	public String getThreadClassName() {
		return threadClassName;
	}

	/**
	 * Configures the class name associated with threads.
	 * 
	 * @param threadClassName
	 *            a Runnable class name.
	 */
	public void setThreadClassName(String threadClassName) {
		this.threadClassName = threadClassName;
	}

	/**
	 * Gets the number of times threads associated with a given class name had
	 * achieved one of the expected states.
	 * 
	 * @return the number of times this configuration has been achieved by
	 *         threads associated with a given class name.
	 */
	public int getTimesToBeInState() {
		return timesToBeInState;
	}

	/**
	 * Configures the number of times threads associated with this configuration
	 * class name should have reached the expected state(s).
	 * 
	 * @param timesToBeInState
	 *            the number of times the state has been reached by threads
	 *            associated with this configuration class name.
	 */
	public void setTimesToBeInState(int timesToBeInState) {
		this.timesToBeInState = timesToBeInState;
	}

	/**
	 * Verifies if a given configuration has been reached, considering the
	 * overall system state given by ThreadManager.
	 * 
	 * @param tManager
	 *            The ThreadManager instance.
	 * @return true if the configuration was reached, and false, otherwise.
	 */
	public boolean wasConfigurationReached(ThreadManager tManager) {
		ThreadsAssociations associations = tManager.getAssociationsForName(this
				.getThreadClassName());
		if (associations == null) {
			return false;
		} else {
			boolean isInState = false;
			if (this.shouldAllThreadsBeInState()) {
				Collection<ThreadState> states = associations
						.getAssociatedStates();
				for (ThreadState st : states) {
					isInState = this.getExpectedStates().contains(st);
					if (!isInState) {
						return false;
					}
				}
			} else {
				List<ThreadAssociation> associationsList = associations
						.getThreadAssociationsList();
				int numberOfThreadsInState = 0;
				for (ThreadAssociation assoc : associationsList) {
					if (this.getExpectedStates().contains(assoc.getState())) {
						numberOfThreadsInState++;
					}
				}
				isInState = (this.getNumberOfThreadsToBeInState() == numberOfThreadsInState);
			}
			if (isInState) {
				if (this.getTimesToBeInState() == ThreadConfiguration.AT_LEAST_ONCE) {
					return true;
				} else {
					return this.getTimesToBeInState() 
					    == tManager
					    .getNumberOfTimesInState(
					    this.getThreadClassName(),
						this.getExpectedStates());
				}
			}
			return isInState;
		}
	}

}
