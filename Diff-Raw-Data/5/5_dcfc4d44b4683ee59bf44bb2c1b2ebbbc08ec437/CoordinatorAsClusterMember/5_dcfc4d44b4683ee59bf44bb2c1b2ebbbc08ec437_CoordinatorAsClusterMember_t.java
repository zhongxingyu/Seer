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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster member (can also be a cluster head).
  */
 public class CoordinatorAsClusterMember extends ClusterMember
 {
 	private static final long serialVersionUID = -8746079632866375924L;
 
 	/**
 	 * Stores the coordinator for which this cluster membership was created 
 	 */
 	private Coordinator mCoordinator = null;
 
 	/**
 	 * Stores if the membership is active
 	 */
 	private boolean mMembershipIsActive = false;
 	
 	/**
 	 * Stores the remote ClusterName.
 	 * This value is used to make this instance unique and identify the correct remote side.
 	 */
 	private ClusterName mRemoteClusterName = null;
 	
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pCoordinator the coordinator which joins a cluster
 	 * @param pCoordinatorClusterName description of the cluster of the coordinator
 	 * @param pJoinedClusterName description of the joined cluster
 	 * @param pCoordinatorNodeL2Address the L2 address of the node where the coordinator of this cluster is located
 	 */
 	private CoordinatorAsClusterMember(HRMController pHRMController, Coordinator pCoordinator, ClusterName pCoordinatorClusterName, ClusterName pJoinedClusterName, L2Address pCoordinatorNodeL2Address)
 	{	
 		super(pHRMController, pJoinedClusterName.getHierarchyLevel() /* use the hierarchy level of the joined cluster here */, pCoordinatorClusterName.getClusterID(), pCoordinatorClusterName.getCoordinatorID(), pCoordinatorNodeL2Address);
 
 		// update the coordinator for which this membership was created
 		mCoordinator = pCoordinator;
 		
 		// store the ClusterName of the remote cluster
 		mRemoteClusterName = pJoinedClusterName;
 	}
 
 	/**
 	 * Factory function: for coordinators which join a cluster
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 * @param pCoordinator the coordinator which joins a cluster
 	 * @param pJoinedClusterName description of the joined cluster
 	 * @param pClusterHeadNodeL2Address the L2 address of the node where the cluster head is located
 	 * @return
 	 */
 	public static CoordinatorAsClusterMember create(HRMController pHRMController, Coordinator pCoordinator, ClusterName pJoinedClusterName, L2Address pClusterHeadNodeL2Address)
 	{
 		CoordinatorAsClusterMember tResult = new CoordinatorAsClusterMember(pHRMController, pCoordinator, pCoordinator.getCluster().createClusterName(), pJoinedClusterName, pClusterHeadNodeL2Address);
 		
 		Logging.log(tResult, "\n\n\n################ CREATED COORDINATOR AS CLUSTER MEMBER at hierarchy level: " + (tResult.getHierarchyLevel().getValue()));
 
 		// creates new elector object, which is responsible for election processes
 		tResult.mElector = new Elector(pHRMController, tResult);
 
 		// register at HRMController's internal database
 		pHRMController.registerCoordinatorAsClusterMember(tResult);
 
 		// register at the parent Coordinator
 		pCoordinator.registerClusterMembership(tResult);
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the coordinator for which this cluster membership was created
 	 * 
 	 * @return the coordinator
 	 */
 	public Coordinator getCoordinator()
 	{
 		return mCoordinator;
 	}
 	
 	/**
 	 * Returns the remote ClusterName
 	 * 
 	 * @return the remote ClusterName
 	 */
 	public ClusterName getRemoteClusterName()
 	{
 		return mRemoteClusterName;	
 	}
 	
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) forward this packet to the coordinator for which this cluster membership was created
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorAnnouncement(ComChannel pComChannel, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "EVENT: coordinator announcement (from above): " + pAnnounceCoordinator);
 			Logging.log(this, "       ..fowarding announcement to coordinator object: " + mCoordinator);
 		}
 		
 		mCoordinator.eventCoordinatorAnnouncement(pComChannel, pAnnounceCoordinator);
 	}
 
 	/**
 	 * EVENT: coordinator invalidation, we react on this by:
 	 *       1.) forward this packet to the coordinator for which this cluster membership was created
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pInvalidCoordinator the received invalidation
 	 */
 	@Override
 	public void eventCoordinatorInvalidation(ComChannel pComChannel, InvalidCoordinator pInvalidCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "EVENT: coordinator invalidation (from above): " + pInvalidCoordinator);
 			Logging.log(this, "       ..fowarding invalidation to coordinator object: " + mCoordinator);
 		}
 		
 		mCoordinator.eventCoordinatorInvalidation(pComChannel, pInvalidCoordinator);
 	}
 
 	/**
 	 * EVENT: cluster membership canceled, triggered by cluster head
 	 * 
 	 *  @param: pComChannel the comm. channel from where the cancellation was received
 	 */
 	@Override
 	public void eventClusterMemberRoleInvalid(ComChannel pComChannel)
 	{
 		Logging.log(this, "============ EVENT: cluster member role invalid, channel: " + pComChannel);
 		
 		/**
 		 * Trigger: cluster member role invalid
 		 */
 		eventCoordinatorAsClusterMemberRoleInvalid();
 	}
 
 	/**
 	 * EVENT: cluster member role invalid
 	 */
 	public void eventCoordinatorAsClusterMemberRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: Coordinator_As_ClusterMember_Role_Invalid");
 
 		/**
 		 * Trigger: Elector invalid
 		 */
 		getElector().eventInvalidation();
 
 		/**
 		 * Trigger: role invalid
 		 */
 		eventInvalidation();
 
 		LinkedList<ComChannel> tComChannels = getComChannels();
 		for(ComChannel tComChannel : tComChannels){
 			unregisterComChannel(tComChannel);
 		}
 		
 		Logging.log(this, "============ Destroying this CoordinatorAsClusterMember now...");
 
 		/**
 		 * Unregister from the HRMController's internal database
 		 */ 
 		mHRMController.unregisterCoordinatorAsClusterMember(this);
 
 		/**
 		 * Unregister from the parent coordinator's internal database
 		 */ 
 		mCoordinator.unregisterClusterMembership(this);
 	}
 
 	/**
 	 * EVENT: cluster membership to superior coordinator
 	 */
 	public void eventClusterMembershipToSuperiorCoordinator()
 	{
 		Logging.log(this, "EVENT: cluster membership to superior coordinator updated to me");
 
 		mCoordinator.eventClusterMembershipToSuperiorCoordinator(this);
 	}
 	
 	/**
 	 * EVENT: cluster membership to superior coordinator lost
 	 */
 	private void eventClusterMembershipToSuperiorCoordinatorLost()
 	{
 		Logging.log(this, "EVENT: cluster membership to superior coordinator lost");
 
 		mCoordinator.eventClusterMembershipToSuperiorCoordinator(null);
 	}
 	
 	/**
 	 * EVENT: new HRMID assigned
      * The function is called when an address update was received.
 	 * 
 	 * @param pSourceComChannel the source comm. channel
 	 * @param pHRMID the new HRMID
 	 */
 	public void eventAssignedHRMID(ComChannel pSourceComChannel, HRMID pHRMID)
 	{
 		Logging.log(this, "EVENT: eventAssignedHRMID with assigned HRMID " + pHRMID.toString() + ", source comm. channel: " + pSourceComChannel);
 
		if((pHRMID != null) && (!pHRMID.isZero())){
			// setHRMID()
			super.eventAssignedHRMID(pSourceComChannel, pHRMID);
		}
		
 		if(isActiveMembership()){
 			if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 				Logging.log(this, "     ..continuing the address distribution process via the coordinator: " + mCoordinator);
 			}
 
 			mCoordinator.eventAssignedHRMID(pSourceComChannel, pHRMID);
 		}
 	}
 	
 	/**
 	 * Updates the membership state
 	 * 
 	 * @param pState the new state
 	 */
 	public void setMembershipActivation(boolean pState)
 	{
 		Logging.log(this, "Updating membership state from " + mMembershipIsActive + " to " + pState);
 		
 		boolean tOldState = mMembershipIsActive;
 
 		/**
 		 * Update the membership
 		 */
 		mMembershipIsActive = pState;
 		
 		/**
 		 * Set our HRMID as the new one for the coordinator if we have a transition from "false" to "true"
 		 */
 		if((!tOldState) && (pState)){
 			if((getHRMID() != null) && (!getHRMID().isZero())){
 //				mCoordinator.eventAssignedHRMID(null, getHRMID());
 			}
 		}
 	}
 	
 	/**
 	 * Returns if the membership is an active one
 	 * 
 	 * @return
 	 */
 	private boolean isActiveMembership()
 	{
 		return mMembershipIsActive;
 	}
 
 	/**
 	 * Sets the cluster activation, triggered by the Elector or the Cluster which got a new local Coordinator
 	 * 
 	 * @param pState the new state
 	 */
 	@Override
 	public void setClusterActivation(boolean pState)
 	{
 		boolean tOldState = isActiveCluster();
 		
 		super.setClusterActivation(pState);
 		
 		/**
 		 * Is it a transition from "true" to " false"?
 		 */
 		if((tOldState) && (!pState)){
 			// is this the currently active membership towards the superior cluster?
 			if(isActiveMembership()){
 				/**
 				 * Trigger: cluster membership to superior coordinator lost
 				 */
 				eventClusterMembershipToSuperiorCoordinatorLost();	
 			}
 		}
 	}
 
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return "CoordAsClusterMember" + mCoordinator.getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + mCoordinator.getHierarchyLevel().getValue() + "(" + idToString() + ", Coord.=" + getCoordinatorNodeL2Address()+ ")";
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
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
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
 			return "Cluster" + getGUIClusterID() + ", Peer Node=" + getCoordinatorNodeL2Address() + ", Remote=" + mRemoteClusterName;
 		}else{
 			return "Cluster" + getGUIClusterID() + ", Peer Node=" + getCoordinatorNodeL2Address() + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
