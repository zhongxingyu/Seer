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
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.Date;
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
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
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
 import edu.uci.ics.jung.graph.util.Pair;
 
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
 	 * Stores the node specific graph decorator for NMS entries
 	 */
 	private NodeDecorator mDecoratorForNMSEntries = null;
 	
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
 	private HRMControllerObservable mGUIInformer = new HRMControllerObservable(this);
 	
 	/**
 	 * Stores the HRG-GUI observable, which is used to notify possible HRG-GUI about changes within the HRG of the HRMController instance.
 	 */
 	private HRMControllerObservable mHRGGUIInformer = new HRMControllerObservable(this);
 	
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
 	 * Stores all former known Coordinator IDs
 	 */
 	private LinkedList<Long> mFormerLocalCoordinatorIDs = new LinkedList<Long>();
 	
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
 	 * Stores a database about all registered L0 cluster members (including Cluster objects).
 	 * This list is used for deriving connectivity data for the distribution of topology data.
 	 */
 	private LinkedList<ClusterMember> mLocalL0ClusterMembers = new LinkedList<ClusterMember>();
 
 	/**
 	 * Stores a database about all registered CoordinatorAsClusterMemeber instances.
 	 */
 	private LinkedList<CoordinatorAsClusterMember> mLocalCoordinatorAsClusterMemebers = new LinkedList<CoordinatorAsClusterMember>();
 	
 	/**
 	 * Stores a database about all registered comm. sessions.
 	 */
 	private LinkedList<ComSession> mCommunicationSessions = new LinkedList<ComSession>();
 	
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
 	 * Stores the amount of registered coordinators globally
 	 */
 	public static long sRegisteredCoordinators = 0;
 
 	/**
 	 * Stores a counter about registered coordinators per hierarchy level
 	 */
 	public static HashMap<Integer, Integer> sRegisteredCoordinatorsCounter = new HashMap<Integer, Integer>();
 
 	/**
 	 * Stores the amount of unregistered coordinators globally
 	 */
 	public static long sUnregisteredCoordinators = 0;
 
 	/**
 	 * Stores an abstract routing graph (ARG), which provides an abstract overview about logical links between clusters/coordinator.
 	 */
 	private AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> mAbstractRoutingGraph = new AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink>();
 	
 	/**
 	 * Stores the hierarchical routing graph (HRG), which provides a hierarchical overview about the network topology.
 	 */
 	private AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink> mHierarchicalRoutingGraph = new AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink>(true);
 
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
 	 * Stores the node priority per hierarchy level.
 	 * Level 0 isn't used here. (see "mNodeConnectivityPriority")
 	 */
 	private long mNodeHierarchyPriority[] = new long[HRMConfig.Hierarchy.HEIGHT + 2];
 	
 	/**
 	 * Stores the connectivity node priority
 	 */
 	private long mNodeConnectivityPriority = HRMConfig.Election.DEFAULT_PRIORITY;
 
 	/**
 	 * Stores the central node for the ARG
 	 */
 	private CentralNodeARG mCentralARGNode = null;
 
 	/**
 	 * Stores a description about all connectivity priority updates
 	 */
 	private String mDesriptionConnectivityPriorityUpdates = new String();
 
 	/**
 	 * Stores a description about all HRMID updates
 	 */
 	private String mDescriptionHRMIDUpdates = new String();
 
 	/**
 	 * Stores a description about all HRG updates
 	 */
 	private String mDescriptionHRGUpdates = new String();
 
 	/**
 	 * Stores a description about all hierarchy priority updates
 	 */
 	private String mDesriptionHierarchyPriorityUpdates = new String();
 	
 	/**
 	 * Stores the thread for clustering tasks and packet processing
 	 */
 	private HRMControllerProcessor mProcessorThread = null;
 	
 	/**
 	 * Stores a database about all known superior coordinators
 	 */
 	private LinkedList<ClusterName> mSuperiorCoordinators = new LinkedList<ClusterName>();
 	
 	/**
 	 * Stores a database about all known network interfaces of this node
 	 */
 	private LinkedList<NetworkInterface> mLocalNetworkInterfaces = new LinkedList<NetworkInterface>();
 	
 	/**
 	 * Stores a counter for the references per known network interface
 	 */
 	private HashMap<NetworkInterface, Integer> mLocalNetworkInterfacesRefCount = new HashMap<NetworkInterface, Integer>();
 	
 	/**
 	 * Stores the node-global election state
 	 */
 	private Object mNodeElectionState = null;
 	
 	/**
 	 * Stores the node-global election state change description
 	 */
 	private String mDescriptionNodeElectionState = new String();
 
 	/**
 	 * Stores if the GUI user has selected to deactivate topology reports.
 	 * This function is not part of the concept. It is only used for debugging purposes and measurement speedup.
 	 */
 	public static boolean GUI_USER_CTRL_REPORT_TOPOLOGY	= HRMConfig.Routing.REPORT_TOPOLOGY_AUTOMATICALLY;
 
 	/**
 	 * Stores if the GUI user has selected to deactivate address distribution.
 	 * This function is not part of the concept. It is only used for debugging purposes and measurement speedup.
 	 */
 	public static boolean GUI_USER_CTRL_ADDRESS_DISTRUTION = HRMConfig.Addressing.ASSIGN_AUTOMATICALLY;
 	
 	/**
 	 * Stores if the GUI user has selected to deactivate topology reports.
 	 * This function is not part of the concept. It is only used for debugging purposes and measurement speedup.
 	 */
 	public static boolean GUI_USER_CTRL_SHARE_ROUTES = HRMConfig.Routing.SHARE_ROUTES_AUTOMATICALLY;
 
 	/**
 	 * Stores if the GUI user has selected to deactivate announcements.
 	 * This function is not part of the concept. It is only used for debugging purposes and measurement speedup.
 	 */
 	public static boolean GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS = true;
 
 	/**
 	 * Stores the simulation time of the last AnnounceCoordinator, which had impact on the current hierarchy structure
 	 * This value is not part of the concept. It is only used for debugging purposes and measurement speedup. 
 	 */
 	private static double mSimulationTimeOfLastCoordinatorAnnouncementWithImpact = 0;
 	
 	/**
 	 * Stores if the simulation was restarted and the global NMS should be reset
 	 */
 	private static boolean sResetNMS = false;
 	
 	/**
 	 * @param pAS the autonomous system at which this HRMController is instantiated
 	 * @param pNode the node on which this controller was started
 	 * @param pHRS is the hierarchical routing service that should be used
 	 */
 	public HRMController(AutonomousSystem pAS, Node pNode, HRMRoutingService pHierarchicalRoutingService)
 	{
 		// initialize the application context
 		super(pNode, null, pNode.getIdentity());
 
 		/**
 		 * Reset FoGSiEm configuration
 		 */
 		GUI_USER_CTRL_REPORT_TOPOLOGY	= HRMConfig.Routing.REPORT_TOPOLOGY_AUTOMATICALLY;
 		GUI_USER_CTRL_SHARE_ROUTES = HRMConfig.Routing.SHARE_ROUTES_AUTOMATICALLY;
 		GUI_USER_CTRL_ADDRESS_DISTRUTION = HRMConfig.Addressing.ASSIGN_AUTOMATICALLY;
 		resetAnnounceCoordinatorGUI();
 		
 		// define the local name "routing://"
 		mApplicationName = new SimpleName(ROUTING_NAMESPACE, null);
 
 		// reference to the physical node
 		mNode = pNode;
 		
 		// reference to the AutonomousSystem object 
 		mAS = pAS;
 		
 		// set the node-global election state
 		mNodeElectionState = Elector.createNodeElectionState();
 		
 		/**
 		 * Create the node specific decorator for HRM coordinators and HRMIDs
 		 */
 		mDecoratorForCoordinatorsAndHRMIDs = new NodeDecorator();
 		
 		/**
 		 * Create the node specific decorator for HRM coordinators and clusters
 		 */
 		mDecoratorForCoordinatorsAndClusters = new NodeDecorator();
 		
 		/**
 		 * Create the node specific decorator for NMS entries
 		 */
 		mDecoratorForNMSEntries = new NodeDecorator();
 				
 		/**
 		 * Create the node specific decorator for HRM node priorities
 		 */
 		mDecoratorForNodePriorities = new NodeDecorator();
 		
 		/**
 		 * Create the node specific decorator for the active HRM infrastructure
 		 */
 		mDecoratorActiveHRMInfrastructure = new NodeDecorator();
 		
 		/**
 		 * Initialize the node hierarchy priority
 		 */
 		for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			mNodeHierarchyPriority[i] = i;//HRMConfig.Election.DEFAULT_BULLY_PRIORITY;
 		}
 		
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
 		// create own decoration for NMS entries
 		tDecoration = Decoration.getInstance(DECORATION_NAME_NMS_ENTRIES);
 		tDecoration.setDecorator(mNode,  mDecoratorForNMSEntries);		
 		// create own decoration for HRM node priorities
 		tDecoration = Decoration.getInstance(DECORATION_NAME_NODE_PRIORITIES);
 		tDecoration.setDecorator(mNode,  mDecoratorForNodePriorities);
 		// create own decoration for HRM node priorities
 		tDecoration = Decoration.getInstance(DECORATION_NAME_ACTIVE_HRM_INFRASTRUCTURE);
 		tDecoration.setDecorator(mNode,  mDecoratorActiveHRMInfrastructure);
 		// overwrite default decoration
 		tDecoration = Decoration.getInstance(GraphViewer.DEFAULT_DECORATION);
 		tDecoration.setDecorator(mNode,  mDecoratorForNMSEntries);
 		
 		/**
 		 * Create clusterer thread
 		 */
 		mProcessorThread = new HRMControllerProcessor(this);
 		/**
 		 * Start the clusterer thread
 		 */
 		mProcessorThread.start();
 
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
 	 * Reset the AnnounceCoordinator handling.
 	 * This function is not part of the concept. It is only useful for debugging purposes and user control. 
 	 */
 	public void resetAnnounceCoordinatorGUI()
 	{
 		Logging.log(this, "##### Reseting AnnounceCoordinator mechanism");
 		GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS = true;
 		mSimulationTimeOfLastCoordinatorAnnouncementWithImpact = 0;
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
 	 * Notifies the HRGViewer about essential updates within the HRG graph
 	 */
 	private void notifyHRGGUI(Object pArgument)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Got HRG notification with argument " + pArgument);
 		}
 		
 		mHRGGUIInformer.notifyObservers(pArgument);
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
 	 * Registers a HRG-GUI for being notified about HRG internal changes. 
 	 */
 	public void registerHRGGUI(Observer pHRGGUI)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Registering HRG-GUI " + pHRGGUI);
 		}
 		mHRGGUIInformer.addObserver(pHRGGUI);
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
 	 * Unregisters a HRG-GUI for being notified about HRG internal changes. 
 	 */
 	public void unregisterHRGGUI(Observer pHRGGUI)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Unregistering HRG-GUI " + pHRGGUI);
 		}
 		mHRGGUIInformer.deleteObserver(pHRGGUI);
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
 
 		// increase hierarchy node priority
 		increaseHierarchyNodePriority_KnownCoordinator(pCoordinatorProxy);
 
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
 		
 		// increase hierarchy node priority
 		decreaseHierarchyNodePriority_KnownCoordinator(pCoordinatorProxy);
 
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
 		if((!pCoordinator.getHierarchyLevel().isBaseLevel()) && (tFoundAnInferiorCoordinator == null)){
 			Logging.err(this, "Hierarchy is temporary non continuous, detected an error in the Matrix!?");
 			Logging.err(this, "    ..registered a coordinator at hierarchy level: " + pCoordinator.getHierarchyLevel().getValue() + " and haven't found a coordinator at level: " + (pCoordinator.getHierarchyLevel().getValue() - 1));
 		}
 			
 		synchronized (mLocalCoordinators) {
 			// register as known coordinator
 			mLocalCoordinators.add(pCoordinator);
 		}
 		
 		sRegisteredCoordinators++;
 		
 		Integer tCounter = sRegisteredCoordinatorsCounter.get(pCoordinator.getHierarchyLevel().getValue());
 		if(tCounter == null){
 			tCounter = new Integer(1);
 		}else{
 			tCounter++;
 		}
 		sRegisteredCoordinatorsCounter.put(pCoordinator.getHierarchyLevel().getValue(), tCounter);
 		
 		// increase hierarchy node priority
 		increaseHierarchyNodePriority_KnownCoordinator(pCoordinator);
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// register the coordinator in the local ARG
 		registerNodeARG(pCoordinator);
 		registerLinkARG(pCoordinator, pCoordinator.getCluster(), new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.OBJECT_REF));
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinator);
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
 			
 			synchronized (mFormerLocalCoordinatorIDs) {
 				mFormerLocalCoordinatorIDs.add(pCoordinator.getGUICoordinatorID());	
 			}
 		}
 
 		Integer tCounter = sRegisteredCoordinatorsCounter.get(pCoordinator.getHierarchyLevel().getValue());
 		if(tCounter != null){
 			tCounter--;
 			sRegisteredCoordinatorsCounter.put(pCoordinator.getHierarchyLevel().getValue(), tCounter);
 		}else{
 			Logging.err(this, "Found an invalid counter about registered coordinators on hierarchy level: " + pCoordinator.getHierarchyLevel().getValue());
 		}
 
 		sUnregisteredCoordinators++;
 
 		// increase hierarchy node priority
 		decreaseHierarchyNodePriority_KnownCoordinator(pCoordinator);
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 		
 		// unregister from the ARG
 		unregisterNodeARG(pCoordinator);
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinator);
 	}
 	
 	/**
 	 * Registers an HRMID at local database
 	 * 
 	 * @param pEntity the entity for which the HRMID should be registered
 	 * @param pCause the cause for the registration
 	 */
 	private void registerHRMID(ControlEntity pEntity, String pCause)
 	{
 		/**
 		 * Get the new HRMID
 		 */
 		HRMID tHRMID = pEntity.getHRMID();
 		
 		if((tHRMID != null) && (!tHRMID.isZero())){
 			registerHRMID(pEntity, tHRMID, pCause);
 		}
 	}
 	
 	/**
 	 * Registers an HRMID at local database
 	 * 
 	 * @param pEntity the entity for which the HRMID should be registered
 	 * @param pHRMID the new HRMID
 	 * @param pCause the cause for the registration
 	 */
 	@SuppressWarnings("unchecked")
 	public void registerHRMID(ControlEntity pEntity, HRMID pHRMID, String pCause)
 	{
 		/**
 		 * Some validations
 		 */
 		if(pHRMID != null){
 			// ignore "0.0.0"
 			if(!pHRMID.isZero()){
 				/**
 				 * Register the HRMID
 				 */
 				synchronized(mRegisteredOwnHRMIDs){
 					if (!mRegisteredOwnHRMIDs.contains(pHRMID)){
 						/**
 						 * Update the local address DB with the given HRMID
 						 */
 						if(!pHRMID.isClusterAddress()){
 							/**
 							 * Register a local loopback route for the new address 
 							 */
 							// register a route to the cluster member as addressable target
 							addHRMRoute(RoutingEntry.createLocalhostEntry(pHRMID, pCause + ", " + this + "::registerHRMID()"));
 
 							/**
 							 * Update the DNS
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
 							Logging.log(this, "Registering NMS entry: " + tLocalRouterName + " => " + pHRMID);
 							tNMS.registerName(tLocalRouterName, pHRMID, NamingLevel.NAMES);				
 							// give some debug output about the current DNS state
 							String tString = new String();
 							for(NameMappingEntry<HRMID> tEntry : tNMS.getAddresses(tLocalRouterName)) {
 								if (!tString.isEmpty()){
 									tString += ", ";
 								}
 								tString += tEntry;
 							}
 							Logging.log(this, "HRM router " + tLocalRouterName + " is now known under: " + tString);
 						}
 					}else{
 						if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 							Logging.warn(this, "Found a HRMID duplicate for " + pHRMID.toString() + ", additional registration is triggered by " + pEntity);
 						}
 					}
 
 					/**
 					 * Add the HRMID
 					 */
 					if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 						Logging.log(this, "Adding the HRMID to: " + pHRMID.toString() + " for: " + pEntity);
 					}
 					// register the new HRMID as local one -> allow duplicates here because two local entities might register the same HRMID and afterwards one of them unregisters its HRMID -> in case one HRMID registration remains!
 					mRegisteredOwnHRMIDs.add(pHRMID);
 					mDescriptionHRMIDUpdates += "\n + " + pHRMID.toString() + " <== " + pEntity + ", cause=" + pCause;
 
 					/**
 					 * Update the GUI
 					 */
 					// updates the GUI decoration for this node
 					updateGUINodeDecoration();
 					// it's time to update the GUI
 					notifyGUI(pEntity);
 				}
 			}else{
 				throw new RuntimeException(this + "registerHRMID() got a zero HRMID " + pHRMID.toString() + " for: " + pEntity);
 			}
 		}else{
 			Logging.err(this, "registerHRMID() got an invalid HRMID for: " + pEntity);
 		}
 	}
 	
 	/**
 	 * Unregisters an HRMID at local database
 	 * 
 	 * @param pEntity the entity for which the HRMID should be registered
 	 * @param pOldHRMID the old HRMID which should be unregistered
 	 * @param pCause the cause for this call
 	 */
 	public void unregisterHRMID(ControlEntity pEntity, HRMID pOldHRMID, String pCause)
 	{
 		/**
 		 * Some validations
 		 */
 		if(pOldHRMID != null){
 			// ignore "0.0.0"
 			if(!pOldHRMID.isZero()){
 				/**
 				 * Unregister the HRMID
 				 */
 				synchronized(mRegisteredOwnHRMIDs){
 
 					/**
 					 * Remove the HRMID
 					 */
 					if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 						Logging.log(this, "Revoking the HRMID: " + pOldHRMID.toString() + " of: " + pEntity);
 					}
 					// unregister the HRMID as local one
 					mRegisteredOwnHRMIDs.remove(pOldHRMID);
 					mDescriptionHRMIDUpdates += "\n - " + pOldHRMID.toString() + " <== " + pEntity + ", cause=" + pCause;
 					
 					if (!mRegisteredOwnHRMIDs.contains(pOldHRMID)){
 						/**
 						 * Update the local address DB with the given HRMID
 						 */
 						if(!pOldHRMID.isClusterAddress()){
 							/**
 							 * Unregister the local loopback route for the address 
 							 */
 							// unregister a route to the cluster member as addressable target
 							delHRMRoute(RoutingEntry.createLocalhostEntry(pOldHRMID, pCause + ", " + this + "::unregisterHRMID()"));
 			
 							/**
 							 * Update the DNS
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
 							Logging.log(this, "Unregistering NMS entry: " + tLocalRouterName + " => " + pOldHRMID);
 							tNMS.unregisterName(tLocalRouterName, pOldHRMID);				
 							// give some debug output about the current DNS state
 							String tString = new String();
 							for(NameMappingEntry<HRMID> tEntry : tNMS.getAddresses(tLocalRouterName)) {
 								if (!tString.isEmpty()){
 									tString += ", ";
 								}
 								tString += tEntry;
 							}
 							Logging.log(this, "HRM router " + tLocalRouterName + " is now known under: " + tString);
 						}
 					}else{
 						if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 							Logging.warn(this, "Found duplicated HRMID " + pOldHRMID.toString() + ", an unregistration is triggered by " + pEntity);
 						}
 					}
 
 					/**
 					 * Update the GUI
 					 */
 					// updates the GUI decoration for this node
 					updateGUINodeDecoration();
 					// it's time to update the GUI
 					notifyGUI(pEntity);
 				}
 			}else{
 				throw new RuntimeException(this + "unregisterHRMID() got a zero HRMID " + pOldHRMID.toString() + " for: " + pEntity);
 			}
 		}else{
 			Logging.err(this, "unregisterHRMID() got an invalid HRMID for: " + pEntity);
 		}
 	}
 	
 	/**
 	 * Updates the registered HRMID for a defined coordinator.
 	 * 
 	 * @param pCluster the cluster whose HRMID is updated
 	 * @param pOldHRMID the old HRMID which should be unregistered
 	 */
 	private int mCallsUpdateCoordinatorAddress = 0;
 	public void updateCoordinatorAddress(Coordinator pCoordinator, HRMID pOldHRMID)
 	{
 		mCallsUpdateCoordinatorAddress++;
 		
 		HRMID tHRMID = pCoordinator.getHRMID();
 		if((pOldHRMID == null) || (!pOldHRMID.equals(tHRMID))){
 			/**
 			 * Unregister old
 			 */
 			if((pOldHRMID != null) && (!pOldHRMID.isZero())){
 				unregisterHRMID(pCoordinator, pOldHRMID, "updateCoordinatorAddress()(" + mCallsUpdateCoordinatorAddress + ") for " + pCoordinator + ", old HRMID=" + pOldHRMID);
 			}
 			
 			/**
 			 * Register new
 			 */
 			Logging.log(this, "Updating address from " + pOldHRMID + " to " + (tHRMID != null ? tHRMID.toString() : "null") + " for Coordinator " + pCoordinator + ", old HRMID=" + pOldHRMID);
 			registerHRMID(pCoordinator, "updateCoordinatorAddress()(" + mCallsUpdateCoordinatorAddress + ") for " + pCoordinator);
 		}
 	}
 
 	/**
 	 * Returns if a coordinator ID is a formerly known one
 	 *  
 	 * @param pCoordinatorID the coordinator ID
 	 * 
 	 * @return true or false
 	 */
 	public boolean isGUIFormerCoordiantorID(long pCoordinatorID)
 	{
 		boolean tResult = false;
 		
 		synchronized (mFormerLocalCoordinatorIDs) {
 			tResult = mFormerLocalCoordinatorIDs.contains(pCoordinatorID);	
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Revokes a coordinator address
 	 * 
 	 * @param pCoordinator the coordinator for which the address is revoked
 	 * @param pOldHRMID the old HRMID which should be unregistered
 	 */
 	public void revokeCoordinatorAddress(Coordinator pCoordinator, HRMID pOldHRMID)
 	{
 		if((pOldHRMID != null) && (!pOldHRMID.isZero())){
 			Logging.log(this, "Revoking address " + pOldHRMID.toString() + " for coordinator " + pCoordinator);
 	
 			unregisterHRMID(pCoordinator, pOldHRMID, "revokeCoordinatorAddress()");
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
 		mDecoratorActiveHRMInfrastructure.setText("- [Active clusters: " + tActiveHRMInfrastructureText + "]");
 		String tHierPrio = "";
 		for(int i = 1; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			if (tHierPrio != ""){
 				tHierPrio += ", ";
 			}
 			tHierPrio += Long.toString(mNodeHierarchyPriority[i]) +"@" + i;
 		}
 		mDecoratorForNodePriorities.setText(" [Hier.: " + tHierPrio + "/ Conn.: " + Long.toString(getConnectivityNodePriority()) + "]");
 		
 		String tNodeText = "";
 		synchronized (mRegisteredOwnHRMIDs) {
 			for (HRMID tHRMID: mRegisteredOwnHRMIDs){
 				if (((!tHRMID.isRelativeAddress()) || (HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES)) && ((!tHRMID.isClusterAddress()) || (HRMConfig.DebugOutput.GUI_SHOW_CLUSTER_ADDRESSES))){
 					if (tNodeText != ""){
 						tNodeText += ", ";
 					}
 					tNodeText += tHRMID.toString();
 				}
 			}			
 		}
 		mDecoratorForCoordinatorsAndHRMIDs.setText("- " + tNodeText);
 		
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
 		
 		NameMappingService tNMS = null;
 		try {
 			tNMS = HierarchicalNameMappingService.getGlobalNameMappingService(mNode.getAS().getSimulation());
 		} catch (RuntimeException tExc) {
 			tNMS = HierarchicalNameMappingService.createGlobalNameMappingService(mNode.getAS().getSimulation());
 		}
 		String tRegisterNMSEntriesText = "";
 		try {
 			for(NameMappingEntry<?> tNMSEntry : tNMS.getAddresses(getNodeName())) {
 				if(tNMSEntry.getAddress() instanceof HRMID) {
 					// get the HRMID of the target node
 					HRMID tNodeHRMID = (HRMID)tNMSEntry.getAddress();
 					
 					if(tRegisterNMSEntriesText != "")
 						tRegisterNMSEntriesText += ", ";
 					tRegisterNMSEntriesText += tNodeHRMID.toString();
 				}
 			}
 		} catch (RemoteException e) {
 		}
 		mDecoratorForNMSEntries.setText("- " + tRegisterNMSEntriesText);
 
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
 		mDecoratorForNMSEntries.setImage(tHighestCoordinatorLevel);
 	}
 
 	/**
 	 * Returns a list of all known network interfaces
 	 * 
 	 * @return the list of known network interfaces
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<NetworkInterface> getAllNetworkInterfaces()
 	{
 		LinkedList<NetworkInterface> tResult = null;
 
 		synchronized (mLocalNetworkInterfaces) {
 			tResult = (LinkedList<NetworkInterface>) mLocalNetworkInterfaces.clone();
 		}
 		
 		return tResult;
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
 		
 		boolean tNewEntry = false;
 		synchronized (mLocalCoordinatorAsClusterMemebers) {
 			// make sure the Election priority is the right one, avoid race conditions here
 			pCoordinatorAsClusterMember.setPriority(ElectionPriority.create(this, getHierarchyNodePriority(pCoordinatorAsClusterMember.getHierarchyLevel())));
 
 			if(!mLocalCoordinatorAsClusterMemebers.contains(pCoordinatorAsClusterMember)){				
 				// register as known coordinator-as-cluster-member
 				mLocalCoordinatorAsClusterMemebers.add(pCoordinatorAsClusterMember);
 				
 				tNewEntry = true;
 			}else{
 				Logging.err(this, "CoordinatorAsClusterMember already known: " + pCoordinatorAsClusterMember);
 			}
 		}
 		
 		if(tNewEntry){
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
 	}
 	
 	/**
 	 * Unregister a coordinator-as-cluster-member from the local database
 	 * 
 	 * @param pCoordinatorAsClusterMember the coordinator-as-cluster-member which should be unregistered
 	 */
 	public synchronized void unregisterCoordinatorAsClusterMember(CoordinatorAsClusterMember pCoordinatorAsClusterMember)
 	{
 		Logging.log(this, "Unregistering coordinator-as-cluster-member " + pCoordinatorAsClusterMember);
 
 		boolean tFoundEntry = false;
 		synchronized (mLocalCoordinatorAsClusterMemebers) {
 			if(mLocalCoordinatorAsClusterMemebers.contains(pCoordinatorAsClusterMember)){				
 				// unregister the old HRMID
 				revokeClusterMemberAddress(pCoordinatorAsClusterMember, pCoordinatorAsClusterMember.getHRMID());
 
 				// unregister from list of known cluster members
 				mLocalCoordinatorAsClusterMemebers.remove(pCoordinatorAsClusterMember);
 				
 				Logging.log(this, "    ..unregistered: " + pCoordinatorAsClusterMember);
 			}else{
 				Logging.log(this, "    ..not found: " + pCoordinatorAsClusterMember);
 			}
 		}
 
 		if(tFoundEntry){
 			if(HRMConfig.DebugOutput.GUI_SHOW_COORDINATOR_CLUSTER_MEMBERS_IN_ARG){
 				// updates the GUI decoration for this node
 				updateGUINodeDecoration();
 		
 				// register at the ARG
 				unregisterNodeARG(pCoordinatorAsClusterMember);
 		
 				// it's time to update the GUI
 				notifyGUI(pCoordinatorAsClusterMember);
 			}
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
 		
 		boolean tNewEntry = false;
 		synchronized (mLocalClusterMembers) {
 
 			// make sure the Election priority is the right one, avoid race conditions here
 			pClusterMember.setPriority(ElectionPriority.create(this, getConnectivityNodePriority()));
 
 			if(!mLocalClusterMembers.contains(pClusterMember)){
 				// register as known cluster member
 				mLocalClusterMembers.add(pClusterMember);
 				
 				tNewEntry = true;
 			}
 		}
 
 		/**
 		 * Register as L0 ClusterMember
 		 */
 		if(pClusterMember.getHierarchyLevel().isBaseLevel()){
 			synchronized (mLocalL0ClusterMembers) {
 				if(!mLocalL0ClusterMembers.contains(pClusterMember)){
 					// register as known cluster member
 					mLocalL0ClusterMembers.add(pClusterMember);
 				}
 			}
 		}
 		
 		if(tNewEntry){
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
 	}
 
 	/**
 	 * Unregister a cluster member from the local database
 	 * 
 	 * @param pClusterMember the cluster member which should be unregistered
 	 */
 	public synchronized void unregisterClusterMember(ClusterMember pClusterMember)
 	{
 		Logging.log(this, "Unregistering cluster member " + pClusterMember);
 
 		boolean tFoundEntry = false;
 		synchronized (mLocalClusterMembers) {
 			if(mLocalClusterMembers.contains(pClusterMember)){
 				// unregister the old HRMID
 				revokeClusterMemberAddress(pClusterMember, pClusterMember.getHRMID());
 				
 				// unregister from list of known cluster members
 				mLocalClusterMembers.remove(pClusterMember);
 				
 				tFoundEntry = true;
 			}
 		}
 
 		/**
 		 * Unregister as L0 ClusterMember
 		 */
 		if(pClusterMember.getHierarchyLevel().isBaseLevel()){
 			synchronized (mLocalL0ClusterMembers) {
 				if(mLocalL0ClusterMembers.contains(pClusterMember)){
 					// register as known cluster member
 					mLocalL0ClusterMembers.remove(pClusterMember);
 				}
 			}
 		}
 
 		if(tFoundEntry){
 			// updates the GUI decoration for this node
 			updateGUINodeDecoration();
 	
 			// register at the ARG
 			unregisterNodeARG(pClusterMember);
 	
 			// it's time to update the GUI
 			notifyGUI(pClusterMember);
 		}
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
 		
 		/**
 		 * Register as L0 ClusterMember
 		 */
 		if(pCluster.getHierarchyLevel().isBaseLevel()){
 			synchronized (mLocalL0ClusterMembers) {
 				if(!mLocalL0ClusterMembers.contains(pCluster)){
 					// register as known cluster member
 					mLocalL0ClusterMembers.add(pCluster);
 				}
 			}
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
 			// unregister the old HRMID
 			revokeClusterAddress(pCluster, pCluster.getHRMID());
 
 			// unregister from list of known clusters
 			mLocalClusters.remove(pCluster);
 		}
 		
 		synchronized (mLocalClusterMembers) {
 			// unregister from list of known cluster members
 			mLocalClusterMembers.remove(pCluster);
 		}
 
 		/**
 		 * Unregister as L0 ClusterMember
 		 */
 		if(pCluster.getHierarchyLevel().isBaseLevel()){
 			synchronized (mLocalL0ClusterMembers) {
 				if(mLocalL0ClusterMembers.contains(pCluster)){
 					// register as known cluster member
 					mLocalL0ClusterMembers.remove(pCluster);
 				}
 			}
 		}
 
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
 
 		// register at the ARG
 		unregisterNodeARG(pCluster);
 
 		// it's time to update the GUI
 		notifyGUI(pCluster);
 	}
 	
 	/**
 	 * Updates the registered HRMID for a defined Cluster.
 	 * 
 	 * @param pCluster the Cluster whose HRMID is updated
 	 * @param pOldHRMID the old HRMID
 	 */
 	public void updateClusterAddress(Cluster pCluster, HRMID pOldHRMID)
 	{
 		HRMID tHRMID = pCluster.getHRMID();
 		if((pOldHRMID == null) || (!pOldHRMID.equals(tHRMID))){
 			/**
 			 * Unregister old
 			 */
 			if((pOldHRMID != null) && (!pOldHRMID.isZero())){
 				unregisterHRMID(pCluster, pOldHRMID, "updateClusterAddress() for " + pCluster);
 			}
 			
 			/**
 			 * Register new
 			 */
 			Logging.log(this, "Updating address from " + pOldHRMID + " to " + (tHRMID != null ? tHRMID.toString() : "null") + " for Cluster " + pCluster);
 			registerHRMID(pCluster, "updateClusterAddress() for " + pCluster);
 		}
 	}
 
 	/**
 	 * Updates the registered HRMID for a defined ClusterMember.
 	 * 
 	 * @param pClusterMember the ClusterMember whose HRMID is updated
 	 * @param pOldHRMID the old HRMID which should be unregistered
 	 */
 	public void updateClusterMemberAddress(ClusterMember pClusterMember, HRMID pOldHRMID)
 	{
 		HRMID tHRMID = pClusterMember.getHRMID();
 		if((pOldHRMID == null) || (!pOldHRMID.equals(tHRMID))){
 			/**
 			 * Unregister old
 			 */
 			if((pOldHRMID != null) && (!pOldHRMID.isZero())){
 				unregisterHRMID(pClusterMember, pOldHRMID, "updateClusterMemberAddress() for " + pClusterMember + ", old HRMID=" + pOldHRMID);
 			}
 			
 			/**
 			 * Register new
 			 */
 			Logging.log(this, "Updating address from " + (pOldHRMID != null ? pOldHRMID.toString() : "null") + " to " + (tHRMID != null ? tHRMID.toString() : "null") + " for ClusterMember " + pClusterMember + ", old HRMID=" + pOldHRMID);
 	
 			// process this only if we are at base hierarchy level, otherwise we will receive the same update from 
 			// the corresponding coordinator instance
 			if (pClusterMember.getHierarchyLevel().isBaseLevel()){
 				registerHRMID(pClusterMember, "updateClusterMemberAddress() for " + pClusterMember + ", old HRMID=" + pOldHRMID);
 			}else{
 				// we are at a higher hierarchy level and don't need the HRMID update because we got the same from the corresponding coordinator instance
 				if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.warn(this, "Skipping HRMID registration " + (tHRMID != null ? tHRMID.toString() : "null") + " for " + pClusterMember + ", old HRMID=" + pOldHRMID);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Revokes a cluster address
 	 * 
 	 * @param pClusterMember the ClusterMember for which the address is revoked
 	 * @param pOldHRMID the old HRMID which should be unregistered
 	 */
 	public void revokeClusterMemberAddress(ClusterMember pClusterMember, HRMID pOldHRMID)
 	{
 		if((pOldHRMID != null) && (!pOldHRMID.isZero())){
 			Logging.log(this, "Revoking address " + pOldHRMID.toString() + " for ClusterMember " + pClusterMember);
 	
 			if (pClusterMember.getHierarchyLevel().isBaseLevel()){
 				unregisterHRMID(pClusterMember, pOldHRMID, "revokeClusterMemberAddress()");
 			}else{
 				// we are at a higher hierarchy level and don't need the HRMID revocation
 				if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.warn(this, "Skipping HRMID revocation of " + pOldHRMID.toString() + " for " + pClusterMember);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Revokes a cluster address
 	 * 
 	 * @param pCluster the Cluster for which the address is revoked
 	 * @param pOldHRMID the old HRMID which should be unregistered
 	 */
 	public void revokeClusterAddress(Cluster pCluster, HRMID pOldHRMID)
 	{
 		if((pOldHRMID != null) && (!pOldHRMID.isZero())){
 			Logging.log(this, "Revoking address " + pOldHRMID.toString() + " for Cluster " + pCluster);
 	
 			if (pCluster.getHierarchyLevel().isBaseLevel()){
 				unregisterHRMID(pCluster, pOldHRMID, "revokeClusterAddress()");
 			}else{
 				// we are at a higher hierarchy level and don't need the HRMID revocation
 				if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.warn(this, "Skipping HRMID revocation of " + pOldHRMID.toString() + " for " + pCluster);
 				}
 			}
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
 	@SuppressWarnings("unchecked")
 	public LinkedList<CoordinatorAsClusterMember> getAllCoordinatorAsClusterMembers()
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
 	 * Returns a list of known L0 cluster members.
 	 * 
 	 * @return the list of known L0 cluster members
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ClusterMember> getAllL0ClusterMembers()
 	{
 		LinkedList<ClusterMember> tResult = null;
 		
 		synchronized (mLocalL0ClusterMembers) {
 			tResult = (LinkedList<ClusterMember>) mLocalL0ClusterMembers.clone();
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
 	 * Returns a list of known CoordinatorAsClusterMember for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 * 
 	 * @return the list of CoordinatorAsClusterMember
 	 */
 	public LinkedList<CoordinatorAsClusterMember> getAllCoordinatorAsClusterMembers(int pHierarchyLevel)
 	{
 		LinkedList<CoordinatorAsClusterMember> tResult = new LinkedList<CoordinatorAsClusterMember>();
 		
 		// get a list of all known coordinators
 		LinkedList<CoordinatorAsClusterMember> tAllCoordinatorAsClusterMembers = getAllCoordinatorAsClusterMembers();
 		
 		// iterate over all known coordinators
 		for (CoordinatorAsClusterMember tCoordinatorAsClusterMember : tAllCoordinatorAsClusterMembers){
 			// have we found a matching coordinator?
 			if (tCoordinatorAsClusterMember.getHierarchyLevel().getValue() == pHierarchyLevel){
 				// add this coordinator to the result
 				tResult.add(tCoordinatorAsClusterMember);
 			}
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
 	private Coordinator getCoordinator(int pHierarchyLevelValue)
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
 	 * Returns a locally known Coordinator object for a given hierarchy level.
 	 * HINT: For base hierarchy level, there could exist more than one local coordinator!
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
 	public Coordinator getCoordinatorByID(long pCoordinatorID)
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
 			Logging.log(this, "CLUSTERING REQUEST for hierarchy level: " + pHierarchyLevel.getValue() + ", cause=" + pCause);
 			if(mProcessorThread != null){
 				mProcessorThread.eventUpdateCluster(pCause, pHierarchyLevel);
 			}
 		}
 	}
 	
 	/**
 	 * Notifies packet processor about a new packet
 	 * 
 	 * @param pComChannel the comm. channel which has a new received packet
 	 */
 	public void notifyPacketProcessor(ComChannel pComChannel)
 	{
 		if(mProcessorThread != null){
 			mProcessorThread.eventReceivedPacket(pComChannel);
 		}
 	}
 
 	/**
 	 * Registers an outgoing communication session
 	 * 
 	 * @param pComSession the new session
 	 */
 	public void registerSession(ComSession pComSession)
 	{
 		Logging.log(this, "Registering communication session: " + pComSession);
 		
 		synchronized (mCommunicationSessions) {
 			mCommunicationSessions.add(pComSession);
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
 		boolean DEBUG = false;
 		
 		// is the destination valid?
 		if (pDestinationL2Address != null){
 			//Logging.log(this, "Searching for outgoing comm. session to: " + pDestinationL2Address);
 			synchronized (mCommunicationSessions) {
 				for (ComSession tComSession : mCommunicationSessions){
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
 				if(DEBUG){
 					Logging.log(this, "getCreateComSession() could find a comm. session for destination: " + pDestinationL2Address + ", knowing these sessions and their channels:");
 					synchronized (mCommunicationSessions) {
 						for (ComSession tComSession : mCommunicationSessions){
 							Logging.log(this, "   ..ComSession: " + tComSession);
 							for(ComChannel tComChannel : tComSession.getAllComChannels()){
 								Logging.log(this, "     ..ComChannel: " + tComChannel);
 								Logging.log(this, "        ..RemoteCluster: " + tComChannel.getRemoteClusterName().toString());
 							}
 						}
 					}
 				}
 
 				/**
 				 * Create the new connection
 				 */
 				if(DEBUG){
 					Logging.log(this, "   ..creating new connection and session to: " + pDestinationL2Address);
 				}
 				tResult = createComSession(pDestinationL2Address);
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
 	private ComSession createComSession(L2Address pDestinationL2Address)
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
 	    ComSession tComSession = new ComSession(this);
 		
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
 	public void unregisterSession(ComSession pComSession)
 	{
 		Logging.log(this, "Unregistering outgoing communication session: " + pComSession);
 		
 		synchronized (mCommunicationSessions) {
 			mCommunicationSessions.remove(pComSession);
 		}
 	}
 	
 	/**
 	 * Returns the list of registered own HRMIDs which can be used to address the physical node on which this instance is running.
 	 *  
 	 * @return the list of HRMIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getHRMIDs()
 	{
 		LinkedList<HRMID> tResult = null;
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			tResult = (LinkedList<HRMID>) mRegisteredOwnHRMIDs.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns true if the given HRMID is a local one.
 	 * 
 	 * @param pHRMID the HRMID
 	 * 
 	 * @return true if the given HRMID is a local one
 	 */
 	private boolean isLocal(HRMID pHRMID)
 	{
 		boolean tResult = false;
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			for(HRMID tKnownHRMID : mRegisteredOwnHRMIDs){
 				if(tKnownHRMID.equals(pHRMID)){
 					tResult = true;
 					break;
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns true if the local node belongs to the given Cluster 
 	 * 
 	 * @param pHRMID the HRMID of the Cluster
 	 * 
 	 * @return true if the local node belongs to the given Cluster
 	 */
 	public boolean isLocalCluster(HRMID pClusterHRMID)
 	{
 		boolean tResult = false;
 
 		if(!pClusterHRMID.isClusterAddress()){
 			pClusterHRMID.setLevelAddress(0, 0);
 		}
 
 		synchronized(mRegisteredOwnHRMIDs){
 			for(HRMID tKnownHRMID : mRegisteredOwnHRMIDs){
 				//Logging.err(this, "Checking isCluster for " + tKnownHRMID + " and if it is " + pHRMID);
 				if(tKnownHRMID.isCluster(pClusterHRMID)){
 					//Logging.err(this, " ..true");
 					tResult = true;
 					break;
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the local sender address for a route with a given next hop
 	 * 
 	 * @param pSource the given source
 	 * @param pNextHop the given next hop
 	 * 
 	 * @return the local sender address
 	 */
 	private HRMID getLocalSenderAddress(HRMID pSource, HRMID pNextHop)
 	{
 		HRMID tResult = pSource;
 		
 		// figure out the L0 cluster
 		HRMID tNextHopL0Cluster = pNextHop;
 		tNextHopL0Cluster.setLevelAddress(0, 0);
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			// iterate over all local HRMIDs
 			for(HRMID tLocalHRMID : mRegisteredOwnHRMIDs){
 				if(!tLocalHRMID.isClusterAddress()){
 					if(tLocalHRMID.isCluster(tNextHopL0Cluster)){
 						tResult = tLocalHRMID.clone();
 						break;
 					}
 				}
 			}
 		}
 		
 		return tResult;	
 	}
 	
 	/**
 	 * 
 	 * @param pForeignHRMID
 	 * @return
 	 */
 	private HRMID aggregateForeignHRMID(HRMID pForeignHRMID)
 	{
 		HRMID tResult = null;
 		
 		if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_AGGREGATION){
 			Logging.err(this, "Aggrgating foreign HRMID: " + pForeignHRMID);
 		}
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			int tHierLevel = HRMConfig.Hierarchy.HEIGHT_LIMIT;
 			// iterate over all local HRMIDs
 			for(HRMID tLocalHRMID : mRegisteredOwnHRMIDs){
 				// ignore cluster addresses
 				if(!tLocalHRMID.isClusterAddress()){
 					/**
 					 * Is the potentially foreign HRMID a local one?
 					 */ 
 					if(tLocalHRMID.equals(pForeignHRMID)){
 						if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_AGGREGATION){
 							Logging.err(this, "   ..found matching local HRMID: " + tLocalHRMID);
 						}
 						tResult = pForeignHRMID;
 						break;
 					}
 					
 					/**
 					 * Determine the foreign cluster in relation to current local HRMID
 					 */
 					HRMID tForeignCluster = tLocalHRMID.getForeignCluster(pForeignHRMID);
 					if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_AGGREGATION){
 						Logging.err(this, "   ..foreign cluster of " + pForeignHRMID + " for " + tLocalHRMID + " is " + tForeignCluster);
 					}
 					
 					/**
 					 * Update the result value
 					 */
 					if((tResult == null) || (tHierLevel < tForeignCluster.getHierarchyLevel())){
 						if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_AGGREGATION){
 							Logging.err(this, "     ..found better result: " + tResult + ", best lvl: " + tHierLevel + ", cur. lvl: " + tForeignCluster.getHierarchyLevel());
 						}
 						tHierLevel = tForeignCluster.getHierarchyLevel();
 						tResult = tForeignCluster;						
 					}
 				}
 			}
 		}
 		
 		if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_AGGREGATION){
 			Logging.err(this, "   ..result: " + tResult);
 		}
 		return tResult;
 	}
 
 	/**
 	 * Adds an entry to the routing table of the local HRS instance.
 	 * In opposite to addHRMRoute() from the HierarchicalRoutingService class, this function additionally updates the GUI.
 	 * If the L2 address of the next hop is defined, the HRS will update the HRMID-to-L2ADDRESS mapping.
 	 * 
 	 * @param pRoutingEntry the new routing entry
 	 * 
 	 * @return true if the entry had new routing data
 	 */
 	private int mCallsAddHRMRoute = 0;
 	private boolean addHRMRoute(RoutingEntry pRoutingEntry)
 	{
 		boolean tResult = false;
 		
 		mCallsAddHRMRoute++;
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Adding (" + mCallsAddHRMRoute + ") HRM routing table entry: " + pRoutingEntry);
 		}
 		
 		// filter invalid destinations
 		if(pRoutingEntry.getDest() == null){
 			throw new RuntimeException(this + "::addHRMRoute() detected an invalid destination in routing entry: " + pRoutingEntry);
 		}
 		
 		// filter invalid next hop
 		if(pRoutingEntry.getNextHop() == null){
 			throw new RuntimeException(this + "::addHRMRoute() detected an invalid next hop in routing entry: " + pRoutingEntry);
 		}
 		
 		// plausibility check
 //		if((!pRoutingEntry.getDest().isClusterAddress()) && (!pRoutingEntry.getDest().equals(pRoutingEntry.getNextHop()))){
 //			throw new RuntimeException(this + "::addHRMRoute() detected an invalid destination (should be equal to the next hop) in routing entry: " + pRoutingEntry);
 //		}
 			
 			
 		/**
 		 * Inform the HRS about the new route
 		 */
 		tResult = getHRS().addHRMRoute(pRoutingEntry);
 
 		/**
 		 * Notify GUI
 		 */
 		if(tResult){
 			//Logging.log(this, "Notifying GUI because of: " + pRoutingEntry + ", cause=" + pRoutingEntry.getCause());
 			// it's time to update the GUI
 			notifyGUI(pRoutingEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Adds interesting parts of a received shared routing table
 	 * 
 	 * @param pReceivedSharedRoutingTable the received shared routing table
 	 * @param pReceiverHierarchyLevel the hierarchy level of the receiver
 	 * @param pSenderHRMID the HRMID of the sender 
 	 * @param pCause the cause for this addition of routes
 	 */
 	public void addHRMRouteShare(RoutingTable pReceivedSharedRoutingTable, HierarchyLevel pReceiverHierarchyLevel, HRMID pSenderHRMID, String pCause)
 	{
 		boolean DEBUG = false;
 //		if(pReceiverHierarchyLevel.isBaseLevel()){
 //			if(!getAllCoordinators(2).isEmpty()){
 //				DEBUG = true;
 //			}
 //		}
 		
 		for(RoutingEntry tEntry : pReceivedSharedRoutingTable){
 			RoutingEntry tReceivedSharedRoutingEntry = tEntry.clone();
 			if(DEBUG){
 				Logging.log(this, "  ..received shared route: " + tReceivedSharedRoutingEntry + ", aggregated foreign destination: " + aggregateForeignHRMID(tReceivedSharedRoutingEntry.getDest()));
 			}
 				
 			/**
 			 * Mark as shared entry
 			 */
 			tReceivedSharedRoutingEntry.setSharedLink(pSenderHRMID);
 
 			boolean tDropRoute = false;
 			
 			/**
 			 * Check if the route starts at this node.
 			 * If the route starts at a direct neighbor node, try to find a combined route and store this one instead of the original shared route.
 			 */ 
 			if(!isLocal(tReceivedSharedRoutingEntry.getSource())){
 				// check if the route starts - at least - at one of the direct neighbor nodes
 				if(isLocalCluster(tReceivedSharedRoutingEntry.getSource())){
 					/**
 					 * The received shared route starts at one of the direct neighbor nodes
 					 *  	=> we derive a new route as new combination of: [route to direct neighbor] ==> [received shared route]
 					 */
 					RoutingEntry tSecondRoutePart = tReceivedSharedRoutingEntry.clone();					
 					RoutingTable tLocalRoutingTable = mHierarchicalRoutingService.getRoutingTable();
 					// search for a routing entry to the direct neighbor node
 					RoutingEntry tFirstRoutePart = tLocalRoutingTable.getDirectNeighborEntry(tReceivedSharedRoutingEntry.getSource());
 					// have we found an routing entry to the source of the received shared route?
 					if(tFirstRoutePart != null){
 						tReceivedSharedRoutingEntry = tFirstRoutePart;
 						tReceivedSharedRoutingEntry.extendCause(pCause);
 						tReceivedSharedRoutingEntry.append(tSecondRoutePart, this + "::addHRMRouteShare(pCause) at lvl: " + pReceiverHierarchyLevel);
 						// set the origin of the shared routing entry as origin for the resulting local routing entry
 						tReceivedSharedRoutingEntry.setOrigin(tSecondRoutePart.getOrigin());
 						// reset the next hop L2 address
 						tReceivedSharedRoutingEntry.setNextHopL2Address(null);
 					}else{
 						tDropRoute = true;
 
 						if(DEBUG){
 				 			Logging.warn(this, "    ..dropping uninteresting (does start at a direct neighbor node but no route to the direct neighbor could be found) route: " + tReceivedSharedRoutingEntry);
 						}
 					}
 				}else{
 					// drop the route
 					tDropRoute = true;
 
 					if(DEBUG){
 			 			Logging.warn(this, "    ..dropping uninteresting (does not start neither at this node nor at a direct neighbor node) route: " + tReceivedSharedRoutingEntry);
 					}
 				}
 			}
 
 			
 			/**
 			 * Store only routes which start at this node
 			 */
 			if(!tDropRoute){
 				if(isLocal(tReceivedSharedRoutingEntry.getSource())){
 					/**
 					 * ignore routes to cluster this nodes belong to
 					 * 		=> such routes are already known based on neighborhood detection of the L0 comm. channels (Clusters) 
 					 */				
 					if((!tReceivedSharedRoutingEntry.getDest().isClusterAddress()) || (!isLocalCluster(tReceivedSharedRoutingEntry.getDest()))){
 						if((!tReceivedSharedRoutingEntry.getDest().isClusterAddress()) || (tReceivedSharedRoutingEntry.getHopCount() > 1)){
 							// patch the source with the correct local sender address
 							tReceivedSharedRoutingEntry.setSource(getLocalSenderAddress(tReceivedSharedRoutingEntry.getSource(), tReceivedSharedRoutingEntry.getNextHop()));
 							tReceivedSharedRoutingEntry.extendCause(pCause);
 							tReceivedSharedRoutingEntry.extendCause(this + "::addHRMRouteShare() at lvl: " + pReceiverHierarchyLevel.getValue());
 							
 							/**
 							 * Set the timeout for the found shared route
 							 * 		=> 2 times the time period between two share phase for the sender's hierarchy level
 							 */
 							double tTimeoffset = 2 * getPeriodSharePhase(pReceiverHierarchyLevel.getValue() + 1 /* the sender is one level above */);
 							tReceivedSharedRoutingEntry.setTimeout(getSimulationTime() + tTimeoffset);
 							
 							/**
 							 * Store the found route
 							 */
 							if(DEBUG){
 								Logging.log(this, "    ..adding shared route (timeout=" + tReceivedSharedRoutingEntry.getTimeout() + ", time-offset=" + tTimeoffset + "): " + tReceivedSharedRoutingEntry);
 							}
 							addHRMRoute(tReceivedSharedRoutingEntry);
 					 	}else{
 							if(DEBUG){
 					 			Logging.warn(this, "    ..dropping uninteresting (leads to a direct neighbor node/cluster) route: " + tReceivedSharedRoutingEntry);
 							}
 					 	}
 				 	}else{
 						if(DEBUG){
 				 			Logging.warn(this, "    ..dropping uninteresting (this node belongs to the destination cluster) route: " + tReceivedSharedRoutingEntry);
 						}
 				 	}
 				}else{
 					if(DEBUG){
 			 			Logging.err(this, "    ..dropping uninteresting (does not start at this node) route: " + tReceivedSharedRoutingEntry);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Registers automatically new links in the HRG based on a given routing table entry.
 	 * This function uses mainly the source and the next hop address. It switches between the hierarchy levels and derives for each hierarchy level the HRG link.
 	 * For example, if a route from 1.2.3 to next 2.5.1 in order to reach 2.0.0 is given, then the following links will be added:
 	 *   		1.2.3 <==> 2.5.1 
 	 *   		1.2.0 <==> 2.5.0 
 	 *   		1.0.0 <==> 2.0.0 
 	 * 
 	 * @param pRoutingEntry the routing table entry
 	 */
 	public void registerAutoHRG(RoutingEntry pRoutingEntry)
 	{
 		HRMID tDestHRMID = pRoutingEntry.getDest();
 		if(tDestHRMID != null){
 			if((!tDestHRMID.isClusterAddress()) || (pRoutingEntry.getNextHop().isCluster(tDestHRMID))){
 				if(HRMConfig.DebugOutput.GUI_SHOW_HRG_UPDATES){
 					Logging.log(this, "  ..registering (" + mCallsAddHRMRoute + ") node-2-node HRG link from " + pRoutingEntry.getSource() + " to " + pRoutingEntry.getNextHop() + " for: " + pRoutingEntry);
 				}
 				RoutingEntry tRoutingEntry = pRoutingEntry.clone();
 				tRoutingEntry.extendCause(this + "::registerAutoHRG() as " + tRoutingEntry);
 				tRoutingEntry.setTimeout(pRoutingEntry.getTimeout());
 				
 				double tBefore = HRMController.getRealTime();
 				registerLinkHRG(pRoutingEntry.getSource(), pRoutingEntry.getNextHop(), tRoutingEntry);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 				if(tSpentTime > 30){
 					Logging.log(this, "      ..registerAutoHRG()::registerLinkHRG() took " + tSpentTime + " ms for processing " + pRoutingEntry);
 				}
 			}
 			HRMID tGeneralizedSourceHRMID = tDestHRMID.getForeignCluster(pRoutingEntry.getSource());
 			// get the hierarchy level at which this link connects two clusters
 			int tLinkHierLvl = tGeneralizedSourceHRMID.getHierarchyLevel();
 			// initialize the source cluster HRMID
 			HRMID tSourceClusterHRMID = pRoutingEntry.getSource();
 			// initialize the destination cluster HRMID
 			HRMID tDestClusterHRMID = pRoutingEntry.getNextHop();
 			for(int i = 0; i <= tLinkHierLvl; i++){
 				// reset the value for the corresponding hierarchy level for both the source and destination cluster HRMID
 				tSourceClusterHRMID.setLevelAddress(i, 0);
 				tDestClusterHRMID.setLevelAddress(i, 0);
 	
 				if(!tSourceClusterHRMID.equals(tDestClusterHRMID)){
 					if(HRMConfig.DebugOutput.GUI_SHOW_HRG_UPDATES){
 						Logging.log(this, "  ..registering (" + mCallsAddHRMRoute + ") cluster-2-cluster (lvl: " + i + ") HRG link from " + tSourceClusterHRMID + " to " + tDestClusterHRMID + " for: " + pRoutingEntry);
 					}
 					RoutingEntry tRoutingEntry = pRoutingEntry.clone();
 //					tRoutingEntry.setDest(tDestClusterHRMID);
 					
 //					RoutingEntry.create(pRoutingEntry.getSource().clone(), tDestClusterHRMID.clone(), pRoutingEntry.getNextHop().clone(), RoutingEntry.NO_HOP_COSTS, RoutingEntry.NO_UTILIZATION, RoutingEntry.NO_DELAY, RoutingEntry.INFINITE_DATARATE, pRoutingEntry.getCause());
 //					tRoutingEntry.setNextHopL2Address(pRoutingEntry.getNextHopL2Address());
 					tRoutingEntry.extendCause(this + "::registerAutoHRG() with destination " + tRoutingEntry.getDest() + ", org. destination=" +pRoutingEntry.getDest() + " as " + tRoutingEntry);
 //					tRoutingEntry.setTimeout(pRoutingEntry.getTimeout());
 					registerCluster2ClusterLinkHRG(tSourceClusterHRMID, tDestClusterHRMID, tRoutingEntry);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds a table to the routing table of the local HRS instance.
 	 * In opposite to addHRMRoute() from the HierarchicalRoutingService class, this function additionally updates the GUI.
 	 * If the L2 address of the next hop is defined, the HRS will update the HRMID-to-L2ADDRESS mapping.
 	 * 
 	 * @param pRoutingTable the routing table with new entries
 	 * 
 	 * @return true if the table had new routing data
 	 */
 	public boolean addHRMRoutes(RoutingTable pRoutingTable)
 	{
 		boolean tResult = false;
 		
 		for(RoutingEntry tEntry : pRoutingTable){
 			tResult |= addHRMRoute(tEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Deletes a route from the local HRM routing table.
 	 * 
 	 * @param pRoutingTableEntry the routing table entry
 	 *  
 	 * @return true if the entry was found and removed, otherwise false
 	 */
 	private boolean delHRMRoute(RoutingEntry pRoutingEntry)
 	{
 		boolean tResult = false;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Deleting HRM routing table entry: " + pRoutingEntry);
 		}
 		
 		/**
 		 * Inform the HRS about the new route
 		 */
 		tResult = getHRS().delHRMRoute(pRoutingEntry);
 
 		/**
 		 * Notify GUI
 		 */
 		if(tResult){
 			pRoutingEntry.extendCause(this + "::delHRMRoute()");
 			
 			// it's time to update the GUI
 			notifyGUI(pRoutingEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Unregisters automatically old links from the HRG based on a given routing table entry
 	 * 
 	 * @param pRoutingEntry the routing table entry
 	 */
 	public void unregisterAutoHRG(RoutingEntry pRoutingEntry)
 	{
 		HRMID tDestHRMID = pRoutingEntry.getDest();
 		if(tDestHRMID != null){
 			if((!tDestHRMID.isClusterAddress()) || (pRoutingEntry.getNextHop().isCluster(tDestHRMID))){
 				if(HRMConfig.DebugOutput.GUI_SHOW_HRG_UPDATES){
 					Logging.log(this, "  ..unregistering (" + mCallsAddHRMRoute + ") node-2-node HRG link from " + pRoutingEntry.getSource() + " to " + pRoutingEntry.getNextHop() + " for: " + pRoutingEntry);
 				}
 				RoutingEntry tRoutingEntry = pRoutingEntry.clone();
 				unregisterLinkHRG(pRoutingEntry.getSource(), pRoutingEntry.getNextHop(), tRoutingEntry);
 			}
 			HRMID tGeneralizedSourceHRMID = tDestHRMID.getForeignCluster(pRoutingEntry.getSource());
 			// get the hierarchy level at which this link connects two clusters
 			int tLinkHierLvl = tGeneralizedSourceHRMID.getHierarchyLevel();
 			// initialize the source cluster HRMID
 			HRMID tSourceClusterHRMID = pRoutingEntry.getSource().clone();
 			// initialize the destination cluster HRMID
 			HRMID tDestClusterHRMID = pRoutingEntry.getNextHop().clone();
 			for(int i = 0; i <= tLinkHierLvl; i++){
 				// reset the value for the corresponding hierarchy level for both the source and destination cluster HRMID
 				tSourceClusterHRMID.setLevelAddress(i, 0);
 				tDestClusterHRMID.setLevelAddress(i, 0);
 	
 				if(!tSourceClusterHRMID.equals(tDestClusterHRMID)){
 					if(HRMConfig.DebugOutput.GUI_SHOW_HRG_UPDATES){
 						Logging.log(this, "  ..unregistering (" + mCallsAddHRMRoute + ") cluster-2-cluster (lvl: " + i + ") HRG link from " + tSourceClusterHRMID + " to " + tDestClusterHRMID + " for: " + pRoutingEntry);
 					}
 					RoutingEntry tRoutingEntry = pRoutingEntry.clone();
 //					tRoutingEntry.setDest(tDestClusterHRMID);
 					
 //					RoutingEntry.create(pRoutingEntry.getSource().clone(), tDestClusterHRMID.clone(), pRoutingEntry.getNextHop().clone(), RoutingEntry.NO_HOP_COSTS, RoutingEntry.NO_UTILIZATION, RoutingEntry.NO_DELAY, RoutingEntry.INFINITE_DATARATE, pRoutingEntry.getCause());
 //					tRoutingEntry.setNextHopL2Address(pRoutingEntry.getNextHopL2Address());
 					tRoutingEntry.extendCause(this + "::unregisterAutoHRG() with destination " + tRoutingEntry.getDest() + ", org. destination=" +pRoutingEntry.getDest());
 //					tRoutingEntry.setTimeout(pRoutingEntry.getTimeout());
 					unregisterCluster2ClusterLinkHRG(tSourceClusterHRMID, tDestClusterHRMID, tRoutingEntry);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Removes a table from the routing table of the local HRS instance.
 	 * 
 	 * @param pRoutingTable the routing table with old entries
 	 * 
 	 * @return true if the table had existing routing data
 	 */
 	public boolean delHRMRoutes(RoutingTable pRoutingTable)
 	{
 		boolean tResult = false;
 		
 		for(RoutingEntry tEntry : pRoutingTable){
 			RoutingEntry tDeleteThis = tEntry.clone();
 			tDeleteThis.extendCause(this + "::delHRMRoutes()");
 			tResult |= delHRMRoute(tDeleteThis);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Adds a route to the local L2 routing table.
 	 * 
 	 * @param pToL2Address the L2Address of the destination
 	 * @param pRoute the route to the direct neighbor
 	 */
 	public void registerL2Route(L2Address pToL2Address, Route pRoute)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
     		Logging.log(this, "REGISTERING LINK (L2):\n  DEST.=" + pToL2Address + "\n  LINK=" + pRoute);
     	}
 
 		// inform the HRS about the new route
 		if(getHRS().registerL2Route(pToL2Address, pRoute)){
 			// it's time to update the GUI
 			notifyGUI(pRoute);
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
 	public Connection connectBlock(Name pDestination, Description pRequirements, Identity pIdentity) throws NetworkException
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
 	public static void eventSimulationCreationHasFinished()
 	{
 		mFoGSiEmSimulationCreationFinished = true;
 	}
 	
 	/**
 	 * EVENT: simulation restarted
 	 */
 	public static void eventSimulationRestarted()
 	{
 		Logging.log(null, "EVENT: simulation restarted");
 		
 		// reset the stored HRMController database
 		mRegisteredHRMControllers = new LinkedList<HRMController>();
 		sRegisteredCoordinators = 0;
 		sRegisteredCoordinatorsCounter = new HashMap<Integer, Integer>();
 		sUnregisteredCoordinators = 0;
 		sResetNMS = true;
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
 	public long getHierarchyNodePriority(HierarchyLevel pLevel)
 	{
 		if (HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY){
 			// the used hierarchy level is always "1" above of the one from the causing entity
 			int tHierLevel = pLevel.getValue();
 			if (!HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL){
 				// always use L1
 				tHierLevel = 1;
 			}
 
 			return mNodeHierarchyPriority[tHierLevel];
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
 		 * Inform all local ClusterMembers/Clusters at level 0 about the change
 		 * HINT: we have to enforce a permanent lock of mLocalClusterMembers, 
 		 *       otherwise race conditions might be caused (another ClusterMemeber 
 		 *       could be created while we are updating the priorities of all the 
 		 *       formerly known ones)
 		 * HINT: mLocalClusterMembers also contains all local Clusters      
 		 */
 		// get a copy of the list about local CoordinatorAsClusterMember instances in order to avoid dead lock between HRMControllerProcessor and main EventHandler
 		LinkedList<ClusterMember> tLocalL0ClusterMembers = getAllL0ClusterMembers();
 		Logging.log(this, "  ..informing about the new priority: " + pPriority + " - update nr. " + mConnectivityPriorityUpdates + ")");
 		int i = 0;
 		for(ClusterMember tClusterMember : tLocalL0ClusterMembers){
 			// only base hierarchy level!
 			if(tClusterMember.getHierarchyLevel().isBaseLevel()){
 				Logging.log(this, "      ..update (" + mConnectivityPriorityUpdates + ") - informing[" + i + "]: " + tClusterMember);
 				tClusterMember.eventConnectivityNodePriorityUpdate(getConnectivityNodePriority());
 				i++;
 			}
 		}
 	}
 	
 	/**
 	 * EVENT: hierarchy data changed
 	 */
 	private void eventHierarchyDataChanged()
 	{
 		/**
 		 * Refresh the stored simulation time describing when the last AnnounceCoordinator packet had impact on the hierarchy
 		 */
 		mSimulationTimeOfLastCoordinatorAnnouncementWithImpact = getSimulationTime();
 		
 		/**
 		 * If GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS is deactivated and the topology changes, we have deactivated the 
 		 * AnnounceCoordinator packets too early or the user has deactivated it too early. -> this leads to faulty results with a high probability 
 		 */
 		if(!GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 			Logging.err(this, "------------------------------------------------------------------------------------------------------------------");
 			Logging.err(this, "--- Detected a hierarchy data change when GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS was already set to false ");
 			Logging.err(this, "------------------------------------------------------------------------------------------------------------------");
 		}
 	}
 
 	/**
 	 * Distributes hierarchy node priority update to all important local entities
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 */
 	public void distributeHierarchyNodePriorityUpdate(HierarchyLevel pHierarchyLevel)
 	{
 		long tNewPrio = getHierarchyNodePriority(pHierarchyLevel);
 		
 		/**
 		 * Inform all local CoordinatorAsClusterMemeber objects about the change
 		 * HINT: we have to enforce a permanent lock of mLocalCoordinatorAsClusterMemebers, 
 		 *       otherwise race conditions might be caused (another CoordinatorAsClusterMemeber 
 		 *       could be created while we are updating the priorities of all the 
 		 *       formerly known ones)
 		 */
 		Logging.log(this, "  ..informing about the priority (" + tNewPrio + ") update (" + mHierarchyPriorityUpdates + ")");
 		// get a copy of the list about local CoordinatorAsClusterMember instances in order to avoid dead lock between HRMControllerProcessor and main EventHandler
 		LinkedList<CoordinatorAsClusterMember> tLocalCoordinatorAsClusterMembers = getAllCoordinatorAsClusterMembers();
 		int i = 0;
 		for(CoordinatorAsClusterMember tCoordinatorAsClusterMember : tLocalCoordinatorAsClusterMembers){
 			if((tCoordinatorAsClusterMember.getHierarchyLevel().equals(pHierarchyLevel)) || (!HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL)){
 				Logging.log(this, "      ..update (" + mHierarchyPriorityUpdates + ") - informing[" + i + "]: " + tCoordinatorAsClusterMember);
 				tCoordinatorAsClusterMember.eventHierarchyNodePriorityUpdate(getHierarchyNodePriority(pHierarchyLevel));
 				i++;
 			}
 		}
 		// get a copy of the list about local CoordinatorAsClusterMember instances in order to avoid dead lock between HRMControllerProcessor and main EventHandler
 		LinkedList<Cluster> tLocalClusters = getAllClusters();
 		i = 0;
 		for(Cluster tLocalCluster : tLocalClusters){
 			if((tLocalCluster.getHierarchyLevel().equals(pHierarchyLevel)) || (!HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL)){
 				Logging.log(this, "      ..update (" + mHierarchyPriorityUpdates + ") - informing[" + i + "]: " + tLocalCluster);
 				tLocalCluster.eventHierarchyNodePriorityUpdate(getHierarchyNodePriority(pHierarchyLevel));
 				i++;
 			}
 		}
 	}
 
 	/**
 	 * Sets new hierarchy node priority for Election processes
 	 * 
 	 * @param pPriority the new hierarchy node priority
 	 * @param pHierarchyLevel the hierarchy level
 	 */
 	private int mHierarchyPriorityUpdates = 0;
 	private synchronized void setHierarchyPriority(long pPriority, HierarchyLevel pHierarchyLevel)
 	{
 		Logging.log(this, "Setting new hierarchy node priority: " + pPriority);
 		mNodeHierarchyPriority[pHierarchyLevel.getValue()] = pPriority;
 
 		mHierarchyPriorityUpdates++;
 		
 		Logging.log(this, "  ..informing about the priority (" + pPriority + ") update (" + mHierarchyPriorityUpdates + ")");
 		/**
 		 * Asynchronous execution of "distributeHierarchyNodePriorityUpdate()" inside context of HRMControllerProcessor.
 		 * This also reduces convergence time for finding the correct network clustering 
 		 */ 
 		if(mProcessorThread != null){
 			mProcessorThread.eventNewHierarchyPriority(pHierarchyLevel);
 		}
 		//HINT: for synchronous execution use here "distributeHierarchyNodePriorityUpdate(pHierarchyLevel)"
 		//      instead of "mProcessorThread.eventNewHierarchyPriority(pHierarchyLevel)"
 		
 		/**
 		 * Trigger: hierarchy data changed
 		 */
 		eventHierarchyDataChanged();
 	}
 
 	/**
 	 * Increases base Election priority
 	 * 
 	 * @param pCausingInterfaceToNeighbor the update causing interface to a neighbor
 	 */
 	private synchronized void increaseNodePriority_Connectivity(NetworkInterface pCausingInterfaceToNeighbor)
 	{
 		// get the current priority
 		long tPriority = getConnectivityNodePriority();
 		
 		Logging.log(this, "Increasing node priority (CONNECTIVITY) by " + ElectionPriority.OFFSET_FOR_CONNECTIVITY);
 
 		// increase priority
 		tPriority += ElectionPriority.OFFSET_FOR_CONNECTIVITY;
 		
 		mDesriptionConnectivityPriorityUpdates += "\n + " + ElectionPriority.OFFSET_FOR_CONNECTIVITY + " ==> " + pCausingInterfaceToNeighbor;
 		
 		// update priority
 		setConnectivityPriority(tPriority);
 
 		Logging.log(this, "Increasing hierarchy node priority (CONNECTIVITY) by " + ElectionPriority.OFFSET_FOR_CONNECTIVITY);
 		
 		// get the current priority
 		long tHierarchyPriority = mNodeHierarchyPriority[1];
 
 		// increase priority
 		tHierarchyPriority += ElectionPriority.OFFSET_FOR_CONNECTIVITY;
 
 		mDesriptionHierarchyPriorityUpdates += "\n + " + ElectionPriority.OFFSET_FOR_CONNECTIVITY + " <== Cause: " + pCausingInterfaceToNeighbor;
 
 		// update priority
 		setHierarchyPriority(tHierarchyPriority, new HierarchyLevel(this,  1));
 	}
 	
 	/**
 	 * Decreases base Election priority
 	 * 
 	 * @param pCausingInterfaceToNeighbor the update causing interface to a neighbor
 	 */
 	private synchronized void decreaseNodePriority_Connectivity(NetworkInterface pCausingInterfaceToNeighbor)
 	{
 		// get the current priority
 		long tPriority = getConnectivityNodePriority();
 		
 		Logging.log(this, "Decreasing node priority (CONNECTIVITY) by " + ElectionPriority.OFFSET_FOR_CONNECTIVITY);
 
 		// increase priority
 		tPriority -= ElectionPriority.OFFSET_FOR_CONNECTIVITY;
 		
 		mDesriptionConnectivityPriorityUpdates += "\n - " + ElectionPriority.OFFSET_FOR_CONNECTIVITY + " ==> " + pCausingInterfaceToNeighbor;
 		
 		// update priority
 		setConnectivityPriority(tPriority);
 
 		Logging.log(this, "Decreasing hierarchy node priority (CONNECTIVITY) by " + ElectionPriority.OFFSET_FOR_CONNECTIVITY);
 		
 		// get the current priority
 		long tHierarchyPriority = mNodeHierarchyPriority[1];
 
 		// increase priority
 		tHierarchyPriority -= ElectionPriority.OFFSET_FOR_CONNECTIVITY;
 
 		mDesriptionHierarchyPriorityUpdates += "\n - " + ElectionPriority.OFFSET_FOR_CONNECTIVITY + " <== Cause: " + pCausingInterfaceToNeighbor;
 
 		// update priority
 		setHierarchyPriority(tHierarchyPriority, new HierarchyLevel(this, 1));
 	}
 
 	/**
 	 * Increases hierarchy Election priority
 	 * 
 	 * @param pCausingEntity the update causing entity
 	 */
 	public void increaseHierarchyNodePriority_KnownCoordinator(ControlEntity pCausingEntity)
 	{
 		/**
 		 * Are we at base hierarchy level or should we accept all levels?
 		 */ 
 		if((pCausingEntity.getHierarchyLevel().isBaseLevel()) || (HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL)){
 			// the used hierarchy level is always "1" above of the one from the causing entity
 			int tHierLevel = pCausingEntity.getHierarchyLevel().getValue() + 1;
 			if (!HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL){
 				// always use L1
 				tHierLevel = 1;
 			}
 			
 			int tDistance = 0;
 			if(pCausingEntity instanceof CoordinatorProxy){
 				tDistance = ((CoordinatorProxy)pCausingEntity).getDistance();
 			}
 	
 			int tMaxDistance = HRMConfig.Hierarchy.EXPANSION_RADIUS;
 			if(!pCausingEntity.getHierarchyLevel().isBaseLevel()){
 				tMaxDistance = HRMConfig.Hierarchy.MAX_HOPS_TO_A_REMOTE_COORDINATOR;
 			}
 			
 			if((tDistance >= 0) && (tDistance <= tMaxDistance)){
 				// get the current priority
 				long tPriority = mNodeHierarchyPriority[tHierLevel];
 				
 				float tOffset = 0;
 				if (pCausingEntity.getHierarchyLevel().isBaseLevel()){
 					tOffset = (float)ElectionPriority.OFFSET_FOR_KNOWN_BASE_REMOTE_L0_COORDINATOR * (2 + tMaxDistance - tDistance);
 				}else{
 					tOffset = (float)ElectionPriority.OFFSET_FOR_KNOWN_BASE_REMOTE_L1p_COORDINATOR * (2 + tMaxDistance - tDistance);
 				}
 						
 				Logging.log(this, "Increasing hierarchy node priority (KNOWN BASE COORDINATOR) by " + (long)tOffset + ", distance=" + tDistance + "/" + tMaxDistance);
 		
 				// increase priority
 				tPriority += (long)(tOffset);
 				
 				String tSpace = "";
 				for(int i = 0; i < tHierLevel; i++){
 					tSpace += "  "; 
 				}
 				mDesriptionHierarchyPriorityUpdates += "\n + " + tSpace + tPriority + "-L" + tHierLevel + ": " + tOffset + " <== HOPS: " + tDistance + "/" + tMaxDistance + ", Cause: " + pCausingEntity;
 	
 				// update priority
 				setHierarchyPriority(tPriority, new HierarchyLevel(this, tHierLevel));
 			}else{
 				Logging.err(this, "Detected invalid distance: " + tDistance + "/" + tMaxDistance);
 			}
 		}
 	}
 
 	/**
 	 * Decreases hierarchy Election priority
 	 * 
 	 * @param pCausingEntity the update causing entity
 	 */
 	public void decreaseHierarchyNodePriority_KnownCoordinator(ControlEntity pCausingEntity)
 	{
 		/**
 		 * Are we at base hierarchy level or should we accept all levels?
 		 */ 
 		if((pCausingEntity.getHierarchyLevel().isBaseLevel()) || (HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL)){
 			// the used hierarchy level is always "1" above of the one from the causing entity
 			int tHierLevel = pCausingEntity.getHierarchyLevel().getValue() + 1;
 			if (!HRMConfig.Hierarchy.USE_SEPARATE_HIERARCHY_NODE_PRIORITY_PER_LEVEL){
 				// always use L1
 				tHierLevel = 1;
 			}
 
 			int tDistance = 0;
 			if(pCausingEntity instanceof CoordinatorProxy){
 				tDistance = ((CoordinatorProxy)pCausingEntity).getDistance();
 			}
 			
 			int tMaxDistance = HRMConfig.Hierarchy.EXPANSION_RADIUS;
 			if(!pCausingEntity.getHierarchyLevel().isBaseLevel()){
 				tMaxDistance = HRMConfig.Hierarchy.MAX_HOPS_TO_A_REMOTE_COORDINATOR;
 			}
 
 			if((tDistance >= 0) && (tDistance <= tMaxDistance)){
 				// get the current priority
 				long tPriority = mNodeHierarchyPriority[tHierLevel];
 				
 				float tOffset = 0;
 				if (pCausingEntity.getHierarchyLevel().isBaseLevel()){
 					tOffset = (float)ElectionPriority.OFFSET_FOR_KNOWN_BASE_REMOTE_L0_COORDINATOR * (2 + tMaxDistance - tDistance);
 				}else{
 					tOffset = (float)ElectionPriority.OFFSET_FOR_KNOWN_BASE_REMOTE_L1p_COORDINATOR * (2 + tMaxDistance - tDistance);
 				}
 				
 				Logging.log(this, "Decreasing hierarchy node priority (KNOWN BASE COORDINATOR) by " + (long)tOffset + ", distance=" + tDistance + "/" + tMaxDistance);
 		
 				// decrease priority
 				tPriority -= (long)(tOffset);
 				
 				String tSpace = "";
 				for(int i = 0; i < tHierLevel; i++){
 					tSpace += "  "; 
 				}
 				mDesriptionHierarchyPriorityUpdates += "\n - " + tSpace + tPriority + "-L" + tHierLevel + ": " + tOffset + " <== HOPS: " + tDistance + "/" + tMaxDistance + ", Cause: " + pCausingEntity;
 	
 				// update priority
 				setHierarchyPriority(tPriority, new HierarchyLevel(this, tHierLevel));
 			}else{
 				Logging.err(this, "Detected invalid distance: " + tDistance + "/" + tMaxDistance);
 			}
 		}
 	}
 
 	/**
 	 * Returns a description about all connectivity priority updates.
 	 * This function is only used within the GUI. It is not part of the concept.
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionConnectivityPriorityUpdates()
 	{
 		return mDesriptionConnectivityPriorityUpdates;
 	}
 
 	/**
 	 * Returns a description about all HRMID updates.
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionHRMIDChanges()
 	{
 		return mDescriptionHRMIDUpdates;
 	}
 
 	/**
 	 * Returns a description about all HRG updates.
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionHRGChanges()
 	{
 		return mDescriptionHRGUpdates;
 	}
 	
 	/**
 	 * Returns a description about all hierarchy priority updates
 	 * This function is only used within the GUI. It is not part of the concept.
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionHierarchyPriorityUpdates()
 	{
 		return mDesriptionHierarchyPriorityUpdates;
 	}
 
 	/**
 	 * Returns a log about "update cluster" events
 	 * This function is only used within the GUI. It is not part of the concept.
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDescriptionClusterUpdates()
 	{
 		return mProcessorThread.getGUIDescriptionClusterUpdates();
 	}
 	
 	/**
 	 * Returns a description about all used cluster addresses
 	 * 
 	 * @return the description
 	 */
 	public String getGUIDEscriptionUsedAddresses()
 	{
 		String tResult = "";
 		
 		LinkedList<Cluster> tAllClusters = getAllClusters();
 		for (Cluster tCluster : tAllClusters){
 			tResult += "\n .." + tCluster + " uses these addresses:";
 			LinkedList<Integer> tUsedAddresses = tCluster.getUsedAddresses();
 			int i = 0;
 			for (int tUsedAddress : tUsedAddresses){
 				tResult += "\n     ..[" + i + "]: " + tUsedAddress;
 				i++;
 			}
 
 			LinkedList<ComChannel> tAllClusterChannels = tCluster.getComChannels();
 			tResult += "\n .." + tCluster + " channels:";
 			i = 0;
 			for(ComChannel tComChannel : tAllClusterChannels){
 				tResult += "\n     ..[" + i + "]: " + tComChannel;
 				i++;
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Reacts on a lost physical neighbor.
 	 * HINT: "pNeighborL2Address" doesn't correspond to the neighbor's central FN!
 	 * 
 	 * @param pInterfaceToNeighbor the network interface to the neighbor 
 	 * @param pNeighborL2Address the L2 address of the detected physical neighbor's first FN towards the common bus.
 	 */
 	public synchronized void eventLostPhysicalNeighborNode(final NetworkInterface pInterfaceToNeighbor, L2Address pNeighborL2Address)
 	{
 		Logging.log(this, "\n\n\n############## LOST DIRECT NEIGHBOR NODE " + pNeighborL2Address + ", interface=" + pInterfaceToNeighbor);
 		
 		synchronized (mCommunicationSessions) {
 			Logging.log(this, "   ..known sessions: " + mCommunicationSessions);
 			for (ComSession tComSession : mCommunicationSessions){
 				if(tComSession.isPeer(pNeighborL2Address)){
 					Logging.log(this, "   ..stopping session: " + tComSession);
 					tComSession.stopConnection();
 				}else{
 					Logging.log(this, "   ..leaving session: " + tComSession);
 				}
 			}
 		}
 		synchronized (mLocalNetworkInterfaces) {
 			if(mLocalNetworkInterfaces.contains(pInterfaceToNeighbor)){
 				Integer tRefCount = mLocalNetworkInterfacesRefCount.get(pInterfaceToNeighbor);
 				
 				/**
 				 * decrease ref counter
 				 */
 				if(tRefCount == null){
 					Logging.err(this, "Invalid ref. count for known network interface: " + pInterfaceToNeighbor); 
 				}
 				tRefCount--;
 				mLocalNetworkInterfacesRefCount.put(pInterfaceToNeighbor, tRefCount);
 				
 				/**
 				 * delete the network interface from the database about known network interfaces
 				 */
 				if(tRefCount.intValue() <= 1){
 					Logging.log(this, "\n######### Detected lost network interface: " + pInterfaceToNeighbor);
 					mLocalNetworkInterfaces.remove(pInterfaceToNeighbor);
 				}
 			}
 			decreaseNodePriority_Connectivity(pInterfaceToNeighbor);
 		}
 		
 		// updates the GUI decoration for this node
 		updateGUINodeDecoration();
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
 				synchronized (mLocalNetworkInterfaces) {
 					/**
 					 * add the network interface to the database about known network interfaces
 					 */
 					if(!mLocalNetworkInterfaces.contains(pInterfaceToNeighbor)){
 						Logging.log(this, "\n######### Detected new network interface: " + pInterfaceToNeighbor);
 						mLocalNetworkInterfaces.add(pInterfaceToNeighbor);
 					}
 					
 					/**
 					 * increase the ref. coutner for this network interface
 					 */
 					Integer tRefCount = mLocalNetworkInterfacesRefCount.get(pInterfaceToNeighbor);
 					if(tRefCount == null){
 						tRefCount = new Integer(1);
 					}else{
 						tRefCount++;
 					}
 					mLocalNetworkInterfacesRefCount.put(pInterfaceToNeighbor, tRefCount);
 					
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
 						
 						// updates the GUI decoration for this node
 						updateGUINodeDecoration();
 					}
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
 					//registerLinkARG(this, tParentCluster, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.REMOTE_CONNECTION));
 
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
 	 * Returns the node-global election state
 	 * 
 	 * @return the node-global election state
 	 */
 	public Object getNodeElectionState()
 	{
 		return mNodeElectionState;
 	}
 	
 	/**
 	 * Returns the node-global election state change description
 	 * This function is only used within the GUI. It is not part of the concept.
 	 * 
 	 * @return the description
 	 */
 	public Object getGUIDescriptionNodeElectionStateChanges()
 	{
 		return mDescriptionNodeElectionState;
 	}
 	
 	/**
 	 * Adds a description to the node-global election state change description
 	 * 
 	 * @param pAdd the additive string
 	 */
 	public void addGUIDescriptionNodeElectionStateChange(String pAdd)
 	{
 		mDescriptionNodeElectionState += pAdd;
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
 	
 	/**
 	 * Determines the current real time
 	 * 
 	 * @return the real time in [ms]
 	 */
 	public static double getRealTime()
 	{
 		double tResult = 0;
 		
 		Date tDate = new Date();		
 		tResult = tDate.getTime();
 		
 		return tResult;
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
 	 * Auto-removes all deprecated coordinator proxies
 	 */
 	private void autoRemoveObsoleteCoordinatorProxies()
 	{
 		if(HRMController.GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 			LinkedList<CoordinatorProxy> tProxies = getAllCoordinatorProxies();
 			for(CoordinatorProxy tProxy : tProxies){
 				// does the link have a timeout?
 				if(tProxy.isObsolete()){
 					Logging.log(this, "AUTO REMOVING COORDINATOR PROXY: " + tProxy);
 	
 					/**
 					 * Trigger: remote coordinator role invalid
 					 */
 					tProxy.eventRemoteCoordinatorRoleInvalid();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Auto-deactivates AnnounceCoordinator packets.
 	 * This function is only useful for measurement speedup or to ease debugging. It is neither part of the concept nor it is used to derive additional data. It only reduces packet overhead in the network.
 	 */
 	@SuppressWarnings("unused")
 	private void autoDeactivateAnnounceCoordinator()
 	{
 		if(mSimulationTimeOfLastCoordinatorAnnouncementWithImpact != 0){
 			double tTimeWithFixedHierarchyData = getSimulationTime() - mSimulationTimeOfLastCoordinatorAnnouncementWithImpact;
 			//Logging.log(this, "Simulation time of last AnnounceCoordinator with impact: " + mSimulationTimeOfLastCoordinatorAnnouncementWithImpact + ", time  diff: " + tTimeWithFixedHierarchyData);
 			if(HRMConfig.Measurement.AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS){
 	
 				if(GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 					/**
 					 * Auto-deactivate the AnnounceCoordinator packets if no further change in hierarchy data is expected anymore
 					 */
 					if(tTimeWithFixedHierarchyData > 2 * HRMConfig.Hierarchy.COORDINATOR_TIMEOUT){
 						GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS = false;
 						
 						Logging.warn(this, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
 						Logging.warn(this, "+++ Deactivating AnnounceCoordinator packets due to long-term stability of hierarchy data");
 						Logging.warn(this, "+++ Current simulation time: " + getSimulationTime() + ", treshold time diff: " + (HRMConfig.Hierarchy.COORDINATOR_TIMEOUT * 2) + ", time with stable hierarchy data: " + tTimeWithFixedHierarchyData);
 						Logging.warn(this, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
 						
 						if(HRMConfig.Measurement.AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS_AUTO_START_ADDRESS_DISTRIBUTION){
 							autoActivateAddressDistribution();
 						}
 						if((GUI_USER_CTRL_ADDRESS_DISTRUTION) && (HRMConfig.Measurement.AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS_AUTO_START_ADDRESS_DISTRIBUTION_AUTO_START_REPORTING_SHARING)){
 							autoActivateReportingSharing();
 						}
 					}
 				}				
 			}
 		}
 		
 	}
 
 	private void autoActivateAddressDistribution()
 	{
 		Logging.warn(this, "+++++++++++++++++++++++++++++++++++++++++++++++++");
 		Logging.warn(this, "+++ Activating address distribution");
 		Logging.warn(this, "+++++++++++++++++++++++++++++++++++++++++++++++++");
 
 		GUI_USER_CTRL_ADDRESS_DISTRUTION = true;
 		
 		// iterate over all HRMControllers
 		int tFound = 0;
 		for(HRMController tHRMController : HRMController.getALLHRMControllers()) {
 			LinkedList<Coordinator> tHighestCoordinators = tHRMController.getAllCoordinators(HRMConfig.Hierarchy.HEIGHT - 1);
 			if(!tHighestCoordinators.isEmpty()){
 				for (Coordinator tHighestCoordinator : tHighestCoordinators){
 					tFound++;
 					if(tFound == 1){
 						Logging.log(this, "Found highest coordinator: " + tHighestCoordinator);
 						tHighestCoordinator.getCluster().distributeAddresses();
 					}else{
 						Logging.err(this, "Found highest coordinator nr. " + tFound + ": " + tHighestCoordinator);
 					}
 				}
 			}
 		}
 		if(tFound == 0){
 			Logging.err(this, "autoActivateAddressDistribution() hasn't found the highest coordinator");
 		}
 	}
 	
 	/**
 	 * Auto-activates reporting/sharing after AnnounceCoordinator packets were deactivated.
 	 */
 	private void autoActivateReportingSharing()
 	{
 		Logging.warn(this, "+++++++++++++++++++++++++++++++++++++++++++++++++");
 		Logging.warn(this, "+++ Activating reporting/sharing of topology data");
 		Logging.warn(this, "+++++++++++++++++++++++++++++++++++++++++++++++++");
 
 		GUI_USER_CTRL_REPORT_TOPOLOGY = true;
 		
 		// HINT: the report/share functions are triggered periodically and will start the start the reports/shares without any further setting
 	}
 	
 	/**
 	 * Validates the hierarchy creation
 	 */
 	private void validateResults()
 	{
 		if((!HRMConfig.Measurement.AUTO_DEACTIVATE_ANNOUNCE_COORDINATOR_PACKETS) || (!GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS)){
 			/**
 			 * Check coordinators
 			 */
 			for (Coordinator tCoordinator : getAllCoordinators()) {
 				if(!tCoordinator.getHierarchyLevel().isHighest()){
 					if(!tCoordinator.isSuperiorCoordinatorValid()){
 						Logging.err(this, "validateResults() detected invalid comm. channel to superior coordinator for: " + tCoordinator);
 					}
 				}
 //				for (ComChannel tComChannel : tCoordinator.getClusterMembershipComChannels()){
 //					if(tComChannel.getPeerPriority().isUndefined()){
 //						Logging.err(this, "validateResults() detected undefined peer priority for CoordinatorAsClusterMember channel: " + tComChannel);
 //					}
 //				}
 			}
 			
 			/**
 			 * Check cluster
 			 */
 			for (Cluster tCluster : getAllClusters()) {
 				HierarchyLevel tClusterLevel = tCluster.getHierarchyLevel();
 		
 				if(tClusterLevel.isHigherLevel()){
 					for (ComChannel tComChannel : tCluster.getComChannels()){
 						if(tComChannel.getPeerPriority().isUndefined()){
 							Logging.err(this, "validateResults() detected undefined peer priority for Cluster channel: " + tComChannel);
 						}else{
 							ElectionPriority tChannelPeerPriority = tComChannel.getPeerPriority();
 							L2Address tChanPeerL2Address = tComChannel.getPeerL2Address();
 							boolean tFound = false;
 							for(HRMController tHRMController : getALLHRMControllers()){
 								if(tHRMController.getNodeL2Address().equals(tChanPeerL2Address)){
 	//								Logging.log(this, "MATCH: " + tHRMController.getNodeL2Address() + " <==> " + tChanPeerL2Address);
 									tFound = true;
 									long tFoundPriority = tHRMController.getHierarchyNodePriority(tClusterLevel);
 									if(tFoundPriority != tChannelPeerPriority.getValue()){
 										Logging.err(this, "validateResults() detected wrong peer priority: " + tChannelPeerPriority.getValue() + " but it should be " + tFoundPriority + " for: " + tComChannel);
 									}
 									break;
 								}else{
 	//								Logging.log(this, "NO MATCH: " + tHRMController.getNodeL2Address() + " <==> " + tChanPeerL2Address);
 								}
 							}
 							if(!tFound){
 								Logging.err(this, "validateResults() wasn't able to find node: " + tChanPeerL2Address + " as peer of: " + tComChannel);
 							}
 						}
 					}
 				}
 			}
 		}
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
 		 * auto-deactivate AnnounceCoordinator 
 		 */
 		autoDeactivateAnnounceCoordinator();
 		
 		/**
 		 * auto-remove old CoordinatorProxies
 		 */
 		autoRemoveObsoleteCoordinatorProxies();
 
 		/**
 		 * auto-remove old HRG links
 		 */
 		autoRemoveObsoleteHRGLinks();
 		
 		/**
 		 * generalize all known HRM routes to neighbors 
 		 */
 //		generalizeMeighborHRMRoutesAuto();
 		
 		if(HRMConfig.Measurement.VALIDATE_RESULTS){
 			validateResults();
 		}
 		
 		if(GUI_USER_CTRL_REPORT_TOPOLOGY){
 			/**
 			 * detect local neighborhood and update HRG/HRMRouting
 			 */
 			for (ClusterMember tClusterMember : getAllL0ClusterMembers()) {
 				tClusterMember.detectNeighborhood();
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
 			if(GUI_USER_CTRL_SHARE_ROUTES){
 				for (Coordinator tCoordinator : getAllCoordinators()) {
 					tCoordinator.sharePhase();
 				}
 			}
 		}
 		
 		/**
 		 * auto-remove old HRM routes
 		 */
 		autoRemoveObsoleteHRMRoutes();
 
 		/**
 		 * register next trigger
 		 */
 		mAS.getTimeBase().scheduleIn(HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE, this);
 	}
 	
 	/**
 	 * Calculate the time period between "share phases" 
 	 *  
 	 * @param pHierarchyLevel the hierarchy level 
 	 * @return the calculated time period
 	 */
 	public double getPeriodSharePhase(int pHierarchyLevelValue)
 	{
 		switch(HRMConfig.Routing.REPORT_SHARE_PHASE_TIMING_SCHEME){
 			case CONSTANT:
 				return (double) 2 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * (0 + 1);
 			case LINEAR:
 				return (double) 2 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * (pHierarchyLevelValue + 1);
 			case EXPONENTIAL:
 				return (double) 2 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * (pHierarchyLevelValue + 1); //TODO: use an exponential time distribution here
 		}
 		
 		return 1;
 	}
 	
 	/**
 	 * Calculate the time period between "share phases" 
 	 *  
 	 * @param pHierarchyLevel the hierarchy level 
 	 * @return the calculated time period
 	 */
 	public double getPeriodReportPhase(HierarchyLevel pHierarchyLevel)
 	{
 		switch(HRMConfig.Routing.REPORT_SHARE_PHASE_TIMING_SCHEME){
 			case CONSTANT:
 				return (double) HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * (0 + 1);
 			case LINEAR:
 				return (double) HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * (pHierarchyLevel.getValue() + 1);
 			case EXPONENTIAL:
 				return (double) HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * (pHierarchyLevel.getValue() + 1); //TODO: use an exponential time distribution here
 		}
 		
 		return 1;
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
 	public L2Address getL2AddressOfFirstFNTowardsNeighbor(L2Address pNeighborName)
 	{
 		L2Address tResult = null;
 
 		if (pNeighborName != null){
 			if(!pNeighborName.equals(getHRS().getCentralFNL2Address())){
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
 						GateID tGateID = tPath.getFirst();				
 						
 						RoutingServiceLink tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor = null;
 	
 						boolean tWithoutException = false; //TODO: rework some software structures to avoid this ugly implementation
 						while(!tWithoutException){
 							try{
 								// get all outgoing links from the central FN
 								Collection<RoutingServiceLink> tOutgoingLinksFromCentralFN = getHRS().getOutgoingLinks(tCentralFNL2Address);
 								
 								// iterate over all outgoing links and search for the link from the central FN to the FN, which comes first when routing towards the neighbor
 								for(RoutingServiceLink tLink : tOutgoingLinksFromCentralFN) {
 									// compare the GateIDs
 									if(tLink.equals(tGateID)) {
 										// found!
 										tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor = tLink;
 									}
 								}
 								tWithoutException = true;
 							}catch(ConcurrentModificationException tExc){
 								// FoG has manipulated the topology data and called the HRS for updating the L2 routing graph
 								continue;
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
 					if(HRMConfig.Measurement.VALIDATE_RESULTS_EXTENSIVE){
 						Logging.warn(this, "Got for neighbor " + pNeighborName + " the route: " + tRoute); //HINT: this could also be a local loop -> throw only a warning
 					}
 				}
 			}else{
 				// we were ask for a route to our central FN: route is []
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
 		
 		/**
 		 * Reset global NMS is needed
 		 */
 		if(sResetNMS){
 			/**
 			 * Reset the DNS
 			 */
 			// register the HRMID in the hierarchical DNS for the local router
 			HierarchicalNameMappingService<HRMID> tNMS = null;
 			try {
 				tNMS = (HierarchicalNameMappingService) HierarchicalNameMappingService.getGlobalNameMappingService(mAS.getSimulation());
 			} catch (RuntimeException tExc) {
 				HierarchicalNameMappingService.createGlobalNameMappingService(getNode().getAS().getSimulation());
 			}				
 			tNMS.clear();	
 			sResetNMS = false;
 		}
 	}
 	
 	/**
 	 * This function gets called if the HRMController appl. should exit/terminate right now
 	 */
 	@Override
 	public synchronized void exit() 
 	{
 		if(!mApplicationStarted){
 			Logging.err(this, "This instance is already terminated.");
 			return;
 		}			
 		
 		mApplicationStarted = false;
 		
 		Logging.log(this, "\n\n\n############## Exiting..");
 		
 		Logging.log(this, "     ..destroying clusterer-thread");
 		if(mProcessorThread != null){
 			mProcessorThread.exit();
 			mProcessorThread = null;
 		}
 
 		Logging.log(this, "     ..destroying all clusters/coordinators");
 		for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			LinkedList<Cluster> tClusters = getAllClusters(i);
 			for(Cluster tCluster : tClusters){
 				tCluster.eventClusterRoleInvalid();
 			}
 		}
 		
 		synchronized (mCommunicationSessions) {
 			for (ComSession tComSession : mCommunicationSessions){
 				tComSession.stopConnection();
 			}
 		}
 		
 		// register in the global HRMController database
 		Logging.log(this, "     ..removing from the global HRMController database");
 		synchronized (mRegisteredHRMControllers) {
 			mRegisteredHRMControllers.remove(this);
 		}
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
 	private Description createHRMControllerDestinationDescription()
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
 			Logging.log(this, "REGISTERING LINK (ARG):\n  SOURCE=" + pFrom + "\n  DEST.=" + pTo + "\n  LINK=" + pLink);
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			pLink.setFirstVertex(pFrom);
 			pLink.setSecondVertex(pTo);
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
 			Logging.log(this, "UNREGISTERING LINK (ARG):\n  SOURCE=" + pFrom + "\n  DEST.=" + pTo + "\n  LINK=" + tLink);
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
 				/**
 				 * We haven't found a direct link - we found a multi-hop route instead.
 				 */
 				//Logging.warn(this, "getLinkARG() expected a route with one entry but got: \nSOURCE=" + pFrom + "\nDESTINATION: " + pTo + "\nROUTE: " + tRoute);
 			}
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
 			tResult = mAbstractRoutingGraph;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Registers an HRMID to the locally stored hierarchical routing graph (HRG)
 	 *  
 	 * @param pNode the node (HRMID) which should be stored in the HRG
 	 * @param pCause the cause for this HRG update
 	 */
 	private synchronized void registerNodeHRG(HRMID pNode, String pCause)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 			Logging.log(this, "REGISTERING NODE ADDRESS (HRG): " + pNode );
 		}
 
 		if(pNode.isZero()){
 			throw new RuntimeException(this + " detected a zero HRMID for an HRG registration");
 		}
 		
 		synchronized (mHierarchicalRoutingGraph) {
 			if(!mHierarchicalRoutingGraph.contains(pNode)) {
 				mDescriptionHRGUpdates += "\n + " + pNode + " <== " + pCause;
 				mHierarchicalRoutingGraph.add(pNode);
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..added to HRG");
 				}
 			}else{
 				mDescriptionHRGUpdates += "\n +/- " + pNode + " <== " + pCause;
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..node for HRG already known: " + pNode);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Unregisters an HRMID from the locally stored hierarchical routing graph (HRG)
 	 *  
 	 * @param pNode the node (HRMID) which should be removed from the HRG
 	 * @param pCause the cause for this HRG update
 	 */
 	private void unregisterNodeHRG(HRMID pNode, String pCause)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 			Logging.log(this, "UNREGISTERING NODE ADDRESS (HRG): " + pNode );
 		}
 		
 		synchronized (mHierarchicalRoutingGraph) {
 			if(mHierarchicalRoutingGraph.contains(pNode)) {
 				mDescriptionHRGUpdates += "\n - " + pNode + " <== " + pCause;
 				mHierarchicalRoutingGraph.remove(pNode);
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..removed from HRG");
 				}
 			}else{
 				mDescriptionHRGUpdates += "\n -/+ " + pNode + " <== " + pCause;
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..node for HRG wasn't known: " + pNode);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Registers a logical link between HRMIDs to the locally stored hierarchical routing graph (HRG)
 	 * 
 	 * @param pFrom the starting point of the link
 	 * @param pTo the ending point of the link
 	 * @param pRoutingEntry the routing entry for this link
 	 * 
 	 * @return true if the link is new to the routing graph
 	 */
 	public boolean registerLinkHRG(HRMID pFrom, HRMID pTo, RoutingEntry pRoutingEntry)
 	{
 		boolean tResult = false;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 			Logging.log(this, "REGISTERING LINK (HRG):\n  SOURCE=" + pFrom + "\n  DEST.=" + pTo + "\n  ROUTE=" + pRoutingEntry);
 		}
 	
 		if(!pFrom.equals(pTo)){
 			/**
 			 * Derive the link
 			 */
 			double tBefore4 = HRMController.getRealTime();
 			pRoutingEntry.assignToHRG(mHierarchicalRoutingGraph);
 			AbstractRoutingGraphLink tLink = new AbstractRoutingGraphLink(new Route(pRoutingEntry));
 			tLink.setTimeout(pRoutingEntry.getTimeout());
 			double tSpentTime4 = HRMController.getRealTime() - tBefore4;
 			if(tSpentTime4 > 10){
 				Logging.log(this, "      ..registerLinkHRG()::AbstractRoutingGraphLink() took " + tSpentTime4 + " ms for processing " + pRoutingEntry);
 			}
 
 			/**
 			 * Do the actual linking
 			 */
 			synchronized (mHierarchicalRoutingGraph) {
 				boolean tLinkAlreadyKnown = false;
 
 				double tBefore = HRMController.getRealTime();
 				Collection<AbstractRoutingGraphLink> tLinks = mHierarchicalRoutingGraph.getOutEdges(pFrom);
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 				if(tSpentTime > 10){
 					Logging.log(this, "      ..registerLinkHRG()::getOutEdges() took " + tSpentTime + " ms for processing " + pRoutingEntry);
 				}
 
 				if(tLinks != null){
 					double tBefore3 = HRMController.getRealTime();
 					for(AbstractRoutingGraphLink tKnownLink : tLinks){
 						// check if both links are equal 
 						if(tKnownLink.equals(tLink)){
 							// check of the end points of the already known link are equal to the pFrom/pTo
 							double tBefore2 = HRMController.getRealTime();
 							Pair<HRMID> tEndPoints = mHierarchicalRoutingGraph.getEndpoints(tKnownLink);
 							double tSpentTime2 = HRMController.getRealTime() - tBefore2;
 							if(tSpentTime2 > 10){
 								Logging.log(this, "      ..registerLinkHRG()::getEndpoints() took " + tSpentTime2 + " ms for processing " + pRoutingEntry);
 							}
 							if (((tEndPoints.getFirst().equals(pFrom)) && (tEndPoints.getSecond().equals(pTo))) || ((tEndPoints.getFirst().equals(pTo)) && (tEndPoints.getSecond().equals(pFrom)))){
 								tKnownLink.incRefCounter();
 								tLinkAlreadyKnown = true;
 								
 								/**
 								 * Update TIMEOUT
 								 */
 								if(pRoutingEntry.getTimeout() > 0){
 									tKnownLink.setTimeout(pRoutingEntry.getTimeout());
 								}
 								
 								/**
 								 * Update QOS VALUES
 								 */
 								tKnownLink.updateQoS(pRoutingEntry);
 								
 								// it's time to update the HRG-GUI
 								notifyHRGGUI(tKnownLink);
 							}
 						}
 					}
 					double tSpentTime3 = HRMController.getRealTime() - tBefore3;
 					if(tSpentTime3 > 10){
 						Logging.log(this, "      ..registerLinkHRG()::for() took " + tSpentTime3 + " ms for processing " + pRoutingEntry);
 					}
 				}
 				if(!tLinkAlreadyKnown){
 					mDescriptionHRGUpdates += "\n + " + pFrom + " to " + pTo + " ==> " + pRoutingEntry.toString() + " <== " + pRoutingEntry.getCause();
 
 					double tBefore1 = HRMController.getRealTime();
 					HRMID tFrom = pFrom.clone();
 					HRMID tTo = pTo.clone(); 
 					tLink.setFirstVertex(tFrom);
 					tLink.setSecondVertex(tTo);
 					mHierarchicalRoutingGraph.link(tFrom, tTo, tLink);
 
 					// it's time to update the HRG-GUI
 					notifyHRGGUI(tLink);
 
 					double tSpentTime1 = HRMController.getRealTime() - tBefore1;
 					if(tSpentTime1 > 10){
 						Logging.log(this, "      ..registerLinkHRG()::link() took " + tSpentTime1 + " ms for processing " + pRoutingEntry);
 					}
 				}else{
 					/**
 					 * The link is already known -> this can occur if:
 					 * 		- both end points are located on this node and both of them try to register the same route
 					 *      - a route was reported and received as shared
 					 */
 					if(HRMConfig.DebugOutput.MEMORY_CONSUMING_OPERATIONS){
 						mDescriptionHRGUpdates += "\n +" + (tLinkAlreadyKnown ? "(REF)" : "") + " " + pFrom + " to " + pTo + " ==> " + pRoutingEntry.toString() + " <== " + pRoutingEntry.getCause();
 					}
 				}
 				tResult = true;
 			}
 		}else{
 			//Logging.warn(this, "registerLinkHRG() skipped because self-loop detected for: " + pRoutingEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines all possible destinations depending on a given root node and its hierarchy level
 	 * 
 	 * @param pRootNode the root node
 	 * 
 	 * @return the found possible destinations
 	 */
 	public LinkedList<HRMID> getSiblingsHRG(HRMID pRootNode)
 	{
 		LinkedList<HRMID> tResult = new LinkedList<HRMID>();
 
 		HRMID tSuperCluster = pRootNode.getSuperiorClusterAddress();
 		
 		int tSearchedLvl = pRootNode.getHierarchyLevel();
 		
 		synchronized (mHierarchicalRoutingGraph) {
 			// iterate over all nodes in the HRG
 			Collection<HRMID> tNodes = mHierarchicalRoutingGraph.getVertices();			
 			for(HRMID tNode : tNodes){
 				if(!tNode.equals(pRootNode)){
 					// does the node belong to the same hierarchy level like the root node?
 					if(tNode.getHierarchyLevel() == tSearchedLvl){
 						if(tNode.isCluster(tSuperCluster)){
 							tResult.add(tNode.clone());
 						}else{
 							//Logging.log(this, "Dropping " + tNode + " as sibling of " + pRootNode);
 						}
 					}
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines a route in the HRG from a given node/cluster to another one
 	 * 
 	 * @param pFrom the starting point
 	 * @param pTo the ending point
 	 * @param pCause the cause for this call
 	 * 
 	 * @return the found routing entry
 	 */
 	public RoutingEntry getRoutingEntryHRG(HRMID pFrom, HRMID pTo, String pCause)
 	{
 		return getRoutingEntryHRG(mHierarchicalRoutingGraph, pFrom, pTo, pCause, null);
 	}
 	
 	/**
 	 * Determines a route in the HRG from a given node/cluster to another one
 	 * 
 	 * @param pHRG the HRG which should be used
 	 * @param pFrom the starting point
 	 * @param pTo the ending point
 	 * @param pCause the cause for this call
 	 * @param pRefDeletedLinks stores the first used inter-node link which was deleted automatically (used for finding multiple routes to the destination), a value "null" deactivates the automatic deletion of links
 	 * 
 	 * @return the found routing entry
 	 */
 	private RoutingEntry getRoutingEntryHRG(AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink> pHRG, HRMID pFrom, HRMID pTo, String pCause, LinkedList<LinkedList<AbstractRoutingGraphLink>> pRefDeletedLinks)
 	{
 		boolean DEBUG = false;
 		RoutingEntry tResult = null;
 		LinkedList<AbstractRoutingGraphLink> tDeletedLinks = null;
 		if(pRefDeletedLinks != null){
 			tDeletedLinks = pRefDeletedLinks.getFirst();
 		}
 		
 		if (DEBUG){
 			Logging.log(this, "getRoutingEntryHRG() searches a route from " + pFrom + " to " + pTo);
 		}
 		
 		synchronized (pHRG){
 			/**********************************************
 			 * Are the source and destination addresses equal?
 			 *********************************************/
 			if(pFrom.equals(pTo)){
 				// create a loop route
 				tResult = RoutingEntry.createLocalhostEntry(pFrom, pCause);
 							
 				// describe the cause for the route
 				tResult.extendCause(this + "::getRoutingEntry() with same source and destination address " + pFrom);
 				
 				// immediate return here
 				return tResult;
 			}
 			
 			/**********************************************
 			 * Is the source address more abstract than the destination one?
 			 * EXAMPLE 1: we are searching for a route from 1.3.0 to 1.4.2.  
 			 *********************************************/
 			if(pFrom.getHierarchyLevel() > pTo.getHierarchyLevel()){
 				/**
 				 * EXAMPLE 1: derive cluster address 1.4.0 from 1.4.2
 				 */			
 				HRMID tAbstractDestination = pTo.getClusterAddress(pFrom.getHierarchyLevel());
 				
 				if (DEBUG){
 					Logging.log(this, "getRoutingEntryHRG() searches a more abstract route from " + pFrom + " to more abstract destination " + tAbstractDestination);
 				}
 	
 				/**
 				 * EXAMPLE 1: determine the route from 1.3.0 to 1.4.0
 				 * 			  (assumption: the found route starts at 1.3.2 and ends at 1.4.1) 
 				 */
 				RoutingEntry tFirstRoutePart = getRoutingEntryHRG(pHRG, pFrom, tAbstractDestination, pCause, null);
 				if (DEBUG){
 					Logging.log(this, "          ..first route part: " + tFirstRoutePart);
 				}
 						
 				if(tFirstRoutePart != null){
 					HRMID tIngressGatewayToDestinationCluster = tFirstRoutePart.getLastNextHop();
 					/**
 					 * EXAMPLE 1: determine the route from 1.4.1 to 1.4.2
 					 */
 					RoutingEntry tIntraClusterRoutePart = getRoutingEntryHRG(pHRG, tIngressGatewayToDestinationCluster, pTo, pCause, null);
 					if (DEBUG){
 						Logging.log(this, "          ..second route part: " + tIntraClusterRoutePart);
 					}
 	
 					if(tIntraClusterRoutePart != null){
 						// clone the first part and use it as first part of the result
 						tResult = tFirstRoutePart.clone();
 						
 						/**
 						 * EXAMPLE 1: combine routes (1.3.2 => 1.4.1) AND (1.4.1 => 1.4.2)
 						 */
 						tResult.append(tIntraClusterRoutePart, pCause);
 						if (DEBUG){
 							Logging.log(this, "          ..resulting route (" + pFrom + " ==> " + tAbstractDestination + "): " + tResult);
 						}
 						
 						/**
 						 * EXAMPLE 1: the result is a route from gateway 1.3.2 (belonging to 1.3.0) to 1.4.2
 						 */
 					}else{
 						Logging.err(this, "getRoutingEntryHRG() couldn't determine an HRG route from " + tIngressGatewayToDestinationCluster + " to " + pTo + " as second part for a route from " + pFrom + " to " + pTo);
 					}
 				}else{
 					Logging.err(this, "getRoutingEntryHRG() couldn't determine an HRG route from " + pFrom + " to " + tAbstractDestination + " as first part for a route from " + pFrom + " to " + pTo);
 				}
 				
				if(tResult != null){
					tResult.extendCause(this + "::getRoutingEntry()");
				}
 						
 				return tResult;
 			}
 			
 			int tStep = 0;
 		
 			/*********************************************
 			 * Determine the overall inter-cluster path
 			 *********************************************/ 
 			List<AbstractRoutingGraphLink> tPath = getRouteHRG(pHRG, pFrom, pTo);
 			AbstractRoutingGraphLink tFirstUsedInterClusterLink = null;
 			if(tPath != null){
 				// the last cluster gateway
 				HRMID tLastClusterGateway = null;
 				HRMID tFirstForeignGateway = null;
 				
 				if(!tPath.isEmpty()){
 					if (DEBUG){
 						if (DEBUG){
 							Logging.log(this, "      ..found inter cluster path:");
 						}
 						int i = 0;
 						for(AbstractRoutingGraphLink tLink : tPath){
 							if (DEBUG){
 								Logging.log(this, "        ..inter-cluster step[" + i + "]: " + tLink);
 							}
 							i++;
 						}
 					}
 					
 					for(AbstractRoutingGraphLink tInterClusterLink : tPath){
 						/*****************************************************
 						 * Determine the current INTER-cluster route part
 						 ****************************************************/
 						RoutingEntry tInterClusterRoutingEntry = (RoutingEntry)tInterClusterLink.getRoute().getFirst().clone();
 						
 						if(tResult != null){
 							if(tLastClusterGateway == null){
 								throw new RuntimeException(this + "::getRoutingEntryHRG() should never reach this point");
 							}
 							
 							/************************************************************************************************
 							 * ROUTE PART: the intra-cluster route from the last gateway to the next one if needed
 							 ***********************************************************************************************/
 							// the next cluster gateway
 							HRMID tNextClusterGateway = tInterClusterRoutingEntry.getSource();
 							if(!tLastClusterGateway.equals(tNextClusterGateway)){
 								// the intra-cluster path
 								List<AbstractRoutingGraphLink> tIntraClusterPath = getRouteHRG(pHRG, tLastClusterGateway, tNextClusterGateway);
 								if(tIntraClusterPath != null){
 									if(!tIntraClusterPath.isEmpty()){
 										RoutingEntry tLogicalIntraClusterRoutingEntry = null;
 										AbstractRoutingGraphLink tIntraClusterLogLink = tIntraClusterPath.get(0);
 										
 										/****************************************************
 										 * Determine the INTRA-cluster route part
 										 ****************************************************/
 										// check if we have only one hop in intra-cluster route
 										if(tIntraClusterPath.size() == 1){
 											// get the routing entry from the last gateway to the next one
 											tLogicalIntraClusterRoutingEntry = (RoutingEntry) tIntraClusterLogLink.getRoute().getFirst();
 										}else{
 											tLogicalIntraClusterRoutingEntry = RoutingEntry.create(tIntraClusterPath);
 											if(tLogicalIntraClusterRoutingEntry == null){
 												if(pRefDeletedLinks == null){
 													Logging.warn(this, "getRoutingEntryHRG() for " + pFrom + " found a complex intra-cluster path from " + tLastClusterGateway + " to " + tNextClusterGateway + " and wasn't able to derive an aggregated logical link from it..");
 													Logging.warn(this, " 	..path: " + tIntraClusterPath);
 													Logging.warn(this, "      ..from: " + tLastClusterGateway);
 													Logging.warn(this, "      ..to: " + tNextClusterGateway);
 													Logging.warn(this, "    ..for a routing from " + pFrom + " to " + pTo);
 												}else{
 													// no further alternative route available
 												}
 												
 												// reset
 												tResult = null;
 	
 												// abort
 												break;
 											}
 										}
 	
 										/*****************************************************
 										 * Add the intra-cluster route part
 										 ****************************************************/
 										if(tLogicalIntraClusterRoutingEntry != null){
 											// chain the routing entries
 											if (DEBUG){
 												Logging.log(this, "        ..step [" + tStep + "] (intra-cluster): " + tLogicalIntraClusterRoutingEntry);
 											}
 											tResult.append(tLogicalIntraClusterRoutingEntry, pCause + "append1_intra_cluster from " + tLastClusterGateway + " to " + tNextClusterGateway);
 											tStep++;
 	
 											/**
 											 * Determine the next hop for the resulting path
 											 */
 											if(tFirstForeignGateway == null){
 												if(tLogicalIntraClusterRoutingEntry.getHopCount() > 0){
 													tFirstForeignGateway = tLogicalIntraClusterRoutingEntry.getNextHop();
 												}
 											}
 											
 											/******************************************************
 											 * Store the first used inter-node link (is it an intra-cluster link?)
 											 *****************************************************/
 //											if(tFirstUsedInterClusterLink == null){
 //												tFirstUsedInterClusterLink = tIntraClusterLogLink;
 //												tFirstUsedInterNodeLinkHopCount = tLogicalIntraClusterRoutingEntry.getHopCount();
 //											}else{
 //												if(tFirstUsedInterNodeLinkHopCount == RoutingEntry.NO_HOP_COSTS){
 //													tFirstUsedInterClusterLink = tIntraClusterLogLink;
 //													tFirstUsedInterNodeLinkHopCount = tLogicalIntraClusterRoutingEntry.getHopCount();
 //												}
 //											}
 										}
 									}else{
 										if(pRefDeletedLinks != null){
 											// do we have a gap?
 											if(!tLastClusterGateway.equals(tNextClusterGateway)){
 												// reset
 												tResult = null;
 	
 												// abort
 												break;
 											}else{
 												// actually, it is an empty path because source and destination are the same
 											}
 										}else{
 											Logging.warn(this, "getRoutingEntryHRG() found an empty intra-cluster path..");
 											Logging.warn(this, "      ..from: " + tLastClusterGateway);
 											Logging.warn(this, "      ..to: " + tNextClusterGateway);
 											Logging.warn(this, "    ..for a routing from " + pFrom + " to " + pTo);
 										}
 									}
 								}else{
 									Logging.warn(this, "getRoutingEntryHRG() couldn't find a route from " + tLastClusterGateway + " to " + tNextClusterGateway + " for a routing from " + pFrom + " to " + pTo);
 									
 									// reset
 									tResult = null;
 	
 									// abort
 									break;
 	
 									//HINT: do not throw a RuntimeException here because such a situation could have a temporary cause
 								}
 							}else{
 								// tLastClusterGateway and tNextClusterGateway are equal => empty route for cluster traversal
 							}
 							/***********************************************************************************************
 							 * ROUTE PART: the inter-cluster link
 							 ***********************************************************************************************/
 							// chain the routing entries
 							if (DEBUG){
 								Logging.log(this, "        ..step [" + tStep + "] (cluster-2-cluster): " + tInterClusterRoutingEntry);
 							}
 							tResult.append(tInterClusterRoutingEntry, pCause + "append2_inter_cluster for a route from " + pFrom + " to " + pTo);
 						}else{
 							/***********************************************************************************************
 							 * ROUTE PART: first step of the resulting path
 							 ***********************************************************************************************/
 							if (DEBUG){
 								Logging.log(this, "        ..step [" + tStep + "] (cluster-2-cluster): " + tInterClusterRoutingEntry);
 							}
 							tInterClusterRoutingEntry.extendCause(pCause + "append3_start_inter_cluster for a route from " + pFrom + " to " + pTo);
 							tResult = tInterClusterRoutingEntry;
 							
 						}
 						tStep++;
 						
 						/******************************************************
 						 * Store the first used inter-node link (is it an inter-cluster link?)
 						 *****************************************************/
 						if(tFirstUsedInterClusterLink == null){
 							tFirstUsedInterClusterLink = tInterClusterLink;
 //							tFirstUsedInterNodeLinkHopCount = tInterClusterRoutingEntry.getHopCount();
 //						}else{
 //							if(tFirstUsedInterNodeLinkHopCount == RoutingEntry.NO_HOP_COSTS){
 //								tFirstUsedInterClusterLink = tInterClusterLink;
 //								tFirstUsedInterNodeLinkHopCount = tInterClusterRoutingEntry.getHopCount();
 //							}
 						}
 
 						/******************************************************
 						 * Store the first used gateway ("next hop")
 						 *****************************************************/
 						if(tFirstForeignGateway == null){
 							if(tInterClusterRoutingEntry.getHopCount() > 0){
 								tFirstForeignGateway = tInterClusterRoutingEntry.getNextHop();
 							}
 						}
 						
 						//update last cluster gateway
 						tLastClusterGateway = tInterClusterRoutingEntry.getNextHop();
 					} // for()
 				}else{
 					if(pRefDeletedLinks == null){
 						Logging.err(this, "getRoutingEntryHRG() found an empty inter-cluster path from " + pFrom + " to " + pTo);
 					}else{
 						// no further alternative route found
 					}
 				}
 				
 				if(tResult != null){
 					/*******************************************************
 					 * finalize the RESULT
 					 ******************************************************/
 					// set the DESTINATION for the resulting routing entry
 					tResult.setDest(pFrom.getForeignCluster(pTo) /* aggregate the destination here */);
 	
 					// reset L2Address for next hop
 					tResult.setNextHopL2Address(null);
 	
 					/*******************************************************
 					 * Deactivate the first used inter-cluster link if desired
 					 ******************************************************/
 					if(tFirstUsedInterClusterLink != null){
 						if(tDeletedLinks != null){
 							if (DEBUG){
 								Logging.log(this, "  ..mark as deleted: " + tFirstUsedInterClusterLink);
 							}
 							
 							/**
 							 * Add the first used inter-node link to the list of deleted links
 							 */
 							tDeletedLinks.add(tFirstUsedInterClusterLink);
 							
 							/**
 							 * Delete the first used inter-node link from the HRG
 							 */
 							pHRG.unlink(tFirstUsedInterClusterLink);
 						}
 					}
 				}
 			}else{
 				Logging.err(this, "getRoutingEntryHRG() couldn't determine an HRG based inter-cluster route from " + pFrom + " to " + pTo);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines all routes in the HRG from a given node/cluster to another one
 	 * 
 	 * @param pFrom the starting point
 	 * @param pTo the ending point
 	 * @param pCause the cause for this call
 	 * 
 	 * @return the found routing entry
 	 */
 	public RoutingTable getAllRoutingEntriesHRG(HRMID pFrom, HRMID pTo, String pCause)
 	{
 		RoutingTable tResult = new RoutingTable();
 		LinkedList<AbstractRoutingGraphLink> tDeletedLinks = new LinkedList<AbstractRoutingGraphLink>();
 		LinkedList<LinkedList<AbstractRoutingGraphLink>> tRefDeletedLinks = new LinkedList<LinkedList<AbstractRoutingGraphLink>>();
 		tRefDeletedLinks.clear();
 		tRefDeletedLinks.add(tDeletedLinks);
 		
 		boolean DEBUG = false;
 //		if(pFrom.getLevelAddress(2) == 5){
 //			DEBUG = true;
 //		}
 
 		if(DEBUG){
 			Logging.log(this, "Searching for all routing entries from " + pFrom + " to " + pTo);
 		}
 		
 		/**************************************************
 		 * Are the source and destination addresses equal?
 		 **************************************************/
 		if(pFrom.equals(pTo)){
 			// create a loop route
 			RoutingEntry tLocalLoopEntry = RoutingEntry.createLocalhostEntry(pFrom, pCause);
 						
 			// describe the cause for the route
 			tLocalLoopEntry.extendCause(this + "::getRoutingEntry() with same source and destination address " + pFrom);
 			
 			// add the RoutingEntry to the result
 			tResult.add(tLocalLoopEntry);
 			
 			// immediate return here
 			return tResult;
 		}
 
 		/***************************************************************************************************
 		 * Determine all known routes, avoid repeated routes by deleting already used outgoing links from "pFrom"
 		 ***************************************************************************************************/
 		synchronized (mHierarchicalRoutingGraph) {
 			RoutingEntry tEntry = null;
 			int i = 0;
 			do{
 				tEntry = getRoutingEntryHRG(mHierarchicalRoutingGraph, pFrom, pTo, pCause, tRefDeletedLinks);
 				if(tEntry != null){
 					if(tEntry != null){
 						if(!tResult.contains(tEntry)){
 							if(DEBUG){
 								Logging.log(this, "  ..found entry[" + i + "]: " + tEntry);
 								Logging.log(this, "    ..deleted " + tDeletedLinks.size() + " links");
 							}
 		
 							// add the RoutingEntry to the result
 							tResult.add(tEntry);					
 							
 							i++;
 						}else{
 							if(DEBUG){
 								Logging.log(this, "  ..found repeated entry: " + tEntry);
 							}
 						}
 					}
 				}
 			}while(tEntry != null);
 		}
 		
 		/**************************************************
 		 * Add again the previously deleted HRG links
 		 **************************************************/
 		if(!tDeletedLinks.isEmpty()){
 			for(AbstractRoutingGraphLink tLink : tDeletedLinks){
 				if(DEBUG){
 					Logging.log(this, "  ..restoring HRG link: " + tLink);
 				}
 						
 				/**
 				 * Add all previously deleted used inter-node links again to the HRG
 				 */
 				mHierarchicalRoutingGraph.link((HRMID)tLink.getFirstVertex(), (HRMID)tLink.getSecondVertex(), tLink);
 			}
 		}
 		
 		/**************************************************
 		 * Check if we have found too long routes 
 		 **************************************************/
 		RoutingTable tTooLongRoutes = new RoutingTable();
 		for(RoutingEntry tEntry : tResult){
 			if(tEntry.getDest().isClusterAddress()){
 				if(tEntry.getHopCount() > RoutingEntry.NO_HOP_COSTS){
 					HRMID tDestinationCluster = tEntry.getDest();
 					HRMID tSourceCluster = tEntry.getDest().getForeignCluster(tEntry.getSource());
 					RoutingEntry tShortestEntry = getRoutingEntryHRG(tSourceCluster, tDestinationCluster, this + "::getAllRoutingEntriesHRG()");
 					// are the source and the destination clusters directly connected?
 					if(tShortestEntry.getHopCount() == RoutingEntry.NO_HOP_COSTS){
 						if(DEBUG){
 							Logging.log(this, "  ..removing the too long route: " + tEntry);
 						}
 						tTooLongRoutes.add(tEntry);
 					}
 				}
 			}
 		}
 		// delete entries with "too long routes"
 ///		tResult.delEntries(tTooLongRoutes);
 		
 		return tResult;
 	}
 	
 	/**
 	 * Unregisters automatically old links from the HRG based on each link's timeout value
 	 */
 	public void autoRemoveObsoleteHRGLinks()
 	{
 		synchronized (mHierarchicalRoutingGraph) {
 			Collection<AbstractRoutingGraphLink> tLinks = mHierarchicalRoutingGraph.getEdges();
 			for(AbstractRoutingGraphLink tLink : tLinks){
 				// does the link have a timeout?
 				if(tLink.getTimeout() > 0){
 					// timeout occurred?
 					if(tLink.getTimeout() < getSimulationTime()){
 						Pair<HRMID> tEndPoints = mHierarchicalRoutingGraph.getEndpoints(tLink);
 	
 						// remove the link from the HRG
 						mHierarchicalRoutingGraph.unlink(tLink);
 						
 						mDescriptionHRGUpdates += "\n -(AUTO_DEL) " + tEndPoints.getFirst() + " to " + tEndPoints.getSecond() + " ==> " + tLink.getRoute().getFirst()  + " <== unregisterAutoHRG()";
 					}
 				}
 			}		
 		}
 		
 		/**
 		 * Unregister all isolated nodes
 		 */
 		unregisterNodesAutoHRG(this + "::unregisterAutoHRG()");
 	}
 
 	/**
 	 * Unregisters automatically old HRM routes based on each route entrie's timeout value
 	 */
 	private void autoRemoveObsoleteHRMRoutes()
 	{
 		RoutingTable tRoutingTable = mHierarchicalRoutingService.getRoutingTable();
 		for(RoutingEntry tEntry : tRoutingTable){
 			// does the link have a timeout?
 			if(tEntry.getTimeout() > 0){
 				// timeout occurred?
 				if(tEntry.getTimeout() < getSimulationTime()){
 					RoutingEntry tDeleteThis = tEntry.clone();
 					tDeleteThis.extendCause(this + "::autoRemoveObsoleteHRMRoutes()");
 					Logging.log(this, "Timeout (" + tEntry.getTimeout() + "<" + getSimulationTime() + ") for: " + tDeleteThis);
 					delHRMRoute(tDeleteThis);
 				}
 			}
 		}		
 	}
 
 	/**
 	 * Unregisters a logical link between HRMIDs from the locally stored hierarchical routing graph (HRG)
 	 * 
 	 * @param pFrom the starting point of the link
 	 * @param pTo the ending point of the link
 	 * @param pRoutingEntry the routing entry of the addressed link
 	 * 
 	 * @return if the link was found in the HRG
 	 */
 	public boolean unregisterLinkHRG(HRMID pFrom, HRMID pTo, RoutingEntry pRoutingEntry)
 	{
 		boolean tResult = false;
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 			Logging.log(this, "UNREGISTERING LINK (HRG):\n  SOURCE=" + pFrom + "\n  DEST.=" + pTo + "\n  LINK=" + pRoutingEntry);
 		}
 
 		if(!pFrom.equals(pTo)){
 			pRoutingEntry.assignToHRG(mHierarchicalRoutingGraph);
 	
 			AbstractRoutingGraphLink tSearchPattern = new AbstractRoutingGraphLink(new Route(pRoutingEntry));
 
 			boolean tChangedRefCounter = false;
 			synchronized (mHierarchicalRoutingGraph) {
 				//Logging.warn(this, "   ..knowing node: " + pFrom + " as " + mHierarchicalRoutingGraph.containsVertex(pFrom));
 				// get all outgoing HRG links of "pFrom"
 				Collection<AbstractRoutingGraphLink> tOutLinks = mHierarchicalRoutingGraph.getOutEdges(pFrom);
 				if(tOutLinks != null){
 					// iterate over all found links
 					for(AbstractRoutingGraphLink tKnownLink : tOutLinks) {
 						//Logging.warn(this, "     ..has link: " + tKnownLink);
 						Pair<HRMID> tEndPoints = mHierarchicalRoutingGraph.getEndpoints(tKnownLink);
 						if (((tEndPoints.getFirst().equals(pFrom)) && (tEndPoints.getSecond().equals(pTo))) ||
 							((tEndPoints.getFirst().equals(pTo)) && (tEndPoints.getSecond().equals(pFrom)))){
 							if(tKnownLink.equals(tSearchPattern)){
 								//Logging.warn(this, "       ..MATCH");
 //								if(tKnownLink.getRefCounter() == 1){
 									// remove the link
 									mHierarchicalRoutingGraph.unlink(tKnownLink);
 									
 									// it's time to update the HRG-GUI
 									notifyHRGGUI(null);
 //								}else{
 //									if(tKnownLink.getRefCounter() < 1){
 //										throw new RuntimeException("Found an HRG link with an invalid ref. counter: " + tKnownLink);
 //									}
 //									
 //									tKnownLink.decRefCounter();
 //									tChangedRefCounter = true;
 //									
 //									// it's time to update the HRG-GUI
 //									notifyHRGGUI(tKnownLink);
 //								}
 //								// we have a positive result
 								tResult = true;
 								// work is done
 								break;
 							}else{
 								//Logging.warn(this, "       ..NO MATCH");
 							}
 						}
 					}
 				}
 			}
 						
 			if(!tResult){
 				/**
 				 * The route was already removed -> this can occur if both end points of a link are located on this node and both of them try to unregister the same route
 				 */
 				mDescriptionHRGUpdates += "\n -/+ " + pFrom + " to " + pTo + " ==> " + pRoutingEntry.toString() + " <== " + pRoutingEntry.getCause();
 				Logging.warn(this, "Haven't found " + pRoutingEntry + " as HRG link between " + pFrom + " and " + pTo);
 //				if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 					synchronized (mHierarchicalRoutingGraph) {
 						
 						Collection<AbstractRoutingGraphLink> tLinks = mHierarchicalRoutingGraph.getOutEdges(pFrom);
 						if(tLinks != null){
 							if(tLinks.size() > 0){
 								Logging.warn(this, "   ..knowing FROM node: " + pFrom);
 								for(AbstractRoutingGraphLink tKnownLink : tLinks){
 									Logging.warn(this, "     ..has link: " + tKnownLink);
 									if(tKnownLink.equals(tSearchPattern)){
 										Logging.err(this, "       ..MATCH");
 									}else{
 										Logging.warn(this, "       ..NO MATCH");
 									}
 								}
 							}
 						}
 						
 						tLinks = mHierarchicalRoutingGraph.getOutEdges(pTo);
 						if(tLinks != null){
 							if(tLinks.size() > 0){
 								Logging.warn(this, "   ..knowing TO node: " + pFrom);
 								for(AbstractRoutingGraphLink tKnownLink : tLinks){
 									Logging.warn(this, "     ..has link: " + tKnownLink);
 									if(tKnownLink.equals(tSearchPattern)){
 										Logging.err(this, "       ..MATCH");
 									}else{
 										Logging.warn(this, "       ..NO MATCH");
 									}
 								}
 							}
 						}
 					}
 //				}
 			}else{
 				mDescriptionHRGUpdates += "\n -" + (tChangedRefCounter ? "(REF)" : "") +" " + pFrom + " to " + pTo + " ==> " + pRoutingEntry.toString() + " <== " + pRoutingEntry.getCause();
 
 				/**
 				 * Unregister all isolated nodes
 				 */
 				unregisterNodesAutoHRG(pRoutingEntry + ", " + this + "::unregisterLinkHRG()_autoDel");
 			}
 		}else{
 			//Logging.warn(this, "unregisterLinkHRG() skipped because self-loop detected for: " + pRoutingEntry);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Unregister automatically all HRG nodes which don't have a link anymore 
 	 */
 	private void unregisterNodesAutoHRG(String pCause)
 	{
 		/**
 		 * Iterate over all nodes and delete all of them which don't have any links anymore
 		 */
 		boolean tRemovedSomething = false;
 		synchronized (mHierarchicalRoutingGraph) {
 			boolean tRemovedANode;
 			do{
 				tRemovedANode = false;
 
 				Collection<HRMID> tNodes = mHierarchicalRoutingGraph.getVertices();
 				for(HRMID tKnownNode : tNodes){
 					Collection<AbstractRoutingGraphLink> tOutLinks = mHierarchicalRoutingGraph.getOutEdges(tKnownNode);
 					Collection<AbstractRoutingGraphLink> tInLinks = mHierarchicalRoutingGraph.getInEdges(tKnownNode);
 					if((tOutLinks != null) && (tInLinks != null)){
 						if((tInLinks.size() == 0) && (tOutLinks.size() == 0)){
 							 // unregister the HRMID in the HRG
 							unregisterNodeHRG(tKnownNode, pCause);
 							tRemovedANode = true;
 							tRemovedSomething = true;
 							break;
 						}
 					}
 				}
 			}while(tRemovedANode);
 		}
 		
 		if(tRemovedSomething){
 			// it's time to update the HRG-GUI
 			notifyHRGGUI(null);
 		}
 	}
 	
 	/**
 	 * Returns a list of direct neighbors of the given HRMID which are stored in the HRG
 	 *  
 	 * @param pHRMID the root HRMID
 	 * 
 	 * @return the list of direct neighbors
 	 */
 	public LinkedList<HRMID> getNeighborsHRG(HRMID pHRMID)
 	{
 		LinkedList<HRMID> tResult = new LinkedList<HRMID>();
 		
 		synchronized (mHierarchicalRoutingGraph) {
 			//Logging.warn(this, "   ..knowing node: " + pFrom + " as " + mHierarchicalRoutingGraph.containsVertex(pFrom));
 			// get all outgoing HRG links of "pFrom"
 			Collection<AbstractRoutingGraphLink> tOutLinks = mHierarchicalRoutingGraph.getOutEdges(pHRMID);
 			if(tOutLinks != null){
 				for(AbstractRoutingGraphLink tOutLink : tOutLinks){
 					HRMID tNeighbor = mHierarchicalRoutingGraph.getDest(tOutLink);
 					tResult.add(tNeighbor.clone());
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns routes to neighbors of a given HRG node
 	 * 
 	 * @param pHRMID the HRMID of the HRG root node
 	 *  
 	 * @return the routing table
 	 */
 	public RoutingTable getReportRoutesToNeighborsHRG(HRMID pHRMID)
 	{
 		RoutingTable tResult = new RoutingTable();
 		
 		synchronized (mHierarchicalRoutingGraph) {
 			//Logging.warn(this, "   ..knowing node: " + pFrom + " as " + mHierarchicalRoutingGraph.containsVertex(pFrom));
 			// get all outgoing HRG links of "pFrom"
 			Collection<AbstractRoutingGraphLink> tOutLinks = mHierarchicalRoutingGraph.getOutEdges(pHRMID);
 			if(tOutLinks != null){
 				// iterate over all found links
 				for(AbstractRoutingGraphLink tKnownLink : tOutLinks) {
 					Route tKnownLinkRoute = tKnownLink.getRoute();
 					if(tKnownLinkRoute.size() == 1){
 						if(tKnownLinkRoute.getFirst() instanceof RoutingEntry){
 							RoutingEntry tRouteToNeighbor = ((RoutingEntry)tKnownLinkRoute.getFirst()).clone();
 							tRouteToNeighbor.extendCause(this + "::getRoutesWithNeighborsHRG() for " + pHRMID);
 							// reset next hop L2Address
 							tRouteToNeighbor.setNextHopL2Address(null);
 							tResult.add(tRouteToNeighbor);
 						}else{
 							throw new RuntimeException("getRoutesToNeighborsHRG() detected an unsupported route type: " + tKnownLinkRoute);
 						}
 					}else{
 						throw new RuntimeException("getRoutesToNeighborsHRG() detected an unsupported route size for: " + tKnownLinkRoute);
 					}
 				}
 			}
 			Collection<AbstractRoutingGraphLink> tInLinks = mHierarchicalRoutingGraph.getInEdges(pHRMID);
 			if(tInLinks != null){
 				// iterate over all found links
 				for(AbstractRoutingGraphLink tKnownLink : tInLinks) {
 					Route tKnownLinkRoute = tKnownLink.getRoute();
 					if(tKnownLinkRoute.size() == 1){
 						if(tKnownLinkRoute.getFirst() instanceof RoutingEntry){
 							RoutingEntry tRouteToNeighbor = ((RoutingEntry)tKnownLinkRoute.getFirst()).clone();
 							tRouteToNeighbor.extendCause(this + "::getRoutesWithNeighborsHRG() for " + pHRMID);
 							// reset next hop L2Address
 							tRouteToNeighbor.setNextHopL2Address(null);
 							tResult.add(tRouteToNeighbor);
 						}else{
 							throw new RuntimeException("getRoutesToNeighborsHRG() detected an unsupported route type: " + tKnownLinkRoute);
 						}
 					}else{
 						throw new RuntimeException("getRoutesToNeighborsHRG() detected an unsupported route size for: " + tKnownLinkRoute);
 					}
 				}
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Registers a link between two clusters.
 	 * 
 	 * @param pFromHRMID the start of the link
 	 * @param pToHRMID the end of the link
 	 * @param pRoutingEntry the routing entry for this link
 	 * 
 	 * @return true if the link is new to the routing graph
 	 */
 	private boolean registerCluster2ClusterLinkHRG(HRMID pFromHRMID, HRMID pToHRMID, RoutingEntry pRoutingEntry)
 	{
 		boolean tResult = false;
 		
 		/**
 		 * Store/update link in the HRG
 		 */ 
 		tResult = registerLinkHRG(pFromHRMID, pToHRMID, pRoutingEntry);
 		if(tResult){
 			if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 				Logging.log(this, "Stored cluster-2-cluster link between " + pFromHRMID + " and " + pToHRMID + " in the HRG as: " + pRoutingEntry);
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Unregisters a link between two clusters.
 	 * 
 	 * @param pFromHRMID the start of the link
 	 * @param pToHRMID the end of the link
 	 * @param pRoutingEntry the routing entry for this link
 	 * 
 	 * @return if the link was found in the HRG
 	 */
 	private boolean unregisterCluster2ClusterLinkHRG(HRMID pFromHRMID, HRMID pToHRMID, RoutingEntry pRoutingEntry)
 	{
 		boolean tResult = false;
 
 		/**
 		 * Store/update link in the HRG
 		 */ 
 		tResult = unregisterLinkHRG(pFromHRMID, pToHRMID, pRoutingEntry);
 		if(tResult){
 			if (HRMConfig.DebugOutput.GUI_SHOW_HRG_DETECTION){
 				Logging.log(this, "Removed cluster-2-cluster link between " + pFromHRMID + " and " + pToHRMID + " from the HRG as: " + pRoutingEntry);
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines a path in the locally stored hierarchical routing graph (HRG).
 	 * 
 	 * @param pSource the source of the desired route
 	 * @param pDestination the destination of the desired route
 	 * 
 	 * @return the determined route, null if no route could be found
 	 */
 	public List<AbstractRoutingGraphLink> getRouteHRG(HRMID pSource, HRMID pDestination)
 	{
 		return getRouteHRG(mHierarchicalRoutingGraph, pSource, pDestination);
 	}
 	
 	/**
 	 * Determines a path in the locally stored hierarchical routing graph (HRG).
 	 * 
 	 * @param pHRG the HRG which should be used
 	 * @param pSource the source of the desired route
 	 * @param pDestination the destination of the desired route
 	 * 
 	 * @return the determined route, null if no route could be found
 	 */
 	public List<AbstractRoutingGraphLink> getRouteHRG(AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink> pHRG, HRMID pSource, HRMID pDestination)
 	{
 		List<AbstractRoutingGraphLink> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET ROUTE (HRG) from \"" + pSource + "\" to \"" + pDestination +"\"");
 		}
 
 		synchronized (pHRG) {
 			tResult = pHRG.getRoute(pSource, pDestination);
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "        ..getRouteHRG() result: " + tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the routing entry from a HRG link between two HRG nodes (neighbors)
 	 * 
 	 * @param pSource the starting point of the searched link
 	 * @param pDestination the ending point of the search link
 	 * 
 	 * @return the search routing entry
 	 */
 	public RoutingEntry getNeighborRoutingEntryHRG(HRMID pSource, HRMID pDestination)
 	{
 		RoutingEntry tResult = null;
 		
 		List<AbstractRoutingGraphLink> tPath = getRouteHRG(pSource, pDestination);
 		if(tPath != null){
 			if(tPath.size() == 1){
 				AbstractRoutingGraphLink tLink = tPath.get(0);
 				// get the routing entry from the last gateway to the next one
 				tResult = ((RoutingEntry) tLink.getRoute().getFirst()).clone();
 			}else{
 				Logging.warn(this, "getRoutingEntryHRG() found a complex intra-cluster route: " + tPath + " from " + pSource + " to " + pDestination);
 			}
 		}else{
 			// no route found
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the HRG for the GraphViewer.
 	 * (only for GUI!)
 	 * 
 	 * @return the HRG
 	 */
 	public AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink> getHRGForGraphViewer()
 	{
 		AbstractRoutingGraph<HRMID, AbstractRoutingGraphLink> tResult = null;
 		
 		synchronized (mHierarchicalRoutingGraph) {
 			tResult = mHierarchicalRoutingGraph; 
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: probe routing
 	 * 
 	 * @param pProbeRoutingProperty the property of the received incoming connection
 	 */
 	private void eventProbeRouting(ProbeRoutingProperty pProbeRoutingProperty)
 	{
 		/**
 		 * We have a probe-routing connection and will print some additional information about the taken route of the connection request
 		 */
 		// get the recorded route from the property
 		LinkedList<HRMID> tRecordedHRMIDs = pProbeRoutingProperty.getRecordedHops();
 		
 		String tDesiredDataRate = "";
 		if(pProbeRoutingProperty.getDesiredDataRate() >= 1000000)
 			tDesiredDataRate += (pProbeRoutingProperty.getDesiredDataRate() / 1000000) + " Gbit/s";
 		else if(pProbeRoutingProperty.getDesiredDataRate() >= 1000)
 			tDesiredDataRate += (pProbeRoutingProperty.getDesiredDataRate() / 1000) + " Mbit/s";
 		else
 			tDesiredDataRate += pProbeRoutingProperty.getDesiredDataRate() + " kbit/s";
 
 		String tPossibleDataRate = "";
 		if(pProbeRoutingProperty.getRecordedDataRate() >= 1000000)
 			tPossibleDataRate += (pProbeRoutingProperty.getRecordedDataRate() / 1000000) + " Gbit/s";
 		else if(pProbeRoutingProperty.getRecordedDataRate() >= 1000)
 			tPossibleDataRate += (pProbeRoutingProperty.getRecordedDataRate() / 1000) + " Mbit/s";
 		else
 			tPossibleDataRate += pProbeRoutingProperty.getRecordedDataRate() + " kbit/s";
 
 		Logging.log(this, "     ..detected a probe-routing connection(source=" + pProbeRoutingProperty.getSourceDescription());
 		Logging.log(this, "       ..source: " + tRecordedHRMIDs.getFirst());
 		Logging.log(this, "       ..destination: " + pProbeRoutingProperty.getDest());
 		Logging.log(this, "       ..desired E2E data rate: " + tDesiredDataRate);
 		Logging.log(this, "       ..desired E2E delay: " + pProbeRoutingProperty.getDesiredDelay() + " ms");
 		Logging.log(this, "       ..recorded max. E2E data rate: " + tPossibleDataRate + " (this is the worst max. avilable data rate along the taken route)");
 		Logging.log(this, "       ..recorded min. E2E delay: " + pProbeRoutingProperty.getRecordedDelay() + " ms (this is the sum of all delays of all used links)");
 		Logging.log(this, "       ..recorded HOP count: " + pProbeRoutingProperty.getRecordedHopCount() + " nodes (this represents the list of passed physical hosts)");
 		Logging.log(this, "       ..passed " + tRecordedHRMIDs.size() + " HRM hops: (this represents the list of passed physical interfaces)");
 
 		// print the recorded route
 		int i = 0;
 		for(HRMID tHRMID : tRecordedHRMIDs){
 			if(i % 2 == 0){
 				Logging.log(this, "        ..source[" + i + "]: " + tHRMID);
 			}else{
 				Logging.log(this, "          ..hop[" + i + "]: " + tHRMID);
 			}
 			i++;
 		}
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
 			ComSession tComSession = new ComSession(this);
 
 			/**
 			 * Start the communication session
 			 */					
 			Logging.log(this, "     ..starting communication session for the new connection");
 			tComSession.startConnection(null, pConnection);
 		}else{
 			eventProbeRouting(tPropProbeRouting);
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
 	@SuppressWarnings("deprecation")
 	public Name getNodeName()
 	{
 		// get the name of the central FN of this node
 		if(getHRS().getCentralFN() != null){
 			return getHRS().getCentralFN().getName();
 		}else{
 			return null;
 		}
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
 	
 	/**
 	 * Stores the identification string for HRM specific routing graph decorations (NMS entries)
 	 */
 	private final static String DECORATION_NAME_NMS_ENTRIES = "HRM(5) - NMS entries";
 }
