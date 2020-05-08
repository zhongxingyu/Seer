 package poker.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.Socket;
 
 import message.data.ClientResponse;
 
 public class Connection implements Runnable {
 
 	/* Private constants. */
 	// Time limit for user to respond, otherwise client 'Socket' will be timed out.
 	private static final int READ_TIMEOUT = 0;
 
 	/* Creates a new instance of 'Connection' class for each client. */
 	public Connection(Socket client) throws IOException {
 		this.client = client;
 		in = new ObjectInputStream(client.getInputStream());
 	}
 
 	public void run() {
 
 	}
 
 	/*
 	 * Sends to a client 'Command' object by which client response type
 	 * is determined. Once this command is sent to client, ObjectInputStream waits
 	 * for client answer. Client has to respond before time limit exceeds.
 	 */
 	public ClientResponse getMove() {
 		try {
			
 			client.setSoTimeout(READ_TIMEOUT);
 			ClientResponse move = null;
 			try {
 				move = (ClientResponse) in.readObject();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			return move;
 		} catch (IOException e) {
 			return null;
		} 
 	}
 	
 	/* Returns client 'Socket'. */
 	public Socket getClient() {
 		return client;
 	}
 
 	/* Private instance variables */
 	private Socket client;
 	public ObjectInputStream in;
 }
