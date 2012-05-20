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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * A kind of SystemConfiguration expressed in terms of ThreadConfiguration
 * objects.
 * 
 */
public class ListOfThreadConfigurations implements SystemConfiguration {

	/**
	 * List of ThreadConfigurations that should all be reached.
	 */
	private List<ThreadConfiguration> threadConfigurations;
	
	private Collection<String> classNamesToIgnore = new HashSet<String>();

	/**
	 * Constructor
	 * 
	 * @param tConfigurations
	 *            List of ThreadConfigurations which should all be achieved.
	 */
	public ListOfThreadConfigurations(List<ThreadConfiguration> tConfigurations) {
		this.threadConfigurations = tConfigurations;
	}

	/**
	 * Default constructor.
	 */
	public ListOfThreadConfigurations() {
		this.threadConfigurations = new LinkedList<ThreadConfiguration>();
	}

	public void addThreadConfiguration(ThreadConfiguration tc){
		this.threadConfigurations.add(tc);
	}
	/**
	 * Verifies if for all ThreadConfiguration objects, the expected state was
	 * reached, considering the given ThreadManager
	 */
	public boolean wasConfigurationReached(ThreadManager manager) {
		if (this.threadConfigurations == null
				|| this.threadConfigurations.size() == 0) {
			return true;
		}
		boolean wasReached = false;
		for (ThreadConfiguration tc : this.threadConfigurations) {
			if (!tc.wasConfigurationReached(manager)) {
				wasReached = false;
				break;
			} else {
				wasReached = true;
			}
		}
		return wasReached;
	}

	/**
	 * Get names of classes to ignore in the monitoring process.
	 */
	public Collection<String> getClassNamesToIgnore() {
		return classNamesToIgnore;
	}
	
	/**
	 * Add a new class to be ignored.
	 * @param c The class whose name will be ignored in the
	 * monitoring process.
	 */
	public void addToClassesToBeIgnored(Class c){
		this.classNamesToIgnore.add(c.getCanonicalName());
	}
	
	/**
	 * Add a new name of class to be ignored.
	 * @param classCanonicalName The canonical name of a class
	 * to be ignored.
	 */
	public void addToClassesToBeIgnored(String classCanonicalName){
		this.classNamesToIgnore.add(classCanonicalName);
	}

}
