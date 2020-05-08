 /* Gameboard */
 
 public class Gameboard {
 
     public int[][] board;
     public int whiteChips;
     public int blackChips;
     public final totalPieces;
     
     /**
      * Gameboard constructor takes no parameters, and constructs
      * an 8 x 8 2-D array of integers.
      */
     public Gameboard() {
     }
 
     /**
      * getWhiteChips() returns the number of white chips remaining.
      */
 
     public int getWhiteChips() {
     }
 
     /**
      * getBlackChips() returns the number of black chips remaining.
      */
 
     public int getBlackChips() {
     }
 
     /**
      * neighbors() takes in a coordinate, (int X, int Y), as parameters
      * and returns a 3 x 3 2-D array of integers representing pieces
      * around it.
      */
 
     public int[][] neighbors(int x, int y) {
     }
 
     /**
      * cellContent() returns the an integer that indicates the piece
      * at the coordinate specified by parameters (int X, int Y).
      */
 
     public int cellContent(int x, int y) {
     }
 
     /**
      * isEmpty() checks whether or not the indicated coordinate
      * specified by parameters (int X, int Y) is empty.
      */
 
     public boolean isEmpty(int x, int y) {
     }
 
     /**
      * addPiece() puts a piece at the indicated coordinate specified
      * by parameters (int X, int Y).
      */
 
     public int addPiece(int x, int y) {
     }
 
     /**
      * removePiece() removes a piece at the indicated coordinate
      * specified by parameters (int X, int Y).
      */
 
     private void removePiece(int x, int y) {
     }
 
     /**
      * switchPiece() moves a piece from one coordinate to another.
      * More specifically, it takes parameters (X1, Y1, X2, Y2) and
      * moves a piece from (X1, Y1) to (X2, Y2).
      */
 
     public void switchPiece(int x1, int y1, int x2, int y2) {
     }
 
     /**
      * getRow() returns an integer array of pieces in the same row
      * as the piece at the indicated coordinate specified by parameters
      * (int X, int Y).
      */
 
     public int[] getRow(int x, int y) {
     }
 
     /**
      * getColumn() returns an integer array of pieces in the same column
      * as the piece at the indicated coordinate specified by parameters
      * (int X, int Y).
      */
 
     public int[] getColumn(int x, int y) {
     }
 
     /**
      * getDiagonal() returns an integer array of pieces in the same
      * diagonal as the piece and cardinality at the indicated
      * coordinate and direction specified by parameters (int X, int Y,
      * int Z). A 0 for int Z represents a diagonal from the northwestern
      * part of the board. A 1 for int Z represents a diagonal from the
      * northeastern part of the board.
      */
 
     public int[] diagonal(int x, int y, int z) {
     }
 
     /**
     * toString() returns a String representation of the board
      */
     public String toString() {
     }
 
    /**
         
 
 
 
