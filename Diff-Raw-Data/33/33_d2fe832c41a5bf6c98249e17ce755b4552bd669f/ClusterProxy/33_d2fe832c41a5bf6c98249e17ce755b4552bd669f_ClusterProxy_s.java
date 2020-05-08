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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnounceRemoteCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used when a remote cluster is detected. It includes all needed data about the remote cluster.
  */
 public class ClusterProxy extends ControlEntity implements ICluster
 {
 	private static final long serialVersionUID = -8746079632866375924L;
 
 	/**
 	 * Stores the reference to the local HRMController instance.
 	 */
 	private HRMController mHRMController;
 
 	private Long mClusterID;
 
 	private Name mCoordName;
 	
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pCoordName
 	 * @param pCoordinatorID
 	 */
 	public ClusterProxy(HRMController pHRMController, Long pClusterID, HierarchyLevel pHierarchyLevel, Name pCoordName, int pCoordinatorID)
 	{	
 		super(pHRMController, pHierarchyLevel);
 
 		mClusterID = pClusterID;
 		mHRMController = pHRMController;
 
 		mCoordName = pCoordName;
 		
 		setSuperiorCoordinatorID(pCoordinatorID);
 		setCoordinatorID(pCoordinatorID);
 		
 		// register the ClusterProxy at the local ARG
 		mHRMController.registerNodeARG(this);
 
 		Logging.log(this, "CREATED");
 	}
 
 	/**
 	 * Determines the physical simulation machine specific ClusterID multiplier
 	 * 
 	 * @return the generated multiplier
 	 */
 	private long clusterIDMachineMultiplier()
 	{
 		//TODO: get this value from the signaling
 		return 1;
 	}
 
 	/**
 	 * Returns the full ClusterID (including the machine specific multiplier)
 	 * 
 	 *  @return the full ClusterID
 	 */
 	public Long getClusterID()
 	{
 		return mClusterID;
 	}
 
 	/**
 	 * Returns the machine-local ClusterID (excluding the machine specific multiplier)
 	 * 
 	 * @return the machine-local ClusterID
 	 */
 	public long getGUIClusterID()
 	{
 		return mClusterID / clusterIDMachineMultiplier();
 	}
 	
 	
 	
 	
 
 	public void handleNeighborAnnouncement(AnnounceRemoteCluster pAnnounce, ComChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				mHRMController.getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		Cluster tCluster = mHRMController.getClusterByID(new ClusterName(mHRMController, pAnnounce.getLevel(), pAnnounce.getToken(), pAnnounce.getClusterID()));
 		if(tCluster == null)
 		{
 			Logging.log(this, "     ..creating cluster proxy");
 			ClusterProxy tClusterProxy = new ClusterProxy(mHRMController, pAnnounce.getClusterID(), getHierarchyLevel(), pAnnounce.getCoordinatorName(), pAnnounce.getToken());
 			mHRMController.setSourceIntermediateCluster(tClusterProxy, mHRMController.getSourceIntermediateCluster(this));
 			tClusterProxy.setPriority(pAnnounce.getCoordinatorsPriority());
 			tClusterProxy.setSuperiorCoordinatorID(pAnnounce.getToken());
 			registerNeighborARG(tClusterProxy);
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 			registerNeighborARG(tCluster);
 		}
 		//((AttachedCluster)tCluster).setNegotiatingHost(pAnnounce.getAnnouncersAddress());
 
 		/*
 		 * function checks whether neighbor relation was established earlier
 		 */
 
 		if(pAnnounce.getCoordinatorName() != null) {
 			mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 		}
 	}
 
 	public void setSuperiorCoordinator(ComChannel pCoordinatorComChannel, Name pCoordinatorName, int pCoordToken, L2Address pCoordinatorL2Address)
 	{
 		Logging.warn(this, "############### This function should never be called #############");
 	}
 
 	public int hashCode()
 	{
 		return mClusterID.intValue() * 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.routing.hierarchical.management.ICluster#getCoordinatorName()
 	 */
 	@Override
	public Name getCoordinatorHostName()
 	{
 		return mCoordName;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.routing.hierarchical.management.ICluster#setCoordinatorName(de.tuilmenau.ics.fog.facade.Name)
 	 */
 	@Override
 	public void setCoordinatorHostName(Name pCoordName)
 	{
 		mCoordName = pCoordName;		
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	@SuppressWarnings("unused")
 	public String toString()
 	{
 		HRMID tHRMID = getHRMID();
 		
 		if(tHRMID != null && HRMConfig.Debugging.PRINT_HRMIDS_AS_CLUSTER_IDS) {
 			return tHRMID.toString();
 		} else {
 			return toLocation() + "(" + idToString() + ")";
 
 		}
 	}
 
 	/**
 	 * Returns a location description about this instance
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUIClusterID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns a string including the ClusterID, the token, and the node priority
 	 * 
 	 * @return the complex string
 	 */
 	private String idToString()
 	{
 		if (getHRMID() == null){
 			return "ID=" + getClusterID() + ", CoordID=" + superiorCoordinatorID() +  ", Prio=" + getPriority().getValue();
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 }
