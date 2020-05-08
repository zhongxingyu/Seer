 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.appcommon.client;
 
 import org.bedework.appcommon.AdminConfig;
 import org.bedework.calfacade.BwAuthUser;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwPreferences;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwResource;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.base.UpdateFromTimeZonesInfo;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.svc.BwAdminGroup;
 import org.bedework.calfacade.svc.BwCalSuite;
 import org.bedework.calfacade.svc.UserAuth;
 import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
 import org.bedework.calsvci.CalSvcFactoryDefault;
 import org.bedework.calsvci.CalSvcIPars;
 import org.bedework.util.misc.Util;
 
 import org.bedework.access.Access;
 import org.bedework.access.AccessException;
 import org.bedework.access.Ace;
 import org.bedework.access.AceWho;
 import org.bedework.access.Privilege;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * User: douglm Date: 7/3/13 Time: 10:37 AM
  */
 public class AdminClientImpl extends ClientImpl {
   /** Auth users for list or mod
    */
   private Collection<BwAuthUser> authUsers;
 
   /** True if we have set the user's group.
    */
   private boolean groupSet;
 
   /** True if user is in only one group
    */
   private boolean oneGroup;
 
   /** True if we are choosing the user's group.
    */
   private boolean choosingGroup;
 
   /** User's current group or null. */
   private String adminGroupName;
 
   public AdminClientImpl(final String id,
                          final String authUser,
                          final String runAsUser,
                          final String calSuiteName,
                          final AdminConfig conf)
           throws CalFacadeException {
     super(id);
 
     reinit(authUser, runAsUser, calSuiteName, conf);
   }
 
   protected AdminClientImpl(final String id) {
     super(id);
   }
 
   public void reinit(final String authUser,
                      final String runAsUser,
                      final String calSuiteName,
                      final AdminConfig conf)
           throws CalFacadeException {
     pars = new CalSvcIPars(authUser,
                            runAsUser,
                            calSuiteName,
                            true,
                            false, // Allow non-admin super user
                            false, // service
                            conf.getAllowEditAllCategories(),
                            conf.getAllowEditAllLocations(),
                            conf.getAllowEditAllContacts(),
                            false);    // sessionless
     pars.setLogId(id);
 
     svci = new CalSvcFactoryDefault().getSvc(pars);
 
     superUser = svci.getSuperUser();
     publicAdmin = true;
     publicView = false;
   }
 
   @Override
   public Client copy(final String id) throws CalFacadeException {
     AdminClientImpl cl = new AdminClientImpl(id);
 
     cl.pars = (CalSvcIPars)pars.clone();
     cl.pars.setLogId(id);
 
     cl.svci = new CalSvcFactoryDefault().getSvc(cl.pars);
 
     cl.superUser = svci.getSuperUser();
     cl.publicAdmin = true;
 
     cl.setGroupSet(getGroupSet());
     cl.setChoosingGroup(getChoosingGroup());
     cl.setOneGroup(getOneGroup());
     cl.setAdminGroupName(getAdminGroupName());
 
     return cl;
   }
 
   /* ------------------------------------------------------------
    *                     Principals
    * ------------------------------------------------------------ */
 
   @Override
   public boolean isGuest() {
     return false;
   }
 
   @Override
   public void updateSyspars(final BwSystem val)
           throws CalFacadeException {
     svci.getSysparsHandler().update(val);
   }
 
   /* ------------------------------------------------------------
    *                     Admin users
    * ------------------------------------------------------------ */
 
   @Override
   public void addUser(final String account)
           throws CalFacadeException {
     svci.getUsersHandler().add(account);
   }
 
   @Override
   public BwAuthUser getAuthUser(final String userid)
           throws CalFacadeException {
     final UserAuth ua = svci.getUserAuth();
     BwAuthUser au = ua.getUser(userid);
 
     if (au == null) {
       // No authuser entry for this user.
       if (!superUser) {
         return null;
       }
       au = BwAuthUser.makeAuthUser(getCurrentPrincipalHref(),
                                    UserAuth.publicEventUser);
       ua.updateUser(au);
     }
 
     return au;
   }
 
   @Override
   public void updateAuthUser(final BwAuthUser val)
           throws CalFacadeException {
     svci.getUserAuth().updateUser(val);
     authUsers = null; // force refresh
   }
 
   @Override
   public Collection<BwAuthUser> getAllAuthUsers()
           throws CalFacadeException {
     if (authUsers == null) {
       authUsers = svci.getUserAuth().getAll();
     }
 
     return authUsers;
   }
 
   /* ------------------------------------------------------------
    *                     Admin Groups
    * ------------------------------------------------------------ */
 
   @Override
   public void addAdminGroup(final BwAdminGroup group)
           throws CalFacadeException {
     svci.getAdminDirectories().addGroup(group);
   }
 
   @Override
   public void removeAdminGroup(final BwAdminGroup group)
           throws CalFacadeException {
     svci.getAdminDirectories().removeGroup(group);
   }
 
   @Override
   public void updateAdminGroup(final BwAdminGroup group)
           throws CalFacadeException {
     svci.getAdminDirectories().updateGroup(group);
   }
 
   @Override
   public void addAdminGroupMember(final BwAdminGroup group,
                                   final BwPrincipal val)
           throws CalFacadeException {
     svci.getAdminDirectories().addMember(group, val);
   }
 
   @Override
   public void removeAdminGroupMember(final BwAdminGroup group,
                                      final BwPrincipal val)
           throws CalFacadeException {
     svci.getAdminDirectories().removeMember(group, val);
   }
 
   /* ------------------------------------------------------------
    *                     Preferences
    * ------------------------------------------------------------ */
 
   @Override
   public BwPreferences getPreferences(final String user)
           throws CalFacadeException {
     if (!superUser) {
       return null;
     }
 
     final BwPrincipal p = getUser(user);
     if (p == null) {
       return null;
     }
 
     return svci.getPrefsHandler().get(p);
   }
 
   /* ------------------------------------------------------------
    *                   Calendar Suites
    * ------------------------------------------------------------ */
 
   @Override
   public BwCalSuiteWrapper addCalSuite(final String name,
                                        final String adminGroupName,
                                        final String rootCollectionPath,
                                        final String submissionsPath)
           throws CalFacadeException {
     return svci.getCalSuitesHandler().add(name,
                                           adminGroupName,
                                           rootCollectionPath,
                                           submissionsPath);
   }
 
   @Override
   public void updateCalSuite(final BwCalSuiteWrapper cs,
                              final String adminGroupName,
                              final String rootCollectionPath,
                              final String submissionsPath)
           throws CalFacadeException {
     svci.getCalSuitesHandler().update(cs,
                                       adminGroupName,
                                       rootCollectionPath,
                                       submissionsPath);
   }
 
   @Override
   public void deleteCalSuite(final BwCalSuiteWrapper val)
           throws CalFacadeException {
     svci.getCalSuitesHandler().delete(val);
   }
 
   @Override
   public UpdateFromTimeZonesInfo updateFromTimeZones(final int limit,
                                                      final boolean checkOnly,
                                                      final UpdateFromTimeZonesInfo info)
           throws CalFacadeException {
     return svci.updateFromTimeZones(limit, checkOnly, info);
   }
 
   /* ------------------------------------------------------------
    *                   Calendar Suite Resources
    * ------------------------------------------------------------ */
 
   @Override
   public void addCSResource(final BwCalSuite suite,
                             final BwResource res,
                             final String rc)
           throws CalFacadeException {
     svci.getResourcesHandler().save(getCSResourcesDir(suite, rc), res);
   }
 
   @Override
   public void deleteCSResource(final BwCalSuite suite,
                                final String name,
                                final String rc)
           throws CalFacadeException {
     svci.getResourcesHandler().delete(Util.buildPath(false,
                                                      getCSResourcesPath(
                                                              suite,
                                                              rc), "/",
                                                      name));
   }
 
   private String getCSResourcesDir(final BwCalSuite suite,
                                   final String rc) throws CalFacadeException {
     String path = getCSResourcesPath(suite, rc);
 
     if (path == null) {
       throw new CalFacadeException(CalFacadeException.noCalsuiteResCol);
     }
 
     if (collectionExists(path)) {
       return path;
     }
 
     /* Create the collection. All are world readable. The calsuite class
      * collection is writable to the calsuite owner.
      */
 
     BwCalendar resCol = new BwCalendar();
 
     resCol.setName(Util.buildPath(false,
                                   path.substring(path.lastIndexOf("/") + 1)));
     resCol.setSummary(resCol.getName());
     resCol.setCreatorHref(suite.getOwnerHref());
 
     if (rc == "calsuite") {
       // Owned by the suite
       resCol.setOwnerHref(suite.getOwnerHref());
     } else {
       resCol.setOwnerHref(svci.getUsersHandler().getPublicUser().getPrincipalRef());
     }
 
     String parentPath = path.substring(0, path.lastIndexOf("/"));
 
     resCol = addCollection(resCol, parentPath);
 
     /* Ownership is enough to control writability. We have to make it
      * world-readable
      */
 
     try {
       Collection<Privilege> readPrivs = new ArrayList<Privilege>();
       readPrivs.add(Access.read);
 
       Collection<Ace> aces = new ArrayList<Ace>();
       aces.add(Ace.makeAce(AceWho.all, readPrivs, null));
 
       changeAccess(resCol, aces, true);
     } catch (AccessException ae) {
       throw new CalFacadeException(ae);
     }
 
     return resCol.getPath();
   }
 
   /* ------------------------------------------------------------
    *                     State of current admin group
    * ------------------------------------------------------------ */
 
   @Override
   public void setGroupSet(final boolean val) throws CalFacadeException {
     groupSet = val;
   }
 
   @Override
   public boolean getGroupSet() {
     return groupSet;
   }
 
   @Override
   public void setChoosingGroup(final boolean val) throws CalFacadeException {
     choosingGroup = val;
   }
 
   @Override
   public boolean getChoosingGroup() {
     return choosingGroup;
   }
 
   @Override
   public void setOneGroup(final boolean val) throws CalFacadeException {
     oneGroup = val;
   }
 
   @Override
   public boolean getOneGroup() {
     return oneGroup;
   }
 
   @Override
   public void setAdminGroupName(final String val) throws CalFacadeException {
     adminGroupName = val;
   }
 
   @Override
   public String getAdminGroupName() {
     return adminGroupName;
   }
 }
