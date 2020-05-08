 package iago;
 
 import iago.Move;
 import iago.players.Player.PlayerType;
 
 import java.util.Comparator;
 
 public class MoveComparator implements Comparator<Move> {
     
     private Board board;
     private PlayerType player;
     
     /**
      * Compare two moves based on number of captures
      * 
      * XXX Note: This compares elements in reverse! Higher
      * scoring moves will be sorted before ("less") than lower
      * scoring moves
      * 
      * @param arg0
      * @param arg1
      * @return sort key value
      */
     @Override
     public int compare(Move arg0, Move arg1) {
         return simpleScore(arg1) - simpleScore(arg0);
     }
     
     private int simpleScore(Move m) {
        return board.scoreMove(m, player);
     }
     
     public void setBoard(Board b) {
         this.board = b;
     }
     
     public void setPlayer(PlayerType p) {
         this.player = p;
     }
     
     public MoveComparator(Board board, PlayerType player) {
         this.board = board;
         this.player = player;
     }
     
     public MoveComparator(MoveComparator other) {
         this(other.board, other.player);
     }
 }
