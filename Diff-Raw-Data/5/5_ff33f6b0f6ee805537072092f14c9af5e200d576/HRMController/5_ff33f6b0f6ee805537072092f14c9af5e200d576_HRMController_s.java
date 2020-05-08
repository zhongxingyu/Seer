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
 
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observer;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.util.ServerCallback;
 import de.tuilmenau.ics.fog.application.util.Service;
 import de.tuilmenau.ics.fog.eclipse.GraphViewer;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.facade.events.ConnectedEvent;
 import de.tuilmenau.ics.fog.facade.events.ErrorEvent;
 import de.tuilmenau.ics.fog.facade.events.Event;
 import de.tuilmenau.ics.fog.facade.properties.CommunicationTypeProperty;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.RequestClusterParticipationProperty.ClusterMemberDescription;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Decoration;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.eclipse.NodeDecorator;
 import de.tuilmenau.ics.fog.util.BlockingEventHandling;
 import de.tuilmenau.ics.fog.util.SimpleName;
 
 /**
  * This is the main HRM controller. It provides functions that are necessary to build up the hierarchical structure - every node contains such an object
  */
 public class HRMController extends Application implements ServerCallback, IEvent
 {
 	/**
 	 * Stores the node specific graph decorator for HRM coordinators and HRMIDs
 	 */
 	private NodeDecorator mDecoratorForCoordinatorsAndHRMIDs = null;
 
 	/**
 	 * Stores the node specific graph decorator for HRM coordinators and clusters
 	 */
 	private NodeDecorator mDecoratorForCoordinatorsAndClusters = null;
 	
 	/**
 	 * Stores the GUI observable, which is used to notify possible GUIs about changes within this HRMController instance.
 	 */
 	private HRMControllerObservable mGUIInformer = null;
 	
 	/**
 	 * The name under which the HRMController application is registered on the local node.
 	 */
 	private SimpleName mApplicationName = null;
 	
 	/**
 	 * Reference to physical node.
 	 */
 	private Node mNode;
 	
 	/**
 	 * Stores a reference to the local autonomous system instance.
 	 */
 	private AutonomousSystem mAS = null;
 	
 	/**
 	 * Stores the registered HRMIDs.
 	 * This is used within the GUI and during "share phase".
 	 */
 	private LinkedList<HRMID> mRegisteredOwnHRMIDs = new LinkedList<HRMID>();
 
 	/**
 	 * Stores a database about all registered coordinators.
 	 * For example, this list is used for the GUI.
 	 */
 	private LinkedList<Coordinator> mLocalCoordinators = new LinkedList<Coordinator>();
 
 	/**
 	 * Stores a database about all registered clusters.
 	 * For example, this list is used for the GUI.
 	 */
 	private LinkedList<Cluster> mLocalClusters = new LinkedList<Cluster>();
 
 	/**
 	 * Stores a database about all registered outgoing comm. sessions.
 	 */
 	private LinkedList<ComSession> mLocalOutgoingSessions = new LinkedList<ComSession>();
 	
 	/**
 	 * Stores a database about all registered incoming comm. sessions.
 	 */
 	private LinkedList<ComSession> mLocalIncomingSessions = new LinkedList<ComSession>();
 
 	/**
 	 * Stores a reference to the local instance of the hierarchical routing service.
 	 */
 	private HRMRoutingService mHierarchicalRoutingService = null;
 	
 	/**
 	 * Stores if the application was already started.
 	 */
 	private boolean mApplicationStarted = false;
 	
 	/**
 	 * Stores a database including all HRMControllers of this physical simulation machine
 	 */
 	private static LinkedList<HRMController> mRegisteredHRMControllers = new LinkedList<HRMController>();
 	
 	/**
 	 * Stores an abstract routing graph (ARG), which provides an abstract overview about logical links between clusters/coordinator.
 	 */
 	private AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> mAbstractRoutingGraph = new AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink>();
 	
 	/**
 	 * Count the outgoing connections
 	 */
 	private int mCounterOutgoingConnections = 0;
 	
 	private HashMap<Integer, ICluster> mLevelToCluster = new HashMap<Integer, ICluster>();
 	private HashMap<ICluster, Cluster> mIntermediateMapping = new HashMap<ICluster, Cluster>();
 	
 	/**
 	 * @param pAS the autonomous system at which this HRMController is instantiated
 	 * @param pNode the node on which this controller was started
 	 * @param pHRS is the hierarchical routing service that should be used
 	 */
 	public HRMController(AutonomousSystem pAS, Node pNode, HRMRoutingService pHierarchicalRoutingService)
 	{
 		// initialize the application context
 		super(pNode, null, pNode.getIdentity());
 
 		// define the local name "routing://"
 		mApplicationName = new SimpleName(ROUTING_NAMESPACE, null);
 
 		// the observable, e.g., it is used to delegate update notifications to the GUI
 		mGUIInformer = new HRMControllerObservable(this);
 		
 		// reference to the physical node
 		mNode = pNode;
 		
 		// reference to the AutonomousSystem object 
 		mAS = pAS;
 		
 		/**
 		 * Create the node specific decorator for HRM coordinators and HRMIDs
 		 */
 		mDecoratorForCoordinatorsAndHRMIDs = new NodeDecorator();
 		
 		/**
 		 * Create the node specific decorator for HRM coordinators and clusters
 		 */
 		mDecoratorForCoordinatorsAndClusters = new NodeDecorator();
 		
 		/**
 		 * Set the node decorations
 		 */
 		Decoration tDecoration = null;
 		// create own decoration for HRM coordinators & HRMIDs
 		tDecoration = Decoration.getInstance(DECORATION_NAME_COORDINATORS_AND_HRMIDS);
 		tDecoration.setDecorator(mNode,  mDecoratorForCoordinatorsAndHRMIDs);
 		// create own decoration for HRM coordinators and clusters
 		tDecoration = Decoration.getInstance(DECORATION_NAME_COORDINATORS_AND_CLUSTERS);
 		tDecoration.setDecorator(mNode,  mDecoratorForCoordinatorsAndClusters);
 		// overwrite default decoration
 		tDecoration = Decoration.getInstance(GraphViewer.DEFAULT_DECORATION);
 		tDecoration.setDecorator(mNode,  mDecoratorForCoordinatorsAndClusters);
 		
 		/**
 		 * Create communication service
 		 */
 		// bind the HRMController application to a local socket
 		Binding tServerSocket=null;
 		// enable simple datagram based communication
 		Description tServiceReq = getDescription();
 		tServiceReq.set(CommunicationTypeProperty.DATAGRAM);
 		tServerSocket = getLayer().bind(null, mApplicationName, tServiceReq, getIdentity());
 		if (tServerSocket != null){
 			// create and start the socket service
 			Service tService = new Service(false, this);
 			tService.start(tServerSocket);
 		}else{
 			Logging.err(this, "Unable to start the HRMController service");
 		}
 		
 		// store the reference to the local instance of hierarchical routing service
 		mHierarchicalRoutingService = pHierarchicalRoutingService;
 		
 		// set the Bully priority 
 		BullyPriority.configureNode(pNode);
 
 		
 		// fire the first "report/share phase" trigger
 		reportAndShare();
 		
 		Logging.log(this, "CREATED");
 		
 		// start the application
 		start();
 	}
 
 	/**
 	 * Returns the local instance of the hierarchical routing service
 	 * 
 	 * @return hierarchical routing service of this entity
 	 */
 	public HRMRoutingService getHRS()
 	{
 		return mHierarchicalRoutingService;
 	}
 	
 	/**
 	 * Returns the local physical node object.
 	 * 
 	 * @return the physical node running this coordinator
 	 */
 	public Node getNode()
 	{
 		return mNode;
 	}
 	
 	/**
 	 * Return the actual GUI name description of the physical node;
      * However, this function should only be used for debug outputs, e.g., GUI outputs.
      * 
 	 * @return the GUI name
 	 */
 	@SuppressWarnings("deprecation")
 	public String getNodeGUIName()
 	{
 		return mNode.getName();
 	}	
 
 	/**
 	 * Notifies the GUI about essential updates within the HRM system
 	 */
 	private void notifyGUI(Object pArgument)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Got notification with argument " + pArgument);
 		}
 		
 		mGUIInformer.notifyObservers(pArgument);
 	}
 
 	/**
 	 * Registers a GUI for being notified about HRMController internal changes. 
 	 */
 	public void registerGUI(Observer pGUI)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Registering GUI " + pGUI);
 		}
 		mGUIInformer.addObserver(pGUI);
 	}
 	
 	/**
 	 * Unregisters a GUI for being notified about HRMController internal changes. 
 	 */
 	public void unregisterGUI(Observer pGUI)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Unregistering GUI " + pGUI);
 		}
 		mGUIInformer.deleteObserver(pGUI);
 	}
 
 	/**
 	 * Registers a coordinator at the local database.
 	 * 
 	 * @param pCoordinator the coordinator for a defined cluster
 	 */
 	public void registerCoordinator(Coordinator pCoordinator)
 	{
 		int tLevel = pCoordinator.getHierarchyLevel().getValue() - 1; //TODO: die Hierarchieebenen im Koordinator richtig verwalten 
 		
 		Logging.log(this, "Registering coordinator " + pCoordinator + " at level " + tLevel);
 
 		// register a route to the coordinator as addressable target
 		getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(pCoordinator.getHRMID()));
 		
 		synchronized (mLocalCoordinators) {
 			// register as known coordinator
 			mLocalCoordinators.add(pCoordinator);
 		}
 		
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// register the coordinator in the local ARG
 		if (HRMConfig.DebugOutput.GUI_SHOW_COORDINATORS_IN_ARG)
 			registerNodeARG(pCoordinator);
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinator);
 	}
 	
 	/**
 	 * Unregisters a coordinator from the internal database.
 	 * 
 	 * @param pCoordinator the coordinator which should be unregistered
 	 */
 	public void unregisterCoordinator(Coordinator pCoordinator)
 	{
 		int tLevel = pCoordinator.getHierarchyLevel().getValue() - 1; //TODO: die Hierarchieebenen im Koordinator richtig verwalten 
 
 		Logging.log(this, "Unregistering coordinator " + pCoordinator + " at level " + tLevel);
 
 		synchronized (mLocalCoordinators) {
 			// unregister from list of known coordinators
 			mLocalCoordinators.remove(pCoordinator);
 		}
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// register at the ARG
 		if (HRMConfig.DebugOutput.GUI_SHOW_COORDINATORS_IN_ARG)
 			unregisterNodeARG(pCoordinator);
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinator);
 	}
 	
 	/**
 	 * Updates the registered HRMID for a defined coordinator.
 	 * 
 	 * @param pCluster the cluster whose HRMID is updated
 	 */
 	public void updateCoordinatorAddress(Coordinator pCoordinator)
 	{
 		HRMID tHRMID = pCoordinator.getHRMID();
 		
 		Logging.log(this, "Updating address to " + tHRMID.toString() + " for coordinator " + pCoordinator);
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			if ((!mRegisteredOwnHRMIDs.contains(tHRMID)) || (!HRMConfig.DebugOutput.GUI_AVOID_HRMID_DUPLICATES)){
 				
 				if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 					Logging.log(this, "Updating the HRMID to " + tHRMID.toString() + " for " + pCoordinator);
 				}
 	
 				/**
 				 * Update the local address DB with the given HRMID
 				 */
 				// register the new HRMID
 				mRegisteredOwnHRMIDs.add(tHRMID);
 	
 				/**
 				 * Register a local loopback route for the new address 
 				 */
 				// register a route to the cluster member as addressable target
 				getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(tHRMID));
 	
 				/**
 				 * Update the GUI
 				 */
 				// updates the GUI decoration for this node
 				updateGUINodeDecoration();
 				// it's time to update the GUI
 				notifyGUI(pCoordinator);
 			}else{
 				Logging. warn(this, "Skipping HRMID duplicate, additional registration is triggered by " + pCoordinator);
 			}
 		}			
 	}
 
 	/**
 	 * Revokes a coordinator address
 	 * 
 	 * @param pCoordinator the coordinator for which the address is revoked
 	 * @param pHRMID the revoked address
 	 */
 	public void revokeCoordinatorAddress(Coordinator pCoordinator, HRMID pHRMID)
 	{
 		Logging.log(this, "Revoking address to " + pHRMID.toString() + " for coordinator " + pCoordinator);
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 				Logging.log(this, "Revoking the HRMID to " + pHRMID.toString() + " for " + pCoordinator);
 			}
 
 			/**
 			 * Update the local address DB with the given HRMID
 			 */
 			if (mRegisteredOwnHRMIDs.contains(pHRMID)){
 				mRegisteredOwnHRMIDs.remove(pHRMID);
 			}else{
 				Logging.err(this, "Cannot revoke unknown HRMID " + pHRMID);
 			}
 
 			/**
 			 * Register a local loopback route for the new address 
 			 */
 			// register a route to the cluster member as addressable target
 			getHRS().delHRMRoute(RoutingEntry.createLocalhostEntry(pHRMID));
 
 			/**
 			 * Update the GUI
 			 */
 			// updates the GUI decoration for this node
 			updateGUINodeDecoration();
 			// it's time to update the GUI
 			notifyGUI(pCoordinator);
 		}			
 	}
 
 	/**
 	 * Updates the decoration of the node (image and label text)
 	 */
 	private void updateGUINodeDecoration()
 	{
 		/**
 		 * Set the decoration texts
 		 */
 		String tNodeText = "";
 		synchronized (mRegisteredOwnHRMIDs) {
 			for (HRMID tHRMID: mRegisteredOwnHRMIDs){
 				if ((!tHRMID.isRelativeAddress()) || (HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES)){
 					tNodeText += ", " + tHRMID.toString();
 				}
 			}			
 		}
 		mDecoratorForCoordinatorsAndHRMIDs.setText(tNodeText);
 		
 		tNodeText = "";
 		LinkedList<Cluster> tAllClusters = getAllClusters();
 		for (Cluster tCluster : tAllClusters){
 			if (tNodeText != ""){
 				tNodeText += ", ";
 			}
 			tNodeText += Long.toString(tCluster.getGUIClusterID());
 		}
 		mDecoratorForCoordinatorsAndClusters.setText("- clusters: " + tNodeText);
 		
 		/**
 		 * Set the decoration images
 		 */
 		LinkedList<Coordinator> tAllCoordinators = getAllCoordinators();
 		int tHighestCoordinatorLevel = -1;
 		for (Coordinator tCoordinator : tAllCoordinators){
 			int tCoordLevel = tCoordinator.getHierarchyLevel().getValue() - 1; 
 			if (tCoordLevel > tHighestCoordinatorLevel){
 				tHighestCoordinatorLevel = tCoordLevel;
 			}
 		}
 		mDecoratorForCoordinatorsAndHRMIDs.setImage(tHighestCoordinatorLevel);
 		mDecoratorForCoordinatorsAndClusters.setImage(tHighestCoordinatorLevel);
 	}
 
 	/**
 	 * Returns a list of all known local coordinators.
 	 * 
 	 * @return the list of known local coordinators
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<Coordinator> getAllCoordinators()
 	{
 		LinkedList<Coordinator> tResult;
 		
 		synchronized (mLocalCoordinators) {
 			tResult = (LinkedList<Coordinator>) mLocalCoordinators.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns all known coordinators for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for which all coordinators have to be determined
 	 * 
 	 * @return the list of coordinators on the defined hierarchy level
 	 */
 	public LinkedList<Coordinator> getAllCoordinators(HierarchyLevel pHierarchyLevel)
 	{
 		LinkedList<Coordinator> tResult = new LinkedList<Coordinator>();
 		
 		// get a list of all known coordinators
 		LinkedList<Coordinator> tAllCoordiantors = getAllCoordinators();
 		
 		// iterate over all known coordinators
 		for (Coordinator tCoordinator : tAllCoordiantors){
 			// have we found a matching coordinator?
 			if (tCoordinator.getHierarchyLevel().equals(pHierarchyLevel)){
 				// add this coordinator to the result
 				tResult.add(tCoordinator);
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Registers a cluster at the local database.
 	 * 
 	 * @param pCluster the cluster which should be registered
 	 */
 	public void registerCluster(Cluster pCluster)
 	{
 		int tLevel = pCluster.getHierarchyLevel().getValue();
 
 		Logging.log(this, "Registering cluster " + pCluster + " at level " + tLevel);
 
 		synchronized (mLocalClusters) {
 			// register as known cluster
 			mLocalClusters.add(pCluster);
 		}
 		
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 
 		// register the cluster in the local ARG
 		registerNodeARG(pCluster);
 
 		// it's time to update the GUI
 		notifyGUI(pCluster);
 	}
 	
 	/**
 	 * Unregisters a cluster from the local database.
 	 * 
 	 * @param pCluster the cluster which should be unregistered.
 	 */
 	public void unregisterCluster(Cluster pCluster)
 	{
 		Logging.log(this, "Unregistering cluster " + pCluster);
 
 		synchronized (mLocalClusters) {
 			// unregister from list of known clusters
 			mLocalClusters.remove(pCluster);
 		}
 		
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 
 		// register at the ARG
 		unregisterNodeARG(pCluster);
 
 		// it's time to update the GUI
 		notifyGUI(pCluster);
 	}
 	
 	/**
 	 * Updates the registered HRMID for a defined cluster.
 	 * 
 	 * @param pCluster the cluster whose HRMID is updated
 	 */
 	public void updateClusterAddress(Cluster pCluster)
 	{
 		HRMID tHRMID = pCluster.getHRMID();
 
 		Logging.log(this, "Updating address to " + tHRMID.toString() + " for cluster " + pCluster);
 
 		// process this only if we are at base hierarchy level, otherwise we will receive the same update from 
 		// the corresponding coordinator instance
 		if (pCluster.getHierarchyLevel().isBaseLevel()){
 			synchronized(mRegisteredOwnHRMIDs){
 				if ((!mRegisteredOwnHRMIDs.contains(tHRMID)) || (!HRMConfig.DebugOutput.GUI_AVOID_HRMID_DUPLICATES)){
 					
 					/**
 					 * Update the local address DB with the given HRMID
 					 */
 					if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 						Logging.log(this, "Updating the HRMID to " + pCluster.getHRMID().toString() + " for " + pCluster);
 					}
 					
 					// register the new HRMID
 					mRegisteredOwnHRMIDs.add(tHRMID);
 					
 					/**
 					 * Register a local loopback route for the new address 
 					 */
 					// register a route to the cluster member as addressable target
 					getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(tHRMID));
 	
 					/**
 					 * We are at base hierarchy level! Thus, the new HRMID is an address for this physical node and has to be
 					 * registered in the DNS as address for the name of this node. 
 					 */
 					// register the HRMID in the hierarchical DNS for the local router
 					HierarchicalNameMappingService<HRMID> tNMS = null;
 					try {
 						tNMS = (HierarchicalNameMappingService) HierarchicalNameMappingService.getGlobalNameMappingService(mAS.getSimulation());
 					} catch (RuntimeException tExc) {
 						HierarchicalNameMappingService.createGlobalNameMappingService(getNode().getAS().getSimulation());
 					}				
 					// get the local router's human readable name (= DNS name)
 					Name tLocalRouterName = getNodeName();				
 					// register HRMID for the given DNS name
 					tNMS.registerName(tLocalRouterName, tHRMID, NamingLevel.NAMES);				
 					// give some debug output about the current DNS state
 					String tString = new String();
 					for(NameMappingEntry<HRMID> tEntry : tNMS.getAddresses(tLocalRouterName)) {
 						if (!tString.isEmpty()){
 							tString += ", ";
 						}
 						tString += tEntry;
 					}
 					Logging.log(this, "HRM router " + tLocalRouterName + " is now known under: " + tString);
 					
 					/**
 					 * Update the GUI
 					 */
 					// it's time to update the GUI
 					notifyGUI(pCluster);
 				}else{
 					Logging.warn(this, "Skipping HRMID duplicate, additional registration is triggered by " + pCluster);
 				}
 			}
 		}else{
 			// we are at a higher hierarchy level and don't need the HRMID update because we got the same from the corresponding coordinator instance
 			Logging.warn(this, "Skipping HRMID registration " + tHRMID.toString() + " for " + pCluster);
 		}
 	}
 
 	/**
 	 * Revokes a cluster address
 	 * 
 	 * @param pCluster the cluster for which the address is revoked
 	 * @param pHRMID the revoked address
 	 */
 	public void revokeClusterAddress(Cluster pCluster, HRMID pHRMID)
 	{
 		Logging.log(this, "Revoking address " + pHRMID.toString() + " for cluster " + pCluster);
 
 		if (pCluster.getHierarchyLevel().isBaseLevel()){
 			synchronized(mRegisteredOwnHRMIDs){
 				/**
 				 * Update the local address DB with the given HRMID
 				 */
 				if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 					Logging.log(this, "Revoking the HRMID to " + pCluster.getHRMID().toString() + " for " + pCluster);
 				}
 				if (mRegisteredOwnHRMIDs.contains(pHRMID)){
 					mRegisteredOwnHRMIDs.remove(pHRMID);
 				}else{
 					Logging.err(this, "Cannot revoke unknown HRMID " + pHRMID);
 				}
 				
 				/**
 				 * Unregister the local loopback route for the address 
 				 */
 				// register a route to the cluster member as addressable target
 				getHRS().delHRMRoute(RoutingEntry.createLocalhostEntry(pHRMID));
 	
 				/**
 				 * We are at base hierarchy level! Thus, the new HRMID is an address for this physical node and has to be
 				 * registered in the DNS as address for the name of this node. 
 				 */
 //TODO				// register the HRMID in the hierarchical DNS for the local router
 	//			HierarchicalNameMappingService<HRMID> tNMS = null;
 	//			try {
 	//				tNMS = (HierarchicalNameMappingService) HierarchicalNameMappingService.getGlobalNameMappingService(mAS.getSimulation());
 	//			} catch (RuntimeException tExc) {
 	//				HierarchicalNameMappingService.createGlobalNameMappingService(getNode().getAS().getSimulation());
 	//			}				
 	//			// get the local router's human readable name (= DNS name)
 	//			Name tLocalRouterName = getNodeName();				
 	//			// register HRMID for the given DNS name
 	//			tNMS.registerName(tLocalRouterName, tHRMID, NamingLevel.NAMES);				
 	//			// give some debug output about the current DNS state
 	//			String tString = new String();
 	//			for(NameMappingEntry<HRMID> tEntry : tNMS.getAddresses(tLocalRouterName)) {
 	//				if (!tString.isEmpty()){
 	//					tString += ", ";
 	//				}
 	//				tString += tEntry;
 	//			}
 	//			Logging.log(this, "HRM router " + tLocalRouterName + " is now known under: " + tString);
 				
 				/**
 				 * Update the GUI
 				 */
 				// it's time to update the GUI
 				notifyGUI(pCluster);
 			}
 		}else{
 			// we are at a higher hierarchy level and don't need the HRMID revocation
 			Logging.warn(this, "Skipping HRMID revocation of " + pHRMID.toString() + " for " + pCluster);
 		}
 	}
 
 	/**
 	 * Returns a list of known clusters.
 	 * 
 	 * @return the list of known clusters
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<Cluster> getAllClusters()
 	{
 		LinkedList<Cluster> tResult = null;
 		
 		synchronized (mLocalClusters) {
 			tResult = (LinkedList<Cluster>) mLocalClusters.clone();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of known clusters for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the list of clusters
 	 */
 	public LinkedList<Cluster> getAllClusters(HierarchyLevel pHierarchyLevel)
 	{
 		LinkedList<Cluster> tResult = new LinkedList<Cluster>();
 		
 		// get a list of all known coordinators
 		LinkedList<Cluster> tAllClusters = getAllClusters();
 		
 		// iterate over all known coordinators
 		for (Cluster tCluster : tAllClusters){
 			// have we found a matching coordinator?
 			if (tCluster.getHierarchyLevel().equals(pHierarchyLevel)){
 				// add this coordinator to the result
 				tResult.add(tCluster);
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the locally known Cluster object, which was identified by its ClusterName
 	 *  
 	 * @param pClusterName the cluster name of the searched cluster
 	 * 
 	 * @return the desired cluster, null if the cluster isn't known
 	 */
 	private Cluster getClusterByName(ClusterName pClusterName)
 	{
 		Cluster tResult = null;
 		
 		for(Cluster tKnownCluster : getAllClusters()) {
 			if(tKnownCluster.equals(pClusterName)) {
 				tResult = tKnownCluster;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns a known cluster, which is identified by its ID.
 	 * 
 	 * @param pClusterID the cluster ID
 	 * 
 	 * @return the searched cluster object
 	 */
 	public Cluster getClusterByID(ClusterName pClusterID)
 	{
 		Cluster tResult = null;
 		
 		for(Cluster tKnownCluster : getAllClusters()) {
 			if (tKnownCluster.equals(pClusterID)) {
 				tResult = tKnownCluster;
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Registers an outgoing communication session
 	 * 
 	 * @param pComSession the new session
 	 */
 	public void registerOutgoingSession(ComSession pComSession)
 	{
 		Logging.log(this, "Registering outgoing communication session: " + pComSession);
 		
 		synchronized (mLocalOutgoingSessions) {
 			mLocalOutgoingSessions.add(pComSession);
 		}
 	}
 	
 	/**
 	 * Registers an incoming communication session
 	 * 
 	 * @param pComSession the new session
 	 */
 	public void registerIncomingSession(ComSession pComSession)
 	{
 		Logging.log(this, "Registering incoming communication session: " + pComSession);
 		
 		synchronized (mLocalIncomingSessions) {
 			mLocalIncomingSessions.add(pComSession);
 		}
 	}
 	
 	/**
 	 * Determines the outgoing communication session for a desired target cluster
 	 * 
 	 * @param pTargetCluster the target cluster
 	 * 
 	 * @return the found comm. session or null
 	 */
 	public ComSession getOutgoingComSession(ControlEntity pTargetCluster)
 	{
 		ComSession tResult = null;
 
 		Logging.log(this, "Searching for outgoing comm. session towards cluster: " + pTargetCluster + ", knowing these sessions and their channels:");
 		synchronized (mLocalOutgoingSessions) {
 			for (ComSession tComSession : mLocalOutgoingSessions){
 				Logging.log(this, "   ..ComSession: " + tComSession);
 				for(ComChannel tComChannel : tComSession.getAllComChannels()){
 					Logging.log(this, "     ..ComChannel: " + tComChannel);
 					Logging.log(this, "        ..RemoteCluster: " + tComChannel.getRemoteClusterName().toString());
 				}
 			}
 			
 		}
 
 		synchronized (mLocalOutgoingSessions) {
 			for (ComSession tComSession : mLocalOutgoingSessions){
 				ComChannel tComChannel = tComSession.getComChannelByRemoteClusterID(pTargetCluster.getClusterID());
 				if (tComChannel != null){
 					tResult = tComSession;
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Unregisters an outgoing communication session
 	 * 
 	 * @param pComSession the session
 	 */
 	public void unregisterOutgoingSession(ComSession pComSession)
 	{
 		Logging.log(this, "Unregistering outgoing communication session: " + pComSession);
 		
 		synchronized (mLocalOutgoingSessions) {
 			mLocalOutgoingSessions.remove(pComSession);
 		}
 	}
 	
 	/**
 	 * Unregisters an incoming communication session
 	 * 
 	 * @param pComSession the session
 	 */
 	public void unregisterIncomingSession(ComSession pComSession)
 	{
 		Logging.log(this, "Unregistering incoming communication session: " + pComSession);
 		
 		synchronized (mLocalIncomingSessions) {
 			mLocalIncomingSessions.remove(pComSession);
 		}
 	}
 
 	/**
 	 * Returns the list of registered own HRMIDs which can be used to address the physical node on which this instance is running.
 	 *  
 	 * @return the list of HRMIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getOwnHRMIDs()
 	{
 		LinkedList<HRMID> tResult = null;
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			tResult = (LinkedList<HRMID>) mRegisteredOwnHRMIDs.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Adds an entry to the routing table of the local HRS instance.
 	 * In opposite to addHRMRoute() from the HierarchicalRoutingService class, this function additionally updates the GUI.
 	 * If the L2 address of the next hop is defined, the HRS will update the HRMID-to-L2ADDRESS mapping.
 	 * 
 	 * @param pRoutingEntry the new routing entry
 	 */
 	public void addHRMRoute(RoutingEntry pRoutingEntry)
 	{
 		// inform the HRS about the new route
 		if(getHRS().addHRMRoute(pRoutingEntry)){
 			// it's time to update the GUI
 			notifyGUI(this);
 		}
 	}
 
 	/**
 	 * Adds a route to the local HRM routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pNeighborL2Address the L2Address of the direct neighbor
 	 * @param pRoute the route to the direct neighbor
 	 */
 	public void addRouteToDirectNeighbor(L2Address pNeighborL2Address, Route pRoute)
 	{
 		// inform the HRS about the new route
 		if(getHRS().addRouteToDirectNeighbor(pNeighborL2Address, pRoute)){
 			// it's time to update the GUI
 			notifyGUI(this);
 		}
 	}
 
 	/**
 	 * Connects to a service with the given name. Method blocks until the connection had been set up.
 	 */
 	private Connection connectBlock(Name pName, Description pDescription, Identity pIdentity) throws NetworkException
 	{
 		Connection conn = getLayer().connect(pName, pDescription, pIdentity);
 		BlockingEventHandling block = new BlockingEventHandling(conn, 1);
 		
 		// wait for the first event
 		Event event = block.waitForEvent();
 		
 		if(event instanceof ConnectedEvent) {
 			if(!conn.isConnected()) {
 				throw new NetworkException(this, "Connected event but connection is not connected.");
 			} else {
 				return conn;
 			}
 		}
 		else if(event instanceof ErrorEvent) {
 			Exception exc = ((ErrorEvent) event).getException();
 			
 			if(exc instanceof NetworkException) {
 				throw (NetworkException) exc;
 			} else {
 				throw new NetworkException(this, "Can not connect to " +pName +".", exc);
 			}
 		}
 		else {
 			throw new NetworkException(this, "Can not connect to " +pName +" due to " +event);
 		}
 	}
 	
 	/**
 	 * Reacts on a detected new physical neighbor. A new connection to this neighbor is created.
 	 * HINT: "pNeighborL2Address" doesn't correspond to the neighbor's central FN!
 	 * 
 	 * @param pNeighborL2Address the L2 address of the detected physical neighbor's first FN towards the common bus.
 	 */
 	public synchronized void eventDetectedPhysicalNeighborNode(L2Address pNeighborL2Address)
 	{
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) mNode.getLayer(FoGEntity.class);
 
 		// get the central FN of this node
 		L2Address tThisHostL2Address = getHRS().getL2AddressFor(tFoGLayer.getCentralFN());
 
 		Logging.info(this, "\n\n\n############## FOUND DIRECT NEIGHBOR NODE " + pNeighborL2Address + " FOR " + tThisHostL2Address);
 		
 		/**
 		 * Create cluster
 		 */
 	    Logging.log(this, "    ..creating new cluster");
 		Cluster tCreatedCluster = Cluster.createBaseCluster(this);
 
 		setSourceIntermediateCluster(tCreatedCluster, tCreatedCluster);
 		
 		/**
 		 * Create communication session
 		 */
 	    Logging.log(this, "    ..creating new communication session");
 	    ComSession tComSession = new ComSession(this, false, tCreatedCluster, tCreatedCluster.getHierarchyLevel());
 	    
 	    /**
 	     * Create communication channel
 	     */
 	    Logging.log(this, "    ..creating new communication channel");
 		ComChannel tComChannel = new ComChannel(this, ComChannel.Direction.OUT, tCreatedCluster, tComSession);
 		tComChannel.setRemoteClusterName(new ClusterName(this, tCreatedCluster.getHierarchyLevel(), tCreatedCluster.getCoordinatorID(), tCreatedCluster.getClusterID()));
 		
 		/**
 		 * Describe the new created cluster
 		 */
 	    Logging.log(this, "    ..creating cluster description");
 		final RequestClusterParticipationProperty tRequestClusterParticipationProperty = new RequestClusterParticipationProperty(HierarchyLevel.createBaseLevel(), tCreatedCluster.getClusterID(), tCreatedCluster.getHierarchyLevel(), 0);
 		/**
 		 * Describe the cluster member
 		 */
 	    Logging.log(this, "    ..creating cluster member description for created cluster " + tCreatedCluster);
 	    tRequestClusterParticipationProperty.addClusterMember(tCreatedCluster.getClusterID(), 0, null);
 
 		/**
 		 * Store the thread specific variables
 		 */
 		final L2Address tNeighborName = pNeighborL2Address;
 		final ComSession tFSession = tComSession;
 		final HRMController tHRMController = this;
 
 		/**
 		 * Create connection thread
 		 */
 		Thread tThread = new Thread() {
 			public String toString()
 			{
 				return tHRMController.toString();
 			}
 			
 			public void run()
 			{
 				/**
 				 * Create connection requirements
 				 */
 				Description tConnectionRequirements = createHRMControllerDestinationDescription();
 				tConnectionRequirements.set(tRequestClusterParticipationProperty);
 
 				/**
 				 * Connect to the neighbor node
 				 */
 				Connection tConnection = null;				
 			    Logging.log(this, "    ..CONNECTING to: " + tNeighborName + " with requirements: " + tConnectionRequirements);
 				try {
 					tConnection = connectBlock(tNeighborName, tConnectionRequirements, getNode().getIdentity());
 				} catch (NetworkException tExc) {
 					Logging.err(this, "Cannot connect to: " + tNeighborName, tExc);
 				}
 			    Logging.log(this, "    ..connectBlock() FINISHED");
 				if(tConnection != null) {
 
 					mCounterOutgoingConnections++;
 					
 					Logging.log(tHRMController, "     ..starting this OUTGOING CONNECTION as nr. " + mCounterOutgoingConnections);
 					tFSession.startConnection(tNeighborName, tConnection);					
 					
 					Logging.log(this, "Connection thread for " + tNeighborName + " finished");
 				}else{
 					Logging.log(this, "Connection thread for " + tNeighborName + " failed");
 				}
 			}
 		};
 		
 		/**
 		 * Start the connection thread
 		 */
 		tThread.start();
 	}
 
 	/**
 	 * Determines a reference to the current AutonomousSystem instance.
 	 * 
 	 * @return the desired reference
 	 */
 	public AutonomousSystem getAS()
 	{
 		return mAS;
 	}
 	
 	/**
 	 * Determines the current simulation time
 	 * 
 	 * @return the simulation time
 	 */
 	public double getSimulationTime()
 	{
 		return mAS.getTimeBase().now();
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.IEvent#fire()
 	 */
 	@Override
 	public void fire()
 	{
 		reportAndShare();
 	}
 
 	/**
 	 * Triggers the "report phase" / "share phase" of all known coordinators
 	 */
 	private void reportAndShare()
 	{	
 		if (HRMConfig.DebugOutput.GUI_SHOW_TIMING_ROUTE_DISTRIBUTION){
 			Logging.log(this, "REPORT AND SHARE TRIGGER received");
 		}
 
 		/**
 		 * report phase
 		 */
 		for (Coordinator tCoordinator : getAllCoordinators()) {
 			tCoordinator.reportPhase();
 		}
 		
 		/**
 		 * share phase
 		 */
 		for (Coordinator tCoordinator : getAllCoordinators()) {
 			tCoordinator.sharePhase();
 		}
 		
 		/**
 		 * register next trigger
 		 */
 		mAS.getTimeBase().scheduleIn(HRMConfig.Routing.GRANULARITY_SHARE_PHASE, this);
 	}
 	
 	/**
 	 * Calculate the time period between "share phases" 
 	 *  
 	 * @param pHierarchyLevel the hierarchy level 
 	 * @return the calculated time period
 	 */
 	public double getPeriodSharePhase(int pHierarchyLevelValue)
 	{
 		return (double) 2 * HRMConfig.Routing.GRANULARITY_SHARE_PHASE * pHierarchyLevelValue; //TODO: use an exponential time distribution here
 	}
 	
 	/**
 	 * Calculate the time period between "share phases" 
 	 *  
 	 * @param pHierarchyLevel the hierarchy level 
 	 * @return the calculated time period
 	 */
 	public double getPeriodReportPhase(HierarchyLevel pHierarchyLevel)
 	{
 		return (double) HRMConfig.Routing.GRANULARITY_SHARE_PHASE * (pHierarchyLevel.getValue() - 1); //TODO: use an exponential time distribution here
 	}
 	
 	/**
 	 * This method is derived from IServerCallback. It is called by the ServerFN in order to acquire the acknowledgment from the HRMController about the incoming connection
 	 * 
 	 * @param pAuths the authentications of the requesting sender
 	 * @param pRequirements the requirements for the incoming connection
 	 * @param pTargetName the registered name of the addressed target service
 	 * @return true of false
 	 */
 	@Override
 	public boolean openAck(LinkedList<Signature> pAuths, Description pRequirements, Name pTargetName)
 	{
 		//TODO: check if a neighbor wants to explore its neighbor -> select if we want to join its cluster or not
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Incoming request for acknowledging the connection:");
 			Logging.log(this, "    ..source: " + pAuths);
 			Logging.log(this, "    ..destination: " + pTargetName);
 			Logging.log(this, "    ..requirements: " + pRequirements);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Helper function to get the local machine's host name.
 	 * The output of this function is useful for distributed simulations if coordinators/clusters with the name might coexist on different machines.
 	 * 
 	 * @return the host name
 	 */
 	public static String getHostName()
 	{
 		String tResult = null;
 		
 		try{	
 			tResult = java.net.InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException tExc) {
 			Logging.err(null, "Unable to determine the local host name", tExc);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines the L2Address of the first FN towards a neighbor. This corresponds to the FN, located between the central FN and the bus to the neighbor node.
 	 * 
 	 * @param pNeighborName the name of the neighbor
 	 * @return the L2Address of the search FN
 	 */
 	public L2Address getL2AddressOfFirstFNTowardsNeighbor(Name pNeighborName)
 	{
 		L2Address tResult = null;
 
 		if (pNeighborName != null){
 			Route tRoute = null;
 			// get the name of the central FN
 			L2Address tCentralFNL2Address = getHRS().getCentralFNL2Address();
 			// get a route to the neighbor node (the destination of the desired connection)
 			try {
 				tRoute = getHRS().getRoute(pNeighborName, new Description(), getNode().getIdentity());
 			} catch (RoutingException tExc) {
 				Logging.err(this, "getL2AddressOfFirstFNTowardsNeighbor() is unable to find route to " + pNeighborName, tExc);
 			} catch (RequirementsException tExc) {
 				Logging.err(this, "getL2AddressOfFirstFNTowardsNeighbor() is unable to find route to " + pNeighborName + " with requirements no requirents, Huh!", tExc);
 			}
 			// have we found a route to the neighbor?
 			if(tRoute != null) {
 				// get the first route part, which corresponds to the link between the central FN and the searched first FN towards the neighbor 
 				RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 				// get the gate ID of the link
 				GateID tGateID= tPath.getFirst();						
 				// get all outgoing links from the central FN
 				Collection<RoutingServiceLink> tOutgoingLinksFromCentralFN = getHRS().getOutgoingLinks(tCentralFNL2Address);
 				
 				RoutingServiceLink tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor = null;
 	
 				// iterate over all outgoing links and search for the link from the central FN to the FN, which comes first when routing towards the neighbor
 				for(RoutingServiceLink tLink : tOutgoingLinksFromCentralFN) {
 					// compare the GateIDs
 					if(tLink.equals(tGateID)) {
 						// found!
 						tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor = tLink;
 					}
 				}
 				// determine the searched FN, which comes first when routing towards the neighbor
 				HRMName tFirstNodeBeforeBusToNeighbor = getHRS().getL2LinkDestination(tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor);
 				if (tFirstNodeBeforeBusToNeighbor instanceof L2Address){
 					// get the L2 address
 					tResult = (L2Address)tFirstNodeBeforeBusToNeighbor;
 				}else{
 					Logging.err(this,  "getL2AddressOfFirstFNTowardsNeighbor() found a first FN (" + tFirstNodeBeforeBusToNeighbor + ") towards the neighbor " + pNeighborName + " but it has the wrong class type");
 				}
 			}
 		}else{
 			Logging.warn(this, "getL2AddressOfFirstFNTowardsNeighbor() found an invalid neighbor name");
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * This function gets called if the HRMController appl. was started
 	 */
 	@Override
 	protected void started() 
 	{
 		mApplicationStarted = true;
 		
 		// register in the global HRMController database
 		synchronized (mRegisteredHRMControllers) {
 			mRegisteredHRMControllers.add(this);
 		}
 	}
 	
 	/**
 	 * This function gets called if the HRMController appl. should exit/terminate right now
 	 */
 	@Override
 	public void exit() 
 	{
 		Logging.warn(this, "Exit the HRMController application isn't supported");
 	}
 
 	/**
 	 * Return if the HRMController application is running
 	 * 
 	 * @return true if the HRMController application is running, otherwise false
 	 */
 	@Override
 	public boolean isRunning() 
 	{
 		return mApplicationStarted;
 	}
 
 	/**
 	 * Returns the list of known HRMController instances for this physical simulation machine
 	 *  
 	 * @return the list of HRMController references
 	 */
 	@SuppressWarnings("unchecked")
 	public static LinkedList<HRMController> getALLHRMControllers()
 	{
 		LinkedList<HRMController> tResult = null;
 		
 		synchronized (mRegisteredHRMControllers) {
 			tResult = (LinkedList<HRMController>) mRegisteredHRMControllers.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Creates a Description, which directs a connection to another HRMController instance
 
 	 * @return the new description
 	 */
 	public Description createHRMControllerDestinationDescription()
 	{
 		Description tResult = new Description();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Creating a HRMController destination description");
 		}
 
 		tResult.set(new DestinationApplicationProperty(HRMController.ROUTING_NAMESPACE, null, null));
 		
 		return tResult;
 	}
 
 	/**
 	 * Registers a cluster/coordinator to the locally stored abstract routing graph (ARG)
 	 *  
 	 * @param pNode the node (cluster/coordinator) which should be stored in the ARG
 	 */
 	public synchronized void registerNodeARG(ControlEntity pNode)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING NODE ADDRESS (ARG): " + pNode );
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			if(!mAbstractRoutingGraph.contains(pNode)) {
 				mAbstractRoutingGraph.add(pNode);
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..added to ARG");
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..node for ARG already known: " + pNode);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Unregisters a cluster/coordinator from the locally stored abstract routing graph (ARG)
 	 *  
 	 * @param pNode the node (cluster/coordinator) which should be removed from the ARG
 	 */
 	private void unregisterNodeARG(ControlEntity pNode)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UNREGISTERING NODE ADDRESS (ARG): " + pNode );
 		}
 		
 		synchronized (mAbstractRoutingGraph) {
 			if(mAbstractRoutingGraph.contains(pNode)) {
 				mAbstractRoutingGraph.remove(pNode);
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..removed from ARG");
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..node for ARG wasn't known: " + pNode);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Registers a logical link between clusters/coordinators to the locally stored abstract routing graph (ARG)
 	 * 
 	 * @param pFrom the starting point of the link
 	 * @param pTo the ending point of the link
 	 * @param pLink the link between the two nodes
 	 */
 	public void registerLinkARG(ControlEntity pFrom, ControlEntity pTo, AbstractRoutingGraphLink pLink)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK (ARG):  source=" + pFrom + " ## dest.=" + pTo + " ## link=" + pLink);
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			mAbstractRoutingGraph.link(pFrom, pTo, pLink);
 		}
 	}
 
 	/**
 	 * Determines a route in the locally stored abstract routing graph (ARG).
 	 * 
 	 * @param pSource the source of the desired route
 	 * @param pDestination the destination of the desired route
 	 * 
 	 * @return the determined route, null if no route could be found
 	 */
 	public List<AbstractRoutingGraphLink> getRouteARG(AbstractRoutingGraphNode pSource, AbstractRoutingGraphNode pDestination)
 	{
 		List<AbstractRoutingGraphLink> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET ROUTE (ARG) from \"" + pSource + "\" to \"" + pDestination +"\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getRoute(pSource, pDestination);
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "        ..result: " + tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the other end of a link and one known link end
 	 * 
 	 * @param pKnownEnd the known end of the link
 	 * @param pLink the link
 	 * 
 	 * @return the other end of the link
 	 */
 	public AbstractRoutingGraphNode getOtherEndOfLinkARG(AbstractRoutingGraphNode pKnownEnd, AbstractRoutingGraphLink pLink)
 	{
 		AbstractRoutingGraphNode tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET OTHER END (ARG) of link \"" + pKnownEnd + "\" connected at \"" + pKnownEnd +"\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getOtherEndOfLink(pKnownEnd, pLink);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "        ..result: " + tResult);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines all known neighbors of a cluster/coordinator, which are stored in the local abstract routing graph (ARG).
 	 * 
 	 * @param pRoot the root node in the ARG
 	 * 
 	 * @return a collection of found neighbor nodes
 	 */
 	public Collection<AbstractRoutingGraphNode> getNeighborsARG(AbstractRoutingGraphNode pRoot)
 	{
 		Collection<AbstractRoutingGraphNode> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET NEIGHBORS (ARG) from \"" + pRoot + "\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getNeighbors(pRoot);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult.size() + " entries:");				
 			int i = 0;
 			for (AbstractRoutingGraphNode tName : tResult){
 				Logging.log(this, "      ..[" + i + "]: " + tName);
 				i++;
 			}			
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines all vertices ordered by their distance from a given root vertex
 	 * 
 	 * @param pRootCluster the root cluster from where the vertices are determined
 	 * 
 	 * @return a list of found vertices
 	 */
 	public List<AbstractRoutingGraphNode> getNeighborClustersOrderedByRadiusInARG(Cluster pRootCluster)
 	{
 		List<AbstractRoutingGraphNode> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET VERTICES ORDERED BY RADIUS (ARG) from \"" + pRootCluster + "\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getVerticesInOrderRadius(pRootCluster);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult.size() + " entries:");				
 			int i = 0;
 			for (AbstractRoutingGraphNode tName : tResult){
 				Logging.log(this, "      ..[" + i + "]: " + tName);
 				i++;
 			}			
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Checks if two nodes in the locally stored abstract routing graph are linked.
 	 * 
 	 * @param pFirst first node
 	 * @param pSecond second node
 	 * 
 	 * @return true or false
 	 */
 	public boolean isLinkedARG(ClusterName pFirst, ClusterName pSecond)
 	{
 		boolean tResult = false;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "IS LINK (ARG) from \"" + pFirst + "\" to \"" + pSecond +"\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.isLinked(pFirst, pSecond);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult);				
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the ARG for the GraphViewer.
 	 * (only for GUI!)
 	 * 
 	 * @return the ARG
 	 */
 	public AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> getARGForGraphViewer()
 	{
 		AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> tResult = null;
 		
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph; //TODO: use a new clone() method here
 		}
 		
 		return tResult;
 	}
 
 	/** 
 	 * This method is derived from IServerCallback and is called for incoming connection requests by the HRMController application's ServerFN.
 	 * Such a incoming connection can either be triggered by an HRMController application or by a probe-routing request
 	 * 
 	 * @param pConnection the incoming connection
 	 */
 	@Override
 	public void newConnection(Connection pConnection)
 	{
 		Logging.log(this, "INCOMING CONNECTION " + pConnection.toString() + " with requirements: " + pConnection.getRequirements());
 
 		// get the connection requirements
 		Description tConnectionRequirements = pConnection.getRequirements();
 
 		/**
 		 * check if the new connection is a probe-routing connection
 		 */
 		ProbeRoutingProperty tPropProbeRouting = (ProbeRoutingProperty) tConnectionRequirements.get(ProbeRoutingProperty.class);
 
 		// do we have a probe-routing connection?
 		if (tPropProbeRouting == null){
 			RequestClusterParticipationProperty tPropClusterDescription = (RequestClusterParticipationProperty) tConnectionRequirements.get(RequestClusterParticipationProperty.class);
 			
 			/******************************************************
 			 * PARSE: cluster description from remote side
 			 ******************************************************/
 			if(tPropClusterDescription != null) {
 				Logging.log(this, "    ..found cluster description: " + tPropClusterDescription);
 
 				ComSession tComSession = null;
 				Cluster tTargetCluster = null;
 
 				/**
 				 * Search if the cluster from the ClusterDescriptionProperty is already locally known
 				 */
 				LinkedList<Cluster> tClusters = getAllClusters();
 				Logging.log(this, "    ..searching for described cluster among " + tClusters.size() + " known clusters:");
 				int i = 0;
 				for(Cluster tLocalCluster : tClusters)
 				{
 					Logging.log(this, "    ..[" + i + "]: " + tLocalCluster);
 					
 					ClusterName tJoinClusterName = new ClusterName(this, tPropClusterDescription.getHierarchyLevel(), tPropClusterDescription.getCoordinatorID(), tPropClusterDescription.getClusterID());
 					ClusterName tJoinClusterNameTok0 = new ClusterName(this, tPropClusterDescription.getHierarchyLevel(), 0, tPropClusterDescription.getClusterID());
 
 					// do we already know the described cluster?
 					if(tLocalCluster.equals(tJoinClusterNameTok0) || tPropClusterDescription.getCoordinatorID() != 0 && tLocalCluster.equals(tJoinClusterName))	{
 						Logging.log(this, "           ..found MATCH: " + tLocalCluster);
 						
 						tTargetCluster = tLocalCluster;
 					}
 					i++;
 				}
 
 				/**
 				 * Create a new cluster object if the described cluster doesn't have a local representation yet
 				 */
 				if (tTargetCluster == null){
 					Logging.log(this, "    ..creating new local cluster object for handling remote cluster with description: " + tPropClusterDescription); 
 					tTargetCluster = Cluster.create(this, tPropClusterDescription.getClusterID(), tPropClusterDescription.getHierarchyLevel());
 					setSourceIntermediateCluster(tTargetCluster, tTargetCluster); //TODO : ??
 				}
 				
 				Logging.log(this, "     ..CONTINUING for target cluster: " + tTargetCluster);
 						
 				/**
 				 * Create the communication session
 				 */
 				Logging.log(this, "     ..creating communication session");
 				tComSession = new ComSession(this, true, tTargetCluster, tPropClusterDescription.getHierarchyLevel());
 
 				/*****************************************************
 				 * PARSE: cluster member descriptions from remote side
 				 *****************************************************/
 				int tFoundDescribedMembers = 0;
 				for(ClusterMemberDescription tClusterMemberDescription : tPropClusterDescription.getClusterMemberDescriptions()) {
 					Logging.log(this, "       ..found cluster member description [" + tFoundDescribedMembers + "]: " + tClusterMemberDescription);
 	
 					/**
 					 * Create the communication channel for the described cluster member
 					 */
 					Logging.log(this, "     ..creating communication channel");
 					ComChannel tComChannel = new ComChannel(this, ComChannel.Direction.IN, tTargetCluster, tComSession);
 					tComChannel.setPeerPriority(tClusterMemberDescription.getPriority());
 
 					/**
 					 * Set the remote ClusterName of the communication channel
 					 */
 					ClusterName tRemoteClusterName = new ClusterName(this, tPropClusterDescription.getSenderHierarchyLevel(), tClusterMemberDescription.getCoordinatorID(), tClusterMemberDescription.getClusterID());
 					tComChannel.setRemoteClusterName(tRemoteClusterName);
 					
 					/**
 					 * Check if the described cluster member is locally connected or a remote (distant) one
 					 */
 					boolean tIsRemoteCluster = (getClusterByName(tRemoteClusterName) != null); 
 
 					/**
 					 * Detected a remote cluster?
 					 */ 
 					if(tIsRemoteCluster && tPropClusterDescription.getHierarchyLevel().isHigherLevel()) {
 						/**
 						 * Create a ClusterProxy object
 						 */
 						Logging.log(this, "     ..creating cluster proxy");
 						ClusterProxy tClusterProxy_ClusterMember = new ClusterProxy(this, tClusterMemberDescription.getClusterID(), new HierarchyLevel(this, tPropClusterDescription.getHierarchyLevel().getValue() - 1), tClusterMemberDescription.getSourceName(), tClusterMemberDescription.getSourceL2Address(), tClusterMemberDescription.getCoordinatorID());
 						tClusterProxy_ClusterMember.setPriority(tClusterMemberDescription.getPriority());
 						
 						/**
 						 * Store the coordinator name of the remote cluster in the local FoGName-to-L2Address mapping
 						 */
 						getHRS().mapFoGNameToL2Address(tClusterMemberDescription.getSourceL2Address(), tClusterMemberDescription.getSourceL2Address());
 						
 						//TODO: store in the ARG
 						for(Cluster tCluster : getAllClusters()) {
 							if(tCluster.getHierarchyLevel().equals(tClusterProxy_ClusterMember.getHierarchyLevel())) {
 								setSourceIntermediateCluster(tClusterProxy_ClusterMember, tCluster);
 							}
 						}
 						
 						/******************************************************
 						 * PARSE: neighbor descriptions per cluster member description from remote side
 						 ******************************************************/
 						if(tClusterMemberDescription.getNeighbors() != null && !tClusterMemberDescription.getNeighbors().isEmpty()) {
 							int tFoundDescribedNeighbors = 0;
 							for(DiscoveryEntry tNeighborDescription : tClusterMemberDescription.getNeighbors()) {
 								
 								Logging.log(this, "     ..found described neighbor [" + tFoundDescribedNeighbors + "]: " + tClusterMemberDescription.getNeighbors());
 
 								/**
 								 * Store routes from the delivered routing data
 								 */
 								if(tNeighborDescription.getRoutingVectors()!= null) {
 									for(RoutingServiceLinkVector tVector : tNeighborDescription.getRoutingVectors()){
 										Logging.log(this, "     ..found routing data: source=" + tVector.getSource() + ", destination=" + tVector.getDestination() + ", route=" + tVector.getPath()); 
 										getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 									}
 								}
 
 								/**
 								 * Create a ClusterName object for the neighbor entry
 								 */
 								ClusterName tNeighborDescriptionClusterName = new ClusterName(this, tNeighborDescription.getLevel(), tNeighborDescription.getToken(), tNeighborDescription.getClusterID());
 								
 								
 								/**
 								 * Search if the neighbor cluster is already locally known
 								 */
 								ControlEntity tLocalCluster_ClusterMemberNeighbor = getClusterByName(tNeighborDescriptionClusterName);
 								if(tLocalCluster_ClusterMemberNeighbor == null) {
 									Logging.log(this, "     ..neighbor of cluster member is a remote cluster, creating ClusterProxy");
 									ClusterProxy tClusterProxy_ClusterMemberNeighbor = new ClusterProxy(this, tNeighborDescription.getClusterID(), tNeighborDescription.getLevel(), tNeighborDescription.getCoordinatorName(), tNeighborDescription.getCoordinatorL2Address(),  tNeighborDescription.getToken());
 									tClusterProxy_ClusterMemberNeighbor.setPriority(tNeighborDescription.getPriority());
 									getHRS().mapFoGNameToL2Address(tClusterProxy_ClusterMemberNeighbor.getCoordinatorHostName(), tNeighborDescription.getCoordinatorL2Address());
 
 									boolean tFoundSourceIntermediate = false;
 									for(Cluster tLocalCluster : getAllClusters()) {
 										if(tLocalCluster.getHierarchyLevel() == tClusterProxy_ClusterMemberNeighbor.getHierarchyLevel()) {
 											Logging.log(this, "     ..registering source intermediate: " + tClusterProxy_ClusterMemberNeighbor + " <-> " + tLocalCluster);
 											setSourceIntermediateCluster(tClusterProxy_ClusterMemberNeighbor, tLocalCluster);
 											tFoundSourceIntermediate = true;
 										}
 									}
 									
 									if(!tFoundSourceIntermediate) {
 										Logging.err(this, "newConnection() hasn't found a source intermediate cluster for" + tClusterProxy_ClusterMemberNeighbor.getClusterDescription());
 									}
 									
 									// register the link in the local ARG
 									registerLinkARG(tClusterProxy_ClusterMember, tClusterProxy_ClusterMemberNeighbor, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.REMOTE_LINK));
 								}else{
 									Logging.log(this, "     ..neighor of cluster member is the locally known cluster: " + tLocalCluster_ClusterMemberNeighbor);
 
 									// register the link in the local ARG
 									registerLinkARG(tClusterProxy_ClusterMember, tLocalCluster_ClusterMemberNeighbor, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.REMOTE_LINK));
 								}
 								
 								tFoundDescribedNeighbors++;
 							}// described neighbors of cluster members
 							
 							//TODO: remove this
 							for(ControlEntity tNeighbor : tClusterProxy_ClusterMember.getNeighborsARG()) {
 								if(getSourceIntermediateCluster(tNeighbor) != null) {
 									setSourceIntermediateCluster(tClusterProxy_ClusterMember, getSourceIntermediateCluster(tNeighbor));
 								}
 							}
 						} else {
 							Logging.log(this, "newConnection() hasn't found a neighbor description within the member description: " + tClusterMemberDescription);
 						}
 					} else {
 						Logging.warn(this, "newConnection() has found an already defined remote ClusterName: " + tComChannel.getRemoteClusterName());
 					}
 
 					if(tComChannel.getRemoteClusterName() == null) {
 						Logging.warn(this, "newConnection() hasn't found a valid remote ClusterName for ComChannel: " + tComChannel);
 					}
 					
 					tFoundDescribedMembers++;
 				}// described cluster members
 			
 				/**
 				 * Start the communication session
 				 */					
 				Logging.log(this, "     ..starting communication session for the new connection");
 				tComSession.start(pConnection);
 			}else{
 				Logging.err(this, "newConnection() hasn't found a valid cluster description property in the connection requirements: " + tConnectionRequirements);
 			}
 		}else{
 			/**
 			 * We have a probe-routing connection and will print some additional information about the taken route of the connection request
 			 */
 			// get the recorded route from the property
 			LinkedList<HRMID> tRecordedHRMIDs = tPropProbeRouting.getRecordedHops();
 			
 			Logging.log(this, "       ..detected a probe-routing connection(source=" + tPropProbeRouting.getSourceDescription() + " with " + tRecordedHRMIDs.size() + " recorded hops");
 
 			// print the recorded route
 			int i = 0;
 			for(HRMID tHRMID : tRecordedHRMIDs){
 				Logging.log(this, "            [" + i + "]: " + tHRMID);
 				i++;
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.application.util.ServerCallback#error(de.tuilmenau.ics.fog.facade.events.ErrorEvent)
 	 */
 	@Override
 	public void error(ErrorEvent pCause)
 	{
 		Logging.log(this, "Got an error message because of \"" + pCause + "\"");
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * 
 	 * @param pCluster cluster to which the distance has to be computed
 	 * @return number of clusters to target
 	 */
 	public int getClusterDistance(ControlEntity pCluster)
 	{
 		List<AbstractRoutingGraphLink> tClusterRoute = null;
 		int tDistance = 0;
 		if(getSourceIntermediateCluster(pCluster) == null || pCluster == null) {
 			Logging.log(this, "source cluster for " + (pCluster instanceof ClusterProxy ? ((ClusterProxy)pCluster).getClusterDescription() : pCluster.toString() ) + " is " + getSourceIntermediateCluster(pCluster));
 		}
 		Cluster tIntermediateCluster = getSourceIntermediateCluster(pCluster);
 		tClusterRoute = getRouteARG(tIntermediateCluster, pCluster);
 		if(tClusterRoute != null && !tClusterRoute.isEmpty()) {
 			for(AbstractRoutingGraphLink tConnection : tClusterRoute) {
 				if(tConnection.getLinkType() == AbstractRoutingGraphLink.LinkType.REMOTE_LINK) {
 					tDistance++;
 				}
 			}
 		} else {
 			//Logging.log(this, "No cluster route available");
 			tClusterRoute = getRouteARG(tIntermediateCluster, pCluster);
 		}
 		return tDistance;
 	}
 
 	/**
 	 * 
 	 * @param pLevel as level at which a a coordinator will be set
 	 * @param pCluster is the cluster that has set a coordinator
 	 */
 	public void setClusterWithCoordinator(HierarchyLevel pLevel, ICluster pCluster)
 	{
 		Logging.log(this, "Setting " + pCluster + " as cluster that has a connection to a coordinator at level " + pLevel.getValue());
 		mLevelToCluster.put(Integer.valueOf(pLevel.getValue()), pCluster);
 	}
 	
 	/**
 	 * 
 	 * @param pLevel level at which a cluster with a coordinator should be provided
 	 * @return cluster that contains a reference or a connection to a coordinator
 	 */
 	public ICluster getClusterWithCoordinatorOnLevel(int pLevel)
 	{
 		return (mLevelToCluster.containsKey(pLevel) ? mLevelToCluster.get(pLevel) : null );
 	}
 	
 	/**
 	 * 
 	 * @param pCluster is the cluster for which an intermediate cluster is saved as entity that is physically connected
 	 * @param pIntermediate is the cluster that acts as cluster that is intermediately connected to the node
 	 */
 	public void setSourceIntermediateCluster(ICluster pCluster, Cluster pIntermediate)
 	{
 		if(pIntermediate == null) {
 			Logging.err(this, "Setting " + pIntermediate + " as source intermediate for " + pCluster);
 		}
 		mIntermediateMapping.put(pCluster, pIntermediate);
 	}
 	
 	/**
 	 * 
 	 * @param pCluster for which an intermediate cluster is searched
 	 * @return intermediate cluster that is directly connected to the node
 	 */
 	public Cluster getSourceIntermediateCluster(ControlEntity pCluster)
 	{
 		if(mIntermediateMapping.containsKey(pCluster)) {
 			
 			return mIntermediateMapping.get(pCluster);
 		} else {
 			return null;
 		}
 	}
 	
 
 
 	
 
 
 	/**
 	 * Creates a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return "HRM controller@" + getNode();
 	}
 
 	/**
 	 * Determine the name of the central FN of this node
 	 * 
 	 * @return the name of the central FN
 	 */
 	public Name getNodeName()
 	{
 		// get the name of the central FN of this node
 		return getHRS().getCentralFN().getName();
 	}
 	
 	/**
 	 * Stores the ID of the HRM plug-in
 	 */
 	private static final String PLUGIN_ID = "de.tuilmenau.ics.fog.routing.hrm";
 	
 	/**
 	 * Stores the path to the HRM icons
 	 */
 	private static final String PATH_ICONS = "/icons/";
 
 	/**
 	 * The global name space which is used to identify the HRM instances on nodes.
 	 */
 	public final static Namespace ROUTING_NAMESPACE = new Namespace("routing");
 
 	/**
 	 * Stores the identification string for HRM specific routing graph decorations (coordinators & HRMIDs)
 	 */
 	private final static String DECORATION_NAME_COORDINATORS_AND_HRMIDS = "HRM coordinators & HRMIDs";
 
 	/**
 	 * Stores the identification string for HRM specific routing graph decorations (coordinators & clusters)
 	 */
 	private final static String DECORATION_NAME_COORDINATORS_AND_CLUSTERS = "HRM coordinators & clusters";
 }
