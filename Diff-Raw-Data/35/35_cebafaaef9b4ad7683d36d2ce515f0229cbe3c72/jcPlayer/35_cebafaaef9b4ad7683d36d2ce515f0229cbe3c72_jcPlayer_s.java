 /****************************************************************************
  * jcPlayer.java - An abstract base class for all types of players
 * by Franois Dominic Larame
  *
  * Purpose: There are currently two types of players in JavaChess: the computer
  * player and the human player.  At a later time, other types may be added,
  * including the demo player (which picks its moves from a file, in which a
  * game has been recorded move by move) and a network player (an entity from
  * which the game obtains moves via a socket connection).
  *
  * History:
  * 08.06.00 Created
  ***************************************************************************/
 
 package javachess;
 
 
 /***************************************************************************
  * abstract public class jcPlayer
  **************************************************************************/
 
 abstract public class jcPlayer
 {
  /***************************************************************************
   * Constants
   **************************************************************************/
 
   public static final int SIDE_BLACK = 1;
   public static final int SIDE_WHITE = 0;
   public static final String PlayerStrings[] = { "WHITE", "BLACK" };
 
   // Data member: which side is this player representing?
   int Side;
 
   // Constructor
   public jcPlayer()
   {
   }
 
   // Accessors
   int GetSide()
   {
     return Side;
   }
   void SetSide( int s )
   {
     Side = s;
   }
 
   // abstract jcMove GetMove()
   // Ask the player to provide a move, given the current board situation
   public abstract jcMove GetMove( jcBoard theBoard );
 }
