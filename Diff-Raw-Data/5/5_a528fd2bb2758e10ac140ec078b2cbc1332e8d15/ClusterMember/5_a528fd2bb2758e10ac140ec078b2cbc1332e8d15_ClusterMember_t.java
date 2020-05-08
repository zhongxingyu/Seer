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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.ISignalingMessageHrmBroadcastable;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AnnounceHRMIDs;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingTable;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster member (can also be a cluster head).
  */
 public class ClusterMember extends ClusterName
 {
 	private static final long serialVersionUID = -8746079632866375924L;
 
 	/**
 	 * Stores the L2 address of the node where the coordinator of the addressed cluster is located
 	 */
 	private L2Address mCoordinatorNodeL2Address = null;
 	
 	/**
 	 * Stores the elector which is responsible for coordinator elections for this cluster.
 	 */
 	protected Elector mElector = null;
 
 	/**
 	 * Returns the cluster activation
 	 */
 	private boolean mClusterActivation = false;
 	
 	/**
 	 * Stores the network interface for base hierarchy level
 	 */
 	private NetworkInterface mBaseHierarchyLevelNetworkInterface = null;
 
 	/**
 	 * Stores the HRMID which is assigned to this node.
 	 * This variable is only used for L0.
 	 */
 	protected HRMID mAssignedL0HRMID = null;
 
 	/**
 	 * Stores the currently reported routing table based on setL0HRMID calls
 	 */
 	private RoutingTable mReportedRoutingTableL0HRMID = new RoutingTable();
 	
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pCoordinatorID the unique coordinator ID for this cluster
 	 * @param pCoordinatorNodeL2Address the L2 address of the node where the coordinator of this cluster is located
 	 */
 	public ClusterMember(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID, int pCoordinatorID, L2Address pCoordinatorNodeL2Address)
 	{	
 		super(pHRMController, pHierarchyLevel, pClusterID, pCoordinatorID);
 
 		// store the L2 address of the node where the coordinator is located
 		mCoordinatorNodeL2Address = pCoordinatorNodeL2Address;
 	}
 
 	/**
 	 * Factory function
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pClusterName a ClusterName which includes the hierarchy level, the unique ID of this cluster, and the unique coordinator ID
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pClusterHeadNodeL2Address the L2 address of the node where the cluster head is located
 	 */
 	public static ClusterMember create(HRMController pHRMController, ClusterName pClusterName, L2Address pClusterHeadNodeL2Address)
 	{	
 		ClusterMember tResult = new ClusterMember(pHRMController, pClusterName.getHierarchyLevel(), pClusterName.getClusterID(), pClusterName.getCoordinatorID(), pClusterHeadNodeL2Address);
 		
 		Logging.log(tResult, "\n\n\n################ CREATED CLUSTER MEMBER at hierarchy level: " + (tResult.getHierarchyLevel().getValue()));
 
 		// creates new elector object, which is responsible for Bully based election processes
 		tResult.mElector = new Elector(pHRMController, tResult);
 
 		// register at HRMController's internal database
 		pHRMController.registerClusterMember(tResult);
 
 		return tResult;
 	}
 
 	/**
 	 * Detect the network interface of the L0 ClusterMember
 	 */
 	public void detectNetworkInterface()
 	{
 		if(getHierarchyLevel().isBaseLevel())  
 		{
 			if(!(this instanceof Cluster)){
 				if(getBaseHierarchyLevelNetworkInterface() == null){
 
 					/**
 					 * The following is FoGSiEm specific and allows for an easy detection of the network interface for a ClusterMember
 					 */
 					ComChannel tThisClusterChannelToHead = getComChannelToClusterHead();
 //					Logging.err(this, "      ..channel to cluster head: " + tThisClusterChannelToHead);
 					if(tThisClusterChannelToHead != null){
 						L2Address tThisClusterChannelToHeadL2Address = tThisClusterChannelToHead.getPeerL2Address();
 //						Logging.err(this, "        ..peer L2Address: " + tThisClusterChannelToHeadL2Address);
 						if(tThisClusterChannelToHeadL2Address != null){
 							LinkedList<Cluster> tClusters = mHRMController.getAllClusters(0);
 							for(ClusterMember tCluster : tClusters){
 //								Logging.err(this, "   ..found other L0 Cluster: " + tCluster);
 								LinkedList<ComChannel> tThisClusterChannels = tCluster.getComChannels();
 								for(ComChannel tThisClusterChannel : tThisClusterChannels){
 //									Logging.err(this, "      ..channel of this cluster: " + tThisClusterChannel);
 									L2Address tThisClusterChannelPeerL2Address = tThisClusterChannel.getPeerL2Address();
 //									Logging.err(this, "        ..peer L2Address: " + tThisClusterChannelPeerL2Address);
 									if(tThisClusterChannelPeerL2Address != null){
 										if(tThisClusterChannelPeerL2Address.equals(tThisClusterChannelToHeadL2Address)){
 											/**
 											 * We found a Cluster, which has a comm. channel to the same peer node which is also the peer node of this ClusterMember
 											 */
 											if(tCluster.getBaseHierarchyLevelNetworkInterface() != null){
 												// update the network interface
 												setBaseHierarchyLevelNetworkInterface(tCluster.getBaseHierarchyLevelNetworkInterface());
 //												Logging.err(this, "        ..SET: " + tCluster.getBaseHierarchyLevelNetworkInterface());
 												return;
 											}										
 										}
 									}
 								}								
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the network interface of this cluster (only for base hierarchy level)
 	 * 
 	 * @param pInterfaceToNeighbor the network interface
 	 */
 	public void setBaseHierarchyLevelNetworkInterface(NetworkInterface pInterfaceToNeighbor)
 	{
 		Logging.log(this, "Setting network interface (base hierarchy level) to: " + pInterfaceToNeighbor);
 		mBaseHierarchyLevelNetworkInterface = pInterfaceToNeighbor;
 		
 		/**
 		 * The following is FoGSiEm specific and allows for an easy detection of the network interface for a ClusterMember
 		 */
 		LinkedList<ClusterMember> tMembers = mHRMController.getAllClusterMembers(0);
 //		Logging.err(this, "   ..found other L0 ClusterMember: " + tMembers);
 		for(ClusterMember tMember : tMembers){
 			// avoid recursive access
 			if(!tMember.equals(this)){
 //				Logging.err(this, "    ..ClusterMember: " + tMember);
 				// we only want to have ClusterMember instances
 				if(!(tMember instanceof Cluster)){
 					ComChannel tMemberChannel = tMember.getComChannelToClusterHead();
 //					Logging.err(this, "      ..channel to cluster head: " + tMemberChannel);
 					if(tMemberChannel != null){
 						L2Address tMemberChannelPeerL2Address = tMemberChannel.getPeerL2Address();
 //						Logging.err(this, "        ..peer L2Address: " + tMemberChannelPeerL2Address);
 						LinkedList<ComChannel> tThisClusterChannels = getComChannels();
 						for(ComChannel tThisClusterChannel : tThisClusterChannels){
 //							Logging.err(this, "      ..channel of this cluster: " + tThisClusterChannel);
 							L2Address tThisClusterChannelPeerL2Address = tThisClusterChannel.getPeerL2Address();
 //							Logging.err(this, "        ..peer L2Address: " + tThisClusterChannelPeerL2Address);
 							if(tThisClusterChannelPeerL2Address != null){
 								if(tThisClusterChannelPeerL2Address.equals(tMemberChannelPeerL2Address))
 								{
 									/**
 									 * We found another ClusterMember, which has a comm channel to the same peer node which is also a peer node of this cluster
 									 */
 									if(tMember.getBaseHierarchyLevelNetworkInterface() == null){
 										// update the network interface
 										tMember.setBaseHierarchyLevelNetworkInterface(pInterfaceToNeighbor);
 									}
 								}
 							}
 						}
 					}					
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns the network interface of this cluster (only for base hierarchy level)
 	 * 
 	 * @return the network interface
 	 */
 	public NetworkInterface getBaseHierarchyLevelNetworkInterface()
 	{
 		return mBaseHierarchyLevelNetworkInterface;
 	}
 
 	/**
 	 * Assign new HRMID for being addressable.
 	 *  
 	 * @param pCaller the caller who assigns the new HRMID
 	 * @param pHRMID the new HRMID
 	 */
 	@Override
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		super.setHRMID(pCaller, pHRMID);
 
 		if(getHierarchyLevel().isBaseLevel()){
 			// is this a new HRMID?
 			if((pHRMID != null) && (!pHRMID.equals(mAssignedL0HRMID)) && (!pHRMID.isClusterAddress()) && (!pHRMID.isZero())){
 				Logging.log(this, "ASSINGED L0 HRMID=" + pHRMID + " (old=" + (mAssignedL0HRMID != null ? mAssignedL0HRMID.toString() : "null") + ", assigner=" + pCaller + ")");
 		
 				// update the HRMID
 				setL0HRMID(pHRMID.clone());
 
 				/**
 				 * Announce in all L0 clusters the new set of local node HRMIDs
 				 */				
 				LinkedList<ClusterMember> tL0ClusterMember = mHRMController.getAllL0ClusterMembers();
 				if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.log(this, "    ..distributing AnnounceHRMIDs in: " + tL0ClusterMember);
 				}
 				for(ClusterMember tClusterMember : tL0ClusterMember){
 					tClusterMember.distributeAnnounceHRMIDs();					
 				}
 			}
 		}
 	}
 
 	/**
 	 * EVENT: need HRG update
 	 * This function updates the HRG based on locally detected ClusterMember siblings.
 	 */
 	private void eventNeedHRGUpdate(String pCause)
 	{
 		boolean tDebug = false;
 		Logging.log(this, "EVENT: need HRG update");
 		
 		if((mAssignedL0HRMID != null) && (!mAssignedL0HRMID.isZero())){
 			// backup old reported routing table
 			RoutingTable tDeprecatedReportedRoutingTable = null;
 			synchronized (mReportedRoutingTableL0HRMID) {
 				tDeprecatedReportedRoutingTable = (RoutingTable) mReportedRoutingTableL0HRMID.clone();
 				if(tDebug){
 					Logging.err(this, "   ..previously reported routing table: " + tDeprecatedReportedRoutingTable);
 				}
 			}
 	
 			/**
 			 * Find other active ClusterMember instances and create a local loopback route to them
 			 */
 			// iterate over all siblings
 			RoutingTable tNewReportedRoutingTable = new RoutingTable();
 			LinkedList<ClusterMember> tSiblings = mHRMController.getAllClusterMembers(getHierarchyLevel());
 			for(ClusterMember tSibling : tSiblings){
 				if(tSibling.isActiveCluster()){
 					HRMID tSiblingL0Address = tSibling.getL0HRMID();
 					// has the sibling a valid L0 address?
 					if((tSiblingL0Address != null) && (!tSiblingL0Address.isZero())){
 						// avoid recursion
 						if(!tSibling.equals(this)){
 							if(tDebug){
 								Logging.err(this, "  ..found active sibling: " + tSibling);
 							}
 							
							HRMID tGeneralizedSiblingL0Address = mAssignedL0HRMID.getForeignCluster(tSiblingL0Address);
									
 							// create the new reported routing table entry
							RoutingEntry tRoutingEntryToSibling = RoutingEntry.create(mAssignedL0HRMID /* this cluster */, tGeneralizedSiblingL0Address /* the sibling */, tSiblingL0Address, 0 /* loopback route */, RoutingEntry.NO_UTILIZATION, RoutingEntry.NO_DELAY, RoutingEntry.INFINITE_DATARATE, pCause + ", ::eventNeedHRGUpdate()");
 							// even more details about the route to make it distinguishable from others
 							tRoutingEntryToSibling.setNextHopL2Address(mHRMController.getNodeL2Address());
 	
 							// add the entry to the reported routing table
 							if(tDebug){							
 								Logging.err(this, "   ..found (" + pCause + ") local route: " + tRoutingEntryToSibling);
 							}
 							tNewReportedRoutingTable.addEntry(tRoutingEntryToSibling);
 						}
 					}
 				}
 			}
 			
 			/**
 			 * Step 1: learn new routes:
 			 * 			- set the new value for reported routes based on setL0HRMID
 			 * 			- and inform the HRMController
 			 */ 
 			synchronized (mReportedRoutingTableL0HRMID) {
 				mReportedRoutingTableL0HRMID = tNewReportedRoutingTable;
 				for(RoutingEntry tEntry : mReportedRoutingTableL0HRMID){
 					if(tDebug){
 						Logging.err(this, "   ..adding (" + pCause + ") to the HRG the route: " + tEntry);
 					}
 					
 					/**
 					 * Update HRG: register L0 link
 					 */
 					RoutingEntry tRoutingEntryToSibling2 = tEntry.clone();
 					tRoutingEntryToSibling2.extendCause("registerLinkHRG()(" + pCause + ")");
 					mHRMController.registerLinkHRG(tEntry.getSource(), tEntry.getNextHop(), tRoutingEntryToSibling2);
 					
 					/**
 					 * Update HRG: register cluster-2-cluster links
 					 */ 
 					RoutingEntry tRoutingEntryToSibling3 = tEntry.clone();
 					tRoutingEntryToSibling3.extendCause("registerAutoHRG()(" + pCause + ")");
 					mHRMController.registerAutoHRG(tRoutingEntryToSibling3);
 				}
 			}
 			
 			/**
 			 * Step 2: forget deprecated routes:
 			 * 			- derive the deprecated routes
 			 * 			- inform the HRS about the deprecated routing table entries
 			 */ 
 			tDeprecatedReportedRoutingTable.delEntries(tNewReportedRoutingTable);
 			synchronized (tDeprecatedReportedRoutingTable) {
 				for(RoutingEntry tEntry : tDeprecatedReportedRoutingTable){
 					if(tDebug){
 						Logging.err(this, "   ..removing (" + pCause + ") from the HRG the route: " + tEntry);
 					}
 	
 					/**
 					 * Update HRG: unregister L0 link
 					 */
 					RoutingEntry tRoutingEntryToSibling2 = tEntry.clone();
 					tRoutingEntryToSibling2.extendCause("unregisterLinkHRG()(" + pCause + ")");
 					mHRMController.unregisterLinkHRG(tEntry.getSource(),  tEntry.getNextHop(), tRoutingEntryToSibling2);
 					
 					/**
 					 * Update HRG: register cluster-2-cluster links
 					 */ 
 					RoutingEntry tRoutingEntryToSibling3 = tEntry.clone();
 					tRoutingEntryToSibling3.extendCause("unregisterAutoHRG()(" + pCause + ")");
 					mHRMController.unregisterAutoHRG(tRoutingEntryToSibling3);
 				}
 			}
 		}else{
 			Logging.log(this, "eventNeedHRGUpdate() skipped because local L0 HRMID is: " + mAssignedL0HRMID);
 		}
 	}
 	
 	/**
 	 * Sets the new L0 address for this physical node
 	 * 
 	 * @param pNewL0HRMID the new L0 HRMID
 	 */
 	private int mCallsSetL0HRMID = 0;
 	protected void setL0HRMID(HRMID pNewL0HRMID)
 	{
 		mCallsSetL0HRMID++;
 		
 		// is this a new HRMID?
 		if((pNewL0HRMID != null) && (!pNewL0HRMID.equals(mAssignedL0HRMID)) && (!pNewL0HRMID.isClusterAddress()) && (!pNewL0HRMID.isZero())){
 			Logging.log(this, "ASSIGNED new (" + mCallsSetL0HRMID + ") L0 node HRMID: " + pNewL0HRMID);
 
 			if((mAssignedL0HRMID != null) && (!mAssignedL0HRMID.isZero())){
 				/**
 				 * Unregister old HRMID
 				 */
 				mHRMController.unregisterHRMID(this, mAssignedL0HRMID, this + "::setL0HRMID()(" + mCallsSetL0HRMID + ")");
 			}
 			
 			/**
 			 * Set the new L0 address for this physical node
 			 */ 
 			mAssignedL0HRMID = pNewL0HRMID;
 			
 			/**
 			 * Register new HRMID
 			 */
 			mHRMController.registerHRMID(this, mAssignedL0HRMID, this + "::setL0HRMID()(" + mCallsSetL0HRMID + ")");
 
 			/**
 			 * Trigger: update HRG
 			 */
 			LinkedList<ClusterMember> tClusterMembers = mHRMController.getAllClusterMembers(getHierarchyLevel());
 			for(ClusterMember tClusterMember : tClusterMembers){
 				if(tClusterMember.isActiveCluster()){
 					tClusterMember.eventNeedHRGUpdate(this + "::setL0HRMID()(" + mCallsSetL0HRMID + ")");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns the L0 address which was assigned by this L0 (if it is so) cluster for this physical node
 	 *  
 	 * @return the assigned HRMID
 	 */
 	public HRMID getL0HRMID()
 	{
 		return mAssignedL0HRMID;	
 	}
 
 	/**
 	 * Returns the comm. channel to the cluster head
 	 * 
 	 * @return the comm. channel
 	 */
 	public ComChannel getComChannelToClusterHead()
 	{
 		ComChannel tResult = null;
 		
 		LinkedList<ComChannel> tChannels = getComChannels();
 		if(tChannels.size() == 1){
 			tResult = tChannels.getFirst();
 		}else{
 			Logging.err(this, "Found an invalid amount of comm. channels: " + tChannels);
 		}
 			
 		return tResult;
 	}
 	
 	/**
 	 * SEND: AnnounceHRMIDs to all known cluster members
 	 */
 	public void distributeAnnounceHRMIDs()
 	{
 		if(getHierarchyLevel().isBaseLevel()){
 			// only announce in active clusters, avoid unnecessary packets here
 			if(isActiveCluster()){
 				if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.log(this, "Distributing AnnounceHRMIDs...");
 				}
 	
 				LinkedList<HRMID >tLocalHRMIDs = mHRMController.getHRMIDs();
 				if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.log(this, "    ..found local HRMIDs: " + tLocalHRMIDs);
 				}
 				
 				/**
 				 * Filter local HRMIDs for L0 node HRMIDs
 				 */
 				LinkedList<HRMID >tLocalL0HRMIDs = new LinkedList<HRMID>();
 				for(HRMID tLocalHRMID : tLocalHRMIDs){
 					// is the HRMID a cluster address?
 					if(!tLocalHRMID.isClusterAddress()){
 						// ignore this ClusterMember's node specific L0 HRMID, which is already known to the peer
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 							if(!tLocalHRMID.equals(mAssignedL0HRMID)){
 								Logging.log(this, "    ..found L0 node HRMID: " + tLocalHRMID.toString());
 							}else{
 								Logging.log(this, "    ..ignoring L0 node HRMID: " + tLocalHRMID.toString());
 							}
 						}
 						tLocalL0HRMIDs.add(tLocalHRMID);
 					}else{
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 							Logging.log(this, "    ..ignoring cluster HRMID: " + tLocalHRMID.toString());
 						}
 					}
 				}
 				
 				if (tLocalL0HRMIDs.size() > 0){
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 						Logging.log(this, "Distributing AnnounceHRMIDs packets..");
 					}
 		
 					HRMID tSenderHRMID = getHRMID();
 					if(mAssignedL0HRMID != null){
 						tSenderHRMID = mAssignedL0HRMID;
 					}
 
 					/**
 					 * Announce the HRMIDs to the peer
 					 */
 					// create the packet
 					AnnounceHRMIDs tAnnounceHRMIDsPacket = new AnnounceHRMIDs(tSenderHRMID, null, tLocalL0HRMIDs);
 					// send the packet
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 						Logging.err(this, "    ..broadcasting (L0-HRMID: " + mAssignedL0HRMID + "): " + tAnnounceHRMIDsPacket);
 					}
 					sendClusterBroadcast(tAnnounceHRMIDsPacket, false);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) store the topology information locally
 	 *       2.) forward the announcement within the same hierarchy level ("to the side")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorAnnouncement(ComChannel pComChannel, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "EVENT: coordinator announcement (from side): " + pAnnounceCoordinator);
 		}
 		
 		/**
 		 * Storing that the announced coordinator is a superior one of this node
 		 */
 		// is the packet still on its way from the top to the bottom AND does it not belong to an L0 coordinator?
 		if((!pAnnounceCoordinator.enteredSidewardForwarding()) && (!pAnnounceCoordinator.getSenderClusterName().getHierarchyLevel().isBaseLevel())){
 			mHRMController.registerSuperiorCoordinator(pAnnounceCoordinator.getSenderClusterName());
 		}
 
 		/**
 		 * Check if we should forward this announcement "to the side"
 		 */
 		// is this the 2+ passed ClusterMember OR (in case it is the first passed ClusterMember) the peer is the origin of the announce -> forward the announcement 
 		Route tRoute = pAnnounceCoordinator.getRoute();
 		if(((tRoute != null) && (!tRoute.isEmpty()) && (tRoute.getFirst() != null)) || (pAnnounceCoordinator.getSenderClusterCoordinatorNodeL2Address().equals(pComChannel.getPeerL2Address()))){
 			/**
 			 * Duplicate the packet and write to the duplicate
 			 */
 			AnnounceCoordinator tForwardPacket = (AnnounceCoordinator)pAnnounceCoordinator.duplicate();
 
 			/**
 			 * Record the passed clusters
 			 */
 			tForwardPacket.addGUIPassedCluster(new Long(getGUIClusterID()));
 	
 			/**
 			 * Enlarge the stored route towards the announcer
 			 */
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "      ..adding route: " + pComChannel.getRouteToPeer());
 			}
 			tForwardPacket.addRouteHop(pComChannel.getRouteToPeer());
 			
 			/**
 			 * Store the announced remote coordinator in the ARG 
 			 */
 			registerAnnouncedCoordinatorARG(this, tForwardPacket);
 			
 			/**
 			 * transition from one cluster to the next one => decrease TTL value
 			 */
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "Deacreasing TTL of: " + tForwardPacket);
 			}
 			tForwardPacket.decreaseTTL(); //TODO: decreasen in abhaengigkeit der hier. ebene -> dafuer muss jeder L0 cluster wissen welche hoeheren cluster darueber liegen
 		
 			/**
 			 * forward the announcement if the TTL is still okay
 			 */
 			if(tForwardPacket.isTTLOkay()){
 				// do we have a loop?
 				if(!tForwardPacket.hasPassedNode(mHRMController.getNodeL2Address())){
 					/**
 					 * Record the passed nodes
 					 */
 					tForwardPacket.addPassedNode(mHRMController.getNodeL2Address());
 					
 					/**
 					 * Check if this announcement is already on its way sidewards
 					 */
 					if(!tForwardPacket.enteredSidewardForwarding()){
 						// are we a cluster member of a cluster, which is located on the same node from where this announcement comes from? -> forward the packet to the side
 						if (pComChannel.getPeerL2Address().equals(tForwardPacket.getSenderClusterCoordinatorNodeL2Address())){
 							/**
 							 * mark packet as "sideward forwarded"
 							 */
 							tForwardPacket.setSidewardForwarding();
 						}else{
 							// we are a cluster member of any cluster located at a node where this announcement was received from a superior coordinator
 							
 							/**
 							 * drop the packet and return immediately
 							 */ 
 							return;
 						}
 					}
 		
 					/**
 					 * Forward the announcement within the same hierarchy level ("to the side")
 					 */
 					// get locally known neighbors for this cluster and hierarchy level
 					LinkedList<Cluster> tLocalClusters = mHRMController.getAllClusters(getHierarchyLevel());
 					if(tLocalClusters.size() > 0){
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "     ..found " + tLocalClusters.size() + " neighbor clusters");
 						}
 			
 						for(Cluster tLocalCluster: tLocalClusters){
 							/**
 							 * Forward the announcement
 							 * HINT: we avoid loops by excluding the sender from the forwarding process
 							 */
 							if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 								Logging.log(this, "     ..fowarding this event to locally known neighbor cluster: " + tLocalCluster);
 							}
 							
 							// forward this announcement to all cluster members
 							tLocalCluster.sendClusterBroadcast(tForwardPacket, true, pComChannel.getPeerL2Address() /* exclude this from the forwarding process */);
 						}
 					}else{
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "No neighbors found, ending forwarding of: " + tForwardPacket);
 						}
 					}
 				}else{
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 						Logging.warn(this, "eventCoordinatorAnnouncement() found a forwarding loop for: " + tForwardPacket + "\n   ..passed clusters: " + tForwardPacket.getGUIPassedClusters()+ "\n   ..passed nodes: " + tForwardPacket.getPassedNodes());
 					}
 				}
 			}else{
 				if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 					Logging.log(this, "TTL exceeded for coordinator announcement: " + tForwardPacket);
 				}
 			}
 		}
 	}
 
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) store the topology information locally
 	 *       2.) forward the announcement within the same hierarchy level ("to the side")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorInvalidation(ComChannel pComChannel, InvalidCoordinator pInvalidCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "EVENT: coordinator invalidation (from side): " + pInvalidCoordinator);
 		}
 		
 		/**
 		 * Duplicate the packet and write to the duplicate
 		 */
 		InvalidCoordinator tForwardPacket = (InvalidCoordinator)pInvalidCoordinator.duplicate();
 
 		/**
 		 * Store the announced remote coordinator in the ARG 
 		 */
 		unregisterAnnouncedCoordinatorARG(this, tForwardPacket);
 		
 		/**
 		 * transition from one cluster to the next one => decrease TTL value
 		 */
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "Deacreasing TTL of: " + tForwardPacket);
 		}
 		tForwardPacket.decreaseTTL(); //TODO: decreasen in abhaengigkeit der hier. ebene -> dafuer muss jeder L0 cluster wissen welche hoeheren cluster darueber liegen
 	
 		/**
 		 * forward the announcement if the TTL is still okay
 		 */
 		if(tForwardPacket.isTTLOkay()){
 			// do we have a loop?
 			if(!tForwardPacket.hasPassedNode(mHRMController.getNodeL2Address())){
 				/**
 				 * Record the passed nodes
 				 */
 				tForwardPacket.addPassedNode(mHRMController.getNodeL2Address());
 
 				/**
 				 * Check if this announcement is already on its way sidewards
 				 */
 				if(!tForwardPacket.enteredSidewardForwarding()){
 					// are we a cluster member of a cluster, which is located on the same node from where this announcement comes from? -> forward the packet to the side
 					if (pComChannel.getPeerL2Address().equals(tForwardPacket.getSenderClusterCoordinatorNodeL2Address())){
 						/**
 						 * mark packet as "sideward forwarded"
 						 */
 						tForwardPacket.setSidewardForwarding();
 					}else{
 						// we are a cluster member of any cluster located at a node where this announcement was received from a superior coordinator
 						
 						/**
 						 * drop the packet and return immediately
 						 */ 
 						return;
 					}
 				}
 	
 				/**
 				 * Forward the announcement within the same hierarchy level ("to the side")
 				 */
 				// get locally known neighbors for this cluster and hierarchy level
 				LinkedList<Cluster> tLocalClusters = mHRMController.getAllClusters(getHierarchyLevel());
 				if(tLocalClusters.size() > 0){
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 						Logging.log(this, "     ..found " + tLocalClusters.size() + " neighbor clusters");
 					}
 		
 					for(Cluster tLocalCluster: tLocalClusters){
 						/**
 						 * Forward the announcement
 						 * HINT: we avoid loops by excluding the sender from the forwarding process
 						 */
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 							Logging.log(this, "     ..fowarding this event to locally known neighbor cluster: " + tLocalCluster);
 						}
 						
 						// forward this announcement to all cluster members
 						tLocalCluster.sendClusterBroadcast(tForwardPacket, true, pComChannel.getPeerL2Address() /* exclude this from the forwarding process */);
 					}
 				}else{
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 						Logging.log(this, "No neighbors found, ending forwarding of: " + tForwardPacket);
 					}
 				}
 			}else{
 				if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 					Logging.warn(this, "eventCoordinatorAnnouncement() found a forwarding loop for: " + tForwardPacket + "\n   ..passed nodes: " + tForwardPacket.getPassedNodes());
 				}
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 				Logging.log(this, "TTL exceeded for coordinator invalidation: " + tForwardPacket);
 			}
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
 		
 		/**
 		 * Trigger: established for the comm. channel
 		 */
 		pComChannel.eventEstablished();
 
 		/**
 		 * Trigger: network interface detection
 		 */
 		if(getHierarchyLevel().isBaseLevel())
 		{
 			if(!(this instanceof Cluster)){
 				detectNetworkInterface();
 			}
 		}
 		
 		/**
 		 * Trigger: start coordinator election
 		 */
 		boolean tStartBaseLevel =  ((getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.START_AUTOMATICALLY_BASE_LEVEL));
 		// start coordinator election for the created HRM instance if the configuration allows this
 		if(((!getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY)) || (tStartBaseLevel)){
 			Logging.log(this, "      ..starting ELECTION");
 			mElector.startElection();
 		}
 	}
 
 	/**
 	 * EVENT: cluster membership request, a cluster requests of a coordinator to acknowledge cluster membership, triggered by the comm. session
 	 * 
 	 * @param pRemoteClusterName the description of the possible new cluster member
 	 * @param pSourceComSession the comm. session where the packet was received
 	 */
 	public void eventClusterMembershipRequest(ClusterName pRemoteClusterName, ComSession pSourceComSession)
 	{
 		Logging.log(this, "EVENT: got cluster membership request from: " + pRemoteClusterName);
 		
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
 		 * Trigger: comm. channel established 
 		 */
 		eventComChannelEstablished(tComChannel);
 		
 		/**
 		 * SEND: acknowledgment -> will be answered by a BullyPriorityUpdate
 		 */
 		tComChannel.signalRequestClusterMembershipAck(null);
 
 		/**
 		 * Trigger: joined a remote cluster (sends a Bully priority update)
 		 */
 		eventJoinedRemoteCluster(tComChannel);
 	}
 
 	/**
 	 * EVENT: we have joined the superior cluster, triggered by ourself or the CoordinatorAsClusterMemeber if a request for cluster membership was ack'ed
 	 * 
 	 * @param pComChannelToRemoteCluster the comm. channel to the cluster
 	 */
 	protected void eventJoinedRemoteCluster(ComChannel pComChannelToRemoteCluster)
 	{
 		Logging.log(this, "HAVE JOINED remote cluster");
 		
 		/**
 		 * Trigger: joined remote cluster (in Elector)
 		 */
 		mElector.eventJoinedRemoteCluster(pComChannelToRemoteCluster);
 	}
 
 	/**
 	 * Sends a packet as broadcast to all cluster members
 	 * 
 	 * @param pPacket the packet which has to be broadcasted
 	 * @param pIncludeLoopback should loopback communication be included?
 	 * @param pExcludeL2Address describe a node which shouldn't receive this broadcast if we are at base hierarchy level
 	 */
 	protected void sendClusterBroadcast(ISignalingMessageHrmBroadcastable pPacket, boolean pIncludeLoopback, L2Address pExcludeL2Address)
 	{
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		// get the L2Addres of the local host
 		L2Address tLocalL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 			Logging.log(this, "Sending BROADCASTS from " + tLocalL2Address + " the packet " + pPacket + " to " + tComChannels.size() + " communication channels, local base prio: " + mHRMController.getHierarchyNodePriority(getHierarchyLevel()));
 		}
 		
 		for(ComChannel tComChannel : tComChannels) {
 			boolean tIsLoopback = tComChannel.toLocalNode();
 			
 			if((pExcludeL2Address == null /* excluded peer address is null, we send everywhere */) || (!pExcludeL2Address.equals(tComChannel.getPeerL2Address()) /* should the peer be excluded? */)){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 					if (!tIsLoopback){
 						Logging.log(this, "       ..to " + tComChannel + ", excluded: " + pExcludeL2Address);
 					}else{
 						Logging.log(this, "       ..to LOOPBACK " + tComChannel);
 					}
 				}
 	
 				if ((pIncludeLoopback) || (!tIsLoopback)){
 					if(tComChannel.isOpen()){
 						SignalingMessageHrm tNewPacket = pPacket.duplicate();
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 							Logging.log(this, "           ..sending duplicate packet: " + tNewPacket);
 						}
 						// send the packet to one of the possible cluster members
 						tComChannel.sendPacket(tNewPacket);
 					}else{
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 							Logging.log(this, "             ..sending skipped because we are still waiting for establishment of channel: " + tComChannel);
 						}
 					}
 				}else{
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 						Logging.log(this, "              ..skipping " + (tIsLoopback ? "LOOPBACK CHANNEL" : ""));
 					}
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 					Logging.log(this, "              ..skipping EXCLUDED DESTINATION: " + pExcludeL2Address);
 				}
 			}
 		}
 	}
 	public void sendClusterBroadcast(ISignalingMessageHrmBroadcastable pPacket, boolean pIncludeLoopback)
 	{
 		sendClusterBroadcast(pPacket, pIncludeLoopback, null);
 	}
 
 	/**
 	 * Sets a new link state for all comm. channels
 	 * 
 	 * @param pState the new state
 	 * @param pCause the cause for this change
 	 */
 	public void setLAllinksActivation(boolean pState, String pCause)
 	{
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 		
 		Logging.log(this, "Setting new link state (" + pState + ") to all " + tComChannels.size() + " comm. channels");
 
 		for(ComChannel tComChannel : tComChannels) {
 			tComChannel.setLinkActivation(pState, pCause);
 		}
 	}
 
 	/**
 	 * Returns all active links
 	 * 
 	 * @return the active links
 	 */
 	public LinkedList<ComChannel> getActiveLinks()
 	{
 		LinkedList<ComChannel> tResult = new LinkedList<ComChannel>();
 		
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 		
 		for(ComChannel tComChannel : tComChannels) {
 			if(tComChannel.isLinkActive()){
 				tResult.add(tComChannel);
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines the coordinator of this cluster. It is "null" if the election was lost or hasn't finished yet. 
 	 * 
 	 * @return the cluster's coordinator
 	 */
 	public Coordinator getCoordinator()
 	{
 		Logging.err(this, "!!!!! >>> ClusterMember::getCoordinator() should never be called, otherwise, an error in higher clustering code exists <<< !!!!!");
 		return null;
 	}
 
 	/**
 	 * Returns how many connected cluster members are known
 	 * 
 	 * @return the count
 	 */
 	public int countConnectedClusterMembers()
 	{
 		int tResult = 0;
 
 		// count all communication channels
 		tResult = getComChannels().size();
 
 		return tResult;
 	}
 
 	/**
 	 * Returns how many connected external cluster members are known
 	 * 
 	 * @return the count
 	 */
 	public int countConnectedRemoteClusterMembers()
 	{
 		int tResult = 0;
 
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		for(ComChannel tComChannel : tComChannels) {
 			// filter loopback channels
 			if (tComChannel.toRemoteNode()){
 				tResult++;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns if this ClusterMember belongs to a remote cluster
 	 * 
 	 * @return true or false
 	 */
 	public boolean isRemoteCluster()
 	{
 		if(this instanceof Cluster){
 			return false;
 		}
 		
 		return (countConnectedRemoteClusterMembers() > 0);
 	}
 	
 	/**
 	 * EVENT: new connectivity node priority
 	 * 
 	 * @param pNewConnectivityNodePriority the new connectivity node priority
 	 */
 	public void eventConnectivityNodePriorityUpdate(long pNewConnectivityNodePriority)
 	{
 		Logging.log(this, "EVENT: base node priority update to:  " + pNewConnectivityNodePriority);
 		
 		if(getHierarchyLevel().isBaseLevel()){
 			/**
 			 * Set the new priority if it differs from the old one
 			 */
 			if((getPriority() == null) || (getPriority().getValue() != pNewConnectivityNodePriority) || (this instanceof Cluster /* a Cluster always reports the current priority! */)){
 				Logging.log(this, "Got new connectivity node priority, updating own priority from " + (getPriority() != null ? getPriority().getValue() : "null") + " to " + pNewConnectivityNodePriority);
 				setPriority(BullyPriority.create(this, pNewConnectivityNodePriority));
 			}else{
 				Logging.log(this, "   ..skipping priority update, current priority: " + getPriority());
 			}
 		}else{
 			throw new RuntimeException("Got a call to ClusterMemeber::eventConnectivityNodePriorityUpdate at higher hierarchy level " + getHierarchyLevel().getValue());
 		}
 	}
 
 	/**
 	 * EVENT: cluster memeber role invalid
 	 * 
 	 *  @param: pComChannel the comm. channel towards the cluster head
 	 */
 	public void eventClusterMemberRoleInvalid(ComChannel pComChannel)
 	{
 		Logging.log(this, "============ EVENT: cluster member role invalid, channel: " + pComChannel);
 		
 		/**
 		 * Trigger: Elector invalid
 		 */
 		getElector().eventInvalidation();
 
 		/**
 		 * Trigger: role invalid
 		 */
 		eventInvalidation();
 
 		unregisterComChannel(pComChannel);
 
 		Logging.log(this, "============ Destroying this CoordinatorAsClusterMember now...");
 
 		/**
 		 * Unregister from the HRMController's internal database
 		 */ 
 		mHRMController.unregisterClusterMember(this);
 	}
 
 	/**
 	 * Sets a new Bully priority
 	 * 
 	 * @param pPriority the new Bully priority
 	 */
 	@Override
 	public void setPriority(BullyPriority pPriority)
 	{
 		BullyPriority tOldPriority = getPriority();
 		
 		if((pPriority != null) && (!pPriority.isUndefined())){
 			/**
 			 * Set the new priority
 			 */
 			super.setPriority(pPriority);
 	
 			/**
 			 * Send priority update if necessary 
 			 */
 			if (((tOldPriority != null) && (!tOldPriority.isUndefined()) && (!tOldPriority.equals(pPriority))) || (this instanceof Cluster)){
 				if(mElector != null){
 					mElector.updatePriority();
 				}else{
 					Logging.warn(this, "Elector is still invalid");
 				}
 			}else{
 				Logging.log(this, "First priority was set: " + pPriority.getValue());
 			}
 		}else{
 			Logging.err(this, "REQUEST FOR SETTING UNDEFINED PRIORITY");
 		}
 	}
 
 	/**
 	 * Returns a hash code for this object.
 	 * This function is used within the ARG for identifying objects.
 	 * 
 	 * @return the hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return getClusterID().intValue();
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
 	 * Returns the L2 address of the node where the coordinator of the described cluster is located
 	 * 
 	 * @return the L2 address
 	 */
 	public Name getCoordinatorNodeL2Address()
 	{
 		return mCoordinatorNodeL2Address;
 	}
 	
 	/**
 	 * Sets the cluster activation, triggered by the Elector or the Cluster which got a new local Coordinator
 	 * 
 	 * @param pState the new state
 	 */
 	public void setClusterActivation(boolean pState)
 	{
 		boolean tOldState = mClusterActivation;
 		
 		/**
 		 * Update the cluster state 
 		 */
 		if(mClusterActivation != pState){
 			Logging.log(this, "Setting cluster activation to: " + pState);
 			
 			mClusterActivation = pState;
 		}
 
 		/**
 		 * If it is a transition from "false" to " true", then distribute AnnounceHRMIDs
 		 */
 		if((!tOldState) && (pState)){
 			distributeAnnounceHRMIDs();
 		}
 	}
 	
 	/**
 	 * Returns the cluster activation
 	 *  
 	 * @return true or false
 	 */
 	public boolean isActiveCluster()
 	{
 		return mClusterActivation;
 	}
 
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return "RemoteCluster" + getGUIClusterID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue() + "(" + idToString() + ")";
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
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return (getGUICoordinatorID() > 0 ? "Coordinator" + getGUICoordinatorID() : "") + (getCoordinatorNodeL2Address() != null ? ", CoordNode.=" + getCoordinatorNodeL2Address() : "");
 		}else{
 			return (getGUICoordinatorID() > 0 ? "Coordinator" + getGUICoordinatorID() : "") + (getCoordinatorNodeL2Address() != null ? ", CoordNode.=" + getCoordinatorNodeL2Address() : "") + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
