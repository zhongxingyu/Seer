 import hybridation.Coordinator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
import problem.Knapsack;
import problem.Parser;
 
 /**
  * User interface for HybridKnapsack.
  * 
  * @author omegak
  */
 public class UI {
 	
 	/** Error message for an invalid option. */
 	private static final String ERROR_INVALID_OPTION = "Error: Invalid option.";
 
 	/** Error message for a not existing directory. */
 	private static final String ERROR_NO_SUCH_DIRECTORY = "Error: No such directory.";
 	
 	/** Option that triggers the exit of the program. */
 	private static final int EXIT_OPTION = 0;
 
 	/** Option that triggers the exit of the program. */
 	private static final int INVALID_OPTION = -1;
 	
 	/** Message to give directions before the prompts asks for an option. */
 	private static final String PROMPT_PREAMBLE = " instances available. Insert instance # to solve it. 0 to exit.";
 	
 	/** Prompt line. */
 	private static final String PROMPT = "Option: ";
 	
 	/** The list of instances of the knapsack problem. */
 	private static ArrayList<File> instances;
 	
 	/** Current option. */
 	private static int option = EXIT_OPTION;
 	
 	/**
 	 * Runs the user interface for a given path that contains the instances for the knapsack problem. It will list the 
 	 * instances and give the option to launch the solutions for them. In case the path is invalid the method will show
 	 * the error and finish the execution.
 	 * 
 	 * @param path The given path that contains the instances for the knapsack problem.
 	 */
 	public static void run(String path) {
 		try {
 			loadDirectory(path);
 			
 			do {
 				// Obtains the option
 				option = prompt();
 
 				// Launches the execution
 				if (option != EXIT_OPTION) {
 					launchExecution(instances.get(option-1));
 				}
 			} while (option != EXIT_OPTION);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 		
 	}
 	
 	/**
 	 * Parses a file and executes the coordinator that solves the knapsack problem instance hybridly.
 	 * 
 	 * @param instance The instance to be solved.
 	 */
 	private static void launchExecution(File instance) {
 		try {
 			
 			// Parses file
 			System.out.println("Parsing file " + instance.getName() + "...");
 			Knapsack knapsack = Parser.parse(instance);
 			
 			// Solves problem
 			System.out.println("Solving...");
 			new Coordinator(knapsack).solve();
 			
 			// Stores results
 			System.out.println("Storing solution...");
 			// TODO store solution
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Tries to load the instances of the knapsack problem from a given path.  
 	 * 
 	 * @param path The given path.
 	 * @throws IOException When there is no such directory.
 	 */
 	private static void loadDirectory(String path) throws IOException {
 		File workspace = new File(path);
 		
 		// Checks if it is a directory
 		if (!workspace.isDirectory()) {
 			throw new IOException(ERROR_NO_SUCH_DIRECTORY);
 		}
 		
 		// Indexes the instances
 		File[] files = workspace.listFiles();
 		instances = new ArrayList<File>();
 		
 		for (int i = 0; i < files.length; i++) {
 			String filename = files[i].getName();
 			if (filename.endsWith(".txt") || filename.endsWith(".TXT")) {
 				instances.add(files[i]);
 			}
 		}
 	}
 	
 	/**
 	 * Shows the prompt awaiting for next action to perform and validates input until it's a correct one.
 	 * 
 	 * @return A valid option.
 	 */
 	private static int prompt() {
 		int input = INVALID_OPTION;
 		Scanner scanner = new Scanner(System.in);			
 		
 		do {
 			System.out.println(instances.size() + PROMPT_PREAMBLE);
 			System.out.print(PROMPT);
 			
 			try {				
 				// Reads input
 				input = scanner.nextInt();
 			} catch (InputMismatchException e) {
 				// Cleans scanner
 				scanner.nextLine();
 			}
 		} while (!validateInput(input));
 		
 		return input;
 	}
 	
 	/**
 	 * Checks if the input introduced by user is a valid option. This means being between 0 and the number of instances. 
 	 * 
 	 * @param input The option introduced by user.
 	 * @return TRUE if the option is valid, FALSE otherwise.
 	 */
 	private static boolean validateInput(int input) {
 		if ((input >= 0) && (input <= instances.size())) {
 			return true;
 		} else {
 			System.out.println(ERROR_INVALID_OPTION);
 			return false;
 		}
 	}
 }
