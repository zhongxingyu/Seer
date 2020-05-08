 package ch.hsr.objectCaching.testFrameworkClient;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import ch.hsr.objectCaching.interfaces.ClientInterface;
 import ch.hsr.objectCaching.interfaces.ClientSystemUnderTest;
 import ch.hsr.objectCaching.interfaces.ServerInterface;
 import ch.hsr.objectCaching.scenario.Scenario;
 import ch.hsr.objectCaching.util.Configuration;
 
 public class ClientController implements ClientInterface {
 
 	private static Logger logger = Logger.getLogger(ClientController.class.getName());
 	private static final int DEFAULT_CLIENT_PORT = 1099;
 	private int ClientRmiPort;
 	private String ClientRegistryName;
 	private String[] args;
 	private ServerInterface server;
 	private TestClient testClient;
 	private Configuration config;
 
 
 	public ClientController(String[] args) {
 		this.args = args;
 	}
 
 	@Override
 	public void initialize(Scenario scenario, Configuration configuration) throws RemoteException {
 		config = configuration;
 		server = getServerStub(config.getServerIP(), config.getServerRMIPort(), config.getServerRegistryName());
 
 		ClientSystemUnderTest clientSystemUnderTest = createClientSystemUnderTest(config.getNameOfSystemUnderTest());
 		clientSystemUnderTest.setServerSocketAdress(new InetSocketAddress(config.getServerIP(), config.getServerSocketPort()));
 
 		testClient = new TestClient(clientSystemUnderTest);
 		testClient.setScenario(scenario);
 		testClient.init();
 
 		notifyServerInitDone();
 	}
 
 	@Override
 	public void startTest() throws RemoteException {
 		testClient.runScenario();
 		sendResultToServer(testClient.getScenario());
 	}
 
 	@Override
 	public void shutdown() throws RemoteException {
 		testClient.shutdown();
 		shutdownClientController();
 	}
 
 	private ServerInterface getServerStub(String serverIP, int port, String registryName) throws RemoteException {
 		try {
 			String url = "rmi://" + serverIP + ":" + port + "/" + registryName;
 			return (ServerInterface) Naming.lookup(url);
 		} catch (Exception e) {
 			logger.severe("loading server stub failed " + e.getMessage());
 			throw new RemoteException("ServerStub could not be loaded on client");
 		}
 	}
 
 	private ClientSystemUnderTest createClientSystemUnderTest(String systemUnderTestName) {
 		try {
 			return CUTFactory.generateCUT(systemUnderTestName);
 		} catch (Exception e) {
 			logger.severe("Generating ClientSystemUnderTest instance failed: " + e.getMessage());
 		}
 		return null;
 	}
 
 	private void sendResultToServer(Scenario scenario) throws RemoteException {
 		try {
 			server.setResults(scenario, InetAddress.getLocalHost().getHostAddress());
 		} catch (UnknownHostException e) {
 			logger.severe("Reading the local host address failed");
 		}
 	}
 
 	private void notifyServerInitDone() {
 		try {
 			InetAddress addr = InetAddress.getLocalHost();
 			server.setReady(addr.getHostAddress());
 			logger.info("Client has successfully notified server about his ready status");
 		} catch (UnknownHostException e) {
 			logger.severe("Could not read local IP address");
 		} catch (RemoteException e) {
 			logger.severe("SetReady on server failed");
 		}
 	}
 
 	private void shutdownClientController() throws RemoteException {
 		try {
 			Naming.unbind("rmi://localhost:" + ClientRmiPort + "/Client");
 		} catch (MalformedURLException e1) {
 			throw new RemoteException("Malformed URL has occurred in ClientController", e1);
 		} catch (NotBoundException e1) {
 			throw new RemoteException("Unbinding the ClientController failed", e1);
 		}
 		UnicastRemoteObject.unexportObject(this, true);
 		closeClientController(2000);
 	}
 
 	private void closeClientController(final long delay) {
 		new Thread() {
 			@Override
 			public void run() {
 				logger.info("ClientController is shutting down");
 				try {
 					sleep(delay);
 				} catch (InterruptedException e) {
 				}
 				System.exit(0);
 			}
 		}.start();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new ClientController(args).startController();
 	}
 
 	private void startController() {
 
 		loadLogggerConfig();
 
 		if (args.length == 1) {			
 			ClientRegistryName = args[0];
 			publishingClient(this, ClientRegistryName, DEFAULT_CLIENT_PORT);
 		} else if (args.length == 2) {		
 			ClientRegistryName = args[0];
 			ClientRmiPort = Integer.valueOf(args[1]);
 			publishingClient(this, ClientRegistryName, ClientRmiPort);
 		} else {
 			logger.warning("Number of parameters does not fit for the ClientController, ClientController is closing");
 			System.exit(0);
 		}
 	}
 
 	private void loadLogggerConfig() {
 		try {
 			System.setProperty("java.util.logging.config.file", "logger.config");
 			LogManager.getLogManager().readConfiguration();
 		} catch (IOException e) {
			System.out.println("Logger configuration file could not be read: " + e.getMessage());
 		}catch(SecurityException se){
			System.out.println("Security exception occurred: " + se.getMessage());
 		}
 	}
 
 	private void publishingClient(ClientController controller, String rmiName, int port) {
 		try {
 			LocateRegistry.createRegistry(port);
 			ClientInterface skeleton = (ClientInterface) UnicastRemoteObject.exportObject(controller, port);
 			Registry r = LocateRegistry.getRegistry(port);
 			r.rebind(rmiName, skeleton);
 			logger.info("Client is ready and listening on port: " + port + " with ClientRegistryName: " + rmiName);
 		} catch (RemoteException e) {
 			logger.severe(e.getMessage());
 			System.exit(0);
 		}
 	}
 }
