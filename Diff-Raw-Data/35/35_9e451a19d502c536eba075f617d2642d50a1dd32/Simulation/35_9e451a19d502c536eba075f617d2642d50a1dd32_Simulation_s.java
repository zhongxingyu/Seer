 package tgm.sew.hit.roboterfabrik;
 
 import java.io.File;
 import java.io.IOException;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
 
 /**
  * Die Main Klasse der Roboterfabrik
  * 
  * @author Dominik
 * @version 0.5
  */
 public class Simulation {
 
     private static final Logger logger = Logger.getLogger("Fabriklog");
     
     /**
      * Wird beim starten aufgerufen
      * @param args die Command-line Argumente
      */
     public static void main(String[] args) {
         new Simulation(args);
     }
     
 
     /**
      * Zum erstellen des SimulationObjektes
      * @param args 
      */
     public Simulation(String[] args) {
         CommandLineParser clp = new CommandLineParser(30,30,5*60*1000);
         if(clp.parse(args)) {
             try {
                FileAppender fa = new FileAppender(new SimpleLayout(), clp.logFolder().getPath() + File.separator + "fabrik.log",false);
                 BasicConfigurator.configure(fa);
                 logger.log(Level.INFO, "booting Simulation");
                 logger.log(Level.INFO, "STARTING THE SIMULATION");
                 new Fabrik(clp.getMonteure(),clp.getLieferanten(),clp.getLaufzeit(),clp.lagerFolder());
             } catch (IOException ex) {
                 logger.log(Level.ERROR, "Fehler beim erstellen des Logs");
             }
         }
     }
     
 }
