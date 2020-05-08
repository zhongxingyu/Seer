 package test;
 import main.*;
 import algorithm.*;
 import userInterface.controller.*;
 
 public class Test {
     private Board board = new Board();
     private Game game;
     private Player playerOne = new HumanPlayer(Colour.RED, board);
     private Player playerTwoHuman = new HumanPlayer(Colour.RED, board);
     private Player playerTwo = new AIPlayer(Colour.BLACK, board);
     private boolean passed = true;
     private Location start;
     private Location end;
     
     public Test(){
         System.out.print(testBoardHumanMove() + testBoardAIMove() + testBoardInvalidMove()
             + testGameInitialization() + testJumpMove());
         if (passed){
             System.out.println("\n\nALL TESTS PASSED.");
             }
     }
     
     public String testBoardHumanMove(){
         board.initializeBoard();
         try{
             start = new Location(1,5);
             end = new Location(2,4);
             Move move1 = new Move(playerOne, start, end, board, false);
             board.movePiece(playerOne, move1);
             if (board.getArray()[5][1] == null && board.getArray()[4][2] != null) {
                 return "\nTestBoardHumanMove PASSED";
             }else{
                 passed = false;
                 return "\nThe board did not move the proper pieces";
             }
         }catch(OutOfBoundsException oobe){
             passed = false;
             return "\nBoard move Locations improperly initialized";
         }    
     }
     public String testBoardAIMove(){
         board.resetBoard();
         playerTwo = new AIPlayer(Colour.BLACK, board);
         Piece[][] firstBoard = board.getArray();
         Piece[][] movedBoard = board.getArray();
         if (firstBoard != movedBoard){
             passed = false;
             return "\nThe AI player did not correctly set a move to be made";
         }else{
             return "\nTestBoardAIMove PASSED";
         }
     }
     public String testBoardInvalidMove(){
         board.resetBoard();
         try{
             start = new Location(7,5);
             end = new Location(8,4);
             Move move3 = new Move(playerOne, start, end, board, true);
             board.movePiece(playerOne, move3);
             if (board.getArray()[5][7] == null){
                 passed = false;
                 return "\nTestBoardInvalidMove FAILED, piece was moved to an invalid location";
             }else{
                 return "\nTestBoardInvalidMove PASSED.";
                 }
         }catch(OutOfBoundsException oobe){
             return "\nTestBoardInvalidMove PASSED.";
             }
         }
     public String testGameInitialization(){
         game = game.getInstance();
         game.initialize(2);
         board = game.getBoard();
         playerOne = game.getRed();
         playerTwoHuman = game.getBlack();
         if (playerOne.getPieces().length != 12 || playerTwoHuman.getPieces().length != 12){
             passed = false;
             return "\nTestGameInitialization FAILED, player piece arrays not full.";
 
         }else{
             return "\nTestGameInitialization PASSED.";
             } 
     }   
    public String testJumpMove(){
        try{
            start = new Location(1,5);
            end = new Location(2,4);
            playerOne.setStart(start);
            playerOne.setEnd(end);
            playerOne.makeCurrentMove();
            board.printArray();
            playerTwoHuman.setStart(new Location(4,2));
            playerTwoHuman.setEnd(new Location(3,3));
            playerTwoHuman.makeCurrentMove();
            board.printArray();
            playerOne.setStart(end);
            playerOne.setEnd(new Location(4,2));
            playerOne.makeCurrentMove();
            board.printArray();
            if (playerTwoHuman.getPieces().length != 11){
                passed = false;
                return "\nTestJumpMove FAILED, jumped piece was not removed from the board.";
            }else if(board.getArray()[2][4] != null){
                passed = false;
                return "\nTestJumpMove FAILED, the jump was not performed.";
            }
            else{                
            return "\nTestJumpMove PASSED.";
            }
        }    
        catch(OutOfBoundsException oobe){
            passed = false;
            return "\nTestJumpMove FAILED, Locations improperly set.";
            }
    }
     public static void main(String[] args){
         Test test = new Test();
     }
 }
 
         
         
