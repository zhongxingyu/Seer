 package balle.main;
 
 import static java.util.Arrays.asList;
 
 import java.io.IOException;
 
package balle.main;

import static java.util.Arrays.asList;

import java.io.IOException;

 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import balle.bluetooth.Communicator;
 import balle.controller.BluetoothController;
 import balle.controller.Controller;
 import balle.controller.DummyController;
 import balle.io.reader.SocketVisionReader;
 import balle.simulator.Simulator;
 import balle.simulator.SoftBot;
 import balle.strategy.AbstractStrategy;
 import balle.strategy.StrategyFactory;
 import balle.world.AbstractWorld;
 import balle.world.BasicWorld;
 import balle.world.SimpleWorldGUI;
 
 /**
  * This is where the main executable code for 4s lies. It is responsible of
  * initialising various subsystems of the code and make sure they interact.
  * 
  * 
  * @author s0909773
  * 
  */
 public class Runner {
 
     private static void print_usage() {
         try {
             getOptionParser().printHelpOn(System.out);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public static OptionParser getOptionParser() {
         OptionParser parser = new OptionParser();
         parser.acceptsAll(asList("s", "simulator"));
         parser.acceptsAll(asList("d", "dummy-controller"));
         parser.acceptsAll(asList("c", "colour", "color")).withRequiredArg()
                 .ofType(String.class);
         return parser;
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         OptionParser parser = getOptionParser();
         OptionSet options = parser.parse(args);
 
         // Get the colour
         boolean balleIsBlue;
         if ("blue".equals(options.valueOf("colour")))
             balleIsBlue = true;
         else if ("yellow".equals(options.valueOf("colour")))
             balleIsBlue = false;
         else {
             System.out
                     .println("Invalid colour provided, try one of the following:");
             System.out.println("javac balle.main.Runner -c blue");
             System.out.println("javac balle.main.Runner -c yellow");
             print_usage();
             System.exit(-1);
             balleIsBlue = false; // This is just to fool Eclipse about
                                  // balleIsBlue initialisation
         }
 
         if (options.has("simulator"))
             runSimulator(balleIsBlue);
         else
             runRobot(balleIsBlue, options.has("dummy-controller"));
     }
 
     public static void initialiseGUI(Controller controller, AbstractWorld world) {
         SimpleWorldGUI gui;
         GUITab mainWindow = new GUITab();
 
         StratTab strategyTab = new StratTab(controller, world);
         for (String strategy : StrategyFactory.availableDesignators())
             strategyTab.addStrategy(strategy);
 
         mainWindow.addToSidebar(strategyTab);
 
         gui = new SimpleWorldGUI(world);
         mainWindow.addToMainPanel(gui.getPanel());
         gui.start();
 
     }
 
     public static void runRobot(boolean balleIsBlue, boolean useDummyController) {
 
         AbstractWorld world;
         SocketVisionReader visionInput;
         Controller controller;
 
         // Initialise world
         world = new BasicWorld(balleIsBlue);
 
         // Moving this forward so we do not start a GUI until controller is
         // initialised
         // If you're getting a merge conflict here leave this before
         // SimpleWorldGUI start!
 
         if (useDummyController)
             controller = new DummyController();
         else
             controller = new BluetoothController(new Communicator());
 
         // Wait for controller to initialise
         while (!controller.isReady()) {
             continue;
         }
 
         initialiseGUI(controller, world);
 
         // Create visionInput buffer
         visionInput = new SocketVisionReader();
         visionInput.addListener(world);
     }
 
     public static void runSimulator(boolean balleIsBlue) {
         Simulator simulator = Simulator.createSimulator();
         BasicWorld world = new BasicWorld(balleIsBlue);
         AbstractStrategy s;
         simulator.addListener(world);
 
         SoftBot bot;
         if (!balleIsBlue)
             bot = simulator.getYellowSoft();
         else
             bot = simulator.getBlueSoft();
 
         System.out.println(bot);
 
         initialiseGUI(bot, world);
     }
 }
