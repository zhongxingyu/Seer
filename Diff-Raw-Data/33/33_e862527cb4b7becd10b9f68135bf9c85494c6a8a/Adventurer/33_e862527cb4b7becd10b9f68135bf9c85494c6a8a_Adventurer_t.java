 //megan is my homeboy. Like, totallyz
 import kareltherobot.*;
 import java.awt.Color;
 /**
  * Adventurer navigates sight-unseen to the lost beeper mine and picks up all the beepers.
  * This class codes to complete the Most Excellent Adventure.
  * 
  * @author <your name here>
  *    With assistance from:  <list of people who helped>
  * @version 
  */
 public class Adventurer extends Robot
 {
     // constructor for Adventurer
     public Adventurer (int street, int avenue, Direction direction, int beepers)
     {
         super(street, avenue, direction, beepers);
     }
     // methods for class Adventurer
     
     /*findMine()
      * findMine() is the main class for the Excellent Adventure. It completes the problem.
      * preconditions: robot is in the Lost Beeper Mine universe
      * postconditions: robot has picked up all beepers in the Lost Beeper Mine.
      */
     
     public void findMine()
     {
         walkNorth();
         walkEast();
         followTrail();
         walkToWall();
         walkEastToWall();
         followTreasureMapTrail();
         walkForTen();
         escapeMaze();
         leftUntilClear();
         afterMazeDilemma();
         getToDaChoppah();
         //pickAllMineBeepers();
     }
     
     /*walkNorth()
      * walkNorth() moves the robot one space to the North, ending facing North
      * preconditions: Robot is not near a wall directly to the North
      * postconditions: Robot is facing north one space north of its original location
      */
     
     public void walkNorth()
     {
         faceNorth();
         move();
     }
     
      /*walkEast()
      * walkEast() moves the robot one space to the East, ending facing East
      * preconditions:  Robot is not near a wall directly to the East
      * postconditions: Robot is facing east one space east of its original location
      */
     
     public void walkEast()
     {
         faceEast();
         move();
     }
     
     /*faceNorth()
      * faceNorth() turns the robot around until it is facing North
      * preconditions: none
      * postconditions: Robot is facing North
      */
     
     public void faceNorth()
     {
         while (!facingNorth())
         {
             turnLeft();
         }
     }
     
     /*faceEast()
      * faceEast() turns the robot around until it is facing East
      * preconditions: none
      * postconditions: Robot is facing East
      */
     
     public void faceEast()
     {
         while (!facingEast())
         {
             turnLeft();
         }
     }
     
     /*faceSouth()
      * faceSouth() turns the robot around until it is facing South
      * preconditions: none
      * postconditions: Robot is facing South
      */
     
     public void faceSouth()
     {
         while (!facingSouth())
         {
             turnLeft();
         }
     }
     
     /*faceWest()
      * faceWest() turns the robot around until it is facing West
      * preconditions: none
      * postconditions: Robot is facing West
      */
     
     public void faceWest()
     {
         while (!facingWest())
         {
             turnLeft();
         }
     }
     
     /*followTrail()
      * follows the first trail in the Excellent Adventure.
      * preconditions: robot is one corner away from beeper trail, facing in the direction of the trail.
      * postconditions: robot is at the end of the beeper trail, having picked up all the beepers within it.
      */
     
     public void followTrail()
     {
         move();
         while (!twoBeepers())
         {
             pickBeeper();
             move();
             if (!nextToABeeper())
             {
                 turnAround();
                 move();
                 turnRight();
                 move();
                 if (!nextToABeeper())
                 {
                     turnAround();
                     move();
                     move();
                 }
             }
                     
         }
         pickBeeper();
         pickBeeper();
     }
     
     /*twoBeppers()
      * determines whether the robot is standing on top of two beepers.
      * preconditions: robot is next to at least one beeper
      * postconditions: the robot has returned true if there are exactly two, false if there are
      * fewer or more
      */
     
     
     public boolean twoBeepers()
     {
         pickBeeper();
         if (nextToABeeper())
         {
             pickBeeper();
             if (!nextToABeeper())
             {
               putBeeper();
               putBeeper();
               return true;
             }
             else
             {
                 putBeeper();
                 putBeeper();
                 return false;
             }
         }
               
         else
         {
             putBeeper();
             return false;
         }
     }
     
     /*walkToWall()
      * walks until the robot is one half square away from a wall.
      * pre: none
      * post: robot is one half square away from the nearest wall in the direction it was originall facing.
      * 
      */
     public void walkToWall()
     {
         while (frontIsClear())
         {
             move();
         }
     }
     
     /*walkEastToWall()
      * turns the robot East, then executes walkToWall()
      * pre: none
      * post: robot is one half square away from the nearest wall east of its original position,
      * regardless of original direction
      * 
      */
     public void walkEastToWall()
     {
         faceEast();
         walkToWall();
     }
     
     /*followTreasureMapTrail()
      * moves the robot according to the treasure map rules stipulated.
      * pre: robot is standing on a corner with at least one beeper
      * post: robot has navigated treasure map trail
      * 
      */
     
     public void followTreasureMapTrail()
     {
         while (!fiveBeepers())
         {
             if (oneBeeper())
             {
                pickBeeper();
                 faceNorth();
                 while (!nextToABeeper())
                 {
                     move();
                 }
             }
             
             if (twoBeepers())
             {
                for (int i=0; i<2; i++) {pickBeeper();}
                 faceWest();
                 while (!nextToABeeper())
                 {
                     move();
                 }
             }
             
             if (threeBeepers())
             {
                for (int i=0; i<3; i++) {pickBeeper();}
                 faceSouth();
                 while (!nextToABeeper())
                 {
                     move();
                 }
             }
             
             if (fourBeepers())
             {
                for (int i=0; i<4; i++) {pickBeeper();}
                 faceEast();
                 while (!nextToABeeper())
                 {
                     move();
                 }
             }
         }
         
         if (facingNorth()) {faceSouth();}
         if (facingWest()) {faceNorth();}
         if (facingSouth() || facingEast()) {faceEast();}
     }
     
     /*oneBeeper()
      * checks if there is exactly one beeper on a corner
      */
     
     public boolean oneBeeper()
     {
         if (nextToABeeper()) 
         {
             pickBeeper();
         
         if (nextToABeeper())
         {
             for (int i=0; i<1; i++) {putBeeper();}
             return false;
         }
         else
         {
             for (int i=0; i<1; i++) {putBeeper();}
             return true;
         }
    }
        else
        {
            return false;
         }
         
     }
     
     /*threeBeppers()
      * determines whether the robot is standing on top of three beepers.
      * preconditions: robot is next to at least one beeper
      * postconditions: the robot has returned true if there are exactly three, false if there are
      * fewer or more
      */
     
     
     public boolean threeBeepers()
     {
         pickBeeper();
         if (nextToABeeper())
         {
             pickBeeper();
             if (nextToABeeper())
             {
               pickBeeper();
               if (!nextToABeeper())
               {
                   for (int i=0; i<3; i++) {putBeeper();}
                   return true;
               }
               else
               {
                   for (int i=0; i<3; i++) {putBeeper();}
                   return false;
                 }
             }
             else
             {
                 for (int i=0; i<2; i++) {putBeeper();}
                 return false;
             }
         }
               
         else
         {
             putBeeper();
             return false;
         }
     }
     
     /*fourBeppers()
      * determines whether the robot is standing on top of four beepers.
      * preconditions: robot is next to at least one beeper
      * postconditions: the robot has returned true if there are exactly four, false if there are
      * fewer or more
      */
     
     public boolean fourBeepers()
     {
         pickBeeper();
         if (nextToABeeper())
         {
             pickBeeper();
             if (nextToABeeper())
             {
               pickBeeper();
               if (nextToABeeper())
               {
                   pickBeeper();
                   if (!nextToABeeper())
                   {
                       for (int i=0; i<4; i++) {putBeeper();}
                       return true;
                   }
                   else
                   {
                       for (int i=0; i<4; i++) {putBeeper();}
                       return false;
                   }
             }
             else
             {
                 for (int i=0; i<3; i++) {putBeeper();}
                 return false;
             }
         }
               
         else
         {
             for (int i=0; i<2; i++) {putBeeper();}
             return false;
         }
     }
     else
     {
         putBeeper();
         return false;
     }
   }
   
   /*fiveBeppers()
      * determines whether the robot is standing on top of five beepers.
      * preconditions: robot is next to at least one beeper
      * postconditions: the robot has returned true if there are exactly five, false if there are
      * fewer or more
      */
     
     public boolean fiveBeepers()
     {
         pickBeeper();
         if (nextToABeeper())
         {
             pickBeeper();
             if (nextToABeeper())
             {
               pickBeeper();
               if (nextToABeeper())
               {
                   pickBeeper();
                   if (nextToABeeper())
                   {
                       pickBeeper();
                       if (!nextToABeeper())
                       {
                           for (int i=0; i<5; i++) {putBeeper();}
                           return true;
                         }
                         else
                         {
                             for (int i=0; i<5; i++) {putBeeper();}
                             return false;
                     }
                 }
                 else
                 {
                     for (int i=0; i<4; i++) {putBeeper();}
                     return false;
                 }
             }
             else
             {
                 for (int i=0; i<3; i++) {putBeeper();}
                 return false;
             }
         }
               
         else
         {
             putBeeper();
             putBeeper();
             return false;
         }
     }
     else
     {
         putBeeper();
         return false;
     }
    }
    
   public void walkForTen()
   {
       for (int i=0; i<10; i++) {move();}
    
     }
     
   public void escapeMaze()
   {
      while (!nextToABeeper() || behindABeeper()) {followWallRight();}
    }
    
  public boolean behindABeeper()
  {
      move();
      if (nextToABeeper())
      {
          turnAround();
          move();
          turnAround();
          return true;
        }
      else
      {
          turnAround();
          move();
          turnAround();
          return false;
        }
     }
     
   public void followWallRight()
   {
       if (frontIsClear())
             {
                 move();
                 turnRight();
                 if (frontIsClear())
                 {
                     move();
                     turnRight();
                     if (frontIsClear())
                     {
                         move();
                     }
                     else
                     {
                         turnLeft();
                     }
                 }
                 else if (!frontIsClear())
                 {
                     turnLeft();
                 }
             }
             else
             {
                 turnLeft();
             }
         }
     
   public void leftUntilClear()
   {
       while (!frontIsClear()) {turnLeft();}
     }
   
   public void afterMazeDilemma()
   {
       if (leftIsClear()) {walkToWall();}
       else {walkForTen();}
       turnRight();
       walkToWall();
     }
   
   public boolean leftIsClear()
   {
       turnLeft();
       if (frontIsClear())
       {
           turnRight();
           return true;
         }
       else
       {
           turnRight();
           return false;
         }
     }
     
   public void turnAround()
   {
       turnLeft();
       turnLeft();
     }
   
   public void turnRight()
   {
       for (int i=0; i<3; i++) {turnLeft();}
     }
     
   
   public void getToDaChoppah()
   {
       faceEast();
       if (!nextToABeeper()) 
       {
           move();
           move();
           faceNorth();
         }
       move();
       getToDaChoppah();
       move();
       
 
     }
 }
     
     
