 package dk.itu.kf04.g4tw;
 
 import dk.itu.kf04.g4tw.controller.WebServer;
 import dk.itu.kf04.g4tw.model.MapModel;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Main entry-point for the application.
  */
 public class Main {
 
     // General TODOs for the project
     // TODO: Clean up in the javascript code
     // TODO: Make som white- and blackbox testing
     // TODO: Jens: Skriv RequestParser test
     // TODO: Jens: Test for helvede, test!
 
     /**
      * The logging-util for the Main class.
      */
     protected static Logger Log = Logger.getLogger(Main.class.getName());
     
     /**
      * Starts the application by initializing data and then starting server.
      * @param args  The arguments to feed the application.
      */
     public static void main(String[] args) {
         // Initialize port
         int port;
 
         // Set properties
         System.setProperty("file.encoding", "ISO8859_1");
         Log.setLevel(Level.ALL);
 
         // Examine input
         if (args.length > 0) {
             if (args[0].equals("help")) {
                 System.out.print(helpText());
                 return;
             } else if (args[0].equals("version")) {
                 System.out.print("G4TW Map Server Application\nVersion 1.0");
                 return;
             } else {
                 // Start the server on the given port or, if none, 80
                 try { port = Integer.parseInt(args[0]); }
                 catch (ArrayIndexOutOfBoundsException e) {
                     Log.info("No port was given. Starting server on port 80.");
                     port = 80;
                 }
                 catch (NumberFormatException e) {
                     Log.severe("Failed to parse number to port, starting server on port 80: " + e);
                     port = 80;
                 }
             }
         } else {
             port = 80;
         }
 
         // Log program start
         System.out.println("***********************************");
         System.out.println("*** G4TW Map Server Application ***");
         System.out.println("***   Compiled 22nd May 2012    ***");
         System.out.println("***        Version 1.0          ***");
         System.out.println("***********************************");
         
         // Try to initialize the program 
         try {
             // Import data
             MapModel model = DataStore.loadRoads();
     
             // Create the server instance.
             new WebServer(model, port);
         } catch (FileNotFoundException e) {
             Log.severe("Critical error: Unable to find data-file: " + e.getMessage());
         } catch (IOException e) {
             Log.severe("Critical error: IOException while loading data: " + e.getMessage());
         } catch (ClassNotFoundException e) {
             Log.severe("Critical error: Could not load binary file. Please delete the current version and run the program again.");
         }
     }
     
     protected static String helpText() {
         return "G4TW Map Server Application, version 1.0\n" +
                 "Usage: java -jar Main.jar [ PORT | OPTION ]\n" +
                 "A Map Server Application which loads data from kdv-data files and serves them through a HTML5-interface at the given port, defaulting to 80.\n\n" +
                 "Port: A number between 1 and 65536 on which port the server listens.\n" +
                 "Options:\n" +
                 "\thelp\tDisplays this help text.\n" +
                 "\tversion\tDisplays the version of the software.";
     }
 
 }
