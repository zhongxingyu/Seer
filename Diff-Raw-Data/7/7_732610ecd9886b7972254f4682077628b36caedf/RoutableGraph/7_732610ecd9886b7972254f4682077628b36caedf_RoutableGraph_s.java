 /*******************************************************************************
  * Graph
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
 package de.tuilmenau.ics.graph;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.apache.commons.collections15.Transformer;
 
 import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseMultigraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 
 
 /**
  * Stores graph and allows routing operations.
  */
 public class RoutableGraph<NodeObject, LinkObject> extends Observable implements Observer
 {
 	public enum EventType { ADDED, REMOVED, UPDATED };
 	
 	/**
 	 * Enables the caching of routing decisions. The caching leads to
 	 * high memory consumption in large networks. Therefore, it should
 	 * only be activated if the scenario is reasonable small.
 	 */
 	private static final boolean ENABLE_CACHING = false; 
 	
 	public class Event
 	{
 		public Event(EventType type, Object obj)
 		{
 			this.type = type;
 			this.obj = obj;
 		}
 		
 		@Override
 		public String toString()
 		{
 			return obj.toString() +" " +type;
 		}
 		
 		public EventType type;
 		public Object obj;
 	}
 	
 	/**
 	 * Constructor for map with constant link weights equal to 1
 	 */
 	public RoutableGraph()
 	{
 		this(null);
 	}
 	
 	/**
 	 * Constructor for dynamic link weights derived from a transformer
 	 * 
 	 * @param pLinkToValueTransformer Transformer from links to link weight values
 	 */
 	public RoutableGraph(LinkTransformer<LinkObject> pLinkToValueTransformer)
 	{
 		super();
 		
 		mNodes = new SparseMultigraph<NodeObject, LinkObject>();
		mLinkToValueTransformer = new LinkTransformerAdapter(pLinkToValueTransformer);
 	}
 	
 	private class LinkTransformerAdapter implements Transformer<LinkObject, Number>
 	{
 		public LinkTransformerAdapter(LinkTransformer<LinkObject> transformer)
 		{
 			this.transformer = transformer;
 		}
 		
 		@Override
 		public Number transform(LinkObject input)
 		{
 			return transformer.transform(input);
 		}
 		
 		private LinkTransformer<LinkObject> transformer;
 	}
 	
 	public Collection<NodeObject> getVertices()
 	{
 		LinkedList<NodeObject> tNodes = new LinkedList<NodeObject>();
 		synchronized(mNodes) {
 			for(NodeObject tObj : mNodes.getVertices()) {
 				tNodes.add(tObj);
 			}
 		}
 		return tNodes;
 	}
 	
 	public int getNumberVertices()
 	{
 		return mNodes.getVertexCount();
 	}
 	
 	public Collection<LinkObject> getEdges()
 	{
 		LinkedList<LinkObject> tCollection = new LinkedList<LinkObject>();
 		synchronized(mNodes) {
 			for(LinkObject tLink : mNodes.getEdges()) {
 				tCollection.add(tLink);
 			}
 		}
 		return tCollection;
 	}
 	
 	public int getNumberEdges()
 	{
 		return mNodes.getEdgeCount();
 	}
 	
 	public NodeObject getDest(LinkObject pLink)
 	{
 		return mNodes.getDest(pLink);
 	}
 	
 	public NodeObject getSource(LinkObject pLink)
 	{
 		return mNodes.getSource(pLink);
 	}
 	
 	public synchronized Collection<LinkObject> getOutEdges(NodeObject pNode)
 	{
 		Collection<LinkObject> tEdges = null;
 		for(NodeObject tNode : mNodes.getVertices()) {
 			if(pNode.equals(tNode)) {
 				tEdges = mNodes.getOutEdges(tNode);
 				break;
 			}
 		}
 		
 		return tEdges;
 	}
 
 	/**
 	 * Method for GUI purposes ONLY!
 	 * 
 	 * @return Reference to plain graph object (for read access only)
 	 */
 	public Graph<NodeObject, LinkObject> getGraphForGUI()
 	{
 		return mNodes;
 	}
 	
 	/**
 	 * Used to remove all elements without notifying anybody about that.
 	 * Needed for exiting a simulation at any time.
 	 */
 	public void cleanup()
 	{
 		Collection<NodeObject> tNodes = getVertices();
 		while(!tNodes.isEmpty()) {
 			NodeObject tNode = tNodes.iterator().next();
 			mNodes.removeVertex(tNode);
 		}
 	}
 	
 	/**
 	 * Adds node to topology, if it is not already included.
 	 * 
 	 * @param pNode node to include
 	 * @return node object used for structure (object added previously but equal to pNode OR pNode if it is new)
 	 */
 	public synchronized NodeObject add(NodeObject pNode)
 	{
 		NodeObject tRes = null;
 		
 		if(pNode != null) {
 			tRes = containsVertex(pNode);
 			
 			if(tRes == null) {
 				mNodes.addVertex(pNode);
 				mResetRouting = true;
 				tRes = pNode;
 				
 				// register map as an observer of object
 				if(pNode instanceof Observable) {
 					((Observable) pNode).addObserver(this);
 				}
 				
 				// inform observer of map about new object
 				notifyObservers(new Event(EventType.ADDED, pNode));
 			}
 		}
 		
 		return tRes;
 	}
 	
 	public synchronized boolean contains(NodeObject pNode)
 	{
 		return (containsVertex(pNode) != null);
 	}
 	
 	public synchronized boolean remove(NodeObject pNode)
 	{
 		NodeObject tUsedObj = containsVertex(pNode);
 		
 		if(tUsedObj != null) {
 			boolean tRes = mNodes.removeVertex(tUsedObj);
 			
 			mResetRouting = true;
 			
 			// remove map as an observer from object
 			if(pNode instanceof Observable) {
 				((Observable) pNode).deleteObserver(this);
 			}
 			
 			// inform observer of map about deletion
 			if(tRes) notifyObservers(new Event(EventType.REMOVED, pNode));
 			
 			return tRes;
 		} else
 			return false;
 	}
 	
 	/**
 	 * Links two nodes. If nodes not exists, they will be created.
 	 * Method do NOT check, if link already exists.
 	 * 
 	 * @param pFrom link starts at
 	 * @param pTo links end at
 	 * @param pLinkValue link object
 	 */
 	public synchronized void link(NodeObject pFrom, NodeObject pTo, LinkObject pLinkValue)
 	{
 		if((pFrom != null) && (pTo != null) && (pLinkValue != null)) {
 			// get equivalent object used for map for pFrom and pTo:
 						
 			pFrom = add(pFrom);
 			pTo = add(pTo);
 			
 			if(mNodes.addEdge(pLinkValue, pFrom, pTo, EdgeType.DIRECTED)) {
 				mResetRouting = true;
 				notifyObservers(new Event(EventType.ADDED, pLinkValue));
 			}
 		}
 	}
 	
 	/**
 	 * Checks whether there is a direct edge between two nodes. Optionally, the
 	 * link between both can be checked, too.
 	 * 
 	 * @param pFrom From node (!= null)
 	 * @param pTo To node (!= null)
 	 * @param pLinkValueTemplate null=any link between the nodes is sufficient; otherwise, it has to equal the parameter 
 	 * @return If the two nodes are directly connected
 	 */
 	public boolean isDirectEdge(NodeObject pFrom, NodeObject pTo, LinkObject pLinkValueTemplate)
 	{
 		return (getEdge(pFrom, pTo, pLinkValueTemplate) != null);
 	}
 	
 	/**
 	 * Searches for a link object between two nodes. Optionally, the
 	 * link between both can be checked, if it equals an search template.
 	 * 
 	 * @param pFrom From node (!= null)
 	 * @param pTo To node (!= null)
 	 * @param pLinkValueTemplate null=any link between the nodes is sufficient; otherwise, it has to equal the parameter 
 	 * @return First object matches the search parameters; null if no edge available
 	 */
 	public synchronized LinkObject getEdge(NodeObject pFrom, NodeObject pTo, LinkObject pLinkValueTemplate)
 	{
 		if((pFrom != null) && (pTo != null)) {
 			// get equivalent object used for map for pFrom and pTo:
 			Collection<LinkObject> tOutEdges = getOutEdges(pFrom);
 			
 			for(LinkObject tLink : tOutEdges) {
 				NodeObject tTo = getDest(tLink);
 				
 				if(tTo != null) {
 					if(tTo.equals(pTo)) {
 						// check optional link object; if not available
 						// we just look for the nodes
 						if(pLinkValueTemplate != null) {
 							if(pLinkValueTemplate.equals(tLink)) {
 								return tLink;
 							}
 						} else {
 							return tLink;
 						}
 					}
 					// else: strange; ignore it
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public synchronized boolean unlink(LinkObject pLinkValue)
 	{
 		boolean tRes = mNodes.removeEdge(pLinkValue);
 		
 		if(tRes) {
 			mResetRouting = true;
 			notifyObservers(new Event(EventType.REMOVED, pLinkValue));
 		}
 
 		return tRes;
 	}
 	
 	public synchronized void edgeWeightChanged(LinkObject pLink)
 	{
 		mResetRouting = true;
 	}
 	
 	public synchronized List<LinkObject> getRoute(NodeObject pFrom, NodeObject pTo)
 	{
 		List<LinkObject> tPath = null;
 				
 		pFrom = containsVertex(pFrom);
 		pTo = containsVertex(pTo);
 		
 		if((pFrom != null) && (pTo != null)) {
 			// is there an old algorithm object, which can be reused?
 			if(mRoutingAlg == null) {
 				mResetRouting = false;
 				
 				// either Dijkstra with variable link weights or constant link weights equal to 1
 				if(mLinkToValueTransformer != null) mRoutingAlg = new DijkstraShortestPath<NodeObject, LinkObject>(mNodes, mLinkToValueTransformer, ENABLE_CACHING);
 				else mRoutingAlg = new DijkstraShortestPath<NodeObject, LinkObject>(mNodes, ENABLE_CACHING);
 			} else {
 				// are there any changes and we have to remove the cached values?
 				if(ENABLE_CACHING) {
 					if(mResetRouting) {
 						mRoutingAlg.reset();
 						mResetRouting = false;
 					}
 				}
 			}
 			
 			tPath = mRoutingAlg.getPath(pFrom, pTo);
 
 			if(!ENABLE_CACHING) {
 				// cleanup data structure; somehow that does not work, if
 				// "chached" is turned of in constructor only.
 				mRoutingAlg.reset();
 			}
 		}
 		
 		return tPath;
 	}
 	
 	/**
 	 * Method is called by objects observed by the map. It is triggering an
 	 * update event for the observers of the map. In special, this mechanism
 	 * enables the GUI to redraw after the map or an object of the map has
 	 * changed.
 	 */
 	@Override
 	public void update(Observable observable, Object parameter)
 	{
 		notifyObservers(new Event(EventType.UPDATED, observable));
 	}
 	
 	/**
 	 * Replaces mNodes.containsVertex because we have to use equals to compare the
 	 * objects. This is needed due to the usage with RMI.
 	 * 
 	 * Note: In order to avoid parallel changes in the vertex list, the access
 	 *       to that list (add, contains, remove) must by synchronized.
 	 * 
 	 * @param pNewNode node to search for
 	 * @return found node in structure equal to pNewNode OR null
 	 */
 	protected NodeObject containsVertex(NodeObject pNewNode)
 	{
 		if(pNewNode == null) {
 			throw new NullPointerException("TopologyMap.containsVertex with null argument (" +this +")");
 		}
 
 		// replaces mNode.containsVertex, which is doing only a reference comparison
 		for(NodeObject tNode : mNodes.getVertices()) {
 			if(pNewNode.equals(tNode)) {
 				return tNode;
 			}
 		}
 				
 		return null;
 	}	
 	
     /**
 	 * Checks whether two nodes are directly linked with each other. Optionally, the
 	 * link between both can be checked, too.
 	 * 
 	 * @param pFrom From node (!= null)
 	 * @param pTo To node (!= null)
 	 * @param pLinkValue Optional; null=any link between the nodes is sufficient
 	 * @return If the two nodes are directly connected
 	 */
 	public synchronized boolean isLinked(NodeObject pFrom, NodeObject pTo, LinkObject pLinkValue)
 	{
 		boolean tRes = false;
 	
 		if((pFrom != null) && (pTo != null)) {
 			// get equivalent object used for map for pFrom and pTo:
 			Collection<LinkObject> tOutEdges = getOutEdges(pFrom);
 			if(tOutEdges != null) {
 				for(LinkObject tLink : tOutEdges) {
 	                NodeObject tTo = getDest(tLink);
 	
 	                if(tTo != null) {
 		                if(tTo.equals(pTo)) {
 	                        // check optional link object; if not available
 	                        // we just look for the nodes
 	                        if(pLinkValue != null) {
                                 if(pLinkValue.equals(tLink)) {
                                     return true;
                                 }
 	                        } else {
                                 return true;
 	                        }
 		                }
 	                }
 	                // else: strange; ignore it
 				}
 			}
 		}
 		return tRes;
 	}
 	
 	public synchronized boolean isLinked(NodeObject pFrom, NodeObject pTo)
 	{
 		return mNodes.isNeighbor(pFrom, pTo);
 	}
 	
 	public synchronized void notifyObservers(Object pEvent)
 	{
 		setChanged();
 		super.notifyObservers(pEvent);
 	}
 	
 	protected Graph<NodeObject, LinkObject> mNodes = null;
 	private Transformer<LinkObject, Number> mLinkToValueTransformer = null;
 
 	private DijkstraShortestPath<NodeObject, LinkObject> mRoutingAlg = null;
 	private boolean mResetRouting = false;
 }
