 
 package game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents the game board. It knows where all the pieces are.
  * 
  * @author pganuza
  */
 public class Board {
     // Board has 63 positions, from number 0-home to 63-parchoca
     public final static int BOARD_SIZE = 63;
 
     // To know what pieces are in a position
     private final List<ISquare> board;
     // For convenience
     private final HomeSquare homeSquare;
     private final ParchocaSquare parchocaSquare;
 
     public Board() {
         this.board = new ArrayList<ISquare>();
 
         homeSquare = new HomeSquare(0, null);
         parchocaSquare = new ParchocaSquare(null);
         this.board.add(homeSquare);
         for (int i = 1; i < BOARD_SIZE; i++) {
             board.add(new Square(i));
         }
         this.board.add(parchocaSquare);
 
         setLinkedSquares();
         setPenaltySquares();
     }
 
     private void setLinkedSquares() {
         board.get(1).setLinkedSquare(board.get(5));
         board.get(5).setLinkedSquare(board.get(9));
         board.get(6).setLinkedSquare(board.get(12));
         board.get(9).setLinkedSquare(board.get(14));
         board.get(12).setLinkedSquare(board.get(6));
         board.get(14).setLinkedSquare(board.get(18));
         board.get(18).setLinkedSquare(board.get(23));
         board.get(23).setLinkedSquare(board.get(27));
         board.get(26).setLinkedSquare(board.get(53));
         board.get(27).setLinkedSquare(board.get(32));
         board.get(32).setLinkedSquare(board.get(36));
         board.get(26).setLinkedSquare(board.get(53));
         board.get(36).setLinkedSquare(board.get(41));
         board.get(41).setLinkedSquare(board.get(45));
         board.get(45).setLinkedSquare(board.get(50));
         board.get(50).setLinkedSquare(board.get(54));
         board.get(53).setLinkedSquare(board.get(26));
         board.get(54).setLinkedSquare(board.get(59));
         board.get(58).setLinkedSquare(board.get(0));
         board.get(59).setLinkedSquare(board.get(63));
     }
 
     private void setPenaltySquares() {
         board.get(19).setPenalty(-1);
         board.get(31).setPenalty(-1);
         board.get(42).setPenalty(-2);
         board.get(52).setPenalty(-1);
     }
 
     public boolean addPiece(final Piece piece) {
         if (piece == null) {
             throw new IllegalArgumentException("Piece to add is null!");
         }
         HomeSquare homeSquare = (HomeSquare) board.get(0);
         if (homeSquare.contains(piece)) {
             throw new IllegalArgumentException("Piece is already in!");
         }
         return homeSquare.add(piece) == null;
     }
 
     public boolean killPiece(final Piece piece) {
         if (piece == null) {
             throw new IllegalArgumentException("Need a piece to kill.");
         }
         if (homeSquare.contains(piece) || parchocaSquare.contains(piece)) {
             throw new IllegalStateException("Can't kill a piece when it is not on the road!");
         }
         boolean removed = piece.getSquare().remove(piece);
         return removed && homeSquare.add(piece) == null;
     }
 
     /**
      * Is responsible of executing the movement in case it is valid and informing of the result.
      * 
      * @param piece
      * @param jumps number of jumps to make
      * @return 20 = piece killed smbd; 10 = piece in Parchoca; +1 = extra turn; 0 = nothing special; <0 = penalty;
      */
     public int move(final Piece piece, final int jumps) {
        // TODO si se mata en puente o dados hacia atrás no se cuenta 20
         if (piece == null || isAtParchoca(piece)) {
             throw new IllegalArgumentException("Need a piece to move and it should not be at Parchoca already.");
         }
         if (jumps < 1) {
             throw new IllegalArgumentException("Can't jump less than 1 square.");
         }
 
        int result = 0;

         ISquare startSquare = piece.getSquare();
        int startSquareNumber = startSquare.getNumber();
        int finalSquareNumber = countJumps(startSquareNumber, jumps, true);

        ISquare finalSquare = board.get(finalSquareNumber);
        ISquare linkedSquare = finalSquare.getLinkedSquare();
 
        if (linkedSquare != null && !linkedSquare.isWall()) {
            finalSquare = linkedSquare;
 
            if (linkedSquare.getNumber() > finalSquareNumber) {
                result += 1;
             }
         }
 
        return result + executeMove(piece, finalSquare);
     }
 
     private int executeMove(final Piece piece, final ISquare finalSquare) {
         int ret = 0;
 
         piece.getSquare().remove(piece);
         Piece killedPiece = finalSquare.add(piece);
 
         if (killedPiece != null) {
             homeSquare.add(killedPiece);
             ret = 20;
         }
 
         if (finalSquare.getPenalty() < 0) {
             ret = finalSquare.getPenalty();
         } else if (isAtParchoca(piece)) {
             ret = 10;
         }
 
         return ret;
 
     }
 
     private int countJumps(final int startSquare, final int jumps, final boolean forward) {
         if (jumps == 0) {
             return startSquare;
         } else {
             if (forward) {
                 if (mayImoveIn(startSquare + 1)) { // If I can move to next square
                     return countJumps(startSquare + 1, jumps - 1, true);
                 } else {
                     if (mayImoveIn(startSquare - 1)) { // If I cannot move to next square but I can move to previous one
                         return countJumps(startSquare - 1, jumps - 1, false);
                     } else { // If I am stucked between two walls or something
                         return startSquare;
                     }
                 }
             } else {
                 if (mayImoveIn(startSquare - 1)) {
                     return countJumps(startSquare - 1, jumps - 1, false);
                 } else {
                     if (mayImoveIn(startSquare + 1)) {
                         return countJumps(startSquare + 1, jumps - 1, true);
                     } else { // If I am stucked between two walls or something
                         return startSquare;
                     }
                 }
             }
         }
     }
 
     private boolean mayImoveIn(final int squareNumber) {
         return (squareNumber > 0) && (squareNumber <= BOARD_SIZE) && !board.get(squareNumber).isWall();
     }
 
     public boolean isSquareEmpty(final int squareNumber) {
         if (squareNumber < 0 || squareNumber > BOARD_SIZE) {
             throw new IllegalArgumentException("Square number must be between 0 and " + BOARD_SIZE + ".");
         }
         return board.get(squareNumber).isEmpty();
     }
 
     public boolean isSquareAWall(final int squareNumber) {
         if (squareNumber < 0 || squareNumber > BOARD_SIZE) {
             throw new IllegalArgumentException("Square number must be between 0 and " + BOARD_SIZE + ".");
         }
         return board.get(squareNumber).isWall();
     }
 
     public boolean isAtHome(final Piece piece) {
         return homeSquare.contains(piece);
     }
 
     public boolean isAtParchoca(final Piece piece) {
         return parchocaSquare.contains(piece);
     }
 
     @Override
     public String toString() {
         // Line 0
         String ret = "+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 01 | 02 | 03 | 04 | 05 | 06 | 07 | 08 | 09 | 10 |";
         ret += "\n|" + occupantsToString(1) + "|" + occupantsToString(2) + "|" + occupantsToString(3) + "|"
                 + occupantsToString(4) + "|" + occupantsToString(5) + "|" + occupantsToString(6) + "|"
                 + occupantsToString(7) + "|" + occupantsToString(8) + "|" + occupantsToString(9) + "|"
                 + occupantsToString(10) + "|";
 
         // Line 1
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 |";
         ret += "\n|" + occupantsToString(11) + "|" + occupantsToString(12) + "|" + occupantsToString(13) + "|"
                 + occupantsToString(14) + "|" + occupantsToString(15) + "|" + occupantsToString(16) + "|"
                 + occupantsToString(17) + "|" + occupantsToString(18) + "|" + occupantsToString(19) + "|"
                 + occupantsToString(20) + "|";
 
         // Line 2
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 21 | 22 | 23 | 24 | 25 | 26 | 27 | 28 | 29 | 30 |";
         ret += "\n|" + occupantsToString(21) + "|" + occupantsToString(22) + "|" + occupantsToString(23) + "|"
                 + occupantsToString(24) + "|" + occupantsToString(25) + "|" + occupantsToString(26) + "|"
                 + occupantsToString(27) + "|" + occupantsToString(28) + "|" + occupantsToString(29) + "|"
                 + occupantsToString(30) + "|";
 
         // Line 3
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 31 | 32 | 33 | 34 | 35 | 36 | 37 | 38 | 39 | 40 |";
         ret += "\n|" + occupantsToString(31) + "|" + occupantsToString(32) + "|" + occupantsToString(33) + "|"
                 + occupantsToString(34) + "|" + occupantsToString(35) + "|" + occupantsToString(36) + "|"
                 + occupantsToString(37) + "|" + occupantsToString(38) + "|" + occupantsToString(39) + "|"
                 + occupantsToString(40) + "|";
 
         // Line 4
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 41 | 42 | 43 | 44 | 45 | 46 | 47 | 48 | 49 | 50 |";
         ret += "\n|" + occupantsToString(41) + "|" + occupantsToString(42) + "|" + occupantsToString(43) + "|"
                 + occupantsToString(44) + "|" + occupantsToString(45) + "|" + occupantsToString(46) + "|"
                 + occupantsToString(47) + "|" + occupantsToString(48) + "|" + occupantsToString(49) + "|"
                 + occupantsToString(50) + "|";
 
         // Line 5
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 51 | 52 | 53 | 54 | 55 | 56 | 57 | 58 | 59 | 60 |";
         ret += "\n|" + occupantsToString(51) + "|" + occupantsToString(52) + "|" + occupantsToString(53) + "|"
                 + occupantsToString(54) + "|" + occupantsToString(55) + "|" + occupantsToString(56) + "|"
                 + occupantsToString(57) + "|" + occupantsToString(58) + "|" + occupantsToString(59) + "|"
                 + occupantsToString(60) + "|";
 
         // Line 6
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
         ret += "\n| 61 | 62 | 63 |++++|++++|++++|++++|++++|++++|++++|";
         ret += "\n|" + occupantsToString(61) + "|" + occupantsToString(62) + "|" + occupantsToString(63)
                 + "|++++|++++|++++|++++|++++|++++|++++|";
 
         ret += "\n+----+----+----+----+----+----+----+----+----+----+";
 
         ret += "\nEn casa: ";
         for (Piece piece : homeSquare.getOccupants()) {
             ret += piece + " ";
         }
         ret += "\nEn Parchoca: ";
         for (Piece piece : parchocaSquare.getOccupants()) {
             ret += piece + " ";
         }
 
         return ret;
     }
 
     private String occupantsToString(final int i) {
         List<Piece> occupants = board.get(i).getOccupants();
         int numberOfOccupants = occupants.size();
 
         switch (numberOfOccupants) {
             case 1:
                 return " " + occupants.get(0) + "  ";
             case 2:
                 return " " + occupants.get(0) + occupants.get(1) + " ";
             default:
                 return "    ";
         }
     }
 }
