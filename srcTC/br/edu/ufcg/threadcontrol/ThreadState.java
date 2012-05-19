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

/**
 * Possible states for threads.
 *
 */
public class ThreadState {
	public static final ThreadState UNKNOWN = new ThreadState("UNKNOWN");
	public static final ThreadState SLEEPING= new ThreadState("SLEEPING");
	public static final ThreadState WAITING = new ThreadState("WAITING");
	public static final ThreadState RUNNING = new ThreadState("RUNNING");
	public static final ThreadState FINISHED = new ThreadState("FINISHED");
	public static final ThreadState STARTED = new ThreadState("STARTED");
	public static final ThreadState NOTIFIED = new ThreadState("NOTIFIED");
	public static final ThreadState POSSIBLY_NOTIFIED = new ThreadState("POSSIBLY_NOTIFIED");
	public static final ThreadState AT_SPECIFIC_APP_POINT = new ThreadState("AT_SPECIFIC_APP_POINT");

	private String state;
	
	/**
	 * Cosntructor
	 * @param state String representing a state.
	 */
	public ThreadState(String state) {
		this.state = state;
	}
	
	/**
	 * Returns the String representing this state
	 * @return the String representing the state.
	 */
	public String getState() {
		return this.state;
	}

	/**
	 * Compares this state with another one, regarding
	 * the String representation of the thread state.
	 */
	public boolean equals(Object o){
		if (o instanceof ThreadState) {
			ThreadState ts = (ThreadState) o;
			return ts.getState().equals(this.getState());
		}
		return false;
	}

	/**
	 * Factory method to create ThreadState instances.
	 * @param state A String representation of a thread state.
	 * @return a ThreadState instance corresponding to the 
	 * given String.
	 */
	public static ThreadState createThreadState(String state){
		return new ThreadState(state);
	}

	/**
	 * Gets the string representation of this thread state.
	 */
	public String toString(){
		return this.getState();
	}
}
