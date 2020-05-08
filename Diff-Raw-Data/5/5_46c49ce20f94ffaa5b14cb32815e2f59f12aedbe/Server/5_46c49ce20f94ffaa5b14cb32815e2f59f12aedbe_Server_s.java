 package net;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class Server {
 
     private static ExecutorService executor = Executors.newCachedThreadPool();
 
 	ServerSocket socket;
 
 	Session mainSession;
 
 	public Server()
 	{
 		initilize();
 	}
 
 	private void initilize()
 	{
 		try {
			socket = new ServerSocket(563);
 			System.out.println("socket managed");
 		} catch (IOException e) {
			System.out.println("Could not start server! " + e.toString());
 			return ;
 		}
 
 		mainSession = new Session();
 		executor.execute(mainSession);
 
 		mainCycle();
 
 	}
 
 	private void mainCycle()
 	{
 		while (true)
 		{
 			Socket client;
 
 			try
 			{
 				client = socket.accept();
 				System.out.println("new client accepted: " + client.getInetAddress().toString());
 				handleConnection(client);
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void handleConnection(Socket client) throws IOException
     {
         PlayerConnection newPlayer = new PlayerConnection(this, client);
         mainSession.addPlayer(newPlayer);
     }
 
    /* public void notifyClients(PlayerConnection playerConnection, BaseCommand command) throws IOException
     {
         command.serverTime = System.currentTimeMillis();
     }
 
     public void handleCommand(PlayerConnection playerConnection, BaseCommand command) throws IOException
     {
         //System.out.println("client: " + playerConnection.id + " >> " + command);
     }     */
 
     public void closed(PlayerConnection playerConnection)
     {
         System.out.println("Player left: " + playerConnection.id);
         mainSession.remove(playerConnection);
     }
 }
