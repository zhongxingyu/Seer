 import java.util.Scanner;
 
 /**
  * Driver for playing a game of Tic Tac Toe
  */
 public class TicTacToeGame {
   public static enum Player {
     X("X"), O("O"), Nobody(" ");
 
     private final String playerName;
 
     Player(String name) {
       this.playerName = name;
     }
 
     public String toString() {
       return this.playerName;
     }
   }
 
   private static final String WELCOME_MESSAGE = "\nWelcome to Tic Tac Toe!\n";
   private static final String MOVE_INSTRUCTION_MESSAGE = "The game board looks"
           + " like this:\n\n" + TicTacToeBoard.LABELED_BOARD + "\nOn your turn, "
           + "enter the number of the square you want to make your move on.\n";
   private static final String MOVE_PROMPT = "Please enter a move";
   private static final String VALID_MOVE_PROMPT = "Please enter a move between "
           + TicTacToeBoard.MIN_MOVE + " and " + TicTacToeBoard.MAX_MOVE;
   private static final String REPEAT_MOVE = "That move has already been done.";
   private static final String COMPUTER_TURN = "Computer's turn!\n";
   private static final String YOU_WON = "You won!";
   private static final String COMP_WON = "The computer won!";
   private static final String TIE = "It was a tie!";
 
   private static final Scanner scanner = new Scanner(System.in);
 
   /**
    * Write a line to the human player.
    * @param toWrite the message to write.
    */
   private static void say(String toWrite) {
     System.out.println(toWrite);
   }
 
   /**
    * Write a message to the human player, asking for input.
    * @param toPrompt the message to prompt with.
    */
   private static void prompt(String toPrompt) {
     System.out.print(toPrompt + ": ");
   }
 
   /**
    * Prompt the user for a move, and return it.
    * @param board the board the game is being played on.
    * @return a valid move entered by the user.
    */
   private static int getMove(TicTacToeBoard board) {
     prompt(MOVE_PROMPT);
     return getValidMove(board);
   }
 
   /**
    * Get a move from the user. If it is invalid, display an error and prompt the
    * user again.
    * @param board the board the game is being played on.
    * @return the valid move the user made.
    */
   private static int getValidMove(TicTacToeBoard board) {
     String playerInput = scanner.nextLine();
     try {
       int move = Integer.parseInt(playerInput);
       if (move < TicTacToeBoard.MIN_MOVE || move > TicTacToeBoard.MAX_MOVE) {
         prompt(VALID_MOVE_PROMPT);
       } else {
         int[] possibleMoves = board.getPossibleMoves();
         for (int possibleMove: possibleMoves) {
           if (move == possibleMove) {
             return move;
           }
         }
         // If the move was valid and was not returned, it was already made
         say(REPEAT_MOVE);
         prompt(MOVE_PROMPT);
       }
     } catch (NumberFormatException e) {
       prompt(VALID_MOVE_PROMPT);
    } finally {
      return getValidMove(board);
     }
   }
 
   /**
    * Have the computer play a randomly chosen valid move.
    */
   private static void playComputerMove(TicTacToeBoard board) {
     int[] possibleMoves = board.getPossibleMoves();
     int move = possibleMoves[(int)(Math.random() * (possibleMoves.length - 1))];
     board.playMove(move, Player.O);
   }
 
   /**
    * Play a round of Tic Tac Toe with the user. A round consists of a move
    * by the user and one by the computer.
    * @param board the board to play on.
    * @return true if the game has ended.
    */
   private static boolean play(TicTacToeBoard board) {
     int move = getMove(board);
     board.playMove(move, Player.X);
     System.out.println();
     System.out.println(board);
     if (board.isDone()) {
       return true;
     }
     playComputerMove(board);
     System.out.println(COMPUTER_TURN);
     System.out.println(board);
     return board.isDone();
   }
 
   /**
    * Plays a game of Tic Tac Toe on the command line with a randomized
    * computer opponent.
    */
   public static void main(String[] args) {
     TicTacToeBoard board = new TicTacToeBoard();
     say(WELCOME_MESSAGE);
     say(MOVE_INSTRUCTION_MESSAGE);
     while(!play(board)); // Will loop until the board game is over.
     if (board.whoWon() == Player.X) {
       say(YOU_WON);
     } else if (board.whoWon() == Player.O) {
       say(COMP_WON);
     } else {
       say(TIE);
     }
   }
 }
