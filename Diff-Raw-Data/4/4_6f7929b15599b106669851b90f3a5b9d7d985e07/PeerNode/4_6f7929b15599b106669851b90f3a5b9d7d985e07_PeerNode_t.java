 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.util.Log;
 import di.kdd.smartmonitor.middlewareServices.TimeSynchronization;
 import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
 
 public final class PeerNode extends DistributedSystemNode implements Runnable {
 	/* The socket that the peer holds in order to get command messages from the Master */
 	
 	private ServerSocket commandsServerSocket;
 
 	private TimeSynchronization timeSync = new TimeSynchronization();
 
 	private static final String TAG = "peer";
 
 	/***
 	 * Sends a JOIN message to the Master node and if it gets accepted,
 	 * starts a thread that accepts commands from the Master
 	 * @param socket The connected to the Master node socket
 	 */
 	
 	public PeerNode(Socket socket) {		
 		Message message;
 		BufferedReader in;
 
 		try {
 			/* The Master node was found, send the JOIN message */
 			
 			message = new Message(Tag.JOIN, "");
 			send(socket, message);
 			
 			/* Receive peer data */
 
 			in = receive(socket);
 										
 			/* Parse peer data */
 			
 			peerData.addPeersFromStream(in);
 			socket.close();
 			
 			new Thread(this).start();
 		}
 		catch(Exception e) {
 			Log.e(TAG, "Failed to join the system");
 			e.printStackTrace();
 		}		
 	}
 	
 	/***
 	 * Accepts a socket connection on the COMMAND_PORT and waits for commands from the Master
 	 */
 	
 	@Override
 	public void run() {
 		Socket MasterSocket;
 		BufferedReader in;
 
 		android.os.Debug.waitForDebugger();
 		
 		try {
 			commandsServerSocket = new ServerSocket(ISmartMonitor.COMMAND_PORT);		
 			MasterSocket = commandsServerSocket.accept();
 		}
 		catch(IOException e) {
			Log.e(TAG, "Failed to accept command socket");
			e.printStackTrace();
			
 			return;
 		}
 		
 		/* Listen on MasterSocket for incoming commands from the Master */
 		
 		try {		
 			while(true) {
 				Message message;
 			
 				in = receive(MasterSocket);
 				message = new Message(in);				
 			
 				switch(message.getTag()) {
 					case NEW_PEER:
 						/* A new peer was accepted form the Master, get his IP address */										
 	
 						peerData.addPeerIP(message.getPayload());
 						break;
 					case SYNC:
 						/* The Master sent a message with its shipment timestamp */
 						
 						timeSync.timeReference(Long.parseLong(message.getPayload()));
 						break;
 					case START_SAMPLING:
 
 						break;
 					case STOP_SAMPLING:
 						
 						break;
 					case SEND_PEAKS:
 							
 						break;
 					default:
 						Log.d(TAG, "Not implemented Tag handling: " + message.getTag().toString());
 						break;
 				}
 			}
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}		
 	}
 
 	@Override
 	public void disconnect() {
 	}
 	
 	@Override
 	public boolean isMaster() {
 		return false;
 	}
 }
