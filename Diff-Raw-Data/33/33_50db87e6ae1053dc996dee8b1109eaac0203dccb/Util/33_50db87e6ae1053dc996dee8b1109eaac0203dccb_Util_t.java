 package de.engine.math;
 
 import de.engine.objects.Circle;
 import de.engine.objects.Ground;
 import de.engine.objects.ObjectProperties;
 import de.engine.objects.Polygon;
 import de.engine.objects.Square;
 
 public class Util
 {
     // needed for derivation
     private static final Double h = Math.pow(10d, 40d);
     private static Vector function = new Vector();
     private static double m = 0;
     private static double n = 0;
     private static Double[] radius = new Double[3];
     private static double diff_x = 0;
     private static double diff_y = 0;
     private static double alpha = 0;
     
     
     public static double distance(Vector p1, Vector p2)
     {
         double x = p2.getX() - p1.getX();
         double y = p2.getY() - p1.getY();
         return Math.hypot(x, y);
     }
     
     public static double distanceToOrigin(Vector p)
     {
         return Math.hypot(p.getX(), p.getY());
     }
     
     public static Vector add(Vector vec1, Vector vec2)
     {
         return new Vector(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY());
     }
     
     public static Vector minus(Vector vec1, Vector vec2)
     {
         return new Vector(vec1.getX() - vec2.getX(), vec1.getY() - vec2.getY());
     }
     
     public static Vector scale(Vector v, double s)
     {
         return new Vector(v.getX() * s, v.getY() * s);
     }
     
     public static double scalarProduct(Vector vec1, Vector vec2)
     {
         return vec1.getX() * vec2.getX() + vec1.getY() * vec2.getY();
     }
     
     public static double crossProduct(Vector vec1, Vector vec2)
     {
         return vec1.getX() * vec2.getY() - vec1.getY() * vec2.getX();
     }
     
     public static double getAngle(Vector vec1, Vector vec2)
     {
         return Math.atan2(vec2.getY() - vec1.getY(), vec2.getX() - vec1.getX());
     }
     
     
     public static Double newtonIteration(ObjectProperties object, Ground ground)
     {
         // If the object has no velocity in x direction, return the objects x-coordinate.
         if (object.velocity.getX() == 0)
             return object.getPosition().getX();
         
         Double xn  = object.getPosition().getX();
         Double new_distance = Double.MAX_VALUE;
         Double old_distance = 0d;
         Double result       = 0d;
         
         radius[0] = -object.getRadius();
         radius[1] = 0d;
         radius[2] = +object.getRadius();
 
         // m = slope, n = shift in y, linear function
         m = object.velocity.getY() / object.velocity.getX();
         n = object.getPosition().getY();
         
         // for calculating a pair of tangents right and left beside the main vector of the sphere
         alpha = Math.atan( m );
         
         for (int p = 0; p < 3; p++)
         {
             old_distance = new_distance;
             
             diff_x =  radius[p] * Math.sin(alpha);
             diff_y = -radius[p] * Math.cos(alpha);
             
             for (int i = 0; i < 6; i++)
             {
                 xn = xn - getFunctionsValue(xn, object, ground) / derive1D(xn, object, ground);
             }
             
             int y = (int) ground.function(xn);
             new_distance = Math.sqrt( Math.pow(xn - object.getPosition().getX(), 2d) + Math.pow(y - object.getPosition().getY(), 2d) );
             
             // returns the intersection coordinate with the shortest distance between circle an ground
             if (new_distance < old_distance)
                 result = xn;
         }
         return result;
     }
     
     /**
      * Calculates the 1st derivation of the function given in <i>getFunctionsValue</i>.
      * 
      * @param x - determines the point of which the derivation is wanted
      * @param object -
      * @param ground
      * @return
      */
     public static Double derive1D(Double x, ObjectProperties object, Ground ground)
     {
        
         // df(x) = ( f(x-h) - f(x-h) ) * 2/h
         if (object.velocity.getY()<0) 
         {
             return  (getFunctionsValue(x + h, object, ground) - getFunctionsValue(x - h, object, ground)) * 2d / h;
         } 
         else 
         {
             return -(getFunctionsValue(x + h, object, ground) - getFunctionsValue(x - h, object, ground)) * 2d / h;
         }
     }
     
     /**
      * Calculates the 2nd derivation of the function given in <i>getFunctionsValue</i>.
      * 
      * @param x - determines the point of which the derivation is wanted
      * @param object -
      * @param ground
      * @return
      */
     public static Double derive2D(Double x, ObjectProperties object, Ground ground)
     {
        
         // df(x) = ( f(x-h) - 2f(x) + f(x-h) ) / h²
         if (object.velocity.getY()<0) 
         {
             return  (getFunctionsValue(x + h, object, ground) - 2d*getFunctionsValue( x, object, ground ) + getFunctionsValue(x - h, object, ground)) / h*h;
         } 
         else 
         {
             return -(getFunctionsValue(x + h, object, ground) - 2d*getFunctionsValue( x, object, ground ) + getFunctionsValue(x - h, object, ground)) / h*h;
         }
     }
     
     public static Double getFunctionsValue(Double x, ObjectProperties object, Ground ground)
     {
         function.set(0, m * (x - object.getPosition().getX() -diff_x) +n +diff_y );
         function.set(1, ground.function(x) );
         
         return function.get(1) - function.get(0);
     }
 
     // ************************************************************
 
     /**
      * returns all axis between a Polygon and a Circle for the seperating axis theorem
      */
     public static Vector[] getAxis(Circle c, Polygon p, double time)
     {
         return mergeArrays(getAxis(p, time), getAxisToPoint(p, c.getPosition(time), time));
     }
     
     /**
      * returns all axis between two Polygons  for the seperating axis theorem
      */
     public static Vector[] getAxis(Polygon p1, Polygon p2, double time)
     {
         return mergeArrays(getAxis(p1, time), getAxis(p2, time));
     }
     
     private static Vector[] getAxisToPoint(Polygon p, Vector point, double time)
     {
         Vector[] axis = new Vector[p.points.length];
         for (int i = 0; i < p.points.length; i++)
         {
             axis[i] = Util.minus(p.getWorldPointPos(i, time), point).getUnitVector();
         }
         return axis;
     }
     
     private static Vector[] getAxis(Polygon p, double time)
     {
         Vector[] axis;
         if (p instanceof Square)
         {
             axis = new Vector[2];
             axis[0] = Util.minus(p.getWorldPointPos(1, time), p.getWorldPointPos(0, time)).getNormalVector().getUnitVector();
             axis[1] = Util.minus(p.getWorldPointPos(2, time), p.getWorldPointPos(1, time)).getNormalVector().getUnitVector();
         }
         else
         {
             axis = new Vector[p.points.length];
             for (int i = 0; i < p.points.length; i++)
             {
                 int j = (i == p.points.length - 1) ? 0 : i + 1;
                 axis[i] = Util.minus(p.getWorldPointPos(i, time), p.getWorldPointPos(j, time)).getNormalVector().getUnitVector();
             }
         }
         return axis;
     }
     
     private static Vector[] mergeArrays(Vector[] v1, Vector[] v2)
     {
         Vector[] array;
         array = new Vector[v1.length + v2.length];
         for (int i = 0; i < array.length; i++)
         {
             if (i < v1.length)
                 array[i] = v1[i];
             else
             {
                 array[i] = v2[i - v1.length];
             }
         }
         return array;
     }
     
     public static Vector crossEdges(Vector pos1, Vector edge1, Vector pos2, Vector edge2)
     {
         Vector pos1n, pos2n, edge1n, edge2n;
         if ((edge1.getX() == 0 && edge1.getY() == 0) || (edge2.getX() == 0 && edge2.getY() == 0))
             return null;
         if (edge1.getY() == 0 && edge2.getY() != 0)
         {
             pos1n = pos2;
             pos2n = pos1;
             edge1n = edge2;
             edge2n = edge1;
         }
         else if (edge1.getY() != 0)
         {
             pos1n = pos1;
             pos2n = pos2;
             edge1n = edge1;
             edge2n = edge2;
         }
         else
         {
             return null;
         }
         
         double s2 = ((pos2n.getY() - pos1n.getY()) * (edge1n.getX() / edge1n.getY()) - (pos2n.getX() - pos1n.getX())) / (edge2n.getX() - (edge2n.getY() * edge1n.getX() / edge1n.getY()));
         double s1 = ((edge2n.getY() * s2) + (pos2n.getY() - pos1n.getY())) / edge1n.getY();
         
         if (s2 >= 0 && s2 <= 1 && s1 >= 0 && s1 <= 1)
         {
            return new Vector(pos1n.getX() + s1 * edge1n.getX(), pos1n.getY() + s1 * edge1n.getY());
         }
         
         return null;
     }
 }
