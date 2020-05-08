 package org.drools.planner.examples.ras2012.model;
 
 public class MaintenanceWindow {
 
     private final Node westNode;
     private final Node eastNode;
     private final long start;
     private final long end;
 
    public MaintenanceWindow(final Node westNode, final Node eastNode, final int time1,
            final int time2) {
         if (eastNode == null || westNode == null) {
             throw new IllegalArgumentException("Neither node can be null.");
         }
         if (eastNode == westNode) {
             throw new IllegalArgumentException("MOW must be an arc, not a single node.");
         }
         if (time1 < 0 || time2 < 0) {
             throw new IllegalArgumentException("Neither time can be less than zero.");
         }
         this.westNode = westNode;
         this.eastNode = eastNode;
         this.start = Math.min(time1, time2) * 60 * 1000;
         this.end = Math.max(time1, time2) * 60 * 1000;
     }
 
     public Node getEastNode() {
         return this.eastNode;
     }
 
     /**
      * Get time when this maintenance window ends. (Inclusive.)
      * 
      * @return Time in milliseconds since the beginning of the world.
      */
     public long getEnd() {
         return this.end;
     }
 
     /**
      * Get time when this maintenance window starts. (Inclusive.)
      * 
      * @return Time in milliseconds since the beginning of the world.
      */
     public long getStart() {
         return this.start;
     }
 
     public Node getWestNode() {
         return this.westNode;
     }
 
     /**
      * Whether or not the give time is inside the window.
      * 
      * @param time Time in milliseconds.
      * @return
      */
     public boolean isInside(final long time) {
         if (this.start > time) {
             return false; // window didn't start yet
         }
         if (this.end < time) {
             return false; // window is already over
         }
         return true;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("MaintenanceWindow [startNode=").append(this.westNode).append(", endNode=")
                 .append(this.eastNode).append(", startingMinute=").append(this.start)
                 .append(", endingMinute=").append(this.end).append("]");
         return builder.toString();
     }
 
 }
