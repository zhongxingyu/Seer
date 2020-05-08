 package org.uci.luci.interchange.Vehicles;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Map;
 import java.util.List;
 
 import org.uci.luci.interchange.Graph.*;
 import org.uci.luci.interchange.Intersections.*;
 import org.uci.luci.interchange.Registry.*;
 import org.uci.luci.interchange.Util.*;
 
 public class Vehicle {
   public static double DISTANCE_TO_CONSIDER_AS_SAME = 0.00002;
   
   public int vin;
   public double lat, lon;
  public Vector2d velocity;
   private String originNodeId, destinationNodeId;
   // lanes are numbered 0-(lanes-1) with 0 being the left-most lane.
   // the highest number is the lane on the right shoulder of the street
   private int onLaneNumber;
   public String state = "";
   boolean paused;
   public boolean flagForRemoval = false;
   public String preparingFor = "";
   
   public boolean vehicleOnLeft() {
     return false;
   }
   
   public boolean vehicleOnRight() {
     return false;
   }
   
   public void pause() {
     paused = true;
   }
   
   public boolean paused() {
     return paused;
   }
   
   public Way getWay() {
     return Oracle.wayBetweenNodes(originNodeId, destinationNodeId);
   }
   
   public boolean isGoingForwardOnWay() {
     if (originNodeId == null || destinationNodeId == null) {
       // this is okay because it just means we haven't been able to determine it yet.
       return true;
     }
     
     Way w = Oracle.wayBetweenNodes(originNodeId, destinationNodeId);
     
     if (w == null) {
       System.out.println("Could not determine direction of vehicle on way. ("+originNodeId + " - " + destinationNodeId + " vehicle " + vin + ")");
       return true;
     }
     else {
       int oI = w.nd.indexOf(originNodeId);
       int dI = w.nd.indexOf(destinationNodeId);
       return oI < dI;
     }
   }
   
   public void setOriginNodeId(String nodeId) {
     if (originNodeId != null)
       Oracle.deregisterVehicleOrigin(vin, originNodeId);
     originNodeId = nodeId;
     Oracle.registerVehicleOrigin(vin, originNodeId);
   }
   
   public void setDestinationNodeId(String nodeId) {
     destinationNodeId = nodeId;
   }
   
   public void setOnLaneNumber(int laneNumber) {
     onLaneNumber = laneNumber;
   }
   
   public int getOnLaneNumber() {
     return onLaneNumber;
   }
   
   public Vehicle(double lat, double lon, String anOriginNodeId, int laneNumber) {
   // public Vehicle(double lat, double lon, String anOriginNodeId, String aDestinationNodeId, int laneNumber) {
     
     this.paused = false;
     this.lat = lat;
     this.lon = lon;
     setOriginNodeId(anOriginNodeId);
     // setDestinationNodeId(aDestinationNodeId);
     setOnLaneNumber(laneNumber);
   }
   
   public boolean isAtDestinationNode() {
     return distanceToDestinationNode() < DISTANCE_TO_CONSIDER_AS_SAME;
   }
   
   public boolean isAtOriginNode() {
     return distanceFromOriginNode() < DISTANCE_TO_CONSIDER_AS_SAME;
   }
   
   public double distanceToDestinationNode() {
     Node nextNode = getDestinationNode();
     double d = Math.sqrt(Math.pow(lat - nextNode.lat,2)+Math.pow(lon - nextNode.lon,2));
     return d;
   }
   public double distanceFromOriginNode() {
     Node lastNode = getOriginNode();
     double d = Math.sqrt(Math.pow(lat - lastNode.lat,2)+Math.pow(lon - lastNode.lon,2));
     return d;
   }
   
   public Node getOriginNode() {
     return Global.openStreetMap.getNode(originNodeId);
   }
   
   public Node getDestinationNode() {
     return Global.openStreetMap.getNode(destinationNodeId);
   }
   
   // we're lying, the vehicle does some magic internally
   // to handle going from node to node. this is only
   // for node-to-node movements that aren't actual
   // transporation infrastructure (intersections, stop signs, etc)
   // for those situations the vehicle does nothing. the driver
   // must figure out where he wants to take the car
   // *this should only be called by the driver*
   public void tick(int tick) {
     Node lastNode = getOriginNode();
     Node nextNode = getDestinationNode();
     
     if (isAtDestinationNode()) {
       if (nextNode.connectedNodes.size() == 2) {
         // pretty obvious where the car goes..
         int i = nextNode.connectedNodes.indexOf(lastNode);
         setOriginNodeId(nextNode.id);
         if (i == 0) {
           setDestinationNodeId(nextNode.connectedNodes.get(1).id);
         }
         else {
           setDestinationNodeId(nextNode.connectedNodes.get(0).id);
         }
         state = "";
       }
       else if (nextNode.connectedNodes.size() == 1) {
         // we make the car go back down the path it came from
         // this happens when the car hits a dead end,
         // potentially remove this and make the driver handle it
         // String _originNodeId = originNodeId;
         // setOriginNodeId(destinationNodeId);
         // setDestinationNodeId(_originNodeId);
         state = "dead_end";
       }
       else {
         // state = "reached_intersection";
         // setVelocity(0);
         // 
         // // make the driver handle this, it's an intersection
         // // we assume that this is an intersection
         // // pick a random way to go.
         // int i = nextNode.connectedNodes.indexOf(lastNode);
         // setOriginNodeId(nextNode.id);
         // setDestinationNodeId(randomConnectedNode(nextNode, lastNode).id);
         // // state = "reached_intersection";
         // 
         // // we need to determine if the vehicle can actually make this turn
         // // and also merge this vehicle onto a lane appropriately
         // Random randomGenerator = Utils.randomNumberGenerator();
         // setOnLaneNumber(randomGenerator.nextInt(Oracle.wayBetweenNodes(originNodeId, destinationNodeId).lanes));
       }
     }
   }
   
   private Node randomConnectedNode(Node n, Node excludeNode) {
     Random randomGenerator = Utils.randomNumberGenerator();
     
     int nodeIndex = -1;
     int excludeNodeIndex = n.connectedNodes.indexOf(excludeNode);
     
     while (nodeIndex == -1 || nodeIndex == excludeNodeIndex) {
       nodeIndex = randomGenerator.nextInt(n.connectedNodes.size());
     }
     
     return n.connectedNodes.get(nodeIndex);
   }
   
   // the vehicle doesn't actuate anything,
   // it simply knows the state of the vehicle
   // in the simulator. also, it provides a convenience
   // methods to calculate the car in front and behind this vehicle.
   
   // double speed = 0.000001;
   double minSpeed = 0;
   double maxSpeed = 0.000002;
   int direction = 0; // 0 or 1
   // int lane = 0;
   
   public Vehicle vehicleBehind, vehicleInFront;
   private void calculateVehicleBehind() {
     Vehicle vehicle = null;
     
     for (Vehicle v : VehicleRegistry.allRegisteredVehicles()) {
       // Vehicle v = entry.getValue();
       
       if (this == v)
         continue;
       
       if (v.originNodeId.equals(originNodeId) && v.destinationNodeId.equals(destinationNodeId)) {
         // at least on the same way, the way we can check if this vehicle is
         // in behind us is by checking how far both vehicles are from their
         // destinationNode
         if (distanceToDestinationNode() < v.distanceToDestinationNode()) {
           vehicle = v;
         }
       }
     }
     
     vehicleBehind = vehicle;
   }
   
   public Intersection getNextIntersection() {
     Node destNode = Global.openStreetMap.getNode(destinationNodeId);
     // this is an intersection
     if (destNode.connectedNodes.size() > 2) {
       return IntersectionRegistry.getIntersectionAtNode(destNode);
     }
     // this is a simple node
     else if (destNode.connectedNodes.size() == 2) {
       return null;
     }
     // this is the end of the road
     else if (destNode.connectedNodes.size() == 1) {
       return null;
     }
     System.out.println("this also shouldn't happen...");
     return null;
   }
   
   public double getDistanceToNextIntersection() {
     Node destNode = Global.openStreetMap.getNode(destinationNodeId);
     // this is an intersection
     if (destNode.connectedNodes.size() > 2) {
       return distanceToDestinationNode();
     }
     // this is a simple node
     else if (destNode.connectedNodes.size() == 2) {
       return Integer.MAX_VALUE;
     }
     // this is the end of the road
     else if (destNode.connectedNodes.size() == 1) {
       return -1;
     }
     // what??
     // else if (destNode.connectedNodes.size() == 0) {
       System.out.println("this also shouldn't happen");
     //   return -1;
     // }
     return -1;
   }
   
   public double getDistanceToVehicleInFront() {
     calculateVehicleInFront();
     if (vehicleInFront == null) {
       return -1;
     }
     else {
       double d = Math.sqrt(Math.pow(lat - vehicleInFront.lat,2)+Math.pow(lon - vehicleInFront.lon,2));
       return d;
     }
   }
   
   private void calculateVehicleInFront() {
     Vehicle vehicle = null;
     
     List<String> vehicles = Oracle.vehiclesWithNodeAsOrigin(originNodeId);
     for (String vin : vehicles) {
       Vehicle v = VehicleRegistry.getVehicle(vin);
       
       if (v == null) {
         // System.out.println("vehicle with VIN " + vin + " is null (i am vehicle " + this.vin + ") (node "+originNodeId+")");
         continue;
       }
       
       if (v == this || !v.destinationNodeId.equals(destinationNodeId) || v.getOnLaneNumber() != getOnLaneNumber())
         continue;
       
       if (distanceToDestinationNode() > v.distanceToDestinationNode()) {
         if (vehicle == null || v.distanceToDestinationNode() > vehicle.distanceToDestinationNode())
           vehicle = v;
       }
     }
     
     // check one node ahead
     if (vehicle == null) {
       Node destNode = Global.openStreetMap.getNode(destinationNodeId);
       if (destNode.connectedNodes.size() == 2) {
         if (destNode.connectedNodes.get(0).id.equals(originNodeId))
           vehicle = findVehicleClosestToOriginNodeOnLane(destNode.id, destNode.connectedNodes.get(1).id, getOnLaneNumber());
         else
           vehicle = findVehicleClosestToOriginNodeOnLane(destNode.id, destNode.connectedNodes.get(0).id, getOnLaneNumber());
       }
     }
     
     vehicleInFront = vehicle;
   }
   
   private Vehicle findVehicleClosestToOriginNodeOnLane(String originNodeId, String destinationNodeId, int onLaneNumber) {
     List<String> vehicles = Oracle.vehiclesWithNodeAsOrigin(originNodeId);
     
     if (vehicles == null || vehicles.isEmpty()) {
       return null;
     }
     else {
       Vehicle vehicle = null;
       for (String vin : vehicles) {
         Vehicle v = VehicleRegistry.getVehicle(vin);
         if (v == null) {
           // System.out.println("2: vehicle with VIN " + vin + " is null (i am vehicle " + this.vin + ") (node "+originNodeId+")");
           continue;
         }
         if (!v.getDestinationNode().id.equals(destinationNodeId) || v.getOnLaneNumber() != onLaneNumber)
           continue;
         if (vehicle == null || v.distanceFromOriginNode() < vehicle.distanceFromOriginNode())
           vehicle = v;
       }
       return vehicle;
     }
   }
   
   private double angleOfTravel() {
     Node lastNode = getOriginNode();
     Node nextNode = getDestinationNode();
     double angle = -Math.atan2((nextNode.lat - lastNode.lat), (nextNode.lon - lastNode.lon));
     angle = Math.toDegrees(angle);
     if (angle < 0)
       angle = 360 + angle;
     return Math.toRadians(angle);
   }
   
   // move by velocity in the right direction
   public void setVelocity(double speed) {
     // System.out.format("setVelocity (%.8f)", speed);
     // System.out.println();
     
     
     Node lastNode = getOriginNode();
     Node nextNode = getDestinationNode();
     
     double oldC = distanceFromOriginNode();// Math.sqrt(Math.pow(lat - Double.valueOf(lastNode.lat),2)+Math.pow(lon - Double.valueOf(lastNode.lon),2));
     double newC = (oldC + speed);
     
     double angle = angleOfTravel();
     double deltaLat = Math.sin(angle)*newC;
     double deltaLon = Math.cos(angle)*newC;
     
     // based on this we can negate deltaLat or deltaLon to the correct sign
     if (Math.toDegrees(angle) < 0) {
       deltaLat*=1;
       deltaLon*=1;
     }
     else if (Math.toDegrees(angle) > 45) {
       deltaLat*=-1;
       deltaLon*=1;
     }
     else {
       deltaLat*=-1;
       deltaLon*=1;
     }
     
     double newLat = lastNode.lat + deltaLat;
     double newLon = lastNode.lon + deltaLon;
     
     velocity = new Vector2d(newLat-lat, newLon-lon);
   }
   
   public boolean isCollidingWith(Vehicle v) {
     if (Math.abs(lat-v.lat) < 0.0000005 && Math.abs(lon-v.lon) < 0.0000005) {
       return true;
     }
     return false;
   }
 }
