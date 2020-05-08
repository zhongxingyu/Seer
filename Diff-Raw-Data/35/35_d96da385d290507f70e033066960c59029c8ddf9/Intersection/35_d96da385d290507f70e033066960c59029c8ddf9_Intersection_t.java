 public class Intersection {
 	
 	public static final int LEFT = -1;
 	public static final int RIGHT = 1;
 	
 	public static double SUPER_TINY = 0.00001;
 
 	public static PPoint ofTwoLines(PLine line1, PLine line2) {
 		double x1 = line1.get1stPoint().getX();
 		double y1 = line1.get1stPoint().getY();
 		double x2 = line1.get2ndPoint().getX();
 		double y2 = line1.get2ndPoint().getY();
 		double a1 = line2.get1stPoint().getX();
 		double b1 = line2.get1stPoint().getY();
 		double a2 = line2.get2ndPoint().getX();
 		double b2 = line2.get2ndPoint().getY();
 		double d = ((x2-x1)*(b1-b2) - (y2-y1)*(a1-a2));
 		if (Math.abs(d) > SUPER_TINY) {
 			double t1 = ((a1-x1)*(b1-b2) - (b1-y1)*(a1-a2)) / ((x2-x1)*(b1-b2) - (y2-y1)*(a1-a2));
 			double intersectionX = x1 + (x2-x1)*t1;
 			double intersectionY = y1 + (y2-y1)*t1;
 			PPoint intersectionPt = new PPoint(intersectionX, intersectionY,"");
 			return intersectionPt;
 		}
 		else {
 			return null;
 		}
 	}
 	
 	public static PPoint ofLineAndCircle(PLine line, PCircle circle, int whichPt) {
 		double rj = circle.getRadius();
 		double xj = circle.getCenter().getX();
 		double yj = circle.getCenter().getY();
 		double x0 = line.get1stPoint().getX();
 		double y0 = line.get1stPoint().getY();
 		double x1 = line.get2ndPoint().getX();
 		double y1 = line.get2ndPoint().getY();
 		double f = x1-x0;
 		double g = y1-y0;
 		double radicand = rj*rj*(f*f + g*g) - (f*(y0 - yj) - g*(x0-xj))*(f*(y0 - yj) - g*(x0-xj));
 		double denominator = f*f + g*g;
 		
 		if (radicand >= 0) { //so intersection exists
 			double t = (f*(xj-x0) + g*(yj-y0) + whichPt*Math.sqrt(radicand))/denominator;
 			return (new PPoint(x0 + f*t, y0 + g*t, ""));
 		}
 
 		return null;
 	}
 	
 	public static PPoint ofTwoCircles(PCircle circle1, PCircle circle2, int whichPt) {
 		double r1 = circle1.getRadius();
 		double r2 = circle2.getRadius();
 		double x1 = circle1.getCenter().getX();
 		double y1 = circle1.getCenter().getY();
 		double x2 = circle2.getCenter().getX();
 		double y2 = circle2.getCenter().getY();
 		PPoint p1 = circle1.getCenter();
 		PPoint p2 = circle2.getCenter();
 		
 		double d = Math.hypot(p2.getX() - p1.getX(), p2.getY() - p1.getY());
 		              
 		double xm = (d*d + r1*r1 - r2*r2)/(2*d);
 			//xm is the distance from p1 to the closest point
 			//on the segment connecting their centers to the intersection
 		              
 		double radicand = (4*d*d*r1*r1 - Math.pow(d*d-r2*r2+r1*r1,2))/(4*d*d);
 		
 		if (radicand >= 0) {
 			double h = Math.sqrt(radicand);
 				//h is the distance from the pt of intersection to the line
 				//connecting the centers
 			              
 			double xn = (y2-y1) / d;	//(xn,yn) is a unit vector normal to the
 			double yn = -(x2-x1) / d;	//segment connecting the centers
 			              
 			double a = x1 + (x2-x1)*xm/d;
 			double b = y1 + (y2-y1)*xm/d;
 			              
 			return (new PPoint(a + whichPt*h*xn, b + whichPt*h*yn,""));
 		}
 		else {
 			return null;  //no intersections exists
 		}
 	}
 	
 	public static boolean pointOnRay(PRay r, PPoint p) {
 		double x1 = r.get1stPoint().getX();
 		double y1 = r.get1stPoint().getY();
 		double x2 = r.get2ndPoint().getX();
 		double y2 = r.get2ndPoint().getY();
 		double x = p.getX();
 		double y = p.getY();
 		double t = (Math.abs(x2-x1) > SUPER_TINY ? (x-x1)/(x2-x1) : (y-y1)/(y2-y1));
 		return (t >= -SUPER_TINY);
 	}
 	
 	public static boolean pointOnSegment(PSegment segment, PPoint point) {
 		PRay ray1 = new PRay(segment.get1stPoint(),segment.get2ndPoint(),"");
 		PRay ray2 = new PRay(segment.get2ndPoint(),segment.get1stPoint(),"");
 		return (pointOnRay(ray1,point) && pointOnRay(ray2,point));
 	}
 	
 }
 
