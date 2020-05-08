 package robot;
 
 import com.sun.squawk.util.MathUtils;
 
 public class DirectionVector {
 
     public static final DirectionVector ZERO = new DirectionVector(0, 0);
     
     double angle;
     double magnitude;
 
     public DirectionVector(double angle, double magnitude) {
         this.angle = angle;
         this.magnitude = magnitude;
     }
 
     public double getAngle() {
         return angle;
     }
 
     public double getMagnitude() {
         return magnitude;
     }
 
     public void add(DirectionVector bVector) {
         DirectionVector ret = addVectors(this, bVector);
         
         angle = ret.getAngle();
         magnitude = ret.getMagnitude();
     }
     
     public static DirectionVector addVectors(DirectionVector aVector, DirectionVector bVector) {
         if(aVector == ZERO) {
             return bVector;
        } else if(bVector == ZERO) {
            return aVector;
         }
         
         //c^2 = a^2 + b^2 - 2ab cosC
 
         //180 - (360-angleA) - angleB
 
         double C = 180 - (360 - aVector.getAngle()) - bVector.getAngle();
 
         double a = aVector.getMagnitude();
         double b = bVector.getMagnitude();
 
         double c = Math.sqrt(square(a) + square(b) - 2 * a * b * Math.cos(C));
 
         //sinA/a = sinB/b
         //A=sin-1(asinC/c)
 
         double B = MathUtils.asin(a * Math.sin(C) / c);
 
         double newAngle = B;
         double newMagnitude = c;
 
         DirectionVector newVector = new DirectionVector(newAngle, newMagnitude);
 
         return newVector;
     }
 
     private static double square(double magnitude) {
         return magnitude * magnitude;
     }
 }
