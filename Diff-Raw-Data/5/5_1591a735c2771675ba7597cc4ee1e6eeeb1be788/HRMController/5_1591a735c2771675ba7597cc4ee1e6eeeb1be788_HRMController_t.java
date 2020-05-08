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
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
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
 	 * Stores the node specific graph decorator for the active HRM infrastructure
 	 */
 	private NodeDecorator mDecoratorActiveHRMInfrastructure = null;
 
 	/**
 	 * Stores the node specific graph decorator for HRM node base priority
 	 */
 	private NodeDecorator mDecoratorForNodePriorities = null;
 
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
 	 * Stores a database about all registered coordinator proxies.
 	 */
 	private LinkedList<CoordinatorProxy> mLocalCoordinatorProxies = new LinkedList<CoordinatorProxy>();
 	
 	/**
 	 * Stores a database about all registered clusters.
 	 * For example, this list is used for the GUI.
 	 */
 	private LinkedList<Cluster> mLocalClusters = new LinkedList<Cluster>();
 
 	/**
 	 * Stores a database about all registered cluster members (including Cluster objects).
 	 */
 	private LinkedList<ClusterMember> mLocalClusterMembers = new LinkedList<ClusterMember>();
 
 	/**
 	 * Stores a database about all registered CoordinatorAsClusterMemeber instances.
 	 */
 	private LinkedList<CoordinatorAsClusterMember> mLocalCoordinatorAsClusterMemebers = new LinkedList<CoordinatorAsClusterMember>();
 	
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
 	
 	/**
 	 * Stores if the entire FoGSiEm simulation was already created.
 	 * This is only used for debugging purposes. This is NOT a way for avoiding race conditions in signaling.
 	 */
 	private static boolean mFoGSiEmSimulationCreationFinished = false;
 	
 	/**
 	 * Stores the hierarchy node priority
 	 */
 	private long mNodeHierarchyPriority = HRMConfig.Election.DEFAULT_BULLY_PRIORITY;
 	
 	/**
 	 * Stores the connectivity node priority
 	 */
 	private long mNodeConnectivityPriority = HRMConfig.Election.DEFAULT_BULLY_PRIORITY;
 
 	/**
 	 * Stores the central node for the ARG
 	 */
 	private CentralNodeARG mCentralARGNode = null;
 
 	/**
 	 * Stores a description about all connectivity priority updates
 	 */
 	private String mDesriptionConnectivityPriorityUpdates = new String();
 
 	/**
 	 * Stores a description about all hierarchy priority updates
 	 */
 	private String mDesriptionHierarchyPriorityUpdates = new String();
 	
 	/**
 	 * Stores the thread for clustering tasks
 	 */
 	private ClustererThread mClustererThread = null;
 	
 	/**
 	 * Stores a database about all known superior coordinators
 	 */
 	private LinkedList<ClusterName> mSuperiorCoordinators = new LinkedList<ClusterName>();
 	
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
 		 * Create the node specific decorator for HRM node priorities
 		 */
 		mDecoratorForNodePriorities = new NodeDecorator();
 		
 		/**
 		 * Create the node specific decorator for the active HRM infrastructure
 		 */
 		mDecoratorActiveHRMInfrastructure = new NodeDecorator();
 		
 		
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
 		// create own decoration for HRM node priorities
 		tDecoration = Decoration.getInstance(DECORATION_NAME_NODE_PRIORITIES);
 		tDecoration.setDecorator(mNode,  mDecoratorForNodePriorities);
 		// create own decoration for HRM node priorities
 		tDecoration = Decoration.getInstance(DECORATION_NAME_ACTIVE_HRM_INFRASTRUCTURE);
 		tDecoration.setDecorator(mNode,  mDecoratorActiveHRMInfrastructure);
 		// overwrite default decoration
 		tDecoration = Decoration.getInstance(GraphViewer.DEFAULT_DECORATION);
 		tDecoration.setDecorator(mNode,  mDecoratorForCoordinatorsAndClusters);
 		
 		/**
 		 * Create clusterer thread
 		 */
 		mClustererThread = new ClustererThread(this);
 		/**
 		 * Start the clusterer thread
 		 */
 		mClustererThread.start();
 
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
 		
 		// create central node in the local ARG
 		mCentralARGNode = new CentralNodeARG(this);
 
 		// create local loopback session
 		ComSession.createLoopback(this);
 		
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
 	 * Registers a coordinator proxy at the local database.
 	 * 
 	 * @param pCoordinatorProxy the coordinator proxy for a defined coordinator
 	 */
 	public synchronized void registerCoordinatorProxy(CoordinatorProxy pCoordinatorProxy)
 	{
 		Logging.log(this, "Registering coordinator proxy " + pCoordinatorProxy + " at level " + pCoordinatorProxy.getHierarchyLevel().getValue());
 
 		synchronized (mLocalCoordinatorProxies) {
 			// register as known coordinator proxy
 			mLocalCoordinatorProxies.add(pCoordinatorProxy);
 		}
 
 		// are we at base hierarchy level
 		if(pCoordinatorProxy.getHierarchyLevel().isBaseLevel()){
 			// increase hierarchy node priority
 			increaseHierarchyNodePriority_KnownBaseCoordinator(pCoordinatorProxy);
 		}
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// register the coordinator prxy in the local ARG
 		registerNodeARG(pCoordinatorProxy);
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinatorProxy);
 	}
 
 	/**
 	 * Unregisters a coordinator proxy from the local database.
 	 * 
 	 * @param pCoordinatorProxy the coordinator proxy for a defined coordinator
 	 */
 	public synchronized void unregisterCoordinatorProxy(CoordinatorProxy pCoordinatorProxy)
 	{
 		Logging.log(this, "Unregistering coordinator proxy " + pCoordinatorProxy + " at level " + pCoordinatorProxy.getHierarchyLevel().getValue());
 
 		synchronized (mLocalCoordinatorProxies) {
 			// unregister as known coordinator proxy
 			mLocalCoordinatorProxies.remove(pCoordinatorProxy);
 		}
 		
 		// are we at base hierarchy level
 		if(pCoordinatorProxy.getHierarchyLevel().isBaseLevel()){
 			// increase hierarchy node priority
 			decreaseHierarchyNodePriority_KnownBaseCoordinator(pCoordinatorProxy);
 		}
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// register the coordinator prxy in the local ARG
 		unregisterNodeARG(pCoordinatorProxy);
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinatorProxy);
 	}
 
 	/**
 	 * Registers a coordinator at the local database.
 	 * 
 	 * @param pCoordinator the coordinator for a defined cluster
 	 */
 	public synchronized void registerCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "Registering coordinator " + pCoordinator + " at level " + pCoordinator.getHierarchyLevel().getValue());
 
 		Coordinator tFoundAnInferiorCoordinator = getCoordinator(pCoordinator.getHierarchyLevel().getValue() - 1);
 		
 		/**
 		 * Check if the hierarchy is continuous
 		 */
 		if((pCoordinator.getHierarchyLevel().isBaseLevel()) || (tFoundAnInferiorCoordinator != null)){
 			// register a route to the coordinator as addressable target
 			getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(pCoordinator.getHRMID()));
 			
 			synchronized (mLocalCoordinators) {
 				// register as known coordinator
 				mLocalCoordinators.add(pCoordinator);
 			}
 			
 			// are we at base hierarchy level
 			if(pCoordinator.getHierarchyLevel().isBaseLevel()){
 				// increase hierarchy node priority
 				increaseHierarchyNodePriority_KnownBaseCoordinator(pCoordinator);
 			}
 	
 			// updates the GUI decoration for this node
 			updateGUINodeDecoration();
 			
 			// register the coordinator in the local ARG
 			if (HRMConfig.DebugOutput.GUI_SHOW_COORDINATORS_IN_ARG){
 				registerNodeARG(pCoordinator);
 				
 				registerLinkARG(pCoordinator, pCoordinator.getCluster(), new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.OBJECT_REF));
 			}
 	
 			// it's time to update the GUI
 			notifyGUI(pCoordinator);
 		}else{
 			throw new RuntimeException("Hierarchy is non continuous, detected an error in the Matrix");
 		}
 	}
 	
 	/**
 	 * Unregisters a coordinator from the internal database.
 	 * 
 	 * @param pCoordinator the coordinator which should be unregistered
 	 */
 	public synchronized void unregisterCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "Unregistering coordinator " + pCoordinator + " at level " + pCoordinator.getHierarchyLevel().getValue());
 
 		synchronized (mLocalCoordinators) {
 			// unregister from list of known coordinators
 			mLocalCoordinators.remove(pCoordinator);
 		}
 
 		// are we at base hierarchy level
 		if(pCoordinator.getHierarchyLevel().isBaseLevel()){
 			// increase hierarchy node priority
 			decreaseHierarchyNodePriority_KnownBaseCoordinator(pCoordinator);
 		}
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// unregister from the ARG
 		if (HRMConfig.DebugOutput.GUI_SHOW_COORDINATORS_IN_ARG){
 			unregisterNodeARG(pCoordinator);
 		}
 
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
 		
 		if(tHRMID != null){
 			if(!tHRMID.isZero()){
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
 						Logging. warn(this, "Skipping HRMID duplicate " + tHRMID.toString() +", additional registration is triggered by " + pCoordinator);
 					}
 				}
 			}else{
 				Logging.err(this, "Got a zero HRMID " + tHRMID.toString() + " for the coordinator: " + pCoordinator);
 			}
 		}else{
 			Logging.err(this, "Got an invalid HRMID for the coordinator: " + pCoordinator);
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
 		String tActiveHRMInfrastructureText = "";
 		for (int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			LinkedList<Cluster> tClusters = getAllClusters(i);
 			for(Cluster tCluster : tClusters){
 				if(tCluster.hasLocalCoordinator()){
 					if (tActiveHRMInfrastructureText != ""){
 						tActiveHRMInfrastructureText += ", ";
 					}
 					tActiveHRMInfrastructureText += "<" + Long.toString(tCluster.getGUIClusterID()) + ">";
 					for(int j = 0; j < tCluster.getHierarchyLevel().getValue(); j++){
 						tActiveHRMInfrastructureText += "^";	
 					}
 				}
 			}
 		}
 		LinkedList<ClusterName> tSuperiorCoordiantors = getAllSuperiorCoordinators();
 		for(ClusterName tSuperiorCoordinator : tSuperiorCoordiantors){
 			if (tActiveHRMInfrastructureText != ""){
 				tActiveHRMInfrastructureText += ", ";
 			}
 			tActiveHRMInfrastructureText += Long.toString(tSuperiorCoordinator.getGUIClusterID());
 			for(int i = 0; i < tSuperiorCoordinator.getHierarchyLevel().getValue(); i++){
 				tActiveHRMInfrastructureText += "^";	
 			}			
 		}
 		mDecoratorActiveHRMInfrastructure.setText(" [Active clusters: " + tActiveHRMInfrastructureText + "]");
 		mDecoratorForNodePriorities.setText(" [Hier.: " + Long.toString(mNodeHierarchyPriority) + "/ Conn.: " + Long.toString(getConnectivityNodePriority()) + "]");
 		
 		String tNodeText = "";
 		synchronized (mRegisteredOwnHRMIDs) {
 			for (HRMID tHRMID: mRegisteredOwnHRMIDs){
 				if ((!tHRMID.isRelativeAddress()) || (HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES)){
 					tNodeText += ", " + tHRMID.toString();
 				}
 			}			
 		}
 		mDecoratorForCoordinatorsAndHRMIDs.setText(tNodeText);
 		
 		String tClustersText = "";
 		tClustersText = "";
 		LinkedList<ClusterMember> tAllClusterMembers = getAllClusterMembers();
 		for (ClusterMember tClusterMember : tAllClusterMembers){
 			if (tClustersText != ""){
 				tClustersText += ", ";
 			}
 			
 			// is this node the cluster head?
 			if (tClusterMember instanceof Cluster){
 				Cluster tCluster = (Cluster)tClusterMember;
 				if(tCluster.hasLocalCoordinator()){
 					tClustersText += "<" + Long.toString(tClusterMember.getGUIClusterID()) + ">";
 				}else{
 					tClustersText += "(" + Long.toString(tClusterMember.getGUIClusterID()) + ")";
 				}
 			}else{
 				tClustersText += Long.toString(tClusterMember.getGUIClusterID());
 			}
 			for(int i = 0; i < tClusterMember.getHierarchyLevel().getValue(); i++){
 				tClustersText += "^";	
 			}			
 		}
 		mDecoratorForCoordinatorsAndClusters.setText("- clusters: " + tClustersText);
 		
 		/**
 		 * Set the decoration images
 		 */
 		LinkedList<Coordinator> tAllCoordinators = getAllCoordinators();
 		int tHighestCoordinatorLevel = -1;
 		for (Coordinator tCoordinator : tAllCoordinators){
 			int tCoordLevel = tCoordinator.getHierarchyLevel().getValue(); 
 			if (tCoordLevel > tHighestCoordinatorLevel){
 				tHighestCoordinatorLevel = tCoordLevel;
 			}
 		}
 		mDecoratorForNodePriorities.setImage(tHighestCoordinatorLevel);
 		mDecoratorForCoordinatorsAndHRMIDs.setImage(tHighestCoordinatorLevel);
 		mDecoratorForCoordinatorsAndClusters.setImage(tHighestCoordinatorLevel);
 		mDecoratorActiveHRMInfrastructure.setImage(tHighestCoordinatorLevel);
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
 		return getAllCoordinators(pHierarchyLevel.getValue());
 	}
 	
 	/**
 	 * Returns all known coordinators for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for which all coordinators have to be determined
 	 * 
 	 * @return the list of coordinators on the defined hierarchy level
 	 */
 	public LinkedList<Coordinator> getAllCoordinators(int pHierarchyLevel)
 	{
 		LinkedList<Coordinator> tResult = new LinkedList<Coordinator>();
 		
 		// get a list of all known coordinators
 		LinkedList<Coordinator> tAllCoordinators = getAllCoordinators();
 		
 		// iterate over all known coordinators
 		for (Coordinator tCoordinator : tAllCoordinators){
 			// have we found a matching coordinator?
 			if (tCoordinator.getHierarchyLevel().getValue() == pHierarchyLevel){
 				// add this coordinator to the result
 				tResult.add(tCoordinator);
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of all known local coordinator proxies.
 	 * 
 	 * @return the list of known local coordinator proxies
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<CoordinatorProxy> getAllCoordinatorProxies()
 	{
 		LinkedList<CoordinatorProxy> tResult;
 		
 		synchronized (mLocalCoordinatorProxies) {
 			tResult = (LinkedList<CoordinatorProxy>) mLocalCoordinatorProxies.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns all known coordinator proxies for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for which all coordinator proxies have to be determined
 	 * 
 	 * @return the list of coordinator proies at the defined hierarchy level
 	 */
 	public LinkedList<CoordinatorProxy> getAllCoordinatorProxies(int pHierarchyLevel)
 	{
 		//Logging.log(this, "Searching for coordinator proxies at hierarchy level: " + pHierarchyLevel);
 		
 		LinkedList<CoordinatorProxy> tResult = new LinkedList<CoordinatorProxy>();
 		
 		// get a list of all known coordinator proxies
 		LinkedList<CoordinatorProxy> tAllCoordinatorProxies = getAllCoordinatorProxies();
 		
 		// iterate over all known coordinator proxies
 		for (CoordinatorProxy tCoordinatorProxy : tAllCoordinatorProxies){
 			// have we found a matching coordinator proxy?
 			if (tCoordinatorProxy.getHierarchyLevel().getValue() == pHierarchyLevel){
 				// add this coordinator proxy to the result
 				tResult.add(tCoordinatorProxy);
 			}
 		}
 		
 		//Logging.log(this, "      ..found: " + tResult);
 				
 		return tResult;
 	}
 
 	/**
 	 * Registers a coordinator-as-cluster-member at the local database.
 	 * 
 	 * @param pCoordinatorAsClusterMember the coordinator-as-cluster-member which should be registered
 	 */
 	public synchronized void registerCoordinatorAsClusterMember(CoordinatorAsClusterMember pCoordinatorAsClusterMember)
 	{
 		int tLevel = pCoordinatorAsClusterMember.getHierarchyLevel().getValue();
 
 		Logging.log(this, "Registering coordinator-as-cluster-member " + pCoordinatorAsClusterMember + " at level " + tLevel);
 		
 		synchronized (mLocalCoordinatorAsClusterMemebers) {
 			// make sure the Bully priority is the right one, avoid race conditions here
 			pCoordinatorAsClusterMember.setPriority(BullyPriority.create(this, getHierarchyNodePriority()));
 
 			// register as known coordinator-as-cluster-member
 			mLocalCoordinatorAsClusterMemebers.add(pCoordinatorAsClusterMember);			
 		}
 		
 		if(HRMConfig.DebugOutput.GUI_SHOW_COORDINATOR_CLUSTER_MEMBERS_IN_ARG){
 			// updates the GUI decoration for this node
 			updateGUINodeDecoration();
 
 			// register the node in the local ARG
 			registerNodeARG(pCoordinatorAsClusterMember);
 
 			// register the link in the local ARG
 			registerLinkARG(pCoordinatorAsClusterMember, pCoordinatorAsClusterMember.getCoordinator(), new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.OBJECT_REF));
 
 			// register link to central node in the ARG
 			if (HRMConfig.DebugOutput.SHOW_ALL_OBJECT_REFS_TO_CENTRAL_NODE_IN_ARG){
 				registerLinkARG(mCentralARGNode, pCoordinatorAsClusterMember, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.OBJECT_REF));
 			}
 
 			// it's time to update the GUI
 			notifyGUI(pCoordinatorAsClusterMember);
 		}
 	}
 	
 	/**
 	 * Unregister a coordinator-as-cluster-member from the local database
 	 * 
 	 * @param pCoordinatorAsClusterMember the coordinator-as-cluster-member which should be unregistered
 	 */
 	public synchronized void unregisterCoordinatorAsClusterMember(CoordinatorAsClusterMember pCoordinatorAsClusterMember)
 	{
 		Logging.log(this, "Unregistering coordinator-as-cluster-member " + pCoordinatorAsClusterMember);
 
 		synchronized (mLocalCoordinatorAsClusterMemebers) {
 			// unregister from list of known cluster members
 			mLocalCoordinatorAsClusterMemebers.remove(pCoordinatorAsClusterMember);
 		}
 
 		if(HRMConfig.DebugOutput.GUI_SHOW_COORDINATOR_CLUSTER_MEMBERS_IN_ARG){
 			// updates the GUI decoration for this node
 			updateGUINodeDecoration();
 	
 			// register at the ARG
 			unregisterNodeARG(pCoordinatorAsClusterMember);
 	
 			// it's time to update the GUI
 			notifyGUI(pCoordinatorAsClusterMember);
 		}
 	}
 
 	/**
 	 * Registers a cluster member at the local database.
 	 * 
 	 * @param pClusterMember the cluster member which should be registered
 	 */
 	public synchronized void registerClusterMember(ClusterMember pClusterMember)
 	{
 		int tLevel = pClusterMember.getHierarchyLevel().getValue();
 
 		Logging.log(this, "Registering cluster member " + pClusterMember + " at level " + tLevel);
 		
 		synchronized (mLocalClusterMembers) {
 			
 			// make sure the Bully priority is the right one, avoid race conditions here
 			pClusterMember.setPriority(BullyPriority.create(this, getConnectivityNodePriority()));
 
 			// register as known cluster member
 			mLocalClusterMembers.add(pClusterMember);			
 		}
 		
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 
 		// register the cluster in the local ARG
 		registerNodeARG(pClusterMember);
 
 		// register link to central node in the ARG
 		if (HRMConfig.DebugOutput.SHOW_ALL_OBJECT_REFS_TO_CENTRAL_NODE_IN_ARG){
 			registerLinkARG(mCentralARGNode, pClusterMember, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.OBJECT_REF));
 		}
 
 		// it's time to update the GUI
 		notifyGUI(pClusterMember);
 	}
 
 	/**
 	 * Unregister a cluster member from the local database
 	 * 
 	 * @param pClusterMember the cluster member which should be unregistered
 	 */
 	public synchronized void unregisterClusterMember(ClusterMember pClusterMember)
 	{
 		Logging.log(this, "Unregistering cluster member " + pClusterMember);
 
 		synchronized (mLocalClusterMembers) {
 			// unregister from list of known cluster members
 			mLocalClusterMembers.remove(pClusterMember);
 		}
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 
 		// register at the ARG
 		unregisterNodeARG(pClusterMember);
 
 		// it's time to update the GUI
 		notifyGUI(pClusterMember);
 	}
 
 	/**
 	 * Registers a cluster at the local database.
 	 * 
 	 * @param pCluster the cluster which should be registered
 	 */
 	public synchronized void registerCluster(Cluster pCluster)
 	{
 		int tLevel = pCluster.getHierarchyLevel().getValue();
 
 		Logging.log(this, "Registering cluster " + pCluster + " at level " + tLevel);
 
 		synchronized (mLocalClusters) {
 			// register as known cluster
 			mLocalClusters.add(pCluster);
 		}
 		
 		synchronized (mLocalClusterMembers) {
 			// register as known cluster member
 			mLocalClusterMembers.add(pCluster);			
 		}
 		
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 
 		// register the cluster in the local ARG
 		registerNodeARG(pCluster);
 
 		// register link to central node in the ARG
 		if (HRMConfig.DebugOutput.SHOW_ALL_OBJECT_REFS_TO_CENTRAL_NODE_IN_ARG){
 			registerLinkARG(mCentralARGNode, pCluster, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.OBJECT_REF));
 		}
 
 		// it's time to update the GUI
 		notifyGUI(pCluster);
 	}
 	
 	/**
 	 * Unregisters a cluster from the local database.
 	 * 
 	 * @param pCluster the cluster which should be unregistered.
 	 */
 	public synchronized void unregisterCluster(Cluster pCluster)
 	{
 		Logging.log(this, "Unregistering cluster " + pCluster);
 
 		synchronized (mLocalClusters) {
 			// unregister from list of known clusters
 			mLocalClusters.remove(pCluster);
 		}
 		
 		synchronized (mLocalClusterMembers) {
 			// unregister from list of known cluster members
 			mLocalClusterMembers.remove(pCluster);
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
 					// updates the GUI decoration for this node
 					updateGUINodeDecoration();
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
 				// updates the GUI decoration for this node
 				updateGUINodeDecoration();
 				// it's time to update the GUI
 				notifyGUI(pCluster);
 			}
 		}else{
 			// we are at a higher hierarchy level and don't need the HRMID revocation
 			Logging.warn(this, "Skipping HRMID revocation of " + pHRMID.toString() + " for " + pCluster);
 		}
 	}
 
 	/**
 	 * Registers a superior coordinator at the local database
 	 * 
 	 * @param pSuperiorCoordinatorClusterName a description of the announced superior coordinator
 	 */
 	public void registerSuperiorCoordinator(ClusterName pSuperiorCoordinatorClusterName)
 	{
 		boolean tUpdateGui = false;
 		synchronized (mSuperiorCoordinators) {
 			if(!mSuperiorCoordinators.contains(pSuperiorCoordinatorClusterName)){
 				Logging.log(this, "Registering superior coordinator: " + pSuperiorCoordinatorClusterName + ", knowing these superior coordinators: " + mSuperiorCoordinators);
 				mSuperiorCoordinators.add(pSuperiorCoordinatorClusterName);
 				tUpdateGui = true;
 			}else{
 				// already registered
 			}
 		}
 		
 		/**
 		 * Update the GUI
 		 */
 		// updates the GUI decoration for this node
 		if(tUpdateGui){
 			updateGUINodeDecoration();
 		}
 	}
 
 	/**
 	 * Unregisters a formerly registered superior coordinator from the local database
 	 * 
 	 * @param pSuperiorCoordinatorClusterName a description of the invalid superior coordinator
 	 */
 	public void unregisterSuperiorCoordinator(ClusterName pSuperiorCoordinatorClusterName)
 	{
 		boolean tUpdateGui = false;
 		synchronized (mSuperiorCoordinators) {
 			if(mSuperiorCoordinators.contains(pSuperiorCoordinatorClusterName)){
 				Logging.log(this, "Unregistering superior coordinator: " + pSuperiorCoordinatorClusterName + ", knowing these superior coordinators: " + mSuperiorCoordinators);
 				mSuperiorCoordinators.remove(pSuperiorCoordinatorClusterName);
 				tUpdateGui = true;
 			}else{
 				// already removed or never registered
 			}
 		}
 		
 		/**
 		 * Update the GUI
 		 */
 		// updates the GUI decoration for this node
 		if(tUpdateGui){
 			updateGUINodeDecoration();
 		}
 	}
 
 	/**
 	 * Returns all superior coordinators
 	 * 
 	 * @return the superior coordinators
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ClusterName> getAllSuperiorCoordinators()
 	{
 		LinkedList<ClusterName> tResult = null;
 		
 		synchronized (mSuperiorCoordinators) {
 			tResult = (LinkedList<ClusterName>) mSuperiorCoordinators.clone();
 		}
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of known coordinator as cluster members.
 	 * 
 	 * @return the list of known coordinator as cluster members
 	 */
 	public LinkedList<CoordinatorAsClusterMember> getAllCoordinatorAsClusterMemebers()
 	{
 		LinkedList<CoordinatorAsClusterMember> tResult = null;
 		
 		synchronized (mLocalCoordinatorAsClusterMemebers) {
 			tResult = (LinkedList<CoordinatorAsClusterMember>) mLocalCoordinatorAsClusterMemebers.clone();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of known cluster members.
 	 * 
 	 * @return the list of known cluster members
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ClusterMember> getAllClusterMembers()
 	{
 		LinkedList<ClusterMember> tResult = null;
 		
 		synchronized (mLocalClusterMembers) {
 			tResult = (LinkedList<ClusterMember>) mLocalClusterMembers.clone();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of known cluster members (including local Cluster objects) for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the list of cluster members
 	 */
 	public LinkedList<ClusterMember> getAllClusterMembers(HierarchyLevel pHierarchyLevel)
 	{
 		return getAllClusterMembers(pHierarchyLevel.getValue());
 	}
 	
 	/**
 	 * Returns a list of known cluster members (including local Cluster objects) for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the list of cluster members
 	 */
 	public LinkedList<ClusterMember> getAllClusterMembers(int pHierarchyLevel)
 	{
 		LinkedList<ClusterMember> tResult = new LinkedList<ClusterMember>();
 		
 		// get a list of all known coordinators
 		LinkedList<ClusterMember> tAllClusterMembers = getAllClusterMembers();
 		
 		// iterate over all known coordinators
 		for (ClusterMember tClusterMember : tAllClusterMembers){
 			// have we found a matching coordinator?
 			if (tClusterMember.getHierarchyLevel().getValue() == pHierarchyLevel){
 				// add this coordinator to the result
 				tResult.add(tClusterMember);
 			}
 		}
 		
 		return tResult;
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
 		return getAllClusters(pHierarchyLevel.getValue());
 	}
 
 	/**
 	 * Returns a list of known clusters for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the list of clusters
 	 */
 	public LinkedList<Cluster> getAllClusters(int pHierarchyLevel)
 	{
 		LinkedList<Cluster> tResult = new LinkedList<Cluster>();
 		
 		// get a list of all known coordinators
 		LinkedList<Cluster> tAllClusters = getAllClusters();
 		
 		// iterate over all known coordinators
 		for (Cluster tCluster : tAllClusters){
 			// have we found a matching coordinator?
 			if (tCluster.getHierarchyLevel().getValue() == pHierarchyLevel){
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
 				break;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the locally known Cluster object for a given hierarchy level
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for which the Cluster object is searched
 	 * 
 	 * @return the found Cluster object
 	 */
 	public Cluster getCluster(int pHierarchyLevel)
 	{
 		Cluster tResult = null;
 
 		for(Cluster tKnownCluster : getAllClusters()) {
 			if(tKnownCluster.getHierarchyLevel().getValue() == pHierarchyLevel) {
 				tResult = tKnownCluster;
 				break;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the locally known Coordinator object for a given hierarchy level
 	 * 
 	 * @param pHierarchyLevelValue the hierarchy level for which the Coordinator object is searched
 	 * 
 	 * @return the found Coordinator object
 	 */
 	public Coordinator getCoordinator(int pHierarchyLevelValue)
 	{
 		Coordinator tResult = null;
 
 		for(Coordinator tKnownCoordinator : getAllCoordinators()) {
 			if(tKnownCoordinator.getHierarchyLevel().getValue() == pHierarchyLevelValue) {
 				tResult = tKnownCoordinator;
 				break;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the locally known Coordinator object for a given hierarchy level
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for which the Coordinator object is searched
 	 * 
 	 * @return the found Coordinator object
 	 */
 	public Coordinator getCoordinator(HierarchyLevel pHierarchyLevel)
 	{
 		Coordinator tResult = null;
 
 		for(Coordinator tKnownCoordinator : getAllCoordinators()) {
 			if(tKnownCoordinator.getHierarchyLevel().equals(pHierarchyLevel)) {
 				tResult = tKnownCoordinator;
 				break;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the locally known CoordinatorProxy object, which was identified by its ClusterName
 	 *  
 	 * @param pClusterName the cluster name of the searched coordinator proxy
 	 * 
 	 * @return the desired CoordinatorProxy, null if the coordinator isn't known
 	 */
 	public CoordinatorProxy getCoordinatorProxyByName(ClusterName pClusterName)
 	{
 		CoordinatorProxy tResult = null;
 		
 		synchronized (mLocalCoordinatorProxies) {
 			for (CoordinatorProxy tCoordinatorProxy : mLocalCoordinatorProxies){
 				if(tCoordinatorProxy.equals(pClusterName)){
 					tResult = tCoordinatorProxy;
 					break;
 				}
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns a known coordinator, which is identified by its ID.
 	 * 
 	 * @param pCoordinatorID the coordinator ID
 	 * 
 	 * @return the searched coordinator object
 	 */
 	public Coordinator getCoordinatorByID(int pCoordinatorID)
 	{
 		Coordinator tResult = null;
 		
 		for(Coordinator tKnownCoordinator : getAllCoordinators()) {
 			if (tKnownCoordinator.getCoordinatorID() == pCoordinatorID) {
 				tResult = tKnownCoordinator;
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Clusters the given hierarchy level
 	 * HINT: It is synchronized to only one call at the same time.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level where a clustering should be done
 	 */
 	public void cluster(ControlEntity pCause, final HierarchyLevel pHierarchyLevel)
 	{
 		if(pHierarchyLevel.getValue() <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 			Logging.log(this, "CLUSTERING TRIGGERED at hierarchy level: " + pHierarchyLevel.getValue());
 			mClustererThread.eventUpdateCluster(pCause, pHierarchyLevel);
 		}
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
 	 * HINT: This function has to be called in a separate thread!
 	 * 
 	 * @param pDestinationL2Address the L2 address of the destination
 	 * 
 	 * @return the found comm. session or null
 	 */
 	public ComSession getCreateComSession(L2Address pDestinationL2Address)
 	{
 		ComSession tResult = null;
 		
 		// is the destination valid?
 		if (pDestinationL2Address != null){
 			//Logging.log(this, "Searching for outgoing comm. session to: " + pDestinationL2Address);
 			synchronized (mLocalOutgoingSessions) {
 				for (ComSession tComSession : mLocalOutgoingSessions){
 					//Logging.log(this, "   ..ComSession: " + tComSession);
 					
 					// get the L2 address of the comm. session peer
 					L2Address tPeerL2Address = tComSession.getPeerL2Address();
 							
 					if(pDestinationL2Address.equals(tPeerL2Address)){
 						//Logging.log(this, "     ..found match");
 						tResult = tComSession;
 						break;
 					}else{
 						//Logging.log(this, "     ..uninteresting");
 					}
 				}
 			}
 			
 			// have we found an already existing connection?
 			if(tResult == null){
 				//Logging.log(this, "getCreateComSession() could find a comm. session for destination: " + pDestinationL2Address + ", knowing these sessions and their channels:");
 				synchronized (mLocalOutgoingSessions) {
 					for (ComSession tComSession : mLocalOutgoingSessions){
 						//Logging.log(this, "   ..ComSession: " + tComSession);
 						for(ComChannel tComChannel : tComSession.getAllComChannels()){
 							//Logging.log(this, "     ..ComChannel: " + tComChannel);
 							//Logging.log(this, "        ..RemoteCluster: " + tComChannel.getRemoteClusterName().toString());
 						}
 					}
 				}
 
 				/**
 				 * Create the new connection
 				 */
 				//Logging.log(this, "   ..creating new connection and session to: " + pDestinationL2Address);
 				tResult = createOutgoingComSession(pDestinationL2Address);
 			}
 		}else{
 			//Logging.err(this, "getCreateComSession() detected invalid destination L2 address");
 		}
 		return tResult;
 	}
 
 	/**
 	 * Creates a new comm. session (incl. connection) to a given destination L2 address and uses the given connection requirements
 	 * HINT: This function has to be called in a separate thread!
 	 * 
 	 * @param pDestinationL2Address the L2 address of the destination
 	 * 
 	 * @return the new comm. session or null
 	 */
 	private ComSession createOutgoingComSession(L2Address pDestinationL2Address)
 	{
 		ComSession tResult = null;
 
 		/**
 		 * Create default connection requirements
 		 */
 		Description tConnectionRequirements = createHRMControllerDestinationDescription();
 
 		Logging.log(this, "Creating connection/comm. session to: " + pDestinationL2Address + " with requirements: " + tConnectionRequirements);
 		
 		/**
 		 * Create communication session
 		 */
 	    Logging.log(this, "    ..creating new communication session");
 	    ComSession tComSession = new ComSession(this, false);
 		
 	    /**
 	     * Wait until the FoGSiEm simulation is created
 	     */
 		if(HRMConfig.DebugOutput.BLOCK_HIERARCHY_UNTIL_END_OF_SIMULATION_CREATION)
 		{
 			while(!simulationCreationFinished()){
 				try {
 					Logging.log(this, "WAITING FOR END OF SIMULATION CREATION");
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 				
 		/**
 		 * Connect to the neighbor node
 		 */
 		Connection tConnection = null;				
 	    Logging.log(this, "    ..CONNECTING to: " + pDestinationL2Address + " with requirements: " + tConnectionRequirements);
 		try {
 			tConnection = connectBlock(pDestinationL2Address, tConnectionRequirements, getNode().getIdentity());
 		} catch (NetworkException tExc) {
 			Logging.err(this, "Cannot connect to: " + pDestinationL2Address, tExc);
 		}
 	    Logging.log(this, "    ..connectBlock() FINISHED");
 		if(tConnection != null) {
 
 			mCounterOutgoingConnections++;
 			
 			Logging.log(this, "     ..starting this OUTGOING CONNECTION as nr. " + mCounterOutgoingConnections);
 			tComSession.startConnection(pDestinationL2Address, tConnection);
 			
 			// return the created comm. session
 			tResult = tComSession;
 		}else{
 			Logging.err(this, "     ..connection failed to: " + pDestinationL2Address + " with requirements: " + tConnectionRequirements);
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
 	public void registerLinkL2(L2Address pNeighborL2Address, Route pRoute)
 	{
 		// inform the HRS about the new route
 		if(getHRS().registerLinkL2(pNeighborL2Address, pRoute)){
 			// it's time to update the GUI
 			notifyGUI(this);
 		}
 	}
 
 	/**
 	 * Connects to a service with the given name. Method blocks until the connection has been set up.
 	 * 
 	 * @param pDestination the connection destination
 	 * @param pRequirements the requirements for the connection
 	 * @param pIdentity the identity of the connection requester
 	 * 
 	 * @return the created connection
 	 * 
 	 * @throws NetworkException
 	 */
 	private Connection connectBlock(Name pDestination, Description pRequirements, Identity pIdentity) throws NetworkException
 	{
 		Logging.log(this, "\n\n\n========> OUTGOING CONNECTION REQUEST TO: " + pDestination + " with requirements: " + pRequirements);
 
 		// connect
 		Connection tConnection = getLayer().connect(pDestination, pRequirements, pIdentity);
 		Logging.log(this, "        ..=====> got connection: " + tConnection);
 		
 		
 		// create blocking event handler
 		BlockingEventHandling tBlockingEventHandling = new BlockingEventHandling(tConnection, 1);
 		
 		// wait for the first event
 		Event tEvent = tBlockingEventHandling.waitForEvent();
 		Logging.log(this, "        ..=====> got connection event: " + tEvent);
 		
 		if(tEvent instanceof ConnectedEvent) {
 			if(!tConnection.isConnected()) {
 				throw new NetworkException(this, "Connected event but connection is not connected.");
 			} else {
 				return tConnection;
 			}
 		}else if(tEvent instanceof ErrorEvent) {
 			Exception exc = ((ErrorEvent) tEvent).getException();
 			
 			if(exc instanceof NetworkException) {
 				throw (NetworkException) exc;
 			} else {
 				throw new NetworkException(this, "Can not connect to " + pDestination +".", exc);
 			}
 		}else{
 			throw new NetworkException(this, "Can not connect to " + pDestination +" due to " + tEvent);
 		}
 	}
 
 	/**
 	 * Marks the FoGSiEm simulation creation as finished.
 	 */
 	public static void simulationCreationHasFinished()
 	{
 		mFoGSiEmSimulationCreationFinished = true;
 	}
 	
 	/**
 	 * Checks if the entire simulation was created
 	 * 
 	 * @return true or false
 	 */
 	private boolean simulationCreationFinished()
 	{
 		return mFoGSiEmSimulationCreationFinished;
 	}
 
 	/**
 	 * Determines the Cluster object (on hierarchy level 0) for a given network interface
 	 * 
 	 * @param pInterface the network interface
 	 * 
 	 * @return the found Cluster object, null if nothing was found
 	 */
 	private Cluster getBaseHierarchyLevelCluster(NetworkInterface pInterface)
 	{
 		Cluster tResult = null;
 		
 		LinkedList<Cluster> tBaseClusters = getAllClusters(HierarchyLevel.BASE_LEVEL);
 		for (Cluster tCluster : tBaseClusters){
 			NetworkInterface tClusterNetIf = tCluster.getBaseHierarchyLevelNetworkInterface();
 			if ((pInterface == tClusterNetIf) || (pInterface.equals(tCluster.getBaseHierarchyLevelNetworkInterface()))){
 				tResult = tCluster;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines the hierarchy node priority for Election processes
 	 * 
 	 * @return the hierarchy node priority
 	 */
 	public long getHierarchyNodePriority()
 	{
 		if (HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY){
 			return mNodeHierarchyPriority;
 		}else{
 			return getConnectivityNodePriority();
 		}
 	}
 	
 	/**
 	 * Determines the connectivity node priority for Election processes
 	 * 
 	 * @return the connectivity node priority
 	 */
 	public long getConnectivityNodePriority()
 	{
 		return mNodeConnectivityPriority;
 	}
 	
 	/**
 	 * Sets new connectivity node priority for Election processes
 	 * 
 	 * @param pPriority the new connectivity node priority
 	 */
 	private int mConnectivityPriorityUpdates = 0;
 	private synchronized void setConnectivityPriority(long pPriority)
 	{
 		Logging.log(this, "Setting new connectivity node priority: " + pPriority);
 		mNodeConnectivityPriority = pPriority;
 
 		mConnectivityPriorityUpdates++;
 		
 		/**
 		 * Inform all local cluster members about the change
 		 * HINT: we have to enforce a permanent lock of mLocalClusterMembers, 
 		 *       otherwise race conditions might be caused (another ClusterMemeber 
 		 *       could be created while we are updating the priorities of all the 
 		 *       formerly known ones)
 		 */
 		synchronized (mLocalClusterMembers) {
 			Logging.log(this, "Informing these cluser members about the priority (" + pPriority + ") update (" + mConnectivityPriorityUpdates + "): " + mLocalClusterMembers);
 			int i = 0;
 			for(ClusterMember tClusterMember : mLocalClusterMembers){
 				Logging.log(this, "      ..update (" + mConnectivityPriorityUpdates + ") - informing[" + i + "]: " + tClusterMember);
 				tClusterMember.eventConnectivityNodePriorityUpdate(getConnectivityNodePriority());
 				i++;
 			}
 		}
 	}
 	
 	/**
 	 * Sets new hierarchy node priority for Election processes
 	 * 
 	 * @param pPriority the new hierarchy node priority
 	 */
 	private int mHierarchyPriorityUpdates = 0;
 	@SuppressWarnings("unchecked")
 	private synchronized void setHierarchyPriority(long pPriority)
 	{
 		Logging.log(this, "Setting new hierarchy node priority: " + pPriority);
 		mNodeHierarchyPriority = pPriority;
 
 		mHierarchyPriorityUpdates++;
 		
 		/**
 		 * Inform all local CoordinatorAsClusterMemeber objects about the change
 		 * HINT: we have to enforce a permanent lock of mLocalCoordinatorAsClusterMemebers, 
 		 *       otherwise race conditions might be caused (another CoordinatorAsClusterMemeber 
 		 *       could be created while we are updating the priorities of all the 
 		 *       formerly known ones)
 		 */
 		synchronized (mLocalCoordinatorAsClusterMemebers) {
 			for(CoordinatorAsClusterMember tCoordinatorAsClusterMember : mLocalCoordinatorAsClusterMemebers){
 				tCoordinatorAsClusterMember.eventHierarchyNodePriorityUpdate(getHierarchyNodePriority());
 			}
 		}
 	}
 
 	/**
 	 * Increases base Bully priority
 	 * 
 	 * @param pCausingInterfaceToNeighbor the update causing interface to a neighbor
 	 */
 	private synchronized void increaseNodePriority_Connectivity(NetworkInterface pCausingInterfaceToNeighbor)
 	{
 		// get the current priority
 		long tPriority = getConnectivityNodePriority();
 		
 		Logging.log(this, "Increasing node priority (CONNECTIVITY) by " + BullyPriority.OFFSET_FOR_CONNECTIVITY);
 
 		// increase priority
 		tPriority += BullyPriority.OFFSET_FOR_CONNECTIVITY;
 		
 		mDesriptionConnectivityPriorityUpdates += "\n + " + BullyPriority.OFFSET_FOR_CONNECTIVITY + " ==> " + pCausingInterfaceToNeighbor;
 		
 		// update priority
 		setConnectivityPriority(tPriority);
 
 		Logging.log(this, "Increasing hierarchy node priority (CONNECTIVITY) by " + BullyPriority.OFFSET_FOR_CONNECTIVITY);
 		
 		// get the current priority
 		long tHierarchyPriority = mNodeHierarchyPriority;
 
 		// increase priority
 		tHierarchyPriority += BullyPriority.OFFSET_FOR_CONNECTIVITY;
 
 		mDesriptionHierarchyPriorityUpdates += "\n + " + BullyPriority.OFFSET_FOR_CONNECTIVITY + " <== Cause: " + pCausingInterfaceToNeighbor;
 
 		// update priority
 		setHierarchyPriority(tHierarchyPriority);
 	}
 	
 	/**
 	 * Increases hierarchy Bully priority
 	 * 
 	 * @param pCausingEntity the update causing entity
 	 */
 	public void increaseHierarchyNodePriority_KnownBaseCoordinator(ControlEntity pCausingEntity)
 	{
 		int tDistance = 0;
 		if(pCausingEntity instanceof CoordinatorProxy){
 			tDistance = ((CoordinatorProxy)pCausingEntity).getDistance();
 		}
 
 		if((tDistance >= 0) && (tDistance <= HRMConfig.Hierarchy.EXPANSION_RADIUS)){
 			// get the current priority
 			long tPriority = mNodeHierarchyPriority;
 			
			float tOffset = (float)BullyPriority.OFFSET_FOR_KNOWN_BASE_REMOTE_COORDINATOR * (2 + HRMConfig.Hierarchy.EXPANSION_RADIUS - tDistance);
 					
 			Logging.log(this, "Increasing hierarchy node priority (KNOWN BASE COORDINATOR) by " + (long)tOffset + ", distance=" + tDistance + "/" + HRMConfig.Hierarchy.EXPANSION_RADIUS);
 	
 			// increase priority
 			tPriority += (long)(tOffset);
 			
 			mDesriptionHierarchyPriorityUpdates += "\n + " + tOffset + " <== HOPS: " + tDistance + "/" + HRMConfig.Hierarchy.EXPANSION_RADIUS + ", Cause: " + pCausingEntity;
 
 			// update priority
 			setHierarchyPriority(tPriority);
 		}else{
 			Logging.err(this, "Detected invalid distance: " + tDistance + "/" + HRMConfig.Hierarchy.EXPANSION_RADIUS);
 		}
 	}
 
 	/**
 	 * Decreases hierarchy Bully priority
 	 * 
 	 * @param pCausingEntity the update causing entity
 	 */
 	public void decreaseHierarchyNodePriority_KnownBaseCoordinator(ControlEntity pCausingEntity)
 	{
 		int tDistance = 0;
 		if(pCausingEntity instanceof CoordinatorProxy){
 			tDistance = ((CoordinatorProxy)pCausingEntity).getDistance();
 		}
 		
 		if((tDistance >= 0) && (tDistance <= HRMConfig.Hierarchy.EXPANSION_RADIUS)){
 			// get the current priority
 			long tPriority = mNodeHierarchyPriority;
 			
			float tOffset = (float)BullyPriority.OFFSET_FOR_KNOWN_BASE_REMOTE_COORDINATOR * (2 + HRMConfig.Hierarchy.EXPANSION_RADIUS - tDistance);
 			
 			Logging.log(this, "Decreasing hierarchy node priority (KNOWN BASE COORDINATOR) by " + (long)tOffset + ", distance=" + tDistance + "/" + HRMConfig.Hierarchy.EXPANSION_RADIUS);
 	
 			// decrease priority
 			tPriority -= (long)(tOffset);
 			
 			mDesriptionHierarchyPriorityUpdates += "\n - " + tOffset + " <== HOPS: " + tDistance + "/" + HRMConfig.Hierarchy.EXPANSION_RADIUS + ", Cause: " + pCausingEntity;
 
 			// update priority
 			setHierarchyPriority(tPriority);
 		}else{
 			Logging.err(this, "Detected invalid distance: " + tDistance + "/" + HRMConfig.Hierarchy.EXPANSION_RADIUS);
 		}
 	}
 
 	/**
 	 * Returns a description about all connectivity priority updates
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionConnectivityPriorityUpdates()
 	{
 		return mDesriptionConnectivityPriorityUpdates;
 	}
 
 	/**
 	 * Returns a description about all hierarchy priority updates
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionHierarchyPriorityUpdates()
 	{
 		return mDesriptionHierarchyPriorityUpdates;
 	}
 
 	/**
 	 * Returns a log about "update cluster" events
 	 */
 	public String getGUIDescriptionClusterUpdates()
 	{
 		return mClustererThread.getGUIDescriptionClusterUpdates();
 	}
 	
 	/**
 	 * Reacts on a detected new physical neighbor. A new connection to this neighbor is created.
 	 * HINT: "pNeighborL2Address" doesn't correspond to the neighbor's central FN!
 	 * 
 	 * @param pInterfaceToNeighbor the network interface to the neighbor 
 	 * @param pNeighborL2Address the L2 address of the detected physical neighbor's first FN towards the common bus.
 	 */
 	public synchronized void eventDetectedPhysicalNeighborNode(final NetworkInterface pInterfaceToNeighbor, final L2Address pNeighborL2Address)
 	{
 		Logging.log(this, "\n\n\n############## FOUND DIRECT NEIGHBOR NODE " + pNeighborL2Address + ", interface=" + pInterfaceToNeighbor);
 		
 		/**
 		 * Helper for having access to the HRMController within the created thread
 		 */
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
 				Thread.currentThread().setName("NeighborConnector@" + tHRMController.getNodeGUIName() + " for " + pNeighborL2Address);
 
 				/**
 				 * Create/get the cluster on base hierarchy level
 				 */
 				Cluster tParentCluster = null;
 				//HINT: we make sure that we use only one Cluster object per Bus
 				Cluster tExistingCluster = getBaseHierarchyLevelCluster(pInterfaceToNeighbor);
 				if (tExistingCluster != null){
 				    Logging.log(this, "    ..using existing level0 cluster: " + tExistingCluster);
 					tParentCluster = tExistingCluster;
 				}else{
 				    Logging.log(this, "    ..knowing level0 clusters: " + getAllClusters(0));
 				    Logging.log(this, "    ..creating new level0 cluster");
 					tParentCluster = Cluster.createBaseCluster(tHRMController);
 					tParentCluster.setBaseHierarchyLevelNetworkInterface(pInterfaceToNeighbor);
 					
 					increaseNodePriority_Connectivity(pInterfaceToNeighbor);
 				}
 
 				/**
 				 * Create communication session
 				 */
 			    Logging.log(this, "    ..get/create communication session");
 				ComSession tComSession = getCreateComSession(pNeighborL2Address);		
 				if(tComSession != null) {
 					/**
 					 * Update ARG
 					 */
 					//mHRMController.registerLinkARG(this, tParentCluster, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.REMOTE_CONNECTION));
 
 				    /**
 				     * Create communication channel
 				     */
 				    Logging.log(this, "    ..creating new communication channel");
 					ComChannel tComChannel = new ComChannel(tHRMController, ComChannel.Direction.OUT, tParentCluster, tComSession);
 					tComChannel.setRemoteClusterName(tParentCluster.createClusterName());
 
 					/**
 					 * Send "RequestClusterMembership" along the comm. session
 					 * HINT: we cannot use the created channel because the remote side doesn't know anything about the new comm. channel yet)
 					 */
 					RequestClusterMembership tRequestClusterMembership = new RequestClusterMembership(getNodeName(), pNeighborL2Address, tParentCluster.createClusterName(), tParentCluster.createClusterName());
 				    Logging.log(this, "           ..sending membership request: " + tRequestClusterMembership);
 					if (tComSession.write(tRequestClusterMembership)){
 						Logging.log(this, "          ..requested sucessfully for membership of: " + tParentCluster + " at node " + pNeighborL2Address);
 					}else{
 						Logging.log(this, "          ..failed to request for membership of: " + tParentCluster + " at node " + pNeighborL2Address);
 					}
 
 					Logging.log(this, "Connection thread for " + pNeighborL2Address + " finished");
 				}else{
 					Logging.log(this, "Connection thread for " + pNeighborL2Address + " failed");
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
 		if(HRMConfig.Hierarchy.TOPOLOGY_REPORTS){
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
 			if((tRoute != null) && (!tRoute.isEmpty())) {
 				// get the first route part, which corresponds to the link between the central FN and the searched first FN towards the neighbor 
 				RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 				// check if route has entries
 				if((tPath != null) && (!tPath.isEmpty())){
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
 						Logging.err(this, "getL2AddressOfFirstFNTowardsNeighbor() found a first FN (" + tFirstNodeBeforeBusToNeighbor + ") towards the neighbor " + pNeighborName + " but it has the wrong class type");
 					}
 				}else{
 					Logging.warn(this, "getL2AddressOfFirstFNTowardsNeighbor() found an empty route to \"neighbor\": " + pNeighborName);
 				}
 			}else{
 				Logging.warn(this, "Got as route to neighbor: " + tRoute); //HINT: this could also be a local loop -> throw only a warning				
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
 	private synchronized void registerNodeARG(ControlEntity pNode)
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
 	public void registerLinkARG(AbstractRoutingGraphNode pFrom, AbstractRoutingGraphNode pTo, AbstractRoutingGraphLink pLink)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK (ARG):  source=" + pFrom + " ## dest.=" + pTo + " ## link=" + pLink);
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			mAbstractRoutingGraph.link(pFrom, pTo, pLink);
 		}
 	}
 
 	/**
 	 * Unregisters a logical link between clusters/coordinators from the locally stored abstract routing graph (ARG)
 	 * 
 	 * @param pFrom the starting point of the link
 	 * @param pTo the ending point of the link
 	 */
 	public void unregisterLinkARG(AbstractRoutingGraphNode pFrom, AbstractRoutingGraphNode pTo)
 	{
 		AbstractRoutingGraphLink tLink = getLinkARG(pFrom, pTo);
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UNREGISTERING LINK (ARG):  source=" + pFrom + " ## dest.=" + pTo + " ## link=" + tLink);
 		}
 
 		if(tLink != null){
 			synchronized (mAbstractRoutingGraph) {
 				mAbstractRoutingGraph.unlink(tLink);
 			}
 		}
 	}
 
 	/**
 	 * Determines the link between two clusters/coordinators from the locally stored abstract routing graph (ARG)
 	 * 
 	 * @param pFrom the starting point of the link
 	 * @param pTo the ending point of the link
 	 * 
 	 * @return the link between the two nodes
 	 */
 	public AbstractRoutingGraphLink getLinkARG(AbstractRoutingGraphNode pFrom, AbstractRoutingGraphNode pTo)
 	{
 		AbstractRoutingGraphLink tResult = null;
 		
 		List<AbstractRoutingGraphLink> tRoute = null;
 		synchronized (mAbstractRoutingGraph) {
 			tRoute = mAbstractRoutingGraph.getRoute(pFrom, pTo);
 		}
 		
 		if((tRoute != null) && (!tRoute.isEmpty())){
 			if(tRoute.size() == 1){
 				tResult = tRoute.get(0);
 			}else{
 				Logging.warn(this, "getLinkARG() expected a route with one entry but got: \nSOURCE=" + pFrom + "\nDESTINATION: " + pTo + "\nROUTE: " + tRoute);
 			}
 		}
 		
 		return tResult;
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
 
 		/**
 		 * Query for neighbors stored in within the ARG
 		 */
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getVerticesInOrderRadius(pRootCluster);
 		}
 		
 		/**
 		 * Remove the root cluster
 		 */
 		tResult.remove(pRootCluster);
 		
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
 	 * Checks if a node is locally stored in the abstract routing graph
 	 * 
 	 * @param pNodeARG a possible node of the ARG
 	 * 
 	 * @return true or false
 	 */
 	public boolean isKnownARG(ControlEntity pNodeARG)
 	{
 		boolean tResult = false;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "IS KNOWN (ARG): \"" + pNodeARG + "\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.contains(pNodeARG);
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
 			/**
 			 * Create the communication session
 			 */
 			Logging.log(this, "     ..creating communication session");
 			ComSession tComSession = new ComSession(this, true);
 
 			/**
 			 * Start the communication session
 			 */					
 			Logging.log(this, "     ..starting communication session for the new connection");
 			tComSession.startConnection(null, pConnection);
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
 	
 	/**
 	 * Callback for ServerCallback: gets triggered if an error is caused by the server socket
 	 * 
 	 * @param the error cause
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.application.util.ServerCallback#error(de.tuilmenau.ics.fog.facade.events.ErrorEvent)
 	 */
 	@Override
 	public void error(ErrorEvent pCause)
 	{
 		Logging.log(this, "Got an error message because of \"" + pCause + "\"");
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
 	 * Determine the L2 address of the central FN of this node
 	 * 
 	 * @return the L2Address of the central FN
 	 */
 	public L2Address getNodeL2Address()
 	{
 		L2Address tResult = null;
 		
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) getNode().getLayer(FoGEntity.class);
 
 		if(tFoGLayer != null){
 			// get the central FN of this node
 			L2Address tThisHostL2Address = getHRS().getL2AddressFor(tFoGLayer.getCentralFN());
 			
 			tResult = tThisHostL2Address;
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * The global name space which is used to identify the HRM instances on nodes.
 	 */
 	public final static Namespace ROUTING_NAMESPACE = new Namespace("routing");
 
 	/**
 	 * Stores the identification string for HRM specific routing graph decorations (coordinators & HRMIDs)
 	 */
 	private final static String DECORATION_NAME_COORDINATORS_AND_HRMIDS = "HRM(1) - coordinators & HRMIDs";
 
 	/**
 	 * Stores the identification string for HRM specific routing graph decorations (node priorities)
 	 */
 	private final static String DECORATION_NAME_NODE_PRIORITIES = "HRM(3) - node priorities";
 	
 	/**
 	 * Stores the identification string for the active HRM infrastructure
 	 */
 	private final static String DECORATION_NAME_ACTIVE_HRM_INFRASTRUCTURE = "HRM(4) - active infrastructure";
 	
 	/**
 	 * Stores the identification string for HRM specific routing graph decorations (coordinators & clusters)
 	 */
 	private final static String DECORATION_NAME_COORDINATORS_AND_CLUSTERS = "HRM(2) - coordinators & clusters";
 }
