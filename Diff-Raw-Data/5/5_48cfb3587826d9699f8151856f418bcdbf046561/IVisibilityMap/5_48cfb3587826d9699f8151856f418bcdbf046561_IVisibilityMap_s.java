 package org.glowacki.core.fov;
 
 /**
  * A map which knows which points are obstructed and
  * whose cells can be set visible.
  *
  * Adapted from http://rlforj.sourceforge.net/
  */
 public interface IVisibilityMap
 {
     /**
     * Is the specified inside the map?
      *
      * @param x X coordinate
      * @param y Y coordinate
      *
     * @return <tt>true</tt> if there is an obstacle at the specified point
      */
     boolean contains(int x, int y);
 
     /**
      * Is the specified point obstructed?
      *
      * @param x X coordinate
      * @param y Y coordinate
      *
      * @return <tt>true</tt> if the specified point is obstructed
      */
     boolean isObstructed(int x, int y);
 
     /**
      * Mark the specified point as visible
      *
      * @param x X coordinate
      * @param y Y coordinate
      */
     void setVisible(int x, int y);
 }
