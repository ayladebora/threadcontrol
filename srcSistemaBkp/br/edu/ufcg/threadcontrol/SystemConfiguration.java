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

/**
 * An expected system configuration.
 * 
 */
public interface SystemConfiguration {

	/**
	 * 
	 * Verifies if the expected system configuration was reached.
	 * 
	 * @param tManager
	 *            A ThreadManager used for the verification process.
	 * @return true, if the system expected state has been reached, and false,
	 *         otherwise.
	 */
	public boolean wasConfigurationReached(ThreadManager tManager);

	/**
	 * Gets the class names to ignore.
	 * @return a Collection with class names to ignore.
	 */
	public Collection<String> getClassNamesToIgnore();
	
}
