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
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnounceRemoteCluster;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembershipAck;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a clusters on a defined hierarchy level.
  * 
  */
 public class Cluster extends ClusterProxy implements IEvent
 {
 	/**
 	 * For using this class within (de-)serialization.
 	 */
 	private static final long serialVersionUID = -7486131336574637721L;
 
 	/**
 	 * This is the cluster counter, which allows for globally (related to a physical simulation machine) unique cluster IDs.
 	 */
 	private static long sNextFreeClusterID = 1;
 
 	/**
 	 * Stores if the neighborhood is already initialized
 	 */
 	private boolean mNeighborInitialized = false;
 	
 	private LinkedList<AnnounceRemoteCluster> mReceivedAnnounces = null;
 
 	/**
 	 * Stores a reference to the local coordinator instance if the local router is also the coordinator for this cluster
 	 */
 	private Coordinator mCoordinator = null;
 	
 	/**
 	/**
 	 * This is the constructor of a cluster object. At first such a cluster is identified by its cluster
 	 * ID and the hierarchical level. Later on - once a coordinator is found, it is additionally identified
 	 * by a token the coordinator sends to all participants. In contrast to the cluster token the identity is used
 	 * to filter potential participants that may be used for the election of a coordinator.
 	 * 
 	 * Constructor
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster, a value of "-1" triggers the creation of a new ID
 	 */
 	private Cluster(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID)
 	{
 		super(pHRMController, pHierarchyLevel, null, -1, null);
 		
 		Logging.log(this, "CONSTRUCTOR got ClusterID: " + pClusterID);
 		
 		// set the ClusterID
 		if ((pClusterID == null) || (pClusterID < 0)){
 			// create an ID for the cluster
 			setClusterID(createClusterID());
 
 			Logging.log(this, "ClusterID - created unique clusterID " + getClusterID() + "(" + getGUIClusterID() + ")");
 		}else{
 			// use the ClusterID from outside
 			setClusterID(pClusterID);
 
 			Logging.log(this, "ClusterID - using pre-defined clusterID " + getClusterID() + "(" + getGUIClusterID() + ")");
 		}
 
 		mReceivedAnnounces = new LinkedList<AnnounceRemoteCluster>();
 
 		// register at HRMController's internal database
 		mHRMController.registerCluster(this);
 
 		Logging.log(this, "\n\n\n################ CREATED CLUSTER on hierarchy level: " + (getHierarchyLevel().getValue()));
 
 		// detect neighbor clusters, increase the Bully priority based on the local connectivity
 		initializeNeighborhood();
 
 		// creates new elector object, which is responsible for Bully based election processes
 		mElector = new Elector(mHRMController, this); //TODO: move to ClusterProxy
 	}
 	
 	/**
 	 * Factory function: create a cluster
 	 * 
 	 * @param pHrmController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster, a value of "-1" triggers the creation of a new ID
 	 * 
 	 * @return the new Cluster object
 	 */
 	static public Cluster create(HRMController pHrmController, Long pClusterID, HierarchyLevel pHierarchyLevel)
 	{
 		return new Cluster(pHrmController, pHierarchyLevel, pClusterID);
 	}
 
 	/**
 	 * Factory function: create a base hierarchy level cluster
 	 * 
 	 * @param pHrmController the local HRMController instance
 	 * 
 	 * @return the new Cluster object
 	 */
 	static public Cluster createBaseCluster(HRMController pHrmController)
 	{
 		return new Cluster(pHrmController, HierarchyLevel.createBaseLevel(), null);
 	}
 
 	/**
 	 * EVENT: cluster communication available, triggered by the comm. session 
 	 */
 	public void eventCommunicationAvailable()
 	{
 		Logging.log(this, "EVENT: communication available");
 		
 		boolean tStartBaseLevel =  ((getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.START_AUTOMATICALLY_BASE_LEVEL));
 		
 		// start coordinator election for the created HRM instance if desired
 		if(((!getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY)) || (tStartBaseLevel)){
 			Logging.log(this, "      ..starting ELECTION");
 			mElector.startElection();
 		}
 	}
 
 	/**
 	 * Detects neighbor clusters and increases the cluster's Bully priority based on the local connectivity. 
 	 */
 	private void initializeNeighborhood()
 	{
 		Logging.log(this, "Checking local connectivity for increasing priority " + getPriority().getValue());
 		
 		for(Cluster tCluster : mHRMController.getAllClusters())
 		{
 			if ((tCluster.getHierarchyLevel().equals(getHierarchyLevel())) && (tCluster != this))
 			{
 				Logging.log(this, "      ..found known neighbor cluster: " + tCluster);
 				
 				// add this cluster as neighbor to the already known one
 				tCluster.registerNeighborARG(this);
 			}
 		}
 		
 		mNeighborInitialized = true;
 
 		// register next trigger for 
 		mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.INTERNAL_CLUSTER_ANNOUNCEMENTS * 2, this);
 	}
 
 	/**
 	 * Returns true if the neighborhood is already initialized - otherwise false
 	 * This function is used by the elector to make sure that the local neighborhood is already probed and initialized.
 	 *  
 	 * @return true of false
 	 */
 	public boolean isNeighborHoodInitialized()
 	{
 		return mNeighborInitialized;
 	}
 	
 	/**
 	 * Generates a new ClusterID
 	 * 
 	 * @return the ClusterID
 	 */
 	static public synchronized long createClusterID()
 	{
 		// get the current unique ID counter
 		long tResult = sNextFreeClusterID * idMachineMultiplier();
 
 		// make sure the next ID isn't equal
 		sNextFreeClusterID++;
 	
 		return tResult;
 	}
 	
 	/**
 	 * Creates a ClusterName object which describes this cluster
 	 * 
 	 * @return the new ClusterName object
 	 */
 	public ClusterName createClusterName()
 	{
 		ClusterName tResult = null;
 		
 		tResult = new ClusterName(mHRMController, getHierarchyLevel(), getClusterID(), getCoordinatorID());
 		
 		return tResult;
 	}
 	
 	/**
 	 * SEND: distribute AnnounceCluster messages among the neighbors which are within the given max. radius (see HRMConfig)        
 	 */
 	public void distributeClusterAnnouncement()
 	{
 		AnnounceCluster tAnnounceClusterPacket = new AnnounceCluster(mHRMController.getNodeName(), createClusterName(), mHRMController.getNodeName());
 		Logging.log(this, "\n\n########## Distributing Cluster announcement: " + tAnnounceClusterPacket);
		sendClusterBroadcast(tAnnounceClusterPacket, true);
 	}
 
 	/**
 	 * Implementation for IEvent::fire()
 	 */
 	@Override
 	public void fire()
 	{
 		Logging.log(this, "###########################");
 		Logging.log(this, "###### FIRE FIRE FIRE #####");
 		Logging.log(this, "###########################");
 		
 		/**
 		 * Trigger: ClusterAnnounce distribution
 		 */
 		distributeClusterAnnouncement();
 		
 		// register next trigger for 
 		mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.INTERNAL_CLUSTER_ANNOUNCEMENTS, this);
 	}
 
 	/**
 	 * EVENT: cluster announcement
 	 * 
 	 * @param pAnnounceCluster the received announcement
 	 */
 	@Override
 	public void eventClusterAnnouncement(AnnounceCluster pAnnounceCluster)
 	{
 		Logging.log(this, "EVENT: cluster announcement: " + pAnnounceCluster);
 		
 		/**
 		 * Parse the announcement and update the ARG 
 		 */
 		if(!pAnnounceCluster.getSenderClusterName().equals(this)){
 			registerAnnouncedClusterARG(pAnnounceCluster);
 		}
 		
 		/**
 		 * get locally known neighbors for this cluster and hierarchy level
 		 */
 		LinkedList<ControlEntity> tLocallyKnownNeighbors = getNeighborsARG();
 		if(tLocallyKnownNeighbors.size() > 0){
 			Logging.log(this, "      ..found " + tLocallyKnownNeighbors.size() + " neighbors: " + tLocallyKnownNeighbors);
 
 			/**
 			 * transition from one cluster to the next one: decrease TTL
 			 */
 			pAnnounceCluster.decreaseTTL();
 			
 			/**
 			 * forward the announcement if the TTL is still okay
 			 */
 			if(pAnnounceCluster.isTTLOkay()){
 				for(ControlEntity tLocallyKnownNeighbor: tLocallyKnownNeighbors){
 					if(tLocallyKnownNeighbor instanceof Cluster){
 						/**
 						 * Get the neighbor Cluster object
 						 */
 						Cluster tLocallyKnownNeighborCluster = (Cluster)tLocallyKnownNeighbor;
 						
 						/**
 						 * Forward the announcement
 						 */
 						Logging.log(this, "     ..fowarding this event to locally known neighbor cluster: " + tLocallyKnownNeighborCluster);
 						tLocallyKnownNeighborCluster.forwardClusterAnnouncement(pAnnounceCluster);
 					}else{
 						Logging.log(this, "Ignoring stored neighbor of uninteresting type in ARG: " + tLocallyKnownNeighbor);
 					}
 				}
 			}else{
 				Logging.log(this, "TTL exceeded for cluster announcement: " + pAnnounceCluster);
 			}
 		}else{
 			Logging.log(this, "No neighbors found, ending forwarding of: " + pAnnounceCluster);
 		}
 	}
 
 	/**
 	 * Store the announced cluster within the local ARG
 	 * 
 	 * @param pAnnounceCluster the announcement
 	 */
 	private void registerAnnouncedClusterARG(AnnounceCluster pAnnounceCluster)
 	{
 		ClusterName tRemoteClusterName = pAnnounceCluster.getSenderClusterName();
 
 		/**
 		 * Storing the ARG node for this announced remote cluster
 		 */
 		Logging.log(this, "Registering ANNOUNCED REMOTE CLUSTER: " + tRemoteClusterName);
 		if(!mHRMController.isKnownARG(tRemoteClusterName)){
 			Logging.log(this, "STORING PROXY FOR ANNOUNCED REMOTE CLUSTER: " + tRemoteClusterName);
 
 			ClusterProxy tClusterProxy = ClusterProxy.create(mHRMController, tRemoteClusterName, pAnnounceCluster.getSenderClusterCoordinatorNodeName());
 			
 			mHRMController.registerNodeARG(tClusterProxy);
 		}else{
 			Logging.log(this, "     ..already known remote cluster: " + tRemoteClusterName);
 		}
 		
 		/**
 		 * Storing the route to this announced remote cluster
 		 */
 	}
 
 	/**
 	 * Forwards a cluster announcement by sending it to its coordinator
 	 * 
 	 * @param pAnnounceCluster the cluster announcement
 	 */
 	private void forwardClusterAnnouncement(AnnounceCluster pAnnounceCluster)
 	{
 		Logging.log(this, "Forwarding cluster announcement: " + pAnnounceCluster);
 		
 		// is this node the local coordinator for this cluster?
 		if(hasLocalCoordinator()){
 			// forward this announcement to all remote cluster members
 			sendClusterBroadcast(pAnnounceCluster);
 		}else{
 			// forward this announcement to the coordinator and let it forward the announcement to the cluster members
 			sendCoordinator(pAnnounceCluster);
 		}
 	}
 	
 	/**
 	 * Determines the coordinator of this cluster. It is "null" if the election was lost or hasn't finished yet. 
 	 * 
 	 * @return the cluster's coordinator
 	 */
 	@Override
 	public Coordinator getCoordinator()
 	{
 		return mCoordinator;
 	}
 	
 	/**
 	 * Determines if a coordinator is known.
 	 * 
 	 * @return true if the coordinator is elected and known, otherwise false
 	 */
 	public boolean hasLocalCoordinator()
 	{
 		return (mCoordinator != null);
 	}
 	
 	/**
 	 * EVENT: new local coordinator, triggered by the Coordinator
 	 * 
 	 * @param pCoordinator the new coordinator, which is located on this node
 	 */
 	public void eventNewLocalCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "EVENT: new local coordinator: " + pCoordinator + ", old one is: " + mCoordinator);
 		
 		// set the coordinator
 		mCoordinator = pCoordinator;
 		
 		// update the stored unique ID for the coordinator
 		if (pCoordinator != null){
 			setSuperiorCoordinatorID(pCoordinator.getCoordinatorID());
 
 			// update the descriptive string about the coordinator
 			setSuperiorCoordinatorDescription(mCoordinator.toLocation());
 		}
 	}
 	
 	/**
 	 * Returns the machine-local ClusterID (excluding the machine specific multiplier)
 	 * 
 	 * @return the machine-local ClusterID
 	 */
 	public long getGUIClusterID()
 	{
 		if (getClusterID() != null)
 			return getClusterID() / idMachineMultiplier();
 		else
 			return -1;
 	}
 	
 	/**
 	 * EVENT: coordinator lost
 	 */
 	public void eventCoordinatorLost()
 	{
 		Logging.log(this, "EVENT: coordiantor was lost");
 		
 		/**
 		 * Revoke HRMID of physical node if we are on base hierarchy level
 		 */ 
 		if(getHierarchyLevel().isBaseLevel()){
 			Logging.log(this, "Revoking physical node HRMID: " + getHRMID());
 			
 			eventRevokedHRMID(this, getHRMID());
 		}
 	}
 	
 	/**
 	 * EVENT: notifies that a communication channel is became available
 	 * 
 	 * @param pComChannel the communication channel which became available
 	 */
 	public void eventComChannelEstablished(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: ComChannel established for " + pComChannel);
 		
 		boolean tCoordinatorIsFixed = false;
 		
 		// does the coordinator already know its superior coordinator?
 		if (getCoordinator() != null){
 			if (getCoordinator().superiorCoordinatorKnown()){
 				tCoordinatorIsFixed = true;
 			}
 		}
 		
 		/**
 		 * TRIGGER: election restart (do this only if election was already started)
 		 */
 		if (!tCoordinatorIsFixed){
 			if (mElector.wasStarted()){
 				mElector.startElection();
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "lost cluster member", triggered by Elector in case a member left the election 
 
 	 * @param pComChannel the comm. channel of the lost cluster member
 	 */
 	@Override
 	public void eventClusterMemberLost(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: lost cluster member, comm. channel: " + pComChannel);
 		
 		// unregister the comm. channel
 		unregisterComChannel(pComChannel);
 		
 		Logging.log(this, "      ..remaining comm. channels: " + getComChannels());
 
 		// no further external candidates available/known (all candidates are gone) ?
 		if (countClusterMembers() < 1){
 			/**
 			 * TRIGGER: all cluster members are gone, we destroy the coordinator
 			 */
 			if (getCoordinator() != null){
 				getCoordinator().eventCoordinatorRoleInvalid();
 			}else{
 				Logging.warn(this, "eventClusterMemberLost() can't find the coordinator");
 			}
 			/**
 			 * TRIGGER: all cluster members are gone, we destroy the cluster
 			 */
 			eventClusterLostAllMembers();
 		}			 
 	}
 
 	/**
 	 * EVENT: "lost all members", triggered by ourself in case the last member left the election 
 	 */
 	private void eventClusterLostAllMembers()
 	{
 		Logging.log(this, "============ EVENT: Cluster_Lost_All_Members");
 		Logging.log(this, "     ..knowing these comm. channels: " + getComChannels());
 
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this cluster now...");
 		
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCluster(this);
 	}
 
 	/**
 	 * EVENT: got membership request, an inferior coordinator requests cluster membership, the event is triggered by the comm. session because of some comm. end point at remote side
 	 * 
 	 * @param pRemoteClusterName the description of the possible new cluster member
 	 * @param pSourceComSession the comm. session where the packet was received
 	 */
 	public void eventMembershipRequest(ClusterName pRemoteClusterName, ComSession pSourceComSession)
 	{
 		Logging.log(this, "EVENT: got a membership request from: " + pRemoteClusterName);
 		
 		/**
 		 * Create the communication channel for the described cluster member
 		 */
 		Logging.log(this, "     ..creating communication channel");
 		ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.IN, this, pSourceComSession);
 
 		/**
 		 * Set the remote ClusterName of the communication channel
 		 */
 		tComChannel.setRemoteClusterName(pRemoteClusterName);
 
 		/**
 		 * SEND: acknowledgment -> will be answered by a BullyPriorityUpdate
 		 */
 		RequestClusterMembershipAck tRequestClusterMembershipAckPacket = new RequestClusterMembershipAck(mHRMController.getNodeName(), getHRMID(), createClusterName());
 		tComChannel.sendPacket(tRequestClusterMembershipAckPacket);
 
 		/**
 		 * Trigger event "cluster member joined"
 		 */
 		eventClusterMemberJoined(tComChannel);
 	}
 	
 	/**
 	 * EVENT: detected additional cluster member, the event is triggered by ourself
 	 * 
 	 * @param pComChannel the comm. channel of the new cluster member
 	 */
 	private void eventClusterMemberJoined(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: lost cluster member, comm. channel: " + pComChannel);
 		
 		//TODO: should we do something additional here?
 
 		if (getCoordinator() != null){
 			/**
 			 * Trigger: assign new HRMID
 			 */
 			getCoordinator().eventClusterMemberNeedsHRMID(pComChannel);
 		}else{
 			Logging.warn(this, "Coordinator missing, we cannot assign a new HRMID to the joined cluster member behind comm. channel: " + pComChannel);
 		}
 	}
 
 	
 	
 	
 	
 	
 	public void eventClusterCoordinatorAvailable2(ComChannel pCoordinatorComChannel, Name pCoordinatorName, int pCoordinatorID, L2Address pCoordinatorL2Address)
 	{
 		super.eventClusterCoordinatorAvailable(pCoordinatorComChannel, pCoordinatorName, pCoordinatorID, pCoordinatorL2Address, null);
 
 		L2Address tLocalCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 	
 		// make sure that in case of a programming mistake, the right unique ID of the superior coordinator is returned
 		setCoordinatorID(pCoordinatorID);
 		
 		mCoordinatorNodeName = pCoordinatorName;
 		if(superiorCoordinatorComChannel() == null) {
 			// store the L2Address of the superior coordinator 
 			setSuperiorCoordinatorHostL2Address(mHRMController.getHRS().getCentralFNL2Address());
 		} else {
 			// store the L2Address of the superior coordinator 
 			setSuperiorCoordinatorHostL2Address(pCoordinatorL2Address);
 
 			mHRMController.getHRS().mapFoGNameToL2Address(pCoordinatorName, pCoordinatorL2Address);
 			
 			if(pCoordinatorComChannel.getRouteToPeer() != null && !pCoordinatorComChannel.getRouteToPeer().isEmpty()) {
 				mHRMController.getHRS().registerNode((L2Address) pCoordinatorL2Address, false);
 				mHRMController.getHRS().registerRoute(tLocalCentralFNL2Address, pCoordinatorComChannel.getPeerL2Address(), pCoordinatorComChannel.getRouteToPeer());
 			}
 		}
 		
 		Logging.log(this, "This cluster has the following neighbors: " + getNeighborsARG());
 		for(ControlEntity tNeighbor : getNeighborsARG()) {
 			if(tNeighbor instanceof Cluster) {
 				Cluster tNeighborCluster = (Cluster)tNeighbor;
 				
 				Logging.log(this, "CLUSTER-CEP - found already known neighbor cluster: " + tNeighborCluster);
 
 				Logging.log(this, "Preparing neighbor zone announcement");
 				AnnounceRemoteCluster tAnnounce = new AnnounceRemoteCluster(pCoordinatorName, getHierarchyLevel(), pCoordinatorL2Address, superiorCoordinatorID(), getClusterID());
 				tAnnounce.setCoordinatorsPriority(getPriority()); //TODO : ???
 				if(pCoordinatorComChannel != null) {
 					tAnnounce.addRoutingVector(new RoutingServiceLinkVector(pCoordinatorComChannel.getRouteToPeer(), tLocalCentralFNL2Address, pCoordinatorComChannel.getPeerL2Address()));
 				}
 				tNeighborCluster.announceNeighborCoord(tAnnounce, pCoordinatorComChannel);
 			}
 		}
 		if(mReceivedAnnounces.isEmpty()) {
 			Logging.log(this, "No announces came in while no coordinator was set");
 		} else {
 			Logging.log(this, "sending old announces");
 			while(!mReceivedAnnounces.isEmpty()) {
 				if(superiorCoordinatorComChannel() != null)
 				{
 					// OK, we have to notify the other node via socket communication, so this cluster has to be at least one hop away
 					superiorCoordinatorComChannel().sendPacket(mReceivedAnnounces.removeFirst());
 				} else {
 					/*
 					 * in this case this announcement came from a neighbor intermediate cluster
 					 */
 //					handleNeighborAnnouncement(mReceivedAnnounces.removeFirst(), pCoordinatorComChannel);
 				}
 			}
 		}
 	}
 	
 //	private ICluster addAnnouncedCluster(AnnounceRemoteCluster pAnnounce, ComChannel pCEP)
 //	{
 //		if(pAnnounce.getRoutingVectors() != null) {
 //			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 //				mHRMController.getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 //			}
 //		}
 //		Cluster tCluster = mHRMController.getClusterByID(new ClusterName(mHRMController, pAnnounce.getLevel(), pAnnounce.getToken(), pAnnounce.getClusterID()));
 //		if(tCluster != null) {
 //			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 //			registerNeighborARG(tCluster);
 //		}else{
 //			Logging.log(this, "     ..creating cluster proxy");
 //			ClusterProxy tNeighborCluster = new ClusterProxy(mHRMController, pAnnounce.getClusterID(), getHierarchyLevel(), pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken());
 //			mHRMController.setSourceIntermediateCluster(tNeighborCluster, this);
 //			tNeighborCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 //			
 //			mHRMController.getHRS().mapFoGNameToL2Address(tNeighborCluster.getCoordinatorHostName(),  pAnnounce.getCoordAddress());
 //			
 //			registerNeighborARG(tCluster);
 //		}
 //		
 //		if(pAnnounce.getCoordinatorName() != null) {
 //			mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 //		}
 //		return tCluster;
 //	}
 	
 //	public void handleNeighborAnnouncement(AnnounceRemoteCluster pAnnounce, ComChannel pComChannel)
 //	{
 //		if(!pAnnounce.getCoordinatorName().equals(mHRMController.getNodeName())) {
 //			Logging.log(this, "Received announcement of foreign cluster");
 //		}
 //		
 //		if(getHierarchyLevel().isBaseLevel()) {
 //			if(pComChannel != null) {
 //				L2Address tLocalCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 //				if(!tLocalCentralFNL2Address.equals(pComChannel.getPeerL2Address()) && pComChannel.getRouteToPeer() != null) {
 //					RoutingServiceLinkVector tLink = new RoutingServiceLinkVector(pComChannel.getRouteToPeer().clone(),  tLocalCentralFNL2Address, pComChannel.getPeerL2Address());
 //					pAnnounce.addRoutingVector(tLink);
 //					Logging.log(this, "Added routing vector " + tLink);
 //				}
 //				pAnnounce.isForeignAnnouncement();
 //			}
 //		} else {
 //			if(mHRMController.getClusterWithCoordinatorOnLevel(getHierarchyLevel().getValue()) == null) {
 //				/*
 //				 * no coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 //				 */
 //				for(Coordinator tCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 //					if(tCoordinator.getNeighborsARG().contains(pAnnounce.getNegotiatorIdentification())) {
 //						tCoordinator.storeAnnouncement(pAnnounce);
 //					}
 //				}
 //			} else {
 //				/*
 //				 * coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 //				 */
 //				for(Coordinator tCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 //					if(tCoordinator.getNeighborsARG().contains(pAnnounce.getNegotiatorIdentification())) {
 //						if(tCoordinator.superiorCoordinatorComChannel() != null) {
 //							tCoordinator.superiorCoordinatorComChannel().sendPacket(pAnnounce);
 //						}
 //					}
 //				}
 //			}
 //			
 //			
 //			if(pAnnounce.getCoveringClusterEntry() != null) {
 ////				Cluster tForwardingCluster = null;
 //				
 //				if(pAnnounce.isRejected()) {
 ////					Cluster tMultiplex = this;
 ////					tForwardingCluster = (Cluster) ((Cluster) getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster()) == null ? pCEP.getRemoteCluster() : getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster())) ;
 //					//pAnnounce.setAnnouncer( (tForwardingCluster.getCoordinatorsAddress() != null ? tForwardingCluster.getCoordinatorsAddress() : null ));
 //					Logging.log(this, "Removing " + this + " as participating CEP from " + this);
 //					getComChannels().remove(this);
 //				}
 //				registerNeighborARG(mHRMController.getClusterByID(pComChannel.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry())));
 //			}
 //		}
 //	}
 	
 	private void announceNeighborCoord(AnnounceRemoteCluster pAnnouncement, ComChannel pCEP)
 	{
 		Logging.log(this, "Handling " + pAnnouncement);
 		if(mCoordinatorNodeName != null)
 		{
 			if(mHRMController.getNodeName().equals(mCoordinatorNodeName))
 			{
 //				handleNeighborAnnouncement(pAnnouncement, pCEP);
 			} else {
 				superiorCoordinatorComChannel().sendPacket(pAnnouncement);
 			}
 		} else {
 			mReceivedAnnounces.add(pAnnouncement);
 		}
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
	 * 
	 * @return the location description
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + (getGUIClusterID() != -1 ? Long.toString(getGUIClusterID()) : "??") + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 	
 	/**
	 * Returns a string including the ClusterID, the coordinator ID, and the node priority
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
