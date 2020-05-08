 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Nikolaos, Anthony
  */
 
import java.awt.Polygon;
 
 public class AlienShip extends Ship{
     
    public AlienShip(int velocity, int heading, int[] coordinates, Polygon shape, GameState gameState, int fireRate, int lives){
        super(velocity, heading, coordinates, shape, gameState, fireRate, lives);
     }
 }
