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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.ISignalingMessageHrmBroadcastable;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * PACKET: This packet is used within the HRM infrastructure in order to tell other clusters about the invalidation of a remote cluster.
  *         Such information is needed for coordinators, which can use this information in order to update their higher clusters. 
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
  *                                                               
  * ****************************************************************************************************************************
  * ****************************************************************************************************************************
 */
 public class InvalidCoordinator extends SignalingMessageHrm implements ISignalingMessageHrmBroadcastable
 {
 	private static final long serialVersionUID = -1548886959657058300L;
 
 	/**
 	 * Stores the ClusterName of the sender
 	 */
 	private ClusterName mSenderClusterName = null;
 	
 	/**
 	 * Stores the L2 address of the node where the coordinator of the announced cluster is located
 	 */
 	private L2Address mCoordinatorNodeL2Address = null;
 	
 	/**
 	 * Stores the current TTL value. If it reaches 0, the packet will be dropped
 	 */
 	private int mTTL = HRMConfig.Hierarchy.EXPANSION_RADIUS;
 	
 	/**
 	 * Stores if the packet is still forward top-downward or sidewards
 	 */
 	private boolean mEnteredSidewardForwarding = false;
 
 	/**
 	 * Stores the passed clusters
 	 */
 	private LinkedList<Long> mPassedClusters = new LinkedList<Long>();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pSenderClusterName the ClusterName of the sender
 	 * @param pCoordinatorNodeL2Address the L2 address of the node where the coordinator is located
 	 */
 	public InvalidCoordinator(Name pSenderName, ClusterName pSenderClusterName, L2Address pCoordinatorNodeL2Address)
 	{
 		super(pSenderName, HRMID.createBroadcast());
 		
 		mSenderClusterName = pSenderClusterName;
 		mCoordinatorNodeL2Address = pCoordinatorNodeL2Address;
 	}
 	
 	/**
 	 * Record the passed clusters
 	 * 
 	 * @param pClusterID the unique ID of the passed cluster
 	 */
 	public void addPassedCluster(Long pClusterID)
 	{
 		synchronized (mPassedClusters) {
 			mPassedClusters.add(pClusterID);
 		}
 	}
 
 	/**
 	 * Checks if a cluster was already passed
 	 * 
 	 * @param pClusterID the unique ID of the passed cluster
 	 */
 	public boolean hasPassedCluster(Long pClusterID)
 	{
 		boolean tResult = false;
 		
 		synchronized (mPassedClusters) {
 			tResult = mPassedClusters.contains(pClusterID);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the ClusterName of the sender
 	 * 
 	 * @return
 	 */
 	public ClusterName getSenderClusterName()
 	{
 		return mSenderClusterName;
 	}
 	
 	/**
 	 * Returns the L2 address of the node where the coordinator of the announced cluster is located
 	 * 
 	 * @return the L2 address
 	 */
 	public L2Address getSenderClusterCoordinatorNodeL2Address()
 	{
 		return mCoordinatorNodeL2Address;
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
 	 * Decreases the TTL value by one 
 	 */
 	public void decreaseTTL()
 	{
 		mTTL--;
 	}
 	
 	/**
 	 * Returns true if the TTL is still okay
 	 * 
 	 * @return true or false
 	 */
 	public boolean isTTLOkay()
 	{
 		/**
 		 * Return always true for the highest hierarchy level
 		 */
 		if(getSenderClusterName().getHierarchyLevel().isHighest()){
 			return true;
 		}
 
 		/**
 		 * Return always true for the second highest hierarchy level
 		 */
 		if(getSenderClusterName().getHierarchyLevel().getValue() == HRMConfig.Hierarchy.HEIGHT -2){
 			return true;
 		}
 		
 		/**
 		 * Return true depending on the TTL value
 		 */
 		return (mTTL > 0);
 	}
 	
 	/**
 	 * Returns the current TTL value
 	 * 
 	 * @return the TTL value
 	 */
 	public int getTTL()
 	{
 		return mTTL;
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
 		InvalidCoordinator tResult = new InvalidCoordinator(getSenderName(), getSenderClusterName(), getSenderClusterCoordinatorNodeL2Address());
 		
 		super.duplicate(tResult);
 
 		// update TTL
 		tResult.mTTL = getTTL();
 		
 		// update "sideward forwarding" marker
 		tResult.mEnteredSidewardForwarding = enteredSidewardForwarding();
 
 		// update the recorded cluster ID
 		tResult.mPassedClusters = (LinkedList<Long>) mPassedClusters.clone();
 				
 		//Logging.log(this, "Created duplicate packet: " + tResult);
 		
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
		return getClass().getSimpleName() + "[" + getMessageNumber() + "/" + getOriginalMessageNumber() + "](Sender=" + getSenderName() + ", Receiver=" + getReceiverName() + ", TTL=" + getTTL() + ", SenderCluster="+ getSenderClusterName() + ")";
 	}
 }
