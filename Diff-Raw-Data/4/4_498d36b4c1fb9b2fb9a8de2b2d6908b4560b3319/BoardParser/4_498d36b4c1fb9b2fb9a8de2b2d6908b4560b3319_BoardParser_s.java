 package sokoban;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * A board parser
  */
 public class BoardParser
 {
     /**
      * Parse a board from an array of bytes
      * 
      * @param boardBytes
      *            The array of bytes representing the board
      * @return A real board
      */
     public static Board parse(byte[] boardBytes)
     {
         int boardWidth = 0;
         int boardHeight = 1; // board input string doesn't end with '\n'
         int playerCol = 0;
         int playerRow = 0;
         int rowLength = 0;
         for (int i = 0; i < boardBytes.length; ++i) {
             rowLength++;
             switch (boardBytes[i]) {
                 case '\n':
                     if (rowLength > boardWidth) {
                         boardWidth = rowLength - 1; // '\n' not part of board
                     }
                     rowLength = 0;
                     ++boardHeight;
                     break;
                 case '@':
                 case '+':
                     // Player position is 0-indexed.
                     playerCol = rowLength - 1;
                     playerRow = boardHeight - 1;
                     break;
             }
         }
 
         Board board = new Board(boardWidth, boardHeight, playerRow, playerCol);
 
         int row = 0;
         int col = 0;
         for (int i = 0; i < boardBytes.length; ++i, ++col) {
             switch (boardBytes[i]) {
                 case '\n':
                     // col is incremented before first char on every row
                     col = -1;
                     ++row;
                     break;
                 case '#':
                     board.cells[row][col] = Board.WALL;
                     break;
                 case '$':
                     board.cells[row][col] = Board.BOX | Board.BOX_START;
                     break;
                 case '+':
                     board.cells[row][col] = Board.GOAL;
                     break;
                 case '*':
                     board.cells[row][col] = Board.BOX | Board.GOAL | Board.BOX_START;
                     break;
                 case '.':
                     board.cells[row][col] = Board.GOAL;
                     break;
             }
         }
 
         board.cells[playerRow][playerCol] |= Board.PLAYER_START;
         board.refresh();
         return board;
     }
 
     /**
      * Constructs and returns the board represented by the given string.
      * 
      * @param boardString
      *            The string representation of the board to build.
      * @return
      */
     public static Board parse(String boardString)
     {
         return parse(boardString.getBytes());
     }
 
     /**
      * Reads the given levels file and returns a list of all level boards as
      * strings.
      * 
      * @param levelsFile
      *            The file containing the levels.
      */
     public static ArrayList<String> getBoardStrings(File levelsFile)
     {
         BufferedReader in = null;
         ArrayList<String> levels = new ArrayList<String>();
 
         try {
             in = new BufferedReader(new FileReader(levelsFile));
             String line;
             StringBuilder partialLevel = new StringBuilder();
 
             while ((line = in.readLine()) != null) {
                if (line.startsWith(";LEVEL")) {
                     if (partialLevel.length() > 0) {
                         levels.add(partialLevel.toString());
                         partialLevel = new StringBuilder();
                     }
                 }
                 else if (!line.startsWith(";")) {
                     partialLevel.append(line);
                     partialLevel.append('\n');
                 }
             }
             
             // Flush (add) last board.
             levels.add(partialLevel.toString());
 
             in.close();
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         return levels;
     }
 }
