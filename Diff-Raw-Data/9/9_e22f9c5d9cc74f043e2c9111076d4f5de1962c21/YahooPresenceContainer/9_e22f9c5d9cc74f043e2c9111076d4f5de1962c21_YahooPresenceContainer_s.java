 /**
  * Copyright (c) 2002-2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	- Initial API and implementation
  *  	- Chris Aniszczyk <zx@us.ibm.com>
  *   	- Borna Safabakhsh <borna@us.ibm.com> 
  *   
  * $Id$
  */
 package org.eclipse.ecf.provider.yahoo.container;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDCreateException;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.user.User;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.internal.provider.yahoo.Activator;
 import org.eclipse.ecf.presence.AbstractPresenceContainer;
 import org.eclipse.ecf.presence.IIMMessageListener;
 import org.eclipse.ecf.presence.IPresence;
 import org.eclipse.ecf.presence.IPresenceListener;
 import org.eclipse.ecf.presence.IPresenceSender;
 import org.eclipse.ecf.presence.Presence;
 import org.eclipse.ecf.presence.history.IHistory;
 import org.eclipse.ecf.presence.history.IHistoryManager;
 import org.eclipse.ecf.presence.im.ChatMessage;
 import org.eclipse.ecf.presence.im.ChatMessageEvent;
 import org.eclipse.ecf.presence.im.IChat;
 import org.eclipse.ecf.presence.im.IChatManager;
 import org.eclipse.ecf.presence.im.IChatMessage;
 import org.eclipse.ecf.presence.im.IChatMessageSender;
 import org.eclipse.ecf.presence.im.ITypingMessageSender;
 import org.eclipse.ecf.presence.im.IChatMessage.Type;
 import org.eclipse.ecf.presence.roster.IRoster;
 import org.eclipse.ecf.presence.roster.IRosterEntry;
 import org.eclipse.ecf.presence.roster.IRosterGroup;
 import org.eclipse.ecf.presence.roster.IRosterItem;
 import org.eclipse.ecf.presence.roster.IRosterListener;
 import org.eclipse.ecf.presence.roster.IRosterManager;
 import org.eclipse.ecf.presence.roster.IRosterSubscriptionListener;
 import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
 import org.eclipse.ecf.presence.roster.Roster;
 import org.eclipse.ecf.presence.roster.RosterEntry;
 import org.eclipse.ecf.presence.roster.RosterGroup;
 import org.eclipse.ecf.provider.yahoo.identity.YahooID;
 
 import ymsg.network.Session;
 import ymsg.network.StatusConstants;
 import ymsg.network.YahooGroup;
 import ymsg.network.YahooUser;
 import ymsg.network.event.SessionEvent;
 import ymsg.network.event.SessionFriendEvent;
 
 public class YahooPresenceContainer extends AbstractPresenceContainer {
 
 	private static final String YAHOO_ACCOUNT_NAME = " [yahoo]";
 	
 	private Session session;
 	
 	List listeners = new ArrayList();
 	
 	IContainer container;
 	
 	public YahooPresenceContainer(IContainer container, Session session) {
 		this.container = container;
 		this.session = session;
 	}
 
 	IChatMessageSender chatSender = new IChatMessageSender() {
 		public void sendChatMessage(ID toID, ID threadID, Type type,
 				String subject, String body, Map properties)
 				throws ECFException {
 			try {
 				session.sendMessage(toID.getName(), body);
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public void sendChatMessage(ID toID, String body) throws ECFException {
 			sendChatMessage(toID, null, IChatMessage.Type.CHAT, null, body, null);
 		}
 		
 	};
 	
 	protected IHistoryManager historyManager = new IHistoryManager() {
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.ecf.presence.im.IChatHistoryManager#getHistory(org.eclipse.ecf.core.identity.ID, java.util.Map)
 		 */
 		public IHistory getHistory(ID partnerID, Map options) {
 			// XXX TODO provide local storage (with some 
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 		 */
 		public Object getAdapter(Class adapter) {
 			return null;
 		}
 
 		public boolean isActive() {
 			return false;
 		}
 
 		public void setActive(boolean active) {
 			// TODO Auto-generated method stub
 			
 		}
 	};
 	
 	IChatManager chatManager = new IChatManager() {
 		public void addMessageListener(IIMMessageListener listener) {
 			listeners.add(listener);
 		}
 
 		public IChatMessageSender getChatMessageSender() {
 			return chatSender;
 		}
 
 		public ITypingMessageSender getTypingMessageSender() {
 			return null;
 		}
 
 		public void removeMessageListener(IIMMessageListener listener) {
 			listeners.remove(listener);
 		}
 
 		public IHistoryManager getHistoryManager() {
 			return historyManager;
 		}
 
		public IChat createChat(ID targetUser) throws ECFException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		
 	};
 	
 	/**
 	 * Notifies any listeners that a message has been received from a yahoo user
 	 * 
 	 * @param event
 	 */
 	public void handleMessageReceived(SessionEvent event) {
 		for(Iterator i=listeners.iterator(); i.hasNext(); ) {
 			IIMMessageListener l = (IIMMessageListener) i.next();
 			ID fromID = makeIDFromName(event.getFrom());
 			l.handleMessageEvent(new ChatMessageEvent(fromID, new ChatMessage(fromID,event.getMessage())));
 		}
 	}
 	
 	Vector presenceListeners = new Vector();
 	
 	Vector rosterListeners = new Vector();
 	
 	protected List getPresenceListeners() {
 		return presenceListeners;
 	}
 	
 	/**
 	 * Notifies any listeners that a friends status has changed
 	 * 
 	 * @param event
 	 */
 	public void handleFriendsUpdateReceived(SessionFriendEvent event) {
 		for(int i = 0; i < getPresenceListeners().size(); i++) {
 			IPresenceListener l = (IPresenceListener) getPresenceListeners().get(i);
 			ID from = makeIDFromName(event.getFrom());
 			IPresence presence = createPresence(from.getName());
 			l.handlePresence(from, presence);
 		}
 	}
 	
 	/** 
 	 * @param name
 	 * @return A proper ID based on a yahoo name
 	 */
     protected ID makeIDFromName(String name) {
         ID result = null;
         try {
             result = IDFactory.getDefault().createID(
             		IDFactory.getDefault().getNamespaceByName(Activator.NAMESPACE_IDENTIFIER),
             		new Object[] { name });
             return result;
         } catch (Exception e) {
         	e.printStackTrace();
         	return null;
         }
     }
 
 	/**
 	 * Note: This method is simplistic
 	 * 
 	 * @param userID
 	 * @return Determines the presence of a YMSG user
 	 */
 	public IPresence createPresence(String userID) {
 		YahooUser user = session.getUser(userID);
 		long status = user.getStatus();
 		Presence presence = new Presence(IPresence.Type.UNAVAILABLE);
 		if(status == StatusConstants.STATUS_AVAILABLE) {
 			presence = new Presence(IPresence.Type.AVAILABLE);
 		} else if(status == StatusConstants.STATUS_BRB || status == StatusConstants.STATUS_BUSY) {
 			presence = new Presence(IPresence.Type.UNAVAILABLE, "User is away", IPresence.Mode.AWAY);
 		} 
 		return presence;
 	}
 
 	Roster roster = null;
 	
 	IPresenceSender presenceSender = new IPresenceSender() {
 
 		public void sendPresenceUpdate(ID targetID, IPresence presence)
 				throws ECFException {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	};
 	
 	IRosterSubscriptionSender rosterSubscriptionSender = new IRosterSubscriptionSender() {
 
 		public void sendRosterAdd(String user, String name, String[] groups)
 				throws ECFException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void sendRosterRemove(ID userID) throws ECFException {
 			// TODO Auto-generated method stub
 			
 		}};
 	
 	IRosterManager rosterManger = new IRosterManager() {
 
 		public void addPresenceListener(IPresenceListener listener) {
 			presenceListeners.add(listener);
 		}
 
 		public void addRosterSubscriptionListener(
 				IRosterSubscriptionListener listener) {
 		}
 
 		public void addRosterListener(IRosterListener listener) {
 			rosterListeners.add(listener);
 		}
 
 		public IPresenceSender getPresenceSender() {
 			return presenceSender;
 		}
 
 		public IRoster getRoster() {
 			return roster;
 		}
 
 		public IRosterSubscriptionSender getRosterSubscriptionSender() {
 			return rosterSubscriptionSender;
 		}
 
 		public void removePresenceListener(IPresenceListener listener) {
 			presenceListeners.remove(listener);
 		}
 
 		public void removeRosterSubscriptionListener(
 				IRosterSubscriptionListener listener) {
 		}
 
 		public void removeRosterListener(IRosterListener listener) {
 			rosterListeners.remove(listener);
 		}
 
 		public Object getAdapter(Class adapter) {
 			return null;
 		}
 		
 	};
 	
 	public IRosterManager getRosterManager() {
 		return rosterManger;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.IPresenceContainerAdapter#getChatManager()
 	 */
 	public IChatManager getChatManager() {
 		return chatManager;
 	}
 
 	/**
 	 * @return
 	 */
 	protected IRoster getRoster() {
 		return roster;
 	}
 	
 	/**
 	 * Creates a Roster entry in the YMSG Buddy List for each buddy. Each roster
 	 * entry includes the targetID (representing this session), the userID
 	 * (created to represent the buddy in ECF), and the userName
 	 * 
 	 * @param user
 	 *            YahooUser to add to ECF buddy list
 	 * @return returns roster entry representing this user in the buddy list
 	 */
 	protected IRosterEntry makeRosterEntry(IRosterGroup group, YahooUser user) {
 		String userName = user.getId();
 		ID userID;
 		try {
 			userID = IDFactory.getDefault().createID(
 					container.getConnectNamespace(), userName);
 			IPresence presence = createPresence(userID
 					.getName());
 			return new RosterEntry(group, new User(userID, userName), presence);
 		} catch (IDCreateException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 
 	protected void populateRoster(YahooID userID, YahooGroup[] groups) {
 		roster = new Roster(this,new User(userID,userID.getName()+YAHOO_ACCOUNT_NAME));
 		for (int i = 0; i < groups.length; i++) {
 			RosterGroup group = new RosterGroup(getRoster(),
 					groups[i].getName());
 			for (int j = 0; j < groups[i].getMembers().size(); j++) {
 				YahooUser u = (YahooUser) groups[i].getMembers().get(j);
 				makeRosterEntry(group, u);
 			}
 			roster.addItem(group);
 		}
 		fireRosterUpdate(roster);
 	}
 
 
 	/**
 	 * @param entry
 	 */
 	protected void fireRosterEntryAdd(IRosterEntry entry) {
 		for(Iterator i=rosterListeners.iterator(); i.hasNext(); ) {
 			IRosterListener l = (IRosterListener) i.next();
 			l.handleRosterEntryAdd(entry);
 		}
 	}
 
 	/**
 	 * @param entry
 	 */
 	public void fireRosterUpdate(IRosterItem entry) {
 		for(Iterator i=rosterListeners.iterator(); i.hasNext(); ) {
 			IRosterListener l = (IRosterListener) i.next();
 			l.handleRosterUpdate(roster,entry);
 		}
 	}
 
 }
