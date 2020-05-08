 package delma.tree;
 
 import delma.graph.visualisation.Vector;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 // TODO: Barnes–Hut simulation
 /**
  *
  * @author aopkarja
  */
 public class QuadTree<N> {
 
     private Node root;
 
     public QuadTree(Map<N, Vector> points, Map<N, Double> masses) {
         Vector min = getMinXYVector(points.values());
         Vector max = getMaxXYVector(points.values());
         root = new Node(min, max);
         for (Iterator<N> it = points.keySet().iterator(); it.hasNext();) {
             N n = it.next();
             Double mass = masses.get(n);
             root.addBody(points.get(n), mass == null ? 1 : mass);
         }
     }
 
     private Vector getMinXYVector(Collection<Vector> points) {
         double minX = Double.MAX_VALUE;
         double minY = Double.MAX_VALUE;
         for (Vector vector : points) {
             if (vector.getX() < minX) {
                 minX = vector.getX();
             }
             if (vector.getY() < minY) {
                 minY = vector.getY();
             }
         }
         return new Vector(minX, minY);
     }
 
     private Vector getMaxXYVector(Collection<Vector> points) {
         double maxX = Double.MIN_VALUE;
         double maxY = Double.MIN_VALUE;
         for (Vector vector : points) {
             if (vector.getX() > maxX) {
                 maxX = vector.getX();
             }
             if (vector.getY() > maxY) {
                 maxY = vector.getY();
             }
         }
         return new Vector(maxX, maxY);
     }
 
     private static class Node {
 
         private Vector center;
         private double mass;
         private Node[][] subNodes;
         private boolean external;
         private Vector min, max;
         private int count;
 
         public Node(Vector min, Vector max) {
             this.min = min;
             this.max = max;
             subNodes = new Node[2][2];
             external = true;
             count = 0;
         }
 
         public void addBody(Vector vector, double mass) {
             count++;
             if (external) {
                 if (center == null) {
                     center = vector;
                     this.mass = mass;
                     return;
                 } else {
                     int quadrantX = center.getX() < getDivisionX() ? 0 : 1;
                     int quadrantY = center.getY() < getDivisionY() ? 0 : 1;
                    subNodes[quadrantX][quadrantX] = new Node(calcMin(quadrantX, quadrantY), calcMax(quadrantX, quadrantY));
                    subNodes[quadrantX][quadrantX].addBody(center, this.mass);
                     external = false;
                 }
             }
             center.add(new Vector(vector).scale(mass));
             this.mass += mass;
             int quadrantX = vector.getX() < getDivisionX() ? 0 : 1;
             int quadrantY = vector.getY() < getDivisionY() ? 0 : 1;
             if (subNodes[quadrantX][quadrantY] == null) {
                 subNodes[quadrantX][quadrantY] = new Node(calcMin(quadrantX, quadrantY), calcMax(quadrantX, quadrantY));
             }
             subNodes[quadrantX][quadrantY].addBody(vector, mass);
         }
 
         private Vector calcMin(double quadrantX, double quadrantY) {
             return new Vector(min.getX() + quadrantX * getDivisionX(), min.getY() + quadrantY * getDivisionY());
         }
 
         private Vector calcMax(double quadrantX, double quadrantY) {
             return new Vector(max.getX() - quadrantX * getDivisionX(), max.getY() - quadrantY * getDivisionY());
         }
 
         private double getDivisionX() {
             return (max.getX() - min.getX()) / 2;
         }
 
         private double getDivisionY() {
             return (max.getY() - min.getY()) / 2;
         }
 
         public double getMass() {
             return mass;
         }
 
         public Vector getMassCenter() {
             return new Vector(center).scale(1 / mass);
         }
     }
 }
