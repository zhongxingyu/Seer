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
 
 import de.tuilmenau.ics.fog.application.util.Session;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnouncePhysicalEndPoint;
 import de.tuilmenau.ics.fog.packets.hierarchical.MultiplexHeader;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used to manage a Session of a connection between two physical nodes.
  * Such a communication session is able to handle several inferior communication channels,
  * which handle again the communication between two control entities of the HRM infrastructure.
  * 
  * There exist a 1:1 relation between a FoG connection and a ComSession instance.
  * 
  */
 public class ComSession extends Session
 {
 
 	/**
 	 * Stores the L2Address of the peer - this reference is used within getPeerL2Address() of CommunicationChannel and CommunicationChannelMultiplexer
 	 */
 	private L2Address mPeerL2Address = null;
 
 	/**
 	 * Stores a reference to the HRMController application.
 	 */
 	private HRMController mHRMController = null;
 	
 	/**
 	 * Stores the registered ComChannel objects
 	 */
 	private LinkedList<ComChannel> mRegisteredComChannels = new LinkedList<ComChannel>();
 	
 	/**
 	 * Stores a reference to the parent FoG connection
 	 */
 	private Connection mParentConnection = null;
 	
 	/**
 	 * Stores the target of this comm. session
 	 */
 	private L2Address mTargetL2Address = null;
 	
 	/**
 	 * Stores the parent control entity
 	 */
 	private ControlEntity mParent = null;
 	
 	private boolean mIncomingConnection = false;
	private L2Address mCentralFNL2Address = null;
 	private HierarchyLevel mHierarchyLevel = null;
 	private Route mRouteToPeer;
 	
 	/**
 	 * 
 	 * @param pHRMController is the HRMController instance this connection end point is associated to
 	 * @param pIncomingConnection indicates whether the connection is incoming or outgoing
 	 * @param pLevel the hierarchy level of this session
 	 * @param pComChannelMuxer the communication multiplexer to use
 	 * 
 	 */
 	public ComSession(HRMController pHRMController, boolean pIncomingConnection, ControlEntity pParent, HierarchyLevel pLevel)
 	{
 		// call the Session constructor
 		super(false, Logging.getInstance(), null);
 		
 		// store a reference to the HRMController application
 		mHRMController = pHRMController;
 		
 		// store the parental control entity
 		mParent = pParent;
 		
 		// store the hierarchy level
 		mHierarchyLevel = pLevel;
 
		mCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address(); 
		
 		// store the connection direction
 		mIncomingConnection = pIncomingConnection;
 		
 		// register at the HRMController as incoming or outgoing session
 		if (mIncomingConnection){
 		    mHRMController.registerIncomingSession(this);
 		}else{
 		    mHRMController.registerOutgoingSession(this);
 		}
 			
 		if (mIncomingConnection){
 			Logging.log(this, "SERVER SESSION CREATED");
 		}else{
 			Logging.log(this, "CLIENT SESSION CREATED");
 		}
 	}
 	
 	/**
 	 * Sends a packet to along the connection 
 	 * 
 	 * @param pData is the data that should be sent
 	 * @return true if success, otherwise false
 	 */
 	public boolean write(Serializable pData)
 	{
 		boolean tResult = false;
 		
 		if (pData instanceof MultiplexHeader){
 			MultiplexHeader tMultiplexHeader = (MultiplexHeader)pData;
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 				Logging.log(this, "SENDING MULTIPLEX HEADER with destination: " + tMultiplexHeader.getReceiverClusterName()  + ", payload=" + tMultiplexHeader.getPayload());
 			}
 			
 			if(tMultiplexHeader.getPayload() instanceof SignalingMessageHrm){
 				SignalingMessageHrm tSignalingHRMPacket = (SignalingMessageHrm)tMultiplexHeader.getPayload();
 				
 				// add source route entry
 				tSignalingHRMPacket.addSourceRoute("[S]: " + this.toString());
 			}
 		}
 		
 		if(getConnection() != null && getConnection().isConnected()) {
 			try	{
 				Logging.log(this, "SENDING PACKET: " + pData.getClass().getSimpleName());
 
 				getConnection().write(pData);
 				tResult = true;
 			} catch (NetworkException tExc) {
 				Logging.err(this, "Unable to send " + pData + " because write operation failed", tExc);
 			}
 		} else {
 			Logging.err(this, "Unable to send " + pData + " because of invalid connection: " + getConnection());
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * This function gets called when the physical end point at remote side is locally known
 	 */
 	private void eventSessionAvailable()
 	{
 		Logging.log(this, "EVENT: session is available now");
 		
 		for(ComChannel tComChannel : getAllComChannels()){
 			/**
 			 * TRIGGER: inform the ComChannel about the established communication session
 			 */
 			tComChannel.eventParentComSessionEstablished();
 		}
 	}
 
 	/**
 	 * Determines the route to the peer (its central FN)
 	 *  
 	 * @return the route to the central FN of the peer
 	 */
 	public Route getRouteToPeer()
 	{
 		return mRouteToPeer;
 	}
 
 	/**
 	 * EVENT: the route to the peer was announced, triggered by ourself if a AnnouncePhysicalEndPoint packet was received, we react by:
 	 * 		1.) store the route to the peer (to its central FN)
 	 * 
 	 * @param pRouteToPeer the route to the peer
 	 */
 	private void eventRouteToPeerAvailable(Route pRouteToPeer)
 	{
 		Logging.log(this, "Setting route to peer " + getPeerL2Address() + " as " + pRouteToPeer);
 		if(mRouteToPeer == null){
 			Logging.log(this, "OLD ROUTE TO PEER WAS INVALID");
 		}
 		
 		/**
 		 * Inform the HRS about the complete route to the peer
 		 */
 		// HINT: we do this only on base hierarchy level, in the higher layers this routing information is already known (otherwise the hierarchy couldn't have grown until this point)
 		if(mHierarchyLevel.isBaseLevel()) {
 			Logging.log(this, "      ..registering route to peer: " + pRouteToPeer);
 			mHRMController.addRouteToDirectNeighbor(mPeerL2Address, pRouteToPeer);
 		}
 		
 		mRouteToPeer = pRouteToPeer;
 	}
 	
 	/**
 	 * Sets new L2Address for peer (central FN)
 	 * 
 	 * @param pL2Address the new L2Address
 	 */
 	private void setPeerL2Address(L2Address pL2Address)
 	{
 		mPeerL2Address = pL2Address;
 	}
 	
 	/**
 	 * Returns the L2Address of the peer (central FN)
 	 * 
 	 * @return the peer L2Address (central FN)
 	 */
 	public L2Address getPeerL2Address()
 	{
 		return mPeerL2Address;
 	}
 	
 	/**
 	 * Returns a reference to the local HRMController application
 	 * 
 	 * @return reference to the HRMController application
 	 */
 	public HRMController getHRMController()
 	{
 		return mHRMController;
 	}
 	
 	/**
 	 * Registers a communication channel
 	 * 
 	 * @param pComChannel the communication channel, which should be registered
 	 */
 	public void registerComChannel(ComChannel pComChannel)
 	{
 		Logging.log(this, "Registering communication channel: " + pComChannel);
 		
 		synchronized (mRegisteredComChannels) {
 			mRegisteredComChannels.add(pComChannel);			
 		}
 	}
 	
 	/**
 	 * Registers a communication channel
 	 * 
 	 * @param pComChannel the communication channel, which should be registered
 	 */
 	public void unregisterComChannel(ComChannel pComChannel)
 	{
 		Logging.log(this, "Unregistering communication channel: " + pComChannel);
 		
 		boolean tLastChannelClosed = false; //needed because of mutex usage below
 		synchronized (mRegisteredComChannels) {
 			mRegisteredComChannels.remove(pComChannel);
 			
 			if (mRegisteredComChannels.size() == 0){
 				Logging.log(this, "    ..last inferior comm. channel was unregistered");
 
 				tLastChannelClosed = true;
 			}
 		}
 		
 		if(tLastChannelClosed){
 			/**
 			 * Trigger the event "all channels lost"
 			 */
 			eventAllChannelsClosed();
 		}		
 	}
 	
 	/**
 	 * EVENT: all inferior channels were closed
 	 */
 	private void eventAllChannelsClosed()
 	{
 		if (HRMConfig.Hierarchy.AUTO_CLEANUP_FOR_CONNECTIONS){
 			Logging.log(this, "\n\n\n########### closing the parent connection(destination=" + mTargetL2Address + ", requirements=" + getConnection().getRequirements() + ")");
 			
 			//stop the session (closes the connection)
 			stop();
 		}
 	}
 
 	/**
 	 * Returns all registered communication channels
 	 * 
 	 * @return the list of known communication channels
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ComChannel> getAllComChannels()
 	{
 		LinkedList<ComChannel> tResult = new LinkedList<ComChannel>();
 		
 		synchronized (mRegisteredComChannels) {
 			tResult = (LinkedList<ComChannel>) mRegisteredComChannels.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Handles the packet "AnnouncePhysicalNeighborhood"
 	 * 
 	 * @param pAnnouncePhysicalNeighborhood the packet
 	 */
 	private void handleAnnouncePhysicalEndPoint(AnnouncePhysicalEndPoint pAnnouncePhysicalNeighborhood)
 	{
 		// get the L2Address of the peer
 		setPeerL2Address(pAnnouncePhysicalNeighborhood.getSenderCentralAddress());
 
 		// get the L2Address of the peer, which should be used as routing target
 		L2Address tSenderAddress = pAnnouncePhysicalNeighborhood.getSenderAddress();
 		
 		if (tSenderAddress != null){
 			/**
 			 * Determine the route to the known FN from the peer
 			 */
 			Route tRouteToPeer = null;
 			// search a route form the central FN to the intermediate FN between the central FN and the bus
 			try {
 				tRouteToPeer = mHRMController.getHRS().getRoute(tSenderAddress, new Description(), getHRMController().getNode().getIdentity());
 			} catch (RoutingException tExc) {
 				Logging.err(this, "Unable to find route to ", tExc);
 			} catch (RequirementsException tExc) {
 				Logging.err(this, "Unable to fulfill requirements ", tExc);
 			}
 			
 			/**
 			 * Enlarge the found route by an L2Address in order to have a route which ends at the central FN of the peer node, but avoid trailing duplicates 
 			 */
 			boolean tPeerL2AddressAlreadyKnown = false;
 			// get the last entry of the known route to the peer
 			RouteSegment tRouteToPeerLastEntry = tRouteToPeer.getLast();
 			// is the last entry an address?
 			if (tRouteToPeerLastEntry instanceof RouteSegmentAddress){
 				RouteSegmentAddress tRouteToPeerLastAddress = (RouteSegmentAddress)tRouteToPeerLastEntry;
 				// is the last entry an L2Address? 
 				if(tRouteToPeerLastAddress.getAddress() instanceof L2Address){
 					// get the L2Address from the last entry
 					L2Address tLastAddress = (L2Address)tRouteToPeerLastAddress.getAddress();
 					// is the found L2Address the same like what we would add in the next step?
 					if(tLastAddress.equals(mPeerL2Address)){
 						tPeerL2AddressAlreadyKnown = true;
 					}
 				}
 			}
 			// should we add the peer L2Address as last entry in the route towards the peer?
 			if(!tPeerL2AddressAlreadyKnown){
 				Logging.log(this, ">>> Old route to peer was: " + tRouteToPeer);
 				// add the peer L2Address as last entry in the route to the peer node
 				tRouteToPeer.add(new RouteSegmentAddress(mPeerL2Address));
 				Logging.log(this, ">>> New route to peer is: " + tRouteToPeer);
 				
 				eventRouteToPeerAvailable(tRouteToPeer);
 			}else{
 				Logging.log(this, ">>> Old route to peer: " + tRouteToPeer + " includes already the entry " + mPeerL2Address + " as last entry");
 			}
 		}
 		
 		/**
 		 * Send an answer packet
 		 */
 		if (!pAnnouncePhysicalNeighborhood.isAnswer()){
 			/**
 			 * get the name of the central FN
 			 */
 			L2Address tCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 
 			/**
 			 *  determine the FN between the local central FN and the bus towards the physical neighbor node and tell this the neighbor 
 			 */
 			L2Address tFirstFNL2Address = (tSenderAddress != null ? mHRMController.getL2AddressOfFirstFNTowardsNeighbor(tSenderAddress) : null);
 
 			/**
 			 * Send AnnouncePhysicalNeighborhood to the neighbor
 			 */
 			if (tFirstFNL2Address == null){
 				Logging.warn(this, "handleAnnouncePhysicalEndPoint() wasn't able to determine the first FN towards: " + tSenderAddress);
 			}
 			// create a map between the central FN and the search FN
 			AnnouncePhysicalEndPoint tAnnouncePhysicalNeighborhoodAnswer = new AnnouncePhysicalEndPoint(tCentralFNL2Address, tFirstFNL2Address, AnnouncePhysicalEndPoint.ANSWER_PACKET);
 			// tell the neighbor about the FN
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "     ..sending ANNOUNCE PHYSICAL NEIGHBORHOOD ANSWER " + tAnnouncePhysicalNeighborhoodAnswer);
 			}
 			write(tAnnouncePhysicalNeighborhoodAnswer);
 		}
 		
 		/**
 		 * TRIGGER: session is available now
 		 */
 		eventSessionAvailable();		
 	}
 
 	/**
 	 * Searches for a registered communication channel which is identified by its local clusterID
 	 * 
 	 * @param pClusterID the local clusterID
 	 * 
 	 * @return the found comm. channel or null
 	 */
 	private ComChannel getComChannelByClusterID(Long pClusterID)
 	{
 		ComChannel tResult = null;
 		
 		LinkedList<ComChannel> tComChannels = getAllComChannels();
 		for (ComChannel tComChannel : tComChannels){
 			if(tComChannel.getParent().getClusterID().equals(pClusterID)) {
 				tResult = tComChannel;
 				break;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Searches for a registered communication channel which is identified by its remote clusterID
 	 * 
 	 * @param pClusterID the remote clusterID
 	 * 
 	 * @return the found comm. channel or null
 	 */
 	public ComChannel getComChannelByRemoteClusterID(Long pClusterID)
 	{
 		ComChannel tResult = null;
 		
 		LinkedList<ComChannel> tComChannels = getAllComChannels();
 		for (ComChannel tComChannel : tComChannels){
 			if(tComChannel.getRemoteClusterName().getClusterID().equals(pClusterID)) {
 				tResult = tComChannel;
 				break;
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Handles a multiplex-packet
 	 * 
 	 * @param pMultiplexHeader the multiplex-packet
 	 */
 	private void handleMultiplexHeader(MultiplexHeader pMultiplexHeader)
 	{
 		/**
 		 * Get the target from the Multiplex-Header
 		 */
 		ClusterName tDestination = pMultiplexHeader.getReceiverClusterName();
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 			Logging.log(this, "RECEIVING MULTIPLEX HEADER with destination: " + tDestination  + ", payload=" + pMultiplexHeader.getPayload());
 		}
 		
 		/**
 		 * Iterate over all communication channels and find the correct channel towards the destination
 		 */
 		ComChannel tDestinationComChannel = getComChannelByClusterID(tDestination.getClusterID()); //TODO: in/out beachten und hier die RemoteClusterID auswerten
 		Logging.log(this, "       ..found communication channel: " + tDestinationComChannel);
 
 		/**
 		 * Get the payload
 		 */
 		Serializable tPayload = pMultiplexHeader.getPayload();
 
 		/**
 		 * Forward the payload to the correct communication channel
 		 */
 		if (tDestinationComChannel != null){
 			try {
 				Logging.log(this, "       ..delivering received payload: " + tPayload);
 				tDestinationComChannel.receiveData(tPayload);
 			} catch (NetworkException tExc) {
 				Logging.err(this, "Unable to forward payload " + tPayload + " to " + tDestination + " via " + tDestinationComChannel);
 			}
 		} else {
 			Logging.warn(this, "Unable to find the communication channel for destination: " + tDestination + ", known communication channels are:");
 			Logging.warn(this, getAllComChannels().toString());
 		}
 	}
 
 	/**
 	 * Processes incoming packet data and forward it to the right ComChannel
 	 * 
 	 * @param pData the packet payload
 	 */	
 	@Override
 	public boolean receiveData(Object pData)
 	{
 		Logging.log(this, "RECEIVED PACKET: " + pData.getClass().getSimpleName());
 		
 		/**
 		 * PACKET: AnnouncePhysicalNeighborhood
 		 */
 		if(pData instanceof AnnouncePhysicalEndPoint) {
 			// get the packet
 			AnnouncePhysicalEndPoint tAnnouncePhysicalNeighborhood = (AnnouncePhysicalEndPoint)pData;
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "ANNOUNCE PHYSICAL NEIGHBORHOOD received: " + tAnnouncePhysicalNeighborhood);
 			}
 			
 			handleAnnouncePhysicalEndPoint(tAnnouncePhysicalNeighborhood);
 
 			return true;
 		} 
 		
 		/**
 		 * RequestClusterMembership
 		 */
 		if(pData instanceof RequestClusterMembership) {
 			RequestClusterMembership tRequestClusterMembershipPacket = (RequestClusterMembership)pData;
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "REQUEST_CLUSTER_MEMBERSHIP-received from \"" + tRequestClusterMembershipPacket.getSenderClusterName());
 			}
 			
 			// is the parent a coordinator or a cluster?
 			if (mParent instanceof Cluster){
 				Cluster tCluster = (Cluster)mParent;
 				
 				tCluster.eventMembershipRequest(tRequestClusterMembershipPacket.getSenderClusterName(), this);
 			}else{
 				Logging.err(this, "Expected a Cluster object as parent for processing RequestClusterMembership data but parent is " + mParent);
 			}
 			return true;
 		}
 
 		/**
 		 * PACKET: MultiplexHeader
 		 */
 		if (pData instanceof MultiplexHeader) {
 			MultiplexHeader tMultiplexHeader = (MultiplexHeader) pData;
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 				Logging.log(this, "MULTIPLEX PACKET received: " + tMultiplexHeader);
 			}
 			
 			/**
 			 * SignalingMessageHRM
 			 */
 			if(tMultiplexHeader.getPayload() instanceof SignalingMessageHrm){
 				SignalingMessageHrm tSignalingHRMPacket = (SignalingMessageHrm)tMultiplexHeader.getPayload();
 				
 				// add source route entry
 				tSignalingHRMPacket.addSourceRoute("[R]: " + this.toString());
 			}
 
 			handleMultiplexHeader(tMultiplexHeader);
 
 			return true;
 		}
 
 		Logging.warn(this, ">>>>>>>>>>>>> Found unsupported packet: " + pData);
 		return true;
 	}
 
 	/**
 	 * Starts the superior connection by the following steps:
 	 *     1.) call "start()" from the superior class
 	 *     2.) store the L2Address of the peer
 	 *     3.) store the route to the peer
 	 *     4.) announce the local physical end point to the peer
 	 *     
 	 * @param pTargetL2Address the L2Address of the target, which should be used as routing target
 	 * @param pConnection the superior connection
 	 */
 	public void startConnection(L2Address pTargetL2Address, Connection pConnection)
 	{
 		Logging.log(this, "\n\n###### STARTING connection for target: " + pTargetL2Address);
 		
 		// store the connection
 		mParentConnection = pConnection;
 		
 		// store the target L2 address
 		mTargetL2Address = pTargetL2Address;
 		
 		/**
 		 * Calls "start()" of the superior class
 		 */
 		start(mParentConnection);
 
 		// do we know the target L2Address?
 		if (pTargetL2Address != null){
 			/**
 			 * Update the peer L2Address
 			 */
 			setPeerL2Address(pTargetL2Address);
 			
 //TODO: remove the following? it stores a route to the first FN towards the peer -> but has bad side-effects on the ARG quality, and it isn't needed for a working HRM infrastructure			
 //			/**
 //			 * Find and set the route to peer within the session object
 //			 */
 //			Route tRouteToNeighborFN = null;
 //			// get a route to the neighbor node (the destination of the desired connection)
 //			try {
 //				tRouteToNeighborFN = mHRMController.getHRS().getRoute(pTargetL2Address, new Description(), mHRMController.getNode().getIdentity());
 //			} catch (RoutingException tExc) {
 //				Logging.err(mHRMController, "Unable to find route to " + pTargetL2Address, tExc);
 //			} catch (RequirementsException tExc) {
 //				Logging.err(mHRMController, "Unable to find route to " + pTargetL2Address + " with requirements no requirents, Huh!", tExc);
 //			}
 //			// have we found a route to the neighbor?
 //			if(tRouteToNeighborFN != null) {
 //				/**
 //				 * Complete the found route to a route which ends at the first FN of the peer node towards its central FN
 //				 */
 //				tRouteToNeighborFN.add(new RouteSegmentAddress(mPeerL2Address));
 //				if(mHierarchyLevel.isBaseLevel()) {
 //					Logging.log(this, "      ..registering route to peer first FN: " + tRouteToNeighborFN);
 //					mHRMController.addRouteToDirectNeighbor(mPeerL2Address, tRouteToNeighborFN);
 //				}
 //			}
 		}else{
 			Logging.trace(this, "startConnection() doesn't know the target L2Address, will send the local L2Address to the peer");
 		}
 
 		/**
 		 * announce physical end point
 		 */
 		L2Address tFirstFNL2Address = (pTargetL2Address != null ? mHRMController.getL2AddressOfFirstFNTowardsNeighbor(pTargetL2Address) : null);
 		// HINT: if tFirstFNL2Address is null we send a blind announce to inform the peer about our L2Address
 		// get the name of the central FN
 		L2Address tCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		// create a map between the central FN and the search FN
 		AnnouncePhysicalEndPoint tAnnouncePhysicalEndPoint = new AnnouncePhysicalEndPoint(tCentralFNL2Address, tFirstFNL2Address, AnnouncePhysicalEndPoint.INIT_PACKET);
 		// tell the neighbor about the FN
 		Logging.log(mHRMController, "     ..sending ANNOUNCE PHYSICAL NEIGHBORHOOD");
 		write(tAnnouncePhysicalEndPoint);
 		
 		/**
 		 * Trigger event "communication available"
 		 */
 		mParent.eventCommunicationAvailable();
 	}
 
 	/**
 	 * Descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		if(getPeerL2Address() != null ) {
 			return getClass().getSimpleName() + "@" + mHRMController.getNodeGUIName() + "(Peer=" + getPeerL2Address() + ")";
 		} else {
 			return getClass().getSimpleName() + "@" + mHRMController.getNodeGUIName();
 		}
 		 
 	}
 }
