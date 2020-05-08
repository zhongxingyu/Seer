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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.packets.hierarchical.ISignalingMessageHrmBroadcastable;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Size;
 
 /**
  * PACKET: This packet is used within the HRM infrastructure in order to tell other clusters about the existence of a remote cluster.
  *         Such information is needed for coordinators, which can use this information in order to create a new higher cluster with the coordinators of announce neighbor clusters. 
  *
  * ****************************************************************************************************************************
  * ************************ Explanation how such a packet is forwarded within the HRM infrastructure  *************************
  * ****************************************************************************************************************************
  * 
  *                      "1. towards the bottom of the hierarchy" 
  *
  *                                      +-------+
  *                    +---------------- |Coord.2| ---------------+
  *                    |                 +-------+                |
  *                    |                                          |
  *                    |                                          |
  *                   \|/                                        \|/
  *                +-------+                                 +-------+
  *           +--- |Coord.1| ---+                       +--- |Coord.1| ---+
  *           |    +-------+    |                       |    +-------+    |
  *           |                 |                       |                 |
  *          \|/               \|/                     \|/               \|/
  *       +-------+         +-------+               +-------+         +-------+
  *       |Coord.0|         |Coord.0|               |Coord.0|         |Coord.0|
  *       +-------+         +-------+               +-------+         +-------+
  *           |                 |                       |                 |
  *           |                 |                       |                 |
  *          \|/               \|/                     \|/               \|/
  *     /==========\       /==========\           /==========\       /==========\
  *     |L0 cluster|       |L0 cluster|           |L0 cluster|       |L0 cluster|
  *     \==========/       \==========/           \==========/       \==========/  
  *
  * 
  * 
  *                              "2. towards the side"
  *     /==========\       /==========\           /==========\       /==========\
  *     |L0 cluster| <---> |L0 cluster| <-------> |L0 cluster| <---> |L0 cluster|
  *     \==========/       \==========/           \==========/       \==========/
  *       
  *                               
  * HINT: Assumption: each L0 coordinator knows to which L1+ clusters it belongs.
  * 
  * HINT (TTL): TTL handling: Based on the previous assumption, each L0 cluster is able to decide if a new logical hop is passed 
  *             when forwarding such packets within level 0. As a result of this, the TTL value can be automatically decreased if
  *             a new logical hop is entered 
  *                                
  * HINT (max. hierarchy level): Level 0 cluster don't have to distribute announces from the coordinator at the maximum hierarchy 
  *                              level beyond the abstract borders of the cluster at maximum hierarchy. Each node gets this information 
  *                              from its superior coordinators. There is not additional node which still needs this information 
  *                              forwarded from the side. Otherwise, we would have an isolated node which doesn't belong to the
  *                              HRM infrastructure. 
  *                                                               
  * ****************************************************************************************************************************
  * ****************************************************************************************************************************
 */
 public class AnnounceCoordinator extends SignalingMessageHrmTopologyUpdate implements ISignalingMessageHrmBroadcastable, ISignalingMessageHrmTopologyASSeparator
 {
 	private static final long serialVersionUID = -1548886959657058300L;
 
 	/**
 	 * Time to announce: stores the current "TTL value". If it reaches 0, the packet will be dropped
 	 */
 	private long mTTA = HRMConfig.Hierarchy.RADIUS;
 	
 	/**
 	 * Stores the logical hop count for the stored route 
 	 */
 	private int mRouteHopCount = 0;
 	
 	/**
 	 * Stores the route to the announced cluster
 	 * This value is FoG-specific and eases the implementation. The recorded L2Address values of the passed nodes (variable "mPassedNodes") are enough to determine a valid route to the sending coordinator. 
 	 */
 	private Route mRoute = new Route();
 	
 	/**
 	 * Stores if the packet is still forward top-downward or sidewards
 	 */
 	private boolean mEnteredSidewardForwarding = false;
 	
 	/**
 	 * Stores the passed clusters for the GUI
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private LinkedList<Long> mGUIPassedClusters = new LinkedList<Long>();
 	
 	/**
 	 * Stores the passed node
 	 */
 	private LinkedList<L2Address> mPassedNodes = new LinkedList<L2Address>();
 
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
 	 * Defines if packet tracking is active
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	private boolean mPacketTracking = false;
 	
 	/**
 	 * Defines the lifetime of this announcement in [s]. Allowed values are between 0 and 255.
 	 */
 	private double mLifetime = 0;
 	
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	private AnnounceCoordinator()
 	{
 		super();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController the HRMController instance
 	 * @param pSenderName the name of the message sender
 	 * @param pSenderClusterName the ClusterName of the sender
 	 * @param pCoordinatorNodeL2Address the L2 address of the node where the coordinator is located
 	 */
 	public AnnounceCoordinator(HRMController pHRMController, HRMName pSenderName, ClusterName pSenderClusterName, L2Address pCoordinatorNodeL2Address)
 	{
 		super(pSenderName, HRMID.createBroadcast());
 		
 		mLifetime = calcLifetime(pHRMController);
 		
 		setSenderEntityName(pSenderClusterName);
 
 		setSenderEntityNodeL2Address(pCoordinatorNodeL2Address);
 		
 		if(pHRMController != null){
 			/**
 			 * Record the sender node
 			 */
 			addPassedNode(pHRMController.getNodeL2Address());
 		
 			/**
 			 * Record the passed clusters
 			 */
 			addGUIPassedCluster(new Long(pSenderClusterName.getGUIClusterID()));
 		}
 		synchronized (sCreatedPackets) {
 			sCreatedPackets++;
 		}
 	}
 	
 	private double calcLifetime(HRMController pHRMController)
 	{
		double tResult = 2 * HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL + HRMConfig.Hierarchy.MAX_E2E_DELAY; 
 
 		if((pHRMController != null) && (pHRMController.getTimeWithStableHierarchy() > HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL_HIERARCHY_INIT_TIME)){
 			//Logging.err(this, "Using higher lifetime here");
			tResult = 2 * HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL_STABLE_HIERARCHY +  + HRMConfig.Hierarchy.MAX_E2E_DELAY;
 		}
 
 		return tResult;
 	}
 	
 	/**
 	 * Record the passed clusters
 	 * 
 	 * @param pClusterID the unique ID of the passed cluster
 	 */
 	public void addGUIPassedCluster(Long pClusterID)
 	{
 		synchronized (mGUIPassedClusters) {
 			mGUIPassedClusters.add(pClusterID);
 		}
 	}
 
 	/**
 	 * Returns a list of passed clusters
 	 * 
 	 * @return the list of passed clusters (their IDs)
 	 */
 	public String getGUIPassedClusters()
 	{
 		String tResult = "";
 		
 		synchronized (mGUIPassedClusters) {
 			for(Long tPassedCluster : mGUIPassedClusters){
 				tResult += " " + Long.toString(tPassedCluster);
 			}
 		}
 
 		return tResult;
 	}
 	
 	/**
 	 * Record the passed nodes
 	 * 
 	 * @param pNode the unique ID of the passed node
 	 */
 	public void addPassedNode(L2Address pNode)
 	{
 		synchronized (mPassedNodes) {
 			mPassedNodes.add(pNode);
 		}
 	}
 
 	/**
 	 * Checks if a cluster was already passed
 	 * 
 	 * @param pNode the unique ID of the passed node
 	 */
 	public boolean hasPassedNode(L2Address pNode)
 	{
 		boolean tResult = false;
 		
 		synchronized (mPassedNodes) {
 			tResult = mPassedNodes.contains(pNode);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns a list of passed nodes
 	 * 
 	 * @return the list of passed nodes
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<L2Address> getPassedNodes()
 	{
 		LinkedList<L2Address> tResult = null;
 		
 		synchronized (mPassedNodes) {
 			tResult = (LinkedList<L2Address>) mPassedNodes.clone();
 		}
 		
 		return tResult; 
 	}
 	
 	/**
 	 * Returns a list of passed nodes
 	 * 
 	 * @return the list of passed nodes
 	 */
 	public String getPassedNodesStr()
 	{
 		String tResult = "";
 		
 		synchronized (mPassedNodes) {
 			for(L2Address tPassedNode : mPassedNodes){
 				tResult += " " + tPassedNode;
 			}
 		}
 
 		return tResult;
 	}
 	
 	/**
 	 * Returns the lifetime of this announcement
 	 * 
 	 * @return the lifetime
 	 */
 	public double getLifetime()
 	{
 		return mLifetime;
 	}
 	
 	/**
 	 * Returns if the sideward forwarding was already started
 	 * 
 	 * @return true or false
 	 */
 	public boolean enteredSidewardForwarding()
 	{
 		return mEnteredSidewardForwarding;
 	}
 	
 	/**
 	 * Marks this packet as currently in sideward forwarding
 	 */
 	public void setSidewardForwarding()
 	{
 		mEnteredSidewardForwarding = true;	
 	}
 	
 	/**
 	 * Increase hop count (decreases the TTL value by one) 
 	 */
 	public void incHopCount()
 	{
 		mTTA--;
 	}
 	
 	/**
 	 * Returns true if the TTA is still okay
 	 * 
 	 * @return true or false
 	 */
 	public boolean isTTAOkay()
 	{
 		/**
 		 * Return always true for the highest hierarchy level, but on this hierarchy level no announces should be sent
 		 */
 		if(getSenderEntityName().getHierarchyLevel().isHighest()){
 			return true;
 		}
 
 		/**
 		 * Return always true for the second highest hierarchy level
 		 */
 		if(getSenderEntityName().getHierarchyLevel().getValue() == HRMConfig.Hierarchy.HEIGHT -2){
 			return true;
 		}
 		
 		/**
 		 * Return true depending on the TTA value
 		 */
 		return (mTTA > 0);
 	}
 	
 	/**
 	 * Checks if the next AS may be entered by this packet
 	 * 
 	 * @param pHRMController the current HRMController instance
 	 * @param the AsID of the next AS
 	 * 
 	 * @return true or false
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.packets.hierarchical.topology.ISignalingMessageASSeparator#isAllowedToEnterAs(de.tuilmenau.ics.fog.routing.hierarchical.HRMController, java.lang.Long)
 	 */
 	@Override
 	public boolean isAllowedToEnterAs(HRMController pHRMController,	Long pNextAsID)
 	{
 		/**
 		 * Return always true for the highest hierarchy level
 		 */
 		if(getSenderEntityName().getHierarchyLevel().getValue() >= HRMConfig.Hierarchy.HEIGHT - 2){
 			return true;
 		}
 
 		/**
 		 * Return true if the given AsID describes the current AS
 		 */
 		if(pHRMController.getAsID().equals(pNextAsID)){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Adds an entry to the recorded route towards the announced cluster
 	 * 
 	 * @param pRoute the partial route which should be added to the route
 	 */
 	public void addRouteHop(Route pRoute)
 	{
 		if(pRoute != null){
 			increaseRouteHopCount();
 			
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "Adding route head");
 				Logging.log(this, "      ..old route to sender: " + mRoute);
 			}
 			Route tNewRoute = pRoute.clone();
 			tNewRoute.add(mRoute);
 			mRoute = tNewRoute;
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "      ..new route to sender: " + mRoute);
 			}
 		}else{
 			Logging.warn(this, "Cannot add an invalid route head");
 		}
 	}
 	
 	/**
 	 * Returns the costs for the route to the announced cluster
 	 * 
 	 * @return the route costs
 	 */
 	public int getRouteHopCount()
 	{
 		return mRouteHopCount;
 	}
 	
 	/**
 	 * Increases the hop count for this route
 	 */
 	private void increaseRouteHopCount()
 	{
 		mRouteHopCount++;
 	}
 	
 	/**
 	 * Returns the route to the announced cluster.
 	 * 
 	 * @return the route
 	 */
 	public Route getRoute()
 	{
 		return mRoute.clone();
 	}
 	
 	/**
 	 * Returns a duplicate of this packet
 	 * 
 	 * @return the duplicate packet
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public SignalingMessageHrm duplicate()
 	{
 		AnnounceCoordinator tResult = new AnnounceCoordinator(null, getSenderName(), getSenderEntityName(), getSenderEntityNodeL2Address());
 		
 		super.duplicate(tResult);
 
 		// update TTL
 		tResult.mTTA = mTTA;
 		
 		// update the route to the announced cluster
 		tResult.mRoute = getRoute();
 		
 		// update the route hop costs 
 		tResult.mRouteHopCount = getRouteHopCount();
 		
 		// update "sideward forwarding" marker
 		tResult.mEnteredSidewardForwarding = enteredSidewardForwarding();
 		
 		// add an entry to the recorded source route
 		tResult.addSourceRoute("[route]: (" + mRoute + ") -> (" + tResult.mRoute + ")");
 
 		// update the recorded cluster ID
 		tResult.mGUIPassedClusters = (LinkedList<Long>) mGUIPassedClusters.clone();
 
 		// update the recorded nodes
 		tResult.mPassedNodes = (LinkedList<L2Address>) mPassedNodes.clone();
 
 		// packet tracking
 		tResult.mPacketTracking = mPacketTracking;
 		
 		// lifetime value
 		tResult.mLifetime = mLifetime;
 		
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
 		 * 		[SignalingMessageHrm]
 		 * 		[SignalingMessageHrmTopologyUpdate]
 		 * 		TTL					     	= 2
 		 * 		Lifetime					= 1
 		 * 		RouteHopCount 			 	= 2
 		 * 		EnteredSidewardForwarding 	= 1
 		 * 		PassedNodes.length    	 	= 1
 		 * 		PassedNodes				 	= dynamic
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		tResult += getDefaultSize();
 		tResult += 1; // size of the following list
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += (mPassedNodes.size() * new L2Address(0).getSerialisedSize());
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		
 		return tResult;
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
 		 * 		[SignalingMessageHrm]
 		 * 		[SignalingMessageHrmTopologyUpdate]
 		 * 		TTL					     	= 2
 		 * 		Lifetime					= 1
 		 *		RouteHopCount 			 	= 2
 		 *		EnteredSidewardForwarding 	= 1
 		 *
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		AnnounceCoordinator tTest = new AnnounceCoordinator();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of " + tTest.getClass().getSimpleName());
 		}
 		tResult += SignalingMessageHrmTopologyUpdate.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += 2; // TTL: use only 2 bytes here
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += 1; // Lifetime: use only 1 byte here
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += 2; // RouteHopCount: use only 2 bytes here
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += Size.sizeOf(tTest.mEnteredSidewardForwarding);
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
 		return true;
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
 	 * Activates packet tracking
 	 */
 	public void activateTracking()
 	{
 		mPacketTracking = true;		
 	}
 
 	/**
 	 * Returns if packet tracking is active
 	 */
 	public boolean isPacketTracking()
 	{
 		return mPacketTracking;
 	}
 	
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "/" + getOriginalMessageNumber() + "](Sender=" + getSenderName() + ", Receiver=" + getReceiverName() + ", TTL=" + mTTA + ", SenderCluster="+ getSenderEntityName() + ")";
 	}
 }
