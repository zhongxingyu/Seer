 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.module.users;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.romaframework.aspect.authentication.AuthenticationAspectAbstract;
 import org.romaframework.aspect.authentication.AuthenticationException;
 import org.romaframework.aspect.authentication.UserObjectPermissionListener;
 import org.romaframework.aspect.persistence.PersistenceAspect;
 import org.romaframework.aspect.persistence.QueryByFilter;
 import org.romaframework.aspect.session.SessionInfo;
 import org.romaframework.aspect.session.SessionListener;
 import org.romaframework.core.Roma;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaFieldListener;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.module.users.domain.BaseAccount;
 import org.romaframework.module.users.domain.BaseAccountStatus;
 import org.romaframework.module.users.domain.BaseFunction;
 import org.romaframework.module.users.domain.BaseProfile;
 import org.romaframework.module.users.domain.BaseProfile.Mode;
 import org.romaframework.module.users.repository.BaseAccountRepository;
 import org.romaframework.module.users.view.domain.AccountManagementUtility;
 
 public class UsersAuthentication extends AuthenticationAspectAbstract implements UserObjectPermissionListener, SessionListener, SchemaFieldListener {
 
 	public static final String		ANONYMOUS_PROFILE_NAME	= "anonymous";
 
 	public static final String		PAR_ALGORITHM						= "algorithm";
 
 	protected static BaseProfile	publicProfile;
 	protected static final int		ERROR_SLEEP_TIME				= 1000;
 
 	private BaseProfile						anonymousProfile;
 
 	private boolean								loadedAnonymousProfile	= false;
 
 	private boolean								singleSessionPerUser		= false;
 
 	public UsersAuthentication() {
 		Controller.getInstance().registerListener(SessionListener.class, this);
 		Controller.getInstance().registerListener(SchemaFieldListener.class, this);
 		Controller.getInstance().registerListener(UserObjectPermissionListener.class, this);
 	}
 
 	public Object authenticate(final String iUserName, final String iUserPasswd, final Map<String, String> iParameters) throws AuthenticationException {
 		BaseAccountRepository repository = Roma.repository(BaseAccount.class);
 
 		QueryByFilter filter = new QueryByFilter(BaseAccount.class);
 		filter.addItem("name", QueryByFilter.FIELD_EQUALS, iUserName);
 		filter.setMode(PersistenceAspect.FULL_MODE_LOADING);
 		filter.setStrategy(PersistenceAspect.STRATEGY_DETACHING);
 		BaseAccount account = repository.findFirstByCriteria(filter);
 		if (account == null) {
 			String iMessage = Roma.i18n().get("UsersAuthentication.accountNotFound.label", iUserName);
 
 			// TODO:REMOVE THIS STATEMENT EXIST ONLY FOR BACKWARD COMPATIBILITY
 			if (iMessage == null)
 				iMessage = "User or Password not correct";
 
 			throwException(iMessage);
 		}
 		try {
 			if (!checkPassword(account.getPassword(), iUserPasswd)) {
 				String iMessage = Roma.i18n().get("UsersAuthentication.wrongPassword.label", iUserName);
 
 				// TODO:REMOVE THIS STATEMENT EXIST ONLY FOR BACKWARD
 				// COMPATIBILITY
 				if (iMessage == null)
 					iMessage = "User or Password not correct";
 				throwException(iMessage);
 			}
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 
 		QueryByFilter byFilter = new QueryByFilter(BaseAccountStatus.class);
 		byFilter.addItem("name", QueryByFilter.FIELD_EQUALS, UsersInfoConstants.STATUS_UNACTIVE);
 		BaseAccountStatus accountStatusInactive = Roma.context().persistence().queryOne(byFilter);
 		if (AccountManagementUtility.isAccountExpired(account)) {
 			account.setStatus(accountStatusInactive);
 			account.setSignedOn(null);
 			account = repository.update(account, PersistenceAspect.STRATEGY_DETACHING);
 		}
 		QueryByFilter byFilterAct = new QueryByFilter(BaseAccountStatus.class);
 		byFilterAct.addItem("name", QueryByFilter.FIELD_EQUALS, UsersInfoConstants.STATUS_ACTIVE);
 		BaseAccountStatus accountStatus = Roma.context().persistence().queryOne(byFilterAct);
 		if (account.getStatus() == null || !account.getStatus().equals(accountStatus)) {
 			String iMessage = Roma.i18n().get("UsersAuthentication.accountDisabled.label", iUserName);
 
 			// TODO:REMOVE THIS STATEMENT EXIST ONLY FOR BACKWARD COMPATIBILITY
 			if (iMessage == null)
 				iMessage = "Account " + iUserName + " is not active";
 			throwException(iMessage);
 		}
 
 		if (isSingleSessionPerUser()) {
 			dropExistingSessions(account);
 		}
 
 		account.setSignedOn(new Date());
 		account = repository.update(account, PersistenceAspect.STRATEGY_DETACHING);
 
 		Roma.session().getActiveSessionInfo().setAccount(account);
 
 		return account;
 	}
 
 	protected void dropExistingSessions(BaseAccount account) {
 		/*
 		 * for (SessionInfo session : ActiveSessionHelper.getActiveSessions()) { if (account.equals(session.getAccount())) {
 		 * Roma.session().destroyCurrentSession(session.getSystemSession()); } }
 		 */
 	}
 
 	public boolean checkPassword(String iPassword, String iPasswordToCheck) throws NoSuchAlgorithmException {
 		if (getEncryptionAlgorithm() == null) {
 			// NO ALGORITHM: SIMPLY CHECK IF PASSWORD ARE THE SAME AS STRINGS =
 			// NO
 			// ENCRYPTION
 			if (iPassword == null && iPasswordToCheck == null)
 				return true;
 
 			return iPasswordToCheck != null && iPassword != null && iPasswordToCheck.equals(iPassword);
 		} else {
 			// USE THE ALGORITHM RECEIVED
 			return encryptPassword(iPasswordToCheck).equals(iPassword);
 		}
 	}
 
 	protected void throwException(String iMessage) throws AuthenticationException {
 		// WAIT A BIT TO PREVENT BRUTE FORCE ATTACKS
 		try {
 			Thread.sleep(ERROR_SLEEP_TIME);
 		} catch (InterruptedException e) {
 		}
 
 		throw new AuthenticationException(iMessage);
 	}
 
 	/**
 	 * Implement the algorithm that check if a function is allowed for a user profile. It get the user's profile and go up until the
 	 * root.
 	 */
 	public boolean allow(Object iProfile, String iFunctionName) {
 		if (iProfile == null) {
 			iProfile = getAnonymousProfile();
 		}
 		if (iProfile == null)
 			return true;
 
 		BaseProfile userProfile = (BaseProfile) iProfile;
 
 		// INSERT ALL PROFILE CHAIN IN A VECTOR TO BE BROWSED JUST AFTER
 		ArrayList<BaseProfile> profiles = new ArrayList<BaseProfile>();
 		BaseProfile profile = userProfile;
 		while (profile != null) {
 			profiles.add(profile);
 			profile = profile.getParent();
 		}
 
 		Mode mode;
 		if (userProfile.getParent() != null) {
 			// GET AS INITIAL MODE THE ROOT MODE
 			mode = profiles.get(profiles.size() - 1).getMode();
 		} else
 			mode = userProfile.getMode();
 
 		boolean allowed = mode != null && mode == BaseProfile.Mode.ALLOW_ALL_BUT;
 		BaseFunction function;
 
 		// BROWSE ALL PROFILES CHECKING FOR THE FUNCTION. IT STARTS FROM CURRENT
 		// AND
 		// GO UP
 		for (BaseProfile profIter : profiles) {
 			if (profIter.getFunctions() == null)
 				continue;
 
 			function = profIter.getFunctions().get(iFunctionName);
 			if (function != null) {
 				// FUNCTION FOUND: GET ALLOW MODE AND BREAK
 				allowed = function.isAllow();
 				break;
 			}
 		}
 
 		return allowed;
 	}
 
 	public void logout() throws AuthenticationException {
 	}
 
 	public boolean allowClass(SchemaClass iClass) {
 		if (iClass == null) {
 			return true;
 		}
 		if (!status.equals(STATUS_UP))
 			return true;
 
 		BaseProfile profile = getCurrentProfile();
 
 		return allow(profile, iClass.getName());
 	}
 
 	private boolean allow(String iFunctionName) {
 		BaseProfile profile = getCurrentProfile();
 		return allow(profile, iFunctionName);
 
 	}
 
 	public boolean allowField(SchemaField iField) {
 		if (!status.equals(STATUS_UP))
 			return true;
 
 		return allow(iField.getFullName());
 	}
 
 	public boolean allowAction(SchemaAction iAction) {
 		if (!status.equals(STATUS_UP))
 			return true;
 
 		return allow(iAction.getFullName());
 	}
 
 	public boolean allowEvent(SchemaEvent iEvent) {
 		if (!status.equals(STATUS_UP))
 			return true;
 
 		return allow(iEvent.getFullName());
 	}
 
 	public BaseProfile getCurrentProfile() {
 		BaseAccount account = (BaseAccount) getCurrentAccount();
 
 		if (account == null) {
 			return getAnonymousProfile();
 		} else
 			return account.getProfile();
 	}
 
 	public void onSessionCreating(SessionInfo iSession) {
 	}
 
 	public void onSessionDestroying(SessionInfo iSession) {
 		logout();
 	}
 
 	@Override
 	public void startup() {
 
 		super.startup();
 	}
 
 	/**
 	 * Return the profile for the anonymous user. It use a lazy-loading mechanism caching the detached instance.
 	 * 
 	 * @return
 	 */
 	private BaseProfile getAnonymousProfile() {
 		if (loadedAnonymousProfile)
 			return null;
 
 		if (!loadedAnonymousProfile && anonymousProfile == null) {
 			synchronized (this) {
 				if (anonymousProfile == null) {
 					QueryByFilter query = new QueryByFilter(BaseProfile.class);
 					query.addItem("name", QueryByFilter.FIELD_EQUALS, ANONYMOUS_PROFILE_NAME);
 					query.setMode(PersistenceAspect.FULL_MODE_LOADING);
 					query.setStrategy(PersistenceAspect.STRATEGY_DETACHING);
 					anonymousProfile = Roma.context().persistence().queryOne(query);
 					loadedAnonymousProfile = true;
 				}
 			}
 		}
 
 		return anonymousProfile;
 	}
 
 	public boolean isSingleSessionPerUser() {
 		return singleSessionPerUser;
 	}
 
 	public void setSingleSessionPerUser(boolean singleSessionPerUser) {
 		this.singleSessionPerUser = singleSessionPerUser;
 	}
 
 	public Object onAfterFieldRead(Object iContent, SchemaField iField, Object iCurrentValue) {
 		if (iCurrentValue instanceof Collection<?>) {
 			Iterator<?> iter = ((Collection<?>) iCurrentValue).iterator();
 			while (iter.hasNext()) {
 				Object o = iter.next();
 				if (o != null && !allowClass(Roma.schema().getSchemaClass(o.getClass())))
 					iter.remove();
 			}
 		}
 
 		if (iCurrentValue instanceof Map<?, ?>) {
 			Map<?, ?> map = (Map<?, ?>) iCurrentValue;
 
 			Object key;
 			Iterator<?> iterator = map.entrySet().iterator();
 			while (iterator.hasNext()) {
 				Map.Entry<?, ?> iter = (Map.Entry<?, ?>) iterator.next();
 				key = iter.getKey();
 
 				// CHECK THE KEY
 				if (key != null && !allowClass(Roma.schema().getSchemaClass(key.getClass())))
 					iterator.remove();
 				// CHECK THE VALUE
 				else if (iter.getValue() != null && !allowClass(Roma.schema().getSchemaClass(iter.getValue().getClass())))
 					iterator.remove();
 			}
 		}
 		return iCurrentValue;
 	}
 
 	public Object onAfterFieldWrite(Object iContent, SchemaField iField, Object iCurrentValue) {
 		return iCurrentValue;
 	}
 
 	public Object onBeforeFieldRead(Object iContent, SchemaField iField, Object iCurrentValue) {
 		return IGNORED;
 	}
 
 	public Object onBeforeFieldWrite(Object iContent, SchemaField iField, Object iCurrentValue) {
		return IGNORED;
 	}
 }
