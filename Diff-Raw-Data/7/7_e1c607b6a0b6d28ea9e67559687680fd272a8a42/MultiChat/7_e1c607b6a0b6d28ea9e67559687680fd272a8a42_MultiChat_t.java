 package week4;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.*;
 import multicast.*;
 //import ChatListener;
 import week4.multicast.ChatQueue;
 
 
 /**
  *
  * @author Martin
  */
 public class MultiChat {
 	private int port = 1337;
 	private final ChatQueue queue = new ChatQueue();
 	private ChatListener listener;
 	
 	public MultiChat(){
 		try{
 			initServer();
 			start();
 		}catch(UnknownHostException e){
 			System.err.println("Fix y0 host kthxplz");
 		}catch(IOException e){
 			System.err.println("Server IOException y0");
 			e.printStackTrace();
 		}
 	}
 	
 	public MultiChat(String host){
 		initMultiChat(host,port,port);
 	}
 	
 	public MultiChat(String host, int serverPort){
 		initMultiChat(host,port,serverPort);
 	}
 	
 	public MultiChat(String host, int ownPort, int serverPort){
 		initMultiChat(host, ownPort, serverPort);
 	}
 	
 	public void initMultiChat(String host, int ownPort, int serverPort){
 		try{
 			initClient(host,ownPort,serverPort);
 			start();
 		}catch(BindException e){
 			System.err.println("You cannot run multiple clients on the same computer, since the port ("+ownPort+") is already in use!");
 			e.printStackTrace();
 			System.exit(0);
 		}catch(IOException e){
 			System.err.println("IOException y0");
 			e.printStackTrace();
 		}
 	}
 	
 	private void start(){
 		listener = new ChatListener(queue);
 		listener.start();
 		
 		listen();
 	}
 			
 	public static void main(String[] args){
 		MultiChat mc;
 		if(args.length == 3){
			int oPort = Integer.parseInt(args[1]);
			int sPort = Integer.parseInt(args[2]);
 			mc = new MultiChat(args[0],oPort,sPort);
 		}
 		else if(args.length == 2){
			int sPort = Integer.parseInt(args[1]);
 			mc = new MultiChat(args[0],sPort);
 		}
 		else if (args.length == 1)
 			mc = new MultiChat(args[0]);
 		else
 			mc = new MultiChat();
 	}
 	
 	private void listen() {
 		System.out.println("Lets get this chat rollin'!");
 		String msg;
 		Scanner in = new Scanner(System.in);
 		while (true) {
 			if ((msg = in.nextLine()) != null) {
 				if (msg.toLowerCase().equals("exit")) {
 					queue.leaveGroup();
 					listener.interrupt();
 					break;
 				} else {
 					queue.put(msg);
 				}
 			}
 		}
 		System.exit(0);
 	}
 	
 	/**
 	 * Init client with host and port
 	 * @param String host of known peer
 	 * @param int port of known peer
 	 * @throws IOException 
 	 */
 	private void initClient(String host, int oPort, int sPort) throws IOException {
 		System.out.println("Joining TrollFace-group~");
 		queue.joinGroup(oPort, new InetSocketAddress(host, sPort), MulticastQueue.DeliveryGuarantee.NONE);
 	}
 	
 	/**
 	 * Init client with default port
 	 * @param String host of known peer
 	 * @throws IOException 
 	 */
 	private void initClient(String host) throws IOException {
 		initClient(host,port,port);
 	}
 	
 	private void initServer() throws UnknownHostException, IOException{
 		System.out.println("Creating TrollFace-group~");
 		
 		queue.createGroup(port, MulticastQueue.DeliveryGuarantee.FIFO);
 		
 		queue.put(motd);
 	}
 	
 	private String motd = 
 			"\n░░░░░▄▄▄▄▀▀▀▀▀▀▀▀▄▄▄▄▄▄░░░░░░░░\n" +
 			"░░░░░█░░░░▒▒▒▒▒▒▒▒▒▒▒▒░░▀▀▄░░░░\n" +
 			"░░░░█░░░▒▒▒▒▒▒░░░░░░░░▒▒▒░░█░░░\n" +
 			"░░░█░░░░░░▄██▀▄▄░░░░░▄▄▄░░░░█░░\n" +
 			"░▄▀▒▄▄▄▒░█▀▀▀▀▄▄█░░░██▄▄█░░░░█░\n" +
 			"█░▒█▒▄░▀▄▄▄▀░░░░░░░░█░░░▒▒▒▒▒░█\n" +
 			"█░▒█░█▀▄▄░░░░░█▀░░░░▀▄░░▄▀▀▀▄▒█\n" +
 			"░█░▀▄░█▄░█▀▄▄░▀░▀▀░▄▄▀░░░░█░░█░\n" +
 			"░░█░░░▀▄▀█▄▄░█▀▀▀▄▄▄▄▀▀█▀██░█░░\n" +
 			"░░░█░░░░██░░▀█▄▄▄█▄▄█▄████░█░░░\n" +
 			"░░░░█░░░░▀▀▄░█░░░█░█▀██████░█░░\n" +
 			"░░░░░▀▄░░░░░▀▀▄▄▄█▄█▄█▄█▄▀░░█░░\n" +
 			"░░░░░░░▀▄▄░▒▒▒▒░░░░░░░░░░▒░░░█░\n" +
 			"░░░░░░░░░░▀▀▄▄░▒▒▒▒▒▒▒▒▒▒░░░░█░\n" +
 			"░░░░░░░░░░░░░░▀▄▄▄▄▄░░░░░░░░█░░";
 }
