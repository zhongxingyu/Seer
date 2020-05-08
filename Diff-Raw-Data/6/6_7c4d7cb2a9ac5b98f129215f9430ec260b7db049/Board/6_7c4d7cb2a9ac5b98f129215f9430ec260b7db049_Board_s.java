 package sokoban;
 
 /**
  *
  */
 public class Board
 {
     // Input values
     /**
      * Value for a WALL on the board
      */
     public final static byte WALL = 0x01;
 
     /**
      * Value for a BOX on the board
      */
     public final static byte BOX = 0x02;
     /**
      * Value for a GOAL position on the board
      */
     public final static byte GOAL = 0x04;
 
     // Generated values
     /**
      * No box allowed.
      */
     public final static byte NO_BOX = 0x08;
     // Bitmasks
     /**
      * A bitmask that says if a cell is occupied by something
      */
     public final static byte OCCUPIED = 0x03;
 
     final int width;
     final int height;
 
     /**
      * The actuall board
      */
     public byte cells[][];
     int playerX;
     int playerY;
     int remainingBoxes;
 
     /**
      * Initialize a new board
      * 
      * @param width The width of the board
      * @param height The height of the board
      * @param playerX The X position of the player
      * @param playerY The Y position of the player
      */
     public Board(int width, int height, int playerX, int playerY)
     {
        cells = new byte[width][height];
        this.height = width;
        this.width = height;
         this.playerX = playerX;
         this.playerY = playerY;
     }
 
     /**
      * Get the width of the board
      * 
      * @return The width of the board
      */
     public int getWidth()
     {
         return width;
     }
 
     /**
      * Get the height of the board
      * 
      * @return The height of the board.
      */
     public int getHeight()
     {
         return height;
     }
 
     /**
      * Returns true if the square has any of the bits in the mask.
      */
     private boolean is(byte square, byte mask)
     {
         return (square & mask) != 0;
     }
 
     /**
      * Updates some variables after a board has been loaded.
      */
     public void refresh()
     {
         countBoxes();
         markNonBoxSquares();
     }
 
     /**
      * Counts the boxes that are not in a goal, and updates remainingBoxes.
      */
     private void countBoxes()
     {
         int remaining = 0;
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 if ((cells[y][x] & (BOX | GOAL)) == BOX) {
                     remaining++;
                 }
             }
         }
         remainingBoxes = remaining;
     }
 
     /**
      * Print the current board
      * 
      * @return The string representing the board
      */
     @Override
     public String toString()
     {
         String tmp = "";
         for (int i = 0; i < height; ++i) {
             for (int j = 0; j < width; ++j) {
                 tmp += cells[i][j];
             }
             tmp += "\n";
         }
         return tmp;
     }
 
     /**
      * Marks squares that boxes would get stuck in.
      */
     private void markNonBoxSquares()
     {
         final byte TOP = 0x1;
         final byte BOTTOM = 0x2;
         final byte LEFT = 0x4;
         final byte RIGHT = 0x8;
 
         final byte VERTICAL = TOP | BOTTOM;
         final byte HORIZONTAL = LEFT | RIGHT;
 
         final byte blocked[][] = new byte[height][width];
 
         for (int y = 1; y < height - 1; y++) {
             for (int x = 1; x < width - 1; x++) {
                 // Break the "blocked lines" if there's a goal
                 if (is(cells[y][x], GOAL)) {
                     continue;
                 }
 
                 final byte neighborWalls = (byte) ((is(cells[y - 1][x], WALL) ? TOP
                         : 0)
                         | (is(cells[y + 1][x], WALL) ? BOTTOM : 0)
                         | (is(cells[y][x - 1], WALL) ? LEFT : 0) | (is(
                         cells[y][x + 1], WALL) ? RIGHT : 0));
 
                 // How the current cell can be blocked at most,
                 // taking cells to the left and above into account.
                 final byte verticalBlocked = (byte) (neighborWalls
                         & blocked[y][x - 1] & VERTICAL);
                 final byte horizontalBlocked = (byte) (neighborWalls
                         & blocked[y - 1][x] & HORIZONTAL);
 
                 if (!is(cells[y - 1][x], WALL)) {
                     // Use common vertical blocking status with the block to the
                     // left
                     blocked[y][x] |= verticalBlocked;
                 }
                 else if ((blocked[y][x - 1] & VERTICAL) != 0) {
                     // There's a wall and the preceeding cells are blocked
                     // somehow
                     for (int i = x; i > 1; i--) {
                         if (is(cells[y][i], WALL)) {
                             break;
                         }
                         else {
                             cells[y][i] |= NO_BOX;
                         }
                     }
                 }
 
                 // Work in progress
                 /*
                  * if (horizontalBlocked != 0 && !is(cells[y][x-1], WALL)) {
                  * blocked[y][x] |= verticalBlocked;
                  * }
                  */
             }
         }
     }
 }
