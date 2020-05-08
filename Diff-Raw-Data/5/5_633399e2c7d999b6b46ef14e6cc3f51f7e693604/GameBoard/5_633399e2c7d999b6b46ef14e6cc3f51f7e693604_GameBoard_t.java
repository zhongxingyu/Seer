 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.common.Coordinate;
 import edu.victone.scrabblah.logic.common.Tile;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 4:00 PM
  */
 public class GameBoard {
     static int[][] cellValues =
             {{4, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 4},
                     {0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0},
                     {0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0},
                     {1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1},
                     {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                     {0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0},
                     {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0},
                     {4, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, 4},
                     {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0},
                     {0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0},
                     {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                     {1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1},
                     {0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0},
                     {0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0},
                     {4, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 4}};
 
 
     public static final int MAXPLAYERS = 4;
 
     private BoardCell[][] boardCells = new BoardCell[15][15];
 
     public GameBoard() {
         initBoard();
     }
 
     private void initBoard() {
         for (int i = 0; i < 15; i++) {
             for (int j = 0; j < 15; j++) {
                 switch (cellValues[i][j]) {
                     case -1: //star cell
                     case 0:  //regular
                         boardCells[i][j] = new BoardCell(1, false);
                         break;
                     case 1:  //dublet
                         boardCells[i][j] = new BoardCell(2, false);
                         break;
                     case 2:  //triplet
                         boardCells[i][j] = new BoardCell(3, false);
                         break;
                     case 3:  //dubword
                         boardCells[i][j] = new BoardCell(2, true);
                         break;
                     case 4:  //tripword
                         boardCells[i][j] = new BoardCell(3, true);
                         break;
                 }
             }
         }
         System.out.println("GameBoard initialized.");
     }
 
     public BoardCell getCell(Coordinate coord) {
        return boardCells[coord.getY()][coord.getX()];
     }
 
     @Override
     public String toString() {
        String header = "  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O\n";
         String row = " |--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|";
         StringBuilder sb = new StringBuilder(header);
         sb.append(row + "\n");
 
         for (int i = 0; i < 15; i++) {
             sb.append((char) (i + 65));
             sb.append("|");
             for (int j = 0; j < 15; j++) {
                 BoardCell bc = boardCells[i][j];
                 if (bc.isEmpty()) {
                     switch (bc.getMultiplier()) {
                         case 1:
                             sb.append("  |");
                             break;
                         case 2:
                             sb.append(bc.affectsWord() ? "DW|" : "DL|");
                             break;
                         case 3:
                             sb.append(bc.affectsWord() ? "TW|" : "TL|");
                             break;
                     }
                 } else {
                     sb.append(bc.getTile().toString() + "|");
                 }
             }
             sb.append((char) (i + 65));
 
             sb.append("\n" + row + "\n");
         }
         sb.append(header);
         return sb.toString();
     }
 }
