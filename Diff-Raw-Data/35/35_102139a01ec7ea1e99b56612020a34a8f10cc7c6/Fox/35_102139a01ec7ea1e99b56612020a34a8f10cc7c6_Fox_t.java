 import java.util.ArrayList;
 import java.util.List;
 import java.awt.Image;
 import java.util.Collections;
 
 public class Fox extends Animal {
     private static double calories;
     private static final int maxBreedingTime = 10; 
     private static final int sightDistance = 10;
     private static final int moveDistance = 2;
     private static double maxHunger;
     private static final int maxAge = 100;
     
     private static ArrayList<String> prey = new ArrayList<String>();
     private static ArrayList<String> predators = new ArrayList<String>();
     private static ArrayList<String> hidingSpots = new ArrayList<String>();
     
     public Fox(Location loc){
         init(loc);
        System.out.println("Fox: Calories:"+calories+" Prey:"+prey+" Predators:"+predators+" Hiding Spots:"+hidingSpots);
     }
 
     public void addMyType(Grid grid, GridSquare square) {
         grid.addAnimal(new Fox(square.getLocation()), square);
     }
 
     public static void addPrey(String p)       { prey.add(p);        }
     public static void addPredator(String p)   { predators.add(p);   }
     public static void addHidingSpot(String p) { hidingSpots.add(p); }
     public static void setCalories(double c)   { 
         calories = c;     
         maxHunger = c * 10;
     }
 
     public int getMaxBreedingTime() {
         return maxBreedingTime;
     }
 
     protected double getMaxHunger()    { return maxHunger;                     }
     protected int getMaxAge()       { return maxAge;                        }
     protected int getSightDistance(){ return sightDistance;                 }
     protected int getMoveDistance() { return moveDistance;                  }
     
     protected ArrayList<String> getPredators()   { return predators;   }
     protected ArrayList<String> getPrey()        { return prey;        }
     protected ArrayList<String> getHidingSpots() { return hidingSpots; }
     
     public double getCalories()        { return calories;                      }
     public String toString()        { return "Fox";                         }
     public Image getImage()         { return Resources.imageByName("Fox");  }
 }
