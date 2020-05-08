 package com.edinarobotics.zed.subsystems;
 
 import com.edinarobotics.utils.subsystems.Subsystem1816;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Relay;
 
 public class Collector extends Subsystem1816 {
     private Relay leftStar;
     private Relay rightStar;
     private Relay collectorLifter;
     private CollectorDirection collectorDirection;
     private CollectorLiftDirection collectorLiftDirection;
     private DigitalInput upperLimitSwitch;
     private DigitalInput lowerLimitSwitch;
     
     public Collector(int leftStar, int rightStar, int collectorLifter,
             int collectorUpperLimitSwitch, int collectorLowerLimitSwitch) {
         super("Collector");
         this.leftStar = new Relay(leftStar);
         this.rightStar = new Relay(rightStar);
         this.collectorLifter = new Relay(collectorLifter);
         collectorDirection = CollectorDirection.COLLECTOR_STOP;
         collectorLiftDirection = CollectorLiftDirection.COLLECTOR_LIFT_STOP;
        this.upperLimitSwitch = new DigitalInput(collectorUpperLimitSwitch);
        this.lowerLimitSwitch = new DigitalInput(collectorLowerLimitSwitch);
     }
     
     /**
      * Sets the direction of the collector mechanisms.
      * A positive value will set the collector to a forward direction. Forward
      * is defined as the direction that will bring discs into the robot.<br/>
      * A negative value will set the collector to a backwards direction.<br/>
      * A zero value will stop the collector.
      * @param direction Sets the collector to the direction as given above.
      */
     public void setCollectorDirection(CollectorDirection direction){
         this.collectorDirection = direction;
         update();
     }
     
     public void setCollectorLiftDirection(CollectorLiftDirection direction) {
         this.collectorLiftDirection = direction;
         update();
     }
     
     public CollectorDirection getCollectorDirection() {
         return collectorDirection;
     }
     
     public CollectorLiftDirection getCollectorLiftDirection() {
         return collectorLiftDirection;
     }
 
     public void update(){
         leftStar.set(collectorDirection.getLeftStarRelayValue());
         rightStar.set(collectorDirection.getRightStarRelayValue());
         CollectorLiftDirection processLiftDirection = collectorLiftDirection;
         if(processLiftDirection.equals(CollectorLiftDirection.COLLECTOR_LIFT_UP) &&
                 getUpperLimitSwitch()) {
             processLiftDirection = CollectorLiftDirection.COLLECTOR_LIFT_STOP;
         }
         if(processLiftDirection.equals(CollectorLiftDirection.COLLECTOR_LIFT_DOWN) &&
                 getLowerLimitSwitch()) {
             processLiftDirection = CollectorLiftDirection.COLLECTOR_LIFT_STOP;
         }
         collectorLifter.set(processLiftDirection.getRelayValue());
     }
     
     public boolean getUpperLimitSwitch() {
         return upperLimitSwitch.get();
     }
     
     public boolean getLowerLimitSwitch() {
         return lowerLimitSwitch.get();
     }
     
     public static class CollectorDirection {
         public static final CollectorDirection COLLECTOR_IN = new CollectorDirection((byte)1);
         public static final CollectorDirection COLLECTOR_OUT = new CollectorDirection((byte)-1);
         public static final CollectorDirection COLLECTOR_STOP = new CollectorDirection((byte)0);
         
         private byte value;
         
         private CollectorDirection(byte value){
             this.value = value;
         }
         
         protected byte getValue(){
             return value;
         }
         
         public boolean equals(Object other){
             if(other instanceof CollectorDirection){
                 return ((CollectorDirection)other).getValue() == this.getValue();
             }
             return false;
         }
         
         public int hashCode(){
             return new Byte(getValue()).hashCode();
         }
         
         public Relay.Value getLeftStarRelayValue(){
             if(equals(COLLECTOR_IN)){
                 return Relay.Value.kForward;
             }
             else if(equals(COLLECTOR_OUT)){
                 return Relay.Value.kReverse;
             }
             return Relay.Value.kOff;
         }
         
         public Relay.Value getRightStarRelayValue(){
             if(equals(COLLECTOR_IN)){
                 return Relay.Value.kForward;
             }
             else if(equals(COLLECTOR_OUT)){
                 return Relay.Value.kReverse;
             }
             return Relay.Value.kOff;
         }
     }
     
     public static class CollectorLiftDirection {
         public static final CollectorLiftDirection COLLECTOR_LIFT_UP = new CollectorLiftDirection((byte)1);
         public static final CollectorLiftDirection COLLECTOR_LIFT_DOWN = new CollectorLiftDirection((byte)-1);
         public static final CollectorLiftDirection COLLECTOR_LIFT_STOP = new CollectorLiftDirection((byte)0);
         
         private byte value;
         
         private CollectorLiftDirection(byte value){
             this.value = value;
         }
         
         protected byte getValue(){
             return value;
         }
         
         public boolean equals(Object other){
             if(other instanceof CollectorLiftDirection){
                 return ((CollectorLiftDirection)other).getValue() == this.getValue();
             }
             return false;
         }
         
         public int hashCode(){
             return new Byte(getValue()).hashCode();
         }
         
         public Relay.Value getRelayValue(){
             if(equals(COLLECTOR_LIFT_UP)){
                 return Relay.Value.kForward;
             }
             else if(equals(COLLECTOR_LIFT_DOWN)){
                 return Relay.Value.kReverse;
             }
             return Relay.Value.kOff;
         }
     }
 }
