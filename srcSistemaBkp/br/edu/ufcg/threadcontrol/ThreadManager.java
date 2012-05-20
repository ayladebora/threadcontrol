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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class manages threads states, and also association states (Classes being
 * run by any system thread).
 * 
 */
public class ThreadManager {

	private static final boolean DEBUG = false;
	
//	private static transient final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
//	.getLogger(ThreadManager.class);
	
	private HashMap<String, ThreadsAssociations> threadsAssociationsByName;

	private HashMap<String, ThreadAssociationHistory> threadsAssociationHistoryByName;

	private HashMap<Thread, ThreadsAssociations> threadsAssociationByThread;

	private ThreadsStates threadsByState;

	/**
	 * Default constructor
	 */
	public ThreadManager() {
		this.threadsAssociationsByName = new HashMap<String, ThreadsAssociations>();
		this.threadsAssociationHistoryByName = new HashMap<String, ThreadAssociationHistory>();
		this.threadsAssociationByThread = new HashMap<Thread, ThreadsAssociations>();
		this.threadsByState = new ThreadsStates();
	}

	/**
	 * General method that prints the string parameter on the log and on the
	 * standard output.
	 * 
	 * @param str
	 *            String to be printed.
	 */
	public static void println(String str) {

		if (DEBUG) {
//			LOG.debug(str);
			System.out.println(str);
		}
	}

	/**
	 * Verifies if a thread instance is in a given state.
	 * 
	 * @param t
	 *            The thread instance.
	 * @param state
	 *            The state to be verified.
	 * @return true f the thread is in the specified state, and false,
	 *         otherwise.
	 */
	public boolean isThreadInState(Thread t, ThreadState state) {
		return this.getThreadState(t).equals(state);
	}

	/**
	 * Informs that a thread, in relation to a given object has transitioned
	 * to a new state.
	 * @param t The executing thread
	 * @param state The new state
	 * @param associatedObject The associatedObject, usually a Runnable.
	 */
	public void changeToState(Thread t, ThreadState state,
			Object associatedObject) {
		println("==>changing thread:" + t + " / " + t.hashCode() + " / " + state);
		if (state.equals(ThreadState.RUNNING)) {
			if (!isThreadBeingManaged(t)) {
				this.addNewThreadToBeManaged(t, ThreadState.STARTED, t);
			}
			if (associatedObject.equals(t)) {
				updateThreadState(t, state);
			} else {
				updateThreadStateConsideringNewAssociation(t, state,
						associatedObject);
			}
		} else if (state.equals(ThreadState.FINISHED)) {
			updateThreadStateConsideringEndOfAssociation(t, state,
					associatedObject);
		} else {
			throw new RuntimeException(
					"Unexpected state transition considering[:"
							+ associatedObject.getClass().getCanonicalName()
							+ "]");
		}

	}

	/**
	 * Informs that a thread, and all current thread associations related with
	 * this thread have transitioned to a new state.
	 * @param t The executing thread
	 * @param state The new state
	 */
	public void changeToState(Thread t, ThreadState state) {
		println("==>changing thread:"+t  + " / " + t.hashCode() + " / " + state);
		if (!isThreadBeingManaged(t)) { // case of started thread
			if (state.equals(ThreadState.STARTED)) {
				this.addNewThreadToBeManaged(t, ThreadState.STARTED, t);
			} else {
				throw new RuntimeException(
					"Unexpected state transition considering [:"
					+ t.getClass().getCanonicalName() + "]");
			}
		} else {
			updateThreadState(t, state);
		}
	}

