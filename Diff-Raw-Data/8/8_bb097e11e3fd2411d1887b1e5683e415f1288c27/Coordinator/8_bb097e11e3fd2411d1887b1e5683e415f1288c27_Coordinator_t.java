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
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnounceRemoteCluster;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.ClusterDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.ClusterDiscovery.NestedDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyLeave;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RoutingInformation;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.RequestClusterParticipationProperty;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.RequestClusterParticipationProperty.ClusterMemberDescription;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Tuple;
 
 /**
  * This class is used for a coordinator instance and can be used on all hierarchy levels.
  * A cluster's elector instance is responsible for creating instances of this class.
  */
 public class Coordinator extends ControlEntity implements ICluster, Localization
 {
 	/**
 	 * This is the GUI specific cluster counter, which allows for globally unique cluster IDs.
 	 * It's only used within the GUI. 	
 	 */
 	private static int sGUICoordinatorID = 0;
 	
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
 
 	private Name mCoordinatorName = null;
 	private LinkedList<Long> mBouncedAnnounces = new LinkedList<Long>();
 	private LinkedList<AnnounceRemoteCluster> mReceivedAnnouncements;
 	
 	/**
 	 * 
 	 */
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
 		mParentCluster.setCoordinator(this);
 
 		// register at HRMController's internal database
 		mHRMController.registerCoordinator(this);
 
 		Logging.log(this, "CREATED");
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
 	 * This function is called for distributing HRMIDs among the cluster members.
 	 */
 	public void signalAddressDistribution()
 	{
 		/**
 		 * The following value is used to assign monotonously growing addresses to all cluster members.
 		 * The addressing has to start with "1".
 		 */
 		int tNextClusterMemberAddress = 1;
 
 		Logging.log(this, "DISTRIBUTING ADDRESSES to entities on level " + (getHierarchyLevel().getValue() - 1) + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 		
 		/**
 		 * Assign ourself an HRMID address
 		 */
 		// are we at the base level?
 		if(super.getHierarchyLevel().isBaseLevel()) {
 			
 			// create new HRMID for ourself
 			HRMID tOwnAddress = createClusterMemberAddress(tNextClusterMemberAddress++);
 
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
 			
 			// create new HRMID for cluster member
 			HRMID tHRMID = createClusterMemberAddress(tNextClusterMemberAddress++);
 
 			// store the HRMID under which the peer will be addressable from now 
 			tComChannel.setPeerHRMID(tHRMID);
 			
 			if ((tComChannel.getPeerHRMID() != null) && (!tComChannel.getPeerHRMID().equals(tHRMID))){
 				Logging.log(this, "    ..replacing HRMID " + tComChannel.getPeerHRMID().toString() + " and assign new HRMID " + tHRMID.toString() + " to " + tComChannel.getPeerL2Address());
 			}else
 				Logging.log(this, "    ..assigning new HRMID " + tHRMID.toString() + " to " + tComChannel.getPeerL2Address());
 
 			// create new AssignHRMID packet for the cluster member
 			AssignHRMID tAssignHRMID = new AssignHRMID(mHRMController.getNodeName(), tComChannel.getPeerHRMID(), tHRMID);
 			
 			// register this new HRMID in the local HRS and create a mapping to the right L2Address
 			Logging.log(this, "    ..creating MAPPING " + tHRMID.toString() + " to " + tComChannel.getPeerL2Address());
 			mHRMController.getHRS().mapHRMIDToL2Address(tHRMID, tComChannel.getPeerL2Address());
 			
 			// share the route to this cluster member with all other cluster members
 			shareRouteToClusterMember(tComChannel);
 			
 			// send the packet
 			tComChannel.sendPacket(tAssignHRMID);
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
 
 			sendAllSuperiorClusters(tBullyLeavePacket, true);
 		}else{
 			Logging.log(this, "eventCoordinatorRoleInvalid() skips further signaling because hierarchy end is already reached at: " + (getHierarchyLevel().getValue() - 1));
 		}
 
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this coordinator now...");
 		
 		// unregister itself as coordinator for the managed cluster
 		mParentCluster.setCoordinator(null);
 
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCoordinator(this);
 	}
 	
 	/**
 	 * @param pBullyLeavePacket
 	 */
 	private void sendAllSuperiorClusters(Serializable pPacket, boolean pIncludeLoopback)
 	{
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		// get the L2Addres of the local host
 		L2Address tLocalL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		
 		Logging.log(this, "Sending BROADCASTS from " + tLocalL2Address + " the packet " + pPacket + " to " + tComChannels.size() + " communication channels");
 		
 		for(ComChannel tComChannel : tComChannels) {
 			boolean tIsLoopback = tLocalL2Address.equals(tComChannel.getPeerL2Address());
 			
 			if (!tIsLoopback){
 				Logging.log(this, "       ..to " + tComChannel);
 			}else{
 				Logging.log(this, "       ..to LOOPBACK " + tComChannel);
 			}
 
 			if ((HRMConfig.Hierarchy.SIGNALING_INCLUDES_LOCALHOST) || (pIncludeLoopback) || (!tIsLoopback)){
 				// send the packet to one of the possible cluster members
 				tComChannel.sendPacket(pPacket);
 			}else{
 				Logging.log(this, "              ..skipping " + (tIsLoopback ? "LOOPBACK CHANNEL" : ""));
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "announced", triggered by Elector if the election was won and this coordinator was announced to all cluster members 	 
 	 */
 	public void eventAnnouncedAsCoordinator()
 	{
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			Logging.log(this, "EVENT ANNOUNCED - triggering address assignment for " + getComChannels().size() + " cluster members");
 
 			signalAddressDistribution();
 		}
 
 		
 		//TODO: ??
 		mHRMController.setSourceIntermediateCluster(this, getCluster());
 
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
 		boolean tSuperiorClusterFound = false;
 		for(Cluster tCluster : mHRMController.getAllClusters(getHierarchyLevel())) {
 			if (joinSuperiorCluster(tCluster)){
 				// we joined the superior cluster and should end the search for a superior coordinator
 				tSuperiorClusterFound = true;
 				break;
 			}
 		}
 		
 		/**
 		 * Explore the neighborhood and create a new superior cluster
 		 */
 		if (!tSuperiorClusterFound){
 			exploreNeighborhodAndCreateSuperiorCluster();
 		}
 	}
 
 	/**
 	 * Tries to join a superior cluster
 	 * 
 	 * @param pSuperiorCluster the possible new superior cluster
 	 * 
 	 * @return true or false to indicate success/error
 	 */
 	private boolean joinSuperiorCluster(ControlEntity pSuperiorCluster)
 	{
 		Logging.log(this, "\n\n\n################ JOINING EXISTING SUPERIOR CLUSTER " + pSuperiorCluster);
 
 		return false;
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
 	 *     2.) trigger event "detectedNeighborCoordinator" with increasing radius
 	 */
 	private void exploreNeighborhodAndCreateSuperiorCluster()
 	{
 		Logging.log(this, "\n\n\n################ EXPLORATION STARTED on hierarchy level " + getHierarchyLevel().getValue());
 		
 		// was the clustering already triggered?
 		if (!isClustered()){
 			// are we already at the highest hierarchy level?
 			if (!getHierarchyLevel().isHighest()){
 				int tMaxRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 		
 				Logging.log(this, "Maximum radius is " + tMaxRadius);
 		
 				/**
 				 * Create the cluster ID for the new cluster, which should consist of the control entities we connect to in the next steps
 				 */
 				Long tIDFutureCluster = Cluster.createClusterID();
 
 				// get all known neighbor clusters ordered by their radius to the parent cluster
 				List<AbstractRoutingGraphNode> tNeighborClustersForClustering = mHRMController.getNeighborClustersOrderedByRadiusInARG(mParentCluster);
 				Logging.log(this, "     ..neighborhood ordered by radius: " + tNeighborClustersForClustering);
 
 				/**
 				 * count from radius 1 to max. radius and connect to each cluster candidate
 				 */
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
 								if (!isConnectedToNeighborCoordinator(tClusterCandidateProxy.getCoordinatorHostName())){
 									
 									// get the logical distance to the neighbor
 									int tNeighborClusterDistance = mHRMController.getClusterDistance(tClusterCandidateProxy);
 									
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
 					
 					/**
 					 * Connect to all found cluster candidates
 					 */
 					for(AbstractRoutingGraphNode tNeighborCluster : tSelectedNeighborClusters) {
 						if(tNeighborCluster instanceof ControlEntity) {
 							ControlEntity tNeighborClusterControlEntity = (ControlEntity)tNeighborCluster;
 							
 							eventDetectedNeighborCoordinator(tNeighborClusterControlEntity, tIDFutureCluster);
 						}else{
 							Logging.err(this, "Unsupported neighbor object: " + tNeighborCluster);
 						}
 					}
 				}
 				
 				/**
 				 * Mark all local coordinators for this hierarchy level as "clustered"
 				 */
 				for(Coordinator tLocalCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 					// trigger event "finished clustering" 
 					tLocalCoordinator.eventInitialClusteringFinished();
 				}
 			}else{
 				Logging.warn(this,  "CLUSTERING SKIPPED, no clustering on highest hierarchy level " + getHierarchyLevel().getValue() + " needed");
 			}
 		}else{
 			Logging.warn(this, "Clustering was already triggered, clustering will be maintained");
 		}
 	}
 
 	/**
 	 * EVENT: detected a neighbor coordinator, we react on this event by the following steps:
 	 * 	 1.) use "RequestClusterParticipationProperty" for every connection to inform about the new cluster 
 	 * 
 	 * @param pNeighborCluster the found neighbor cluster whose coordinator is a neighbor of this one
 	 * @param pIDForFutureCluster the clusterID for the common cluster
 	 */
 	private void eventDetectedNeighborCoordinator(ControlEntity pNeighborCluster, Long pIDForFutureCluster)
 	{
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) mHRMController.getNode().getLayer(FoGEntity.class);
 
 		// get the central FN of this node
 		L2Address tThisHostL2Address = mHRMController.getHRS().getL2AddressFor(tFoGLayer.getCentralFN());
 
 		Logging.info(this, "\n\n\n############## FOUND INFERIOR NEIGHBOR CLUSTER " + pNeighborCluster + " FOR " + tThisHostL2Address);
 
 		/**
 		 * get the name of the target coordinator name
 		 */
 		Name tNeighborCoordinatorHostName = ((ICluster)pNeighborCluster).getCoordinatorHostName();
 		
 		if(!isConnectedToNeighborCoordinator(tNeighborCoordinatorHostName)) {
 			// store the connection to avoid connection duplicates during later processing
 			registerConnectionToNeighborCoordinator(tNeighborCoordinatorHostName);
 
 			HierarchyLevel tTargetClusterHierLvl = new HierarchyLevel(this, pNeighborCluster.getHierarchyLevel().getValue() + 1);
 
 			ControlEntity tNeighborClusterControlEntity = (ControlEntity)pNeighborCluster;
 
 			/**
 			 * Describe the common cluster
 			 */
 		    Logging.log(this, "    ..creating cluster description");
 		    // create the cluster description
			RequestClusterParticipationProperty tRequestClusterParticipationProperty = new RequestClusterParticipationProperty(new HierarchyLevel(this, getHierarchyLevel().getValue() - 1), pIDForFutureCluster, tTargetClusterHierLvl, pNeighborCluster.getCoordinatorID());
 
 			/**
 			 * Create communication session
 			 */
 		    Logging.log(this, "    ..creating new communication session");
 		    ComSession tComSession = new ComSession(mHRMController, false, getHierarchyLevel());
		    
 		    /**
 		     * Iterate over all local coordinators on this hierarchy level and add them as already known cluster members to the cluster description
 		     */
 			Logging.log(this, "    ..iterate over all coordinators on hierarchy level: " + (getHierarchyLevel().getValue() - 1));
 			// store how many local coordinators on this hierarchy level were found
 			int tKnownLocalCoordinatorsOnThisHierarchyLevel = 0;
 			for(Coordinator tLocalCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 				tKnownLocalCoordinatorsOnThisHierarchyLevel++;
 				Logging.log(this, "         ..found [" + tKnownLocalCoordinatorsOnThisHierarchyLevel + "] : " + tLocalCoordinator);
 
 			    /**
 			     * Create communication channel
 			     */
 			    Logging.log(this, "           ..creating new communication channel");
 				ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.OUT, tLocalCoordinator, tComSession);
 				tComChannel.setRemoteClusterName(new ClusterName(mHRMController, pNeighborCluster.getHierarchyLevel(), pNeighborCluster.getCoordinatorID(), pIDForFutureCluster /* HINT: we communicate with the new cluster -> us clusterID of new cluster */));
 				tComChannel.setPeerPriority(pNeighborCluster.getPriority());
				
 				// do we know the neighbor coordinator? (we check for a known coordinator of the neighbor cluster)
 				if(tNeighborClusterControlEntity.superiorCoordinatorHostL2Address() != null) {
 					Cluster tCoordinatorCluster = tLocalCoordinator.getCluster();
 					
 					/**
 					 * Describe the cluster member
 					 */
 				    Logging.log(this, "           ..creating cluster member description for the found cluster " + tCoordinatorCluster);
 					ClusterMemberDescription tClusterMemberDescription = tRequestClusterParticipationProperty.addClusterMember(tCoordinatorCluster.getClusterID(), tCoordinatorCluster.getCoordinatorID(), tCoordinatorCluster.getPriority());
 					tClusterMemberDescription.setSourceName(mHRMController.getNodeName());
 					tClusterMemberDescription.setSourceL2Address(tThisHostL2Address);
 					
 					/**
 					 * Iterate over all known neighbors of the current cluster member: we inform the connection target about this neighborhood topology
 					 */
 					for(ControlEntity tClusterMemberNeighbor: tLocalCoordinator.getCluster().getNeighborsARG()) {
 						ICluster tIClusterNeighbor = (ICluster)tClusterMemberNeighbor;
 						
 						DiscoveryEntry tDiscoveryEntry = new DiscoveryEntry(tClusterMemberNeighbor.getCoordinatorID(), tIClusterNeighbor.getCoordinatorHostName(), tClusterMemberNeighbor.getClusterID(), tClusterMemberNeighbor.superiorCoordinatorHostL2Address(), tClusterMemberNeighbor.getHierarchyLevel());
 						tDiscoveryEntry.setPriority(tClusterMemberNeighbor.getPriority());
 						
 						if(tLocalCoordinator.getPathToCoordinator(tLocalCoordinator.getCluster(), tIClusterNeighbor) != null) {
 							for(RoutingServiceLinkVector tVector : tLocalCoordinator.getPathToCoordinator(tLocalCoordinator.getCluster(), tIClusterNeighbor)) {
 								tDiscoveryEntry.addRoutingVectors(tVector);
 							}
 						}
 						tClusterMemberDescription.addDiscoveryEntry(tDiscoveryEntry);
 					}
 				}else{
 					Logging.err(this, "eventDetectedNeighborCoordinator() didn't found the L2Address of the coordinator for: " + tNeighborClusterControlEntity);
 				}
 			}
 			
 			/**
 			 * Create connection requirements
 			 */
 			Description tConnectionRequirements = mHRMController.createHRMControllerDestinationDescription();
 			tConnectionRequirements.set(tRequestClusterParticipationProperty);
 			
 		    ClusterDiscovery tBigDiscovery = new ClusterDiscovery(mHRMController.getNodeName());
 
 			/**
 			 * Connect to the neighbor coordinator
 			 */
 			Connection tConnection = null;				
 		    Logging.log(this, "    ..CONNECTING to: " + tNeighborCoordinatorHostName + " with requirements: " + tConnectionRequirements);
 			tConnection = tFoGLayer.connect(tNeighborCoordinatorHostName, tConnectionRequirements, mHRMController.getNode().getIdentity());
 		    Logging.log(this, "    ..connect() FINISHED");
 			if (tConnection != null){
 				
 				mCounterOutgoingConnections++;
 
 				Logging.log(this, "     ..starting this OUTGOING CONNECTION as nr. " + mCounterOutgoingConnections);
 				tComSession.startConnection(null, tConnection);
 	
 				
 				/**
 				 * Create and send ClusterDiscovery
 				 */
 				for(Coordinator tCoordinator : mHRMController.getAllCoordinators(new HierarchyLevel(this, getHierarchyLevel().getValue() - 1))) {
 					LinkedList<Integer> tCoordinatorIDs = new LinkedList<Integer>();
 					for(ControlEntity tNeighbor : tCoordinator.getCluster().getNeighborsARG()) {
 						if(tNeighbor.getHierarchyLevel().getValue() == tCoordinator.getHierarchyLevel().getValue() - 1) {
 							tCoordinatorIDs.add(((ICluster) tNeighbor).getCoordinatorID());
 						}
 					}
 					tCoordinatorIDs.add(tCoordinator.getCluster().getCoordinatorID());
 					if(!tNeighborCoordinatorHostName.equals(mHRMController.getNodeName())) {
 						int tDistance = 0;
 						if (pNeighborCluster instanceof ClusterProxy){
 							ClusterProxy tClusterProxy = (ClusterProxy) pNeighborCluster;
 						
 							tDistance = mHRMController.getClusterDistance(tClusterProxy); 
 						}
 						
 						NestedDiscovery tNestedDiscovery = tBigDiscovery.new NestedDiscovery(tCoordinatorIDs, pNeighborCluster.getClusterID(), pNeighborCluster.getCoordinatorID(), pNeighborCluster.getHierarchyLevel(), tDistance);
 						Logging.log(this, "Created " + tNestedDiscovery + " for " + pNeighborCluster);
 						tNestedDiscovery.setOrigin(tCoordinator.getClusterID());
 						tNestedDiscovery.setTargetClusterID(tNeighborClusterControlEntity.superiorCoordinatorHostL2Address().getComplexAddress().longValue());
 						tBigDiscovery.addNestedDiscovery(tNestedDiscovery);
 					}
 				}
 				// send the ClusterDiscovery to peer
 				tComSession.write(tBigDiscovery);
 				
 				for(NestedDiscovery tDiscovery : tBigDiscovery.getDiscoveries()) {
 					String tClusters = new String();
 					for(Cluster tCluster : mHRMController.getAllClusters()) {
 						tClusters += tCluster + ", ";
 					}
 					String tDiscoveries = new String();
 					for(DiscoveryEntry tEntry : tDiscovery.getDiscoveryEntries()) {
 						tDiscoveries += ", " + tEntry;
 					}
 					if(tDiscovery.getNeighborRelations() != null) {
 						for(Tuple<ClusterName, ClusterName> tTuple : tDiscovery.getNeighborRelations()) {
 							if(!mHRMController.isLinkedARG(tTuple.getFirst(), tTuple.getSecond())) {
 								Cluster tFirstCluster = mHRMController.getClusterByID(tTuple.getFirst());
 								Cluster tSecondCluster = mHRMController.getClusterByID(tTuple.getSecond());
 								if(tFirstCluster != null && tSecondCluster != null ) {
 									tFirstCluster.registerNeighborARG(tSecondCluster);
 									Logging.log(this, "Connecting " + tFirstCluster + " with " + tSecondCluster);
 								} else {
 									Logging.warn(this, "Unable to find cluster " + tTuple.getFirst() + ":" + tFirstCluster + " or " + tTuple.getSecond() + ":" + tSecondCluster + " out of \"" + tClusters + "\", cluster discovery contained " + tDiscoveries + " and CEP is " + tComSession);
 								}
 							}
 						}
 					} else {
 						Logging.warn(this, tDiscovery + "does not contain any neighbor relations");
 					}
 				}
 			}else{
 				Logging.err(this, "eventDetectedNeighborCoordinator() wasn't able to connect to: " + tNeighborCoordinatorHostName);
 			}
 		}else{
 			Logging.warn(this, "eventDetectedNeighborCoordinator() skips this connection request because there exist already a connection to: " + tNeighborCoordinatorHostName);
 		}
 	}
 
 	/**
 	 * EVENT: "superior cluster coordinator was announced", triggered by Elector
 	 */ 
 	public void eventSuperiorClusterCoordinatorAnnounced(BullyAnnounce pAnnouncePacket, ComChannel pComChannel)
 	{
 		// store superior coordinator data
 		setSuperiorCoordinator(pComChannel, pAnnouncePacket.getSenderName(), pAnnouncePacket.getCoordinatorID(), pComChannel.getPeerL2Address());
 	}
 
 	
 	
 	
 	
 	
 	
 	public void storeAnnouncement(AnnounceRemoteCluster pAnnounce)
 	{
 		Logging.log(this, "Storing " + pAnnounce);
 		if(mReceivedAnnouncements == null) {
 			mReceivedAnnouncements = new LinkedList<AnnounceRemoteCluster>();
 		}
 		pAnnounce.setNegotiatorIdentification(new ClusterName(mHRMController, mParentCluster.getHierarchyLevel(), mParentCluster.superiorCoordinatorID(), mParentCluster.getClusterID()));
 		mReceivedAnnouncements.add(pAnnounce);
 	}
 	
 	public LinkedList<Long> getBounces()
 	{
 		return mBouncedAnnounces;
 	}
 	
 	private LinkedList<RoutingServiceLinkVector> getPathToCoordinator(ICluster pSourceCluster, ICluster pDestinationCluster)
 	{
 		List<Route> tCoordinatorPath = mHRMController.getHRS().getCoordinatorRoutingMap().getRoute(((ControlEntity)pSourceCluster).superiorCoordinatorHostL2Address(), ((ControlEntity)pDestinationCluster).superiorCoordinatorHostL2Address());
 		LinkedList<RoutingServiceLinkVector> tVectorList = new LinkedList<RoutingServiceLinkVector>();
 		if(tCoordinatorPath != null) {
 			for(Route tPath : tCoordinatorPath) {
 				tVectorList.add(new RoutingServiceLinkVector(tPath, mHRMController.getHRS().getCoordinatorRoutingMap().getSource(tPath), mHRMController.getHRS().getCoordinatorRoutingMap().getDest(tPath)));
 			}
 		}
 		return tVectorList;
 	}
 
 	@Override
 	public Name getCoordinatorHostName() {
 		return mCoordinatorName;
 	}
 
 	@Override
 	public void setCoordinatorHostName(Name pCoordName) {
 		mCoordinatorName = pCoordName;
 	}
 
 	public void eventClusterCoordinatorAnnounced(BullyAnnounce pAnnounce, ComChannel pCEP)
 	{
 		/**
 		 * the name of the cluster, which is managed by this coordinator
 		 */
 		ClusterName tLocalManagedClusterName = new ClusterName(mHRMController, mParentCluster.getHierarchyLevel(), mParentCluster.superiorCoordinatorID(), mParentCluster.getClusterID());
 
 		/*
 		 * check whether old priority was lower than new priority
 		 */
 //		if(pAnnounce.getSenderPriority().isHigher(this, getCoordinatorPriority())) {
 			/*
 			 * check whether a coordinator is already set
 			 */
 			if(superiorCoordinatorComChannel() != null) {
 				if(getCoordinatorHostName() != null && !pAnnounce.getSenderName().equals(getCoordinatorHostName())) {
 					/*
 					 * a coordinator was set earlier -> therefore inter-cluster communicatino is necessary
 					 * 
 					 * find the route from the managed cluster to the cluster this entity got to know the higher cluster
 					 */
 					List<AbstractRoutingGraphLink> tRouteARG = mHRMController.getRouteARG(mParentCluster, pCEP.getRemoteClusterName());
 					if(tRouteARG.size() > 0) {
 						if(mHRMController.getOtherEndOfLinkARG(pCEP.getRemoteClusterName(), tRouteARG.get(tRouteARG.size() - 1)) instanceof Cluster) {
 							Logging.warn(this, "Not sending neighbor zone announce because another intermediate cluster has a shorter route to target");
 							if(tRouteARG != null) {
 								String tClusterRoute = new String();
 								AbstractRoutingGraphNode tRouteARGNode = mParentCluster;
 								for(AbstractRoutingGraphLink tConnection : tRouteARG) {
 									tClusterRoute += tRouteARGNode + "\n";
 									tRouteARGNode = mHRMController.getOtherEndOfLinkARG(tRouteARGNode, tConnection);
 								}
 								Logging.log(this, "cluster route to other entity:\n" + tClusterRoute);
 							}
 						} else {
 							
 							/*
 							 * This is for the new coordinator - he is being notified about the fact that this cluster belongs to another coordinator
 							 * 
 							 * If there are intermediate clusters between the managed cluster of this cluster manager we do not send the announcement because in that case
 							 * the forwarding would get inconsistent
 							 * 
 							 * If this is a rejection the forwarding cluster as to be calculated by the receiver of this neighbor zone announcement
 							 */
 							
 							AnnounceRemoteCluster tOldCovered = new AnnounceRemoteCluster(getCoordinatorHostName(), getHierarchyLevel(), superiorCoordinatorHostL2Address(),getCoordinatorID(), superiorCoordinatorHostL2Address().getComplexAddress().longValue());
 							tOldCovered.setCoordinatorsPriority(superiorCoordinatorComChannel().getPeerPriority());
 							tOldCovered.setNegotiatorIdentification(tLocalManagedClusterName);
 							
 							DiscoveryEntry tOldCoveredEntry = new DiscoveryEntry(mParentCluster.getCoordinatorID(), mParentCluster.getCoordinatorHostName(), mParentCluster.superiorCoordinatorHostL2Address().getComplexAddress().longValue(), mParentCluster.superiorCoordinatorHostL2Address(), mParentCluster.getHierarchyLevel());
 							/*
 							 * the forwarding cluster to the newly discovered cluster has to be one level lower so it is forwarded on the correct cluster
 							 * 
 							 * calculation of the predecessor is because the cluster identification on the remote site is multiplexed
 							 */
 							tRouteARG = mHRMController.getRouteARG(mParentCluster, superiorCoordinatorComChannel().getRemoteClusterName());
 							tOldCoveredEntry.setPriority(superiorCoordinatorComChannel().getPeerPriority());
 							tOldCoveredEntry.setRoutingVectors(pCEP.getPath(mParentCluster.superiorCoordinatorHostL2Address()));
 							tOldCovered.setCoveringClusterEntry(tOldCoveredEntry);
 							//tOldCovered.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 							pCEP.sendPacket(tOldCovered);
 							
 							/*
 							 * now the old cluster is notified about the new cluster
 							 */
 							
 							AnnounceRemoteCluster tNewCovered = new AnnounceRemoteCluster(pAnnounce.getSenderName(), getHierarchyLevel(), pCEP.getPeerL2Address(), pAnnounce.getCoordinatorID(), pCEP.getPeerL2Address().getComplexAddress().longValue());
 							tNewCovered.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 							tNewCovered.setNegotiatorIdentification(tLocalManagedClusterName);
 							DiscoveryEntry tCoveredEntry = new DiscoveryEntry(pAnnounce.getCoordinatorID(),	pAnnounce.getSenderName(), (pCEP.getPeerL2Address()).getComplexAddress().longValue(), pCEP.getPeerL2Address(),	getHierarchyLevel());
 							tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerL2Address()));
 							tNewCovered.setCoveringClusterEntry(tCoveredEntry);
 							tCoveredEntry.setPriority(pAnnounce.getSenderPriority());
 							
 							Logging.warn(this, "Rejecting " + (superiorCoordinatorComChannel().getPeerL2Address()).getDescr() + " in favor of " + pAnnounce.getSenderName());
 							tNewCovered.setRejection();
 							superiorCoordinatorComChannel().sendPacket(tNewCovered);
 							for(ComChannel tCEP : getComChannels()) {
 								if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerL2Address())) {
 									tCEP.setAsParticipantOfMyCluster(true);
 								} else {
 									tCEP.setAsParticipantOfMyCluster(false);
 									
 								}
 							}
 							setSuperiorCoordinatorID(pAnnounce.getCoordinatorID());
 							setSuperiorCoordinator(pCEP, pAnnounce.getSenderName(),pAnnounce.getCoordinatorID(),  pCEP.getPeerL2Address());
 							mHRMController.setClusterWithCoordinator(getHierarchyLevel(), this);
 							superiorCoordinatorComChannel().sendPacket(tNewCovered);
 						}
 					}
 				}
 				
 			} else {
 				if (pAnnounce.getCoveredNodes() != null){
 					for(ComChannel tCEP : getComChannels()) {
 						if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerL2Address())) {
 							tCEP.setAsParticipantOfMyCluster(true);
 						} else {
 							tCEP.setAsParticipantOfMyCluster(false);
 						}
 					}
 				}
 				setSuperiorCoordinatorID(pAnnounce.getCoordinatorID());
 				mHRMController.setClusterWithCoordinator(getHierarchyLevel(), this);
 				setSuperiorCoordinator(pCEP, pAnnounce.getSenderName(), pAnnounce.getCoordinatorID(), pCEP.getPeerL2Address());
 			}
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
 	}
 	
 	private ICluster addAnnouncedCluster(AnnounceRemoteCluster pAnnounce, ComChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				mHRMController.getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		ClusterProxy tCluster = null;
 		if(pAnnounce.isAnnouncementFromForeign())
 		{
 			Logging.log(this, "     ..creating cluster proxy");
 			tCluster = new ClusterProxy(mParentCluster.mHRMController, pAnnounce.getCoordAddress().getComplexAddress().longValue() /* TODO: als clusterID den Wert? */, new HierarchyLevel(this, super.getHierarchyLevel().getValue() + 2),	pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken());
 			mHRMController.setSourceIntermediateCluster(tCluster, mHRMController.getSourceIntermediateCluster(this));
 			tCluster.setSuperiorCoordinatorID(pAnnounce.getToken());
 			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			//mParentCluster.addNeighborCluster(tCluster);
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 		}
 		if(pAnnounce.getCoordinatorName() != null) {
 			mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 		}
 		return tCluster;
 	}
 	
 	@Override
 	public void handleNeighborAnnouncement(AnnounceRemoteCluster	pAnnounce, ComChannel pCEP)
 	{		
 		if(pAnnounce.getCoveringClusterEntry() != null) {
 			
 			if(pAnnounce.isRejected()) {
 				Logging.log(this, "Removing " + this + " as participating CEP from " + this);
 				getComChannels().remove(this);
 			}
 			if(pAnnounce.getCoordinatorName() != null) {
 				mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 			}
 			pCEP.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry());
 		}
 	}
 
 	@Override
 	public void setSuperiorCoordinator(ComChannel pCoordinatorComChannel, Name pCoordinatorName, int pCoordToken, L2Address pCoordinatorL2Address) 
 	{
 		super.setSuperiorCoordinator(pCoordinatorComChannel, pCoordinatorName, pCoordToken, pCoordinatorL2Address);
 
 		Logging.log(this, "Setting channel to superior coordinator to " + pCoordinatorComChannel + " for coordinator " + pCoordinatorName + " with routing address " + pCoordinatorL2Address);
 		Logging.log(this, "Previous channel to superior coordinator was " + superiorCoordinatorComChannel() + " with name " + mCoordinatorName);
 		setSuperiorCoordinatorComChannel(pCoordinatorComChannel);
 		mCoordinatorName = pCoordinatorName;
 		synchronized(this) {
 			notifyAll();
 		}
 
 		//		LinkedList<CoordinatorCEP> tEntitiesToNotify = new LinkedList<CoordinatorCEP> ();
 		ClusterName tLocalManagedClusterName = new ClusterName(mHRMController, mParentCluster.getHierarchyLevel(), mParentCluster.getCoordinatorID(), mParentCluster.getClusterID());
 		for(AbstractRoutingGraphNode tNeighbor: mHRMController.getNeighborsARG(mParentCluster)) {
 			if(tNeighbor instanceof ICluster) {
 				for(ComChannel tComChannel : getComChannels()) {
 					if(((ControlEntity)tNeighbor).superiorCoordinatorHostL2Address().equals(tComChannel.getPeerL2Address()) && !tComChannel.isPartOfMyCluster()) {
 						Logging.info(this, "Informing " + tComChannel + " about existence of neighbor zone ");
 						AnnounceRemoteCluster tAnnounce = new AnnounceRemoteCluster(pCoordinatorName, getHierarchyLevel(), pCoordinatorL2Address, getCoordinatorID(), pCoordinatorL2Address.getComplexAddress().longValue());
 						tAnnounce.setCoordinatorsPriority(superiorCoordinatorComChannel().getPeerPriority());
 						LinkedList<RoutingServiceLinkVector> tVectorList = tComChannel.getPath(pCoordinatorL2Address);
 						tAnnounce.setRoutingVectors(tVectorList);
 						tAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 						tComChannel.sendPacket(tAnnounce);
 					}
 					Logging.log(this, "Informed " + tComChannel + " about new neighbor zone");
 				}
 			}
 		}
 		if(mReceivedAnnouncements != null) {
 			for(AnnounceRemoteCluster tAnnounce : mReceivedAnnouncements) {
 				superiorCoordinatorComChannel().sendPacket(tAnnounce);
 			}
 		}
 		
 	}
 
 
 	
 	
 	
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
 
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + (getHierarchyLevel().getValue() - 1);
 		
 		return tResult;
 	}
 
 	private String idToString()
 	{
 		if (getHRMID() == null){
 			return "ID=" + getClusterID() + ", CordID=" + getCoordinatorID() +  ", NodePrio=" + getPriority().getValue();
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 }
