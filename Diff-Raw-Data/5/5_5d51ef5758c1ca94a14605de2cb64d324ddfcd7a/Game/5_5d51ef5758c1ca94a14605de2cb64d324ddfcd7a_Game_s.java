 package com.xebia.xke;
 
 import java.util.Arrays;
 
 public class Game {
 
     Board board = new Board();
 
     public void run(Strategy s1, Strategy s2) {
         Strategy winner = play(s1, s2, 1);
        System.out.println(board.isFull ? "Game over" : winner + " has won");
     }
 
     public Strategy play(Strategy s1, Strategy s2, int currentPlayer) {
         int field = s1.play(this.board.copy());
         board.play(field, currentPlayer);
        return board.isBoardWinning || board.isFull ? s1 : play(s2, s1, currentPlayer*-1);
     }
 
     public static class Board {
         public int[] grid = new int[9];
         public int nbFieldTaken;
         public boolean isBoardWinning;
         public boolean isFull;
 
         public void play(int field, int player) {
             if (grid[field] != 0) {
                 throw new IllegalArgumentException("field already taken");
             }
             nbFieldTaken++;
             grid[field] = player;
             isBoardWinning =
                     isWinning(grid[(field / 3 * 3)] + grid[1 + field / 3 * 3] + grid[2 + field / 3 * 3]) ||
                             isWinning(grid[(field % 3)] + grid[3 + field % 3] + grid[6 + field % 3]) ||
                             isWinning(grid[0] + grid[4] + grid[8]) || isWinning(grid[6] + grid[4] + grid[2]);
             isFull = nbFieldTaken == 9;
         }
 
         private boolean isWinning(int sum) {
             return Math.abs(sum) == 3;
         }
 
         public Board copy() {
             Board board = new Board();
             board.grid = Arrays.copyOf(this.grid, 9);
             board.nbFieldTaken = this.nbFieldTaken;
             board.isBoardWinning = this.isBoardWinning;
             board.isFull = this.isFull;
             return board;
         }
     }
 
     public static interface Strategy {
         int play(Board board);
     }
 }
