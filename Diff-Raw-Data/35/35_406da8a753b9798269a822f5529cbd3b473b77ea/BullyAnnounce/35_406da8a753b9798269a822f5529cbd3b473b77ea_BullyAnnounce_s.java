 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical.election;
 
import java.util.LinkedList;

 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 
 /**
  * PACKET: It is used when a new coordinator is signaled to all cluster members.
  * 		   The packet has to be send as broadcast.
  */
 public class BullyAnnounce extends SignalingMessageBully
 {
 	private static final long serialVersionUID = 794175467972815277L;
 
 	private int mCoordinatorID;
 	private String mCoordinatorDescription = null;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender (coordinator)
 	 * @param pSenderPriority the priority of the message sender (coordinator)
 	 * @param pCoordinatorID the unique ID of the message sender (coordinator)
 	 * @param pCoordinatorDescription a description text of the coordinator
 	 */
 	public BullyAnnounce(Name pSenderName, BullyPriority pSenderPriority, int pCoordinatorID, String pCoordinatorDescription)
 	{
 		super(pSenderName, HRMID.createBroadcast(), pSenderPriority);
 		mCoordinatorDescription = pCoordinatorDescription;
 		mCoordinatorID = pCoordinatorID;
 	}
 	
 	/**
 	 * Returns the descriptive string about the coordinator which announces it coordination
 	 * 
 	 * @return the descriptive string
 	 */
 	public String getCoordinatorDescription()
 	{
 		return mCoordinatorDescription;
 	}
 	
 	/**
 	 * Returns the unique coordinator ID
 	 * 
 	 * @return the unique coordinator ID
 	 */
 	public int getCoordinatorID()
 	{
 		return mCoordinatorID;
 	}
 
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "](Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ", SenderPrio=" + getSenderPriority().getValue() + ", Coordinator=" + mCoordinatorDescription + ")";
 	}
 }
