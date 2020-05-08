 package model.playground;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import model.general.Constances;
 import model.general.Status;
 
 /** 
  * The Playground class provide all operation which are necessary to interact with
  * a playground (shoot to playground, place ships,..).
  * @author Dennis Parlak
  */
 public class Playground extends AbstractPlayground {
     private List<Ship> ships;
     private int numberOfDestroyedShips;
     private Status status;
     
     /**
      * Create a Playground object.
      * @param rows The number of columns of the playground
      * @param columns The number of rows of the playground
      */
     public Playground(int rows, int columns) {
         super(rows, columns);
         this.ships = new LinkedList<Ship>();
         this.status = new Status();
         this.placeShipsRandom();
     }
     
     /**
      * Create a Playground object.
      * @param rows The number of columns of the playground
      * @param columns The number of rows of the playground
      */
     public Playground(PlaygroundCell[][] matrix) {
         super(matrix);
         this.ships = new LinkedList<Ship>();
         this.status = new Status();
         this.placeShipsOnOldPosition();
     }    
     /**
      * Return the status object, which log information about the program flow after
      * calling e.g. shoot(..)  method.
      * @return status The Status object.
      */
     public Status getStatus() {
         return status;
     }
     /**
      * Method to place a ship on given Coordinates into a given direction.
      * @param target The Coordinates where to place the ship
      * @param ship The Ship to place
      * @param direction The direction in which to place the ship
      * @return true if the ship could be placed.
      * @return false if the ship could not be placed.
      */
     private boolean placeShip(Coordinates target, Ship ship, int vertical, int horizontal) {
         Coordinates coords = target;
         
         //loop until the ship is complete placed
         for (int shipSize = ship.getLength(); shipSize > 0; shipSize--) {
             //check if the row and column is not in range
             if (!this.checkCoordinates(new Coordinates(coords))) {
                 status.addError("Ship cannot be placed, because Coordinates are invalid.");
                 return false;
             }
             
             //check if a ship is too near
             if (0 != checkForNearShips(coords)) {
                 return false;
             }   
             coords = new Coordinates(coords.getRow() + vertical, coords.getColumn() + horizontal);
         }
         //try to add the ship and check if it failed
         if (!addShip(ship)) {
             return false;
         }
         //place the ship on the playground
         placeShipOnPlayground(target, ship, vertical, horizontal);
         return true;
     }
         
     /**
      * Method to shoot on given Coordinates
      * @param target The Coordinates which where to shoot.
      * @return status of shot
      */
     public int shoot(Coordinates target) {
         return (Constances.MATRIX_INIT != this.get(target)) ? validShot(target) : invalidShot(target);
     }
    
     /**
      * Method to save a valid shot
      * @param target The Coordinates which where to shoot.
      * @return the status of the ship which was hit
      */
     private int validShot(Coordinates target) {
         char shipId = get(target);
         set(target, Constances.MATRIX_HIT);
         return updateShipsAfterHit(shipId);  
     }
    
     /**
      * Method to save an invalid shot
      * @param target The Coordinates which where to shoot.
      * @return the status of an invalid shot
      */
     private int invalidShot(Coordinates target) {
         status.addText("No ship was hit.");    
         set(target, Constances.MATRIX_MISS);
         return Constances.SHOOT_MISS;
     }
     
     /**
      * Method to check if on given Coordinates where already a shot.
      * @param target The Coordinates which to check.
      * @return true if there was already shot to the Coordinates
      * @return false if there was not shot to the Coordinates
      */
     public boolean alreadyShot(Coordinates target) {
         return Constances.MATRIX_HIT == get(target) || Constances.MATRIX_MISS == get(target);
     }
     
     /**
      * Return the number of ships which still exist.
      * @return number of ships
      */
     public int getNumberOfExistingShips() {
         return this.ships.size() - numberOfDestroyedShips;
     }
      
     
     /**
      * Helper Method to place a ship with the given Coordinates on the playground.
      * There should be checked before calling this Method if it is possible to place
      * the ship, because this method expect that it is possible.
      * @param target The Coordinates where to start placing the ship.
      * @param ship The ship which to place
      * @param direction The direction into which to place the ship
      */
     private void placeShipOnPlayground(Coordinates target, Ship ship, int vertical, int horizontal) {
        Coordinates coords = new Coordinates(target);
         for (int shipSize = ship.getLength(); shipSize > 0; shipSize--) {
            setShip(coords, ship.getId());
            coords = new Coordinates(coords.getRow() + vertical, coords.getColumn() + horizontal);
         }
     }
     
     /**
      * Helper Method to add a ship to the existing ships
      * @param ship The ship to add.
      * @return true if a given ship, with this id still not exist
      * @return false if a ship already exist with the same id. The ship was not add.
      */
     private boolean addShip(Ship ship) {
         if (ships.contains(ship)) {
             status.addError("Cannot add ship, because a ship with the id '"+ship.getId()+"' does already exist.");
             return false;
         }
         ships.add(ship);
         return true;
     }
     
     /**
      * Helper Method to updated the List with all ships after any ship was hit.
      * @param shipId The shipId which comes from the plaground.
      */
     private int updateShipsAfterHit(char shipId) {
         for (Ship s: this.ships) {
             if (s.getId() == shipId) {
                 return updateShip(s);
             }
         }    
         throw new IllegalArgumentException("A Ship with the given id '"+shipId+"' does not exist.");
     }
     /**
      * Helper Method to updated one ship. There will be checked if the ships was hit or destroyed.
      * Call this method only if a ship was really hit.
      * @param s The ship to check.
      * @param method will return SHOOT_DESTROYED or as default SHOOT_HIT.
      */
     private int updateShip(Ship s) {
         s.setDamage();
         if (s.isDestroyed()) {
             status.addText("Destroyed ship: "+s.getName());
             numberOfDestroyedShips++;
             return Constances.SHOOT_DESTROYED;
         }
         status.addText("A ship was hit.");    
         return Constances.SHOOT_HIT;
     }
     
     /**
      * Helper Method to check if on the given row and column can be placed a ship. Round through
      * this coordinate should be no other ship placed.
      * @param row The row to check
      * @param column The column to check
      * @return true if at the coordinates can be placed a ship
      * @return false if at the coordinates can no ship be placed
      */
     private int checkCoord(Coordinates target) {
         if (!checkCoordinates(target)) {
             return 0;
         }
         if (Constances.MATRIX_INIT != this.get(target)) {
             return 1;
         }
         return 0;
     }
     
     /**
      * Helper Method to get the number of near ships of a given Coordinate in the playground
      * @param row The row coordinate
      * @param column The column coordinate
      * @return The number of near ships (0 if no ship is near)
      */
     private int checkForNearShips(Coordinates target) {
         int[] checkRows = {0, 1, -1, 0, 0, 1, 1, -1, -1};
         int[] checkColumns = {0, 0, 0, -1, 1, -1, 1, 1, -1};
         int nearShipsCount = 0;
         
         for (int i = 0; i < checkRows.length; i++) {
             nearShipsCount += checkCoord(new Coordinates(target.getRow() + checkRows[i], target.getColumn() + checkColumns[i]));
         }
         return nearShipsCount;
     }
     
     private void placeShipsOnOldPosition() {
         this.ships = createShips();
         for (int row = 0; row < getRows(); row++) {
             for (int column = 0; column < getColumns(); column++){
                 Coordinates coord = new Coordinates(row, column);
                 if (Constances.MATRIX_HIT == get(coord)) {
                     updateShipsAfterHit(getShip(coord));
                 }
             }
         }
     }
     
     /**
      * Place a list of ships random on the playground
      * @param ships A list with the ships
      */
     private void placeShipsRandom() {
         List<Ship> newShips = createShips();
         while (!newShips.isEmpty()) {
             Ship s = newShips.get(0);
             Coordinates coords = randomCoords();
             int vertical = random(-1, +1);
             int horizontal = (vertical == 0) ? -1 : 0;
             if (placeShip(coords, s, vertical, horizontal)) {
                 newShips.remove(0);
             }
         }
     }
     /**
      * Returns a fleet of ships
      * @return A ship list with ships
      */
     private List<Ship> createShips() {
         List<Ship> tmp = new LinkedList<Ship>();
         tmp.add(new Ship("Battleship", Ship.LENGTHBATTLESHIP, 'A'));
         tmp.add(new Ship("1st Cruiser", Ship.LENGTHCRUISER, 'B'));
         tmp.add(new Ship("2nd Cruiser", Ship.LENGTHCRUISER, 'C'));
         tmp.add(new Ship("1st Destroyer", Ship.LENGTHDESTROYER , 'D'));
         tmp.add(new Ship("2nd Destroyer", Ship.LENGTHDESTROYER, 'E'));
         tmp.add(new Ship("1st Submarine", Ship.LENGTHSUBMARINE, 'F'));
         tmp.add(new Ship("2nd Submarine", Ship.LENGTHSUBMARINE, 'G'));
         tmp.add(new  Ship("3rd Submarine", Ship.LENGTHSUBMARINE, 'H'));
         return tmp;
     }
     /**
      * Generates random Coordinates
      * @return Random coordinates where a ship could be placed
      */
     private Coordinates randomCoords() {
         return new Coordinates(random(0, this.getRows()), random(0, this.getColumns()));
     }
     
     /**
      * Generate Random numbers in a range (min, max)
      * @param min Minimum number (inclusive)
      * @param max Maximum number (inclusive)
      * @return A random number
      */
     private int random(int min, int max) {
         return (int) (Math.random() * ((max + 1) - min) + min); 
     }
 }
