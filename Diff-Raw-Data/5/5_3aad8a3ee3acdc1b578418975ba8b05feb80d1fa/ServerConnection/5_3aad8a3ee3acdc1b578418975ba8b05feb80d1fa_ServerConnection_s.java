 package server;
 
 import java.net.Socket;
 import java.util.Calendar;
 
 import main.Connection;
 import main.Packet;
 
 
 
 public class ServerConnection extends Connection {
 	// Commented out fields inherited from Connection:
 	
 	// public String username;
 	// public Socket socket;
 	private int userId;
 	private Server server;
 
 	public ServerConnection(int userId, Socket sock, Server server) {
 		super(sock);
 		this.userId = userId;
 		this.server = server;
 	}
 
 	public void processMessage(Packet message){
 		Packet response;
 		System.out.println(message.getCommand().name() + " " + message.getChannelName());
 		switch(message.getCommand()){
 		case JOIN:
 			// This user joins appropriate channel
 			System.out.println("CALLING addUserToChannel with: " + String.valueOf(this.userId) +" " + message.getChannelName());
 			this.server.addUserToChannel(this.userId, message.getChannelName());
 			response = new Packet(Command.REPLY_SUCCESS, "", Calendar.getInstance(), "", "");
 			sendMessage(response);
 			break;
 		case LIST_CHANNELS:
 			String channelList = this.server.getChannelList();
 			response = new Packet(Command.REPLY_LIST_CHANNELS, "", Calendar.getInstance(), channelList, "");
 			sendMessage(response);
 			break;
 		case LIST_USERS:
 			String userList = this.server.getUserList();
 			response = new Packet(Command.REPLY_LIST_USERS, "", Calendar.getInstance(), userList, "");			
 			sendMessage(response);
 			break;
 		case LOGIN:
 			String nickname = message.getMessageText();
 			this.user.setNickname(nickname);
 			response = new Packet(Command.REPLY_SUCCESS, "", Calendar.getInstance(), "", "");
 			sendMessage(response);
 			break;
 		case LOGOUT:
 			this.userDisconnected = true;	
 			// How do we deal with logout - threads will kill themselves
 			break;
 		case MESSAGE:
 		    System.out.println("Received message");
 		    System.out.println(message.getChannelName() + " " + message.getMessageText());
			this.server.sendMessageToChannel(this.user, message);
 			break;
 		case QUIT:
			this.server.removeUserFromChannel(this.user, message.getChannelName());
 			break;
 		default:
 			System.out.println("Fell through");
 			break;
 			
 		}
 	}
 	
 	public void processUserDisconnect(){
 		this.server.notifyServerOfUserDisconnect(this.userId);
 	}
 	
 	public void closeSockets(){
 		try{
 			// Stop handling IO
 			this.readerThread.interrupt();
 			this.writerThread.interrupt();
 			
 			// Process disconnect and clean up sockets
 			this.socket.close();
 		}
 		catch(Exception e){
 			System.err.println("Error attempting to process disconnect for user");
 			e.printStackTrace();
 		}
 	}
 }
