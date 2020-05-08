 package hypeerweb;
 
 import hypeerweb.visitors.SendVisitor;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.TreeMap;
 import validator.HyPeerWebInterface;
 
 /**
  * The Great HyPeerWeb
  * @param <T> The Node type for this HyPeerWeb instance
  * @author isaac
  */
 public class HyPeerWebSegment<T extends Node> extends Node implements HyPeerWebInterface{
 	private Database db = null;
 	private TreeMap<Integer, Node> nodes;
 	private HyPeerWebState state;
 	//Random number generator for getting random nodes
 	private static final Random rand = new Random();
 	private static SendVisitor randVisitor;
 	//Error messages
 	private static final Exception
 			addNodeErr = new Exception("Failed to add a new node"),
 			removeNodeErr = new Exception("Failed to remove a node"),
 			clearErr = new Exception("Failed to clear the HyPeerWeb"),
 			replaceErr = new Exception("Failed to replace a node. Warning! Your HyPeerWeb is corrupted.");
 	
 	/**
 	 * Constructor for initializing the HyPeerWeb
 	 * @param dbName should we sync our HyPeerWeb to a database;
 	 *	Warning! Database access can be very slow (e.g. "HyPeerWeb.sqlite")
 	 * @param seed the random seed number for getting random nodes; use -1
 	 *	to get a pseudo-random seed
 	 * @throws Exception if there was a database error
 	 * @author isaac
 	 */
 	public HyPeerWebSegment(String dbName, long seed) throws Exception{
 		if (dbName != null){
 			db = Database.getInstance(dbName);
 			nodes = db.getAllNodes();
 		}
 		else nodes = new TreeMap();
 		if (seed != -1)
 			rand.setSeed(seed);
 	}
 	
 	// <editor-fold defaultstate="collapsed" desc="REMOVE NODE">
 	/**
 	 * Removes the node of specified webid
 	 * @param webid the webid of the node to remove
 	 * @return the removed node, or null if it doesn't exist
 	 * @throws Exception if it fails to remove the node
 	 * @author isaac
 	 */
 	public T removeNode(int webid) throws Exception{
 		return this.removeNode(getNode(webid));
 	}
 	/**
 	 * Removes the node
 	 * @param n the node to remove
 	 * @return the removed node, or null if it doesn't exist in the HyPeerWeb
 	 * @throws Exception if it fails to remove the node
 	 * @author isaac
 	 */
 	public T removeNode(T n) throws Exception{
 		//Make sure Node exists in HyPeerWeb
 		if (n == null || !nodes.containsValue(n))
 			return null;
 		
 		//special case with 1/2 nodes in HyPeerWeb
 		//There are two special cases:
 		//1) One node
 		if (nodes.size() == 1){
 			removeAllNodes();
 			return n;
 		}
 		//2) Two nodes
 		if (nodes.size() == 2)
 			return removeSecondNode(n);
 		
 		//Find a disconnection point
 		Node replace = getRandomNode().findDisconnectNode().disconnectNode(db);
 		if (replace == null)
 			throw removeNodeErr;
 		//Remove node from list of nodes
 		nodes.remove(replace.getWebId());
 		//Replace the node to be deleted
 		if (!n.equals(replace)){
 			int newWebID = n.getWebId();
 			nodes.remove(newWebID);
 			nodes.put(newWebID, replace);
 			if (!replace.replaceNode(db, n))
 				throw replaceErr;
 		}
 		return n;
 	}
 	/**
 	 * Remove the second to last node
 	 * @return the removed node
 	 * @throws Exception if it fails to modify the database
 	 */
 	private T removeSecondNode(T n) throws Exception{		
 		Node last = n.getNeighbors()[0];
 		//Save the remaining node's attributes
 		HashMap<String, Object> attrs = last.getAllAttributes();
 		removeAllNodes();
 		addFirstNode().setAllAttributes(attrs);
 		return n;
 	}
 	/**
 	 * Removes all nodes from HyPeerWeb
 	 * @author isaac
 	 * @throws Exception if it fails to clear the HyPeerWeb
 	 */
 	public void removeAllNodes() throws Exception{
 		if (db != null && !db.clear())
 			throw clearErr;
 		nodes = new TreeMap<>();
 	}
 	// </editor-fold>
 	
 	// <editor-fold defaultstate="collapsed" desc="ADD NODE">
 	/**
 	 * Adds a new node to the HyPeerWeb
 	 * @return the new node
 	 * @author guy, brian, isaac
 	 * @throws Exception if it fails to add a node
 	 */
 	public T addNode() throws Exception{
 		//There are two special cases:
 		//1) No nodes
 		if (nodes.isEmpty())
 			return addFirstNode();
 		//2) One node
 		if (nodes.size() == 1)
 			return addSecondNode();
 		
 		//Otherwise, use the normal insertion algorithm
 		Node child = getRandomNode().findInsertionNode().addChild(db);
 		if (child == null)
 			throw addNodeErr;
 		//Node successfully added!
 		nodes.put(child.getWebId(), child);
 		return (T) child;
 	}
 	/**
 	 * Special case to handle adding the first node
 	 * @return the new node
 	 * @author isaac
 	 */
 	private T addFirstNode() throws Exception{
 		Node first = new Node(0, 0);
 		if (db != null && !db.addNode(first))
 			throw addNodeErr;
 		nodes.put(0, first);
 		return (T) first;
 	}
 	/**
 	 * Special case to handle adding the second node
 	 * @return the new node
 	 * @author isaac
 	 */
 	private T addSecondNode() throws Exception{
 		Node sec = new Node(1, 1),
 			first = nodes.firstEntry().getValue();
 		//Update the database first
 		if (db != null) {
 			db.beginCommit();
 			db.addNode(sec);
 			db.setHeight(0, 1);
 			//reflexive folds
 			db.setFold(0, 1);
 			db.setFold(1, 0);
 			//reflexive neighbors
 			db.addNeighbor(0, 1);
 			db.addNeighbor(1, 0);
 			if (!db.endCommit())
 				throw addNodeErr;
 		}
 		//Update java struct
 		{
 			first.setHeight(1);
 			first.L.setFold(sec);
 			sec.L.setFold(first);
 			first.L.addNeighbor(sec);
 			sec.L.addNeighbor(first);
 			nodes.put(1, sec);
 			return (T) sec;
 		}
 	}
 	// </editor-fold>
 	
 	/**
 	 * Retrieves a random node in the HyPeerWeb
 	 * @return a random node; null, if there are no nodes
 	 * @author John, Josh
 	 */
 	public T getRandomNode(){
 		//Always start at Node with WebID = 0
 		if (nodes.isEmpty())
 			return null;
 		Node first = nodes.firstEntry().getValue();
 		randVisitor = new SendVisitor(rand.nextInt(Integer.MAX_VALUE), true);
 		randVisitor.visit(first);
 		return (T) randVisitor.getFinalNode();
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="VALIDATION">
 	@Override
 	public T[] getOrderedListOfNodes() {
 		return (T[]) nodes.values().toArray(new Node[nodes.size()]);
 	}
 	/**
 	 * Retrieve a node with the specified webid
 	 * @return the node with the specified webid; otherwise null
 	 * @author isaac
 	 */
 	@Override
 	public T getNode(int webId){
 		return (T) nodes.get(webId);
 	}
 	/**
 	 * Gets the first node in the HyPeerWeb
 	 * @return node with webID = 0
 	 */
 	public T getFirstNode(){
 		if (nodes.isEmpty())
 			return null;
 		return (T) nodes.firstEntry().getValue();
 	}
 	/**
 	 * Gets the last node in the HyPeerWeb
 	 * @return 
 	 */
 	public T getLastNode(){
 		if (nodes.isEmpty())
 			return null;
 		return (T) nodes.lastEntry().getValue();
 	}
 	/**
 	 * Get the size of the HyPeerWeb
 	 * @return the number of nodes in the web
 	 */
 	public int getSize(){
 		return nodes.size();
 	}
 	/**
 	 * Is the HyPeerWeb empty?
 	 * @return true if it is empty
 	 */
 	public boolean isEmpty(){
 		return nodes.isEmpty();
 	}
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         for (Node n : nodes.values())
             builder.append(n);
         return builder.toString();
     }
 	// </editor-fold>
 	
 	private enum HyPeerWebState{
 		//No nodes
 		HAS_NONE {
 			public HyPeerWebState addNode(){
 				//Use a proxy, if the request came from another segment
 				//broadcast state change to HAS_ONE
 				//handle special case
 				return HAS_ONE;
 			}
 			public HyPeerWebState removeNode(Node n){
 				//Throw an error; this shouldn't happen
 				return HAS_NONE;
 			}
 		},
 		//Only one node
 		HAS_ONE {
 			public HyPeerWebState addNode(){
 				//Use a proxy, if the request came from another segment
 				//broadcast state change to HAS_MANY
 				//handle special case
 				return HAS_MANY;
 			}
 			public HyPeerWebState removeNode(){
 				//broadcast state change to HAS_NONE
 				//handle special case
 				return HAS_NONE;
 			}
 		},
 		//More than one node
 		HAS_MANY {
 			public HyPeerWebState addNode(){
 				//Use a proxy, if the request came from another segment
 				return HAS_MANY;
 			}
 			public HyPeerWebState removeNode(TreeMap<Integer, Node> nodes){
 				//If the HyPeerWeb has more than two nodes, remove normally
 				int size = nodes.size();
 				Node last, first = null;
 				if (size > 2 ||
 					//We can get rid of the rest of these checks if we end
 					//up storing proxy nodes in "nodes"
 					//Basically, we're trying to find a node with webID > 1 or height > 1
 					(last = nodes.lastEntry().getValue()).getWebId() > 1 ||
 					//The only nodes left are 0 and 1; check their heights to see if they have children
 					last.getHeight() > 1 ||
 					(size == 2 && nodes.firstEntry().getValue().getHeight() > 1) ||
 					//The only other possibility is if we have one node, with a proxy child
 					(size == 1 && last.L.getHighestLink().getWebId() > 1))
 				{
 					return HAS_MANY;
 				}
 				//If the entire HyPeerWeb has only two nodes
 				else{
 					//handle special case
 					//broadcast state change to HAS_ONE
 					return HAS_ONE;
 				}				
 			}
 		};
 	}
 }