	/**
	 * Gets the number of times a thread associated with a given class name has
	 * entered in either one of the possible states.
	 * 
	 * @param className
	 *            The name of the class associated with a Thread. It is usually
	 *            a Runnable class name.
	 * @param possibleStates
	 *            collection of possible states in which this thread has been.
	 * @return the number of times a thread associated with a given class name
	 *         has entered in either one of the possible states.
	 */
	protected int getNumberOfTimesInState(String className,
			Collection<ThreadState> possibleStates) {
		ThreadAssociationHistory threadHistory = this.threadsAssociationHistoryByName
				.get(className);
		if (threadHistory == null) {
			return 0;
		} else {
			int timesInStates = 0;
			for (ThreadState s : possibleStates) {
				timesInStates += threadHistory.getNumberOfTimesInState(s);
			}
			return timesInStates;
		}
	}

	/**
	 * Updates the associations related with a given thread that are
	 * not finished.
	 * @param t The thread object.
	 * @param newState The new state to be updated.
	 */
	private void updateThreadState(Thread t, ThreadState newState) {
		List<ThreadAssociation> associations = this.threadsAssociationByThread
				.get(t).getThreadAssociationsList();
		ThreadState previousState = this.getThreadState(t);
		if (!previousState.equals(newState)) {
			this.threadsByState.changeThreadState(t, previousState, newState);
			for (ThreadAssociation assoc : associations) {
				if (!assoc.getState().equals(newState)
						&& !assoc.getState().equals(ThreadState.FINISHED)) {
					assoc.setState(newState);
					this.updateHistoryOfAssociatedClassNames(newState, assoc
							.getName());
				}
			}
		}
	}

	/**
	 * Updates associations and the thread state considering that an
	 * association will finish. For instance, a Runnable class has finished
	 * its run method execution. If this is the first run being monitored
	 * (the association defines the thread state) and therefore all 
	 * associations will be considered to be finished.
	 */
	private void updateThreadStateConsideringEndOfAssociation(Thread t,
			ThreadState state, Object associatedObject) {
		if (state.equals(ThreadState.FINISHED)) {
			ThreadAssociation tAssociation = this.threadsAssociationByThread
					.get(t).getThreadAssociationBetween(t, associatedObject);
			if (tAssociation.definesGlobalEndOfThread()) {
				ThreadState previousState = this.getThreadState(t);
				this.threadsByState.changeThreadState(t, previousState,
						ThreadState.FINISHED);
				List<ThreadAssociation> associations = this.threadsAssociationByThread
						.get(t).getThreadAssociationsList();
				for (ThreadAssociation assoc : associations) {
					if (!assoc.getState().equals(state)) {
						assoc.setState(state);
						this.updateHistoryOfAssociatedClassNames(state, assoc
								.getName());
					}
				}
				this.threadsByState.changeThreadState(t,
						this.getThreadState(t), state);
			} else {
				tAssociation.setState(state);
				this.updateHistoryOfAssociatedClassNames(state, tAssociation
						.getName());
			}
		}

	}

	/**
	 * Updates thread and its associations states considering the following
	 * cases: 1) a run is being executed, but the thread is already managed,
	 * but is not running yet (thread is considered started); 2) the thread 
	 * is running, but inside an existing run, a run method is invoked. 
	 * @param t The current thread.
	 * @param state The new state (in this case we only expect RUNNING)
	 * @param associatedObject The Runnable whose run is being executed.
	 */
	private void updateThreadStateConsideringNewAssociation(Thread t,
			ThreadState state, Object associatedObject) {
		if (state.equals(ThreadState.RUNNING)) {
			List<ThreadAssociation> associations = this.threadsAssociationByThread
					.get(t).getThreadAssociationsList();
			for (ThreadAssociation assoc : associations) {
				if (!assoc.getState().equals(state)
						&& !(assoc.getState().equals(ThreadState.FINISHED) 
								&& !assoc.getObject().equals(assoc.getThread()))) {
					assoc.setState(state);
					this.updateHistoryOfAssociatedClassNames(state, assoc
							.getName());
				}
			}
			ThreadAssociation newAssoc;
			ThreadState previousState = this.getThreadState(t);
			if (!previousState.equals(ThreadState.RUNNING)) {
				this.threadsByState.changeThreadState(t, previousState,
						ThreadState.RUNNING);
				newAssoc = new ThreadAssociation(t, ThreadState.RUNNING,
						associatedObject, true);
			} else {
				newAssoc = new ThreadAssociation(t, ThreadState.RUNNING,
						associatedObject);
			}

			this.threadsAssociationByThread.get(t).addAssociation(newAssoc);
			this.updateHistoryOfAssociatedClassNames(state, newAssoc.getName());
			ThreadsAssociations assocsByName = this.threadsAssociationsByName
					.get(newAssoc.getName());
			if (assocsByName == null) {
				assocsByName = new ThreadsAssociations();
				assocsByName.addAssociation(newAssoc);
				this.threadsAssociationsByName.put(newAssoc.getName(),
						assocsByName);
			} else {
				assocsByName.addAssociation(newAssoc);
			}

		} else {
			throw new RuntimeException(
					"Unexpected state transition to  state ["+state+"]  considering [:"
					+ associatedObject.getClass().getCanonicalName()
					+ "]");
		}
	}

