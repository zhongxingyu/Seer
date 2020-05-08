 package twp.core;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 
 public abstract class TWPServer {
 	
 	private class Listener extends Thread {
 
 		private TWPServer server;
 		private ServerSocket socket;
 		private boolean isActive;
 		
 		public Listener(TWPServer server, ServerSocket socket) {
 			this.server = server;	
 			this.socket = socket;
 			this.isActive = true;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				socket.setSoTimeout(1000);
 				while (isActive) {
 					try {
 						Socket s = socket.accept();
 						server.openConnection(s);
 					} catch (SocketTimeoutException e) {}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		public void disconnect() {
 			isActive = false;
 		}
 		
 	}
 	
 	private ServerSocket serverSocket;
 	private Listener listener;
 	protected TWPHandler handler;
 	
 	public TWPServer(int port) throws IOException {
 		serverSocket = new ServerSocket(port);
 		
 	}
 	
 	public void listen(TWPHandler handler) {
 		this.handler = handler;
 		if (listener != null && listener.isAlive())
 			listener.disconnect();
		System.out.println("Starting server thread");
		Listener listener = new Listener(this, serverSocket);
 		listener.start();
		System.out.println("going on");
 	}
 	
 	public void stop() {
		listener.disconnect();
 	}
 	
 	public abstract void openConnection(Socket socket);
 }
