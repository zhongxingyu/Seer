 package battleship.objects;
 
 import battleship.logic.Game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * FieldView, handels placement of ships and shots
  *
  * @author Hinrich Kaestner, Tom Ohme
  */
 public class Field implements java.io.Serializable {
 
     private static final long serialVersionUID = 1L;
 
     static public enum Fieldelements {
         WATER, // virgin field
         SHIP,  // ship in perfect condition
         HIT,   // ship with a hit
         SHOT,  // a shot in the water
         SUNK,  // sunk ship
         ERROR  // everything else
     }
 
     static public enum Directionelements {
         HORIZONTAL,
         VERTICAL
     }
 
     private int sizeX = 10;
     private int sizeY = 10;
     private ArrayList<ArrayList<Fieldelements>> field = new ArrayList<ArrayList<Fieldelements>>();
 
     private ArrayList<Ship> ships = new ArrayList<Ship>();
 
     /**
      * Create a new field with a custom size.
      *
      * @param sizeX field width
      * @param sizeY field height
      */
     @SuppressWarnings("SameParameterValue")
     public Field(int sizeX, int sizeY) {
         this.sizeX = sizeX;
         this.sizeY = sizeY;
 
         for (int i = 0; i < this.sizeX; i++) {
             this.field.add(new ArrayList<Fieldelements>());
             for (int j = 0; j < this.sizeY; j++) {
                 this.field.get(i).add(Fieldelements.WATER);
             }
         }
     }
 
     /**
      * Returns the field width.
      *
      * @return field width
      */
     public int getSizeX() {
         return sizeX;
     }
 
     /**
      * Returns the field height
      *
      * @return field height
      */
     public int getSizeY() {
         return sizeY;
     }
 
     /**
      * Checks if all ships on this field are sunk.
      *
      * @return boolean true if all ships are sunk, false if not
      */
     public boolean getAllShipsSunk() {
         for (Ship ship : this.ships) {
             if (!ship.getSunk()) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Get the status of a specific field.
      *
      * @param posX field X value
      * @param posY field Y value
      * @return field status
      */
     public Fieldelements getFieldStatus(int posX, int posY) {
         if (posX < 0 || posX >= this.sizeX || posY < 0 || posY >= this.sizeY) {
             return Fieldelements.ERROR;
         }
         return this.field.get(posX).get(posY);
     }
 
     /**
      * Set a new status for a specific field.
      *
      * @param posX   field X value
      * @param posY   field Y value
      * @param status the new, desired status
      * @return the new status (not necessarily the same as the desired status)
      */
     Fieldelements setFieldStatus(int posX, int posY, Fieldelements status) {
         if (posX < 0 || posX >= this.sizeX || posY < 0 || posY >= this.sizeY) {
             return Fieldelements.ERROR;
         }
         Fieldelements place = this.getFieldStatus(posX, posY);
         if (place == status) {
             return Fieldelements.ERROR;
         }
 
         // set to ship
         switch (status) {
             case SHIP:
                 if (Fieldelements.WATER == place) {
                     field.get(posX).set(posY, Fieldelements.SHIP);
                     return Fieldelements.SHIP;
                 } else {
                     return Fieldelements.ERROR;
                 }
             case SHOT:
                 if (Fieldelements.WATER == place) {
                     field.get(posX).set(posY, Fieldelements.SHOT);
                     return Fieldelements.SHOT;
                 } else if (Fieldelements.SHIP == place) {
                     field.get(posX).set(posY, Fieldelements.HIT);
                     return Fieldelements.HIT;
                 } else {
                     return Fieldelements.ERROR;
                 }
             case SUNK:
                 if (Fieldelements.HIT == place) {
                     field.get(posX).set(posY, status);
                     return Fieldelements.SUNK;
                 } else {
                     return Fieldelements.ERROR;
                 }
         }
         // just in case
         return this.getFieldStatus(posX, posY);
     }
 
     /**
      * Add a new ship to the field.
      *
      * @param ship the ship
      * @return true if ship was successfully set, false if not
      */
     public boolean addShip(Ship ship) {
         int shipPosX = ship.getPosX();
         int shipPosY = ship.getPosY();
         int shipLength = ship.getLength();
 
         // check if ship is within field borders
         if ((Directionelements.HORIZONTAL == ship.getDirection() && shipPosX + shipLength > this.sizeX) || (Directionelements.VERTICAL == ship.getDirection() && shipPosY + shipLength > this.sizeY)) {
             return false;
         }
 
         // collision detection
         // horizontal ship
         if (Directionelements.HORIZONTAL == ship.getDirection()) {
             // check for a neighbouring ship above
             for (int i = -1; i <= shipLength; i++) {
                 if (Fieldelements.SHIP == getFieldStatus(shipPosX + i, shipPosY - 1)) {
                     return false;
                 }
             }
             // check for ship at the same place
             for (int i = -1; i <= shipLength; i++) {
                 if (Fieldelements.SHIP == getFieldStatus(shipPosX + i, shipPosY)) {
                     return false;
                 }
             }
             // check for neighbouring ship below
             for (int i = -1; i <= shipLength; i++) {
                 if (Fieldelements.SHIP == getFieldStatus(shipPosX + i, shipPosY + 1)) {
                     return false;
                 }
             }
             // set fields to SHIP
             for (int i = 0; i < shipLength; i++) {
                 setFieldStatus(shipPosX + i, shipPosY, Fieldelements.SHIP);
             }
             this.ships.add(ship);
             return true;
         }
 
         // vertical ship
         if (Directionelements.VERTICAL == ship.getDirection()) {
             // check for neighbouring ship on the left
             for (int i = -1; i <= shipLength; i++) {
                 if (Fieldelements.SHIP == getFieldStatus(shipPosX - 1, shipPosY + i)) {
                     return false;
                 }
             }
             // check for ship at the same place
             for (int i = -1; i <= shipLength; i++) {
                 if (Fieldelements.SHIP == getFieldStatus(shipPosX, shipPosY + i)) {
                     return false;
                 }
             }
             // check for neighbouring ship on the right
             for (int i = -1; i <= shipLength; i++) {
                 if (Fieldelements.SHIP == getFieldStatus(shipPosX + 1, shipPosY + i)) {
                     return false;
                 }
             }
             // set fields to SHIP
             for (int i = 0; i < shipLength; i++) {
                 setFieldStatus(shipPosX, shipPosY + i, Fieldelements.SHIP);
             }
             this.ships.add(ship);
             return true;
         }
         // just in case
         return false;
     }
 
     /**
      * Shoot on a specific field.
      *
      * @param posX X value of the shot
      * @param posY Y value of the shot
      * @return new status of the specific field
      */
     public Fieldelements shoot(int posX, int posY) {
         Fieldelements status = setFieldStatus(posX, posY, Fieldelements.SHOT);
         switch (status) {
             case HIT:
                 Fieldelements shipStatus;
                 for (Ship ship : this.ships) {
                     shipStatus = ship.shoot(posX, posY);
                     if (Fieldelements.SUNK == shipStatus) {
                         int posXHelper = posX;
                         int posYHelper = posY;
                         for (int i = 0; i < ship.getLength(); i++) {
                            try {
                                Fieldelements oldstatus = getFieldStatus(posXHelper, posYHelper);
                                if (Fieldelements.SHIP == oldstatus || Fieldelements.HIT == oldstatus) {
                                    this.field.get(posXHelper).set(posYHelper, Fieldelements.SUNK);
                                }
                            } catch (IndexOutOfBoundsException ignore) {
                            }
                             if (Directionelements.HORIZONTAL == ship.getDirection()) {
                                 posXHelper++;
                             } else {
                                 posYHelper++;
                             }
                         }
                         break;
                     }
                     if (Fieldelements.ERROR != shipStatus) {
                         break;
                     }
                 }
             default:
                 return status;
         }
     }
 
     /**
      * Get all ships on this field.
      *
      * @return all ships
      */
     public ArrayList<Ship> getShips() {
         return ships;
     }
 }
