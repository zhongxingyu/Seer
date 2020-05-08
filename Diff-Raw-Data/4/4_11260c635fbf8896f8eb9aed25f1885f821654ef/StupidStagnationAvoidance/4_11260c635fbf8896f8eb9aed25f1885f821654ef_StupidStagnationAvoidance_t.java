 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brooks.impl;
 
 import brooks.Behaviour;
 import device.ResponseDevices;
 import device.SensoryInputs;
 import device.input.ProximityArray;
 import device.output.Wheels;
 import utils.Pair;
 
 /**
  *
  * @author Nicklas
  */
 public class StupidStagnationAvoidance implements Behaviour {
 
     public static final int pushOrientationThreshold = 600;
     public static final int pushCounterLimit = 200;
     public static final int neightbourProximityThreshold = 100;
     public static final int isPushingThreshold = 1;
     private int pushCounter = 0;
     private RepositioningState repositioningState = RepositioningState.NONE;
     private boolean turnLeft = true;
     private int stateCounter = 0;
     private int orientationCounterLimit = 30;
     private int orientationCounter = 0;
 
     @Override
     public boolean trigger(SensoryInputs input, ResponseDevices devices) {
         Pair<Integer, Double> smallestReading = input.getLightArray().getSmallestValueAndIndex();
         if (isRepositioningActived()) {
             return true;
         } else if (isNotPushing(smallestReading, input, devices)) {
             pushCounter = 0;
             return false;
         } else if (isNotCorrectlyOrientet(smallestReading, input, devices)) {
             orientationCounter++;
             return (orientationCounter > orientationCounterLimit);
         } else {
             pushCounter++;
             if (pushCounter > pushCounterLimit) {
                 int numberOfNeighbours = getNumberOfNeightbours(input, devices);
                 if (numberOfNeighbours >= 2) {
                     pushCounter = 0;
                     return false;
                 } else if (numberOfNeighbours == 1) {
                     boolean b = cointossDecision(0.5);
                     if (!b) {
                         pushCounter = 0;
                     }
                     return b;
                 }
             }
             return (pushCounter > pushCounterLimit);
         }
     }
 
     @Override
     public void execute(SensoryInputs input, ResponseDevices devices) {
         pushCounter = 0;
         orientationCounter = 0;
         if (isNotCorrectlyOrientet(null, input, devices) && !isRepositioningActived()) {
             ProximityArray proximity = input.getProximityArray();
             double frontalDiff = proximity.getDistanceSensorValue(0) - proximity.getDistanceSensorValue(7);
             alignWithBox(frontalDiff, devices);
         } else {
             if (repositioningState == RepositioningState.NONE) {
                 turnLeft = cointossDecision(0.5);
                 repositioningState = RepositioningState.REVERSE;
             }
             processRepositioningState(repositioningState, input, devices);
         }
     }
 
     private void alignWithBox(double frontalDiff, ResponseDevices devices) {
         setSpeedBasedOnAngle(frontalDiff / 10000, devices);
     }
 
     private void setSpeedBasedOnAngle(double angle, ResponseDevices devices) {
         double left = 1.0, right = 1.0;
        double coef = 5.0;
         if (angle > 0) {
             right -= angle * coef;
             right *= -1;
         } else {
             left += angle * coef;
             left *= -1;
         }
        System.out.println("Aligning: "+left+" "+right);
         devices.getWheels().moveWheels(left, right);
     }
 
     private void processRepositioningState(RepositioningState state, SensoryInputs input, ResponseDevices devices) {
         Wheels wheels = devices.getWheels();
         switch (state) {
             case REVERSE:
                 wheels.backward();
                 break;
             case TURNOFFSET:
                 wheels.spinAngle(state.getTimestepLimit() * ((turnLeft) ? -1 : 1));
                 stateCounter += 89;
                 break;
             case OFFSET:
                 wheels.forward();
                 break;
             case TURNFORWARD:
                 wheels.spinAngle(state.getTimestepLimit() * ((turnLeft) ? 1 : -1));
                 stateCounter += 89;
                 break;
             case FORWARD:
                 wheels.forward();
                 break;
         }
         stateCounter++;
         if (stateCounter == state.getTimestepLimit()) {
             repositioningState = state.getNextState();
             stateCounter = 0;
         }
     }
 
     private boolean isNotCorrectlyOrientet(Pair<Integer, Double> smallestReading, SensoryInputs input, ResponseDevices devices) {
         ProximityArray proximity = input.getProximityArray();
         double frontalDiff = proximity.getDistanceSensorValue(0) - proximity.getDistanceSensorValue(7);
 
         return Math.abs(frontalDiff) > pushOrientationThreshold;
     }
 
     private boolean isRepositioningActived() {
         return repositioningState != RepositioningState.NONE;
     }
 
     private int getNumberOfNeightbours(SensoryInputs input, ResponseDevices devices) {
         double[] proximitySideAndBack = {
             input.getProximityArray().getDistanceSensorValue(2),
             input.getProximityArray().getDistanceSensorValue(3),
             input.getProximityArray().getDistanceSensorValue(4),
             input.getProximityArray().getDistanceSensorValue(5),};
         int neighbourCounter = 0;
         for (double proximity : proximitySideAndBack) {
             if (proximity > neightbourProximityThreshold) {
                 neighbourCounter++;
             }
         }
         return neighbourCounter;
     }
 
     private boolean isNotPushing(Pair<Integer, Double> smallestReading, SensoryInputs input, ResponseDevices devices) {
         return smallestReading.t > isPushingThreshold;
     }
 
     private boolean cointossDecision(double limit) {
         return Math.random() > limit;
     }
 }
