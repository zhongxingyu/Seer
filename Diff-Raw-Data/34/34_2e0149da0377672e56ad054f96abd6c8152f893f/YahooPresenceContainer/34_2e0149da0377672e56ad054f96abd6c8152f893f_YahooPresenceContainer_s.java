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
 
import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.user.IUser;
 import org.eclipse.ecf.internal.provider.yahoo.Activator;
 import org.eclipse.ecf.presence.AbstractPresenceContainer;
 import org.eclipse.ecf.presence.IMessageListener;
 import org.eclipse.ecf.presence.IMessageSender;
 import org.eclipse.ecf.presence.IPresence;
 import org.eclipse.ecf.presence.IPresenceListener;
 import org.eclipse.ecf.presence.IRosterEntry;
 import org.eclipse.ecf.presence.Presence;
 import org.eclipse.ecf.presence.roster.IRosterManager;
 
 import ymsg.network.Session;
 import ymsg.network.StatusConstants;
 import ymsg.network.YahooUser;
 import ymsg.network.event.SessionEvent;
 import ymsg.network.event.SessionFriendEvent;
 
 public class YahooPresenceContainer extends AbstractPresenceContainer {
 
 	private Session session;
 	
 	public YahooPresenceContainer(Session session) {
 		this.session = session;
 	}
 
 	public IMessageSender getMessageSender() {
 		return new IMessageSender() {
 			public void sendMessage(
 					ID toID, 
 					String subject, 
 					String messageBody) {
 				try {
 					session.sendMessage(toID.getName(), messageBody);
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 	}
 
 	/**
 	 * Notifies any listeners that a message has been received from a yahoo user
 	 * 
 	 * @param event
 	 */
 	public void handleMessageReceived(SessionEvent event) {
 		for(int i = 0; i < getMessageListeners().size(); i++) {
 			IMessageListener l = (IMessageListener) getMessageListeners().get(i);
 			ID from = makeIDFromName(event.getFrom());
 			ID to = makeIDFromName(event.getTo());
 			l.handleMessage(
 					from, 
 					to, 
 					IMessageListener.Type.NORMAL, 
 					event.getFrom(), 
 					event.getMessage());
 		}
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
 
     protected void fireRosterEntry(IRosterEntry entry) {
         for (int i = 0; i < getPresenceListeners().size(); i++) {
             IPresenceListener l = (IPresenceListener) getPresenceListeners().get(i);
             l.handleRosterEntryAdd(entry);
         }
     }
     
     protected void fireContainerJoined(ID container) {
         for (int i = 0; i < getPresenceListeners().size(); i++) {
             IPresenceListener l = (IPresenceListener) getPresenceListeners().get(i);
             l.handleConnected(container);
         }
     }
     
     protected void fireContainerDeparted(ID container) {
         for (int i = 0; i < getPresenceListeners().size(); i++) {
             IPresenceListener l = (IPresenceListener) getPresenceListeners().get(i);
             l.handleDisconnected(container);
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
 
 	public IRosterManager getRosterManager() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IUser getUser() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
