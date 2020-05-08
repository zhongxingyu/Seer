 package ecologylab.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.Socket;
 
 import ecologylab.generic.Debug;
 import ecologylab.services.logging.LoggingDef;
 import ecologylab.services.messages.ErrorResponse;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * Interface Ecology Lab Distributed Computing Services framework<p/>
  * 
  * Runs the connection from the server to a client.
  * 
  * @author andruid
  * @author zach
  * @author eunyee
  */
 public class ServerToClientConnection extends Debug
 implements Runnable
 {
 	/**
 	 * If we get more bad messages than this, it may be malicous.
 	 */
 	static final int 				MAXIMUM_TRANSMISSION_ERRORS = 3;
 	
 	protected InputStream  		inputStream;
 	protected PrintStream			outputStreamWriter;
 	
 	protected ServicesServer		servicesServer;
 	protected Socket				incomingSocket;
 	
 	protected boolean				running	= true;
 	
 	public ServerToClientConnection(Socket incomingSocket, ServicesServer servicesServer)
 	throws IOException
 	{
 		this.incomingSocket	= incomingSocket;
 		
 		inputStream			= incomingSocket.getInputStream();
 
 		outputStreamWriter	= new PrintStream(incomingSocket.getOutputStream());
 		
 		this.servicesServer	= servicesServer;
 	}
 	public String toString()
 	{
 		return super.getClassName() + "[" + incomingSocket + "]";
 	}
 	/**
 	 * Service the client connection.
 	 * <p/>
 	 * Do not override this method!
 	 * If you need more specific functionality, add some sort of a hook that gets called from in here,
 	 * that subclasses can override. -- Andruid
 	 */
 	public final void run()
 	{
 		int badTransmissionCount	= 0;
 		while (running) 
 		{
 			//debug("waiting for packet");
 			//get the packet message
 			String messageString	= "";
 			try
 			{
 				//TODO -- change to nio
 		//		messageString = inputStreamReader.readLine();
 				messageString = readToMax(inputStream);
 				if( messageString != null )
 				{
 					debug("got raw message: " + messageString.getBytes().length );
 			
 					RequestMessage requestMessage = translateXMLStringToRequestMessage(messageString);
 					
 					if (requestMessage == null)
 						debug("ERROR: translation failed: " + messageString);
 					else
 					{
 						//perform the service being requested
 						ResponseMessage responseMessage = performService(requestMessage);
 						
 						sendResponse(responseMessage);
 						badTransmissionCount	= 0;
 					}
 				} else {
         System.err.println("null returned.");            
                 }
 			} catch (java.net.SocketException e)
 			{
 				// this seems to mean the connection went away
 				if (outputStreamWriter != null) // dont need the message if we're already shutting down
 					debug("STOPPING:  It seems we are no longer connected to the client.");
 				break;
 			} catch (IOException e)
 			{
 				// TODO count streak of errors and break;
 				debug("IO ERROR: " + e.getMessage());
 				e.printStackTrace();
 			}
 			catch (XmlTranslationException e)
 			{
 				// report error on XML passed through the socket
 				debug("Bogus Message ERROR: " + messageString);
 				e.printStackTrace();
 				if (++badTransmissionCount >= MAXIMUM_TRANSMISSION_ERRORS)
 				{
 					debug("Too many bogus messages.");
 					break;
 				} 
 				else
 					try
 					{
 //						sendResponse(ResponseMessage.BADTransmissionResponse());
 						sendResponse(new BadTransmissionResponse());
 					} catch (XmlTranslationException e1)
 					{
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 			} catch (Exception e) {
 				debug("Exception Caught: " + e.toString());
 				e.printStackTrace();
 				break;
 			}
 		}
 		synchronized (this)
 		{
 			if (running)
 				stop();
 		}
 	}
 	/**
 	 * Use the ServicesServer and its ObjectRegistry to do the translation.
 	 * Can be overridden to provide special functionalities
 	 * 
 	 * @param messageString
 	 * @return
 	 * @throws XmlTranslationException
 	 */
 	protected RequestMessage translateXMLStringToRequestMessage(String messageString)
 	throws XmlTranslationException 
 	{
 		RequestMessage requestMessage = servicesServer.translateXMLStringToRequestMessage(messageString, true);
 		return requestMessage;
 	}
 	/**
 	 * Perform the service specified by the request method.
 	 * The default implementation, here, simply passes the message to the servicesServer,
 	 * which is keeping an objectRegistry context, and does the perform.
 	 * <p/>
 	 * This routine is abstracted out here, so that customized Servers can do thread/connection
 	 * specific custom processing in this method, as needed, by overriding the definition.
 	 * 
 	 * @param requestMessage
 	 * @return
 	 */
 	protected ResponseMessage performService(RequestMessage requestMessage)
 	{
 		ResponseMessage responseMessage = servicesServer.performService(requestMessage);
 		return responseMessage;
 	}
     
 	protected void sendResponse(ResponseMessage responseMessage) throws XmlTranslationException
 	{
 		//send the response
 		outputStreamWriter.println(responseMessage.translateToXML(false));
 		outputStreamWriter.flush();
 	}
     
 	public synchronized void stop()
 	{
 		running	= false;
 		debug("stopping.");
 		try
 		{
 			if (outputStreamWriter != null)
 			{
 				outputStreamWriter.close();
 				//debug("writer is closed.");
 				outputStreamWriter	= null;
 			}
 			if (inputStream != null)
 			{
 				inputStream.close();
 				//debug("reader is closed.");
 				inputStream	= null;
 			}
 		} catch (IOException e)
 		{
 			debug("while closing reader & writer: " + e.getMessage());
 		}
 		servicesServer.connectionTerminated(this);
 	}
 	
 	/**
 	 * Limit the data size and send exception if the request data is bigger than defined size. 
 	 * 
 	 * @param in
 	 * @return
 	 * @throws Exception
 	 */
 	public String readToMax(InputStream in) throws Exception
 	{
 		char[] ch_array = new char[LoggingDef.maxSize];
 		int count = 0;
 
 		while(count < LoggingDef.maxSize)
 		{
 			int c = in.read();
 			if( c == -1 )
				throw new Exception("Client terminated connection.");
 			
 			ch_array[count] = (char)c;	
 			count++;
 			if( (count!=1) && (c == '\n' || c == '\r'))
 			{
 				String str = new String(ch_array, 0, count);
 				str.trim();
 				return str;
 			}
 		}
 		
 		throw new Exception("Data is over Maximum Size !!");
 
 	}
 	/**
 	 * This is the error Response sent 3 times by this server, when it receives a bogus (not proper xml)
 	 * message. Outside of in this server, and in ServicesClient, where this message must recognized as
 	 * a request for a retry, this class *MUST* not be used anywhere!
 	 *
 	 * @author andruid
 	 */
 	class BadTransmissionResponse extends ErrorResponse
 	{
 		private BadTransmissionResponse()
 		{
 			
 		}
 	}
 }
