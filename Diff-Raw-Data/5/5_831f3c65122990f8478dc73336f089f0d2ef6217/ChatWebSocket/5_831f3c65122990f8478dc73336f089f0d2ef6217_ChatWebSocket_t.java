 /**
  * Fall 2011 - 15-437
  * Tyler Huberty
  * Jack Phelan
  * 
  * Chat Web Socket
  */
 
 package demo.controller;
 
 import java.io.IOException;
 import net.sf.json.JSONObject;
 import org.eclipse.jetty.websocket.WebSocket;
 
 
 /**
  * This class represents an open web socket. Each instance of this class belongs
  * to a chat member. This class contains the methods invoked by the server for
  * changes in state to the web socket. This class takes first action on these 
  * changes by dispatching to controller actions and altering the state of the
  * appropriate chat member. This class is perhaps the highest level controller
  * since most communication is done from the client to the server via a web socket.
  * 
  * Objects of this class hold the state of the socket, such as the connection and
  * the chat member it belongs to.
  */
 public class ChatWebSocket implements WebSocket.OnTextMessage
 {
 	volatile private Connection _connection;
 
 	private ChatMember member;
 
 	public ChatMember getMember() {
 		return member;
 	}
 
 	public void setMember(ChatMember member) {
 		this.member = member;
 	}
 
 	/** 
 	 * Callback for when a WebSocket connection is opened.
 	 * Stores the connection for future use and adds a socket to the chat member.
 	 */
 	public void onOpen(Connection connection)
 	{
 		_connection=connection;
 		member.addSocket(this);
 
 		// check if user authenticated
 		onMessage(null);
 	}
 
 
 	/** 
 	 * Callback for when a WebSocket connection is closed.
 	 * 
 	 * Inform chat member that the socket should be removed.
 	 * If this is the only open socket, that chat member will subsequently destroy itself.
 	 */
 	public void onClose(int closeCode, String message)
 	{
 		member.removeSocket(this);
 	}
 
 	/** 
 	 * Callback for when a WebSocket message is received.
 	 * 
 	 * Dispatch handling of the requests to the appropriate action.
 	 * This is a central controller hub.
 	 */
 	public void onMessage(String data)
 	{
 		Message message;
		System.out.println(data);
 		if (data != null) message = (Message) JSONObject.toBean(new JSONObject().fromObject(data), Message.class);
 		else message = Message.getDefaultMessage();
 		
 		// prevent XSS, etc.
 		message.escapeHTML();
 		
 		if (message.getHeader() == null || message.getBody() == null || message.getSender() == null) {
 			// must be forged
 			return;
 		}
 		
 		//----------------------------------------------
 		// This is a request for all user's preferences
 		//----------------------------------------------
 		if (message.getHeader().equalsIgnoreCase("cloud")) {
 			new CloudAction().perform(member, message);
 			return;
 		}
 		//----------------------------------------------
 		// This is a request to logout
 		//----------------------------------------------
 		if (message.getHeader().equalsIgnoreCase("logout")) {
 			new LogoutAction().perform(member, message);
 			return;
 		}
 		
 		//----------------------------------------------
 		// Member not authenticated
 		//----------------------------------------------
 		if (!member.isAuthenticated()) {
 			System.out.println("not authenticated");
 			//user is responding to register or login prompt
 			if (message.getHeader().equals("register-login")){
 				if (message.getBody().trim().equalsIgnoreCase("login"))
 					new LoginPromptAction().perform(member, message);
 				else if (message.getBody().trim().equalsIgnoreCase("register"))
 					new RegisterPromptAction().perform(member, message);
 				else {
 					//send them the register or login prompt
 					Message m = new Message();
 					m.setBody("Welcome! Please type one of: [register/login]");
 					m.setHeader("register-login");
 					m.setSender("system");
 					send(m);
 				}
 			} else if (message.getHeader().startsWith("login")) {
 				new LoginAction().perform(member, message);
 			} else if (message.getHeader().startsWith("register")) {			
 				new RegisterAction().perform(member, message);
 			}
 		}
 		else {
 			//----------------------------------------------
 			// This is a preference being pushed to server
 			//----------------------------------------------
 			if (message.getHeader().equalsIgnoreCase("preference")) {
 				new PreferAction().perform(member, message);
 				return;
 			}
 			//----------------------------------------------
 			// Member is waiting for a partner
 			//----------------------------------------------
 			if (member.getPartner() == null) {
 				new PartnerlessAction().perform(member, message);
 				return;
 			}
 			//----------------------------------------------
 			// Member has a partner
 			//----------------------------------------------
 			if (data != null) {
 				new ChatAction().perform(member, message);
 				return;
 			}
 			// default in case of new session or forged
			message = new Message();
			message.setSender("system");
 			message.setHeader("chat");
 			message.setBody("You've created a duplicate window and may continue chatting.");
 			member.sendMessage(message);
 		}
 	}
 
 	/**
 	 * To send a message to client via this socket, this method should be used.
 	 * 
 	 * Converts message to JSON before sending.
 	 */
 	public void send(Message message) {
 		JSONObject json = JSONObject.fromObject(message);
 
 		try {
 			_connection.sendMessage(json.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
