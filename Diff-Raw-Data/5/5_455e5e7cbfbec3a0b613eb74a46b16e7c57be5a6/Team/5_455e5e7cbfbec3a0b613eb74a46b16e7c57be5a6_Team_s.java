 package team15;
 
 import java.awt.Color;
 
 import hockey.api.Player;
 import hockey.api.GoalKeeper;
 import hockey.api.ITeam;
 import hockey.api.*;
 
 public class Team implements ITeam {
     public int getLuckyNumber() { return -1; }
     public String getShortName() { return "KBYTE"; }
     public String getTeamName() { return "Killa Bytes"; }
     public Color getTeamColor() { return new Color(255, 105, 180);  }
     public Color getSecondaryTeamColor() { return Color.BLACK; }
     public GoalKeeper getGoalKeeper() { return new ShooterGoalie(); }
     public Player getPlayer(int index) {
         if (index % 2 == 0) {
             return new Shooter(index);
         }
         else {
             return new Bullie(index);
         }
     }
 }
 
 class Shooter extends Player {
    private static int[] numbers = {1, 2, 3};
     private static String[] names = {
             "", "Glock", "Ruger", "Smith", "Wesson", "Colt"
     };
     private int index;
 
     public Shooter(int index) { this.index = index; }
     public int getNumber() { return numbers[index]; }
     public String getName() { return names[index]; }
     public boolean isLeftHanded() { return false; }
     public void step() {
         if (hasPuck()) // If player has the puck...
             if (Math.abs(Util.dangle(getHeading(), // ...and is turned towards the goal.
                     Util.datan2(0 - getY(),
                             2500 - getX()))) < 90) {
                 int target = (int)(Math.random()*200)-100;
                 shoot(2600, target, 10000); // Shoot.
             } else // If not
                 skate(2600, 0, 1000); // Turn towards goal.
         else // If not
             skate(getPuck(), 1000); // Get the puck.
     }
 }
 
 class Bullie extends Player {
    private static int[] numbers = {4, 5, 6};
     private static String[] names = {
             "", "Biffen", "Doris", "Harry", "Vanheden", "Sickan"
     };
     private int index;
 
     public Bullie(int index) { this.index = index; }
     public int getNumber() { return numbers[index]; }
     public String getName() { return names[index]; }
     public boolean isLeftHanded() { return false; }
     public void step() {
         if (hasPuck()) // If we have the puck.
             skate(2600, 0, 1000); // Skate towards the goal.
         else if (Util.dist(getX() - getPuck().getX(), // If the puck is within 5m.
                 getY() - getPuck().getY()) < 500)
             skate(getPuck(), 1000); // Get puck
         else {
             IPlayer best = null;
             for (int i = 0; i < 12; ++i) { // Loop through all players.
                 IPlayer cur = getPlayer(i);
 
                 if (cur.isOpponent() && // If player is opponent...
                         (best == null ||
                                 Util.dist(getX() - cur.getX(), // ...and closest so far...
                                         getY() - cur.getY()) <
                                         Util.dist(getX() - best.getX(),
                                                 getY() - best.getY())))
                     best = cur; // ...save player.
             }
 
             skate(best, 1000); // Tackle closest opponent.
         }
     }
 }
 
 
 
 class ShooterGoalie extends GoalKeeper {
     public int getNumber() { return 1; }
     public String getName() { return "Beretta"; }
     public boolean isLeftHanded() { return false; }
     public void step() {
         if (hasPuck()) // If goalie has the puck
             shoot(2600, 0, 10000); // Shoot (or throw)
         skate(-2550, 0, 200); // Stand in the middle of the goal.
         turn(getPuck(), MAX_TURN_SPEED); // Turn towards puck.
     }
 }
