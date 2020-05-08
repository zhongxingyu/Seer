 package main.pieces;
 
 import main.globals.Globals.Side;
 import main.position.Position;
 
 /**
  * @author Alex Telon
  */
 public interface IPiece {
 
     /**
      * Getter for the position of the piece
      * @return the position of the piece
      */
     public Position getPosition();
 
     /**
      * Try to move to newPosition, if invalid move return false
      * @param newPosition the new position of the piece.
      * Note: same position is never a valid move!
      * @return true if move was valid
      */
     public boolean tryMove(Position newPosition);
 
     /**
      * A kind of destructor for every piece, it cleans itself up from
      * the board.
      */
     public void remove();
 
     /**
      * Used to get the value of the piece
      * @return
      */
     public int getValue();
 
     /**
      *
      * @return which side the piece is on
      */
     public Side getSide();
 
 
 }
