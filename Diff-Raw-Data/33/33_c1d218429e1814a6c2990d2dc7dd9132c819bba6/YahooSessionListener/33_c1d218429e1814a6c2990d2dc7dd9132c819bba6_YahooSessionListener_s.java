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
 package org.eclipse.ecf.provider.yahoo.util;
 
 import org.eclipse.ecf.provider.yahoo.container.YahooPresenceContainer;
 
 import ymsg.network.event.SessionChatEvent;
 import ymsg.network.event.SessionConferenceEvent;
 import ymsg.network.event.SessionErrorEvent;
 import ymsg.network.event.SessionEvent;
 import ymsg.network.event.SessionExceptionEvent;
 import ymsg.network.event.SessionFileTransferEvent;
 import ymsg.network.event.SessionFriendEvent;
 import ymsg.network.event.SessionListener;
 import ymsg.network.event.SessionNewMailEvent;
 import ymsg.network.event.SessionNotifyEvent;
 
 /** 
  * Utility class to deal with yahoo session events
  */
 public class YahooSessionListener implements SessionListener {
 
 	YahooPresenceContainer presenceContainer;
 	
	public YahooSessionListener(YahooPresenceContainer container) {
		this.presenceContainer = container;
 	}
 	
 	public void fileTransferReceived(SessionFileTransferEvent arg0) {}
 
	public void connectionClosed(SessionEvent arg0) {}
 
 	public void listReceived(SessionEvent arg0) {}
 
 	public void messageReceived(SessionEvent event) {
 		presenceContainer.handleMessageReceived(event);
 	}
 
 	public void buzzReceived(SessionEvent arg0) {}
 
 	public void offlineMessageReceived(SessionEvent arg0) {}
 
 	public void errorPacketReceived(SessionErrorEvent arg0) {}
 
 	public void inputExceptionThrown(SessionExceptionEvent arg0) {}
 
 	public void newMailReceived(SessionNewMailEvent arg0) {}
 
 	public void notifyReceived(SessionNotifyEvent arg0) {}
 
 	public void contactRequestReceived(SessionEvent arg0) {}
 
 	public void contactRejectionReceived(SessionEvent arg0) {}
 
 	public void conferenceInviteReceived(SessionConferenceEvent arg0) {}
 
 	public void conferenceInviteDeclinedReceived(SessionConferenceEvent arg0) {}
 
 	public void conferenceLogonReceived(SessionConferenceEvent arg0) {}
 
 	public void conferenceLogoffReceived(SessionConferenceEvent arg0) {}
 
 	public void conferenceMessageReceived(SessionConferenceEvent arg0) {}
 
 	public void friendsUpdateReceived(SessionFriendEvent event) {
 		presenceContainer.handleFriendsUpdateReceived(event);
 	}
 
 	public void friendAddedReceived(SessionFriendEvent arg0) {}
 
 	public void friendRemovedReceived(SessionFriendEvent arg0) {}
 
 	public void chatLogonReceived(SessionChatEvent arg0) {}
 
 	public void chatLogoffReceived(SessionChatEvent arg0) {}
 
 	public void chatMessageReceived(SessionChatEvent event) {
 		presenceContainer.handleMessageReceived(event);
 	}
 
 	public void chatUserUpdateReceived(SessionChatEvent arg0) {}
 
 	public void chatConnectionClosed(SessionEvent arg0) {}
 
 }
