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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * This class monitors the threads related with a MonitoredSemaphore.
 * 
 */
public class MonitoredSemaphore {

	/**
	 * Number of permits released and not yet used
	 */
	private int availablePermits;
	
	/**
	 * The threads monitoring the object.
	 */
	private ConcurrentHashMap<Thread, Integer> monitoringThreadsAndPermits = new ConcurrentHashMap<Thread, Integer>();

	/**
	 * Constructor.
	 * @param sem The semaphore being monitored.
	 * @param firstThread The first thread being monitored and related with
	 * a semaphore <code>sem</code>.
	 * @param permitsRequested
	 */
	public MonitoredSemaphore(Semaphore sem, Thread firstThread, int permitsRequested) {
		this.availablePermits = sem.availablePermits();
		this.monitoringThreadsAndPermits.put(firstThread, permitsRequested);
	}
	
	/**
	 * Constructor. 
	 * @param sem The Semaphore being monitored.
	 */
	public MonitoredSemaphore(Semaphore sem) {
		this.availablePermits = sem.availablePermits();
	}

	/**
	 * Gets the number of threads being monitored.
	 * @return the number of threads being monitored.
	 */
	public int getNumberOfMonitoringThreads() {
		return this.monitoringThreadsAndPermits.size();
	}

	/**
	 * Adds a new thread to be monitored with the number of permits
	 * that were requested.
	 * @param t The thread to be monitored.
	 * @param permitsRequested The number of permits requested.
	 * @return true if the thread was added and was not being monitored,
	 * and false, otherwise.
	 */
	public boolean addThreadWithPermits(Thread t, int permitsRequested) {
		return this.monitoringThreadsAndPermits.put(t, permitsRequested) == null;
	}

	/**
	 * Removes a thread from the collection of threads being monitored.
	 * @param t The thread.
	 */
	public void removeMonitoringThread(Thread t) {
		this.monitoringThreadsAndPermits.remove(t);
		
	}
	
	/**
	 * Gets the state of a given threads considering the number
	 * of permits that exist, and the number of permits requested
	 * by a certain thread.
	 * @param t The thread.
	 * @return the state for the thread considering the number
	 * of permits available and requested.
	 */
	public ThreadState getThreadStateForAvailablePermits(Thread t) {
		int permitsRequested = this.monitoringThreadsAndPermits.get(t);
		if (this.availablePermits < permitsRequested){
			return ThreadState.WAITING;
		} else {
			return ThreadState.POSSIBLY_NOTIFIED;
		}
	}

	/**
	 * Gets the threads being monitored.
	 * @return the set of threads being monitored.
	 */
	public Set<Thread> getMonitoringThreads() {
		return this.monitoringThreadsAndPermits.keySet();
	}

	/**
	 * Decreases the number of available permits by 
	 * <code>permitsToRemove</code>.
	 * @param permitsToRemove The number of permits which are not
	 * available anymore.
	 */
	public void removeAvailablePermits(int permitsToRemove) {
		this.availablePermits-=permitsToRemove;
	}

	/**
	 * Increases the number of available permits by <code>numPermits</code>.
	 * @param numPermits The number of permits to be added.
	 */
	public void addAvailablePermits(int numPermits) {
		this.availablePermits+= numPermits;
	}

	/**
	 * Sets the number of available permits to 0 (zero).
	 */
	public void resetAvailablePermits( ) {
		this.availablePermits = 0;
	}
}
