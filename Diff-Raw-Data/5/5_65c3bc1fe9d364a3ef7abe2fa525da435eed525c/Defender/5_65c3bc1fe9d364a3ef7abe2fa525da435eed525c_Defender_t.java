 package team25;
 
 import hockey.api.*;
 
 public class Defender extends BasePlayer {
     // Number of defender
     public int getNumber() { return getIndex() == 1 ? 1 : 2; }
 
     // Name of defender
     public String getName() { return "Defender"; }
 
     // Make left defender left handed, right defender right handed.
     public boolean isLeftHanded() { return getIndex() == 1; }
 
     // Initiate
     public void init() {
       setAimOnStick(false);
     }
 
     private int normalY() {
      return (getIndex() == 1) ? -1000 : 1000;
     }
 
     // Defender intelligence
     public void step() {
       if (hasPuck()) {
         IPlayer center = getPlayer(5);
         if (center.getX() > getX())
           shoot(getPlayer(5), MAX_SHOT_SPEED);
         else
           shoot(GOAL_POSITION, MAX_SHOT_SPEED);
         return;
       }
 
       IPuck puck = getPuck();
       if (puck.isHeld()) {
         IPlayer holder = puck.getHolder();
         if (holder.isOpponent())
           skate(puck.getHolder(), MAX_SPEED);
         else
           skate(puck.getX(), normalY(), MAX_SPEED);
       }
       else {
        skate(-2000, normalY(), 1000);
       }
     }
 }
