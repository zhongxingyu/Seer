 package fr.iutvalence.java.mp.Battleship;
 
 /**
  * This class represents a new naval battle game.
  * 
  * @author begous
  * 
  */
 // TODO FIXED this class should only have one public metod called 'play' that allows
 // to start and play a game. Every other method should be private (for internal use only)
 public class Battle
 {
     /**
      * Default number of ships
      */
     private final static int DEFAULT_NUMBER_OF_SHIPS = 5;
 
     /**
      * Default size of grid
      */
     private final static int DEFAULT_GRID_SIZE = 10;
 
     /**
      * Player 1 score ; number of ships sank at player 2
      */
     private int player1Score;
 
     /**
      * Player 2 score ; number of ships sank at player 1 ; computer or human ?
      */
     private int player2Score;
 
     /**
     * Number of turns 
      */
     private int numberOfTurns;
 
     /**
      * player 1 ship list
      */
     // TODO FIXED there are two players but only one ship list?
     private Ship[] Player1Ship;
 
     /**
      * player 2 ship list
      */
     private Ship[] Player2Ship;
     
     
    
     // TODO FIXED rewrite comment
     /**
      * Initialize a game (initialize scores and turn number)
      */
     public Battle()
     {
         this.numberOfTurns = 0;
 
         this.player1Score = 0;
         this.player2Score = 0;
 
         /*
          * this.player1Grid = new int[Battle.GRID_LENGTH][Battle.GRID_LENGTH] ;
          * this.player2Grid = new int[Battle.GRID_LENGTH][Battle.GRID_LENGTH] ;
          */
     }
 
     /**
      * return player 1 score
      * 
      * @return player 1 score
      */
     private int getPlayer1Score()
     {
         return this.player1Score;
     }
 
     /**
      * return player 2 score
      * 
      * @return player 2 score
      */
     private int getPlayer2Score()
     {
         return this.player2Score;
     }
 
     /**
      * return turn number
      * 
      * @return turn number
      */
     private int getNumberOfTurns()
     {
         return this.numberOfTurns;
     }
 
     /**
      * Add a turn
      */
     private void updateTurn()
     {
         this.numberOfTurns = this.numberOfTurns + 1;
     }
 
     /**
      * When the player 1 sink ship at player 2
      */
     private void updatePlayer1Score()
     {
         this.player1Score = this.player1Score + 1;
     }
 
     /**
      * When the player 2 sink ship at player 1
      */
     private void updatePlayer2Score()
     {
         this.player2Score = this.player2Score + 1;
     }
 
     // TODO fixed the class Battle should have **** only one public method ****
     // called "play"
 
     public void play()
     {
 
     }
 
     /**
      * 
      * Research in ships the targeted area. Target the ship with Ship.isHitArea
      * and update ship
      * 
      * @param c  coordinate of targeted area
      * @return true if coordinate found and so ship hit, else false (coordinate
      *         not found in the ship)
      */
     private boolean isHitShip(Coordinates c)
     {
 
     }
 
 }
