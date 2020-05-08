 /*************************************************************************
  * Name: Uros Slana
  * Email: urossla(at)gmail.com
  *
  * Compilation: javac ClosestPoint.java
  * Dependencies: Point.java, KdTree.java, KdNode.java  
  *
  * Description: Finds the closest points from a specified set of points
  *
  * % java ClosestPoint  set_of_points.txt set_of_querry_points.txt
  * 0.4 0.5
  * 0.5 0.3
  * 0.34 0.56
  * ...
  *************************************************************************/
 
 import java.io.File;
 import java.util.Scanner;
 import java.util.LinkedList;
 import java.io.FileNotFoundException;
 
 public class ClosestPoint {
 	
 	KdTree kdtree;
 
 	public ClosestPoint(Iterable<Point> points) {
 
 		kdtree = new KdTree();
 		
 		for (Point p: points) {
 			kdtree.insert(p);
 		}
 	}
 	
 	public Point nearestPoint(Point p) {   
 		return nearestPoint(p, kdtree.root(), 1, Double.POSITIVE_INFINITY);
 	}
 
 	private Point nearestPoint(Point p, KdNode cur, int depth, double min) {
 		
 		if (cur == null) {
 			return null;
 		}
 		
 		KdNode up;					//go to other side where point p is
		KdNode down;				//go to side where point p is
		Point champ;				//point which is closest to p
		Point other;				//maybe another point is closer than champ
 		double dist;
 		double perpendicularDist;
        
 		//split vertical
 		if (depth%2 == 1) {
 			perpendicularDist = Math.abs(cur.p.x - p.x);
 			if (p.x < cur.p.x) {
 				down = cur.lb;
 				up = cur.rt;
 			}
 			else {
 				down = cur.rt;
 				up = cur.lb;
 			}
 		}
 		//split horizontal
 		else {
 			perpendicularDist = Math.abs(cur.p.y - p.y);
 			if (p.y < cur.p.y) {
 				down = cur.lb;
 				up = cur.rt;
 			}
 			else {
 				down = cur.rt;
 				up = cur.lb;
 		   }
 		}
 		
 		dist = p.distanceTo(cur.p);
 		
 		if (dist < min) {
 			min = dist;
 			champ = cur.p;
 		}
 		else {
 			champ = null;
 		}
 		
 		other = nearestPoint(p, down, depth+1, min);
 		
 		if (other != null && other.distanceTo(p) < min) {
 		   min = other.distanceTo(p);
 		   champ = other;
 	   }
 	   
 	   if (perpendicularDist < min) {
 		   other = nearestPoint(p, up, depth+1, min);
 		   if (other != null && other.distanceTo(p) < min) {
 			   min = other.distanceTo(p);
 			   champ = other;
 		   }
 	   }
 	   return champ;
 	}
 
 	public static void main(String[] args) {
 
 		if (args.length < 2) {
 			System.out.println("Incorrect usage.");
 			return;
 		}
 		
 		Scanner scanPoint;
 		Scanner scanClosest;
 		ClosestPoint closest;
 		File points = new File(args[0]);
 		File querryPoints = new File(args[1]);
 		LinkedList<Point> p = new LinkedList<Point>();
 		LinkedList<Point> closestP = new LinkedList<Point>();
 
 		try {
 			scanPoint = new Scanner(points);
 			scanClosest = new Scanner(querryPoints);
 
			while(scanPoint.hasNextDouble()) {
 				double x = scanPoint.nextDouble();
 				double y = scanPoint.nextDouble();
 				p.add(new Point(x, y));
 			}
 			
 			while(scanClosest.hasNextDouble()) {
 				double x = scanClosest.nextDouble();
 				double y = scanClosest.nextDouble();
 				closestP.add(new Point(x, y));
 			}
 
 		}catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 
 		closest = new ClosestPoint(p);
 
 		for (Point fClosest: closestP) {
 			System.out.println("For point " +  fClosest + " the closest one is: " + closest.nearestPoint(fClosest));
 		}
 	}
 }
