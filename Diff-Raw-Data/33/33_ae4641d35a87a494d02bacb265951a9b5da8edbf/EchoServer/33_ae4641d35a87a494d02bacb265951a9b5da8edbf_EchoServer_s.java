package relay;
 
 /**
  * Listen on port and echo incoming to stdout
  * 
  */
 
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class EchoServer {
 	
 	public static void main(String args[]){
     	
 			
     		try {
 		        int port = 2001;
 		        ServerSocket srv = new ServerSocket(port);
		        System.out.println("Started Echo Server.-..");
 		        while(true){
 			        // Wait for connection from client.
 			        Socket socket = srv.accept();
 			        
 			        EchoWorkerThread rwt = new EchoWorkerThread(socket);
 			        rwt.start();
 		        }
 		        
 		    } catch (Exception e) {
 		    	
 		    	e.printStackTrace();
 		
 		    }
 		    
     
 
 	}
 
 }
