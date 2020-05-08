 package bt.Utils;
 
 /**
  * It will statically handle commands and parse them while the client runs.
  * @author Ike, Robert and Fernando
  *
  */
 
 public class CommandParser {
 	
 	/** 
 	 * Main entry point for parsing commands.
 	 * @param String command
 	 */
 	public static void execute(String command) {
 		switch(command) {
 		case "quit":
 			Utilities.callClose();
 		case "help":
 			CommandParser.printHelp();
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid command, input help for commands.");
 		}
 	}
 	
 	/**
 	 * Prints a list of available commands. It will expand as we advance.
 	 */
 	public static void printHelp() {
 		System.out.println("\nHELP - Available Commands (so far):");
 		System.out.println("quit\t - terminates the client.");
 		System.out.println("--------------------------------\n");
 	}
 	
 }
