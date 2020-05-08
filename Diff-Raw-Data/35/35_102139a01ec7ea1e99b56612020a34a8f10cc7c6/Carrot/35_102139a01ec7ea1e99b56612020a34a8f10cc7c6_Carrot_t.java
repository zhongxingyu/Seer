 import java.awt.Image;
 
 public class Carrot extends Plant {
     private static double calories;
 
     private static int maxAmount = 25;
     private static final int maxStepsUntilEdible = 50;
     
     public Carrot(Location loc){
         setLocation(loc);
         alive = true;
         amount = maxAmount;
         stepsUntilEdible = 0;
        System.out.println("Carrot: Calories:"+calories);
     }
 
     
     public static void setCalories(double c)  { 
         calories = c;
     }
     
     protected int getMaxAmount()           { return maxAmount;           }
     protected int getMaxStepsUntilEdible() { return maxStepsUntilEdible; }
     public double getCalories()               { return calories;            }
     public String toString() { return "Carrot"; }
     public Image getImage(){
         if(isAlive()){
             return Resources.imageByName("Carrot");
         } else {
             return null;
         }
     }
 }
