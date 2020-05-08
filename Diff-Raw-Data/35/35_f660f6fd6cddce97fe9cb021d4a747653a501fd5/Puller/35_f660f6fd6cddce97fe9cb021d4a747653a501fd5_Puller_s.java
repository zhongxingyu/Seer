 /**
  * 
  */
 package sokoban.solvers;
 
 import java.util.LinkedList;
 
 import sokoban.Board;
 
 public class Puller implements Solver
 {
     private final Board startBoard;
     private Board board;
     
    private LinkedList<Box> boxes;
     
    public class Box {
         final int x, y;
        public Box(int x, int y) {
             this.x = x;
             this.y = y;
         }
     }
 
     public Puller(Board startBoard)
     {
         // Store all boxes
        boxes = new LinkedList<Box>();
         this.startBoard = startBoard;
         
         for (int i = 0; i < board.height; ++i) {
             for (int j = 0; j < board.width; ++j) {
                 if (board.cells[i][j] == Board.BOX) {
                    boxes.add(new Box(i, j));
                 }
             }
         }
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
