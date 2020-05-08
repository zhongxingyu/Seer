 package fr.iutvalence.java.mp.BrickBreaker;
 
/**
 * This class gives tools for differents class
 * @author ochiers
 *
 */
 public class Tools
 {
     /**
      * Function who say if the number toCompare is between the number a and the
      * number b
      * 
      * @param toCompare
      *            Number to compare
      * @param a
      *            Bound of comparison
      * @param b
      *            Bound of comparison
      * @return true if the number is in, false otherwise
      */
     public static boolean isFloatBetween(float toCompare, float a, float b)
     {
         return (toCompare >= a && toCompare <= b) || (toCompare >= b && toCompare <= a);
     }
 }
