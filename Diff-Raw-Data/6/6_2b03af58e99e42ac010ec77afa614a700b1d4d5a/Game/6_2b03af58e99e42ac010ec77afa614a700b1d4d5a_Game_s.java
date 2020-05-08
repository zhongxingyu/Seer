 package battleship.logic;
 
 import battleship.objects.Ship;
 import battleship.objects.Field;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * most important class!!!!!
  *
  * @author H. Kaestner
  */
 public class Game implements java.io.Serializable {
     static public enum Playerelements {
         USER,
         COMPUTER,
         ERROR
     }
     private Field fieldUser;
     private Field fieldComputer;
     private ArrayList<Ship> shipTypes;
 
     public Game(int sizeX, int sizeY, ArrayList<Ship> ships) {
         this.fieldUser = new Field(sizeX, sizeY);
         this.fieldComputer = new Field(sizeX, sizeY);
         this.shipTypes = ships;
 
         this.placeShipsComputer();
     }
 
     public Game() {
         this.shipTypes = new ArrayList<Ship>();
         this.fieldUser = new Field(10, 10);
         this.fieldComputer = new Field(10, 10);
 
         Ship ac_carrier = new Ship(5, "Aircraft carrier", "ac_carrier.png");
         shipTypes.add(ac_carrier);
         Ship cruiser = new Ship(4, "Cruiser", "destroyer.jpg");
         shipTypes.add(cruiser);
         Ship destroyer = new Ship(3, "Destroyer", "destroyer3.png");
         shipTypes.add(destroyer);
         Ship submarine = new Ship(3, "Submarine", "submarine2.png");
         shipTypes.add(submarine);
         Ship frigate = new Ship(2, "Frigate", "frigate.png");
         shipTypes.add(frigate);
 
         this.placeShipsComputer();
         for(Ship ship : shipTypes) {
             ship.unset();
         }
     }
 
     public Ship getNextUnsetShip() {
         for (Ship ship :shipTypes) {
             if(!ship.isSet()) {
                 return ship;
             }
         }
         return null;
     }
 
     /**
      * adds a ship to the user field
      * @param ship Ship
      * @return boolean
      */
     public boolean placeShipUser(Ship ship) {
         return fieldUser.addShip(ship);
     }
 
     public Field.Fieldelements placeShotUser(int posX, int posY) {
         return fieldComputer.shoot(posX, posY);
     }
 
     private void placeShipsComputer() {
         List<Ship> computerShips = this.shipTypes;
         Random random = new Random();
         int sizeX = this.fieldComputer.getSizeX();
         int sizeY = this.fieldComputer.getSizeY();
 
         for (Ship ship : computerShips) {
             do {
                 int posX = random.nextInt(sizeX + 1);
                 int posY = random.nextInt(sizeY + 1);
                 int pick = random.nextInt(Field.Directionelements.values().length);
                 ship.setPosition(posX, posY, Field.Directionelements.values()[pick]);
             } while (!fieldComputer.addShip(ship));
         }
     }
 
     /**
      * automatically places a shot on the gamers field
      * algorithm based on an idea by someone at the TU Munich who didn't write his name in the comments of his/her C code
      *
      * @return Field.Fieldelements
      */
     public Field.Fieldelements placeShotComputer() {
         int sizeX = this.fieldUser.getSizeX();
         int sizeY = this.fieldUser.getSizeY();
 
         // probability that a field contains a ship (not in percent, 1000 is best)
         double[][] probability = new double[sizeX][sizeY];
         double sumProbability = 0;
 
         for (int posX = 0; posX < sizeX; posX++) {
             for (int posY = 0; posY < sizeY; posY++) {
                 // set probability for each field to 1
                 probability[sizeX][sizeY] = 1.;
 
                 // check neighbouring fields for hits and set probability to 1000
                 if (Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX, posY - 1) ||
                         Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX, posY + 1) ||
                         Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX - 1, posY) ||
                         Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX + 1, posY)) {
                     probability[posX][posY] = 1000;
                 }
 
                 // 0 points if all fields above and below are SUNK or SHOT
                 if ((Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX, posY - 1) || Field.Fieldelements.WATER == fieldUser.getFieldStatus(posX, posY - 1)) &&
                         (Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX, posY + 1) || Field.Fieldelements.WATER == fieldUser.getFieldStatus(posX, posY + 1)) &&
                         (Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX - 1, posY) || Field.Fieldelements.WATER == fieldUser.getFieldStatus(posX - 1, posY)) &&
                         (Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX + 1, posY) || Field.Fieldelements.WATER == fieldUser.getFieldStatus(posX + 1, posY))
                         ) {
                     probability[posX][posY] = 0;
                 }
 
                 // 0 points if a neighbouring field is SUNK
                 if (Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX - 1, posY - 1) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX - 1, posY) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX - 1, posY + 1) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX, posY - 1) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX, posY + 1) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX + 1, posY - 1) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX + 1, posY) ||
                         Field.Fieldelements.SUNK == fieldUser.getFieldStatus(posX + 1, posY + 1)
                         ) {
                     probability[posX][posY] = 0;
                 }
 
                 // 0 points if top right, top left, bottom right or bottom left is HIT
                 if (Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX - 1, posY - 1) ||
                         Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX - 1, posY + 1) ||
                         Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX + 1, posY - 1) ||
                         Field.Fieldelements.HIT == fieldUser.getFieldStatus(posX + 1, posY + 1)
                         ) {
                     probability[posX][posY] = 0;
                 }
 
                 // 0 points if a shot has already been placed
                 if (Field.Fieldelements.WATER != fieldUser.getFieldStatus(posX, posY) &&
                         Field.Fieldelements.SHIP != fieldUser.getFieldStatus(posX, posY)) {
                     probability[posX][posY] = 0;
                 }
             }
         }
 
         // calculate sum of all points
         // TODO check if probability must be smaller numbers
         for (int posX = 0; posX < sizeX; sizeX++) {
             for (int posY = 0; posY < sizeY; posY++) {
                 sumProbability += probability[posX][posY];
             }
         }
 
         // random element
         Random random = new Random();
        sumProbability = random.nextInt((int) sumProbability--);
         sumProbability++; // must at least be 1
         int posY = -1;
         int posX = -1;
         while (posY < sizeY - 1 && sumProbability >= 0) {
             posY++;
             posX = -1;
             while (posX < sizeX && sumProbability >= 0) {
                 posX++;
                 sumProbability = sumProbability - probability[posX][posY];
             }
         }
         return fieldUser.shoot(posX, posY);
     }
 
     /**
      * @return Playerelements
      */
     public Playerelements getWinner() {
         if (this.fieldComputer.getAllShipsSunk()) {
             return Playerelements.COMPUTER;
         }
         if (this.fieldUser.getAllShipsSunk()) {
             return Playerelements.USER;
         }
         return Playerelements.ERROR;
     }
 }
