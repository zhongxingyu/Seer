 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
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
 package de.tuilmenau.ics.fog.facade;
 
 import de.tuilmenau.ics.fog.facade.events.Event;
 
 
 /**
  * Base class for objects that informs others about asynchronous events.
  */
 public interface EventSource
 {
 	/**
 	 * Registers observer for the event source.
 	 * 
 	 * @param observer entity, which will be informed about event
 	 */
 	public void registerListener(EventListener observer);
 	
 	/**
 	 * Unregisters observer for the event source.
 	 * 
 	 * @param observer entity, which should be removed from the observer list
 	 * @return true, if observer had been successfully unregistered; false otherwise
 	 */
 	public boolean unregisterListener(EventListener observer);
 	
 	/**
	 * Interface for observer of the event source
 	 */
 	public interface EventListener
 	{
 		/**
		 * Called if an event is occurring at the event source.
 		 * This callback method is NOT allowed to block.
 		 * It MUST return as fast as possible since it it
 		 * executed in the thread of the event source.
 		 * 
 		 * @param source Source of the event
 		 * @param event Event itself
 		 * @throws Exception On error; Exceptions are ignored by the caller. 
 		 */
 		public void eventOccured(Event event) throws Exception;
 	}
 
 }
