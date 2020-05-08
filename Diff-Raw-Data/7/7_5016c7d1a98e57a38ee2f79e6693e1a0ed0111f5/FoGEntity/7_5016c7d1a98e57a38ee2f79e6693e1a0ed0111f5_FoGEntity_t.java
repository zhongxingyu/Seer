 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.application.util.LayerObserverCallback;
 import de.tuilmenau.ics.fog.authentication.IdentityManagement;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Layer;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.CommunicationTypeProperty;
 import de.tuilmenau.ics.fog.packets.statistics.ReroutingExperiment;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.RoutingServiceMultiplexer;
 import de.tuilmenau.ics.fog.topology.NeighborList;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.topology.SimulationElement;
 import de.tuilmenau.ics.fog.transfer.TransferPlane;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ClientFN;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ConnectionEndPoint;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ServerFN;
 import de.tuilmenau.ics.fog.transfer.manager.Controller;
 import de.tuilmenau.ics.fog.transfer.manager.Process;
 import de.tuilmenau.ics.fog.transfer.manager.ProcessConnection;
 import de.tuilmenau.ics.fog.transfer.manager.ProcessRegister;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.EventSourceBase;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.graph.GraphProvider;
 import de.tuilmenau.ics.graph.RoutableGraph;
 
 
 /**
  * A FoGEntity represents an instance of a FoG layer on a node.
  */
 public class FoGEntity extends EventSourceBase implements Layer, GraphProvider, SimulationElement
 {
 	public FoGEntity(Node pNode)
 	{
 		mNode = pNode;
 		
 		controlgate = new Controller(this);
 		transferPlane = new TransferPlane(getTimeBase(), getLogger());
 		
 		// Note: Do not create central FN here, because we do not have
 		//       a routing service available.
 		multiplexgate = null;
 	}
 	
 	@Override
 	public Binding bind(Connection pParentSocket, Name pName, Description pDescription, Identity pIdentity)
 	{
 		// make sure there is no null point in
 		if(pDescription == null) pDescription = Description.createEmpty();
 		
 		// Create named server forwarding node
 		ServerFN tAppFN = new ServerFN(this, pName, NamingLevel.NAMES, pDescription, pIdentity);
 		tAppFN.open();
 		
 		// TODO use pParentSocket to look at which multiplexer to add server
 		// insert new FN in transfer plane
 		tAppFN.connectMultiplexer(getCentralFN());
 		
 		// add server to the internal list for being able to enumerate all server applications
 		synchronized(mRegisteredServers){
 			mRegisteredServers.add(pName);	
 		}
 
 		return tAppFN.getBinding();
 	}
 	
 	/**
 	 * Returns all server names registered at this host.
 	 * 
 	 * @return Reference to the list of registered server applications for this host
 	 */
 	public LinkedList<Name> getServerNames()
 	{
 		return mRegisteredServers;		
 	}
 	
 	private class WaitForSocketContinuation implements IContinuation<Process>
 	{
 		public WaitForSocketContinuation(ProcessConnection pProcess, ConnectionEndPoint pCEP)
 		{
 			mProcess = pProcess;
 			mCEP = pCEP;
 		}
 
 		@Override
 		public void success(Process pCaller)
 		{
 			handleResult();
 		}
 
 		@Override
 		public void failure(Process pCaller, Exception pException)
 		{
 			handleResult();
 		}
 		
 		private void handleResult()
 		{
 			/*
 			 * get and check result
 			 */
 			ClientFN tRes = mProcess.getEndForwardingNode();
 			if(tRes == null) {
 				mCEP.setError(new NetworkException("Can not construct up gates for socket.", mProcess.getTerminationCause()));
 			} else {
 				if(!tRes.isConnected()) {
 					mCEP.setError(new NetworkException("Socket can not connect to peer.", mProcess.getTerminationCause()));
 				} else {
 					mCEP.setForwardingNode(tRes);
 					tRes.setConnectionEndPoint(mCEP);
 					mCEP.connect();
 				}
 			}
 		}
 		
 		private ProcessConnection mProcess;
 		private ConnectionEndPoint mCEP;
 	};
 	
 	@Override
 	public Connection connect(Name pName, Description pDescription, Identity pRequester)
 	{
 		ConnectionEndPoint tCEP = new ConnectionEndPoint(pName, getLogger(), null);
 		
 		// do not start calculation without a useful name
 		if(pName == null) {
 			// throw a short message to the console in case no event lister is used for the CEP
 			getLogger().err(this, "Can not connect without a destination name.");
 			
 			tCEP.setError(new NetworkException("Can not connect without a destination name."));
 			return tCEP;
 		}
 		
 		// check if name is known; otherwise we can skip the gate creation stuff
 		if(!getRoutingService().isKnown(pName)) {
 			tCEP.setError(new NetworkException(this, "Name " +pName +" is not known to routing service."));
 			return tCEP;
 		}
 		
 		// make sure there is no null pointer
 		if(pDescription == null) pDescription = Description.createEmpty();
 		
 		// in which name do we start the creation and the signaling?
 		if(pRequester == null) {
 			pRequester = getIdentity();
 			getLogger().info(this, "Connect to " +pName +" in the name of the node " +mNode +" (=" +pRequester +")");
 		}
 		
 		// select FN the connection should be added to; default: central FN
 		Multiplexer tMultiplexer = getCentralFN();
 		
 		// Create constructing process.
 		ProcessConnection tProcess = new ProcessConnection(tMultiplexer, pName, pDescription, pRequester);
 
 		// block fast mode in intermediate time between
 		// stating timer for process timeout and sending the
 		// first packet
 		boolean isInFastMode = mNode.getTimeBase().isInFastMode();
 		if(isInFastMode) {
 			mNode.getTimeBase().setFastMode(false);
 		}
 		
 		try {
 			/*
 			 * Create and register client FN.
 			 */
 			tProcess.start();
 			
 			/*
 			 * Build path from local base FN to local client FN.
 			 */
 			if(Config.Connection.LAZY_INITIATOR) {
 				// Socket-path will not be created before a handshake arrives.
 				// Do nothing here.
 			} else {
 				// Socket-path will be created instantly.
 				tProcess.recreatePath(pDescription, null);
 			}
 	
 			/*
 			 * Calculate route for intermediate functions
 			 */
 			Description tIntermediateDescr = tProcess.getIntermediateDescr();
 			Logging.log(this, "Determined intermediate description: " + tIntermediateDescr + ", ORIGINAL REQS: " + pDescription);
 			Route tRoute = getTransferPlane().getRoute(tMultiplexer, pName, tIntermediateDescr, pRequester);
 	
 			/*
 			 * Register for notification of state changes now, since "handlePacket" might cause it immediately.
 			 */
 			IContinuation<Process> tCont = new WaitForSocketContinuation(tProcess, tCEP);
 			tProcess.observeNextStateChange(-1.0d, tCont);
 			
 			if(signalingRequired(pDescription)) {
 				/*
 				 * Send request to remote system.
 				 */
 				tProcess.signal(true, tRoute);
 			} else {
 				/*
 				 * No signaling required; use partial route and start right away.
 				 */
 				tProcess.updateRoute(tRoute, null);
 			}
 		}
 		catch(Exception exc) {
 			mNode.getLogger().err(this, "Exception during connect to " +pName, exc);
 			
 			// something went wrong => terminate process
 			tProcess.terminate(exc);
 			
 			tCEP.setError(exc);
 		}
 		finally {
 			if(isInFastMode) {
 				mNode.getTimeBase().setFastMode(true);
 			}
 		}
 		
 		return tCEP;
 	}
 	
 	@Override
 	public Description getCapabilities(Name name, Description requirements) throws NetworkException
 	{
 		return null;
 	}
 	
 	private static boolean signalingRequired(Description requConnect)
 	{
 		boolean isBE = requConnect.isBestEffort();
 		
 		if(isBE) {
 			if(requConnect != null) {
 				CommunicationTypeProperty tCommType = (CommunicationTypeProperty) requConnect.get(CommunicationTypeProperty.class);
 				if(tCommType != null) {
 					return tCommType.requiresSignaling();
 				}
 			}
 			
 			return CommunicationTypeProperty.getDefault().requiresSignaling();
 		} else {
 			// we require some QoS and, thus, have to signal it
 			return true;
 		}
 	}
 	
 	/**
 	 * Checks whether or not a name is known by the FoG system.
 	 * That does not imply that a connection to this name can
 	 * be constructed.
 	 * 
 	 * @param pName Name to search for
 	 * @return true, if name is known; false otherwise
 	 */
 	public boolean isKnown(Name pName)
 	{
 		// do not start search without a usefull name
 		if(pName != null) {
 			return getRoutingService().isKnown(pName);
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * @return Authentication service of the node (!= null)
 	 */
 	public IdentityManagement getAuthenticationService()
 	{
 		return mNode.getAuthenticationService();
 	}
 	
 	public static boolean registerRoutingService(Host pHost, RoutingService pRS)
 	{
 		FoGEntity layer = (FoGEntity) pHost.getLayer(FoGEntity.class);
 		
 		if(layer != null) {
 			layer.registerRoutingService(pRS);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Helper function for registering routing service entity at a host.
 	 */
 	public void registerRoutingService(RoutingService pRS)
 	{
 		if(routingService == null) {
 			routingService = pRS;
 		} else {
 			// check, if already a multiplexer available
 			if(routingService instanceof RoutingServiceMultiplexer) {
 				((RoutingServiceMultiplexer) routingService).add(pRS); 
 			} else {
 				// ... no -> create one and store old and new rs entities in it
 				RoutingService rs = routingService;
 				
 				RoutingServiceMultiplexer rsMult = new RoutingServiceMultiplexer(); 
 				rsMult.add(rs);
 				rsMult.add(pRS);
 				
 				// activate new RS multiplexer as new RS of node
 				routingService = rsMult;
 			}
 		}
 		
 		// inform transfer service about new routing service
 		transferPlane.setRoutingService(routingService);
 		
 		// send event "registered"
 		pRS.registered();
 	}
 	
 	public boolean hasRoutingService()
 	{
 		return routingService != null;
 	}
 	
 	public RoutingService getRoutingService()
 	{
 		return routingService;
 	}
 	
 	/**
 	 * Unregisters a local routing service entity.
 	 * 
 	 * @param pRS Routing service entity to unregister
 	 * @returns true==success; false==RS was not registered
 	 */
 	public boolean unregisterRoutingService(RoutingService pRS)
 	{
 		if(routingService != null) {
 			// check, if already a multiplexer available
 			if(routingService instanceof RoutingServiceMultiplexer) {
 				return ((RoutingServiceMultiplexer) routingService).remove(pRS); 
 			} else {
 				if(routingService == pRS) {
 					routingService = null;
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public String toString()
 	{
 		return "FoG@" +mNode.toString();
 	}
 
 	@Override
 	public NeighborList getNeighbors(Name namePrefix) throws NetworkException
 	{
 		return null;
 	}
 
 	@Override
 	public void registerObserverNeighborList(LayerObserverCallback observer)
 	{
 		
 	}
 
 	@Override
 	public boolean unregisterObserverNeighborList(LayerObserverCallback observer)
 	{
 		return false;
 	}
 	
 	/**
 	 * @return Reference to routing service of node (!= null)
 	 */
 	public TransferPlane getTransferPlane()
 	{
 		// Debug check: It should not happen, since a node gets at least one
 		//              routing service created by the RoutingServiceFactory.
		if((!mEntityDeleted) && (transferPlane == null)) throw new RuntimeException("Node " +this +" does not have a routing service.");
 			
 		return transferPlane;
 	}
 	
 	public ProcessRegister getProcessRegister()
 	{
 		if(processes == null) {
 			processes = new ProcessRegister();
 		}
 		
 		return processes; 
 	}
 	
 	public Controller getController()
 	{
 		return controlgate;
 	}
 	
 	/**
 	 * The main FN is just an implementation artifact. From the FoG concept, it is
 	 * not needed. It would be possible to use several FNs within a node (e.g. one connecting
 	 * the interfaces and one connecting the services). But a central one make debugging
 	 * much easier and simplifies the attachment question for elements of the transfer
 	 * service.
 	 * 
 	 * @return The main FN of a node, which connects all interfaces and services within a node.
 	 */
 	public Multiplexer getCentralFN()
 	{
 		if(multiplexgate == null) {
 			Name nameObj = null;
 			if(!Config.Routing.REDUCE_NUMBER_FNS) {
 				nameObj = new SimpleName(Node.NAMESPACE_HOST, mNode.getName());
 			}
 			// Register node in routing services at attaching the first interface.
 			// It is important, that it is registered before the interface is created.
 			// TODO name for multiplexer is not really needed => remove it when code finished
 			multiplexgate = new Multiplexer(this, nameObj, NamingLevel.NAMES, Config.Routing.ENABLE_NODE_RS_HIERARCHY_LEVEL, getIdentity(), controlgate);
 			multiplexgate.open();
 		}
 		
 		return multiplexgate;
 	}
 	
 	/**
 	 * FoG uses the same identity than the Node objects, since
 	 * there are some dependencies between the identity names
 	 * and the Node names. In particular, the rerouting experiment
 	 * uses the names in the signatures in order to determine the
 	 * Node that has to be set to broken.
 	 * (see {@link ReroutingExperiment#determineElementToBreak})
 	 */
 	public Identity getIdentity()
 	{
 		return mNode.getIdentity();
 	}
 	
 	public Logger getLogger()
 	{
 		return mNode.getLogger();
 	}
 	
 	public Node getNode()
 	{
 		return mNode; 
 	}
 
 	/**
 	 * @return Get time base for this node
 	 */
 	public EventHandler getTimeBase()
 	{
 		return mNode.getTimeBase();
 	}
 	
 	/**
 	 * For GUI purposes, only!
 	 */
 	@Override
 	public RoutableGraph getGraph()
 	{
 		return transferPlane.getGraph();
 	}
 
 	/**
 	 * Informs FoG entity that it was deleted from the scenario.
 	 */
 	@Override
 	public void deleted()
 	{
		mEntityDeleted = true;
		
 		if(controlgate != null)
 			controlgate.closed();
 		
 		if(multiplexgate != null)
 			multiplexgate.close();
 		
 		if((routingService != null) && (routingService instanceof RoutingServiceMultiplexer)) {
 			((RoutingServiceMultiplexer) routingService).clear();
 		}
 		
 		routingService = null;
 		transferPlane = null;
 		controlgate = null;
 		multiplexgate = null;
 	}
 
 	
	private boolean mEntityDeleted = false;
 	private Node mNode;
 	private Controller controlgate;
 	private Multiplexer multiplexgate;
 	private TransferPlane transferPlane;
 	private RoutingService routingService;
 
 	private ProcessRegister processes;
 	
 	private LinkedList<Name> mRegisteredServers = new LinkedList<Name>();
 	
 }
