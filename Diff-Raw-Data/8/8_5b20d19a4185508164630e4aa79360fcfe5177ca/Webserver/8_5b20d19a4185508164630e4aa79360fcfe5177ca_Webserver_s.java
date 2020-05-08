 package http;
 
 import java.net.*;
 import java.util.*;
 import java.io.*;
 
 public class Webserver implements Runnable
 {
 	ServerSocket socket;
 
 	RequestHandler handler;
 
 	public Webserver(RequestHandler handler, int port) throws IOException
 	{
 		this.socket = new ServerSocket(port);
 		this.handler = handler;
 	}
 
 	public void run()
 	{
 		try
 		{
 			while (true)
 			{
 				Socket connection = socket.accept();
 
 				ResponseDelegate delegate = new ResponseDelegate(connection, handler);
 
 				new Thread(delegate).start();
 			}
 		}
 		catch (IOException e)
 		{
			System.out.println("Could not accept connection");
			e.printStackTrace();
 		}
 	}
}
