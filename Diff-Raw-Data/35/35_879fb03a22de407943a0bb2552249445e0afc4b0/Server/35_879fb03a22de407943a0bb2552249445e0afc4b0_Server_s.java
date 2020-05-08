 package xinxat.server;
 
 /**
  * This class represents the main server that listens to every single
  * request and deals with it depending on its purpose.
  * 
  * @author Fran Hermoso <franhp@franstelecom.com>
  */
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import xinxat.server.Xmpp;
 
 @SuppressWarnings("serial")
 public class Server extends HttpServlet {
 	/**
 	 * This Map holds a Stack of messages for every user in the system
 	 */
 	private Map<String, Stack<String>> messageStack = new HashMap<String, Stack<String>>();
 	
 	/**
 	 * This Map holds a List of users for every channel
 	 */
 	private static Map<String, ArrayList<String>> rooms = new HashMap<String, ArrayList<String>>();
 	
 	/**
 	 * This Map holds a List of bannned users for every channel
 	 */
 	private static Map<String, ArrayList<String>> bans = new HashMap<String,ArrayList<String>>();
 	
 	/**
 	 * Antispam  system
 	 */
 	private static Map<String, ArrayList<Date>> antispam = new HashMap<String,ArrayList<Date>>();
 	
 	/**
 	 * Number of miliseconds it takes an spammer to write more than 5 messages
 	 */
 	private static final long spammerTime = 30000;
 	
 	
 	/**
 	 * Datastore connection
 	 */
 	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
 	/**
 	 * Logger
 	 */
 	private static final Logger log = Logger.getLogger(Server.class.getName());
 
 
 	/**
 	 * This Servlet receives all the messages and puts them on the stack.
 	 * 
 	 * In order to send a message the user must send a POST to the following URL
 	 * 		http://projecte-xinxat.appspot.com/messages
 	 * 		Add the following fields to the request
 	 * 			msg: 	contains the xmpp message 
 	 * 					type= can be defined by: chat, groupchat, system
 	 * 			token:	contains the token given by the frontend 
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
 		//Read the XMPP
 		Xmpp msg = new Xmpp(req.getParameter("msg"));
 		
 		//Process it
 		try {
 			Entity from = getUserByToken(req.getParameter("token"));
 			if(from != null 
 					&& 
 					msg.getSender().equalsIgnoreCase(from.getProperty("username").toString())){
 				
 				//What if the user is spamming?
 				updateSpammerCount(from.getProperty("username").toString());
 				if(!isSpammer(from.getProperty("username").toString())){
 						
 					//If it is a private chat
 					if(msg.getType().equals("chat")){
 						String recipient = msg.getRecipient();
 						//Get the Stack if it exists
 						Stack<String> userStack = messageStack.get(recipient);
 						//If it doesn't exist, check if the user should have an stack
 				    	if(userStack == null) {
 				    		Query q = new Query("user");
 				    		q.addFilter("username", Query.FilterOperator.EQUAL, recipient);
 							PreparedQuery pq = datastore.prepare(q);
 							for (Entity result : pq.asIterable()) {
 								String username = result.getProperty("username").toString();
 								if(username != null)
 									userStack = new Stack<String>();
 							}
 							
 				    	} 
 				    	//Now that we have a Stack, fill it!
 				    	if(userStack != null){
 					    	userStack.push(msg.getAllMessage());
 					    	messageStack.put(recipient, userStack);
 					    	resp.getWriter().println("OK");
 	
 						}
 				    	else {
 				    		resp.getWriter().println("NOEXISTS");
 				    	}
 					} 
 					
 					//If it is a groupchat
 					else if (msg.getType().equals("groupchat")){
 						String destChannel = msg.getRecipient();
 						//if(!getUsersFromRoom(destChannel).contains(msg.getSender())){
 						if(!listRooms().contains(destChannel)){
 							resp.getWriter().println("CANT");
 						}
 						else {
 							ArrayList<String> peopleInRoom = getUsersFromRoom(destChannel);
 							for (String user : peopleInRoom){
 								Stack<String> pila_usuari = messageStack.get(user);
 								//If it doesn't exist, check if the user should have an stack
 						    	if(pila_usuari == null) {
 						    		Query q = new Query("user");
 						    		q.addFilter("username", Query.FilterOperator.EQUAL, user);
 									PreparedQuery pq = datastore.prepare(q);
 									for (Entity result : pq.asIterable()) {
 										String username = result.getProperty("username").toString();
 										if(username != null)
 											pila_usuari = new Stack<String>();
 									}	
 						    	}
 						    	//Now that we have a Stack, fill it!
 						    	if(pila_usuari != null){
 							    	pila_usuari.push(msg.getAllMessage());
 							    	messageStack.put(user, pila_usuari);
 							    	resp.getWriter().println("OK");
 						    	}
 						    	else {
 						    		resp.getWriter().println("NOEXISTS");
 						    	}
 							}
 						}
 					}
 					//If it is a system message
 					else if(msg.getType().equals("system")){
 						String command = msg.getBody();
 						String[] params = command.split(" ");
 						
 						// /join room
 						if(params[0].equals("/join")){
 							if(roomIsPrivate(params[1])){
 								if(!isBanned(msg.getSender(), params[1])){
 									addUserToRoom(msg.getSender(), params[1]);
 									resp.getWriter().println("OK");
 								}
 								else if(!isAllowedToJoin(msg.getSender(),params[1])){
 									log.info("[" + msg.getSender() + "] was trying to join [" + params[1] + "+] but couldn't");
 									sendMessage(msg.getSender(), "You cannot enter this room");
 									resp.getWriter().println("CANT");
 								}
 							}
 							else if(banlist(params[1])!=null){
 								if(banlist(params[1]).contains(msg.getSender())){
 									log.info("[" + msg.getSender() + "] was trying to join [" + params[1] + "+] but couldn't");
 									sendMessage(msg.getSender(), "You cannot enter this room");
 									resp.getWriter().println("CANT");
 								}
 								else{
 									addUserToRoom(msg.getSender(), params[1]);
 									resp.getWriter().println("OK");
 								}
 							}
 							else {
 								addUserToRoom(msg.getSender(), params[1]);
 								resp.getWriter().println("OK");
 							}
 							
 							
 						}
 						// /leave
 						else if(params[0].equals("/leave")){
 							deleteUserFromRoom(msg.getSender(), msg.getRecipient());
 							resp.getWriter().println("OK");
 						}
 						// /list
 						else if(params[0].equals("/list")){
 							resp.getWriter().println(getUsersFromRoom(params[1]));
 						}
 						// /invite
 						else if(params[0].equals("/invite")){
 							addUserToRoom(params[1], msg.getRecipient());
 							resp.getWriter().println("OK");
 						}
 						// /ban username reason
 						else if(params[0].equals("/ban")){
 							sendMessage(msg.getRecipient(), params[1] + "was banned because: " + params[2]);
 							deleteUserFromRoom(params[1], msg.getRecipient());
 							ban(params[1],msg.getRecipient());
 							resp.getWriter().println("OK");
 						}
 						// /unban username
 						else if(params[0].equals("/unban")){
 							unban(params[1],msg.getRecipient());
 							resp.getWriter().println("OK");
 						}
 						else if(params[0].equals("/banlist")){
 							for (String banned : banlist(msg.getRecipient())){
 								resp.getWriter().println("\n" + banned + "is banned");
 							}
 						}
 						// /kick username reason
 						else if(params[0].equals("/kick")){
 							sendMessage(msg.getRecipient(), params[1] + " was kicked because: " + params[2]);
 							deleteUserFromRoom(params[1], msg.getRecipient());
 							resp.getWriter().println("OK");
 						}
 						// /info username
 						else if(params[0].equals("/info")){
 							sendMessage(msg.getSender(), userInfo(params[1]));
 						}
 						
 					}
 				}
 				else {
 					resp.getWriter().println("SPAM");
 				}
 			}
 			else {
 				resp.getWriter().println("WRONG");
 			}
 		}
 		catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e){
 			resp.getWriter().println("NOEXISTS");
 		}
 
     }
 	
 
 
 	/**
 	 * This servlet outputs the pending messages still on the stack and
 	 * also sets the presence
 	 * 
 	 * In order to receive the messages the user must send a GET request to the following URL:
 	 * 			http://projecte-xinxat.appspot.com/messages
 	 * 			status: contains the status message (optional)
 	 * 			show:	contains one of the following: chat, dnd, away, offline (optional)
 	 * 			to:		contains the requestor's username
 	 * 			token:	contains the token given by the frontend 
 	 */
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
     		throws IOException {
     	Entity from = getUserByToken(req.getParameter("token"));
     	String recipient = req.getParameter("to");
 		if(from != null && recipient.equals(from.getProperty("username"))){
 			//Get the stack of pending messages from the map
 	    	Stack<String> msgStack = messageStack.get(recipient);
 	    	if(msgStack == null) msgStack = new Stack<String>();
 	    	
 	    	//Set the presence of the user
 			String show = req.getParameter("show");
 			String status = req.getParameter("status");
 			
 			if(show != null && status != null){
 				Query q = new Query("user");
 				q.addFilter("username", Query.FilterOperator.EQUAL, recipient);
 				PreparedQuery pq = datastore.prepare(q);
 				
 				//Write the presence on the datastore
 				Entity user = new Entity("user");
 				for (Entity result : pq.asIterable()) {
 					user.setProperty("username", result.getProperty("username"));
 					user.setProperty("password", result.getProperty("password"));
 					user.setProperty("show", show);
 					user.setProperty("status", status);
 					long lastonline = System.currentTimeMillis() / 1000L;
 					user.setProperty("lastonline", lastonline);
 					datastore.delete(result.getKey());
 				}
 				datastore.put(user);
 			}
 			
 			//Return presence if no messages are unread
 	    	if(msgStack.isEmpty()){
 	    			resp.setContentType("text/plain");
 	    			resp.getWriter().println("<presence xml:lang=\"en\">" +
 	    	    			"\n\t<show>" + show + "</show>" +
 	    	    			"\n\t<status>" + status + "</status>" +
 	    	    			"\n</presence>");
 	    	}
 	    	//Return the messages
 		    else {
 		    	//Reverse the Stack
 		    	Stack <String> reversedStack = new Stack<String>();
 		    	while(!msgStack.isEmpty()){
 		    		String message = msgStack.pop();
 		    		reversedStack.push(message);
 		    	}
 		    	
 		    	//Output the Stack
 		    	resp.setContentType("text/plain");
 		    	resp.getWriter().println("<?xml version=\"1.0\"?>\n" +
 		    								"<messages>\n");
 		    	while (!reversedStack.isEmpty()){
 		    		resp.getWriter().println( reversedStack.pop());
 		    	}
 		    	resp.getWriter().println("</messages>\n");
 		    }
 
 		}
 		else {
 			resp.getWriter().println("WRONG");
 		}
     }
     
     /**
      * Returns a list of the users in a room
      * 
      * @param room
      * @return ArrayList of users
      */
 	public static ArrayList<String> getUsersFromRoom(String room){
 		return rooms.get(room);
 	}
 	
 	
 	/**
 	 * Adds a user to a room
 	 * 
 	 * @param user
 	 * @param room
 	 */
 	public static void addUserToRoom(String user, String room){
 		ArrayList<String> users = new ArrayList<String>();
 		try{
 			if(!rooms.get(room).equals(null)){
 				for(String otherUser: rooms.get(room)){
 					if(!otherUser.equalsIgnoreCase(user))
 						users.add(otherUser);
 				}
 			}
 		} catch (NullPointerException e){
 			log.info("Created room [" + room + "]");
 		}
 		users.add(user);
 		rooms.put(room, users);
 		log.info("[" + user + "] joined [" + room + "]");
 	}
 	
 	/**
 	 * Deletes a user from a room
 	 * 
 	 * @param user
 	 * @param room 
 	 */
 	public static void deleteUserFromRoom(String user, String room){
 		ArrayList<String> users = new ArrayList<String>();
 		for(String otherUser: rooms.get(room))
 			if(!otherUser.equals(user))
 				users.add(otherUser);
 		rooms.put(room, users);
 		log.info("[" + user + "] left [" + room + "]");
 	}
 	
 	/**
 	 * Deletes all the rooms
 	 */
 	public static void resetRooms() {
 		rooms.clear();
 		log.warning("Rooms have been reset");
 	}
 
 	/**
 	 * Checks if the user exists on the datastore
 	 * 
 	 * @param token password created by the frontend
 	 * @return User's entity
 	 */
 	private Entity getUserByToken(String token){
 		Query query = new Query("user");
 		query.addFilter("password", Query.FilterOperator.EQUAL, token);
 		PreparedQuery pquery = datastore.prepare(query);
 		if(pquery.countEntities(FetchOptions.Builder.withDefaults()) >= 1) {
 			for(Entity user : pquery.asIterable()){
 				return user;
 			}
 		}
 		log.info("Failed auth for token [" + token + "]" );
 		return null;
 	}
 	
 	/**
 	 * Bans a user from a room
 	 * 
 	 * @param who the user to be banned
 	 * @param where the room where it will be banned
 	 */
 	public static void ban(String who, String where) {
 		ArrayList<String> banned = null;
 		try{
 			banned = bans.get(where);
 			if(!banned.contains(who))
 				banned.add(who);
 		} catch (NullPointerException e){
 			bans.put(where, new ArrayList<String>());
 			banned = bans.get(where);
 			banned.add(who);
 		} finally {
 			bans.put(where, banned);
 			log.info("[" + who + "] was banned from [" + where + "]");
 		}
 		
 	}
 	
 	public static void resetBans(){
 		bans.clear();
 		log.warning("Bans were reset!");
 	}
 
 	/**
 	 * Unbans a user from a room
 	 * 
 	 * @param who the user to be unbanned
 	 * @param where the room where it will be unbanned
 	 */
 	public static void unban(String who, String where) {
 		ArrayList<String> banned = bans.get(where);
 		banned.remove(who);
 		bans.put(where, banned);
 		log.info("[" + who + "] was unbanned from [" + where + "]");
 		
 	}
 	
 	/**
 	 * List the rooms in the server
 	 * @return room list
 	 */
 	public static ArrayList<String> listRooms(){
 		ArrayList<String> list = new ArrayList<String>();
 		Set<String> set = rooms.keySet();
 		String[] strkeys = set.toArray(new String[set.size()]);
 		Arrays.sort(strkeys);
 		for(int i = 0; i < set.size(); i++){
 			list.add(strkeys[i]);
 		}
 		return list;
 	}
 
 
 
 
 	/**
 	 * Sends a message to a user as system
 	 * 
 	 * @param to the recipient user
 	 * @param message the actual message
 	 */
 	private void sendMessage(String to, String message) {
 		String msg = "<message to=\"" + to + "\" type=\"system\">\n" +
 							"<body>" + message + "</body>\n" +
 						"</message>";
 		
 		try {
 			ArrayList<String> peopleInRoom = rooms.get(to);
 			for (String user : peopleInRoom){
 				Stack<String> pila_usuari = messageStack.get(user);
 		    	//Now that we have a Stack, fill it!
 		    	if(pila_usuari != null){
 			    	pila_usuari.push(msg);
 			    	messageStack.put(user, pila_usuari);
 		    	}
 			}
 		} catch (NullPointerException e){
 			//The room doesn't exist
 		}
 		
 	}
 
 	/**
 	 * Lists the rooms that a user is in
 	 * 
 	 * @param username
 	 * @return roomlist
 	 */
 	private String userInfo(String username) {
 		String roomlist = "";
 		ArrayList<String> rooms = listRooms();
 		for(String room : rooms){
 			ArrayList<String> users = getUsersFromRoom(room);
 			for(String user: users){
 				if(user.equals(username)){
 					roomlist += room + ",";
 				}
 			}
 		}
 		return "User " + username + " can be found in: " + roomlist;
 	}
 
 	
 	/**
 	 * Shows a list of bans from a room
 	 * 
 	 * @param room
 	 * @return
 	 */
 	public static ArrayList<String> banlist(String room) {
 		return bans.get(room);
 	}
 
 
 
 	/**
 	 * Checks if a user is banned from a certain room
 	 * 
 	 * @param user
 	 * @param room
 	 * @return boolean
 	 */
 	private boolean isBanned(String user, String room) {
 		ArrayList<String> banned = null;
 		try{
 			banned = bans.get(room);
 		} catch (NullPointerException e){
 			bans.put(room, new ArrayList<String>());
 			banned = bans.get(room);
 		} 
 		try{
 			if(banned.contains(user)){
 				return true;
 			}
 			else {
 				return false;
 			}
 		} catch (NullPointerException e){
 			return false;
 		}
 	}
 
 	
 	/**
	 * Returns true if the room name starts with #
 	 * @param room
 	 * @return
 	 */
 	private boolean roomIsPrivate(String room) {
		if(room.startsWith("#")) return true;
 		else return false;
 	}
 
 	/**
 	 * Shows if a user has the permission to enter a room
 	 * 
 	 * @param user
 	 * @param room
 	 * @return
 	 */
 	private boolean isAllowedToJoin(String user, String room){
 		ArrayList<String> roomUsers = rooms.get(room);
 		if(roomUsers.contains(user)) return true;
 		else return false;
 		
 	}
 	
 
 	/**
 	 * Updates the spammer count of the user
 	 * 
 	 * @param username
 	 */
 	private void updateSpammerCount(String username) {
 		ArrayList<Date> times = antispam.get(username);
 		try {
 			ArrayList<Date> timesResult = new ArrayList<Date>();
 			timesResult.add(new Date());
 			if(times.size() > 5){
 				for(int i = 0; i<5; i++){
 					timesResult.add(times.get(i));
 				}
 			}
 			else {
 				timesResult.addAll(times);
 			}
 			antispam.put(username, timesResult);
 		}
 			catch (NullPointerException e)
 		{
 			ArrayList<Date> time = new ArrayList<Date>();
 			time.add(new Date());
 			if(times != null) time.addAll(times);
 			antispam.put(username, time);
 		}
 				
 		
 	}
 
 
 	/**
 	 * Defines if the user is a spammer or not
 	 * @param user
 	 * @return
 	 */
 	private boolean isSpammer(String user) {
 		if(user.equals("testuser")) return false;
 		try{
 			ArrayList<Date> times = antispam.get(user);
 			Long first = times.get(0).getTime();
 			Long last = times.get(5).getTime();
 			if((first-last) < spammerTime) {
 				log.warning("[" + user + "] is spamming!");
 				return true;
 			}
 			else return false;
 		} catch (NullPointerException e ){
 			return false;
 		} catch (IndexOutOfBoundsException e){
 			return false;
 		}
 		
 
 	}
 
 
 
 
 
 }
