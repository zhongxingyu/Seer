 package bcmNetworking;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 
 import bcmBackend.Informable;
 import bcmGUI.ChatWindow;
 
 public class SendSocketSW extends SwingWorker {
 		
 	//Information passed to the Socket to facilitate creation
 	private String ipAdd;
 	private int curPort;
 	private ChatWindow chatWindow;
 	private Bridge bridge;
 	
 	public SendSocketSW(String ipAdd, int curPort, Bridge bridge){
 		this.ipAdd = ipAdd;
 		this.curPort = curPort;
 		this.bridge = bridge;
 		this.execute();
 	}
 	
 	@Override
 	protected Object doInBackground() throws Exception{
 		String messageTyped;	
 		Socket connection;
 		BufferedReader incoming;
 		PrintWriter outgoing;
 		String messageIn;
 		try{
 			connection = new Socket(ipAdd, curPort);
 			incoming = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
 			outgoing = new PrintWriter(connection.getOutputStream(), true);
 			BufferedReader streamIn = new BufferedReader(new InputStreamReader(System.in));
 			
 			//Send out handshake
 			outgoing.println(BCMProtocol.HANDSHAKE);
 			
 			//take in handshake
 			messageIn = incoming.readLine();
 			if(! BCMProtocol.HANDSHAKE.equals(messageIn)){
 				throw new Exception("Connected program is not a BCMsgr");
 			}
 			System.out.println("Connected.");
 			
 			
 			while(!isCancelled()) {
 				//System.out.print("> ");
 				messageTyped = bridge.getMessage();
 				
 				outgoing.println(messageTyped);
 			}
 
 		} catch (Exception e) {
 			System.out.println("Error: " + e);
 		}
 		return null;
 	}
 }
