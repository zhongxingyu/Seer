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
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnounceRemoteCluster;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
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
 public class Cluster extends ControlEntity implements ICluster
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
 	 * Stores the elector which is responsible for coordinator elections for this cluster.
 	 */
 	private Elector mElector = null;
 	
 	/**
 	 * Stores a descriptive string about the elected coordinator
 	 */
 	private String mCoordinatorDescription = null;
 	
 	/**
 	 * Stores if the neighborhood is already initialized
 	 */
 	private boolean mNeighborInitialized = false;
 	
 	private Name mCoordName;
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
 	 * @param pClusterID the unique ID of this cluster, a value of "-1" triggers the creation of a new ID
 	 * @param pHierarchyLevel the hierarchy level
 	 */
 	private Cluster(HRMController pHRMController, Long pClusterID, HierarchyLevel pHierarchyLevel)
 	{
 		super(pHRMController, pHierarchyLevel);
 		
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
 
 		// detect neighbor clusters, increase the Bully priority based on the local connectivity
 		initializeNeighborhood();
 
 		// creates new elector object, which is responsible for Bully based election processes
 		mElector = new Elector(mHRMController, this);
 		
 		Logging.log(this, "CREATED");
 	}
 	
 	/**
 	 * Factory function: create a cluster
 	 * 
 	 * @param pHrmController the local HRMController instance
 	 * @param pClusterID the unique ID of this cluster, a value of "-1" triggers the creation of a new ID
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the new Cluster object
 	 */
 	static public Cluster create(HRMController pHrmController, Long pClusterID, HierarchyLevel pHierarchyLevel)
 	{
 		return new Cluster(pHrmController, pClusterID, pHierarchyLevel);
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
 		return new Cluster(pHrmController, null, HierarchyLevel.createBaseLevel());
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
 	 * Returns the elector of this cluster
 	 * 
 	 * @return the elector
 	 */
 	public Elector getElector()
 	{
 		return mElector;
 	}
 	
 	/**
 	 * Determines the coordinator of this cluster. It is "null" if the election was lost or hasn't finished yet. 
 	 * 
 	 * @return the cluster's coordinator
 	 */
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
 	 * Set the new coordinator, which was elected by the Elector instance.
 	 * 
 	 * @param pCoordinator the new coordinator
 	 */
 	public void setCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "Updating the coordinator from " + mCoordinator + " to " + pCoordinator);
 		
 		// set the coordinator
 		mCoordinator = pCoordinator;
 		
 		// update the stored unique ID for the coordinator
 		if (pCoordinator != null){
 			setSuperiorCoordinatorID(pCoordinator.getCoordinatorID());
 
 			// update the descriptive string about the coordinator
 			mCoordinatorDescription = mCoordinator.toLocation();
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
 	 * Sends a packet as broadcast to all cluster members
 	 * 
 	 * @param pPacket the packet which has to be broadcasted
 	 */
 	public void sendClusterBroadcast(Serializable pPacket)
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
 
 			if ((HRMConfig.Hierarchy.SIGNALING_INCLUDES_LOCALHOST) || (!tIsLoopback)){
 				// send the packet to one of the possible cluster members
 				tComChannel.sendPacket(pPacket);
 			}else{
 				Logging.log(this, "              ..skipping " + (tIsLoopback ? "LOOPBACK CHANNEL" : ""));
 			}
 		}
 	}
 
 	/**
 	 * Returns how many external cluster members are known
 	 * 
 	 * @return the count
 	 */
 	public int countClusterMembers()
 	{
 		int tResult = 0;
 
 		// if the local host is also treated as cluster member, we return an additional cluster member 
 		if (HRMConfig.Hierarchy.SIGNALING_INCLUDES_LOCALHOST){
 			tResult++;
 		}
 		
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		// get the L2Addres of the local host
 		L2Address tLocalL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		
 		for(ComChannel tComChannel : tComChannels) {
 			boolean tIsLoopback = tLocalL2Address.equals(tComChannel.getPeerL2Address());
 			
 			// filter loopback channels
 			if (!tIsLoopback){
 				tResult++;
 			}
 		}
 
 		return tResult;
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
 			if (getElector().wasStarted()){
 				getElector().startElection();
 			}
 		}
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	public void handleBullyAnnounce(BullyAnnounce pBullyAnnounce, ComChannel pComChannel)
 	{
 		// update the description about the elected coordinator
 		mCoordinatorDescription = pBullyAnnounce.getCoordinatorDescription();
 				
 		setSuperiorCoordinator(pComChannel, pBullyAnnounce.getSenderName(), pBullyAnnounce.getCoordinatorID(), pComChannel.getPeerL2Address());
 		mHRMController.setClusterWithCoordinator(getHierarchyLevel(), this);
 	}
 	
 	public void setSuperiorCoordinator(ComChannel pCoordinatorComChannel, Name pCoordinatorName, int pCoordinatorID, L2Address pCoordinatorL2Address)
 	{
 		super.setSuperiorCoordinator(pCoordinatorComChannel, pCoordinatorName, pCoordinatorID, pCoordinatorL2Address);
 
 		L2Address tLocalCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 	
 		// make sure that a programming mistakes returns also the right unique ID of the superior coordinator
 		setCoordinatorID(pCoordinatorID);
 		
 		mCoordName = pCoordinatorName;
 		if(superiorCoordinatorComChannel() == null) {
 			// store the L2Address of the superior coordinator 
 			setSuperiorCoordinatorL2Address(mHRMController.getHRS().getCentralFNL2Address());
 		} else {
 			// store the L2Address of the superior coordinator 
 			setSuperiorCoordinatorL2Address(pCoordinatorL2Address);
 
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
 					handleNeighborAnnouncement(mReceivedAnnounces.removeFirst(), pCoordinatorComChannel);
 				}
 			}
 		}
 	}
 	
 	private ICluster addAnnouncedCluster(AnnounceRemoteCluster pAnnounce, ComChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				mHRMController.getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		Cluster tCluster = mHRMController.getClusterByID(new ClusterName(mHRMController, pAnnounce.getLevel(), pAnnounce.getToken(), pAnnounce.getClusterID()));
 		if(tCluster != null) {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 			registerNeighborARG(tCluster);
 		}else{
 			Logging.log(this, "     ..creating cluster proxy");
 			ClusterProxy tNeighborCluster = new ClusterProxy(mHRMController, pAnnounce.getClusterID(), getHierarchyLevel(), pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken());
 			mHRMController.setSourceIntermediateCluster(tNeighborCluster, this);
 			tNeighborCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			
 			mHRMController.getHRS().mapFoGNameToL2Address(tNeighborCluster.getCoordinatorName(),  pAnnounce.getCoordAddress());
 			
 			registerNeighborARG(tCluster);
 		}
 		
 		if(pAnnounce.getCoordinatorName() != null) {
 			mHRMController.getHRS().mapFoGNameToL2Address(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 		}
 		return tCluster;
 	}
 	
 	public void handleNeighborAnnouncement(AnnounceRemoteCluster pAnnounce, ComChannel pComChannel)
 	{
 		if(!pAnnounce.getCoordinatorName().equals(mHRMController.getNodeName())) {
 			Logging.log(this, "Received announcement of foreign cluster");
 		}
 		
 		if(getHierarchyLevel().isBaseLevel()) {
 			if(pComChannel != null) {
 				L2Address tLocalCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 				if(!tLocalCentralFNL2Address.equals(pComChannel.getPeerL2Address()) && pComChannel.getRouteToPeer() != null) {
 					RoutingServiceLinkVector tLink = new RoutingServiceLinkVector(pComChannel.getRouteToPeer().clone(),  tLocalCentralFNL2Address, pComChannel.getPeerL2Address());
 					pAnnounce.addRoutingVector(tLink);
 					Logging.log(this, "Added routing vector " + tLink);
 				}
 				pAnnounce.isForeignAnnouncement();
 			}
 		} else {
 			if(mHRMController.getClusterWithCoordinatorOnLevel(getHierarchyLevel().getValue()) == null) {
 				/*
 				 * no coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 				 */
 				for(Coordinator tCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 					if(tCoordinator.getNeighborsARG().contains(pAnnounce.getNegotiatorIdentification())) {
 						tCoordinator.storeAnnouncement(pAnnounce);
 					}
 				}
 			} else {
 				/*
 				 * coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 				 */
 				for(Coordinator tCoordinator : mHRMController.getAllCoordinators(getHierarchyLevel())) {
 					if(tCoordinator.getNeighborsARG().contains(pAnnounce.getNegotiatorIdentification())) {
 						if(tCoordinator.superiorCoordinatorComChannel() != null) {
 							tCoordinator.superiorCoordinatorComChannel().sendPacket(pAnnounce);
 						}
 					}
 				}
 			}
 			
 			
 			if(pAnnounce.getCoveringClusterEntry() != null) {
 //				Cluster tForwardingCluster = null;
 				
 				if(pAnnounce.isRejected()) {
 //					Cluster tMultiplex = this;
 //					tForwardingCluster = (Cluster) ((Cluster) getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster()) == null ? pCEP.getRemoteCluster() : getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster())) ;
 					//pAnnounce.setAnnouncer( (tForwardingCluster.getCoordinatorsAddress() != null ? tForwardingCluster.getCoordinatorsAddress() : null ));
 					Logging.log(this, "Removing " + this + " as participating CEP from " + this);
 					getComChannels().remove(this);
 				}
 				registerNeighborARG(mHRMController.getClusterByID(pComChannel.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry())));
 			}
 		}
 	}
 	
 	private void announceNeighborCoord(AnnounceRemoteCluster pAnnouncement, ComChannel pCEP)
 	{
 		Logging.log(this, "Handling " + pAnnouncement);
 		if(mCoordName != null)
 		{
 			if(mHRMController.getNodeName().equals(mCoordName))
 			{
 				handleNeighborAnnouncement(pAnnouncement, pCEP);
 			} else {
 				superiorCoordinatorComChannel().sendPacket(pAnnouncement);
 			}
 		} else {
 			mReceivedAnnounces.add(pAnnouncement);
 		}
 	}
 	
 	public void setCoordinatorName(Name pCoordName)
 	{
 		mCoordName = pCoordName;
 	}
 
 	public Name getCoordinatorName()
 	{
 		return mCoordName;
 	}
 	
 	@Override
 	public Namespace getNamespace() {
 		return new Namespace("cluster");
 	}
 
 	@Override
 	public int getSerialisedSize() {
 		return 0;
 	}
 	
 	public int hashCode()
 	{
 		return getClusterID().intValue();
 	}
 
 	
 	
 	
 	
 	
 	/**
 	 * Returns a descriptive string about the elected coordinator 
 	 * 
 	 * @return the descriptive string
 	 */
 	public String getCoordinatorDescription()
 	{
 		return mCoordinatorDescription;
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
