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
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.*;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.graph.RoutableGraph;
 
 
 /**
  * Routing service instance local to a host.
  * 
  * The local information are stored locally. Furthermore, they are duplicated
  * and reported to the next higher level routing service instance.
  */
 public class HRMRoutingService implements RoutingService, Localization
 {
 	/**
 	 * The physical node on which this routing service instance is running.
 	 */
 	private Node mNode = null;
 
 	/**
 	 * Stores the reference to the AutonomousSystem. 
 	 */
 	private AutonomousSystem mAS = null;
 	
 	/**
 	 * Stores the name mapping instance for mapping FoG names to L2 addresses
 	 */
 	private HierarchicalNameMappingService<L2Address> mFoGNamesToL2AddressesMapping = null;
 
 	/**
 	 * Stores the mapping from FoG FNs to L2 addresses
 	 */
 	private HashMap<ForwardingNode, L2Address> mFNToL2AddressMapping = new HashMap<ForwardingNode, L2Address>();
 	
 	/**
 	 * Stores a reference to the local HRMController application
 	 */
 	private HRMController mHRMController = null;
 
 	/**
 	 * Stores the L2 address of the central FN
 	 */
 	private L2Address mCentralFNL2Address = null;
 
 	/**
 	 * Stores the HRMIDs of direct neighbor nodes.
 	 */
 	private LinkedList<HRMID> mDirectNeighborAddresses = new LinkedList<HRMID>();
 
 	/**
 	 * Stores the mapping from HRMIDs of local neighbors to their L2 addresses
 	 */
 	private HashMap<HRMID, L2Address> mHRMIDToL2AddressMapping = new HashMap<HRMID, L2Address>();
 
 	/**
 	 * Stores routes to physical neighbors nodes
 	 */
 	private HashMap<L2Address, Route> mRoutesToDirectNeighbors = new HashMap<L2Address, Route>();
 	
 	/**
 	 * Stores the HRM based routing table which is used for hop-by-hop routing.
 	 */
 	private LinkedList<RoutingEntry> mRoutingTable = new LinkedList<RoutingEntry>();
 	
 	/**
 	 * Stores the FoG specific routing graph (consisting of FNs and Gates)
 	 */
 	private final RoutableGraph<HRMName, RoutingServiceLink> mFoGRoutingGraph;
 
 	/**
 	 * Stores if the start of the HRMController application instance is still pending
 	 */
 	private boolean mWaitOnControllerstart = true;
 	
 
 	private final RoutableGraph<HRMName, Route> mCoordinatorRoutingMap;
 
 	
 	/**
 	 * Creates a local HRS instance for a node.
 	 * @param pAS the autonomous system at which the HRS is instantiated 
 	 * @param pNode the node on which this routing service instance is created on
 	 */
 	public HRMRoutingService(AutonomousSystem pAS, Node pNode)
 	{
 		Logging.log(this, "CREATED ON " + pNode);
 		
 		mNode = pNode;
 		mAS = pAS;
 		
 		// create the FoG specific routing graph (consisting of FNs and Gates)
 		mFoGRoutingGraph = new RoutableGraph<HRMName, RoutingServiceLink>();
 
 		// create name mapping instance to map FoG names to L2 addresses
 		mFoGNamesToL2AddressesMapping = new HierarchicalNameMappingService<L2Address>(HierarchicalNameMappingService.getGlobalNameMappingService(), null);
 		
 		mCoordinatorRoutingMap = new RoutableGraph<HRMName, Route>();
 	}
 
 	/**
 	 * This function creates the local HRM controller application. It uses a FoG server FN for offering its CEP.
 	 * For this purpose, the HRS has to be already registered because the server FN registers a node and links at the local (existing!) routing service.
 	 */
 	@Override
 	public void registered()
 	{
 		Logging.log(this, "Got event \"ROUTING SERVICE REGISTERED\"");
 		
 		// create HRM controller instance 
 		mHRMController = new HRMController(mAS, mNode, this);
 		
 		mWaitOnControllerstart = false;
 
 		// end an active waiting by getHRMController()
 		synchronized(this){
 			notify();
 		}
 	}
 
 	/**
 	 * Returns a reference to the HRMController application.
 	 * However, this function waits in case the application wasn't started yet.
 	 * 
 	 * @return the HRMController application
 	 */
 	public HRMController getHRMController()
 	{
 		int tLoop = 0;
 		while(mWaitOnControllerstart){
 			try {
 				synchronized (this) {
 					wait(500 /* ms */);
 				}
 				if (tLoop > 0){
 					Logging.log(this, "WAITING FOR HRMController application start - loop " + tLoop);
 				}
 				tLoop++;
 			} catch (InterruptedException e) {
 				Logging.log(this, "CONTINUING PROCESSING");
 			}		
 		}
 		
 		if (mHRMController == null){
 			throw new RuntimeException(this + ": HRMController reference is still invalid");
 		}
 		
 		return mHRMController;
 	}
 	
 	/**
 	 * Adds a route to the local HRM routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pRoutingTableEntry the routing table entry
 	 * @return true if the entry is new and was added, otherwise false
 	 */
 	public boolean addHRMRoute(RoutingEntry pRoutingTableEntry)
 	{
 		boolean tResult = false;
 		
 		synchronized(mRoutingTable){
 			/**
 			 * Check for duplicates
 			 */
 			boolean tFoundDuplicate = false;
 			if (HRMConfig.Routing.AVOID_DUPLICATES_IN_ROUTING_TABLES){
 				for (RoutingEntry tEntry: mRoutingTable){
 					// have we found a route to the same destination which uses the same next hop?
 					//TODO: what about multiple links to the same next hop?
 					if ((tEntry.getDest().equals(pRoutingTableEntry.getDest())) /* same destination? */ &&
 						(tEntry.getNextHop().equals(pRoutingTableEntry.getNextHop())) /* same next hop? */){
 
 						//Logging.log(this, "REMOVING DUPLICATE: " + tEntry);
 						tFoundDuplicate = true;
 						
 						break;						
 						
 					}
 				}
 			}
 			
 			/**
 			 * Add the entry to the local routing table
 			 */
 			if (!tFoundDuplicate){
 				Logging.log(this, "ADDING ROUTE      : " + pRoutingTableEntry);
 
 				// add the route to the routing table
 				mRoutingTable.add(pRoutingTableEntry.clone());
 				
 				// save HRMID of the given route if it belongs to a direct neighbor node
 				if (pRoutingTableEntry.isRouteToDirectNeighbor())
 				{
 					synchronized(mDirectNeighborAddresses){
 						// get the HRMID of the direct neighbor
 						HRMID tHRMID = pRoutingTableEntry.getDest().clone();
 
 						// get the L2 address of the next (might be null)
 						L2Address tL2Address = pRoutingTableEntry.getNextHopL2Address();
 						
 						// add address for a direct neighbor
 						Logging.log(this, "     ..adding " + tHRMID + " as address of a direct neighbor");
 						mDirectNeighborAddresses.add(tHRMID);
 
 						if (tL2Address != null){
 							// add L2 address for this direct neighbor
 							Logging.log(this, "     ..add mapping from " + tHRMID + " to " + tL2Address);
 							mapHRMIDToL2Address(tHRMID, tL2Address);
 						}
 					}
 				}
 				
 				tResult = true;
 			}else{
 				//TODO: support for updates
 			}
 		}
 		
 		return tResult;
 	}	
 
 	/**
 	 * Deletes a route from the local HRM routing table.
 	 * This function is usually used when a timeout occurred and the corresponding route became too old. 
 	 * 
 	 * @param pRoutingTableEntry the routing table entry 
 	 * @return true if the entry was found and removed, otherwise false
 	 */
 	private boolean delHRMRoute(RoutingEntry pRoutingTableEntry)
 	{
 		boolean tResult = false;
 		
 		Logging.log(this, "REMOVING ROUTE: " + pRoutingTableEntry);
 
 		synchronized(mRoutingTable){
 			if (mRoutingTable.contains(pRoutingTableEntry)){
 				// remove the entry
 				mRoutingTable.remove(pRoutingTableEntry);
 				
 				tResult = true;
 			}else{
 				Logging.err(this, "The following route couldn't be removed from the local routing table: \n     " + pRoutingTableEntry);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Adds a route to the local HRM routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pNeighborL2Address the L2Address of the direct neighbor
 	 * @param pRoute the route to the direct neighbor
 	 * @return returns true if the route was added, false if a duplicate was found
 	 */
 	public boolean addRouteToDirectNeighbor(L2Address pNeighborL2Address, Route pRoute)
 	{
 		boolean tResult = true;
 		
 		synchronized (mRoutesToDirectNeighbors) {
 			Route tOldRoute = mRoutesToDirectNeighbors.get(pNeighborL2Address);
 			if (tOldRoute != null){
 				Logging.log(this, "Found duplicate route (" + pRoute + ") to direct neighbor (" + pNeighborL2Address + ") ");
 				tResult = false;
 			}
 			Logging.log(this, "ADDING ROUTE (" + pRoute + ") to direct neighbor (" + pNeighborL2Address + ") ");
 			mRoutesToDirectNeighbors.put(pNeighborL2Address,  pRoute);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the local HRM routing table
 	 * 
 	 * @return the local HRM routing table
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<RoutingEntry> routingTable()
 	{
 		LinkedList<RoutingEntry> tResult = null;
 		
 		synchronized (mRoutingTable) {
 			tResult = (LinkedList<RoutingEntry>) mRoutingTable.clone();
 		}
 		
 		return tResult;		
 	}
 	
 	/**
 	 * Returns the local table with routes to direct neighbors
 	 * 
 	 * @return the route table
 	 */
 	public HashMap<L2Address, Route> getRoutesToDirectNeighbors()
 	{
 		HashMap<L2Address, Route> tResult = new HashMap<L2Address, Route>();
 		
 		synchronized (mRoutesToDirectNeighbors) {
 			for (L2Address tAddr : mRoutesToDirectNeighbors.keySet()){
 				Route tRoute = mRoutesToDirectNeighbors.get(tAddr);
 				tResult.put(tAddr.clone(), tRoute.clone());
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the list of known neighbor HRMIDs
 	 * 
 	 * @return the desired list of HRMIDs
 	 */
 	public HashMap<HRMID, L2Address> getHRMIDToL2AddressMapping()
 	{
 		HashMap<HRMID, L2Address> tResult = new HashMap<HRMID, L2Address>();
 		
 		synchronized (mHRMIDToL2AddressMapping) {
 			for (HRMID tAddr : mHRMIDToL2AddressMapping.keySet()){
 				L2Address tL2Address = mHRMIDToL2AddressMapping.get(tAddr);
 				if (tL2Address != null){
 					tResult.put(tAddr.clone(), tL2Address.clone());
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines a route within a RoutableGraph.
 	 *  
 	 * @param pGraph the RoutableGraph instance
 	 * @param pSource the source where the searched route should start
 	 * @param pDestination the destination where the searched route should start
 	 * @return the found route
 	 */
 	@SuppressWarnings("unchecked")
 	private <LinkType> List<RoutingServiceLink> getRouteFromGraph(RoutableGraph pGraph, HRMName pSource, HRMName pDestination)
 	{
		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
			Logging.log(this, "GET ROUTE in graph " + pGraph.getClass().getSimpleName().toString() + " from " + pSource + " to " + pDestination);
		}
 
 		List<RoutingServiceLink> tResult = null;
 
 		List<LinkType> tFoundRoute = null;
 		
 		// HINT: keep the locked (synchronized) area small!
 		synchronized (pGraph) {
 			// check if source/destination are known by the graph
 			if(pGraph.contains(pSource) && pGraph.contains(pDestination)) {
 				try {
 					// determine the route in the graph instance
 					tFoundRoute = (List<LinkType>)pGraph.getRoute(pSource, pDestination);
 				} catch (ClassCastException tExc) {
 					Logging.err(this, "Unable to cast the getRouteFromGraph result, returning null", tExc);
 					
 					// reset the result
 					tResult = null;
 				}
 			}
 		}
 
 		// have we found a route in the graph instance?
 		if((tFoundRoute != null) && (!tFoundRoute.isEmpty())) {
 			// create result object
 			tResult = new LinkedList<RoutingServiceLink>();
 			
 			/**
 			 * transform the route from the graph instance to a list of RoutingServiceLink(GateIDs) objects
 			 */
 			if(tFoundRoute.get(0) instanceof RoutingServiceLink) {
 				// iterate over all links(GateIDs), add them to the result list
 				for(RoutingServiceLink tLinkInFoundRoute : (List<RoutingServiceLink>)tFoundRoute) {
 					tResult.add(tLinkInFoundRoute);
 				}
 			} else if(tFoundRoute.get(0) instanceof RouteSegmentPath) {
 				// iterate over all routing segments and their stored links(GateIDs), add them to the result list
 				for(RouteSegmentPath tRouteSegment : (List<RouteSegmentPath>)tFoundRoute) {
 					for(GateID tID : tRouteSegment) {
 						tResult.add(new RoutingServiceLink(tID, null, RoutingServiceLink.DEFAULT));
 					}
 				}
 			}
 		}
 
		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
			Logging.log(this, "      ..RESULT(getRouteFromGraph): " + tResult);
		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the FoG specific routing graph
 	 * 
 	 * @return the routing graph
 	 */
 	public RoutableGraph<HRMName, RoutingServiceLink> getFoGRoutingGraph()
 	{
 		return mFoGRoutingGraph;
 	}
 
 	/**
 	 * Stores a link in the local FoG specific routing graph
 	 * 
 	 * @param pFromL2Address the starting point of the link
 	 * @param pToL2Address the ending point of the link
 	 * @param pRoutingServiceLink the link description
 	 */
 	private void storeLinkInFogRoutingGraph(L2Address pFromL2Address, L2Address pToL2Address,	RoutingServiceLink pRoutingServiceLink)
 	{
 		synchronized (mFoGRoutingGraph) {
 			mFoGRoutingGraph.storeLink(pFromL2Address, pToL2Address, pRoutingServiceLink);
 		}
 	}
 
 	/**
 	 * Creates a mapping from a FoG name to an L2Address
 	 *   
 	 * @param pName the FoG name
 	 * @param pL2Address the L2 address
 	 * @throws RemoteException
 	 */
 	public void mapFoGNameToL2Address(Name pName, Name pL2Address) throws RemoteException
 	{
 		Logging.log(this, "REGISTERING NAME-to-L2ADDRESS MAPPING: " + pName + " to " + pL2Address);
 
 		if (pL2Address instanceof L2Address){
 			synchronized (mFoGNamesToL2AddressesMapping) {
 				mFoGNamesToL2AddressesMapping.registerName(pName, (L2Address)pL2Address, NamingLevel.NAMES);
 			}
 		}else{
 			Logging.err(this, "Given L2Address has invalid type: " + pL2Address);
 		}
 	}
 
 	/**
 	 * Creates a mapping from an HRMID to an L2Address
 	 * 
 	 * @param pHRMID the HRMID 
 	 * @param pL2Address the L2Address
 	 */
 	public void mapHRMIDToL2Address(HRMID pHRMID, L2Address pL2Address)
 	{
 		boolean tDuplicateFound = false;
 		
 		synchronized (mHRMIDToL2AddressMapping) {
 			for (HRMID tHRMID: mHRMIDToL2AddressMapping.keySet()){
 				if (tHRMID.equals(pHRMID)){
 					tDuplicateFound = true;
 					break;
 				}
 			}
 			if (!tDuplicateFound){
 				mHRMIDToL2AddressMapping.put(pHRMID, pL2Address);
 			}else{
 				// HRMID is already known, mapping already exists
 			}
 		}
 	}
 
 	/**
 	 * Returns the L2 address of this physical node's central FN
 	 * 
 	 * @return the L2 address of this physical node's central FN
 	 */
 	public L2Address getCentralFNL2Address()
 	{
 		return mCentralFNL2Address;
 	}
 	
 	/**
 	 * Determines the L2Address for the given FN
 	 * 
 	 * @param pNode the FN for which the HRM name should be determined
 	 * @return the determined L2Address if it exists, otherwise null is returned 
 	 */
 	public L2Address getL2AddressFor(ForwardingNode pNode)
 	{
 		L2Address tResult = null;
 		
 		synchronized (mFNToL2AddressMapping) {
 			tResult = mFNToL2AddressMapping.get(pNode);			
 		}
 		
 		return tResult;
 	}
 	@Override
 	public L2Address getNameFor(ForwardingNode pNode)
 	{
 		return getL2AddressFor(pNode);
 	}
 
 	/**
 	 * Determines the L2Address for a given FoG name.
 	 * 
 	 * @param pName the FoG name
 	 * @return the L2Addresses
 	 */
 	private NameMappingEntry<L2Address>[] getL2AddressFor(Name pName)
 	{
 		NameMappingEntry<L2Address>[] tResult = null;
 		
 		synchronized (mFoGNamesToL2AddressesMapping) {
 			tResult = mFoGNamesToL2AddressesMapping.getAddresses(pName);			
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines the L2Addresses for a given HRMID
 	 * 
 	 * @param pHRMID the HRMID for which the L2Address has to be determined
 	 * @return the resulting L2Address, returns "null" if no mapping was found
 	 */
 	private L2Address getL2AddressFor(HRMID pHRMID)
 	{
 		L2Address tResult = null;
 		
 		synchronized (mHRMIDToL2AddressMapping) {
 			// iterate over all mappings
 			for (HRMID tHRMID : mHRMIDToL2AddressMapping.keySet()){
 				// compare the values
 				if (tHRMID.equals(pHRMID)){
 					// get the L2 address
 					tResult = mHRMIDToL2AddressMapping.get(tHRMID);
 					// leave the for-loop
 					break;
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Registers a node at the database of this HRS instance
 	 * 
 	 * @param pElement the FN to register at routing service
 	 * @param pName the name for the FN (null, if no name available)
      * @param pLevel the level of abstraction for the naming  
      * @param pDescription the requirements description for a connection to this node
 	 */
 	@Override
 	public void registerNode(ForwardingNode pElement, Name pName, NamingLevel pLevel, Description pDescription)
 	{	
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING NODE: " + pElement + " with name " + pName + (pLevel != NamingLevel.NONE ? " on naming level " + pLevel : "") + " with description " + pDescription);
 		}
 
 		/**
 		 * Determine addresses for "pName"
 		 */
 		NameMappingEntry<L2Address> [] tAddresses = getL2AddressFor(pName);
 
 		// have we found any already existing addresses?
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			if ((tAddresses != null) && (tAddresses.length > 0)){
 				Logging.log(this, "Found " + tAddresses.length + " already registered address for " + pElement);
 				for (int i = 0; i < tAddresses.length; i++){
 					Logging.log(this, "     ..[" + i + "](" + tAddresses[i].getAddress().getClass().getSimpleName() + "): " + tAddresses[i].getAddress().toString());
 				}
 			}
 		}		
 		
 		/**
 		 * Register name mappings 
 		 */
 		synchronized (mFNToL2AddressMapping) {
 			// is there already an L2Address registered for the node?
 			if(!mFNToL2AddressMapping.containsKey(pElement)) {
 				/**
 				 * Generate L2 address for the node
 				 */
 				L2Address tNodeL2Address = L2Address.create();
 				tNodeL2Address.setDescr(pElement.toString());
 				
 				if (pElement.equals(mNode.getCentralFN())){
 					Logging.log(this, "     ..registering L2 address for central FN: " + tNodeL2Address);
 					mCentralFNL2Address = tNodeL2Address;
 				}
 				
 				/** 
 				 * Register mapping from FN to L2address
 				 */
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..registering NAME MAPPING for FN \"" + pElement + "\": L2address=\"" + tNodeL2Address + "\", level=" + pLevel);
 				}
 				mFNToL2AddressMapping.put(pElement, tNodeL2Address);
 				/** 
 				 * Register mapping from FoG name to L2address
 				 */
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..registering NAME MAPPING for FoG name \"" + pName + "\": L2address=\"" + tNodeL2Address + "\", level=" + pLevel);
 				}
 				synchronized (mFoGNamesToL2AddressesMapping) {
 					mFoGNamesToL2AddressesMapping.registerName(pName, tNodeL2Address, pLevel);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Unregisters an FN.
 	 * 
 	 * @param pElement the FN which should be unregistered.
 	 * @return true if the FN was removed, otherwise false
 	 */
 	@Override
 	public boolean unregisterNode(ForwardingNode pElement)
 	{
 		Logging.log(this, "UNREGISTERING NODE: " + pElement);
 
 		// get the L2Address of this FN
 		L2Address tNodeL2Address = getL2AddressFor(pElement);
 
 		// have we found an L2 address?
 		if (tNodeL2Address != null){
 			/**
 			 * remove the FN from the FoG specific routing graph
 			 */
 			synchronized (mFoGRoutingGraph) {
 				if(mFoGRoutingGraph.contains(tNodeL2Address)) {
 					mFoGRoutingGraph.remove(tNodeL2Address);
 				}
 			}
 			
 			//TODO: remove this
 			synchronized (mCoordinatorRoutingMap) {
 				if(mCoordinatorRoutingMap.contains(tNodeL2Address)) {
 					mCoordinatorRoutingMap.remove(tNodeL2Address);
 				}
 			}
 		}
 			
 		return true;
 	}
 	
 	/**
 	 * Registers a link in the local FoG specific routing graph
 	 * 
 	 *  @param pFrom the FN where the link begins
 	 *  @param pGate the Gate (link) which is to be registered
 	 *  @throws NetworkException
 	 */
 	@Override
 	public void registerLink(ForwardingElement pFrom, AbstractGate pGate) throws NetworkException
 	{
 		Logging.log(this, "REGISTERING LINK: source=" + pFrom + " ## dest.=" + pGate.getNextNode() + " ## attr.=" + pGate.getDescription() + " ## gate=" + pGate + " ## peer=" + pGate.getRemoteDestinationName());
 
 		/**
 		 * Make sure that the starting point of the link is already known to the FN-to-L2address mapping.
 		 * Determine the L2Address of the starting point.
 		 */
 		L2Address tFromL2Address = null;
 		// check the class type of pFrom
 		if (pFrom instanceof ForwardingNode){
 			// get the FN where the route should start
 			ForwardingNode tFromFN = (ForwardingNode)pFrom;
 			
 			// determine the L2address of the starting point
 			tFromL2Address = getL2AddressFor(tFromFN);
 			
 			// is the FN still unknown for the FN-to-L2address mapping?
 			if(tFromL2Address == null) {
 				Logging.warn(this, "Source node " + pFrom +" of link " + pGate + " isn't known yet. It will be registered implicitly.");
 				
 				registerNode(tFromFN, null, NamingLevel.NONE, null);
 				
 				// determine the L2address of the starting point to check if the registration was successful
 				tFromL2Address = getL2AddressFor(tFromFN);
 				if(tFromL2Address == null) {
 					throw new RuntimeException(this + " - FN " + pFrom + " is still unknown to the FN-to-L2address mapping, although it was registered before.");
 				}
 			}
 		}else{
 			// pFrom isn't derived from ForwardingNode
 			throw new RuntimeException(this + " - Source FE " + pFrom + " has the wrong class hierarchy(ForwardingNode is missing).");
 		}
 
 		/**
 		 * Check if the link is one to another physical neighbor node or not.
 		 * Determine the L2Address of the ending point of the link
 		 */
 		L2Address tToL2Address = null;
 		boolean tIsLinkToPhysicalNeigborNode = false;
 		if( pGate instanceof DirectDownGate){
 			// mark as link to another node
 			tIsLinkToPhysicalNeigborNode = true;
 
 			// determine the L2Address of the destination FN for this gate
 			// HINT: For DirectDownGate gates, this address is defined in "DirectDownGate" by a call to "RoutingService.getL2AddressFor(ILowerLayer.getMultiplexerGate())".
 			//       However, there will occur two calls to registerLink():
 			//				* 1.) the DirectDownGate is created
 			//				* 2.) the peer has answered by a packet of "OpenGateResponse" and the peer name is now known
 			//       Therefore, we ignore the first registerLink() request and wait for the (hopfefully) appearing second request.
 			tToL2Address = (L2Address) pGate.getRemoteDestinationName();
 			if (tToL2Address == null){
 				Logging.log(this, "Peer name wasn't avilable via AbstractGate.getRemoteDestinationName(), will skip this registerLink() request and wait until the peer is known");
 			}
 
 			Logging.log(this, "      ..external link, which ends at the physical node " + tToL2Address);
 		}else{
 			// mark as node-internal link
 			tIsLinkToPhysicalNeigborNode = false;
 
 			// we have any kind of a gate, we determine its ending point
 			ForwardingNode tToFN = (ForwardingNode)pGate.getNextNode();
 			
 			/**
 			 * Make sure that the starting point of the link is already known to the FN-to-L2address mapping.
 		 	*/
 			tToL2Address = getL2AddressFor(tToFN);
 			// is the FN still unknown for the FN-to-L2address mapping?
 			if(tToL2Address == null) {
 				Logging.warn(this, "Destination node " + tToFN +" of link " + pGate + " isn't known yet. It will be registered implicitly.");
 				
 				registerNode(tToFN, null, NamingLevel.NONE, null);
 				
 				// determine the L2address of the starting point to check if the registration was successful
 				tToL2Address = getL2AddressFor(tToFN);
 				if(tToL2Address == null) {
 					throw new RuntimeException(this + " - Destination FN " + pFrom + " is still unknown to the FN-to-L2address mapping, although it was registered before.");
 				}
 			}
 
 			Logging.log(this, "      ..internal link, which ends at the local node " + tToL2Address);
 		}
 
 		if(tToL2Address == null) {
 			// return immediately because the peer name is sill unknown
 			Logging.log(this, "Peer name is still unknown, waiting for the second request (source=" + tFromL2Address + ", gate=" + pGate + ")");
 			return;
 		}
 		
 		/**
 		 * Add link to FoG specific routing graph
 		 */
 		storeLinkInFogRoutingGraph(tFromL2Address, tToL2Address, new RoutingServiceLink(pGate.getGateID(), null, RoutingServiceLink.DEFAULT));
 		
 		/**
 		 * DIRECT NEIGHBOR FOUND: create a HRM connection to it
 		 */
 		if(tIsLinkToPhysicalNeigborNode) {
 			L2Address tThisHostL2Address = getL2AddressFor(mNode.getCentralFN());
 
 			Logging.info(this, "      ..NODE " + tThisHostL2Address + " FOUND POSSIBLE DIRECT NEIGHBOR: " + tToL2Address + "?");
 
 			if((!pFrom.equals(tThisHostL2Address)) && (!tToL2Address.equals(tThisHostL2Address))) {
 				if(tFromL2Address.getComplexAddress().longValue() < tToL2Address.getComplexAddress().longValue()) {
 					Logging.log(this, "    ..actually found an interesting link from " + tThisHostL2Address + " to " + tToL2Address + " via FN " + pFrom);
 					getHRMController().detectedPhysicalNeighborNode(tToL2Address);
 				}else{
 					Logging.warn(this, "registerLink() ignores the new link to a possible neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address);
 				}
 			}else{
 				Logging.warn(this, "registerLink() ignores the new link to a possible neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address + " because it is linked to the central FN " + tThisHostL2Address);
 			}
 		}
 	}
 	
 	@Override
 	public boolean unregisterLink(ForwardingElement pFrom, AbstractGate pGate)
 	{
 		Logging.log(this, "UNREGISTERING LINK from " + pFrom + " to " + pGate.getNextNode() + ", gate " + pGate);
 
 		/**
 		 * Check if the link is one to another physical neighbor node or not.
 		 * Determine the L2Address of the ending point of the link
 		 */
 		boolean tIsLinkToPhysicalNeigborNode = false;
 		if( pGate instanceof DirectDownGate){
 			// mark as link to another node
 			tIsLinkToPhysicalNeigborNode = true;
 		}else{
 			// mark as node-internal link
 			tIsLinkToPhysicalNeigborNode = false;
 		}
 			
 		/**
 		 * Determine the L2Address of the starting point of the link
 		 */
 		L2Address tFromL2Address = mFNToL2AddressMapping.get(pFrom);
 
 		/**
 		 * Remove the connections to this neighbor node
 		 */
 		if (tIsLinkToPhysicalNeigborNode){
 			//TODO: implement this
 		}
 		
 		/**
 		 * Remove the link from the FoG specific routing graph
 		 */
 		// determine which links are outgoing from the FN "pFrom"
 		Collection<RoutingServiceLink> tOutgoingLinksFromFN = mFoGRoutingGraph.getOutEdges(tFromL2Address);
 		// have we found at least one outgoing link?
 		if(tOutgoingLinksFromFN != null) {
 			for(RoutingServiceLink tOutgoingLink : tOutgoingLinksFromFN) {
 				// have we found the right outgoing link? (we check the GateIDs)
 				if(tOutgoingLink.equals(pGate)) {
 					// remove the link from the FoG specific routing graph
 					mFoGRoutingGraph.unlink(tOutgoingLink);
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Updates the capabilities of existing forwarding nodes.
 	 * (However, this function is only used to update the capabilities of the central FN instead of all FNs. 
 	 * Because either the central FN is able to provide a special function or no FN on this physical node is able to do this.)
 	 * 
 	 * @param pElement the FN for which the capabilities have to be updated
 	 * @param pCapabilities the new capabilities for the FN
 	 */
 	@Override
 	public void updateNode(ForwardingNode pElement, Description pCapabilities)
 	{
 		Logging.log(this, "UPDATING NODE " + pElement + ": old caps.=" + mNode.getCapabilities() + ", new caps.=" + pCapabilities);
 
 		// TODO: what about functional requirements and function placing?
 	}
 	
 	/**
 	 * Returns a reference to the used name mapping service
 	 * 
 	 * @return the reference to the name mapping service
 	 */
 	@Override
 	public NameMappingService<L2Address> getNameMappingService()
 	{
 		return mFoGNamesToL2AddressesMapping;
 	}
 
 	/**
 	 * Checks if a given FoG FN name is known to this HRS instance
 	 * 
 	 * @return true if the FoG name is known, otherwise false
 	 */
 	@Override
 	public boolean isKnown(Name pName)
 	{
 		boolean tResult = false;
 		
 		// check if the FoG name is stored in the FoG-to-L2Addresses mapping
 		synchronized (mFoGNamesToL2AddressesMapping) {
 			tResult = (mFoGNamesToL2AddressesMapping.getAddresses(pName) != null);			
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Unregisters a FoG name for a given FN
 	 * 
 	 * @return true if the operation was successful, otherwise false is returned
 	 */
 	@Override
 	public boolean unregisterName(ForwardingNode pFN, Name pName)
 	{
 		boolean tResult = false;
 		
 		// determine the L2Address 
 		L2Address tFNL2Address = getL2AddressFor(pFN);
 		
 		// unregister mapping from FoG name to the determined L2address
 		synchronized (mFoGNamesToL2AddressesMapping) {
 			tResult = mFoGNamesToL2AddressesMapping.unregisterName(pName, tFNL2Address);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines a route in the local routing graphs
 	 * 
 	 * @param pSource the name of the node where the route should start 
 	 * @param pDestination the name of the node where the route should end
 	 * @param pDescription the desired route attributes
 	 * @param pRequester the identity of the caller	 *  
 	 * @return the determined route, null if no route was found
 	 */
 	private <LinkType> List<RoutingServiceLink> getRouteFromLocalGraphs(HRMName pSource, HRMName pDestination, Description pDescription, Identity pRequester)
 	{
 		Logging.log(this, "GET ROUTE (getRouteFromLocalGraphs) from " + pSource + " to " + pDestination);
 		
 		List<RoutingServiceLink> tResult = null;
 		
 		/**
 		 * Look in the local FoG specific routing graph
 		 */
 		tResult = getRouteFromGraph(mFoGRoutingGraph, pSource, pDestination);
 		Logging.log(this, "      ..RESULT(getRouteFromLocalGraphs-routingMap): " + tResult);
 		
 		/**
 		 * 
 		 */
 		if (tResult == null){
 			tResult = getRouteFromGraph(mCoordinatorRoutingMap, pSource, pDestination);
 			Logging.log(this, "      ..RESULT(getRouteFromLocalGraphs-coordinatorMap): " + tResult);
 		}
 		
 		Logging.log(this, "      ..RESULT(getRouteFromLocalGraphs): " + tResult);
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines a list of Gate IDs from an L2 address to another L2 address based on the FoG specific routing graph.
 	 * 
 	 * @param pFromL2Address the starting point of the desired route
 	 * @param pToL2Address the ending point of the desired route
 	 * @return a list of Gate IDs to the neighbor node, returns "null" if no route was found
 	 */
 	private List<RoutingServiceLink> getGateIDsForRoute(L2Address pFromL2Address, L2Address pToL2Address)
 	{
 		List<RoutingServiceLink> tResult = null;
 
 		synchronized (mFoGRoutingGraph) {
 			// query route in the FoG specific routing graph
 			tResult = getRouteFromGraph(mFoGRoutingGraph, pFromL2Address, pToL2Address);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines a list of Gate IDs to a node, identified by its L2 address, based on the FoG specific routing graph.
 	 * 
 	 * @param pToL2Address the L2 address of the neighbor node
 	 * @return a list of Gate IDs to the neighbor node, returns "null" if no route was found
 	 */
 	public List<RoutingServiceLink> getGateIDsToL2Address(L2Address pToL2Address)
 	{
 		List<RoutingServiceLink> tResult = null;
 
 		// determine address of this physical node
 		L2Address tThisHostL2Address = getL2AddressFor(mNode.getCentralFN());
 
 		// query route in the FoG specific routing graph
 		tResult = getGateIDsForRoute(tThisHostL2Address, pToL2Address);
 
 		return tResult;
 	}
 
 	/**
 	 * Determines a route to a direct neighbor, identified by its L2 address.
 	 * 
 	 * @param pNeighborNodeL2Address the L2 address of the neighbor node
 	 * @return the found route, returns null if no route was available
 	 */
 	private Route getRouteToDirectNeighborNode(L2Address pNeighborNodeL2Address)
 	{
 		Route tResultRoute = null;
 		
 		synchronized (mRoutesToDirectNeighbors) {
 			tResultRoute = mRoutesToDirectNeighbors.get(pNeighborNodeL2Address);
 		}
 		
 		return tResultRoute;
 	}
 
 	/**
 	 * Determines a route from an L2 address to another one, based on the FoG specific routing graph.
 	 * 
 	 * @param pFromL2Address the L2 address of the starting point
 	 * @param pToL2Address the L2 address of the ending point
 	 * @return the found route, returns null if no route was available
 	 */
 	private Route getRoute(L2Address pFromL2Address, L2Address pToL2Address)
 	{
 		Route tResultRoute = null;
 		
 		// get a list of Gate IDs to the destination
 		List<RoutingServiceLink> tGateIDsToDestination = getGateIDsForRoute(pFromL2Address, pToL2Address);
 
 		// gate ID list is empty?
 		if((tGateIDsToDestination != null) && (!tGateIDsToDestination.isEmpty())) {
 			// create a route segment which can store a list of Gate IDs
 			RouteSegmentPath tRouteSegmentPath = new RouteSegmentPath();
 			// iterate over all gate IDs in the list
 			for(RoutingServiceLink tGateID : tGateIDsToDestination) {
 				// store the Gate ID in the route segment
 				tRouteSegmentPath.add(tGateID.getID());
 			}
 			
 			// create new route
 			tResultRoute = new Route();
 			
 			// add the list of Gate IDs to the resulting route
 			tResultRoute.add(tRouteSegmentPath);
 		}
 
 		return tResultRoute;
 	}
 	
 	/**
 	 * Determines a general route from this node's central FN to a destination with special requirements
 	 * 
 	 * @param pDestination
 	 * @param pRequirements
 	 * @param pRequester
 	 * @return the determined route
 	 * @throws RoutingException
 	 * @throws RequirementsException
 	 */
 	public Route getRoute(Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{
 		return getRoute(mNode.getCentralFN(), pDestination, pRequirements, pRequester);
 	}
 	
 	/**
 	 * Determines a general route from a source to a destination with special requirements
 	 * 
 	 * @param pSource the FN where the route should start
 	 * @param pDestination the FoG name of the ending point of the route
 	 * @param pRequirements the route requirements 
 	 * @param pRequester the getRoute() caller
 	 * @return the determined route
 	 */
 	@Override
 	public Route getRoute(ForwardingNode pSource, Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{		
 		Route tResultRoute = null;
 		L2Address tDestinationL2Address = null;
 		L2Address tSourceL2Address = null;
 
 		/**
 		 * Check parameters
 		 */
 		// check source parameter
 		if(pSource == null){
 			throw new RoutingException("Invalid source parameter.");
 		}
 		// check destination parameter
 		if(pDestination == null){
 			throw new RoutingException("Invalid destination parameter.");
 		}
 		// avoid additional checks
 		if(pRequirements == null) {
 			pRequirements = new Description();
 		}
 
 		/**
 		 * Debug output about process start
 		 */
 		// debug output
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			if(pRequirements.isBestEffort()) {
 				Logging.log(this, "GET ROUTE from \"" + pSource + "\" to \"" + pDestination +"\"");
 			} else {
 				Logging.log(this, "GET ROUTE from \"" + pSource + "\" to \"" + pDestination + "\" with requirements \"" + pRequirements.toString() + "\"");
 			}
 		}
 
 		/**
 		 * Count the route request
 		 */
 		// count call
 		//TODO: mCounterGetRoute.write(+1.0, mTimeBase.nowStream());
 
 		/**
 		 * HRM based routing to an HRMID
 		 */
 		if (pDestination instanceof HRMID){
 			HRMID tDestHRMID = (HRMID)pDestination;
 			
 			/**
 			 * CHECK NEIGHBORHOD
 			 * Check if the destination is a direct neighbor
 			 */
 			boolean tDestinationIsDirectNeighbor = false;
 			synchronized (mDirectNeighborAddresses) {
 				for (HRMID tHRMID : mDirectNeighborAddresses){
 					if (tHRMID.equals(tDestHRMID)){
 						if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 							Logging.log(this, "      .." + tDestHRMID + " is an address of a direct neighbor node");
 						}
 						tDestinationIsDirectNeighbor = true;
 						break;
 					}
 				}
 			}
 
 			/**
 			 * FIND NEXT HOP
 			 * Determine the next hop of the desired route
 			 */
 			HRMID tNextHopHRMID = null;
 			if(!tDestinationIsDirectNeighbor){
 
 				//TODO
 				//tNextHopHRMID = ??
 			}else{
 				// the next hop is the final destination
 				tNextHopHRMID = tDestHRMID;
 				
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "      ..NEXT HOP(HRMID): " + tNextHopHRMID);
 				}
 			}
 			
 			if (tNextHopHRMID != null){
 				/**
 				 * FIND NEXT L2 ADDRESS
 				 * Determine the FoG specific gates towards the neighbor node
 				 */
 				L2Address tNextHopL2Address = getL2AddressFor(tNextHopHRMID);
 				if (tNextHopL2Address != null){
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "      ..NEXT HOP(L2ADDRESS)" + tNextHopL2Address);
 					}
 
 					/**
 					 * GET ROUTE to L2 address
 					 */
 					if (tNextHopL2Address != null){
 						// get a route to the neighbor
 						tResultRoute = getRouteToDirectNeighborNode(tNextHopL2Address);
 					}
 					
 					if (tResultRoute == null){
 						// no route found
 						Logging.log(this, "Couldn't determine a route from " + pSource + " to " + pDestination + ", knowing the following routing graph");
 						synchronized (mFoGRoutingGraph) {
 							Collection<HRMName> tGraphNodes = mFoGRoutingGraph.getVertices();
 							int i = 0;
 							for (HRMName tNodeName : tGraphNodes){
 								Logging.log(this, "     ..node[" + i + "]: " + tNodeName);
 								i++;
 							}
 							Collection<RoutingServiceLink> tGraphLinks = mFoGRoutingGraph.getEdges();
 							i = 0;
 							for (RoutingServiceLink tLink : tGraphLinks){
 								Logging.log(this, "     ..gate[" + i + "]: " + tLink.getID());
 								i++;
 							}
 						}
 					}
 				}else{
 					Logging.err(this, "getRoute() wasn't able to determine the L2 address of the next hop in the following mapping");
 					synchronized (mHRMIDToL2AddressMapping) {
 						for (HRMID tHRMID : mHRMIDToL2AddressMapping.keySet()){
 							Logging.log(this, "       ..mapping " + tHRMID + " to " + mHRMIDToL2AddressMapping.get(tHRMID));
 						}
 					}
 				}
 			}else{
 				Logging.err(this, "getRoute() wasn't able to determine the HRMID of the next hop");
 			}
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "      ..RESULT(getRoute() to HRMID): " + tResultRoute);
 			}
 			
 		}else{
 			/**
 			 * FoG based routing to a FoG name
 			 */
 			
 			/**
 			 * Determine the L2 address of the destination (FoG name)
 			 */
 			//L2Address tDestinationL2Address = null;
 			if (pDestination instanceof L2Address){
 				// cast directly
 				tDestinationL2Address = (L2Address)pDestination;
 			}else{
 				// search in the FoGName-to-L2Address mapping
 				NameMappingEntry<L2Address>[] tDestinationL2Addresses = getL2AddressFor(pDestination);
 				// have we found valid L2 address(es)?
 				if ((tDestinationL2Addresses != null) && (tDestinationL2Addresses.length > 0)){
 					// use the last, and therefore most up-to-date, found L2 address
 					tDestinationL2Address = tDestinationL2Addresses[tDestinationL2Addresses.length - 1].getAddress();
 				}
 			}
 
 			// have we found the L2 address of the destination?
 			if (tDestinationL2Address != null){
 				/**
 				 * Determine the L2 address of the source
 				 */
 				//L2Address tSourceL2Address = null;
 				if (pSource instanceof L2Address){
 					// cast directly
 					tSourceL2Address = (L2Address)pSource;
 				}else{
 					// search in the FN-to-L2Address mapping
 					tSourceL2Address = getL2AddressFor(pSource);
 				}
 				// check if the L2 address of the source is valid
 				if (tSourceL2Address != null){
 					/**
 					 * Get a route from the source to the destination
 					 */
 					tResultRoute = getRoute(tSourceL2Address, tDestinationL2Address);
 		
 					//TODO: remove the following by merging routing graphs
 					if (tResultRoute == null){
 						List<Route> tListRouteParts = null;
 						synchronized (mCoordinatorRoutingMap) {
 							if(mCoordinatorRoutingMap.contains(tSourceL2Address) && mCoordinatorRoutingMap.contains(tDestinationL2Address)) {
 								tListRouteParts = getCoordinatorRoutingMap().getRoute(tSourceL2Address, tDestinationL2Address);
 							}							
 						}
 						if (tListRouteParts != null){
 							// create route object
 							tResultRoute = new Route();
 							// iterate over all route parts
 							for(Route tPath : tListRouteParts) {
 								tResultRoute.addAll(tPath.clone());
 							}
							if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
								Logging.log(this, "COORDINATOR GRAPH returned a route from " + pSource + " to " + pDestination + " as " + tResultRoute);
							}
 						}
 					}
 					
 					if (tResultRoute != null){
 						// do we have requirements?
 						if(pRequirements != null) {
 							/**
 							 * Check if a destination application is encoded in the requirements
 							 */
 							DestinationApplicationProperty tPropDestApp = null;
 							for(Property tProperty : pRequirements) {
 								if(tProperty instanceof DestinationApplicationProperty) {
 									tPropDestApp = (DestinationApplicationProperty) tProperty;
 								}
 							}
 							
 							/**
 							 * Add the destination application to the route
 							 */
 							if(tPropDestApp != null) {
 								if(tPropDestApp.getAppName() != null) {
 									tResultRoute.add(new RouteSegmentAddress(tPropDestApp.getAppName()));
 								} else {
 									tResultRoute.add(new RouteSegmentAddress(new SimpleName(tPropDestApp.getAppNamespace())));
 								}
 							}
 						}
 					}else{
 						// no route found
 						Logging.log(this, "Couldn't determine a route from " + pSource + " to " + pDestination + ", knowing the following routing graph nodes");
 						synchronized (mFoGRoutingGraph) {
 							Collection<HRMName> tGraphNodes = mFoGRoutingGraph.getVertices();
 							int i = 0;
 							for (HRMName tNodeName : tGraphNodes){
 								Logging.log(this, "     ..[" + i + "]: " + tNodeName);
 								i++;
 							}
 						}
 					}
 					
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "      ..RESULT(getRoute() to " + pDestination + "): " + tResultRoute);
 					}
 				}else{
 					Logging.err(this, "getRoute() wasn't able to determine the L2 address of the source " + pSource);
 				}
 			}else{
 				Logging.err(this, "getRoute() wasn't able to determine the L2 address of the destination " + pDestination);
 			}
 		}
 
 		// return immediately
 		return tResultRoute;
 	}
 	
 	/**
 	 * Determines all links which are outgoing from a defined FN
 		
 	 * @param pFNName the name of the FN from which the outgoing links should be enumerated
 	 * @return the list of outgoing links
 	 */
 	public Collection<RoutingServiceLink> getOutgoingLinks(HRMName pFNName)
 	{
 		Collection<RoutingServiceLink> tResult = null;
 
 		synchronized (mFoGRoutingGraph) {
 			tResult = mFoGRoutingGraph.getOutEdges(pFNName);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * This method is derived from RoutingService
 	 * 
 	 * @param pElement the element for which an error is reported
 	 */
 	@Override
 	public void reportError(Name pElement)
 	{
 		Logging.warn(this, "############ Transfer plane reported an error for " + pElement + " #############");
 		//TODO: remove the element from the routing graph
 	}
 	
 	
 	
 	
 	
 	public void registerNode(L2Address pAddress, boolean pGloballyImportant)
 	{
 		Logging.log(this, "REGISTERING NODE ADDRESS: " + pAddress + ", glob. important=" + pGloballyImportant);
 
 		mFoGRoutingGraph.add(pAddress);
 	}
 	
 	public boolean registerRoute(HRMName pFrom, HRMName pTo, Route pRoute)
 	{
 		Logging.log(this, "Registering route from " + pFrom + " to " + pTo + " by path \"" + pRoute + "\"");
 		
 		if(!mCoordinatorRoutingMap.contains(pFrom)){
 			mCoordinatorRoutingMap.add(pFrom);
 		}
 		
 		if(!mCoordinatorRoutingMap.contains(pTo)){
 			mCoordinatorRoutingMap.add(pTo);
 		}
 		
 		if(!mCoordinatorRoutingMap.isLinked(pFrom, pTo, pRoute)) {
 			if(pRoute != null) {
 				Route tPath = (Route)pRoute.clone();
 				if(!mCoordinatorRoutingMap.isLinked(pFrom, pTo, tPath)) {
 					mCoordinatorRoutingMap.storeLink(pFrom, pTo, tPath);
 				}
 			}
 		} else {
 			Logging.warn(this, "Link already known, source=" + pFrom + ", destination=" + pTo + ", route=" + pRoute);
 		}
 		return true;
 	}
 	
 	public RoutableGraph<HRMName, Route> getCoordinatorRoutingMap()
 	{
 		return mCoordinatorRoutingMap;
 	}
 	
 	@Override
 	public int getNumberVertices()
 	{
 		return 0;
 	}
 
 	@Override
 	public int getNumberEdges()
 	{
 		return 0;
 	}
 
 	@Override
 	public int getSize()
 	{
 		return 0;
 	}
 	
 	public Namespace getNamespace()
 	{
 		return HRMID.HRMNamespace;
 	}
 	
 	@Override
 	public ForwardingNode getLocalElement(Name pDestination)
 	{
 		if(pDestination != null) {
 			for(ForwardingElement tElement : mFNToL2AddressMapping.keySet()) {
 				L2Address tAddr = mFNToL2AddressMapping.get(tElement) ;
 				
 				if(pDestination.equals(tAddr)) {
 					if(tElement instanceof ForwardingNode) {
 						return (ForwardingNode) tElement;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public LinkedList<Name> getIntermediateFNs(ForwardingNode pSource,	Route pRoute, boolean pOnlyDestination)
 	{
 		return null;
 	}
 
 
 	/**
 	 * Returns a descriptive string for this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation();
 	}
 
 	/**
 	 * Returns a string describing the location of this instance
 	 * 
 	 * @return the descriptive string
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + (mNode != null ? "@" + mNode.toString() : "");
 		
 		return tResult;
 	}
 }
