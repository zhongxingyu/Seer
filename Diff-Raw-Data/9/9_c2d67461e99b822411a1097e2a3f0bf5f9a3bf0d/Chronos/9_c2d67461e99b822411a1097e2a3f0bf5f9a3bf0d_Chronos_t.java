 package chronos;
 
 import client.ClientController;
 import server.ServerController;
 
 /**
  * Main class. Starts either a server or a client, depending on args input. ALL
  * HAIL THE CHRONOS.
  * 
  * @author anon
  * 
  */
 public class Chronos {
 	public static void main(String[] args) {
 		new Chronos().run(args);
 
 	}
 
 	private void run(String[] args) {
 		if (args.length == 1)
 			if (args[0].equals("-s"))
 				startServer();
 			else
 				startClient();
 
 		Singleton.getInstance().enableLog();
//		Singleton.getInstance().enableLogin();
 		// Singleton.getInstance().enableNetwork();
 		startClient();
 		// startServer();
 
 	}
 
 	private void startServer() {
 		ServerController serverController = new ServerController();
 		serverController.run();
 	}
 
 	private void startClient() {
 		ClientController clientController = new ClientController();
 		clientController.run();
 	}
 }
