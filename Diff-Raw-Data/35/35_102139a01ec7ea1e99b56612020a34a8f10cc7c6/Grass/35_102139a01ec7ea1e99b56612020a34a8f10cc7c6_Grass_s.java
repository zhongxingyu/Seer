 import java.awt.Image;
 
 public class Grass extends Plant {
     private static double calories;
 
     private static int maxAmount = 50;
     private static final int maxStepsUntilEdible = 50;
     
     public Grass(Location loc) {
         setLocation(loc);
         alive = true;
         amount = maxAmount;
         stepsUntilEdible = maxStepsUntilEdible;
     }
     
     
     public static void setCalories(double c)  { 
         calories = c;
     }
     
     protected int getMaxAmount()           { return maxAmount;           }
     protected int getMaxStepsUntilEdible() { return maxStepsUntilEdible; }
     public double getCalories()               { return calories;            }
     public String toString() { return "Grass"; }
     public Image getImage(){
         if(isAlive()){
             return Resources.imageByName("Grass");
         } else {
             return null;
         }
     }
 
    
 }
