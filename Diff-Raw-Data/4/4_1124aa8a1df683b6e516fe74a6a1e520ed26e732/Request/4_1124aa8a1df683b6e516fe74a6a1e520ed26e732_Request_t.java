 package net.dandielo.stats.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.dandielo.stats.exceptions.InvalidRequestException;
 
 public class Request implements Runnable {
 
 	private Socket socket;
 	private boolean valid = true;
 
 	public Request(Socket incoming)
 	{
 		this.socket = incoming;
 	}
 
 	@Override
 	public void run()
 	{
 		try
 		{
 			// Get input from the client
 			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 			
 			RequestType req;
 			String line;
 			
 			//handle the incoming connection
 			while(valid)
 			{
 				line = null;
 				req = null;
 				
 				while((line = in.readLine()) != null && 
 					  (req = RequestType.valueOf(line)).invalid() );
 				
 				//if disconnect then disconnect, lol
 				if ( req == null || req.disconnect() ) valid = false;
 
 				try
 				{
 					//if its a read request
 					if ( valid && req.update() )
 					{
 						RequestInfo info = new RequestInfo(in.readLine());
 
 						//update the stat
 						Manager.update(info.getPlugin(), info.getData());
 					}
 
 					//if it's a update request
 					if ( valid && req.get() )
 					{
 						RequestInfo info = new RequestInfo(in.readLine());
 
 						//get the stat value
 						Object result = Manager.get(info.getPlugin(), info.getData());
 
 						//send it back
 						out.println(result);
 					}
 				} 
 				catch(InvalidRequestException e)
 				{
 					System.out.print("An invalid request was send");
 				}
 			}
 
 			//close the socket
 			socket.close();
 		}
 		catch( IOException e ) { }
 	}
 	
 	static class RequestInfo
 	{
 		//the pattern used to split the request string
 		private static Pattern pattern = Pattern.compile("(?<plugin>[^:]+):(?<data>[\\S\\s]+)");
 
 		private String plugin;
 		private String data;
 		
 		public RequestInfo(String request) throws InvalidRequestException
 		{
 			Matcher matcher = pattern.matcher(request);
 			if ( !matcher.matches() )
 				throw new InvalidRequestException(request);
 			
			plugin = matcher.group(1);
			data = matcher.group(2);
 		}
 		
 		public String getData()
 		{
 			return data;
 		}
 		
 		public String getPlugin()
 		{
 			return plugin;
 		}
 	}
 	
 	enum RequestType
 	{
 		UPDATE, GET, DISCONNECT, INVALID;
 		
 		public static RequestType byId(int id)
 		{
 			switch(id)
 			{
 			case 0x1: return DISCONNECT;
 			case 0x2: return UPDATE;
 			case 0x4: return GET;
 			default: return INVALID;
 			}
 		}
 
 		public boolean update()
 		{
 			return this.equals(UPDATE);
 		}
 
 		public boolean get()
 		{
 			return this.equals(GET);
 		}
 
 		public boolean invalid()
 		{
 			return this.equals(INVALID);
 		}
 
 		public boolean disconnect()
 		{
 			return this.equals(DISCONNECT);
 		}
 	}
 }
