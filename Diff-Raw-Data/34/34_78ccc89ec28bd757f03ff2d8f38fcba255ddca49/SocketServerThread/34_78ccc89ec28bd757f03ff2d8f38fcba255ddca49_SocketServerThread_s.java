 package fi.leif.java.kindlet.sailersensor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import fi.leif.java.kindlet.sailersensor.messagehandler.MessageHandler;
 
 public class SocketServerThread extends Thread
 {
 	private boolean running = false;
 	ServerSocket server = null;
 	private Config config;
 	private MessageHandler msgHandler;
 	
 	public SocketServerThread(Config config, MessageHandler msgHandler)
 	{
 		this.config = config;
 		this.msgHandler = msgHandler;
 	}
 	
 	public void run()
 	{
 		startServer();
 	}
 	
 	private void startServer()
 	{
 		// Init DatagramSocket
 		try
 		{
 			server = new ServerSocket(config.SOCKET_PORT);
 			this.running = true;
 		}
 		// Unable to listen to port. Sleep & try again.
 		catch(IOException e1)
 		{
 			try 
 			{
 				SocketServerThread.sleep(config.SOCKET_SLEEP_ON_ERROR); 
 				startServer();
 			}
 			// Sleep might be interrupted, quit here
 			catch(InterruptedException e2) { return; }
 		}
 		
 		// Sart socket loop
 		while(this.running)
 		{
 			Socket socket = null;
 			BufferedReader in = null;
 			
 			try
 			{
 				// Wait for packets (blocking mode)
 				socket = server.accept();
 				
 				// Get data
 				in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
 				String msg = in.readLine();
 				
 				// Send message
 				msgHandler.sendMessage(msg);
 			}
 				
 			// ServerSocket.close() was called, end while loop
 			catch(SocketException se)
 			{
 				running = false;
 			}
 			
 			// Any other exception, sleep and continue while loop
 			catch(Exception e)
 			{
 				try 
 				{ 
 					SocketServerThread.sleep(config.SOCKET_SLEEP_ON_ERROR); 
 				}
 				// Sleep might be interrupted, end while loop
 				catch(InterruptedException ie) 
 				{
 					running = false;
 				}
 			}
 			
 			// Close resources
 			finally
 			{
 				try { in.close(); } catch(Exception e) {}
 				try { socket.close(); } catch(Exception e) {}
 			}
 		}
 	}
 	
 	public void stopServer()
 	{
 		if(server != null)
 		{
 			try 
 			{ 
 				running = false;
 				server.close();
 			}
 			catch(Exception e) {}
 		}
 	}
 }
