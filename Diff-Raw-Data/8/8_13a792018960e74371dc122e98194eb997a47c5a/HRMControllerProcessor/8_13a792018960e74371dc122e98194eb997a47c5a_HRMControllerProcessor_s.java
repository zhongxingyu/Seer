 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComSession;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.CoordinatorProxy;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is implemented as thread. It is responsible for clustering tasks and packet processing of an HRMController.
  */
 public class HRMControllerProcessor extends Thread
 {
 	/**
 	 * Stores the HRMController reference
 	 */
 	private HRMController mHRMController = null;
 	
 	/**
 	 * Stores how many pending request do exist per hierarchy level
 	 */
 	private int[] mPendingClusterRequests = new int[HRMConfig.Hierarchy.HEIGHT]; 
 
 	/**
 	 * Stores pending requests for packet processing
 	 */
 	private LinkedList<ComChannel> mPendingPacketRequests = new LinkedList<ComChannel>();
 	
 	/**
 	 * Stores pending requests for hierarchy priority update processing 
 	 */
 	private LinkedList<HierarchyLevel> mPendingHierarchyUpdates = new LinkedList<HierarchyLevel>();
 	
 	/**
 	 * Stores the pending requests for connectivity update processing
 	 */
 	private LinkedList<NetworkInterface> mPendingConnectivityUpdates = new LinkedList<NetworkInterface>();
 	
 	/**
 	 * Stores the pending requests for update local coordinators
 	 */
 	private LinkedList<CoordinatorProxy> mPendingCoordinatorUpdates = new LinkedList<CoordinatorProxy>();
 	
 	/**
 	 * Stores the pending com. session closings
 	 */
 	private LinkedList<ComSession> mPendingComSessionClosings = new LinkedList<ComSession>();
 	
 	/**
 	 * Stores a log about "update" events
 	 */
 	private String mDescriptionClusterUpdates = new String();
 	
 	/**
 	 * Stores the number of update requests
 	 */
 	private int mNumberUpdateRequests = 0;
 	
 	/**
 	 * Allow to exit the processor thread
 	 */
 	private boolean mProcessorNeeded = true;
 	
 	/**
 	 * Stores if the main process loop is still running
 	 */
 	private boolean mProcessLoopIsRunning = false;
 	
 	private boolean DEBUG_NOTIFICATION = false;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController the HRMController instance
 	 */
 	HRMControllerProcessor(HRMController pHRMController)
 	{
 		mHRMController = pHRMController;
 		Logging.log(this, "##### Created clusterer thread for: " + mHRMController);
 	}
 	
 	/**
 	 * Returns a log about "update cluster" events
 	 */
 	public String getGUIDescriptionClusterUpdates()
 	{
 		return mDescriptionClusterUpdates;
 	}
 	
 	/**
 	 * Logs all pending requests
 	 */
 	public void logPendingEvents()
 	{
 		Logging.log(this, "### Logging pending events");
 		Logging.log(this, "Pending clustering requests:");
 		synchronized (mPendingClusterRequests) {
 			for(int i = 0; i < mPendingClusterRequests.length; i++){
 				Logging.log(this, "  ..lvl[" + i + "]: " + mPendingClusterRequests[i]);
 			}			
 		}
 		
 		Logging.log(this, "Pending packet requests:");
 		synchronized (mPendingPacketRequests) {
 			for(int i = 0; i < mPendingPacketRequests.size(); i++){
 				Logging.log(this, "  ..entry[" + i + "]: " + mPendingPacketRequests.get(i));
 			}			
 		}
 
 		Logging.log(this, "Pending hierarchy priority update requests:");
 		synchronized (mPendingHierarchyUpdates) {
 			for(int i = 0; i < mPendingHierarchyUpdates.size(); i++){
 				Logging.log(this, "  ..entry[" + i + "]: " + mPendingHierarchyUpdates.get(i));
 			}			
 		}
 
 		Logging.log(this, "Pending connectivity priority update requests:");
 		synchronized (mPendingConnectivityUpdates) {
 			for(int i = 0; i < mPendingConnectivityUpdates.size(); i++){
 				Logging.log(this, "  ..entry[" + i + "]: " + mPendingConnectivityUpdates.get(i));
 			}			
 		}
 
 		Logging.log(this, "Pending coordinator update requests:");
 		synchronized (mPendingCoordinatorUpdates) {
 			for(int i = 0; i < mPendingCoordinatorUpdates.size(); i++){
 				Logging.log(this, "  ..entry[" + i + "]: " + mPendingCoordinatorUpdates.get(i));
 			}			
 		}
 		
 		Logging.log(this, "Pending com. session closing requests:");
 		synchronized (mPendingComSessionClosings) {
 			for(int i = 0; i < mPendingComSessionClosings.size(); i++){
 				Logging.log(this, "  ..entry[" + i + "]: " + mPendingComSessionClosings.get(i));
 			}			
 		}
 		
 		Logging.log(this, "### logged pending events");
 	}
 	
 	/**
 	 * EVENT: "update cluster" for a given hierarchy level
 	 * 
 	 * @param pCause the causing control entity
 	 * @param pHierarchyLevel the hierarchy level where a clustering should be done
 	 */
 	private long mEventUpdateCluster = 0;
 	public synchronized void eventUpdateCluster(ControlEntity pCause, HierarchyLevel pHierarchyLevel)
 	{
 		mEventUpdateCluster++;
 		
 		if(mProcessorNeeded){
 			if(pHierarchyLevel.getValue() <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 				Logging.log(this, "\n\n################ CLUSTERING EVENT TRIGGERED at hierarchy level: " + pHierarchyLevel.getValue() + ", cause=" + pCause);
 				mPendingClusterRequests[pHierarchyLevel.getValue()]++;
 				
 				mDescriptionClusterUpdates += "\n [" + mNumberUpdateRequests + "]: (L" + pHierarchyLevel.getValue() + ") <== " + pCause; 
 				mNumberUpdateRequests++;
 				
 				// trigger wake-up
 				if(DEBUG_NOTIFICATION){
 					Logging.log(this, "Notify - [" + mEventUpdateCluster + "] - eventUpdateCluster(" + pCause + ", " + pHierarchyLevel + ")");
 				}
 				notify();
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "received packet"
 	 * 
 	 * @param pComChannel the comm. channel which received a new packet
 	 */
 	private long mEventReceivedPacket = 0;
 	public synchronized void eventReceivedPacket(ComChannel pComChannel)
 	{
 		mEventReceivedPacket++;
 		
 		synchronized (mPendingPacketRequests) {
 			mPendingPacketRequests.add(pComChannel);
 		}
 
 		// trigger wake-up
 		if(DEBUG_NOTIFICATION){
 			Logging.log(this, "Notify - [" + mEventReceivedPacket + "] - eventReceivedPacket(" + pComChannel + ")");
 		}
 		notify();
 	}
 
 	/**
 	 * EVENT: "new hierarchy priority"
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 */
 	private long mEventNewHierarchyPriority = 0;
 	public synchronized void eventNewHierarchyPriority(HierarchyLevel pHierarchyLevel)
 	{
 		mEventNewHierarchyPriority++;
 		
 		synchronized (mPendingHierarchyUpdates) {
 			mPendingHierarchyUpdates.add(pHierarchyLevel);
 		}
 
 		// trigger wake-up
 		if(DEBUG_NOTIFICATION){
 			Logging.log(this, "Notify - [" + mEventNewHierarchyPriority + "] - eventNewHierarchyPriority(" + pHierarchyLevel + ")");
 		}
 		notify();
 	}
 
 	/**
 	 * EVENT: "new connectivity priority"
 	 *  
 	 * @param pCausingNetworkInterface the causing network interface 
 	 */
 	private long mEventNewConnectivityPriority = 0;
 	public synchronized void eventNewConnectivity(NetworkInterface pCausingNetworkInterface)
 	{
 		mEventNewConnectivityPriority++;
 		
 		synchronized (mPendingConnectivityUpdates) {
 			mPendingConnectivityUpdates.add(pCausingNetworkInterface);
 		}
 
 		// trigger wake-up
 		if(DEBUG_NOTIFICATION){
 			Logging.log(this, "Notify - [" + mEventNewConnectivityPriority + "] - mEventNewConnectivityPriority(" + pCausingNetworkInterface + ")");
 		}
 		notify();
 	}
 
 	/**
 	 * EVENT: "lost remote coordinator"
 	 * 
 	 * @param pCausingCoordinatorProxy the lost coordinator proxy
 	 */
 	private long mEventUpdateCoordinators = 0;
 	public synchronized void eventUpdateCoordinatorsAboutLostRemoteCoordinator(CoordinatorProxy pCausingCoordinatorProxy)
 	{
 		mEventUpdateCoordinators++;
 		
 		synchronized (mPendingCoordinatorUpdates) {
 			mPendingCoordinatorUpdates.add(pCausingCoordinatorProxy);
 		}
 
 		// trigger wake-up
 		if(DEBUG_NOTIFICATION){
 			Logging.log(this, "Notify - [" + mEventUpdateCoordinators + "] - mEventUpdateCoordinators(" + pCausingCoordinatorProxy + ")");
 		}
 		notify();
 	}
 	
 	/**
 	 * EVENT: "close the com. session"
 	 * 
 	 * @param pComSession the com. session
 	 */
 	private long mEventClosedSessions = 0;
 	public synchronized void eventCloseSession(ComSession pComSession)
 	{
 		mEventClosedSessions++;
 		
 	    mHRMController.unregisterSession(pComSession);
 
 		synchronized (mPendingComSessionClosings) {
 			mPendingComSessionClosings.add(pComSession);
 		}
 
 		// trigger wake-up
 		if(DEBUG_NOTIFICATION){
 			Logging.log(this, "Notify - [" + mEventClosedSessions + "] - mEventNewCloseSession(" + pComSession + ")");
 		}
 		notify();
 	}
 	
 	/**
 	 * Returns the next "cluster event" (uses passive waiting)
 	 * 
 	 * @return the next cluster event (a hierarchy level)
 	 */
 	private synchronized int getNextClusterEvent()
 	{
 		for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			if(mPendingClusterRequests[i] > 0){
 				mPendingClusterRequests[i]--;
 				return i;
 			}
 		}
 		
 		return -1;
 	}
 
 	/**
 	 * Returns the next "coordinator update event" (uses passive waiting)
 	 * 
 	 * @return the next coordinator update event
 	 */
 	private synchronized CoordinatorProxy getNextCoordinatorUpdateEvent()
 	{
 		CoordinatorProxy tResult = null;
 		
 		if(mPendingCoordinatorUpdates.size() > 0){
 			tResult = mPendingCoordinatorUpdates.removeFirst();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the next comm. channel which has pending packet data
 	 * 
 	 * @return the next comm. channel
 	 */
 	private synchronized ComChannel getNextComChannel()
 	{
 		ComChannel tResult = null;
 		
 		synchronized (mPendingPacketRequests) {
 			if(mPendingPacketRequests.size() > 0){
 				tResult = mPendingPacketRequests.removeFirst();
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the next hierarchy level which has pending priority updates
 	 * 
 	 * @return the next hierarchy level
 	 */
 	private synchronized HierarchyLevel getNextHierarchyLevelForPriorityUpdate()
 	{
 		HierarchyLevel tResult = null;
 		
 		synchronized (mPendingHierarchyUpdates) {
 			if(mPendingHierarchyUpdates.size() > 0){
 				tResult = mPendingHierarchyUpdates.removeFirst();
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the next connectivity event
 	 * 
 	 * @return the next connectivity event
 	 */
 	private synchronized NetworkInterface getNextConnectivityPriorityUpdate()
 	{
 		NetworkInterface tResult = null;
 		
 		synchronized (mPendingConnectivityUpdates) {
 			if(mPendingConnectivityUpdates.size() > 0){
 				tResult = mPendingConnectivityUpdates.removeFirst();
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the next connectivity event
 	 * 
 	 * @return the next connectivity event
 	 */
 	private synchronized ComSession getNextComSessionClosing()
 	{
 		ComSession tResult = null;
 		
 		synchronized (mPendingComSessionClosings) {
 			if(mPendingComSessionClosings.size() > 0){
 				tResult = mPendingComSessionClosings.removeFirst();
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Implements the actual clustering
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for clustering
 	 */
 	private void cluster(int pHierarchyLevel)
 	{
 		if(pHierarchyLevel <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 			if((pHierarchyLevel >= 0) && (pHierarchyLevel < HRMConfig.Hierarchy.HEIGHT)){
 				// search for an existing cluster at this hierarchy level
 				Cluster tTargetCluster = mHRMController.getCluster(pHierarchyLevel);
 				
 				/**
 				 * Create a new superior cluster if none does already exist
 				 */
 				if(tTargetCluster == null){
 					// does a local inferior coordinator exist?
 					if(!mHRMController.getAllCoordinators(pHierarchyLevel - 1).isEmpty()){
 						tTargetCluster = Cluster.create(mHRMController, new HierarchyLevel(this, pHierarchyLevel), Cluster.createClusterID());
 					}else{
						Logging.log(this, "No local inferior coordinator found, skipping clustering request at hierarchy level: " + pHierarchyLevel);						
 					}
 				}
 				
 				/**
 				 * Distribute membership requests
 				 */
 				if(tTargetCluster != null){
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "\n\n################ CLUSTERING STARTED at hierarchy level: " + pHierarchyLevel);
 					}
 					tTargetCluster.updateClusterMembers();
 				}
 				
 			}else{
 				Logging.err(this, "cluster() cannot start for a hierarchy level  of: " + pHierarchyLevel);
 			}
 		}else{
 			Logging.warn(this, "cluster() canceled clustering because height limitation is reached at level: " + pHierarchyLevel);
 		}
 	}
 	
 	private synchronized void waitForNextEvent()
 	{
 		// suspend until next trigger
 		try {
 			wait();
 			//Logging.log(this, "WakeUp");
 		} catch (InterruptedException tExc) {
 			Logging.warn(this, "waitForNextEvent() got an interrupt", tExc);
 		}
 	}
 
 	/**
 	 * main function
 	 */
 	public void run()
 	{
 		Thread.currentThread().setName("Sim" + Simulation.sStartedSimulations + "@Processor@" + mHRMController);
 
 		mProcessLoopIsRunning = true;
 		
 		while(mProcessorNeeded){
 			boolean tFoundEvent = false;
 			
 			/************************
 			 * Session closing
 			 ***********************/
 			ComSession tComSession = getNextComSessionClosing();
 			while(tComSession != null){
 				tFoundEvent = true;
 
 				double tBefore = HRMController.getRealTime();
 
 				Logging.log(this, "\n\n################ CLOSING COM. SESSION: " + tComSession);
 				tComSession.eventSessionInvalidated();
 				
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				if(tSpentTime > 100){
 					Logging.log(this, "Processing a com. session closing request for " + tComSession + " took " + tSpentTime + " ms");
 				}
 				
 				// get the next request
 				tComSession = getNextComSessionClosing();
 			}
 
 			/************************
 			 * Packet processing
 			 ***********************/
 			ComChannel tNextCommChannel = getNextComChannel();
 			while(tNextCommChannel != null){
 				tFoundEvent = true;
 
 				double tBefore = HRMController.getRealTime();
 				
 				// process the next comm. channel data
 				tNextCommChannel.processOnePacket();
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				if(tSpentTime > 250){
 					Logging.log(this, "Processing a packet took " + tSpentTime + " ms for " + tNextCommChannel);
 				}
 				
 				// get the next waiting comm. channel
 				tNextCommChannel = getNextComChannel();
 			}	
 
 			/************************
 			 * Hierarchy priority processing
 			 ***********************/
 			HierarchyLevel tNextHierarchyLevel = getNextHierarchyLevelForPriorityUpdate();
 			while(tNextHierarchyLevel != null){
 				tFoundEvent = true;
 				
 				double tBefore = HRMController.getRealTime();
 
 				// process the next hierarchy priority update
 				mHRMController.distributeHierarchyNodePriorityUpdate(tNextHierarchyLevel);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				if(tSpentTime > 100){
 					Logging.log(this, "Processing an hierarchy priority update for hier. level " + tNextHierarchyLevel + " took " + tSpentTime + " ms");
 				}
 
 				// get the next hierarchy priority update
 				tNextHierarchyLevel = getNextHierarchyLevelForPriorityUpdate();
 			}	
 
 			/************************
 			 * Connectivity priority processing
 			 ***********************/
 			NetworkInterface tNextNetworkInterfaceWithConnectivity = getNextConnectivityPriorityUpdate();
 			while(tNextNetworkInterfaceWithConnectivity != null){
 				tFoundEvent = true;
 				
 				double tBefore = HRMController.getRealTime();
 
 				// process the next hierarchy priority update
 				mHRMController.distributeConnectivityNodePriorityUpdate(tNextNetworkInterfaceWithConnectivity);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				if(tSpentTime > 100){
 					Logging.log(this, "Processing a connectivity priority update took " + tSpentTime + " ms");
 				}
 
 				// get the next connectivity priority update
 				tNextNetworkInterfaceWithConnectivity = getNextConnectivityPriorityUpdate();
 			}	
 			
 			/************************
 			 * Coordinator update
 			 ***********************/
 			CoordinatorProxy tNextLostCoordinatorProxy = getNextCoordinatorUpdateEvent();
 			while(tNextLostCoordinatorProxy != null){
 				tFoundEvent = true;
 
 				double tBefore = HRMController.getRealTime();
 				
 				// process the next CoordinatorProxy lost request
 				//Logging.warn(this, "\n\n################ Updating coordinators because of: " + tNextLostCoordinatorProxy);
 				mHRMController.detectAndInformInferiorCoordinatorsAboutLostCoordinatorProxy(tNextLostCoordinatorProxy);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				if(tSpentTime > 250){
 					Logging.log(this, "Processing a Coordinator update took " + tSpentTime + " ms for " + tNextLostCoordinatorProxy);
 				}
 				
 				// get the next waiting comm. channel
 				tNextLostCoordinatorProxy = getNextCoordinatorUpdateEvent();
 			}	
 			
 			/***********************
 			 * Clustering
 			 ***********************/
 			int tNextClusterEvent = getNextClusterEvent();
 			if(tNextClusterEvent >= 0){
 				tFoundEvent = true;
 
 				double tBefore = HRMController.getRealTime();
 
 				cluster(tNextClusterEvent);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				if(tSpentTime > 100){
 					Logging.log(this, "Processing a clustering request for hier. level " + tNextClusterEvent + " took " + tSpentTime + " ms");
 				}
 			}
 			
 			/**
 			 * Wait for next event
 			 */
 			if(!tFoundEvent){
 				waitForNextEvent();
 			}
 		}
 		
 		mProcessLoopIsRunning = false;
 	}
 
 	/**
 	 * Returns if this processor is valid
 	 * 
 	 * @return true or false
 	 */
 	public boolean isValid()
 	{
 		return (isRunning() && mProcessorNeeded);
 	}
 	
 	/**
 	 * Terminates this processor
 	 */
 	public synchronized void exit()
 	{
 		Logging.log(this, "Exiting this processor");
 		if(isValid()){
 			mProcessorNeeded = false;
 			notify();
 		}else{
 			Logging.log(this, "  ..was already killed");
 		}
 	}
 	
 	/**
 	 * Returns if this processor is still running
 	 * 
 	 * @return true or false
 	 */
 	public boolean isRunning()
 	{
 		return mProcessLoopIsRunning;
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mHRMController;
 	}
 }
