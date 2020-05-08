 
 /** 
  * A node in the HyPeerWeb<br>
  * <br>
  * 
  * <pre>
  * <b>Domain</b>
  *      webid : WedId
  *      connections : Connections
  * </pre>
  * 
  * @author Trevor Bentley, Brian Davis, Matthew
  * 
  * */
 
 package hypeerweb;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class Node extends ProxyableObject implements Comparable<Node>
 {
     /**
      * This represents the state of the node in the cap node finding algorithm.
      * The findCapNode method is called to produce the correct behavior at each step.
      * 
      *   @author Matthew, Brian, Trevor
      */
     public enum State
     {   
         CAP
         {
             @Override
             public Node findCapNode(final Node n)
             {
                 return n;
             }
             public State getInitialStateofChild()
             {
                 return CAP;
             }
             public State getNextState()
             {
                 return STANDARD;
             }
         },
         DOWN
         {
             @Override
             public Node findCapNode(final Node n)
             {
                 if(n.connections.getSurrogateNeighbors().size() > 0)
                 {
                     return n.getHighestSurrogateNeighbor();
                 }
                 else
                 {
                     return n.getHighestNeighbor();
                 }
             }
             public State getInitialStateofChild()
             {
                 return DOWN;
             }
             
             public State getNextState()
             {
                 return STANDARD;
             }
         },
         STANDARD
         {
             @Override
             public Node findCapNode(final Node n)
             {
                 return n.getHighestNeighbor();
             }
             public State getInitialStateofChild()
             {
                 return DOWN;
             }
             
             public State getNextState()
             {
                 return STANDARD;
             }
         };
         
         public abstract Node findCapNode(Node n);
         public abstract State getInitialStateofChild();
         public abstract State getNextState();
     }
     
     /**
      * State represents this node's position (and next action) in the cap node locating algorithm
      */
     private transient State state;
     
     
     /**
      * This node's webId
      */
     private transient WebId webid;
     
     /**
      * This node's contents.
      */
     private transient Contents contents;
     
     /**
      * All of this nodes relations to other nodes (neighbors, folds, surrogates, etc.)
      */
     private transient Connections connections;
     
     public static final Node NULL_NODE = null;
     
     
     public Object writeReplace()
     {
         command.NodeProxy myProx = new command.NodeProxy(getId());
         return myProx;
     }
     
     
     /**
      * Constructor for a new node.
      * It's webId is given by the parameter.
      * @param i
      * @pre none
      * @post This node has the given webId, and is it's own fold.
      */
     public Node(final int i) //Wait, how would we even know what it's webId is supposed to be?
     {
         super();
         webid = new WebId(i);
         connections = new Connections();
         connections.setFold(this);
         contents = new Contents();
         state = State.CAP;
     }
     
     /**
      * Constructor for a new node.
      * @pre none
      * @post This node has a webId of 0, is a CAP node, and is a fold of itself
      */
     public Node()
     {
         super();
         webid = new WebId(0);
         connections = new Connections();
         connections.setFold(this);
         contents = new Contents();
         state = State.CAP;
     }
     
     public Node(int i, command.GlobalObjectId id) //Wait, how would we even know what it's webId is supposed to be?
     {
         super(id);
         webid = new WebId(i);
         connections = new Connections();
         connections.setFold(this);
         contents = new Contents();
         state = State.CAP;
     }
     
     /**
      * Copy constructor
      * This is particularly used to create a local copy of a node proxy
      * @param node
      */
     public Node(Node node)
     {
         super();
         webid = new WebId(node.getWebId());
         connections = new Connections(node.getConnections());
         contents = new Contents(node.getContents());
         state = node.getState();
     }
 
 
     protected State getState()
     {
         return state;
     }
 
 
     /**
      * Gets this node's contents
      * 
      * @pre None
      * @post See return
      * @return This node's contents.
      */
     public Contents getContents()
     {
         return contents;
     }
     
     /**
      * @obvious
      * @return the neighboring node with the largest id
      *     or this node if no such neighbor exists.
      */
     public Node getHighestNeighbor()
     {
         return connections.getHighestNeighbor();
     }
     
     /**
      * @obvious
      * @return the neighboring node with the smallest id
      *     or this node if no such neighbor exists.
      */
     public Node getLowestNeighbor()
     {
         return connections.getLowestNeighbor();
     }
     
     /**
      * @obvious
      * @return the surrogate neighbor node with the largest id
      */
     public Node getHighestSurrogateNeighbor()
     {
         return connections.getHighestSurrogateNeighbor();
     }
     
     /**
      * Returns fully initialized SimplifiedNodeDomain needed for testing
      * 
      * @pre this node is initialized
      * @post no changes to this node
      * @return SimplifiedNodeDomain representing this node
      */
     public synchronized SimplifiedNodeDomain constructSimplifiedNodeDomain() 
     {
         final HashSet<Integer> intNeighbors = new HashSet<Integer>();
         final HashSet<Integer> intSurrogateNeighbors = new HashSet<Integer>();
         final HashSet<Integer> intInverseSurrogateNeighbors = new HashSet<Integer>();
         int tempFold = -1;
         int tempSurrogateFold = -1;
         int tempInverseSurrogateFold = -1;
         
         
         //convert HashSets to integer hash sets using iteration
         Iterator<Node> iter = connections.getNeighbors().iterator();
         while(iter.hasNext())
         {
             final Node temp = iter.next();
            intNeighbors.add(temp.webid.getValue());
         }
         
         iter = connections.getSurrogateNeighbors().iterator();
         while(iter.hasNext())
         {
             final Node temp = iter.next();
            intSurrogateNeighbors.add(temp.webid.getValue());
         }
         
         iter = connections.getInverseSurrogateNeighbors().iterator();
         while(iter.hasNext())
         {
             final Node temp = iter.next();
            intInverseSurrogateNeighbors.add(temp.webid.getValue());
         }
         
         final Node fold = connections.getFold();
         final Node surrogateFold = connections.getSurrogateFold();
         final Node inverseSurrogateFold = connections.getInverseSurrogateFold();
         
         if(fold != NULL_NODE) 
            tempFold = fold.webid.getValue();
         
         if(surrogateFold != NULL_NODE)
            tempSurrogateFold = surrogateFold.webid.getValue();
         
         if(inverseSurrogateFold != NULL_NODE) 
            tempInverseSurrogateFold = inverseSurrogateFold.webid.getValue();
         
         final SimplifiedNodeDomain simpleNode = new SimplifiedNodeDomain( webid.getValue(),
                                                                         getHeight(),
                                                                         intNeighbors,
                                                                         intInverseSurrogateNeighbors, 
                                                                         intSurrogateNeighbors,
                                                                         tempFold, 
                                                                         tempSurrogateFold,
                                                                         tempInverseSurrogateFold);
         return simpleNode;
     }
     
     /**
      * @obvious
      * @param webId 
      */
     public synchronized void setWebId(final WebId webId) 
     {
         webid = webId;
     }
     
     /**
      * @obvious
      * @param state 
      */
     public synchronized void setState(final State state)
     {
         this.state = state;
     }
     
     /**
      * @obvious
      * @param node 
      */
     public synchronized void setFold(final Node node) 
     {
         // if node WebId is fold of this.WebId
         connections.setFold(node);
     }
     
     /**
      * @obvious
      * @param node 
      */
     public synchronized void setSurrogateFold(final Node node) 
     {
         // if node WebId is surrogate fold of this.WebId
         connections.setSurrogateFold(node);
     }
     
     /**
      * @obvious
      * @param node 
      */
     public synchronized void setInverseSurrogateFold(final Node node) 
     {
         connections.setInverseSurrogateFold(node);
     }
     
     /**
      *
      * @param node 
      * @pre none
      * @post If node is not this or null, it is added as a neighbor
      */
     public synchronized void addNeighbor(final Node node) 
     {
         // if WebIds are neighbors - can't add itself as a neighbor
         if(!node.equals(this) && node != null)
             connections.addNeighbor(node);
     }
     
     /**
      * @obvious
      * @param node 
      */
     public synchronized void removeNeighbor(final Node node) 
     {
         if (node != null)
             connections.removeNeighbor(node);
     }
     
     /**
      * @obvious
      * @return int representation of the node's web id
      */
     public int getWebId()
     {
         return webid.getValue();
     }
     
     /**
      * @obvious
      * @return the height of this node
      */
     public int getHeight()
     {
         return connections.getNeighbors().size() + connections.getSurrogateNeighbors().size();
     }
     
     
     /**
      * 
      * @param node0 
      * @pre none
      * @post If node is not this or null, it is added as a inverse surrogate neighbor
      */
     public synchronized void addUpPointer(final Node node0)
     {
         if(!node0.equals(this) && node0 != null)
             connections.addInverseSurrogateNeighbor(node0);
     }
     
     /**
      * @obvious
      * @param node 
      */
     public synchronized void removeUpPointer(final Node node)
     {
         connections.removeInverseSurrogateNeighbor(node);
     }
     
     /**
      * 
      * @param node
      * @pre none
      * @post If node is not this or null, it is added as a surrogate neighbor 
      */
     public synchronized void addDownPointer(final Node node)
     {
         // Cannot add itself as a Down Pointer
         if(!node.equals(this) && node != null)
             connections.addSurrogateNeighbor(node);
     }
     
     /**
      * @obvious
      * @param node 
      */
     public synchronized void removeDownPointer(final Node node)
     {
         connections.removeSurrogateNeighbor(node);
     }
     
     /**
      * @obvious
      * @return a set of the neighboring nodes ids
      */
     public HashSet<Integer> getNeighborsIds()
     {
         return connections.getNeighborsIds();
     }
     
     /**
      * @obvious
      * @return copy of surrogate neighbor set
      */
     public HashSet<Integer> getSurNeighborsIds()
     {
         return connections.getSurrogateNeighborsIds();
     }
     
     /**
      * @obviousNR
      * @return copy of inverse surrogate neighbor set
      */
     public HashSet<Integer> getInvSurNeighborsIds()
     {
         return connections.getInverseSurrogateNeighborsIds();
     }
     
     
     /**
      * insertSelf
      * This method is called on a new node that hasn't been put into the HyPeerWeb yet.
      * 
      * @pre start node is part of a valid HyPeerWeb. This node is not part of the HyPeerWeb.
      * @post This node will be part of the HyPeerWeb and all connections will be modified to match the project constraints
      * @param startNode 
      * @return 
      */
     public synchronized boolean insertSelf(final Node startNode)
     {
         final Node parent = findInsertionPoint(startNode);
        webid = new WebId((int) (parent.webid.getValue() + 
                 Math.pow(2, parent.getHeight())));
         
         // Give child its' connections.
         connections = parent.connections.getChildConnections(parent);
         connections.addNeighbor(parent);
         
         // Update states
         setState(parent.state.getInitialStateofChild());
         parent.setState(parent.state.getNextState());
         
         // Child Notify
         connections.childNotify(this, parent);
         
         // Parent Notify
         parent.connections.parentNotify(parent);
         return true;
     }
     /**
      * findCapNode
      * @param startNode
      * @pre All nodes in the HyPeerWeb are of the correct state and have the correct connections.
      *      startNode is in the HyPeerWeb
      * @post No change to HyPeerWeb
      * @return The cap node of the HyPeerWeb
      */
     public synchronized Node findCapNode(Node startNode)
     {
         Node currentNode = startNode.state.findCapNode(startNode);
         //This loop controls the stepping of the algorithm finding the cap node
         while(!currentNode.equals(startNode))
         {
             startNode = currentNode;
             currentNode = currentNode.state.findCapNode(currentNode);
         }//The cap node is now found (currentNode).
         
         return currentNode;
     }
     /**
      * findInsertionPoint
      * @param startNode 
      * @pre All nodes in the HyPeerWeb are of the correct state and have the correct connections.
      *      startNode is in the HyPeerWeb
      * @post No change to HyPeerWeb.
      * @return The node which is the insertion point
      */
     private synchronized Node findInsertionPoint(Node startNode)
     {
         Node currentNode = startNode.findCapNode(startNode);
         //The cap node is now found (currentNode).
         
         do
         {
             startNode = currentNode;
             currentNode = currentNode.getLowestNeighborWithoutChild();
         }
         while(!currentNode.equals(startNode));
         return currentNode;
     }
     
     /**
      * Remove this node from the HypeerWeb
      * 
      * @pre this is part of a valid HyPeerWeb and the nodes have the correct state.
      * @post The deletionPoint node will be removed from the end of the HyPeerWeb and given this node's connections.
      *       The nodes in this node's connections are updated to point to the just moved deletionPoint node.
      *       The HyPeerWeb still meets all constraints at the end of the removal.
      */
     public synchronized void removeFromHyPeerWeb()
     {
         // find deletionPoint from this point
         final Node deletionPoint = findDeletionPoint(this);
         // Disconnect deletionPont
         deletionPoint.disconnectDeletionPoint();
         // Replace deleted node with deletionPoint node 
         if(getWebId() != deletionPoint.getWebId())
             Connections.replace(this, deletionPoint);
         // Delete node from HyPeerWeb - Garbage collection should take care of it
     }
     
     /**
      * Find the deletion point from startNode
      * @param startNode 
      * @pre All nodes in the HyPeerWeb are of the correct state and have the correct connections.
      *      startNode is in the HyPeerWeb
      * @post No change to HyPeerWeb.
      * @return The node which is the deletion point
      */
     private synchronized Node findDeletionPoint(Node startNode)
     {
         Node currentNode = startNode.state.findCapNode(startNode);
         //This loop controls the stepping of the algorithm finding the cap node
         while(!currentNode.equals(startNode))
         {
             startNode = currentNode;
             currentNode = currentNode.state.findCapNode(currentNode);
         }//The cap node is now found (currentNode).
         
         // Node 0 
         currentNode = currentNode.connections.getFold();
         if(currentNode.getLowestNeighbor() != Node.NULL_NODE)
             currentNode = currentNode.getLowestNeighbor();
         
         // Loop till you get to the deletionPoint - currentNode will then be set to its highest
         // neighbor which will have a WebID less than the deletion point's WebID.
         do
         {
             startNode = currentNode;
             currentNode = currentNode.getHighestNeighbor();
             if(currentNode == Node.NULL_NODE)
                 currentNode = startNode;
         }
         while(currentNode.getWebId() > startNode.getWebId()); 
         return startNode;
     }
     
     /**
      * Disconnect the deletion point from the HypeerWeb
      * 
      * Called from the deletionPoint node
      */
     private synchronized void disconnectDeletionPoint()
     {
         Connections.disconnect(this);
     }
     
     /**
      * @pre This is part of a valid hypeerweb.
      * @post No change to hypeerweb
      * @return
      *  If this has no neighbors, it returns itself
      *  If this has at least one neighbor without any children, it returns the lowest of them
      *  If this has no neighbors without children, it returns itself
      */
     public Node getLowestNeighborWithoutChild()
     {
         final Node temp = connections.getLowestNeighborWithoutChild();
         if(temp == NULL_NODE)
         {
             return this;
         }
         else if(temp.getHeight() < getHeight())
         {
             return temp;
         }
         else if(temp.compareTo(this) < 0 && temp.getHeight() == getHeight())
         {
             return temp;
         }
         else
         {
             return this;
         }
     }
     
     /**
      * @obvious
      * @return the fold's id
      */
     public int getFoldId()
     {
         return connections.getFoldId() ;
     }
     
     /**
      * @obvious
      * @return the surrogate fold's id
      */
     public int getSurrogateFoldId()
     {
         return connections.getSurrogateFoldId();
     }
     
     /**
      * @obvious
      * @return the inverse surrogate fold's id
      */
     public int getInverseSurrogateFoldId()
     {
         return connections.getInverseSurrogateFoldId();
     }
     
     /**
      * The accept method for the visitor Pattern.
      * 
      * @param visitor 
      * @param parameters 
      * @pre visitor ≠ null AND parameters ≠ null AND visitor.visit(this, parameters).pre-condition
      * @post visitor.visit(this, parameters).post-condition
      */
     public void accept(final Visitor visitor, final Parameters parameters)
     {
         visitor.visit(this, parameters);
     }
 
    /*@Override
    public int compareTo(final Object o)
    {
        if (o.getClass() == this.getClass())
            return webid.compareTo(((Node) o).webid);
        return -1;
    }*/
     
     public int compareTo(Node o)
     {
         if (o.getClass() == this.getClass())
            return webid.compareTo(((Node) o).webid);
         return -1;
     }
     
     public boolean equals(final Object o)
     {
        return (o.getClass() == this.getClass()) && webid.equals(((Node) o).webid);
     }
     
     /**
      * 
      * @return 
      */
     public Node getFold()
     {
         final Node fold = connections.getFold();
         return fold;
     }
     
     /**
      * This should only be called in the Node class and Connections class. If you use it somewhere else, 
      * be sure there will be no serializing/proxy issues. You shouldn't need to though. 
      * (other than test cases)
      * 
      * @return 
      */
     public Connections getConnections()
     {
         return connections;
     }
     
     /**
      * This should only be called in the Node class and Connections class. If you use it somewhere else, 
      * be sure there will be no serializing/proxy issues. You shouldn't need to though.
      * (other than test cases)
      * 
      * @param newconnections
      */
     public synchronized void setConnections(final Connections newconnections)
     {
         connections = newconnections;
     }
     
     public void notifyAllConnectionsOfChangeInId()
     {
         connections.notifyAllConnectionsOfChangeInId(this);
     }
 
     /**
      * Returns the nieghbors of this node
      * @obvious
      * @return
      */
     public Set<Node> getNeighbors()
     {
         return connections.getNeighbors();
     }
 
     /**
      * @obvious
      * @return
      */
     public Node getSurrogateFold()
     {
         return connections.getSurrogateFold();
     }
 
     /**
      * @obvious
      * @return
      */
     public Node getInverseSurrogateFold()
     {
         return connections.getInverseSurrogateFold();
     }
 
     /**
      * @obvious
      * @return
      */
     public Set<Node> getSurrogateNeighbors()
     {
         return connections.getSurrogateNeighbors();
     }
 
     /**
      * @obvious
      * @return
      */
     public Set<Node> getInverseSurrogateNeighbors()
     {
         return connections.getInverseSurrogateNeighbors();
     }
 }
