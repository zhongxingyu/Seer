 package Networking;
 
 import java.net.Socket;
 
 import javax.swing.JFrame;
 
 import Utils.Constants;
 
 /**
  * Abstract class that all managers extend from.
  * 
  * @author Peter Zhang
  */
 public abstract class Client extends JFrame{
 	
 	private Socket socket;
 	protected ServerReader reader;
 	protected StreamWriter writer;
 	
 	protected String clientName;
 	
 	/**
 	 * This is called by ServerReaders' receiveData(Object), taking in a Request variable casted from ObjectInput.
 	 * Must be implemented by the Manager subclasses so to parse the Request variable accordingly.
 	 */
 	public abstract void receiveData(Request req);
 	
 	public void initStreams() {
 		try {
 		    socket = new Socket("localhost", Constants.SERVER_PORT);
		    System.out.println("Client: connected to the server");
 		    
 		    reader = new ServerReader(socket, this);
 		    writer = new StreamWriter(socket);
 		    new Thread(reader).start();
		    System.out.println("Client: streams ready");
 		} catch (Exception e) {
 		    System.out.println("Client: got an exception initing streams" + e.getMessage() );
 		    e.printStackTrace();
 		    System.exit(1);
 		}
 		
 		// establish client identity with server
 		writer.sendData(new Request(Constants.IDENTIFY_COMMAND, Constants.SERVER_TARGET, clientName));
 	}
 }
