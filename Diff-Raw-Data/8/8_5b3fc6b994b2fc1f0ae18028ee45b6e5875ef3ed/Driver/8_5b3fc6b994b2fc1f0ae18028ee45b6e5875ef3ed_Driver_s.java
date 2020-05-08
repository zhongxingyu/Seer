 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Scanner;
 
 public class Driver {
 
     // Create a method that reads input from Joe's output
     // For each person, if they are inserted, create as a new person.
     // Then insert into the building accordingly
     public static int idGenerator = 1;
 
     public static void main (String[] args) {
 
         runDriver ();
     }
 
     private static void runDriver () {
         /**
          * This method reads output from the test case generator. The output
          * contains the format <command>
          * <time><direction><initialFloor><destinationFloor> Given the command,
          * move or create, then the driver will either insert the person in the
          * building, or get the person ready in the queue towards its direction.
          * Manager will start moving when there is a change in time.
          */
         Building building = new Building (10, 3);
         Elevator[] elevators = new Elevator[building.getNumElevators ()];
         for (int i = 0; i < elevators.length; i++) {
             elevators[i] = new Elevator (10, 0, 9, 0, "d");
         }
         ElevatorManager manager = new ElevatorManager (elevators, building, "d");
         BuildingSwing gui = new BuildingSwing (10, 3);
         gui.init (elevators);
         // Read file line by line
         try {
            FileInputStream fstream = new FileInputStream ("rawOutput1.txt");
             DataInputStream in = new DataInputStream (fstream);
             BufferedReader br = new BufferedReader (new InputStreamReader (in));
             String strLine;
             Scanner scan;
             String movement;
             int time = 0;
             int currentTime = 0;
             int initialFloor;
             int destFloor;
             int direction;
             int currentLine = 0;
             int difference = 0;
             while (true) {
                 // Manage for every single time
                 strLine = br.readLine ();
                 if (strLine == null) {
                     while (building.moreCallsToBeServed ()
                             || elevators[0].getCurCap () > 0) {
                         manager.manage ();
                        gui.update (0, elevators);
                     }
                     break;
                 }
                 scan = new Scanner (strLine);
                 movement = scan.next ();
                 time = scan.nextInt ();
                 direction = scan.nextInt ();
                 initialFloor = scan.nextInt ();
                 destFloor = scan.nextInt ();
                 Person current;
                 if ( ( time != currentTime) && currentLine != 0) { // check this
                                                                    // line for
                                                                    // bugs
                     // Do the movements since there is a change on management
                     difference = time - currentTime;
                     for (int i = 0; i < difference; i++) {
                         manager.manage ();
                        gui.update (0, elevators);
                         for (int j = 0; j < elevators.length; j++) {
                             if ( ( elevators[j].getCurrentFloor () > building.numbFloors)
                                     || ( elevators[j].getCurCap () < 0)) {
                                 System.out.println ("Error here. Fix me!");
                             }
                         }
                     }
                     // manager.manage ();
                 }
                 if (movement.equals ("CREATE")) {
                     building.enterBuilding (new Person (idGenerator++, time,
                             destFloor, direction));
                 } else if (movement.equals ("MOVE")) {
                     /**
                      * If moves, then take a person from the static queue in the
                      * given initial floor(current floor), move it to the new
                      * queue, given its direction and new floor.
                      */
 
                     if (building.floors[initialFloor][Building.STATIC].size () > 0) {
                         current = building.remove (initialFloor,
                                 Building.STATIC);
                         current.setDestinationFloor (destFloor);
                         current.setDirection (direction);
                         building.insertInFloor (initialFloor, current);
                         System.out.println ("move");
                     } else {
                         current = new Person (idGenerator, time, destFloor,
                                 direction);
                         building.insertInFloor (initialFloor, current);
                     }
 
                 }
                 currentTime = time;
                 currentLine++;
             }
             for (int i = 0; i < elevators.length; i++) {
                 System.out.println ("Elevator " + i + " is in floor: "
                         + elevators[i].getCurrentFloor () + " and has "
                         + elevators[i].getCurCap () + " people.");
             }
             // Close the input stream
             in.close ();
         } catch (Exception e) {// Catch exception if any
             System.err.println ("Error: " + e.getMessage ());
         }
     }
 
 }
