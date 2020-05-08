 package reversi;
 
 public abstract class Player {
   public int color;
   public Player(int c) {
     color = c;
   }
   
   public Move getMove(Board b) {
    Move[] validMoves = b.getValidMoves(color);
    return validMoves[0];
   }
  
   public boolean hasMove(Board b) {
     return b.existValidMove(color);
   }
   
   public String toString() {
     return Integer.toString(color);
   }
 }
 
