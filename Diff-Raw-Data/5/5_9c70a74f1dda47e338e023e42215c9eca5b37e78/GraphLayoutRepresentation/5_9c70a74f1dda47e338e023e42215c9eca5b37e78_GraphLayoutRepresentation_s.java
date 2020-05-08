 package cytoscape.graph.layout.util;
 
 import cytoscape.graph.layout.GraphLayout;
 import cytoscape.graph.util.GraphTopologyRepresentation;
 import java.awt.geom.Point2D;
 
 public class GraphLayoutRepresentation
   extends GraphTopologyRepresentation
   implements GraphLayout
 {
 
   private final double m_maxWidth;
   private final double m_maxHeight;
 
   /**
    * Node X positions for subclasses that implement mutable functionality.
   * subclasses should take care to respect zero and max width bounds.
    **/
   protected final double[] m_nodeXPositions;
 
   /**
    * Node Y positions for subclasses that implement mutable functionality.
    * Subclasses should take care to respect zero and max height bounds.
    **/
   protected final double[] m_nodeYPositions;
 
   /**
    * Copies are made of all the array input parameters; modifying
    * the arrays after this constructor is called will have no effect on an
    * instance of this class.  An instance of this class never modifies any of
    * the arrays passed into the constructor.<p>
    * An instance of the exact class <code>GraphLayoutRepresentation</code>
    * represents a graph layout that is not mutable.  However,
    * an instance of a subclass of <code>GraphLayoutRepresentation</code> may
    * represent a graph layout that <i>is</i> mutable.  A test of whether
    * or not the expression below evaluates to <code>true</code> can be
    * used in determining whether or not an instance of this class is
    * guaranteed to represent a non-mutable graph layout:
    * <blockquote><pre>
    *((GraphLayoutRepresentation) obj).getClass().equals(cytoscape.graph.layout.util.GraphLayoutRepresentation.class)
    * </pre></blockquote><p>
    * This constructor calls
    * <blockquote><pre>
    *super(numNodes,
    *      directedEdgeSourceNodeIndices,
    *      directedEdgeTargetNodeIndices,
    *      undirectedEdgeNode0Indices,
    *      undirectedEdgeNode1Indices);
    * </pre></blockquote>
    * - for the sake of preventing the same documentation from existing in
    * two different source code files, please refer to
    * <code>GraphTopologyRepresentation</code> for a definition of these
    * first five input parameters.
    *
    * @param maxWidth
    *   <blockquote>the maximum allowable X position of nodes, as defined by
    *   <code>getMaxWidth()</code>.</blockquote>
    * @param maxHeight
    *   <blockquote>the maximum allowable Y position of nodes, as defined by
    *   <code>getMaxHeight()</code>.</blockquote>
    * @param nodeXPositions
    *   <blockquote>an array of length <code>numNodes</code> such that
    *   <code>nodeXPositions[nodeIndex]</code> defines the value
    *   <code>getNodePosition(nodeIndex).getX()</code>; if this array is
    *   <code>null</code>, all node X positions are defined to be
    *   <code>0.0</code>.</blockquote>
    * @param nodeYPositions
    *   <blockquote>an array of length <code>numNodes</code> such that
    *   <code>nodeYPositions[nodeIndex]</code> defines the value
    *   <code>getNodePosition(nodeIndex).getY()</code>; if this array is
    *   <code>null</code>, all node Y positions are defined to be
    *   <code>0.0</code>.</blockquote>
    * @exception IllegalArgumentException if parameters are passed which
    *   don't agree with a possible graph definition.
   * @see GraphTopologyRepresentation
    **/
   public GraphLayoutRepresentation(int numNodes,
                                    int[] directedEdgeSourceNodeIndices,
                                    int[] directedEdgeTargetNodeIndices,
                                    int[] undirectedEdgeNode0Indices,
                                    int[] undirectedEdgeNode1Indices,
                                    double maxWidth,
                                    double maxHeight,
                                    double[] nodeXPositions,
                                    double[] nodeYPositions)
   {
     super(numNodes, directedEdgeSourceNodeIndices,
           directedEdgeTargetNodeIndices, undirectedEdgeNode0Indices,
           undirectedEdgeNode1Indices);
 
     // Let's be anal and prove to ourselves that we no longer need any
     // of the parameters that are passed to our superclass' constructor.
     numNodes = -1;
     directedEdgeSourceNodeIndices = null; directedEdgeTargetNodeIndices = null;
     undirectedEdgeNode0Indices = null; undirectedEdgeNode1Indices = null;
 
     // Preliminary error checking.
     if (nodeXPositions == null) nodeXPositions = new double[getNumNodes()];
     if (nodeYPositions == null) nodeYPositions = new double[getNumNodes()];
 
     // Real parameter checking.  Set member variables.
     if (maxWidth < 0.0d) throw new IllegalArgumentException
                            ("cannot have negative maxWidth");
     m_maxWidth = maxWidth;
     if (maxHeight < 0.0d) throw new IllegalArgumentException
                             ("cannot have negative maxHeight");
     m_maxHeight = maxHeight;
     if (nodeXPositions.length != getNumNodes())
       throw new IllegalArgumentException
         ("node X positions array does not have length numNodes");
     m_nodeXPositions = new double[nodeXPositions.length];
     System.arraycopy(nodeXPositions, 0, m_nodeXPositions, 0,
                      nodeXPositions.length);
     if (nodeYPositions.length != getNumNodes())
       throw new IllegalArgumentException
         ("node Y positions array does not have length numNodes");
     m_nodeYPositions = new double[nodeYPositions.length];
     System.arraycopy(nodeYPositions, 0, m_nodeYPositions, 0,
                      nodeYPositions.length);
     for (int i = 0; i < m_nodeXPositions.length; i++) {
       if (m_nodeXPositions[i] < 0.0d || m_nodeXPositions[i] > m_maxWidth ||
           m_nodeYPositions[i] < 0.0d || m_nodeYPositions[i] > m_maxHeight) {
         throw new IllegalArgumentException
           ("a node position falls outside of allowable rectangle"); } }
   }
 
   public final double getMaxWidth() { return m_maxWidth; }
 
   public final double getMaxHeight() { return m_maxHeight; }
 
   public final Point2D getNodePosition(int nodeIndex) {
     // This will automatically throw an ArrayIndexOutOfBoundsException,
     // which is a subclass of IndexOutOfBoundsException, if nodeIndex
     // is not a valid index.
     return new Point2D.Double(m_nodeXPositions[nodeIndex],
                               m_nodeYPositions[nodeIndex]); }
 
 }
