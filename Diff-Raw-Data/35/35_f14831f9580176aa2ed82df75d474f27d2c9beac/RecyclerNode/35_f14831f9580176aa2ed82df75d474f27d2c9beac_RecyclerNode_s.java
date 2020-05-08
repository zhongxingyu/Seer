 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package team039.common;
 
 import battlecode.common.*;
 
 /**
  *
  * @author sashko
  */
 public class RecyclerNode {
 
     public int myRobotID;
     public int parentRobotID;
 
     public MapLocation myLocation;
     public MapLocation parentLocation;
 
     @Override
     public String toString()
     {
         return "RecyclerNode [id: " + myRobotID + " location: (" + myLocation.x +
                 ", " + myLocation.y + ") " + "parent id: " + parentRobotID + (parentLocation!=null? " parent location: (" +
                 parentLocation.x + ", " + parentLocation.y + ") ]":"");
     }
 
     public boolean hasParent()
     {
         return parentLocation!=null;
     }
 
     public MapLocation getVector()
     {
         if( myLocation!=null && parentLocation!=null )
         {
             return myLocation.add( -parentLocation.x, -parentLocation.y);
         } else {
             return null;
         }
     }
 
     public Message generatePing()
     {
         int[] ints = new int[1];
         MapLocation[] locations = new MapLocation[1];
 
         ints[0] = parentRobotID;
 
         locations[0] = parentLocation;
 
        return MessageCoder.encodeMessage(MessageCoder.RECYCLER_PING, myRobotID, myLocation, Clock.getRoundNum(), false, null, ints, locations);
     }
 
     public Message generateDesignation()
     {
         int[] ints = new int[1];
         MapLocation[] locations = new MapLocation[1];
         String strings[] = new String[1];
 
         ints[0] = myRobotID;
 
         locations[0] = myLocation;
 
 
         return MessageCoder.encodeMessage(MessageCoder.RECYCLER_DESIGNATION, myRobotID, myLocation, Clock.getRoundNum(), false, strings, ints, locations);
     }
 
     public static RecyclerNode getFromPing( Message ping )
     {
         RecyclerNode output = new RecyclerNode();
 
         output.myRobotID = MessageCoder.getBroadcasterID(ping);
         output.myLocation = MessageCoder.getBroadcasterLocation(ping);
 
         return output;
     }
 }
