 /**
  * 
  */
 package chat.app.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * @author Azamat Turgunbaev
  *
  */
 public class NewClient implements Runnable {
 
 	private ObjectOutputStream outputStream;
 	private ObjectInputStream inputStream;
 	
 	private List<NewClient> clientList = new ArrayList<>();
 	
 	private Server server;
 	
 	private Socket connection;
 	
 	private String userName;
 	
 	public NewClient( Socket socket ){
 		
 		connection = socket;
 		
 		try {
 			getStreams();		
 		} 
 		catch ( IOException e ) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 		
 	public void run() {
 			
 		try {
 			getUserInformation();
 			processConnection();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		finally {
 			closeConnection();
 		}
 	}
 	
 	public void getStreams() throws IOException {
 		
 		outputStream = new ObjectOutputStream( connection.getOutputStream() );
 		outputStream.flush();
 		inputStream = new ObjectInputStream( connection.getInputStream() );
 	}
 
 	public void processConnection() throws IOException {
 		
 		String message = "";
 		
 		do {
 			try {
 				message = (String)inputStream.readObject();
 				StringTokenizer tokens = new StringTokenizer(message, "<<>>" );
 				
 				String receiver = null;
 				
 				if( tokens.hasMoreTokens() ){
 					receiver = tokens.nextToken();
 					if( tokens.hasMoreTokens()) {
 						message = tokens.nextToken();	
 					}
 				}
 								
 				NewClient targetClient = null;
 				boolean hasTarget = false;
 								
 				for( NewClient count : clientList ){
 					if( count.getUserName().equals( receiver) ){
 						targetClient = count;
 						hasTarget = true;
 						break;
 					}
 				}
 				
 				if(hasTarget){
 					targetClient.sendMessage( message );
 				}
 			}
 			catch ( ClassNotFoundException e ) {
 				
 			}
		} while ( !message.equals( "TERMINATE" ));
 		sendMessage("TERMINATE");
 	}
 	
 	public void sendMessage( String message ) {
 		
 		try {
 			outputStream.writeObject( message );
 			outputStream.flush();
 		}
 		catch (IOException e) {
 			
 		}
 	}
 	
 	public void getUserInformation() {
 		try {
 			userName = (String)inputStream.readObject();
 		}
 		catch ( Exception e ) {}
 	}
 	
 	public void closeConnection() {
 		
 		try {
 			outputStream.close();
 			inputStream.close();
 			connection.close();
 		} 
 		catch( IOException e ) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	public List<NewClient> getClientList() {
 		return clientList;
 	}
 
 	public void setClientList(List<NewClient> clientList) {
		this.clientList = null;
 		this.clientList = clientList;
 	}
 
 	public Server getServer() {
 		return server;
 	}
 
 	public void setServer(Server server) {
 		this.server = server;
 	}	
 }
