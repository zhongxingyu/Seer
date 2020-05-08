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
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.ClusterDiscovery.NestedDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.RequestCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.*;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RoutingInformation;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMRoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 
 /**
  * A communication channel can exist between a cluster and the cluster members, or between a coordinator and other coordinators.
  */
 public class ComChannel
 {
 	private static final long serialVersionUID = -8290946480171751216L;
 	private ClusterName mRemoteCluster;
 
 	/**
 	 * Stores the parent control entity (cluster or coordinator) to which this communication channel belongs to
 	 */
 	private ControlEntity mParent;
 	
 	/**
 	 * Stores the Bully priority of the peer
 	 */
 	private BullyPriority mPeerPriority = null;
 	
 	/**
 	 * Stores the freshness of the Bully priority of the peer
 	 */
 	private double mPeerPriorityTimestampLastUpdate = 0; 
 	
 	private boolean mKnowsCoordinator = false;
 	private boolean mPartOfCluster = false;
 	private HRMController mHRMController = null;
 	
 	/**
 	 * For COORDINATORS: Stores the HRMID under which the corresponding peer cluster member is addressable.
 	 */
 	private HRMID mPeerHRMID = null;
 	
 	/**
 	 * 
 	 * @param pHRMController is the coordinator of a node
 	 * @param pParent is the parent cluster/coordinator
 	 */
 	public ComChannel(HRMController pHRMController, ControlEntity pParent)
 	{
 		// store the HRMController application reference
 		mHRMController = pHRMController;
 		
 		// store the parent (owner) of this communication channel
 		mParent = pParent;
 		
 		// the peer priority gets initialized by a default value ("undefined")
 		mPeerPriority = new BullyPriority(this);
 		
 		// register at the parent (owner)
 		mParent.registerComChannel(this);
 		
 		Logging.log(this, "CREATED");
 	}
 	
 	/**
 	 * Defines the HRMID of the peer which is a cluster member.
 	 * 
 	 * @param pHRMID the new HRMID under which the peer is addressable
 	 */
 	public void setPeerHRMID(HRMID pHRMID)
 	{
 		mPeerHRMID = pHRMID.clone();		
 	}
 	
 	/**
 	 * Determines the address of the peer (e.g., a cluster member).
 	 * 
 	 * @return the HRMID of the peer or "null"
 	 */
 	public HRMID getPeerHRMID()
 	{
 		return mPeerHRMID;
 	}
 	
 	/**
 	 * Handles a SignalingMessageHrm packet.
 	 * 
 	 * @param pSignalingMessageHrmPacket the packet
 	 */
 	private void handleSignalingMessageHRM(SignalingMessageHrm pSignalingMessageHrmPacket)
 	{
 		// can we learn the peer's HRMID from the packet?
 		if (pSignalingMessageHrmPacket.getSenderName() instanceof HRMID){
 			// get the HRMID of the peer
 			HRMID tPeerHRMID = (HRMID)pSignalingMessageHrmPacket.getSenderName();
 			
 			// update peer's HRMID
 			setPeerHRMID(tPeerHRMID);
 		}		
 	}
 
 	/**
 	 * Handles a RoutingInformation packet.
 	 * 
 	 * @param pRoutingInformationPacket the packet
 	 */
 	private void handleSignalingMessageSharePhase(RoutingInformation pRoutingInformationPacket)
 	{
 		if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 			Logging.log(this, "SHARE PHASE DATA received from \"" + getPeerHRMID() + "\", DATA: " + pRoutingInformationPacket);
 		}
 		
 		for (RoutingEntry tEntry : pRoutingInformationPacket.getRoutes()){
 			if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE)
 				Logging.log(this, "      ..found route: " + tEntry);
 			
 			getHRMController().addHRMRoute(tEntry);
 		}
 	}
 
 	/**
 	 * Updates the Bully prirority of the peer.
 	 * 
 	 * @param pPeerPriority the Bully priority
 	 */
 	public void setPeerPriority(BullyPriority pPeerPriority)
 	{
 		if (pPeerPriority == null){
 			Logging.warn(this, "Trying to set NULL POINTER as Bully priority, ignoring this request");
 			return;
 		}
 
 		// get the current simulation time
 		double tNow = getHRMController().getSimulationTime();
 		
 		Logging.log(this, "Updating peer priority from " + mPeerPriority.getValue() + " to " + pPeerPriority.getValue() + ", last update was " + (tNow - mPeerPriorityTimestampLastUpdate) + " seconds before");
 		
 		// update the freshness of the peer priority
 		mPeerPriorityTimestampLastUpdate = tNow;
 
 		// update the peer Bully priority itself
 		mPeerPriority = pPeerPriority;
 	}
 
 	
 	//TODO: implement this, integrate check in getPeerPriority()
 	public double getPeerPriorityFreshness()
 	{
 		return 0;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Handles a Bully signaling packet.
 	 * 
 	 * @param pPacketBully the packet
 	 */
 //	private void handleSignalingMessageBully(SignalingMessageBully pPacketBully) throws NetworkException
 //	{
 //		Node tNode = getHRMController().getNode();
 //		Name tLocalNodeName = getHRMController().getNodeName(); 
 //				
 //		boolean BULLY_SIGNALING_DEBUGGING = true;
 //
 //		if (getPeer() == null){
 //			Logging.warn(this, "Peer is still invalid!");
 //		}
 //		
 //		/**
 //		 * ELECT
 //		 */
 //		if(pPacketBully instanceof BullyElect)	{
 //			
 //			// cast to Bully elect packet
 //			BullyElect tPacketBullyElect = (BullyElect)pPacketBully;
 //			
 //			if (BULLY_SIGNALING_DEBUGGING)
 //				Logging.log(this, "BULLY-received from \"" + mParent + "\" an ELECT: " + tPacketBullyElect);
 //
 //			if ((getPeer().getSuperiorCoordinatorCEP() != null) && (getPeer().getHighestPriority().isHigher(this, tPacketBullyElect.getSenderPriority()))) {
 //				
 //				mPeerPriority = tPacketBullyElect.getSenderPriority();
 //				
 //				if (getPeer().getHRMController().equals(tLocalNodeName)) {
 //					BullyAnnounce tAnnouncePacket = new BullyAnnounce(tLocalNodeName, getPeer().getBullyPriority(), "CEP-to?", getPeer().getToken());
 //					
 //					for(CoordinatorCEPChannel tCEP : getPeer().getClusterMembers()) {
 //						tAnnouncePacket.addCoveredNode(tCEP.getPeerL2Address());
 //					}
 //					if(tAnnouncePacket.getCoveredNodes() == null || (tAnnouncePacket.getCoveredNodes() != null && tAnnouncePacket.getCoveredNodes().isEmpty())) {
 //						Logging.log(this, "Sending announce that does not cover anyhting");
 //					}
 //
 //					// send packet
 //					if (BULLY_SIGNALING_DEBUGGING)
 //						Logging.log(this, "BULLY-sending to \"" + mParent + "\" an ANNOUNCE: " + tAnnouncePacket);
 //					sendPacket(tAnnouncePacket);
 //					
 //				} else {
 //					// create ALIVE packet
 //					BullyAlive tAlivePacket = new BullyAlive(tLocalNodeName);
 //					
 //					// send packet
 //					if (BULLY_SIGNALING_DEBUGGING)
 //						Logging.log(this, "BULLY-sending to \"" + mParent + "\" an ALIVE: " + tAlivePacket);
 //					sendPacket(tAlivePacket);
 //					//TODO: packet is sent but never parsed or a timeout timer reset!!
 //				}
 //			} else {
 //				if (getPeer() instanceof Cluster){
 //					// store peer's Bully priority
 //					//TODO: peer prio direkt mal abspeichern und auf grte checken!
 //					mPeerPriority = tPacketBullyElect.getSenderPriority();
 //					
 //					// create REPLY packet
 //					BullyReply tReplyPacket = new BullyReply(tLocalNodeName, getPeerHRMID(), getPeer().getBullyPriority());
 //					
 //					// send the answer packet
 //					if (BULLY_SIGNALING_DEBUGGING)
 //						Logging.log(this, "BULLY-sending to \"" + mParent + "\" a REPLY: " + tReplyPacket);
 //					sendPacket(tReplyPacket);
 //				}else{
 //					Logging.err(this, "Peer is not a cluster, skipping BullyReply message, peer is " + getPeer());
 //				}
 //			}
 //		}
 //		
 //		/**
 //		 * REPLY
 //		 */
 //		if(pPacketBully instanceof BullyReply) {
 //			
 //			// cast to Bully replay packet
 //			BullyReply tReplyPacket = (BullyReply)pPacketBully;
 //
 //			if (BULLY_SIGNALING_DEBUGGING)
 //				Logging.log(this, "BULLY-received from \"" + mParent + "\" a REPLY: " + tReplyPacket);
 //
 //			// store peer's Bully priority
 //			//TODO: peer prio direkt mal abspeichern und auf grte checken!
 //			mPeerPriority = tReplyPacket.getSenderPriority();
 //		}
 //		
 //		/**
 //		 * ANNOUNCE
 //		 */
 //		if(pPacketBully instanceof BullyAnnounce)  {
 //			// cast to Bully replay packet
 //			BullyAnnounce tAnnouncePacket = (BullyAnnounce)pPacketBully;
 //
 //			if (BULLY_SIGNALING_DEBUGGING)
 //				Logging.log(this, "BULLY-received from \"" + mParent + "\" an ANNOUNCE: " + tAnnouncePacket);
 //
 //			//TODO: only an intermediate cluster on level 0 is able to store an announcement and forward it once a coordinator is set
 //			getPeer().handleBullyAnnounce(tAnnouncePacket, this);
 //		}
 //
 //		/**
 //		 * PRIORITY UPDATE
 //		 */
 //		if(pPacketBully instanceof BullyPriorityUpdate) {
 //			// cast to Bully replay packet
 //			BullyPriorityUpdate tPacketBullyPriorityUpdate = (BullyPriorityUpdate)pPacketBully;
 //
 //			if (BULLY_SIGNALING_DEBUGGING)
 //				Logging.log(this, "BULLY-received from \"" + mParent + "\" a PRIORITY UPDATE: " + tPacketBullyPriorityUpdate);
 //
 //			// store peer's Bully priority
 //			mPeerPriority = tPacketBullyPriorityUpdate.getSenderPriority();
 //		}
 //	}
 	
 	/**
 	 * 
 	 * @param pData is the data that should be sent to the receiver side of this connection end point
 	 * @return true if the packet left the central multiplexer and the forwarding node that is attached to a direct down gate
 	 * @throws NetworkException
 	 */
 	public boolean handlePacket(Serializable pData) throws NetworkException
 	{
 		if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS){
 			Logging.log(this, "RECEIVED DATA from \"" + getPeerL2Address() + "/" + getPeerHRMID() + "\": " + pData);
 		}
 			
 
 		Node tNode = getHRMController().getNode();
 		HRMRoutingService tHRS = getHRMController().getHRS();
 		
 		/*
 		 * Invalid data
 		 */
 		if(pData == null) {
 			throw new NetworkException("Received invalid null pointer as data");
 		}
 
 		/*
 		 * main packet processing
 		 */
 		try {
 			
 			/**
 			 * HRM signaling message
 			 */
 			if (pData instanceof SignalingMessageHrm){
 				// cast to a SignalingMessageHrm signaling message
 				SignalingMessageHrm tSignalingMessageHrmPacket = (SignalingMessageHrm)pData;
 			
 				// process SignalingMessageHrm message
 				handleSignalingMessageHRM(tSignalingMessageHrmPacket);
 				
 				//HINT: don't return here because we are still interested in the more detailed packet data from derived packet types!
 			}
 			
 			/**
 			 * Bully signaling message
 			 */
 			if (pData instanceof SignalingMessageBully) {
 				// the packet is received by a cluster
 				//HINT: this is only possible at base hierarchy level
 				if (mParent instanceof Cluster){
 					if (!mParent.getHierarchyLevel().isBaseLevel()){
 						Logging.warn(this, "EXPECTED BASE HIERARCHY LEVEL");
 					}
 					Cluster tParentCluster = (Cluster)mParent;
 					
 					Logging.log(this, "BULLY MESSAGE FROM " + getPeerL2Address());
 					
 					// cast to a Bully signaling message
 					SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 				
 					// process Bully message
 					tParentCluster.handlePacket(tBullyMessage, this);
 				}
 				
 				// the packet is received by a coordinator
 				if (mParent instanceof Coordinator){
 					Coordinator tCoordinator = (Coordinator)mParent;
 					
 					Logging.log(this, "BULLY MESSAGE FROM " + getPeerL2Address());
 					Logging.log(this, "POSSIBLE LOOP DETECTED HERE");
 					
 					// cast to a Bully signaling message
 					SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 				
 					// process Bully message
 					tCoordinator.getCluster().handlePacket(tBullyMessage, this);
 				}
 
 				else{
 					Logging.err(this, "Parent " + mParent + " has the wrong class type, expected Cluster");
 				}
 				
 				return true;
 			}
 
 			/**
 			 * RoutingInformation
 			 */
 			if (pData instanceof RoutingInformation){
 				// cast to a RoutingInformation signaling message
 				RoutingInformation tRoutingInformationPacket = (RoutingInformation)pData;
 
 				// process Bully message
 				handleSignalingMessageSharePhase(tRoutingInformationPacket);
 				
 				return true;
 			}
 			
 			
 			
 			
 			
 			/**
 			 * NeighborClusterAnnounce
 			 */
 			if(pData instanceof NeighborClusterAnnounce) {
 				NeighborClusterAnnounce tAnnouncePacket = (NeighborClusterAnnounce)pData;
 
 				if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 					Logging.log(this, "NEIGHBOR received from \"" + mParent + "\" a NEIGHBOR CLUSTER ANNOUNCE: " + tAnnouncePacket);
 
 				if(tAnnouncePacket.isInterASAnnouncement()) {
 					Logging.log(this, tNode.getAS().getName() + " received an announcement from " + tAnnouncePacket.getASIdentification());
 					if(tNode.getAS().getName().equals(tAnnouncePacket.getASIdentification())) {
 						if(!getSourceName().equals(getPeerL2Address())) {
 							for(Route tPath : tHRS.getCoordinatorRoutingMap().getRoute(getSourceName(), getPeerL2Address())) {
 								tAnnouncePacket.addRoutingVector(new RoutingServiceLinkVector(tPath, tHRS.getCoordinatorRoutingMap().getSource(tPath), tHRS.getCoordinatorRoutingMap().getDest(tPath)));
 							}
 						}
 					} else {
 						if(getParent() instanceof Cluster) {
 							if(!getSourceName().equals(getPeerL2Address())) {
 								RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(getRouteToPeer(), getSourceName(), getPeerL2Address());
 								tAnnouncePacket.addRoutingVector(tVector);
 							}
 							for(ComChannel tCEP : getParent().getComChannels()) {
 								boolean tWroteAnnouncement = false;
 								if(tCEP.getRemoteClusterName().getHierarchyLevel().getValue() - 1 == tAnnouncePacket.getLevel().getValue()) {
 									
 									// send packet
 									tCEP.sendPacket(tAnnouncePacket);
 									
 									tWroteAnnouncement = true;
 								}
 								Logging.log(this, "Testing " + tCEP + " whether it leads to the clusters coordinator: " + tWroteAnnouncement);
 							}
 						} else if(getParent() instanceof Coordinator) {
 							Logging.log(this, "Inter AS announcement " + tAnnouncePacket + " is handled by " + getParent() + " whether it leads to the clusters coordinator");
 							((Coordinator)getParent()).getCluster().handleNeighborAnnouncement(tAnnouncePacket, this);
 						}
 					}
 				} else {
 					if (!(getParent() instanceof Cluster)){
 						Logging.err(this, "Peer should be a cluster here");
 					}
 					getParent().handleNeighborAnnouncement(tAnnouncePacket, this);
 				}
 				Logging.log(this, "Received " + tAnnouncePacket + " from remote cluster " + mRemoteCluster);
 			}
 			
 			/**
 			 * AssignHRMID
 			 */
 			if(pData instanceof AssignHRMID) {
 				AssignHRMID tAssignHRMIDPacket = (AssignHRMID)pData;
 
 				if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 					Logging.log(this, "ASSIGN_HRMID-received from \"" + getPeerHRMID() + "\" assigned HRMID: " + tAssignHRMIDPacket.getHRMID().toString());
 
 				if (getParent() instanceof Coordinator){
 					Coordinator tCoordinator = (Coordinator)getParent();
 					tCoordinator.handleAssignHRMID(tAssignHRMIDPacket);
 				} else if (getParent() instanceof Cluster){
 					Cluster tCluster = (Cluster)getParent();
 					tCluster.handleAssignHRMID(tAssignHRMIDPacket);
 				} 
 			}
 			
 			/**
 			 * RequestCoordinator
 			 */
 			if (pData instanceof RequestCoordinator) {
 				RequestCoordinator tRequestCoordinatorPacket = (RequestCoordinator) pData;
 				
 				if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 					Logging.log(this, "CHANNEL-received from \"" + mParent + "\" COORDINATOR REQUEST: " + tRequestCoordinatorPacket);
 
 				if(!tRequestCoordinatorPacket.isAnswer()) {
 					if(getParent().superiorCoordinatorComChannel() != null) {
 						ICluster tCluster = getParent().getHRMController().getClusterWithCoordinatorOnLevel(getParent().getHierarchyLevel().getValue());
 						Logging.log(this, "Name of coordinator is " + tCluster.getCoordinatorName());
 						
 						int tToken = tCluster.getToken();
 						Name tCoordinatorName = tCluster.getCoordinatorName();
 						L2Address tCoordL2Addr = ((ControlEntity)tCluster).superiorCoordinatorL2Address();
 						
 						long tCoordinatorAddress = tCoordL2Addr.getComplexAddress().longValue();
 						DiscoveryEntry tEntry = new DiscoveryEntry(tToken, tCoordinatorName, tCoordinatorAddress, tCoordL2Addr, tCluster.getHierarchyLevel());
 						tEntry.setPriority(getParent().getPriority());
 						tEntry.setRoutingVectors(getPath(tCoordL2Addr));
 						tRequestCoordinatorPacket.addDiscoveryEntry(tEntry);
 						tRequestCoordinatorPacket.setCoordinatorKnown(true);
 						tRequestCoordinatorPacket.setAnswer();
 					} else {
 						tRequestCoordinatorPacket.setCoordinatorKnown(false);
 						tRequestCoordinatorPacket.setAnswer();
 					}
 					
 					// send packet
 					sendPacket(tRequestCoordinatorPacket);
 					
 				} else {
 					if(tRequestCoordinatorPacket.isCoordinatorKnown()) {
 						mKnowsCoordinator = true;
 					} else {
 						mKnowsCoordinator = false;
 					}
 					if(tRequestCoordinatorPacket.getDiscoveryEntries() != null) {
 						for(DiscoveryEntry tEntry : tRequestCoordinatorPacket.getDiscoveryEntries()) {
 							ClusterName tDummy = handleDiscoveryEntry(tEntry);
 							getParent().getHRMController().getCluster(new ClusterName(((ICluster)getParent()).getToken(), ((getSourceName()).getComplexAddress().longValue()), getParent().getHierarchyLevel())).registerNeighbor(getParent().getHRMController().getCluster(tDummy));
 						}
 					}
 					synchronized(tRequestCoordinatorPacket) {
 						Logging.log(this, "Received answer to " + tRequestCoordinatorPacket + ", notifying");
 						tRequestCoordinatorPacket.notifyAll();
 					}
 				}
 			}
 		} catch (PropertyException tExc) {
 			Logging.err(this, "Unable to fulfill requirements", tExc);
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param pTarget is the target to which routing service link vectors should be generated
 	 * @return vectors that provide a patch between forwarding nodes along with the gate numbers
 	 */
 	public LinkedList<RoutingServiceLinkVector> getPath(HRMName pTarget)
 	{
 		LinkedList<RoutingServiceLinkVector> tVectors = new LinkedList<RoutingServiceLinkVector>();
 		RoutableGraph<HRMName, Route> tRoutingDatabase = getHRMController().getHRS().getCoordinatorRoutingMap();
 		List<Route> tRoute = tRoutingDatabase.getRoute(getMultiplexer().getSourceRoutingServiceAddress(this), pTarget);
 		HRMName tSource = getMultiplexer().getSourceRoutingServiceAddress(this);
 		HRMName tDestination;
 		if(tRoute == null) {
 			return null;
 		} else {
 			for(int i = 0 ; i < tRoute.size() ; i++) {
 				if(tRoute.get(i) instanceof Route) {
 					tDestination = tRoutingDatabase.getDest(tRoute.get(i));
 					RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(tRoute.get(i), tSource, tDestination);
 					tVectors.add(tVector);
 					tSource = tDestination;
 				}
 			}
 		}
 		return tVectors;
 	}
 
 	/**
 	 * The nested discovery entry you provide in the first argument is the message that has to be filled with
 	 * information on how to get to the cluster(s coordinator) provided as second argument
 	 * 
 	 * @param pDiscovery entry of the entity that has to be informed about the cluster provided as second argument
 	 * @param pCluster as cluster (along with the coordinator) to which a path has to be filled into the discovery entry
 	 * @throws NetworkException 
 	 * @throws PropertyException in case the requirements to the target coordinator can not be fulfilled
 	 */
 	private void getPathTo(NestedDiscovery pDiscovery, ICluster pCluster) throws NetworkException, PropertyException
 	{
 		if(pCluster.getCoordinatorName() != null) {
 			L2Address tCoordL2Addr = ((ControlEntity)pCluster).superiorCoordinatorL2Address();
 
 			DiscoveryEntry tEntry = new DiscoveryEntry(pCluster.getToken(), pCluster.getCoordinatorName(), pCluster.getClusterID(), tCoordL2Addr, pCluster.getHierarchyLevel());
 			tEntry.setClusterHops(getParent().getHRMController().getClusterDistance(pCluster));
 			tEntry.setPriority(pCluster.getPriority());
 			tEntry.setRoutingVectors(getPath(tCoordL2Addr));
 			
 			List<AbstractRoutingGraphLink> tClusterList = getHRMController().getRouteARG((ICluster)getParent(), pCluster);
 			if(!tClusterList.isEmpty()) {
 				ICluster tPredecessorCluster = (ICluster) getHRMController().getOtherEndOfLinkARG(pCluster, tClusterList.get(tClusterList.size() - 1));
 				ClusterName tPredecessorClusterName = new ClusterName(tPredecessorCluster.getToken(), tPredecessorCluster.getClusterID(), tPredecessorCluster.getHierarchyLevel());
 				tEntry.setPredecessor(tPredecessorClusterName);
 			}
 			
 			pDiscovery.addDiscoveryEntry(tEntry);
 		}
 	}
 	
 	/**
 	 * Connection end points control the clusters they are associated to.
 	 * 
 	 * @return As one node may be associated to more than one cluster you can use this method to find out
 	 * which cluster is controlled by this connection end point.
 	 */
 	public ControlEntity getParent()
 	{
 		return mParent;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ClusterName getRemoteClusterName()
 	{
 //		ICluster tCluster = null;
 //		if(mRemoteCluster instanceof ClusterName) {
 //			tCluster = getHRMController().getCluster(mRemoteCluster);
 //		}
 //		if(getCluster().getHierarchyLevel() == HRMConfig.Hierarchy.BASE_LEVEL) {
 //			return getCluster();
 //		}
 //		return (tCluster == null ? mRemoteCluster : tCluster);
 		return mRemoteCluster;
 	}
 
 	public HRMController getHRMController()
 	{
 		return mHRMController;
 	}
 	
 	public void setAsParticipantOfMyCluster(boolean pPartOfMyCluster)
 	{
 		mPartOfCluster = pPartOfMyCluster;
 	}
 	
 	public boolean knowsCoordinator()
 	{
 		return mKnowsCoordinator;
 	}
 	
 	public BullyPriority getPeerPriority()
 	{
 		if (mPeerPriority == null){
 			mPeerPriority = new BullyPriority(this);
 		}
 			
 		return mPeerPriority;
 	}
 
 	public void setRemoteClusterName(ClusterName pClusterName)
 	{
 		Logging.log(this, "Setting remote/peer cluster " + pClusterName);
 		mRemoteCluster = pClusterName;
 	}
 
 	public boolean sendPacket(Serializable pData)
 	{
 		Logging.log(this, "Sending to " + getRemoteClusterName() + " the packet " + pData);
 		
 		if(pData instanceof RequestCoordinator) {
 //			mRequestedCoordinator = true;
 			Logging.log(this, "Sending " + pData);
 		}
 		if(getParent() instanceof Coordinator) {
 			getMultiplexer().write(pData, this, new ClusterName(((ICluster)getParent()).getToken(), ((L2Address)getPeerL2Address()).getComplexAddress().longValue(), getParent().getHierarchyLevel()));
 		} else {
 			getMultiplexer().write(pData, this, getRemoteClusterName());
 		}
 		return true;
 	}
 	
 	public L2Address getPeerL2Address()
 	{
 		return getMultiplexer().getPeerL2Address(this);
 	}
 	
 	public HRMName getSourceName()
 	{
 		return getMultiplexer().getSourceRoutingServiceAddress(this);
 	}
 	
 	public boolean isPartOfMyCluster()
 	{
 		return mPartOfCluster;
 	}
 	
 	public void handleClusterDiscovery(NestedDiscovery pDiscovery, boolean pRequest) throws PropertyException, NetworkException
 	{
 		if(pRequest){
 			Cluster tSourceCluster = getHRMController().getCluster(new ClusterName(pDiscovery.getToken(), pDiscovery.getSourceClusterID(), pDiscovery.getLevel()));
 			if(tSourceCluster == null) {
 				Logging.err(this, "Unable to find appropriate cluster for" + pDiscovery.getSourceClusterID() + " and token" + pDiscovery.getToken() + " on level " + pDiscovery.getLevel() + " remote cluster is " + getRemoteClusterName());
 			}
 
			List<AbstractRoutingGraphNode> tARGNodesOrderDistance = getHRMController().getVerticesInOrderRadiusARG(tSourceCluster);
 			
 			if(tSourceCluster != null) {
				for(AbstractRoutingGraphNode tARGNode : tARGNodesOrderDistance) {
					if(tARGNode instanceof ICluster) {
						ICluster tCluster = (ICluster) tARGNode;
 						
 						int tRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 						Logging.log(this, "Radius is " + tRadius);
 						
 						if(tCluster instanceof NeighborCluster && ((NeighborCluster)tCluster).getClusterDistanceToTarget() + pDiscovery.getDistance() > tRadius){
 							continue;
 						}
 						int tToken = tCluster.getToken();
 						if(!pDiscovery.getTokens().contains(Integer.valueOf(tToken))) {
 							if(tCluster instanceof NeighborCluster) {
 								Logging.log(this, "Reporting " + tCluster + " to " + getPeerL2Address().getDescr() + " because " + pDiscovery.getDistance() + " + " + ((NeighborCluster)tCluster).getClusterDistanceToTarget() + "=" + (pDiscovery.getDistance() + ((NeighborCluster)tCluster).getClusterDistanceToTarget()));
 								Logging.log(this, "token list was " + pDiscovery.getTokens());
 							}
 							getPathTo(pDiscovery, tCluster);
 							for(ICluster tNeighbor : tCluster.getNeighbors()) {
 								ClusterName tFirstClusterName = new ClusterName(tCluster.getToken(), tCluster.getClusterID(), tCluster.getHierarchyLevel()); 
 								ClusterName tSecondClusterName = new ClusterName(tNeighbor.getToken(), tNeighbor.getClusterID(), tNeighbor.getHierarchyLevel()); 
 								pDiscovery.addNeighborRelation(tFirstClusterName, tSecondClusterName);
 							}
 						} else {
 							/*
 							 * Maybe print out additional stuff if required
 							 */
 						}
 					}
 				}
 			}
 		} else {
 			if(pDiscovery.getDiscoveryEntries() != null) {
 				HashMap<ClusterName, ClusterName> tToSetNegotiator = new HashMap<ClusterName, ClusterName>();
 				for(DiscoveryEntry tEntry : pDiscovery.getDiscoveryEntries()) {
 					tToSetNegotiator.put(handleDiscoveryEntry(tEntry), tEntry.getPredecessor());
 				}
 			}
 		}
 	}
 	
 	public ClusterName handleDiscoveryEntry(DiscoveryEntry pEntry) throws PropertyException
 	{
 		Logging.trace(this, "Handling " + pEntry);
 		ICluster tNewCluster = getHRMController().getCluster(new ClusterName(pEntry.getToken(), pEntry.getClusterID(), pEntry.getLevel()));
 		if(tNewCluster == null) {
 			for(Cluster tCluster : getHRMController().getAllClusters()) {
 				if(tCluster.equals(new ClusterName(pEntry.getToken(), pEntry.getClusterID(), new HierarchyLevel(this, getParent().getHierarchyLevel().getValue() - 1)))) {
 					tNewCluster = tCluster;
 					if(tNewCluster instanceof NeighborCluster && ((NeighborCluster)tNewCluster).getCoordinatorsAddress() == null && tNewCluster.getCoordinatorName() == null) {
 						Logging.log(this, "Filling required information into " + tNewCluster);
 						tNewCluster.setSuperiorCoordinator(null, pEntry.getCoordinatorName(), pEntry.getToken(), pEntry.getCoordinatorRoutingAddress());
 					}
 				}
 			}
 			if(tNewCluster == null) {
 				/*
 				 * Be aware of the fact that the new attached cluster has lower level
 				 */
 				tNewCluster = new NeighborCluster(pEntry.getClusterID(), pEntry.getCoordinatorName(), pEntry.getCoordinatorRoutingAddress(), pEntry.getToken(), pEntry.getLevel(), getHRMController());
 				
 				getParent().getHRMController().setSourceIntermediateCluster(tNewCluster, getParent().getHRMController().getSourceIntermediate((ICluster)getParent()));
 				((NeighborCluster)tNewCluster).addAnnouncedCEP(this);
 				tNewCluster.setToken(pEntry.getToken());
 				tNewCluster.setPriority(pEntry.getPriority());
 				getHRMController().registerNodeARG(tNewCluster);
 				try {
 					getHRMController().getHRS().mapFoGNameToL2Address(tNewCluster.getCoordinatorName(), ((NeighborCluster)tNewCluster).getCoordinatorsAddress());
 				} catch (RemoteException tExc) {
 					Logging.err(this, "Unable to register " + tNewCluster.getCoordinatorName(), tExc);
 				}
 				Logging.log(this, "Created " + tNewCluster);
 			}
 			
 			((NeighborCluster)tNewCluster).addAnnouncedCEP(this);
 //			((NeighborCluster)tNewCluster).setClusterHopsOnOpposite(pEntry.getClusterHops(), this);
 		}
 		if(pEntry.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tLink : pEntry.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tLink.getSource(), tLink.getDestination(), tLink.getPath());
 			}
 		}
 		return new ClusterName(tNewCluster.getToken(), tNewCluster.getClusterID(), tNewCluster.getHierarchyLevel());
 	}
 	
 	public Route getRouteToPeer()
 	{
 		return getMultiplexer().getRouteToPeer(this);
 	}
 	
 	private ComChannelMuxer getMultiplexer()
 	{
 		return ((ICluster)getParent()).getMultiplexer();
 	}
 
 	
 	
 	
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mParent.toString() + "(PeerPrio=" + mPeerPriority.getValue() + (getPeerL2Address() != null ? ", PeerL2Addres=" + getPeerL2Address() + ", Peer=" + getPeerHRMID() : "") + ")";
 	}
 }
