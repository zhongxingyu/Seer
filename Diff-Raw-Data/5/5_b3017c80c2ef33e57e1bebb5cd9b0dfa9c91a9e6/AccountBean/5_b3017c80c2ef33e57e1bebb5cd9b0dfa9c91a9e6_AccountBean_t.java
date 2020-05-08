 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.main;
 
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.SelectBean;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.faces.model.FxJSFSelectItem;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import static com.flexive.shared.EJBLookup.getAccountEngine;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.interfaces.UserGroupEngine;
 import com.flexive.shared.security.*;
 import com.flexive.war.beans.admin.content.BeContentEditorBean;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.component.UISelectBoolean;
 import javax.faces.model.SelectItem;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 
 /**
  * Management of accounts.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class AccountBean implements Serializable {
     private static final long serialVersionUID = -5857559146644805299L;
     private static final Log LOG = LogFactory.getLog(AccountBean.class);
 
     private transient UISelectBoolean activeFilterCheckbox;
     private transient UISelectBoolean validatedFilterCheckbox;
 
     private long groupFilter = -1;
     private long accountIdFilter = -1;
     private boolean activeFilter = true;
     private boolean validatedFilter = true;
     private boolean showOldPassword;
     private String oldPassword = "";
     private String password;
     private String passwordConfirm;
     private AccountEditBean account;
     private final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");
     private Long[] groups;
     private Long[] roles;
     private Mandator mandator;
     private long mandatorFilter = -1;
     private Hashtable<String, List<Account>> listCache;
     private static final String ID_CACHE_KEY = AccountBean.class + "_id";
     private FxContent contactData;
     private boolean languageChanged;
 
     // user preferences fields
     private FxLanguage defaultInputLanguage;
 
     public List<Role> getRoles() {
         if (roles == null) {
             return new ArrayList<Role>(0);
         }
         List<Role> res = new ArrayList<Role>(roles.length);
         for (long r : roles)
             res.add(Role.getById(r));
         return res;
     }
 
     public static class AccountEditBean extends Account {
         private static final long serialVersionUID = -5550483398375933412L;
 
         private AccountEditBean() {
         }
 
         public AccountEditBean(Account other) {
             super(other);
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public void setLoginName(String loginName) {
             this.loginName = loginName;
         }
 
         public void setId(long id) {
             this.id = id;
         }
 
         public void setMandatorId(long mandatorId) {
             this.mandatorId = mandatorId;
         }
 
         public void setEmail(String email) {
             this.email = email;
         }
 
         public void setLanguage(FxLanguage language) {
             this.language = language;
         }
 
         public void setActive(boolean active) {
             this.active = active;
         }
 
         public void setValidated(boolean validated) {
             this.validated = validated;
         }
 
         public void setValidFrom(Date validFrom) {
             this.validFrom = validFrom;
         }
 
         public void setValidTo(Date validTo) {
             this.validTo = validTo;
         }
 
         public void setDefaultNodeId(long defaultNodeId) {
             this.defaultNodeId = defaultNodeId;
         }
 
         public void setDescription(String description) {
             this.description = description;
         }
 
         public void setContactDataId(long contactDataId) {
             this.contactDataId = contactDataId;
         }
 
         public void setAllowMultiLogin(boolean allowMultiLogin) {
             this.allowMultiLogin = allowMultiLogin;
         }
 
         public void setUpdateToken(String updateToken) {
             this.updateToken = updateToken;
         }
 
         public void setLifeCycleInfo(LifeCycleInfo lifeCycleInfo) {
             this.lifeCycleInfo = lifeCycleInfo;
         }
     }
 
     public Long[] getRolesIds() {
         return this.roles;
     }
 
     public void setRolesIds(Long[] roles) {
         this.roles = roles;
     }
 
     public boolean isShowOldPassword() {
         showOldPassword = FxJsfUtils.getRequest().getUserTicket().getUserId() == account.getId();
         return showOldPassword;
     }
 
     public void setShowOldPassword(boolean showOldPassword) {
         this.showOldPassword = showOldPassword;
     }
 
     /**
      * Has the language setting changed?
      * USed to reload the whole UI after changes
      *
      * @return language setting changed
      */
     public boolean isLanguageChanged() {
         return languageChanged;
     }
 
     /**
      * Reset the languageChanged flag to false
      *
      * @return reset the languageChanged flag
      */
     public boolean isResetLanguageChanged() {
         languageChanged = false;
         return languageChanged;
     }
 
     /**
      * Getter for roles implicitly set from groups
      *
      * @return roles implicitly set from groups
      * @throws FxApplicationException on errors
      */
     public List<Role> getRolesGroups() throws FxApplicationException {
         if (groups == null)
             return new ArrayList<Role>(0);
         List<Role> result = new ArrayList<Role>(Role.values().length);
         for (long groupId : groups) {
             List<Role> tmp = EJBLookup.getUserGroupEngine().getRoles(groupId);
             result.removeAll(tmp);
             result.addAll(tmp);
         }
         return result;
     }
 
     public Long[] getGroups() {
         return groups == null ? new Long[0] : groups;
     }
 
     public void setGroups(Long[] groups) {
         this.groups = groups;
     }
 
 
     public long getAccountIdFilter() {
         return accountIdFilter;
     }
 
     public void setAccountIdFilter(long accountIdFilter) {
         this.accountIdFilter = accountIdFilter;
         FxJsfUtils.setSessionAttribute(ID_CACHE_KEY, this.accountIdFilter);
     }
 
     /**
      * Constructor.
      */
     public AccountBean() {
         listCache = new Hashtable<String, List<Account>>(5);
         resetFilter();
     }
 
     private void resetAccount() {
         account = new AccountEditBean();
     }
 
     private void resetFilter() {
         resetAccount();
         groupFilter = -1;
         groups = null;
         roles = null;
         activeFilter = true;
         validatedFilter = true;
         UserTicket ticket = FxContext.getUserTicket();
         if (!ticket.isGlobalSupervisor()) {
             mandator = CacheAdmin.getFilteredEnvironment().getMandator(ticket.getMandatorId());
         }
         try {
             // Default valid from: Today, but delete time part
             account.setValidFrom(SDF.parse(SDF.format(new Date())));
             // Default valid to: Way in the future
             account.setValidTo(SDF.parse("01-01-3000"));
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
     }
 
     public boolean isActiveFilter() {
         if (activeFilterCheckbox != null && activeFilterCheckbox.getSubmittedValue() != null) {
             // use submitted value during postbacks to apply the correct filters
            return Boolean.parseBoolean(activeFilterCheckbox.getSubmittedValue().toString());
         }
         return activeFilter;
     }
 
     public void setActiveFilter(boolean activeFilter) {
         this.activeFilter = activeFilter;
     }
 
     public boolean isValidatedFilter() {
         if (validatedFilterCheckbox != null && validatedFilterCheckbox.getSubmittedValue() != null) {
             // use submitted value during postbacks to apply the correct filters
            return Boolean.parseBoolean(validatedFilterCheckbox.getSubmittedValue().toString());
         }
         return validatedFilter;
     }
 
     public void setValidatedFilter(boolean validatedFilter) {
         this.validatedFilter = validatedFilter;
     }
 
     public long getMandatorFilter() {
         return mandatorFilter;
     }
 
     public void setMandatorFilter(long mandatorFilter) {
         this.mandatorFilter = mandatorFilter;
     }
 
     public Mandator getMandator() {
         if (this.mandator == null) {
             List<SelectItem> mandators = FxJsfUtils.getManagedBean(SelectBean.class).getMandatorsForEditNoEmpty();
             if (mandators.size() > 0)
                 setMandator(CacheAdmin.getEnvironment().getMandator((Long) mandators.get(0).getValue()));
         }
         return mandator;
     }
 
     public void setMandator(Mandator mandator) {
         this.mandator = mandator;
         this.account.setMandatorId(mandator == null ? -1 : mandator.getId());
     }
 
     public long getMandatorId() {
         return getMandator().getId();
     }
 
     public void setMandatorId(long mandatorId) {
         if (mandatorId == -1)
             this.mandator = null;
         else
             this.mandator = CacheAdmin.getEnvironment().getMandator(mandatorId);
         this.account.setMandatorId(mandatorId);
     }
 
     public long getGroupFilter() {
         return groupFilter;
     }
 
     public void setGroupFilter(long groupFilter) {
         this.groupFilter = groupFilter;
     }
 
     public String getOldPassword() {
         return oldPassword;
     }
 
     public void setOldPassword(String oldPassword) {
         this.oldPassword = oldPassword;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPasswordConfirm() {
         return passwordConfirm;
     }
 
     public void setPasswordConfirm(String passwordConfirm) {
         this.passwordConfirm = passwordConfirm;
     }
 
     public Account getAccount() {
         return account;
     }
 
 
     public void setAccount(Account account) {
         this.account = new AccountEditBean(account);
     }
 
     public UISelectBoolean getActiveFilterCheckbox() {
         return activeFilterCheckbox;
     }
 
     public void setActiveFilterCheckbox(UISelectBoolean activeFilterCheckbox) {
         this.activeFilterCheckbox = activeFilterCheckbox;
     }
 
     public UISelectBoolean getValidatedFilterCheckbox() {
         return validatedFilterCheckbox;
     }
 
     public void setValidatedFilterCheckbox(UISelectBoolean validatedFilterCheckbox) {
         this.validatedFilterCheckbox = validatedFilterCheckbox;
     }
 
     public FxLanguage getDefaultInputLanguage() throws FxApplicationException {
         if (defaultInputLanguage == null && account != null) {
             return account.getLanguage();
         }
         return defaultInputLanguage;
     }
 
     public void setDefaultInputLanguage(FxLanguage defaultInputLanguage) {
         this.defaultInputLanguage = defaultInputLanguage;
     }
 
     /**
      * Returns all user groups defined for the current mandator that are not flagged as system.
      *
      * @return all user groups defined for the current mandator.
      * @throws FxApplicationException if the user groups could not be fetched successfully.
      */
     public List<SelectItem> getFilteredUserGroups() throws FxApplicationException {
         long mandatorId;
         if (getAccount().isNew()) {
             if (getMandator() == null)
                 return new ArrayList<SelectItem>(0);
             else
                 mandatorId = getMandator().getId();
         } else
             mandatorId = getAccount().getMandatorId();
         UserGroupEngine groupEngine = EJBLookup.getUserGroupEngine();
         List<UserGroup> groups = groupEngine.loadAll(mandatorId);
         List<SelectItem> userGroupsNonSystem = new ArrayList<SelectItem>(groups.size());
         for (UserGroup group : groups) {
             if (group.isSystem())
                 continue;
             userGroupsNonSystem.add(new FxJSFSelectItem(group));
         }
         return userGroupsNonSystem;
     }
 
     /**
      * Force an initialization of the current user preferences if the bean is not initialized
      *
      * @return dummy
      */
     public String getEditUserPref() {
         if (this.account == null || this.account.getId() == -1) {
             editUserPref();
         }
         return null;
     }
 
     public String getParseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 // no action requested
                 return null;
             }
             // hack!
             FxJsfUtils.resetFaceletsComponent("listForm");
 
             if ("editUserPref".equals(action)) {
                 editUserPref();
             }
         } catch (Exception e) {
             // TODO possibly pass some error message to the HTML page
             LOG.error("Failed to parse request parameters: " + e.getMessage(), e);
         }
         return null;
     }
 
     public String showContactData() {
         BeContentEditorBean ceb = (BeContentEditorBean) FxJsfUtils.getManagedBean("beContentEditorBean");
         ceb.initEditor(this.account.getContactData(), true);
         return "showContentEditor";
     }
 
     /**
      * Deletes a user, with the id specified by accountIdFiler.
      *
      * @return the next pageto render
      */
     public String deleteUser() {
         try {
             ensureAccountIdSet();
             getAccountEngine().remove(accountIdFilter);
             new FxFacesMsgInfo("User.nfo.deleted").addToContext();
             listCache.clear();
             resetFilter();
         } catch (Exception exc) {
             resetAccount();
             new FxFacesMsgErr(exc).addToContext();
         }
         return "accountOverview";
     }
 
     private void ensureAccountIdSet() {
         if (this.accountIdFilter <= 0) {
             this.accountIdFilter = (Long) FxJsfUtils.getSessionAttribute(ID_CACHE_KEY);
         }
     }
 
     /**
      * Loads the user specified by the parameter accountIdFilter.
      *
      * @return the next page to render
      */
     public String editUser() {
         try {
             ensureAccountIdSet();
             this.account = new AccountEditBean(getAccountEngine().load(this.accountIdFilter));
             this.showOldPassword = FxJsfUtils.getRequest().getUserTicket().getUserId() == account.getId();
             List<Role> r = getAccountEngine().getRoles(this.accountIdFilter, RoleLoadMode.FROM_USER_ONLY);
             this.roles = new Long[r.size()];
             for (int i = 0; i < this.roles.length; i++)
                 this.roles[i] = r.get(i).getId();
             List<UserGroup> g = getAccountEngine().getGroups(this.accountIdFilter);
             this.groups = new Long[g.size()];
             for (int i = 0; i < this.groups.length; i++)
                 this.groups[i] = g.get(i).getId();
             return "accountEdit";
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return "accountEdit";
         }
     }
 
     /**
      * Loads the account of the current user
      *
      * @return the next page to render
      */
     public String editUserPref() {
         try {
             this.account = new AccountEditBean(getAccountEngine().load(FxContext.getUserTicket().getUserId()));
             setAccountIdFilter(this.account.getId());
             List<Role> r = getAccountEngine().getRoles(this.accountIdFilter, RoleLoadMode.ALL);
             this.roles = new Long[r.size()];
             for (int i = 0; i < roles.length; i++)
                 roles[i] = r.get(i).getId();
             List<UserGroup> g = getAccountEngine().getGroups(this.accountIdFilter);
             this.groups = new Long[g.size()];
             for (int i = 0; i < this.groups.length; i++)
                 this.groups[i] = g.get(i).getId();
             this.contactData = null;
             this.contactData = EJBLookup.getContentEngine().load(this.account.getContactData());
             // load configuration parameters
             final long inputLanguageId = EJBLookup.getConfigurationEngine().get(SystemParameters.USER_DEFAULTINPUTLANGUAGE);
             if (inputLanguageId != -1) {
                 this.defaultInputLanguage = EJBLookup.getLanguageEngine().load(inputLanguageId);
             }
             return "userEdit";
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return null;
         }
     }
 
     public FxContent getContactData() {
         return contactData;
     }
 
     /**
      * Navigate back to the overview and reset all form data
      *
      * @return overview oage
      */
     public String overview() {
         //keep filter but reset account data
         resetAccount();
         return "accountOverview";
     }
 
     /**
      * save the user settings
      *
      * @return  content page
      */
     public String saveUserPref() {
         try {
             String newPasswd = null;
 
             // Determine if the password must be set (and check the old one)
             if (password != null && password.trim().length() > 0) {
                 try {
                     if (StringUtils.isBlank(oldPassword) || !getAccountEngine().loginCheck(account.getName(), oldPassword, FxJsfUtils.getRequest().getUserTicket())) {
                         new FxFacesMsgErr("User.err.noOldPasswordMatch").addToContext();
                         return editUserPref();
                     }
                 } catch (FxLoginFailedException e) {
                     new FxFacesMsgErr("User.err.settingsNotSaved").addToContext();
                     new FxFacesMsgErr(e).addToContext();
                     return editUserPref();
                 }
                 // actual pw
                 if (!password.equals(passwordConfirm)) {
                     new FxFacesMsgErr("User.err.passwordsDontMatch").addToContext();
                     return editUserPref();
                 }
                 newPasswd = password;
             }
             getAccountEngine().updateUser(this.accountIdFilter, newPasswd, null, null, this.account.getEmail(), this.account.getLanguage().getId());
             languageChanged = true; //currently a "fake" ...
             // update user configuration
             if (getDefaultInputLanguage() != null) {
                 EJBLookup.getUserConfigurationEngine().put(SystemParameters.USER_DEFAULTINPUTLANGUAGE, getDefaultInputLanguage().getId());
             }
             new FxFacesMsgInfo("User.nfo.settingsSaved").addToContext();
         } catch (FxApplicationException e) {
             new FxFacesMsgErr("User.err.settingsNotSaved").addToContext();
             new FxFacesMsgErr(e).addToContext();
             return editUserPref();
         }
         FxContext.get()._reloadUserTicket();
         return "editUserPref";
     }
 
     /**
      * Action method to save the user settings
      * 
      * @return edit user page
      */
     public String saveUser() {
         try {
             String newPasswd = null;
 
             // check if the old password was entered correctly (only upon setting a new one)
             if (isShowOldPassword() && (password != null && password.trim().length() > 0)) {
                 try {
                     if (StringUtils.isBlank(oldPassword) || !getAccountEngine().loginCheck(account.getName(), oldPassword, FxJsfUtils.getRequest().getUserTicket())) {
                         new FxFacesMsgErr("User.err.noOldPasswordMatch").addToContext();
                         return "accountEdit";
                     }
                 } catch (FxLoginFailedException e) {
                     new FxFacesMsgErr("User.err.settingsNotSaved").addToContext();
                     new FxFacesMsgErr(e).addToContext();
                     return "accountEdit";
                 }
             }
 
             // Determine if the password must be set
             if (password != null && password.trim().length() > 0) {
                 if (!password.equals(passwordConfirm)) {
                     new FxFacesMsgErr("User.err.passwordsDontMatch").addToContext();
                     return "accountEdit";
                 }
                 newPasswd = password;
             }
 
             // Update the user
             getAccountEngine().update(this.accountIdFilter, newPasswd, null, null, null, this.account.getEmail(),
                     this.account.isValidated(), this.account.isActive(), this.account.getValidFrom(),
                     this.account.getValidTo(), this.account.getLanguage().getId(), this.account.getDescription(),
                     this.account.isAllowMultiLogin(), this.account.getContactData().getId());
             new FxFacesMsgInfo("User.nfo.saved").addToContext();
             // Assign the given groups to the account
             try {
                 getAccountEngine().setGroups(this.accountIdFilter, ArrayUtils.toPrimitive(this.groups));
             } catch (Exception exc) {
                 new FxFacesMsgErr(exc).addToContext();
             }
 
             // Assign the given roles to the account
             try {
                 getAccountEngine().setRoles(this.accountIdFilter, getRoles());
             } catch (Exception exc) {
                 new FxFacesMsgErr(exc).addToContext();
             }
             // Reload and display
             return editUser();
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return "accountEdit";
         }
     }
 
     /**
      * Creates a new user from the beans data.
      *
      * @return the next jsf page to render
      */
     public String createUser() {
 
         boolean hasError = false;
 
         // Param check
         if (password == null) {
             password = "";
         }
 
         // Passwords must match
         if (!password.equals(passwordConfirm)) {
             new FxFacesMsgErr("User.err.passwordsDontMatch").addToContext();
             hasError = true;
         }
 
         // Mandator select and check
         final UserTicket ticket = FxContext.getUserTicket();
         long mandatorId = ticket.isGlobalSupervisor() ? account.getMandatorId() : ticket.getMandatorId();
         if (mandatorId < 0) {
             FxFacesMsgErr msg = new FxFacesMsgErr("User.err.mandatorMissing");
             msg.setId("mandator");
             msg.addToContext();
             hasError = true;
         }
 
         // If we have an error abort
         if (hasError) {
             return "accountCreate";
         }
 
         // Create the new account itself
         try {
             setAccountIdFilter(getAccountEngine().create(account, password));
             account = new AccountEditBean(getAccountEngine().load(this.accountIdFilter));
             new FxFacesMsgInfo("User.nfo.saved", account.getName()).addToContext();
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
             return "accountCreate";
         }
 
         // Assign the given groups to the account
         try {
             getAccountEngine().setGroups(this.accountIdFilter, ArrayUtils.toPrimitive(this.groups));
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
 
         // Assign the given roles to the account
         try {
             getAccountEngine().setRoles(this.accountIdFilter, getRoles());
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         listCache.clear();
         resetFilter();
 
         return "accountOverview";
     }
 
 
     /**
      * Retrieves all users, filtering by loginName, userName, email, active, vaidated and mandatorFilter.
      *
      * @return all users matching the filter criterias.
      */
     public List<Account> getList() {
         final UserTicket ticket = FxContext.getUserTicket();
         try {
             long _mandatorFilter = getMandatorFilter();
 
             // If not supervisor fallback to the own mandator
             if (!ticket.isGlobalSupervisor()) {
                 _mandatorFilter = ticket.getMandatorId();
             }
 
             long[] userGroupIds = new long[1];
             if (groupFilter != -1) {
                 userGroupIds[0] = groupFilter;
             } else {
                 userGroupIds = null;
             }
 
             // Load list if needed, and cache within the request
             String cacheKey = account.getName() + "_" + account.getLoginName() + "_" +
                     account.getEmail() + "_" + isActiveFilter() + "_" + isValidatedFilter() + "_" + _mandatorFilter + "_" +
                     StringUtils.join(ArrayUtils.toObject(userGroupIds), ',');
             List<Account> result = listCache.get(cacheKey);
             if (result == null) {
                 result = getAccountEngine().loadAll(account.getName(), account.getLoginName(),
                         account.getEmail(), isActiveFilter() ? true : null, isValidatedFilter() ? true : null, _mandatorFilter,
                         null, userGroupIds, 0, -1);
                 listCache.put(cacheKey, result);
             }
             return result;
 
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
             return new ArrayList<Account>(0);
         }
     }
 
 }
