 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.physics;
 
 import java.util.List;
 
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.WireframeState;
 
 import ussr.description.Robot;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.WorldDescription;
 import ussr.model.Module;
 import ussr.physics.jme.pickers.Picker;
 
 /**
  * A physics simulation, defined independently of the underlying physics engine.  Instances
  * of this interface are created using the PhysicsFactory class.  The methods allow 
  * simulation parameters to be defined, and to start the simulation running.
  * 
  * @author ups
  *
  */
 public interface PhysicsSimulation {
 
     /**
      * Define a user input handler for the running simulation, allowing the user to
      * interact with the simulation in a dedicated way.  Note that the underlying
      * simulation engine may already define certain keys to certain functions, which
      * may or may not be overridden by this method (the behavior is undefined).
      * @param keyName the name of the key (or key sequence) for which to create an action
      * @param handler an object encapsulating the user-defined action
      */
     public void addInputHandler(String keyName, Handler handler);
 
     /**
      * Define the default robot to be used in this simulation, including visual appearance,
      * physical characteristics, and controller functionality.
      * @param bot the one description of the default robot used in this simulation
      */
     public void setRobot(Robot bot);
 
     /**
      * Define the robots to be used in this simulation, including visual appearance,
      * physical characteristics, and controller functionality.
      * @param bot the description of one or more robots used in this simulation
      */
     public void setRobot(Robot bot, String type);
     
     /**
      * Define the world in which the robots are simulation, including starting configuration
      * of the robot and physical obstacles.
      * @param world the descriptoin of the world used for the simulation
      */
     public void setWorld(WorldDescription world);
 
     /**
      * An ultra-simple user-input handler
      * @author Modular Robots @ MMMI
      */
     public static interface Handler {
         /**
          * Handle an input from the user
          */
         public void handle();
     }
     
     /**
      * Start the simulation
      */
     public void start();
     /**
      * Stop the simulation
      *
      */
     public void stop();
 
     /**
      * Get a list containing all the modules currently present in the simulation
      * @return the modules in the simulation
      */
     public List<Module> getModules();
     
     /**
      * Test whether the simulation is currently paused
      * @return true is the simulation is paused, false otherwise
      */
     public boolean isPaused();
     
     /**
      * Test whether the simulation has been stopped
      * @return true if the simulation has been stopped, false otherwise
      */
     public boolean isStopped();
 
     /**
      * Get the current simulation time
      * @return the simulation time
      */
 	public float getTime();
 
 	/**
 	 * Directly set the gravity being used in the simulation.  The preferred way of setting
 	 * the gravity for a simulation is to use {@link PhysicsParameters#setGravity(float)}
 	 * @param g the gravity
 	 * @see PhysicsParameters#setGravity(float)
 	 */
 	public void setGravity(float g);
 
 	/**
 	 * Wait for the physics step to complete by blocking the thread.  If notification is
 	 * requested, all other threads waiting on the simulation object are notified (awakened)
 	 * @param notify whether to notify other threads or not
 	 */
 	public void waitForPhysicsStep(boolean notify);
 	public void waitForPhysicsStep(Module m);
 
 	/**
 	 * Subscribe to the simulation steps, meaning that the observer is notified each time a
 	 * physics step has been simulated (e.g., at the end of the physics step)
 	 * @param observer the observer to register for future events
 	 * @see PhysicsObserver#physicsTimeStepHook(PhysicsSimulation)
 	 */
 	public void subscribePhysicsTimestep(PhysicsObserver observer);
 	
 	public void unsubscribePhysicsTimestep(PhysicsObserver observer);
 
 	/**
 	 * Set whether the simulation is paused.
 	 * @param paused, true if the simulation should be paused, false otherwise
 	 */
 	public void setPause(boolean paused);
 
 	/**
 	 * Set whether the simulation is running in real time.
 	 * @param realtime, true if the simulation should running in real time.
 	 */
 	public void setRealtime(boolean realtime);
 	
 	/**
 	 * Set whether the simulation is single step.
 	 * @param singleStep,the state of simulation step.
 	 */
 	public void setSingleStep(boolean singleStep);	
 	
 	/**
 	 * Obtain a reference to the simulation helper, which contains various utility
 	 * methods that work on the simulation
 	 * @return a reference to the simulation helper object for this simulation
 	 * @see PhysicsSimulationHelper
 	 */
     public PhysicsSimulationHelper getHelper();	
 
     /**
      * Add a simulation gadget to this simulation (a plugin that modifies the user
      * interface to cater to a specific simulation).
      * @param gadget The gadget to add
      */
     public void addGadget(SimulationGadget gadget);
     
     /**
      * Get the positions of all obstacles
      */
     public List<VectorDescription> getObstaclePositions();
    

 
     public void waitForPhysicsTimestep(TimedPhysicsObserver observer);
 }
