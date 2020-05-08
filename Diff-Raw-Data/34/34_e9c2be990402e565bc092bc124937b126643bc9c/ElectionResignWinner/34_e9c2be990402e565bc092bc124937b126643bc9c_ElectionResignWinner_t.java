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
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.packets.hierarchical.ISignalingMessageHrmBroadcastable;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 
 /**
  * PACKET: It is used when a coordinator resigns and this fast is signaled to all cluster members.
  * 		   The packet has to be send as broadcast.
  * 		   This packet was introduced as add-on for the standard Bully algorithm in order to have a shorter convergence phase.
  */
 public class ElectionResignWinner extends SignalingMessageElection implements ISignalingMessageHrmBroadcastable
 {
 	private static final long serialVersionUID = 794175467972815277L;
 	
 	/**
 	 * Stores some GUI description about the resigning coordinator
 	 */
 	private String mCoordinatorDescription = null;
 	
 	/**
 	 * Stores the counter of created packets from this type
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	public static Long sCreatedPackets = new Long(0);
 
 	/**
 	 * Stores the counter of sent broadcasts from this type
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	public static Long sSentBroadcasts = new Long(0);
 
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	private ElectionResignWinner()
 	{
 		super();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender (coordinator)
 	 * @param pSenderPriority the priority of the message sender (coordinator)
 	 * @param pCoordinatorDescription a description text of the coordinator
 	 */
 	public ElectionResignWinner(HRMName pSenderName, ElectionPriority pSenderPriority, String pCoordinatorDescription)
 	{
 		super(pSenderName, HRMID.createBroadcast(), pSenderPriority);
 		mCoordinatorDescription = pCoordinatorDescription;
 		synchronized (sCreatedPackets) {
 			sCreatedPackets++;
 		}
 	}
 	
 	/**
 	 * Returns the descriptive string about the coordinator which announces it coordination
 	 * 
 	 * @return the descriptive string
 	 */
 	private String getCoordinatorDescription()
 	{
 		return new String(mCoordinatorDescription);
 	}
 	
 	/**
 	 * Returns a duplicate of this packet
 	 * 
 	 * @return the duplicate packet
 	 */
 	@Override
 	public SignalingMessageHrm duplicate()
 	{
 		ElectionResignWinner tResult = new ElectionResignWinner(getSenderName(), getSenderPriority(), getCoordinatorDescription());
 
 		super.duplicate(tResult);
 
 		//Logging.log(this, "Created duplicate packet: " + tResult);
 		
 		return tResult;
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
		 * 		[MultiplexHeader]
		 * 		[SignalingMessageHrm]
		 * 		[SignalingMessageElection]
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
		 * 		[MultiplexHeader]
		 * 		[SignalingMessageHrm]
		 * 		[SignalingMessageElection]
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		ElectionResignWinner tTest = new ElectionResignWinner();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of " + tTest.getClass().getSimpleName());
 		}
 		tResult += SignalingMessageElection.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		
 		return tResult;
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
 	 * Accounts a broadcast of this packet type
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.packets.hierarchical.ISignalingMessageHrmBroadcastable#accountBroadcast()
 	 */
 	@Override
 	public void accountBroadcast()
 	{
 		super.accountBroadcast();
 		synchronized (sCreatedPackets) {
 			sCreatedPackets--;
 			sSentBroadcasts++;
 		}
 	}
 
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "/" + getOriginalMessageNumber() + "](Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ", SenderPrio=" + getSenderPriority().getValue() + ", Coordinator=" + mCoordinatorDescription + ")";
 	}
 }
