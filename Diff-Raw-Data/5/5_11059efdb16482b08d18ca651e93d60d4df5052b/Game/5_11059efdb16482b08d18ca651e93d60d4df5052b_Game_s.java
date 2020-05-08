 package ConnectKSource;
 
 import DeveloperTools.*;
 
 public class Game
 {
     private Board board;
     private Player black;
     private Player red;
     private int moveCount;
     private int kInRow;
     private boolean debugMode;
 
     public Game(){
         this(new HumanPlayer("Black"), new HumanPlayer("Red"));
     }
 
     public Game(Player b, Player r) {
         this(b, r, 6, 7, 4, false);
     }
     
     public Game(int numRow, int numCol, int k){
         this(new HumanPlayer("Black"), new HumanPlayer("Red"), numRow, numCol, k, false);
     }
 
     public Game(Player b, Player r, int numRow, int numCol, int k, boolean debug) {
         board = new Board(numRow, numCol);
         black = b;
         red = r;
         moveCount = 0;
         kInRow = k;
         debugMode = debug;
     }
 
     public Player playGame()
     {
         Location lastMove = new Location(0,0);
         while(!board.isWon(lastMove, kInRow) && board.hasAvailableMove()) {
             try{
                Board copy = new Board(board);
 
                 Chip color = (moveCount%2 == 0) ? Chip. BLACK: Chip.RED;
                 Player nextPlayer = (moveCount%2 == 0) ? black : red;
                          
                int col = nextPlayer.makeMove(board, color);
                 lastMove = board.findPlacing(col);
 
                 boolean validMove = board.set(lastMove, color);
                 if(validMove)
                     moveCount++;
                 else
                     break; //invalid move from nextPlayer.makeMove
                     
                 if(debugMode){
                     System.out.println(board);
                     System.in.read();
                 }
             }catch(Exception e){
                 break; //nextPlayer.makeMove threw an error
             }
         }
         return returnWinner(lastMove);
 
     }
     
     public Board getCopyBoard(){
         return new Board(board);
     }
 
     //Assumes playGame() has taken place
     private Player returnWinner(Location lastMove)
     {
         if(board.hasAvailableMove() || board.isWon(lastMove, kInRow))
             return (moveCount%2 == 0) ? red : black;
         else //there was a draw
             return null;
     }
 
 
     public String toString() {
         return board.toString();
     }
 }
