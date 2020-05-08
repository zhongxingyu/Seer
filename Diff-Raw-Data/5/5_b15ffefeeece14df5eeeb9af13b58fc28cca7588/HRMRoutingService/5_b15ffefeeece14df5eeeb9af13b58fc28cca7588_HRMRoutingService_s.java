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
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.bus.Bus;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.properties.DatarateProperty;
 import de.tuilmenau.ics.fog.facade.properties.DedicatedQoSReservationProperty;
 import de.tuilmenau.ics.fog.facade.properties.DelayProperty;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.facade.properties.MinMaxProperty.Limit;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.RouteSegmentDescription;
 import de.tuilmenau.ics.fog.routing.RouteSegmentMissingPart;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.*;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ClientFN;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ServerFN;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.DownGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.GateIterator;
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
 	 * Stores the mapping from HRMIDs of local neighbors to their L2 route
 	 */
 	private HashMap<HRMID, Route> mHRMIDToL2RouteMapping = new HashMap<HRMID, Route>();
 
 	/**
 	 * Stores the HRM based routing table which is used for hop-by-hop routing.
 	 */
 	private RoutingTable mRoutingTable = new RoutingTable();
 	
 	/**
 	 * Stores the local L2 addresses based routing graph (consisting of FNs and Gates from local node and links to direct physical neighbors)
 	 */
 	private final RoutableGraph<L2Address, RoutingServiceLink> mL2RoutingGraph = new RoutableGraph<L2Address, RoutingServiceLink>();
 
 	/**
 	 * Stores if the start of the HRMController application instance is still pending
 	 */
 	private boolean mWaitOnControllerstart = true;
 
 	/**
 	 * Stores a database about already known links
 	 */
 	private LinkedList<AbstractGate> mRegisteredLinks = new LinkedList<AbstractGate>();
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pNode the node on which this routing service instance is created on
 	 */
 	public HRMRoutingService(Node pNode)
 	{
 		Logging.log(this, "CREATED ON " + pNode);
 		
 		mNode = pNode;
 		
 		// create name mapping instance to map FoG names to L2 addresses
 		mFoGNamesToL2AddressesMapping = new HierarchicalNameMappingService<L2Address>(HierarchicalNameMappingService.getGlobalNameMappingService(mNode.getAS().getSimulation()), null);
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
 //		if(mNode.getName().equals("node8")){
 //			mHRMController = new HRMController(mNode, this, 4);
 //		}else if(mNode.getName().equals("node9")){
 //			mHRMController = new HRMController(mNode, this, 3);
 //		}else{
 			mHRMController = new HRMController(mNode, this);
 //		}
 		
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
 	 * Return the list of stored neighbor addresses
 	 * 
 	 * @return the list of stored neighbor addresses
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getNeighborAddress()
 	{
 		LinkedList<HRMID> tResult = null;
 		
 		synchronized(mDirectNeighborAddresses){
 			tResult = (LinkedList<HRMID>) mDirectNeighborAddresses.clone();
 		}
 		
 		return tResult;
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
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION;
 //		if(mHRMController.getNodeGUIName().equals("node1")){
 //			DEBUG = true;
 //		}
 		
 		/**
 		 * Check for the max. available data rate to the next hop
 		 */
 		// if the destination is a direct neighbor, the max. avail. DR to the next hop is automatically learned by RoutingEntry.createRouteToDirectNeighbor() 
 		if(pRoutingTableEntry.getDest().isClusterAddress()){
 			HRMID tNextHopHRMID = pRoutingTableEntry.getNextHop();
 			synchronized (mRoutingTable) {
 				for(RoutingEntry tEntry : mRoutingTable){
 					if(tEntry.isRouteToDirectNeighbor()){
 						if(tEntry.getDest().equals(tNextHopHRMID)){
 							pRoutingTableEntry.setNextHopMaxAvailableDataRate(tEntry.getMaxAvailableDataRate());
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		/**
 		 * Store the routing entry in the routing table
 		 */
 		if (DEBUG){
 			Logging.log(this, "Adding HRM route: " + pRoutingTableEntry);
 		}
 		boolean tResult = false;
 		synchronized (mRoutingTable) {
 			tResult = mRoutingTable.addEntry(pRoutingTableEntry);;
 		}
 		
 		/**
 		 * Store the destination HRMID in the HRMID-2-L2Address mapping and as direct neighbor
 		 */
 		// is this routing entry new to us?
 		if(tResult){
 			// get the HRMID of the direct neighbor
 			HRMID tDestHRMID = pRoutingTableEntry.getDest().clone();
 
 			// get the HRMID of the destination
 			HRMID tNextHopHRMID = pRoutingTableEntry.getNextHop().clone();
 
 			// get the L2 address of the next (might be null)
 			L2Address tNextHopL2Address = (pRoutingTableEntry.getNextHopL2Address() != null ? pRoutingTableEntry.getNextHopL2Address().clone() : null);
 
 			/**
 			 * Update neighbor database
 			 */
 			if(pRoutingTableEntry.isRouteToDirectNeighbor()){
 				if(!tDestHRMID.isClusterAddress()){
 					// add address for a direct neighbor
 					if (DEBUG){
 						Logging.log(this, "     ..adding " + tDestHRMID + " as address of a direct neighbor");
 					}
 					synchronized(mDirectNeighborAddresses){
 						if(!mDirectNeighborAddresses.contains(tDestHRMID)){
 							mDirectNeighborAddresses.add(tDestHRMID);
 						}
 					}
 				}
 			}
 
 			Route tNextHopL2Route = null;
 			if(tNextHopL2Address != null)
 			{
 				//Logging.log(this, "Found a next network interface entry in " + pRoutingTableEntry);
 				tNextHopL2Route = getL2RouteViaNetworkInterface(tNextHopL2Address, pRoutingTableEntry.getNextHopL2NetworkInterface());
 			}
 			if (DEBUG){
 				Logging.log(this, "     ..L2 route to next hop " + tNextHopL2Address + " is: " + tNextHopL2Route);
 			}
 			
 			/**
 			 * Update neighbor database and HRMID-t-L2Address mapping
 			 */ 
 			if (tNextHopL2Address != null){
 				if(!tNextHopHRMID.isClusterAddress()){
 					/**
 					 * Update HRMID-2-L2Address mapping
 					 */
 					// add L2 address for this direct neighbor
 					if (DEBUG){
 						Logging.log(this, "     ..add mapping from " + tNextHopHRMID + " to " + tNextHopL2Address);
 					}
 					mapHRMID(tNextHopHRMID, tNextHopL2Address, tNextHopL2Route);
 				}
 			}else{
 				/**
 				 * For local loopback routes we also learn the HRMID-2-L2Address mapping
 				 */
 				if(pRoutingTableEntry.isLocalLoop()){
 					/**
 					 * Update mapping HRMID-2-L2Address
 					 */
 					// add L2 address for this direct neighbor
 					if (DEBUG){
 						Logging.log(this, "     ..add mapping from " + tNextHopHRMID + " to " + getHRMController().getNodeL2Address());
 					}
 					mapHRMID(tNextHopHRMID, getHRMController().getNodeL2Address(), tNextHopL2Route);
 				}
 			}
 		}
 		
 		return tResult;
 	}	
 
 	/**
 	 * Deletes a route from the local HRM routing table.
 	 * This function is usually used when a timeout occurred and the corresponding route became too old. 
 	 * 
 	 * @param pRoutingTableEntry the routing table entry
 	 *  
 	 * @return true if the entry was found and removed, otherwise false
 	 */
 	public boolean delHRMRoute(RoutingEntry pRoutingTableEntry)
 	{
 		/**
 		 * Remove the routing entry from the routing table
 		 */
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Deleting HRM route: " + pRoutingTableEntry);
 		}
 		boolean tResult = mRoutingTable.delEntry(pRoutingTableEntry);
 		
 		/**
 		 * Remove the destination HRMID from the HRMID-2-L2Address mapping and as direct neighbor
 		 */
 		if(tResult){
 			// get the HRMID of the direct neighbor
 			HRMID tDestHRMID = pRoutingTableEntry.getDest().clone();
 
 			// get the HRMID of the destination
 			HRMID tNextHopHRMID = pRoutingTableEntry.getNextHop().clone();
 
 			/**
 			 * Update neighbor database
 			 */
 			if(pRoutingTableEntry.isRouteToDirectNeighbor()){
 				if(!tDestHRMID.isClusterAddress()){
 					// add address for a direct neighbor
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "     ..removing " + tDestHRMID + " as address of a direct neighbor");
 					}
 					synchronized(mDirectNeighborAddresses){
 						if(mDirectNeighborAddresses.contains(tDestHRMID)){
 							mDirectNeighborAddresses.remove(tDestHRMID);
 						}
 					}
 				}
 
 				/**
 				 * Update mapping HRMID-2-L2Address
 				 */
 				// add L2 address for this direct neighbor
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..remove HRMID-2-L2Address mapping for " + tDestHRMID);
 				}
 				unmapHRMID(tDestHRMID);
 			}
 
 			if(pRoutingTableEntry.isLocalLoop()){
 				/**
 				 * Update mapping HRMID-2-L2Address
 				 */
 				unmapHRMID(tDestHRMID);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Auto-cleans the L2 routing graph from deprecated routes
 	 */
 	private void autoCleanL2GraphFromDeprecatedEntries()
 	{
 		Multiplexer tCentralFN = getCentralFN();
 		
 		synchronized (mL2RoutingGraph) {
 			boolean tDeletedOne = false;
 			do{
 				tDeletedOne = false;
 				
 				// get the FoG-specific central FN and determine all outgoing links
 				Collection<RoutingServiceLink> tLinksFromCentralFN = (Collection<RoutingServiceLink>) mL2RoutingGraph.getOutEdges(mHRMController.getNodeL2Address());
 				
 				/**
 				 * iterate over all links outgoing from central FN and look for logical links (routes)
 				 */
 				for(RoutingServiceLink tKnownL2Link : tLinksFromCentralFN){
 					if(tKnownL2Link instanceof L2LogicalLink){
 						if(tKnownL2Link instanceof L2LogicalLink){
 							L2LogicalLink tKnownL2RouteLink = (L2LogicalLink)tKnownL2Link;
 							Route tKnownL2Route = tKnownL2RouteLink.getRoute();
 							if(tKnownL2Route.getFirst() instanceof RouteSegmentPath){							
 								RouteSegmentPath tGateIDList = (RouteSegmentPath) tKnownL2Route.getFirst();
 								if(tGateIDList.size() > 1){
 									GateID tFirstGateID = tGateIDList.getFirst();
 									GateID tSecondGateID = tGateIDList.get(1);
 									boolean tDeprecated = true;
 									AbstractGate tFirstGate = tCentralFN.getGate(tFirstGateID);
 									if(tFirstGate != null){
 										Multiplexer tIntermediateFN = (Multiplexer)tFirstGate.getNextNode();
 										AbstractGate tSecondGate = tIntermediateFN.getGate(tSecondGateID);
 										if(tSecondGate != null){
 											tDeprecated = false;
 										}
 									}
 									
 									if(tDeprecated){
 										Logging.warn(this, ">>>>>>>>>>>>>>> Found deprecated L2 route: " + tKnownL2Route);
 										
 										mL2RoutingGraph.unlink(tKnownL2Link);
 										tDeletedOne = true;
 										break;
 									}
 								}
 							}
 						}
 					}
 				}			
 			}while(tDeletedOne);
 		}
 	}
 	
 	/**
 	 * Registers a BE route in the local L2 routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pToL2Address the L2Address of the destination
 	 * @param pRoute the route to the direct neighbor
 	 * 
 	 * @return returns true if the route was stored and an GUI update is needed
 	 */
 	public boolean registerL2RouteBestEffort(L2Address pToL2Address, Route pRoute)
 	{
 		boolean tResult = true;
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION;
 				
 		if (DEBUG){
 			Logging.log(this, "REGISTERING L2 BE ROUTE: dest.=" + pToL2Address + ", route=\"" + pRoute + "\"");
 		}
 
 		autoCleanL2GraphFromDeprecatedEntries();
 		
 		if (pToL2Address != null){
 			
 			/**
 			 * Determine the old logical L2 link towards the neighbor
 			 */
 			Route tOldRoute = null;
 			L2LogicalLink tOldL2Link = null;
 			List<RoutingServiceLink> tOldLinkList = getRouteFromGraph(mL2RoutingGraph, getCentralFNL2Address(), pToL2Address);
 			if((tOldLinkList != null) && (tOldLinkList.size() == 1)){
 				// get the first and only route entry
 				RoutingServiceLink tLink = tOldLinkList.get(0);
 				if(tLink instanceof L2LogicalLink){
 					// get the logical L2 link
 					tOldL2Link = (L2LogicalLink) tLink;
 					
 					// get the old route from the logical L2 link description
 					tOldRoute = tOldL2Link.getRoute();					
 
 					if (DEBUG){
 						Logging.log(this, "      ..found old route: " + tOldRoute + " to: " + pToL2Address);
 					}
 				}
 			}
 
 			/**
 			 * Clone the route
 			 */
 			Route tNewRoute = pRoute.clone();
 
 			/**
 			 * Check if the new route is a duplicate of the best old one
 			 */
 			boolean tDuplicate = (tOldRoute != null ? tNewRoute.equals(tOldRoute) : false);
 
 			/**
 			 * Check if the new route isn't too long -> otherwise, drop this route
 			 */
 			boolean tNewRouteIsTooLong = (tOldRoute != null ? tNewRoute.isLonger(tOldRoute) : false);
 			
 			
 			if((!tNewRouteIsTooLong) && (!tDuplicate)){
 				/**
 				 * Check if the new route is shorter than the best old known one. In this case, drop all longer old routes.
 				 */
 				if (tOldRoute != null){
					if (tNewRoute.isShorter(tOldRoute)){
 						if (DEBUG){
 							Logging.warn(this, "      ..updating to better ROUTE \"" + tNewRoute + "\" to direct neighbor: " + pToL2Address);
 						}
 	
 						/**
 						 * delete all longer routes: they could be reported based on hierarchy communication
 						 */
 						@SuppressWarnings("unchecked")
 						LinkedList<RoutingServiceLink> tAllL2Routes = mL2RoutingGraph.getEdges(getCentralFNL2Address(), pToL2Address);
 						if(tAllL2Routes != null){
 							// iterate over all found links
 							for(RoutingServiceLink tKnownL2Link : tAllL2Routes) {
 								if(tKnownL2Link instanceof L2LogicalLink){
 									L2LogicalLink tKnownL2RouteLink = (L2LogicalLink)tKnownL2Link;
 									Route tKnownL2Route = tKnownL2RouteLink.getRoute();
									if(tNewRoute.isShorter(tKnownL2Route)){
 										mL2RoutingGraph.unlink(tKnownL2Link);
 									}
 								}
 							}
 						}					
 					}
 				}
 				
 				boolean tOneHopRouteAlreadyKnown = false;
 				
 				/**
 				 * iterate over all known L2 links and check for duplicates
 				 */
 				@SuppressWarnings("unchecked")
 				LinkedList<RoutingServiceLink> tAllL2Routes = mL2RoutingGraph.getEdges(getCentralFNL2Address(), pToL2Address);
 				if(tAllL2Routes != null){
 					// iterate over all found links
 					for(RoutingServiceLink tKnownL2Link : tAllL2Routes) {
 						if(tKnownL2Link instanceof L2LogicalLink){
 							L2LogicalLink tKnownL2RouteLink = (L2LogicalLink)tKnownL2Link;
 							Route tKnownL2Route = tKnownL2RouteLink.getRoute();
 							
 							/**
 							 * Check for route duplicates
 							 */
 							if(tKnownL2Route.equals(pRoute)){
 								tDuplicate = true;
 								break;
 							}
 							
 							/**
 							 * Check for one-hop route
 							 */
 							if(tKnownL2Route.size() <= 2){
 								tOneHopRouteAlreadyKnown = true;	
 							}							
 						}
 					}
 				}					
 
 				if(!tDuplicate){
 					/**
 					 * Do we have a multi-hop route? -> either a distant node or a route to a neighbor, which is not the shortest possible one
 					 */
 					if(pRoute.size() > 2 /* FoG-based route to the next hop has structure "[[gate list] , [L2Address]] and has a size of 2 */){
 						/**
 						 * Is this multi-hop route maybe a too-long route to a direct neighbor?
 						 */
 						if(!tOneHopRouteAlreadyKnown){
 							/**
 							 * iterate over all found already known routes and delete them in order to use always the MOST FRESH route to a distant node
 							 */
 							for(RoutingServiceLink tKnownL2Link : tAllL2Routes) {
 								if(tKnownL2Link instanceof L2LogicalLink){
 									mL2RoutingGraph.unlink(tKnownL2Link);
 								}
 							}
 							
 							if (DEBUG){
 								Logging.warn(this, ">>>>>>>>>>>>>      ..storing the new ROUTE \"" + tNewRoute + "\" to distant node: " + pToL2Address + " with size: " + pRoute.size() + "(" + (pRoute.size() / 2) + " nodes)");
 							}
 						}else{
 							Logging.err(this, "      ..found unexpected a too-long ROUTE \"" + tNewRoute + "\" to neighbor node: " + pToL2Address + " with size: " + pRoute.size() + "(" + (pRoute.size() / 2) + " nodes)");
 							tNewRouteIsTooLong = true;
 						}
 					}else{
 						// iterate over all found already known routes and delete all too-long ones in order to use always the shortest one to the direct neighbor
 						for(RoutingServiceLink tKnownL2Link : tAllL2Routes) {
 							if(tKnownL2Link instanceof L2LogicalLink){
 								L2LogicalLink tKnownL2RouteLink = (L2LogicalLink)tKnownL2Link;
 								Route tKnownL2Route = tKnownL2RouteLink.getRoute();
 								// is it multi-hop?
 								if(tKnownL2Route.size() > 2){
 									Logging.err(this, "      ..found unexpected the too-long ROUTE \"" + tNewRoute + "\" to neighbor node: " + pToL2Address + " with size: " + pRoute.size() + "(" + (pRoute.size() / 2) + " nodes)");
 									mL2RoutingGraph.unlink(tKnownL2Link);
 								}
 							}
 						}
 
 					}
 				}
 			}
 			
 			if(!tDuplicate){
 				if(!tNewRouteIsTooLong){
 					if (DEBUG){
 						Logging.log(this, "      ..storing NEW ROUTE \"" + tNewRoute + "\" to: " + pToL2Address + " with size: " + pRoute.size());
 					}
 					// store the new route
 					storeL2Link(getCentralFNL2Address(), pToL2Address, new L2LogicalLink(tNewRoute));
 				}else{
 					if (DEBUG){
 						Logging.log(this, "      ..dropping TOO-LONG ROUTE \"" + tNewRoute + "\" to neighbor node: " + pToL2Address + " with size: " + pRoute.size() + "(" + (pRoute.size() / 2) + " nodes)");
 					}
 				}
 			}else{
 				Logging.log(this, "      ..is DUPLICATE of already known ROUTE \"" + tNewRoute + "\" to: " + pToL2Address);
 			}
 		}else{
 			Logging.err(this, "addRouteToDirectNeighbor() got an invalid neighbor L2Address");
 
 			// route was dropped
 			tResult = false;
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the local HRM routing table
 	 * 
 	 * @return the local HRM routing table
 	 */
 	public RoutingTable getRoutingTable()
 	{
 		RoutingTable tResult = null;
 		
 		synchronized (mRoutingTable) {
 			tResult = (RoutingTable) mRoutingTable.clone();
 		}
 		
 		return tResult;		
 	}
 	
 	/**
 	 * Returns the list of known mappings from neighbor HRMIDs to their L2 routes
 	 * 
 	 * @return the desired list of mappings
 	 */
 	public HashMap<HRMID, Route> getHRMIDToL2RouteMapping()
 	{
 		HashMap<HRMID, Route> tResult = new HashMap<HRMID, Route>();
 		
 		synchronized (mHRMIDToL2RouteMapping) {
 			for (HRMID tAddr : mHRMIDToL2RouteMapping.keySet()){
 				Route tL2Route = mHRMIDToL2RouteMapping.get(tAddr);
 				if (tL2Route != null){
 					tResult.put(tAddr.clone(), tL2Route.clone());
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
 						tResult.add(new RoutingServiceLink(tID, null));
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
 	 * Stores a link in the local L2 specific routing graph
 	 * 
 	 * @param pFromL2Address the starting point of the link
 	 * @param pToL2Address the ending point of the link
 	 * @param pRoutingServiceLink the link description
 	 * 
 	 * @return true if the link was added to the graph, false if the link was already known
 	 */
 	private boolean storeL2Link(L2Address pFromL2Address, L2Address pToL2Address, RoutingServiceLink pRoutingServiceLink)
 	{
 		boolean tAdded = false;
 		
 //		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK IN L2 GRAPH:  source=" + pFromL2Address + " ## dest.=" + pToL2Address + " ## link=" + pRoutingServiceLink);
 //		}
 		
 		synchronized (mL2RoutingGraph) {
 			L2Address tFrom = pFromL2Address;
 			L2Address tTo = pToL2Address;
 			if (tFrom.equals(getCentralFNL2Address())){
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "   ..mark as central address: " + tFrom);
 				}
 				tFrom.setHostCentralNode();
 			}
 			if (tTo.equals(getCentralFNL2Address())){
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "   ..mark as central address: " + tTo);
 				}
 				tTo.setHostCentralNode();
 			}
 
 			tAdded = mL2RoutingGraph.link(tFrom, tTo, pRoutingServiceLink);
 			if(tAdded){
 				Logging.log(this, "  ..stored L2 link " + pRoutingServiceLink + " from " + tFrom + " to " + tTo);
 			}
 		}
 		
 		return tAdded;
 	}
 
 	/**
 	 * Returns the destination node of a link from the local FoG based routing graph
 	 * This function is used within HRMController::getL2AddressOfFirstFNTowardsNeighbor().
 	 * 
 	 * @param pLink the link for which the destination has to be determined
 	 * 
 	 * @return the search destination node
 	 */
 	public HRMName getL2LinkDestination(RoutingServiceLink pLink)
 	{
 		HRMName tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "Search for the L2 destination node of " + pLink);
 		}
 		
 		synchronized (mL2RoutingGraph) {
 			tResult = mL2RoutingGraph.getDest(pLink);
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Creates a mapping from a FoG name to an L2Address
 	 *   
 	 * @param pName the FoG name
 	 * @param pL2Address the L2 address
 	 * @throws RemoteException
 	 */
 	public void mapFoGNameToL2Address(Name pName, Name pL2Address)
 	{
 		Logging.log(this, "REGISTERING NAME-to-L2ADDRESS MAPPING: " + pName + " to " + pL2Address);
 
 		if (pL2Address != null){
 			if (pL2Address instanceof L2Address){
 				if(!isKnown(pName)) {
 					synchronized (mFoGNamesToL2AddressesMapping) {
 						mFoGNamesToL2AddressesMapping.registerName(pName, (L2Address)pL2Address, NamingLevel.NAMES);
 					}
 				}else{
 					Logging.warn(this, "       ..skipped registration of NAME-to-L2Address because a mapping already exists to the L2Address: " + getL2AddressFor(pName).toString());
 				}
 			}else{
 				Logging.err(this, "Given L2Address has invalid type: " + pL2Address);
 			}
 		}else{
 			Logging.warn(this, "       ..skipped registration of NAME-to-L2Address because the L2Address is invalid");
 		}
 	}
 
 	/**
 	 * Creates a mapping from an HRMID to an L2Address
 	 * 
 	 * @param pHRMID the HRMID 
 	 * @param pL2Address the L2Address
 	 */
 	public void mapHRMID(HRMID pHRMID, L2Address pL2Address, Route pL2Route)
 	{
 		boolean DEBUG = false;
 		
 		if(!pHRMID.isClusterAddress()){
 			boolean tDuplicateFound = false;
 			
 			synchronized (mHRMIDToL2AddressMapping) {
 				for (HRMID tHRMID: mHRMIDToL2AddressMapping.keySet()){
 					if (tHRMID.equals(pHRMID)){
 						tDuplicateFound = true;
 						if(DEBUG){
 							Logging.log(this, "mapHRMID() - found HRMID2L2Address mapping duplicate for: " + pHRMID);
 						}
 						break;
 					}
 				}
 				if (!tDuplicateFound){
 					if(DEBUG){
 						Logging.log(this, "mapHRMID() - adding HRMID2L2Address mapping for: " + pHRMID + " and " + pL2Address);
 					}
 					mHRMIDToL2AddressMapping.put(pHRMID, pL2Address);
 				}else{
 					// HRMID is already known, mapping already exists
 				}
 			}
 			
 			/**
 			 * try to find BE based L2 route if none was explicitly given
 			 */
 			if(pL2Route == null){
 				//Logging.log(this, "Searching for an L2 route to " + pHRMID);
 				pL2Route = mHRMController.getHRS().getL2RouteViaNetworkInterface(pL2Address, null);
 				if(DEBUG){
 					Logging.log(this, "mapHRMID() - found BE route for: " + pHRMID + " as: " + pL2Route);
 				}
 			}
 			
 			/**
 			 * have we a valid L2 route to the neighbor?
 			 */
 			if(pL2Route != null){
 				synchronized (mHRMIDToL2RouteMapping) {
 					Route tOldRoute = mHRMIDToL2RouteMapping.get(pHRMID);
 					if((tOldRoute == null) || ((!tOldRoute.equals(pL2Route)) && (pL2Route.isShorter(tOldRoute)))){
 						if(DEBUG){
 							Logging.warn(this, "mapHRMID() - got new L2 route towards: " + pHRMID + " as: " + pL2Route);
 						}
 						mHRMIDToL2RouteMapping.put(pHRMID, pL2Route);
 					}
 				}					
 			}else{
 				Logging.log(this, "mapHRMID() - have not found an L2 route for: " + pHRMID);
 			}
 		}else{
 			Logging.warn(this, "mapHRMID() - aborted due to found cluster address: " + pHRMID);
 		}
 	}
 
 	/**
 	 * Removes a mapping for a given HRMID
 	 * 
 	 * @param pHRMID the HRMID for which the mapping should be removed
 	 */
 	public void unmapHRMID(HRMID pHRMID)
 	{
 		synchronized (mHRMIDToL2AddressMapping) {
 			for (HRMID tHRMID: mHRMIDToL2AddressMapping.keySet()){
 				if (tHRMID.equals(pHRMID)){
 					mHRMIDToL2AddressMapping.remove(pHRMID);
 					break;
 				}
 			}
 		}
 		synchronized (mHRMIDToL2RouteMapping) {
 			for (HRMID tHRMID: mHRMIDToL2RouteMapping.keySet()){
 				if (tHRMID.equals(pHRMID)){
 					//Logging.warn(this, "Dropping L2 route towards: " + pHRMID + " as: " + mHRMIDToL2RouteMapping.get(pHRMID));
 					mHRMIDToL2RouteMapping.remove(pHRMID);
 					break;
 				}
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
 			if(tResult != null){
 				tResult = tResult.clone();
 			}
 		}
 	
 //		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 //			Logging.log(this, "Determined name for " + pNode.toString() + " as " + tResult);
 //		}
 		
 		return tResult;
 	}	
 	@Override
 	public L2Address getNameFor(ForwardingNode pNode)
 	{
 		return getL2AddressFor(pNode);
 	}
 
 	/**
 	 * This function is part of the RoutingService interface. It executes a reverse lookup for a given L2Address 
 	 * and determines the corresponding FN. The function is used within the transfer plane in order to check if 
 	 * a given destination is local or not.
 	 * 
 	 *  @return the determined FN, null if no FN was found 
 	 */
 	@Override
 	public ForwardingNode getLocalElement(Name pSearchedL2Address)
 	{
 		ForwardingNode tResult = null;
 		
 // HINT: The following lines would provide the correct functionality but this would lead to routing problems within TransferPlane::getRoute when a DestinationApplicationProperty requirement is used.
 //       By deactivating this function, we enforce calls to the HRS instance for each "incompleteRoute" event.
 //
 //		
 //		// is the search L2Address valid?
 //		if(pSearchedL2Address != null) {
 //			
 //			// iterate over all known local FEs(FNs)
 //			for(ForwardingElement tFE : mFNToL2AddressMapping.keySet()) {
 //				// get the L2Address of the current FE
 //				L2Address tFoundL2Address = mFNToL2AddressMapping.get(tFE);
 //				
 //				// have we found the searched mapping?
 //				if(pSearchedL2Address.equals(tFoundL2Address)) {
 //					// check if the FE is also an FN
 //					if(tFE instanceof ForwardingNode) {
 //						// we have a valid result
 //						tResult = (ForwardingNode) tFE;
 //						
 //						// return immediately
 //						break;
 //					}else{
 //						Logging.warn(this, "     ..found FE entry isn't derived from an FN, entry=" + tFE);
 //					}
 //				}
 //			}
 //		}
 		
 		return tResult;
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
 	public L2Address getL2AddressFor(HRMID pHRMID)
 	{
 		L2Address tResult = null;
 	
 		if(mHRMController.isLocal(pHRMID)){
 			return mHRMController.getNodeL2Address();
 		}
 				
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
 	 * Determines the stored L2 route for a given HRMID
 	 * 
 	 * @param pHRMID the HRMID for which the L" route has to be determined
 	 * @return the resulting L2 route, returns "null" if no mapping was found
 	 */
 	public Route getL2RouteExplicitMapping(HRMID pHRMID)
 	{
 		Route tResult = null;
 		
 		synchronized (mHRMIDToL2RouteMapping) {
 			// iterate over all mappings
 			for (HRMID tHRMID : mHRMIDToL2RouteMapping.keySet()){
 				// compare the values
 				if (tHRMID.equals(pHRMID)){
 					// get the L2 address
 					tResult = mHRMIDToL2RouteMapping.get(tHRMID);
 					if(HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.warn(this, "Found explicit L2 mapping from: " + pHRMID + " to: " + tResult);
 					}
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
 				L2Address tNodeL2Address = L2Address.createL2Address(this);
 				tNodeL2Address.setDescr(pElement.getOwner().toString() + "@" + HRMController.getHostName() + " => " + pElement.toString());
 				
 				if (pElement.equals(getCentralFN())){
 					Logging.log(this, "     ..registering L2 address for central FN: " + tNodeL2Address);
 					mCentralFNL2Address = tNodeL2Address;
 				}
 				
 				/** 
 				 * Register mapping from FN to L2address
 				 */
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..registering NAME MAPPING for FN \"" + pElement + "\": L2address=\"" + tNodeL2Address + "\", level=" + pLevel);
 				}
 				if(pElement instanceof ServerFN){
 					tNodeL2Address.setServer();
 				}
 				if(pElement instanceof ClientFN){
 					tNodeL2Address.setClient();
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
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UNREGISTERING NODE: " + pElement);
 		}
 
 		// get the L2Address of this FN
 		L2Address tNodeL2Address = getL2AddressFor(pElement);
 
 		// have we found an L2 address?
 		if (tNodeL2Address != null){
 			/**
 			 * remove the FN from the L2 specific routing graph
 			 */
 			synchronized (mL2RoutingGraph) {
 				if(mL2RoutingGraph.contains(tNodeL2Address)) {
 					mL2RoutingGraph.remove(tNodeL2Address);
 				}
 			}
 		}
 			
 		return true;
 	}
 	
 	/**
 	 * Registers a link in the local L2 specific routing graph
 	 * HINT: This function must not block. Otherwise, the entire FoGSiEm GUI will hang until this functions returns.
 	 * 
 	 *  @param pFrom the FN where the link begins
 	 *  @param pGate the Gate (link) which is to be registered
 	 *  @throws NetworkException
 	 */
 	@Override
 	public void registerLink(ForwardingElement pFrom, AbstractGate pGate) throws NetworkException
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK: source=" + pFrom + " ## dest.=" + pGate.getNextNode() + " ## attr.=" + pGate.getDescription() + " ## gate=" + pGate + " ## peer=" + pGate.getRemoteDestinationName());
 		}
 
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
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Source node " + pFrom +" of link " + pGate + " isn't known yet. It will be registered implicitly.");
 				}
 				
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
 		// mark as node-internal link
 		boolean tIsPhysicalLinkToPhysicalNeigborNode = false;
 		NetworkInterface tInterfaceToNeighbor = null;
 		if( pGate instanceof DirectDownGate){
 			// get the direct down gate
 			DirectDownGate tDirectDownGate = (DirectDownGate)pGate;
 			
 			Description tGateDescription = tDirectDownGate.getDescription();
 			
 			// get the network interface to the neighbor
 			tInterfaceToNeighbor = tDirectDownGate.getNetworkInterface();
 			
 			// determine the L2Address of the destination FN for this gate
 			// HINT: For DirectDownGate gates, this address is defined in "DirectDownGate" by a call to "RoutingService.getL2AddressFor(ILowerLayer.getMultiplexerGate())".
 			//       However, there will occur two calls to registerLink():
 			//				* 1.) the DirectDownGate is created
 			//				* 2.) the peer has answered by a packet of "OpenGateResponse" and the peer name is now known
 			//       Therefore, we ignore the first registerLink() request and wait for the (hopefully) appearing second request.
 			tToL2Address = (L2Address) pGate.getRemoteDestinationName();
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				if (tToL2Address == null){
 					Logging.log(this, "Peer name wasn't available via AbstractGate.getRemoteDestinationName(), will skip this registerLink() request and wait until the peer is known");
 				}
 				Logging.log(this, "      ..external link, which ends at the physical node: " + tToL2Address);
 			}
 
 			/**
 			 * Detect if the DirectDownGate belongs to a dedicated QoS reservation
 			 */
 			boolean tBelongsToQoSReserveration = false;
 			if(tGateDescription != null){
 				tBelongsToQoSReserveration = tGateDescription.get(DedicatedQoSReservationProperty.class) != null;
 			}
 			
 			if(!tBelongsToQoSReserveration){
 				// mark as link to another node
 				tIsPhysicalLinkToPhysicalNeigborNode = true;
 				
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Found new physical link: " + tDirectDownGate + ", description=" + tGateDescription);
 				}
 			}else{
 				// tell the HRMController instance that we have reserved some resources and it should trigger an early routing data update
 				mHRMController.eventQoSReservation(tToL2Address, tGateDescription);
 
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Found new QoS reservation link: " + tDirectDownGate + ", description=" + tGateDescription);
 				}
 			}
 		}else{
 			// we have any kind of a gate, we determine its ending point
 			ForwardingNode tToFN = (ForwardingNode)pGate.getNextNode();
 			
 			/**
 			 * Make sure that the starting point of the link is already known to the FN-to-L2address mapping.
 		 	*/
 			tToL2Address = getL2AddressFor(tToFN);
 			// is the FN still unknown for the FN-to-L2address mapping?
 			if(tToL2Address == null) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Destination node " + tToFN +" of link " + pGate + " isn't known yet. It will be registered implicitly.");
 				}
 				
 				registerNode(tToFN, null, NamingLevel.NONE, null);
 				
 				// determine the L2address of the starting point to check if the registration was successful
 				tToL2Address = getL2AddressFor(tToFN);
 				if(tToL2Address == null) {
 					throw new RuntimeException(this + " - Destination FN " + pFrom + " is still unknown to the FN-to-L2address mapping, although it was registered before.");
 				}
 			}
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..internal link, which ends at the local node " + tToL2Address);
 			}
 		}
 
 		if(tToL2Address == null) {
 			// return immediately because the peer name is sill unknown
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "registerLink() - peer name is still unknown, waiting for the second request (source=" + tFromL2Address + ", gate=" + pGate + ")");
 			}
 			return;
 		}
 		
 		/**
 		 * Check if the link is new
 		 */
 		boolean tLinkIsNew = false;
 		synchronized (mRegisteredLinks) {
 			if(!mRegisteredLinks.contains(pGate)){
 				mRegisteredLinks.add(pGate);
 				tLinkIsNew = true;
 			}
 			
 		}
 		
 		/**
 		 * Add link to L2 specific routing graph
 		 */
 		if(tLinkIsNew){
 			storeL2Link(tFromL2Address, tToL2Address, new RoutingServiceLink(pGate.getGateID(), null));
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "   ..link is already known: " + pGate);
 			}
 		}
 
 		/**
 		 * DIRECT NEIGHBOR FOUND: create a HRM connection to it
 		 */
 		if(tIsPhysicalLinkToPhysicalNeigborNode) {
 			L2Address tThisHostL2Address = getL2AddressFor(getCentralFN());
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..NODE " + tThisHostL2Address + " FOUND POSSIBLE DIRECT NEIGHBOR: " + tToL2Address + "?");
 			}
 
 			if(tLinkIsNew){
 				if((!pFrom.equals(tThisHostL2Address)) && (!tToL2Address.equals(tThisHostL2Address))) {
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "    ..actually found an interesting link from " + tThisHostL2Address + " to " + tToL2Address + " via FN " + pFrom);
 					}
 					getHRMController().eventDetectedPhysicalNeighborNode(tInterfaceToNeighbor, tToL2Address);
 				}else{
 					Logging.warn(this, "registerLink() ignores the new link to a possible neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address + " because it is linked to the central FN " + tThisHostL2Address);
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.warn(this, "registerLink() ignores the link to a possible neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address + " because it is already known");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Unregisters a link from the local L2 specific routing graph
 	 * HINT: This function must not block. Otherwise, the entire FoGSiEm GUI will hang until this functions returns.
 	 * 
 	 *  @param pFrom the FN where the link begins
 	 *  @param pGate the Gate (link) which is to be unregistered
 	 */  
 	@Override
 	public boolean unregisterLink(ForwardingElement pFrom, AbstractGate pGate)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.warn(this, "#### UNREGISTERING LINK from " + pFrom + " to " + pGate.getNextNode() + ", gate " + pGate);
 		}
 
 		/**
 		 * Determine the L2Address of the starting point.
 		 */
 		L2Address tFromL2Address = null;
 		// check the class type of pFrom
 		if (pFrom instanceof ForwardingNode){
 			// get the FN where the route should start
 			ForwardingNode tFromFN = (ForwardingNode)pFrom;
 			
 			// determine the L2address of the starting point
 			tFromL2Address = getL2AddressFor(tFromFN);
 		}else{
 			// pFrom isn't derived from ForwardingNode
 			throw new RuntimeException(this + " - Source FE " + pFrom + " has the wrong class hierarchy(ForwardingNode is missing).");
 		}
 
 		/**
 		 * Check if the link is one to another physical neighbor node or not.
 		 * Determine the L2Address of the ending point of the link
 		 */
 		L2Address tToL2Address = null;
 		// mark as node-internal link
 		boolean tIsPhysicalLinkToPhysicalNeigborNode = false;
 		NetworkInterface tInterfaceToNeighbor = null;
 
 		if( pGate instanceof DirectDownGate){
 			// get the direct down gate
 			DirectDownGate tDirectDownGate = (DirectDownGate)pGate;
 			
 			Description tGateDescription = tDirectDownGate.getDescription();
 
 			// get the network interface to the neighbor
 			tInterfaceToNeighbor = tDirectDownGate.getNetworkInterface();
 
 			// determine the L2Address of the destination FN for this gate
 			// HINT: For DirectDownGate gates, this address is defined in "DirectDownGate" by a call to "RoutingService.getL2AddressFor(ILowerLayer.getMultiplexerGate())".
 			//       However, there will occur two calls to registerLink():
 			//				* 1.) the DirectDownGate is created
 			//				* 2.) the peer has answered by a packet of "OpenGateResponse" and the peer name is now known
 			//       Therefore, we ignore the first registerLink() request and wait for the (hopefully) appearing second request.
 			tToL2Address = (L2Address) pGate.getRemoteDestinationName();
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				if (tToL2Address == null){
 					Logging.warn(this, "Peer name wasn't avilable via AbstractGate.getRemoteDestinationName(), will skip this unregisterLink() request and wait until the peer is known");
 				}
 			}
 			
 			/**
 			 * Detect if the DirectDownGate belongs to a dedicated QoS reservation
 			 */
 			boolean tBelongsToQoSReserveration = false;
 			if(tGateDescription != null){
 				tBelongsToQoSReserveration = tGateDescription.get(DedicatedQoSReservationProperty.class) != null;
 			}
 			
 			if(!tBelongsToQoSReserveration){
 				// mark as link to another node
 				tIsPhysicalLinkToPhysicalNeigborNode = true;
 
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Found deprecated physical link: " + tDirectDownGate + ", description=" + tGateDescription);
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Found deprecated QoS reservation link: " + tDirectDownGate + ", description=" + tGateDescription);
 				}
 			}
 		}
 			
 		if(tToL2Address == null) {
 			// return immediately because the peer name is sill unknown
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "registerLink() - peer name is still unknown, waiting for the second request (source=" + tFromL2Address + ", gate=" + pGate + ")");
 			}
 			return false;
 		}
 
 		/**
 		 * Remove the link from the L2 specific routing graph
 		 */
 		synchronized (mL2RoutingGraph) {
 			// determine which links are outgoing from the FN "pFrom"
 			Collection<RoutingServiceLink> tOutgoingLinksFromFN = mL2RoutingGraph.getOutEdges(tFromL2Address);
 			// have we found at least one outgoing link?
 			if(tOutgoingLinksFromFN != null) {
 				for(RoutingServiceLink tOutgoingLink : tOutgoingLinksFromFN) {
 					// have we found the right outgoing link? (we check the GateIDs)
 					if(tOutgoingLink.equals(pGate)) {
 						// remove the link from the L2 specific routing graph
 						mL2RoutingGraph.unlink(tOutgoingLink);
 						break;
 					}
 				}
 			}
 		}
 		
 		/**
 		 * DIRECT NEIGHBOR FOUND: create a HRM connection to it
 		 */
 		if(tIsPhysicalLinkToPhysicalNeigborNode) {
 			// update L2 graph
 			autoCleanL2GraphFromDeprecatedEntries();
 			
 			L2Address tThisHostL2Address = getL2AddressFor(getCentralFN());
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..NODE " + tThisHostL2Address + " FOUND POSSIBLE DIRECT NEIGHBOR: " + tToL2Address + "?");
 			}
 
 			if((!pFrom.equals(tThisHostL2Address)) && (!tToL2Address.equals(tThisHostL2Address))) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "    ..actually found an interesting lost link from " + tThisHostL2Address + " to " + tToL2Address + " via FN " + pFrom);
 				}
 				getHRMController().eventLostPhysicalNeighborNode(tInterfaceToNeighbor, tToL2Address);
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.warn(this, "unregisterLink() ignores the lost link to the neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address + " because it is linked to the central FN " + tThisHostL2Address);
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
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UPDATING NODE " + pElement + ": old caps.=" + mNode.getCapabilities() + ", new caps.=" + pCapabilities);
 		}
 
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
 	 * Determines a list of Gate IDs from an L2 address to another L2 address based on the L2 specific routing graph.
 	 * 
 	 * @param pFromL2Address the starting point of the desired route
 	 * @param pToL2Address the ending point of the desired route
 	 * @return a list of Gate IDs to the neighbor node, returns "null" if no route was found
 	 */
 	private List<RoutingServiceLink> getL2GateIDsForRoute(L2Address pFromL2Address, L2Address pToL2Address)
 	{
 		List<RoutingServiceLink> tResult = null;
 
 		synchronized (mL2RoutingGraph) {
 			// query route in the L2 specific routing graph
 			tResult = getRouteFromGraph(mL2RoutingGraph, pFromL2Address, pToL2Address);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines a route from an L2 address to another one, based on the L2 specific routing graph.
 	 * 
 	 * @param pFromL2Address the L2 address of the starting point
 	 * @param pToL2Address the L2 address of the ending point
 	 * @return the found route, returns null if no route was available
 	 */
 	private Route getL2RouteBestEffort(L2Address pFromL2Address, L2Address pToL2Address)
 	{
 		Route tResultRoute = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET ROUTE from \"" + pFromL2Address + "\" to \"" + pToL2Address +"\"");
 		}
 		
 		// is the source equal to the destination? 
 		if (!pFromL2Address.equals(pToL2Address)){
 			// get a list of Gate IDs to the destination
 			List<RoutingServiceLink> tGateIDsToDestination = getL2GateIDsForRoute(pFromL2Address, pToL2Address);
 	
 			// gate ID list is empty?
 			if((tGateIDsToDestination != null) && (!tGateIDsToDestination.isEmpty())) {
 				// create a route segment which can store a list of Gate IDs
 				RouteSegmentPath tRouteSegmentPath = new RouteSegmentPath();
 				// iterate over all gate IDs in the list
 				for(RoutingServiceLink tLink : tGateIDsToDestination) {
 					if (tLink instanceof L2LogicalLink){
 						L2LogicalLink tLogicalLink = (L2LogicalLink)tLink;
 						
 						if(tLogicalLink.getRoute() != null){
 							// store the route as immediate result
 							return tLogicalLink.getRoute().clone();
 						}else{
 							Logging.warn(this, "getL2RouteBestEffort() found an invalid route from " + pFromL2Address + " to " + pToL2Address + ", knowing the following routing graph nodes");
 							// list known nodes
 							synchronized (mL2RoutingGraph) {
 								Collection<L2Address> tGraphNodes = mL2RoutingGraph.getVertices();
 								int i = 0;
 								for (L2Address tL2Address : tGraphNodes){
 									Logging.warn(this, "     ..[" + i + "]: " + tL2Address);
 									Collection<RoutingServiceLink> tOutLinks = mL2RoutingGraph.getOutEdges(tL2Address);
 									for(RoutingServiceLink tOutLink : tOutLinks){
 										Logging.warn(this, "      ..out link: " + tOutLink + " [" + tOutLink.getClass().getSimpleName() + "]");	
 									}
 									i++;
 								}
 							}
 						}
 					}else{
 						// store the Gate ID in the route segment
 						tRouteSegmentPath.add(tLink.getID());
 					}
 				}
 				
 				// create new route with the list of Gate IDs
 				tResultRoute = new Route(tRouteSegmentPath);
 			}
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "getL2Route() delivers an empty route because source and destination are equal: " + pFromL2Address);
 			}
 			
 			// create a new empty route because we have already reached the destination
 			tResultRoute = new Route();
 		}
 
 		return tResultRoute;
 	}
 	
 	/**
 	 * Returns the L2 route to a given destination, if a given network interface should be used.
 	 * This function uses the base data from layer 2 (FoG forwarding structure).
 	 * 
 	 * @param pDestination the destination of the route
 	 * @param pViaNetworkInterface the network interface via a route is searched
 	 * 
 	 * @return the searched route, "null" if none was found
 	 */
 	public Route getL2RouteViaNetworkInterface(L2Address pDestination, NetworkInterface pViaNetworkInterface)
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ROUTING;
 		
 		Route tResult = null;
 		if(pViaNetworkInterface == null){
 			if (DEBUG){
 				Logging.log(this, "Searching for an L2 based BE route to " + pDestination);
 			}
 
 			// use the default BE route
 			tResult = getL2RouteBestEffort(mHRMController.getNodeL2Address(),  pDestination);
 		}else{
 			Bus tViaLink = (Bus) pViaNetworkInterface.getBus();
 			if (DEBUG){
 				Logging.log(this, "Searching for an L2 route to " + pDestination + " via the link " + tViaLink);
 			}
 			
 			/**
 			 * Fog-specific: find the FN which is responsible for traffic for the given via-network-interface
 			 */
 			L2Address tIntermediaDestination = null;
 			boolean tWithoutException = false; //TODO: rework some software structures to avoid this ugly implementation
 			while(!tWithoutException){
 				try{
 					Set<ForwardingNode> tLocalFoGFNs = mFNToL2AddressMapping.keySet();
 					for(ForwardingNode tLocalFN : tLocalFoGFNs){
 						if(tLocalFN instanceof Multiplexer){
 							Multiplexer tLocalMux = (Multiplexer) tLocalFN;
 		
 							GateIterator iter = tLocalMux.getIterator(DirectDownGate.class);
 							/**
 							 * iterate over all logical links (FoG DirectDownGate instances)
 							 */
 							while(iter.hasNext()) {
 								DirectDownGate tDirectDownGate = (DirectDownGate) iter.next();
 		
 								/**
 								 * A.) get the network interface to the neighbor
 								 */ 
 								Bus tNextNetworkBus = (Bus)tDirectDownGate.getNextNode();//getNetworkInterface();
 								if(tNextNetworkBus != null){
 									if(tNextNetworkBus.equals(tViaLink)){
 										//TODO: improve this ugly solution of the following 3 lines
 										String tDirectDownGatePeerNode = tDirectDownGate.getOwner().toString();
 										String tSearchedPeerNode = (String)pDestination.getDescr(); 
 										boolean tDirectDownGateLeadsToCorrectPeerNode = tSearchedPeerNode.endsWith("@" + tDirectDownGatePeerNode); 
 										
 										
 										/**
 										 * For domains with more than 2 nodes: find the correct FoG DirectDownGate towards the desired peer node
 										 */
 										if(tDirectDownGateLeadsToCorrectPeerNode){
 											if (DEBUG){
 												Logging.log(this, "Found matching logical link " + tDirectDownGate + " for target link: " + tNextNetworkBus + ", ending at: " + tDirectDownGate.getOwner() + "[" + tDirectDownGate.getOwner().getClass().getSimpleName() + "]");
 											}
 											tIntermediaDestination = getL2AddressFor(tLocalFN);
 											if(tIntermediaDestination != null){
 												RouteSegmentPath tGateList = (RouteSegmentPath) getL2RouteBestEffort(mHRMController.getNodeL2Address(), tIntermediaDestination).getFirst();
 												if (DEBUG){
 													Logging.log(this, "  ..route to intermediate FN " + tIntermediaDestination + ": " + tGateList);
 												}
 												tGateList.add(tDirectDownGate.getGateID());
 												tResult = new Route(tGateList);
 												tResult.add(new RouteSegmentAddress(pDestination));
 												if (DEBUG){
 													Logging.log(this, "  ..resulting route: " + tResult);
 												}
 											}
 										}else{
 											if(DEBUG){
 												Logging.log(this, "  ..ignoring DirectDownGate leading to wrong peer node, gate=: " + tDirectDownGate);
 											}
 										}
 									}
 								}
 							}
 						}
 						tWithoutException = true;
 					}
 				}catch(ConcurrentModificationException tExc){
 					// FoG has manipulated the topology data and called the HRS for updating the L2 routing graph
 					continue;
 				}
 			}
 		}		
 
 		/**
 		 * Fall-back to BE routing 
 		 */
 		if (tResult == null){
 			tResult = getL2RouteBestEffort(mHRMController.getNodeL2Address(), pDestination);
 			if (DEBUG){
 				Logging.log(this, "  ..resulting (fall-back) BE route: " + tResult);
 			}
 		}
 		
 		return tResult;
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
 		return getRoute(getCentralFN(), pDestination, pRequirements, pRequester);
 	}
 
 	/**
 	 * Checks if a given HRMID belongs to a direct neighbor
 	 * 
 	 * @param pAddress the HRMID of a possible direct neighbor
 	 * 
 	 * @return true or false
 	 */
 	public boolean isNeighbor(HRMID pAddress)
 	{
 		boolean tResult = false;
 		
 		synchronized (mDirectNeighborAddresses) {
 			if(mDirectNeighborAddresses.contains(pAddress)){
 				tResult = true;
 			}
 		}
 		
 		return tResult;
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
 	@SuppressWarnings("unused")
 	@Override
 	public Route getRoute(ForwardingNode pSource, Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{		
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ROUTING;
 		Route tRoutingResult = null;
 		L2Address tDestinationL2Address = null;
 		L2Address tSourceL2Address = null;
 
 //		if(pDestination instanceof L2Address){
 //			DEBUG = true;
 //		}
 		
 		/**
 		 * Make sure that the HRMController is already started if the routing should lead to an application.
 		 * It can occur that a new node is created and the previous node already wants to connect to this node before the local HRMController is completely started.
 		 * Therefore, if a routing to an application is request, the HRS has to wait until the local HRMController is started completely.
 		 */
 		if(pDestination instanceof SimpleName){
 			Logging.log(this, "\n\n\n########################### Routing to application requested, to: " + pDestination);
 			// wait for the HRMController
 			getHRMController();
 		}
 		
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
 		 * Determine the desired QoS values
 		 * HINT: We use the standard FoG mechanism to determine the QoS values in order to be more compatible to FoG applications.
 		 * 		 However, the same QoS values should be stored within the HRMRoutingProperty.
 		 */
 		int tDesiredDelay = pRequirements.getDesiredDelay();
 		int tDesiredDataRate = pRequirements.getDesiredDataRate();
 		Description tNonFunctionalDescription = pRequirements.getNonFunctional();
 
 		/**
 		 * Debug output about process start
 		 */
 		// debug output
 		if (DEBUG){
 			if(pRequirements == null) {
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
 
 		/************************************************************************
 		 * HRM based routing to an HRMID
 		 ************************************************************************/
 		if (pDestination instanceof HRMID){
 			HRMID tDestHRMID = (HRMID)pDestination;
 			
 			if (DEBUG){
 				Logging.log(this, "      ..HRM based routing to " + tDestHRMID);
 			}
 
 			/*************************************
 			 * CHECK TTR
 			 ************************************/
 			// search for the ProbeRoutingProperty property
 			HRMRoutingProperty tHRMRoutingProp = (HRMRoutingProperty) pRequirements.get(HRMRoutingProperty.class);
 			if(tHRMRoutingProp != null){
 				if(!tHRMRoutingProp.isTTROkay()){
 					throw new RoutingException("TTR exceeded for a route to: " + pDestination + ", requirements=" + pRequirements);	
 				}
 			}
 				
 			/*************************************
 			 * CHECK NEIGHBORHOD
 			 * Check if the destination is a direct neighbor
 			 ************************************/
 			boolean tDestinationIsDirectNeighbor = isNeighbor(tDestHRMID);
 			if(tDestinationIsDirectNeighbor){
 				if (DEBUG){
 					Logging.log(this, "      .." + tDestHRMID + " is an address of a direct neighbor node, neighbors: " + mDirectNeighborAddresses);
 				}
 			}
 
 			/*************************************
 			 * FIND NEXT HRMID
 			 * Determine the next hop of the desired route
 			 *************************************/
 			HRMID tNextHopHRMID = null;
 			HRMID tLocalSourceHRMID = null;
 			HRMID tLastHopHRMID = null;
 			L2Address tLastHopL2Address = null;
 			if(tHRMRoutingProp != null){
 				tLastHopHRMID = tHRMRoutingProp.getLastHopHRMID();
 				tLastHopL2Address = tHRMRoutingProp.getLastHopL2Address();
 			}
 			boolean tDestHRMIDIsLocalHRMID = false;
 			RoutingEntry tRoutingEntryNextHop = getBestRoutingEntryNextHop(tDestHRMID, tDesiredDelay, tDesiredDataRate, tLastHopHRMID, tLastHopL2Address);
 			if(tRoutingEntryNextHop != null){
 				if(!tRoutingEntryNextHop.isLocalLoop()){
 					// derive the next hop HRMID
 					tNextHopHRMID = tRoutingEntryNextHop.getNextHop();
 	
 					// derive the HRMID of this node - use the source from the found routing entry
 					tLocalSourceHRMID = tRoutingEntryNextHop.getSource();
 				}
 				
 				// mark HRMID as local
 				tDestHRMIDIsLocalHRMID = tRoutingEntryNextHop.isLocalLoop(); 
 			}
 			/**
 			 * Increase the HOP COUNT (for TTR update)
 			 */
 			if(tHRMRoutingProp != null){
 				tHRMRoutingProp.incHopCount(tLocalSourceHRMID, mHRMController.getNodeL2Address());
 			}	
 
 			/**
 			 * Record the HRM based route
 			 */
 			if(tNextHopHRMID != null){
 				recordHRMRoute(pRequirements, tLocalSourceHRMID, tNextHopHRMID, (tDestHRMIDIsLocalHRMID ? 0 : 1));	
 				
 				/**
 				 * Show debug output
 				 */
 				if (DEBUG){
 					Logging.log(this, "      ..NEXT HOP: " + tNextHopHRMID);
 					if(tLocalSourceHRMID != null){
 						Logging.log(this, "      ..VIA: " + tLocalSourceHRMID);
 					}
 				}
 			}else{
 				if (DEBUG){
 					Logging.trace(this, "      ..haven't found next hop (HRMID) for destination: " + tDestHRMID);
 				}
 			}
 			
 			/*************************************
 			 * FIND NEXT L2 ADDRESS
 			 * Determine the L2 specific gates towards the neighbor node
 			 *************************************/
 			L2Address tNextHopL2Address = null;
 			if (tNextHopHRMID != null){
 				tNextHopL2Address = getL2AddressFor(tNextHopHRMID);
 			}else{
 				if(!tDestHRMIDIsLocalHRMID){
 					Logging.log(this, "getRoute() wasn't able to determine the HRMID of the next hop in order to route towards " + tDestHRMID.toString());
 				}
 			}
 
 			/*************************************
 			 * GET ROUTE to next L2 address
 			 *************************************/
 			if (tNextHopL2Address != null){
 				Route tL2RoutingResult = null;
 
 				if (DEBUG){
 					Logging.log(this, "      ..NEXT HOP(L2ADDRESS): " + tNextHopL2Address);
 				}
 				
 				if (tNextHopL2Address != null){
 					/**
 					 * Get the stored explicit L2 route to the determined next HRMID
 					 */
 					tL2RoutingResult = getL2RouteExplicitMapping(tNextHopHRMID);
 					if (DEBUG){
 						Logging.log(this, "      ..explicit L2 ROUTE TO NEXT HOP: " + tL2RoutingResult);
 					}
 
 					/**
 					 * Fall back to BE based L2 routing
 					 */
 					if(tL2RoutingResult == null){
 						// get BE route to the L2 neighbor
 						tL2RoutingResult = getL2RouteBestEffort(getCentralFNL2Address(), tNextHopL2Address);
 					}
 				}
 				
 				if (DEBUG){
 					Logging.log(this, "      ..ROUTE TO NEXT HOP: " + tL2RoutingResult);
 				}
 
 				/**
 				 * Search for a DirectDownGate:
 				 * 		+ A.) derive the NEXT BUS -> use it later to record the gotten QoS values
 				 * 		+ B.) hard QoS reservations -> FoG-specific: trigger creation of dedicated QoS gate
 				 */
 				RouteSegmentPath tRoutingResultFirstGateListWithoutDirectDownGates = new RouteSegmentPath();
 				RouteSegmentMissingPart tQoSReservation = null;
 				if (tL2RoutingResult != null){
 					Bus tNextNetworkBus = null;
 					long tBusMinAdditionalDelay = RoutingEntry.NO_DELAY;
 					long tBusMaxAvailableDataRate = RoutingEntry.INFINITE_DATARATE;
 
 					if(!tL2RoutingResult.isEmpty()){
 						Multiplexer tCurrentMultiplexer = getCentralFN();
 						
 						/**
 						 * get the first part of the routing result: [[gateID list], [an L2 address]] => [gateID list]
 						 */
 						RouteSegmentPath tRoutingResultOriginalFirstPart = (RouteSegmentPath)tL2RoutingResult.getFirst().clone();
 						if (DEBUG){
 							Logging.log(this, "Temporary routing result: " + tL2RoutingResult);
 							for(RouteSegment tRoutingResultPart : tL2RoutingResult){
 								Logging.log(this, "   ..[" + tRoutingResultPart.getClass().getSimpleName() + "]: " + tRoutingResultPart);
 							}
 							Logging.log(this, "Temporary routing result - first part (gate list): " + tRoutingResultOriginalFirstPart);
 						}
 						
 						/**
 						 * Search for DedicatedQoSReservationProperty property
 						 */
 						DedicatedQoSReservationProperty tDedicatedQoSReservationProperty = (DedicatedQoSReservationProperty) pRequirements.get(DedicatedQoSReservationProperty.class);
 						boolean tUseHardQoSReservations = (tDedicatedQoSReservationProperty != null);
 						if (DEBUG){
 							if(tDedicatedQoSReservationProperty != null){
 								Logging.log(this, "Found DedicatedQoSReservationProperty property: " + tDedicatedQoSReservationProperty);
 							}else{
 								Logging.log(this, "Have not found DedicatedQoSReservationProperty property");
 							}
 						}
 						
 						/**
 						 * Iterate over all given gateIDs in [gateID list] and search for the DirectDownGate and the Bus
 						 */
 						while(!tRoutingResultOriginalFirstPart.isEmpty()) {
 							GateID tGateID = tRoutingResultOriginalFirstPart.removeFirst();
 							if (DEBUG){
 								Logging.log(this, "  ..found gate: " + tGateID);
 							}
 							AbstractGate tGate = tCurrentMultiplexer.getGate(tGateID);
 							Name tNextName = null;
 							
 							if(tGate != null) {
 								
 								boolean tHasDescribedMissingQoSReservation = false;
 								if(tGate instanceof DirectDownGate){
 									// get the direct down gate
 									DirectDownGate tDirectDownGate = (DirectDownGate)tGate;
 									
 									/**
 									 * A.) get the network interface to the neighbor
 									 */ 
 									tNextNetworkBus = (Bus)tDirectDownGate.getNextNode();//getNetworkInterface();
 									tBusMinAdditionalDelay = tNextNetworkBus.getDelayMSec();
 									tBusMaxAvailableDataRate = tNextNetworkBus.getAvailableDataRate();
 
 									/**
 									 * B.) describe a missing QoS reservation, which is later is used in order to trigger QoS RESERVATION within the FoG transfer service
 									 */
 									if((HRMConfig.QoS.QOS_RESERVATIONS) && (tUseHardQoSReservations)){
 										AbstractGate tParallelGate = tGate;
 										if (DEBUG){
 											Logging.log(this, "Needing QoS gate parallel to: " + tParallelGate + " with non-functional requirements: " +tNonFunctionalDescription);
 											Logging.log(this, "  ..remote node of next link is: " + tDirectDownGate.getRemoteDestinationName());
 										}
 											
 										/**
 										 * Check if the desired data rate can be fulfilled -> otherwise use the best possible value
 										 */
 										int tDataRate = tNonFunctionalDescription.getDesiredDataRate();
 										if(tDataRate > 0){
 											if(tDataRate > tBusMaxAvailableDataRate){
 												tDataRate = (int) tBusMaxAvailableDataRate;
 												tNonFunctionalDescription.set(new DatarateProperty(tDataRate, Limit.MIN));
 											}
 										}
 										
 										/**
 										 * Check if the desired delay can be fulfilled -> otherwise drop this property
 										 */
 										int tDelay = tNonFunctionalDescription.getDesiredDelay();
 										if(tDelay > 0){
 											if(tDelay < tBusMinAdditionalDelay){
 												tNonFunctionalDescription.set(new DelayProperty(0, Limit.MIN));
 											}
 										}
 
 										
 										Description tCapabilities = tParallelGate.getDescription();
 										if (DEBUG){
 											Logging.log(this, "  ..original gate has caps.: " + tCapabilities);
 										}
 										Description tNeededReservationRequirements = null;
 										
 										if(tCapabilities != null) {
 											boolean tRepeat = false;
 											do{
 												tRepeat = false;
 												try {
 													tNeededReservationRequirements = tCapabilities.deriveRequirements(tNonFunctionalDescription);
 												}
 												catch(PropertyException tExc) {
 													// reset data rate
 													tNonFunctionalDescription.set(new DatarateProperty(0, Limit.MIN));
 													
 													// reset delay
 													tNonFunctionalDescription.set(new DelayProperty(0, Limit.MIN));
 													
 													// repeat
 													tRepeat = true;
 													Logging.log(this, "Repeat deriveRequirements()");
 													//avoid: throw new RoutingException(this, "Requirements " + tNonFunctionalDescription +" can not be fulfilled.", tExc);
 												}
 											}while(tRepeat);
 										} else {
 											tNeededReservationRequirements = null;
 										}
 				
 										// mark the gate as HRM based QoS reservation
 										tNeededReservationRequirements.set(new DedicatedQoSReservationProperty(tDedicatedQoSReservationProperty.isBidirectional()));
 										
 										// describe the needed QoS reservation					
 										if (DEBUG){
 											Logging.log(this, "Creating QoS reservation with requirements: " + tNeededReservationRequirements);
 										}
 										tQoSReservation = new RouteSegmentMissingPart(tNeededReservationRequirements, tParallelGate, pRequester);
 										
 										// mark as described QoS reservation
 										tHasDescribedMissingQoSReservation = true;
 									}
 								}
 								
 								/**
 								 * Add the found gateID to the final routing result
 								 */
 								if(!tHasDescribedMissingQoSReservation){
 									tRoutingResultFirstGateListWithoutDirectDownGates.add(tGateID);
 								}
 								
 								/**
 								 * jump to next forwarding node
 								 */
 								if(tGate.getNextNode() instanceof Multiplexer){
 									tCurrentMultiplexer = (Multiplexer)tGate.getNextNode();
 								}else{
 									if (DEBUG){
 										Logging.log(this, " ..reached the FoG Bus");
 									}
 									// we reached the Bus
 									break;
 								}
 							} else {
 								if (DEBUG){
 									Logging.log(this, " ..no further data");
 								}
 								// no further data
 								break;
 							}
 						}
 					}
 					
 					
 					/**
 					 * CREATE resulting routing result
 					 */
 					if(tQoSReservation != null){
 						tRoutingResult = new Route();
 						
 						/**
 						 * ENCODE the first gate list (unlimited QoS)
 						 */
 						if(!tRoutingResultFirstGateListWithoutDirectDownGates.isEmpty()){
 							if (DEBUG){
 								Logging.log(this, "   ..encoding first gate list: " + tRoutingResultFirstGateListWithoutDirectDownGates);
 							}
 							tRoutingResult.add(tRoutingResultFirstGateListWithoutDirectDownGates);
 						}
 	
 						/**
 						 * ENCODE hard QoS reservation (limited QoS)
 						 */
 						if(!tRoutingResultFirstGateListWithoutDirectDownGates.isEmpty()){
 							if (DEBUG){
 								Logging.log(this, "   ..encoding hard QoS reservation: " + tQoSReservation);
 							}
 							tRoutingResult.add(tQoSReservation);
 						}						
 
 						/**
 						 * ENCODE the remaining parts of the original routing result
 						 */
 						if (DEBUG){
 							Logging.log(this, "   ..encoding additional destination description");
 						}
 						int i = 0;
 						for(RouteSegment tRoutingResultPart : tL2RoutingResult){
 							// ignore the first gate list
 							if(i > 0){
 								if (DEBUG){
 									Logging.log(this, "     ..encoding [" + tRoutingResultPart.getClass().getSimpleName() + "]: " + tRoutingResultPart);
 								}
 								tRoutingResult.add(tRoutingResultPart);
 							}
 							i++;
 						}
 					}else{
 						/**
 						 * Fall-back
 						 */
 						Logging.warn(this, "   ..invalid hard QoS reservation: " + tQoSReservation);
 						Logging.warn(this, "   ..using previously determined BE route: " + tL2RoutingResult);
 						tRoutingResult = tL2RoutingResult;
 					}
 
 					/**
 					 * ENCODE the destination HRMID
 					 */
 					// do we route to a direct neighbor?
 					if(!tDestinationIsDirectNeighbor){
 						// do we already reached the destination?
 						if(!tL2RoutingResult.isEmpty()){
 							if (DEBUG){
 								Logging.log(this, "   ..encoding original destination: " + tDestHRMID);
 							}
 							tRoutingResult.addLast(new RouteSegmentAddress(tDestHRMID));
 						}
 					}
 
 					/**
 					 * ENCODE the original requirements
 					 */
 					// do we already reached the destination?
 					if(!tL2RoutingResult.isEmpty()){
 						if (DEBUG){
 							Logging.log(this, "   ..encoding original requirements: " + pRequirements);
 						}
 						tRoutingResult.addLast(new RouteSegmentDescription(pRequirements));
 					}
 
 					/**
 					 * Get the QOS VALUEs of the next network BUS and record it
 					 */
 					if(tNextNetworkBus != null){
 						double tUtilization = 0.0;
 						
 						if (DEBUG){
 							Logging.log(this, "NEXT NETWORK BUS IS: " + tNextNetworkBus);
 							Logging.log(this, "   ..min. additional delay: " + tBusMinAdditionalDelay + " ms");
 							Logging.log(this, "   ..max. available data rate: " + tBusMaxAvailableDataRate + " kbit/s");
 							Logging.log(this, "   ..utilization: " + " %");			
 						}
 						
 						recordHRMRouteQoS(pRequirements, tBusMinAdditionalDelay, tBusMaxAvailableDataRate, tUtilization);	
 					}
 				}
 			}else{
 				/**
 				 * ERROR MESSAGE
 				 */
 				if((tNextHopHRMID != null) && (!tDestHRMIDIsLocalHRMID)){
 					Logging.err(this, "getRoute() wasn't able to determine the L2 address of the next hop " + tNextHopHRMID + " in the following mapping: ");
 
 					synchronized (mHRMIDToL2AddressMapping) {
 						if(mHRMIDToL2AddressMapping.size() > 0){
 							for (HRMID tHRMID : mHRMIDToL2AddressMapping.keySet()){
 								Logging.log(this, "       ..mapping " + tHRMID + " to " + mHRMIDToL2AddressMapping.get(tHRMID));
 							}
 						}else{
 							Logging.log(this, "   ..no mapping entries found");
 						}
 					}
 				}
 			}
 
 			/***************************************************************************
 			 * IF ENCODE the destination application as destination if possible,
 			 *            use the following L2 based routing
 			 ***************************************************************************/
 			if(tNextHopHRMID == null){
 				if(tDestHRMIDIsLocalHRMID){
 					/**
 					 * Check if a destination application is encoded in the requirements and use it for routing
 					 */
 					pDestination = getDestinationApp(pRequirements);
 					if(pDestination == null){
 						Logging.err(this, "getRoute() hasn't found the destination application for a route to " + pDestination + " with requirements: " + pRequirements);
 					}
 				}else{
 					Logging.err(this, "getRoute() wasn't able to determine an HRM based route to " + pDestination + " with requirements: " + pRequirements);
 				}
 			}else{
 				/**
 				 * show result
 				 */
 				if(tRoutingResult != null){
 					if (DEBUG){
 						Logging.log(this, "      ..RESULT(getRoute() to HRMID): " + tRoutingResult);
 					}
 				}
 			}
 		}
 
 		/***********************************************************************
 		 * L2 based routing to a FoG name
 		 ***********************************************************************/
 		if(tRoutingResult == null){
 			if (DEBUG){
 				Logging.log(this, "      ..L2 based routing to " + pDestination);
 			}
 			
 			/**
 			 * Determine the L2 address of the destination
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
 
 				if (DEBUG){
 					Logging.log(this, "      ..found destination L2Address: " + tDestinationL2Address);
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
 					 * Get a route from the source to the destination based on their L2 addresses
 					 */
 					tRoutingResult = getL2RouteBestEffort(tSourceL2Address, tDestinationL2Address);
 					if(DEBUG){
 						Logging.log(this, "Determined the L2 route to " + tDestinationL2Address + " as: " + tRoutingResult);
 					}
 					
 					if (tRoutingResult != null){
 						encodeDestinationApplication(tRoutingResult, pRequirements);
 					}else{
 						// no route found
 						if(HRMConfig.Measurement.VALIDATE_RESULTS_EXTENSIVE){
 							Logging.warn(this, "getRoute() couldn't determine an L2 route from " + tSourceL2Address + " to " + tDestinationL2Address + ", knowing the following routing graph nodes");
 							// list known nodes
 							synchronized (mL2RoutingGraph) {
 								Collection<L2Address> tGraphNodes = mL2RoutingGraph.getVertices();
 								int i = 0;
 								for (L2Address tL2Address : tGraphNodes){
 									Logging.warn(this, "     ..[" + i + "]: " + tL2Address);
 									Collection<RoutingServiceLink> tOutLinks = mL2RoutingGraph.getOutEdges(tL2Address);
 									for(RoutingServiceLink tOutLink : tOutLinks){
 										Logging.warn(this, "      ..out link: " + tOutLink + " [" + tOutLink.getClass().getSimpleName() + "]");	
 									}
 									i++;
 								}
 							}
 						}
 					}
 					
 					if (DEBUG){
 						Logging.log(this, "      ..RESULT(getRoute() to " + pDestination + "): " + tRoutingResult);
 					}
 				}else{
 					Logging.err(this, "getRoute() wasn't able to determine the L2 address of the source " + pSource);
 				}
 			}else{
 				Logging.err(this, "getRoute() wasn't able to determine the L2 address of the destination " + pDestination);
 			}
 		}
 
 		/**
 		 * CATCH ROUTING ERROR
 		 */
 		if(tRoutingResult == null){
 			// no route found
 			if (DEBUG){
 				Logging.warn(this, "getRoute() couldn't determine a route from " + pSource + " to " + pDestination + ", knowing the following routing graph");
 				
 				// list known topology
 				synchronized (mL2RoutingGraph) {
 					Collection<L2Address> tGraphNodes = mL2RoutingGraph.getVertices();
 					int i = 0;
 					for (L2Address tL2Address : tGraphNodes){
 						Logging.warn(this, "     ..node[" + i + "]: " + tL2Address);
 						i++;
 					}
 					Collection<RoutingServiceLink> tGraphLinks = mL2RoutingGraph.getEdges();
 					i = 0;
 					for (RoutingServiceLink tLink : tGraphLinks){
 						Logging.warn(this, "     ..gate[" + i + "]: " + tLink.getID());
 						i++;
 					}
 				}
 				//throw new RuntimeException(this + "::getRoute() failed");
 			}
 		}
 		
 		// return immediately
 		return tRoutingResult;
 	}
 	
 	/**
 	 * Returns the best routing entry towards a given destination, aware of given QoS parameters
 	 * 
 	 * @param pDestination the HRMID of the destination
 	 * @param pDesiredDelay the desired max. delay
 	 * @param pDesiredDataRate the desired min. data rate reservation
 	 * @param pLastHopHRMID the HRMID of the last hop
 	 * @param pLastHopL2Address the L2Address of the last hop
 	 * 
 	 * @return the found routing entry
 	 */
 	public RoutingEntry getBestRoutingEntryNextHop(HRMID pDestination, long pDesiredDelay, long pDesiredDataRate, HRMID pLastHopHRMID, L2Address pLastHopL2Address)
 	{
 		RoutingEntry tResult = null;
 	
 		synchronized (mRoutingTable) {
 			tResult = mRoutingTable.getBestEntry(pDestination, pDesiredDelay, pDesiredDataRate, pLastHopHRMID, pLastHopL2Address);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Records the current HOP within an possibly existing ProbeRoutingProperty property.
 	 * 
 	 * @param pRequirements the requirements of the getRoute() request
 	 * @param pHopHRMID the HRMID of the hop, which should be recorded
 	 * @param pHopCount
 	 */
 	private void recordHRMRoute(Description pRequirements, HRMID pSourceHRMID, HRMID pNextHopHRMID, int pHopCount)
 	{
 		if (HRMConfig.Routing.RECORD_ROUTE_FOR_PROBES){
 			// check if we have valid requirements
 			if (pRequirements != null){
 				// search for the ProbeRoutingProperty property
 				HRMRoutingProperty tHRMRoutingProp = (HRMRoutingProperty) pRequirements.get(HRMRoutingProperty.class);
 				if(tHRMRoutingProp != null) {
 					HRMID tLastRecordedHopHRMID = tHRMRoutingProp.getLastRecordedHop();
 					
 					/**
 					 * Check if last and current hop have the same HRMID
 					 */
 					boolean tDuplicate = false;
 					if ((tLastRecordedHopHRMID != null) && (tLastRecordedHopHRMID.equals(pNextHopHRMID))){
 						tDuplicate = true;
 					}
 					
 					/**
 					 * Store the HRMID of the current node
 					 */
 					if(pSourceHRMID != null){
 						tHRMRoutingProp.addRecordedHop(pSourceHRMID);
 					}
 					
 					/**
 					 * Store the HRMID of the next hop
 					 */
 					if (!tDuplicate){
 						// don't store the same entry two times
 						if(!pNextHopHRMID.equals(pSourceHRMID)){
 							// store the HRMID
 							tHRMRoutingProp.addRecordedHop(pNextHopHRMID);
 						}
 					}else{
 						// we have the same hop like last time
 					}
 				}else{
 					Logging.warn(this, "Cannot record HRM Route (next=" + pNextHopHRMID + " via " + pSourceHRMID + ") because the needed property wasn't found within the requirements");
 				}
 			}else{
 				Logging.warn(this, "Cannot record HRM Route (next=" + pNextHopHRMID + " via " + pSourceHRMID + ") because no requirements were found");
 			}
 		}
 	}
 
 	/**
 	 * Records the gotten QoS values
 	 * 
 	 * @param pRequirements the requirements of the getRoute() request
 	 * @param pMinAdditionalDelay the additional delay for the current next link
 	 * @param pMaxAvailableDataRate the max. available data rate along the current next link
 	 * @param pGottenUtilization the gotten utilization for the current next link
 	 */
 	private void recordHRMRouteQoS(Description pRequirements, long pMinAdditionalDelay, long pMaxAvailableDataRate, double pGottenUtilization)
 	{
 		/**
 		 * Additional check of the parameters
 		 */
 		if(pMaxAvailableDataRate <0){
 			pMaxAvailableDataRate = 0;
 		}
 		if(pMinAdditionalDelay < 0){
 			pMinAdditionalDelay = 0;
 		}
 		
 		/**
 		 * Do the actual recording
 		 */
 		if (HRMConfig.Routing.RECORD_ROUTE_FOR_PROBES){
 			// check if we have valid requirements
 			if (pRequirements != null){
 				// search for the ProbeRoutingProperty property
 				HRMRoutingProperty tPropProbeRouting = (HRMRoutingProperty) pRequirements.get(HRMRoutingProperty.class);
 				if(tPropProbeRouting != null) {
 					/**
 					 * record the minimum DELAY along the route
 					 */
 					tPropProbeRouting.recordAdditionalDelay(pMinAdditionalDelay);
 					
 					/**
 					 * record the DATA RATE along the route
 					 */
 					tPropProbeRouting.recordDataRate(pMaxAvailableDataRate);
 				}else{
 					Logging.warn(this, "Cannot record HRM Route QoS because the needed property wasn't found within the requirements");
 				}
 			}else{
 				Logging.warn(this, "Cannot record HRM Route QoS because no requirements were found");
 			}
 		}
 	}
 	
 	/**
 	 * Determines the destination application from a requirements description
 	 * 
 	 * @param pRequirements the requirements
 	 * 
 	 * @return the app. name
 	 */
 	private Name getDestinationApp(Description pRequirements)
 	{
 		Name tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "getDestinationApp() is deriving the destination app from: " + pRequirements);
 		}
 		
 		/**
 		 * Check if a destination application is encoded in the requirements
 		 */
 		DestinationApplicationProperty tPropDestApp = (DestinationApplicationProperty) pRequirements.get(DestinationApplicationProperty.class);
 		if(tPropDestApp != null) {
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "    ..found destination application property: " + tPropDestApp);
 			}
 			
 			// remove the found property from the requirements
 			pRequirements.remove(tPropDestApp);
 		
 			/**
 			 * Add the destination application as FoG name to the route
 			 */
 			if(tPropDestApp.getAppName() != null) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..encoding as getRoute() destination the destination  appl. name: " + tPropDestApp.getAppName());
 				}
 				tResult = tPropDestApp.getAppName();
 			} else {
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..encoding as getRoute() destination the destination  appl. name space: " + tPropDestApp.getAppNamespace());
 				}
 				tResult = new SimpleName(tPropDestApp.getAppNamespace());
 			}
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "    ..found destination app.: " + tResult);
 			}
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "getDestinationApp() wasn't able to derive the destination app. from: " + pRequirements);
 			}
 		}
 		
 		return tResult;
 	}
 
 
 	/**
 	 * Encodes the destination application in the given route. The destination application is 
 	 * derived from the data of the given requirements.
 	 * This function should be used if the destination node was reached.
 	 * 
 	 * @param pRoute the current route
 	 * @param pRequirements the requirements, which can include a description for the destination application
 	 */
 	private void encodeDestinationApplication(Route pRoute, Description pRequirements)
 	{
 		// do we have requirements?
 		if(pRequirements != null) {
 			/**
 			 * Check if a destination application is encoded in the requirements
 			 */
 			DestinationApplicationProperty tPropDestApp = (DestinationApplicationProperty) pRequirements.get(DestinationApplicationProperty.class);
 			if(tPropDestApp != null) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..found destination application property: " + tPropDestApp);
 				}
 				
 				// remove the found property from the requirements
 				pRequirements.remove(tPropDestApp);
 			
 				/**
 				 * Add the destination application to the route
 				 */
 				if(tPropDestApp.getAppName() != null) {
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "    ..encoding in route the destination  appl. name: " + tPropDestApp.getAppName());
 					}
 					pRoute.add(new RouteSegmentAddress(tPropDestApp.getAppName()));
 				} else {
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "    ..encoding in route the destination  appl. name space: " + tPropDestApp.getAppNamespace());
 					}
 					pRoute.add(new RouteSegmentAddress(new SimpleName(tPropDestApp.getAppNamespace())));
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..no destination application property found");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determines all links which are outgoing from a defined FN
 		
 	 * @param pFNName the name of the FN from which the outgoing links should be enumerated
 	 * @return the list of outgoing links
 	 */
 	public Collection<RoutingServiceLink> getOutgoingLinks(L2Address pFNName)
 	{
 		Collection<RoutingServiceLink> tResult = null;
 
 		synchronized (mL2RoutingGraph) {
 			tResult = mL2RoutingGraph.getOutEdges(pFNName);
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
 
 	/**
 	 * Returns the local L2Address based routing graph (for GUI only)
 	 * 
 	 * @return the routing graph
 	 */
 	public RoutableGraph<L2Address, RoutingServiceLink> getL2RGForGraphViewer()
 	{
 		return mL2RoutingGraph;
 	}
 
 	/**
 	 * Returns the central FN of this node
 	 *  
 	 * @return the central FN
 	 */
 	public Multiplexer getCentralFN()
 	{
 		Multiplexer tResult = null;
 		
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) mNode.getLayer(FoGEntity.class);
 		
 		// get the central FN of this node
 		if(tFoGLayer != null){
 			tResult = tFoGLayer.getCentralFN();
 		}
 		
 		return tResult;
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
 
 	@Override
 	public LinkedList<Name> getIntermediateFNs(ForwardingNode pSource,	Route pRoute, boolean pOnlyDestination)
 	{
 		return null;
 	}
 
 	
 
 	/**
 	 * Returns the namespace which is handled by our HRM routing service.
 	 * This function is inherited from RoutingService. 
 	 */
 	@Override
 	public Namespace getNamespace()
 	{
 		return HRMName.NAMESPACE_HRM;
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
