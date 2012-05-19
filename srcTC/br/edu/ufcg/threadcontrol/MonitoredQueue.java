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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * This class monitors the threads related with a BlockingQueue.
 */
public class MonitoredQueue {

	/**
	 * Blocking queue operations being monitored.
	 *
	 */
	public enum Operation {
		take, put;
	}
	
	/**
	 * The queue size.
	 */
	private int size;

	/**
	 * The queue capacity.
	 */
	private int capacity;

	/**
	 * Map with the threads being monitored regarding a blocking queue operation
	 * and also the queue size.
	 */
	private HashMap<Operation, HashSet<Thread>> monitoringThreadsAndQueueSize;

	/**
	 * Constructor
	 * @param queue the queue being monitored.
	 */
	public MonitoredQueue(BlockingQueue<?> queue) {
		this.size = queue.size();
		this.capacity = queue.size() + queue.remainingCapacity();
		this.monitoringThreadsAndQueueSize = new HashMap<Operation, 
			HashSet<Thread>>();
		for (Operation oper : Operation.values()) {
			this.monitoringThreadsAndQueueSize.put(oper, 
					new HashSet<Thread>());
		}
	}

	/**
	 * Gets the number of threads waiting on a blocking queue operation.
	 * @param oper The queue operation.
	 * @return the number of threads waiting on a blocking queue operation.
	 */
	public int getNumberOfMonitoringThreads(Operation oper) {
		return this.monitoringThreadsAndQueueSize.get(oper).size();
	}
	
	/**
	 * Adds a new thread to be monitored because it is waiting on
	 * a certain queue operation.
	 * @param t The new thread.
	 * @param oper The BlockingQueue operation called.
	 * @return true if the collection of monitored threads has
	 * changed when the thread was added. 
	 */
	public boolean addThreadBlockingQueue(Thread t, Operation oper) {
		return this.monitoringThreadsAndQueueSize.get(oper).add(t);
	}

	/**
	 * Removes a thred from the collection of monitored threads waiting
	 * on this operation.
	 * @param t The Thread instance.
	 * @param oper The BlockingQueue operation.
	 */
	public void removeThreadBlockingQueue(Thread t, Operation oper) {
		if (Operation.put.equals(oper)) {
			size++;
		} else {
			size--;
		}
		this.monitoringThreadsAndQueueSize.get(oper).remove(t);
	}
	
	/**
	 * Get the state in which the thread should be considering the
	 * evaluation of the capacity and size values being managed by this
	 * class.
	 * @param t The thread.
	 * @return the state of this thread considering the evaluation
	 * of the queue capacity and size.
	 */
	public ThreadState getThreadStateForQueueSize(Thread t) {
		if (this.monitoringThreadsAndQueueSize.get(Operation.put).contains(t)) {
			if (this.capacity > this.size) {
				return ThreadState.POSSIBLY_NOTIFIED;
			} else {
				return ThreadState.WAITING;	
			}
		} else if (this.monitoringThreadsAndQueueSize.get(Operation.take).contains(t)){
			if (this.size > 0) {
				return ThreadState.POSSIBLY_NOTIFIED;
			} else {
				return ThreadState.WAITING;
			}
		} else {
			return ThreadState.UNKNOWN;
		}
	}

	/**
	 * Gets the set of threads being monitored regarding a BlockingQueue
	 * operation.
	 * @param oper The BlockingQueue operation.
	 * @return the set of threads being monitored regarding a BlockingQueue
	 * operation.
	 */
	public Set<Thread> getMonitoringThreads(Operation oper) {
		return this.monitoringThreadsAndQueueSize.get(oper);
	}

}