	/**
	 * Gets the thread state. 
	 * @param t The thread whose state is being requested.
	 * @return the thread current state.
	 */
	public ThreadState getThreadState(Thread t) {
		return this.threadsByState.getThreadState(t);
	}

	/**
	 * Adds a new thread to be managed and its initial state.
	 * @param t The thread.
	 * @param initialState The initial state of the thread.
	 * @param associatedObject The object being run.
	 */
	private void addNewThreadToBeManaged(Thread t, ThreadState initialState,
			Object associatedObject) {
		ThreadAssociation assoc = new ThreadAssociation(t, initialState,
				associatedObject, true);
		String assocName = assoc.getName();
		ThreadsAssociations associationsByName = this.threadsAssociationsByName
				.get(assocName);
		if (associationsByName == null) {
			associationsByName = new ThreadsAssociations();
			associationsByName.addAssociation(assoc);
			this.threadsAssociationsByName.put(assocName, associationsByName);
		} else {
			associationsByName.addAssociation(assoc);
		}
		ThreadsAssociations threadsAssociationsForThread = new ThreadsAssociations();
		threadsAssociationsForThread.addAssociation(assoc);
		this.threadsAssociationByThread.put(t, threadsAssociationsForThread);
		this.threadsByState.addNewThreadState(t, initialState);
		this.updateHistoryOfAssociatedClassNames(initialState, assocName);
	}

	/**
	 * Updates the history of previous state for each association name
	 * (class name).
	 * @param state The state to update.
	 * @param names One or more classes that had a state transition
	 * to <code>state</code>.
	 */
	private void updateHistoryOfAssociatedClassNames(ThreadState state,
			String... names) {
		for (String name : names) {
			ThreadAssociationHistory historyForName = this.threadsAssociationHistoryByName
					.get(name);
			if (historyForName == null) {
				historyForName = new ThreadAssociationHistory(name);
				historyForName.changedToState(state);
				this.threadsAssociationHistoryByName.put(name, historyForName);
			} else {
				historyForName.changedToState(state);
			}
		}
	}

	/**
	 * Verifies if a previous state transition has being
	 * registered for a given Thread.
	 * @param t The thread.
	 * @return true, if this thread is already known by this
	 * class, and false, otherwise.
	 */
	private boolean isThreadBeingManaged(Thread t) {
		return this.threadsAssociationByThread.get(t) != null;
	}

	/**
	 * Get ThreadsAssociations related with a given class name.
	 * @param className The class name.
	 * @return the ThreadsAssociation with ThreadAssociations
	 * related with <code>className</code>.
	 */
	protected ThreadsAssociations getAssociationsForName(String className) {
		return this.threadsAssociationsByName.get(className);
	}

}

/**
 * Manages the collection of associations between threads and 
 * objects related with these threads, such as instances of Runnable classes.
 *
 */
class ThreadsAssociations {

