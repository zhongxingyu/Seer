 import java.util.Hashtable;
 
 /**
  * Workout intervals from http://www.coolrunning.com/engine/2/2_3/181.shtml
  */
 public class Workouts {
     private static Hashtable workouts;
     static {
         workouts = new Hashtable();
 
         Workout testWorkout = new Workout(new int[] {
             20, 20, 20
         });
 
         Workout week1Workout = new Workout(new int[] {
             300, 60, 90, 60, 90, 60, 90, 60, 90, 60, 90, 60, 90
         });
         workouts.put("1-1", testWorkout);
         workouts.put("1-2", week1Workout);
         workouts.put("1-3", week1Workout);
 
         Workout week2Workout = new Workout(new int[] {
             300, 90, 120, 90, 120, 90, 120, 90, 120, 90, 120
         });
         workouts.put("2-1", week2Workout);
         workouts.put("2-2", week2Workout);
         workouts.put("2-3", week2Workout);
 
         Workout week3Workout = new Workout(new int[] {
             300, 90, 90, 180, 180, 90, 90, 180, 180
         });
         workouts.put("3-1", week3Workout);
         workouts.put("3-2", week3Workout);
         workouts.put("3-3", week3Workout);
 
         Workout week4Workout = new Workout(new int[] {
             300, 180, 90, 300, 150, 180, 90, 300
         });
         workouts.put("4-1", week4Workout);
         workouts.put("4-2", week4Workout);
         workouts.put("4-3", week4Workout);
 
         workouts.put("5-1", new Workout(new int[] {
            300, 380, 180, 300, 180, 300
         }));
         workouts.put("5-2", new Workout(new int[] {
             300, 480, 300, 480
         }));
         workouts.put("5-3", new Workout(new int[] {
             300, 1200
         }));
 
         workouts.put("6-1", new Workout(new int[] {
            300, 380, 180, 480, 180, 300
         }));
         workouts.put("6-2", new Workout(new int[] {
             300, 600, 180, 600
         }));
         workouts.put("6-3", new Workout(new int[] {
             300, 1500
         }));
 
         Workout week7Workout = new Workout(new int[] {
             300, 1500
         });
         workouts.put("7-1", week7Workout);
         workouts.put("7-2", week7Workout);
         workouts.put("7-3", week7Workout);
 
         Workout week8Workout = new Workout(new int[] {
             300, 1680
         });
         workouts.put("8-1", week8Workout);
         workouts.put("8-2", week8Workout);
         workouts.put("8-3", week8Workout);
 
         Workout week9Workout = new Workout(new int[] {
             300, 1800
         });
         workouts.put("9-1", week9Workout);
         workouts.put("9-2", week9Workout);
         workouts.put("9-3", week9Workout);
     }
 
     public static Workout getWorkout(int week, int workout) {
         return (Workout)workouts.get(week + "-" + workout);
     }
 }
