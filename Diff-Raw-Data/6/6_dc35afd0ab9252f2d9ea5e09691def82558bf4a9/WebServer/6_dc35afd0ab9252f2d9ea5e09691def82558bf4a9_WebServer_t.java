 package com.nexus.webserver;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.nexus.logging.NexusLogger;
 
 public class WebServer implements Runnable {
 	
 	private Logger Log = Logger.getLogger("Webserver");
 	private int port;
 	private boolean restart = false;
 
 	private ServerSocket Socket;
 	private ServerSocketChannel SocketChannel;
 	private Selector SocketSelector;
 
 	public WebServer(int port){
 		this.port = port;
 		this.Log.setParent(NexusLogger.getLogger());
 	}
 
 	public void run() {
 		this.Log.info("Server starting...");
 		try {
 			SocketAddress addr = new InetSocketAddress(port);
 			SocketChannel = ServerSocketChannel.open();
 			SocketChannel.configureBlocking(false);
 			Socket = this.SocketChannel.socket();
 			Socket.bind(addr);
 			SocketSelector = Selector.open();
 			SocketChannel.register(SocketSelector, SocketChannel.validOps());
 		} 
 		catch (IOException e) {
 		    this.Log.severe("Could not listen on port " + this.port + "!");
 		    return;
 		}
 		
 		this.Log.info("Server started!");
 
 		boolean stop = false;
 		while (!stop) {
 			Socket ClientSocket = null;
 			SelectionKey Key = null;
 			try {
 				SocketSelector.select();
 				Set<SelectionKey> keys = SocketSelector.selectedKeys();
 				Iterator<SelectionKey> i = keys.iterator();
 				
 				while(i.hasNext()){
 					Key = i.next();
 					i.remove();
 					
 					if(!Key.isValid()){
 						continue;
 					}
 					try{
 						if (Key.isAcceptable()) {
 	                        SocketChannel Channel = SocketChannel.accept();
 	                        Channel.configureBlocking(false);
 	                        Channel.register(SocketSelector, SelectionKey.OP_READ);
 	                    }else if (Key.isReadable()) {
 	                        SocketChannel Channel = (SocketChannel) Key.channel();
 	                        WebServerSession Session = (WebServerSession) Key.attachment();
 	                        if (Session == null) {
 	                        	Session = new WebServerSession(Channel);
 	                            Key.attach(Session);
 	                        }
 	                        Session.readData();
 	                        ClientSocket = Channel.socket();
 							
 	                        this.Log.finest("Connected by " + ClientSocket.getRemoteSocketAddress().toString().substring(1).split(":")[0]  + ".");
 							
 							WebServerRequestThread Handler = new WebServerRequestThread(Session);
 						    (new Thread(Handler)).run();
 							
 						    Session.close();
 						    ClientSocket.close();
 						    Channel.close();
 
 						    if (Handler.requestedStop())
 						    	stop = true;
 						    if (Handler.requestedRestart()) {
 						    	stop = true;
 						    	this.setRestart(true);
 						    }
 	                    }
 					}catch(Exception e){
                         if (Key.attachment() instanceof WebServerSession) {
                             ((WebServerSession) Key.attachment()).close();
                         }
 					}
 				}
 
 			} 
 			catch (IOException e) {
				System.err.println("Error while accepting connections.");
 				e.printStackTrace();
 			    stop = true;
 			}
 		}
 
 		try {
 			Socket.close();
 			SocketChannel.close();
 		} catch (IOException e) {
 
 		}
 	}
 
 	public boolean requestedRestart() {
 		return restart;
 	}
 
 	public void setRestart(boolean restart) {
 		this.restart = restart;
 	}
 }
