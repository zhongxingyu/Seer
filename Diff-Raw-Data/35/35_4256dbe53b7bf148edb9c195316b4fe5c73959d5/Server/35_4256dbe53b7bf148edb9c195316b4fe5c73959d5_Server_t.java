 package chat.app.server;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 /**
  * @author Azamat Turgunbaev
  *
  */
 public class Server extends JFrame{
 
 	private static final long serialVersionUID = 1L;
 	
 	private ExecutorService executorService = Executors.newCachedThreadPool();
 	
 	private JTextArea displayArea;
 	
 	private ObjectOutputStream outputStream;
 	private ObjectInputStream inputStream;
 	
 	public List<NewClient> clientList = new ArrayList<>();
 
 	private ServerSocket server;
 		
 	private int counter = 0;
 			
 	public Server() {
 		
 		super("Server Application");
 		
 		try {
 			server = new ServerSocket( 12345, 10 );
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		displayArea = new JTextArea();
 		displayArea.setEditable(false);
 		add( new JScrollPane( displayArea ));
 		
 		setSize( 300, 500 );
 		setVisible( true );
 	}
 	
 	public void runServer() throws IOException {
 		
 		displayMessage( "Server listening on " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort() );
 		while( true ) {
 		
 			try {
 				Socket connection = server.accept();
 				NewClient newClient = new NewClient( connection );
 				redraw();
 				executorService.execute( newClient );
				clientList.add(newClient);
				for( NewClient client : clientList ) {
					client.setClientList(clientList);
				}
 				displayMessage( "\nConnection " + ++counter + " received from " + connection.getInetAddress().getHostAddress() );
 				Thread.sleep(100);
 			} 
 			catch ( EOFException e) {
 				displayMessage("\nServer terminated connection");
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}	
 		}
 	}
 
 	public void getStreams( Socket socket ){
 		
 		try {
 		     outputStream = new ObjectOutputStream( socket.getOutputStream() );
 		     outputStream.flush();
 		
 		     inputStream = new ObjectInputStream( socket.getInputStream() );
 		}
 		catch ( IOException e) {
 			e.printStackTrace();
 		}
 	}
 	/*private void closeConnection() {
 		
 		displayMessage( "\nTerminating connection\n");
 		
 		try {
 			outputStream.close();
 			inputStream.close();
 			connection.close();
 		} 
 		catch( IOException e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	private void sendMessage( String message ) {
 		
 		try {
 			outputStream.writeObject( "SERVER>>> " + message );
 			outputStream.flush();
 			displayMessage( "\nSERVER>>> " + message );
 		}
 		catch (IOException e) {
 			displayArea.append( "Error writing Object" );
 		}
 	}*/
 	public void processConnection() throws IOException {
 		
 		String message = "";
 		
 		try {
 			message = (String)inputStream.readObject();
 			System.out.println( message );
 		}
 		catch ( ClassNotFoundException e ) {
 			
 		}
 		
 	}
 
 	public void getClientInformation( ) throws IOException{
 		processConnection();
 	}
 	
 	public void displayMessage( String message ) {
 		displayArea.append( message );	
 	}
 	
 	public void redraw() {
 		
 		for( NewClient count : clientList ) {
 			count.sendMessage( "redraw" );
 		}
 	}
 }
