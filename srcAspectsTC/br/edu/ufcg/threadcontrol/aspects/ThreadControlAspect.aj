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
package br.edu.ufcg.threadcontrol.aspects;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.aspectj.lang.JoinPoint;

import br.edu.ufcg.threadcontrol.ListOfThreadConfigurations;
import br.edu.ufcg.threadcontrol.MonitoredQueue.Operation;
import br.edu.ufcg.threadcontrol.SystemConfiguration;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;
import br.edu.ufcg.threadcontrol.ThreadWatcher;
import br.edu.ufcg.threadcontrol.WaitType;

/**
 * This aspect handles thread state changes and also replaces ThreadControl
 * methods by operations from the ThreadWatcher class.
 * 
 */
public aspect ThreadControlAspect {

	private Collection<String> classesToIgnoreVerification = new HashSet<String>();
	
	/**
	 * The object responsible for storing threads and their states.
	 */
	private ThreadWatcher threadWatcher = new ThreadWatcher();

	/**
	 * Replaces ThreadControl.waitUntilStateIsReached() method implementation
	 * using ThreadWatcher.
	 */
	void around(ThreadControl tcs): execution(
			public void ThreadControl.waitUntilStateIsReached()) && this (tcs){
		threadWatcher.waitUntilSystemConfiguration();
	}

	/**
	 * Replaces ThreadControl.proceed() method implementation using
	 * ThreadWatcher.
	 */
	void around(ThreadControl tcs): execution(public void ThreadControl.proceed()) 
		&& this (tcs){
		threadWatcher.proceed();
	}

	/**
	 * Replaces ThreadControl.reset() method implementation using ThreadWatcher.
	 */
	void around(ThreadControl tcs): execution(public void ThreadControl.reset()) 
		&& this (tcs){
		threadWatcher.reset();
	}

	/**
	 * Replaces ThreadControl.prepare(List<ThreadConfiguration>) method
	 * implementation using ThreadWatcher.
	 */
	void around(List<ThreadConfiguration> threadsConfiguration): 
		execution(public void ThreadControl.prepare(List<ThreadConfiguration>)) 
			&& args(threadsConfiguration){
		threadWatcher.prepare(new ListOfThreadConfigurations(threadsConfiguration));
		this.classesToIgnoreVerification = new HashSet<String>();
	}
	
	/**
	 * Replaces ThreadControl.prepare(SystemConfiguration) method
	 * implementation using ThreadWatcher.
	 */
	void around (SystemConfiguration sysConfig): 
		execution(public void ThreadControl.prepare(SystemConfiguration)) 
			&& args(sysConfig){
		threadWatcher.prepare(sysConfig);
		this.classesToIgnoreVerification = sysConfig.getClassNamesToIgnore();
	}

	/*
	 * In the following we define pointcuts and advices to monitor the
	 * application and notify ThreadWatcher about state transitions so that it
	 * can provide its functionality.
	 */

	/**
	 * This pointcut defines join points where thread actions should not be
	 * monitored such as operations inside ThreadWatcher class.
	 */
	pointcut excludedEntities():
        !within(br.edu.ufcg.threadcontrol.ThreadWatcher) &&
        !within(br.edu.ufcg.threadcontrol.aspects.ThreadSleeperAspect);
        
	/**
	 * Collects calls to Thread.start() method.
	 */
	pointcut threadStartCalls(Thread t):call(public void start())&& target(t) 
		&& excludedEntities();

	/**
	 * Collects calls to Object.wait() method.
	 */
	pointcut waitCalls(Object o):call(public void wait())&& target(o) 
		&& excludedEntities();

	/**
	 * Collects the execution of the run() method of a Runnable implementation.
	 */
	pointcut runnableRunExecutions():execution(public void Runnable+.run()) 
		&& excludedEntities();

	/**
	 * Collects the calls to Thread.sleep(long) method.
	 */
	pointcut sleepCalls(): call (public static void Thread.sleep(long)) 
		&& excludedEntities();

	/**
	 * Collects calls to Object.wait that use a timeout.
	 */
	pointcut timedWaitCalls(Object o): call(public void wait(long))
		&& target(o) && excludedEntities();

	/**
	 * Collects calls to Object.notifyAll method.
	 */
	pointcut notifyAllCalls(Object o): call(public void notifyAll())
		&& target(o) && excludedEntities();

	/**
	 * Collects calls to Object.notify method.
	 */
	pointcut notifyCalls(Object o): call(public void notify())
		&& target(o) && excludedEntities();

	/**
	 * Collects calls to BlockingQueue+.put
	 */
	pointcut blockingQueuePutCalls(BlockingQueue q): call(
			public void BlockingQueue+.put(..)) && target(q)
			&& excludedEntities();

	/**
	 * Collects calls to BlockingQueue+.take
	 */
	pointcut blockingQueueTakeCalls(BlockingQueue q): call(
			public * BlockingQueue+.take())	&& target(q)
			&& excludedEntities();
	
	/**
	 * Collects calls to Semaphore.acquire without a permits
	 * parameter
	 */
	pointcut semaphoreAcquireCallsOnePermit(Semaphore sem): call(
			public void Semaphore.acquire()) && target(sem)
			&& excludedEntities();
	
	/**
	 * Collects calls to Semaphore.acquire with a specific
	 * permits parameter value.
	 */
	pointcut semaphoreAcquireCallsDefiningPermits(Semaphore sem, int permits): 
		call(public void Semaphore.acquire(int)) && target(sem)
			&& args(permits) && excludedEntities();
	
	/**
	 * Collects calls to Semaphore.release without a permits
	 * parameter
	 */
	pointcut semaphoreReleaseCallsOnePermit(Semaphore sem): call(
			public void Semaphore.release()) && target(sem)
			&& excludedEntities();
	
	/**
	 * Collects calls to Semaphore.release with a specific
	 * permits parameter value.
	 */
	pointcut semaphoreReleaseCallsDefiningPermits(Semaphore sem, int permits): 
		call(public void Semaphore.release(int)) && target(sem)
			&& args(permits) && excludedEntities();
	
	/**
	 * Collects calls to Semaphore.drainPermits() method.
	 */
	pointcut semaphoreDrainPermits(Semaphore sem):
		call(public int Semaphore.drainPermits()) && target(sem);
	
	/*
	 * ADVICE
	 */

	/**
	 * Before a Thread.sleep call, ThreadWatcher is notified about a state
	 * change: the current thread has started to sleep.
	 */
	before(): sleepCalls() {
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadHadStateChange(Thread.currentThread(),
				ThreadState.SLEEPING);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * After a Thread.sleep call, when it is about to return, ThreadWatcher is
	 * notified about a state change: the current thread will go to the RUNNING
	 * state again.
	 */
	after():sleepCalls() {
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadHadStateChange(Thread.currentThread(),
				ThreadState.RUNNING);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * Before a Thread.start call, ThreadWatcher is notified that a new thread
	 * should be monitored and that its initial state is STARTED.
	 */
	before(Thread t): threadStartCalls(t) {
		threadWatcher.threadHadStateChange(t, ThreadState.STARTED);
	}

	/**
	 * Before a run() method from a Runnable is to be executed, ThreadWatcher is
	 * notified about a state change: the current thread is now RUNNING,
	 * considering its association with the executing Runnable class.
	 */
	before(): runnableRunExecutions() {
		Object associatedObject = thisJoinPoint.getThis();
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadHadStateChange(Thread.currentThread(),
				ThreadState.RUNNING, associatedObject);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * After a run() method from a Runnable has executed, ThreadWatcher is
	 * notified about a state change: the current thread is now FINISHED,
	 * considering its association with the executing Runnable class.
	 */
	after(): runnableRunExecutions() {
		Object associatedObject = thisJoinPoint.getThis();
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadHadStateChange(Thread.currentThread(),
				ThreadState.FINISHED, associatedObject);
	}

	/**
	 * Before a Thread begins to wait on a monitor, ThreadWatcher is notified
	 * about a state change: the current thread is now in the WAITING state.
	 */
	before(Object obj):(waitCalls(obj)|| timedWaitCalls(obj)) {
		threadWatcher.threadStartedToWaitOnObject(Thread.currentThread(), obj, WaitType.WAIT_ON_OBJ_LOCK);
	}

	/**
	 * Before a Thread possibly begins to wait on a BlockingQueue <code>put</code> 
	 * operation, ThreadWatcher is notified about a state change: the current 
	 * thread may be now in the WAITING state, depending on the queue capacity.
	 */	
	before(BlockingQueue queue) : blockingQueuePutCalls(queue) {
		Thread currentThread = Thread.currentThread();
		threadWatcher.threadPossiblyStartedToWaitOnBlockingQueue(
				currentThread, queue, Operation.put);
	}
	
	/**
	 * After a Thread had possibly waited on a BlockingQueue <code>put</code> 
	 * operation, ThreadWatcher is notified about a state change: the current 
	 * thread is now in the RUNNING state. The state of Threads that were 
	 * waiting on the <code>take</code> operation are updated considering the 
	 * queue current capacity.
	 */	
	after(BlockingQueue queue) : blockingQueuePutCalls(queue) {
		Thread t = Thread.currentThread();
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadPossiblyFinishedToWaitOnBlockingQueue( 
				t, queue, Operation.put);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * Before a Thread possibly begins to wait on a BlockingQueue <code>take</code> 
	 * operation, ThreadWatcher is notified about a state change: the current 
	 * thread may be now in the WAITING state, if the queue is empty.
	 */	
	before(BlockingQueue queue) : blockingQueueTakeCalls(queue) {
		Thread currentThread = Thread.currentThread();
		threadWatcher.threadPossiblyStartedToWaitOnBlockingQueue(
				currentThread, queue, Operation.take);
	}
	
	/**
	 * After a Thread had possibly waited on a BlockingQueue <code>take</code> 
	 * operation, ThreadWatcher is notified about a state change: the current 
	 * thread is now in the RUNNING state. The state of Threads that were 
	 * waiting on the <code>put</code> operation are updated considering the 
	 * queue current capacity.
	 */	
	after(BlockingQueue queue) : blockingQueueTakeCalls(queue) {
		Thread t = Thread.currentThread();
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadPossiblyFinishedToWaitOnBlockingQueue( 
				t, queue, Operation.take);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}
	
	/**
	 * Before a Thread possibly begins to wait on a Semaphore 
	 * <code>acquire</code> operation, ThreadWatcher is notified about 
	 * a state change: the current thread may be now in the WAITING 
	 * state, if the number of permits available is not enough.
	 */	
	before(Semaphore sem): semaphoreAcquireCallsOnePermit(sem) {
		Thread currentThread = Thread.currentThread();
		int permitsRequested = 1;
		threadWatcher.threadPossiblyStartedToWaitOnSemaphore(
				currentThread, sem,permitsRequested);
	}
	
	/**
	 * Before a Thread possibly begins to wait on a Semaphore 
	 * <code>acquire</code> operation, ThreadWatcher is notified about 
	 * a state change: the current thread may be now in the WAITING 
	 * state, if the number of permits available is not enough.
	 */	
	before(Semaphore sem, int permitsRequested): 
		semaphoreAcquireCallsDefiningPermits(sem, permitsRequested) {
		Thread currentThread = Thread.currentThread();
		threadWatcher.threadPossiblyStartedToWaitOnSemaphore(currentThread, sem,
					permitsRequested);
	}
	
	/**
	 * After a Thread had possibly waited on a Semaphore <code>acquire</code> 
	 * operation, ThreadWatcher is notified about a state change: the current 
	 * thread is now in the RUNNING state. The state of Threads that were 
	 * waiting for available permits from the Semaphore are updated considering
	 * the number of permits available.
	 */	
	after(Semaphore sem): 
		semaphoreAcquireCallsOnePermit(sem){
		int permitsRequested = 1;
		Thread t = Thread.currentThread();
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadPossiblyFinishedToWaitOnSemaphore( t, sem, permitsRequested );
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}
	
	/**
	 * After a Thread had possibly waited on a Semaphore <code>acquire</code> 
	 * operation, ThreadWatcher is notified about a state change: the current 
	 * thread is now in the RUNNING state. The state of Threads that were 
	 * waiting for available permits from the Semaphore are updated considering
	 * the number of permits available.
	 */	
	after(Semaphore sem, int permitsRequested): 
		semaphoreAcquireCallsDefiningPermits(sem, permitsRequested){
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.threadPossiblyFinishedToWaitOnSemaphore( Thread.currentThread(), sem, permitsRequested );
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * Before a call to the <code>release</code> operation from Semaphore 
	 * ThreadWatcher is notified to update the state of threads waiting on
	 * a Semaphore, considering the permits being released.
	 */	
	before(Semaphore sem): semaphoreReleaseCallsOnePermit(sem){
		int numPermits = 1;
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.updateThreadsAfterSemaphoreRelease(sem, numPermits);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}
	
	
	/**
	 * Before a call to the <code>release</code> operation from Semaphore 
	 * ThreadWatcher is notified to update the state of threads waiting on
	 * a Semaphore, considering the permits being released.
	 */	
	before(Semaphore sem, int numPermits): semaphoreReleaseCallsDefiningPermits(sem, numPermits){
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.updateThreadsAfterSemaphoreRelease(sem, numPermits);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}
	
	/**
	 * After a call to the <code>release</code> operation from Semaphore 
	 * ThreadWatcher is notified to update the state of threads waiting on
	 * a Semaphore, considering the permits have been reset.
	 */	
	after(Semaphore sem): semaphoreDrainPermits(sem){
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.updateThreadsAfterSemaphoreDrainPermits(sem);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}
	/**
	 * After a Thread finishes to wait on a monitor (returning from the wait
	 * call that had a timeout), ThreadWatcher is notified about a state change:
	 * the current thread is now in the RUNNING state, and not in the WAITING
	 * state anymore.
	 */
	after(Object o):timedWaitCalls(o) {
		verifyAndBlockThreadIfNecessary(thisJoinPoint, o);
		threadWatcher.threadFinishedToWaitOnObject(Thread.currentThread(), o,
				true, WaitType.WAIT_ON_OBJ_LOCK);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * After a Thread finishes to wait on a monitor (returning from the wait
	 * call), ThreadWatcher is notified about a state change: the current thread
	 * is now in the RUNNING state, and not in the WAITING state anymore.
	 */
	after(Object o): waitCalls(o) {
		verifyAndBlockThreadIfNecessary(thisJoinPoint, o);
		threadWatcher.threadFinishedToWaitOnObject(Thread.currentThread(), o,
				false, WaitType.WAIT_ON_OBJ_LOCK);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * Before a Object.notifyAll() is called, ThreadWatcher is notified about a
	 * state change: all threads waiting on a given monitor should go to the
	 * NOTIFIED state.
	 */
	before(Object o): notifyAllCalls(o) {
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.notifyAllWaitingThreads(o);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}

	/**
	 * Before a Object.notify() is called, ThreadWatcher is notified about a
	 * state change: one of the threads waiting on a given monitor should go to
	 * the NOTIFIED state. If more than one thread is waiting, it is not
	 * possible to detect at this moment which one will be notified. In this
	 * case, all of them go to the POSSIBLY_NOTIFIED state
	 */
	before(Object o): notifyCalls(o) {
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
		threadWatcher.notifyOneWaitingThread(o, WaitType.WAIT_ON_OBJ_LOCK);
		verifyAndBlockThreadIfNecessary(thisJoinPoint);
	}
	
	/**
	 * Verifies if the current thread involved in a possible state change 
	 * should or not proceed. In case the expected state has been reached
	 * the current thread will be blocked using an external lock object.
	 * @param jp The instance of the joinPoint associated with the state
	 * transition. 
	 * 
	 */
	private void verifyAndBlockThreadIfNecessary(JoinPoint jp){
		String className = jp.getSourceLocation().getWithinType().getCanonicalName();
		if (threadWatcher.isSituationBeingExpected() 
				&& !this.classesToIgnoreVerification.contains(className)) {
			threadWatcher.verifyThread();
		}
	}
	
	/**
	 * Verifies if the current thread involved in a possible state change 
	 * should or not proceed. In case the expected state has been reached
	 * the current thread will be blocked. The lock used will be the object
	 * used in the wait call. 
	 * @param jp The instance of the joinPoint associated with the state
	 * transition. 
	 * @param o The object involved in the wait call, so that it can be 
	 * used as the lock object to block the thread if the expected state has
	 * been reached in order to avoid state changes while assertions are 
	 * being performed.
	 * 
	 */
	private void verifyAndBlockThreadIfNecessary(JoinPoint jp, Object o){
		String className = jp.getSourceLocation().getWithinType().getCanonicalName();
		if (threadWatcher.isSituationBeingExpected() 
				&& !this.classesToIgnoreVerification.contains(className)) {
			while (threadWatcher.verifyIfThreadShouldBeBlocked()){
				try {
					long timeToWait = 200;
					o.wait(timeToWait);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
