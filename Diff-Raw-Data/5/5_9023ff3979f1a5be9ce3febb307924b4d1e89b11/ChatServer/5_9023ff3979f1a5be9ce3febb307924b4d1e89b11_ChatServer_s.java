 package edu.berkeley.cs.cs162;
 
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.sql.ResultSet;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 /**
  * This is the core of the chat server.  Put the management of groups
  * and users in here.  You will need to control all of the threads,
  * and respond to requests from the test harness.
  *
  * It must implement the ChatServerInterface Interface, and you should
  * not modify that interface; it is necessary for testing.
  */
 
 public class ChatServer extends Thread implements ChatServerInterface {
 
 	private BlockingQueue<User> waiting_users;
 	private Map<String, User> users;
 	private Map<String, ChatGroup> groups;
 	private Set<String> onlineNames;
 	private Set<String> registeredUsers;
 	private ReentrantReadWriteLock lock;
 	private volatile boolean isDown;
 	private final static int MAX_USERS = 100;
 	private final static int MAX_WAITING_USERS = 10;
 	private final static long TIMEOUT = 20;
 	private ServerSocket mySocket;
 	private ExecutorService pool;
 	
 	public ChatServer() {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		onlineNames = new HashSet<String>();
 		registeredUsers = new HashSet<String>();
 		lock = new ReentrantReadWriteLock(true);
 		waiting_users = new ArrayBlockingQueue<User>(MAX_WAITING_USERS);
 		isDown = false;
 		
 	}
 	
 	public ChatServer(int port) throws IOException {
 		this();
 		pool = Executors.newFixedThreadPool(1000);
 		try {
 			mySocket = new ServerSocket(port);
 		} catch (Exception e) {
 			throw new IOException("Server socket creation failed");
 		}
 		try {
 			initStructures();
 		} catch (Exception e){
 			e.printStackTrace();
 			return;
 		}
 		this.start();
 	}
 	
 	private void initStructures() throws Exception {
 		//initialize registeredUsers
 		ResultSet Usernames = DBHandler.getUsers();
 		while(Usernames.next()) {
 			String s = Usernames.getString("username");
 			registeredUsers.add(s);
 		}
 		
 		//initialize groups as well as add group names to onlineNames
 		ResultSet Groupnames = DBHandler.getGroups();
 		while(Groupnames.next()) {
 			String g = Groupnames.getString("gname");
 			onlineNames.add(g);
 			groups.put(g, new ChatGroup(g));
 		}
 		
 		ResultSet Members = DBHandler.getMemberships();
 		while(Members.next()) {
 			String u = Members.getString("username"); 
 			String g = Members.getString("gname");
 			ChatGroup group = groups.get(g);
 			if(group != null)
 				group.addUser(u);
 		}
 	}
 	
 	public boolean isDown() { return isDown; }
 	
 	@Override
 	public BaseUser getUser(String username) {
 		BaseUser u;
 		lock.readLock().lock();
 		u = users.get(username);
 		lock.readLock().unlock();
 		return u;
 	}
 	
 	public ChatGroup getGroup(String groupname) {
 		ChatGroup group;
 		lock.readLock().lock();
 		group = groups.get(groupname);
 		lock.readLock().unlock();
 		return group;
 	}
 	
 	public Set<String> getGroups() {
 		Set<String> groupNames;
 		lock.readLock().lock();
 		groupNames = this.groups.keySet();
 		lock.readLock().unlock();
 		return groupNames;
 	}
 	
 	public Set<String> getActiveUsers() {
 		Set<String> userNames;
 		lock.readLock().lock();
 		userNames = users.keySet();
 		lock.readLock().unlock();
 		return userNames;
 	}
 	
 	public Set<String> getAllUsers() {
 		return registeredUsers;
 	}
 	
 	public int getNumUsers(){
 		int num;
 		lock.readLock().lock();
 		num = users.size();
 		lock.readLock().unlock();
 		return num;
 	}
 	
 	public int getNumGroups(){
 		int num;
 		lock.readLock().lock();
 		num = groups.size();
 		lock.readLock().unlock();
 		return num;
 	}
 	
 	private void initUserGroups(User u){
 		ResultSet rs = DBHandler.getUserMemberships(u.getUsername());
 		try {
 			System.out.println("initiating " + u.getUsername());
 			while(rs.next()){
 				ChatGroup group = groups.get(rs.getString("gname"));
 				group.addLoggedInUser(u.getUsername(), u);
 				u.addToGroups(group.getName());
 			}
 			System.out.println("done initiating " + u.getUsername());
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public ServerReply addUser(String username, String password){
 		lock.writeLock().lock();
 		Set<String> allNames = new HashSet<String>();
 		allNames.addAll(onlineNames);
 		allNames.addAll(registeredUsers);
 		if(allNames.contains(username)) {
 			lock.writeLock().unlock();
 			return ServerReply.REJECTED;
 		}
 		SecureRandom random = null;
 		byte[] salt = null;
 		try {
 			random = SecureRandom.getInstance("SHA1PRNG");
 			salt = new byte[2];
 			random.nextBytes(salt);
 		} catch (NoSuchAlgorithmException e1) {
 			System.err.println("no PRNG algorithm");
 		}
 		String hash = hashPassword(password, salt);
 
 		System.out.println("creating salt: " + salt + "hash: " + hash);
 		
 		try {
 			DBHandler.addUser(username, salt, hash);
 			System.out.println("salt check: " + DBHandler.getSalt(username));
 		} catch(Exception e) {
 			lock.writeLock().unlock();
 			e.printStackTrace();
 			return ServerReply.REJECTED;
 		}
 		registeredUsers.add(username);
 		lock.writeLock().unlock();
 		return ServerReply.OK;
 	}
 	
 	public void readlog(String username) throws SQLException{
 		lock.readLock().lock();
 		List<Message> unsentMessages = DBHandler.readAndClearLog(username);
 		for (Message message : unsentMessages) {
 			users.get(username).acceptMsg(message);
 		}
 		lock.readLock().unlock();
 	}
 	
 	@Override
 	public LoginError login(String username) { return null; }
 	
 	public LoginError login(String username, String password) {
 		lock.writeLock().lock();
 		System.out.println("loggedin got called");
 		if (isDown || onlineNames.contains(username) || !registeredUsers.contains(username)) {
 			TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_REJECTED);
 			lock.writeLock().unlock();
 			return LoginError.USER_REJECTED;
 		}
 		System.out.println("logged in passed rejected");
 		LoginError error = loginAttempt(username, password);
 		lock.writeLock().unlock();
 		return error;
 	}
 	
 	public LoginError loginAttempt(String username, String password) {
 		byte[] salt = null;
 		try {
 			salt = DBHandler.getSalt(username);
 			
 			String hash = hashPassword(password, salt);
 			System.out.println(salt + ":salt hash:" + hash);
 			if (hash == null || !hash.equals(DBHandler.getHashedPassword(username))) {
 				TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_REJECTED);
 				return LoginError.USER_REJECTED;			
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (users.size() >= MAX_USERS) {		//exceeds capacity
 			User newUser = new User(this, username);
 			if(waiting_users.offer(newUser)) {	//attempt to add to waiting queue 
 				onlineNames.add(username);
 				return LoginError.USER_QUEUED;
 			}
 			else {								//else drop user
 				TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_DROPPED);
 				return LoginError.USER_DROPPED;				
 			}
 		}
 		User newUser = new User(this, username);
 		users.put(username, newUser);
 		onlineNames.add(username);
 		newUser.connected();
 		TestChatServer.logUserLogin(username, new Date());
 		initUserGroups(newUser);
 		return LoginError.USER_ACCEPTED;		
 	}
 	
 	public String hashPassword(String password, byte[] salt) {
 		String hashed = null;
 		try {
 			MessageDigest md = MessageDigest.getInstance("SHA-256");
 			md.reset();
 			md.update(salt);
 		    hashed = DBHandler.byteToBase64(md.digest(password.getBytes("UTF-8")));
 		} catch (Exception e) {
 			System.err.println("oops");
 		}
 		System.out.println("HASHED!! " + hashed);
 	    return hashed;
 	}
 
 	@Override
 	public boolean logoff(String username) {
 		lock.writeLock().lock();
 		if (!users.containsKey(username)){
 			User toRemove = null;
 			for (User u : waiting_users) {
 				if(u.getUsername().equals(username)) {
 					u.logoff();
 					toRemove = u;
 				}
 			}
 			if (toRemove != null) {
 				waiting_users.remove(toRemove);
 				onlineNames.remove(toRemove.getUsername());
 				lock.writeLock().unlock();
 				return true;
 			}
 			lock.writeLock().unlock();
 			return false;
 		}
 		ResultSet rs = DBHandler.getUserMemberships(username);
 		try {
 			while(rs.next()) {
 				String g = rs.getString("gname");
 				ChatGroup c = groups.get(g);
 				if(c != null)
 					c.removeLoggedInUser(username);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		users.get(username).logoff();
 		onlineNames.remove(username);
 		users.remove(username);
 		
 		// Check for waiting users
 		User newUser = waiting_users.poll();
 		if (newUser != null) {							//add to ChatServer
 			String newUsername = newUser.getUsername();
 			users.put(newUsername, newUser);
 			TransportObject reply = new TransportObject(Command.login, ServerReply.OK);
 			newUser.queueReply(reply);
 			newUser.connected();
 			TestChatServer.logUserLogin(newUsername, new Date());
 			initUserGroups(newUser);
 		}
 		
 		lock.writeLock().unlock();	
 		return true;
 	}
 	
 	public void joinAck(User user, String gname, ServerReply reply) {
 		TransportObject toSend = new TransportObject(Command.join,gname,reply);
 		user.queueReply(toSend);
 	}
 	
 	public void leaveAck(User user, String gname, ServerReply reply) {
 		TransportObject toSend = new TransportObject(Command.leave,gname,reply);
 		user.queueReply(toSend);
 	}
 
 	public void startNewTimer(SocketParams params) throws IOException {
 		List<Handler> task = new ArrayList<Handler>();
 		try {
 			task.add(new Handler(params));
 			ObjectOutputStream sent = params.getOutputStream();
 			List<Future<Handler>> futures = pool.invokeAll(task, TIMEOUT, TimeUnit.SECONDS);
 			if (futures.get(0).isCancelled()) {
 				TransportObject sendObject = new TransportObject(ServerReply.timeout);
 				sent.writeObject(sendObject);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public boolean joinGroup(BaseUser baseUser, String groupname) {
 		lock.writeLock().lock();
 		ChatGroup group;
 		User user = (User) baseUser;
 		boolean success = false;
 		if (!users.keySet().contains(user.getUsername())) {
 			lock.writeLock().unlock();
 			return false;
 		}
 		if (groups.containsKey(groupname)) {
 			group = groups.get(groupname);
 			success = group.joinGroup(user.getUsername(), user);
 			if(user.getUserGroups().contains(groupname)){
 				joinAck(user,groupname,ServerReply.ALREADY_MEMBER);
 				lock.writeLock().unlock();
 				return false;
 			}
 			if (success){
 				user.addToGroups(groupname);
 				joinAck(user,groupname,ServerReply.OK_JOIN);
 				TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			} else
 				joinAck(user,groupname,ServerReply.FAIL_FULL);
 			lock.writeLock().unlock();
 			return success;
 		}
 		else {
 			if (onlineNames.contains(groupname)){
 				joinAck(user,groupname,ServerReply.BAD_GROUP);
 				lock.writeLock().unlock();
 				return false;
 			}
 			group = new ChatGroup(groupname);
 			groups.put(groupname, group);
 			success = group.joinGroup(user.getUsername(), user);
 			user.addToGroups(groupname);
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			if(success){
 				joinAck(user,groupname,ServerReply.OK_CREATE);
 				try {
 					DBHandler.addGroup(groupname);
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			lock.writeLock().unlock();
 			return success;
 		}
 	}
 
 	@Override
 	public boolean leaveGroup(BaseUser baseUser, String groupname) {
 		User user = (User) baseUser;
 		lock.writeLock().lock();
 		ChatGroup group = groups.get(groupname);
 		if (group == null){
 			leaveAck(user,groupname,ServerReply.BAD_GROUP);
 			lock.writeLock().unlock();
 			return false;
 		}
 		if (group.leaveGroup(user.getUsername())) {
 			leaveAck(user,groupname,ServerReply.OK);
 			if(group.getNumUsers() <= 0) { 
 				groups.remove(group.getName()); 
 				onlineNames.remove(group.getName());
 				try {
 					DBHandler.removeGroup(groupname);
 				} catch (SQLException e) {
 					System.err.println("unsuccessful group removal from database");
 				}
 			}
 			user.removeFromGroups(groupname);
 			TestChatServer.logUserLeaveGroup(groupname, user.getUsername(), new Date());
 			String username = user.getUsername();
 			try {
 				DBHandler.removeFromGroup(username, groupname);
 			} catch (SQLException e) {
 				System.err.println("unsuccessful membership deletion in database");
 			}
 			lock.writeLock().unlock();
 			return true;
 		}
 		else {
 			leaveAck(user,groupname,ServerReply.NOT_MEMBER);
 		}
 		lock.writeLock().unlock();
 		return false;
 	}
 
 	@Override
 	public void shutdown() {
 		lock.writeLock().lock();
 		Set<String> userNames = users.keySet();
 		for(String name: userNames){
 			users.get(name).logoff();
 		}
 		users.clear();
 		groups.clear();
 		isDown = true;
 		lock.writeLock().unlock();
 	}
 
 	public MsgSendError processMessage(String source, String dest, String msg, int sqn, String timestamp) {	
 		Message message = new Message(timestamp, source, dest, msg);
 		message.setSQN(sqn);
 		lock.readLock().lock();
 		if (users.containsKey(source)) {				//Valid destination user
 			if (users.containsKey(dest)) {
 				User destUser = users.get(dest);
 				destUser.acceptMsg(message);
 			}
 			else if (registeredUsers.contains(dest)) {	//Registered offline user
 				try {
 					DBHandler.writeLog(message, dest);
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			else if (groups.containsKey(dest)) {		//Group destination
 				message.setIsFromGroup();
 				ChatGroup group = groups.get(dest);
 				MsgSendError sendError = group.forwardMessage(message);
 				if (sendError == MsgSendError.NOT_IN_GROUP) {
 					TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 					lock.readLock().unlock();
 					return sendError;
 				} else if (sendError == MsgSendError.MESSAGE_FAILED){
 					lock.readLock().unlock();
 					return sendError;
 				}
 				
 			} else {
 				TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 				lock.readLock().unlock();
 				return MsgSendError.INVALID_DEST;
 			}
 			
 		} else {
 			TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 			lock.readLock().unlock();
 			return MsgSendError.INVALID_SOURCE;
 		}
 		
 		lock.readLock().unlock();
 		return MsgSendError.MESSAGE_SENT;
 	}
 	
 	@Override
 	public void run(){
 		while(!isDown){
 			List<Handler> task = new ArrayList<Handler>();
 			Socket newSocket;
 			try {
 				newSocket = mySocket.accept();
 				Handler handler = new Handler(newSocket);
 				task.add(handler);
 				Thread t = new FirstThread(task, handler);
 				t.start();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class FirstThread extends Thread {
 		private List<Handler> task;
 		private Handler handler;
 		
 		public FirstThread(List<Handler> task, Handler handler) {
 			this.task = task;
 			this.handler = handler;
 		}
 		
 		public void run() {
 			try {
 				List<Future<Handler>> futures = pool.invokeAll(task, TIMEOUT, TimeUnit.SECONDS);
 				if (futures.get(0).isCancelled()) {
 					ObjectOutputStream sent = handler.sent;
 					TransportObject sendObject = new TransportObject(ServerReply.timeout);
 					sent.writeObject(sendObject);
 					handler.socket.close();
 				}
 			} catch (Exception e){
 				e.printStackTrace();
 			}
 				
 		}
 		
 	}
 	
 	class Handler implements Callable<ChatServer.Handler>, Runnable {
 		private final Socket socket;
 		    Handler(Socket socket) throws IOException { 
 		    	this.socket = socket;
 		    	received = new ObjectInputStream(socket.getInputStream());
 				sent = new ObjectOutputStream(socket.getOutputStream());
 		    }
 		    
 		    Handler(SocketParams params) {
 		    	this.socket = params.getMySocket();
 		    	received = params.getInputStream();
 		    	sent = params.getOutputStream();
 		    }
 		    private ObjectInputStream received;
 			private ObjectOutputStream sent;
 		    public void run() {
 		    		
 		    }
 			@Override
 			public Handler call() throws Exception {
 		    	TransportObject recObject = null;
 		    	while(recObject == null) {
 					try {
 						recObject = (TransportObject) received.readObject();
 					} catch (EOFException e) {
 						System.err.println("user connection dropped/finished");
 						return null;
 					} catch (SocketException e) {
 						System.err.println("user socket exception");
 						return null;
 					} catch (Exception e) {
 						e.printStackTrace();
 						return null;
 					}
 					if (recObject != null) {
 						Command type = recObject.getCommand();
 						if (type == Command.login) {
 							String username = recObject.getUsername();
 							String password = recObject.getPassword();
 							LoginError loginError = login(username,password);
 							System.out.println("called login: " + loginError);
 							TransportObject sendObject;
 							if (loginError == LoginError.USER_ACCEPTED) {
 								sendObject = new TransportObject(Command.login, ServerReply.OK);
 								User newUser = (User) getUser(username);
 								newUser.setSocket(socket, received, sent);
 							} else if (loginError == LoginError.USER_QUEUED) {
 								sendObject = new TransportObject(Command.login, ServerReply.QUEUED);
 								User newUser = null;
 								for(User u : waiting_users) {
 									if(u.getUsername().equals(username))
 										newUser = u;
 								}
 								if(newUser != null)
 									newUser.setSocket(socket, received, sent);
 							} else if (loginError == LoginError.USER_DROPPED || loginError == LoginError.USER_REJECTED){
 								sendObject = new TransportObject(Command.login, ServerReply.REJECTED);
 								recObject = null;
 							} else {
 								sendObject = new TransportObject(ServerReply.error);
 								recObject = null;
 							}
 							try {
 								sent.writeObject(sendObject);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}	
 						} else if (type == Command.adduser) {
 							String username = recObject.getUsername();
 							String password = recObject.getPassword();
 							ServerReply reply = addUser(username,password);
 							TransportObject sendObject = new TransportObject(type,reply);
 							try {
 								sent.writeObject(sendObject);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 							recObject = null;
 						}
 					}
 		    	}
 		    	return null;
 			
 			}
 	}
 	
 	public static void main(String[] args) throws Exception{
 		if (args.length != 1) {
 			throw new Exception("Invalid number of args to command");
 		}
 		int port = Integer.parseInt(args[0]);
 		ChatServer chatServer = new ChatServer(port);
 		BufferedReader commands = new BufferedReader(new InputStreamReader(System.in));
		/*while(!chatServer.isDown()){
 			String line = commands.readLine();
 			String[] tokens = line.split(" ");
 			if(tokens[0].equals("users")){
 				if(tokens.length==1) // get users
 					System.out.println(chatServer.getAllUsers());
 				else { // get users from a specific group
 					ChatGroup group = chatServer.getGroup(tokens[1]);
 					if(group==null)
 						System.out.println("no such group: " + tokens[1]);
 					else{
 						Map<String,User> userList = group.getUserList();
 						System.out.println(userList.keySet());
 					}
 				}
 			} else if(tokens[0].equals("groups")){
 				System.out.println(chatServer.getGroups());
 			} else if (tokens[0].equals("active-users")){
 				System.out.println(chatServer.getActiveUsers());
 			} else if (tokens[0].equals("shutdown")){
 				chatServer.shutdown();
 			}
		}*/
 	}
 }
