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
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
 import edu.uci.ics.jung.graph.util.EdgeType;
 
 /**
  * Data storage for an abstracted topology view of higher hierarchy levels. 
  * See http://jung.sourceforge.net/site/apidocs/edu/uci/ics/jung/graph/Graph.html for documentation about inherited member functions.
  * 
  * @param <NodeObject> define what is used as node objects
  * @param <LinkObject> define what is used as link objects
  */
 public class AbstractRoutingGraph<NodeObject, LinkObject> extends RoutableGraph<NodeObject, LinkObject>
 {
 	public AbstractRoutingGraph()
 	{
 		super(null);
 	}
 
 	/**
 	 * 
 	 * @param pSource is the node you want to know all neighbors for.
 	 * @return
 	 */
 	public synchronized Collection<NodeObject> getNeighbors(NodeObject pSource)
 	{
 		return (mRoutingGraph.containsVertex(pSource) ? mRoutingGraph.getNeighbors(pSource) : new LinkedList<NodeObject>());
 	}
 	
 	/**
 	 * This method registers a link between two nodes in the routing graph. 
 	 * If the nodes don't exist in the routing graph, they are registered implicitly.
 	 * 
 	 * @param pFrom starting point of the link
 	 * @param pTo the ending point of the link
 	 * @param pLinkObject the link object
 	 */
 	@Override
 	public synchronized void link(NodeObject pFrom, NodeObject pTo, LinkObject pLinkObject)
 	{
 		// check if parameters are valid
 		if((pFrom != null) && (pTo != null) && (pLinkObject != null)) {
 			// make sure the starting point is known
 			pFrom = add(pFrom);
 			
 			// make sure the ending point is known
 			pTo = add(pTo);
 			
 			// check if link already exists
 			if(!isLinked(pFrom, pTo)) {
 				// check if there already exist a link between these two nodes
 				if(!mRoutingGraph.getNeighbors(pFrom).contains(pTo)) {
 					// add the link to the routing graph
 					if(mRoutingGraph.addEdge(pLinkObject, pFrom, pTo, EdgeType.UNDIRECTED)) {
 						notifyObservers(new Event(EventType.ADDED, pLinkObject));
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Get all nodes that are between the source and the target
 	 * 
 	 * @param pFrom This is the source of the path you want to get all nodes for.
 	 * @param pTo This is the target of the path you want to get all nodes for
 	 * @return a list of all nodes between source and target
 	 */
 //	public synchronized List<NodeObject> getIntermediateNodes(NodeObject pFrom, NodeObject pTo)
 //	{
 //		List<LinkObject> tPath = null;
 //		
 //		LinkedList<NodeObject> tNodes = new LinkedList<NodeObject>();
 //		
 //		pFrom = containsVertex(pFrom);
 //		pTo = containsVertex(pTo);
 //		
 //		if((pFrom != null) && (pTo != null)) {
 //			DijkstraShortestPath<NodeObject, LinkObject> tRoutingAlgo = new DijkstraShortestPath<NodeObject, LinkObject>(mRoutingGraph);
 //			tPath = tRoutingAlgo.getPath(pFrom, pTo);
 //			
 //			NodeObject tTarget = pFrom;
 //			
 //			for(LinkObject tLink : tPath) {
 //				tTarget = getOtherEndOfLink(tTarget, tLink);
 //				tNodes.add(tTarget);
 //			}
 //		}
 //
 //		return tNodes;
 //	}
 	
 	/**
 	 * Get the other end node of a link in the stored undirected graph.
 	 * 
 	 * @param pKnownEnd the known end node of the link
 	 * @param pLink the link for which the other end node should be determined
 	 * @return the other end node of the link
 	 */
 	public synchronized NodeObject getOtherEndOfLink(NodeObject pKnownEnd, LinkObject pLink)
 	{
 		NodeObject tResult = null;
 		
 		try {
 			tResult = mRoutingGraph.getOpposite(pKnownEnd, pLink);
 		} catch (IllegalArgumentException tExc) {
 			Logging.err(this, pKnownEnd + " isn't an end node of the link " + pLink + "(possible end nodes are: " + mRoutingGraph.getIncidentVertices(pLink) + ")", tExc);
 		}
 		
 		return tResult;
 	}
 	
 
 	/**
 	 * Checks if two nodes have a known link.
 	 * 
 	 * @param pFirst the first node
 	 * @param pSecond the second node
 	 * @return true if a link is known, otherwise false
 	 */
 	public synchronized boolean isLinked(NodeObject pFirst, NodeObject pSecond)
 	{
 		if(mRoutingGraph.containsVertex(pFirst)) {
 			return mRoutingGraph.getNeighbors(pFirst).contains(pSecond);
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks if a node is a known one.
 	 * 
 	 * @param pNode the node
 	 * @return true if the node is known, otherwise false
 	 */
	public synchronized boolean isknown(NodeObject pNode)
 	{
		return mRoutingGraph.containsVertex(pNode);
 	}
 
 	/**
 	 * Return a descriptive string
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName();
 	}
 
 	@Override
 	public synchronized List<LinkObject> getRoute(NodeObject pFrom, NodeObject pTo)
 	{
 		List<LinkObject> tResult = null;
 
 		pFrom = containsVertex(pFrom);
 		pTo = containsVertex(pTo);
 
 		if((pFrom != null) && (pTo != null)) {
 			// use Djikstra over the routing graph
 			DijkstraShortestPath<NodeObject, LinkObject> tRoutingAlgo = new DijkstraShortestPath<NodeObject, LinkObject>(mRoutingGraph);
 			tResult = tRoutingAlgo.getPath(pFrom, pTo);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * @param pFromRadius
 	 * @param pToRadius
 	 * @return
 	 */
 	public List<NodeObject> getVerticesInOrderRadius(NodeObject pRootVertex)
 	{
 		List<NodeObject> tResult = null;
 		
 		//HINT: http://jung.sourceforge.net/doc/api/edu/uci/ics/jung/algorithms/shortestpath/BFSDistanceLabeler.html
 		
 		// create "Breadth-First Search" (BFS) object
 		BFSDistanceLabeler<NodeObject, LinkObject> tBreadthFirstSearch = new BFSDistanceLabeler<NodeObject, LinkObject>();
 
 		// compute the distances of all the node from the specified root node (parent cluster).
 		tBreadthFirstSearch.labelDistances(getGraphForGUI(), pRootVertex);
 
 		// the result
 		tResult = tBreadthFirstSearch.getVerticesInOrderVisited();
 		
 		return tResult;
 	}
 }
