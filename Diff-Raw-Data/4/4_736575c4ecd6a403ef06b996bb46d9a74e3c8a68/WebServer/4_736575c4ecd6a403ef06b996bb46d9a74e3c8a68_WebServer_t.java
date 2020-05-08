 package lab;
 import java.io.* ;
 import java.net.* ;
 import java.util.* ;
 
 public final class WebServer
 {
     ConnectionPool pool;
 
 	public void start() throws IOException
 	{
 		int port = ConfigManager.getInstance().getPort();
 		
 		// Establish the listen socket.
		//ServerSocket socket = new ServerSocket(port);
        Logger.info("Listening on port " + port + "...");
        ServerSocket socket = new ServerSocket(port);
 
         pool = new ConnectionPool();
         pool.start();
 		
 		// Process HTTP service requests in an infinite loop.
 		while (true)
 		{
 		    // Listen for a TCP connection request.
 		    Socket connection = socket.accept();
             pool.enqueue(connection);
 		}
     }
 	
 	public void stop() {
 		pool.shutdown();
 	}
 }
 
 
