 import java.util.Vector;
 import java.util.LinkedList;
 
 public class KdTree {
     
     private static class KDNode {
         private Point2D point;      // the point
         // the axis-aligned rectangle corresponding to this node
         private RectHV rect;
         private KDNode lb;          // the left/bottom subtree
         private KDNode rt;          // the right/top subtree
         private int level;          // the level of node;
     }
     
     private KDNode root;
     private int totalNum;
     
     // construct an empty set of points
     public KdTree() {
         root = null;
         totalNum = 0;
         
     }
     
     // is the set empty?
     public boolean isEmpty() {
         return (totalNum == 0);
     }
     
     // number of points in the set
     public int size() {
         return totalNum;
     }
     
     // put the point into KDTree
     private KDNode put(KDNode node, Point2D p, int level, KDNode parent) {
         
         if (node == null) {
             // Insert new node here
             KDNode out = new KDNode();
             out.level = level;
             out.lb = null;
             out.rt = null;
             out.point = p;
             out.rect = null;
             totalNum++;
             if (parent != null) {
                 // rectangle are only for non-root nodes
                 if (parent.rect == null) {
                     // for level one nodes
                     if (p.x() <= parent.point.x()) {
                         // left rectangle
                         out.rect = new RectHV(0, 0, parent.point.x(), 1);
                     } else {
                         // right rectangle
                         out.rect = new RectHV(parent.point.x(), 0, 1, 1); 
                     }
                 }
                 else {
                     // for other level nodes
                     if (level % 2 == 0) {
                         // Even Level, use x-coordinate 
                         if (p.x() <= parent.point.x()) {
                             // left rectangle
                             out.rect = new RectHV(parent.rect.xmin(),
                                     parent.rect.ymin(),
                                     p.x(),
                                     parent.rect.ymax());
                         } else {
                             // right rectangle
                             out.rect = new RectHV(p.x(),
                                     parent.rect.ymin(),
                                     parent.rect.xmax(),
                                     parent.rect.ymax());
                         }
                         
                     } else {
                         // Odd Level, use y-coordinate
                         if (p.y() <= parent.point.y()) {
                             // upper rectangle
                             out.rect = new RectHV(parent.rect.xmin(),   
                                     p.y(),
                                     parent.rect.xmax(),
                                     parent.rect.ymax());
                         } else {
                             // bottom rectangle
                             out.rect = new RectHV(parent.rect.xmin(), 
                                     parent.rect.ymin(),
                                     parent.rect.xmax(),
                                     p.y());
                         }
                     }
                 }
             }
             return out;
         }
         
         // do nothing for the duplicate node
         if (node.point.equals(p))
             return node;
         
         if (((level % 2 == 0) && (p.x() <= node.point.x())) 
             || ((level % 2 == 1) && (p.y() <= node.point.y()))) {
             node.lb = put(node.lb, p, level+1, node);
         }
         else if (((level % 2 == 0) && (p.x() <= node.point.x())) 
                 || ((level % 2 == 1) && (p.y() <= node.point.y()))) {
             node.rt = put(node.rt, p, level+1, node);
         }
         
 
         
         return node;
     }
 
     
     // add the point p to the set (if it is not already in the set)
     public void insert(Point2D p) {
         root = put(root, p, 0, null);
     }
     
     private KDNode get(Point2D p) {
         KDNode node = root;
         while (node != null) {
             if (node.point.equals(p))
                 return node;
             if (((node.level % 2 == 0) && (p.x() <= node.point.x())) 
                     || ((node.level % 2 == 1) && (p.y() <= node.point.y()))) {
                 node = node.lb;
             }
             else
                 node = node.rt;
 
         }
         return null;
     }
 
     // does the set contain the point p?
     public boolean contains(Point2D p) {
         return (get(p) == null);
     }
     
     private void drawNode(KDNode node, KDNode parent) {
         
         if (node != null) {
             Point2D begin = null;
             Point2D end = null; 
             // Even level
             if (node.level % 2 == 0) {
                 // use x-coordinate , color red
                 StdDraw.setPenColor(StdDraw.RED);
                 if (parent == null) {
                     // for root node
                     begin = new Point2D(node.point.x(), 0);
                     end = new Point2D(node.point.x(), 1);
                 } else {
                     // for non-root node
                     begin = new Point2D(node.point.x(), node.rect.ymin());
                     end = new Point2D(node.point.x(), node.rect.ymax());
                 }
                 
             }
             // Odd level
             else {
                 // use y-coordinate , color blue
                 StdDraw.setPenColor(StdDraw.BLUE);
                 begin = new Point2D(node.rect.xmin(), node.point.y());
                 end = new Point2D(node.rect.xmax(), node.point.y());
             }
             
             begin.drawTo(end);
 
             drawNode(node.lb, node);
             drawNode(node.rt, node);
         }
     }
     
     // draw all of the points to standard draw
     public void draw() {
         drawNode(root, null);       
     }
     
     // find the points inside the rectangle
     private void searchRect(KDNode node, RectHV rect, Vector<Point2D> result) {
         if (node != null) {
             // check if the node is in range
             if (rect.contains(node.point))
                 result.add(node.point);
             
             // check if the left subtree is in range
             if (node.lb != null && node.lb.rect.intersects(rect))
                 searchRect(node.lb, rect, result);
             
             // check if the right subtree is in range
             if (node.lb != null && node.rt.rect.intersects(rect))
                 searchRect(node.rt, rect, result);
             
         }
     }
     
     // all points in the set that are inside the rectangle
     public Iterable<Point2D> range(RectHV rect) {
         Vector<Point2D> out = new Vector<Point2D>();
         searchRect(root, rect, out);
         return out;
     }
     
     
     // a nearest neighbor in the set to p; null if set is empty
     public Point2D nearest(Point2D p) {
         if (totalNum == 0)
             return null;
         KDNode node;
         Point2D out = null;
         double minLen = Double.POSITIVE_INFINITY;
 
         LinkedList<KDNode> queue = new  LinkedList<KDNode>();
         
         queue.addFirst(root);
         
         // search the point
         while (queue.size() > 0) {
             node = queue.removeLast();
             if (node.point.equals(p))
                 return node.point;
             double d = node.point.distanceTo(p);
             
             if (d < minLen) {
                 minLen = d;
                 out = node.point;
             }
             
             if (node.lb != null 
                && node.lb.rect.distanceTo(p) < minLen) {
                 queue.addFirst(node.lb);
             }
             
             if (node.rt != null 
                && node.rt.rect.distanceTo(p) < minLen) {
                 queue.addFirst(node.rt);
             }
                 
         }
 
         
 
         
         return out;
         
     }
 }
