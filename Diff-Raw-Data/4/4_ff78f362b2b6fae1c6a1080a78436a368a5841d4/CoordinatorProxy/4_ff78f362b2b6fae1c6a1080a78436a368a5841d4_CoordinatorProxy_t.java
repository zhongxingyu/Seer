 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster member (can also be a cluster head).
  */
 public class CoordinatorProxy extends ClusterMember
 {
 	private static final long serialVersionUID = 5456243840140110970L;
 
 	/**
 	 * Stores the hop distance to the coordinator node.
 	 */
 	private int mDistance = -1;
 	
 	/**
 	 * Stores the L2 address of the node where the coordinator is located
 	 */
 	private L2Address mCoordinatorNodeL2Address = null;
 	
 	/**
 	 * Defines which priority value is reported
 	 */
 	private final long PRIORITY = 0;
 	
 	/**
 	 * Stores the timeout of this proxy
 	 */
 	private double mTimeout = 0;
 	
 	/**
 	 * Stores the last received AnnounceCoordinator packet
 	 */
 	private AnnounceCoordinator mLastAnnounceCoordinator = null;
 	
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pCoordinatorID the unique coordinator ID for this cluster
 	 * @param pCoordinatorNodeL2Address the node L2 address where the coordinator is located
 	 * @param pHopCount the hop count to the coordinator node
 	 */
 	private CoordinatorProxy(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID, long pCoordinatorID, L2Address pCoordinatorNodeL2Address, int pHopCount)
 	{	
 		super(pHRMController, pHierarchyLevel, pClusterID, pCoordinatorID, pCoordinatorNodeL2Address);
 
 		mDistance = pHopCount;
 		
 		// store the L2 address of the node where the coordinator is located
 		mCoordinatorNodeL2Address = pCoordinatorNodeL2Address;
 
 		//Logging.log(this, "CREATED");
 	}
 
 	/**
 	 * Factory function
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pClusterName a ClusterName which includes the hierarchy level, the unique ID of this cluster, and the unique coordinator ID
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pCoordinatorNodeL2Address the node L2 address where the coordinator is located
 	 * @param pHopCount the hop count to the coordinator node
 	 */
 	public synchronized static CoordinatorProxy create(HRMController pHRMController, ClusterName pClusterName, L2Address pCoordinatorNodeL2Address, int pHopCount)
 	{	
 		CoordinatorProxy tResult = new CoordinatorProxy(pHRMController, pClusterName.getHierarchyLevel(), pClusterName.getClusterID(), pClusterName.getCoordinatorID(), pCoordinatorNodeL2Address, pHopCount);
 		
 		Logging.log(tResult, "\n\n\n################ CREATED COORDINATOR PROXY at hierarchy level: " + (tResult.getHierarchyLevel().getValue()));
 
 		// register at HRMController's internal database
 		pHRMController.registerCoordinatorProxy(tResult);
 
 		return tResult;
 	}
 
 	/**
 	 * EVENT: remote coordinator role invalid, triggered by ControlEntity::unregisterAnnouncedCoordinatorARG() if a coordinator invalidation is received, the reaction is:
 	 * 	 	1.) update the local ARG
 	 */
 	public synchronized void eventRemoteCoordinatorRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: Coordinator_Role_Invalid");
 
 		if(isThisEntityValid()){
 			// trigger invalidation
 			eventInvalidation();
 			
 			// register at HRMController's internal database
 			mHRMController.unregisterCoordinatorProxy(this);
 		}else{
 			Logging.warn(this, "This CoordinatorProxy is already invalid");
 		}
 	}
 	
 	/**
 	 * Returns the Election priority of this node for this cluster
 	 * 
 	 * @return the Election priority
 	 */
 	@Override
 	public ElectionPriority getPriority()
 	{
 		return ElectionPriority.create(this, PRIORITY);
 	}
 
 	/**
 	 * Creates a ClusterName object which describes this coordinator
 	 * 
 	 * @return the new ClusterName object
 	 */
 	public ClusterName createCoordinatorName()
 	{
 		ClusterName tResult = null;
 		
 		tResult = new ClusterName(mHRMController, getHierarchyLevel(), getClusterID(), getCoordinatorID());
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the L2 address of the node where the coordinator is located
 	 * 
 	 * @return the L2 address
 	 */
 	public L2Address getCoordinatorNodeL2Address()
 	{
 		return mCoordinatorNodeL2Address; 
 	}
 
 	/**
 	 * Refreshes this proxy and resets the timeout
 	 * 
 	 * @param pAnnounceCoordinatorPacket the current announce packet
 	 */
 	public void refresh(AnnounceCoordinator pAnnounceCoordinatorPacket)
 	{
 		mLastAnnounceCoordinator = (AnnounceCoordinator)pAnnounceCoordinatorPacket.duplicate();
 		
 		mTimeout = mHRMController.getSimulationTime() + HRMConfig.Hierarchy.COORDINATOR_TIMEOUT;
 	}
 	
 	/**
 	 * Returns the last received AnnounceCoordinator packet
 	 *  
 	 * @return the last AnnounceCoordinator packet
 	 */
 	public AnnounceCoordinator getLastRefresh()
 	{
 		return mLastAnnounceCoordinator;
 	}
 	
 	/**
 	 * Returns the timeout of this proxy
 	 * 
 	 * @return the timeout
 	 */
 	public double getTimeout()
 	{
 		return mTimeout;
 	}
 
 	/**
 	 * Returns if this proxy is obsolete due refresh timeout
 	 * 
 	 * @return true or false
 	 */
 	public boolean isObsolete()
 	{
 		boolean tResult = false;
 		
 		if(getTimeout() > 0){
 			// timeout occurred?
 			if(getTimeout() < mHRMController.getSimulationTime()){
 				tResult = true;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Sets a new distance (hop count to the coordinator node)
 	 * This function has to be synchronized in order to avoid concurrent manipulations of the internal distance value.
 	 * 
 	 * @param pDistance the new distance
 	 */
 	public synchronized void setDistance(int pDistance)
 	{
 		if (mDistance != pDistance){
 			/**
 			 * Update the base node priority
 			 */
 			// are we at base hierarchy level
 //			if(getHierarchyLevel().isBaseLevel()){
 				// distance is the init. value?
 				if(mDistance != -1){
 					// decrease base node priority
 					mHRMController.decreaseHierarchyNodePriority_KnownCoordinator(this);
 				}
 	
 				Logging.log(this, "Updating the distance (hop count) to the coordinator node from: " + mDistance + " to: " + pDistance);
 				mDistance = pDistance;
 
 				// increase base node priority
 				//HINT: this step is atomic with the previous "decreasing" step because this function is marked with "synchronized"
 				mHRMController.increaseHierarchyNodePriority_KnownCoordinator(this);
 //			}else{
 //				Logging.log(this, "Updating the distance (hop count) to the coordinator node from: " + mDistance + " to: " + pDistance);
 //				mDistance = pDistance;
 //			}
 			
 		}else{
 			// old value == new value
 		}
 	}
 	
 	/**
 	 * Returns the hop distance to the coordinator
	 * This function may not be synchronized. Otherwise, a deadlock can occur.
 	 * 
 	 * @return the hop distance
 	 */
	public int getDistance()
 	{
 		return mDistance;
 	}
 
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return "RemoteCoordinator" + getGUICoordinatorID() + "@" + getHierarchyLevel().getValue() +  "(hops=" + mDistance + ", " + idToString() + ")";
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation() + "(" + idToString() + ")";
 	}
 
 	/**
 	 * Returns a location description about this instance
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + (getHierarchyLevel().getValue());
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns a string including the ClusterID, the token, and the node priority
 	 * 
 	 * @return the complex string
 	 */
 	private String idToString()
 	{
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return "Cluster" + getGUIClusterID() + ", Node.=" + getCoordinatorNodeL2Address();
 		}else{
 			return "Cluster" + getGUIClusterID() + ", Node.=" + getCoordinatorNodeL2Address() + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
