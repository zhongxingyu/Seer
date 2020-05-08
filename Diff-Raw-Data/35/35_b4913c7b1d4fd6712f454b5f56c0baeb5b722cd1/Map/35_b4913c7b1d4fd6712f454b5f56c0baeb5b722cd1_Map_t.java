 import java.awt.*;
 
 /** Contains map data
   * This includes references to all entities
   * items, and other stuff probably
   * 
   * @author Charles Zinn
   */
 public class Map {
   Tile[][] tiles;
   
  static int SPLIT_MIN = 20;
  static int MIN_ROOM_SIZE = 8;
  
   Map(int rows, int cols) {
     tiles = new Tile[rows][cols];
     for(int i = 0; i < rows; i++) {
       for(int j = 0; j < cols; j++) {
         tiles[i][j] = new Tile(Tile.WALL);
       }
     }
     generateDungeon(0, 0, rows, cols);
   }
   
   public void clear() {
     for(int i = 0; i < getRows(); i++) {
       for(int j = 0; j < getCols(); j++) {
         tiles[i][j] = new Tile(Tile.WALL);
       }
     }
   }
   
   public int getRows() {
     return tiles.length;
   }
   
   public int getCols() {
     return tiles[0].length;
   }
   
   public Tile getTile(int row, int col) {
     return tiles[row][col];
   }
   
   /** Recursively generates a dungeon map inside the given box
     * Map should be clear()ed beforehand
     */
   public void generateDungeon(int row, int col, int height, int width) {
    //At least one dimension must be over SPLIT_MIN (dimensions under SPLIT_MIN should not be split)
    if(height >= SPLIT_MIN || width >= SPLIT_MIN) {
      boolean vertSplit = Game.rand(0, 2) == 0; //whether the seperation line is vertical or horizontal (random)
      if(height < SPLIT_MIN) vertSplit = true; //must split vertical if less than 30 tall
      if(width < SPLIT_MIN) vertSplit = false; //must split horizontal if less than 30 wide
       
       if(vertSplit) { //splitting vertically
        int splitLine = Game.rand(MIN_ROOM_SIZE, width - MIN_ROOM_SIZE);
         generateDungeon(row, col, height, splitLine);
         generateDungeon(row, col + splitLine, height, width - splitLine);
         //connect them
        int pathPos = Game.rand(MIN_ROOM_SIZE / 2, height - (MIN_ROOM_SIZE / 2)); //somewhere where there is definitely room on both sides
         int colIndex = col + splitLine;
         System.out.println("making a path on row " + pathPos + " which is from 6 to " + (height - 6));
         while(getTile(pathPos + row, colIndex).getType() == Tile.WALL) {
           getTile(pathPos + row, colIndex).setType(Tile.FLOOR);
           colIndex++;
         }
         colIndex = col + splitLine - 1;
         while(getTile(pathPos + row, colIndex).getType() == Tile.WALL) {
           getTile(pathPos + row, colIndex).setType(Tile.FLOOR);
           colIndex--;
         }
       } else { //splitting horizontally
        int splitLine = Game.rand(MIN_ROOM_SIZE, height - MIN_ROOM_SIZE);
         generateDungeon(row, col, splitLine, width);
         generateDungeon(row + splitLine, col, height - splitLine, width);
         //connect them
        int pathPos = Game.rand(MIN_ROOM_SIZE / 2, width - (MIN_ROOM_SIZE / 2)); //somewhere where there is definitely room on both sides
         int rowIndex = row + splitLine;
         System.out.println("making a path on col " + pathPos + " which is from 6 to " + (width - 6));
         while(getTile(rowIndex, pathPos + col).getType() == Tile.WALL) {
           getTile(rowIndex, pathPos + col).setType(Tile.FLOOR);
           rowIndex++;
         }
         rowIndex = row + splitLine - 1;
         while(getTile(rowIndex, pathPos + col).getType() == Tile.WALL) {
           getTile(rowIndex, pathPos + col).setType(Tile.FLOOR);
           rowIndex--;
         }
       }
     } else {
       //Create a single room inside this box
      int upad = Game.rand(1, MIN_ROOM_SIZE / 2 - 1);
      int dpad = Game.rand(1, MIN_ROOM_SIZE / 2 - 1);
      int lpad = Game.rand(1, MIN_ROOM_SIZE / 2 - 1);
      int rpad = Game.rand(1, MIN_ROOM_SIZE / 2 - 1);
       for(int r = row + upad; r < row + height - dpad; r++) {
         for(int c = col + lpad; c < col + width - rpad; c++) {
           getTile(r, c).setType(Tile.FLOOR);
         }
       }
       //Put extra features in rooms to make them less boring
       //Spawn monsters and/or loot in the room (depending on size)
     }
   }
   
   
   /** Checks for lines of sight between two tiles using Bresenham's line algorithm */
   public boolean sight(int r0, int c0, int r1, int c1) {
     if(isOpaque(r0, c0)) return false;
     
     //Comment line to allow infinite sight
     //Uncomment to only allow sight within radius of 15
     if((r1 - r0) * (r1 - r0) + (c1 - c0) * (c1 - c0) > 15 * 15) return false;
     
     boolean steep = (int)Math.abs(r1 - r0) > (int)Math.abs(c1 - c0);
     if(steep) {
       int temp = r0;
       r0 = c0;
       c0 = temp;
       temp = r1;
       r1 = c1;
       c1 = temp;
     }
     if(c0 > c1) {
       int temp = c0;
       c0 = c1;
       c1 = temp;
       temp = r0;
       r0 = r1;
       r1 = temp;
     }
     int dc = c1 - c0;
     int dr = (int)Math.abs(r1 - r0);
     
     int d = 2 * dr - dc;
     int r = r0;
     int rstep = -1;
     if(r0 < r1) rstep = 1;
     
     for(int c = c0 + 1; c < c1; c++) {
       if(d > 0) {
         r += rstep;
         if(steep) {
           if(isOpaque(c, r)) return false;
         } else {
           if(isOpaque(r, c)) return false;
         }
         d = d + (2 * dr - 2 * dc);
       } else {
         if(steep) {
           if(isOpaque(c, r)) return false;
         } else {
           if(isOpaque(r, c)) return false;
         }
         d = d + (2 * dr);
       }
     }
     
     return true;
   }
   
   /** Checks whether the tile at (row, col) is opaque */
   public boolean isOpaque(int row, int col) {
     return getTile(row, col).isOpaque();
   }
   
   /** Draws part of the map, with the top left corner being positioned at (row, col)
     * The tile drawn at that position is the tile (srow, scol)
     * The box is (height, width) big
     * Things not visible from (vr, vc) are greyed out; eventually they may not be drawn at all or something idk
     */
   public void draw(TextPanel p, int row, int col, int srow, int scol, int height, int width, int vr, int vc) {
     for(int r = 0; r < height; r++) {
       for(int c = 0; c < width; c++) {
         Tile t = getTile(srow + r, scol + c);
         char tc = t.getChar();
         CharCol tcc = sight(vr, vc, srow + r, scol + c) ? t.getCol() : new CharCol(Color.WHITE, Color.GRAY);
         p.drawChar(tc, tcc, row + r, col + c);
       }
     }
   }
 }
 
 /** Contains details about a single tile
   * Its type and "state" (open or closed, etc.)
   * 
   * @author Charles Zinn
   */
 class Tile {
   int type;
   int data;
   
   static int FLOOR = 0;
   static int WALL = 1;
   
   Tile(int type) {
     this(type, 0);
   }
   
   Tile(int type, int data) {
     this.type = type;
     this.data = data;
   }
   
   public int getType() {
     return type;
   }
   
   public int getData() {
     return data;
   }
   
   public void setType(int type) {
     this.type = type;
     data = 0; //remove data; different types of tiles have different meanings for values, should clear by default
   }
   
   public void setData(int data) {
     this.data = data;
   }
   
   public char getChar() {
     return Tile.getChar(type);
   }
   
   public static char getChar(int type) {
     if(type == FLOOR)
       return '.';
     if(type == WALL)
       return '#';
     
     return '?';
   }
   
   public CharCol getCol() {
     return Tile.getCol(type);
   }
   
   public static CharCol getCol(int type) {
     if(type == FLOOR)
       return new CharCol();
     if(type == WALL)
       return new CharCol();
     
     return new CharCol();
   }
   
   public boolean isOpaque() {
     return Tile.isOpaque(type);
   }
   
   public static boolean isOpaque(int type) {
     if(type == FLOOR) return false;
     if(type == WALL) return true;
     
     return false;
   }
 }
