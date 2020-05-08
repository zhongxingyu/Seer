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
 
 package org.romaframework.module.users.install;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.romaframework.aspect.persistence.PersistenceAspect;
 import org.romaframework.aspect.persistence.QueryByFilter;
 import org.romaframework.core.Roma;
 import org.romaframework.core.install.ApplicationInstaller;
 import org.romaframework.module.users.ActivityLogCategories;
 import org.romaframework.module.users.UsersAuthentication;
 import org.romaframework.module.users.UsersHelper;
 import org.romaframework.module.users.UsersInfoConstants;
 import org.romaframework.module.users.domain.ActivityLogCategory;
 import org.romaframework.module.users.domain.BaseAccount;
 import org.romaframework.module.users.domain.BaseAccountStatus;
 import org.romaframework.module.users.domain.BaseFunction;
 import org.romaframework.module.users.domain.BaseProfile;
 import org.romaframework.module.users.domain.Realm;
 import org.romaframework.module.users.repository.ActivityLogCategoryRepository;
 import org.romaframework.module.users.repository.BaseAccountRepository;
 import org.romaframework.module.users.repository.BaseAccountStatusRepository;
 
 public class UsersApplicationInstaller extends ApplicationInstaller {
 
 	public static final String	PROFILE_ADMINISTRATOR	= "Administrator";
 	public static final String	PROFILE_BASIC					= "Basic";
 	public static final String	ACCOUNT_ADMIN					= "admin";
 	public static final String	ACCOUNT_USER					= "user";
 	public static final String	ACCOUNT_SEPARATOR			= ".";
 
 	protected Realm							realm;
 	protected BaseProfile				pAnonymous;
 	protected BaseProfile				pAdmin;
 	protected BaseProfile				pBasic;
 	protected BaseAccountStatus	defStatus;
 
 	public UsersApplicationInstaller() {
 	}
 
 	@Override
	public boolean alreadyInstalled() {
		return Roma.component(BaseAccountRepository.class).countByCriteria(new QueryByFilter(BaseAccount.class)) != 0;
 	}
 
 	@Override
 	public synchronized void install() {
 
 		PersistenceAspect db = Roma.context().persistence();
 
 		createStatuses(db);
 		createProfiles();
 		try {
 			createAccounts();
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public synchronized void install(Object obj) {
 		realm = (Realm) obj;
 		install();
 	}
 
 	protected void createStatuses(PersistenceAspect db) {
 
 		BaseAccountStatusRepository repoStatus = Roma.component(BaseAccountStatusRepository.class);
 		defStatus = repoStatus.create(new BaseAccountStatus(UsersInfoConstants.STATUS_ACTIVE), PersistenceAspect.STRATEGY_DETACHING);
 		repoStatus.create(new BaseAccountStatus(UsersInfoConstants.STATUS_UNACTIVE));
 		repoStatus.create(new BaseAccountStatus(UsersInfoConstants.STATUS_SUSPENDED));
 		ActivityLogCategoryRepository repoCategory = Roma.component(ActivityLogCategoryRepository.class);
 		repoCategory.create(new ActivityLogCategory(ActivityLogCategories.CATEGORY_SYSTEM));
 		repoCategory.create(new ActivityLogCategory(ActivityLogCategories.CATEGORY_LOGIN));
 		repoCategory.create(new ActivityLogCategory(ActivityLogCategories.CATEGORY_ADMIN));
 	}
 
 	protected void createAccounts() throws NoSuchAlgorithmException {
 		BaseAccount aAdmin = new BaseAccount(realm);
 		if (realm == null) {
 			aAdmin.setName(ACCOUNT_ADMIN);
 			aAdmin.setPassword(ACCOUNT_ADMIN);
 		} else {
 			aAdmin.setName(ACCOUNT_ADMIN + ACCOUNT_SEPARATOR + realm);
 			aAdmin.setPassword(ACCOUNT_ADMIN + ACCOUNT_SEPARATOR + realm);
 		}
 		aAdmin.setSignedOn(new Date());
 		aAdmin.setStatus(defStatus);
 		aAdmin.setLastModified(aAdmin.getSignedOn());
 		aAdmin.setProfile(pAdmin);
 
 		UsersHelper.getInstance().setAccount(aAdmin);
 		BaseAccount uUser = new BaseAccount(realm);
 		if (realm == null) {
 			uUser.setName(ACCOUNT_USER);
 			uUser.setPassword(ACCOUNT_USER);
 		} else {
 			uUser.setName(ACCOUNT_USER + ACCOUNT_SEPARATOR + realm);
 			uUser.setPassword(ACCOUNT_USER + ACCOUNT_SEPARATOR + realm);
 		}
 		uUser.setSignedOn(new Date());
 		uUser.setStatus(defStatus);
 		uUser.setLastModified(uUser.getSignedOn());
 		uUser.setProfile(pBasic);
 
 		UsersHelper.getInstance().setAccount(uUser);
 	}
 
 	protected void createProfiles() {
 		pAnonymous = new BaseProfile(realm);
 		pAnonymous.setName(UsersAuthentication.ANONYMOUS_PROFILE_NAME);
 		pAnonymous.setHomePage("HomePage");
 		pAnonymous.setFunctions(new HashMap<String, BaseFunction>());
 		pAnonymous.setMode(BaseProfile.MODE_ALLOW_ALL_BUT);
 		UsersHelper.getInstance().setProfile(pAnonymous);
 
 		pAdmin = new BaseProfile();
 		if (realm == null) {
 			pAdmin.setName(PROFILE_ADMINISTRATOR);
 		} else {
 			pAdmin.setName(PROFILE_ADMINISTRATOR + ACCOUNT_SEPARATOR + realm);
 		}
 		pAdmin.setHomePage("HomePageAdmin");
 		pAdmin.setMode(BaseProfile.MODE_ALLOW_ALL_BUT);
 		UsersHelper.getInstance().setProfile(pAdmin);
 
 		pBasic = new BaseProfile();
 		if (realm == null) {
 			pBasic.setName(PROFILE_BASIC);
 		} else {
 			pBasic.setName(PROFILE_BASIC + ACCOUNT_SEPARATOR + realm);
 		}
 		pBasic.setHomePage("HomePage");
 		pBasic.setMode(BaseProfile.MODE_ALLOW_ALL_BUT);
 		UsersHelper.getInstance().setProfile(pBasic);
 	}
 
 	/**
 	 * @return the realm
 	 */
 	public Realm getRealm() {
 		return realm;
 	}
 
 	/**
 	 * @param realm
 	 *          the realm to set
 	 */
 	public void setRealm(Realm realm) {
 		this.realm = realm;
 	}
 }
