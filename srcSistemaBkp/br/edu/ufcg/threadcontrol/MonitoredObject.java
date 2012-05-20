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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents an object and the threads waiting for it.
 */
public class MonitoredObject {

	/**
	 * The object being monitored (an object that has received a wait call).
	 */
	private Object monitored;

	/**
	 * The threads monitoring the object.
	 */
	private Set<Thread> monitoringThreads = Collections
			.synchronizedSet(new HashSet<Thread>());

	/**
	 * Constructor.
	 * 
	 * @param o
	 *            The object being monitored (where a wait was called).
	 * @param firstThread
	 *            The thread waiting for the object.
	 */
	public MonitoredObject(Object o, Thread firstThread) {
		this.monitored = o;
		this.monitoringThreads.add(firstThread);
	}

	/**
	 * Verifies if an object is monitored by a certain Thread.
	 * 
	 * @param t
	 *            The Thread we want to know if it is monitored.
	 * @return true if while t was running, a wait on a certain object was
	 *         called.
	 */
	public boolean isMonitoredBy(Thread t) {
		return this.monitoringThreads.contains(t);
	}

	/**
	 * Adds a certain Thread to the list of threads waiting for an object.
	 * 
	 * @param t
	 *            The Thread.
	 * @return true, if t was not monitoring this object, and false otherwise.
	 */
	public boolean addThread(Thread t) {
		Object o = this.monitoringThreads.add(t);
		return o != null;
	}

	/**
	 * Gets the list of threads waiting for an object notify or notifyAll.
	 * 
	 * @return the list of threads waiting for an object notify or notifyAll.
	 */
	public Iterator<Thread> getMonitoringThreadsIterator() {

		return this.monitoringThreads.iterator();
	}

	/**
	 * Gets the Map storing threads waiting for an object notify or notifyAll.
	 * 
	 * @return the Map storing threads waiting for an object notify or
	 *         notifyAll.
	 */
	public Set<Thread> getMonitoringThreads() {

		return this.monitoringThreads;
	}

	/**
	 * Gets a String that represents this object.
	 * 
	 * @return the textual representation of this object.
	 */
	public String toString() {

		return "Obj:" + this.monitored + " Threads.size:"
				+ this.monitoringThreads.size();
	}

	/**
	 * Removes a thread that was being monitored.
	 * @param t The thread.
	 */
	public void removeMonitoringThread(Thread t) {
		this.monitoringThreads.remove(t);

	}
}
