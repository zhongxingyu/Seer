 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.client.model.observers.LoginObserver;
 import org.eclipse.emf.emfstore.client.model.observers.LogoutObserver;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.ConnectionException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.SessionId;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.OrgUnitProperty;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Usersession</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getUsername <em>Username</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getPassword <em>Password</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getSessionId <em>Session Id</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getPersistentPassword <em>Persistent Password
  * </em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getServerInfo <em>Server Info</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#isSavePassword <em>Save Password</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getACUser <em>AC User</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.UsersessionImpl#getChangedProperties <em>Changed Properties
  * </em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class UsersessionImpl extends EObjectImpl implements Usersession {
 
 	/**
 	 * The default value of the '{@link #getUsername() <em>Username</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getUsername()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String USERNAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getUsername() <em>Username</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getUsername()
 	 * @generated
 	 * @ordered
 	 */
 	protected String username = USERNAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getPassword() <em>Password</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getPassword()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PASSWORD_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getPassword() <em>Password</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getPassword()
 	 * @generated
 	 * @ordered
 	 */
 	protected String password = PASSWORD_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getSessionId() <em>Session Id</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getSessionId()
 	 * @generated
 	 * @ordered
 	 */
 	protected SessionId sessionId;
 
 	/**
 	 * The default value of the '{@link #getPersistentPassword() <em>Persistent Password</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getPersistentPassword()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PERSISTENT_PASSWORD_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getPersistentPassword() <em>Persistent Password</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getPersistentPassword()
 	 * @generated
 	 * @ordered
 	 */
 	protected String persistentPassword = PERSISTENT_PASSWORD_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getServerInfo() <em>Server Info</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getServerInfo()
 	 * @generated
 	 * @ordered
 	 */
 	protected ServerInfo serverInfo;
 
 	/**
 	 * The default value of the '{@link #isSavePassword() <em>Save Password</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #isSavePassword()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean SAVE_PASSWORD_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isSavePassword() <em>Save Password</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #isSavePassword()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean savePassword = SAVE_PASSWORD_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getACUser() <em>AC User</em>}' containment reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getACUser()
 	 * @generated
 	 * @ordered
 	 */
 	protected ACUser acUser;
 
 	/**
 	 * The cached value of the '{@link #getChangedProperties()
 	 * <em>Changed Properties</em>}' containment reference list. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getChangedProperties()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<OrgUnitProperty> changedProperties;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected UsersessionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.USERSESSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setUsername(String newUsername) {
 		String oldUsername = username;
 		username = newUsername;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__USERNAME, oldUsername,
 				username));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getPasswordGen() {
 		return password;
 	}
 
 	// begin of custom code
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Usersession#getPassword()
 	 * @generated NOT
 	 */
 	public String getPassword() {
 		if (isSavePassword()) {
			return getPersistentPassword();
 		} else {
 			return password;
 		}
 
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setPasswordGen(String newPassword) {
 		String oldPassword = password;
 		password = newPassword;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__PASSWORD, oldPassword,
 				password));
 	}
 
 	// begin of custom code
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Usersession#setPassword(java.lang.String)
 	 * @generated NOT
 	 */
 	public void setPassword(String newPassword) {
 		if (newPassword != null) {
 			setPasswordGen(KeyStoreManager.getInstance().encrypt(newPassword, getServerInfo()));
 		} else {
 			setPasswordGen(null);
 		}
 		if (isSavePassword()) {
 			if (!(newPassword.equals(persistentPassword))) {
 				setPersistentPassword(KeyStoreManager.getInstance().encrypt(newPassword, getServerInfo()));
 			}
 		}
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public SessionId getSessionIdGen() {
 		if (sessionId != null && sessionId.eIsProxy()) {
 			InternalEObject oldSessionId = (InternalEObject) sessionId;
 			sessionId = (SessionId) eResolveProxy(oldSessionId);
 			if (sessionId != oldSessionId) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.USERSESSION__SESSION_ID,
 						oldSessionId, sessionId));
 			}
 		}
 		return sessionId;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public SessionId basicGetSessionId() {
 		return sessionId;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setSessionId(SessionId newSessionId) {
 		SessionId oldSessionId = sessionId;
 		sessionId = newSessionId;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__SESSION_ID, oldSessionId,
 				sessionId));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getPersistentPassword() {
 		return persistentPassword;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setPersistentPasswordGen(String newPersistentPassword) {
 		String oldPersistentPassword = persistentPassword;
 		persistentPassword = newPersistentPassword;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__PERSISTENT_PASSWORD,
 				oldPersistentPassword, persistentPassword));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Usersession#setPersistentPassword(java.lang.String)
 	 * @generated NOT
 	 */
 	public void setPersistentPassword(String newPersistentPassword) {
 
 		setPersistentPasswordGen(newPersistentPassword);
 	}
 
 	// end of custom code
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ServerInfo getServerInfo() {
 		if (serverInfo != null && serverInfo.eIsProxy()) {
 			InternalEObject oldServerInfo = (InternalEObject) serverInfo;
 			serverInfo = (ServerInfo) eResolveProxy(oldServerInfo);
 			if (serverInfo != oldServerInfo) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.USERSESSION__SERVER_INFO,
 						oldServerInfo, serverInfo));
 			}
 		}
 		return serverInfo;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ServerInfo basicGetServerInfo() {
 		return serverInfo;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setServerInfo(ServerInfo newServerInfo) {
 		ServerInfo oldServerInfo = serverInfo;
 		serverInfo = newServerInfo;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__SERVER_INFO, oldServerInfo,
 				serverInfo));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean isSavePassword() {
 		return savePassword;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setSavePasswordGen(boolean newSavePassword) {
 		boolean oldSavePassword = savePassword;
 		savePassword = newSavePassword;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__SAVE_PASSWORD,
 				oldSavePassword, savePassword));
 	}
 
 	// begin of custom code
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Usersession#setSavePassword(boolean)
 	 * @generated NOT
 	 */
 	public void setSavePassword(boolean newSavePassword) {
 
 		if (!newSavePassword) {
 			setPersistentPassword(null);
		} else if (getPasswordGen() != null) {
 			setPersistentPassword(getPasswordGen());
 		}
 
 		setSavePasswordGen(newSavePassword);
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ACUser getACUser() {
 		if (acUser != null && acUser.eIsProxy()) {
 			InternalEObject oldACUser = (InternalEObject) acUser;
 			acUser = (ACUser) eResolveProxy(oldACUser);
 			if (acUser != oldACUser) {
 				InternalEObject newACUser = (InternalEObject) acUser;
 				NotificationChain msgs = oldACUser.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.USERSESSION__AC_USER, null, null);
 				if (newACUser.eInternalContainer() == null) {
 					msgs = newACUser.eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.USERSESSION__AC_USER,
 						null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.USERSESSION__AC_USER,
 						oldACUser, acUser));
 			}
 		}
 		return acUser;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ACUser basicGetACUser() {
 		return acUser;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetACUser(ACUser newACUser, NotificationChain msgs) {
 		ACUser oldACUser = acUser;
 		acUser = newACUser;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.USERSESSION__AC_USER, oldACUser, newACUser);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setACUser(ACUser newACUser) {
 		if (newACUser != acUser) {
 			NotificationChain msgs = null;
 			if (acUser != null)
 				msgs = ((InternalEObject) acUser).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.USERSESSION__AC_USER, null, msgs);
 			if (newACUser != null)
 				msgs = ((InternalEObject) newACUser).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.USERSESSION__AC_USER, null, msgs);
 			msgs = basicSetACUser(newACUser, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USERSESSION__AC_USER, newACUser,
 				newACUser));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<OrgUnitProperty> getChangedProperties() {
 		if (changedProperties == null) {
 			changedProperties = new EObjectContainmentEList.Resolving<OrgUnitProperty>(OrgUnitProperty.class, this,
 				ModelPackage.USERSESSION__CHANGED_PROPERTIES);
 		}
 		return changedProperties;
 	}
 
 	// begin of custom code
 	/**
 	 * <!-- begin-user-doc --> Return whether session is logged in.
 	 * 
 	 * @return true if session is logged in <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isLoggedIn() {
 		return this.sessionId != null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Usersession#logIn()
 	 * @generated NOT
 	 */
 	public void logIn() throws EmfStoreException, AccessControlException {
 		ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		// sanity checks
 		if (getUsername() == null || getPassword() == null) {
 			throw new AccessControlException("Username or Password not set!");
 		}
 		ServerInfo serverInfo = getServerInfo();
 		if (serverInfo == null) {
 			throw new IllegalStateException("No ServerInfo set!");
 		}
 		if (serverInfo.getUrl() == null) {
 			throw new ConnectionException("Invalid server url: null");
 		}
 
 		// prepare serverInfo for send: copy and remove usersession
 		ServerInfo copy = ModelUtil.clone(serverInfo);
 		copy.setLastUsersession(null);
 		SessionId newSessionId = null;
 
 		newSessionId = connectionManager.logIn(username, getPassword(), copy, Configuration.getClientVersion());
 		getServerInfo().setLastUsersession(this);
 		this.setSessionId(newSessionId);
 		WorkspaceManager.getObserverBus().notify(LoginObserver.class).loginCompleted(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void logout() throws EmfStoreException {
 		ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		connectionManager.logout(sessionId);
 		setSessionId(null);
 		WorkspaceManager.getObserverBus().notify(LogoutObserver.class).logoutCompleted(this);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case ModelPackage.USERSESSION__AC_USER:
 			return basicSetACUser(null, msgs);
 		case ModelPackage.USERSESSION__CHANGED_PROPERTIES:
 			return ((InternalEList<?>) getChangedProperties()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	// end of custom code
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 		case ModelPackage.USERSESSION__USERNAME:
 			return getUsername();
 		case ModelPackage.USERSESSION__PASSWORD:
 			return getPassword();
 		case ModelPackage.USERSESSION__SESSION_ID:
 			if (resolve)
 				return getSessionId();
 			return basicGetSessionId();
 		case ModelPackage.USERSESSION__PERSISTENT_PASSWORD:
 			return getPersistentPassword();
 		case ModelPackage.USERSESSION__SERVER_INFO:
 			if (resolve)
 				return getServerInfo();
 			return basicGetServerInfo();
 		case ModelPackage.USERSESSION__SAVE_PASSWORD:
 			return isSavePassword();
 		case ModelPackage.USERSESSION__AC_USER:
 			if (resolve)
 				return getACUser();
 			return basicGetACUser();
 		case ModelPackage.USERSESSION__CHANGED_PROPERTIES:
 			return getChangedProperties();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 		case ModelPackage.USERSESSION__USERNAME:
 			setUsername((String) newValue);
 			return;
 		case ModelPackage.USERSESSION__PASSWORD:
 			setPassword((String) newValue);
 			return;
 		case ModelPackage.USERSESSION__SESSION_ID:
 			setSessionId((SessionId) newValue);
 			return;
 		case ModelPackage.USERSESSION__PERSISTENT_PASSWORD:
 			setPersistentPassword((String) newValue);
 			return;
 		case ModelPackage.USERSESSION__SERVER_INFO:
 			setServerInfo((ServerInfo) newValue);
 			return;
 		case ModelPackage.USERSESSION__SAVE_PASSWORD:
 			setSavePassword((Boolean) newValue);
 			return;
 		case ModelPackage.USERSESSION__AC_USER:
 			setACUser((ACUser) newValue);
 			return;
 		case ModelPackage.USERSESSION__CHANGED_PROPERTIES:
 			getChangedProperties().clear();
 			getChangedProperties().addAll((Collection<? extends OrgUnitProperty>) newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 		case ModelPackage.USERSESSION__USERNAME:
 			setUsername(USERNAME_EDEFAULT);
 			return;
 		case ModelPackage.USERSESSION__PASSWORD:
 			setPassword(PASSWORD_EDEFAULT);
 			return;
 		case ModelPackage.USERSESSION__SESSION_ID:
 			setSessionId((SessionId) null);
 			return;
 		case ModelPackage.USERSESSION__PERSISTENT_PASSWORD:
 			setPersistentPassword(PERSISTENT_PASSWORD_EDEFAULT);
 			return;
 		case ModelPackage.USERSESSION__SERVER_INFO:
 			setServerInfo((ServerInfo) null);
 			return;
 		case ModelPackage.USERSESSION__SAVE_PASSWORD:
 			setSavePassword(SAVE_PASSWORD_EDEFAULT);
 			return;
 		case ModelPackage.USERSESSION__AC_USER:
 			setACUser((ACUser) null);
 			return;
 		case ModelPackage.USERSESSION__CHANGED_PROPERTIES:
 			getChangedProperties().clear();
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 		case ModelPackage.USERSESSION__USERNAME:
 			return USERNAME_EDEFAULT == null ? username != null : !USERNAME_EDEFAULT.equals(username);
 		case ModelPackage.USERSESSION__PASSWORD:
 			return PASSWORD_EDEFAULT == null ? password != null : !PASSWORD_EDEFAULT.equals(password);
 		case ModelPackage.USERSESSION__SESSION_ID:
 			return sessionId != null;
 		case ModelPackage.USERSESSION__PERSISTENT_PASSWORD:
 			return PERSISTENT_PASSWORD_EDEFAULT == null ? persistentPassword != null : !PERSISTENT_PASSWORD_EDEFAULT
 				.equals(persistentPassword);
 		case ModelPackage.USERSESSION__SERVER_INFO:
 			return serverInfo != null;
 		case ModelPackage.USERSESSION__SAVE_PASSWORD:
 			return savePassword != SAVE_PASSWORD_EDEFAULT;
 		case ModelPackage.USERSESSION__AC_USER:
 			return acUser != null;
 		case ModelPackage.USERSESSION__CHANGED_PROPERTIES:
 			return changedProperties != null && !changedProperties.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) {
 			return super.toString();
 		}
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (username: ");
 		result.append(username);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Usersession#getSessionId()
 	 */
 	public SessionId getSessionId() {
 		return getSessionIdGen();
 	}
 
 } // UsersessionImpl
