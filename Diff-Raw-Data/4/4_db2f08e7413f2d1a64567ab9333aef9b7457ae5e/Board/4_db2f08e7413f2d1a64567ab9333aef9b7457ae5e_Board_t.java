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
     boolean isActive, validShipPlacement;
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
 
         onSquares = new Vector2[5]; // TODO
         for (int i = 0; i < onSquares.length; i++) {
             onSquares[i] = new Vector2();
         }
         activeSquares = new boolean[5]; // TODO
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
             x = topLeft.x + getOnSquares()[temp_adjust].x * tileSize + temp_adjust_half;
             y = topLeft.y + getOnSquares()[temp_adjust].y * tileSize + half_tile;
             break;
         case VERTICAL:
         default:
             x = topLeft.x + getOnSquares()[temp_adjust].x * tileSize + half_tile;
             y = topLeft.y + getOnSquares()[temp_adjust].y * tileSize + temp_adjust_half;
             break;
 
         }
         s.move(x, y);
     }
 
     public void identifySquares(int x, int y, Ship s) {
         int ship_length = s.getShipClass().getLength();
         int ship_length_offset = -(ship_length - 1) / 2;
         float odd_length_adj = (ship_length + 1) % 2 * tileSize / 2;
         int length, width;
         float min_bound, max_bound;
         validShipPlacement = false;
         switch (s.getOrientation()) {
         case HORIZONTAL:
 
             length = (int) ((x - odd_length_adj - topLeft.x) / tileSize);
             width = (int) ((y - topLeft.y) / tileSize);
             min_bound = topLeft.y;
             max_bound = topLeft.y + dimensions.y;
             if (y >= min_bound && y < max_bound) {
                 validShipPlacement = true;
                 for (int i = 0; i < s.getShipClass().getLength(); i++) {
 
                     if (length + ship_length_offset >= 0 && length + ship_length_offset < getSize()
                             && myGrid[length + ship_length_offset][width] == 0) {
 
                         getOnSquares()[i].x = length + ship_length_offset;
                         getOnSquares()[i].y = width;
                         getActiveSquares()[i] = true;
 
                     } else {
                         getActiveSquares()[i] = false;
                         validShipPlacement = false;
                     }
                     ship_length_offset++;
                 }
 
             } else {
                 deselectSquares();
             }
             break;
         case VERTICAL:
         default:
             width = (int) (x - topLeft.x) / tileSize;
             length = (int) (y - odd_length_adj - topLeft.y) / tileSize;
             min_bound = topLeft.x;
             max_bound = topLeft.x + dimensions.x;
             if (x >= min_bound && x < max_bound) {
                 validShipPlacement = true;
                 for (int i = 0; i < s.getShipClass().getLength(); i++) {
                     if (length + ship_length_offset >= 0 && length + ship_length_offset < getSize()
                             && myGrid[width][length + ship_length_offset] == 0) {
 
                         getOnSquares()[i].x = width;
                         getOnSquares()[i].y = length + ship_length_offset;
                         getActiveSquares()[i] = true;
 
                     } else {
                         getActiveSquares()[i] = false;
                         validShipPlacement = false;
                     }
                     ship_length_offset++;
                 }
 
             } else {
                 deselectSquares();
             }
             break;
         }
         for (int i = s.getShipClass().getLength(); i < activeSquares.length; i++)
             activeSquares[i] = false;
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
 
     public void placeShipOnGrid(Ship s) {
         for (int i = 0; i < activeSquares.length; i++) {
             if (activeSquares[i]) {
                 myGrid[(int) onSquares[i].x][(int) onSquares[i].y] = Globals.FILLED;
                 s.getOnSquares()[i] = onSquares[i].cpy();
             }
         }
         s.locationSet = true;
     }
 
     public void removeShipIfOnGrid(Ship s) {
         if (s.locationSet) {
             for (int i = 0; i < s.getShipClass().getLength(); i++) {
                 myGrid[(int) s.getOnSquares()[i].x][(int) s.getOnSquares()[i].y] = Globals.EMPTY;
             }
             s.locationSet = false;
         }
     }
 
     public void autoPlace() {
         for (Ship ship : ships) {
             validShipPlacement = false;
             while (!validShipPlacement) {
                 removeShipIfOnGrid(ship);
                 if (Math.random() > .5){
                     ship.changeOrientation();
                 }
                int x = (int)((getBotX() - getTopX()) * Math.random()+getTopX());
                int y = (int)((getBotY() - getTopY()) * Math.random()+getTopY());
                 ship.move(x,y) ;
                 identifySquares(x,y,ship);
             }
             centerShipOnSquare(ship);
             placeShipOnGrid(ship);
             deselectSquares();
         }
     }
 }
