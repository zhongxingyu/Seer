 /**
  * 
  */
 package sokoban.solvers;
 
 import java.util.LinkedList;
 
 import sokoban.Board;
 
 public class Puller implements Solver
 {
     private final int DEPTH_LIMIT = 1000;
 
     private final Board startBoard;
     private Board board;
     
     public class Box {
         final int x, y;
         public Box(int x, int y) {
             this.x = x;
             this.y = y;
         }
     }
 
 
     public Puller(Board startBoard)
     {
         this.startBoard = startBoard;
     }
     
     /**
      * Resets the board to the starting board.
      */
     private void reset() {
         
     }
     
     @Override
     public String solve()
     {
         do {
             reset();
             
             do {
                 Box box = chooseBox();
                 while (moveBox(box)) { }
                 
            } while (!deadlock());
        } while (!solved());
         
         return null;
     }
 
     private boolean solved()
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     private boolean deadlock()
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     private boolean moveBox(Box box)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     private Box chooseBox()
     {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
