 package client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import network.tcp.client.TCPInputConnection;
 import network.tcp.client.TCPOutputConnection;
 import network.udp.client.UDPSocket;
 
 public class ClientMain {
 
 	/**
 	 * @param args
 	 */
 	/*
 	private static boolean ack= false;
 	private static String user="";
 	private static boolean kill= false;
 
 	 */
 	private static Socket socket = null;
 	private static UDPSocket udp=null;
 	private static TCPOutputConnection out=null;
 	private static TCPInputConnection in=null;
 	private static ClientSetup setup = null;
 	private static BufferedReader reader;
 
 	public static void main(String[] args) throws InterruptedException {
 		System.out.println("Welcome");
 
 		setup= new ClientSetup(args);
 		System.out.println("setup: "+setup.toString());
 		
 		reader = new BufferedReader(new InputStreamReader(System.in));	
 
 		//while(!ClientStatus.getInstance().isKill()){
 			try {
 				socket = new Socket(setup.getHost(), setup.getServerPort());
 
 				//ClientStatus.getInstance().setSocket(socket);
 				//start TCP connection
 				//in= new TCPInputConnection(socket);
 				//in.start();
 
 				/* UDP in this round obsolete
 				 * 
 				 * 
 				//start UDP
 				udp = new UDPSocket(setup);
 				udp.start();
 				System.out.println("UDP ready");
 				 */
 
 				//start blocking input
 				out= new TCPOutputConnection(socket,setup);
 				ClientStatus.getInstance().setConnection(out);
 				out.start();
 
 				in= new TCPInputConnection(socket,setup);
 				System.out.println("TCP ready");
 				in.run();
 
 				System.out.println("client done");
 
 			} catch (UnknownHostException e) {
 				System.out.println("ERROR: unknown hostname!");
 			} catch (ConnectException e) {
 				System.out.println("Error: Server unreachable!");
 			} catch (IOException e) {
 				System.out.println("Error:....!");
 			}
 			finally{
 				kill();
 			}
 
 			//System.out.println(">>>> Reconnecting ...");
 			//Thread.sleep(1000);
 
 		//}
 	}
 
 	public static void printToOutputstream(String query) {
 		out.printToOutputstream(query);
 	}
 
 	/*
 	public static boolean isAck(){
 		return ClientMain.ack;
 	}
 
 	public static String getUser(){
 		return ClientMain.user;
 	}
 
 	public static boolean isKill(){
 		return ClientMain.kill;
 	}
 
 	public static void setAck(boolean ack){
 		System.out.println("ack changed");
 		ClientMain.ack=ack;
 		System.out.println(ClientMain.ack+" | "+ack);
 	}
 
 	public static void setUser(String user){
 		ClientMain.user=user;
 	}
 	 */
 
 	public static synchronized void kill(){
 		try {
 			System.out.println("closing connections");
 			
 			if(in!=null)
 				in.close();
 			
 			if(out!=null)
 				out.close();
 			
 			if(socket!=null)
 				socket.close();
 
			
 			
 			//if(socket!=null)
 			//	socket.close();
 			//if(udp!=null)
 			//	udp.close();
 
 			System.out.println("connections successfully closed!\n\nHave a nice day!");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	public static ClientSetup getClientSetup() {
 		return setup;
 	}
 	
 	public static BufferedReader getReader() {
 		return reader;
 	}
 
 }
