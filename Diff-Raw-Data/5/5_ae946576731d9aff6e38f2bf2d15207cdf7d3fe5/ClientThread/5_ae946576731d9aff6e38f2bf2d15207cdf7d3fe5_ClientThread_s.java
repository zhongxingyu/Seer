 package grouppractical.server;
 
 import grouppractical.client.commands.ClientType;
 import grouppractical.client.commands.Command;
 import grouppractical.client.commands.CommandParser;
 import grouppractical.client.commands.ConnectCommand;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.concurrent.Semaphore;
 
 /**
  * Thread which is bound to a connection to a client, which parses input from the client and sends it to
  * the main server thread
  * @author janslow
  *
  */
 class ClientThread extends Thread implements CommandListener {
 	/** Parent server */
 	private final MultiServerThread server;
 	/** Socket connection to client */
 	private final Socket socket;
 	/** Client ID */
 	private final int id;
 	private final Semaphore writeSem;
 	
 	private PrintWriter pw;
 	private BufferedReader br;
 	/** Is the thread running or has it been closed */
 	private boolean running;
 	private ClientType clientType;
 	
 	/**
 	 * Constructs a new thread to communicate with client
 	 * @param server Server object to send finished responses to
 	 * @param group
 	 * @param socket
 	 * @param id
 	 */
 	public ClientThread(MultiServerThread server, ThreadGroup group, Socket socket, int id) {
 		super(group, String.format("ClientThread(%d)",id));
 		this.server = server;
 		this.socket = socket;
 		this.id = id;
 		this.clientType = ClientType.REMOTE;
 		this.writeSem = new Semaphore(1);
 	}
 	
 	/**
 	 * Closes the thread and the connection to the client
 	 */
 	public void close() {
 		if (!running) return;
 
 		running = false;
 		
 		//Interrupts the thread (to break from the run loop)
 		this.interrupt();
 		//Closes the input and output streams
 		if (pw != null) pw.close();
 		if (br != null)
 			try {
 				br.close();
 			} catch (IOException e) { }
 		try {
 			socket.close();
 		} catch (IOException e) { }
 		
 		//Tells the main thread that this client is closed
 		server.closeClient(id);
 	}
 	
 	/**
 	 * Accepts input from the client and attempts to parse it into commands, sending the parsed commands
 	 * to the main server thread
 	 */
 	public void run() {
 		running = true;
 		try {
 			pw = new PrintWriter(socket.getOutputStream(), true);
 			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		} catch (IOException e) {
 			close();
 		}
 		
 		CommandParser cmdparse = new CommandParser();
 		while (running) {
 			boolean word = false;
 			while (!word) {
 				try {
 					char c = (char) br.read();
 					cmdparse.enqueue(c);
 				} catch (IOException e) {
 					System.err.println(e.toString());
 					close();
 					return;
 				} catch (InterruptedException e) {
 					close();
 					return;
 				}
 				
 				while (!cmdparse.isOutputEmpty()) {
 					Command cmd = cmdparse.dequeue();
 					switch (cmd.getCommandType()) {
 					//Connect should update the client type
 					case CONNECT:
 						clientType = ((ConnectCommand)cmd).getClientType();
 						break;
 					//MInitialize should cause the ClientThread to send the entire map
 					case MINITIALISE:
 						//TODO Send all positions to client
 						break;
 					//The following commands should be passed to the main thread
 					case MPOSITION:
 					case RDISTANCE:
 					case RLOCK:
 					case RROTATE:
 					case RSTOP:
 					case RUNLOCK:
 						server.enqueueCommand(cmd);
 					//RStatus should be ignored
 					case RSTATUS:
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Gets whether the thread is running
 	 * @return True if running, otherwise false
 	 */
 	public boolean isRunning() {
 		return running;
 	}
 
 	@Override
 	public void enqueueCommand(Command cmd) {
 		switch (clientType) {
 		//Kinect client listens to RStatus and RLock commands
 		case KINECT:
 			switch (cmd.getCommandType()) {
 			case RSTATUS:
 			case RLOCK:
 				sendCommand(cmd);
 				break;
 			}
 			break;
 		//Mapper clients listen to MPosition, RStatus, RLock, RUnlock
 		case MAPPER:
 			switch (cmd.getCommandType()) {
 			case MPOSITION:
 			case RSTATUS:
 			case RLOCK:
 			case RUNLOCK:
 				sendCommand(cmd);
 				break;
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Sends a command to the client
 	 * @param cmd Command to transmit
 	 */
 	private void sendCommand(Command cmd) {
 		writeSem.acquireUninterruptibly();
 		if (cmd != null)
 			pw.println(cmd.serialize());
 		writeSem.release();
 	}
 }
