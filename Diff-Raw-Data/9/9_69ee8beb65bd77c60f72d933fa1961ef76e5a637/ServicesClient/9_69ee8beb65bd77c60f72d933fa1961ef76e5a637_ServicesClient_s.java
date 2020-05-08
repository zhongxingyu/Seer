 package ecologylab.services;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.Socket;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 import ecologylab.xml.NameSpace;
 
 /**
  * Interface Ecology Lab Distributed Computing Services framework<p/>
  * 
  * Client to connect to ServicesServer.
  * 
  * @author blake
  * @author andruid
  */
 public class ServicesClient
 extends Debug
 {
 	private Socket 		sock;
 	BufferedReader 		reader;
 	PrintStream 		output;
 	
 	private int 		port;
 	private String		server;
 	private NameSpace	translationSpace = null;
 	
 	protected ObjectRegistry	objectRegistry;
 	
 	/**
 	 * Create a client that will connect on the provided port. Assume localhost
 	 * 
 	 * @param port The localhost port to connect.
 	 */
 	public ServicesClient(int port)
 	{
 		this(port, null);
 	}
 	
 	
 	public ServicesClient(int port, NameSpace messageSpace)
 	{
 		this("localhost", port, messageSpace);
 	}
 	
 	public ServicesClient(String server, int port)
 	{
 		this("localhost", port, null);
 	}
 	
 	public ServicesClient(String server, int port, NameSpace messageSpace)
 	{
 		this(server, port, messageSpace, null);
 	}
 	public ServicesClient(String server, int port, NameSpace messageSpace, ObjectRegistry objectRegistry)
 	{
 		this.port			= port;
 		this.server 		= server;
 		this.translationSpace 	= messageSpace;
 		
 		if (objectRegistry == null)
 			objectRegistry	= new ObjectRegistry();
 		this.objectRegistry	= objectRegistry;
 	}
 	
 	private boolean createConnection()
 	{
 		InetAddress address = null;
 		try 
 		{
 			//address = InetAddress.getLocalHost();
 			//get the address and connect
 			address 	= InetAddress.getByName(server);
 			sock 		= new Socket(address, port);
 			reader 		= new BufferedReader(new InputStreamReader(sock.getInputStream()));
 			output		= new PrintStream(sock.getOutputStream()); 
 		}
 		catch (Exception e)
 		{
 			//e.printStackTrace();
 			debug("Couldnt create connection to server: " + e);
 			sock = null;
 		}
 		return sock != null;
 	}
 	String 		toString;
 	public String toString()
 	{
 		String toString	= this.toString;
 		if (toString == null)
 		{
 			toString		= this.getClassName() + "[" + server + ": " + port + "]";
 			this.toString	= toString;
 		}
 		return toString;
 	}
 	
 	/**
 	 * Determine if we are connected.
 	 * 
 	 * @return True if connected, false if not.
 	 */
 	public boolean connected()
 	{
 		return (sock != null);
 	}
 	
 	/**
 	 * Connect to the server (if not already connected). Return connection status.
 	 * 
 	 * @return True if connected, false if not.
 	 */
 	public boolean connect()
 	{
 		if (connected())
 			return true;
 		
 		createConnection(); // has side effects on connected()!
 		
 		return connected();
 	}
     
     /**
      * Disconnect from the server (if connected).
      */
     public void disconnect()
     {
         if (connected())
         {
             try
             {
                 sock.close();
                 System.out.println("Closed the connection.");
                 sock = null;
             } catch (IOException e)
             {
                 e.printStackTrace();
             }
         } else
         {
             System.out.println("Could not close connection: not connected!");
         }
     }
     
 	/**
 	 * Send a message to the ServicesServer to get a service performed,
 	 * 
 	 * @param requestMessage
 	 * @return	The ResponseMessage from the server.
 	 * 			This could be null, which means that communication with the server failed.
 	 * 			Reasons for failure include:
 	 * 			1) IOException: the socket connection broke somehow.
 	 * 			2) XmlTranslationException: The message was malformed or 
 	 * 			   translation failed strangely.
 	 */
 	public ResponseMessage sendMessage(RequestMessage requestMessage)
 	{
 		if (!connected())
 			createConnection();
 		
 		ResponseMessage responseMessage	= null;
 		boolean transactionComplete = false;
 		int badTransmissionCount = 0;
 		while (!transactionComplete)
 		{
 			String requestMessageXML		= null;
 			try
 			{
 				requestMessageXML = requestMessage.translateToXML(false);
 
 				output.println(requestMessageXML);
 			
 				if (show(5))
 					debug("Services Client: just sent message: " + requestMessageXML);
 				String response;
 				
 				if (show(5))
 					debug("Services Client: awaiting a response");
 				response = reader.readLine();
 				
 				responseMessage = (ResponseMessage)
 						ResponseMessage.translateFromXMLString(response, translationSpace);
 				
 //				if (responseMessage.response.equals(BADTransmission))
 				if (responseMessage instanceof ServerToClientConnection.BadTransmissionResponse)
 				{
 					badTransmissionCount++;
 					if( badTransmissionCount == 3 )
 					{
 						debug("ERROR: Quitting sending to the server because of the network condition after " +
 								badTransmissionCount + " times try ");
 						break;
 					}
 					else
 						debug("ERROR: BADTransmission of: " + requestMessageXML + "\n\t Resending.");
 						
 				}
 				else
 				{
					//if (show(5))
 						debug("received response: " + response);
 					processResponse(responseMessage);
 					transactionComplete = true;
 				}
 			}
 			catch (Exception e)
 			{
 				debug("ERROR: Failed sending " + requestMessage + ": " + e);
 				transactionComplete	= true;
 			}
 		}
 		return responseMessage;
 	}
 
 	protected void processResponse(ResponseMessage responseMessage)
 	{
 		responseMessage.processResponse(objectRegistry);
 	}
 }
