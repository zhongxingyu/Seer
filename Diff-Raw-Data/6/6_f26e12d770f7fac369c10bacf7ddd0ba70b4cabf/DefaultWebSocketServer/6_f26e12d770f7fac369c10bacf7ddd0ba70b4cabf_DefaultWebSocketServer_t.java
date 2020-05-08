 package websocket.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashSet;
 import java.util.Set;
 
 import websocket.WebSocket;
 
 import org.apache.log4j.Logger;
 
 public abstract class DefaultWebSocketServer implements WebSocketServer {
 	private int port;
 	protected Set<WebSocket> connections;
 	protected Logger logger = Logger.getLogger(DefaultWebSocketServer.class);
 	private int connectionIndex=0;
 	private boolean running=false;
	private ServerSocket listener;
 	
 	public int getPort(){
 		return this.port;
 	}
 	
 	public DefaultWebSocketServer(){
 		this.connections = new HashSet<WebSocket>();
 	}
 	
 	public DefaultWebSocketServer(int port){
 		this();
 		this.port = port;
 	}
 
 	public void run() {
 		try{
 			logger.debug("Starting server on port "+port);
			listener = new ServerSocket(port);
 			running=true;
 			while(running){
 				logger.debug("Waiting for new connection");
 				Socket server = listener.accept();
 				WebSocket connection = new WebSocket(this,server);
 				connections.add(connection);
 				Thread t = new Thread(connection);
 				t.setName("websocket_"+(connectionIndex++));
 				t.start();
 			}
 		} catch (IOException ioe) {
 			logger.debug("IOException on socket listen: " + ioe);
 			ioe.printStackTrace();
 		}
 	}
 
 	public void sendToAll(String message) {
 		for (WebSocket ws:connections){
 			try {
 				ws.send(message);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void onClientDisconnect(WebSocket websocket) throws IOException{
 		if(this.connections.contains(websocket)){
 			this.connections.remove(websocket);
 		}
 	}
 	
 	public int getNumConnections(){
 		return connections.size();
 	}
 	
 	public void shutdown() throws IOException{
 	    running=false;
 	    for (WebSocket connection:connections){
 	        connection.disconnect( );
 	    }
	    listener.close();
 	}
 }
