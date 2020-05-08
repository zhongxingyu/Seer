 
 public class Board {
   private BoardSquare[][] squares;
   private King[] kings;
   
   public Board() {
     String color = "white";
     squares = new BoardSquare[8][8];
     kings = new King[2];
     
     for (int i = 0; i < 8; i++) {
       for (int j = 0; j < 8; j++) {
         squares[i][j] = new BoardSquare(color);
         color = (color == "white" ? "black" : "white" );
       }
       color = (color == "white" ? "black" : "white" );
     }
     
     // set up the black pieces
     for (int j = 0; j < 8; j++) {
       squares[1][j].occupy(new Pawn("black", j, 1));
     }
     
     squares[0][0].occupy(new Rook("black", 0, 0));
     squares[0][7].occupy(new Rook("black", 0, 7));
     
     squares[0][1].occupy(new Horse("black", 0, 1));
     squares[0][6].occupy(new Horse("black", 0, 6));
     
     squares[0][2].occupy(new Bishop("black", 0, 2));
     squares[0][5].occupy(new Bishop("black", 0, 5));
     
     squares[0][3].occupy(new Queen("black", 0, 3));
     squares[0][4].occupy(new King("black", 0, 4));
     kings[0] = (King) squares[0][4].getOccupant();
     
     // set up the white pieces
     for (int j = 0; j < 8; j++) {
       squares[6][j].occupy(new Pawn("white", j, 1));
     }
     
     squares[7][0].occupy(new Rook("white", 7, 0));
     squares[7][7].occupy(new Rook("white", 7, 7));
     
     squares[7][1].occupy(new Horse("white", 7, 1));
     squares[7][6].occupy(new Horse("white", 7, 6));
     
     squares[7][2].occupy(new Bishop("white", 7, 2));
     squares[7][5].occupy(new Bishop("white", 7, 5));
     
     squares[7][3].occupy(new Queen("white", 7, 3));
     squares[7][4].occupy(new King("black", 7, 4));
     kings[1] = (King) squares[7][4].getOccupant();
   }
   
   public int movePieceTo(int rank, int file, int drank, int dfile) {
     Piece piece = squares[rank][file].getOccupant();
     
    // there is no piece at the source square
    if (piece == null) return -1;
    // the source is outside the bounds of the board
    if (rank < 0 || file < 0 || rank > 7 || file > 7) return -2;
    // the destination is outside the bounds of the board
    if (drank < 0 || dfile < 0 || drank > 7 || dfile > 7) return -3;
     
     // just move the piece
     if (piece.canMoveTo(drank, dfile) && squares[rank][file] == null) {
       squares[drank][dfile].occupy(piece);
       squares[rank][file].vacate();
       checkForUpgrades();
       return 0;
     }
     
     if (piece.canMoveTo(drank, dfile) && squares[rank][file] != null) {
       if (squares[rank][file].getOccupant().getColor().equals(piece.color)) {
         // the piece cannot capture it's own bretheren
         return 1;
       } else {
         // there is a capture happening!!!
         squares[drank][dfile].occupy(piece);
         squares[rank][file].vacate();
         checkForUpgrades();
         return 2;
       }
     } else {
       // the piece can't move to there
       return 3;
     }
   }
   
   public void checkForUpgrades() {
     
   }
   
   public String toString() {
     String ret = "";
     
     for (int i = 0; i < 8; i++) {
       for (int j = 0; j < 8; j++) {
         ret = ret.concat(squares[i][j].toString() + " ");
       }
       ret = ret.concat("\n");
     }
  
     return ret;
   }
 }
