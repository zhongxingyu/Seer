 package com.me.battleship;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class Board extends BaseObject {
 
     List<Ship> ships;
     List<Torpedo> torpedoes;
     private int[][] myGrid, enemyGrid;
     private int size, tileSize;
     int turns;
     boolean isActive;
     String name;
     private Vector2[] onSquares;
     private boolean[] activeSquares;
 
     public List<Ship> getShips() {
         return ships;
     }
 
     public Board(int x, int y, int u, int v, String n, int size) {
         super(x, y, u, v);
         name = n;
         this.size = size;
         myGrid = new int[size][size];
         turns = 0;
         isActive = false;
         torpedoes = new ArrayList<Torpedo>();
         ships = new ArrayList<Ship>();
         tileSize = u / size;
         /*
          * for (int i = 0; i < Globals.numShips; i++) { ships.add(new
          * Ship(Globals.shipsRequired[i], Globals.HORIZONTAL)); }
          */
 
         onSquares = new Vector2[5]; // needs to be changed!!
         for (int i = 0; i < onSquares.length; i++) {
             onSquares[i] = new Vector2();
         }
         activeSquares = new boolean[5]; // needs to be changed!!
         deselectSquares();
     }
 
     public void addShip(Ship s) {
         ships.add(s);
     }
 
     public void emptyBoard() {
         for (int i = 0; i < size; i++)
             for (int j = 0; j < size; j++)
                 myGrid[i][j] = Globals.EMPTY;
         ships.clear();
 
     }
 
     /*
      * void placeShip(int x, int y, int shipClass, int ori) {
      *
      * if (ori == Globals.VERTICAL) { for (int i = 0; i < shipClass; i++)
      * myGrid[x + i][y] = shipClass; } else { for (int i = 0; i < shipClass;
      * i++) myGrid[x][y + i] = shipClass; } ships.add(new Ship(x, y, shipClass,
      * ori));// gotta change thsi later }
      */
 
     public int attackLocation(int x, int y) {
 
         if (myGrid[x][y] < 0) // any negative square has already been targeted
                               // before
             return 0;
         else if (myGrid[x][y] == 0) { // empty square
             torpedoes.add(new Torpedo(x, y, Globals.AttackStatus.MISS));
             return 1;
         } else { // square has ship
             torpedoes.add(new Torpedo(x, y, Globals.AttackStatus.HIT));
             boolean gameFinished = true;
             for (int i = 0; i < 10 && gameFinished; i++)
                 for (int j = 0; j < 10; j++)
                     if (myGrid[i][j] > 0) {
                         gameFinished = false;
                         break;
                     }
             if (gameFinished) // all your ships are dead
                 return 3;
             else
                 // you still have ships left
                 return 2;
         }
     }
 
     public void setEnemy(int[][] e) {
         enemyGrid = e;
     }
 
     public int getSize() {
         return size;
 
     }
 
     public void centerShipOnSquare(Ship s) {
         int temp_adjust;
         float temp_adjust_half, x, y;
         temp_adjust = (s.getShipClass().getLength()) / 2;
         float half_tile = .5f * tileSize;
         temp_adjust_half = (s.getShipClass().getLength()) % 2 * half_tile;
         switch (s.getOrientation()) {
         case HORIZONTAL:
             x = topLeft.x + getOnSquares()[temp_adjust].x * tileSize
                     + temp_adjust_half;
             y = topLeft.y + getOnSquares()[temp_adjust].y * tileSize
                     + half_tile;
             break;
         case VERTICAL:
         default:
             x = topLeft.x + getOnSquares()[temp_adjust].x * tileSize
                     + half_tile;
             y = topLeft.y + getOnSquares()[temp_adjust].y * tileSize
                     + temp_adjust_half;
             break;
 
         }
         s.move(x, y);
     }
 
     public void highlightSquares(int x, int y, Ship s) {
         int length = s.getShipClass().getLength();
         int j = -(length - 1) / 2;
         float odd_length_adj = (length + 1) % 2 * tileSize / 2;
         int temp_x, temp_y;
         switch (s.getOrientation()) {
         case HORIZONTAL:
 
             temp_x = (int) ((x - odd_length_adj - topLeft.x) / tileSize);
             temp_y = (int) ((y - topLeft.y) / tileSize);
             if (y >= topLeft.y && y < topLeft.y + dimensions.y) {
                 for (int i = 0; i < s.getShipClass().getLength(); i++) {
 
                     if (temp_x + j >= 0 && temp_x + j < getSize()
                             && myGrid[temp_x + j][temp_y] == 0) {
 
                         getOnSquares()[i].x = temp_x + j;
                         getOnSquares()[i].y = temp_y;
                         getActiveSquares()[i] = true;
 
                     } else
                         getActiveSquares()[i] = false;
                     j++;
                 }
                 for (int i = s.getShipClass().getLength(); i < 5; i++)
                     getActiveSquares()[i] = false;
             } else
                 deselectSquares();
             break;
         case VERTICAL:
         default:
 
             temp_x = (int) (x - topLeft.x) / tileSize;
             temp_y = (int) (y - odd_length_adj - topLeft.y) / tileSize;
             if (x >= topLeft.x && x < topLeft.x + dimensions.x) {
                 for (int i = 0; i < getOnSquares().length; i++) {
                     if (temp_y + j >= 0 && temp_y + j < getSize()
                             && myGrid[temp_x][temp_y + j] == 0) {
 
                         getOnSquares()[i].x = temp_x;
                         getOnSquares()[i].y = temp_y + j;
                         getActiveSquares()[i] = true;
 
                     } else
                         getActiveSquares()[i] = false;
                     j++;
                 }
                 for (int i = s.getShipClass().getLength(); i < 5; i++)
                     getActiveSquares()[i] = false;
             } else
                 deselectSquares();
             break;
         }
     }
 
     public Vector2[] getOnSquares() {
         return onSquares;
     }
 
     public boolean[] getActiveSquares() {
         return activeSquares;
     }
 
     public void deselectSquares() {
         for (int i = 0; i < activeSquares.length; i++)
             activeSquares[i] = false;
     }
 
    public void placeShipOnGrid() {
         for (int i = 0; i < activeSquares.length; i++) {
             if (activeSquares[i]) {
                 myGrid[(int) onSquares[i].x][(int) onSquares[i].y] = Globals.FILLED;
             }
         }
     }
 }
