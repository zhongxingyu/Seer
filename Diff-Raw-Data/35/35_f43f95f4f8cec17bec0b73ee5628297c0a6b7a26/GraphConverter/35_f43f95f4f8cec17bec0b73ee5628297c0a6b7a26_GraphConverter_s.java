 package cytoscape.foo;
 
 import cytoscape.Cytoscape;
 import cytoscape.graph.layout.algorithm.MutableGraphLayout;
 import cytoscape.graph.layout.algorithm.util.MutableGraphLayoutRepresentation;
 import cytoscape.view.CyNetworkView;
 import giny.model.Edge;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * This class is very temporary.  It is in heavy flux until some other
  * APIs which are not part of core Cytoscape are finalized.
  * And yes, this class does belong in the core.  It's not a plugin, I feel.
  **/
 public final class GraphConverter
 {
 
   // No constructor.
   private GraphConverter() {}
 
   private static final class MyCMutableGraphLayout
     extends MutableGraphLayoutRepresentation
   {
     // Definiition of m_nodeTranslation:
     // m_nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     final NodeView[] m_nodeTranslation;
 
     final double m_xOff;
     final double m_yOff;
 
     private MyCMutableGraphLayout(int numNodes,
                                   int[] directedEdgeSourceNodeIndices,
                                   int[] directedEdgeTargetNodeIndices,
                                   int[] undirectedEdgeNode0Indices,
                                   int[] undirectedEdgeNode1Indices,
                                   double width,
                                   double height,
                                   double[] nodeXPositions,
                                   double[] nodeYPositions,
                                   boolean[] isMovableNode,
                                   NodeView[] nodeTranslation,
                                   double xOff,
                                   double yOff)
     {
       super(numNodes,
             directedEdgeSourceNodeIndices,
             directedEdgeTargetNodeIndices,
             undirectedEdgeNode0Indices,
             undirectedEdgeNode1Indices,
             width,
             height,
             nodeXPositions,
             nodeYPositions,
             isMovableNode);
       m_nodeTranslation = nodeTranslation;
       m_xOff = xOff;
       m_yOff = yOff;
     }
   }
 
   private static class MyRMutableGraphLayout
     implements MutableGraphLayout
   {
     // Definiition of m_nodeTranslation:
     // m_nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     private final NodeView[] m_nodeTranslation;
 
     // Definition of m_edgeTranslation:
     // m_edgeTranslation[i] defines, for edge at index i in our
     // GraphTopology object, the corresponding EdgeView's Edge in Giny.
     private final Edge[] m_edgeTranslation;
 
     // Definiton of m_nodeIndexTranslation:
     // Both keys and values of this hashtable are java.lang.Integer objects.
     // There are exactly m_nodeTranslation.length keys in this hashtable.
     // Key-to-value mappings define index-of-node-in-Giny to
     // index-of-node-in-GraphTopology mappings.  When I say
     // "index-of-node-in-Giny", I mean giny.model.Node.getRootGraphIndex().
     private final Hashtable m_nodeIndexTranslation;
 
     private final double m_xOff;
     private final double m_yOff;
     private final double m_width;
     private final double m_height;
     private final boolean m_allMovable;
 
     private MyRMutableGraphLayout(NodeView[] nodeTranslation,
                                   Edge[] edgeTranslation,
                                   Hashtable nodeIndexTranslation,
                                  double minX, double maxX,
                                  double minY, double maxY,
                                  double percentBorder,
                                   boolean allMovable)
     {
       m_nodeTranslation = nodeTranslation;
       m_edgeTranslation = edgeTranslation;
       m_nodeIndexTranslation = nodeIndexTranslation;
      m_xOff = minX - ((maxX - minX) * percentBorder * 0.5d);
      m_yOff = minY - ((maxY - minY) * percentBorder * 0.5d);
      m_width = (maxX - minX) * (percentBorder + 1.0d);
      m_height = (maxY - minY) * (percentBorder + 1.0d);
       m_allMovable = allMovable;
     }
 
     public int getNumNodes() { return m_nodeTranslation.length; }
     public int getNumEdges() { return m_edgeTranslation.length; }
     public boolean isDirectedEdge(int edgeIndex) {
       return m_edgeTranslation[edgeIndex].isDirected(); }
     public int getEdgeNodeIndex(int edgeIndex, boolean sourceNode) {
       Edge edge = m_edgeTranslation[edgeIndex];
       int ginyNInx;
       if (sourceNode) ginyNInx = edge.getSource().getRootGraphIndex();
       else ginyNInx = edge.getTarget().getRootGraphIndex();
       Object nativeNodeIndex =
         m_nodeIndexTranslation.get(new Integer(ginyNInx));
       return ((Integer) nativeNodeIndex).intValue(); }
     public double getMaxWidth() { return m_width; }
     public double getMaxHeight() { return m_height; }
     public double getNodePosition(int nodeIndex, boolean xPosition) {
       NodeView node = m_nodeTranslation[nodeIndex];
       if (xPosition) return node.getXPosition() - m_xOff;
       else return node.getYPosition() - m_yOff; }
     public boolean isMovableNode(int nodeIndex) {
       NodeView node = m_nodeTranslation[nodeIndex];
       if (m_allMovable) return true;
       else return node.isSelected(); }
     public void setNodePosition(int nodeIndex, double xPos, double yPos) {
       NodeView node = m_nodeTranslation[nodeIndex];
       if (xPos < 0.0d || xPos > getMaxWidth())
         throw new IllegalArgumentException("xPos is out of bounds");
       if (yPos < 0.0d || yPos > getMaxHeight())
         throw new IllegalArgumentException("yPos is out of bounds");
      node.setOffset(xPos + m_xOff, yPos + m_xOff); }
   }
 
   /**
    * Returns a representation of Cytoscape's current network view.
    * Returns a <code>MutableGraphLayout</code> which, when mutated, has no
    * effect on the underlying Cytoscape network view.  Use the return value
    * of this method when moving positions of nodes in a thread that is
    * not the AWT dispatch thread.  Use <code>updateCytoscapeLayout()</code>,
    * passing as an argument the return value of <code>getGraphCopy()</code>,
    * to move nodes in the underlying Cytoscape network view.
    * <code>getMaxWidth()</code> of the return object is ...
    * All distances are preserved from to Cytoscape graph to the return object.
    **/
   public static MutableGraphLayout getGraphCopy(double percentBorder)
   {
     if (percentBorder < 0.0d)
       throw new IllegalArgumentException("percentBorder < 0.0");
 
     CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     final int numNodesInTopology = graphView.getNodeViewCount();
 
     // Definiition of nodeTranslation:
     // nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     final NodeView[] nodeTranslation = new NodeView[numNodesInTopology];
 
     // Definiton of nodeIndexTranslation:
     // Both keys and values of this hashtable are java.lang.Integer objects.
     // There are exactly numNodesInTopology keys in this hashtable.
     // Key-to-value mappings define index-of-node-in-Giny to
     // index-of-node-in-GraphTopology mappings.  When I say
     // "index-of-node-in-Giny", I mean giny.model.Node.getRootGraphIndex().
     final Hashtable nodeIndexTranslation = new Hashtable();
 
     Iterator nodeIterator = graphView.getNodeViewsIterator();
     int nodeIndex = 0;
     double minX = Double.MAX_VALUE;
     double maxX = Double.MIN_VALUE;
     double minY = Double.MAX_VALUE;
     double maxY = Double.MIN_VALUE;
     boolean[] mobility = new boolean[numNodesInTopology];
     final boolean noNodesSelected =
       (graphView.getSelectedNodeIndices().length == 0);
 
     while (nodeIterator.hasNext())
     {
       NodeView currentNodeView = (NodeView) nodeIterator.next();
       nodeTranslation[nodeIndex] = currentNodeView;
       if (nodeIndexTranslation.put
           (new Integer(currentNodeView.getNode().getRootGraphIndex()),
            new Integer(nodeIndex)) != null)
         throw new IllegalStateException("Giny farted and someone lit a match");
       minX = Math.min(minX, currentNodeView.getXPosition());
       maxX = Math.max(maxX, currentNodeView.getXPosition());
       minY = Math.min(minY, currentNodeView.getYPosition());
       maxY = Math.max(maxY, currentNodeView.getYPosition());
       if (noNodesSelected) mobility[nodeIndex] = true;
       else mobility[nodeIndex] = currentNodeView.isSelected();
       nodeIndex++;
     }
     if (nodeIndex != numNodesInTopology)
       throw new IllegalStateException("something smells really bad here");
     Vector directedEdgeVector = new Vector();
     Vector undirectedEdgeVector = new Vector();
     Iterator edgeIterator = graphView.getEdgeViewsIterator();
     while (edgeIterator.hasNext())
     {
       Edge currentEdge = ((EdgeView) edgeIterator.next()).getEdge();
       int ginySourceNodeIndex = currentEdge.getSource().getRootGraphIndex();
       int ginyTargetNodeIndex = currentEdge.getTarget().getRootGraphIndex();
       int nativeSourceNodeIndex =
         ((Integer) nodeIndexTranslation.get
          (new Integer(ginySourceNodeIndex))).intValue();
       int nativeTargetNodeIndex =
         ((Integer) nodeIndexTranslation.get
          (new Integer(ginyTargetNodeIndex))).intValue();
       Vector chosenEdgeVector = undirectedEdgeVector;
       if (currentEdge.isDirected()) chosenEdgeVector = directedEdgeVector;
       chosenEdgeVector.add(new int[] { nativeSourceNodeIndex,
                                        nativeTargetNodeIndex });
     }
     final int[] directedEdgeSourceNodeIndices =
       new int[directedEdgeVector.size()];
     final int[] directedEdgeTargetNodeIndices =
       new int[directedEdgeVector.size()];
     for (int i = 0; i < directedEdgeVector.size(); i++) {
       int[] edge = (int[]) directedEdgeVector.get(i);
       directedEdgeSourceNodeIndices[i] = edge[0];
       directedEdgeTargetNodeIndices[i] = edge[1]; }
     final int[] undirectedEdgeSourceNodeIndices =
       new int[undirectedEdgeVector.size()];
     final int[] undirectedEdgeTargetNodeIndices =
       new int[undirectedEdgeVector.size()];
     for (int i = 0; i < undirectedEdgeVector.size(); i++) {
       int[] edge = (int[]) undirectedEdgeVector.get(i);
       undirectedEdgeSourceNodeIndices[i] = edge[0];
       undirectedEdgeTargetNodeIndices[i] = edge[1]; }
     final double[] nodeXPositions = new double[numNodesInTopology];
     final double[] nodeYPositions = new double[numNodesInTopology];
     final double border =
       Math.max(maxX - minX, maxY - minY) * percentBorder * 0.5d;
     final double xOff = minX - border;
     final double yOff = minY - border;
     for (int i = 0; i < numNodesInTopology; i++) {
       nodeXPositions[i] = nodeTranslation[i].getXPosition() - xOff;
       nodeYPositions[i] = nodeTranslation[i].getYPosition() - yOff; }
     return new MyCMutableGraphLayout(numNodesInTopology,
                                      directedEdgeSourceNodeIndices,
                                      directedEdgeTargetNodeIndices,
                                      undirectedEdgeSourceNodeIndices,
                                      undirectedEdgeTargetNodeIndices,
                                      maxX - minX + border + border,
                                      maxY - minY + border + border,
                                      nodeXPositions,
                                      nodeYPositions,
                                      mobility,
                                      nodeTranslation,
                                      xOff,
                                      yOff);
   }
 
   /**
    * <code>layout</code> must be an object previously returned
    * by <code>GraphConverter.getGraphCopy()</code>.  During the time
    * that passes between setting <code>layout</code> as the return value
    * of <code>getGraphCopy()</code> and calling this method with the same
    * <code>layout</code>, Cytoscape's current network view topology
    * cannot change, otherwise this method will barf.
    *
    * @exception IllegalArgumentException if <code>layout</code> is not
    *   a value that was previously returned by <code>getGraphCopy()</code>.
    **/
   public static void updateCytoscapeLayout(MutableGraphLayout layout)
   {
     MyCMutableGraphLayout myLayout;
     try { myLayout = (MyCMutableGraphLayout) layout; }
     catch (RuntimeException e) {
       throw new IllegalArgumentException
         ("layout is not a previous return value of getGraphCopy()"); }
     NodeView[] nodeTranslation = myLayout.m_nodeTranslation;
     for (int i = 0; i < nodeTranslation.length; i++)
       nodeTranslation[i].setOffset
         (layout.getNodePosition(i, true) + myLayout.m_xOff,
          layout.getNodePosition(i, false) + myLayout.m_yOff);
   }
 
   /**
    * Returns a representation of Cytoscape's current network view.
    * Returns a <code>MutableGraphLayout</code>, which, when mutated,
    * has a direct effect on the underlying Cytoscape network view.  You'd
    * sure as heck better'd be using the returned object from the AWT event
    * dispatch thread!  Better yet, lock the Cytoscape desktop somehow
    * (with a modal dialog for example) while using this returned object.
    * Movable nodes are defined to be selected nodes in Cytoscape - if no
    * nodes are selected then all nodes are movable.  If selected node
    * information changes while we have a reference to this return object,
    * then the movability of corresponding node also changes.  This is one
    * reason why it's important to &quot;lock&quot; the Cytoscape desktop
    * while operating on this return object.
    **/
   public static MutableGraphLayout getGraphReference(double percentBorder)
   {
     if (percentBorder < 0.0d)
       throw new IllegalArgumentException("percentBorder < 0.0");
 
     CyNetworkView graphView = Cytoscape.getCurrentNetworkView();
     final int numNodesInTopology = graphView.getNodeViewCount();
     final int numEdgesInTopology = graphView.getEdgeViewCount();
 
     // Definiition of nodeTranslation:
     // nodeTranslation[i] defines, for node at index i in our
     // GraphTopology object, the corresponding NodeView in Giny.
     final NodeView[] nodeTranslation = new NodeView[numNodesInTopology];
 
     // Definition of edgeTranslation:
     // edgeTranslation[i] defines, for edge at index i in our
     // GraphTopology object, the corresponding EdgeView's Edge in Giny.
     final Edge[] edgeTranslation = new Edge[numEdgesInTopology];
 
     // Definiton of nodeIndexTranslation:
     // Both keys and values of this hashtable are java.lang.Integer objects.
     // There are exactly numNodesInTopology keys in this hashtable.
     // Key-to-value mappings define index-of-node-in-Giny to
     // index-of-node-in-GraphTopology mappings.  When I say
     // "index-of-node-in-Giny", I mean giny.model.Node.getRootGraphIndex().
     final Hashtable nodeIndexTranslation = new Hashtable();
 
     Iterator nodeIterator = graphView.getNodeViewsIterator();
     int nodeIndex = 0;
     double minX = Double.MAX_VALUE;
     double maxX = Double.MIN_VALUE;
     double minY = Double.MAX_VALUE;
     double maxY = Double.MIN_VALUE;
     final boolean noNodesSelected =
       (graphView.getSelectedNodeIndices().length == 0);
 
     while (nodeIterator.hasNext())
     {
       NodeView currentNodeView = (NodeView) nodeIterator.next();
       nodeTranslation[nodeIndex] = currentNodeView;
       if (nodeIndexTranslation.put
           (new Integer(currentNodeView.getNode().getRootGraphIndex()),
            new Integer(nodeIndex)) != null)
         throw new IllegalStateException("Giny farted and someone lit a match");
       minX = Math.min(minX, currentNodeView.getXPosition());
       maxX = Math.max(maxX, currentNodeView.getXPosition());
       minY = Math.min(minY, currentNodeView.getYPosition());
       maxY = Math.max(maxY, currentNodeView.getYPosition());
       nodeIndex++;
     }
     if (nodeIndex != numNodesInTopology)
       throw new IllegalStateException("something smells really bad here");
     Iterator edgeIterator = graphView.getEdgeViewsIterator();
     int edgeIndex = 0;
     while (edgeIterator.hasNext())
     {
       Edge currentEdge = ((EdgeView) edgeIterator.next()).getEdge();
       edgeTranslation[edgeIndex] = currentEdge;
       edgeIndex++;
     }
     if (edgeIndex != numEdgesInTopology)
       throw new IllegalStateException("someone [did] cut the cheese here");
     return new MyRMutableGraphLayout(nodeTranslation,
                                      edgeTranslation,
                                      nodeIndexTranslation,
                                     minX, maxX, minY, maxY,
                                     percentBorder, noNodesSelected);
   }
 
 }
