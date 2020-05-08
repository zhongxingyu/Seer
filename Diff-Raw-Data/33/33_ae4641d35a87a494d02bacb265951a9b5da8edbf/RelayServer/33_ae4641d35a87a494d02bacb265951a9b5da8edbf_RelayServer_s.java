package relay;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Properties;
 
 /**
  * Listen on a port and forward to another
  * 
  * 
  */
 public class RelayServer {
 	private static int listenPort;
 	private static String forwardHost;
 	private static int forwardPort;
 	private static boolean debugSetting;
 
 	public RelayServer() {
 
 		System.out.println("Loading Property File...");
 
 		// Load application properties into memory
 		Properties properties = new Properties();
 		String propertyFileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".properties";
 		try {
 			properties.load(new FileInputStream(propertyFileName));
 		} catch (IOException e) {
 			System.out.println("Could not find property file: " + propertyFileName);
 			System.exit(1);
 		}
 		System.out.println("...Loading Complete");
 
 		if (properties.get("DEBUG") != null && properties.get("DEBUG").toString().equalsIgnoreCase("TRUE")) {
 			debugSetting = true;
 		} else {
 			debugSetting = false;
 		}
 
 		if (properties.get("LISTEN_PORT") != null) {
 			// Check if Listen port is a valid number
 			try {
 				listenPort = Integer.parseInt(properties.get("LISTEN_PORT").toString());
 			} catch (Exception e) {
 				System.out.println("Invalid Listening Port");
 				System.exit(1);
 			}
 		} else {
 			System.out.println("No LISTEN_PORT set in config file");
 			System.exit(1);
 		}
 
 		if (properties.get("FORWARD_PORT") != null) {
 			// Check if Forward port is a valid number
 			try {
 				forwardPort = Integer.parseInt(properties.get("FORWARD_PORT").toString());
 			} catch (Exception e) {
 				System.out.println("Invalid Forward Port");
 				System.exit(1);
 			}// try-catch
 		} else {
 			System.out.println("No FORWARD_PORT set in config file");
 			System.exit(1);
 		}
 
 		if (properties.get("FORWARD_HOST") != null) {
 			forwardHost = properties.get("FORWARD_HOST").toString();
 		} else {
 			System.out.println("No FORWARD_HOST set in config file");
 			System.exit(1);
 		}
 
 	}
 
 	public void startServer() {
 
 		try {
 			ServerSocket serversocket = null;
 
 			int port = listenPort;
 			System.out.println("port = " + port);
 			ServerSocket srv = new ServerSocket(port);
 			System.out.println("-- Started Relay Server --");
 			System.out.println("Listening on port : " + port);
 			System.out.println("Fowarding to host : " + forwardHost + " on port " + forwardPort);
 			while (true) {
 				// Wait for connection from client.
 				Socket socket = srv.accept();
 
 				RelayWorkerThread rwt = new RelayWorkerThread(socket);
 				rwt.start();
 			}
 
 		}
 
 		catch (Exception e)
 
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 	}
 
 	public static boolean isDebugSetting() {
 		return debugSetting;
 	}
 
 	public static String getForwardHost() {
 		return forwardHost;
 	}
 
 	public static int getForwardPort() {
 		return forwardPort;
 	}
 
 	public static int getListenPort() {
 		return listenPort;
 	}
 
 	public static void main(String args[]) {
 
 		new RelayServer().startServer();
 
 	}
 
 }
