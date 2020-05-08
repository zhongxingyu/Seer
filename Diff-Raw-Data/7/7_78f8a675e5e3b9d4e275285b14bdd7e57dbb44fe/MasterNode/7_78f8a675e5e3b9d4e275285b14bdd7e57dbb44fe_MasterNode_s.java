 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.util.Log;
 import di.kdd.smartmonitor.protocol.ISmartMonitor.Tag;
 import di.kdd.smartmonitor.protocol.exceptions.MasterException;
 
 public final class MasterNode extends DistributedSystemNode {
 	/* List of open sockets with the peers, in order to send them commands */
 	
 	private List<Socket> commandSockets = new ArrayList<Socket>();
 	
 	/* This thread is used only from the Master, in order to accept new 
 	 * nodes in the system.
 	 */
 	
 	private JoinThread joinThread;
 
	private static final String TAG = "Master";	
 	
 	public MasterNode() {
 		joinThread = new JoinThread(peerData);
 		joinThread.start();
 	}
 
 	/***
 	 * Sends a message to each connected peer
 	 * @param message The message to broadcast
 	 * @throws IOException
 	 */
 	
 	private void broadcastCommand(Message message) throws IOException {
 		Log.i(TAG, "Broadcasting " + message.toString());
 		
 		for(Socket peer : commandSockets) {
 			DistributedSystemNode.send(peer, message);
 		}		
 	}
 	
 	/***
	 * Called by the PeerData instance that this class holds,
	 * when a new IP is added by the KnockKnockThread.
 	 * @param ip The IP address of the node that joined the network.
 	 */
 	
 	protected void newPeerAddedHandler(String ip) {
 		Log.i(TAG, "New peer added: " + ip);
 		
 		try {			
 			/* Connect to the peer, in order to have a communication channel for commands */
 			
 			Socket commandSocket = new Socket(ip, ISmartMonitor.COMMAND_PORT);
 			commandSockets.add(commandSocket);
 			
 			/* Notify peers about the new peer that joined the network */
 			
 			Message message = new Message(Tag.NEW_PEER, ip);						
 			broadcastCommand(message);
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Failed to connect to " + ip);
 			peerData.removePeerIP(ip);
 		}
 	}
 	
 	public void startSampling() throws IOException {
 		broadcastCommand(new Message(Tag.START_SAMPLING, ""));
 	}
 
 	public void stopSampling() throws IOException {
 		broadcastCommand(new Message(Tag.STOP_SAMPLING, ""));
 	}
 	
 	@Override
 	public void disconnect() {
 		Log.i(TAG, "Disconnecting");
 		
 		if(joinThread != null) {
 			joinThread.interrupt();
 		}
 	}
 	
 	@Override
 	public boolean isMaster() {
 		return true;
 	}
 
 	public void computeModalFrequencies(Date from, Date to) throws MasterException, IOException {
 		Message message = new Message(Tag.SEND_PEAKS, 
 								Long.toString(from.getTime()) + "\n" + 
 								Long.toString(to.getTime()));
 		broadcastCommand(message);
 		
 		/* Gather each peer's peaks */
 		
 		for(Socket socket : commandSockets) {
 			BufferedReader in = receive(socket);
 			
 			message = new Message(in);
 			
 			//TODO
 		}
 	}	
 }
