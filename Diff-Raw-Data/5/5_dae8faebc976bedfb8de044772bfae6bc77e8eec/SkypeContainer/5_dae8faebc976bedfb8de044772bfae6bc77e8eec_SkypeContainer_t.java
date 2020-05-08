 /****************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Composent, Inc. - initial API and implementation
  *****************************************************************************/
 
 package org.eclipse.ecf.provider.skype;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.ecf.core.ContainerConnectException;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.events.ContainerConnectedEvent;
 import org.eclipse.ecf.core.events.ContainerConnectingEvent;
 import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
 import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.security.IConnectContext;
 import org.eclipse.ecf.core.sharedobject.ISharedObject;
 import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.datashare.IChannel;
 import org.eclipse.ecf.internal.provider.skype.Messages;
 import org.eclipse.ecf.presence.IAccountManager;
 import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
 import org.eclipse.ecf.presence.im.IChatManager;
 import org.eclipse.ecf.presence.roster.IRosterManager;
 import org.eclipse.ecf.presence.search.ICriteria;
 import org.eclipse.ecf.presence.search.IRestriction;
 import org.eclipse.ecf.presence.search.ISearch;
 import org.eclipse.ecf.presence.search.IUserSearchListener;
 import org.eclipse.ecf.presence.search.IUserSearchManager;
 import org.eclipse.ecf.presence.search.UserSearchException;
 import org.eclipse.ecf.presence.service.IPresenceService;
 import org.eclipse.ecf.provider.comm.ConnectionCreateException;
 import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
 import org.eclipse.ecf.provider.generic.ClientSOContainer;
 import org.eclipse.ecf.provider.generic.SOWrapper;
 import org.eclipse.ecf.provider.skype.identity.SkypeUserID;

import com.skype.Profile;
import com.skype.SkypeException;
import com.skype.connector.ConnectorException;
 
 /**
  * 
  */
 public class SkypeContainer extends ClientSOContainer implements IContainer, IPresenceService {
 
 	private static final String SKYPE_ACCOUNT_NAME = " [skype]"; //$NON-NLS-1$
 
 	private SkypeAccountManager accountManager = null;
 	private SkypeRosterManager rosterManager = null;
 	private SkypeChatManager chatManager = null;
 	private SkypeChatRoomManager chatRoomManager = null;
 
 	private SkypeUserID userID = null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.generic.ClientSOContainer#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		if (accountManager != null) {
 			accountManager.dispose();
 			accountManager = null;
 		}
 		if (rosterManager != null) {
 			rosterManager.dispose();
 			rosterManager = null;
 		}
 		if (chatManager != null) {
 			chatManager.dispose();
 			chatManager = null;
 		}
 		if (chatRoomManager != null) {
 			chatRoomManager.dispose();
 			chatRoomManager = null;
 		}
 	}
 
 	/**
 	 * @param skypeProfile 
 	 * @param id
 	 * @throws SkypeException 
 	 * @throws ConnectorException 
 	 */
 	public SkypeContainer(final Profile skypeProfile, final String id) throws SkypeException, ConnectorException {
 		super(new ISharedObjectContainerConfig() {
 
 			public Object getAdapter(Class clazz) {
 				return null;
 			}
 
 			public Map getProperties() {
 				return new HashMap();
 			}
 
 			public ID getID() {
 				try {
 					return IDFactory.getDefault().createStringID(id);
 				} catch (final Exception e) {
 					return null;
 				}
 			}
 		});
 		this.userID = new SkypeUserID(skypeProfile.getId());
 		//SkypeClient.hideSkypeWindow();
 		String fullName = skypeProfile.getFullName();
 		fullName = (fullName == null || fullName.equals("")) ? userID.getUser() : fullName; //$NON-NLS-1$
 		final org.eclipse.ecf.core.user.User user = new org.eclipse.ecf.core.user.User(userID, fullName + SKYPE_ACCOUNT_NAME);
 
 		accountManager = new SkypeAccountManager(this, skypeProfile, userID, user);
 		rosterManager = new SkypeRosterManager(this, user);
 		chatManager = new SkypeChatManager();
 		chatRoomManager = new SkypeChatRoomManager();
 	}
 
 	public SkypeUserID getSkypeUserID() {
 		return userID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.generic.ClientSOContainer#connect(org.eclipse.ecf.core.identity.ID,
 	 *      org.eclipse.ecf.core.security.IConnectContext)
 	 */
 	public synchronized void connect(ID remote, IConnectContext joinContext) throws ContainerConnectException {
 		if (this.connectionState != CONNECTED) {
 			fireContainerEvent(new ContainerConnectingEvent(getID(), this.remoteServerID));
 			try {
 				this.remoteServerID = userID;
 				this.connectionState = CONNECTED;
 				rosterManager.fillRoster();
 				fireContainerEvent(new ContainerConnectedEvent(getID(), this.remoteServerID));
 			} catch (final Exception e) {
 				throw new ContainerConnectException(Messages.SkypeContainer_EXCEPTION_COULDNOT_CONNECT, e);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.generic.ClientSOContainer#disconnect()
 	 */
 	public synchronized void disconnect() {
 		fireContainerEvent(new ContainerDisconnectingEvent(getID(), this.remoteServerID));
 		accountManager.disconnect();
 		rosterManager.disconnect();
 		chatManager.disconnect();
 		chatRoomManager.disconnect();
 		fireContainerEvent(new ContainerDisconnectedEvent(getID(), this.remoteServerID));
 		this.remoteServerID = null;
 		this.connectionState = DISCONNECTED;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.presence.IPresenceContainerAdapter#getAccountManager()
 	 */
 	public IAccountManager getAccountManager() {
 		return accountManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.presence.IPresenceContainerAdapter#getChatManager()
 	 */
 	public IChatManager getChatManager() {
 		return chatManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.presence.IPresenceContainerAdapter#getChatRoomManager()
 	 */
 	public IChatRoomManager getChatRoomManager() {
 		return chatRoomManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.presence.IPresenceContainerAdapter#getRosterManager()
 	 */
 	public IRosterManager getRosterManager() {
 		return rosterManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.generic.ClientSOContainer#createConnection(org.eclipse.ecf.core.identity.ID,
 	 *      java.lang.Object)
 	 */
 	protected ISynchAsynchConnection createConnection(ID remoteSpace, Object data) throws ConnectionCreateException {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.provider.generic.SOContainer#createSharedObjectWrapper(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.sharedobject.ISharedObject, java.util.Map)
 	 */
 	protected SOWrapper createSharedObjectWrapper(ID id, ISharedObject s, Map props) throws ECFException {
 		if (s instanceof IChannel)
 			return new SOWrapper(new SkypeChannelSOConfig(id, getID(), this, props), s, this);
 		return super.createSharedObjectWrapper(id, s, props);
 	}
 
 	public IUserSearchManager getUserSearchManager() {
 		return new IUserSearchManager(){
 
 			public ICriteria createCriteria() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public IRestriction createRestriction() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public String[] getUserPropertiesFields() throws ECFException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public boolean isEnabled() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			public ISearch search(ICriteria criteria)
 					throws UserSearchException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public void search(ICriteria criteria, IUserSearchListener listener) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		};
 	}
 
 
 }
