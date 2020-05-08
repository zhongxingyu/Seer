 package it.polimi.elet.reds;
 
 import java.net.BindException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Main class to start a reds broker
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class RedsBrokerMain {
 
 	private static final Integer DEFAULT_PORT = 42000;
 
 	public static void main(String[] args) {
 
 		if (args == null || args.length < 1) {
			System.err.println("Usage\n\tjava it.polimi.elet.reds.RedsBroker brokerPort [otherBrokerAddres]");
 			System.exit(0);
 		}
 
 		int port;
 
 		try {
 			port = new Integer(args[0]).intValue();
 		} catch (NumberFormatException e) {
 			port = DEFAULT_PORT;
 		}
 
 		RedsBroker redsBroker = null;
 		if (args.length > 1) {
 			redsBroker = new RedsBroker(port, args[1]);
 		} else {
 			redsBroker = new RedsBroker(port);
 		}
 
 		try {
 			redsBroker.start();
 		} catch (BindException e) {
			Logger.getLogger("polimi.reds").log(Level.ALL, "Error while starting REDS middleware. There is probably another REDS instance already active");
 			System.exit(0);
 		}
 
 	}
 
 }
