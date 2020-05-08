 package ussr.remote;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.rmi.RemoteException;
 
 import ussr.aGui.MainFrameSeparate;
 import ussr.aGui.MainFrames;
 import ussr.aGui.controllers.GeneralController;
 import ussr.aGui.controllers.MainFrameSeparateController;
 import ussr.aGui.tabs.constructionTabs.ConstructRobotTab;
 import ussr.aGui.tabs.controllers.ConsoleTabController;
 
 import ussr.aGui.tabs.controllers.SimulationTabController;
 import ussr.aGui.tabs.simulation.SimulationTab;
 import ussr.aGui.tabs.simulation.SimulationTreeEditors;
 import ussr.builder.simulationLoader.SimulationSpecification;
 import ussr.remote.facade.ActiveSimulation;
 import ussr.remote.facade.GUICallbackControlImpl;
 
 
 import ussr.remote.facade.RemotePhysicsSimulation;
 
 /**
  * Is responsible for connecting GUI with remote simulation.
  * @author Konstantinas
  */
 public class GUIRemoteSimulationAdapter {
 
 	public static final int SERVER_PORT = 54323;
 
 	/**
 	 * The server to run simulation on.
 	 */
 	private static SimulationLauncherServer server;
 
 	public GUIRemoteSimulationAdapter(){
 		// Start a simulation server (one that manages a number of running simulation processes)
 		try {
 			server = new SimulationLauncherServer(AbstractSimulationBatch.SERVER_PORT);
 		} catch (RemoteException e) {
 			throw new Error("Unable to create server: "+e);
 		}      
 	} 
 
 	/**
 	 * Starts simulation from specified xml file.
 	 * @param simulationXMLFile
 	 */
 	public static void runSimulation(final String simulationXMLFile) throws IOException { 
 
 		// Start a simulation server process
 		final ActiveSimulation simulation;
 		try {
 			simulation = server.launchSimulation();
 		} catch (IOException e) {
 			throw new Error("Unable to start simulation subprocess: "+e);
 		}
 
 		// Get standard output and error streams and direct them to GUI console tab, so that the buffer is not full, which will cause simulation to stop.
 		ConsoleTabController.appendStreamToConsole("StandardOut", simulation.getStandardOut());
 		ConsoleTabController.appendStreamToConsole("Error/Info/Warning", simulation.getStandardErr());
 
 
 		// FIXME USE THIS IF YOU ARE INTERESTED IN GETTING OUTPUT IN ECLIPSE CONSOLE
 		// Discard standard out (avoid buffers running full)
 		// simulation.discardStandardOut();
 		// Get standard err, pass it to method that prints it in separate thread
 		//dumpStream("err", simulation.getStandardErr());
 
 
 		// Wait for simulation process to be ready to start a new simulation
 		if(!simulation.isReady()) {
 			System.out.println("Waiting for simulation");
 			simulation.waitForReady();
 		}
 
 		/*Set new position of simulation window relative to GUI*/
 		simulation.setWindowPosition(MainFrameSeparate.simWindowX, MainFrameSeparate.simWindowY);
 
 		new Thread() {		
 			public void run() {
 				try {
 					simulation.start(simulationXMLFile);
 				} catch (RemoteException e) {
 					// Normal or abnormal termination, inspection of remote exception currently needed to determine...
 					System.err.println("Simulation stopped");
 				}
 			}
 
 		}.start();
 
 		RemotePhysicsSimulation sim = null;
 		while(sim==null) {
 			System.out.println("Simulation still null");
 			sim = simulation.getSimulation();
 			try {
 				Thread.sleep(1000);
 			} catch(InterruptedException exn) {
 				throw new Error("Unexpected interruption");
 			}
 		}
 
 		callBackGUI(simulation,sim);
 	}
 
 
 	/**
 	 * Sets remote objects of simulation in GUI and adapts it to simulation.
 	 * @param simulation, proxy for a simulation running in a different process. 
 	 * @param remotePhysicsSimulation, remote version of the standard PhysicsSimulation interface.
 	 */
 	private static void callBackGUI(ActiveSimulation simulation,RemotePhysicsSimulation remotePhysicsSimulation)throws IOException {
 		remotePhysicsSimulation.setGUICallbackControl(new GUICallbackControlImpl());
 		GeneralController.setRemotePhysicsSimulation(remotePhysicsSimulation);
 		MainFrameSeparateController.setRendererControl(remotePhysicsSimulation.getRendererControl());
 
 		GeneralController.setBuilderControl(remotePhysicsSimulation.getBuilderControl());		
 		MainFrameSeparate.setMainFrameSeparateEnabled(true);		
 
 		SimulationSpecification simulationSpecification = simulation.getXmlSimulationProvider().getSimulationSpecification();
 		SimulationTabController.setSimulationSpecification(simulationSpecification);
 		SimulationTab.addRobotNodes(simulationSpecification,true,true);
 		MainFrames.deSelectConstructRobotButton();

 		SimulationTreeEditors.updateValuesEditors();
		SimulationTab.resizeComponents();
 		SimulationTab.setTabVisible(true); 
 		ConstructRobotTab.setTabEnabled(true);
 	}
 
 	/**
 	 * Dump an input stream to standard out, prefixing all lines with a fixed text 
 	 * @param prefix the prefix to use
 	 * @param stream the stream to dump
 	 *  FIXME IS USED WHEN OUTPUTS ARE DIRECTED TO ECLIPSE CONSOLE.
 	 */
 	private static void dumpStream(final String prefix, final InputStream stream) {
 		new Thread() {
 			public void run() {
 				BufferedReader input = new BufferedReader(new InputStreamReader(stream));
 				while(true) {
 					String line;
 					try {
 						line = input.readLine();
 						if(line==null) break;
 						System.out.println(prefix+": "+line);
 					} catch (IOException e) {
 						throw new Error("Unable to dump stream: "+e); 
 					}
 				}
 			}
 		}.start();
 	}    
 }
 
