 package balle.main;
 
 import balle.controller.Controller;
 import balle.controller.DummyController;
 import balle.io.reader.SocketVisionReader;
 import balle.simulator.Simulator;
 import balle.simulator.SoftBot;
 import balle.strategy.AbstractStrategy;
 import balle.strategy.DummyStrategy;
 import balle.strategy.UserInputStrategy;
 import balle.world.AbstractWorld;
 import balle.world.BasicWorld;
 
 /**
  * This is where the main executable code for BALL-E lies. It is responsible of
  * initialising various subsystems of the code and make sure they interact.
  * 
  * 
  * @author s0909773
  * 
  */
 public class Runner {
 
     private static void print_usage() {
         System.out.println("Usage: java balle.main.Runner <balle_colour>");
         System.out
                 .println("Where <balle_colour> is either \"blue\" or \"yellow\"");
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
     	// Check the usage
         if (args.length != 1) {
             print_usage();
             System.exit(-1);
         }
 
         // Get the colour
         boolean balleIsBlue;
         if (args[0].equals("blue"))
             balleIsBlue = true;
         else if (args[0].equals("yellow"))
             balleIsBlue = false;
         else {
             System.out.println("Invalid colour provided");
             print_usage();
             System.exit(-1);
             balleIsBlue = false; // This is just to fool Eclipse about
                                  // balleIsBlue initialisation
         }
         
         // runRobot(balleIsBlue);
         runSimulator(balleIsBlue);
     }
     
     public static void runRobot(boolean balleIsBlue) {
     	
     	AbstractWorld world;
         SocketVisionReader visionInput;
         Controller controller;
         AbstractStrategy strategy;
 
         
      // Initialise world
         world = new BasicWorld(balleIsBlue);
 
         // Create visionInput buffer
         visionInput = new SocketVisionReader();
         visionInput.addListener(world);
 
         // Initialise controller
         // controller = new BluetoothController(new Communicator());
         controller = new DummyController();
         strategy = new DummyStrategy(controller, world);
         // Wait for controller to initialise
         while (!controller.isReady()) {
             continue;
         }
         // Once the controller is ready, start the strategy
         strategy.start();
     }
     
     public static void runSimulator(boolean balleIsBlue) {
     	Simulator simulator = Simulator.createSimulator();
     	BasicWorld world = new BasicWorld(balleIsBlue);
     	simulator.addListener(world);
     	
     	SoftBot bot;
     	if (balleIsBlue)
     		bot = simulator.getYellowSoft();
     	else 
     		bot = simulator.getBlueSoft();
     	
    	AbstractStrategy s = new UserInputStrategy(bot, new BasicWorld(true));
 		s.start();
     	
     }
 }
