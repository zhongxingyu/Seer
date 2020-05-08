 package edu.berkeley.cs.cs162;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * This is the core of the chat server.  Put the management of groups
  * and users in here.  You will need to control all of the threads,
  * and respond to requests from the test harness.
  *
  * It must implement the ChatServerInterface Interface, and you should
  * not modify that interface; it is necessary for testing.
  */
 
 public class ChatServer extends Thread implements ChatServerInterface {
 
 	private Map<String, User> users;
 	private Map<String, ChatGroup> groups;
 	private Set<String> allNames;
 	private ReentrantReadWriteLock lock;
 	private boolean isDown;
 	private final static int MAX_USERS = 100;
 	
 	public ChatServer() {
 		users = new HashMap<String, User>();
 		groups = new HashMap<String, ChatGroup>();
 		allNames = new HashSet<String>();
 		lock = new ReentrantReadWriteLock();
 		isDown = false;
 	}
 	
 	@Override
 	public LoginError login(String username) {
 		lock.writeLock().lock();
 		if(isDown)
 			return LoginError.USER_REJECTED;
 		if (users.size() >= MAX_USERS) {
 			lock.writeLock().unlock();
 			return LoginError.USER_DROPPED;
 		}
 		if (allNames.contains(username)) {
 			lock.writeLock().unlock();
 			return LoginError.USER_REJECTED;
 		}
 		User newUser = new User(this, username);
 		users.put(username, newUser);
 		allNames.add(username);
 		newUser.connected();
 		lock.writeLock().unlock();
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
 			groups.get(it.next()).leaveGroup(username);
 		}
 		lock.writeLock().unlock();	
 		return true;
 	}
 
 	@Override
 	public boolean joinGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		lock.writeLock().lock();
 		ChatGroup group;
 		User user = (User) baseUser;
 		boolean success = false;
 		if(groups.containsKey(groupname)) {
 			group = groups.get(groupname);
 			success = group.joinGroup(user.getUsername(), user);
 			lock.writeLock().unlock();
 			return success;
 		}
 		else {
 			group = new ChatGroup(groupname);
 			groups.put(groupname, group);
 			success = group.joinGroup(user.getUsername(), user);
 			lock.writeLock().unlock();
 			return success;
 		}
 	}
 
 	@Override
 	public boolean leaveGroup(BaseUser baseUser, String groupname) {
 		// TODO Auto-generated method stub
 		User user = (User) baseUser;
 		ChatGroup group = groups.get(groupname);
 		if(group.leaveGroup(user.getUsername())) {
 			if(group.getNumUsers() <= 0) { groups.remove(groupname); }
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void shutdown() {
 		lock.writeLock().lock();
 		users.clear();
 		groups.clear();
 		isDown = true;
 		lock.writeLock().unlock();
 	}
 
 	@Override
 	public BaseUser getUser(String username) {
 		return users.get(username);
 	}
 	
 	public Set<String> getGroups() {
 		return groups.keySet();
 	}
 	
 	public Set<String> getUsers() {
 		return users.keySet();
 	}
 	
 	public MsgSendError processMessage(String source, String dest, String msg) {
 		lock.readLock().lock();
 		if (users.containsKey(source)) {
 			if(users.containsKey(dest)) {
 				Message message = new Message(Long.toString(System.currentTimeMillis()),dest, source, msg);
 				User destUser = users.get(dest);
 				destUser.msgReceived(message);
 			} else if(groups.containsKey(dest)) {
 				Message message = new Message(Long.toString(System.currentTimeMillis()),dest, source, msg);
 				ChatGroup group = groups.get(dest);
 				// Have group broadcast the message and if fail release read lock and return error
 			} else {
 				lock.readLock().unlock();
 				return MsgSendError.INVALID_DEST;
 			}
 		} else {
 			lock.readLock().unlock();
 			return MsgSendError.INVALID_SOURCE;
 		}
 		lock.readLock().unlock();
 		return MsgSendError.MESSAGE_SENT;
 	}
 }
