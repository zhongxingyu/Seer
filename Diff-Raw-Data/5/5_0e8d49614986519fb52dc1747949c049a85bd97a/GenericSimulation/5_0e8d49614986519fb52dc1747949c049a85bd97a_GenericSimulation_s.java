 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.samples;
 
 import ussr.builder.helpers.ControllerFactory;
 import ussr.builder.helpers.ControllerFactoryImpl;
 import ussr.description.Robot;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.WorldDescription;
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsLogger;
 import ussr.physics.PhysicsSimulation;
 
 /**
  * An abstract simulation implementation that sets up the necessary objects
  * before starting the simulation.  Hook methods are provided for customization.
  * 
  * 
  * @author Modular Robots @ MMMI
  *
  */
 public abstract class GenericSimulation {
     
     /**
      * Last time user toggle activeness of connectors, help to avoid multiple re-activations
      */
     private static long lastConnectorToggleTime = -1;
         
     protected abstract Robot getRobot();
     protected static PhysicsSimulation simulation;
 
     /**
      * Adapt description of simulation world, hook method that subclasses can override
      * @param world the world description to adapt
      */
     protected void adaptWorldToSimulationHook(WorldDescription world) { ; }
     
     public static PhysicsSimulation getPhysicsSimulation() {
     	return simulation;
     }
     public void runSimulation(WorldDescription world, boolean startPaused) {
         //System.out.println("java.library.path="+System.getProperty("java.library.path"));
         PhysicsLogger.setDefaultLoggingLevel();
         simulation = PhysicsFactory.createSimulator();
         simulation.setRobot(getRobot());
         this.simulationHook(simulation);
         if(world==null) world = createWorld();
         adaptWorldToSimulationHook(world);
         simulation.setWorld(world);
         simulation.setPause(startPaused);
 
         // Start
         simulation.start();
     }
 
     protected void simulationHook(PhysicsSimulation simulation) {
     	
     }
     
     public WorldDescription createGenericSimulationWorld(ControllerFactory controllerFactory) {
         PhysicsLogger.setDefaultLoggingLevel();
         /* Create the simulation*/
        PhysicsSimulation simulation = PhysicsFactory.createSimulator();
         
         /* Assign controller to selection of robots */
         DefaultSimulationSetup.addDefaultRobotSelection(simulation, controllerFactory);
         
         /*Create the world description of simulation and set it to simulation*/
         WorldDescription world = DefaultSimulationSetup.createWorld();
         simulation.setWorld(world);
         return world;
     }
     
     /**
      * Create a world description for our simulation
      * @return the world description
      */
     private static WorldDescription createWorld() {
         WorldDescription world = new WorldDescription();
         world.setPlaneSize(250);
         world.setObstacles(new VectorDescription[] {
                 new VectorDescription(0,-2.5f,0),
                 new VectorDescription(5,-1.5f,2)
         });
         return world;
     }
 
    protected void start(boolean startPaused) {
         /* Should simulation be in paused state (static)in the beginning*/
         simulation.setPause(startPaused);
         /* Start simulation */
         simulation.start();
     }
 }
