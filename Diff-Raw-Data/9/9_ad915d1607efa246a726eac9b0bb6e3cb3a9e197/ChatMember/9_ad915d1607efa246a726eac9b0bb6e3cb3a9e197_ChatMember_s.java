 package demo.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpSession;
 
 import org.mybeans.dao.DAOException;
 
 import demo.model.Model;
 import demo.model.MyDAOException;
 import demo.model.Preferable;
 import demo.model.PreferenceDAO;
 import demo.model.User;
 import demo.model.UserDAO;
 
 /**
  * This class represents a chat member which is an instance of a user identified by
  * a unique session id. Since the same session can have multiple web sockets open,
  * a chat member can potentially have multiple open sockets (i.e. browser tabs).
  * The purpose of this object is to hold the state of such chat member and provide
  * helper methods to modify the state of the chat member.
  * 
  * When the server sends a message to a chat member, it will be routed to all sockets
  * that that member has active.
  * 
  * A chat member is authenticated when its session contains a user object which represents
  * a unique user as identified by database and user id.
  */
 public class ChatMember {
 	private HttpSession session;
 	private User user;
 	private ChatMember partner;
 	private Set<ChatWebSocket> sockets;
 	private static Map<String,ChatMember> members;
 	private static UserDAO userDAO = null;
 	private static PreferenceDAO preferenceDAO = null;
	private static Set<Integer> preferables;
 
 	public ChatMember(HttpSession session) {
 		this.session = session;
 		sockets = new CopyOnWriteArraySet<ChatWebSocket>();
 		preferables = new CopyOnWriteArraySet<Integer>();
 
 		// session may contain data, so load it here
 		// partner = (ChatMember) session.getAttribute("partner");
 		if (isAuthenticated()) {
 			user = ((User)session.getAttribute("user"));
 		}
 		else user = new User();
 	}
 
 
 	public static void setMembers(Map<String,ChatMember> m) {
 		members = m;
 	}
 
 	public static void setDAO(Model model) {
 		userDAO = model.getUserDAO();
 		preferenceDAO = model.getPreferenceDAO();
 	}
 	
 	public static PreferenceDAO getPreferenceDAO() {
 		return preferenceDAO;
 	}
 
 	public String processMessageTo(String data) {
 		return data;
 	}
 
 	public String processMessageFrom(String data) {
 		return data;
 	}
 
 	public HttpSession getSession() {
 		return session;
 	}
 
 	public void setSession(HttpSession session) {
 		this.session = session;
 	}
 
 	public String getUsername() {
 		return user.getUsername();
 	}
 
 	public void setUsername(String name) {
 		user.setUsername(name);
 	}
 	
 	public void updateFieldsFromDB() throws DAOException {
 		user = userDAO.lookup(user.getUsername());
 	}
 
 	public boolean persistUser(String password) {
 		synchronized(userDAO) {
 			user.setPassword(password);
 			try { 
 				userDAO.create(user);
 				return true;
 			}
 			catch(DAOException e) {return false;}
 		}
 	}
 
 	public boolean isAvailable(String name) {
 		synchronized(userDAO) {
 			try {
 				return (userDAO.lookup(name) == null);
 			} catch (DAOException e) {
 				// TODO Auto-generated catch block
 				return false;
 			}
 		}
 	}
 
 	public boolean passwordMatches(String password) {
 		synchronized(userDAO) {
 			return userDAO.verifyPassword(user.getUsername(), password);
 		}
 	}
 	public ChatMember getPartner() {
 		return partner;
 	}
 
 	public void setPartner(ChatMember partner) {
 		// save partner in session for extra persistence
 		session.setAttribute("partner", partner);
 		this.partner = partner;
 	}
 
 	public Set<ChatWebSocket> getSockets() {
 		return sockets;
 	}
 
 	public void setSockets(Set<ChatWebSocket> sockets) {
 		this.sockets = sockets;
 	}
 
 	/**
 	 * Add an open socket to this member, prompting to begin authentication
 	 * if necessary.
 	 */
 	public void addSocket(ChatWebSocket socket) {
 		sockets.add(socket);
 	}
 
 	/**
 	 * Removes a socket open by this chat member.
 	 * 
 	 * If it is the last socket belonging to this member, the member itself is
 	 * removed from the global members list.
 	 */
 	public void removeSocket(ChatWebSocket socket) {
 		sockets.remove(socket);
 
 		// if no more sockets exist for this member, this member no longer needs to exist
 		if (sockets.isEmpty()) {
 			synchronized(members) {
 				members.remove(session.getId());
 				
 				// inform partner of non-existence of this user
 				if (partner != null) {
 					partner.disconnectPartner();
 					setPartner(null);
 				}
 			}
 		}
 	}
 	
 	public void disconnectPartner() {
 		setPartner(null);
 		Message message = new Message();
 		message.setHeader("partner");
 		message.setBody("Your partner disconnected. Please wait while we try to find another.");
 		message.setSender("system");
 		sendMessage(message);
 		new PartnerlessAction().perform(this, null);
 	}
 
 	/**
 	 * Returns whether this chat member is authenticated
 	 */
 	public boolean isAuthenticated() {
 		return (session.getAttribute("user") != null);
 	}
 
 	/**
 	 * Sends message to all sockets belonging to this chat member
 	 */
 	public void sendMessage(Message message) {
 		for (ChatWebSocket socket : sockets) {
 			socket.send(message);
 		}
 	}
 
 	/**
 	 * Searches member set for the best match that currently also does not have a partner
 	 * 
 	 * Returns true if a partner is found, false otherwise.
 	 */
 	public boolean findPartner() {
 		// thread safe - multiple objects grabbing same partner shouldn't be allowed
 		synchronized(members) {
 			ChatMember partner = null;
 			int maxBonding = Integer.MIN_VALUE;
 			for (ChatMember member : members.values()) {
 				if (member.equals(this)) continue; // exclude this member
 				if (member.getPartner() != null) continue; // exclude members already paired
 				if (!member.isAuthenticated()) continue; // exclude non-authenticated members
 				int bonding;
 				try {
 					bonding = comparePrefs(member);
 				} catch (MyDAOException e) {
 					// TODO Auto-generated catch block
 					bonding = 0;
 					e.printStackTrace();
 				}
 				if (bonding > maxBonding) {
 					partner = member;
 					maxBonding = bonding;
 				}
 			}
 			// no chat members are available
 			if (partner == null) return false;
 			
 			//a partner was found
 			this.setPartner(partner);
 			partner.setPartner(this);
 			return true;
 		}
 
 		
 	}
 	
 	private int comparePrefs(ChatMember him) throws MyDAOException {
 		List<Preferable> ourList = preferenceDAO.getUserPreferences(user.getUid());
 		List<Preferable> hisList = preferenceDAO.getUserPreferences(him.getUser().getUid());
 		ourList.retainAll(hisList);
 		int bond = 0;
 		for (Preferable pref : ourList) {
 			Preferable hisPref = hisList.get(hisList.indexOf((pref)));
 			bond -= Math.pow((pref.getPreference() - hisPref.getPreference()),2);
 		}
 		return bond;
 	}
 
 
 	public User getUser() {
 		return user;
 	}
 	
 	public Set<Integer> getPreferables() {
 		return preferables;
 	}
 }
