 package cytoscape.graph.layout;
 
 import java.awt.geom.Point2D;
 import cytoscape.graph.GraphTopology;
 
 /**
  * This class represents not just a graph's topology but also a layout of its
  * nodes in 2D space.
  */
 public interface GraphLayout extends GraphTopology
 {
 
   /**
    * Returns the maximum allowable value of X positions of nodes.
    * All X positions of nodes in this graph will lie in the interval
    * <nobr><code>[0, getMaxWidth()]</code></nobr>.
   * @see #getNodePosition()
    */
   public double getMaxWidth();
 
   /**
    * Returns the maximum allowable value of Y positions of nodes.
    * All Y positions of nodes in this graph will lie in the interval
    * <nobr><code>[0, getMaxHeight()]</code></nobr>.
   * @see #getNodePosition()
    */
   public double getMaxHeight();
 
   /**
    * Returns the X,Y position of a node.
    * This method shall never return <code>null</code>.
    * @exception IndexOutOfBoundsException if <code>nodeIndex</code> is not
    *   in the interval <nobr><code>[0, getNumNodes() - 1]</code></nobr>.
    */
   public Point2D getNodePosition(int nodeIndex);
 
 }
