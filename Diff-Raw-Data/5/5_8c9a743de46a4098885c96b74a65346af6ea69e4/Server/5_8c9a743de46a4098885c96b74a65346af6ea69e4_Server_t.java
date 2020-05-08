 package ch.hsr.objectCaching.testFrameworkServer;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import ch.hsr.objectCaching.interfaces.ClientInterface;
import ch.hsr.objectCaching.interfaces.Scenario;
 import ch.hsr.objectCaching.interfaces.ServerInterface;
 import ch.hsr.objectCaching.testFrameworkServer.Client.Status;
 
 public class Server implements ServerInterface
 {
 	private ArrayList<Client> clients;
 	private static int clientRmiPort;
 	private static int serverRmiPort;
 	private Properties initFile;
 	private TestCaseFactory factory;
 	private Dispatcher dispatcher;
 	private int serverSocketPort; 
 	
 	public Server()
 	{
 		loadInitFile();
 		loadClientList();
 		loadSettings();
 		factory = new TestCaseFactory();
 		serverSocketPort = 12345;
 		dispatcher = new Dispatcher(serverSocketPort);
 		new Thread(dispatcher).start();
 	}
 	
 	private void initializeClient(Client client) 
 	{
 		try {
 			ClientInterface clientStub = (ClientInterface)Naming.lookup("rmi://" + client.getIp() + ":" + clientRmiPort + "/Client");
			//TODO scenario erzeugen
			clientStub.initialize("152.96.193.9", new Scenario(1));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadInitFile()
 	{
 		initFile = new Properties();
 		InputStream initFileStream;
 		
 		try {
 			initFileStream = new FileInputStream("initFile.conf");
 			initFile.load(initFileStream);
 			initFileStream.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	private void loadSettings()
 	{
 		Iterator<Entry<Object, Object>> iter = initFile.entrySet().iterator();
 		while(iter.hasNext())
 		{
 			Entry<Object, Object> temp = iter.next();
 			if(temp.getKey().equals("Clientport"))
 			{
 				clientRmiPort = Integer.valueOf((String)temp.getValue());
 			}
 			if(temp.getKey().equals("Serverport"))
 			{
 				serverRmiPort = Integer.valueOf((String)temp.getValue());
 			}
 		}
 		System.out.println(serverRmiPort);
 	}
 	
 	private void loadClientList()
 	{
 		clients = new ArrayList<Client>();
 		Iterator<Entry<Object, Object>> iter = initFile.entrySet().iterator();
 		while(iter.hasNext())
 		{
 			Entry<Object, Object> temp = iter.next();
 			if(temp.getKey().equals("Client"))
 			{
 				Client client = new Client((String)temp.getValue());
 				clients.add(client);
 			}
 		}
 	}
 	
 	@Override
 	public void setReady(String ip) 
 	{
 		System.out.println(ip);
 		for(int i = 0; i < clients.size(); i++)
 		{
 			if(clients.get(i).getIp().equals(ip))
 			{
 				clients.get(i).setStatus(Status.READY);
 			}
 		}
 		if(checkAllReady())
 		{
 			start();
 		}
 	}
 	
 	public int getSocketPort()
 	{
 		return serverSocketPort;
 	}
 	
 	private Client getClient()
 	{
 		return clients.get(0);
 	}
 	
 	private TestCase getTestCase()
 	{
 		return null;
 	}
 	
 	private void start()
 	{
 		
 	}
 	
 	private boolean checkAllReady()
 	{
 		for(int i = 0; i < clients.size(); i++)
 		{
 			if(clients.get(i).getStatus() == Status.NOTREADY)
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private void createTestFactory()
 	{
 		TestCaseFactory factory = new TestCaseFactory();
 	}
 	
 	private void createRegistry()
 	{
 		try {
 			LocateRegistry.createRegistry(serverRmiPort);
 			ServerInterface skeleton = (ServerInterface) UnicastRemoteObject.exportObject(this, serverRmiPort);
 			Registry reg = LocateRegistry.getRegistry(serverRmiPort);
 			reg.rebind("blupp", skeleton);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) 
 	{
 		Server myServer = new Server();
 		myServer.createRegistry();
 		myServer.initializeClient(myServer.getClient());
 	}
 
 	@Override
 	public void setResults() {
 		// TODO Auto-generated method stub
 		
 	}
 }
