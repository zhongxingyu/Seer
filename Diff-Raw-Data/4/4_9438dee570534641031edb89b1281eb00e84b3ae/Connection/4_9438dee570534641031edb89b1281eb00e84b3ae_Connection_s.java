 package edu.virginia.cs.sgd.gpsgames.connection;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import android.util.Log;
 
 public class Connection implements Runnable{
 
 	private static final String TAG = "GPSGamesServerConnection";
 	public StringSocketHandler sock;
 	private boolean open;
 	private String ip;
 	private int port;
 	BlockingQueue<String> outgoing = new LinkedBlockingQueue<String>();
 	BlockingQueue<String> incoming = new LinkedBlockingQueue<String>();
 	
 	public Connection(String ip, int port){
 		this.ip = ip;
 		this.port = port;
 	}
 	@Override
 	public void run() {
 		
 		openSocket();
 		
		while (open && !sock.isClosed() && !sock.isConnected())
 		{
 			//wait around and let the socket handler do its job
 			String message = outgoing.poll();
 			while (message != null){
 				sock.write(message);
 				message = outgoing.poll();
 			}
 			if (sock.getIncomingMessages() != null)
 			{
 				incoming.addAll(sock.getIncomingMessages());
 			}
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		Log.d(TAG,"Closing");
 		open = false;
 		try {
 			sock.close();
 			Log.d(TAG,"Closed");
 	    	
 		} catch (IOException e) {
 			Log.d(TAG,"Problem closing");
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns whether or not the connection is supposed to be open
 	 * @return
 	 */
 	public boolean isOpen(){
 		return open;
 	}
 	
 	public void close(){
 		open = false;
     	Log.d(TAG,"Asked to close");
 	}
 	
 	private void openSocket(){
 		Socket s;
 		try {
 			Log.d(TAG,"Creating socket "+ip+":"+port);
 			s = new Socket(ip,port);
 			Log.d(TAG,"socket connected");
 			sock = new StringSocketHandler(s);
 			open = true;
 	    	Log.d(TAG,"Opened Connection");
 	    	
 		} catch (UnknownHostException e1) {
 			Log.d(TAG,"Unknown host");
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			Log.d(TAG,"IO Exception creating socket");
 			e1.printStackTrace();
 			Log.d(TAG,e1.getMessage());
 		}
 	}
 	
 	public void send(String message){
 		outgoing.add(message);
 	}
 	
 	/**
 	 * Gets all of the current messages and deletes them from the connection
 	 * @return
 	 */
 	public ArrayList<String> retrieve(){
 		ArrayList<String> messages = new ArrayList<String>();
 		messages.addAll(incoming);
 		incoming.clear();
 		return messages;
 	}
 	
 	
 
 }
