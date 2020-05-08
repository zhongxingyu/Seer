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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 
 /**
  * PACKET: This packet is used within the HRM infrastructure in order to tell other clusters about the existence of a remote cluster.
  *         Such information is needed for coordinators, which can use this information in order to create a new higher cluster with the coordinators of announce neighbor clusters. 
  */
 public class AnnounceCluster extends SignalingMessageHrm
 {
	private static final long serialVersionUID = -1548886959657058300L;

 	/**
 	 * Stores the ClusterName of the sender
 	 */
 	private ClusterName mSenderClusterName = null;
 	
 	/**
 	 * Stores the name of the node where the coordinator of the announced cluster is located
 	 */
 	private Name mCoordinatorNodeName = null;
 	
 	/**
 	 * Stores the current TTL value. If it reaches 0, the packet will be dropped
 	 */
 	private int mTTL = HRMConfig.Hierarchy.EXPANSION_RADIUS;
 	
 	/**
 	 * Stores the logical hop count for the stored route 
 	 */
 	private int mRouteCosts = 0;
 	
 	/**
 	 * Stores the route to the announced cluster
 	 */
 	private Route mRoute = new Route();
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pSenderClusterName the ClusterName of the sender
 	 * @param pCoordinatorNodeName the name of the node where the coordinator of the announced cluster is located
 	 */
 	public AnnounceCluster(Name pSenderName, ClusterName pSenderClusterName, Name pCoordinatorNodeName)
 	{
 		super(pSenderName, HRMID.createBroadcast());
 		
 		mSenderClusterName = pSenderClusterName;
 		mCoordinatorNodeName = pCoordinatorNodeName;
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
 	 * Returns the name of the node where the coordinator of the announced cluster is located
 	 * 
 	 * @return the node name
 	 */
 	public Name getSenderClusterCoordinatorNodeName()
 	{
 		return mCoordinatorNodeName;
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
 		return (mTTL >= 0);
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
 	 * Adds an entry to the recorded route towards the announced cluster
 	 * 
 	 * @param pSegment the segment which should be added to the route
 	 */
 	public void addToRoute(RouteSegment pSegment)
 	{
 		mRoute.add(pSegment);
 	}
 	
 	/**
 	 * Returns the costs for the route to the announced cluster
 	 * 
 	 * @return the route costs
 	 */
 	public int getRouteCosts()
 	{
 		return mRouteCosts;
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
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "](Sender=" + getSenderName() + ", Receiver=" + getReceiverName() + ", TTL=" + getTTL() + ", SenderCluster="+ getSenderClusterName() + ")";
 	}
 }
