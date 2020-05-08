 package galapagos;
 
 import java.util.Random;
 
 public class RandomFinch implements Behavior {
     /**
      * Chooses randomly between CLEANING and IGNORING the other finch
      */
     public Action decide(Finch finch) {
        int choice = ((int) Math.random()) * 2;
 
         switch (choice) {
         case 0:
             return Action.CLEANING;
         case 1:
             return Action.IGNORING;        
             
         //Unused
         default:
             return Action.CLEANING;
         }
     }
 
     /**
      * Doesn't use the finch's action.
      */
     public void response(Finch finch, Action action) {
         
     }
     
     /**
      * A new instance of this behavior.
      */
     public Behavior clone() {
         return new RandomFinch();
     }
     
     /**
      * A toString method.
      */
     public String toString() {
         return "Random";
     }
 }
