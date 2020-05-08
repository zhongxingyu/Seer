 package cs2114.tiletraveler;
 
 import student.TestCase;
 
 /**
  * Test class for the Player object.
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards (MrZchuck)
  * @author Jacob Stenzel (sjacob95)
  * @version 2013.12.08
  */
 public class PlayerTest
     extends TestCase
 {
 
     private Player player;
     private Map    testMap;
 
 
     // private Stage stage;
     // private Location startLocation;
 
     /**
      * Sets up the test area using Stage 1 to test.
      */
     public void setUp()
     {
         Stage stage = new Stage1(10f);
         Location startLocation = new Location(5, 0);
         player = new Player(5f, 6f, 10f, stage);
         player = new Player(startLocation, 10f, stage);
         testMap = new Map(10);
     }
 
 
     /**
      * Tests the act() method in the Player class to ensure that it functions as
      * expected.
      */
     public void testAct()
     {
         player.setDirection(Direction.EAST);
         assertEquals(Direction.EAST, player.getDirection());
         player.act(Direction.WEST);
         assertEquals(true, player.isMoving());
         assertEquals(Direction.EAST, player.getDirection());
         player.movingStopped();
         assertEquals(false, player.isMoving());
         player.act(Direction.EAST);
         assertEquals(Direction.EAST, player.getDirection());
     }
 
 
     /**
      * Tests the move() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testMove()
     {
        assertEquals(player.getMoveTime(), 400, 0.01);
         player.move(null);
         player.movingStopped();
         player.move(Direction.SOUTH);
         player.move(Direction.NORTH);
         player.move(Direction.NORTH);
         player.move(Direction.WEST);
         player.move(Direction.WEST);
         player.move(Direction.SOUTH);
         player.move(Direction.WEST);
         player.move(Direction.EAST);
         assertEquals(new Location(5, 0), player.getLocation());
         InvalidLineException e =
             new InvalidLineException(new Location(1, 0), new Location(5, 7));
         assertEquals(
             "(1, 0) and (5, 7) do not form a valid line",
             e.getMessage());
         OutsideMapException t =
             new OutsideMapException(new Location(10, 20), testMap);
         assertEquals("The point(10, 20) does"
             + " not lie entirely on the map of size 10 + 10", t.getMessage());
 
     }
 
 
     /**
      * Test the player winning
      */
     public void testWon()
     {
         for (int x = 0; x < 10; x++)
         {
             player.move(Direction.NORTH);
         }
         player.checkAndMove();
         assertFalse(player.isJumping());
         assertEquals(true, player.checkCurrentStatus());
         assertEquals(new Location(5, 1), player.getLocation());
         player.isWon();
         player.resumeInput();
         player.blockInput();
         assertEquals(new Location(5, 1), player.getLocation());
     }
 
 
     /**
      * Test if the player is dead or not
      */
     public void testDie()
     {
         player.move(Direction.NORTH);
         player.checkEnemyCollision();
         assertEquals(true, player.checkCurrentStatus());
         player.move(Direction.WEST);
         assertEquals(true, player.checkCurrentStatus());
         player.die();
         assertEquals(true, player.checkCurrentStatus());
     }
 
 
     /**
      * Tests the nudge() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testNudge()
     {
         player.nudge(Direction.SOUTH);
         assertEquals(player.getLocation(), new Location(5, 0));
 
     }
 
 
     /**
      * Tests the jump() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testJump()
     {
         player.jump(Direction.SOUTH);
         assertEquals(new Location(5, -1), player.getLocation());
         player.jump(Direction.NORTH);
         player.jump(Direction.WEST);
         player.jump(Direction.EAST);
         assertEquals(new Location(5, 0), player.getLocation());
     }
 
 
     /**
      * Tests the walk() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testWalk()
     {
         player.walk(Direction.SOUTH);
         assertEquals(new Location(5, -1), player.getLocation());
         player.walk(Direction.NORTH);
         player.setRestImage();
         player.setJumpImage();
         player.setWalkImage();
         player.walk(Direction.WEST);
         player.setRestImage();
         player.setJumpImage();
         player.setWalkImage();
         player.walk(Direction.EAST);
         player.setRestImage();
         player.setJumpImage();
         player.setWalkImage();
         assertEquals(new Location(5, 0), player.getLocation());
     }
 
 
     /**
      * tests to set image
      */
     public void testSetImage()
     {
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 0), player.getLocation());
         player.act(Direction.NORTH);
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 1), player.getLocation());
         for (int x = 0; x < 4; x++)
         {
             player.act(Direction.NORTH);
         }
         player.act(Direction.WEST);
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 1), player.getLocation());
         player.act(Direction.EAST);
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 1), player.getLocation());
     }
 }
