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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import br.edu.ufcg.threadcontrol.MonitoredQueue.Operation;

/**
 * This class watches operations involving threads and provides services that
 * should know if certain system states have been achieved.
 * 
 */
public class ThreadWatcher {

	private ThreadManager threadManager;
	/**
	 * Indicates if an expected system state has been reached.
	 */
	private boolean stateReached = false;
	/**
	 * Indicates if a certain system state is being expected. A prepare has been
	 * called, and the corresponding proceed() method has not been called yet.
	 */
	private boolean isSituationBeingExpected = false;
	/**
	 * The system expected state, which can be defined in terms of names of 
	 * Runnable classes and the associated expected state.
	 */
	private SystemConfiguration systemConfiguration;
	/**
	 * Controls the matching between prepare and proceed calls.
	 */
	private int prepareCallsWithoutProceed = 0;
	/**
	 * Lock used to block threads trying to perform operations after an expected
	 * state has been reached.
	 */
	private Object controllerLock;
	/**
	 * The objects in which a wait was called.
	 */
	private Map<WaitType, Map<Object, MonitoredObject>> monitoredObjectsByWaitType;
	
	private Map<Semaphore,MonitoredSemaphore> monitoredSemaphores;
	
	private Map<BlockingQueue<?>,MonitoredQueue> monitoredQueues;
	
	/**
	 * A debug variable.
	 */
	private static final boolean DEBUG = false;

	/**
	 * The current test thread
	 */
	private Thread currentTestThread = null;
	
	/**
	 * Default constructor.
	 */
	public ThreadWatcher() {
		this.reset();
	}
	

