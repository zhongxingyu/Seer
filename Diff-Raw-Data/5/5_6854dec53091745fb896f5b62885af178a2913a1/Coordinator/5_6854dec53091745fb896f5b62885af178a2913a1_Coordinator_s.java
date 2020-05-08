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
 
 import java.math.BigInteger;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembershipAck;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RoutingInformation;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for a coordinator instance and can be used on all hierarchy levels.
  * A cluster's elector instance is responsible for creating instances of this class.
  */
 public class Coordinator extends ControlEntity implements Localization, IEvent
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
 	 * This is the coordinator counter, which allows for globally (related to a physical simulation machine) unique coordinator IDs.
 	 */
 	private static int sNextFreeCoordinatorID = 1;
 
 	/**
 	 * Count the outgoing connections
 	 */
 	private int mCounterOutgoingConnections = 0;
 
 	/**
 	 * Stores the next free address for a cluster member
 	 */
 	private int mNextFreeClusterMemberAddress = 1;
 	
 	/**
 	 * Stores the cluster memberships of this coordinator
 	 */
 	private LinkedList<CoordinatorAsClusterMember> mClusterMemberships = new LinkedList<CoordinatorAsClusterMember>();
 	
 	/**
 	 * Stores how many announces were already sent
 	 */
 	private int mSentAnnounces = 0;
 	
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
 		setCoordinatorID(createCoordinatorID());
 		
 		// clone the HRMID of the managed cluster because it can already contain the needed HRMID prefix address
 		setHRMID(this,  mParentCluster.getHRMID().clone());
 		
 		// update the cluster ID
 		setClusterID(mParentCluster.getClusterID());
 		
 		// register itself as coordinator for the managed cluster
 		mParentCluster.eventNewLocalCoordinator(this);
 
 		// register at HRMController's internal database
 		mHRMController.registerCoordinator(this);
 
 		Logging.log(this, "\n\n\n################ CREATED COORDINATOR at hierarchy level: " + getHierarchyLevel().getValue());
 		
 		// trigger periodic Cluster announcements
 		if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 			if(HRMConfig.Hierarchy.PERIODIC_COORDINATOR_ANNOUNCEMENTS){
 				Logging.log(this, "Activating periodic coordinator announcements");
 				
 				// register next trigger for 
 				mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.PERIOD_COORDINATOR_ANNOUNCEMENTS * 2, this);
 			}
 		}
 	}
 	
 	/**
 	 * Generates a new ClusterID
 	 * 
 	 * @return the ClusterID
 	 */
 	static private synchronized int createCoordinatorID()
 	{
 		// get the current unique ID counter
 		int tResult = sNextFreeCoordinatorID * idMachineMultiplier();
 
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
 
 		Logging.log(this, "DISTRIBUTING ADDRESSES to entities at level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 		
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
 		if (getHierarchyLevel().isBaseLevel()){
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
 		double tDesiredTimePeriod = mHRMController.getPeriodSharePhase(getHierarchyLevel().getValue());
 		
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
 				Logging.log(this, "SHARE PHASE with cluster members on level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
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
 					if (getHierarchyLevel().isBaseLevel()){
 	
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
 	public synchronized void eventCoordinatorRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: Coordinator_Role_Invalid");
 
 		/**
 		 * Trigger: role invalid
 		 */
 		eventInvalidation();
 		
 		/**
 		 * Trigger: invalid coordinator
 		 */
 		distributeCoordinatorInvalidation();
 		
 		/**
 		 * Inform all superior clusters about the event and trigger the invalidation of this coordinator instance -> we leave all Bully elections because we are no longer a possible election winner
 		 */
 		if (!getHierarchyLevel().isHighest()){
 			eventAllClusterMembershipsInvalid();
 		}else{
 			Logging.log(this, "eventCoordinatorRoleInvalid() skips further signaling because hierarchy end is already reached at: " + getHierarchyLevel().getValue());
 		}
 
 		/**
 		 * Revoke own HRMID
 		 */ 
 		if(!getHRMID().isZero()){
 			eventRevokedHRMID(this, getHRMID());
 		}
 		
 		/**
 		 * Revoke all assigned HRMIDs of all cluster members
 		 */
 		revokeAssignedHRMIDsFromClusterMembers();
 		
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this coordinator now...");
 		
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCoordinator(this);
 		
 		/**
 		 * Inform the inferior cluster about our destruction
 		 */
 		mParentCluster.eventCoordinatorLost();
 	}
 	
 	/**
 	 * EVENT: all cluster membership invalid
 	 */
 	private void eventAllClusterMembershipsInvalid()
 	{
 		Logging.log(this, "EVENT: all cluster memberships invalid");
 		
 		Logging.log(this, "     ..invalidating these cluster memberships: " + mClusterMemberships);
 		while(mClusterMemberships.size() > 0) {
 			CoordinatorAsClusterMember tCoordinatorAsClusterMember = mClusterMemberships.getLast();
 			tCoordinatorAsClusterMember.eventClusterMembershipInvalid();
 		}
 	}
 	
 	/**
 	 * Revokes all HRMIDs per comm. channel
 	 */
 	private void revokeAssignedHRMIDsFromClusterMembers()
 	{
 		Logging.log(this, "###### Revoking assigned HRMIDs for all cluster members");
 
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
 		
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			// create new HRMID for cluster member
 			HRMID tHRMID = createClusterMemberAddress(mNextFreeClusterMemberAddress++);
 	
 			// store the HRMID under which the peer will be addressable from now 
 			pComChannel.setPeerHRMID(tHRMID);
 			
 			if ((pComChannel.getPeerHRMID() != null) && (!pComChannel.getPeerHRMID().equals(tHRMID))){
 				Logging.log(this, "    ..replacing HRMID " + pComChannel.getPeerHRMID().toString() + " and assign new HRMID " + tHRMID.toString() + " to " + pComChannel.getPeerL2Address());
 			}else
 				Logging.log(this, "    ..assigning new HRMID " + tHRMID.toString() + " to " + pComChannel.getPeerL2Address());
 	
 			// register this new HRMID in the local HRS and create a mapping to the right L2Address
 			Logging.log(this, "    ..creating MAPPING " + tHRMID.toString() + " to " + pComChannel.getPeerL2Address());
 			mHRMController.getHRS().mapHRMIDToL2Address(tHRMID, pComChannel.getPeerL2Address());
 			
 			// share the route to this cluster member with all other cluster members
 			shareRouteToClusterMember(pComChannel);
 			
 			// store the assignment for this comm. channel
 			pComChannel.storeAssignedHRMID(tHRMID);
 			
 			// send the packet
 			pComChannel.signalAssignHRMID(tHRMID);
 		}else{
 			Logging.log(this, "Address distribution is deactivated, no new assigned HRMID for: " + pComChannel);
 		}
 	}
 
 	/**
 	 * SEND: distribute AnnounceCoordinator messages among the neighbors which are within the given max. radius (see HRMConfig)        
 	 */
 	public static boolean USER_CTRL_COORDINATOR_ANNOUNCEMENTS = true;
 	private synchronized void distributeCoordinatorAnnouncement()
 	{
 		if(isThisEntityValid()){
 			// trigger periodic Cluster announcements
 			if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 				if (USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 					LinkedList<Cluster> tL0Clusters = mHRMController.getAllClusters(0);
					AnnounceCoordinator tAnnounceCoordinatorPacket = new AnnounceCoordinator(mHRMController.getNodeName(), getCluster().createClusterName(), mHRMController.getNodeL2Address());
 					
 					/**
 					 * Count the sent announces
 					 */
 					mSentAnnounces++;
 					
 					if(getHierarchyLevel().isBaseLevel()){
 						/**
 						 * Send cluster broadcasts in all other active L0 clusters if we are at level 0 
 						 */
 						for(Cluster tCluster : tL0Clusters){
 							tCluster.sendClusterBroadcast(tAnnounceCoordinatorPacket, true);
 						}
 					}else{
 						/**
 						 * Send cluster broadcast (to the bottom) in all active inferior clusters - either direct or indirect via the forwarding function of a higher cluster
 						 */
 						LinkedList<Cluster> tClusters = mHRMController.getAllClusters(getHierarchyLevel().getValue());
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "########## Distributing Coordinator announcement (to the bottom): " + tAnnounceCoordinatorPacket);
 							Logging.log(this, "     ..distributing in clusters: " + tClusters);
 						}
 						for(Cluster tCluster : tClusters){
 							tCluster.sendClusterBroadcast(tAnnounceCoordinatorPacket, true);
 						}
 						
 						/**
 						 * Send cluster broadcasts in all known inactive L0 clusters
 						 */
 						LinkedList<Cluster> tInactiveL0Clusters = new LinkedList<Cluster>();
 						for(Cluster tCluster : tL0Clusters){
 							if(!tCluster.getClusterActivation()){
 								tInactiveL0Clusters.add(tCluster);
 							}
 						}					
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "########## Distributing Coordinator announcement (to the side): " + tAnnounceCoordinatorPacket);
 							Logging.log(this, "     ..distributing in inactive clusters: " + tClusters);
 						}
 						for(Cluster tCluster : tInactiveL0Clusters){
 							tCluster.sendClusterBroadcast(tAnnounceCoordinatorPacket, true);
 						}
 					}
 				}else{
 					Logging.warn(this, "USER_CTRL_COORDINATOR_ANNOUNCEMENTS is set to false, this prevents the HRM system from creating a correct hierarchy");
 				}
 			}else{
 				Logging.warn(this, "HRMConfig->COORDINATOR_ANNOUNCEMENTS is set to false, this prevents the HRM system from creating a correct hierarchy");
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.warn(this, "distributeCoordinatorAnnouncement() skipped because coordinator role is already invalidated");
 			}
 		}
 	}
 
 	/**
 	 * SEND: distribute InvalidCoordinator messages among the neighbors which are within the given max. radius (see HRMConfig)        
 	 */
 	private synchronized void distributeCoordinatorInvalidation()
 	{
 		// trigger periodic Cluster announcements
 		if((HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS) && (USER_CTRL_COORDINATOR_ANNOUNCEMENTS)){
			InvalidCoordinator tInvalidCoordinatorPacket = new InvalidCoordinator(mHRMController.getNodeName(), getCluster().createClusterName(), mHRMController.getNodeL2Address());
 			/**
 			 * Send broadcasts in all locally known clusters at this hierarchy level
 			 */
 			LinkedList<Cluster> tClusters = mHRMController.getAllClusters(0); //HINT: we have to broadcast via level 0, otherwise, an inferior could already be destroyed and the invalidation message might get dropped
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 				Logging.log(this, "########## Distributing Coordinator invalidation (to the bottom): " + tInvalidCoordinatorPacket);
 				Logging.log(this, "     ..distributing in clusters: " + tClusters);
 			}
 			for(Cluster tCluster : tClusters){
 				tCluster.sendClusterBroadcast(tInvalidCoordinatorPacket, true);
 			}
 		}
 	}
 
 	/**
 	 * Returns how many announces were already sent
 	 * 
 	 * @return the number of announces
 	 */
 	public int countAnnounces()
 	{
 		return mSentAnnounces;
 	}
 	
 	/**
 	 * Implementation for IEvent::fire()
 	 */
 	@Override
 	public void fire()
 	{
 		if(isThisEntityValid()){
 			if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 				if(USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 						Logging.log(this, "###########################");
 						Logging.log(this, "###### FIRE FIRE FIRE #####");
 						Logging.log(this, "###########################");
 					}
 					
 					/**
 					 * Trigger: ClusterAnnounce distribution
 					 */
 					distributeCoordinatorAnnouncement();
 				}
 				
 				// register next trigger for 
 				mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.PERIOD_COORDINATOR_ANNOUNCEMENTS, this);
 			}
 		}else{
 			if(USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 				//Logging.warn(this, "fire() skipped because coordinator role is already invalidated");
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "announced", triggered by Elector if the election was won and this coordinator was announced to all cluster members 	 
 	 */
 	public synchronized void eventAnnouncedAsCoordinator()
 	{
 		Logging.log(this, "EVENT: announced as coordinator");
 
 		/**
 		 * Trigger: explicit cluster announcement to neighbors
 		 */ 
 		distributeCoordinatorAnnouncement();
 
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			//Logging.log(this, "EVENT ANNOUNCED - triggering address assignment for " + mParentCluster.getComChannels().size() + " cluster members");
 
 			distributeAddresses();
 		}
 		
 
 		/**
 		 * AUTO CLUSTERING
 		 */
 		if(!getHierarchyLevel().isHighest()) {
 			if (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY){ 
 				if(getHierarchyLevel().getValue() < HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 					//Logging.log(this, "EVENT ANNOUNCED - triggering clustering of this cluster's coordinator and its neighbors");
 
 					// start the clustering at the hierarchy level
 					mHRMController.cluster(this, new HierarchyLevel(this, getHierarchyLevel().getValue() + 1));
 				}else{
 					//Logging.log(this, "EVENT ANNOUNCED - stopping clustering because height limitation is reached at level: " + getHierarchyLevel().getValue());
 				}
 			}else{
 				Logging.warn(this, "EVENT ANNOUNCED - stopping clustering because automatic continuation is deactivated");
 			}
 		}
 	}
 
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) store the topology information locally
 	 *       2.) forward the announcement downward the hierarchy to all locally known clusters (where this node is the head) ("to the bottom")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorAnnouncement(ComChannel pComChannel, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "EVENT: coordinator announcement (from above): " + pAnnounceCoordinator);
 		}
 		
 		/**
 		 * Storing that the announced coordinator is a superior one of this node
 		 */
 		// is the packet still on its way from the top to the bottom AND does it not belong to an L0 coordinator?
 		if((!pAnnounceCoordinator.enteredSidewardForwarding()) && (!pAnnounceCoordinator.getSenderClusterName().getHierarchyLevel().isBaseLevel())){
 			mHRMController.registerSuperiorCoordinator(pAnnounceCoordinator.getSenderClusterName());
 		}
 
 		//HINT: we don't store the announced remote coordinator in the ARG here because we are waiting for the side-ward forwarding of the announcement
 		//      otherwise, we would store [] routes between this local coordinator and the announced remote one
 
 		/**
 		 * Record the passed clusters
 		 */
 		pAnnounceCoordinator.addGUIPassedCluster(new Long(getGUIClusterID()));
 
 		/**
 		 * Forward the coordinator announcement to all locally known clusters at this hierarchy level
 		 */
 		LinkedList<Cluster> tClusters = mHRMController.getAllClusters(getHierarchyLevel());
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "\n\n########## Forwarding Coordinator announcement: " + pAnnounceCoordinator);
 			Logging.log(this, "     ..distributing in clusters: " + tClusters);
 		}
 		for(Cluster tCluster : tClusters){
 			tCluster.sendClusterBroadcast(pAnnounceCoordinator, true);
 		}
 	}
 	
 	/**
 	 * EVENT: coordinator invalidation, we react on this by:
 	 *       1.) remove the topology information locally
 	 *       2.) forward the invalidation downward the hierarchy to all locally known clusters (where this node is the head) ("to the bottom")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pInvalidCoordinator the received invalidation
 	 */
 	@Override
 	public synchronized void eventCoordinatorInvalidation(ComChannel pComChannel, InvalidCoordinator pInvalidCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "EVENT: coordinator invalidation (from above): " + pInvalidCoordinator);
 		}
 		
 		/**
 		 * Store the announced remote coordinator in the ARG 
 		 */
 		if(!pInvalidCoordinator.getSenderClusterName().equals(this)){
 			unregisterAnnouncedCoordinatorARG(this, pInvalidCoordinator);
 		}else{
 			Logging.err(this, "eventCoordinatorInvalidation() was triggered for an invalidation of ourself, announcement: " + pInvalidCoordinator);
 		}
 
 		/**
 		 * Forward the coordinator invalidation to all locally known clusters at this hierarchy level
 		 */
 		LinkedList<Cluster> tClusters = mHRMController.getAllClusters(getHierarchyLevel());
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "\n\n########## Forwarding Coordinator invalidation: " + pInvalidCoordinator);
 			Logging.log(this, "     ..distributing in clusters: " + tClusters);
 		}
 		for(Cluster tCluster : tClusters){
 			tCluster.sendClusterBroadcast(pInvalidCoordinator, true);
 		}
 	}
 
 	/**
 	 * Creates a ClusterName object which describes this coordinator
 	 * 
 	 * @return the new ClusterName object
 	 */
 	public ClusterName createCoordinatorName()
 	{
 		ClusterName tResult = null;
 		
 		tResult = new ClusterName(mHRMController, getHierarchyLevel(), getCluster().getClusterID(), getCoordinatorID());
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: cluster membership request, a cluster requests of a coordinator to acknowledge cluster membership, triggered by the comm. session
 	 * 
 	 * @param pRemoteClusterName the description of the possible new cluster member
 	 * @param pSourceComSession the comm. session where the packet was received
 	 */
 	private int mClusterMembershipRequestNr = 0;
 	public void eventClusterMembershipRequest(ClusterName pRemoteClusterName, ComSession pSourceComSession)
 	{
 		mClusterMembershipRequestNr++;
 		
 		Logging.log(this, "EVENT: got cluster membership request (" + mClusterMembershipRequestNr + ") from: " + pRemoteClusterName);
 		
 		if(isThisEntityValid()){
 			/**
 			 * Create new cluster (member) object
 			 */
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "    ..creating new local cluster membership for: " + pRemoteClusterName + ", remote node: " + pSourceComSession.getPeerL2Address());
 			}
 			CoordinatorAsClusterMember tClusterMembership = CoordinatorAsClusterMember.create(mHRMController, this, pRemoteClusterName, pSourceComSession.getPeerL2Address());
 			
 			/**
 			 * Create the communication channel for the described cluster member
 			 */
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "     ..creating communication channel");
 			}
 			ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.IN, tClusterMembership, pSourceComSession);
 	
 			/**
 			 * Set the remote ClusterName of the communication channel
 			 */
 			tComChannel.setRemoteClusterName(pRemoteClusterName);
 	
 			/**
 			 * Trigger: comm. channel established 
 			 */
 			tClusterMembership.eventComChannelEstablished(tComChannel);
 	
 			/**
 			 * SEND: acknowledgment -> will be answered by a BullyPriorityUpdate
 			 */
 			tComChannel.signalRequestClusterMembershipAck(createCoordinatorName());
 			
 			/**
 			 * Trigger: joined a remote cluster (sends a Bully priority update)
 			 */
 			tClusterMembership.eventJoinedRemoteCluster(tComChannel);
 		}else{
 			Logging.warn(this, "eventClusterMembershipRequest() skipped because coordinator role is already invalidated");
 		}
 	}
 
 	/**
 	 * Registers a new cluster membership for this coordinator
 	 * 
 	 * @param pMembership the new cluster membership
 	 */
 	public void registerClusterMembership(CoordinatorAsClusterMember pMembership)
 	{
 		synchronized (mClusterMemberships) {
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "Registering cluster membership for: " + pMembership);
 			
 				//HINT: a check for already existing cluster memberships has to be done based on equals AND a check of the peer ClusterName
 			}
 			
 			// add this cluster membership
 			mClusterMemberships.add(pMembership);
 		}
 	}
 	
 	/**
 	 * Unregisters a new cluster membership for this coordinator
 	 * 
 	 * @param pMembership the cluster membership which should be removed
 	 */
 	public void unregisterClusterMembership(CoordinatorAsClusterMember pMembership)
 	{
 		synchronized (mClusterMemberships) {
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "Unregistering cluster membership for: " + pMembership);
 			
 				//HINT: a check for already existing cluster memberships has to be done based on equals AND a check of the peer ClusterName
 			}
 			
 			// remove this cluster membership
 			mClusterMemberships.remove(pMembership);
 		}
 	}
 
 	/**
 	 * Returns all register communication channels
 	 * 
 	 * @return the communication channels
 	 */
 	public LinkedList<ComChannel> getClusterMembershipComChannels()
 	{
 		LinkedList<ComChannel> tResult = new LinkedList<ComChannel>();
 			
 		synchronized (mClusterMemberships) {
 			for (ClusterMember tClusterMembership : mClusterMemberships){
 				tResult.addAll(tClusterMembership.getComChannels());
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Checks if a membership to a given cluster does already exist 
 	 * 
 	 * @param pCluster the ClusterName of a cluster for which the membership is searched
 	 */
 	private boolean hasMembership(ClusterName pCluster)
 	{
 		boolean tResult = false;
 		
 		//Logging.log(this, "Checking cluster membership for: " + pCluster);
 		synchronized (mClusterMemberships) {
 			for(CoordinatorAsClusterMember tClusterMembership : mClusterMemberships){
 				//Logging.log(this, "       ..cluster membership: " + tClusterMembership);
 				//Logging.log(this, "         ..comm. channels: " + tClusterMembership.getComChannels());
 				if(tClusterMembership.hasComChannel(pCluster)){
 					tResult = true;
 					break;
 				}
 			}
 		}
 		return tResult;
 	}
 	
 	/**
 	 * Returns if the initial clustering has already finished
 	 * 
 	 * @return true or false
 	 */
 	public boolean isClustered()
 	{
 		// search for an existing cluster at this hierarchy level
 		Cluster tSuperiorCluster = mHRMController.getCluster(getHierarchyLevel().getValue() + 1);
 		
 		return ((getHierarchyLevel().isHighest()) || ((tSuperiorCluster != null) && (hasMembership(tSuperiorCluster))));
 	}
 
 	/**
 	 * Returns the hierarchy Bully priority of the node
 	 * 
 	 * @return the Bully priority
 	 */
 	@Override
 	public BullyPriority getPriority() 
 	{
 		return BullyPriority.create(this, mHRMController.getHierarchyNodePriority());
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
 	 * Generates a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation() + "(" + idToString() + ")";
 	}
 
 	/**
 	 * Returns a location description about this instance
 	 * 
 	 * @return the location description
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
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
 			return "Cluster" + getGUIClusterID();
 		}else{
 			return "Cluster" + getGUIClusterID() + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
