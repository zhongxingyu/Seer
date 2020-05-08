 package ar.edu.it.itba.pdc.v2.implementations.configurator;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.ws.rs.core.MediaType;
 
 import ar.edu.it.itba.pdc.v2.interfaces.Configurator;
 import ar.edu.it.itba.pdc.v2.interfaces.ConnectionHandler;
 
 public class ConfiguratorImpl implements Configurator {
 
 	private int port = 9092;
 	private static int maxMessageLength = 512;
 	
 	private ConfiguratorConnectionHandler handler;
 	private ConfiguratorConnectionDecoder decoder;
 
 	public ConfiguratorImpl() {
 		this.decoder = new ConfiguratorConnectionDecoder();
 		this.handler = new ConfiguratorConnectionHandler(maxMessageLength, decoder);
 	}
 	
 	public void run() {
 		ServerSocket socketServer = null;
 		try {
 			socketServer = new ServerSocket(port);
 		} catch (IOException e) {
 			System.out
 					.printf("Could not initiate Configurator. Proxy will run without it. Please check that the port %d is available\n",
 							port);
 			return;
 		}
 		
 		while(true) {
 			Socket socket;
 			try {
 				socket = socketServer.accept();
 				handler.handle(socket);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean applyRotations() {
 		return handler.applyRotations();
 	}
 	
 	public boolean applyTextTransformation() {
		return handler.applyTextTransformation()
 	}
 	
 	public int getMaxSize() {
 		return handler.getMaxSize();
 	}
 	
 	public boolean isAccepted(InetAddress addr) {
 		return handler.isAccepted(addr);
 	}
 	
 	public boolean isAccepted(String str) {
 		return handler.isAccepted(str);
 	}
 	
 	public boolean isAccepted(MediaType str) {
		return handler.isAccepted(mtype);
 	}
 }
