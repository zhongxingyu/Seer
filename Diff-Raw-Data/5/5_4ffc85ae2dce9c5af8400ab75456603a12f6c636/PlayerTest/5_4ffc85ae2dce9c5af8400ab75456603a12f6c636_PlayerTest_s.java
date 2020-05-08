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
         stage.getEnemyMap().addEnemy(
             new Bug(10f, stage, new Location(5, 0), new Location(5, 2)));
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
         player.blockInput();
         player.act(Direction.NORTH);
     }
 
 
     /**
      * Tests the move() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testMove()
     {
         assertEquals(player.getMoveTime(), 250, 0.01);
         player.move(null);
         player.movingStopped();
         player.move(Direction.SOUTH);
         assertEquals(new Location(5, 0), player.getLocation());
         player.move(Direction.NORTH);
         player.move(Direction.NORTH);
         player.move(Direction.WEST);
         assertEquals(new Location(5, 0), player.getLocation());
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
         player.setLocation(new Location(2, 1));
         player.setJumpCount(1);
         player.move(Direction.WEST);
        assertEquals(new Location(2, 1), player.getLocation());
         player.setJumpCount(3);
         player.setLocation(new Location(5, 1));
         player.move(Direction.WEST);
         assertEquals(new Location(5, 1), player.getLocation());
 
     }
 
 
     /**
      * Test the player winning
      */
     public void testWon()
     {
         assertFalse(player.getWon());
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
         player.movingStopped();
         assertEquals(new Location(5, 0), player.getLocation());
         player.walk(Direction.WEST);
         player.setRestImage();
         player.setJumpImage();
         player.setWalkImage();
         player.movingStopped();
         assertEquals(new Location(4, 0), player.getLocation());
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
         player.setCurrentDirection(Direction.NORTH);
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 0), player.getLocation());
         for (int x = 0; x < 4; x++)
         {
             player.act(Direction.NORTH);
         }
         player.setCurrentDirection(Direction.WEST);
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 1), player.getLocation());
         player.setCurrentDirection(Direction.EAST);
         player.setRestImage();
         player.setWalkImage();
         player.setJumpImage();
         assertEquals(new Location(5, 1), player.getLocation());
     }
 
 
     /**
      * test enemy collision
      */
     public void testCheckEnemyCollision()
     {
         player.checkEnemyCollision();
         assertFalse(player.isAlive());
         player.nextMove();
         setUp();
         player.setLocation(new Location(5, 3));
         player.checkEnemyCollision();
         assertTrue(player.isAlive());
     }
 
 
     /**
      * test not on normal tile
      */
     public void testEmptyTile()
     {
         player.act(Direction.SOUTH);
         player.act(Direction.EAST);
         player.move(Direction.SOUTH);
         assertEquals(new Location(5, 0), player.getLocation());
     }
 
 
     /**
      * Tests the current status of the player.
      */
     public void testCurrentStatus()
     {
         player.setLocation(new Location(5, 10));
         player.checkAndMove();
         assertTrue(player.isAlive());
 
     }
 
 }
