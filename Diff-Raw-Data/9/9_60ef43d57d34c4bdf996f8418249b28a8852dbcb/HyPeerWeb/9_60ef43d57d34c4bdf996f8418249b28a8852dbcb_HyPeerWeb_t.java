 package hypeerweb;
 
 import hypeerweb.visitors.SendVisitor;
 import java.util.Random;
 import java.util.TreeMap;
 import validator.HyPeerWebInterface;
 
 /**
  * The Great HyPeerWeb Singleton
  * @author isaac
  */
 public class HyPeerWeb implements HyPeerWebInterface {
 	private static HyPeerWeb instance;
 	private static Database db = null;
 	private static TreeMap<Integer, Node> nodes;
 	//Random number generator for getting random nodes
 	private static final Random rand = new Random();
 	private static SendVisitor randVisitor;
 	//Error messages
 	private static final Exception
 			addNodeErr = new Exception("Failed to add a new node"),
 			removeNodeErr = new Exception("Failed to remove a node"),
 			clearErr = new Exception("Failed to clear the HyPeerWeb"),
 			replaceErr = new Exception("Failed to replace a node. Warning! Your HyPeerWeb is corrupted.");
 	//Draw a graph of the HyPeerWeb
 
 	
 	/**
 	 * Private constructor for initializing the HyPeerWeb
 	 * @author isaac
 	 */
 	private HyPeerWeb(boolean useDatabase, boolean useGraph, long seed) throws Exception{
 		nodes = new TreeMap<>();
 		if (seed != -1)
 			rand.setSeed(seed);
 
 	}
 	/**
 	 * Retrieve the HyPeerWeb singleton
 	 * @param useDatabase should we sync our HyPeerWeb to a database;
 	 *	Warning! Database access can be very slow
 	 * @param useGraph is graph drawing enabled {@link #drawGraph(hypeerweb.Node)}
 	 * @param seed the random seed number for getting random nodes; use -1
 	 *	to get a pseudo-random seed
 	 * @return a reference to the initialized HyPeerWeb singleton
 	 * @throws Exception if there was a database error
 	 * @author isaac
 	 */
 	public static HyPeerWeb initialize(boolean useDatabase, boolean useGraph, long seed) throws Exception{
 		if (instance != null)
 			return instance;
 		instance = new HyPeerWeb(useDatabase, useGraph, seed);
 		return instance;
 	}
 	/**
 	 * Retrieves the HyPeerWeb singleton if you know it has
 	 * already been initialized by calling the method:
 	 *		initialize(bool, bool, long)
 	 * @return a previously initialized HyPeerWeb; null, if no HyPeerWeb has been initialized
 	 */
 	public static HyPeerWeb getInstance(){
 		return instance;
 	}
 	
 	/**
 	 * Removes the node of specified webid
 	 * @param webid the webid of the node to remove
 	 * @return the removed node, or null if it doesn't exist
 	 * @throws Exception if it fails to remove the node
 	 * @author isaac
 	 */
 	public Node removeNode(int webid) throws Exception{
 		return this.removeNode(getNode(webid));
 	}
 	/**
 	 * Removes the node
 	 * @param n the node to remove
 	 * @return the removed node, or null if it doesn't exist in the HyPeerWeb
 	 * @throws Exception if it fails to remove the node
 	 * @author isaac
 	 */
 	public Node removeNode(Node n) throws Exception{
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
 	private Node removeSecondNode(Node n) throws Exception{		
 		Node last = n.getNeighbors()[0];
 		//Save the remaining node's attributes
 		removeAllNodes();
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
 	
 	/**
 	 * Adds a new node to the HyPeerWeb
 	 * @return the new node
 	 * @author guy, brian, isaac
 	 * @throws Exception if it fails to add a node
 	 */
 	public Node addNode() throws Exception{
 		//There are two special cases:
 		//1) No nodes
 		if (nodes.isEmpty())
 			return addFirstNode();
 		//2) One node
 		if (nodes.size() == 1)
 			return addSecondNode();
 		
 		//Otherwise, use the normal insertion algorithm
		Node child = getRandomNode().findInsertionNode().addChild(db, new Node(0,0));
 		if (child == null)
 			throw addNodeErr;
 		//Node successfully added!
 		nodes.put(child.getWebId(), child);
 		return child;
 	}
 	/**
 	 * Special case to handle adding the first node
 	 * @return the new node
 	 * @author isaac
 	 */
 	private Node addFirstNode() throws Exception{
 		Node first = new Node(0, 0);
 		if (db != null && !db.addNode(first))
 			throw addNodeErr;
 		nodes.put(0, first);
 		return first;
 	}
 	/**
 	 * Special case to handle adding the second node
 	 * @return the new node
 	 * @author isaac
 	 */
 	private Node addSecondNode() throws Exception{
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
 			return sec;
 		}
 	}
 	
 	/**
 	 * Retrieves a random node in the HyPeerWeb
 	 * @return a random node; null, if there are no nodes
 	 * @author John, Josh
 	 */
 	public Node getRandomNode(){
 		//Always start at Node with WebID = 0
 		if (nodes.isEmpty())
 			return null;
 		return getAllSegmentNodes()[rand.nextInt(nodes.size())];
 	}
 	
 	//VALIDATION
 	@Override
 	public Node[] getAllSegmentNodes() {
 		return nodes.values().toArray(new Node[nodes.size()]);
 	}
 	/**
 	 * Retrieve a node with the specified webid
 	 * @return the node with the specified webid; otherwise null
 	 * @author isaac
 	 */
 	@Override
 	public Node getNode(int webId){
 		return nodes.get(webId);
 	}
 	/**
 	 * Gets the first node in the HyPeerWeb
 	 * @return node with webID = 0
 	 */
 	public Node getFirstNode(){
 		if (nodes.isEmpty())
 			return null;
 		return nodes.firstEntry().getValue();
 	}
 	/**
 	 * Gets the last node in the HyPeerWeb
 	 * @return 
 	 */
 	public Node getLastNode(){
 		if (nodes.isEmpty())
 			return null;
 		return nodes.lastEntry().getValue();
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
 }
