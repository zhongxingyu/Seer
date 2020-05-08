 import java.rmi.*;
 import java.rmi.server.*;
 import java.io.*; 
 
 public class RMIClient{
 
 	private String message = "";
 	private boolean firstMessage = true;
 	private static String name;
 	private static String hostname;
 	private BufferedReader input; 
 	private PrintStream output; 
 
 	RMINotifyInterface displayChat;
 	RMIServerInterface server;
 
 	public RMIClient(){
 		input = new BufferedReader(new InputStreamReader(System.in)); 
 		output = System.out;
 	}
 
 	public void run(){
 		try {
			System.out.print("hostname: ");
			hostname = input.readLine();
 			
 			//BUILD HOST ADDRESS HERE
 			Remote remoteObject = Naming.lookup("rmi://" + hostname + "/drawIt");
 
 			if (remoteObject instanceof RMIServerInterface) {
 				server = (RMIServerInterface)remoteObject ;
 				displayChat = new RMIDisplayHandler(output);
 			}
 			else{
 				System.out.println("I didn't find the RMI server I was looking for.");
 				System.exit(0);
 			}
 		}
 		catch(Exception e){
 			System.out.println("RMI Lookup Exception");
 			System.exit(0);
 		};    
 
 		try{
 			String message = "";
			System.out.print("username: ");
 			while((message = readMessage()) != null){
 				handleMessage(message);
 			}	
 		}
 		catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 		finally {
 			try {
 
 				server.leave(displayChat);
 
 				output.close();
 				input.close();
 			}
 			catch (IOException e) {
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 
 	public String readMessage() {
 		try{
 			String line = input.readLine().trim();
 			if(line.equals(""))
 				return null;
 			return line;
 		}
 		catch (IOException e){
 			System.err.println("IO Error"); 
 			return null;
 		}
 	}	
 
 	private void handleMessage(String message) {
 		try{
 			if (firstMessage) {
 				name = message;
 				displayChat.setName(message);
 				server.join(displayChat);
 				firstMessage = false;
 			}
 			else {
 				if(message.startsWith("chat:")){
 					message = message.substring(5, message.length());
 					server.broadcastExcept(displayChat, message, name);
 				}
 				else if(message.trim().equals("exit")){
 					server.leave(displayChat);
 					
 					input.close();
 					output.close();
 					
 					System.exit(0);
 				}
 				else
 					server.broadcastAll(displayChat, message);
 			}
 		}
 		catch (Exception ie) {
 			output.append("Failed to send message.");
 		}
 
 	}
 
 
 	public static void main(String args[]) {
 		new RMIClient().run();
 	}
 
 }
