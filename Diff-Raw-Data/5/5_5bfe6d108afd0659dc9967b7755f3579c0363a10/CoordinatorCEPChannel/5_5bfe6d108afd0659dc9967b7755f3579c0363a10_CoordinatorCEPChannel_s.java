 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.coordination;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.ClusterDiscovery.NestedDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.RequestCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest.ResultType;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData.FIBEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.*;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.HierarchicalRoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterName;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.IRoutableClusterGraphNode;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.RoutableClusterGraphLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionManager;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMIPMapper;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 
 /**
  * The class is used for the communication between a cluster and its coordinator.
  * For this purpose, both the cluster object and the coordinator object have a reference to this object. 
  */
 public class CoordinatorCEPChannel implements IRoutableClusterGraphNode
 {
 	private static final long serialVersionUID = -8290946480171751216L;
 	private ClusterName mRemoteCluster;
 	private ICluster mPeerCluster;
 	private BullyPriority mPeerPriority = null;
 	private boolean mReceivedBorderNodeAnnouncement = false;
 	private boolean mPeerIsCoordinatorForNeighborZone = false;
 	private boolean mIsEdgeRouter = false;
 	private boolean mKnowsCoordinator = false;
 	private HashMap<ICluster, ICluster> mAnnouncerMapping;
 	private boolean mPartOfCluster = false;
 	private HRMController mHRMController = null;
 	private BFSDistanceLabeler<IRoutableClusterGraphNode, RoutableClusterGraphLink> mBreadthFirstSearch;
 	private boolean mCrossLevelCEP = false;
 	
 	/**
 	 * 
 	 * @param pHRMController is the coordinator of a node
 	 * @param pPeerCluster is the peer cluster/coordinator
 	 */
 	public CoordinatorCEPChannel(HRMController pHRMController, ICluster pPeerCluster)
 	{
 		mHRMController = pHRMController;
 		mPeerCluster = pPeerCluster;
 		mPeerPriority = new BullyPriority(this);
 		Logging.log(this, "CREATED for " + mPeerCluster);
 	}
 	
 	/**
 	 * Handles a Bully signaling packet.
 	 * 
 	 * @param pBullyPacket the Bully signaling packet
 	 */
 	private void handleSignalingMessageBully(SignalingMessageBully pPacketBully) throws NetworkException
 	{
 		Node tNode = getHRMController().getNode();
 		Name tLocalNodeName = tNode.getCentralFN().getName(); 
 				
 		boolean BULLY_SIGNALING_DEBUGGING = true;
 
 		/**
 		 * ELECT
 		 */
 		if(pPacketBully instanceof BullyElect)	{
 			
 			// cast to Bully elect packet
 			BullyElect tPacketBullyElect = (BullyElect)pPacketBully;
 			
 			if (BULLY_SIGNALING_DEBUGGING)
 				Logging.log(this, "BULLY-received from \"" + mPeerCluster + "\" an ELECT: " + tPacketBullyElect);
 
 			if ((getPeer().getCoordinatorCEP() != null) && (getPeer().getHighestPriority().isHigher(this, tPacketBullyElect.getSenderPriority()))) {
 				
 				mPeerPriority = tPacketBullyElect.getSenderPriority();
 				
 				if (getPeer().getHRMController().equals(tLocalNodeName)) {
 					// create ANNOUNCE packet
 					HRMSignature tSignature = getHRMController().getIdentity().createSignature(tNode.toString(), null, getPeer().getHierarchyLevel());
 					
 					BullyAnnounce tAnnouncePacket = new BullyAnnounce(tLocalNodeName, getPeer().getBullyPriority(), tSignature, getPeer().getToken());
 					
 					for(CoordinatorCEPChannel tCEP : getPeer().getParticipatingCEPs()) {
 						tAnnouncePacket.addCoveredNode(tCEP.getPeerName());
 					}
 					if(tAnnouncePacket.getCoveredNodes() == null || (tAnnouncePacket.getCoveredNodes() != null && tAnnouncePacket.getCoveredNodes().isEmpty())) {
 						Logging.log(this, "Sending announce that does not cover anyhting");
 					}
 
 					// send packet
 					if (BULLY_SIGNALING_DEBUGGING)
 						Logging.log(this, "BULLY-sending to \"" + mPeerCluster + "\" an ANNOUNCE: " + tAnnouncePacket);
 					sendPacket(tAnnouncePacket);
 					
 				} else {
 					// create ALIVE packet
 					BullyAlive tAlivePacket = new BullyAlive(tLocalNodeName);
 					
 					// send packet
 					if (BULLY_SIGNALING_DEBUGGING)
 						Logging.log(this, "BULLY-sending to \"" + mPeerCluster + "\" an ALIVE: " + tAlivePacket);
 					sendPacket(tAlivePacket);
 					//TODO: packet is sent but never parsed or a timeout timer reset!!
 				}
 			} else {
 				// store peer's Bully priority
 				//TODO: peer prio direkt mal abspeichern und auf grte checken!
 				mPeerPriority = tPacketBullyElect.getSenderPriority();
 				
 				// create REPLY packet
 				BullyReply tReplyPacket = new BullyReply(tLocalNodeName, getPeer().getBullyPriority());
 				
 				// send the answer packet
 				if (BULLY_SIGNALING_DEBUGGING)
 					Logging.log(this, "BULLY-sending to \"" + mPeerCluster + "\" a REPLY: " + tReplyPacket);
 				sendPacket(tReplyPacket);
 			}
 		}
 		
 		/**
 		 * REPLY
 		 */
 		if(pPacketBully instanceof BullyReply) {
 			
 			// cast to Bully replay packet
 			BullyReply tReplyPacket = (BullyReply)pPacketBully;
 
 			if (BULLY_SIGNALING_DEBUGGING)
 				Logging.log(this, "BULLY-received from \"" + mPeerCluster + "\" a REPLY: " + tReplyPacket);
 
 			// store peer's Bully priority
 			//TODO: peer prio direkt mal abspeichern und auf grte checken!
 			mPeerPriority = tReplyPacket.getSenderPriority();
 		}
 		
 		/**
 		 * ANNOUNCE
 		 */
 		if(pPacketBully instanceof BullyAnnounce)  {
 			// cast to Bully replay packet
 			BullyAnnounce tAnnouncePacket = (BullyAnnounce)pPacketBully;
 
 			if (BULLY_SIGNALING_DEBUGGING)
 				Logging.log(this, "BULLY-received from \"" + mPeerCluster + "\" an ANNOUNCE: " + tAnnouncePacket);
 
 			//TODO: only an intermediate cluster on level 0 is able to store an announcement and forward it once a coordinator is set
 			getPeer().handleBullyAnnounce(tAnnouncePacket, this);
 		}
 
 		/**
 		 * PRIORITY UPDATE
 		 */
 		if(pPacketBully instanceof BullyPriorityUpdate) {
 			// cast to Bully replay packet
 			BullyPriorityUpdate tPacketBullyPriorityUpdate = (BullyPriorityUpdate)pPacketBully;
 
 			if (BULLY_SIGNALING_DEBUGGING)
 				Logging.log(this, "BULLY-received from \"" + mPeerCluster + "\" a PRIORITY UPDATE: " + tPacketBullyPriorityUpdate);
 
 			// store peer's Bully priority
 			mPeerPriority = tPacketBullyPriorityUpdate.getSenderPriority();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pData is the data that should be sent to the receiver side of this connection end point
 	 * @return true if the packet left the central multiplexer and the forwarding node that is attached to a direct down gate
 	 * @throws NetworkException
 	 */
 	public boolean receive(Serializable pData) throws NetworkException
 	{
 		boolean CHANNEL_SIGNALING_DEBUGGING = true;
 
 		Node tNode = getHRMController().getNode();
 		HierarchicalRoutingService tHRS = getHRMController().getHRS();
 		
 		/*
 		 * Invalid data
 		 */
 		if(pData == null) {
 			if (CHANNEL_SIGNALING_DEBUGGING)
 				Logging.log(this, "received from \"" + mPeerCluster + "\" invalid data");
 
 			throw new NetworkException("Received invalid null pointer as data");
 		}
 
 		/*
 		 * main packet processing
 		 */
 		try {
 			
 			/**
 			 * Bully signaling message
 			 */
 			if (pData instanceof SignalingMessageBully) {
 				// cast to a Bully signaling message
 				SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 			
 				// process Bully message
 				handleSignalingMessageBully(tBullyMessage);
 				
 				return true;
 			}
 			
 			/**
 			 * NeighborClusterAnnounce
 			 */
 			if(pData instanceof NeighborClusterAnnounce) {
 				NeighborClusterAnnounce tAnnouncePacket = (NeighborClusterAnnounce)pData;
 
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					Logging.log(this, "NEIGHBOR received from \"" + mPeerCluster + "\" a NEIGHBOR CLUSTER ANNOUNCE: " + tAnnouncePacket);
 
 				if(tAnnouncePacket.isInterASAnnouncement()) {
 					Logging.log(this, tNode.getAS().getName() + " received an announcement from " + tAnnouncePacket.getASIdentification());
 					if(tNode.getAS().getName().equals(tAnnouncePacket.getASIdentification())) {
 						if(!getSourceName().equals(getPeerName())) {
 							for(Route tPath : tHRS.getCoordinatorRoutingMap().getRoute(getSourceName(), getPeerName())) {
 								tAnnouncePacket.addRoutingVector(new RoutingServiceLinkVector(tPath, tHRS.getCoordinatorRoutingMap().getSource(tPath), tHRS.getCoordinatorRoutingMap().getDest(tPath)));
 							}
 						}
 						for(CoordinatorCEPChannel tCEP : getPeer().getParticipatingCEPs()) {
 							boolean tWroteAnnouncement = false;
 							if(tCEP.isEdgeCEP()) {
 								
 								// send packet
 								tCEP.sendPacket(tAnnouncePacket);
 
 								tWroteAnnouncement = true;
 							}
 							Logging.log(this, "Testing " + tCEP + " whether it is an inter as link:" + tWroteAnnouncement);
 						}
 					} else {
 						if(getPeer() instanceof Cluster) {
 							if(!getSourceName().equals(getPeerName())) {
 								RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(getRouteToPeer(), getSourceName(), getPeerName());
 								tAnnouncePacket.addRoutingVector(tVector);
 							}
 							for(CoordinatorCEPChannel tCEP : getPeer().getParticipatingCEPs()) {
 								boolean tWroteAnnouncement = false;
 								if(tCEP.getRemoteClusterName().getHierarchyLevel().getValue() - 1 == tAnnouncePacket.getLevel().getValue()) {
 									
 									// send packet
 									tCEP.sendPacket(tAnnouncePacket);
 									
 									tWroteAnnouncement = true;
 								}
 								Logging.log(this, "Testing " + tCEP + " whether it leads to the clusters coordinator: " + tWroteAnnouncement);
 							}
 						} else if(getPeer() instanceof Coordinator) {
 							Logging.log(this, "Inter AS announcement " + tAnnouncePacket + " is handled by " + getPeer() + " whether it leads to the clusters coordinator");
 							((Coordinator)getPeer()).getManagedCluster().handleNeighborAnnouncement(tAnnouncePacket, this);
 						}
 					}
 				} else {
 					if (!(getPeer() instanceof Cluster)){
 						Logging.err(this, "Peer should be a cluster here");
 					}
 					getPeer().handleNeighborAnnouncement(tAnnouncePacket, this);
 				}
 				Logging.log(this, "Received " + tAnnouncePacket + " from remote cluster " + mRemoteCluster);
 			}
 			
 			
 			/**
 			 * TopologyData
 			 */
 			if(pData instanceof TopologyData) {
 				TopologyData tTopologyPacket = (TopologyData)pData;
 				
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					Logging.log(this, "TOPOLOGY-received from \"" + mPeerCluster + "\" TOPOLOGY DATA: " + tTopologyPacket);
 
 				getPeer().handleTopologyData(tTopologyPacket);
 			}/* else if (pData instanceof NestedDiscovery) {
 				NestedDiscovery tDiscovery = (NestedDiscovery) pData;
 				handleClusterDiscovery(tDiscovery);
 			}*/
 			
 			
 			/**
 			 * RouteRequest
 			 */
 			if(pData instanceof RouteRequest) {
 				RouteRequest tRouteRequestPacket = (RouteRequest) pData;
 				
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					Logging.log(this, " ROUTE-received from \"" + mPeerCluster + "\" ROUTE REQUEST: " + tRouteRequestPacket);
 
 				if(tRouteRequestPacket.getTarget() instanceof HRMID) {
 					HRMName tRequestAddress = tRouteRequestPacket.getSource();
 					HRMName tDestinationAddress = getSourceName();
 					if(!tRouteRequestPacket.isAnswer() && tHRS.getFIBEntry( (HRMID) tRouteRequestPacket.getTarget()) != null && tRequestAddress != null && tRequestAddress.equals(tDestinationAddress)) {
 						/*
 						 * Find out if route request can be solved by this entity without querying a higher coordinator
 						 */
 						for(IRoutableClusterGraphNode tCluster : getHRMController().getClusters(0)) {
 							FIBEntry tEntry = tHRS.getFIBEntry( (HRMID) tRouteRequestPacket.getTarget());
 							if(tCluster instanceof Cluster && tEntry != null && (tEntry.getFarthestClusterInDirection() == null || tEntry.getFarthestClusterInDirection().equals(tCluster))) {
 								Route tRoute = tHRS.getRoutePath( getSourceName(), tRouteRequestPacket.getTarget(), new Description(), tNode.getIdentity());
 								RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 								HRMName tSource = null;
 								HRMName tTarget = null;
 								for(Route tCandidatePath : tHRS.getCoordinatorRoutingMap().getEdges()) {
 									if(tCandidatePath.equals(tPath)) {
 										 tSource = tHRS.getCoordinatorRoutingMap().getSource(tCandidatePath);
 										 tTarget = tHRS.getCoordinatorRoutingMap().getDest(tCandidatePath);
 										 break;
 									}
 								}
 								tRouteRequestPacket.addRoutingVector(new RoutingServiceLinkVector(tRoute, tSource, tTarget));
 								tRouteRequestPacket.setAnswer();
 								tRouteRequestPacket.setResult(ResultType.SUCCESS);
 
 								// send packet
 								sendPacket(tRouteRequestPacket);
 								
 								return true;
 							}
 						}
 					}
 					
 					if(!tRouteRequestPacket.isAnswer() && tRouteRequestPacket.isRouteAccumulation()) {
 						if(getRemoteClusterName().getHierarchyLevel() != getPeer().getHierarchyLevel() && getPeer().isInterASCluster()) {
 							HRMID tAddress =  (HRMID) tRouteRequestPacket.getTarget();
 							LinkedList<Name> tIPAddresses = HRMIPMapper.getHRMIPMapper().getIPFromHRMID(tAddress);
 							Route tRoute = null;
 							if(tIPAddresses != null) {
 								for(Name tTargetAddress : tIPAddresses) {
 									try {
 										tRoute = ((RoutingServiceMultiplexer)tNode.getRoutingService()).getRoute(tNode.getCentralFN(), tTargetAddress, ((RouteRequest)pData).getDescription(), null);
 									} catch (NetworkException tExc) {
 										Logging.info(this, "BGP routing service did not find a route to " + tTargetAddress);
 									}
 									Logging.log(this, "Interop: Route to "+ tAddress + " with IP address " + tTargetAddress + " is " + tRoute);
 								}
 							} else {
 								Logging.err(this, "Unable to distribute addresses because no IP address is available");
 							}
 							if(tRoute != null) {
 								tRouteRequestPacket.setAnswer();
 								tRouteRequestPacket.setRoute(tRoute);
 								
 								// send packet
 								sendPacket(tRouteRequestPacket);
 								
 							}
 						} 
 						return true;
 					} else if(tRouteRequestPacket.isAnswer()) {
 						/*
 						 * In this case normally someone is waiting for this packet to arrive, therefore is not handled by any cluster and you only get a notification.
 						 */
 						synchronized(tRouteRequestPacket) {
 							tRouteRequestPacket.notifyAll();
 						}
 						return true;
 					}
 					if(getPeer() instanceof Cluster) {
 						Coordinator tManager = ((Cluster)getPeer()).getCoordinator();
 						tManager.handleRouteRequest((RouteRequest) pData, getRemoteClusterName());
 						tManager.registerRouteRequest(tRouteRequestPacket.getSession(), this);
 					} else if (getPeer() instanceof Coordinator) {
 						/*
 						 * Normally that case should not appear ...
 						 */
 						((Coordinator)getPeer()).handleRouteRequest((RouteRequest) pData, this);
 					}
 					/*
 					 * This comment relates to the following else if statement: use routing service address as last instance because it is the default and all
 					 * other addresses are derived from the HRMID
 					 */
 				}
 				
 				/**
 				 * HRMID
 				 */
 				if(tRouteRequestPacket.getTarget() instanceof HRMID && !tRouteRequestPacket.isAnswer()) {
 					List<Route> tFinalPath = tHRS.getCoordinatorRoutingMap().getRoute(tRouteRequestPacket.getSource(), tRouteRequestPacket.getTarget());
 					if(tRouteRequestPacket.getRequiredClusters() != null) {
 
						for(ICluster tDummy : tRouteRequestPacket.getRequiredClusters()) {
 							tFinalPath = null;
 							List<Route> tPath = tHRS.getCoordinatorRoutingMap().getRoute(tRouteRequestPacket.getSource(), tRouteRequestPacket.getTarget());
 							
							Cluster tCluster = getHRMController().getCluster(tDummy);
 							LinkedList<HRMName> tAddressesOfCluster = new LinkedList<HRMName>();
 							
 							for(CoordinatorCEPChannel tCEP : tCluster.getParticipatingCEPs()) {
 								tAddressesOfCluster.add(tCEP.getPeerName());
 							}
 							if( tAddressesOfCluster.contains(tHRS.getCoordinatorRoutingMap().getDest(tPath.get(0))) ) {
 								tFinalPath = tPath;
 							} else {
 								for(HRMName tCandidate : tAddressesOfCluster) {
 									List<Route> tOldPath = tPath;
 									tPath = tHRS.getCoordinatorRoutingMap().getRoute(tCandidate, tRouteRequestPacket.getTarget());
 									
 									if(tPath.size() < tOldPath.size()) {
 										List<Route> tFirstPart = tHRS.getCoordinatorRoutingMap().getRoute(tRouteRequestPacket.getSource(), tCandidate); 
 										Route tSegment = (tFirstPart.size() > 0 ? tFirstPart.get(0) : null);
 										if(tSegment != null) {
 											tPath.add(0, tSegment);
 										}
 										tFinalPath = tPath;
 									}
 								}
 							}
 						}
 					}
 					if(tFinalPath != null && !tFinalPath.isEmpty()) {
 						for(Route tSegment : tFinalPath) {
 							tRouteRequestPacket.addRoutingVector(new RoutingServiceLinkVector(tSegment, tHRS.getCoordinatorRoutingMap().getSource(tSegment), tHRS.getCoordinatorRoutingMap().getDest(tSegment)));
 						}
 					}
 					tRouteRequestPacket.setAnswer();
 					
 					// send packet
 					sendPacket(tRouteRequestPacket);
 					
 				} else if(tRouteRequestPacket.getTarget() instanceof HRMID && tRouteRequestPacket.isAnswer()) {
 					synchronized (tRouteRequestPacket) {
 						tRouteRequestPacket.notifyAll();
 					}
 				}
 			}
 			
 			/**
 			 * RequestCoordinator
 			 */
 			if (pData instanceof RequestCoordinator) {
 				RequestCoordinator tRequestCoordinatorPacket = (RequestCoordinator) pData;
 				
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					Logging.log(this, "CHANNEL-received from \"" + mPeerCluster + "\" COORDINATOR REQUEST: " + tRequestCoordinatorPacket);
 
 				if(!tRequestCoordinatorPacket.isAnswer()) {
 					if(getPeer().getCoordinatorCEP() != null) {
 						ICluster tCluster = getPeer().getHRMController().getClusterWithCoordinatorOnLevel(getPeer().getHierarchyLevel().getValue());
 						Logging.log(this, "Name of coordinator is " + tCluster.getCoordinatorName());
 						
 						int tToken = tCluster.getToken();
 						Name tCoordinatorName = tCluster.getCoordinatorName();
 						long tCoordinatorAddress = tCluster.getCoordinatorsAddress().getAddress().longValue();
 						HRMName tL2Address = tCluster.getCoordinatorsAddress();
 						DiscoveryEntry tEntry = new DiscoveryEntry(tToken, tCoordinatorName, tCoordinatorAddress, tL2Address, tCluster.getHierarchyLevel());
 						tEntry.setPriority(getPeer().getCoordinatorPriority());
 						tEntry.setRoutingVectors(getPath(tCluster.getCoordinatorsAddress()));
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
 							getPeer().getHRMController().getCluster(new ClusterName(getPeer().getToken(), ((getSourceName()).getAddress().longValue()), getPeer().getHierarchyLevel())).addNeighborCluster(getPeer().getHRMController().getCluster(tDummy));
 							addAnnouncedCluster(getHRMController().getCluster(tDummy), getRemoteClusterName());
 						}
 					}
 					synchronized(tRequestCoordinatorPacket) {
 						Logging.log(this, "Received answer to " + tRequestCoordinatorPacket + ", notifying");
 						tRequestCoordinatorPacket.mWasNotified = true;
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
 			DiscoveryEntry tEntry = new DiscoveryEntry(pCluster.getToken(), pCluster.getCoordinatorName(), pCluster.getClusterID(), pCluster.getCoordinatorsAddress(), pCluster.getHierarchyLevel());
 			tEntry.setClusterHops(getPeer().getHRMController().getClusterDistance(pCluster));
 			tEntry.setPriority(pCluster.getBullyPriority());
 			tEntry.setRoutingVectors(getPath(pCluster.getCoordinatorsAddress()));
 			if(pCluster.isInterASCluster()) {
 				tEntry.setInterASCluster();
 			}
 			
 			List<RoutableClusterGraphLink> tClusterList = getHRMController().getRoutableClusterGraph().getRoute(getPeer(), pCluster);
 			if(!tClusterList.isEmpty()) {
 				ICluster tPredecessorCluster = (ICluster) getHRMController().getRoutableClusterGraph().getDest(pCluster, tClusterList.get(tClusterList.size()-1));
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
 	public ICluster getPeer()
 	{
 		return mPeerCluster;
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
 	
 	public boolean receivedBorderNodeAnnouncement()
 	{
 		return mReceivedBorderNodeAnnouncement;
 	}
 	
 	public ICluster getNegotiator(ICluster pCluster)
 	{
 		if(mAnnouncerMapping == null) {
 			mAnnouncerMapping = new HashMap<ICluster, ICluster>();
 		}
 		ICluster tCluster = mAnnouncerMapping.get(pCluster);
 		return tCluster;
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
 
 	public void setPeerPriority(BullyPriority pPeerPriority)
 	{
 		if (pPeerPriority == null){
 			Logging.warn(this, "Trying to set NULL POINTER as Bully priority, ignoring this request");
 			return;
 		}
 			
 		mPeerPriority = pPeerPriority;
 	}
 	
 	public void addAnnouncedCluster(ICluster pAnnounced, ICluster pNegotiate)
 	{
 		Logging.log(this, "Cluster " + pAnnounced + " as announced by " + pNegotiate);
 		if(pNegotiate == null) {
 			return;
 		}
 		if(mAnnouncerMapping == null) {
 			mAnnouncerMapping = new HashMap<ICluster, ICluster>();
 		}
 		if(!mAnnouncerMapping.containsKey(pAnnounced)) {
 			mAnnouncerMapping.put(pAnnounced, pNegotiate);
 		} else {
 			Logging.log(this, "comparing " + pNegotiate + " to " + mAnnouncerMapping.get(pAnnounced));
 			if(mAnnouncerMapping.get(pAnnounced).getHierarchyLevel().isHigher(this,  pNegotiate.getHierarchyLevel())) {
 				mAnnouncerMapping.remove(pAnnounced);
 				mAnnouncerMapping.put(pAnnounced, pNegotiate);
 			} else if (pNegotiate instanceof NeighborCluster && mAnnouncerMapping.get(pAnnounced) instanceof NeighborCluster && ((NeighborCluster)pNegotiate).getClusterDistanceToTarget() < ((NeighborCluster)mAnnouncerMapping.get(pAnnounced)).getClusterDistanceToTarget()) {
 				Logging.log(this, "replacing negotiating cluster of " + pAnnounced + ": " + mAnnouncerMapping.get(pAnnounced) + " with " + pNegotiate);
 				mAnnouncerMapping.remove(pAnnounced);
 				mAnnouncerMapping.put(pAnnounced, pNegotiate);
 			}
 		}
 	}
 	
 	public void setRemoteClusterName(ClusterName pClusterName)
 	{
 		Logging.log(this, "Setting remote/peer cluster " + pClusterName);
 		mRemoteCluster = pClusterName;
 	}
 
 	public CoordinatorCEPMultiplexer getCEPMultiplexer()
 	{
 		return getMultiplexer();
 	}
 	
 	public boolean sendPacket(Serializable pData)
 	{
 		Logging.log(this, "Sending to \"" + getRemoteClusterName() + "\" the packet " + pData);
 		
 		if(pData instanceof RequestCoordinator) {
 //			mRequestedCoordinator = true;
 			Logging.log(this, "Sending " + pData);
 		}
 		if(getPeer() instanceof Coordinator && !mCrossLevelCEP) {
 			getCEPMultiplexer().write(pData, this, new ClusterName(getPeer().getToken(), ((L2Address)getPeerName()).getAddress().longValue(), getPeer().getHierarchyLevel()));
 		} else {
 			getCEPMultiplexer().write(pData, this, getRemoteClusterName());
 		}
 		return true;
 	}
 	
 	public HRMName getPeerName()
 	{
 		return getMultiplexer().getPeerRoutingServiceAddress(this);
 	}
 	
 	public HRMName getSourceName()
 	{
 		return getMultiplexer().getSourceRoutingServiceAddress(this);
 	}
 	
 	public boolean isPeerCoordinatorForNeighborZone()
 	{
 		return mPeerIsCoordinatorForNeighborZone;
 	}
 	
 	public boolean isPartOfMyCluster()
 	{
 		return mPartOfCluster;
 	}
 	
 	public boolean isEdgeCEP()
 	{
 		return mIsEdgeRouter;
 	}
 	
 	public void setEdgeCEP()
 	{
 		ElectionManager.getElectionManager().removeElection(getPeer().getHierarchyLevel().getValue(), getPeer().getClusterID());
 		mIsEdgeRouter = true;
 	}
 	
 	public void handleClusterDiscovery(NestedDiscovery pDiscovery, boolean pRequest) throws PropertyException, NetworkException
 	{
 		if(pRequest){
 			Cluster tSourceCluster = getHRMController().getCluster(new ClusterName(pDiscovery.getToken(), pDiscovery.getSourceClusterID(), pDiscovery.getLevel()));
 			if(tSourceCluster == null) {
 				Logging.err(this, "Unable to find appropriate cluster for" + pDiscovery.getSourceClusterID() + " and token" + pDiscovery.getToken() + " on level " + pDiscovery.getLevel() + " remote cluster is " + getRemoteClusterName());
 			}
 			if(mBreadthFirstSearch == null ) {
 				mBreadthFirstSearch = new BFSDistanceLabeler<IRoutableClusterGraphNode, RoutableClusterGraphLink>();
 			}
 			mBreadthFirstSearch.labelDistances(getHRMController().getRoutableClusterGraph().getGraphForGUI(), tSourceCluster);
 			List<IRoutableClusterGraphNode> tDiscoveryCandidates = mBreadthFirstSearch.getVerticesInOrderVisited();
 			if(tSourceCluster != null) {
 				for(IRoutableClusterGraphNode tVirtualNode : tDiscoveryCandidates) {
 					if(tVirtualNode instanceof ICluster) {
 						ICluster tCluster = (ICluster) tVirtualNode;
 						
 						int tRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 						Logging.log(this, "Radius is " + tRadius);
 						
 						if(tCluster instanceof NeighborCluster && ((NeighborCluster)tCluster).getClusterDistanceToTarget() + pDiscovery.getDistance() > tRadius) continue;
 						boolean tBreak=false;
 						for(CoordinatorCEPChannel tCEP : tCluster.getParticipatingCEPs()) {
 							if(tCEP.isEdgeCEP()) tBreak = true;
 						}
 						if(tBreak) {
 							continue;
 						}
 						int tToken = tCluster.getToken();
 						if(!pDiscovery.getTokens().contains(Integer.valueOf(tToken))) {
 							if(tCluster instanceof NeighborCluster) {
 								Logging.log(this, "Reporting " + tCluster + " to " + getPeerName().getDescr() + " because " + pDiscovery.getDistance() + " + " + ((NeighborCluster)tCluster).getClusterDistanceToTarget() + "=" + (pDiscovery.getDistance() + ((NeighborCluster)tCluster).getClusterDistanceToTarget()));
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
 				for(ClusterName tDummy : tToSetNegotiator.keySet()) {
 					addAnnouncedCluster(getHRMController().getCluster(tDummy), getHRMController().getCluster(tToSetNegotiator.get(tDummy)));
 				}
 			}
 		}
 	}
 	
 	public ClusterName handleDiscoveryEntry(DiscoveryEntry pEntry) throws PropertyException
 	{
 		Logging.trace(this, "Handling " + pEntry);
 		ICluster tNewCluster = getHRMController().getCluster(new ClusterName(pEntry.getToken(), pEntry.getClusterID(), pEntry.getLevel()));
 		if(tNewCluster == null) {
 			for(Cluster tCluster : getHRMController().getRoutingTargetClusters()) {
 				if(tCluster.equals(new ClusterName(pEntry.getToken(), pEntry.getClusterID(), new HierarchyLevel(this, getPeer().getHierarchyLevel().getValue() - 1)))) {
 					tNewCluster = tCluster;
 					if(tNewCluster instanceof NeighborCluster && tNewCluster.getCoordinatorsAddress() == null && tNewCluster.getCoordinatorName() == null) {
 						Logging.log(this, "Filling required information into " + tNewCluster);
 						tNewCluster.setCoordinatorCEP(null, null, pEntry.getCoordinatorName(), pEntry.getCoordinatorRoutingAddress());
 						if(pEntry.isInterASCluster()){
 							tNewCluster.setInterASCluster();
 						}
 					}
 				}
 			}
 			if(tNewCluster == null) {
 				/*
 				 * Be aware of the fact that the new attached cluster has lower level
 				 */
 				tNewCluster = new NeighborCluster(pEntry.getClusterID(), pEntry.getCoordinatorName(), pEntry.getCoordinatorRoutingAddress(), pEntry.getToken(), pEntry.getLevel(), getHRMController());
 				
 				getPeer().getHRMController().setSourceIntermediateCluster(tNewCluster, getPeer().getHRMController().getSourceIntermediate(getPeer()));
 				((NeighborCluster)tNewCluster).addAnnouncedCEP(this);
 				tNewCluster.setToken(pEntry.getToken());
 				tNewCluster.setPriority(pEntry.getPriority());
 				getHRMController().addRoutableTarget(tNewCluster);
 				if(pEntry.isInterASCluster()) {
 					tNewCluster.setInterASCluster();
 				}
 				try {
 					getHRMController().getHRS().registerNode(tNewCluster.getCoordinatorName(), tNewCluster.getCoordinatorsAddress());
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
 	
 	@Override
 	public int getSerialisedSize()
 	{
 		return 0;
 	}
 	
 	public HRMID getHrmID()
 	{
 		return null;
 	}
 
 	@Override
 	public Namespace getNamespace()
 	{
 		return null;
 	}
 	
 	private CoordinatorCEPMultiplexer getMultiplexer()
 	{
 		return getPeer().getMultiplexer();
 	}
 
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + getPeer().getClusterDescription() +  "(PeerPrio=" + mPeerPriority.getValue() + (getPeerName() != null ? ", Peer=" + getPeerName().getDescr() : "") + "EdgeRouter=" + (mIsEdgeRouter ? "yes" : "no") + ")";
 	}
 }
