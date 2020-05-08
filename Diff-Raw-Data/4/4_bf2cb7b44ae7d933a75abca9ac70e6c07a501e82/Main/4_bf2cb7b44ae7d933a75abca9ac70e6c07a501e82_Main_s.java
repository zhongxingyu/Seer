 package dk.itu.kf04.g4tw;
 
 import dk.itu.kf04.g4tw.controller.MapController;
 import dk.itu.kf04.g4tw.controller.XMLDocumentParser;
 import dk.itu.kf04.g4tw.model.MapModel;
 import dk.itu.kf04.g4tw.model.MapParser;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Main entry-point for the application.
  */
 public class Main {
 
     /**
      * The logging-util for the Main class.
      */
     protected static Logger Log = Logger.getLogger(Main.class.getName());
     
     /**
      * Starts the application by initializing data and then starting server.
      * @param args  The arguments to feed the application.
      */
     public static void main(String[] args) {
         new XMLDocumentParser();
 
 		Log.setLevel(Level.ALL);
         
         // Log program start
         Log.info("Main starting up.");
 
         // Find the files
        File nodeFile = new File("krak/kdv_node_unload.txt");
        File edgeFile = new File("krak/kdv_unload.txt");
 
         Log.fine("Starting import of map-data.");
         
         // Import data
         MapModel model = MapParser.load(nodeFile, edgeFile);
         
         Log.info("Import of map-data done. Starting server.");
 
         // Start the controller
         new MapController(model);
     }
 
 }
