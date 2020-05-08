 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical;
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.LoggableElement;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 public class SignalingMessageHrm extends LoggableElement implements Serializable
 {
 
 	/**
 	 * The name of the message sender. This is always a name of a physical node.
 	 */
 	private Name mSenderName = null;
 	
 	/**
 	 * The name of the message receiver. This always a name of a physical node. 
 	 */
 	private Name mReceiverName = null;
 
 	/**
 	 * Counts the HRM internal messages
 	 */
 	private static int mHRMMessagesCounter = 0;
 	
 	/**
 	 * Stores the HRM message number
 	 */
 	private int mMessageNumber = -1;
 	
 	/**
 	 * Stores the recorded source route.
 	 * This is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private String mSourceRoute = "";
 	
 	/**
 	 * For using the class within (de-)serialization. 
 	 */
 	private static final long serialVersionUID = 7253912074438961613L;
 	
 	public SignalingMessageHrm(Name pSenderName, Name pReceiverName)
 	{
 		mSenderName = pSenderName;
 		mReceiverName = pReceiverName;
 		mMessageNumber = createMessageNumber();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 			Logging.log(getClass().getSimpleName() + "(Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + "): CREATED");
 		}
 	}
 	
 	/**
 	 * Creates an HRM message number
 	 * 
 	 * @return the create HRM message number
 	 */
 	private static synchronized int createMessageNumber()
 	{
 		int tResult = -1;		
 		
 		tResult = ++mHRMMessagesCounter;
 		
 		Logging.log("\n########### CREATING HRM MESSAGE nr. " + tResult);
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the HRM message number
 	 * 
 	 * @return the HRM message number
 	 */
 	public int getMessageNumber()
 	{
 		return mMessageNumber;
 	}
 	
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public Name getSenderName()
 	{
 		return mSenderName;
 	}
 
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public Name getReceiverName()
 	{
 		return mReceiverName;
 	}
 	
 	/**
 	 * Add a part to the recorded source route
 	 * 
 	 * @param pRoutePart the route part
 	 */
 	public void addSourceRoute(String pRoutePart)
 	{
		mSourceRoute += "\n=> " + pRoutePart; 
 	}
 	
 	/**
 	 * Returns the route this packet has passed
 	 * 
 	 * @return the source route
 	 */
 	public String getSourceRoute()
 	{
 		return new String(mSourceRoute);
 	}
 	
 	/**
 	 * Duplicates all member variables for another packet
 	 * 
 	 * @param pOtherPacket the other packet
 	 */
 	public void duplicate(SignalingMessageHrm pOtherPacket)
 	{
 		// update the recorded source route
 		pOtherPacket.mSourceRoute = getSourceRoute();
 		
 		// add an entry to the recorded source route
		pOtherPacket.addSourceRoute("[duplicated]");
 	}
 	
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "](Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ")";
 	}
 }
