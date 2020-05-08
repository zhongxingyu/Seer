 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Nikolaos, Anthony
  */
 
import java.util.Vector;
import javax.swing.ImageIcon;
 
 public class AlienShip extends Ship{
     
    public AlienShip(int fireRate, int lives, int heading, Vector velocity, int[] coordinates, ImageIcon img){
        super(heading, coordinates, img, fireRate, lives);
     }
 }
