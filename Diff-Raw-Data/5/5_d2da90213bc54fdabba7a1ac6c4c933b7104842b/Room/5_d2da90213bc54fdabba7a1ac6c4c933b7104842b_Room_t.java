 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * A basic two-dimensional map. 
  * Starting coordinate (0,0) is in the top-left corner. 
  * Due to this, field grid is accessed with grid[coordinate y][coordinate x].
  * 
  * @author Jonathan Trudeau
  */
 public class Room {
 
     /** the default number of columns */
     private static final int DEFAULT_SIZE_X = 30;
 
     /** the default number of rows */
     private static final int DEFAULT_SIZE_Y = 15;
 
     /** the number of columns */
     private final int size_x;
 
     /** the number of rows */
     private final int size_y;
 
     private Tile[][] grid;
     private char[][] displayedGrid;
 
     private List<Tile> actorList = new ArrayList<Tile>();
 
     public Room() {
 
         size_x = DEFAULT_SIZE_X;
         size_y = DEFAULT_SIZE_Y;
 
         grid = new Tile[size_y][size_x];
         displayedGrid = new char[size_y][size_x];
 
         initializeGrid();
 
     }
 
     public Room(int x, int y) {
 
         size_x = x;
         size_y = y;
 
         grid = new Tile[size_y][size_x];
         displayedGrid = new char[size_y][size_x];
 
         initializeGrid();
 
     }
 
     public void add(int index_x, int index_y, Tile newTile) {
 
         if (checkInBounds(index_x, index_y)) {
             
             newTile.setCoordinates(index_x, index_y);
             actorList.add(newTile);
             
         }
         
         else throw new IndexOutOfBoundsException();
 
     }
 
     public void remove(int index_x, int index_y) {
 
         // a copy is needed, because an object can not be iterated over and modified at the same time
         ArrayList<Tile> actorListCopy = new ArrayList<Tile>(actorList);
         
         if (checkInBounds(index_x, index_y)) {
             
             for(Tile t: actorListCopy)
                 if(t.getCoordinateX() == index_x && t.getCoordinateY() == index_y)
                     actorList.remove(t);
             
         }
        
        else throw new IndexOutOfBoundsException();
 
     }
 
     private boolean checkInBounds(int index_x, int index_y) {
 
        if ((index_x > size_x) || (index_x < 0) || (index_y > size_y) || (index_y < 0))
             return false;
 
         return true;
 
     }
 
     private void initializeGrid() {
 
         int row, col, randomNumber;
         Random rand = new Random(); 
 
         for (row = 0; row < size_y; row++)
             for (col = 0; col < size_x; col++) {
                 
                 randomNumber = rand.nextInt(4);
                 
                 if(randomNumber == 0)
                     grid[row][col] = new TileGround('.');
                 else if(randomNumber == 1)
                     grid[row][col] = new TileGround(',');
                 else if(randomNumber == 2)
                     grid[row][col] = new TileGround('\'');
                 else if(randomNumber == 3)
                     grid[row][col] = new TileGround('`');
                 
             }
 
     }
 
     /**
      * This will draw the actors onto the base grid according to their locations, updating the displayed grid instead of
      * the base grid to ensure tiles (such as grass, ground, etc) are not overwritten in the base grid.
      */
     private void updateDisplay() {
 
         int row, col;
 
         for (row = 0; row < size_y; row++)
             for (col = 0; col < size_x; col++)
                 displayedGrid[row][col] = grid[row][col].display();
         
         for (Tile t: actorList)
             displayedGrid[t.getCoordinateY()][t.getCoordinateX()] = t.display();
 
     }
 
     public String toString() {
 
         updateDisplay();
 
         StringBuilder string = new StringBuilder();
         
         for(char[] row: displayedGrid) {
             
             for(char c: row)
                 string.append(c);
             
             string.append("\n");
             
         }
         
         return string.toString();
 
     }
 
 }
