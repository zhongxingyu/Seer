 package nl.tue.fingerpaint.server.simulator;
 
 import nl.tue.fingerpaint.client.simulator.SimulatorService;
 
 /**
  * Class that is able to communicate with the native simulation-service code.
  * Use this class only indirectly through {@link SimulatorService}
  * 
 * @author Lasse Blaauwbroek
 *
  */
 class NativeCommunicator {
 	
 	private static NativeCommunicator instance = new NativeCommunicator();
 	
 	static {
 		System.loadLibrary("NativeCommunicator");  
     }
 	
 	private NativeCommunicator() {
 		
 	}
 	
 	public static NativeCommunicator getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Communicates with the native simulation-service code to simulate a
 	 * step on a given concentration vector. The given vector is modified
 	 * with the result.
 	 * 
 	 * @param geometry The geometry used for the simulation
 	 * @param mixer The mixer used for the simulation
 	 * @param concentrationVector
 	 * 				The concentration vector that used for the simulation;
 	 * 				note that results are returned in this parameter
 	 * @param stepSize The size of the step that is performed
 	 * @param stepName The name of the step to simulate
 	 * @return The segregation of the simulation
 	 */
 	public native synchronized double simulate(String geometry, 
 								String mixer, 
 								double[] concentrationVector, 
 								double stepSize, 
 								String stepName);
 }
