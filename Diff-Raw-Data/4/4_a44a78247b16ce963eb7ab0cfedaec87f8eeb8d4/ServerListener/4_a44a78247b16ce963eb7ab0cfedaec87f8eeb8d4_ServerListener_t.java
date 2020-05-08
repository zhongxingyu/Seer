 package client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 
 import commands.Command;
 
 
 /**
  * Used to listen for server messages,
  * part of the non-blocking input scheme
  * @author Aleksey
  *
  */
 public class ServerListener implements Runnable {
 
 	/**
 	 * in: the inputstream
 	 * que: the object used for communication with the ClientGame
 	 */
 	private ObjectInputStream in;
 	private TaskQueue que;
 	
 	/**
 	 * Constructs the ServerListener from intputstream and communication
 	 * object
 	 * @param in
 	 *  Inputstream
 	 * @param que
 	 *  taskqueue of the {@link ClientGame}
 	 */
 	public ServerListener(InputStream in, TaskQueue que) {
 		try {
 			this.in =  new ObjectInputStream(in);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.que = que;
 	}
 	
 	
 	/**
 	 * Start of execution, reads in objects sent from the server
 	 * and passes them to {@link ClientGame}
 	 */
 	
 	public void run() {
 		
 		while(true) {
 			try {
 				Object token = in.readObject();
 				que.addTask((Command)token);
				synchronized (que) {
					que.notify();
				}
 				// String token = in.next();
 				// que.addTask(token)
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				System.out.println("Failed to load the right Class");
 				e.printStackTrace();
 			}
 		}
 	}
 }