	private LinkedList<ThreadAssociation> threadAssociations;

	/**
	 * Constructor.
	 */
	public ThreadsAssociations() {
		this.threadAssociations = new LinkedList<ThreadAssociation>();
	}

	/**
	 * Adds a new association for the collection managed by this class.
	 * @param assoc The ThreadAssociation.
	 */
	public void addAssociation(ThreadAssociation assoc) {
		threadAssociations.add(assoc);
	}

	/**
	 * Gets the list of thread associations.
	 * @return the list of ThreadAssociation objects.
	 */
	public List<ThreadAssociation> getThreadAssociationsList() {
		return this.threadAssociations;
	}

	/**
	 * Gets the collection of states associated with the
	 * threads of the ThreadsAssociation.
	 * @return
	 */
	public Collection<ThreadState> getAssociatedStates() {
		Collection<ThreadState> states = new HashSet<ThreadState>();
		for (ThreadAssociation assoc : threadAssociations) {
			states.add(assoc.getState());
		}
		return states;
	}

	/**
	 * Gets the specific ThreadAssociation that relates a thread with an 
	 * object.
	 * @param t The Thread.
	 * @param obj The related object.
	 * @return the corresponding ThreadAssociation.
	 */
	public ThreadAssociation getThreadAssociationBetween(Thread t, Object obj) {
		for (ThreadAssociation assoc : threadAssociations) {
			if (assoc.getThread().equals(t) && assoc.getObject().equals(obj)) {
				return assoc;
			}
		}
		return null;
	}
}

/**
 * Represents the association between a thread and an object related with this
 * thread.
 *
 */
class ThreadAssociation {
	private Thread thread;
	private ThreadState associatedThreadState;
	private Object associatedObject;
	private boolean definesGlobalEndOfThread;

	/**
	 * Constructor.
	 * @param t The thread.
	 * @param state The state for this thread considering the associatedObject.
	 * @param associatedObject The object associated with the thread. Normally
	 * a Runnable instance.
	 */
	public ThreadAssociation(Thread t, ThreadState state,
			Object associatedObject) {
		this.thread = t;
		this.associatedThreadState = state;
		this.associatedObject = associatedObject;
	}

	/**
	 * Constructor.
	 * @param t The thread.
	 * @param state The state for this thread considering the associatedObject.
	 * @param associatedObject The object associated with the thread. Normally
	 * @param definesEnd boolean that indicates if once this association has
	 * gone to the finished state, the overall thread state has also gone to 
	 * finished.
	 */
	public ThreadAssociation(Thread t, ThreadState state,
			Object associatedObject, boolean definesEnd) {
		this.thread = t;
		this.associatedThreadState = state;
		this.associatedObject = associatedObject;
		this.definesGlobalEndOfThread = definesEnd;
	}

	/**
	 * Gets the associated object.
	 * @return the associated object.
	 */
	public Object getObject() {
		return this.associatedObject;
	}

	/**
	 * Gets the associated thread.
	 * @return the associated thread.
	 */
	public Object getThread() {
		return this.thread;
	}

	/**
	 * Tells if this association defines the finished state of
	 * the thread. 
	 * @return true if once this association has the state finished,
	 * the thread will also have this overall state.
	 */
	public boolean definesGlobalEndOfThread() {
		return this.definesGlobalEndOfThread;
	}

	/**
	 * Sets the state of this association.
	 * @param newState The new state.
	 */
	public void setState(ThreadState newState) {
		this.associatedThreadState = newState;

	}

	/**
	 * Gets a String representation of this association.
	 * @return the string representation of this association,
	 * represented by the canonical class name of the associated object.
	 */
	public String getName() {
		return this.associatedObject.getClass().getCanonicalName();
	}

	/**
	 * Gets the state of this association.
	 * @return the state of this association.
	 */
	public ThreadState getState() {
		return associatedThreadState;
	}
}

/**
 * Represents the history with the number of times a thread
 * has been in each state.
 *
 */
