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
 
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.Service;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.facade.IServerCallback;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.ClusterParticipationProperty.NestedParticipation;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.fog.util.Tuple;
 
 /**
  * This is the main HRM controller. It provides functions that are necessary to build up the hierarchical structure - every node contains such an object
  */
 public class HRMController extends Application implements IServerCallback
 {
 	private boolean HRM_CONTROLLER_DEBUGGING = false;
 	
 	private SimpleName mName = null;
 	/**
 	 * Reference to physical node.
 	 */
 	private Node mPhysicalNode; //TV
 	private HierarchicalRoutingService mHRS = null;
 	private RoutableClusterGraph<IRoutableClusterGraphTargetName, RoutableClusterGraphLink> mRoutableClusterGraph = new RoutableClusterGraph<IRoutableClusterGraphTargetName, RoutableClusterGraphLink>();
 	private boolean mIsEdgeRouter;
 	private HashMap<Integer, ICluster> mLevelToCluster = new HashMap<Integer, ICluster>();
 	private HashMap<ICluster, Cluster> mIntermediateMapping = new HashMap<ICluster, Cluster>();
 	private HashMap<Integer, CoordinatorCEPMultiplexer> mMuxOnLevel;
 	private LinkedList<LinkedList<Coordinator>> mRegisteredCoordinators;
 	private LinkedList<HRMSignature> mApprovedSignatures;
 	private HRMIdentity mIdentity;
 	private LinkedList<HRMID> mIdentifications = new LinkedList<HRMID>();
 	
 	/**
 	 * The global name space which is used to identify the HRM instances on neighbor nodes. //TV
 	 */
 	private final static Namespace ROUTING_NAMESPACE = new Namespace("routing");
 	
 	private int mConnectionCounter = 0;
 	
 	/**
 	 * @param pNode the node on which this controller was started
 	 * @param pHRS is the hierarchical routing service that should be used
 	 */
 	public HRMController(Node pNode, HierarchicalRoutingService pHRS)
 	{
 		super(pNode.getHost(), null, pNode.getIdentity());
 		mName = new SimpleName(ROUTING_NAMESPACE, null);
 		mPhysicalNode = pNode;
 		Logging.log(this, "created");
 		Binding serverSocket=null;
 		try {
 			serverSocket = getHost().bind(null, mName, getDescription(), getIdentity());
 			Service service = new Service(false, this);
 			service.start(serverSocket);
 		} catch (NetworkException tExc) {
 			Logging.err(this, "Unable to bind to hosts application interface", tExc);
 		}
 		mHRS = pHRS;
 		mApprovedSignatures = new LinkedList<HRMSignature>();		
 		
 		// set the Bully priority 
 		BullyPriority.configureNode(pNode);
 	}
 
 	/**
 	 * This method is inherited from the class application and is called by the ServerFN object once a new connection setup request is required to be established.
 	 */
 	@Override
 	public void newConnection(Connection pConnection)
 	{
 		Logging.log(this, "NEW CONNECTION " + pConnection);
 
 		//long tClusterID = 0;
 		CoordinatorSession tConnectionSession = null;
 		
 		ClusterParticipationProperty tJoin = null;
 		Description tRequirements = pConnection.getRequirements();
 		for(Property tProperty : tRequirements) {
 			if(tProperty instanceof ClusterParticipationProperty) {
 				tJoin = (ClusterParticipationProperty)tProperty;
 			}
 		}
 		
 		try {
 			tJoin = (ClusterParticipationProperty) tRequirements.get(ClusterParticipationProperty.class);
 		} catch (ClassCastException tExc) {
 			Logging.err(this, "Unable to find the information which cluster should be attached.", tExc);
 		}
 					
 		for(NestedParticipation tParticipate : tJoin.getNestedParticipations()) {
 			CoordinatorCEPChannel tCEP = null;
 			boolean tClusterFound = false;
 			ICluster tFoundCluster = null;
 			for(Cluster tCluster : getRoutingTargetClusters())
 			{
 				ClusterName tJoinClusterName = new ClusterName(tJoin.getTargetToken(), tJoin.getTargetClusterID(), tJoin.getHierarchyLevel());
 				ClusterName tJoinClusterNameTok0 = new ClusterName(0, tJoin.getTargetClusterID(), tJoin.getHierarchyLevel());
 				
 				if(tCluster.equals(tJoinClusterNameTok0) || tJoin.getTargetToken() != 0 && tCluster.equals(tJoinClusterName))	{
 					if(tConnectionSession == null) {
 						tConnectionSession = new CoordinatorSession(this, true, tJoin.getHierarchyLevel(), tCluster.getMultiplexer());
 					}
 					
 					tCEP = new CoordinatorCEPChannel(this, tCluster);
 					((Cluster)tCluster).getMultiplexer().addMultiplexedConnection(tCEP, tConnectionSession);
 					if(tJoin.getHierarchyLevel().isHigherLevel()) {
 						((Cluster)tCluster).getMultiplexer().registerDemultiplex(tParticipate.getSourceClusterID(), tJoin.getTargetClusterID(), tCEP);
 					} else {
 						if(tParticipate.isInterASCluster()) {
 							tCEP.setEdgeCEP();
 							mIsEdgeRouter = true;
 						}
 					}
 					tCluster.addParticipatingCEP(tCEP);
 					tClusterFound = true;
 					tFoundCluster = tCluster;
 				}
 			}
 			if(!tClusterFound)
 			{
 				Cluster tCluster = new Cluster(new Long(tJoin.getTargetClusterID()), tJoin.getHierarchyLevel(), this);
 				if(tParticipate.isInterASCluster()) {
 					tCluster.setInterASCluster();
 					setSourceIntermediateCluster(tCluster, tCluster);
 				}
 				setSourceIntermediateCluster(tCluster, tCluster);
 				if(tConnectionSession == null) {
 					tConnectionSession = new CoordinatorSession(this, true, tJoin.getHierarchyLevel(), tCluster.getMultiplexer());
 				}
 
 				if(tJoin.getHierarchyLevel().isHigherLevel()) {
 					for(ICluster tVirtualNode : getRoutingTargetClusters()) {
 						if(tVirtualNode.getHierarchyLevel().getValue() == tJoin.getHierarchyLevel().getValue() - 1) {
 							tCluster.setPriority(tVirtualNode.getBullyPriority());
 						}
 					}
 				}
 				tCEP = new CoordinatorCEPChannel(this, tCluster);
 				if(tJoin.getHierarchyLevel().isHigherLevel()) {
 					((Cluster)tCluster).getMultiplexer().registerDemultiplex(tParticipate.getSourceClusterID(), tJoin.getTargetClusterID(), tCEP);
 				} else {
 					if(tParticipate.isInterASCluster()) {
 						tCEP.setEdgeCEP();
 						mIsEdgeRouter = true;
 					}
 				}
 				tCluster.getMultiplexer().addMultiplexedConnection(tCEP, tConnectionSession);
 				tCluster.addParticipatingCEP(tCEP);
 				tCluster.setAnnouncedCEP(tCEP);
 				tCEP.addAnnouncedCluster(tCluster, tCluster);
 				addRoutableTarget(tCluster);
 				tFoundCluster = tCluster;
 			}
 			tFoundCluster.getMultiplexer().addMultiplexedConnection(tCEP, tConnectionSession);
 			for(ICluster tNegotiatingCluster : getRoutingTargetClusters()) {
 				ClusterName tNegClusterName = new ClusterName(tParticipate.getSourceToken(), tParticipate.getSourceClusterID(), new HierarchyLevel(this, tJoin.getHierarchyLevel().getValue() - 1 > HRMConfig.Hierarchy.BASE_LEVEL ? tJoin.getHierarchyLevel().getValue() - 1 : 0 ));
 				if(tNegotiatingCluster.equals(tNegClusterName)) {
 					tCEP.setRemoteClusterName(tNegClusterName);
 				}
 			}
 			if(tCEP.getRemoteClusterName() == null && tJoin.getHierarchyLevel().isHigherLevel()) {
 				HashMap<ICluster, ClusterName> tNewlyCreatedClusters = new HashMap<ICluster, ClusterName>(); 
 				NeighborCluster tAttachedCluster = new NeighborCluster(tParticipate.getSourceClusterID(), tParticipate.getSourceName(), tParticipate.getSourceAddress(), tParticipate.getSourceToken(), new HierarchyLevel(this, tJoin.getHierarchyLevel().getValue() - 1), this);
 				tAttachedCluster.setPriority(tParticipate.getSenderPriority());
 				if(tAttachedCluster.getCoordinatorName() != null) {
 					try {
 						getHRS().registerNode(tAttachedCluster.getCoordinatorName(), tAttachedCluster.getCoordinatorsAddress());
 					} catch (RemoteException tExc) {
 						Logging.err(this, "Unable to fulfill requirements", tExc);
 					}
 				}
 				tNewlyCreatedClusters.put(tAttachedCluster, tParticipate.getPredecessor());
 				Logging.log(this, "as joining cluster");
 				for(ICluster tCandidate : getRoutingTargetClusters()) {
 					if((tCandidate instanceof Cluster) && (tCandidate.getHierarchyLevel().equals(tAttachedCluster.getHierarchyLevel()))) {
 						setSourceIntermediateCluster(tAttachedCluster, (Cluster)tCandidate);
 					}
 				}
 				if(getSourceIntermediate(tAttachedCluster) == null) {
 					Logging.err(this, "No source intermediate cluster for" + tAttachedCluster.getClusterDescription() + " found");
 				}
 				
 				Logging.log(this, "Created " + tAttachedCluster);
 				
 				tCEP.setRemoteClusterName(new ClusterName(tAttachedCluster.getToken(), tAttachedCluster.getClusterID(), tAttachedCluster.getHierarchyLevel()));
 				tAttachedCluster.addAnnouncedCEP(tCEP);
 				addRoutableTarget(tAttachedCluster);
 				if(tParticipate.getNeighbors() != null && !tParticipate.getNeighbors().isEmpty()) {
 					Logging.log(this, "Working on neighbors " + tParticipate.getNeighbors());
 					for(DiscoveryEntry tEntry : tParticipate.getNeighbors()) {
 						
 						/**
 						 * Create a ClusterName object from this entry
 						 */
 						ClusterName tEntryClusterName = new ClusterName(tEntry.getToken(), tEntry.getClusterID(), tEntry.getLevel());
 						
 						
 						ICluster tCluster = null;
 						if(tEntry.getRoutingVectors()!= null) {
 							for(RoutingServiceLinkVector tVector : tEntry.getRoutingVectors())
 							getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 						}
 						if(!getRoutingTargetClusters().contains(tEntryClusterName)) {
 							tCluster = new NeighborCluster(tEntry.getClusterID(), tEntry.getCoordinatorName(), tEntry.getCoordinatorRoutingAddress(),  tEntry.getToken(), tEntry.getLevel(), this);
 							tCluster.setPriority(tEntry.getPriority());
 							if(tEntry.isInterASCluster()) {
 								tCluster.setInterASCluster();
 							}
 							try {
 								getHRS().registerNode(tCluster.getCoordinatorName(), tCluster.getCoordinatorsAddress());
 							} catch (RemoteException tExc) {
 								Logging.err(this, "Unable to fulfill requirements", tExc);
 							}
 							
 							
 							
 							if(tEntry.isInterASCluster()) tCluster.setInterASCluster();
 							tNewlyCreatedClusters.put(tCluster, tEntry.getPredecessor());
 							for(ICluster tCandidate : getRoutingTargetClusters()) {
 								if(tCandidate instanceof Cluster && tCluster.getHierarchyLevel() == tCandidate.getHierarchyLevel()) {
 									setSourceIntermediateCluster(tCluster, (Cluster)tCandidate);
 									Logging.log(this, "as joining neighbor");
 								}
 							}
 							if(getSourceIntermediate(tAttachedCluster) == null) {
 								Logging.err(this, "No source intermediate cluster for" + tCluster.getClusterDescription() + " found");
 							}
 //							((NeighborCluster)tCluster).setClusterHopsOnOpposite(tEntry.getClusterHops(), tCEP);
 							((NeighborCluster)tCluster).addAnnouncedCEP(tCEP);
 							Logging.log(this, "Created " +tCluster);
 						} else {
 							for(ICluster tPossibleCandidate : getRoutingTargetClusters()) {
 								if(tPossibleCandidate.equals(tEntryClusterName)) {
 									tCluster = tPossibleCandidate;
 								}
 							}
 						}
 						getRoutableClusterGraph().storeLink(tAttachedCluster, tCluster, new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.LOGICAL_LINK));
 					}
 					for(ICluster tCluster : tAttachedCluster.getNeighbors()) {
 						if(getSourceIntermediate(tCluster) != null) {
 							setSourceIntermediateCluster(tAttachedCluster, getSourceIntermediate(tCluster));
 						}
 					}
 				} else {
 					Logging.warn(this, "Adding cluster that contains no neighbors");
 				}
 				for(ICluster tEveluateNegotiator : tNewlyCreatedClusters.keySet()) {
 					tCEP.addAnnouncedCluster(tEveluateNegotiator, getCluster(tNewlyCreatedClusters.get(tEveluateNegotiator)));
 				}
 			} else {
 				Logging.trace(this, "remote cluster was set earlier");
 			}
 			if(tCEP.getRemoteClusterName() == null) {
 				Logging.err(this, "Unable to set remote cluster");
 				ClusterName tRemoteClusterName = new ClusterName(tParticipate.getSourceToken(), tParticipate.getSourceClusterID(), tParticipate.getLevel());
 						
 				tCEP.setRemoteClusterName(tRemoteClusterName);
 			}
 			tCEP.setPeerPriority(tParticipate.getSenderPriority());
 			Logging.log(this, "Got request to open a new connection with reference cluster " + tFoundCluster);
 		}
 		
 		tConnectionSession.start(pConnection);
 	}
 	
 	@Override
 	public boolean openAck(LinkedList<Signature> pAuths, Description pDescription, Name pTargetName)
 	{
 		return true;
 	}
 	
 	public String toString()
 	{
 		return "HRM controller@" + getNode();
 	}
 	
 	/**
 	 * 
 	 * @param pSourceCluster source cluster
 	 * @param pTargetCluster specify the target cluster to which the path has to be checked for separation through another coordinator
 	 * @param pCEPsToEvaluate list of connection end points that have to be chosen to the target
 	 * @return true if the path contains a node that is covered by another coordinator
 	 */
 	public boolean checkPathToTargetContainsCovered(IRoutableClusterGraphTargetName pSourceCluster, IRoutableClusterGraphTargetName pTargetCluster, LinkedList<CoordinatorCEPChannel> pCEPsToEvaluate)
 	{
 		if(pSourceCluster == null || pTargetCluster == null) {
 			Logging.log(this, "checking cluster route between null and null");
 			return false;
 		}
 		RoutableClusterGraph<IRoutableClusterGraphTargetName, RoutableClusterGraphLink> tMap = ((ICluster)pSourceCluster).getHRMController().getRoutableClusterGraph();
 		List<RoutableClusterGraphLink> tClusterConnection = tMap.getRoute(pSourceCluster, pTargetCluster);
 		String tCheckedClusters = new String();
 		boolean isCovered = false;
 		for(RoutableClusterGraphLink tConnection : tClusterConnection) {
 			Collection<IRoutableClusterGraphTargetName> tNodes = tMap.getGraphForGUI().getIncidentVertices(tConnection);
 			for(IRoutableClusterGraphTargetName tNode : tNodes) {
 				if(tNode instanceof ICluster) {
 					CoordinatorCEPChannel tCEPLookingFor = null;
 					for(CoordinatorCEPChannel tCEP : pCEPsToEvaluate) {
 						if(tCEP.getRemoteClusterName().equals(tNode)) {
 							tCEPLookingFor = tCEP;
 						}
 					}
 					tCheckedClusters += tNode + " knows coordinator " + (tCEPLookingFor != null ? tCEPLookingFor.knowsCoordinator() : "UNKNOWN" ) + "\n";
 					if(tCEPLookingFor != null && tCEPLookingFor.knowsCoordinator()) {
 						isCovered = isCovered || true;
 					}
 				}
 			}
 		}
 		Logging.log(this, "Checked clusterroute from " + pSourceCluster + " to clusters " + tCheckedClusters);
 		return isCovered;
 	}
 	
 	/**
 	 * 
 	 * @param pCluster cluster identification
 	 * @return local object that holds meta information about the specified entity
 	 */
 	public Cluster getCluster(ICluster pCluster)
 	{
 		for(Cluster tCluster : getRoutingTargetClusters()) {
 			if (tCluster.equals(pCluster)) {
 				return tCluster;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param pCluster cluster to which the distance has to be computed
 	 * @return number of clusters to target
 	 */
 	public int getClusterDistance(ICluster pCluster)
 	{
 		List<RoutableClusterGraphLink> tClusterRoute = null;
 		int tDistance = 0;
 		if(getSourceIntermediate(pCluster) == null || pCluster == null) {
 			Logging.log(this, "source cluster for " + (pCluster instanceof NeighborCluster ? ((NeighborCluster)pCluster).getClusterDescription() : pCluster.toString() ) + " is " + getSourceIntermediate(pCluster));
 		}
 		ICluster tIntermediate = getSourceIntermediate(pCluster);
 		tClusterRoute = getRoutableClusterGraph().getRoute(tIntermediate, pCluster);
 		if(tClusterRoute != null && !tClusterRoute.isEmpty()) {
 			for(RoutableClusterGraphLink tConnection : tClusterRoute) {
 				if(tConnection.getLinkType() == RoutableClusterGraphLink.LinkType.LOGICAL_LINK) {
 					tDistance++;
 				}
 			}
 		} else {
 			Logging.log(this, "No cluster route available");
 			tClusterRoute = getRoutableClusterGraph().getRoute(tIntermediate, pCluster);
 		}
 		return tDistance;
 	}
 
 	/**
 	 * 
 	 * @param pParticipationProperty is the object that describes in which cluster this node wishes to participate
 	 * @return @return the description that will be put into the packet
 	 */
 	public Description getConnectDescription(ClusterParticipationProperty pParticipationProperty)
 	{
 		Logging.log(this, "Creating a cluster participation property for level " + pParticipationProperty.getHierarchyLevel());
 		Description tDescription = new Description();
 		//try {
 		tDescription.set(new ContactDestinationApplication(null, HRMController.ROUTING_NAMESPACE));
 		//} catch (PropertyException tExc) {
 		//	Logging.err(this, "Unable to fulfill requirements given by ContactDestinationProperty", tExc);
 		//}
 
 		try {
 			tDescription.add(pParticipationProperty);
 		} catch (PropertyException tExc) {
 			Logging.err(this, "Unable to match property that wants us to participate in a cluster", tExc);
 		}
 		return tDescription;
 	}
 	
 	/**
 	 * This method has to be invoked once a new neighbor node is spotted (/hierarchy level 0).
 	 * It causes the addition to the intermediate cluster that is associated to the interface the note was spotted at.
 	 * 
 	 * @param pName is the name of the entity a connection will be established to
 	 * @param pLevel is the level at which a connection is added
 	 * @param pToClusterID is the identity of the cluster a connection will be added to
 	 * @param pConnectionToOtherAS says whether the connection leads to another autonomous system
 	 */
 	public void addConnection(Name pName, HierarchyLevel pLevel, Long pToClusterID, boolean pConnectionToOtherAS)
 	{
 		Logging.log(this, "ADDING CONNECTION to " + pName + "(ClusterID=" + pToClusterID + ", interAS=" + pConnectionToOtherAS + " on hier. level " + pLevel);
 
 		CoordinatorSession tCEP = null;
 		ICluster tFoundCluster = null;
 		CoordinatorCEPChannel tDemux = null;
 		
 		boolean tClusterFound = false;
 		for(Cluster tCluster : getRoutingTargetClusters())
 		{
 			if(tCluster.getClusterID().equals(pToClusterID)) {
 				tCEP = new CoordinatorSession(this, false, pLevel, tCluster.getMultiplexer());
 				Route tRoute = null;
 				try {
 					tRoute = getHRS().getRoute(getNode().getCentralFN(), pName, new Description(), getNode().getIdentity());
 				} catch (RoutingException tExc) {
 					Logging.err(this, "Unable to resolve route to " + pName, tExc);
 				} catch (RequirementsException tExc) {
 					Logging.err(this, "Unable to fulfill requirements for a route to " + pName, tExc);
 				}
 				tCEP.setRouteToPeer(tRoute);
 				tDemux = new CoordinatorCEPChannel(this, tCluster);
 				tCluster.getMultiplexer().addMultiplexedConnection(tDemux, tCEP);
 				
 				tCluster.addParticipatingCEP(tDemux);
 				tFoundCluster = tCluster;
 				tClusterFound = true;
 			}
 		}
 		if(!tClusterFound)
 		{
 			Logging.log(this, "Cluster is new, creating objects...");
 			Cluster tCluster = new Cluster(new Long(pToClusterID), pLevel, this);
 			setSourceIntermediateCluster(tCluster, tCluster);
 			addRoutableTarget(tCluster);
 			tCEP = new CoordinatorSession(this, false, pLevel, tCluster.getMultiplexer());
 			tDemux = new CoordinatorCEPChannel(this, tCluster);
 			tCluster.getMultiplexer().addMultiplexedConnection(tDemux, tCEP);
 			
 			tCluster.addParticipatingCEP(tDemux);
 			tFoundCluster = tCluster;
 		}
 		final ClusterParticipationProperty tProperty = new ClusterParticipationProperty(pToClusterID, pLevel, 0);
 		NestedParticipation tParticipate = tProperty.new NestedParticipation(pToClusterID, 0);
 		tProperty.addNestedparticipation(tParticipate);
 		
 		if(pConnectionToOtherAS) {
 			tFoundCluster.setInterASCluster();
 			mIsEdgeRouter = true;
 			tDemux.setEdgeCEP();
 			tParticipate.setInterASCluster();	
 		}
 		tParticipate.setSourceClusterID(pToClusterID);
 		
 		final Name tName = pName;
 		final CoordinatorSession tConnectionCEP = tCEP;
 		final CoordinatorCEPChannel tDemultiplexed = tDemux;
 		final ICluster tClusterToAdd = tFoundCluster;
 		
 		Thread tThread = new Thread() {
 			public void run()
 			{
 				Connection tConn = null;
 				try {
 					Logging.log(this, "CREATING CONNECTION to " + tName);
 					tConn = getHost().connectBlock(tName, getConnectDescription(tProperty), getNode().getIdentity());
 				} catch (NetworkException tExc) {
 					Logging.err(this, "Unable to connecto to " + tName, tExc);
 				}
 				if(tConn != null) {
 					Logging.log(this, "Sending source routing service address " + tConnectionCEP.getSourceRoutingServiceAddress() + " for connection number " + (++mConnectionCounter));
 					tConnectionCEP.start(tConn);
 					
 					HRMName tMyAddress = tConnectionCEP.getSourceRoutingServiceAddress();
 
 					Route tRoute = null;
 					try {
 						tRoute = getHRS().getRoute(getNode().getCentralFN(), tName, new Description(), getNode().getIdentity());
 					} catch (RoutingException tExc) {
 						Logging.err(this, "Unable to find route to " + tName, tExc);
 					} catch (RequirementsException tExc) {
 						Logging.err(this, "Unable to find route to " + tName + " with requirements no requirents, Huh!", tExc);
 					}
 					
 					HRMName tMyFirstNodeInDirection = null;
 					if(tRoute != null) {
 						RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 						GateID tID= tPath.getFirst();
 						
 						Collection<RoutingServiceLink> tLinkCollection = getHRS().getLocalRoutingMap().getOutEdges(tMyAddress);
 						RoutingServiceLink tOutEdge = null;
 						
 						for(RoutingServiceLink tLink : tLinkCollection) {
 							if(tLink.equals(tID)) {
 								tOutEdge = tLink;
 							}
 						}
 						
 						tMyFirstNodeInDirection = getHRS().getLocalRoutingMap().getDest(tOutEdge);
 						tConnectionCEP.setRouteToPeer(tRoute);
 					}
 					
 					Tuple<HRMName, HRMName> tTuple = new Tuple<HRMName, HRMName>(tMyAddress, tMyFirstNodeInDirection);
 					tConnectionCEP.write(tTuple);
 					tDemultiplexed.setRemoteClusterName(new ClusterName(tClusterToAdd.getToken(), tClusterToAdd.getClusterID(), tClusterToAdd.getHierarchyLevel()));
 				}
 			}
 		};
 		tThread.start();
 	}
 	
 	@Override
 	protected void started() {
 		;
 	}
 	
 	@Override
 	public void exit() {
 	}
 
 	@Override
 	public boolean isRunning() {
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @return hierarchical routing service of this entity
 	 */
 	public HierarchicalRoutingService getHRS()
 	{
 		return mHRS;
 	}
 	
 	/**
 	 * @return the physical node running this coordinator
 	 */
 	public Node getNode() //TV
 	{
 		return mPhysicalNode;
 	}
 	
 	/**
 	 * Return the actual GUI name description of the physical node;
      * However, this function should only be used for debug outputs, e.g., GUI outputs.
 	 * @return the GUI name
 	 */
 	public String getNodeGUIName()
 	{
 		return mPhysicalNode.getName();
 	}	
 	
 	/**
 	 * 
 	 * @param pCluster is the cluster to be added to the local cluster map
 	 */
 	public synchronized void addRoutableTarget(ICluster pCluster)
 	{
 		if(!mRoutableClusterGraph.contains(pCluster)) {
 			mRoutableClusterGraph.add(pCluster);
 		}
 	}
 	
 	/**
 	 * Calculates the clusters which are known to the local routing database (graph)
 	 * 
 	 * @return list of all known clusters from the local routing database (graph)
 	 */
 	public synchronized LinkedList<Cluster> getRoutingTargetClusters()
 	{
 		LinkedList<Cluster> tResult = new LinkedList<Cluster>();
 
 		if (HRM_CONTROLLER_DEBUGGING) {
 			Logging.log(this, "Amount of found routing targets: " + mRoutableClusterGraph.getVertices().size());
 		}
 		int j = -1;
 		for(IRoutableClusterGraphTargetName tRoutableGraphNode : mRoutableClusterGraph.getVertices()) {
 			if (tRoutableGraphNode instanceof Cluster) {
 				Cluster tCluster = (Cluster)tRoutableGraphNode;
 				j++;
 			
 				if (HRM_CONTROLLER_DEBUGGING) {
 					Logging.log(this, "Returning routing target cluster " + j + ": " + tRoutableGraphNode.toString());
 				}
 				
 				tResult.add(tCluster);
 			}else if (tRoutableGraphNode instanceof NeighborCluster){
 				Logging.warn(this, "Ignoring routing target " + tRoutableGraphNode);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Calculates the clusters which are known to the local routing database (graph)
 	 * 
 	 * @return list of all known clusters from the local routing database (graph)
 	 */
 	public synchronized LinkedList<ICluster> getRoutingTargets()
 	{
 		LinkedList<ICluster> tResult = new LinkedList<ICluster>();
 
 		if (HRM_CONTROLLER_DEBUGGING) {
 			Logging.log(this, "Amount of found routing targets: " + mRoutableClusterGraph.getVertices().size());
 		}
 		int j = -1;
 		for(IRoutableClusterGraphTargetName tRoutableGraphNode : mRoutableClusterGraph.getVertices()) {
 			ICluster tCluster = (ICluster)tRoutableGraphNode;
 			j++;
 		
 			if (HRM_CONTROLLER_DEBUGGING) {
 				Logging.log(this, "Returning routing target " + j + ": " + tRoutableGraphNode.toString());
 			}
 			
 			tResult.add(tCluster);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * 
 	 * @return cluster map that is actually the graph that represents the network
 	 */
 	public RoutableClusterGraph<IRoutableClusterGraphTargetName, RoutableClusterGraphLink> getRoutableClusterGraph()
 	{
 		return mRoutableClusterGraph;
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
 	public Cluster getSourceIntermediate(ICluster pCluster)
 	{
 		if(mIntermediateMapping.containsKey(pCluster)) {
 			
 			return mIntermediateMapping.get(pCluster);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Determines the coordinator for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel level for which all cluster managers should be provided
 	 * @return list of managers at the level
 	 */
 	public LinkedList<Coordinator> getCoordinator(HierarchyLevel pHierarchyLevel)
 	{
 		// is the given hierarchy level valid?
 		if (pHierarchyLevel.isUndefined()){
 			Logging.warn(this, "Cannot determine coordinator on an undefined hierachy level, return null");
 			return null;
 		}
 
 		// check of we know the search coordinator
 		if(mRegisteredCoordinators.size() - 1 < pHierarchyLevel.getValue()) {
 			// we don't know a valid coordinator
 			return null;
 		} else {
 			// we have found the searched coordinator
 			return mRegisteredCoordinators.get(pHierarchyLevel.getValue());
 		}
 	}
 	
 	/**
 	 * Registers a coordinator for a defined hierarchy level.
 	 * 
 	 * @param pCoordinator the coordinator for a defined cluster
 	 * @param pHierarchyLevel the hierarchy level at which the coordinator is located
 	 */
 	public void registerCoordinator(Coordinator pCoordinator, HierarchyLevel pHierarchyLevel)
 	{
 		int tLevel = pHierarchyLevel.getValue();
 		
 		// make sure we have a valid linked list object
 		if(mRegisteredCoordinators == null) {
 			mRegisteredCoordinators = new LinkedList<LinkedList<Coordinator>>();
 		}
 		
 		if(mRegisteredCoordinators.size() <= tLevel) {
 			for(int i = mRegisteredCoordinators.size() - 1; i <= tLevel ; i++) {
 				mRegisteredCoordinators.add(new LinkedList<Coordinator>());
 			}
 		}
 		
 		if (mRegisteredCoordinators.get(tLevel).size() > 0){
			Logging.log(this, "#### Got more than one coordinator at level " + tLevel + ", already known (0): " + mRegisteredCoordinators.get(tLevel).get(0) + ", new one: " + pCoordinator);
 		}
 		
 		// store the new coordinator
 		mRegisteredCoordinators.get(tLevel).add(pCoordinator);
 		
 		// update GUI: image for node object 
 		//TODO: check and be aware of topology dynamics
		getNode().setDecorationParameter("L"+ tLevel);
 	}
 	
 	public void unregisterCoordinator(Coordinator pCoordiantor)
 	{
 		//TODO: implement this
 	}
 	
 	/**
 	 * 
 	 * @return list of all signatures that were already approved
 	 */
 	public LinkedList<HRMSignature> getApprovedSignatures()
 	{
 		return mApprovedSignatures;
 	}
 	
 	/**
 	 * 
 	 * @param pSignature is a signature that validates a FIB entry.
 	 */
 	public void addApprovedSignature(HRMSignature pSignature)
 	{
 		if(mApprovedSignatures == null) {
 			mApprovedSignatures = new LinkedList<HRMSignature>();
 		}
 		if(!mApprovedSignatures.contains(pSignature)) {
 			mApprovedSignatures.add(pSignature);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pIdentity is the identity that is supposed to be used for signing FIB entries
 	 */
 	public void setIdentity(HRMIdentity pIdentity)
 	{
 		mIdentity = pIdentity;
 	}
 
 	public HRMIdentity getIdentity()
 	{
 		return mIdentity;
 	}
 	
 	/**
 	 * 
 	 * @param pIdentification is one more identification the physical node may have because it can be either coordinator of different hierarchical levels or attached to different clusters
 	 */
 	public void addIdentification(HRMID pIdentification)
 	{
 		if(!mIdentifications.contains(pIdentification)) {
 			mIdentifications.add(pIdentification);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pIdentification is one HRMID that is checked against the identifications of the node owning the coordinator object
 	 * @return
 	 */
 	public boolean containsIdentification(HRMID pIdentification)
 	{
 		return mIdentifications.contains(pIdentification);
 	}
 	
 	/**
 	 * 
 	 * @param pLevel is the level at which a search for clusters is done
 	 * @return all virtual nodes that appear at the specified hierarchical level
 	 */
 //	public LinkedList<IRoutableClusterGraphTargetName> getClusters(int pLevel)
 //	{
 //		LinkedList<IRoutableClusterGraphTargetName> tClusters = new LinkedList<IRoutableClusterGraphTargetName>();
 //		for(IRoutableClusterGraphTargetName tNode : getRoutableClusterGraph().getVertices()) {
 //			if(tNode instanceof ICluster && ((ICluster) tNode).getHierarchyLevel().getValue() == pLevel) {
 //				tClusters.add((ICluster) tNode);
 //			}
 //		}
 //		return tClusters;
 //	}
 //	
 	/**
 	 * Find out whether this object is an edge router
 	 * 
 	 * @return true if the node is a router to another autonomous system
 	 */
 	public boolean isEdgeRouter()
 	{
 		return mIsEdgeRouter;
 	}
 	
 	/**
 	 * 
 	 * @param pLevel is the level at which a multiplexer to other clusters is installed and that has to be returned
 	 * @return
 	 */
 	public CoordinatorCEPMultiplexer getMultiplexerOnLevel(int pLevel)
 	{
 		if(mMuxOnLevel == null) {
 			mMuxOnLevel = new HashMap<Integer, CoordinatorCEPMultiplexer>();
 		}
 		if(!mMuxOnLevel.containsKey(pLevel)) {
 			CoordinatorCEPMultiplexer tMux = new CoordinatorCEPMultiplexer(this);
 			mMuxOnLevel.put(pLevel, tMux);
 			Logging.log(this, "Created new Multiplexer " + tMux + " for cluster managers on level " + pLevel);
 		}
 		return mMuxOnLevel.get(pLevel);
 	}
 }
