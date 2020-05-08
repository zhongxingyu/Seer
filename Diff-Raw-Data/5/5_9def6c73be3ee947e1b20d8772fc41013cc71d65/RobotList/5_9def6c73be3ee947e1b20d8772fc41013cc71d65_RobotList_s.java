 package arena;
 
 /* Default libraries */
 import java.util.Random;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Comparator;
 
 /* Libraries */
 import exception.*;
 import operation.*;
 import parameters.*;
 
 // Import links
 import static parameters.Game.*;
 
 /**
  * <b>Auxiliar class RobotList</b><br>
  * Impelments a list to be used within
  * the World to control the arena.
  *
  * @author Renato Cordeiro Ferreira
  * @author Vinicius Silva
  */
 final public class RobotList implements Iterable<Robot>
 {
     // Robot's list and info
     private Robot[] armies;
     private HashMap <Integer,Double> speedy 
         = new HashMap <Integer,Double>();
     
     private HashMap <Robot,Operation> actions
         = new HashMap <Robot,Operation>();
     
     // Random number generator
     final private Random rand = RAND;
     
     // Last empty space
     private int population = 0;
     private int emptySpace = 0;
     
     /**
      * Default Constructor<br>
      * Create a list of robots for 'n' players,
      * with ROBOTS_NUM_MAX robots for each.
      * 
      * @param nPlayers Number of players
      */
     RobotList(int nPlayers) 
     {
         Debugger.say("[RobotList] Builded");
         armies = new Robot[nPlayers * ROBOTS_NUM_MAX];
     }
     
     /** 
      * Add a robot in the list.
      * @param robot Reference to a robot
      */
     void add(Robot robot)
     {
         /* TODO: When the three initial are created,
          *       we will not need this test any more */
         if(robot == null) return;
         armies[emptySpace++] = robot;
         speedy.put(robot.ID, 1.0 * robot.speed);
         actions.put(robot, null);
         population++;
     }
     
     /**
      * Remove a robot from the list.
      * @param robot Reference to the robot 
      *              up to be removed
      */
     void remove(Robot robot)
     {
         for(int i = 0; i < emptySpace; i++) 
         {
             if(armies[i] == robot) 
             {
                 Robot r = armies[i];
                 armies[i] = null;
                 speedy.remove(r.ID);
                 actions.remove(robot);
                 break;
             }
         }
     }
     
     /**
      * Upload an operation for a given robot,
      * for being used as a sort method.
      * @param robot Robot of the turn
      * @param op    Operation to be stored
      */
     void setOperation(Robot robot, Operation op)
         throws NotInitializedException
     {
         if(actions.containsKey(robot))
             actions.put(robot, op);
         else throw new NotInitializedException(robot.toString());
     }
     
     /**
      * Recover the operation for the robot
      * of the turn.
      * @return Stored operation
      */
     Operation getOperation(Robot robot)
         throws NotInitializedException
     {
         if(actions.containsKey(robot))
             return actions.get(robot);
         else throw new NotInitializedException(robot.toString());
     }
     
     /**
      * Generate a new iterator.
      * @return Iterator to the robot list
      */
     public Iterator<Robot> iterator()
     {
         return new RobotListIterator(emptySpace);
     }
     
     /**
      * Sort the robot list accordingly to 
      * the robots speed. If there are conflicts,
      * solve the randomically.
      */
     void sort()
     {
         // TODO: redefine priority method
         for(Robot r: armies)
         {
             if(r == null) continue;
             speedy.put(r.ID, rand.nextDouble() + r.speed);
             Debugger.say("[SPEED][", r, "] ", speedy.get(r.ID));
         }
         
         // Debug (unordered array)
         Debugger.print("[INIT] ");
         for(int i = 0; i < emptySpace; i++) 
         {
             Robot r = armies[i];
             Debugger.print( (r != null) ? r : "null" );
             if(i != emptySpace-1) Debugger.print(", ");
         }
         Debugger.say();
         
         // Sorts array untill the first known empty space
         quickSort(0, emptySpace-1);
         
         // Updates first empty space position
         emptySpace = 0;
         for(Robot r: armies) 
             if(r == null) break; 
             else emptySpace++;
         
         // Debug (sorted array)
         Debugger.print("[SORT] ");
         for(int i = 0; i < emptySpace; i++) 
         {
             Robot r = armies[i];
             Debugger.print( (r != null) ? r : "null" );
             if(i != emptySpace-1) Debugger.print(", ");
         }
         Debugger.say("\n");
     }
     
     /**
      * Prints in a more legible format the
      * list with the robots (mainly with 
      * debug purposes).
      * @return List with robots
      */
     public String toString()
     {
         String robotList = "";
         for(Robot r: armies)
             robotList += (r + " ");
         return robotList;
     }
     
     /**
      * Number of robots created.
      * @return Number of robots built 
      *         along the game
      */
     int getPopulation()
     {
         return this.population;
     }
     
     /**
      * Quicksort implementation to be
      * used over the Robot List.
      * @param begin Start of the subarray
      * @param end   End of the subarray
      */
     private void quickSort(int begin, int end)
     {
         if(begin < end)
         {
             int middle = divide(begin, end);
             quickSort(begin, middle-1);
             quickSort(middle +1, end);
         }
     }
     
     /**
      * Auxiliar funcion for quicksort 
      * @param begin Start of the subarray
      * @param end   End of the subarray
      */
     private int divide(int begin, int end)
     {
         int     i = begin -1;
         Robot   x = armies[end];
        
         for(int j = begin; j <= end; j++)
             if(cmpLessRobot(armies[j], x))
             {
                 Robot t = armies[++i];
                 armies[i] = armies[j];
                 armies[j] = t;
             }
         
         return i;
     }
     
     /**
      * Comparison function used for quicksort
      * @param robotA First robot
      * @param robotB Second robot
      */
     private boolean cmpLessRobot(Robot robotA, Robot robotB)
     {
        if(robotA == null) return false;
         if(robotB == null) return true;
         
         /* Comparison function */
         double costA = speedy.get(robotA.ID);
         double costB = speedy.get(robotB.ID);
         return (costA <= costB);
     }
     
     /** 
      * <b>Inner class for iterator</b><br>
      * Inner class implementing interface 
      * Iterator to run over the Robot List.
      */
     private class RobotListIterator implements Iterator<Robot>
     {
         private int nextRobot = -1;
         final private int emptySpace;
         
         /**
          * Default constructor<br>
          * @param emptySpace Position untill which
          *                   it is possible to find
          *                   robots
          */
         public RobotListIterator(int emptySpace)
         {
             this.emptySpace = emptySpace;
         }
         
         /* Interface Iterator */
         public boolean hasNext()
         {
             return nextRobot + 1 != emptySpace;
         }
         
         /* Interface Iterator */
         public Robot next()
         {
             nextRobot++;
             return armies[nextRobot];
         }
         
         /* Interface Iterator */
         public void remove()
         {
             Robot r = armies[nextRobot];
             armies[nextRobot] = null;
             speedy.remove(r.ID);
         }
     }
 }