	/**
	 * Waits until a given system configuration specified by the
	 * <code>systemConfiguration</code> attribute configured in a previous
	 * prepare call.
	 */
	public synchronized void waitUntilSystemConfiguration() {
		this.isSituationBeingExpected = true;
		while (!this.stateReached) {
			if (verifyIfConfigurationWasReached()) {
				return;
			} else {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Informs ThreadWatcher that a given system configuration is being
	 * expected.
	 * 
	 * @param sysConfiguration
	 *            the system expected state. It can be expressed, for
	 *            instance,  in terms of Runnable class names and
	 *            expected states for each thread-Runnable association.
	 */
	public synchronized void prepare(SystemConfiguration sysConfiguration) {
		this.prepareCallsWithoutProceed++;
		this.systemConfiguration = sysConfiguration;
		this.setSituationBeingExpected(true);
		this.setStateReached(false);
		this.currentTestThread = Thread.currentThread();
		if (prepareCallsWithoutProceed <=1){
			this.threadHadStateChange( currentTestThread, ThreadState.RUNNING, 
				currentTestThread);
		}
	}

	/**
	 * Verifies if the system configuration specified by
	 * <code>systemConfiguration</code> has been reached.
	 */
	private boolean verifyIfConfigurationWasReached() {
		boolean wasReached = this.systemConfiguration.wasConfigurationReached(this.threadManager);
		this.setStateReached(wasReached);
		return wasReached;
	}

	/**
	 * Configures the stateReached value.
	 * 
	 * @param stateReached
	 *            true if the expected state has been reached, and false,
	 *            otherwise.
	 */
	private void setStateReached(boolean stateReached) {
		synchronized (this) {
			this.stateReached = stateReached;
			this.notifyAll();
		}
	}

	/**
	 * Verifies if the current thread (probably trying to perform an operation
	 * being monitored by ThreadControlAspect) should be blocked because the
	 * expected state has been reached.
	 */
	//Obs: We should avoid to call this method when the thread used in the verification
    //is holding a lock necessary for the verifications performed in the assertions.
	public void verifyThread() {
		if (Thread.currentThread() != this.currentTestThread) {
			synchronized (controllerLock) {
				boolean shouldBlockCurrentThread = false;
				synchronized (this) {
					shouldBlockCurrentThread = this.isStateReached()
							&& isSituationBeingExpected();
				}
				while (shouldBlockCurrentThread) {
					try {
						this.println("Will block..." + Thread.currentThread().getClass().getCanonicalName()+ "==>"+Thread.currentThread() );
						controllerLock.wait();
						synchronized (this) {
							shouldBlockCurrentThread = this.isStateReached()
									&& isSituationBeingExpected();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	//TODO: REVIEW THIS
	public boolean verifyIfThreadShouldBeBlocked() {
		if (Thread.currentThread() != this.currentTestThread) {
			synchronized (controllerLock) {
				boolean shouldBlockCurrentThread = false;
				synchronized (this) {
					shouldBlockCurrentThread = this.isStateReached()
							&& isSituationBeingExpected();
					return shouldBlockCurrentThread;
				}
			}
		} else {
			return false;
		}

	}
	
	/**
	 * Configures if a given system state is being expected or not.
	 * 
	 * @param isSituationExpected
	 *            true, if a system state (systemConfiguation) is being
	 *            expected, and false, otherwise.
	 */
	private void setSituationBeingExpected(boolean isSituationExpected) {
		this.isSituationBeingExpected = isSituationExpected;
	}

	/**
	 * Returns true if a system state (systemConfiguration) is being expected,
	 * and false, otherwise.
	 */
	public boolean isSituationBeingExpected() {
		return this.isSituationBeingExpected;
	}

	/**
	 * Indicates if the expected state, which has been checked before has been
	 * reached.
	 * 
	 * @return true if the expected system state has been reached, and false
	 *         otherwise.
	 */
	public boolean isStateReached() {
		return stateReached;
	}

	/**
	 * When several threads are possibly notified and associated with the same
	 * monitor, if one of them starts to run again, the other possibly notified
	 * are considered to be waiting.
	 * 
	 * @param possiblyNotifiedThreads
	 *            Collection of threads in the state
	 *            ThreadState.POSSIBLY_NOTIFIED
	 * @return true if it was necessary to change the state of at least one of
	 *         the threads, and false otherwise.
	 */
	private boolean makePossiblyNotifiedThreadsWaitAgain(
			Collection<Thread> possiblyNotifiedThreads) {
		boolean changed = false;
		for (Thread t : possiblyNotifiedThreads) {
			ThreadState state = threadManager.getThreadState(t);
			if (state.equals(ThreadState.POSSIBLY_NOTIFIED)) {
				changed = true;
				this.threadManager.changeToState(t, ThreadState.WAITING);
			}
		}
		return changed;

	}

	/**
	 * Notifies that a state change has happened, verifies if after this change
	 * the expected state has been reached. A notification is sent to any thread
	 * waiting until the state is reached and this thread may be unblocked in
	 * case this has happened.
	 */
	private synchronized void notifyThreadsStateChange() {
		if (this.systemConfiguration != null) {
			this.verifyIfConfigurationWasReached();
		}
		if (this.isStateReached()) {
			this.notify();
		}
	}

	/**
	 * Adds to the list of monitored objects a given object and the thread it is
	 * waiting for.
	 * 
	 * @param o
	 *            The object where the wait was called.
	 * @param t
	 *            The thread being executed when the wait was called.
	 * @param waitType
	 * 			  Type of wait used on this object.
	 */
	private boolean addToMonitoredObjects(Object o, Thread t, WaitType waitType) {
		boolean added = false;
		Map<Object, MonitoredObject> monitoredObjects = monitoredObjectsByWaitType.get(waitType);
		MonitoredObject mo = monitoredObjects.get(o);
		if (mo == null) {
			monitoredObjects.put(o, new MonitoredObject(o, t));
			added = true;
		} else {
			added = mo.addThread(t);
		}
		return added;
	}

	/**
	 * General method that prints the string parameter on the log and on the
	 * standard output.
	 * 
	 * @param str
	 *            String to be printed.
	 */
	private void println(String str) {

		if (DEBUG) {
			System.out.println(str);
		}
	}

	/**
	 * Unblocks any threads that were blocked while trying to perform a
	 * monitored operation that is not allowed after the system expected state
	 * is reached.
	 */
	public void proceed() {
		synchronized (controllerLock) {
			prepareCallsWithoutProceed--;
			if (prepareCallsWithoutProceed == 0) {
				this.setStateReached(false);
				this.setSituationBeingExpected(false);
			}
			// if there are unmatched prepare and proceed calls, a system state
			// (situation) is still being expected (situationBeingExpected=true)
			controllerLock.notifyAll();
		}
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Resets any information regarding threads monitoring and expected system
	 * configurations.
	 */
	public synchronized void reset() {
		this.threadManager = new ThreadManager();
		controllerLock = new Object();
		this.systemConfiguration = null;
		this.stateReached = false;
		this.isSituationBeingExpected = false;
		monitoredObjectsByWaitType = new HashMap<WaitType, Map<Object, MonitoredObject>>();
		for ( WaitType waitType : WaitType.values()) {
			monitoredObjectsByWaitType.put(waitType, new HashMap<Object, MonitoredObject>());
		}
		this.prepareCallsWithoutProceed = 0;
		monitoredSemaphores = new HashMap<Semaphore,MonitoredSemaphore>() ;
		monitoredQueues = new HashMap<BlockingQueue<?>,MonitoredQueue>();
	}

	/**
	 * Indicates that a state change has happened to a Thread.
	 * 
	 * @param t
	 *            The Thread.
	 * @param toState
	 *            The new state in which the thread should be.
	 */
	public synchronized void threadHadStateChange(Thread t, ThreadState toState) {
		if (isSituationBeingExpected()) {
			this.println("@@@@@CHANGING THREAD ["+t.getClass().getCanonicalName()+"] to state: "+toState);
			this.threadManager.changeToState(t, toState);
			this.notifyThreadsStateChange();
		}
	}

	/**
	 * Indicates that a state change has happened to a Thread, regarding a given
	 * Runnable object.
	 * 
	 * @param t
	 *            The current thread.
	 * @param toState
	 *            The new state for this Thread considering the Runnable object.
	 * @param associatedObject
	 *            A Runnable object whose class name identifies its relation
	 *            with the current thread.
	 */
	public synchronized void threadHadStateChange(Thread t,
			ThreadState toState, Object associatedObject) {
		if (isSituationBeingExpected()) {
			this.threadManager.changeToState(t, toState, associatedObject);
			this.notifyThreadsStateChange();
		}
	}

	/**
	 * Indicates that Thread <code>t</code> is now in the WAITING state due to
	 * a monitor.
	 * 
	 * @param t
	 *            The waiting thread.
	 * @param monitoredObject
	 *            The object on which the wait was called.
	 * @param waitType 
	 *            Wait type.
	 */
	public synchronized void threadStartedToWaitOnObject(Thread t,
			Object monitoredObject, WaitType waitType) {
		if (isSituationBeingExpected()) {
			this.addToMonitoredObjects(monitoredObject, t, waitType);
			this.threadManager.changeToState(t, ThreadState.WAITING);
			this.notifyThreadsStateChange();
		}
	}

	/**
	 * Indicates that Thread <code>t</code> is now in the RUNNING state due
	 * because it is returning from a wait call.
	 * 
	 * @param t
	 *            The waiting thread.
	 * @param monitoredObject
	 *            The object on which the wait was called.
	 * @param timedWait
	 *            true, if a the wait call that has finished had a timeout, and
	 *            false, otherwise.
	 */
	public synchronized void threadFinishedToWaitOnObject(Thread t, Object o,
			boolean timedWait, WaitType waitType) {
		if (isSituationBeingExpected()) {
			if ((this.threadManager.isThreadInState(t, ThreadState.WAITING)
					|| this.threadManager.isThreadInState(t, ThreadState.NOTIFIED) || this.threadManager
					.isThreadInState(t, ThreadState.POSSIBLY_NOTIFIED))) {
				makeThreadRunAndAnalyzeNotifications(t, o, timedWait, waitType);
			} else if (this.threadManager.isThreadInState(t, ThreadState.RUNNING)) {
				this.println("==>Thread was already running");
			} else {
				this.println("==>STRANGE:"+this.threadManager.getThreadState(t));
				this.threadManager.changeToState(t, ThreadState.RUNNING);
				this.notifyThreadsStateChange();
			}
		}
	}

	/**
	 * Makes the thread go to the RUNNING state and analyzes if
	 * POSSIBLY_NOTIFIED threads that were waiting on the same lock should
	 * transition to the WAITING state.
	 * 
	 * @param t
	 *            The thread that will start to run.
	 * @param o
	 *            The object on which this thread was waiting.
	 * @param timedWait
	 *            a boolean indicating if the Object.wait call had a timeout
	 *            (true) or not (false).
	 */
	private void makeThreadRunAndAnalyzeNotifications(Thread t, Object o,
			boolean timedWait, WaitType waitType) {
		ThreadState threadPreviousState = threadManager.getThreadState(t);
		synchronized (monitoredObjectsByWaitType) {
			Map<Object, MonitoredObject> monitoredObjects = monitoredObjectsByWaitType.get(waitType);
			MonitoredObject mo = monitoredObjects.get(o);
			threadManager.changeToState(t, ThreadState.RUNNING);
			if (mo != null) {
				if (mo.getMonitoringThreads().size() == 1
						&& mo.getMonitoringThreads().contains(t)) {
					monitoredObjects.remove(o);
				} else {
					mo.removeMonitoringThread(t);
					if (threadPreviousState
							.equals(ThreadState.POSSIBLY_NOTIFIED)) {
						if (!timedWait) {
							makePossiblyNotifiedThreadsWaitAgain(mo
									.getMonitoringThreads());
						} else {
							// FIXME: Verify what to do when we do not know if
							// the thread has waked up due to a notify or due to
							// elapsed time. This is not so critical because
							// using
							// only notify when more than one thread can be
							// waiting is not
							// a good programming practice.
						}
					}
				}
			}
			this.notifyThreadsStateChange();
		}
	}

	/**
	 * Notifies all threads waiting for an object o, making them go to the
	 * NOTIFIED state.
	 * 
	 * @param o
	 *            The object where a notifyAll was called.
	 */
	public synchronized void notifyAllWaitingThreads(Object o) {
		if (isSituationBeingExpected()) {
			synchronized (monitoredObjectsByWaitType) {
				MonitoredObject mo = monitoredObjectsByWaitType.get(WaitType.WAIT_ON_OBJ_LOCK).get(o);
				if (mo != null) {
					Iterator<Thread> it = mo.getMonitoringThreadsIterator();
					while (it.hasNext()) {
						Thread t = it.next();
						this.threadHadStateChange(t, ThreadState.NOTIFIED);
					}
				} else {
					this.println("NOTIFY LOST: No application thread was "
							+ "waiting on object #" + o
							+ "# and notifyAll was called");
				}
			}
		}
	}

	/**
	 * Notifies a thread that is waiting for an object o
	 * 
	 * @param o
	 *            The object where a notify was called.
	 */
	public synchronized void notifyOneWaitingThread(Object o, WaitType waitType) {
		if (this.isSituationBeingExpected()) {
			synchronized (monitoredObjectsByWaitType) {
				Map<Object, MonitoredObject> monitoredObjects = monitoredObjectsByWaitType.get(waitType);
				MonitoredObject mo = monitoredObjects.get(o);
				if (mo != null) {
					Iterator<Thread> it = mo.getMonitoringThreadsIterator();
					boolean moreThanOneThreadWaitingNotification = false;
					if (mo.getMonitoringThreads().size() > 1) {
						System.err.println("notify() method was called on object="
								+ o
								+ " , but more than one thread should be notified");
						moreThanOneThreadWaitingNotification = true;
					}
					while (it.hasNext()) {
						Thread t = it.next();
						ThreadState toState;
						if (moreThanOneThreadWaitingNotification) {
							toState = ThreadState.POSSIBLY_NOTIFIED;
						} else {
							toState = ThreadState.NOTIFIED;
						}
	
						threadHadStateChange(t, toState);
					}
				} else {
					this.println("NOTIFY LOST: No application thread was waiting"
							+" on object #"	+ o + "# and notify was called");
				}
			}
		}
	}

	/**
	 * Notifies this class that a thread has possibly started to wait on 
	 * a Semaphore. This would depend on the permits available and on the
	 * number of permits requested.
	 * @param currentThread The current thread.
	 * @param sem The Semaphore.
	 * @param permitsRequested The number of permits requested.
	 */
	public synchronized void threadPossiblyStartedToWaitOnSemaphore(Thread currentThread,
			Semaphore sem, int permitsRequested) {
		if (isSituationBeingExpected()) {
			MonitoredSemaphore monSem = this.addToMonitoredObjectsWithPermits(sem, currentThread,
					permitsRequested);
			ThreadState state = monSem.getThreadStateForAvailablePermits(currentThread);
			this.threadManager.changeToState(currentThread, state);
			this.notifyThreadsStateChange();
		}

	}

	/**
	 * Starts to monitor a thread that is waiting for permits.
	 * @param sem The Semaphore.
	 * @param t The thread.
	 * @param permitsRequested The number of permits requested.
	 * @return the MonitoredSemaphore related with the thread.
	 */
	private MonitoredSemaphore addToMonitoredObjectsWithPermits(Semaphore sem, Thread t, int permitsRequested) {
		MonitoredSemaphore monSemaphore = this.monitoredSemaphores.get(sem);
		if (monSemaphore == null){
			monSemaphore = new MonitoredSemaphore(sem, t, permitsRequested);
			this.monitoredSemaphores.put(sem, monSemaphore);
		} else {
			monSemaphore.addThreadWithPermits(t, permitsRequested);
		}
		return monSemaphore;
	}

	/**
	 * Notifies this class that a thread will start to run again and updates
	 * the current number of permits available.
	 * @param t The thread.
	 * @param sem The Semaphore.
	 * @param permitsRequested The number of permits this thread has requested.
	 */
	public synchronized void threadPossiblyFinishedToWaitOnSemaphore(Thread t, Semaphore sem, int permitsRequested) {
		if (isSituationBeingExpected()) {
			if (isWaitingNotifiedOrPossiblyNotified(t)) {
				makeThreadRunAndAnalyzeNotificationsForSemaphore(t, sem, permitsRequested);
			} else if (this.threadManager.isThreadInState(t, ThreadState.RUNNING)) {
				this.println("Thread was already running");
			} else {
				this.threadManager.changeToState(t, ThreadState.RUNNING);
				this.notifyThreadsStateChange();
			}
		}
	}

	/**
	 * Moves a thread to the running state and updates the state of the
	 * other threads waiting on the same Semaphore.
	 * @param t The thread.
	 * @param sem The Semaphore.
	 * @param permitsRequested The number of permits this thread has requested.
	 */
	private void makeThreadRunAndAnalyzeNotificationsForSemaphore(Thread t,
			Semaphore sem, int permitsRequested) {
		synchronized (monitoredSemaphores) {
			MonitoredSemaphore monSem = this.monitoredSemaphores.get(sem);
			threadManager.changeToState(t, ThreadState.RUNNING);
			if (monSem != null) {
				monSem.removeAvailablePermits(permitsRequested);
				if (monSem.getNumberOfMonitoringThreads() == 1) {
					monSem.removeMonitoringThread(t);
				} else {
					monSem.removeMonitoringThread(t);
					updateStateOfThreadsAssociatedWithSemaphore(monSem);
				}
			}
			this.notifyThreadsStateChange();
		}
	}


	/**
	 * Updates the state of threads after a release call on a Semaphore.
	 * @param sem The Semaphore.
	 * @param numPermits The number of permits released.
	 */
	public synchronized void updateThreadsAfterSemaphoreRelease(Semaphore sem, int numPermits) {
		MonitoredSemaphore monSem = this.monitoredSemaphores.get(sem);
		if (monSem != null){
			monSem.addAvailablePermits(numPermits);
			updateStateOfThreadsAssociatedWithSemaphore(monSem);		
			this.notifyThreadsStateChange();
		} else {
			monSem = new MonitoredSemaphore(sem);
			this.monitoredSemaphores.put(sem, monSem);
			monSem.addAvailablePermits(numPermits);
		}
	}

	/**
	 * Updates the states of threads waiting on a semaphore when
	 * the drainPermits method is called.
	 * @param sem The Semaphore.
	 */
	public void updateThreadsAfterSemaphoreDrainPermits( Semaphore sem) {
		MonitoredSemaphore monSem = this.monitoredSemaphores.get(sem);
		if (monSem != null){
			monSem.resetAvailablePermits();
			updateStateOfThreadsAssociatedWithSemaphore(monSem);		
			this.notifyThreadsStateChange();
		} 
	}

	/**
	 * Updates the state of threads associated with a Semaphore.
	 * @param monSem The semaphore being monitored.
	 */
	private void updateStateOfThreadsAssociatedWithSemaphore(
			MonitoredSemaphore monSem) {
		Set <Thread> threads = monSem.getMonitoringThreads();
		for (Thread thread: threads){
			ThreadState state = this.threadManager.getThreadState(thread);
			ThreadState newState = monSem.getThreadStateForAvailablePermits(thread);
			if (isWaitingNotifiedOrPossiblyNotified(thread) && !state.equals(newState)){
				threadManager.changeToState(thread, newState);
			}
		}
	}
	
	/**
	 * Verifies if the state of a given thread is WAITING, NOTIFIED or
	 * POSSIBLY_NOTIFIED.
	 * @param t The thread.
	 * @return true if the thread is in one of this sates: WAITING, 
	 * NOTIFIED or POSSIBLY_NOTIFIED; and false, otherwise.
	 * 
	 */
	private boolean isWaitingNotifiedOrPossiblyNotified(Thread t){
		return (this.threadManager.isThreadInState(t, ThreadState.WAITING)
				|| this.threadManager.isThreadInState(t, ThreadState.NOTIFIED) 
				|| this.threadManager
				.isThreadInState(t, ThreadState.POSSIBLY_NOTIFIED));
	}
	
	/**
	 * Adds a queue to the collection of queues being monitored.
	 * @param queue The queue to be monitored.
	 * @return The MonitoredQueue object associated with a queue.
	 */
	private MonitoredQueue addToMonitoredObjectsWithQueue(BlockingQueue queue) {
		MonitoredQueue monQueue = this.monitoredQueues.get(queue);
		if (monQueue == null){
			monQueue = new MonitoredQueue(queue);
			this.monitoredQueues.put(queue, monQueue);
		}
		return monQueue;
	}

	/**
	 * Thread has possibly started to wait on a BlockingQueue.
	 * @param currentThread The current thread.
	 * @param queue The queue.
	 * @param oper The operation being called on the BlockingQueue.
	 */
	public synchronized void threadPossiblyStartedToWaitOnBlockingQueue(
			Thread currentThread, BlockingQueue<?> queue, Operation oper) {
		if (isSituationBeingExpected()) {
			synchronized (monitoredQueues) {
				MonitoredQueue monQueue = this.addToMonitoredObjectsWithQueue(queue);
				monQueue.addThreadBlockingQueue(currentThread, oper);
				ThreadState state = monQueue.getThreadStateForQueueSize(currentThread);
				this.threadManager.changeToState(currentThread, state);
				this.notifyThreadsStateChange();
			}
		}
	}

	/**
	 * Informs that a thread is not waiting on a BlockingQueue anymore.
	 * @param t The thread.
	 * @param queue The BlockingQueue.
	 * @param oper The operation called on the BlockingQueue.
	 */
	public synchronized void threadPossiblyFinishedToWaitOnBlockingQueue(Thread t,
			BlockingQueue<?> queue, Operation oper) {
		if (isSituationBeingExpected()) {
			if (isWaitingNotifiedOrPossiblyNotified(t)) {
				makeThreadRunAndAnalyzeNotificationsForQueue(t, queue, oper);
			} else if (this.threadManager.isThreadInState(t, ThreadState.RUNNING)) {
				this.println("Thread was already running");
			} else {
				this.threadManager.changeToState(t, ThreadState.RUNNING);
				this.notifyThreadsStateChange();
			}
		}
	}

	/**
	 * Updates the state of a thread to RUNNING and updates the state of other
	 * threads waiting on the same queue.
	 * @param t The thread.
	 * @param queue The BlockingQueue instance.
	 * @param operation The operation invoked on the BlockingQueue.
	 */
	private void makeThreadRunAndAnalyzeNotificationsForQueue(Thread t,
			BlockingQueue<?> queue, Operation operation) {
		synchronized (monitoredQueues) {
			MonitoredQueue monQueue = this.monitoredQueues.get(queue);
			threadManager.changeToState(t, ThreadState.RUNNING);
			if (monQueue != null) {
				monQueue.removeThreadBlockingQueue(t, operation);
				updateStateOfThreadsAssociatedWithQueue(monQueue, operation);					
				Operation symetricOper;
				if (Operation.put.equals(operation)) {
					symetricOper = Operation.take;
				} else { 
					symetricOper = Operation.put;
				}
				updateStateOfThreadsAssociatedWithQueue(monQueue, symetricOper );
			}
			this.notifyThreadsStateChange();
		}
	}

	/**
	 * Updates the states of threads associated with a queue.
	 * @param monQueue The MonitoredQueue object that associates a thread
	 * with a queue.
	 * @param oper The operation called on a BlockingQueue.
	 */
	private void updateStateOfThreadsAssociatedWithQueue(
			MonitoredQueue monQueue, Operation oper) {
		Set <Thread> threads = monQueue.getMonitoringThreads(oper);
		for (Thread thread : threads){
			ThreadState state = this.threadManager.getThreadState(thread);
			ThreadState newState = monQueue.getThreadStateForQueueSize(thread);
			if (isWaitingNotifiedOrPossiblyNotified(thread) && !state.equals(newState)){
				threadManager.changeToState(thread, newState);
			}
		}
	}	
}
