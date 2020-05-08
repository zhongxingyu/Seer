 package fr.iutvalence.java.mp.BrickBreaker;
 
 /**
  * This class is used to display the game
  * @author ochiers
  *
  */
 public class Display
 {
    
     /**
      * Method who build a line of the game, in order to be printed in console
      * 
      * @param bricks The tab of brick who need to be displayed
      * @param thePaddle The paddle who need to be displayed
      * @param theBall The ball who need to be displayed
      * @param pos The part of the game who is represented by this string
      * @param increment The part of the game who is represented by this string
      * @return A string who represent the state of a part of the game
      */
    private static String printLineInConsole(Brick[] bricks, Paddle thePaddle, Ball theBall, int pos, int increment)
     {
         String res = " # ";
         boolean aBrickIsWritten = false;
         int count = 0;
         for (int k = 0; k < Game.DEFAULT_MAP_WIDTH; k += Game.DEFAULT_MAP_WIDTH / (2 * Ball.DEFAULT_SIZE))
         {
             if (count < Game.DEFAULT_NUMBER_OF_BRICKS)
             {
                 if (Tools.isFloatBetween((float)bricks[count].getBrickBox().getBox().getY(), pos, pos + increment)
                         || Tools.isFloatBetween((float)bricks[count].getBrickBox().getBox().getY() + Brick.DEFAULT_WIDTH,
                                 pos, pos + increment))
                 {
                     aBrickIsWritten = true;
                     if (bricks[count].getState() != BrickState.DESTROYED_STATE)
                         res += " N ";
                     else
                         res += " X ";
                 }
                 else
                 {
                     res += "   ";
                 }
                 count++;
             }
             else
             {
                 res += "   ";
             }
 
         }
         if (!aBrickIsWritten && Tools.isFloatBetween((float)theBall.getBallBox().getBox().getY(), pos, pos + increment))
         {
             res = theBall.stringBallInConsole();
         }
 
         if (Tools.isFloatBetween((float)thePaddle.getPaddleBox().getBox().getY(), pos, pos + increment))
         {
             res = thePaddle.stringPaddleInConsole();
         }
 
         return res;
     }
     
 
     /**
      * The method create an ASCII-art representation of the game
      * @param bricks The tab of brick who need to be displayed
      * @param thePaddle The paddle who need to be displayed
      * @param theBall The ball who need to be displayed
      */
    public static void displayScreen(Brick[] bricks, Paddle thePaddle, Ball theBall)
     {
             System.out.print(" # ");
             for (int i = 0; i < Game.DEFAULT_MAP_WIDTH; i += Game.DEFAULT_MAP_WIDTH / (2 * Ball.DEFAULT_SIZE))
             {
                 System.out.print(" # ");
             }
             System.out.println();
             for (int j = 0; j < Game.DEFAULT_MAP_HEIGHT; j += Game.DEFAULT_MAP_HEIGHT / (2 * Ball.DEFAULT_SIZE))
             {
                 System.out.println(printLineInConsole(bricks, thePaddle, theBall, j, Game.DEFAULT_MAP_HEIGHT / (2 * Ball.DEFAULT_SIZE)));
             }
         
     }
 }
