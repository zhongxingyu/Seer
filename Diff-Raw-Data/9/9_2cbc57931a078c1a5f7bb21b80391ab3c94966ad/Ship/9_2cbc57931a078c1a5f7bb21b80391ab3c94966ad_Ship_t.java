 package fr.iutvalence.java.mp.Battleship;
 
 import java.util.Random;
 
 // TODO  FIXED detail comment, it is not understandable as is
 /**
  * A Ship is an array of ShipArea :  a ship is divided in several part 
  * @author begous
  * 
  */
public class Ship 
 {
     /**
      * Ship array : contain area ship (in order or in disorder)
      */
     // TODO FIXED rename field (more explicit with regards to the game)
     private ShipArea[] boat;
 
     // TODO (fix) rewrite comment (how is the ship once created?)
     /**
      * Initialize a ship then created by locationShip()
      */
     public Ship()
     {
         // TODO (fix) all fields must be initialized
         
     }
 
     /**
      * Research if targeted area is contained in the ship and if yes, touched it
      * 
      * @param c  coordinate of targeted area
      * @return true if ship is hit, else false (coordinate not found in the ship)
      */
     // TODO (fix) use exception to handle bad coordinates
     public boolean isHitAt(Coordinates c)
     {
         int i = 0;
         while (i < this.boat.length && this.boat[i].getPosition() != c)
         {
             i = i + 1;
         }
 
         if (i < this.boat.length)
         {
             this.boat[i].setHit(true);
             return true;
         }
         // TODO (fix) simplify
         else
         {
             return false;
         }
     }
 
     /**
      * place a ship in the grid
      * 
      * @param n :area number of ship to create
      */
     public void locationShip(int n)
     {
         Random r = new Random();
         int position = r.nextInt(1); // 0 : horizontal and 1 : vertical
         int x;
         int y;
         int i = 0;
         Coordinates couple;
         ShipArea[] ship = new ShipArea[n];
 
         if (position == 0)
         {
             x = 1 + r.nextInt(10 -n);
             y = 1 + r.nextInt(9);
             couple = new Coordinates(x, y);
             while (i < n)
             {
                 ship[i] = new ShipArea(couple);
                 i = i + 1;
                 x = x + 1;
                 couple = new Coordinates(x, y);
             }
             this.boat = ship;
         }
         else
         {
             x = 1 + r.nextInt(9);
             y = 1 + r.nextInt(10 - n);
             couple = new Coordinates(x, y);
             while (i < n)
             {
                 ship[i] = new ShipArea(couple);
                 i = i + 1;
                 y = y + 1;
                 couple = new Coordinates(x, y);
             }
             this.boat = ship;
         }
     }
 
 }