class ThreadAssociationHistory {
	HashMap<ThreadState, Integer> threadHistory;
	private String associationIdentifier;

	/**
	 * Constructor.
	 * @param associationIdentifier The association identifier.
	 */
	public ThreadAssociationHistory(String associationIdentifier) {
		this.associationIdentifier = associationIdentifier;
		this.threadHistory = new HashMap<ThreadState, Integer>();
		//TODO: MAYBE GET THE POSSIBLE STATES FROM ThreadsStates
		threadHistory.put(ThreadState.FINISHED, 0);
		threadHistory.put(ThreadState.RUNNING, 0);
		threadHistory.put(ThreadState.SLEEPING, 0);
		threadHistory.put(ThreadState.WAITING, 0);
		threadHistory.put(ThreadState.STARTED, 0);
		threadHistory.put(ThreadState.UNKNOWN, 0);
		threadHistory.put(ThreadState.NOTIFIED, 0);
		threadHistory.put(ThreadState.POSSIBLY_NOTIFIED, 0);
		threadHistory.put(ThreadState.AT_SPECIFIC_APP_POINT, 0);
	}

	/**
	 * Notifies this class that a state change has happened.
	 * @param state The new state.
	 */
	public void changedToState(ThreadState state) {
		int previousNumberOfTimesInState = this.threadHistory.get(state);
		this.threadHistory.put(state, previousNumberOfTimesInState + 1);
		ThreadManager.println("==> Thread "+associationIdentifier+" changed to state:"+state+" "+(previousNumberOfTimesInState + 1)+ " times");
	}

	/**
	 * Gets the number of times a thread has been associated with a given
	 * state.
	 * @param state The desired state
	 * @return the number of times a thread has been in a state.
	 */
	public int getNumberOfTimesInState(ThreadState state) {
		return this.threadHistory.get(state);
	}
	
	/**
	 * Returns the thread association identifier.
	 * @return the thread association identifier.
	 */
	public String getAssociationIdentifier() {
		return this.associationIdentifier;
	}
	
}

/**
 * Classes that manages thread states by different indexes.
 *
 */
class ThreadsStates {
	private HashMap<Thread, ThreadState> stateByThread;
	private HashMap<ThreadState, Set<Thread>> threadsByState;

	/**
	 * Constructor.
	 */
	public ThreadsStates() {
		this.stateByThread = new HashMap<Thread, ThreadState>();
		this.threadsByState = new HashMap<ThreadState, Set<Thread>>();
		this.threadsByState.put(ThreadState.FINISHED,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.RUNNING,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.SLEEPING,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.WAITING,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.STARTED,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.UNKNOWN,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.NOTIFIED,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.POSSIBLY_NOTIFIED,
				new LinkedHashSet<Thread>());
		this.threadsByState.put(ThreadState.AT_SPECIFIC_APP_POINT,
				new LinkedHashSet<Thread>());
	}

	/**
	 * Associates a thread with a given state.
	 * @param t The thread.
	 * @param initialState The initial state of the thread.
	 */
	public void addNewThreadState(Thread t, ThreadState initialState) {
		this.stateByThread.put(t, initialState);
		this.threadsByState.get(initialState).add(t);

	}

	/**
	 * Gets the current thread state.
	 * @param t The thread.
	 * @return the current thread state.
	 */
	public ThreadState getThreadState(Thread t) {
		ThreadState state = this.stateByThread.get(t);
		if (state == null) {
			return ThreadState.UNKNOWN;
		}
		return state;
	}

	/**
	 * Changes the state associated with a given thread.
	 * @param t The thread.
	 * @param fromState The previous state.
	 * @param toState The new state.
	 */
	public void changeThreadState(Thread t, ThreadState fromState,
			ThreadState toState) {
		this.stateByThread.put(t, toState);
		this.threadsByState.get(fromState).remove(t);
		this.threadsByState.get(toState).add(t);
	}
	
}
