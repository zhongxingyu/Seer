 package driver;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 
 import cobweb.LocalUIInterface;
 import cobweb.LocalUIInterface.TickEventListener;
 import cobweb.UIInterface;
 import cobweb.UIInterface.UIClient;
 import driver.config.GUI;
 
 /**
  * This class contains the main method to drive the application.  
  * 
  * @author Cobweb Team (Might want to specify)
  *
  */
 public class CobwebApplicationRunner {
 
 	/**
 	 * The NullDisplayApplication class is used when the user uses the -hide flag.  
 	 * Otherwise, the CobwebApplication class will be used.
 	 * 
 	 * @see CobwebApplication
 	 */
 	private static final class NullDisplayApplication implements UIClient {
 
 		@Override
 		public void refresh(boolean wait) {
 			return;
 		}
 
 		@Override
 		public boolean isReadyToRefresh() {
 			return true;
 		}
 
 		@Override
 		public void setCurrentFile(String input) {
 			return;
 		}
 
 		@Override
 		public void fileOpened(SimulationConfig conf) {
 			return;
 		}
 
 		@Override
 		public void setSimulation(UIInterface simulation) {
 			return;
 		}
 	}
 
 	static UIInterface simulation;
 
 	static boolean visible = true;
 
 
 	/**
 	 * The main function is found here for the application version of cobweb.  
 	 * It initializes the simulation and settings using a settings file optionally 
 	 * defined by the user.
 	 * 
 	 * <p>Switches: 
 	 * 
 	 * <p><p> --help 
 	 * <br>Prints the various flags that can be used to run the program:
 	 * Syntax = "cobweb2 [--help] [-hide] [-autorun finalstep] [-log LogFile.tsv] 
 	 * [[-open] SettingsFile.xml]"
 	 * 
 	 * <p> -hide 
 	 * <br>When the hide flag is used, the user interface does not initialize 
 	 * (visible is set to false).  If visible is set to false, the User Interface 
 	 * Client will be set to a NullDisplayApplication rather than a 
 	 * CobwebApplication.  Need to specify an input file to use this switch.
 	 * 
 	 * <p> -open [must specify] 
 	 * <br>If not used, the default is 
 	 * CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME  + 
 	 * CobwebApplication.CONFIG_FILE_EXTENSION otherwise will be set to 
 	 * whatever the user specifies.  The input file contains the initial conditions 
 	 * of the simulation (AgentTypeCount, FoodTypeCount, etc.)
 	 * 
 	 * <p> -log [must specify] 
 	 * <br>Specify the name of the log file.
 	 * 
 	 * <p> -autorun [specify integer >= -1]
 	 * 
 	 * @param args
 	 * @see CobwebApplication#INITIAL_OR_NEW_INPUT_FILE_NAME
 	 * @see CobwebApplication#CONFIG_FILE_EXTENSION
 	 */
 
 	public static void main(String[] args) {
 
 		// Process Arguments`
 
 		String inputFileName = "";
 		String logFileName = "";
 		boolean autostart = false;
 		int finalstep = 0;
 
 		if (args.length > 0) {
 			for (int arg_pos = 0; arg_pos < args.length; ++arg_pos){
 				if (args[arg_pos].equalsIgnoreCase("--help")){
 					System.out.println("Syntax: " + CobwebApplicationRunner.Syntax);
 					System.exit(0);
 				} else if (args[arg_pos].equalsIgnoreCase("-autorun")){
 					autostart = true;
 					try{
 						finalstep = Integer.parseInt(args[++arg_pos]);
 					} catch (NumberFormatException numexception){
 						System.out.println("-autorun argument must be integer");
 						System.exit(1);
 					}
 					if (finalstep < -1) { 
 						System.out.println("-autorun argument must >= -1");
 						System.exit(1);
 					}
 				} else if (args[arg_pos].equalsIgnoreCase("-hide")){
 					visible=false;
 				} else if (args[arg_pos].equalsIgnoreCase("-open")){
 					if (args.length - arg_pos == 1) {
 						System.out.println("No value attached to '-open' argument,\n" +
 								"Correct Syntax is: " + CobwebApplicationRunner.Syntax);
 						System.exit(1);
 					} else {
 						inputFileName = args[++arg_pos];
 					}
 				} else if (args[arg_pos].equalsIgnoreCase("-log")){
 					if (args.length - arg_pos == 1) {
 						System.out.println("No value attached to '-log' argument,\n" +
 								"Correct Syntax is: " + CobwebApplicationRunner.Syntax);
 						System.exit(1);
 					} else {
 						logFileName = args[++arg_pos];
 					}
 				} else {
 					inputFileName = args[arg_pos];
 				}
 			}
 		}
 
 		if (!inputFileName.equals("") && ! new File(inputFileName).exists()){
 			System.out.println("Invalid settings file value: '" + inputFileName + "' does not exist" );
 			System.exit(1);
 		}
 		if (!logFileName.equals("") && new File(logFileName).exists()){
 			System.out.println("WARNING: log '" + logFileName + "' already exists, overwriting it!" );
 		}
 
 		//Create CobwebApplication and threads; this is not done earlier so 
 		// that argument errors will result in quick exits.
 
 		boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf(
 		"-agentlib:jdwp") > 0;
 
 		if (!isDebug) {
 			MyUncaughtExceptionHandler handler = new MyUncaughtExceptionHandler();
 			Thread.setDefaultUncaughtExceptionHandler(handler);
 		}
 
 		UIClient CA = null;
 
 		if (visible) {
 			CA = new CobwebApplication();
 		} else {
 			CA = new NullDisplayApplication();
 		}
 
 		simulation = new LocalUIInterface(CA);
 		CA.setSimulation(simulation);
 
 		//Set up inputFile
 
 		if (inputFileName.equals("")) {
 			if (!visible) {
 				System.err.println("Please specify an input file name when running with the -hide option");
 				return;
 			}
 
 			String tempdir = System.getProperty("java.io.tmpdir");
 			String sep = System.getProperty("file.separator");
 			if (!tempdir.endsWith(sep))
 				tempdir = tempdir + sep;
 
 			inputFileName = CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME + CobwebApplication.CONFIG_FILE_EXTENSION;
 			File testFile = new File(inputFileName);
 			if (! (testFile.exists() && testFile.canWrite()))
 				inputFileName = tempdir + CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME + CobwebApplication.CONFIG_FILE_EXTENSION;
 
 		}
 		CA.setCurrentFile(inputFileName); // $$$$$$ added on Mar 14
 
 		SimulationConfig defaultconf = null;
 		try {
 			defaultconf = new SimulationConfig(inputFileName);
 		} catch (FileNotFoundException ex) {
 			defaultconf = new SimulationConfig();
 		} catch (Exception e) {
 			error("Cannot load " + inputFileName + "");
 		}
 
 		simulation.load(defaultconf);
 
 
 		// $$$$$$ Added to check if the new data file is hidden. Feb 22
 		File inf = new File(inputFileName);
 		if (inf.isHidden() || (inf.exists() && !inf.canWrite())) {
 			JOptionPane.showMessageDialog(GUI.frame, "Caution:  The initial data file \"" + inputFileName
 					+ "\" is NOT allowed to be modified.\n"
 					+ "\n                  Any modification of this data file will be neither implemented nor saved.");
 		}
 
 		if (logFileName != ""){
 			try {
 				simulation.log(logFileName);
 			} catch (IOException ex) {
 				System.err.println("couldn't open log file!");
 			}
 		}
 
 		if (autostart) {
 			System.out.println(String.format("Running '%1$s' for %2$d steps with log %3$s..."
 					, inputFileName, finalstep, logFileName));
 			simulation.slowDown(0);
 			simulation.resume();
 
 			final class AutoStopTickListener implements TickEventListener {
 				private long stopTick;
 
 				private long increment;
 
 				public AutoStopTickListener(long stopTick) {
 					this.stopTick = stopTick;
 
 					increment = stopTick / 10;
 					if (increment > 2000)
 						increment = 2000;
 				}
 
 				@Override
 				public void TickPerformed(long currentTick) {
 					if (currentTick % increment == 0) {
 						System.out.print(100 * currentTick / stopTick + "% ");
 
 					}
 					if (currentTick > stopTick) {
 						simulation.pause();
 
 						simulation.killScheduler();
 						System.out.println("Done!");
 						System.exit(0);
 					}
 				}
 			}
 
 			simulation.AddTickEventListener(new AutoStopTickListener(finalstep));
 		}
 	}
 
 	private static void error(String string) {
 		if (visible) {
 			throw new CobwebUserException(string);
 		} else {
 			System.err.println(string);
 		}
 	}
 
 	public static final String Syntax = "cobweb2 [--help] [-hide] [-autorun finalstep] [-log LogFile.tsv] [[-open] SettingsFile.xml]";
 
 
 
 }
