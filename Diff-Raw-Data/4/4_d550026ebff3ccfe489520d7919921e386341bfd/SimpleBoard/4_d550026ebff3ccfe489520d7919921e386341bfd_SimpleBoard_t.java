 package simpleboard;
 
 import base.board.Board;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: 16-Sep-2009
  * Time: 2:09:05 PM
  * This class is meant to be a simple implementation of Board -- it's not going to be the fastest
  */
 public class SimpleBoard implements Board {
     private final static int[] initBoard = new int[]{
             3,3,3,3,3,3,3,3,3,3,
             3,0,0,0,0,0,0,0,0,3,
             3,0,0,0,0,0,0,0,0,3,
             3,0,0,0,0,0,0,0,0,3,
            3,0,0,0,-1,1,0,0,0,3,
            3,0,0,0,1,-1,0,0,0,3,
             3,0,0,0,0,0,0,0,0,3,
             3,0,0,0,0,0,0,0,0,3,
             3,0,0,0,0,0,0,0,0,3,
             3,3,3,3,3,3,3,3,3,3};
     private final int[] board = new int[100];
     public SimpleBoard() {
         
     }
 
     @Override public int getSquare(int location) {
         return board[location];
     }
 
     @Override public void resetToStart() {
         System.arraycopy(initBoard,0,board,0,initBoard.length);
     }
 }
