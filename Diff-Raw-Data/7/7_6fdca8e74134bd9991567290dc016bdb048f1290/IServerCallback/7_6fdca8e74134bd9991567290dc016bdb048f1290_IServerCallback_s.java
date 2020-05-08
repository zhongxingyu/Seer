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
 
 import java.util.LinkedList;
 
 
 /**
  * Interface of a higher layer for callbacks from FoG.
  */
 public interface IServerCallback
 {
 	/**
 	 * Callback asking for the permission to open a new connection to a server.
 	 * The method should return a reference to the object, which should handle
 	 * the received packets. Furthermore, the IReceiveCallback object will be
 	 * informed about the FoG-socket for sending data via this connection. 
 	 * 
 	 * @param pAuths Authentications for the open request
	 * @param pRoute Route to the client requesting the new connection
 	 * @param pTargetName Registered name of the Server, which the new connection was requested to
	 * @return Reference to object handling the connection or null if open is not allowed
 	 * 
	 * TODO check if parameter pRoute is needed. It should be deleted, since if shows FoG details to the application
 	 */
 	public boolean openAck(LinkedList<Signature> pAuths, Description pDescription, Name pTargetName);
 
 	/**
 	 * Is called in order to set corresponding send interface for receiving
 	 * application. It is called after {@code openAck} if the connection
 	 * establishment was allowed.
 	 * 
 	 * @param pConnection Socket for sending data via the connection to the communication partner  
 	 */
 	public void newConnection(Connection pConnection);
 }
