 /**
  * Uniform Simulator for Self-reconfigurable (modular) Robots
  * 
  * (C) 2006 University of Southern Denmark
  */
 package ussr.samples;
 
 import java.util.logging.Level;
 
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsLogger;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.jme.JMESimulation;
 import ussr.robotbuildingblocks.GeometryDescription;
 import ussr.robotbuildingblocks.Robot;
 import ussr.robotbuildingblocks.SphereShape;
 import ussr.robotbuildingblocks.VectorDescription;
 import ussr.robotbuildingblocks.WorldDescription;
 
 import com.jme.math.Vector3f;
 import com.jme.util.LoggingSystem;
 
 /**
  * A simple simulation with the sticky bot, using the key "Z" to globally toggle stickiness
  * of the connectors.
  * 
  * @author ups
  *
  */
 public abstract class GenericSimulation {
     
     /**
      * Last time user toggle activeness of connectors, help to avoid multiple re-activations
      */
     private static long lastConnectorToggleTime = -1;
     
     /**
      * Property indicating whether connectors should be active or not
      */
    private static boolean connectorsAreActive = false;
 
     protected abstract Robot getRobot();
     
     public void runSimulation(WorldDescription world, boolean startPaused) {
         //System.out.println("java.library.path="+System.getProperty("java.library.path"));
         PhysicsLogger.setDefaultLoggingLevel();
         final PhysicsSimulation simulation = PhysicsFactory.createSimulator();
         simulation.setRobot(getRobot());
         if(world==null) world = createWorld();
         simulation.setWorld(world);
         simulation.setPause(startPaused);
 
         // Global connector activation toggle 
         simulation.addInputHandler("Z", new PhysicsSimulation.Handler() {
             public void handle() {
                 // Avoid problem with keypress being registered twice
                 if(System.currentTimeMillis()-lastConnectorToggleTime<1000) return;
                 lastConnectorToggleTime = System.currentTimeMillis();
                 // Toggle connector activeness flag
                 setConnectorsAreActive(!getConnectorsAreActive());
                 // Tell user what is happening
                 if(getConnectorsAreActive()) System.out.println("Connectors are now active");
                 else System.out.println("Connectors are now inactive");
             }
         });
 
         // Start
         simulation.start();
     }
 
     /**
      * Create a world description for our simulation
      * @return the world description
      */
     private static WorldDescription createWorld() {
         WorldDescription world = new WorldDescription();
         world.setNumberOfModules(7);
         world.setPlaneSize(25);
         world.setObstacles(new VectorDescription[] {
                 new VectorDescription(0,-2.5f,0),
                 new VectorDescription(5,-1.5f,2)
         });
         return world;
     }
 
     /**
      * Return the state of whether connector are active or not, as controlled by user input.
      * @return true if the connectors should be active, false otherwise
      */
     public static boolean getConnectorsAreActive() {
         return connectorsAreActive;
     }
 
     /**
      * Set the tate of whether connector are active or not
      * @param connectorsAreActive set the states of whether the connectors should be active
      */
     public static void setConnectorsAreActive(boolean active) {
         connectorsAreActive = active;
     }
 
 }
