 import java.io.IOException;
 import java.net.ServerSocket;
 
 /* Server
  * Multithreaded server that listens to port 8080
  *  
  * @author Jeanne Deng
  */
 
 public class Server {
 	
 	public static void main(String[] agrs) {
 		try {
 			int i = 1;
 			ServerSocket socket = new ServerSocket(8089);
 			while(true) {
 				System.out.println("Spawning " + i);
				Runnable run = new RequestHandler(socket.accept());
 				Thread thread = new Thread(run);
 				thread.start();
 				i++;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
