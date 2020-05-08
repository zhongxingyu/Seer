 package main;
 
 /**
  * The game TicTacToe, player1 - X and Player 2 - O
  *
  */
 
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 public class TicTacToe {
     public boolean quitGame;
     public char[][] board;
     public boolean player1;
     public boolean player2;
 
     public TicTacToe() {
         this.quitGame = false;
         board = new char[3][3];
         this.player1 = true;
         this.player2 = false;
 
     }
 
     /**
      *  Fills out board[3][3] with char symbols of numbers 1-9
      *
      *  [1][2][3]
      *  [4][5][6]
      *  [7][8][9]
      */
 
     public void newBoard() {
         char cntr = '1';
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 board[i][j] = cntr;
                 cntr++;
             }
         }
     }
 
     /**Marks the selected field (1-9) with either a "X" for player 1 or "O" for player 2.
      *
      * @param field   The field which we want to mark
      * @return        Returns an int; 1 the field was successfully marked
      *                -1 if the field is already marked
      */
     public int setMark(int field) {
 
 
         //Player 1 uses X and Player 2 uses O
         char symbol;
         if (player1)
             symbol = 'X';
         else
             symbol = 'O';
 
 
         //We want to compare it to field value to see if it's already marked
         char cfield;
         cfield = Character.forDigit(field, 10);
 
 
         //Use modulus to map from int field to matrix(i,j) and check if it has already been marked
 
         if(field>6)
             if(board[2][(field-1) % 3]==cfield)
                 board[2][(field-1) % 3]=symbol;
             else
                 return -1;
         else if (field > 3)
             if (board[1][(field - 1) % 3] == cfield)
                 board[1][(field - 1) % 3] = symbol;
             else
                 return -1;
         else if (board[0][(field - 1) % 3] == cfield)
             board[0][(field - 1) % 3] = symbol;
         else
             return -1;
         return 1;
     }
 
     /**
      *  Switches players from true to false and false to true.
      *  One of the players is always true and the other false
      */
 
     public void switchPlayer(){
 
         if(player1)
         {
             player2=true;
             player1=false;
         }
         else
         {
             player1=true;
             player2=false;
         }
     }
 
     /** Check if the current board has a winning game or a draw.
      *
      * For a winning game it simply goes over the eight possible winning games and checks if
      * the fields have the same mark. We also check one of those fields symbol to see who won.
      * To see if it's a draw we go over each field checking if the char value corresponds to a number,
      * if none of them do then we have a draw.
      *
      * @return      1 if player 1 has won
      *              2 if player 2 has won
      *              0 if it's a draw between player 1 and 2
      *              -1 if now winning game and the board is not fully marked
      */
 
     public int checkWinner() {
         //Returns:
         //1 :  If player 1 won
         //2 : If player 2 won
         //0 : If it's a draw
         //-1 : If no winner yet, game still playing
 
         //First check if the table is empty
 
         boolean draw = true;
         int winner;
         //Check for 3-in-a-row vertical and horizontal
         for (int i = 0; i < 3; i++) {
             if (board[i][i] == 'X')      //We check the intersecting field [0][0], [1][1], [2][2]
                 winner = 1;
             else
                 winner = 2;
             if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                 return winner;
             }
             if (board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                 return winner;
             }
 
         }
 
         //We check for 3-in-a-row on the diagonal
         if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
             if (board[0][0] == 'X')
                 return 1;
             else
                 return 2;
         }
         if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
             if (board[0][0] == 'X')
                 return 1;
             else
                 return 2;
         }
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 if (Character.isDigit(board[i][j]))
                     draw = false;
 
             }
         }
         if (draw) {
             return 0;
         }
 
         //The game is not over yet
         return -1;
     }
 
     /**
      * Checks if we have decided to quit the game (get function).
      *
      * @return  the boolean value quitGame
      */
 
     public boolean isQuitGame() {
         return quitGame;
     }
 
     /**
      * Sets quitGame to true
      */
 
     public void quitGame() {
         this.quitGame = true;
     }
 
     /**
      * Gets userinput from user (keyboard)
      *
      * @return  The user input if it's an int
      *          -1 if the input is not an integer
      */
 
     public int getUserInput() {
         try {
 			//Scan the command line for user input
             return new Scanner(System.in).nextInt();
         } catch (InputMismatchException ex) {
 			//User input was not an integer
             return -1;
         }
     }
 
     /**
      * Gets the symbol marked in field (only used for tests)
      *
      * @param field     an int value from 1-9
      * @return          returns the symbol in the field
      */
 
     public char getMark(int field) {
         field -= 1;  //Switch to 0 based indices
         int i = -1;
         int j = -1;
 
         if (field >= 6) {
             //Bottom row
             i = 2;
             j = field % 3;
         } else if (field >= 3 && field <= 5) {
             //Middle row
             i = 1;
             j = field % 3;
         } else if (field >= 0 && field <= 2) {
             //Top row
             i = 0;
             j = field % 3;
         }
 
         return board[i][j];
     }
 
     /**
      * Prints the Winner
      *
      * @param winner    Who won; 0 for draw, 1 for player 1 and 2 for player 2
      */
 
     public void printWinner(int winner) {
         if (winner == 0) {
             System.out.println("DRAW!");
         } else if (winner == 1) {
             System.out.println("Player 1 wins!");
         } else if (winner == 2) {
             System.out.println("Player 2 wins!");
         } else
             System.out.println("something went terribly wrong..");
 
     }
 
     /**
      * Print out the current board, i.e. the values of the matrix board[][]
      */
 
     public void printBoard() {
         for (int i = 0; i < 3; i++) {
             System.out.print("[");
             for (int j = 0; j < 3; j++) {
                 System.out.print(" " + board[i][j] + " ");
             }
             System.out.println("]");
         }
     }
 
     /**Gameplay
      * 
 	 * Player 1 has the first turn. Gameplay continues until a winner has been determined or a draw is reached.
      */
 
     public void playGame() {
         player1 = true;
         player2 = false;
 
         int winner = -1;
 
         newBoard();
 		assert(board[0][0]!=0);
 
         while (winner == -1) {
 		
 			//The game is still in progress
 
             System.out.println();
             printBoard();
             System.out.println();
 
             if (player1)
                 System.out.println("Player 1, it's your turn (X).");
             else
                 System.out.println("Player 2, it's your turn (O).");
 
             //Get user input
             boolean validInput = false;
             int userInput;
             while (!validInput) {
 				//Wait for input from user
                 userInput = getUserInput();
                 if (userInput == -1 || userInput < 1 || userInput > 9)
 					//User input is not valid
                     System.out.println("Please select a number between 1 and 9");
                 else if (setMark(userInput) == 1)
 					//User input is valid and a field has been marked
                     validInput = true;
                 else
 					//User input is not valid, the chosen field is already marked
                     System.out.println("The field is already marked");
             }
 
             winner = checkWinner();
             switchPlayer();
         }

        printBoard(); //The winning game
 
         //Game has ended
         //winner = 0 if draw
         //winner = 1 if player 1 won
         //winner = 2 if player 2 won
 
         System.out.println();
         printWinner(winner);
     }
 
 }
 
 
