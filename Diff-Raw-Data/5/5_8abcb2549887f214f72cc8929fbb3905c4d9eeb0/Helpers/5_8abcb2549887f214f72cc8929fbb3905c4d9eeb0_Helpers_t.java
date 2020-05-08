 package game.strategy.tom;
 
 import game.Cell;
 import game.Command;
 import game.State;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.PriorityQueue;
 
 
 public class Helpers {
   
   
   /**
    * Moves to the given target destination in the shortest way.
    * But does not consider rock movement. 
    * 
    * @param s starting state
    * @param destCol destination column
    * @param destRow destination row
    * @return commands leading to destination or null if no wa yfound
    */
   public static List<Command> moveToSimple(State s, int destCol, int destRow) {
     
     WayCoordinateDirected[][] m = new WayCoordinateDirected[s.board.width][s.board.height];
     PriorityQueue<WayCoordinateDirected> p = new PriorityQueue<WayCoordinateDirected>();
     WayCoordinateDirected wc = new WayCoordinateDirected(s.robotCol, s.robotRow, destCol, destRow, 0, null);
      
     
     while (wc != null && (wc.col != destCol || wc.row != destRow)) {
       if (m[wc.col][wc.row] == null || m[wc.col][wc.row].steps > wc.steps) { // not already better way
 
         Cell c = s.board.get(wc.col, wc.row);
        if (c == Cell.Earth || c == Cell.Empty || c == Cell.Lambda || c == Cell.Robot || c == Cell.Razor) { // may move there
           
           m[wc.col][wc.row] = wc;
 
           // generate follow up moves
           p.add(new WayCoordinateDirected(wc.col - 1, wc.row, destCol, destRow, wc.steps + 1, Command.Left));
           p.add(new WayCoordinateDirected(wc.col + 1, wc.row, destCol, destRow, wc.steps + 1, Command.Right));
           p.add(new WayCoordinateDirected(wc.col, wc.row + 1, destCol, destRow, wc.steps + 1, Command.Up));
           if (s.board.get(wc.col, wc.row + 1) != Cell.Rock)
             p.add(new WayCoordinateDirected(wc.col, wc.row - 1, destCol, destRow, wc.steps + 1, Command.Down));
 
         }
       } 
       wc = p.poll();
     }
      //here: wc is destination or null 
     if (wc == null) return null;
     
     //now build commands that led here
     Command[] cmds= new Command[wc.steps];
     
     for (int i = wc.steps - 1; i > 0; i--) {
       cmds[i] = wc.steppedHereWith;
       switch (wc.steppedHereWith) {
       case Left: 
         wc = m[wc.col+1][wc.row];
         break;
       case Right:
         wc = m[wc.col-1][wc.row];
         break;
       case Up:
         wc = m[wc.col][wc.row-1];
         break;
       case Down: 
         wc = m[wc.col][wc.row+1];
       }
     }
     if (cmds.length > 0)
       cmds[0] = wc.steppedHereWith;
     
     
     return Arrays.asList(cmds);    
   }
 
   
   /**
    * Moves to the n-th closest given target cell in the shortest way.
    * But does not consider rock movement. 
    * 
    * @param s starting state
    * @param destCol destination column
    * @param destRow destination row
    * @return commands leading to destination or null if no way found
    */
   public static List<Command> moveToCell(State s, Cell goal, final int N) {
     
     WayCoordinateSimple[][] m = new WayCoordinateSimple[s.board.width][s.board.height];
     PriorityQueue<WayCoordinateSimple> p = new PriorityQueue<WayCoordinateSimple>();
     WayCoordinateSimple wc = new WayCoordinateSimple(s.robotCol, s.robotRow, 0, null);
      
     int seenTargets = 0;
     
     while (wc != null) {
       Cell c = s.board.get(wc.col, wc.row);
       
       if (m[wc.col][wc.row] == null) { // not already better way
         
         if (c == goal) { //target cell found
           seenTargets++;
           m[wc.col][wc.row] = wc;
           if (seenTargets == N) break;
         }
         
        if (c == Cell.Earth || c == Cell.Empty || c == Cell.Lambda || c == Cell.Robot || c == Cell.Razor) { // may move there
           
           m[wc.col][wc.row] = wc;
 
           // generate follow up moves
           p.add(new WayCoordinateSimple(wc.col - 1, wc.row, wc.steps + 1, Command.Left));
           p.add(new WayCoordinateSimple(wc.col + 1, wc.row, wc.steps + 1, Command.Right));
           p.add(new WayCoordinateSimple(wc.col, wc.row + 1, wc.steps + 1, Command.Up));
           if (s.board.get(wc.col, wc.row + 1) != Cell.Rock)
             p.add(new WayCoordinateSimple(wc.col, wc.row - 1, wc.steps + 1, Command.Down));
 
         }
       } 
       wc = p.poll();
     }
      //here: wc is destination or null 
     if (wc == null) return null;
     
     //now build commands that led here
     Command[] cmds= new Command[wc.steps];
     
     for (int i = wc.steps - 1; i > 0; i--) {
       cmds[i] = wc.steppedHereWith;
       switch (wc.steppedHereWith) {
       case Left: 
         wc = m[wc.col+1][wc.row];
         break;
       case Right:
         wc = m[wc.col-1][wc.row];
         break;
       case Up:
         wc = m[wc.col][wc.row-1];
         break;
       case Down: 
         wc = m[wc.col][wc.row+1];
       }
     }
     if (cmds.length > 0)
       cmds[0] = wc.steppedHereWith;
     
     
     return Arrays.asList(cmds);    
   }
   
   
   /**
    * Comutes the manhattan distance between two coordinates.
    * 
    * @param robotCol
    * @param robotRow
    * @param col
    * @param row
    * @return
    */
   public static int manhattan(int col1, int row1, int col2, int row2) {
     return Math.abs(col1 - col2) + Math.abs(row1 - row2);
   }
 }
