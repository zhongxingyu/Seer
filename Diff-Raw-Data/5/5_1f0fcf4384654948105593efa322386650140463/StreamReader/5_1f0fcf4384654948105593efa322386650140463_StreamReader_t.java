 package Networking;
 
 import java.io.EOFException;
 import java.io.ObjectInputStream;
 import java.net.SocketException;
 
 /**
  * Abstract class, so that client/server side can have their own implementation on how the data is handled.
  * 
  * @author Peter Zhang
  */
 public abstract class StreamReader implements Runnable{
 	private ObjectInputStream ois;	
 
 	public StreamReader(ObjectInputStream o){
 		ois = o;
 		System.out.println("StreamReader: got stream");
 	}
 	
 	@Override
 	public void run() {
 		while(true) {
 			try {
 				Request req = (Request) ois.readObject();
 				System.out.println("StreamReader: received request = " + req.getCommand());
 				receiveData(req);
 			} catch(EOFException e) {
				System.out.println("StreamReader: Connection lost. Other terminal has disconnected.");
 				break;
 			} catch(SocketException e) {
				System.out.println("StreamReader: Connection lost. Other terminal has disconnected.");
 				break;
 			} catch(Exception e) {
 				System.out.println("StreamReader: Cannot read data");
 				e.printStackTrace();
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * To be implemented by either ServerReader and ClientReader, specifying target terminal to receive the request. 
 	 */
 	public abstract void receiveData(Request req);
 
 }
