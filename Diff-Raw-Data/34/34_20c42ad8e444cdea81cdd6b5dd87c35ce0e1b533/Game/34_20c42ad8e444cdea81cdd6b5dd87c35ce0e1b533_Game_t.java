 package fr.iutvalence.java.mp.BrickBreaker;
 
 import java.awt.geom.Rectangle2D;
 import java.util.Random;
 
 /**
  * Called when a game is created
  * 
  * @author ochiers soulierc
  * */
 public class Game
 {
     /**
      * Width size of the container
      */
     public static int DEFAULT_MAP_WIDTH = 800;
 
     /**
      * Height size of the container
      */
     public static int DEFAULT_MAP_HEIGHT = 600;
 
     /**
      * This number is the number of bricks in the level, But it's a temp
      * constant because this number can change depending on the level
      */
     public static final int DEFAULT_NUMBER_OF_BRICKS = 5;
 
     /**
      * Maximal number of player's lives
      */
     private static final int MAXIMAL_LIVES = 3;
 
     /**
      * Paddle initial position
      */
     private static final float PADDLE_INITIAL_POSITION = 550.0F;
 
     /**
      * Number that specify that there is no collision between ball and brick
      */
     private static final int NO_SIDE_COLLISION = 0;
 
     /**
      * Number that specify a collision between the ball and the left or the
      * right side of a brick
      */
     private static final int COLLISION_LEFT_RIGHT_SIDE = 1;
 
     /**
      * Number that specify a collision between the ball and the top or the
      * bottom side of a brick
      */
     private static final int COLLISION_TOP_BOTTOM_SIDE = 2;
 
     /**
      * Number that specify a collision between the ball and a corner of a brick
      */
     private static final int COLLISION_CORNER = 3;
 
     /**
     * The object who display this game
     */
    private Display display;
    
    /**
      * Number of lives
      */
     private int currentNumberOfBalls;
 
     /**
      * The ball that shoots the bricks
      */
     private Ball theBall;
 
     /**
      * The paddle where the ball bounces
      */
     private Paddle thePaddle;
 
     /**
      * This tab contains all bricks of the game That save their position, their
      * state
      * 
      */
     private Brick[] bricks;
 
     /**
      * This variable is a random number to give another direction to the ball
      */
     private Random rand;
 
     /**
      * Number of brick currently not in state destroyed
      */
     private int currentNumberOfBricks;
 
     /**
      * Used to stop the game
      */
     private boolean stopGame;
 
     /**
      * This is the procedure where the game find its start It's the
      * initialization of the game : a new paddle, a new ball and a new tab of
      * bricks is created, the number of lifes is 3
      * 
      */
     public Game()
     {
         super();
 
         int yPositionBricks = Brick.DEFAULT_HEIGHT;
         this.stopGame = false;
         this.currentNumberOfBalls = Game.MAXIMAL_LIVES;
 
         this.theBall = new Ball(new Position(100, 300));
         this.theBall.setTrajectory(new Trajectory(0.5F,0.5F));
         
         // For the tests, the paddle size is the total game size
         this.thePaddle = new Paddle(new Position(0, Game.PADDLE_INITIAL_POSITION), 400);
 
         this.bricks = new Brick[Game.DEFAULT_NUMBER_OF_BRICKS];
         for (int i = 0; i < Game.DEFAULT_NUMBER_OF_BRICKS; i++)
         {
             this.bricks[i] = new Brick(new Position(i * Brick.DEFAULT_WIDTH, yPositionBricks));
         }
         this.rand = new Random();
         this.currentNumberOfBricks = Game.DEFAULT_NUMBER_OF_BRICKS;
        this.display = new Display();
     }
 
     /**
      * method who move the ball and take care of collisions
      */
     public void play()
     {
         boolean thereWasAcollision = false;
         int collisionSide = 0;
 
         while (!this.stopGame)
         {
            this.display.displayScreen(this.bricks, this.thePaddle, this.theBall);
             if (!manageCollisionWithGamePanelSides())
             {
                 manageCollisionWithPaddle();
             }
             
             Position temp = new Position((float)this.theBall.getBallBox().getBox().getX(),(float)this.theBall.getBallBox().getBox().getY());
             this.theBall.updatePositions(temp.translate(
                     this.theBall.getTrajectory().getBCoefficient(), this.theBall.getTrajectory().getACoefficient()));
 
             if (this.currentNumberOfBalls != 0 && this.currentNumberOfBricks == 0)
             {
                 onVictory();
             }
 
             if (!this.stopGame)
             {
                 int j;
                 thereWasAcollision = false;
                 for (j = 0; j < Game.DEFAULT_NUMBER_OF_BRICKS; j++)
                 {
                     if (this.bricks[j].getState() != BrickState.DESTROYED_STATE)
                     {
                         collisionSide = isBallInCollisionWithBrick(this.bricks[j]);
                         onCollisionWithBrick(thereWasAcollision, j, collisionSide);
                         if (collisionSide != NO_SIDE_COLLISION)
                             thereWasAcollision = true;
                     }
                 }
                 // System.out.println(this.theBall.toString());
             }
 
             try
             {
                 Thread.sleep(5);
             }
             catch (InterruptedException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
     }
 
     /**
      * Called when the ball collide a brick
      * 
      * @param thereWasAcollision
      *            true if the ball collide at least two bricks in the same time
      * @param indexOfBrick
      *            index of the brick who is collided
      * @param collisionSide
      *            side collided
      */
     private void onCollisionWithBrick(boolean thereWasAcollision, int indexOfBrick, int collisionSide)
     {
         if (collisionSide != NO_SIDE_COLLISION)
         {
             this.bricks[indexOfBrick].setState(BrickState.DESTROYED_STATE);
             this.currentNumberOfBricks--;
             if (!thereWasAcollision)
             {
                 switch (collisionSide)
                 {
                 case COLLISION_LEFT_RIGHT_SIDE:
                     this.theBall.getTrajectory().reverseBCoefficient();
                     break;
                 case COLLISION_TOP_BOTTOM_SIDE:
                     this.theBall.getTrajectory().reverseACoefficient();
                     break;
                 case COLLISION_CORNER:
                     this.theBall.getTrajectory().reverseACoefficient();
                     this.theBall.getTrajectory().reverseBCoefficient();
                     break;
                 }
             }
 
         }
     }
 
     /**
      * Called when the player win the game
      */
     private void onVictory()
     {
         System.out.println("GAGNE !");
         this.stopGame = true;
     }
 
     /**
      * Called when the player lose the game
      */
     private void onLose()
     {
         if (this.currentNumberOfBalls == 0)
         {
             System.out.println("perdu");
             this.stopGame = true;
         }
         else
         {
             this.currentNumberOfBalls--;
             this.theBall = new Ball(new Position(300, 100));
         }
     }
 
     /**
      * Function that says if the ball is in collision with the brick, in the tab
      * of bricks to the index i
      * 
      * @param brick
      *            The brick to compare in the game tab brick
      * @return 0 if no collision, 1 in case of collision with left or right
      *         sides, 2 in case of collision with top or bottom sides
      */
     private int isBallInCollisionWithBrick(Brick brick)
     {
         int result = NO_SIDE_COLLISION;
         Rectangle2D.Float dest = new Rectangle2D.Float();
         dest = this.theBall.getBallBox().getRectangleFromIntersectionWithOtherCollisionBox(brick.getBrickBox());
         if (dest.getWidth() < 0 && dest.getHeight() < 0)
         {
             return NO_SIDE_COLLISION;
         }
         if ((int)dest.getWidth() == 1 && (int)dest.getHeight() == 1)
         {
             result = COLLISION_CORNER;
         }
         // TODO (fix) simplify
         // We don't know how to simplify ...
         else if ((int)dest.getWidth() == 1 && dest.getHeight() > 1)
         {
             result = COLLISION_LEFT_RIGHT_SIDE;
         }
 
         // TODO (fix) simplify
         // We don't know how to simplify ...
         else if (dest.getWidth() > 1 && (int)dest.getHeight() == 1)
         {
             result = COLLISION_TOP_BOTTOM_SIDE;
         }
         return result;
     }
 
     /**
      * Method who manages collisions between ball and paddle, if the ball
      * doesn't hit the paddle the player loses
      */
     private void manageCollisionWithPaddle()
     {
         if (this.theBall.getBallBox().getBox().getY() + Ball.DEFAULT_SIZE <= this.thePaddle
                 .getPaddleBox().getBox().getY())
         {
             Rectangle2D.Float dest = this.theBall.getBallBox().getRectangleFromIntersectionWithOtherCollisionBox(
                     this.thePaddle.getPaddleBox());
             if (dest.getWidth() >= 0 && dest.getHeight() >= 0)
             {
                 this.theBall.setTrajectory(new Trajectory(-1 * this.rand.nextFloat(), this.theBall.getTrajectory()
                         .getBCoefficient()));
             }
         }
         else
         {
             onLose();
         }
     }
 
     /**
      * Method who manage collision between the ball and the sides of the game
      * panel, change the ball's direction when collided and return true
      * 
      * @return true if the ball hit a side
      */
     private boolean manageCollisionWithGamePanelSides()
     {
         boolean thereIsCollision = false;
         if (!Tools.isFloatBetween((float)this.theBall.getBallBox().getBox().getX(), 0, Game.DEFAULT_MAP_WIDTH)
                 || !Tools.isFloatBetween((float)this.theBall.getBallBox().getBox().getX() + Ball.DEFAULT_SIZE, 0,
                         Game.DEFAULT_MAP_WIDTH))
         {
             this.theBall.getTrajectory().reverseBCoefficient();
             thereIsCollision = true;
         }
         else if (this.theBall.getBallBox().getBox().getY() <= 0)
         {
             this.theBall.getTrajectory().reverseACoefficient();
             thereIsCollision = true;
         }
         return thereIsCollision;
     }
 }
