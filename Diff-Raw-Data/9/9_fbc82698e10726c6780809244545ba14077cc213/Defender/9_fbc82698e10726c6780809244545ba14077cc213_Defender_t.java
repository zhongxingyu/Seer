 package team25;
 
 import hockey.api.*;
 
 public class Defender extends BasePlayer {
     // Number of defender
     public int getNumber() { return getIndex() == 1 ? 1 : 2; }
     
 
     // Name of defender
     public String getName() { 
       if (getIndex() == 1) {
         return "Birgitta";
       } else {
        return "Gööörgy"; 
       }
     }
 
     // Make left defender left handed, right defender right handed.
     public boolean isLeftHanded() { return getIndex() == 1; }
 
     // Initiate
     public void init() {
       setAimOnStick(false);
       SHOOTING_POSITION = new Position(1000, normalY());
       DEFENDING_POSITION = new Position(-1000, normalY());
      DEFENDING_MIDDLE_POSITION = new Position(-700, normalY());
      ATTACKING_MIDDLE_POSITION = new Position(700, normalY());
     }
 
     private int normalY() {
       return (getIndex() == 1) ? -700 : 700;
     }
 
     // Defender intelligence
     public void step() {
       setDebugMessage();
       IPuck puck = getPuck();
       if (hasPuck()) {
         IPlayer center = getPlayer(5);
         if (puckInForwardArea(puck)) {
           shoot(GOAL_POSITION, MAX_SHOT_SPEED);
         } else if (center.getX() > getX()) {
           shoot(getPlayer(5), MAX_SHOT_SPEED);
         }
         return;
       }
 
       if (puck.isHeld()) {
         IPlayer holder = puck.getHolder();
         if (holder.isOpponent()) {
           // Dom har den
           if (puckInDefenderArea(puck)) {
             skate(puck.getHolder(), MAX_SPEED);
           }
         } else {
           // Vi håller den.
           if (puckInForwardArea(puck)) {
             moveToShootingArea();
           } else if (puckInMiddleArea(puck)) {
             moveToAttackingMiddleArea();
           } else {
             /* skate(puck.getX(), normalY(), MAX_SPEED); */
           }
         }
       } else {
         if (puckInAssignedDefenderArea(puck)) {
           skate(puck, MAX_SPEED);
         } else if(puckInForwardArea(puck)) {
           moveToShootingArea();
         } else if (puckInMiddleArea(puck)) {
           moveToDefendingArea();
         }
       }
     }
 
 }
