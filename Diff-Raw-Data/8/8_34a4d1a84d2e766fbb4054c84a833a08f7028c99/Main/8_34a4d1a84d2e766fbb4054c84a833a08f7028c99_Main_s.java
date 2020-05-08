 /*
  * Application entry point
  */
 package serialserver;
 
 import serialserver.exceptions.MissingPort;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
import serialserver.impl.SerialFactory;
 import serialserver.interfaces.BaseSerialPort;
 
 /**
  * Application entry class. Parse command line arguments and init server.
  * 
  */
 public class Main {
 
     private static Logger log = Logger.getLogger(Main.class.toString());
 
     public static void main(String[] args) {
         try {
             log.info("Parsing command line arguments.");
 
             Config config = parseCommandLine(args);
             log.info(config.toString());
 
             startServer(config);
 
         } catch(MissingPort ex) {
             log.severe(ex.getMessage());
             listAvailablePorts();
         } catch(IllegalArgumentException ex) {
             log.severe(ex.getMessage());
         }
     }
 
     private static Config parseCommandLine(String[] args) {
         OptionParser parser = new OptionParser() {
             {
                 accepts("serial-port", "serial port").withRequiredArg();
                 accepts("serial-speed", "serial port speed").withRequiredArg().ofType(Integer.class);
                 accepts("listen-port", "server listen port").withRequiredArg().ofType(Integer.class);
                 accepts("debug", "display debug info");
                 acceptsAll(asList("h", "help", "?"), "show help");
             }
         };
 
         OptionSet options = parser.parse(args);
         if(options.has("?")) {
             try {
                 parser.printHelpOn(System.out);
             } catch (IOException ex) {
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Config config = new Config();
         if(options.hasArgument("serial-port")) {
             config.setSerialPort(String.valueOf(options.valueOf("serial-port")));
         } else {
             throw new MissingPort("Expecting serial port as a command line argument.");
         }
         if(options.hasArgument("serial-speed")) {
             config.setSerialSpeed(String.valueOf(options.valueOf("serial-speed")));
         }
         if(options.hasArgument("listen-port")) {
             config.setServerPort(String.valueOf(options.valueOf("listen-port")));
         }
         if(options.has("debug")) {
             config.setDebug(true);
         }
 
         return config;
     }
 
     private static void startServer(Config config) {
         log.info("Starting server and connecting to port.");
 
     }
 
     private static void listAvailablePorts() {
         log.info("Listing all available serial ports");
         BaseSerialPort serial = SerialFactory.createInstance(null);
         if(serial != null) {
             List<String[]> ports = serial.getPortsList();
             for(String[] pair : ports) {
                 System.out.println(pair[0] + " " + pair[1]);
             }
         }
     }
 
     private static List<String> asList(String ... args) {
         List<String> result = new LinkedList<String>();
         for(String arg : args) {
             result.add(arg);
         }
         return result;
     }
 }
