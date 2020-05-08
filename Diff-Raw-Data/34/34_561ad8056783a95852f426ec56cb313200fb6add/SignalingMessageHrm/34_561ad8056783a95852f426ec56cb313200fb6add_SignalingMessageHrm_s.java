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
 
 import de.tuilmenau.ics.fog.packets.LoggableElement;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.transfer.gates.headers.ProtocolHeader;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 public class SignalingMessageHrm extends LoggableElement implements Serializable, ProtocolHeader
 {
 
 	/**
 	 * The name of the message sender. This is always a name of a physical node.
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private HRMName mSenderName = new L2Address(0);
 	
 	/**
 	 * The name of the message receiver. This always a name of a physical node. 
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private HRMName mReceiverName = new L2Address(0);
 
 	/**
 	 * Counts the HRM internal messages
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
	private static int mHRMMessagesCounter = 0;
 	
 	/**
 	 * Stores the HRM message number
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private int mMessageNumber = -1;
 	
 	/**
 	 * Stores the original HRM message number
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private int mOriginalMessageNumber = -1;
 	
 	/**
 	 * Stores the recorded source route.
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private String mSourceRoute = null;
 	
 	/**
 	 * Stores if the route of packets should be recorded
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private static final boolean RECORD_ROUTE = false;
 	
 	/**
 	 * Stores the counter of created packets from this type
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	public static Long sCreatedPackets = new Long(0);
 
 	/**
 	 * For using the class within (de-)serialization. 
 	 */
 	private static final long serialVersionUID = 7253912074438961613L;
 	
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	protected SignalingMessageHrm()
 	{
 		
 	}
 
 	/**
 	 * Constructor 
 	 * 
 	 * @param pSenderName the name of the sender
 	 * @param pReceiverName the name of the receiver
 	 */
 	public SignalingMessageHrm(HRMName pSenderName, HRMName pReceiverName)
 	{
 		mSenderName = pSenderName;
 		mReceiverName = pReceiverName;
 		mMessageNumber = createMessageNumber();
 		mOriginalMessageNumber = mMessageNumber;
 		synchronized (sCreatedPackets) {
 			sCreatedPackets++;
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 			Logging.log(this, "\n########### NEW HRM MESSAGE nr. " + mMessageNumber + "(Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ")");
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
 	 * Returns the original HRM message number
 	 * 
 	 * @return the original HRM message number
 	 */
 	public int getOriginalMessageNumber()
 	{
 		return mOriginalMessageNumber;
 	}
 	
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public HRMName getSenderName()
 	{
 		return mSenderName;
 	}
 
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public HRMName getReceiverName()
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
 		if(mSourceRoute == null){
 			mSourceRoute = new String();
 		}
 		
 		if(RECORD_ROUTE){
 			mSourceRoute += "\n(" + getMessageNumber() + ")=> " + pRoutePart;
 		}
 	}
 	
 	/**
 	 * Returns the route this packet has passed
 	 * 
 	 * @return the source route
 	 */
 	public String getSourceRoute()
 	{
 		if(mSourceRoute != null){
 			return new String(mSourceRoute);
 		}else{
 			return "";
 		}
 	}
 	
 	/**
 	 * Duplicates all member variables for another packet
 	 * 
 	 * @param pOtherPacket the other packet
 	 */
 	public void duplicate(SignalingMessageHrm pOtherPacket)
 	{
 		sCreatedPackets--;
 
 		// update the recorded source route
 		pOtherPacket.mSourceRoute = getSourceRoute();
 		
 		// update the original message number
 		pOtherPacket.mOriginalMessageNumber = getOriginalMessageNumber();
 		
 		// add an entry to the recorded source route
 		pOtherPacket.addSourceRoute("[duplicated]: (" + getMessageNumber() + ") -> (" + pOtherPacket.getMessageNumber() + ")");
 	}
 	
 	/**
 	 * Returns the size of a serialized representation of this packet 
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.transfer.gates.headers.ProtocolHeader#getSerialisedSize()
 	 */
 	@Override
 	public int getSerialisedSize()
 	{
 		/*************************************************************
 		 * Size of serialized elements in [bytes]:
 		 * 
 		 * 		Signaling packet type = 1
 		 * 
 		 *************************************************************/
 
 		return getDefaultSize();
 	}
 
 	/**
 	 * Returns the default size of this packet
 	 * 
 	 * @return the default size
 	 */
 	public static int getDefaultSize()
 	{
 		/*************************************************************
 		 * Size of serialized elements in [bytes]:
 		 * 
 		 * 		Signaling packet type = 1
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		SignalingMessageHrm tTest = new SignalingMessageHrm();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of " + tTest.getClass().getSimpleName());
 		}
 		tResult += 1;
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns if this packet type has a dynamic size
 	 * 
 	 * @return true or false
 	 */
 	public static boolean hasDynamicSize()
 	{
 		return false;
 	}
 	
 	/**
 	 * Returns the counter of created packets from this type
 	 *  
 	 * @return the packet counter
 	 */
 	public static long getCreatedPackets()
 	{
 		long tResult = 0;
 		
 		synchronized (sCreatedPackets) {
 			tResult = sCreatedPackets;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "/" + getOriginalMessageNumber() + "](Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ")";
 	}
 }
