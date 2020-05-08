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
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnounceRemoteCluster;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyLeave;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyPriorityUpdate;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RoutingInformation;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.RequestClusterParticipationProperty;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for a coordinator instance and can be used on all hierarchy levels.
  * A cluster's elector instance is responsible for creating instances of this class.
  */
 public class Coordinator extends ControlEntity implements Localization
 {
 	/**
 	 * List of already known neighbor coordinators
 	 */
 	private LinkedList<Name> mConnectedNeighborCoordinators = new LinkedList<Name>();
 	
 	/**
 	 * Stores the simulation timestamp of the last "share phase"
 	 */
 	private double mTimeOfLastSharePhase = 0; 
 	
 	/**
 	 * Stores the routes which should be shared with cluster members.
 	 */
 	private LinkedList<RoutingEntry> mSharedRoutes = new LinkedList<RoutingEntry>();
 	
 	/**
 	 * Stores whether the data of the "shared phase" has changed or not.
 	 */
 	private boolean mSharedRoutesHaveChanged = false;
 	
 	/**
 	 * Stores the parent cluster, which is managed by this coordinator instance.
 	 */
 	private Cluster mParentCluster = null;;
 
 
 	/**
 	 * Stores if the initial clustering has already finished
 	 */
 	private boolean mInitialClusteringFinished = false;
 	
 	/**
 	 * This is the coordinator counter, which allows for globally (related to a physical simulation machine) unique coordinator IDs.
 	 */
 	private static long sNextFreeCoordinatorID = 1;
 
 	/**
 	 * Count the outgoing connections
 	 */
 	private int mCounterOutgoingConnections = 0;
 
 	/**
 	 * Stores the next free address for a cluster member
 	 */
 	private int mNextFreeClusterMemberAddress = 1;
 	
 	private static final long serialVersionUID = 6824959379284820010L;
 	
 	/**
 	 * The constructor for a cluster object. Usually, it is called by a cluster's elector instance
 	 * 
 	 * @param pCluster the parent cluster instance
 	 */
 	public Coordinator(Cluster pCluster)
 	{
 		// use the HRMController reference and the hierarchy level from the cluster
 		super(pCluster.mHRMController, pCluster.getHierarchyLevel());
 		
 		mParentCluster = pCluster;
 		
 		// create an ID for the cluster
 		setCoordinatorID((int)createCoordinatorID());
 		
 		// clone the HRMID of the managed cluster because it can already contain the needed HRMID prefix address
 		setHRMID(this,  mParentCluster.getHRMID().clone());
 		
 		// register itself as coordinator for the managed cluster
 		mParentCluster.eventNewLocalCoordinator(this);
 
 		// register at HRMController's internal database
 		mHRMController.registerCoordinator(this);
 
 		Logging.log(this, "\n\n\n################ CREATED COORDINATOR on hierarchy level: " + (getHierarchyLevel().getValue() - 1));
 	}
 	
 	/**
 	 * Generates a new ClusterID
 	 * 
 	 * @return the ClusterID
 	 */
 	static private synchronized long createCoordinatorID()
 	{
 		// get the current unique ID counter
 		long tResult = sNextFreeCoordinatorID * idMachineMultiplier();
 
 		// make sure the next ID isn't equal
 		sNextFreeCoordinatorID++;
 	
 		return tResult;
 	}
 
 	/**
 	 * Creates a new HRMID for a cluster member depending on the given member number.
 	 * 
 	 * @param pMemberNumber the member number
 	 * @return the new HRMID for the cluster member
 	 */
 	private HRMID createClusterMemberAddress(int pMemberNumber)
 	{
 		HRMID tHRMID = getHRMID().clone();
 		
 		// transform the member number to a BigInteger
 		BigInteger tAddress = BigInteger.valueOf(pMemberNumber);
 
 		// set the member number for the given hierarchy level
 		tHRMID.setLevelAddress(super.getHierarchyLevel(), tAddress);
 
 		// some debug outputs
 		if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 			Logging.log(this, "Set " + tAddress + " on hierarchy level " + super.getHierarchyLevel().getValue() + " for HRMID " + tHRMID.toString());
 			Logging.log(this, "Created for a cluster member the NEW HRMID=" + tHRMID.toString());
 		}
 		
 		return tHRMID;
 	}
 
 	/**
 	 * Returns the machine-local CoordinatorID (excluding the machine specific multiplier)
 	 * 
 	 * @return the machine-local CoordinatorID
 	 */
 	public long getGUICoordinatorID()
 	{
 		return getCoordinatorID() / idMachineMultiplier();
 	}
 
 	/**
 	 * DISTRIBUTE: distribute addresses among cluster members if:
 	 *           + an HRMID was received from a superior coordinator, used to distribute HRMIDs downwards the hierarchy,
 	 *           + we were announced as coordinator
 	 * This function is called for distributing HRMIDs among the cluster members.
 	 */
 	public void distributeAddresses()
 	{
 		/**
 		 * The following value is used to assign monotonously growing addresses to all cluster members.
 		 * The addressing has to start with "1".
 		 */
 		mNextFreeClusterMemberAddress = 1;
 
 		Logging.log(this, "DISTRIBUTING ADDRESSES to entities at level " + (getHierarchyLevel().getValue() - 1) + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 		
 		/**
 		 * Assign ourself an HRMID address
 		 */
 		// are we at the base level?
 		if(super.getHierarchyLevel().isBaseLevel()) {
 			
 			// create new HRMID for ourself
 			HRMID tOwnAddress = createClusterMemberAddress(mNextFreeClusterMemberAddress++);
 
 			Logging.log(this, "    ..setting local HRMID " + tOwnAddress.toString());
 
 			//HINT: don't update the HRMID of the coordinator here!
 			
 			// update the HRMID of the managed cluster by direct call and avoid additional communication overhead
 			mParentCluster.setHRMID(this, tOwnAddress);
 		}
 
 		/**
 		 * Distribute AssignHRMID packets among the cluster members 
 		 */
 		LinkedList<ComChannel> tComChannels = mParentCluster.getComChannels();
 		
 		Logging.log(this, "    ..distributing HRMIDs among cluster members: " + tComChannels);
 		for(ComChannel tComChannel : tComChannels) {
 
 			//TODO: don't send this update in a loop to ourself!
 			//TODO: check if cluster members already have an address and distribute only free addresses here
 			
 			/**
 			 * Trigger: cluster member needs HRMID
 			 */
 			eventClusterMemberNeedsHRMID(tComChannel);
 		}
 	}
 	
 	/**
 	 * Shares a route to a cluster cluster member with other cluster members
 	 * 
 	 * @param pClusterMemberChannel the cluster member to whom we have a sharable route
 	 */
 	private void shareRouteToClusterMember(ComChannel pClusterMemberChannel)
 	{
 		// determine the HRMID of the cluster member
 		HRMID tMemberHRMID = pClusterMemberChannel.getPeerHRMID();
 		
 		// are we on base hierarchy level?
 		if (getHierarchyLevel().getValue() == 1){ // TODO: isBaseLevel()){
 			// create the new routing table entry
 			RoutingEntry tRoutingEntry = RoutingEntry.createRouteToDirectNeighbor(tMemberHRMID, 0 /* TODO */, 1 /* TODO */, RoutingEntry.INFINITE_DATARATE /* TODO */);
 			// define the L2 address of the next hop in order to let "addHRMRoute" trigger the HRS instance the creation of new HRMID-to-L2ADDRESS mapping entry
 			tRoutingEntry.setNextHopL2Address(pClusterMemberChannel.getPeerL2Address());
 			
 			Logging.log(this, "SHARING ROUTE: " + tRoutingEntry);
 			
 			// add the entry to the local routing table
 			mHRMController.addHRMRoute(tRoutingEntry);
 			
 			// store the entry for route sharing with cluster members
 			synchronized (mSharedRoutes){
 				/**
 				 * Check for duplicates
 				 */
 				if (HRMConfig.Routing.AVOID_DUPLICATES_IN_ROUTING_TABLES){
 					boolean tRestartNeeded;
 					do{		
 						tRestartNeeded = false;
 						for (RoutingEntry tEntry: mSharedRoutes){
 							// have we found a route to the same destination which uses the same next hop?
 							//TODO: what about multiple links to the same next hop?
 							if ((tEntry.getDest().equals(tRoutingEntry.getDest())) /* same destination? */ &&
 								(tEntry.getNextHop().equals(tRoutingEntry.getNextHop())) /* same next hop? */){
 		
 								Logging.log(this, "REMOVING DUPLICATE: " + tEntry);
 								
 								// remove the route
 								mSharedRoutes.remove(tEntry);
 								
 								// mark "shared phase" data as changed
 								mSharedRoutesHaveChanged = true;
 								
 								// force a restart at the beginning of the routing table
 								tRestartNeeded = true;
 								//TODO: use a better(scalable) method here for removing duplicates
 								break;						
 								
 							}
 						}
 					}while(tRestartNeeded);
 				}
 				
 				/**
 				 * Add the entry to the shared routing table
 				 */
 				mSharedRoutes.add(tRoutingEntry);//TODO: use a database per cluster member here
 				// mark "shared phase" data as changed
 				mSharedRoutesHaveChanged = true;
 			}
 		}else{
 			//TODO
 			Logging.log(this, "IMPLEMENT ME - SHARING ROUTE TO: " + pClusterMemberChannel);
 		}
 	}
 
 	/**
 	 * Checks if the "share phase" should be started or not
 	 * 
 	 * @return true if the "share phase" should be started, otherwise false
 	 */
 	private boolean sharePhaseHasTimeout()
 	{
 		// determine the time between two "share phases"
 		double tDesiredTimePeriod = mHRMController.getPeriodSharePhase(getHierarchyLevel().getValue() - 1);
 		
 		// determine the time when a "share phase" has to be started 
 		double tTimeNextSharePhase = mTimeOfLastSharePhase + tDesiredTimePeriod;
 	
 		// determine the current simulation time from the HRMCotnroller instance
 		double tCurrentSimulationTime = mHRMController.getSimulationTime();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TIMING_ROUTE_DISTRIBUTION){
 			Logging.log(this, "Checking for timeout of \"share phase\": desired time period is " + tDesiredTimePeriod + ", " + tCurrentSimulationTime + " > " + tTimeNextSharePhase + "? -> " + (tCurrentSimulationTime >= tTimeNextSharePhase));
 		}
 		
 		return (tCurrentSimulationTime >= tTimeNextSharePhase);
 	}
 	
 	/**
 	 * Determines if new "share phase" data is available
 	 * 
 	 * @return true if new data is available, otherwise false
 	 */
 	private boolean hasNewSharePhaseData()
 	{
 		boolean tResult = false;
 		
 		synchronized (mSharedRoutes){
 			tResult = mSharedRoutesHaveChanged;
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * This function implements the "share phase".
 	 * It distributes locally stored sharable routing data among the known cluster members
 	 */
 	public void sharePhase()
 	{
 		// should we start the "share phase"?
 		if (sharePhaseHasTimeout()){
 			if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 				Logging.log(this, "SHARE PHASE with cluster members on level " + (getHierarchyLevel().getValue() - 1) + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 			}
 
 			// store the time of this "share phase"
 			mTimeOfLastSharePhase = mHRMController.getSimulationTime();
 
 			if ((!HRMConfig.Routing.PERIODIC_SHARE_PHASES) && (!hasNewSharePhaseData())){
 				if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 					Logging.log(this, "SHARE PHASE skipped because routing data hasn't changed since last signaling round");
 				}
 				return;
 			}
 			
 			/**
 			 * SHARE PHASE 
 			 */
 			// determine own local cluster address
 			HRMID tOwnClusterAddress = mParentCluster.getHRMID();
 	
 			if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 				Logging.log(this, "    ..distributing as " + tOwnClusterAddress.toString() + " aggregated ROUTES among cluster members: " + mParentCluster.getComChannels());
 			}
 			
 			synchronized (mSharedRoutes){
 				// send the routing information to cluster members
 				for(ComChannel tClusterMember : mParentCluster.getComChannels()) {
 					RoutingInformation tRoutingInformationPacket = new RoutingInformation(tOwnClusterAddress, tClusterMember.getPeerHRMID());
 				
 					// are we on base hierarchy level?
 					if (getHierarchyLevel().getValue() == 1){ // TODO: isBaseLevel()){
 	
 						/**
 						 * ADD ROUTES: routes from the cluster member to this node for every registered local HRMID.
 						 */
 						// determine the L2 address of this physical node
 						L2Address tPhysNodeL2Address = mHRMController.getHRS().getCentralFNL2Address();
 						// iterate over all HRMIDs which are registered for this physical node
 						for (HRMID tHRMID : mHRMController.getOwnHRMIDs()){
 							// create entry for cluster internal routing towards us
 							RoutingEntry tRouteFromClusterMemberToHere = RoutingEntry.createRouteToDirectNeighbor(tHRMID, 0 /* TODO */, 1 /* TODO */, RoutingEntry.INFINITE_DATARATE /* TODO */);
 							// define the L2 address of the next hop in order to let the receiver store it in its HRMID-to-L2ADDRESS mapping
 							tRouteFromClusterMemberToHere.setNextHopL2Address(tPhysNodeL2Address);
 							// add the route in the "share phase" signaling
 							tRoutingInformationPacket.addRoute(tRouteFromClusterMemberToHere);
 						}
 						
 						//TODO: need routing graph here!
 						
 						//TODO: routes to other cluster members
 					}else{
 						//TODO: implement me
 					}	
 					
 					/**
 					 * Send the routing data to the cluster member
 					 */
 					// do we have interesting routing information?
 					if (tRoutingInformationPacket.getRoutes().size() > 0){
 						tClusterMember.sendPacket(tRoutingInformationPacket);
 					}else{
 						// no routing information -> no packet is sent
 					}
 				}
 
 				/**
 				 * mark "share phase" data as known
 				 */
 				mSharedRoutesHaveChanged = false;
 			}
 		}else{
 			// share phase shouldn't be started, we have to wait until next trigger
 		}
 	}
 	
 	/**
 	 * This function implements the "report phase".
 	 * It sends locally stored sharable routing data to the superior coordinator
 	 */
 	public void reportPhase()
 	{
 		if (!getHierarchyLevel().isHighest()){
 			
 		}else{
 			// we are the highest hierarchy level, no one to send topology reports to
 		}
 	}
 	
 	/**
 	 * EVENT: "eventCoordinatorRoleInvalid", triggered by the Elector, the reaction is:
 	 * 	 	1.) create signaling packet "BullyLeave"
 	 * 		2.) send the packet to the superior coordinator 
 	 */
 	public void eventCoordinatorRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: Coordinator_Role_Invalid");
 
 		/**
 		 * Inform all superior clusters about the event and trigger the invalidation of this coordinator instance -> we leave all Bully elections because we are no longer a possible election winner
 		 */
 		if (!getHierarchyLevel().isHighest()){
 			// create signaling packet for signaling that we leave the Bully group
 			BullyLeave tBullyLeavePacket = new BullyLeave(mHRMController.getNodeName(), getPriority());
 
 			sendSuperiorClusters(tBullyLeavePacket);
 		}else{
 			Logging.log(this, "eventCoordinatorRoleInvalid() skips further signaling because hierarchy end is already reached at: " + (getHierarchyLevel().getValue() - 1));
 		}
 
 		/**
 		 * Revoke own HRMID
 		 */ 
 		eventRevokedHRMID(this, getHRMID());
 		
 		/**
 		 * Revoke all assigned HRMIDs of all cluster members
 		 */
 		revokeAssignedHRMIDsFromClusterMembers();
 		
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this coordinator now...");
 		
 		// unregister itself as coordinator for the managed cluster
 		mParentCluster.eventNewLocalCoordinator(null);
 
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCoordinator(this);
 		
 		/**
 		 * Inform the inferior cluster about our destruction
 		 */
 		mParentCluster.eventCoordinatorLost();
 	}
 	
 	/**
 	 * Revokes all HRMIDs per comm. channel
 	 */
 	private void revokeAssignedHRMIDsFromClusterMembers()
 	{
 		Logging.log(this, "###### Revoking assigned HRMIDs for all clsuter members");
 
 		LinkedList<ComChannel> tComChannels = mParentCluster.getComChannels();
 		for (ComChannel tcomChannel : tComChannels){
 			tcomChannel.signalRevokeHRMIDs();
 		}
 	}
 	
 	/**
 	 * EVENT: cluster member needs HRMID
 	 * 
 	 * @param pComChannel the comm. channel towards the cluster member, which needs a new HRMID
 	 */
 	public void eventClusterMemberNeedsHRMID(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: Cluster_Member_Needs_HRMID for: " + pComChannel);
 		
 		// create new HRMID for cluster member
 		HRMID tHRMID = createClusterMemberAddress(mNextFreeClusterMemberAddress++);
 
 		// store the HRMID under which the peer will be addressable from now 
 		pComChannel.setPeerHRMID(tHRMID);
 		
 		if ((pComChannel.getPeerHRMID() != null) && (!pComChannel.getPeerHRMID().equals(tHRMID))){
 			Logging.log(this, "    ..replacing HRMID " + pComChannel.getPeerHRMID().toString() + " and assign new HRMID " + tHRMID.toString() + " to " + pComChannel.getPeerL2Address());
 		}else
 			Logging.log(this, "    ..assigning new HRMID " + tHRMID.toString() + " to " + pComChannel.getPeerL2Address());
 
 		// create new AssignHRMID packet for the cluster member
 		AssignHRMID tAssignHRMIDPacket = new AssignHRMID(mHRMController.getNodeName(), pComChannel.getPeerHRMID(), tHRMID);
 		
 		// register this new HRMID in the local HRS and create a mapping to the right L2Address
 		Logging.log(this, "    ..creating MAPPING " + tHRMID.toString() + " to " + pComChannel.getPeerL2Address());
 		mHRMController.getHRS().mapHRMIDToL2Address(tHRMID, pComChannel.getPeerL2Address());
 		
 		// share the route to this cluster member with all other cluster members
 		shareRouteToClusterMember(pComChannel);
 		
 		// store the assignment for this comm. channel
 		pComChannel.storeAssignedHRMID(tHRMID);
 		
 		// send the packet
 		pComChannel.sendPacket(tAssignHRMIDPacket);
 	}
 
 	/**
 	 * EVENT: "announced", triggered by Elector if the election was won and this coordinator was announced to all cluster members 	 
 	 */
 	public void eventAnnouncedAsCoordinator()
 	{
 		/**
 		 * Trigger: explicit cluster announcement to neighbors
 		 */ 
 		Logging.log(this, "EVENT ANNOUNCED - triggering distribution of ClusterAnnounces");
 		getCluster().distributeClusterAnnouncement();
 
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			Logging.log(this, "EVENT ANNOUNCED - triggering address assignment for " + getComChannels().size() + " cluster members");
 
 			distributeAddresses();
 		}
 		
 		//TODO: ??
 //		mHRMController.setSourceIntermediateCluster(this, getCluster());
 
 		/**
 		 * AUTO CLUSTERING
 		 */
 		if(!getHierarchyLevel().isHighest()) {
 			if (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY){
 				Logging.log(this, "EVENT ANNOUNCED - triggering clustering of this cluster's coordinator and its neighbors");
 
 				// start the clustering of this cluster's coordinator and its neighbors if it wasn't already triggered by another coordinator
 				if (!isClustered()){
 					cluster();
 				}else{
 					Logging.warn(this, "Clustering is already finished for this hierarchy level, skipping cluster-request");
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 * Clusters the superior hierarchy level or tries to join an already existing superior cluster
 	 */
 	public void cluster()
 	{
 		Logging.log(this, "\n\n\n################ CLUSTERING STARTED");
 
 		/**
 		 * Try to find a matching local cluster on the superior hierarchy level
 		 */
 		boolean tJoinedExistingSuperiorCluster = false;
 		Logging.log(this, "      ..searching for a locally known superior cluster on hierarchy level: " + getHierarchyLevel().getValue());
 		if(HRMConfig.Hierarchy.COORDINATORS_MAY_JOIN_EXISTING_SUPERIOR_CLUSTERS){
 			for(Cluster tCluster : mHRMController.getAllClusters(getHierarchyLevel())) {
 				Logging.log(this, "        ..found superior cluster: " + tCluster);
 				if (joinExistingSuperiorCluster(tCluster)){
 					Logging.log(this, "          ..joined locally known superior cluster: " + tCluster);
 					// we joined the superior cluster and should end the search for a superior coordinator
 					tJoinedExistingSuperiorCluster = true;
 					break;
 				}
 			}
 		}
 		
 		/**
 		 * Explore the neighborhood and create a new superior cluster
 		 */
 		if (!tJoinedExistingSuperiorCluster){
 			Logging.log(this, "      ..can't find a locally known superior cluster on hierarchy level: " + getHierarchyLevel().getValue() + ", known clusters are: " + mHRMController.getAllClusters());
 			Logging.log(this, "      ..starting neighbor exploration now..");
 			exploreNeighborhoodAndCreateSuperiorCluster();
 		}
 	}
 
 	/**
 	 * Tries to join an existing superior cluster
 	 * 
 	 * @param pSuperiorCluster an existing superior cluster where we want to join
 	 * 
 	 * @return true or false to indicate success/error
 	 */
 	private boolean joinExistingSuperiorCluster(ControlEntity pSuperiorCluster)
 	{
 		boolean tResult = false;
 		
 		Logging.log(this, "\n\n\n################ JOINING EXISTING SUPERIOR CLUSTER " + pSuperiorCluster);
 
 		/**
 		 * Try to get the comm. session towards the superior cluster
 		 */
 		ComSession tComSession = mHRMController.getOutgoingComSession(pSuperiorCluster);		
 		// have we found the comm. session?
 		if (tComSession != null){
 			Logging.log(this, "           ..found matching comm. session: " + tComSession);
 			Logging.log(this, "             ..has com. channels: " + tComSession.getAllComChannels());
 			
 		    /**
 		     * Create communication channel
 		     */
 		    Logging.log(this, "           ..creating new communication channel");
 			ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.OUT, this, tComSession);
 			tComChannel.setRemoteClusterName(new ClusterName(mHRMController, pSuperiorCluster.getHierarchyLevel(), pSuperiorCluster.getClusterID(), pSuperiorCluster.getCoordinatorID()));
 			tComChannel.setPeerPriority(pSuperiorCluster.getPriority());
 			
 			/**
 			 * Send "RequestClusterMembership" along the comm. session
 			 * HINT: we cannot use the created channel because the remote side doesn't know anything about the new comm. channel yet)
 			 */
 		    ClusterName tOwnClusterName = new ClusterName(mHRMController, getHierarchyLevel(), getClusterID(), getCoordinatorID());
 		    Logging.log(this, "           ..sending membership request via communication session");
 			RequestClusterMembership tRequestClusterMembership = new RequestClusterMembership(mHRMController.getNodeName(), pSuperiorCluster.getHRMID(), tOwnClusterName);
 			if (tComSession.write(tRequestClusterMembership)){
 				tResult = true;
 				
 				// trigger event "finished clustering" 
 				eventInitialClusteringFinished();
 			}
 		}else{
 			Logging.warn(this, "joinSuperiorCluster() wasn't able to find the outgoing comm. session towards: " + pSuperiorCluster);
 			//TODO: create new connection !?
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: initial clustering has finished  
 	 */
 	private void eventInitialClusteringFinished()
 	{
 		// mark initial clustering as "finished"
 		mInitialClusteringFinished = true;		
 	}
 
 	/**
 	 * EVENT: we have joined the superior cluster, triggered by the comm. channel if the request for cluster membership was ack'ed
 	 * 
 	 * @param pSourceComChannel the source comm. channel
 	 */
 	public void eventJoinedSuperiorCluster(ComChannel pSourceComChannel)
 	{
 		Logging.log(this, "HAVE JOINED superior cluster");
 		
 		BullyPriorityUpdate tBullyPriorityUpdatePacket = new BullyPriorityUpdate(mHRMController.getNodeName(), BullyPriority.createForSuperiorControlEntity(mHRMController,  this));
 		pSourceComChannel.sendPacket(tBullyPriorityUpdatePacket);
 	}
 
 	/**
 	 * Sends a packet towards all possible superior clusters
 	 * If there is more than one comm. channel an error occurs but the message is sent
 	 * 
 	 * @param pPacket the packet
 	 */
 	private void sendSuperiorClusters(Serializable pPacket)
 	{
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		// get the L2Addres of the local host
 		L2Address tLocalL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		
 		Logging.log(this, "Sending TOWARDS SUPERIOR CLUSTER from " + tLocalL2Address + " the packet " + pPacket + " to " + tComChannels.size() + " communication channels");
 		
 		int tUsedChannels = 0;
 		for(ComChannel tComChannel : tComChannels) {
 			boolean tIsLoopback = tLocalL2Address.equals(tComChannel.getPeerL2Address());
 			
 			if (!tIsLoopback){
 				Logging.log(this, "       ..to " + tComChannel);
 			}else{
 				Logging.log(this, "       ..to LOOPBACK " + tComChannel);
 			}
 
 			// send the packet to one of the possible cluster members
 			tComChannel.sendPacket(pPacket);
 			
 			tUsedChannels++;
 		}
 		
 		// drop the warning in case too many comm. channels were used
 		if (tUsedChannels > 1){
 			Logging.warn(this, "Found " + tUsedChannels + " instead of ONLY ONE channel twoards the top of the hierarchy");
 		}
 	}
 
 	/**
 	 * Returns if the initial clustering has already finished
 	 * 
 	 * @return true or false
 	 */
 	public boolean isClustered()
 	{
 		return mInitialClusteringFinished;
 	}
 
 	//TODO: fix this +1 stuff
 	@Override
 	public HierarchyLevel getHierarchyLevel() {
 		return new HierarchyLevel(this, super.getHierarchyLevel().getValue() + 1);
 	}
 
 	/**
 	 * Returns the Bully priority of the parent cluster
 	 * 
 	 * @return the Bully priority
 	 */
 	@Override
 	public BullyPriority getPriority() 
 	{
 		// return the Bully priority of the managed cluster object
 		return mParentCluster.getPriority();
 	}
 	
 	/**
 	 * Sets a new Bully priority
 	 * 
 	 * @param pPriority the new Bully priority
 	 */
 	@Override
 	public void setPriority(BullyPriority pPriority) 
 	{
 		if (!getPriority().equals(pPriority)){
 			Logging.err(this, "Updating Bully priority from " + getPriority() + " to " + pPriority);
 		}else{
 			Logging.log(this, "Trying to set same Bully priority " + getPriority());
 		}
 		
 		// update the Bully priority of the parent cluster, which is managed by this coordinator
 		mParentCluster.setPriority(pPriority);
 	}
 
 	/**
 	 * Returns a reference to the cluster, which this coordinator manages.
 	 * 
 	 * @return the managed cluster
 	 */
 	public Cluster getCluster()
 	{
 		return mParentCluster;
 	}
 	
 	/**
 	 * Returns the unique ID of the parental cluster
 	 * 
 	 * @return the unique cluster ID
 	 */
 	@Override
 	public Long getClusterID() {
 		return mParentCluster.getClusterID();
 	}
 	
 	/**
 	 * Checks if there already exists a connection to a neighbor coordinator
 	 * 
 	 * @param pCoordinatorName the name of the neighbor coordinator
 	 * 
 	 * @return true or false
 	 */
 	private boolean isConnectedToNeighborCoordinator(Name pCoordinatorName)
 	{
 		boolean tResult = false;
 		
 		synchronized (mConnectedNeighborCoordinators) {
 			tResult = mConnectedNeighborCoordinators.contains(pCoordinatorName);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Registers an already existing connection to a neighbor coordinator in order to avoid connection duplicates
 	 * 
 	 * @param pCoordinatorName the name of the neighbor coordinator
 	 */
 	private void registerConnectionToNeighborCoordinator(Name pCoordinatorName)
 	{
 		synchronized (mConnectedNeighborCoordinators) {
 			mConnectedNeighborCoordinators.add(pCoordinatorName);
 		}
 	}
 
 	/**
 	 * Creates a cluster consisting of this coordinator and neighbor coordinators by the following steps:
 	 *     1.) querying the ARG from the HRMController for neighbor clusters within the given max. radius
 	 *     2.) depending on if there were found any neighbors: 
 	 *          ..if neighbors were found: for an increasing radius find each neighbor and trigger event "detectedNeighborCoordinator" (this expands the superior cluster)
 	 *          ..if no neighbors were found: expand locally by connecting in a local loop  to ourself and trigger the creation of a new superior cluster
 	 *     
 	 */
 	private void exploreNeighborhoodAndCreateSuperiorCluster()
 	{
 		Logging.log(this, "\n\n\n################ EXPLORATION STARTED on hierarchy level " + getHierarchyLevel().getValue());
 		
 		// was the clustering already triggered?
 		if (!isClustered()){
 			// are we already at the highest hierarchy level?
 			if (!getHierarchyLevel().isHighest()){
 				int tMaxRadius = HRMConfig.Hierarchy.EXPANSION_RADIUS;
 		
 				Logging.log(this, "Maximum radius is " + tMaxRadius);
 		
 				// get all known neighbor clusters ordered by their radius to the parent cluster
 				List<AbstractRoutingGraphNode> tNeighborClustersForClustering = mHRMController.getNeighborClustersOrderedByRadiusInARG(mParentCluster);
 				Logging.log(this, "     ..neighborhood ordered by radius: " + tNeighborClustersForClustering);
 
 				/**
 				 * Detected isolation
 				 */
 				boolean tDetectedIsolation = true;
 				
 				/**
 				 * count from radius 1 to max. radius and connect to each cluster candidate
 				 */
 				if(tNeighborClustersForClustering.size() > 0){
 					for(int tRadius = 1; tRadius <= tMaxRadius; tRadius++) {
 						
 						Logging.log(this, "\n>>> Exploring neighbors with radius (" + tRadius + "/" + tMaxRadius + ")");
 						
 						// create list for storing the found neighbor clusters, which have a cluster distance equal to the current radius value 
 						List<AbstractRoutingGraphNode> tSelectedNeighborClusters = new LinkedList<AbstractRoutingGraphNode>();
 						
 						List<AbstractRoutingGraphNode> tNeighborClustersForClustering_RemoveList = new LinkedList<AbstractRoutingGraphNode>();
 						
 						/**
 						 * Iterate over all cluster candidates and determines the logical distance of each found candidate
 						 */
 						for(AbstractRoutingGraphNode tClusterCandidate : tNeighborClustersForClustering) {
 							// is the found neighbor a Cluster object?
 							if(tClusterCandidate instanceof Cluster) {
 								 if (tRadius == 1){
 									 // add this Cluster object as cluster candidate because it obviously has a logical distance of 1
 									 Logging.log(this, "     ..[r=" + tRadius + "]: found Cluster candidate: " + tClusterCandidate);
 									 tSelectedNeighborClusters.add(tClusterCandidate);
 									 
 									 // remove this candidate from the global list
 									 tNeighborClustersForClustering_RemoveList.add(tClusterCandidate);
 								 }else{
 									 // found a Cluster object but the radius is already beyond 1
 								 }
 							} else {
 								// is the found neighbor a ClusterProxy object?
 								if (tClusterCandidate instanceof ClusterProxy){
 									// get the proxy for this cluster
 									ClusterProxy tClusterCandidateProxy = (ClusterProxy)tClusterCandidate;
 									
 									// are we already connected to this candidate?
 									if (!isConnectedToNeighborCoordinator(tClusterCandidateProxy.getCoordinatorNodeName())){
 										
 										// get the logical distance to the neighbor
 										int tNeighborClusterDistance = 0; //TODO: mHRMController.getClusterDistance(tClusterCandidateProxy);
 										
 										// should we connect to the found cluster candidate?
 										if ((tNeighborClusterDistance > 0) && (tNeighborClusterDistance <= tRadius)) {
 											// add this candidate to the list of connection targets
 											Logging.log(this, "     ..[r=" + tRadius + "]: found ClusterProxy candidate: " + tClusterCandidate);
 											tSelectedNeighborClusters.add(tClusterCandidateProxy);
 	
 											// remove this candidate from the global list
 											tNeighborClustersForClustering_RemoveList.add(tClusterCandidate);
 										}else{
 											// the logical distance doesn't equal to the current radius value
 											if (tNeighborClusterDistance > tRadius){
 												// we have already passed the last possible candidate -> we continue the for-loop
 												continue;
 											}
 										}
 									}
 								}else{
 									Logging.err(this, "Found unsupported neighbor: " + tClusterCandidate);
 								}
 							}
 						}
 	
 						/**
 						 * Remove all processed cluster candidates from the global candidate list in order to reduce processing time
 						 */
 						for (AbstractRoutingGraphNode tRemoveCandidate : tNeighborClustersForClustering_RemoveList){
 							tNeighborClustersForClustering.remove(tRemoveCandidate);
 						}
 						
 						Long tFutureClusterID = null;
 
 						/**
 						 * Connect to all found cluster candidates
 						 */
 						for(AbstractRoutingGraphNode tAbstractNeighborCluster : tSelectedNeighborClusters) {
 							Logging.log(this, "     ..processing neighbor cluster: " + tAbstractNeighborCluster);
 							
 							if(tAbstractNeighborCluster instanceof ControlEntity) {
 								ControlEntity tNeighborClusterControlEntity = (ControlEntity)tAbstractNeighborCluster;
 
 								// is the node name, where the coordinator is located, already known?
 								if (tNeighborClusterControlEntity.superiorCoordinatorNodeName() != null){
 									
 									/**
 									 * Create the cluster ID for the new cluster ONE TIME
 									 */
 									if(tFutureClusterID == null){
 										tFutureClusterID = Cluster.createClusterID();
 									}
 									
 									// we found at minimum one neighbor candidate
 									tDetectedIsolation = false;
 									
 									eventDetectedNeighborCoordinator(tNeighborClusterControlEntity, tFutureClusterID);
 								}else{
 									Logging.warn(this, "Node name of the coordinator is still unknown for: " + tNeighborClusterControlEntity);
 								}
 							}else{
 								Logging.err(this, "Unsupported neighbor object: " + tAbstractNeighborCluster);
 							}
 						}
 					}
 					
 				}else{
 					Logging.log(this, "     ..haven't found a neighbor cluster, will create a local new cluster");
 					
 					//HINT: we detected isolation
 				}
 
 				/**
 				 * React on isolation situation
 				 */
 				if(tDetectedIsolation){
 					/**
 					 * Trigger event "detected isolation"
 					 */
 					eventDetectedIsolation();
 				}
 			}else{
 				Logging.warn(this,  "CLUSTERING SKIPPED, no clustering on highest hierarchy level " + getHierarchyLevel().getValue() + " needed");
 			}
 		}else{
 			Logging.warn(this, "Clustering was already triggered, clustering will be maintained");
 		}
 	}
 
 	/**
 	 * EVENT: detected isolation
 	 */
 	private void eventDetectedIsolation()
 	{
 		Logging.log(this, "EVENT: detected lcoal isolation");
 		
 		/**
 		 * Expand locally and create a new local superior cluster
 		 */
 		expandSuperiorCluster(mHRMController.getNodeName(), Cluster.createClusterID());
 		
 		/**
 		 * Trigger event "finished clustering" because we have created a local superior cluster 
 		 */
 		eventInitialClusteringFinished();
 	}
 	
 	/**
 	 * EVENT: detected a neighbor coordinator, we react on this event by the following steps:
 	 *   1.) expand the superior cluster to this neighbor coordinator 
 	 * 
 	 * @param pNeighborCluster the found neighbor cluster whose coordinator is a neighbor of this one
 	 * @param pFutureClusterID the clusterID for the common cluster
 	 */
 	private void eventDetectedNeighborCoordinator(ControlEntity pNeighborClusterControlEntity, Long pFutureClusterID)
 	{
 		Logging.info(this, "\n\n\n############## FOUND NEIGHBOR CLUSTER ENTITY " + pNeighborClusterControlEntity + " FOR " + mHRMController.getNodeL2Address());
 
 		/**
 		 * get the name of the target coordinator name
 		 */
 		Name tNeighborCoordinatorNode = pNeighborClusterControlEntity.superiorCoordinatorNodeName();
 		
 		if(tNeighborCoordinatorNode != null){
 			/**
 			 * Expand the superior cluster to the new detected neighbor coordinator
 			 */
 			expandSuperiorCluster(tNeighborCoordinatorNode, pFutureClusterID);
 
 			/**
 			 * Mark a local (instantiated on this physical node) coordinator for this hierarchy level as "clustered"
 			 * HINT: All local coordinators on a defined hierarchy level have the same logical hop distance.
 			 * 		 Hence, they have to be part of the same cluster.
 			 */
 			Coordinator tLocalNeighborCoordinator = null;
 			if(pNeighborClusterControlEntity instanceof Cluster){
 				Cluster tNeighborCluster = (Cluster)pNeighborClusterControlEntity;
 				
 				tLocalNeighborCoordinator = tNeighborCluster.getCoordinator();
 			}else if(pNeighborClusterControlEntity instanceof Coordinator){
 				tLocalNeighborCoordinator = (Coordinator)pNeighborClusterControlEntity;
 			}			
 			// trigger event "finished clustering" 
 			tLocalNeighborCoordinator.eventInitialClusteringFinished();
 		}else{
 			Logging.warn(this, "Invalid node name for the neighbor coordinator: " + pNeighborClusterControlEntity);
 			Logging.warn(this, "     ..skipping further processing here");
 		}
 	}
 
 	/**
 	 * Expands the superior cluster to coordinators in the same hierarchy level which are located on a defined node by:
 	 *   1.) connect to the destination node and: 
 	 * 	 	a.) use "RequestClusterParticipationProperty" to inform about the desired new superior cluster which both should belong to 
 	 * 		b.) tell the neighbor coordinator about locally known network topology data from the ARG (stored in the HRMController) 
  
 	 * @param pDestinationNodeName the name of the destination node
 	 * @param pFutureClusterID the clusterID for the desired superior cluster
 	 */
 	private void expandSuperiorCluster(Name pDestinationNodeName, Long pFutureClusterID)
 	{
 		if(pDestinationNodeName != null){
 			/**
 			 * Create the hierarchy level description for the new cluster, which should be 1 above the current one
 			 */
 			HierarchyLevel tFutureClusterHierLvl = getHierarchyLevel(); //TODO: add "+1" if the coordinator hier. level handling was fixed
 	
 			Logging.info(this, "\n\n\n############## EXPANDING TO " + pDestinationNodeName + " FOR " + mHRMController.getNodeL2Address() + ", future clusterID is " + Long.toString(pFutureClusterID) + ", future hier. level is " + tFutureClusterHierLvl.getValue());
 	
 			//TODO: was ist wenn man zu einem anderem knoten mit mehreren koordinatoren auf der ebene expandieren will?
 			
 			if(!isConnectedToNeighborCoordinator(pDestinationNodeName)) {
 				/**
 				 * Store the connection to avoid connection duplicates during later processing
 				 */
 				registerConnectionToNeighborCoordinator(pDestinationNodeName);
 				
 				/**
 				 * Describe the common cluster
 				 */
 			    Logging.log(this, "    ..creating cluster description");
 			    // create the cluster description
 				RequestClusterParticipationProperty tRequestClusterParticipationProperty = RequestClusterParticipationProperty.create(mHRMController, new HierarchyLevel(this, getHierarchyLevel().getValue() - 1), pFutureClusterID, tFutureClusterHierLvl);
 		
 				/**
 				 * Create communication session
 				 */
 			    Logging.log(this, "    ..creating new communication session");
 			    ComSession tComSession = new ComSession(mHRMController, false, this, getHierarchyLevel());
 		
 			    /**
 			     * Iterate over all local neighbor coordinators on this hierarchy level and add them as already known cluster members for the future superior cluster
 			     */
 				Logging.log(this, "    ..iterate over all coordinators on hierarchy level: " + (getHierarchyLevel().getValue() - 1));
 				// store how many local coordinators on this hierarchy level were found
 				int tKnownLocalCoordinatorsOnThisHierarchyLevel = 0;
 				for(Coordinator tLocalNeighborCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 					tKnownLocalCoordinatorsOnThisHierarchyLevel++;
 					Logging.log(this, "         ..found [" + tKnownLocalCoordinatorsOnThisHierarchyLevel + "] : " + tLocalNeighborCoordinator);
 		
 				    /**
 				     * Create communication channel
 				     */
 				    Logging.log(this, "           ..creating new communication channel");
 					ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.OUT, tLocalNeighborCoordinator, tComSession);
 					tComChannel.setRemoteClusterName(new ClusterName(mHRMController, tFutureClusterHierLvl, pFutureClusterID /* HINT: we will communicate with the new cluster -> us clusterID of new cluster */, -1));
 					
 					/**
 					 * Get get cluster for the locally found neighbor coordinator
 					 */
 					Cluster tLocalNeighborCluster = tLocalNeighborCoordinator.getCluster();
 						
 					if(tLocalNeighborCluster != null){
 						/**
 						 * Describe the locally known neighbor and its coordinator as member for the new superior cluster 
 						 */
 					    Logging.log(this, "           ..creating cluster member description for the found local neighbor: " + tLocalNeighborCluster);
 						//TODO: ClusterMemberDescription tClusterMemberDescription = 
 						tRequestClusterParticipationProperty.addSenderClusterMember(tLocalNeighborCluster);
 							
 //						/**
 //						 * Iterate over all known (local AND remote!) neighbor clusters of the current neighbor cluster and add this topology data to the cluster member description
 //						 */
 //						for(ControlEntity tClusterMemberNeighbor: tLocalNeighborCluster.getNeighborsARG()) {
 //							DiscoveryEntry tDiscoveryEntry = new DiscoveryEntry(tClusterMemberNeighbor.getCoordinatorID(), tClusterMemberNeighbor.getCoordinatorNodeName(), tClusterMemberNeighbor.getClusterID(), tClusterMemberNeighbor.superiorCoordinatorHostL2Address(), tClusterMemberNeighbor.getHierarchyLevel());
 //							tDiscoveryEntry.setPriority(tClusterMemberNeighbor.getPriority());
 //							
 //							if(tLocalNeighborCoordinator.getPathToCoordinator(tLocalNeighborCluster, tClusterMemberNeighbor) != null) {
 //								for(RoutingServiceLinkVector tVector : tLocalNeighborCoordinator.getPathToCoordinator(tLocalNeighborCluster, tClusterMemberNeighbor)) {
 //									tDiscoveryEntry.addRoutingVectors(tVector);
 //								}
 //							}
 //							tClusterMemberDescription.addDiscoveryEntry(tDiscoveryEntry);
 //						}
 					}else{
 						Logging.err(this, "expandSuperiorCluster() wasn'T able to determine the cluster for the coordinator: " + tLocalNeighborCoordinator);
 					}
 				}
 		
 				/**
 				 * Create connection requirements
 				 */
 				Description tConnectionRequirements = mHRMController.createHRMControllerDestinationDescription();
 				tConnectionRequirements.set(tRequestClusterParticipationProperty);
 		
 				/**
 				 * Get the recursive FoG layer
 				 */ 
 				FoGEntity tFoGLayer = (FoGEntity) mHRMController.getNode().getLayer(FoGEntity.class);
 		
 				/**
 				 * Connect
 				 */
 			    Logging.log(this, "    ..CONNECTING to: " + pDestinationNodeName + " with requirements: " + tConnectionRequirements);
 			    // connect via the recursive FoG layer
 			    Connection tConnection = tFoGLayer.connect(pDestinationNodeName, tConnectionRequirements, mHRMController.getNode().getIdentity());
 			    Logging.log(this, "    ..connect() FINISHED");
 				if (tConnection != null){
 					
 					
 				    mCounterOutgoingConnections++;
 		
 				    Logging.log(this, "     ..starting this OUTGOING CONNECTION as nr. " + mCounterOutgoingConnections);
 					tComSession.startConnection(null, tConnection);
 					
 	//				/**
 	//				 * Create and send ClusterDiscovery
 	//				 */
 	//			    ClusterDiscovery tBigDiscovery = new ClusterDiscovery(mHRMController.getNodeName());
 	//				for(Coordinator tCoordinator : mHRMController.getAllCoordinators(new HierarchyLevel(this, getHierarchyLevel().getValue() - 1))) {
 	//					LinkedList<Integer> tCoordinatorIDs = new LinkedList<Integer>();
 	//					for(ControlEntity tNeighbor : tCoordinator.getCluster().getNeighborsARG()) {
 	//						if(tNeighbor.getHierarchyLevel().getValue() == tCoordinator.getHierarchyLevel().getValue() - 1) {
 	//							tCoordinatorIDs.add(((ICluster) tNeighbor).getCoordinatorID());
 	//						}
 	//					}
 	//					tCoordinatorIDs.add(tCoordinator.getCluster().getCoordinatorID());
 	//					if(!pDestinationNodeName.equals(mHRMController.getNodeName())) {
 	//						int tDistance = 0;
 	//						if (pNeighborCluster instanceof ClusterProxy){
 	//							ClusterProxy tClusterProxy = (ClusterProxy) pNeighborCluster;
 	//						
 	//							tDistance = mHRMController.getClusterDistance(tClusterProxy); 
 	//						}
 	//						
 	//						NestedDiscovery tNestedDiscovery = tBigDiscovery.new NestedDiscovery(tCoordinatorIDs, pNeighborCluster.getClusterID(), pNeighborCluster.getCoordinatorID(), pNeighborCluster.getHierarchyLevel(), tDistance);
 	//						Logging.log(this, "Created " + tNestedDiscovery + " for " + pNeighborCluster);
 	//						tNestedDiscovery.setOrigin(tCoordinator.getClusterID());
 	//						tNestedDiscovery.setTargetClusterID(tNeighborClusterControlEntity.superiorCoordinatorHostL2Address().getComplexAddress().longValue());
 	//						tBigDiscovery.addNestedDiscovery(tNestedDiscovery);
 	//					}
 	//				}
 	//				// send the ClusterDiscovery to peer
 	//				tComSession.write(tBigDiscovery);
 					
 	//				for(NestedDiscovery tDiscovery : tBigDiscovery.getDiscoveries()) {
 	//					String tClusters = new String();
 	//					for(Cluster tCluster : mHRMController.getAllClusters()) {
 	//						tClusters += tCluster + ", ";
 	//					}
 	//					String tDiscoveries = new String();
 	//					for(DiscoveryEntry tEntry : tDiscovery.getDiscoveryEntries()) {
 	//						tDiscoveries += ", " + tEntry;
 	//					}
 	//					if(tDiscovery.getNeighborRelations() != null) {
 	//						for(Tuple<ClusterName, ClusterName> tTuple : tDiscovery.getNeighborRelations()) {
 	//							if(!mHRMController.isLinkedARG(tTuple.getFirst(), tTuple.getSecond())) {
 	//								Cluster tFirstCluster = mHRMController.getClusterByID(tTuple.getFirst());
 	//								Cluster tSecondCluster = mHRMController.getClusterByID(tTuple.getSecond());
 	//								if(tFirstCluster != null && tSecondCluster != null ) {
 	//									tFirstCluster.registerNeighborARG(tSecondCluster);
 	//									Logging.log(this, "Connecting " + tFirstCluster + " with " + tSecondCluster);
 	//								} else {
 	//									Logging.warn(this, "Unable to find cluster " + tTuple.getFirst() + ":" + tFirstCluster + " or " + tTuple.getSecond() + ":" + tSecondCluster + " out of \"" + tClusters + "\", cluster discovery contained " + tDiscoveries + " and CEP is " + tComSession);
 	//								}
 	//							}
 	//						}
 	//					} else {
 	//						Logging.warn(this, tDiscovery + "does not contain any neighbor relations");
 	//					}
 	//				}
 				}else{
 					Logging.err(this, "expandSuperiorCluster() wasn't able to connect to: " + pDestinationNodeName);
 				}
 			}else{
 				Logging.warn(this, "expandSuperiorCluster() skips this connection request because there exist already a connection to: " + pDestinationNodeName);
 			}
 		}else{
 			Logging.log(this, "expandSuperiorCluster() can't continue with an invalid destination");
 		}
 	}
 
 
 	
 	
 	
 	
 	
 //	public void storeAnnouncement(AnnounceRemoteCluster pAnnounce)
 //	{
 //		Logging.log(this, "Storing " + pAnnounce);
 //		if(mReceivedAnnouncements == null) {
 //			mReceivedAnnouncements = new LinkedList<AnnounceRemoteCluster>();
 //		}
 //		pAnnounce.setNegotiatorIdentification(new ClusterName(mHRMController, mParentCluster.getHierarchyLevel(), mParentCluster.superiorCoordinatorID(), mParentCluster.getClusterID()));
 //		mReceivedAnnouncements.add(pAnnounce);
 //	}
 	
//	public LinkedList<Long> getBounces()
//	{
//		return mBouncedAnnounces;
//	}
 	
 //	private LinkedList<RoutingServiceLinkVector> getPathToCoordinator(ICluster pSourceCluster, ICluster pDestinationCluster)
 //	{
 //		if(pDestinationCluster == null){
 //			return null;
 //		}
 //		
 //		if(pSourceCluster == null){
 //			return null;
 //		}
 //
 //		if(((ControlEntity)pDestinationCluster).superiorCoordinatorHostL2Address() == null){
 //			return null;
 //		}
 //		
 //		List<Route> tCoordinatorPath = mHRMController.getHRS().getCoordinatorRoutingMap().getRoute(((ControlEntity)pSourceCluster).superiorCoordinatorHostL2Address(), ((ControlEntity)pDestinationCluster).superiorCoordinatorHostL2Address());
 //		LinkedList<RoutingServiceLinkVector> tVectorList = new LinkedList<RoutingServiceLinkVector>();
 //		if(tCoordinatorPath != null) {
 //			for(Route tPath : tCoordinatorPath) {
 //				tVectorList.add(new RoutingServiceLinkVector(tPath, mHRMController.getHRS().getCoordinatorRoutingMap().getSource(tPath), mHRMController.getHRS().getCoordinatorRoutingMap().getDest(tPath)));
 //			}
 //		}
 //		return tVectorList;
 //	}
 
 
 //	@Override
 //	public void handleCoordinatorBullyAnnounce(BullyAnnounce pAnnounce, ComChannel pCEP)
 //	{
 //		/**
 //		 * the name of the cluster, which is managed by this coordinator
 //		 */
 //		ClusterName tLocalManagedClusterName = new ClusterName(mHRMController, mParentCluster.getHierarchyLevel(), mParentCluster.superiorCoordinatorID(), mParentCluster.getClusterID());
 //
 //		/*
 //		 * check whether old priority was lower than new priority
 //		 */
 ////		if(pAnnounce.getSenderPriority().isHigher(this, getCoordinatorPriority())) {
 //			/*
 //			 * check whether a coordinator is already set
 //			 */
 //			if(superiorCoordinatorComChannel() != null) {
 //				if(getCoordinatorNodeName() != null && !pAnnounce.getSenderName().equals(getCoordinatorNodeName())) {
 //					/*
 //					 * a coordinator was set earlier -> therefore inter-cluster communicatino is necessary
 //					 * 
 //					 * find the route from the managed cluster to the cluster this entity got to know the higher cluster
 //					 */
 //					List<AbstractRoutingGraphLink> tRouteARG = mHRMController.getRouteARG(mParentCluster, pCEP.getRemoteClusterName());
 //					if(tRouteARG.size() > 0) {
 //						if(mHRMController.getOtherEndOfLinkARG(pCEP.getRemoteClusterName(), tRouteARG.get(tRouteARG.size() - 1)) instanceof Cluster) {
 //							Logging.warn(this, "Not sending neighbor zone announce because another intermediate cluster has a shorter route to target");
 //							if(tRouteARG != null) {
 //								String tClusterRoute = new String();
 //								AbstractRoutingGraphNode tRouteARGNode = mParentCluster;
 //								for(AbstractRoutingGraphLink tConnection : tRouteARG) {
 //									tClusterRoute += tRouteARGNode + "\n";
 //									tRouteARGNode = mHRMController.getOtherEndOfLinkARG(tRouteARGNode, tConnection);
 //								}
 //								Logging.log(this, "cluster route to other entity:\n" + tClusterRoute);
 //							}
 //						} else {
 //							
 //							/*
 //							 * This is for the new coordinator - he is being notified about the fact that this cluster belongs to another coordinator
 //							 * 
 //							 * If there are intermediate clusters between the managed cluster of this cluster manager we do not send the announcement because in that case
 //							 * the forwarding would get inconsistent
 //							 * 
 //							 * If this is a rejection the forwarding cluster as to be calculated by the receiver of this neighbor zone announcement
 //							 */
 //							
 //							AnnounceRemoteCluster tOldCovered = new AnnounceRemoteCluster(getCoordinatorNodeName(), getHierarchyLevel(), superiorCoordinatorHostL2Address(),getCoordinatorID(), superiorCoordinatorHostL2Address().getComplexAddress().longValue());
 //							tOldCovered.setCoordinatorsPriority(superiorCoordinatorComChannel().getPeerPriority());
 //							tOldCovered.setNegotiatorIdentification(tLocalManagedClusterName);
 //							
 //							DiscoveryEntry tOldCoveredEntry = new DiscoveryEntry(mParentCluster.getCoordinatorID(), mParentCluster.getCoordinatorNodeName(), mParentCluster.superiorCoordinatorHostL2Address().getComplexAddress().longValue(), mParentCluster.superiorCoordinatorHostL2Address(), mParentCluster.getHierarchyLevel());
 //							/*
 //							 * the forwarding cluster to the newly discovered cluster has to be one level lower so it is forwarded on the correct cluster
 //							 * 
 //							 * calculation of the predecessor is because the cluster identification on the remote site is multiplexed
 //							 */
 //							tRouteARG = mHRMController.getRouteARG(mParentCluster, superiorCoordinatorComChannel().getRemoteClusterName());
 //							tOldCoveredEntry.setPriority(superiorCoordinatorComChannel().getPeerPriority());
 //							tOldCoveredEntry.setRoutingVectors(pCEP.getPath(mParentCluster.superiorCoordinatorHostL2Address()));
 //							tOldCovered.setCoveringClusterEntry(tOldCoveredEntry);
 //							//tOldCovered.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 //							pCEP.sendPacket(tOldCovered);
 //							
 //							/*
 //							 * now the old cluster is notified about the new cluster
 //							 */
 //							
 //							AnnounceRemoteCluster tNewCovered = new AnnounceRemoteCluster(pAnnounce.getSenderName(), getHierarchyLevel(), pCEP.getPeerL2Address(), pAnnounce.getCoordinatorID(), pCEP.getPeerL2Address().getComplexAddress().longValue());
 //							tNewCovered.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 //							tNewCovered.setNegotiatorIdentification(tLocalManagedClusterName);
 //							DiscoveryEntry tCoveredEntry = new DiscoveryEntry(pAnnounce.getCoordinatorID(),	pAnnounce.getSenderName(), (pCEP.getPeerL2Address()).getComplexAddress().longValue(), pCEP.getPeerL2Address(),	getHierarchyLevel());
 //							tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerL2Address()));
 //							tNewCovered.setCoveringClusterEntry(tCoveredEntry);
 //							tCoveredEntry.setPriority(pAnnounce.getSenderPriority());
 //							
 //							Logging.warn(this, "Rejecting " + (superiorCoordinatorComChannel().getPeerL2Address()).getDescr() + " in favor of " + pAnnounce.getSenderName());
 //							tNewCovered.setRejection();
 //							superiorCoordinatorComChannel().sendPacket(tNewCovered);
 //							for(ComChannel tCEP : getComChannels()) {
 //								if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerL2Address())) {
 //									tCEP.setAsParticipantOfMyCluster(true);
 //								} else {
 //									tCEP.setAsParticipantOfMyCluster(false);
 //									
 //								}
 //							}
 //							setSuperiorCoordinatorID(pAnnounce.getCoordinatorID());
 //							setSuperiorCoordinator(pCEP, pAnnounce.getSenderName(),pAnnounce.getCoordinatorID(),  pCEP.getPeerL2Address());
 //							mHRMController.setClusterWithCoordinator(getHierarchyLevel(), this);
 //							superiorCoordinatorComChannel().sendPacket(tNewCovered);
 //						}
 //					}
 //				}
 //				
 //			} else {
 //				if (pAnnounce.getCoveredNodes() != null){
 //					for(ComChannel tCEP : getComChannels()) {
 //						if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerL2Address())) {
 //							tCEP.setAsParticipantOfMyCluster(true);
 //						} else {
 //							tCEP.setAsParticipantOfMyCluster(false);
 //						}
 //					}
 //				}
 //				setSuperiorCoordinatorID(pAnnounce.getCoordinatorID());
 //				mHRMController.setClusterWithCoordinator(getHierarchyLevel(), this);
 //				setSuperiorCoordinator(pCEP, pAnnounce.getSenderName(), pAnnounce.getCoordinatorID(), pCEP.getPeerL2Address());
 //			}
 //		} else {
 //			/*
 //			 * this part is for the coordinator that intended to announce itself -> send rejection and send acting coordinator along with
 //			 * the announcement that is just gained a neighbor zone
 //			 */
 //			
 //			NeighborClusterAnnounce tUncoveredAnnounce = new NeighborClusterAnnounce(getCoordinatorName(), getHierarchyLevel(), superiorCoordinatorL2Address(), getToken(), superiorCoordinatorL2Address().getComplexAddress().longValue());
 //			tUncoveredAnnounce.setCoordinatorsPriority(superiorCoordinatorComChannel().getPeerPriority());
 //			/*
 //			 * the routing service address of the announcer is set once the neighbor zone announce arrives at the rejected coordinator because this
 //			 * entity is already covered
 //			 */
 //			
 //			tUncoveredAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 //			
 //			DiscoveryEntry tUncoveredEntry = new DiscoveryEntry(mParentCluster.getToken(), mParentCluster.getCoordinatorName(), mParentCluster.superiorCoordinatorL2Address().getComplexAddress().longValue(), mParentCluster.superiorCoordinatorL2Address(), mParentCluster.getHierarchyLevel());
 //			List<RoutableClusterGraphLink> tClusterList = mHRMController.getRoutableClusterGraph().getRoute(mParentCluster, pCEP.getRemoteClusterName());
 //			if(!tClusterList.isEmpty()) {
 //				ICluster tPredecessor = (ICluster) mHRMController.getRoutableClusterGraph().getLinkEndNode(mParentCluster, tClusterList.get(0));
 //				tUncoveredEntry.setPredecessor(new ClusterName(tPredecessor.getToken(), tPredecessor.getClusterID(), tPredecessor.getHierarchyLevel()));
 //			}
 //			tUncoveredEntry.setPriority(getCoordinatorPriority());
 //			tUncoveredEntry.setRoutingVectors(pCEP.getPath(mParentCluster.superiorCoordinatorL2Address()));
 //			tUncoveredAnnounce.setCoveringClusterEntry(tUncoveredEntry);
 //			Logging.warn(this, "Rejecting " + (superiorCoordinatorComChannel().getPeerL2Address()).getDescr() + " in favour of " + pAnnounce.getSenderName());
 //			tUncoveredAnnounce.setRejection();
 //			pCEP.sendPacket(tUncoveredAnnounce);
 //			
 //			/*
 //			 * this part is for the acting coordinator, so NeighborZoneAnnounce is sent in order to announce the cluster that was just rejected
 //			 */
 //			
 //			NeighborClusterAnnounce tCoveredAnnounce = new NeighborClusterAnnounce(pAnnounce.getSenderName(), getHierarchyLevel(), pCEP.getPeerL2Address(), pAnnounce.getToken(), (pCEP.getPeerL2Address()).getComplexAddress().longValue());
 //			tCoveredAnnounce.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 //			
 ////			List<Route> tPathToCoordinator = getCoordinator().getHRS().getCoordinatorRoutingMap().getRoute(pCEP.getSourceName(), pCEP.getPeerName());
 //			
 //			//tCoveredAnnounce.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 //			tCoveredAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 //			DiscoveryEntry tCoveredEntry = new DiscoveryEntry(pAnnounce.getToken(), pAnnounce.getSenderName(), (pCEP.getPeerL2Address()).getComplexAddress().longValue(), pCEP.getPeerL2Address(), getHierarchyLevel());
 //			tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerL2Address()));
 //			tCoveredAnnounce.setCoveringClusterEntry(tCoveredEntry);
 //			tCoveredEntry.setPriority(pAnnounce.getSenderPriority());
 //			tCoveredAnnounce.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 //			
 //			List<RoutableClusterGraphLink> tClusters = mHRMController.getRoutableClusterGraph().getRoute(mParentCluster, superiorCoordinatorComChannel().getRemoteClusterName());
 //			if(!tClusters.isEmpty()) {
 //				ICluster tNewPredecessor = (ICluster) mHRMController.getRoutableClusterGraph().getLinkEndNode(mParentCluster, tClusters.get(0));
 //				tUncoveredEntry.setPredecessor(new ClusterName(tNewPredecessor.getToken(), tNewPredecessor.getClusterID(), tNewPredecessor.getHierarchyLevel()));
 //			}
 //			Logging.log(this, "Coordinator CEP is " + superiorCoordinatorComChannel());
 //			superiorCoordinatorComChannel().sendPacket(tCoveredAnnounce);
 //		}
 //	}
 	
 //	private ICluster addAnnouncedCluster(AnnounceRemoteCluster pAnnounce, ComChannel pCEP)
 //	{
 //		if(pAnnounce.getRoutingVectors() != null) {
 //			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 //				mHRMController.getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 //			}
 //		}
 //		ClusterProxy tCluster = null;
 //		if(pAnnounce.isAnnouncementFromForeign())
 //		{
 //			Logging.log(this, "     ..creating cluster proxy");
 //			tCluster = new ClusterProxy(mParentCluster.mHRMController, pAnnounce.getCoordAddress().getComplexAddress().longValue() /* TODO: als clusterID den Wert? */, new HierarchyLevel(this, super.getHierarchyLevel().getValue() + 2),	pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken());
 //			mHRMController.setSourceIntermediateCluster(tCluster, mHRMController.getSourceIntermediateCluster(this));
 //			tCluster.setSuperiorCoordinatorID(pAnnounce.getToken());
 //			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 //			//mParentCluster.addNeighborCluster(tCluster);
 //		} else {
 //			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 //		}
 //		if(pAnnounce.getCoordinatorName() != null) {
 //			mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 //		}
 //		return tCluster;
 //	}
 //	
 
 //	@Override
 //	public void handleNeighborAnnouncement(AnnounceRemoteCluster	pAnnounce, ComChannel pCEP)
 //	{		
 //		if(pAnnounce.getCoveringClusterEntry() != null) {
 //			
 //			if(pAnnounce.isRejected()) {
 //				Logging.log(this, "Removing " + this + " as participating CEP from " + this);
 //				getComChannels().remove(this);
 //			}
 //			if(pAnnounce.getCoordinatorName() != null) {
 //				mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 //			}
 //			pCEP.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry());
 //		}
 //	}
 
 //	@Override
 //	public void eventClusterCoordinatorAvailable(ComChannel pCoordinatorComChannel, Name pCoordinatorName, int pCoordToken, L2Address pCoordinatorL2Address) 
 //	{
 //		super.eventClusterCoordinatorAvailable(pCoordinatorComChannel, pCoordinatorName, pCoordToken, pCoordinatorL2Address);
 //
 //		Logging.log(this, "Setting channel to superior coordinator to " + pCoordinatorComChannel + " for coordinator " + pCoordinatorName + " with routing address " + pCoordinatorL2Address);
 //		Logging.log(this, "Previous channel to superior coordinator was " + superiorCoordinatorComChannel() + " with name " + mCoordinatorName);
 //		setSuperiorCoordinatorComChannel(pCoordinatorComChannel);
 //		mCoordinatorName = pCoordinatorName;
 //		synchronized(this) {
 //			notifyAll();
 //		}
 //
 //		//		LinkedList<CoordinatorCEP> tEntitiesToNotify = new LinkedList<CoordinatorCEP> ();
 //		ClusterName tLocalManagedClusterName = new ClusterName(mHRMController, mParentCluster.getHierarchyLevel(), mParentCluster.getCoordinatorID(), mParentCluster.getClusterID());
 //		for(AbstractRoutingGraphNode tNeighbor: mHRMController.getNeighborsARG(mParentCluster)) {
 //			if(tNeighbor instanceof ICluster) {
 //				for(ComChannel tComChannel : getComChannels()) {
 //					if(((ControlEntity)tNeighbor).superiorCoordinatorHostL2Address().equals(tComChannel.getPeerL2Address()) && !tComChannel.isPartOfMyCluster()) {
 //						Logging.info(this, "Informing " + tComChannel + " about existence of neighbor zone ");
 //						AnnounceRemoteCluster tAnnounce = new AnnounceRemoteCluster(pCoordinatorName, getHierarchyLevel(), pCoordinatorL2Address, getCoordinatorID(), pCoordinatorL2Address.getComplexAddress().longValue());
 //						tAnnounce.setCoordinatorsPriority(superiorCoordinatorComChannel().getPeerPriority());
 //						LinkedList<RoutingServiceLinkVector> tVectorList = tComChannel.getPath(pCoordinatorL2Address);
 //						tAnnounce.setRoutingVectors(tVectorList);
 //						tAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 //						tComChannel.sendPacket(tAnnounce);
 //					}
 //					Logging.log(this, "Informed " + tComChannel + " about new neighbor zone");
 //				}
 //			}
 //		}
 //		if(mReceivedAnnouncements != null) {
 //			for(AnnounceRemoteCluster tAnnounce : mReceivedAnnouncements) {
 //				superiorCoordinatorComChannel().sendPacket(tAnnounce);
 //			}
 //		}
 //		
 //	}
 
 
 	
 	
 	
 	/**
 	 * Generates a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		//return getClass().getSimpleName() + (mParentCluster != null ? "(" + mParentCluster.toString() + ")" : "" ) + "TK(" +mToken + ")COORD(" + mCoordinatorSignature + ")@" + mLevel;
 		return toLocation() + "(" + (mParentCluster != null ? "Cluster" + mParentCluster.getGUIClusterID() + ", " : "") + idToString() + ")";
 	}
 
	/**
	 * Returns a location description about this instance
	 * 
	 * @return the location description
	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + (getHierarchyLevel().getValue() - 1);
 		
 		return tResult;
 	}
 
	/**
	 * Returns a string including the ClusterID, the coordinator ID, and the node priority
	 * 
	 * @return the complex string
	 */
 	private String idToString()
 	{
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return "ID=" + getClusterID() + ", CordID=" + getCoordinatorID() +  ", NodePrio=" + getPriority().getValue();
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 }
