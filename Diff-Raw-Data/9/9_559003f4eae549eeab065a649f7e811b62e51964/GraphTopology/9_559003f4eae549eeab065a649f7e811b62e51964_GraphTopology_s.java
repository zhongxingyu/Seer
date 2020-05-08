 package cytoscape.graph;
 
 /**
  * A bare-minimum definition of a graph.  A given edge in a graph can
  * be either <i>directed</i> or <i>undirected</i>.  Directedness of edges
  * is an important part of graph topology for calculations such as
  * finding a path <i>from</i> node A <i>to</i> node B.  A number of edges
  * between two nodes may exist; this number may have significance in some
  * applications, and so we don't want to represent an undirected edge
  * simply as two directed edges which are exact opposites of each other.<p>
  * Some extreme cases of graphs representable with this object are
  * graphs containing only nodes and no edges.  Another strange graph is one
  * with just one node and a million edges.  An erroneous graph is one
  * with one edge and no nodes, because the presence of one edge implies that
  * at least one node exists (an edge may in fact start and end at the same
  * node).<p>
  * Topologists claim that they can't tell a donut apart from a coffee cup.
  * In fact, a coffee cup with a handle can be &quot;smoothly morphed&quot;
  * into a donut shape.  In topology, distances do not matter but connectedness
  * does.  Therefore, a topological definition of a graph should not
  * contain any notion of a &quot;distance along edge&quot;.<p>
  * The methods on this interface do not expose any mutable behavior; this does
  * not mean, however, that instances of this interface are not mutable.  A
  * sub-interface extending <code>GraphTopology</code> could be defined which
  * exposes mutable functionality.
  */
 public interface GraphTopology
 {
 
   /**
    * Returns the number of nodes in this graph.  In other methods of this
    * interface a node is referenced by its index.  Indexes of nodes start
    * at <code>0</code> and end at <nobr><code>getNumNodes() - 1</code></nobr>,
    * inclusive.
    *
    * @return number of nodes in this graph.
    */
   public int getNumNodes();
 
   /**
    * Returns the number of edges in this graph.  In other methods of this
    * interface an edge is referenced by its index.  Indexes of edges start
    * at <code>0</code> and end at <nobr><code>getNumEdges() - 1</code></nobr>,
    * inclusive.<p>
    * Note: a graph which contains an edge must also contain at least one
    * node; therefore, there are certain constraints on allowable return values.
   * For example, if <code>getNumEdges()</code> returns <code>1</code>,
   * then <code>getNumEdges()</code> must return a value greater than zero.
    *
    * @return number of edges in this graph.
    */
   public int getNumEdges();
 
   /**
    * Returns the directedness of edge at index <code>edgeIndex</code>.
    *
    * @param edgeIndex index of edge whose directedness we're seeking.
    * @return <code>true</code> if directed edge, <code>false</code> if
    *   undirected edge.
    * @exception IndexOutOfBoundsException if <code>edgeIndex</code> is not
    *   in the interval <nobr><code>[0, getNumEdges() - 1]</code></nobr>.
    */
   public boolean isDirectedEdge(int edgeIndex);
 
   /**
    * Returns <code>true</code> if and only if edges in this graph are
    * either all directed or all undirected.
    */
   public boolean areAllEdgesSimilar();
 
   /**
    * Returns an index to a node which is either the source node or the
    * target node of edge at index <code>edgeIndex</code>.
    *
    * @param edgeIndex the index of the edge whose end nodes we are seeking.
    * @param sourceNode if <code>true</code>, returns the source node of this
    *   edge; if <code>false</code>, returns the target node of this edge -
    *   for undirected edges &quot;source node&quot; should be interpreted
    *   as &quot;node 0&quot; and &quot;target node&quot; should be
    *   interpreted as &quot;node 1&quot;.
    * @return the index of the node we are asking for; the return value
    *   shall lie in the interval
    *   <nobr><code>[0, getNumNodes() - 1]</code></nobr>.
    * @exception IndexOutOfBoundsException if <code>edgeIndex</code> is not
    *   in the interval <nobr><code>[0, getNumEdges() - 1]</code></nobr>.
    */
   public int getEdgeNode(int edgeIndex, boolean sourceNode);
 
 }
