 /**
  *
  */
 package de.htwg.se.battleship.model;
 
 /**
  * This class contains all data of a single Cell
  * 
  * @author Philipp Daniels<philipp.daniels@gmail.com>
  * 
  */
 public class Cell {
 
    private final static int STATUS_NORMAL = 0;
    private final static int STATUS_SHOT   = 1;
    private final static int STATUS_HIT    = 2;
 
     private final int x;
     private final int y;
     private final String key;
     private final Grid grid;
     private Ship ship;
     private int status;
 
     /**
      * Create a new Cell instance with coordinates and initialize list for Ship
      * and Player.
      * 
      * @param x X-Coordinate
      * @param y Y-Coordinate
      */
     public Cell(int x, int y, Grid grid) {
         this.x = x;
         this.y = y;
         this.key = Cell.createKey(x, y);
         this.status = STATUS_NORMAL;
         this.grid = grid;
     }
 
     /**
      * Returns the x-coordinate of this Cell.
      * 
      * @return X-Coordinate
      */
     public int getX() {
         return x;
     }
 
     /**
      * Returns the y-coordinate of this Cell.
      * 
      * @return Y-Coordinate
      */
     public int getY() {
         return y;
     }
 
     /**
      * Return the key for Cell identification
      * 
      * @return Key
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Returns Grid instance.
      * 
      * @return Grid instance
      */
     public Grid getGrid() {
         return grid;
     }
 
     /**
      * Set relationship between Ship and cell (n:1 relationship)
      * 
      * @param ship Instance of a ship
      */
     public void setShip(final Ship ship) {
         this.ship=ship;
         ship.addCell(this);
     }
 
     /**
      * Returns true, when Cell has already an instance of this Ship.
      * 
      * @return Instance of ship
      */
     public Ship getShip() {
         return ship;
     }
 
     /**
      * Create with x- and y-coordinate a key for a Map
      * 
      * @param x X-Coordinate
      * @param y Y-Coordinate
      * @return Key of the cell
      */
     public static String createKey(final int x, final int y) {
         return x + "." + y;
     }
 
     /**
      * Returns true, when the Player has made nothing with this Cell.
      * 
      * @return Boolean
      */
     public boolean isNormal() {
         return (status == STATUS_NORMAL);
     }
 
     /**
      * Returns true, when the Player has hit a ship on this Cell.
      * 
      * @return Boolean
      */
     public boolean isHit() {
         return (status == STATUS_HIT);
     }
 
     /**
      * Returns true, when the Player has made previously a shot on this Cell.
      * 
      * @return Boolean
      */
     public boolean isShot() {
         return (status == STATUS_SHOT);
     }
 
     /**
      * Set status of this Cell to HIT (Player has hit a ship on this Cell).
      */
     public void setToHit() {
         status = STATUS_HIT;
     }
 
     /**
      * Set status of this Cell to SHOT (Player has made a shot on this Cell).
      */
     public void setToShot() {
         status = STATUS_SHOT;
     }
 }
