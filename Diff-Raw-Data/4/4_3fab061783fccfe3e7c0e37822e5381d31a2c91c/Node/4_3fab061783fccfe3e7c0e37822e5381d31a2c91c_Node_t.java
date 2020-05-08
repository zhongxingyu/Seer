 package oneway.g4;
 import oneway.sim.MovingCar;
 import oneway.sim.Parking;
 import java.util.*;
 
 /*
  * A class representing one state of the game - meaning some combination of
  * car positions and light settings. This class should handle interfacing between
  * oneway.g4 package representation of the states, and oneway.sim representation
  * of game states.
 */
 
 public class Node implements Comparable<Node> {
   private static final int LENGTHS_PER_SECOND = 4;
   private Segment[] segments;
   private ParkingLot[] lots;
   private ArrayList<Car> allCars;
   private Node parent = null;
   private int currentTime;
   
   // Initialize a Node from Simulator information
   public Node(int time, int nSegments, int[] lengths, MovingCar[] movingCars,
     Parking[] left, Parking[] right, int[] capacities, boolean[] llights, 
     boolean[] rlights){
     
     currentTime = time;
     
     // Initialize the allCars array
     allCars = new ArrayList<Car>();
     
     // Create proper number of parking lots and fill with cars
     lots = new ParkingLot[nSegments+1];
     for (int i = 0; i <= nSegments; i++) {
       lots[i] = new ParkingLot(capacities[i]);
       addCarsToParkingLot(left[i], lots[i]);
       addCarsToParkingLot(right[i], lots[i]);
     }
     
     // Create proper number of segments
     segments = new Segment[nSegments];
     for (int i = 0; i < nSegments; i++) {
       segments[i] = new Segment(lengths[i], llights[i], rlights[i]);
     }
     
     // Place the movingCars on the segments
     for(MovingCar movingCar : movingCars) {
       Direction dir = movingCar.dir > 0 ? Direction.RIGHT : Direction.LEFT;
       Segment segment = segments[movingCar.segment];
       Car car = new Car(movingCar.startTime, dir);
       
       allCars.add(car);
       segment.addCarAtPosition(car, movingCar.block);
     }
   }
   
   private Node(Node node) {
     currentTime = node.currentTime;
     
     allCars = new ArrayList<Car>();
     
     // Copy the parking lots and add their cars the arrays
     lots = new ParkingLot[node.lots.length];
     for (int i = 0; i < lots.length; i++) {
       lots[i] = node.lots[i].copy();
       List<Car> cars = lots[i].getCars();
       for (Car c : cars) {
         allCars.add(c);
       }
     }
     
     segments = new Segment[node.segments.length];
     for (int i = 0; i < segments.length; i++) {
       segments[i] = node.segments[i].copy();
       List<Car> cars = segments[i].getCars();
       for (Car c : cars) {
         allCars.add(c);
       }
     }
     
     for (Car c : node.allCars) {
       if (c.isComplete()) {
         allCars.add(c);
       }
     }
   }
   
   private void addCarsToParkingLot(List<Integer> cars, ParkingLot lot) {
    if (cars == null) { return; }
     for (Integer carStartTime : cars) {
       Car car = new Car(carStartTime, Direction.LEFT);
       allCars.add(car);
       lot.add(car);
     }
   }
 
   public ArrayList<Node> successors() {
     ArrayList<Node> children = new ArrayList<Node>();
     
     // max is the maximum number of light permutations
     int max = (int) Math.pow(2, segments.length);
     for(int i = 0; i < max; i++) {
       
       // Use a bit vector to find different permutations of lights
       int binaryLightRepresentation = max;
       boolean[] lights = new boolean[segments.length * 2];
       for (int j = 0; j < lights.length; j++) {
        lights[j] = binaryLightRepresentation % 2 == 0;
         binaryLightRepresentation = binaryLightRepresentation >> 1;
       }
       
       //Create the child, test it out, and keep it if its good
       Node child = new Node(this);
       child.setLights(lights);
       child.parent = this;
       if(child.playTurn() == false
           && child.noFutureCrashes()
           && child.noFutureOverflows()) {
         children.add(child);
       }
     }
     return children;
   }
   
   private void setLights(boolean[] lights) {
     int nSegments = segments.length;
     for(int i = 0; i < nSegments; i++) {
       segments[i].setLight(Direction.LEFT, lights[i]);
       segments[i].setLight(Direction.RIGHT, lights[i+nSegments]);
     }
   }
 
   // TODO: Should return false if overflows are guaranteed in the future
   private boolean noFutureOverflows() {
     return true;
   }
 
   // TODO: Should return false if crashes are guaranteed in the future
   private boolean noFutureCrashes() {
     return true;
   }
 
   /**
    * Plays out a single turn on the current node.
    * @return Whether or not the result of playing the turn results in overflow or crash
    */
   private boolean playTurn() {
     currentTime += 1;
     boolean fail = false;
     for (int i = 0; i < segments.length; i++) {
       fail = fail || segments[i].moveCarsForward(lots[i], lots[i+1]);
     }
     for (int i = 0; i < lots.length; i++) {
       Segment leftSegment = i > 0 ? segments[i-1] : null;
       Segment rightSegment = i < segments.length ? segments[i] : null;
       fail = fail || lots[i].unparkCars(leftSegment, rightSegment);
     }
     lots[0].removeCars(Direction.LEFT, currentTime);
     lots[lots.length-1].removeCars(Direction.RIGHT, currentTime);
     return fail;
   }
   
   public double f() {
     return g() + h();
   }
 
   // Path cost until this point
   private double g() {
     double cost = 0.0;
     double m = 0.0;
     for (Segment segment : segments) {
       m += (double) segment.getLength() / LENGTHS_PER_SECOND;
     }
 
     // Sum cost of each car
     for (Car car : allCars) {
       int l = 0;
       if (car.isComplete()) {
         l = car.getLatency();
       }
       else {
         l = currentTime - car.startTime;
         
       }
       cost += (l * Math.log10(l)) - (m * Math.log10(m));
     }
 
     return cost;
   }
 
   // Estimate cost from here to the end
   // Maybe use a feature vector of different heuristics and later train
   // to get optimal weights
   private double h() {
     // TODO: Make this
     return 10000;
   }
   
   @Override
   public int compareTo(Node other) {
     return (int) (this.f() - other.f());
   }
 
   public boolean[] getLLights() {
     boolean[] llights = new boolean[segments.length];
     for(int i = 0; i < llights.length; i++) {
       llights[i] = segments[i].isLeftGreen();
     }
     return llights;
   }
 
   public boolean[] getRLights() {
     boolean[] rlights = new boolean[segments.length];
     for(int i = 0; i < rlights.length; i++) {
       rlights[i] = segments[i].isRightGreen();
     }
     return rlights;
   }
 }
