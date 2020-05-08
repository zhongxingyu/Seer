 package com.game.xo.game;
 
 import com.game.xo.display.IDisplay;
 import com.game.xo.field.IField;
 import com.game.xo.input.InputDataString;
 import com.game.xo.players.Player;
 
 public class Game implements IGame {
     private Player player1;
     private Player player2;
     private InputDataString inputDataString;
     private IDisplay display;
     private IField field;
     private boolean endOfGame;
     private int globalSteps = 0;
     private final int MAX_CELLS = 9;
     private final int THREE_WINNING_MOVES = 3;
     private int findWinner;
 
 
     public Game(Player player1, Player player2, IDisplay display, IField field) {
         this.player1 = player1;
         this.player2 = player2;
         this.display = display;
         this.field = field;
         inputDataString = new InputDataString();
         endOfGame = false;
     }
 
     public void startGame() {
         do {
 
             do {
                 gameMoves(player1, player2);
             } while (globalSteps < MAX_CELLS && (!player1.getYouWin() && !player2.getYouWin()));
             if (globalSteps >= MAX_CELLS && (!player1.getYouWin() && !player2.getYouWin())) {
                 display.displayMessage("Standoff\n");
                 field.displayField();
                 display.displayMessage("\n");
             }
             getChoice();
 
         } while (!endOfGame);
 
 
     }
 
     private void gameMoves(Player player1, Player player2) {
 
         insertToField(player1);
         display.displayMessage("player1.getPlayerSteps() " + player1.getPlayerSteps() + "\n");
         if (globalSteps < MAX_CELLS && !player1.getYouWin()) {
             insertToField(player2);
 //            field.returnBack();
 //            field.displayField();
             display.displayMessage("player2.getPlayerSteps() " + player2.getPlayerSteps() + "\n");
 
         }
     }
 
     private void insertToField(Player player) {
         display.displayMessage(player.getInfo() + "\n");
         field.displayField();
         do {
             player.getCoordinates(display);
         } while (!field.setGameField(player, display));
         checkWin(player);
         globalSteps++;
         player.setPlayerSteps(globalSteps);
 
     }
 
     private char checkChoice(char inputChar) {
         if (inputChar == 'y' || inputChar == 'Y') {
             return 'y';
         }
         if (inputChar == 'n' || inputChar == 'N') {
             return 'n';
         }
         return 'e';
 
     }
 
     private void getChoice() {
        boolean flagContinue;
 
         do {
             display.displayMessage("You want to start new game? y/n : ");
             String choiceString = inputDataString.getData();
             switch (checkChoice(choiceString.charAt(0))) {
                 case 'y':
                     field.clearField();
                     globalSteps = 0;
                     player1.resetPlayer();
                     player2.resetPlayer();
                     flagContinue = true;
                     break;
                 case 'n':
                     endOfGame = true;
                     flagContinue = true;
                     break;
                 default:
                     display.displayMessage("Invalid option.\n");
                     flagContinue = false;
                     break;
             }
         } while (!flagContinue);
     }
 
     private void checkWin(Player player) {
         if (searchWinner(player.getPlayerSymbol())) {
             display.displayMessage("\n" + player.getName() + " win!!!\n");
             field.displayField();
             player.setYouWin(true);
            getChoice();
         }
     }
 
 
     public boolean searchWinner(char symbol) {
         if (rowWinner(symbol) || columnWinner(symbol) || diagonalWinner(symbol)) {
             return true;
         } else {
             return false;
         }
     }
 
     /*search winner in rows*/
     private boolean rowWinner(char symbol) {
         for (int i = 0; i < THREE_WINNING_MOVES; i++) {
             findWinner = rowWinnerSub(i, symbol);
             if (findWinner == THREE_WINNING_MOVES) {
                 return true;
             }
         }
         return false;
     }
 
     private int rowWinnerSub(int axisX, char symbol) {
         int sum = 0;
         for (int j = 0; j < THREE_WINNING_MOVES; j++) {
             if (field.getGameField(axisX, j) == symbol) {
                 sum++;
             }
         }
         return sum;
     }
 
     /*search winner in columns*/
     private boolean columnWinner(char symbol) {
         for (int i = 0; i < THREE_WINNING_MOVES; i++) {
             findWinner = columnWinnerSub(i, symbol);
             if (findWinner == THREE_WINNING_MOVES) {
                 return true;
             }
         }
         return false;
     }
 
     private int columnWinnerSub(int axisY, char symbol) {
         int sum = 0;
         for (int j = 0; j < THREE_WINNING_MOVES; j++) {
             if (field.getGameField(j, axisY) == symbol) {
                 sum++;
             }
         }
         return sum;
     }
 
     /*search winner in diagonals*/
     private boolean diagonalWinner(char symbol) {
         int i, sum = 0;
         for (i = 0; i < THREE_WINNING_MOVES; i++) {
             if (field.getGameField(i, i) == symbol) {
                 sum++;
             }
         }
         i = 1;
         if (sum == THREE_WINNING_MOVES || field.getGameField(i, i) == symbol &&
                 field.getGameField(i + 1, i - 1) == symbol && field.getGameField(i - 1, i + 1) == symbol) {
             return true;
         }
         return false;
 
     }
 }
