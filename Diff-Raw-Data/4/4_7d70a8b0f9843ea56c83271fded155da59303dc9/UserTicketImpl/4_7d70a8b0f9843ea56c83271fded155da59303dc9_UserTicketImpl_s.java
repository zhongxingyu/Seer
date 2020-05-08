 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.core.security;
 
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.AccountEngine;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.FxEnvironment;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Implementation of the interface UserTicket.<br>
  * The UserTicket caches informations about a user.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class UserTicketImpl implements UserTicket, Serializable {
     private static final long serialVersionUID = -8780256553138578843L;
     private static final Log LOG = LogFactory.getLog(UserTicketImpl.class);
 
     //private static final Log LOG = LogFactory.getLog(UserTicketImpl.class);
     private final String userName;
     private final String loginName;
     private final long userId;
     private final boolean multiLogin;
     private final long mandator;
     private final String applicationId;
     private final boolean webDav;
     private FxLanguage language;
 
     private boolean globalSupervisor;
     private boolean mandatorSupervisor;
     private long[] groups;
     private Role[] roles;
     private boolean dirty;
     private ACLAssignment[] assignments;
     private long creationTime;
     private FxPK contactData;
 
     private static volatile long STRUCTURE_TIMESTAMP = -1;
     private static ACLAssignment[] guestACLAssignments = null;
     private static Role[] guestRoles = null;
     private static long[] guestGroups = null;
     private static FxPK guestContactData = new FxPK(1); // will be updated when the environment is available
     private long failedLoginAttempts = 0;
     private AuthenticationSource authenticationSource = AuthenticationSource.None;
 
     /**
      * Returns a guest ticket, based on the request information data.
      * <p/>
      * The guest ticket always belong to the MANDATOR_PUBLIC.
      * <p/>
      *
      * @return the guest ticket
      */
     public static UserTicket getGuestTicket() {
         FxContext si = FxContext.get();
         if (CacheAdmin.isEnvironmentLoaded()) {
             final FxEnvironment environment = CacheAdmin.getEnvironment();
             if (environment.getTimeStamp() != STRUCTURE_TIMESTAMP) {
                 STRUCTURE_TIMESTAMP = environment.getTimeStamp();
                 reloadGuestTicketAssignments(false);
             }
         }
         synchronized(UserTicketImpl.class) {
             // Don't use the class lock for the entire method, because otherwise it could
             // deadlock when reloadGuestTicketAssignments is called from another thread (FX-435)
             
             return new UserTicketImpl(si.getApplicationId(), si.isWebDAV(), "GUEST", "GUEST", Account.USER_GUEST,
                     guestContactData, Mandator.MANDATOR_FLEXIVE, true, guestGroups, guestRoles, guestACLAssignments,
                     FxLanguage.DEFAULT, 0, AuthenticationSource.None);
         }
     }
 
     /**
      * (Re)load all assignments for the guest user ticket
      *
      * @param flagDirty flag the UserTicketStores guest ticket as dirty?
      */
     public static synchronized void reloadGuestTicketAssignments(boolean flagDirty) {
         try {
             AccountEngine accountInterface = EJBLookup.getAccountEngine();
             final List<ACLAssignment> assignmentList = accountInterface.loadAccountAssignments(Account.USER_GUEST);
             guestACLAssignments = assignmentList.toArray(new ACLAssignment[assignmentList.size()]);
             final List<Role> roles = accountInterface.getRoles(Account.USER_GUEST, RoleLoadMode.ALL);
             guestRoles = roles.toArray(new Role[roles.size()]);
             final List<Long> groupList = FxSharedUtils.getSelectableObjectIdList(accountInterface.getGroups(Account.USER_GUEST));
             guestGroups = ArrayUtils.toPrimitive(groupList.toArray(new Long[groupList.size()]));
             guestContactData = accountInterface.load(Account.USER_GUEST).getContactData();
             if (flagDirty)
                 UserTicketStore.flagDirtyHavingUserId(Account.USER_GUEST);
         } catch (FxApplicationException e) {
             guestACLAssignments = null;
             LOG.error(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public final boolean isInRole(Role role) {
         return isGlobalSupervisor() || roles != null && ArrayUtils.contains(roles, role);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getApplicationId() {
         return this.applicationId;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isInGroup(long group) {
         return ArrayUtils.contains(groups, group);
     }
 
     /**
      * If true the ticket is dirty and needs to be synced with the database.
      *
      * @return true the ticket is dirty and needs to be synced with the database
      */
     public boolean isDirty() {
         return dirty;
     }
 
     /**
      * Sets the dirty flag.
      *
      * @param value true the ticket is dirty and needs to be synced with the database
      */
     public void setDirty(boolean value) {
         this.dirty = value;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public boolean isInGroups(int groups[]) {
         if (groups == null || groups.length == 0) {
             return true;
         }
         for (int group : groups) {
             if (!ArrayUtils.contains(this.groups, (long) group)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isInAtLeastOneGroup(long[] groups) {
         if (groups == null || groups.length == 0) {
             return false;
         }
         for (long group : groups) {
             if (ArrayUtils.contains(this.groups, group)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns true if the user is assigned to at least one of the given ACLs.
      * <p/>
      * <p/>
      * Returns false if the acls parameter is null or empty
      *
      * @param acls the ACLs to check for
      * @return true if the user is a assigned to at least one of the given ACLs
      */
     public boolean hasAtLeastOneACL(long[] acls) {
         if (assignments == null || assignments.length == 0 || acls == null || acls.length == 0) {
             return false;
         }
         for (ACLAssignment aclData : assignments) {
             for (long acl : acls) {
                 if (aclData.getAclId() == acl) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isGuest() {
         return (userId == Account.USER_GUEST);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getUserName() {
         return userName;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getLoginName() {
         return loginName;
     }
 
     /**
      * {@inheritDoc}
      */
     public long getUserId() {
         if( /*FxContext.get().getRunAsSystem() &&*/ FxContext.get().isExecutingRunOnceScripts()) {
 //            System.out.println("Executing run once scripts -> user id set to " + Account.USER_GLOBAL_SUPERVISOR);
             return Account.USER_GLOBAL_SUPERVISOR;
         } else
             return userId;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPK getContactData() {
         return contactData;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isMultiLogin() {
         return multiLogin;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isGlobalSupervisor() {
         return globalSupervisor;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isMandatorSupervisor() {
         return (mandatorSupervisor || globalSupervisor);
     }
 
     /**
      * {@inheritDoc}
      */
     public long getMandatorId() {
         return this.mandator;
     }
 
     /**
      * {@inheritDoc}
      */
     public long[] getGroups() {
         return ArrayUtils.clone(this.groups);
     }
 
     /**
      * Constructor.
      *
      * @param applicationId the application id this ticket belongs to
      * @param acc           the account
      * @param groups        the groups
      * @param roles         the roles
      * @param aad           the acl assignemnts
      * @param language      the language
      * @param isWebDav      true if this is a webdav ticket
      */
     public UserTicketImpl(String applicationId, boolean isWebDav, Account acc, long[] groups, Role[] roles,
                           ACLAssignment aad[], FxLanguage language) {
         this.userName = acc.getName();
         this.loginName = acc.getLoginName();
         this.userId = acc.getId();
         this.contactData = acc.getContactData();
         this.multiLogin = acc.isAllowMultiLogin();
         this.roles = (Role[]) ArrayUtils.clone(roles);
         this.groups = ArrayUtils.clone(groups);
         this.mandator = acc.getMandatorId();
         this.applicationId = applicationId;
         this.assignments = aad.clone();
         this.language = language;
         this.webDav = isWebDav;
         populateData();
     }
 
     /**
      * Private Constructor.
      *
      * @param applicationId        the application name that the ticket belongs to
      * @param userName             the user name
      * @param loginName            the login name
      * @param userId               the user id
      * @param contactData          contact data pk
      * @param mandatorId           the mandator id that the user belongs to
      * @param multiLogin           true if the account may be logged in more than once at a time
      * @param groups               the groups
      * @param roles                the roles
      * @param assignments          the acl assignemnts
      * @param language             the language
      * @param isWebDav             true if this is a webdav ticket
      * @param failedLoginAttempts  number of failed login attempts
      * @param authenticationSource source of authentication
      */
     private UserTicketImpl(String applicationId, boolean isWebDav, String userName, String loginName, long userId,
                            FxPK contactData, long mandatorId,
                            boolean multiLogin, long[] groups, Role[] roles, ACLAssignment assignments[],
                            FxLanguage language, long failedLoginAttempts, AuthenticationSource authenticationSource) {
         this.applicationId = applicationId;
         this.userName = userName;
         this.loginName = loginName;
         this.userId = userId;
         this.contactData = contactData;
         this.multiLogin = multiLogin;
         this.groups = groups;
         this.roles = roles;
         this.mandator = mandatorId;
         this.assignments = assignments;
         this.language = language;
         this.webDav = isWebDav;
         this.failedLoginAttempts = failedLoginAttempts;
         this.authenticationSource = authenticationSource;
         populateData();
     }
 
     /**
      * Helper function for the constructor.
      */
     private void populateData() {
 
         this.dirty = false;
         this.creationTime = System.currentTimeMillis();
 
         // Check ACL assignments
         if (assignments == null) {
             assignments = new ACLAssignment[0];
         }
 
         // Check groups
         if (this.groups == null || this.groups.length == 0) {
             this.groups = new long[]{UserGroup.GROUP_EVERYONE};
         } else {
             // Group everyone has to be present
             if (!(ArrayUtils.contains(this.groups, UserGroup.GROUP_EVERYONE))) {
                 this.groups = ArrayUtils.add(this.groups, UserGroup.GROUP_EVERYONE);
             }
         }
 
         // Check roles
         if (this.roles == null) {
             this.roles = new Role[0];
         }
 
         // Set mandator/global supervisor flag
         if (this.userId == Account.USER_GLOBAL_SUPERVISOR || isInRole(Role.GlobalSupervisor)) {
             globalSupervisor = true;
         }
         if (isInRole(Role.MandatorSupervisor)) {
             mandatorSupervisor = true;
         }
     }
 
     /**
      * Returns the time that this ticket was created at.
      *
      * @return the time that the ticket was created at
      */
     public long getCreationTime() {
         return this.creationTime;
     }
 
     /**
      * Returns a string representation of the ticket.
      *
      * @return a string representation of the ticket.
      */
     @Override
     public String toString() {
         return this.getClass() + "@[" +
                 "id=" + this.userId +
                 "; contactData=" + this.contactData +
                 "; name:" + this.userName +
                 "; mandator:" + this.mandator +
                 "; language: " + this.getLanguage().getIso2digit() +
                 "; groups:" + StringUtils.join(ArrayUtils.toObject(this.groups), ',') +
                 "; roles:" + StringUtils.join(FxSharedUtils.getSelectableObjectIdList(Arrays.asList((SelectableObjectWithLabel[]) this.roles)), ',') +
                 "; globalSupervisor:" + this.globalSupervisor +
                 "; multiLogin:" + this.multiLogin +
                 "]";
 
     }
 
     /**
      * {@inheritDoc}
      */
     public UserTicketImpl cloneAsGlobalSupervisor() {
        final UserTicketImpl clone = new UserTicketImpl(this.applicationId, this.webDav, this.userName, this.loginName,
                1, this.contactData, this.mandator, this.multiLogin, this.groups.clone(), this.roles.clone(),
                ACLAssignment.clone(this.assignments), this.language, this.failedLoginAttempts, this.authenticationSource);
         clone.globalSupervisor = true;
         return clone;
     }
 
     /**
      * {@inheritDoc}
      */
     public UserTicketImpl copy() {
         return new UserTicketImpl(this.applicationId, this.webDav, this.userName, this.loginName,
                 this.userId, this.contactData, this.mandator, this.multiLogin, this.groups.clone(), this.roles.clone(),
                 ACLAssignment.clone(this.assignments), this.language, this.failedLoginAttempts, this.authenticationSource);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public ACLAssignment[] getACLAssignments() {
         return this.assignments.clone();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isAssignedToACL(long aclId) {
         for (ACLAssignment item : this.assignments) {
             if (item.getAclId() == aclId && !item.isOwnerGroupAssignment()) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean mayReadACL(long aclId, long ownerId) {
         if (this.isGlobalSupervisor()) return true;
         for (ACLAssignment item : this.assignments) {
             if (item.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (item.getMayRead() && item.getAclId() == aclId) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean mayEditACL(long aclId, long ownerId) {
         if (this.isGlobalSupervisor()) return true;
         for (ACLAssignment item : this.assignments) {
             if (item.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (item.getMayEdit() && item.getAclId() == aclId) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean mayExportACL(long aclId, long ownerId) {
         if (this.isGlobalSupervisor()) return true;
         for (ACLAssignment item : this.assignments) {
             if (item.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (item.getMayExport() && item.getAclId() == aclId) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean mayRelateACL(long aclId, long ownerId) {
         if (this.isGlobalSupervisor()) return true;
         for (ACLAssignment item : this.assignments) {
             if (item.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (item.getMayRelate() && item.getAclId() == aclId) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean mayCreateACL(long aclId, long ownerId) {
         if (this.isGlobalSupervisor()) return true;
         for (ACLAssignment item : this.assignments) {
             if (item.isOwnerGroupAssignment()) continue; //group owner may never create!
             if (item.getMayCreate() && item.getAclId() == aclId) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean mayDeleteACL(long aclId, long ownerId) {
         if (this.isGlobalSupervisor()) return true;
         for (ACLAssignment item : this.assignments) {
             if (item.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (item.getMayDelete() && item.getAclId() == aclId) return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public ACLAssignment[] getACLAssignments(ACLCategory category, long ownerId, ACLPermission... perms) {
         Boolean mayCreate = null;
         Boolean mayRead = null;
         Boolean mayEdit = null;
         Boolean mayDelete = null;
         Boolean mayRelate = null;
         Boolean mayExport = null;
         for (ACLPermission perm : perms) {
             switch (perm) {
                 case CREATE:
                     mayCreate = true;
                     break;
                 case NOT_CREATE:
                     mayCreate = false;
                     break;
                 case READ:
                     mayRead = true;
                     break;
                 case NOT_READ:
                     mayRead = false;
                     break;
                 case EDIT:
                     mayEdit = true;
                     break;
                 case NOT_EDIT:
                     mayEdit = false;
                     break;
                 case DELETE:
                     mayDelete = true;
                     break;
                 case NOT_DELETE:
                     mayDelete = false;
                     break;
                 case RELATE:
                     mayRelate = true;
                     break;
                 case NOT_RELATE:
                     mayRelate = false;
                     break;
                 case EXPORT:
                     mayExport = true;
                     break;
                 case NOT_EXPORT:
                     mayExport = false;
                     break;
             }
         }
         List<ACLAssignment> result = new ArrayList<ACLAssignment>(this.assignments.length);
         for (ACLAssignment acl : this.assignments) {
             if (acl.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (mayRead != null && mayRead != acl.getMayRead()) continue;
             if (mayEdit != null && mayEdit != acl.getMayEdit()) continue;
             if (mayDelete != null && mayDelete != acl.getMayDelete()) continue;
             if (mayRelate != null && mayRelate != acl.getMayRelate()) continue;
             if (mayExport != null && mayExport != acl.getMayExport()) continue;
             if (mayCreate != null && mayCreate != acl.getMayCreate()) continue;
             if (category != null && category != acl.getACLCategory()) continue;
             result.add(acl);
         }
         return result.toArray(new ACLAssignment[result.size()]);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getACLsCSV(long ownerId, ACLCategory category, ACLPermission... perms) {
         final StringBuilder result = new StringBuilder();
         Long ACLs[] = getACLsId(ownerId, category, perms);
         for (long acl : ACLs) {
             result.append((result.length() > 0) ? "," : "").append(acl);
         }
         return result.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     public Long[] getACLsId(long ownerId, ACLCategory category, ACLPermission... perms) {
         Boolean mayCreate = null;
         Boolean mayRead = null;
         Boolean mayEdit = null;
         Boolean mayDelete = null;
         Boolean mayRelate = null;
         Boolean mayExport = null;
         for (ACLPermission perm : perms) {
             switch (perm) {
                 case CREATE:
                     mayCreate = true;
                     break;
                 case NOT_CREATE:
                     mayCreate = false;
                     break;
                 case READ:
                     mayRead = true;
                     break;
                 case NOT_READ:
                     mayRead = false;
                     break;
                 case EDIT:
                     mayEdit = true;
                     break;
                 case NOT_EDIT:
                     mayEdit = false;
                     break;
                 case DELETE:
                     mayDelete = true;
                     break;
                 case NOT_DELETE:
                     mayDelete = false;
                     break;
                 case RELATE:
                     mayRelate = true;
                     break;
                 case NOT_RELATE:
                     mayRelate = false;
                     break;
                 case EXPORT:
                     mayExport = true;
                     break;
                 case NOT_EXPORT:
                     mayExport = false;
                     break;
             }
         }
         Hashtable<Long, boolean[]> hlp = new Hashtable<Long, boolean[]>(this.assignments.length);
 
         // Condense the ACL right informations
         // If a ACL is assigned via groupX and groupY the rights are taken from both assignments.
         for (ACLAssignment acl : this.assignments) {
             if (acl.isOwnerGroupAssignment() && ownerId != userId) continue;
             if (category != null && acl.getACLCategory() != category) continue;
             Long key = acl.getAclId();
             boolean[] rights = hlp.get(key);
             if (rights == null) {
                 rights = new boolean[]{false, false, false, false, false, false, false};
             }
             if (acl.getMayRead()) rights[ACLPermission.READ.ordinal()] = true;
             if (acl.getMayEdit()) rights[ACLPermission.EDIT.ordinal()] = true;
             if (acl.getMayDelete()) rights[ACLPermission.DELETE.ordinal()] = true;
             if (acl.getMayRelate()) rights[ACLPermission.RELATE.ordinal()] = true;
             if (acl.getMayExport()) rights[ACLPermission.EXPORT.ordinal()] = true;
             if (acl.getMayCreate() && !acl.isOwnerGroupAssignment()) rights[ACLPermission.CREATE.ordinal()] = true;
             hlp.put(key, rights);
         }
 
         // Return matching ACLs
         Enumeration keys = hlp.keys();
         List<Long> result = new ArrayList<Long>(hlp.size());
         while (keys.hasMoreElements()) {
             Long aclId = (Long) keys.nextElement();
             boolean[] rights = hlp.get(aclId);
             if (mayRead != null && mayRead != rights[ACLPermission.READ.ordinal()]) continue;
             if (mayEdit != null && mayEdit != rights[ACLPermission.EDIT.ordinal()]) continue;
             if (mayDelete != null && mayDelete != rights[ACLPermission.DELETE.ordinal()]) continue;
             if (mayRelate != null && mayRelate != rights[ACLPermission.RELATE.ordinal()]) continue;
             if (mayExport != null && mayExport != rights[ACLPermission.EXPORT.ordinal()]) continue;
             if (mayCreate != null && mayCreate != rights[ACLPermission.CREATE.ordinal()]) continue;
             result.add(aclId);
         }
         return result.toArray(new Long[result.size()]);
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL[] getACLs(long owner, ACLCategory category, ACLPermission... perms) {
         Long[] acls = getACLsId(owner, category, perms);
         List<ACL> res = new ArrayList<ACL>(acls.length);
         FxEnvironment struct;
         struct = CacheAdmin.getEnvironment();
         for (Long acl : acls) {
             res.add(struct.getACL(acl));
         }
         return res.toArray(new ACL[res.size()]);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLanguage getLanguage() {
         return language;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isWebDav() {
         return this.webDav;
     }
 
     public void setFailedLoginAttempts(long failedLoginAttempts) {
         this.failedLoginAttempts = failedLoginAttempts;
     }
 
     public void setAuthenticationSource(AuthenticationSource authenticationSource) {
         this.authenticationSource = authenticationSource;
     }
 
     /**
      * {@inheritDoc}
      */
     public long getFailedLoginAttempts() {
         return failedLoginAttempts;
     }
 
     /**
      * {@inheritDoc}
      */
     public AuthenticationSource getAuthenticationSource() {
         return authenticationSource;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setLanguage(FxLanguage language) {
         if (language != null)
             this.language = language;
     }
 }
