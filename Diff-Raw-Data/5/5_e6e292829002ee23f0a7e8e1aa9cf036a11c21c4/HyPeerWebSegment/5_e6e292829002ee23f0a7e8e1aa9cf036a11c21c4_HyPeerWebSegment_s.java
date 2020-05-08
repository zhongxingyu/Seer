 package hypeerweb;
 
 import chat.ChatServer;
 import communicator.LocalObjectId;
 import hypeerweb.visitors.SendVisitor;
 import hypeerweb.visitors.BroadcastVisitor;
 import java.util.ArrayList;
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
 	private ChatServer chatServer;
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
 			replaceErr = new Exception("Failed to replace a node. Warning! Your HyPeerWeb is corrupted."),
 			corruptErr = new Exception("The HyPeerWeb is corrupt! Cannot proceed.");
 	
 	private static ArrayList<HyPeerWebSegment> segmentList = new ArrayList();
 	
 	/**
 	 * Constructor for initializing the HyPeerWeb with default Node values
 	 * @param dbName should we sync our HyPeerWeb to a database;
 	 *	Warning! Database access can be very slow (e.g. "HyPeerWeb.sqlite")
 	 * @param seed the random seed number for getting random nodes; use -1
 	 *	to get a pseudo-random seed
 	 * @param cs the ChatServer that this segment belongs to
 	 * @throws Exception if there was a database error
 	 * @author isaac
 	 */
 	public HyPeerWebSegment(String dbName, long seed, ChatServer cs) throws Exception{
 		this(dbName, seed, 0, 0, cs);
 	}
 	/**
 	 * Constructor for initializing the HyPeerWeb with defined Node values
 	 * @param dbName should we sync our HyPeerWeb to a database;
 	 *	Warning! Database access can be very slow (e.g. "HyPeerWeb.sqlite")
 	 * @param seed the random seed number for getting random nodes; use -1
 	 *	to get a pseudo-random seed
 	 * @param webID the node webID, if it has one
 	 * @param height the node height, if it has one
 	 * @param cs the ChatServer that this segment belongs to
 	 * @throws Exception if there was a database error
 	 * @author isaac
 	 */
 	public HyPeerWebSegment(String dbName, long seed, int webID, int height, ChatServer cs) throws Exception{
 		super(0, 0);
 		if (dbName != null){
 			db = Database.getInstance(dbName);
 			nodes = db.getAllNodes();
 		}
 		else nodes = new TreeMap();
 		if (seed != -1)
 			rand.setSeed(seed);
 		chatServer = cs;
 		segmentList.add(this);
 	}
 	
 	/**
 	 * Removes the node of specified webid
 	 * @param webid the webid of the node to remove
 	 * @return the removed node, or null if it doesn't exist
 	 * @throws Exception if it fails to remove the node
 	 */
 	public T removeNode(int webid) throws Exception{
 		return removeNode(getNode(webid));
 	}
 	/**
 	 * Removes the node
 	 * @param n the node to remove
 	 * @return the removed node, or null if it doesn't exist in the HyPeerWeb
 	 * @throws Exception if it fails to remove the node
 	 */
 	public T removeNode(T n) throws Exception{
 		//Make sure Node exists in HyPeerWeb
 		//TODO, make this here work for more stuff
 		if (n == null || !nodes.containsValue(n))
 			return null;
 		state.removeNode(this, n);
 		return n;
 	}
 	/**
 	 * Removes all nodes from HyPeerWeb
 	 * @throws Exception if it fails to clear the HyPeerWeb
 	 */
 	public void removeAllNodes(){
 		(new BroadcastVisitor(){
 			@Override
 			public void performOperation(Node n) {
 				HyPeerWebSegment seg = (HyPeerWebSegment) n;
 				//If we can't remove all nodes, HyPeerWeb is corrupt
 				if (seg.db != null && !seg.db.clear())
 					changeState(HyPeerWebState.CORRUPT);
 				seg.nodes = new TreeMap<>();
 			}
 		}).visit(this);
 	}
 	
 	/**
 	 * Adds a new node to the HyPeerWeb
 	 * @return the new node
 	 * @throws Exception if it fails to add a node
 	 */
 	public void addNode(Node.Listener listener) throws Exception{
 		getNonemptySegment().state.addNode(this, listener);
 	}
 	/**
 	 * 
 	 * @param node
 	 * @return 
 	 */
 	public void deleteNode(Node node, Node.Listener listener){
 		//todo something here
 		getNonemptySegment().state.removeNode(this, node, listener);
 	}
 	/**
 	 * Holds the state of the entire HyPeerWeb, not just
 	 * this individual segment. Handles special cases for
 	 * add and remove node, as well as a corrupt HyPeerWeb.
 	 */
 	private enum HyPeerWebState{
 		//No nodes
 		HAS_NONE {
 			@Override
 			public void addNode(final HyPeerWebSegment web, Node.Listener listener){
 				//Use a proxy, if the request came from another segment
 				Node first = new Node(0, 0);
 				if (web.db != null && !web.db.addNode(first))
 					web.changeState(CORRUPT);
 				else{
 					web.nodes.put(0, first);
 					//broadcast state change to HAS_ONE
 					web.changeState(HAS_ONE);
 					listener.callback(first);
 				}
 			}
 			@Override
 			public void removeNode(final HyPeerWebSegment web, Node n, Node.Listener listener){
 				//Throw an error; this shouldn't happen
 				web.changeState(CORRUPT);
 			}
 		},
 		//Only one node
 		HAS_ONE {
 			@Override
 			public void addNode(final HyPeerWebSegment web, Node.Listener listener){
 				//Use a proxy, if the request came from another segment
 				//broadcast state change to HAS_MANY
 				//handle special case
 				Node sec = new Node(1, 1),
 					first = (Node) web.nodes.firstEntry().getValue();
 				//Update the database first
 				if (web.db != null) {
 					web.db.beginCommit();
 					web.db.addNode(sec);
 					web.db.setHeight(0, 1);
 					//reflexive folds
 					web.db.setFold(0, 1);
 					web.db.setFold(1, 0);
 					//reflexive neighbors
 					web.db.addNeighbor(0, 1);
 					web.db.addNeighbor(1, 0);
 					if (!web.db.endCommit())
 						web.changeState(CORRUPT);
 				}
 				//Update java struct
 				{
 					first.setHeight(1);
 					first.L.setFold(sec);
 					sec.L.setFold(first);
 					first.L.addNeighbor(sec);
 					sec.L.addNeighbor(first);
 					web.nodes.put(1, sec);
 					web.changeState(HAS_MANY);
 					listener.callback(sec);					
 				}
 			}
 			@Override
 			public void removeNode(final HyPeerWebSegment web, Node n, Node.Listener listener){
 				//broadcast state change to HAS_NONE
 				//handle special case
 				if (web.db != null && !web.db.clear())
 					web.changeState(CORRUPT);
 				else{
 					web.nodes = new TreeMap<>();
 					web.changeState(HAS_NONE);
 					listener.callback(n);
 				}
 			}
 		},
 		//More than one node
 		HAS_MANY {
 			@Override
 			public void addNode(final HyPeerWebSegment web, final Node.Listener listener){
 				//Use a proxy, if the request came from another segment
 				web.getRandomNode(new Node.Listener() {
 					@Override
 					public void callback(Node n) {
 						Node child = n.findInsertionNode().addChild(web.db, new Node(0,0));
 						if (child == null)
 							web.changeState(CORRUPT);
 						else{
 							//Node successfully added!
 							//TODO: might need to change this here
 							web.nodes.put(child.getWebId(), child);
 							listener.callback(child);
 						}
 					}
 				});
 			}
 			@Override
 			public void removeNode(final HyPeerWebSegment web, Node n, final Node.Listener listener){
 				//If the HyPeerWeb has more than two nodes, remove normally
 				int size = web.nodes.size();
 				Node last, first = null;
 				if (size > 2 ||
 					//We can get rid of the rest of these checks if we end
 					//up storing proxy nodes in "nodes"
 					//Basically, we're trying to find a node with webID > 1 or height > 1
 					(last = (Node) web.nodes.lastEntry().getValue()).getWebId() > 1 ||
 					//The only nodes left are 0 and 1; check their heights to see if they have children
 					last.getHeight() > 1 ||
 					(size == 2 && ((Node) web.nodes.firstEntry().getValue()).getHeight() > 1) ||
 					//The only other possibility is if we have one node, with a proxy child
 					(size == 1 && last.L.getHighestLink().getWebId() > 1))
 				{
 					//Find a disconnection point
 					web.getRandomNode(new Node.Listener(){
 						@Override
 						public void callback(Node n) {
 							Node replace = n.findDisconnectNode().disconnectNode(web.db);
 							if (replace == null)
 								web.changeState(CORRUPT);
 							//Remove node from list of nodes
 							web.nodes.remove(replace.getWebId());
 							//Replace the node to be deleted
 							if (!n.equals(replace)){
 								int newWebID = n.getWebId();
 								web.nodes.remove(newWebID);
 								web.nodes.put(newWebID, replace);
 								if (!replace.replaceNode(web.db, n))
 									web.changeState(CORRUPT);
 							}
 							web.changeState(HAS_MANY);
 							listener.callback(n);
 						}
 					});
 				}
 				//If the broadcastStateChangeentire HyPeerWeb has only two nodes
 				else{
 					//removing node 0
 					if(n.getWebId() == 0){
 						Node replace = n.getFold(); //gets node 1
 						if (replace == null)
							throw removeNodeErr;
 						//Remove node from list of nodes
 						web.nodes.remove(0);
 						//Replace the node to be deleted
 						replace.L.removeNeighbor(n);
 						replace.L.setFold(null);
 						replace.setWebID(0);
 						replace.setHeight(0);
 					}
 					//removing node 1
 					else{
 						Node other = n.getFold();
 						if (other == null)
							throw removeNodeErr;
 						web.nodes.remove(1);
 						other.L.removeNeighbor(n);
 						other.L.setFold(null);
 						other.setHeight(0);
 					}
 					web.changeState(HAS_ONE);
 				}				
 			}
 		},
 		//Network is corrupt; a segment failed to perform an operation
 		CORRUPT {
 			@Override
 			public void addNode(HyPeerWebSegment web, Node.Listener listener) throws Exception {
 				throw corruptErr;
 			}
 			@Override
 			public void removeNode(HyPeerWebSegment web, Node n, Node.Listener listener) throws Exception {
 				throw corruptErr;
 			}
 		};
 		public abstract void addNode(final HyPeerWebSegment web, Node.Listener listener) throws Exception;
 		public abstract void removeNode(final HyPeerWebSegment web, Node n, Node.Listener listener) throws Exception;
 	}
 	/**
 	 * Change the state of the HyPeerWeb
 	 * @param state the new state
 	 */
 	private void changeState(final HyPeerWebState state){
 		(new BroadcastVisitor(){
 			@Override
 			public void performOperation(Node n) {
 				((HyPeerWebSegment) n).state = state;
 			}
 		}).visit(this);
 	}
 	
 	// <editor-fold defaultstate="collapsed" desc="SEGMENT GETTERS">
 	@Override
 	public Node[] getAllSegmentNodes() {
 		return (Node[]) nodes.values().toArray();
 	}
 	public TreeMap<Integer, Node> getTreeMapOfAllSegmentNodes(){
 		return nodes;
 	}
 	public NodeCache getNodeCache(int networkID){
 		NodeCache c = new NodeCache();
 		for (Node n: nodes.values())
 			c.addNode(n, false);
 		return c;
 	}
 	/**
 	 * Gets the first node in the HyPeerWeb
 	 * @return node with webID = 0
 	 */
 	public T getFirstSegmentNode(){
 		if (nodes.isEmpty())
 			return null;
 		return (T) nodes.firstEntry().getValue();
 	}
 	/**
 	 * Gets the last node in the HyPeerWeb
 	 * @return 
 	 */
 	public T getLastSegmentNode(){
 		if (nodes.isEmpty())
 			return null;
 		return (T) nodes.lastEntry().getValue();
 	}
 	/**
 	 * Get the size of the HyPeerWeb
 	 * @return the number of nodes in the web
 	 */
 	public int getSegmentSize(){
 		return nodes.size();
 	}
 	/**
 	 * Is the HyPeerWeb empty?
 	 * @return true if it is empty
 	 */
 	public boolean isSegmentEmpty(){
 		return nodes.isEmpty();
 	}
 	/**
 	 * Looks for a HyPeerWebSegment that is not empty
 	 * @return the segment found
 	 */
 	public HyPeerWebSegment getNonemptySegment(){
 		if (!isSegmentEmpty())
 				return this;
 		else
 			for (Node neighbor: L.getNeighbors())
 			return ((HyPeerWebSegment)neighbor).getNonemptySegment();
 		//For Add Node method. If no segments are nonempty, 
 		//this segment is as good a place to start as any.
 		return this;
 	}
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         for (Node n : nodes.values())
             builder.append(n);
         return builder.toString();
     }
 	/**
 	 * gets the ChatServer that this node is a part of
 	 * @return 
 	 */
 	public ChatServer getChatServer(){
 		return chatServer;
 	}
 	
 	public T getNode(int webId, LocalObjectId id) {
 		Node node = getNode(webId);
 		if(node.getLocalObjectId().equals(id))
 			return (T) node;
 		return null;
 	}
 	// </editor-fold>
 	
 	// <editor-fold defaultstate="collapsed" desc="HYPEERWEB GETTERS">
 	/**
 	 * Retrieves a random node in the HyPeerWeb
 	 * @return a random node; null, if there are no nodes
 	 */
 	public void getRandomNode(Node.Listener listener){
 		//Always start at Node with WebID = 0
 		if (state == HyPeerWebState.HAS_NONE)
 			listener.callback(null);
 		
 		Node first = (Node) getNonemptySegment().nodes.firstEntry().getValue();
 		randVisitor = new SendVisitor(rand.nextInt(Integer.MAX_VALUE), true, listener);
 		randVisitor.visit(first);
 	}
 	/**
 	 * Get a list of all the nodes in the HyPeerWeb
 	 * @return an array of nodes
 	 */
 	public void getAllNodes(GetAllNodesListener listener) {
 		GetAllNodesVisitor visitor = new GetAllNodesVisitor(listener);
 	}
 	
 	/**
 	 * Retrieve a node with the specified webid
 	 * @return the node with the specified webid; otherwise null
 	 * @author isaac
 	 */
 	@Override
 	public T getNode(int webId){
 		//TODO, use sendvisitor to get actual node
 		return (T) getNonemptySegment().nodes.get(webId);
 	}
 	/**
 	 * Is the HyPeerWeb empty?
 	 * @return true if it is empty
 	 */
 	public boolean isEmpty(){
 		return state == HyPeerWebState.HAS_NONE;
 	}
 	
 	private class GetAllNodesVisitor extends BroadcastVisitor{
 		GetAllNodesListener l;
 		public GetAllNodesVisitor(GetAllNodesListener listener){
 			super();
 			l = listener;
 		}
 		@Override
 		public void performOperation(Node n) {
 			l.callback(((HyPeerWebSegment) n).);
 		}
 	}
 
 	public abstract class GetAllNodesListener{
 			public abstract void callback(NodeCache cache);
 		}
 
 	// </editor-fold>
 }
