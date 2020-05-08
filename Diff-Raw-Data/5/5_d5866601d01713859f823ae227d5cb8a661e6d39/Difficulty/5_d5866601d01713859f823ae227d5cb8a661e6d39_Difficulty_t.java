 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package game;
 
 import gui.MenuGUI;
 import java.util.Random;
 
 /**
  *
 * @author Nikolaos Bukas
  */
 public class Difficulty {
     private static int level;
     private static Random rand= new Random();
     
     public void setLevel(int level)
     {
         this.level = level;
     }
     
     public static float randomHeading()
     {
         return randomFloat() * 360;
     }
     
     public static float randomAsteroidVelocity()
     {
         return randomFloat() * rand.nextInt(20);
     }
     
     public static float randomAlienVelocity()
     {
         return randomFloat() * rand.nextInt(20);
     }
     
     public static int randomXPos()
     {
         return rand.nextInt(MenuGUI.WIDTH);
     }
     
     public static int randomYPos()
     {
         return rand.nextInt(MenuGUI.HEIGHT);
     }
     
     public String bonusDropRate()
     {
         return null;
     }
     
     public static float randExplosionVelocity()
     {
        return (rand.nextInt(5) + 1)*(rand.nextFloat() - rand.nextFloat());
     }
     
     
     private static float randomFloat()
     {
         return rand.nextFloat() * 2 - 1;
     }
 }
