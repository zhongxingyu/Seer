 package sim;
 
 import calculations.DecisionValues;
 import calculations.Movement;
 import calculations.State;
 import util.Input;
 import util.Output;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: shyam
  * Date: 7/13/13
  * Time: 7:21 PM
  */
 public class RunSim {
 
    public static ArrayList<Input> INPUTRoads = new ArrayList<Input>();
    public static ArrayList<Output> OUTPUTRoads = new ArrayList<Output>();
 
     public static void main (String args[]) {
         Input zero = new Input(0);
         Input one = new Input(1);
         Input two = new Input(2);
         Input three = new Input(3);
 
         INPUTRoads.add(zero);
         INPUTRoads.add(one);
         INPUTRoads.add(two);
         INPUTRoads.add(three);
 
         DecisionValues decisionValues = new DecisionValues(8,"2.1 2.1 2.1 2.1 2.1 2.1 2.1 2.1 ");
 
         State currentState = new State("0220133101231230",decisionValues);
 
         ArrayList<Movement> movements = currentState.getMovements();
 
         for (Movement movement: movements) {
             System.out.println(movement.toString());
         }
     }
 
     /**
      *
      * @return ArrayList of INPUT Roads
      */
     public static ArrayList<Input> getINPUTRoads() {
         return INPUTRoads;
     }
 
     /**
      *
      * @return ArrayList of OUTPUT Roads
      */
     public static ArrayList<Output> getOUTPUTRoads() {
         return OUTPUTRoads;
     }
 }
