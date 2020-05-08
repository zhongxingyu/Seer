 package fr.iutvalence.java.mp.tictactoe;
 
 /**
  * TicTacToe
  * 
  * This class is used to start and handle a new game.
  * 
  * @version 0.0
  * 
  * 2013-09-20
  * 
  * @author seneclap
  *
  * //TODO (FIXED) misplaced comment (class comment should be placed before any other tag)
  */
 public class TicTacToe
{
    // TODO (fix) move field declarations after constants
    /**
     * grid[Ordinate][Abscissa]
     * Game's grid
     */
    // TODO (fix) write declaration as int[][] grid
    private int grid[][];
    
     /**
      * EMPTY
      * This constant means there is no symbol in a square
      */
     public final static int EMPTY = 0;
     /**
      * CROSS
      * This constant means the square contains a cross
      */
     public final static int CROSS = 1;
     /**
      * CIRCLE
      * This constant means the square contains a circle
      */
     public final static int CIRCLE = 2;
     
     /**
      *  NewGame
      *  This method generates a 16x16 grid 
      *  and allows for a new game to start
      */
     public void NewGame()
     {
        // TODO (fix) define hard-coded values as constants
         int i, j; // Point respectively on an ordinate and an abscissa
        for (i=1;i<17;i++)
         {
            for (j=1;j<17;j++)
             {
                 this.grid[i][j] = TicTacToe.EMPTY;
             }
         }
     }
     
     /**
      * GameTurn
      * Puts the player's symbol in the square located in (x,y)
      * @param player The number of the player playing this turn
      * @param x Ordinate of the square in which the player places his symbol
      * @param y Abscissa of the square in which the player places his symbol
      */
     public void GameTurn(int player, int x, int y)
     {
         if (this.grid[x][y] == TicTacToe.EMPTY);
         {
             //TODO (fix) Complete this method
         }
     }
 }
