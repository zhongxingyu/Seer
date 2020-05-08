 package edu.berkeley.cs.cs162;
 
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
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
 
 	private BlockingQueue<String> waiting_users;
 	private Map<String, User> users;
 	private Map<String, ChatGroup> groups;
 	private Set<String> allNames;
 	private ReentrantReadWriteLock lock;
 	private volatile boolean isDown;
 	private final static int MAX_USERS = 100;
 	private final static int MAX_WAITING_USERS = 10;
 	private ServerSocket mySocket;
 	private ExecutorService pool;
 	
 	public ChatServer() {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		allNames = new HashSet<String>();
 		lock = new ReentrantReadWriteLock(true);
 		waiting_users = new ArrayBlockingQueue<String>(MAX_WAITING_USERS);
 		isDown = false;
 		
 	}
 	
 	public ChatServer(int port) throws IOException {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		allNames = new HashSet<String>();
 		lock = new ReentrantReadWriteLock(true);
 		waiting_users = new ArrayBlockingQueue<String>(MAX_WAITING_USERS);
 		isDown = false;
 		pool = Executors.newFixedThreadPool(100);
 		try {
 			mySocket = new ServerSocket(port);
 		} catch (Exception e) {
 			throw new IOException("Server socket creation failed");
 		}
 		
 		this.start();
 	}
 	
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
 	
 	public Set<String> getUsers() {
 		Set<String> userNames;
 		lock.readLock().lock();
 		userNames = users.keySet();
 		lock.readLock().unlock();
 		return userNames;
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
 	
 	@Override
 	public LoginError login(String username) {
 		lock.writeLock().lock();
 		if(isDown){
 			TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_REJECTED);
 			lock.writeLock().unlock();
 			return LoginError.USER_REJECTED;
 		}
 		if (allNames.contains(username)) {
 			lock.writeLock().unlock();
 			TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_REJECTED);
 			return LoginError.USER_REJECTED;
 		}
 		if (users.size() >= MAX_USERS) {		//exceeds capacity
 			lock.writeLock().unlock();
 			if(waiting_users.offer(username))	//attempt to add to waiting queue
 				return LoginError.USER_QUEUED;
 			else {								//else drop user
 				TestChatServer.logUserLoginFailed(username, new Date(), LoginError.USER_DROPPED);
 				return LoginError.USER_DROPPED;				
 			}
 		}
 		User newUser = new User(this, username);
 		users.put(username, newUser);
 		allNames.add(username);
 		newUser.connected();
 		lock.writeLock().unlock();
 		TestChatServer.logUserLogin(username, new Date());
 		return LoginError.USER_ACCEPTED;
 	}
 
 	@Override
 	public boolean logoff(String username) {
 		// TODO Auto-generated method stub
 		lock.writeLock().lock();
 		if(!users.containsKey(username)){
 			lock.writeLock().unlock();
 			return false;
 		}
 		List <String> userGroups = users.get(username).getUserGroups();
 		Iterator<String> it = userGroups.iterator();
 		while(it.hasNext()){
 			ChatGroup group = groups.get(it.next());
 			if(group.leaveGroup(username)){
 				if(group.getNumUsers() <= 0) { 
 					groups.remove(group.getName()); 
 					allNames.remove(group.getName());
 				}
 			}
 		}
 		users.get(username).logoff();
 		allNames.remove(username);
 		users.remove(username);
 		
 		// Check for waiting users
 		String uname = waiting_users.poll();
 		if(uname != null) {							//add to ChatServer
 			User newUser = new User(this, uname);
 			users.put(uname, newUser);
 			allNames.add(uname);
 			newUser.connected();
 			TestChatServer.logUserLogin(uname, new Date());
 		}
 		
 		lock.writeLock().unlock();	
 		return true;
 	}
 
 	public void startNewTimer(Socket socket) {
 		List<Handler> task = new ArrayList<Handler>();
 		try {
 			task.add(new Handler(socket));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			pool.invokeAll(task, (long) 20, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 		@Override
 	public boolean joinGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		lock.writeLock().lock();
 		ChatGroup group;
 		User user = (User) baseUser;
 		boolean success = false;
 		if (!users.keySet().contains(user.getUsername())) {
 			lock.writeLock().unlock();
 			return false;
 		}
 		if(groups.containsKey(groupname)) {
 			group = groups.get(groupname);
 			success = group.joinGroup(user.getUsername(), user);
 			user.addToGroups(groupname);
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return success;
 		}
 		else {
 			if(allNames.contains(groupname)){
 				lock.writeLock().unlock();
 				return false;
 			}
 			group = new ChatGroup(groupname);
 			groups.put(groupname, group);
 			success = group.joinGroup(user.getUsername(), user);
 			user.addToGroups(groupname);
 			TestChatServer.logUserJoinGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return success;
 		}
 	}
 
 	@Override
 	public boolean leaveGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		User user = (User) baseUser;
 		lock.writeLock().lock();
 		ChatGroup group = groups.get(groupname);
 		if (group == null){
 			lock.writeLock().unlock();
 			return false;
 		}
 		if(group.leaveGroup(user.getUsername())) {
 			if(group.getNumUsers() <= 0) { 
 				groups.remove(group.getName()); 
 				allNames.remove(group.getName());
 			}
 			user.removeFromGroups(groupname);
 			TestChatServer.logUserLeaveGroup(groupname, user.getUsername(), new Date());
 			lock.writeLock().unlock();
 			return true;
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
 		if (users.containsKey(source)) {
 			if (users.containsKey(dest)) {
 				User destUser = users.get(dest);
 				destUser.acceptMsg(message);
 			} else if (groups.containsKey(dest)) {
 				message.setIsFromGroup();
 				ChatGroup group = groups.get(dest);
 				if (!group.forwardMessage(message)) {
 					TestChatServer.logChatServerDropMsg(message.toString(), new Date());
 					lock.readLock().unlock();
 					return MsgSendError.NOT_IN_GROUP;
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
 			try {
 				task.add(new Handler(mySocket.accept()));
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try {
 				pool.invokeAll(task, (long) 20, TimeUnit.SECONDS);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
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
 		    private ObjectInputStream received;
 			private ObjectOutputStream sent;
 		    public void run() {
 		    	TransportObject recObject = null;
 				try {
 					recObject = (TransportObject) received.readObject();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				if (recObject != null) {
 					Command type = recObject.getCommand();
 					if (type == Command.login) {
 						String username = recObject.getUsername();
 						LoginError loginError = login(username);
 						TransportObject sendObject;
 						if (loginError == LoginError.USER_ACCEPTED) {
 							sendObject = new TransportObject(Command.login, ServerReply.OK);
 							User newUser = (User) getUser(username);
 							newUser.setSocket(socket);
 						} else if (loginError == LoginError.USER_QUEUED) {
 							sendObject = new TransportObject(Command.login, ServerReply.QUEUED);
 						} else {
 							sendObject = new TransportObject(Command.login, ServerReply.REJECTED);
 						}
						try {
							sent.writeObject(sendObject);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
 						
 					}
 				}
 				
 				
 		    }
 			@Override
 			public Handler call() throws Exception {
 				// TODO Auto-generated method stub
 				return null;
 			}
 	}
 	
 	public static void main(String[] args) throws Exception{
 		if (args.length != 2) {
 			throw new Exception("Invalid number of args to command");
 		}
 		int port = Integer.parseInt(args[1]);
 		ChatServer chatServer = new ChatServer(port);
 	}
 }
