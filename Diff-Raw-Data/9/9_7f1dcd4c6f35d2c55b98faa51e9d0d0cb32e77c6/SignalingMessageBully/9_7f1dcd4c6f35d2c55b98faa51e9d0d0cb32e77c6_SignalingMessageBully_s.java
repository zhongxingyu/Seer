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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 
 public class SignalingMessageBully extends SignalingMessageHrm
 {
 	private static final long serialVersionUID = -7721094891385820251L;
 
 	/**
 	 * This is the Bully priority of the message sender.
 	 */
 	private BullyPriority mSenderPriority = null;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pSenderPriority the Bully priority of the message sender
 	 * @param pReceiverName the name of the message receiver
 	 */
 	public SignalingMessageBully(Name pSenderName, Name pReceiverName, BullyPriority pSenderPriority)
 	{
 		super(pSenderName, pReceiverName);
 		mSenderPriority = pSenderPriority;
 	}
 	
 	/**
 	 * Determine the sender's priority.
 	 * 
 	 * @return the priority of the message sender
 	 */
 	public BullyPriority getSenderPriority()
 	{
		return mSenderPriority.clone();
 	}
 
 	/**
 	 * Duplicates all member variables for another packet
 	 * 
 	 * @param pOtherPacket the other packet
 	 */
 	public void duplicate(SignalingMessageBully pOtherPacket)
 	{
 		super.duplicate(pOtherPacket);
 		
 		// update the recorded source route
 		pOtherPacket.mSenderPriority = getSenderPriority();
 	}
 
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "[" + getMessageNumber() + "](Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ", SenderPrio=" + getSenderPriority().getValue() + ")";
 	}
 }
