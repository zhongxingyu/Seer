 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical.topology;
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.bus.Bus;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.transfer.gates.headers.ProtocolHeader;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Size;
 
 /**
  * PACKET: This packet is used to inform the neighbor about the association between the central FN and the FN between the central FN and the bus.
  * 		   It is necessary to inform the neighbor about the FN which it should use to route to the central FN of a neighbor node.
  * 		   This packet part is only needed for the FoG specific implementation. It is not mandatory for HRM.
  * 
  * 		   Additionally, this packet is used to inform the peer about the local AS number.
  */
 public class AnnouncePhysicalEndPoint extends SignalingMessageHrm implements Serializable, ProtocolHeader, IEthernetPayload
 {
 	/**
 	 * Stores the L2Address of the central FN of the sender
 	 */
 	private L2Address mSenderCentralFN = new L2Address(0);
 	
 	/**
 	 * Stores the AsID of the sender
 	 */
 	private Long mSenderAsID = new Long(-1);
 	
 	/**
 	 * Stores the L2Address of the FN which should be used as routing target 
 	 */
 	private L2Address mRoutingTargetFN = new L2Address(0);
 
 	/**
 	 * Stores if this packet is an answer to another packet of this type
 	 */
 	private boolean mIsAnswer = false;
 
 	public static final boolean INIT_PACKET = false;
 	public static final boolean ANSWER_PACKET = true;
 	public static Long sCreatedPackets = new Long(0);
 	private static final long serialVersionUID = 7253912074438961613L;
 
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	private AnnouncePhysicalEndPoint()
 	{
 		
 	}
 	
 	/**
 	 * Constructor 
 	 * 
 	 * @param pSenderAsID the AsID of the sender
 	 * @param pSenderCentralFN the central FN of the sender
 	 * @param pRoutingTargetFN the name of the first FN, which should be used for routing from the receiver towards the sender
 	 * @param pIsAnswer true if this packet is answer to a previous AnnouncePhysicalEndPoint packet
 	 */
 	public AnnouncePhysicalEndPoint(Long pSenderAsID, L2Address pSenderCentralFN, L2Address pRoutingTargetFN, boolean pIsAnswer)
 	{
 		mSenderCentralFN = pSenderCentralFN;
 		mRoutingTargetFN = pRoutingTargetFN;
 		mSenderAsID = pSenderAsID;
 		mIsAnswer = pIsAnswer;
 		synchronized (sCreatedPackets) {
 			sCreatedPackets++;
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 			Logging.log(getClass().getSimpleName() + "(SenderCentralAddress=" + getSenderCentralAddress()  + ", SenderAddress=" + getSenderAddress() + "): CREATED");
 		}
 	}
 	
 	/**
 	 * Returns if this packet is an answer to another packet of this type
 	 * 
 	 * @return true if it is an answer, otherwise false
 	 */
 	public boolean isAnswer()
 	{
 		return mIsAnswer;
 	}
 	
 	/**
 	 * Returns the name of the central FN
 	 * 
 	 * @return name of the central FN
 	 */
 	public L2Address getSenderCentralAddress()
 	{
 		return mSenderCentralFN;
 	}
 
 	/**
 	 * Returns the AsID of the sender
 	 * 
 	 * @return the AsID of the sender
 	 */
 	public Long getSenderAsID()
 	{
 		return mSenderAsID;
 	}
 
 	/**
 	 * Determine the name of the FN, which should be used as routing target in case the central FN should be reached
 	 * 
 	 * @return name of the FN
 	 */
 	public L2Address getSenderAddress()
 	{
 		return mRoutingTargetFN;
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
 		 * 		[SignalingMessageHrm]
 		 * 		CentralFN				= 16
 		 * 		RoutingTargetFN			= 16
 		 * 		SenderAsID				= 4
 		 * 		IsAnswer				= 1
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
 		 * 		CentralFN				= 16
 		 * 		RoutingTargetFN			= 16
 		 * 		SenderAsID				= 4
 		 * 		IsAnswer				= 1
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		AnnouncePhysicalEndPoint tTest = new AnnouncePhysicalEndPoint();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of " + tTest.getClass().getSimpleName());
 		}
 		tResult += tTest.mSenderCentralFN.getSerialisedSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += tTest.mRoutingTargetFN.getSerialisedSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += 4; // SenderAsID: use only 4 bytes here
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += Size.sizeOf(tTest.mIsAnswer);
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
 	 * Accounts link usage
 	 * 
 	 * @param pLink the link which is used
 	 */
 	@Override
 	public void accountLinkUsage(ILowerLayer pLink)
 	{
 		if(pLink instanceof Bus){
 			Bus tBus = (Bus)pLink;
 			
 			HRMController.accountPacket(tBus, this);
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
 		return getClass().getSimpleName() + " (" + (isAnswer() ? "ANSWER" : "INIT") + ", CentralFN=" + getSenderCentralAddress()  + ", RoutingTargetFN=" + getSenderAddress() + ")";
 	}
 }
