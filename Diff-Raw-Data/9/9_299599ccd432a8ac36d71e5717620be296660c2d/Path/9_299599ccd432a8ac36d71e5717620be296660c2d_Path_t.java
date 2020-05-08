 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class Path {
   private ArrayList<Point> positions;
   private ArrayList<Robot.Move> moves;
 
   public Path() {
     positions = new ArrayList<Point>();
     moves = new ArrayList<Robot.Move>();
   }
 
   public void add(final Point currentPosition, final Robot.Move move) {
     positions.add(new Point(currentPosition));
     moves.add(move);
   }
 
   public boolean inPosition(final Point position) {
     for(final Point p: positions) {
       if(position.getC() == p.getC() && position.getR() == p.getR())
         return true;
     }
     return false;
   }
 
   public boolean inPosition(final int x, final int y) {
     for(final Point p: positions) {
       if(x == p.getC() && x == p.getR())
         return true;
     }
     return false;
   }
 
   public void reverse() {
     Collections.reverse(positions);
    Collections.reverse(moves);
   }
 
   public void addAll(final Path newPath) {
     positions.addAll(newPath.positions);
     moves.addAll(newPath.moves);
   }
 
   public int size() {
     return positions.size();
   }
 
   public String toString() {
     StringBuilder s = new StringBuilder();
     for (Robot.Move move : moves) {
       if (move != null)
         s.append(move.toString());
     }
     return s.toString();
   }
 }
