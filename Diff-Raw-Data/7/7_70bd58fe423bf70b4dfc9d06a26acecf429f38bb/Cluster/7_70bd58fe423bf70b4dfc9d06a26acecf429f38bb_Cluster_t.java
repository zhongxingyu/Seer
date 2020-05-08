 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.clustering;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyPriorityUpdate;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.Coordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.IElementDecorator;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a clusters on a defined hierarchy level.
  * 
  */
 public class Cluster implements ICluster, IElementDecorator, HRMEntity
 {
 	/**
 	 * For using this class within (de-)serialization.
 	 */
 	private static final long serialVersionUID = -7486131336574637721L;
 
 	/**
 	 * This is the GUI specific cluster counter, which allows for globally unique cluster IDs.
 	 * It's only used within the GUI. 	
 	 */
 	private static int sGUIClusterID = 0;
 
 	/**
 	 * Stores the Bully priority of this node for this cluster.
 	 * The value is also used inside the Elector of this cluster.
 	 */
 	private BullyPriority mBullyPriority = null;
 
 	/**
 	 * Stores the hierarchy level of this cluster.
 	 */
 	private HierarchyLevel mHierarchyLevel;
 
 	/**
 	 * Stores the elector which is responsible for coordinator elections for this cluster.
 	 */
 	private Elector mElector = null;
 	
 	/**
 	 * Stores the HRMSignature of this cluster.
 	 */
 	private HRMSignature mSignature = null;
 
 	/**
 	 * This is the GUI specific cluster ID. It is used to allow for an easier debugging.
 	 */
 	private int mGUIClusterID = sGUIClusterID++;
 
 	/**
 	 * Counter about how many times a coordinator was defined
 	 */
 	private int mCoordinatorUpdateCounter = 0;
 
 	/**
 	 * The HRM ID of this cluster.
 	 */
 	private HRMID mHRMID = null;
 
 	private CoordinatorCEPChannel mChannelToCoordinator = null;
 	private Long mClusterID;
 	private BullyPriority mHighestPriority = null;
 	private BullyPriority mCoordinatorPriority;
 	private Name mCoordName;
 	private Name mCoordAddress;
 	private HRMController mHRMController;
 	private LinkedList<CoordinatorCEPChannel> mCEPs;
 	private LinkedList<NeighborClusterAnnounce> mReceivedAnnounces = null;
 	private HRMSignature mCoordSignature;
 	private boolean mInterASCluster = false;
 	private int mToken;
 	private LinkedList<CoordinatorCEPChannel> mLaggards; // only used by the Elector
 	private TopologyData mTopologyData = null;
 	private LinkedList<CoordinatorCEPChannel> mOldParticipatingCEPs;
 
 	/**
 	 * Stores a reference to the local coordinator instance if the local router is also the coordinator for this cluster
 	 */
 	private Coordinator mCoordinator = null;
 	
 	private CoordinatorCEPMultiplexer mMux = null;
 	
 	/**
 	 * This is the constructor of an intermediate cluster. At first such a cluster is identified by its cluster
 	 * ID and the hierarchical level. Later on - once a coordinator is found, it is additionally identified
 	 * by a token the coordinator sends to all participants. In contrast to the cluster token the identity is used
 	 * to filter potential participants that may be used for the election of a coordinator.
 	 * 
 	 * @param pClusterID
 	 * @param pLevel
 	 * @param ptHRMController
 	 */
 	public Cluster(Long pClusterID, HierarchyLevel pHierarchyLevelValue, HRMController pHRMController)
 	{
 		// initialize the HRMID of the cluster to ".0.0.0"
 		mHRMID = new HRMID(0);
 		
 		mClusterID = pClusterID;
 		mHierarchyLevel = pHierarchyLevelValue;
 		mCEPs = new LinkedList<CoordinatorCEPChannel>();
 		mReceivedAnnounces = new LinkedList<NeighborClusterAnnounce>();
 		mHRMController = pHRMController;
 		mBullyPriority = BullyPriority.createForCluster(this);
 		Logging.log(this, "CREATED CLUSTER " + mClusterID + " on level " + mHierarchyLevel + " with priority " + mBullyPriority.getValue());
 		for(ICluster tCluster : getHRMController().getRoutingTargets())
 		{
 			Logging.log(this, "Found already known neighbor: " + tCluster);
 			if ((tCluster.getHierarchyLevel().equals(pHierarchyLevelValue)) && (tCluster != this))
 			{
 				if (!(tCluster instanceof Cluster)){
 					Logging.err(this, "Routing target should be a cluster, but it is " + tCluster);
 				}
 				tCluster.addNeighborCluster(this);
 
 				// increase Bully priority because of changed connectivity (topology depending)
 				mBullyPriority.increaseConnectivity();
 			}
 		}
 
 		// creates the cluster signature
 		mSignature = getHRMController().createClusterSignature(this);
 
 		// creates new elector object, which is responsible for Bully based election processes
 		mElector = new Elector(this);
 		
 		mMux = new CoordinatorCEPMultiplexer(mHRMController);
 		
 		mMux.setCluster(this);
 		
 		// register at HRMController's internal database
 		getHRMController().registerCluster(this);
 
 		Logging.log(this, "CREATED");
 	}
 	
 	/**
 	 * Returns the cluster HRMSignature
 	 * 
 	 * @return the signature
 	 */
 	public HRMSignature getSignature()
 	{
 		return mSignature;
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
 	 * Returns the reference to the node local HRMController instance 
 	 */
 	public HRMController getHRMController()
 	{
 		return mHRMController;
 	}
 
 	public void handleBullyAnnounce(BullyAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		setCoordinatorCEP(pCEP, pAnnounce.getCoordSignature(), pAnnounce.getSenderName(), pAnnounce.getToken(), pCEP.getPeerName());
 		getHRMController().setClusterWithCoordinator(getHierarchyLevel(), this);
 	}
 	
 	public void setCoordinatorCEP(CoordinatorCEPChannel pCoordinatorChannel, HRMSignature pCoordSignature, Name pCoordName, int pCoordToken, HRMName pAddress)
 	{
 		setToken(pCoordToken);
 		
 		Logging.log(this, "announcement number " + (++mCoordinatorUpdateCounter) + ": Setting Coordinator " + pCoordinatorChannel + " with signature " + pCoordSignature + " with routing address " + pAddress + " and priority ");
 		Logging.log(this, "previous channel to coordinator was " + mChannelToCoordinator + " for coordinator " + mCoordName);
 		mChannelToCoordinator = pCoordinatorChannel;
 		mCoordSignature = pCoordSignature;
 		mCoordName = pCoordName;
 		if(mChannelToCoordinator == null) {
 			synchronized(this) {
 				mCoordAddress = getHRMController().getNode().getRoutingService().getNameFor(getHRMController().getNode().getCentralFN());
 				notifyAll();
 			}
 			setCoordinatorPriority(getBullyPriority());
 		} else {
 			synchronized(this) {
 				mCoordAddress = pAddress;
 				notifyAll();
 			}
 			setCoordinatorPriority(pCoordinatorChannel.getPeerPriority());
 			try {
 				getHRMController().getHRS().registerNode(pCoordName, pAddress);
 			} catch (RemoteException tExc) {
 				Logging.err(this, "Unable to register " + pCoordName, tExc);
 			}
 			
 			if(pCoordinatorChannel.getRouteToPeer() != null && !pCoordinatorChannel.getRouteToPeer().isEmpty()) {
 				if(pAddress instanceof L2Address) {
 					getHRMController().getHRS().registerNode((L2Address) pAddress, false);
 				}
 				
 				getHRMController().getHRS().registerRoute(pCoordinatorChannel.getSourceName(), pCoordinatorChannel.getPeerName(), pCoordinatorChannel.getRouteToPeer());
 			}
 		}
 		Logging.log(this, "This cluster has the following neighbors: " + getNeighbors());
 		for(ICluster tCluster : getNeighbors()) {
 			if(tCluster instanceof Cluster) {
 				Logging.log(this, "CLUSTER-CEP - found already known neighbor cluster: " + tCluster);
 
 				Logging.log(this, "Preparing neighbor zone announcement");
 				NeighborClusterAnnounce tAnnounce = new NeighborClusterAnnounce(pCoordName, mHierarchyLevel, pCoordSignature, pAddress, getToken(), mClusterID);
 				tAnnounce.setCoordinatorsPriority(mBullyPriority); //TODO : ???
 				if(pCoordinatorChannel != null) {
 					tAnnounce.addRoutingVector(new RoutingServiceLinkVector(pCoordinatorChannel.getRouteToPeer(), pCoordinatorChannel.getSourceName(), pCoordinatorChannel.getPeerName()));
 				}
 				((Cluster)tCluster).announceNeighborCoord(tAnnounce, pCoordinatorChannel);
 			}
 		}
 		if(mReceivedAnnounces.isEmpty()) {
 			Logging.log(this, "No announces came in while no coordinator was set");
 		} else {
 			Logging.log(this, "sending old announces");
 			while(!mReceivedAnnounces.isEmpty()) {
 				if(mChannelToCoordinator != null)
 				{
 					// OK, we have to notify the other node via socket communication, so this cluster has to be at least one hop away
 					mChannelToCoordinator.sendPacket(mReceivedAnnounces.removeFirst());
 				} else {
 					/*
 					 * in this case this announcement came from a neighbor intermediate cluster
 					 */
 					handleNeighborAnnouncement(mReceivedAnnounces.removeFirst(), pCoordinatorChannel);
 				}
 			}
 		}
 		if(pCoordinatorChannel == null) {
 //			boolean tIsEdgeRouter = false;
 			LinkedList<ClusterName> tInterASClusterIdentifications = new LinkedList<ClusterName>();
 
 			for(HRMGraphNodeName tNode : getHRMController().getRoutableClusterGraph().getNeighbors(this)) {
 				if(tNode instanceof ICluster && ((ICluster) tNode).isInterASCluster()) {
 					ICluster tCluster = (ICluster)tNode;
 //					tIsEdgeRouter = true;
 					tInterASClusterIdentifications.add(new ClusterName(tCluster.getToken(), tCluster.getClusterID(), tCluster.getHierarchyLevel()));
 				}
 			}
 		}
 	}
 	
 	private ICluster addAnnouncedCluster(NeighborClusterAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		ICluster tCluster = getHRMController().getCluster(new ClusterName(pAnnounce.getToken(), pAnnounce.getClusterID(), pAnnounce.getLevel()));
 		if(tCluster == null) {
 			tCluster = new NeighborCluster(pAnnounce.getClusterID(), pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken(), mHierarchyLevel, getHRMController());
 			getHRMController().setSourceIntermediateCluster(tCluster, this);
 			((NeighborCluster)tCluster).addAnnouncedCEP(pCEP);
 			((NeighborCluster)tCluster).setSourceIntermediate(this);
 			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			tCluster.setToken(pAnnounce.getToken());
 			
 			if(pAnnounce.isInterASCluster()) {
 				tCluster.setInterASCluster();
 			}
 			
 			try {
 				getHRMController().getHRS().registerNode(tCluster.getCoordinatorName(), tCluster.getCoordinatorsAddress());
 			} catch (RemoteException tExc) {
 				Logging.err(this, "Unable to fulfill requirements", tExc);
 			}
 			
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 		}
 		/*if(tCluster instanceof AttachedCluster) {
 			((AttachedCluster)tCluster).setNegotiatingHost(pAnnounce.getAnnouncersAddress());
 		}*/
 		
 		/*
 		 * function checks whether neighbor relation was established earlier
 		 */
 		addNeighborCluster(tCluster);
 
 		if(pAnnounce.getCoordinatorName() != null) {
 //			Description tDescription = new Description();
 			try {
 				getHRMController().getHRS().registerNode(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 			} catch (RemoteException tExc) {
 				Logging.warn(this, "Unable to register " + pAnnounce.getCoordinatorName(), tExc);
 			}
 		}
 		return tCluster;
 	}
 	
 	public void handleNeighborAnnouncement(NeighborClusterAnnounce	pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		if(!pAnnounce.getCoordinatorName().equals(getHRMController().getNode().getCentralFN().getName())) {
 			Logging.log(this, "Received announcement of foreign cluster");
 		}
 		
 		if(getHierarchyLevel().isBaseLevel()) {
 			if(pCEP != null) {
 				if(!pCEP.getSourceName().equals(pCEP.getPeerName()) && pCEP.getRouteToPeer() != null) {
 					RoutingServiceLinkVector tLink = new RoutingServiceLinkVector(pCEP.getRouteToPeer().clone(),  pCEP.getSourceName(), pCEP.getPeerName());
 					pAnnounce.addRoutingVector(tLink);
 					Logging.log(this, "Added routing vector " + tLink);
 				}
 				pAnnounce.isForeignAnnouncement();
 			}
 			if(pCEP != null) {
 				pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			}
 		} else {
 			if(getHRMController().getClusterWithCoordinatorOnLevel(mHierarchyLevel.getValue()) == null) {
 				/*
 				 * no coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 				 */
 				for(Coordinator tManager : getHRMController().getCoordinator(mHierarchyLevel)) {
 					if(tManager.getNeighbors().contains(pAnnounce.getNegotiatorIdentification())) {
 						tManager.storeAnnouncement(pAnnounce);
 					}
 				}
 			} else {
 				/*
 				 * coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 				 */
 				for(Coordinator tManager : getHRMController().getCoordinator(mHierarchyLevel)) {
 					if(tManager.getNeighbors().contains(pAnnounce.getNegotiatorIdentification())) {
 						if(tManager.getCoordinatorCEP() != null) {
 							tManager.getCoordinatorCEP().sendPacket(pAnnounce);
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
 					getClusterMembers().remove(this);
 				}
 				try {
 					addNeighborCluster(getHRMController().getCluster(pCEP.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry())));
 				} catch (PropertyException tExc) {
 					Logging.log(this, "Unable to fulfill requirements");
 				}
 				Logging.log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 				pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			} else if(pCEP != null) {
 				Logging.log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 				pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			}
 		}
 	}
 	
 	public CoordinatorCEPChannel getCoordinatorCEP()
 	{
 		return mChannelToCoordinator;
 	}
 	
 	public void addNeighborCluster(ICluster pNeighbor)
 	{
 		LinkedList<ICluster> tNeighbors = getNeighbors(); 
 		if(!tNeighbors.contains(pNeighbor))
 		{
 			if(pNeighbor instanceof Cluster) {
 				RoutableClusterGraphLink tLink = new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.PHYSICAL_LINK);
 				getHRMController().getRoutableClusterGraph().storeLink(pNeighbor, this, tLink);
 			} else {
 				RoutableClusterGraphLink tLink = new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.LOGICAL_LINK);
 				getHRMController().getRoutableClusterGraph().storeLink(pNeighbor, this, tLink);
 			}
 			if(pNeighbor instanceof Cluster && !pNeighbor.isInterASCluster()) {
 				
 				Logging.log(this, "CLUSTER - adding neighbor cluster: " + pNeighbor);
 
 				// increase Bully priority because of changed connectivity (topology depending) 
 				mBullyPriority.increaseConnectivity();
 				
 				if(!mInterASCluster) {
 					Logging.log(this, "Informing " + getClusterMembers() + " about change in priority and initiating new election");
 					
 					sendClusterBroadcast(new BullyPriorityUpdate(getHRMController().getNode().getCentralFN().getName(), mBullyPriority), null);
 					
 					Logging.log(this, "Informed other clients about change of priority - it is now " + mBullyPriority.getValue());
 				}
 			}
 		}
 	}
 	
 	private void announceNeighborCoord(NeighborClusterAnnounce pAnnouncement, CoordinatorCEPChannel pCEP)
 	{
 		Logging.log(this, "Handling " + pAnnouncement);
 		if(mCoordName != null)
 		{
 			if(getHRMController().getNode().getCentralFN().getName().equals(mCoordName))
 			{
 				handleNeighborAnnouncement(pAnnouncement, pCEP);
 			} else {
 				mChannelToCoordinator.sendPacket(pAnnouncement);
 			}
 		} else {
 			mReceivedAnnounces.add(pAnnouncement);
 		}
 	}
 	
 	/**
 	 * Assign new HRMID for being addressable as cluster member.
 	 *  
 	 * @param pCaller the caller who assigns the new HRMID
 	 * @param pHRMID the new HRMID
 	 */
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		Logging.log(this, "ASSINGED HRMID=" + pHRMID + " (caller=" + pCaller + ")");
 
 		// update the HRMID
 		mHRMID = pHRMID.clone();
 		
		// inform HRM controller about the address change
		getHRMController().updateClusterAddress(this);
 	}
 	
 	public BullyPriority getHighestPriority()
 	{
 		if (mHighestPriority == null){
 			mHighestPriority = new BullyPriority(this);
 		}
 		
 		return mHighestPriority;
 	}
 	
 	public LinkedList<CoordinatorCEPChannel> getOldParticipatingCEPs()
 	{
 		return mOldParticipatingCEPs;
 	}
 	
 	public void setParticipatingCEPs(LinkedList<CoordinatorCEPChannel> pCEPs)
 	{
 		mOldParticipatingCEPs = mCEPs;
 		Logging.log(this, "Setting participating CEPs to " + pCEPs);
 		mCEPs = pCEPs;
 	}
 	
 	public void addParticipatingCEP(CoordinatorCEPChannel pParticipatingCEP)
 	{
 		if(!mCEPs.contains(pParticipatingCEP)) {
 			mCEPs.add(pParticipatingCEP);
 			Logging.info(this, "Added " + pParticipatingCEP + " to participating CEPs");
 			if(mCEPs.size() > 1) {
 				Logging.info(this, "Adding second participating CEP " + pParticipatingCEP);
 //				StackTraceElement[] tStackTrace = Thread.currentThread().getStackTrace();
 //				for (StackTraceElement tElement : tStackTrace) {
 //					getCoordinator().getLogger().log(tElement.toString());
 //				}
 			}
 		}
 	}
 
 	public LinkedList<CoordinatorCEPChannel> getClusterMembers()
 	{
 		return mCEPs;
 	}
 	
 
 	public void setPriority(BullyPriority pPriority)
 	{
 		BullyPriority tBullyPriority = mBullyPriority;
 		mBullyPriority = pPriority;
 		Logging.log(this, "######## Setting Bully priority for cluster " + toString() + " from " + tBullyPriority.getValue() + " to " + mBullyPriority.getValue());
 	}
 	
 	public BullyPriority getCoordinatorPriority()
 	{
 		return mCoordinatorPriority;
 	}
 	
 	public void setCoordinatorPriority(BullyPriority pCoordinatorPriority)
 	{
 		mCoordinatorPriority = pCoordinatorPriority;
 	}
 	
 	public Long getClusterID()
 	{
 		return mClusterID;
 	}
 	
 	public HRMSignature getCoordinatorSignature()
 	{
 		return mCoordSignature;
 	}
 	
 	public LinkedList<ICluster> getNeighbors()
 	{
 		LinkedList<ICluster> tList = new LinkedList<ICluster>();
 		for(HRMGraphNodeName tNode : getHRMController().getRoutableClusterGraph().getNeighbors(this)) {
 			if(tNode instanceof ICluster) {
 				tList.add((ICluster)tNode);
 			}
 		}
 		return tList;
 	}
 	
 	public int getToken()
 	{
 		return mToken;
 	}
 	
 	public L2Address getCoordinatorsAddress()
 	{
 		return (L2Address) mCoordAddress;
 	}
 	
 	public void setCoordinatorName(Name pCoordName)
 	{
 		mCoordName = pCoordName;
 	}
 
 	/**
 	 * Sends a packet as broadcast to call cluster members
 	 * 
 	 * @param pPacket the packet which has to be broadcasted
 	 */
 	public void sendPacketBroadcast(Serializable pPacket)
 	{
 		Logging.log(this, "Sending CLUSTER BROADCAST " + pPacket);
 		
 		for(CoordinatorCEPChannel tClusterMember : getClusterMembers()) {			
 			Logging.log(this, "       ..to " + tClusterMember);
 			
 			// send the packet to one of the possible cluster members
 			tClusterMember.sendPacket(pPacket);
 		}
 	}
 	
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPChannel> pAlreadyInformed)
 	{
 		Logging.log(this, "Sending CLUSTER BROADCAST " + pData);
 		
 		if(pData instanceof BullyPriorityUpdate)
 		{
 			Logging.log(this, "Will send priority update to" + mCEPs);
 		}
 		LinkedList<CoordinatorCEPChannel> tInformedCEPs = null;
 		if(pAlreadyInformed != null) {
 			tInformedCEPs= pAlreadyInformed;
 		} else {
 			tInformedCEPs = new LinkedList<CoordinatorCEPChannel>(); 
 		}
 		try {
 			for(CoordinatorCEPChannel tClusterMember : mCEPs)
 			{
 				if(!tInformedCEPs.contains(tClusterMember))
 				{
 					Logging.log(this, "   BROADCAST TO " + tClusterMember);
 					tClusterMember.sendPacket(pData);
 					tInformedCEPs.add(tClusterMember);
 				}
 			}
 		} catch (ConcurrentModificationException tExc) {
 			Logging.warn(this, "change in cluster CEP number occured, sending message to new peers", tExc);
 			sendClusterBroadcast(pData, tInformedCEPs);
 		}
 	}
 	
 	public String getClusterDescription()
 	{
 		return toLocation();
 		//getHRMController().getPhysicalNode() + ":" + mClusterID + "@" + mHierarchyLevel + "(" + mCoordSignature + ")";
 	}
 	
 	public BullyPriority getBullyPriority()
 	{
 		if (mBullyPriority == null){
 			mBullyPriority = new BullyPriority(this);
 		}
 			
 		return mBullyPriority;
 	}
 	
 	public Name getCoordinatorName()
 	{
 		return mCoordName;
 	}
 	
 	public HierarchyLevel getHierarchyLevel()
 	{
 		return mHierarchyLevel;
 	}
 	
 	public int getGUIClusterID()
 	{
 		return mGUIClusterID;
 	}
 	
 	@Override
 	public void setToken(int pToken) {
 		Logging.log(this, "Updating token from " + mToken + " to " + pToken);
 		mToken = pToken;
 	}
 	
 	@Override
 	public HRMID getHRMID() {
 		return mHRMID;
 	}
 	
 	@Override
 	public void setHighestPriority(BullyPriority pHighestPriority) {
 		mHighestPriority = pHighestPriority;
 	}
 	
 	@Override
 	public Namespace getNamespace() {
 		return new Namespace("cluster");
 	}
 
 	@Override
 	public int getSerialisedSize() {
 		return 0;
 	}
 	
 
 	@Override
 	public boolean equals(Object pObj)
 	{
 		if(pObj instanceof Coordinator) {
 			return false;
 		}
 		if(pObj instanceof Cluster) {
 			ICluster tCluster = (ICluster) pObj;
 			if (tCluster.getClusterID().equals(getClusterID()) && (tCluster.getToken() == getToken()) && (tCluster.getHierarchyLevel() == getHierarchyLevel())) {
 				return true;
 			} else if(tCluster.getClusterID().equals(getClusterID()) && tCluster.getHierarchyLevel() == getHierarchyLevel()) {
 				return false;
 			} else if (tCluster.getClusterID().equals(getClusterID())) {
 				return false;
 			}
 		}
 		if(pObj instanceof ClusterName) {
 			ClusterName tClusterName = (ClusterName) pObj;
 			if (tClusterName.getClusterID().equals(getClusterID()) && (tClusterName.getToken() == getToken()) && (tClusterName.getHierarchyLevel() == getHierarchyLevel())) {
 				return true;
 			} else if(tClusterName.getClusterID().equals(getClusterID()) && tClusterName.getHierarchyLevel() == getHierarchyLevel()) {
 				return false;
 			} else if (tClusterName.getClusterID().equals(getClusterID())) {
 				return false;
 			}
 		}
 		return false;
 	}	
 
 	@Override
 	public boolean isInterASCluster()
 	{
 		return mInterASCluster;
 	}
 
 	@Override
 	public void setInterASCluster()
 	{
 		mInterASCluster = true;
 	}
 
 	/**
 	 * As the implemented version of HRM uses a fully distributed algorithm for signaling it is possible that some nodes are not
 	 * associated to a coordinator because they were not covered. In that case such a node sends RequestCoordinator messages to 
 	 * the neighbors. If a neighbor is not covered by a coordinator either, it is added as laggard.
 	 * 
 	 * @return Return the list of laggards that were not covered by a coordinator either. 
 	 */
 	public LinkedList<CoordinatorCEPChannel> getLaggards()
 	{
 		return mLaggards;
 	}
 
 	/**
 	 * As the implemented version of HRM uses a fully distributed algorithm for signaling it is possible that some nodes are not
 	 * associated to a coordinator because they were not covered. In that case such a node sends RequestCoordinator messages to
 	 * the neighbors. If a neighbor is not covered by a coordinator either, it is added as laggard.
 	 * 
 	 * @param pCEP Add one connection end point as laggard here.
 	 */
 	public void addLaggard(CoordinatorCEPChannel pCEP)
 	{
 		if(mLaggards == null) {
 			mLaggards = new LinkedList<CoordinatorCEPChannel>();
 			mLaggards.add(pCEP);
 		} else {
 			mLaggards.add(pCEP);
 		}
 	}
 
 //	public void handleTopologyData(TopologyData pTopologyData)
 //	{
 //		Logging.err(this, "Ignoring topology data for HRMID " + pTopologyData.getHRMID().toString() + " and topology data " + pTopologyData);
 //		Logging.err(this, "Continuing");
 		
 //		mTopologyData = pTopologyData;
 //
 //		setHRMID(this, pTopologyData.getHRMID());
 //		
 //		if(mTopologyData.getEntries() != null) {
 //			for(FIBEntry tEntry : mTopologyData.getEntries()) {
 //				if((tEntry.getDestination() != null && !tEntry.getDestination().equals(new HRMID(0)) ) && tEntry.getNextHop() != null) {
 //					/*if(!getCoordinator().getHRS().getRoutingTable().containsKey(tEntry.getDestination())) {
 //						getCoordinator().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 //					} else {
 //						if(getCoordinator().getHRS().getFIBEntry(tEntry.getDestination()).isWriteProtected()) {
 //							getCoordinator().getLogger().log(this, "Not replacing " + getCoordinator().getHRS().getFIBEntry(tEntry.getDestination()) + " with " + tEntry);
 //						} else {
 //							getCoordinator().getHRS().getRoutingTable().remove(tEntry.getDestination());
 //							getCoordinator().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 //						}
 //					}*/
 //					getHRMController().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 //				}
 //			}
 //		}
 		// TODO: der koordinator soltle die routing-updates vornehmen, was wenn baselevel 0 ist? dann doch cluster?
 //	}
 	
 	@Override
 	public TopologyData getTopologyData()
 	{
 		return mTopologyData;
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
 		mCoordinator = pCoordinator;
 	}
 	
 	@Override
 	public synchronized CoordinatorCEPMultiplexer getMultiplexer()
 	{
 		return mMux;
 	}
 
 	/**
 	 * This method is specific for the handling of RouteRequests.
 	 * 
 	 * @param pCluster
 	 * @return
 	 */
 //	public CoordinatorCEPChannel getCEPOfCluster(ICluster pCluster)
 //	{
 //		for(CoordinatorCEPChannel tCEP : getParticipatingCEPs()) {
 //			if(tCEP.getRemoteClusterName().equals(pCluster)) {
 //				return tCEP;
 //			}
 //		}
 //		return null;
 //	}
 	
 	@Override
 	public Object getDecorationParameter()
 	{
 		return IElementDecorator.Color.GREEN;
 
 	}
 
 	@Override
 	public void setDecorationParameter(Object pDecoration)
 	{
 		// not used, but have to be implemented for implementing interface IElementDecorator
 	}
 
 	@Override
 	public Object getDecorationValue()
 	{
 		return Float.valueOf(0.8f);
 	}
 
 	@Override
 	public void setDecorationValue(Object tLabal)
 	{
 		// not used, but have to be implemented for implementing interface IElementDecorator
 	}
 
 	public int hashCode()
 	{
 		return mClusterID.intValue();
 	}
 	
 	@SuppressWarnings("unused")
 	public String toString()
 	{
 		if(mHRMID != null && HRMConfig.Debugging.PRINT_HRMIDS_AS_CLUSTER_IDS) {
 			return mHRMID.toString();
 		} else {
 			return toLocation() + " (" + idToString() + ")";
 
 		}
 	}
 
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + mGUIClusterID + "@" + getHRMController().getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 	
 	private String idToString()
 	{
 		if (getHRMID() == null){
 			return "ID=" + getClusterID() + ", Tok=" + mToken +  ", NodePrio=" + getBullyPriority().getValue() +  (getCoordinatorSignature() != null ? ", Coord.=" + getCoordinatorSignature() : "") + (mInterASCluster ? ", TRANSIT" : "");
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 
 	/**
 	 * Handles packet type "AssignHRMID".
      * The function is called when an address update for the physical node (hierarchy level 0) was received.
 	 * 
 	 * @param pAssignHRMIDPacket the received packet with the new hierarchy level 0 address
 	 */
 	public void handleAssignHRMIDForPhysicalNode(AssignHRMID pAssignHRMIDPacket)
 	{
 		// extract the HRMID from the packet 
 		HRMID tHRMID = pAssignHRMIDPacket.getHRMID();
 		
 		Logging.log(this, "Handling AssignHRMID with assigned HRMID " + tHRMID.toString());
 
 		// we process such packets only on base hierarchy level, on higher hierarchy levels coordinators should be the only target for such packets
 		if (getHierarchyLevel().isBaseLevel()){
 			Logging.log(this, "     ..setting assigned HRMID " + tHRMID.toString());
 			
 			// update the local HRMID
 			setHRMID(this, tHRMID);
 		}else{
 			Logging.warn(this, "     ..ignoring AssignHRMID packet because we are at the higher hierachy level " + getHierarchyLevel().getValue());
 		}
 
 		// the local router has also the coordinator instance for this cluster?
 		if (hasLocalCoordinator()){
 			// we should automatically continue the address distribution?
 			if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 				Logging.log(this, "     ..continuing the address distribution process via the coordinator " + getCoordinator());
 				getCoordinator().signalAddressDistribution();				
 			}			
 		}else{
 			Logging.log(this, "     ..stopping address propagation here because node " + getHRMController().getNodeGUIName() + " is only a cluster member");
 		}
 	}
 }
