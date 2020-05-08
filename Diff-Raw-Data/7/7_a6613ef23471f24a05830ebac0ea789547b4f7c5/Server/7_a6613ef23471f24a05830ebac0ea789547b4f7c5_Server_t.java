 /**
 * @Author Chris Card
 * CSCI 565 project 1
 * This file defines the rmi method sendReceive and also starts the server
 */
 
 package Server;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import Compute.BulletinBoard;
 import Compute.Article;
 import java.util.concurrent.*;
 import java.util.*;
 
 
 public class Server implements BulletinBoard
 {
 	//Stores all messages that where recieved from clients
 	private ConcurrentLinkedQueue<Article> articles;
 
 	private boolean isMaster;
 	
 	/**
 	* @param master true if it is the master node false if it a slave node
 	* @param the location of the master node if it is a slave node this will be
 	* 		 a string in the form of <masterhostname>:<masterportnumber>
 	*/
 	public Server(boolean master, String masterName)
 	{
 		super();
 		articles = new ConcurrentLinkedQueue<Article>();
 
 		if (master) 
 		{
 			
 		}
 		else
 		{
 
 		}
 	}
 
 	//##################################################################
 	// Client Methods
 	//##################################################################
 
 	public void post(Article article)
 	{
 
 	}
 
 	public List<Article> getArticles()
 	{
 		return null;
 	}
 
 	public Article choose(int id)
 	{
 		return null;
 	}
 
 	//##################################################################
 	// Server Methods
 	//##################################################################
 
 	public void replicateWrite(Article article)
 	{
 
 	}
 
 	public void registerSlaveNode(String slaveNodeAddress)
 	{
 
 	}
 	
 	/**
 	* @param list of arguments in the following order
 	*			args[0] = socket
 	*/
 	public static void main(String[] args)
 	{
 		try{
 			String name = "Compute";
 
			BulletinBoard engine = new Server(false,"test");
 
 			BulletinBoard stub = (BulletinBoard) UnicastRemoteObject.exportObject(engine,0);
 
 			//This creates the rmiregistry so the user doesn't have to create it
 			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
 			registry.rebind(name,stub);
 
 			//Notifies user the server was bound to the socket
 			System.out.println("Server bound to socket: "+args[0]);
 			System.out.println("EOF");
 		} catch (Exception e){
 			System.err.println("Server exception:");
 			e.printStackTrace();
 		}
 	}
 }
