 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Launcher
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.launcher;
 
 import de.tuilmenau.ics.fog.topology.Simulation;
 
 
 /**
  * Interface for an observer of a simulation.
  * For each simulation all observer will be instantiated and
  * informed about the status of the simulation.
  * 
  * Observer can register by using the extension point
  * "de.tuilmenau.ics.fog.simulation".
  */
 public interface SimulationObserver
 {
 	/**
 	 * Called directly after calling the constructor
 	 * of a simulation. Thus, simulation configuration
 	 * might not be configured completely. 
 	 * 
 	 * @param sim Simulation the observer is used for
 	 */
 	public void created(Simulation sim);
 	
 	/**
	 * Called before the simulation is initialised.
 	 * This is the very first call the observer will
 	 * receive. 
 	 */
 	public void init();
 	
 	/**
	 * Is called after the simulation is initialised.
	 * Therefore, the sceanario is already loaded.
 	 */
 	public void started();
 	
 	/**
 	 * Is called after the simulation was ended by the
 	 * exit command. Afterwards, the observer will never
 	 * be called again.
 	 */
 	public void ended();
 	
 	/**
 	 * Might be called after all observers had been
 	 * informed about an ended simulation. If one of them
 	 * terminates the VM, the not all observers are called.
 	 */
 	public void finished();
 }
