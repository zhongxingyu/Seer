 package AP2DX;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.lang.Thread;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 
 import AP2DX.*;
 import AP2DX.usarsim.UsarSimMessageReader;
 
 //import com.sun.xml.internal.ws.api.message.Message;
 
 /**
  * 
  * This class represents a connection with another module. It continues to
  * listen for incoming messages and puts them in the BlockingQueue of the
  * abstract base. There the message will wait until it can be processed by the
  * Logic thread. <br />
  * <br />
  * Besides listening for message, it can also send messages.
  * 
  * @author Maarten Inja
  */
 public class ConnectionHandler extends Thread {
 
 	private Socket socket;
 	private AP2DXBase base;
     /** The module with which this ConnectionHandler has a connection */
 	public final Module moduleID;
 
 	private PrintWriter out;
 	private IMessageReader in;
 	private boolean alive = true;
 
 	/**
 	 * The constructor, saves arguments and reads in- and outcoming streams from
 	 * the socket.
 	 * 
 	 * This constructor is for the base-class self-started connections.
 	 * 
 	 * @param usar
 	 *            If true, handle a USARsim Connection
 	 * @throws IOException
 	 */
 	public ConnectionHandler(boolean usar, AP2DXBase base, Socket socket,
 			Module origin, Module destination) throws IOException {
 		this.base = base;
 		this.socket = socket;
 		this.moduleID = destination;
 		out = new PrintWriter(socket.getOutputStream(), true);
 
 		if (usar) {
 			in = new UsarSimMessageReader(socket.getInputStream());
 		} else {
 			in = new AP2DXMessageReader(socket.getInputStream(), origin,
 					destination);
 		}
 	}
 
 	/**
 	 * The constructor, saves arguments and reads in- and outcoming streams from
 	 * the socket.
 	 * 
 	 * This constructor is for the incoming connections.
 	 * 
 	 * @param usar
 	 *            If true, handle a USARsim Connection
 	 * @throws IOException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 */
 	public ConnectionHandler(AP2DXBase base, Socket socket, Module origin)
 			throws IOException, InstantiationException, IllegalAccessException {
 		this.base = base;
 		this.socket = socket;
 
 		out = new PrintWriter(socket.getOutputStream(), true);
 
 		in = new AP2DXMessageReader(socket.getInputStream(), origin);
<<<<<<< HEAD
 
		System.out.println("Waiting for first message on socket " + socket.getLocalPort());
 		this.base.logger.info("Waiting for msg in conn handler ctor");
 		Message firstIncomingMessage = in.readMessage();
 		this.base.logger.info(String.format("got msg in conn handler ctor: %s", firstIncomingMessage.messageString));
		System.out.println("Got it!");
 
=======
		Message firstIncomingMessage = in.readMessage();
>>>>>>> 7d30e22650b0d36301f0251af58d0a2b4088c35f
 		this.moduleID = firstIncomingMessage.getSourceModuleId();
 
 	}
 
 	/** Thread logic. */
 	public void run() {
 		while (true)
 		{
 			try {
 				AP2DXMessage incomingMessage = (AP2DXMessage) in.readMessage();
                 System.out.printf("Incoming message: %s\n", incomingMessage);
 				base.getReceiveQueue().put(incomingMessage);
 			} catch (SocketTimeoutException e) {
 				AP2DXBase.logger.warning(String.format("Time out receiving from: %s",
 						socket.getRemoteSocketAddress()));
 				if (!socket.isConnected())
 					setConnAlive(false);
 					break;
 			} catch (Exception e) {
 				AP2DXBase.logger
 						.severe(String.format("%s IN ConnectionHandler.run, attempting to read message.",e.getMessage()));
                 e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Sends a message object, this is currently only the message object
 	 * toString().
 	 */
 	public void sendMessage(Message message) {
 		out.println(message.toString());
 	}
 	
 	public boolean isConnAlive() {
 		return alive ;
 	}
 	
 	private void setConnAlive(boolean value) {
 		alive = value;
 	}
 	
 	public int getPort() {
 		return this.socket.getPort();
 	}
 	
 	public String getAddress() {
 		return this.socket.getInetAddress().getHostAddress();
 	}
 	
 	public Module getModule() {
 		return this.moduleID;
 	}
 
 }
