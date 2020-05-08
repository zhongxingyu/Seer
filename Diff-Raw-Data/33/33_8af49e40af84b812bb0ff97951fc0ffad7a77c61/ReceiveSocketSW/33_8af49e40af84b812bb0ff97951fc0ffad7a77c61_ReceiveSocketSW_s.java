 package bcmNetworking;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.List;
 
 import javax.swing.SwingWorker;
 
 import bcmBackend.Informable;
 
 public class ReceiveSocketSW extends SwingWorker<Integer, String>{
 
 	//Information passed to the Socket to facilitate creation
 	private int curPort;
 	private Informable informable;
 	
 	public ReceiveSocketSW(int curPort){
 		this.curPort = curPort;
 		this.execute();
 	}
 	
 	@Override
 	protected Integer doInBackground() throws Exception {
 		int port = curPort;
 		ServerSocket listener;
 		Socket connection;
 		BufferedReader incoming;
 		PrintWriter outgoing;
 		String messageIn;
 		
 		try{
 			//Initialize Server
 			listener = new ServerSocket(port);
 			System.out.println("Listening on port" + listener.getLocalPort());
 			//Upon successful connection, close listener
 			connection = listener.accept();
 			listener.close();
 			
 			//Initialize Stream Writers and Readers
 			incoming  = new BufferedReader(new InputStreamReader(connection.getInputStream()) );
 			outgoing = new PrintWriter(connection.getOutputStream());
 			
 			//Send handshake
 			outgoing.println(BCMProtocol.HANDSHAKE);
 			outgoing.flush();
 			//receive handshake and verify 
 			/*TODO: Create a loop that will wait for the right connection for an 
 			* appropriate amount of time, then timeout. 
 			*/
 			messageIn = incoming.readLine();
 			if(! BCMProtocol.HANDSHAKE.equals(messageIn)){
 				throw new Exception("Connected program is not a BCMsgr");
 			}
 			System.out.println("Connected.");
 			
 		} catch (Exception e) {
 			System.out.println("An error occured while opening the connection.");
 			System.out.println(e.toString());
 			return -1;
 		}
 		
 			//Continuously read lines from the input stream
 		try{
 			while(!isCancelled()) {
 			
 				messageIn = incoming.readLine();
 				
 				if(messageIn.length() > 0) {
 					System.out.println(messageIn);
 					publish(messageIn);
 					
 					/*if(messageIn.charAt(0) == CLOSED_CODE) {
 						System.out.println("Quit Command Recieved");
 						connection.close();
 						break;
 					} else if (messageIn.charAt(0) == MESSAGE_CODE) {
 						messageIn = messageIn.substring(1);
 						System.out.println(messageIn);
 					}*/
 				}
 			}
 			
 			
 		} catch (Exception e) {
 			System.out.println("Sorry, an error has occured. Connection lost.");
 			//System.exit(1);
 			return -1;
 		}
 		return 0;
 	}
 	
 	public void setInformable(Informable informable){
 		this.informable = informable;
 	}
 	
 	@Override
 	protected void process(List<String> chunks){
 		for(String message : chunks){
 			informable.messageReceived(message);
 		}
 	}
 	
 }
