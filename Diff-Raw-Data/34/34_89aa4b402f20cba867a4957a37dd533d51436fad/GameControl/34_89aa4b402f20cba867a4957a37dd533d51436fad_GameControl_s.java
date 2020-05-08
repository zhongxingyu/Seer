 
 package game;
 
 public class GameControl {
     private final Board board;
     private final PlayerManager playerManager;
 
     public GameControl() {
         this.board = new Board();
         this.playerManager = new PlayerManager();
     }
 
     public void addPlayer(final Player player) {
         playerManager.addPlayer(player);
         for (Piece piece : player.getPieces()) {
             board.addPiece(piece);
         }
     }
 
     public void start() {
         for (int i = 0; i < Dice.throwDice() - 1; i++) {
             playerManager.nextPlayer();
         }
 
         while (!gameOver()) {
             System.out.println("************** Aún no hemos terminado ************************");
             // How many 6 in a row has the player had
             int result6 = 0;
 
             Player playerCurrent = playerManager.nextPlayer();
             System.out.println("Le toca a " + playerCurrent.getName() + "." + playerCurrent.getColor());
             Piece pieceSelected = null;
             boolean tiraOtraVez = true;
 
             while (tiraOtraVez) {
                 tiraOtraVez = false;
                 int diceResult = Dice.throwDice();
                 int jumps = diceResult;
                 System.out.println("Dice: " + diceResult);
 
                 if (diceResult == 6) {
                     tiraOtraVez = true;
 
                     if (++result6 == 3) {
                         board.killPiece(pieceSelected);
                         tiraOtraVez = false;
                         jumps = 0;
                     } else if (playerCurrent.allPiecesOut()) {
                         jumps = 7;
                     }
                 } else {
                     result6 = 0;
                 }
 
                 while (jumps > 0) {
                     System.out.println("--------------------------- \nJumps: " + jumps);
                     pieceSelected = playerCurrent.selectPieceToMove(board, jumps);
                     int moveResult = board.move(pieceSelected, jumps);
 
                     switch (moveResult) {
                         case 21:
                             tiraOtraVez = true;
                         case 20:
                             jumps = 20;
                             break;
                         case 11:
                             tiraOtraVez = true;
                         case 10:
                             jumps = 10;
                             break;
                         case 1:
                             tiraOtraVez = true;
                             jumps = 0;
                             break;
                         default:
                             tiraOtraVez = false;
                             jumps = 0;
                            playerCurrent.setPenalty(moveResult);
                     }
 
                     System.out.println(board);
                 }
             }
         }
     }
 
     private boolean gameOver() {
         return playerManager.numberOfPlayersStillPlaying() == 0;
     }
 
     public void printState() {
         System.out.println(board);
     }
 }
