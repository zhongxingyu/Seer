 package cytoscape.giny;
 
 import java.util.*;
 
 import giny.model.RootGraph;
 import giny.model.GraphPerspective;
 
 import coltginy.ColtGraphPerspective;
 import cern.colt.map.*;
 
 import cytoscape.*;
 import cytoscape.giny.*;
 
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.data.ExpressionData;
 
 /**
  * ColtCyNetwork extends the GraphPerspective implementation found 
  * in the coltginy pacakge of the GINY distribution.
  *
  * ColtCyNetwork provides an implementation of the CyNetwork interface,
  * as well as the GraphPerspective inteface, and also provides the 
  * functionality formally provided by GraphObjAttributes.
  *
  * The Network will notify listeners when nodes/edges are added/removed 
  * and when relavant data changes.
  */
 public class ColtCyNetwork 
   extends ColtGraphPerspective 
   implements CyNetwork {
     
 
   public static int uid_counter = 0;
 
   protected String identifier;
   protected String title;
    
   /**
    * The Network Listeners Set 
    */
   //TODO: implement the bean accepted way
   protected Set listeners = new HashSet();
 
   /**
    * The ClientData map
    */
   protected Map clientData;
 
   //TODO: remove
   int activityCount = 0;
 
   //----------------------------------------//
   // Constructors
   //----------------------------------------//
 
   /**
    * Default constructor delegates to the RootGraph, OpenIntIntHashMap,
    * OpenIntIntHashMap constructor with null arguments.
    */
   public ColtCyNetwork () {
     super( Cytoscape.getRootGraph(),
            new OpenIntIntHashMap(),
            new OpenIntIntHashMap() 
            );
     initialize();
   } // <init>()
 
   /**
    * Creates a new CyNetwork with the given mappings
    */
   public ColtCyNetwork ( RootGraph root_graph,
                          OpenIntIntHashMap r_node_i_to_p_node_i_map,
                          OpenIntIntHashMap r_edge_i_to_p_edge_i_map ) {
     super( root_graph, 
            r_node_i_to_p_node_i_map,
            r_edge_i_to_p_edge_i_map );
     initialize();
   }
 
   protected void initialize () {
 
     // TODO: get a better naming system in place
     Integer i = new Integer( uid_counter );
     identifier = i.toString();
     uid_counter++;
 
     clientData = new HashMap();
   }
 
   /**
    * Can Change
    */
   public String getTitle () {
     if ( title == null ) 
       return identifier;
     return title;
   }
 
   
   /**
    * Can Change
    */
   public void setTitle ( String new_id ) {
     title = new_id;
   }
 
   public String getIdentifier () {
     return identifier;
   }
 
   public String setIdentifier ( String new_id ) {
     identifier = new_id;
     return identifier;
   }
 
 
   //------------------------------//
   // Client Data
   //------------------------------//
 
   /**
    * Networks can support client data.
    * @param data_name the name of this client data
    */
   public void putClientData ( String data_name, Object data ) {
     clientData.put( data_name, data );
   }
 
   /**
    * Get a list of all currently available ClientData objects
    */
   public Collection getClientDataNames () {
     return clientData.keySet();
   }
   
   /**
    * Get Some client data
    * @param data_name the data to get
    */
   public Object getClientData ( String data_name ) {
     return clientData.get( data_name );
   }
   
 
   //------------------------------//
   // Deperecation
   //------------------------------//
  /**
    * @deprecated
    * This method should be called before reading or changing the data held
    * in this network object. A CyNetworkEvent of type CyNetworkEvent.BEGIN
    * will be fired to all listeners attached to this object, *only* if this
    * is the first begin of a nested stack of begin/end methods. No event
    * will be fired if a previous beginActivity call hasn't been closed by
    * a matching endActivity call.<P>
    *
    * The argument is simply a String that is useful for identifying the
    * caller of this method. This is provided for debugging purposes, in case
    * an algorithm forgets to provide a matching end method for each begin.
    */
   public void beginActivity(String callerID) {
     activityCount++;
     if (activityCount == 1) {fireEvent(CyNetworkEvent.BEGIN);}
   }
     
   /**
    * @deprecated
    * This method should be called when an algorithm is finished reading
    * or changing the data held in this network object. A CyNetworkEvent
    * of type CyNetworkEvent.END will be fired to listeners attached to
    * this object, *only* if this is the last end in a nested block of
    * begin/end calls.<P>
    *
    * The argument is a String for identifying the caller of this method.
    */
   public void endActivity(String callerID) {
     if (activityCount == 0) {return;} //discard calls without a matching begin
     activityCount--;
     if (activityCount == 0) {fireEvent(CyNetworkEvent.END);}
   }
     
   /**
    * @deprecated
    * This method returns true if the current state of this object is clear;
    * that is, if every beginActivity call has been followed by a matching
    * endActivity call, so that one can reasonably assume that no one is
    * currently working with the network.
    */
   public boolean isStateClear() {return (activityCount == 0);}
     
   /**
    * @deprecated
    * This method is provided as a failsafe in case an algorithm fails to
    * close its beginActivity calls without matching endActivity calls. If
    * the current state is not clear, this method resets this object to the
    * state of no activity and fires a CyNetworkEvent of type
    * CyNetworkEvent.END to all registered listeners.<P>
    *
    * If the current state is clear (i.e., there are no calls to beginActivity
    * without matching endActivity calls), then this method does nothing.<P>
    *
    * The argument is a String for identifying the caller of this method.
    */
   public void forceClear(String callerID) {
     if (activityCount > 0) {
       activityCount = 0;
       fireEvent(CyNetworkEvent.END);
     }
   }
   /**
    * @deprecated
    * use @link{Cytoscape.getRootGraph()} instead
    */
   public RootGraph getRootGraph() {
     // delegate to the Central CytoscapeRootGraph
     return Cytoscape.getRootGraph();
   }
   
   /**
    * @deprecated
    * This <b>is a</b> GraphPerspective now! Therefore treat it as such.
    * This method will not be changed and will simply return itself, 
    * recasted as a GraphPerspective
    */
   public GraphPerspective getGraphPerspective() {
     return ( GraphPerspective )this;
   }
 
   /**
    * A new Network should be made instead.  
    * @see #appendNetwork 
    * @deprecated
    */
   public void setGraphPerspective( GraphPerspective perspective ) {
    
     // hide the current nodes 
     hideNodes( getNodeIndicesArray() );
     // hide the current edges
     hideEdges( getEdgeIndicesArray() );
 
     // restore the new nodes and edges
     restoreNodes( perspective.getNodeIndicesArray() );
     restoreEdges( perspective.getEdgeIndicesArray() );
     
     fireEvent(CyNetworkEvent.GRAPH_REPLACED);
   }
 
   /**
    * A new Network should be made instead.  
    * @see #appendNetwork 
    * @deprecated
    */
   public void setNewGraphFrom(CyNetwork newNetwork, boolean replaceAttributes) {
   
     // this will call the GRAPH_REPLACED event as well
     setGraphPerspective( newNetwork );
   }
 
   /**
    * Appends all of the nodes and edges in teh given Network to 
    * this Network
    */
   public void appendNetwork ( CyNetwork network ) {
     int[] nodes = network.getNodeIndicesArray();
     int[] edges = network.getEdgeIndicesArray();
     restoreNodes( nodes );
     restoreEdges( edges );
   }
     
   /**
    * @deprecated
    */
   public boolean getNeedsLayout () {
     return false;
   }
   
   /**
    * @deprecated
    */
   public void setNeedsLayout ( boolean needsLayout ) {
   }
     
    /**
    *@deprecated
    * Returns the node attributes data object for this network.
    */
   public GraphObjAttributes getNodeAttributes () {
     return ( GraphObjAttributes )Cytoscape.getNodeNetworkData();
   }
   
   /**
    * @deprecated
    * does nothing, all attributes are shared right now
    */
   public void setNodeAttributes ( GraphObjAttributes newNodeAttributes ) {
   }
 
   /**
    * @deprecated @see{getNetworkData}
    * Returns the edge attributes data object for this network.
    */
   public GraphObjAttributes getEdgeAttributes () {
     return ( GraphObjAttributes )Cytoscape.getEdgeNetworkData();
   }
   
   /**
    * @deprecated
    * does nothing, all attributes are shared right now
    */
   public void setEdgeAttributes ( GraphObjAttributes newEdgeAttributes ) {
   }
 
   /**
    * @deprecated @see{getNetworkData}
    * Returns the expression data object associated with this network.
    */
   public ExpressionData getExpressionData () {
     return Cytoscape.getExpressionData();
   }
   
   /**
    * @deprecated
    * Sets the expression data object associated with this network.
    */
   public void setExpressionData ( ExpressionData newData ) {
     //null?
     // use Cytoscape.loadExpressionData instead
   }
 
   
    //----------------------------------------//
   // Data Access Methods
   //----------------------------------------//
 
   //--------------------//
   // Member Data
 
   // get
   
   /**
    * Return the requested Attribute for the given Node
    * @param node the given CyNode
    * @param attribute the name of the requested attribute
    * @return the value for the give node, for the given attribute
    */
   public Object getNodeAttributeValue ( CyNode node, String attribute ) {
     return Cytoscape.getNodeNetworkData().get( attribute, 
                                                Cytoscape.getNodeNetworkData().getCanonicalName( node ) );
   }
 
   /**
    * Return the requested Attribute for the given Node
    */
   public Object getNodeAttributeValue ( int node, String attribute ) {
     return Cytoscape.getNodeNetworkData().get( attribute, 
                                                Cytoscape.getNodeNetworkData().getCanonicalName( getNode( node ) ) );
   }
 
   /**
    * Return the requested Attribute for the given Edge
    */
   public Object getEdgeAttributeValue ( CyEdge edge, String attribute ) {
    return Cytoscape.getEdgeNetworkData().get( attribute, 
                                                Cytoscape.getEdgeNetworkData().getCanonicalName( edge ) );
   }
 
   /**
    * Return the requested Attribute for the given Edge
    */
   public Object getEdgeAttributeValue ( int edge, String attribute ) {
    return Cytoscape.getEdgeNetworkData().get( attribute, 
                                                Cytoscape.getEdgeNetworkData().getCanonicalName( getEdge( edge ) ) );
   }
 
   /**
    * Return all availble Attributes for the Nodes in this CyNetwork
    */
   public String[] getNodeAttributesList () {
     return Cytoscape.getNodeNetworkData().getAttributeNames();
   }
   
   /**
    * Return all available Attributes for the given Nodes
    */
   public String[] getNodeAttributesList ( CyNode[] nodes ) {
     return Cytoscape.getNodeNetworkData().getAttributeNames();
   }
 
   /**
    * Return all availble Attributes for the Edges in this CyNetwork
    */
   public String[] getEdgeAttributesList () {
     return Cytoscape.getEdgeNetworkData().getAttributeNames();
   }
 
   /**
    * Return all available Attributes for the given Edges
    */
   public String[] getNodeAttributesList ( CyEdge[] edges ) {
     return Cytoscape.getEdgeNetworkData().getAttributeNames();
   }
 
 
    /**
    * Return the requested Attribute for the given Node
    * @param node the given CyNode
    * @param attribute the name of the requested attribute
    * @param value the value to be set
    * @return if it overwrites a previous value
    */
   public boolean setNodeAttributeValue ( CyNode node, String attribute, Object value ) {
     return Cytoscape.getNodeNetworkData().set( attribute, 
                                                Cytoscape.
                                                getNodeNetworkData().
                                                getCanonicalName( node ),
                                                value );
     
 
   }
 
   /**
    * Return the requested Attribute for the given Node
    */
   public boolean setNodeAttributeValue ( int node, String attribute, Object value ) {
     return Cytoscape.getNodeNetworkData().set( attribute, 
                                                Cytoscape.
                                                getNodeNetworkData().
                                                getCanonicalName( getNode(node) ),
                                                value );
     
 
   }
 
   /**
    * Return the requested Attribute for the given Edge
    */
   public boolean setEdgeAttributeValue ( CyEdge edge, String attribute, Object value ) {
     return Cytoscape.getEdgeNetworkData().set( attribute, 
                                                Cytoscape.
                                                getEdgeNetworkData().
                                                getCanonicalName( edge ),
                                                value );
   }
 
   /**
    * Return the requested Attribute for the given Edge
    */
   public boolean setEdgeAttributeValue ( int edge, String attribute, Object value ) {
     return Cytoscape.getEdgeNetworkData().set( attribute, 
                                                Cytoscape.
                                                getEdgeNetworkData().
                                                getCanonicalName( getEdge(edge) ),
                                                value );
     
 
   }
  
 
 
 
 
   //------------------------------//
   // Listener Methods
   //------------------------------//
   
     
   /**
    * Registers the argument as a listener to this object. Does nothing if
    * the argument is already a listener.
    */
   public void addCyNetworkListener ( CyNetworkListener listener ) {
     listeners.add(listener);
   }
 
   /**
    * Removes the argument from the set of listeners for this object. Returns
    * true if the argument was a listener before this call, false otherwise.
    */
   public boolean removeCyNetworkListener ( CyNetworkListener listener ) {
     return listeners.remove(listener);
   }
 
   /**
    * Returns the set of listeners registered with this object.
    */
   public Set getCyNetworkListeners () {
     return new HashSet(listeners);
   }
     
   //--------------------//
   // Event Firing
   //--------------------//
   
 
   /**
    * Fires an event to all listeners registered with this object. The argument
    * should be a constant from the CyNetworkEvent class identifying the type
    * of the event.
    */
   protected void fireEvent(int type) {
     CyNetworkEvent event = new CyNetworkEvent(this, type);
     for (Iterator i = listeners.iterator(); i.hasNext(); ) {
       CyNetworkListener listener = (CyNetworkListener)i.next();
       listener.onCyNetworkEvent(event);
     }
   }
 
   //----------------------------------------//
   // Implements Network
   //----------------------------------------//
   
   //----------------------------------------//
   // Node and Edge creation/deletion
   //----------------------------------------//
 
   //--------------------//
   // Nodes
 
   /**
    * This method will create a new node.
    * @return the Cytoscape index of the created node 
    */
   public int createNode () {
     return restoreNode(  Cytoscape.getRootGraph().createNode() );
   }
 
   /**
    * Add a node to this Network that already exists in 
    * Cytoscape
    * @return the Network Index of this node
    */
   public int addNode ( int cytoscape_node ) {
     return restoreNode( cytoscape_node );
   }
 
   /**
    * Add a node to this Network that already exists in 
    * Cytoscape
    * @return the Network Index of this node
    */
   public CyNode addNode ( CyNode cytoscape_node ) {
     return ( CyNode )restoreNode( cytoscape_node);
   }
  
   /**
    * Adds a node to this Network, by looking it up via the 
    * given attribute and value
    * @return the Network Index of this node
    */
   public int addNode ( String attribute, Object value ) {
     return 0;
   }
 
   /**
    * This will remove this node from the Network. However,
    * unless forced, it will remain in Cytoscape to be possibly
    * resused by another Network in the future.
    * @param force force this node to be removed from all Networks
    * @return true if the node is still present in Cytoscape 
    *          ( i.e. in another Network )
    */
   public boolean removeNode ( int node_index, boolean force ) {
     hideNode( node_index );
     return true;
   }
 
   //--------------------//
   // Edges
 
   /**
    * This method will create a new edge.
    * @param source the source node
    * @param target the target node
    * @param directed weather the edge should be directed
    * @return the Cytoscape index of the created edge 
    */
   public int createEdge ( int source, int target, boolean directed ) {
     return restoreEdge( Cytoscape.getRootGraph().createEdge( source, target, directed ) );
   }
 
   /**
    * Add a edge to this Network that already exists in 
    * Cytoscape
    * @return the Network Index of this edge
    */
   public int addEdge ( int cytoscape_edge ) {
     return restoreEdge( cytoscape_edge );
   }
 
   /**
    * Add a edge to this Network that already exists in 
    * Cytoscape
    * @return the Network Index of this edge
    */
   public CyEdge addEdge ( CyEdge cytoscape_edge ) {
     return ( CyEdge )restoreEdge( cytoscape_edge );
   }
  
   /**
    * Adds a edge to this Network, by looking it up via the 
    * given attribute and value
    * @return the Network Index of this edge
    */
   public int addEdge ( String attribute, Object value ) {
     return 0;
   }
 
   /**
    * This will remove this edge from the Network. However,
    * unless forced, it will remain in Cytoscape to be possibly
    * resused by another Network in the future.
    * @param force force this edge to be removed from all Networks
    * @return true if the edge is still present in Cytoscape 
    *          ( i.e. in another Network )
    */
   public boolean removeEdge ( int edge_index, boolean force ) {
     hideEdge( edge_index );
     return true;
   }
 
 }
 
