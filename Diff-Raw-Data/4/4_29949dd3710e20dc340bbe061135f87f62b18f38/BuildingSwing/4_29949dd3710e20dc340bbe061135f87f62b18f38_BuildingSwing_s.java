 import java.awt.Color;
 import java.awt.FlowLayout;
 
 import javax.swing.JFrame;
 
 /**
  * Creates a GUI that simulates the elevators in an entire building
  * with a certain number of floors and elevators
  * @author Kim
  *
  */
 public class BuildingSwing {
 
    /** Floors in the building */
    int numFloors;
    /** Elevators in the building */
    int numElevators;
    /** Array of sliders to represent each elevator */
    ElevatorSlider[] elevator;
    /** GUI frame */
    JFrame frame;
 
    /**
     * Creates a new GUI for the Building View
     * @param numFloors Number of floors in the building
     * @param numElevators Number of elevators in the building
     * @param system Control system for the elevator
     */
    public BuildingSwing (int floors, int elevators) {
       numFloors = floors;
       numElevators = elevators;
       elevator = new ElevatorSlider[numElevators];
    }
 
    /**
     * Generates the building view using sliders to represent the elevators
     */
    public void init (Elevator[] inputElevators) {
       System.out.println ("Creating GUI");
       // Create a JFrame with "Elevator Proposal" as the title
       frame = new JFrame ("Building View");
       // Set the frame so that the program stops when the frame is closed
       frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
       System.out.println ("Setting frame size");
       frame.setSize (500, 500);
 
       // Generate the elevators
       for (int i = 0; i < numElevators; i++) {
          elevator[i] = new ElevatorSlider (inputElevators[i], i + 1);
          frame.add (elevator[i]);
       }
       frame.setLayout (new FlowLayout ());
       frame.setVisible (true);
       frame.setBackground (Color.white);
       frame.pack ();
    }
 
    /**
     * Changes the elevator positions
     * @param rate The number of milliseconds between "frames"
     * @throws InterruptedException 
     */
    public void update (int rate, Elevator[] elevators)
    throws InterruptedException {
       // Get the state of each individual elevator and then update
       for (int i = 0; i < numElevators; i++) {
          // Update the value
         elevator[i].update (elevators[i], i);
       }
       Thread.sleep (rate);
    }
 }
