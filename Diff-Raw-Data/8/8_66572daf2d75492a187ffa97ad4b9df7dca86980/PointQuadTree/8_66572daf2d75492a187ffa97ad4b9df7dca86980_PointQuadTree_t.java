 package dataStructure;
 
 import java.util.ArrayList;
 import java.math.BigDecimal;
 
 /**
  * PointQuadTree is a linked list quad tree data structures.
  * It is used to structure and quickly find points, taking advantage of their 2D location. 
  * 
  * This class is written with inspiration from QuadTree.java by Robert Sedgewick and Kevin Wayne
  * http://algs4.cs.princeton.edu/92search/QuadTree.java.html
  * 
  * Thanks to hypesystem and Morten Roed for pushing we towards this data structure and implementation
  * 
  * @author Claus L. Henriksen
  * @version 28. Marts 2012
  * @param <Point>
  * @see Point
  */
 public class PointQuadTree<Point> {
   private Node root;
   private ArrayList<Point> array;
   
   /**
    * Wrapper class used for the 2D data structure. 
    * Each leaf is a Node.
    */
   private class Node{
     Node NW, NE, SE, SW; //Four subtrees
     Point point;
     
     Node(Point point){
       this.point = point;
     }
   }
   
   /**
    * Insert new point to the quad tree.
    * The point will be wrapped and placed correctly within the tree.
    * 
    * @param point
    */
   public void inset(Point point){
     root = insert(root, point);
   }
   
   /**
    * Private method to wrap and insert a new point correctly within the tree.
    * This is called recursively until a correct location for the point is found.
    *  
    * @param h Root of the subtree where the point is to be placed 
    * @param point The point to be placed
    * @return The inserted node
    */
   private Node insert(Node h, Point point){
     if(h == null) return new Node(point); //First point inserted becomes root
     else if ( less(point.getX().doubleValue(), h.point.getX().doubleValue()) &&  less(point.getY().doubleValue(), h.point.getY().doubleValue())) h.SW = insert(h.SW, point);
     else if ( less(point.getX().doubleValue(), h.point.getX().doubleValue()) && !less(point.getY().doubleValue(), h.point.getY().doubleValue())) h.NW = insert(h.NW, point);
     else if (!less(point.getX().doubleValue(), h.point.getX().doubleValue()) &&  less(point.getY().doubleValue(), h.point.getY().doubleValue())) h.SE = insert(h.SE, point);
     else if (!less(point.getX().doubleValue(), h.point.getX().doubleValue()) && !less(point.getY().doubleValue(), h.point.getY().doubleValue())) h.NE = insert(h.NE, point);    
     return h;
   }
   
   /**
    * Find all points within a rectangle.
    * 
    * @param rect A rectangular interval defined by the Interval2D class
    * @see Interval2D
    * @see Point
    * @return ArrayList of points within interval
    */
   public ArrayList<Point> getRect(Interval2D rect){
     array = new ArrayList<Point>();
     getRect(root, rect);
     return array;
   }
   
   /**
    * Private method for finding points within interval. Method works recursively.
    * @param h Root of subtree to be searched
    * @param rect Interval to be within
    * @return ArrayList of points within interval
    */
   private ArrayList<Point> getRect(Node h, Interval2D rect){
     if (h == null) return null;
    double xmin = rect.getIntervalX().getLow();
    double ymin = rect.getIntervalY().getLow();
    double xmax = rect.getIntervalX().getHigh();
    double ymax = rect.getIntervalY().getHigh();
     if (rect.contains(h.point.getX().doubleValue(), h.point.getY().doubleValue()))
         array.add(h.point);
     if ( less(xmin, h.point.getX().doubleValue()) &&  less(ymin, h.point.getY().doubleValue())) getRect(h.SW, rect);
     if ( less(xmin, h.point.getX().doubleValue()) && !less(ymax, h.point.getY().doubleValue())) getRect(h.NW, rect);
     if (!less(xmax, h.point.getX().doubleValue()) &&  less(ymin, h.point.getY().doubleValue())) getRect(h.SE, rect);
     if (!less(xmax, h.point.getX().doubleValue()) && !less(ymax, h.point.getY().doubleValue())) getRect(h.NE, rect);
     return array;
   }
   
   /**
    * Private method to compare doubles
    * @param k1 First double
    * @param k2 Second double
    * @return True if first double is less than the secound double
    */
   private boolean less(double k1, double k2) { return k1 - k2 < 0; }
 }
