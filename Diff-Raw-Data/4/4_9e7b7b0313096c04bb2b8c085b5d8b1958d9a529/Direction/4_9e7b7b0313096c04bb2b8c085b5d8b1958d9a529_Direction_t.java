 package org.glowacki.core;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Directions
  */
 public enum Direction
 {
     /** left */
     LEFT,
     /** upper left */
     LEFT_UP,
     /** up */
     UP,
     /** upper right */
     RIGHT_UP,
     /** right */
     RIGHT,
     /** lower right */
     RIGHT_DOWN,
     /** down */
     DOWN,
     /** lower left */
     LEFT_DOWN,
     /** climb */
     CLIMB,
     /** descend */
     DESCEND,
     /** unknown */
     UNKNOWN;
 
     private static final List<Direction> VALUES =
         Collections.unmodifiableList(Arrays.asList(values()));
     private static final int SIZE = VALUES.size();
 
     /**
      * Get the next cardinal direction
      *
      * @return cardinal direction
      */
     public Direction next()
     {
         Direction[] vals = values();
         return vals[(ordinal() + 1) % 8];
     }
 
     /**
      * Get a cardinal direction
      *
      * @param val integer direction
      *
      * @return cardinal direction
      */
     public static Direction getDirection(int val)
     {
        if (val < 0) {
            throw new Error("Direction cannot be negative");
        }

         return VALUES.get(val % 8);
     }
 }
