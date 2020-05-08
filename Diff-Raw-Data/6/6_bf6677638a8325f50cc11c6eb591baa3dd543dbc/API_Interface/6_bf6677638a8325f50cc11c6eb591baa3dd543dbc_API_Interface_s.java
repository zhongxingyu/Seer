 package api;
 
import java.awt.Container;

import casa.Status;
import casa.TransientAgent;
import casa.ui.TransientAgentInternalFrame;

 public interface API_Interface {
 	
 	/**
 	 * Starts the CASA process
 	 * 	Should not load any code. Use loadToxxx or translateLoadToxxx instead. (Where xxx is Robot or Simulator) 
 	 * @return Status of the casa process
 	 */
 	public boolean initalize();
 	
 	/**
 	 * Checks a username / password combination against a database or external system.
 	 * @param user_name login name
 	 * @param password login password (preferably the password has already been encrypted)
 	 * @return true is the user is valid, false otherwise.
 	 */
 	public boolean authenticate_user(String user_name, String password);
 	
 	/**
 	 * Loads the code found at 'filepath' to the robot.
 	 * @param filepath
 	 */
 	public String loadToRobot(String filepath);
 	
 	/**
 	 * Loads the code found at 'filepath' to the simulator.
 	 * @param filepath
 	 */
 	public String loadToSimulator(String filepath);
 	
 	/**
 	 * Translates the code found at 'filepath', then loads it into the robot.
 	 * @param filepath
 	 * @return String containing any error messages related to the translation. Null if translation was successful.
 	 */
 	public String translateLoadToRobot(String filepath);
 	
 	/**
 	 * Translates the code found at 'filepath', then loads it into the simulator.
 	 * @param filepath
 	 * @return String containing any error messages related to the translation. Null if translation was successful.
 	 */
 	public String translateLoadToSimulator(String filepath);
 
 }
