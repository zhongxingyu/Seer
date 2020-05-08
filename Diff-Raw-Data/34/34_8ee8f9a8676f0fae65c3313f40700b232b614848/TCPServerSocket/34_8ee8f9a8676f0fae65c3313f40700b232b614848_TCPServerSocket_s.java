 package network.tcp.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.HashSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.TimeUnit;
 
 public class TCPServerSocket extends Thread{
 	private ServerSocket server;
 	private int port;
 	private int NTHREDS=100;
 	private	ExecutorService executor;
 	private HashSet<TCPServerConnection> connectionSet;
 	private boolean pauseListener=false;
 	private boolean listening=true;
 
 	public TCPServerSocket(int port){
 		this.port=port;
 		executor = Executors.newCachedThreadPool();
 		connectionSet= new HashSet<TCPServerConnection>();
 	}
 
 	public void init() throws IOException{
 		server= new ServerSocket(port);
 
 	}
 
 	@Override
 	public void run() {
 		TCPServerConnection conn=null;
 		System.out.println("Server listening for new connection");
 		try {
 			while(listening){
 
 				conn= new TCPServerConnection(server.accept());
 				System.out.println("new connection");
 				if(conn!=null){
 					connectionSet.add(conn);
 					executor.execute(conn);
 				}
 			} 
 
 		}
		catch (IOException | RejectedExecutionException e) {
 			//e.printStackTrace();
 			System.out.println("Socket-listener closed.");
 		}finally{
 		}
 	}
 
 	public synchronized void shutdown() throws IOException{
 
 		listening=false;
 		System.out.println("tcp server shutdown");
 
 		executor.shutdown();
 		/*
 		try {
 			executor.awaitTermination(1, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			//e1.printStackTrace();
 		}
 		 */
 
 		for(TCPServerConnection conn: connectionSet){
 			System.out.println("socket: terminating connections");
 			if(conn!=null){
 				//if(co)
 				conn.shutdown();
 			}
 		}
 		server.close();
 	}
 
 	public synchronized void closeConnections() throws IOException{
 		pauseListener=true;
 		for(TCPServerConnection conn: connectionSet){
 			System.out.println("socket: terminating connections");
 			conn.goOffline();
 		}
 		server.close();
 	}
 
 }
