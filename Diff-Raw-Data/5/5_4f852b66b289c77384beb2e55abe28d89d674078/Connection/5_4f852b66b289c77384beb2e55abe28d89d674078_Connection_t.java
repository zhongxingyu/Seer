 package game;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.LinkedList;
 import java.util.Queue;
 
 
 public class Connection {
 
 	private Socket sock;
 	private BufferedReader in;
 	private PrintWriter out;
 	private Queue<String> writeQueue;
 	
 	// Threads
 	private Thread readThread;
 	private Thread writeThread;
 	private volatile boolean running = true;
 	
 	// OnReceiveListener
 	private OnReceiveListener onReceiveListener = null;
 	
 	public Connection() {
 		sock = new Socket();
 		
 		// TODO: find out if LinkedList is the best to use here
 		writeQueue = new LinkedList<String>();
 	}
 	
 	public boolean isConnected() {
 		return sock.isConnected();
 	}
 	
 	public void setOnReceiveListener(OnReceiveListener onReceiveListener) {
 		this.onReceiveListener = onReceiveListener;
 	}
 	
 	public void connect(String hostname, int port) throws Exception {
 		sock.connect(new InetSocketAddress(hostname, port));
 		
 		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 		out = new PrintWriter(sock.getOutputStream(), true);
 		
 		// Start the read thread
 		readThread = new Thread() {
 			@Override
 			public void run() {
 				while(running) {
 					try {
 						// Read a line
 						String line = in.readLine();
 						
 						// Process the line
 						if(onReceiveListener != null)
 							onReceiveListener.onReceive(line);
 					} catch (IOException e) {
 						System.out.println("Disconnected by server");
 						running = false;
 						stop();
 					}
 				}
 			}
 		};
 		
 		readThread.start();
 		
 		// Start the write thread
 		writeThread = new Thread() {
 			@Override
 			public void run() {
 				while(running) {
 					if(writeQueue.size() > 0) {
 						String line = writeQueue.remove();
 						out.println(line);
 					}
 				}
 			}
 		};
 		
 		writeThread.start();
 	
 	}
 	
 	public void stop() {
 		running = false;
 		
 		// Force stop the threads
		if(readThread != null && readThread.isAlive())
 			readThread.stop();
		if(writeThread != null && writeThread.isAlive())
 			writeThread.stop();
 		
 		// Close the socket
 		try {
 			if(sock.isConnected())
 				sock.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void writeLine(String line) {
 		writeQueue.add(line);
 	}
 	
 	public interface OnReceiveListener {
 		public void onReceive(String line);
 	}
 	
 }
