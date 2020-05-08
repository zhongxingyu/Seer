 package tablebases;
 
 import chess.SideToMove;
 
 /**
 * The positions in the tablebase with the specified side-to-move only.
  * 
  * @author Kestutis
  * 
  */
 public class TablebaseWithSTM {    
 
     /**
      * The computerized database containing all possible legal chess positions
      * and their evaluations, given the set of specific chess pieces.
      */
     private Tablebase tablebase;
 
     /** The side whose turn is to make a move - White or Black. */
     private SideToMove sideToMove;
     
     /**
      * Instantiates a new tablebase with either White-to-move-only or
      * Black-to-move-only chess positions.
      * 
      * @param tablebase
      *            computerized database containing all possible legal chess
      *            positions and their evaluations, given the set of specific
      *            chess pieces
      * @param sideTomove
      *            the side whose turn is to make a move - White or Black
      */
     public TablebaseWithSTM(Tablebase tablebase, SideToMove sideTomove) {
 	this.tablebase = tablebase;
 	this.sideToMove = sideTomove;
     }
     
     /**
      * Gets the side-to-move.
      * 
      * @return the side whose turn is to make a move - White or Black
      */
     public SideToMove getSideToMove() {
         return sideToMove;
     }
    
     /**
      * Sets the side to make the next move - White or Black.
      * 
      * @param sideToMove
      *            the new side-to-move
      */
     public void setSideToMove(SideToMove sideToMove) {
         this.sideToMove = sideToMove;
     }
     
     /**
      * Gets the tablebase.
      * 
      * @return the computerized database containing all possible legal chess
      *         positions and their evaluations, given the set of specified chess
      *         pieces
      */
     public Tablebase getTablebase() {
         return tablebase;
     }    
     
}
