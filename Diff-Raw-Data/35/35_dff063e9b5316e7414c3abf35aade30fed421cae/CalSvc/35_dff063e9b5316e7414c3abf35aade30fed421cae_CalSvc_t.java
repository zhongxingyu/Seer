 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package org.bedework.calsvc;
 
 import org.bedework.calenv.CalEnv;
 import org.bedework.calfacade.base.BwOwnedDbentity;
 import org.bedework.calfacade.base.BwShareableDbentity;
 import org.bedework.calfacade.BwAlarm;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwEventAlarm;
 import org.bedework.calfacade.BwEventAnnotation;
 import org.bedework.calfacade.BwEventProxy;
 import org.bedework.calfacade.BwFreeBusy;
 import org.bedework.calfacade.BwFreeBusyComponent;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwSponsor;
 import org.bedework.calfacade.BwStats;
 import org.bedework.calfacade.BwSynchInfo;
 import org.bedework.calfacade.BwSynchState;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.CalFacadeAccessException;
 import org.bedework.calfacade.CalFacadeDefs;
 import org.bedework.calfacade.CalFacadeException;
 import org.bedework.calfacade.CalFacadeUtil;
 import org.bedework.calfacade.filter.BwFilter;
 import org.bedework.calfacade.ifs.CalTimezones;
 import org.bedework.calfacade.ifs.Calintf;
 import org.bedework.calfacade.ifs.Groups;
 import org.bedework.calfacade.svc.BwAuthUser;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.svc.UserAuth;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.calsvci.CalSvcIPars;
 import org.bedework.icalendar.IcalCallback;
 import org.bedework.icalendar.TimeZoneRegistryImpl;
 //import org.bedework.mail.MailerIntf;
 
 import edu.rpi.cct.uwcal.common.URIgen;
 import edu.rpi.cct.uwcal.resources.Resources;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Period;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.property.Created;
 import net.fortuna.ical4j.model.property.DtStamp;
 import net.fortuna.ical4j.model.property.LastModified;
 
 import org.apache.log4j.Logger;
 
 /** This is an implementation of the service level interface to the calendar
  * suite.
  *
  * @author Mike Douglass       douglm@rpi.edu
  */
 public class CalSvc extends CalSvcI {
   private CalSvcIPars pars;
 
   private boolean debug;
 
   private boolean open;
 
   //private BwFilter currentFilter;
 
   /* The account that owns public entities
    */
   private BwUser publicUser;
 
   // Set up by call to getCal()
   private String publicUserAccount;
 
   private BwView currentView;
 
   /** Used to see if applications need to force a refresh
    */
   private long publicLastmod;
 
   /** Core calendar interface
    */
   private transient Calintf cali;
 
   /** Db interface for our own data structures.
    */
   private CalSvcDb dbi;
 
   /** handles timezone info.
    */
   private CalTimezones timezones;
 
   /** The user authorisation object
    */
   private UserAuth userAuth;
 
   private transient UserAuth.CallBack uacb;
 
   private transient Groups.CallBack gcb;
 
   /* If we're doing admin this is the authorised user entry
    */
   BwAuthUser adminUser;
 
   /** Class used by UseAuth to do calls into CalSvci
    *
    */
   public static class UserAuthCallBack extends UserAuth.CallBack {
     CalSvc svci;
 
     UserAuthCallBack(CalSvc svci) {
       this.svci = svci;
     }
 
     public BwUser getUser(String account) throws CalFacadeException {
       return svci.getCal().getUser(account);
     }
 
     public BwUser getUser(int id) throws CalFacadeException {
       return svci.getCal().getUser(id);
     }
 
     public void addUser(BwUser user) throws CalFacadeException {
       svci.addUser(user);
     }
 
     public UserAuth getUserAuth() throws CalFacadeException {
       return svci.getUserAuth();
     }
 
     /* (non-Javadoc)
      * @see org.bedework.calfacade.svc.UserAuth.CallBack#getDbSession()
      */
     public Object getDbSession() throws CalFacadeException {
       return svci.getCal().getDbSession();
     }
   }
 
   /** Class used by groups implementations for calls into CalSvci
    *
    */
   public static class GroupsCallBack extends Groups.CallBack {
     CalSvc svci;
 
     GroupsCallBack(CalSvc svci) {
       this.svci = svci;
     }
 
     public BwUser getUser(String account) throws CalFacadeException {
       return svci.getCal().getUser(account);
     }
 
     public BwUser getUser(int id) throws CalFacadeException {
       return svci.getCal().getUser(id);
     }
 
     /* (non-Javadoc)
      * @see org.bedework.calfacade.svc.UserAuth.CallBack#getDbSession()
      */
     public Object getDbSession() throws CalFacadeException {
       return svci.getCal().getDbSession();
     }
   }
 
   /** The user groups object.
    */
   private Groups userGroups;
 
   /** The admin groups object.
    */
   private Groups adminGroups;
 
   /* The mailer object.
    */
   //private MailerIntf mailer;
 
   private IcalCallback icalcb;
 
   /* These are only relevant for the public admin client.
    */
   //private boolean adminAutoDeleteSponsors;
   //private boolean adminAutoDeleteLocations;
 
   private boolean adminCanEditAllPublicCategories;
   private boolean adminCanEditAllPublicLocations;
   private boolean adminCanEditAllPublicSponsors;
 
   private transient Logger log;
 
   private CalEnv env;
 
   private Resources res = new Resources();
 
   /* (non-Javadoc)
    * @see org.bedework.calsvci.CalSvcI#init(org.bedework.calsvci.CalSvcIPars)
    */
   public void init(CalSvcIPars parsParam) throws CalFacadeException {
     pars = (CalSvcIPars)parsParam.clone();
     debug = pars.getDebug();
 
     //if (userAuth != null) {
     //  userAuth.reinitialise(getUserAuthCallBack());
     //}
 
     if (userGroups != null) {
       userGroups.init(getGroupsCallBack());
     }
 
     if (adminGroups != null) {
       adminGroups.init(getGroupsCallBack());
     }
 
     try {
       String appPrefix = null;
 
       if (pars.getPublicAdmin()) {
         appPrefix = CalEnv.webAdminAppPrefix;
       } else if (pars.isGuest()) {
         if (pars.getCaldav()) {
           appPrefix = CalEnv.caldavPublicAppPrefix;
         } else {
           appPrefix = CalEnv.webPublicAppPrefix;
         }
       } else {
         if (pars.getCaldav()) {
           appPrefix = CalEnv.caldavPersonalAppPrefix;
         } else {
           appPrefix = CalEnv.webPersonalAppPrefix;
         }
       }
 
       env = new CalEnv(appPrefix, debug);
 
       //publicUserAccount = CalEnv.getGlobalProperty("public.user");
 
       if (pars.isGuest() && (pars.getUser() == null)) {
         pars.setUser(env.getAppProperty("run.as.user"));
       }
 
       if (pars.getPublicAdmin()) {
         //adminAutoDeleteSponsors = env.getAppBoolProperty("app.autodeletesponsors");
         //adminAutoDeleteLocations = env.getAppBoolProperty("app.autodeletelocations");
 
         adminCanEditAllPublicCategories = env.getAppBoolProperty("app.allowEditAllCategories");
         adminCanEditAllPublicLocations = env.getAppBoolProperty("app.allowEditAllLocations");
         adminCanEditAllPublicSponsors = env.getAppBoolProperty("app.allowEditAllSponsors");
       }
 
       timezones = getCal().getTimezones();
 
       /* Nominate our timezone registry */
       System.setProperty("net.fortuna.ical4j.timezone.registry",
                     "org.bedework.icalendar.TimeZoneRegistryFactoryImpl");
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   public BwStats getStats() throws CalFacadeException {
     if (!pars.getPublicAdmin()) {
       throw new CalFacadeAccessException();
     }
 
     return getCal().getStats();
   }
 
   /** Get the system pars
    *
    * @return BwSystem object
    * @throws CalFacadeException if not admin
    */
   public BwSystem getSyspars() throws CalFacadeException {
     return getCal().getSyspars();
   }
 
   public CalTimezones getTimezones() throws CalFacadeException {
     return getCal().getTimezones();
   }
 
   public void logStats() throws CalFacadeException {
     logIt(getStats().toString());
   }
 
   public void setUser(String val) throws CalFacadeException {
     getCal().setUser(val);
   }
 
   public BwUser findUser(String val) throws CalFacadeException {
     return getCal().getUser(val);
   }
 
   public void addUser(BwUser user) throws CalFacadeException {
     getCal().addUser(user);
   }
 
   public void flushAll() throws CalFacadeException {
     getCal().flushAll();
   }
 
   public void open() throws CalFacadeException {
     open = true;
     TimeZoneRegistryImpl.setThreadCb(getIcalCallback());
     getCal().open();
     dbi.open();
   }
 
   public boolean isOpen() {
     return open;
   }
 
   public void close() throws CalFacadeException {
     open = false;
     getCal().close();
     dbi.close();
   }
 
   public void beginTransaction() throws CalFacadeException {
     getCal().beginTransaction();
   }
 
   public void endTransaction() throws CalFacadeException {
     getCal().endTransaction();
   }
 
   public void rollbackTransaction() throws CalFacadeException {
     getCal().rollbackTransaction();
   }
 
   public IcalCallback getIcalCallback() {
     if (icalcb == null) {
       icalcb = new IcalCallbackcb();
     }
 
     return icalcb;
   }
 
   public String getEnvProperty(String name) throws CalFacadeException {
     try {
       return CalEnv.getProperty(name);
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
    public Resources getResources() {
      return res;
    }
 
   public String getSysid() throws CalFacadeException {
     return getCal().getSysid();
   }
 
   public BwUser getUser() throws CalFacadeException {
     if (pars.isGuest()) {
       return getPublicUser();
     }
 
     return getCal().getUser();
   }
 
   public Collection getInstanceOwners() throws CalFacadeException {
     return getCal().getInstanceOwners();
   }
 
   private BwUser getPublicUser() throws CalFacadeException {
     if (publicUser == null) {
       publicUser = getCal().getUser(publicUserAccount);
     }
 
     if (publicUser == null) {
       throw new CalFacadeException("No guest user proxy account - expected " + publicUserAccount);
     }
 
     return publicUser;
   }
 
   public BwPreferences getUserPrefs() throws CalFacadeException {
     return dbi.getPreferences();
   }
 
   public BwPreferences getUserPrefs(BwUser user) throws CalFacadeException {
     return dbi.fetchPreferences(user);
   }
 
   public void updateUserPrefs(BwPreferences  val) throws CalFacadeException {
     dbi.updatePreferences(val);
   }
 
   public UserAuth getUserAuth(String user, Object par) throws CalFacadeException {
     if (userAuth != null) {
       //userAuth.reinitialise(getUserAuthCallBack());
       return userAuth;
     }
 
     try {
       userAuth = (UserAuth)CalFacadeUtil.getObject(getSyspars().getUserauthClass(),
                                                   UserAuth.class);
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     userAuth.initialise(user, getUserAuthCallBack(), par);
 
     return userAuth;
   }
 
   public UserAuth getUserAuth() throws CalFacadeException {
     if (userAuth != null) {
       //userAuth.reinitialise(getUserAuthCallBack());
       return userAuth;
     }
 
     return getUserAuth(pars.getAuthUser(), null);
   }
 
   public Groups getGroups() throws CalFacadeException {
     if (isPublicAdmin()) {
       return getAdminGroups();
     }
 
     return getUserGroups();
   }
 
   public Groups getUserGroups() throws CalFacadeException {
     if (userGroups != null) {
       return userGroups;
     }
 
     try {
       userGroups = (Groups)CalFacadeUtil.getObject(getSyspars().getUsergroupsClass(), Groups.class);
       userGroups.init(getGroupsCallBack());
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     return userGroups;
   }
 
   public Groups getAdminGroups() throws CalFacadeException {
     if (adminGroups != null) {
       return adminGroups;
     }
 
     try {
       adminGroups = (Groups)CalFacadeUtil.getObject(getSyspars().getAdmingroupsClass(), Groups.class);
       adminGroups.init(getGroupsCallBack());
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     return adminGroups;
   }
 
   public void refreshEvents() throws CalFacadeException {
     getCal().refreshEvents();
   }
 
   public boolean refreshNeeded() throws CalFacadeException {
     /* See if the events were updated */
     long lastmod;
 
     lastmod = getCal().getPublicLastmod();
 
     if (lastmod == publicLastmod) {
       return false;
     }
 
     publicLastmod = lastmod;
     return true;
   }
 
   /* ====================================================================
    *                   Access
    * ==================================================================== */
 
   public void changeAccess(Object o, Collection aces) throws CalFacadeException {
     getCal().changeAccess(o, aces);
   }
 
   public Collection getAces(Object o) throws CalFacadeException {
     return getCal().getAces(o);
   }
 
   /* ====================================================================
    *                   Timezones
    * ==================================================================== */
 
   public void saveTimeZone(String tzid, VTimeZone vtz)
           throws CalFacadeException {
     timezones.saveTimeZone(tzid, vtz);
   }
 
   public void registerTimeZone(String id, TimeZone timezone)
       throws CalFacadeException {
     timezones.registerTimeZone(id, timezone);
   }
 
   public TimeZone getTimeZone(final String id) throws CalFacadeException {
     return timezones.getTimeZone(id);
   }
 
   public VTimeZone findTimeZone(final String id, BwUser owner) throws CalFacadeException {
     return timezones.findTimeZone(id, owner);
   }
 
   public void clearPublicTimezones() throws CalFacadeException {
     timezones.clearPublicTimezones();
   }
 
   public void refreshTimezones() throws CalFacadeException {
     timezones.refreshTimezones();
   }
 
   /* ====================================================================
    *                   Calendars
    * ==================================================================== */
 
   public BwCalendar getPublicCalendars() throws CalFacadeException {
     return getCal().getPublicCalendars();
   }
 
   public Collection getPublicCalendarCollections() throws CalFacadeException {
     return getCal().getPublicCalendarCollections();
   }
 
   public boolean getCalendarInuse(BwCalendar val) throws CalFacadeException {
     return getCal().checkCalendarRefs(val);
   }
 
   public BwCalendar getCalendars() throws CalFacadeException {
     if (pars.isGuest() || isPublicAdmin()) {
       return getCal().getPublicCalendars();
     }
 
     return getCal().getCalendars();
   }
 
   public Collection getCalendarCollections() throws CalFacadeException {
     return getCal().getCalendarCollections();
   }
 
   public Collection getAddContentCalendarCollections()
           throws CalFacadeException {
     if (isPublicAdmin()) {
       return getCal().getAddContentPublicCalendarCollections();
     }
     return getCal().getAddContentCalendarCollections();
   }
 
   public BwCalendar getCalendar(int val) throws CalFacadeException {
     return getCal().getCalendar(val);
   }
 
   public BwCalendar getCalendar(String path) throws CalFacadeException{
     if (path == null) {
       return null;
     }
 
     if ((path.length() > 1) && path.endsWith("/")) {
       return getCal().getCalendar(path.substring(0, path.length() - 1));
     }
 
     return getCal().getCalendar(path);
   }
 
   public BwCalendar getDefaultCalendar() throws CalFacadeException {
     return getCal().getDefaultCalendar();
   }
 
   public void addCalendar(BwCalendar val, BwCalendar parent) throws CalFacadeException {
     updateOK(val);
 
     setupSharableEntity(val);
 
     getCal().addCalendar(val, parent);
   }
 
   public void updateCalendar(BwCalendar val) throws CalFacadeException {
     getCal().updateCalendar(val);
   }
 
   public int deleteCalendar(BwCalendar val) throws CalFacadeException {
     /** Only allow delete if not in use
      */
     if (getCal().checkCalendarRefs(val)) {
       return 2;
     }
 
     /* Remove from preferences */
     getUserAuth().removeCalendar(null, val);
 
     /* Attempt to delete
      */
     if (getCal().deleteCalendar(val)) {
       return 0;
     }
 
     return 1; //doesn't exist
   }
 
   /* ====================================================================
    *                   Views
    * ==================================================================== */
 
   public boolean addView(BwView val,
                          boolean makeDefault) throws CalFacadeException {
     if (val == null) {
       return false;
     }
 
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     setupOwnedEntity(val);
 
     if (prefs.getViews().contains(val)) {
       return false;
     }
 
     prefs.addView(val);
 
     if (makeDefault) {
       prefs.setPreferredView(val.getName());
     }
 
     dbi.updatePreferences(prefs);
 
     return true;
   }
 
   public boolean removeView(BwView val) throws CalFacadeException{
     if (val == null) {
       return false;
     }
 
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     Collection views = prefs.getViews();
     setupOwnedEntity(val);
 
     if (!views.contains(val)) {
       return false;
     }
 
     String name = val.getName();
 
     views.remove(val);
 
     if (name.equals(prefs.getPreferredView())) {
       prefs.setPreferredView(null);
     }
 
     dbi.updatePreferences(prefs);
 
     return true;
   }
 
   public void setCurrentView(BwView val) throws CalFacadeException {
     currentView = val;
   }
 
   public boolean setCurrentView(String val) throws CalFacadeException {
     if (val == null) {
       currentView = null;
       return true;
     }
 
     Iterator it = getPreferences().iterateViews();
 
     while (it.hasNext()) {
       BwView view = (BwView)it.next();
       if (val.equals(view.getName())) {
         currentView = view;
 
         if (debug) {
           trace("view has " + view.getSubscriptions().size() + " subscriptions");
 
           trace("set view to " + view);
         }
         return true;
       }
     }
 
     return false;
   }
 
   public BwView findView(String val) throws CalFacadeException {
     if (val == null) {
       BwPreferences prefs = getPreferences();
 
       val = prefs.getPreferredView();
       if (val == null) {
         return null;
       }
     }
 
     Collection views = getViews();
     Iterator it = views.iterator();
     while (it.hasNext()) {
       BwView view = (BwView)it.next();
 
       if (view.getName().equals(val)) {
         return view;
       }
     }
 
     return null;
   }
 
   public boolean addViewSubscription(String name,
                                      BwSubscription sub) throws CalFacadeException {
 
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     BwView view = findView(name);
 
     if (view == null) {
       return false;
     }
 
     view.addSubscription(sub);
 
     dbi.updatePreferences(prefs);
 
     return true;
   }
 
   public boolean removeViewSubscription(String name,
                                         BwSubscription sub) throws CalFacadeException {
 
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     BwView view = findView(name);
 
     if (view == null) {
       return false;
     }
 
     view.removeSubscription(sub);
 
     dbi.updatePreferences(prefs);
 
     return true;
   }
 
   public BwView getCurrentView() throws CalFacadeException {
     return currentView;
   }
 
   public Collection getViews() throws CalFacadeException {
     return getPreferences().getViews();
   }
 
   /* ====================================================================
    *                   Search and filters
    * ==================================================================== */
 
   public void setSearch(String val) throws CalFacadeException {
     getCal().setSearch(val);
   }
 
   public String getSearch() throws CalFacadeException {
     return getCal().getSearch();
   }
 
   public void addFilter(BwFilter val) throws CalFacadeException {
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     getCal().addFilter(val);
   }
 
   public void updateFilter(BwFilter val) throws CalFacadeException {
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     getCal().updateFilter(val);
   }
 
   /*
   public void setFilter(BwFilter val) throws CalFacadeException {
     currentFilter = val;
   }
 
   public BwFilter getFilter() throws CalFacadeException {
     return currentFilter;
   }
 
   public void resetFilters() throws CalFacadeException {
     currentFilter = null;
   }
   */
 
   /* ====================================================================
    *                   Subscriptions
    * ==================================================================== */
 
   public void addSubscription(BwSubscription val) throws CalFacadeException {
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     setupOwnedEntity(val);
     val = (BwSubscription)val.clone(); // Avoid hibernate
 
     prefs.addSubscription(val);
 
     dbi.updatePreferences(prefs);
   }
 
   public BwSubscription findSubscription(String name) throws CalFacadeException {
     if (name == null) {
       return null;
     }
 
     Collection subs = getSubscriptions();
     Iterator it = subs.iterator();
     while (it.hasNext()) {
       BwSubscription sub = (BwSubscription)it.next();
 
       if (sub.getName().equals(name)) {
         return sub;
       }
     }
 
     return null;
   }
 
   public void removeSubscription(BwSubscription val) throws CalFacadeException {
     BwPreferences prefs = getPreferences();
     checkOwnerOrSuper(prefs);
 
     prefs.getSubscriptions().remove(val);
     dbi.updatePreferences(prefs);
   }
 
   public void updateSubscription(BwSubscription val) throws CalFacadeException {
     BwPreferences prefs = getPreferences();
     BwSubscription sub = findSubscription(val.getName());
 
     if (sub == null) {
       throw new CalFacadeException("Unknown subscription" + val.getName());
     }
 
     val.copyTo(sub);
     dbi.updatePreferences(prefs);
   }
 
   public boolean getSubscribed(BwCalendar val) throws CalFacadeException {
     BwSubscription sub = BwSubscription.makeSubscription(val, null, false, false, false);
     setupOwnedEntity(sub);
 
     return getPreferences().getSubscriptions().contains(sub);
   }
 
   public Collection getSubscriptions() throws CalFacadeException {
     return getPreferences().getSubscriptions();
   }
 
   public BwSubscription getSubscription(int id) throws CalFacadeException {
     return dbi.getSubscription(id);
   }
 
   /* ====================================================================
    *                   Free busy
    * ==================================================================== */
 
   public BwFreeBusy getFreeBusy(BwPrincipal who, BwDateTime start, BwDateTime end)
           throws CalFacadeException {
     if (!(who instanceof BwUser)) {
       throw new CalFacadeException("Unsupported: non user principal for free-busy");
     }
 
     if (isGuest() || (!currentUser().equals(who))) {
       // No access for the moment
       throw new CalFacadeAccessException();
     }
 
     BwFreeBusy fb = new BwFreeBusy(who, start, end);
 
     Collection evs = getEvents(null, null, start, end, CalFacadeDefs.retrieveRecurExpanded);
 
     try {
       /* For the moment just build a single FreeBusyComponentVO
        */
       BwFreeBusyComponent fbc = new BwFreeBusyComponent();
 
       Iterator it = evs.iterator();
 
       TreeSet eventPeriods = new TreeSet();
 
       while (it.hasNext()) {
         EventInfo ei = (EventInfo)it.next();
         BwEvent ev = ei.getEvent();
 
         // Ignore if times were specified and this event is outside the times
 
         BwDateTime estart = ev.getDtstart();
         BwDateTime eend = ev.getDtend();
 
         /* Don't report out of the requested period */
 
         String dstart;
         String dend;
 
         if (estart.before(start)) {
           dstart = start.getDtval();
         } else {
           dstart = estart.getDtval();
         }
 
         if (eend.after(end)) {
           dend = end.getDtval();
         } else {
           dend = eend.getDtval();
         }
 
         eventPeriods.add(new EventPeriod(new DateTime(dstart),
                                          new DateTime(dend)));
       }
 
       /* iterate through the sorted periods combining them where they are
          adjacent or overlap */
 
       Period p = null;
 
       it = eventPeriods.iterator();
       while (it.hasNext()) {
         EventPeriod ep = (EventPeriod)it.next();
 
         if (p == null) {
           p = new Period(ep.start, ep.end);
         } else if (ep.start.after(p.getEnd())) {
           // Non adjacent periods
           fbc.addPeriod(p);
           p = new Period(ep.start, ep.end);
         } else if (ep.end.after(p.getEnd())) {
           // Extend the current period
           p = new Period(p.getStart(), ep.end);
         } // else it falls within the existing period
       }
 
       if (p != null) {
         fbc.addPeriod(p);
       }
 
       fb.addTime(fbc);
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     return fb;
   }
 
   private static class EventPeriod implements Comparable {
     DateTime start;
     DateTime end;
 
     EventPeriod(DateTime start, DateTime end) {
       this.start = start;
       this.end = end;
     }
 
     public int compareTo(Object o) {
       if (!(o instanceof EventPeriod)) {
         return -1;
       }
 
       EventPeriod that = (EventPeriod)o;
 
       int res = start.compareTo(that.start);
       if (res != 0) {
         return res;
       }
 
       return end.compareTo(that.end);
     }
   }
 
   /* ====================================================================
    *                   Categories
    * ==================================================================== */
 
   public Collection getCategories() throws CalFacadeException {
     return getCal().getCategories(getUser(), null);
   }
 
   public Collection getPublicCategories() throws CalFacadeException {
     return getCal().getCategories(getPublicUser(), null);
   }
 
   public Collection getEditableCategories() throws CalFacadeException {
     if (!isPublicAdmin()) {
       return getCal().getCategories(getUser(), null);
     }
 
     if (isSuper() || adminCanEditAllPublicCategories) {
       return getCal().getCategories(getPublicUser(), null);
     }
     return getCal().getCategories(getPublicUser(), getUser());
   }
 
   public BwCategory getCategory(int id) throws CalFacadeException {
     return getCal().getCategory(id);
   }
 
   public void addCategory(BwCategory val) throws CalFacadeException {
     getCal().addCategory(val);
   }
 
   public void replaceCategory(BwCategory val) throws CalFacadeException {
     getCal().updateCategory(val);
   }
 
   public int deleteCategory(BwCategory val) throws CalFacadeException {
     /** Only allow delete if not in use
      */
     if (getCal().getCategoryRefs(val).size() > 0) {
       return 2;
     }
 
     /* Remove from preferences */
     getUserAuth().removeCategory(null, val);
 
     /* Attempt to delete
      */
     if (getCal().deleteCategory(val)) {
       return 0;
     }
 
     return 1; //doesn't exist
   }
 
   public BwCategory findCategory(BwCategory cat) throws CalFacadeException {
     setupSharableEntity(cat);
     return getCal().findCategory(cat);
   }
 
   public boolean ensureCategoryExists(BwCategory category) throws CalFacadeException {
     if (category.getId() != 0) {
       // We've already checked it exists
       return false;
     }
 
     category.setPublick(pars.getPublicAdmin());
     category.setCreator(getUser());
 
     // Assume a new category - but see if we can find it.
     BwCategory k = findCategory(category);
 
     if (k != null) {
       category.setId(k.getId());
       return false;
     }
 
     // doesn't exist at this point, so we add it to db table
 
     getCal().addCategory(category);
 
     return true;
   }
 
   /* ====================================================================
    *                   Locations
    * ==================================================================== */
 
   public Collection getLocations() throws CalFacadeException {
     return getCal().getLocations(getUser(), null);
   }
 
   public Collection getPublicLocations() throws CalFacadeException {
     return getCal().getLocations(getPublicUser(), null);
   }
 
   public Collection getEditableLocations() throws CalFacadeException {
     if (!isPublicAdmin()) {
       return getCal().getLocations(getUser(), null);
     }
 
     if (isSuper() || adminCanEditAllPublicLocations) {
       return getCal().getLocations(getPublicUser(), null);
     }
     return getCal().getLocations(getPublicUser(), getUser());
   }
 
   /** Add a location to the database. The id will be set in the parameter
    * object.
    *
    * @param val   LocationVO object to be added
    * @return boolean true for added, false for already exists
    * @throws CalFacadeException
    */
   public boolean addLocation(BwLocation val) throws CalFacadeException {
     updateOK(val);
 
     setupSharableEntity(val);
 
     if (findLocation(val) != null) {
       return false;
     }
 
     if (debug) {
       trace("Add location " + val);
     }
 
     getCal().addLocation(val);
     return true;
   }
 
   public void replaceLocation(BwLocation val) throws CalFacadeException {
     getCal().updateLocation(val);
   }
 
   public BwLocation getLocation(int id) throws CalFacadeException {
     return getCal().getLocation(id);
   }
 
   public BwLocation findLocation(BwLocation val) throws CalFacadeException {
     setupSharableEntity(val);
     return getCal().findLocation(val);
   }
 
   public int deleteLocation(BwLocation val) throws CalFacadeException {
     deleteOK(val);
     if (val.getId() <= CalFacadeDefs.maxReservedLocationId) {
       // claim it doesn't exist
       return 1;
     }
 
     /** Only allow delete if not in use
      */
     if (getCal().getLocationRefs(val).size() != 0) {
       return 2;
     }
 
     if (pars.getPublicAdmin()) {
       /* Remove from preferences */
       getUserAuth().removeLocation(null, val);
     }
 
     /* Attempt to delete
      */
     if (getCal().deleteLocation(val)) {
       return 0;
     }
 
     return 1; //doesn't exist
   }
 
   public BwLocation ensureLocationExists(BwLocation val)
       throws CalFacadeException {
     if (val.getId() > 0) {
       // We've already checked it exists
       return val;
     }
 
     // Assume a new entity
     setupSharableEntity(val);
     BwLocation l = findLocation(val);
 
     if (l != null) {
       return l;
     }
 
     // doesn't exist at this point, so we add it to db table
     addLocation(val);
 
     return null;
   }
 
   public Collection getLocationRefs(BwLocation val) throws CalFacadeException {
     return getCal().getLocationRefs(val);
   }
 
   /* ====================================================================
    *                   Sponsors
    * ==================================================================== */
 
   public Collection getSponsors() throws CalFacadeException {
     return getCal().getSponsors(getUser(), null);
   }
 
   public Collection getPublicSponsors() throws CalFacadeException {
     return getCal().getSponsors(getPublicUser(), null);
   }
 
   public Collection getEditableSponsors() throws CalFacadeException {
     if (!isPublicAdmin()) {
       return getCal().getSponsors(getUser(), null);
     }
 
     if (isSuper() || adminCanEditAllPublicSponsors) {
       return getCal().getSponsors(getPublicUser(), null);
     }
     return getCal().getSponsors(getPublicUser(), getUser());
   }
 
   public boolean addSponsor(BwSponsor val) throws CalFacadeException {
     updateOK(val);
 
     setupSharableEntity(val);
 
     if (findSponsor(val) != null) {
       return false;
     }
 
     if (debug) {
       trace("Add sponsor " + val);
     }
 
     getCal().addSponsor(val);
     return true;
   }
 
   public void replaceSponsor(BwSponsor val) throws CalFacadeException {
     updateOK(val);
     getCal().updateSponsor(val);
   }
 
   public BwSponsor getSponsor(int id) throws CalFacadeException {
     return getCal().getSponsor(id);
   }
 
   public BwSponsor findSponsor(BwSponsor s) throws CalFacadeException {
     setupSharableEntity(s);
     return getCal().findSponsor(s);
   }
 
   public int deleteSponsor(BwSponsor val) throws CalFacadeException {
     deleteOK(val);
     if (val.getId() <= CalFacadeDefs.maxReservedLocationId) {
       // claim it doesn't exist
       return 1;
     }
 
     /** Only allow delete if not in use
      */
     if (getCal().getSponsorRefs(val).size() != 0) {
       return 2;
     }
 
     if (pars.getPublicAdmin()) {
       /* Remove from all auth preferences */
       getUserAuth().removeSponsor(null, val);
     }
 
     /* Attempt to delete
      */
     if (getCal().deleteSponsor(val)) {
       return 0;
     }
 
     return 1; //doesn't exist
   }
 
   public BwSponsor ensureSponsorExists(BwSponsor val)
       throws CalFacadeException {
     if (val.getId() > 0) {
       // We've already checked it exists
       return val;
     }
 
     // Assume a new entity
     setupSharableEntity(val);
     BwSponsor s = findSponsor(val);
 
     if (s != null) {
       return s;
     }
 
     // doesn't exist at this point, so we add it to db
     addSponsor(val);
 
     return null;
   }
 
   public Collection getSponsorRefs(BwSponsor val) throws CalFacadeException {
     return getCal().getSponsorRefs(val);
   }
 
   /* ====================================================================
    *                   Events
    * ==================================================================== */
 
   public EventInfo getEvent(int eventId) throws CalFacadeException {
     return postProcess(getCal().getEvent(eventId), null);
   }
 
   public Collection getEvent(String guid, String recurrenceId,
                              int recurRetrieval) throws CalFacadeException {
     return postProcess(getCal().getEvent(guid, recurrenceId, null, recurRetrieval),
                        null);
   }
 
   public Collection getEvents(BwSubscription sub,
                          int recurRetrieval) throws CalFacadeException {
     return getEvents(sub, null, null, null, recurRetrieval);
   }
 
   public Collection getEvents(BwSubscription sub, BwFilter filter,
                               BwDateTime startDate, BwDateTime endDate,
                               int recurRetrieval)
           throws CalFacadeException {
     TreeSet ts = new TreeSet();
 
     if (pars.getPublicAdmin() || (sub != null)) {
       BwCalendar cal = null;
       if (sub != null) {
         cal = sub.getCalendar();
       }
       return postProcess(getCal().getEvents(cal, filter, startDate,
                                             endDate, recurRetrieval),
                          sub);
     }
 
     // Iterate over the subscriptions and merge the results.
     Iterator it;
 
     if (currentView != null) {
       it = currentView.iterateSubscriptions();
     } else {
       Collection subs = getSubscriptions();
       if (subs == null) {
         sub = new BwSubscription();
         sub.setName("All events"); // XXX property?
         sub.setDisplay(true);
         sub.setInternalSubscription(true);
 
         return postProcess(getCal().getEvents(null, filter, startDate,
                                               endDate, recurRetrieval),
                            sub);
       }
 
       it = subs.iterator();
     }
 
     while (it.hasNext()) {
       sub = (BwSubscription)it.next();
 
       if (sub.getInternalSubscription() && !sub.getCalendarDeleted()) {
         BwCalendar calendar = sub.getCalendar();
 
         if (calendar == null) {
           String path;
           String uri = sub.getUri();
 
           if (uri.startsWith(CalFacadeDefs.bwUriPrefix)) {
             path = uri.substring(CalFacadeDefs.bwUriPrefix.length());
           } else {
             // Shouldn't happen?
             path = uri;
           }
 
           if (debug) {
             trace("Search for calendar \"" + path + "\"");
           }
 
           calendar = getCal().getCalendar(path);
           if (calendar == null) {
             // Assume deleted
             sub.setCalendarDeleted(true);
             getCal().updateUser(getUser());
           } else {
             sub.setCalendar(calendar);
           }
         }
 
         if (calendar != null) {
           ts.addAll(postProcess(getCal().getEvents(calendar, filter,
                                                    startDate, endDate,
                                                    recurRetrieval),
                                 sub));
         }
       }
     }
 
     return ts;
   }
 
   public DelEventResult deleteEvent(BwEvent event,
                                     boolean delUnreffedLoc) throws CalFacadeException {
     DelEventResult der = new DelEventResult(false, false, 0);
 
     if (event == null) {
       return der;
     }
 
     BwLocation loc = event.getLocation();
 
     Calintf.DelEventResult cider = getCal().deleteEvent(event);
 
     if (!cider.eventDeleted) {
       return der;
     }
 
     der.eventDeleted = true;
     der.alarmsDeleted = cider.alarmsDeleted;
 
     if (delUnreffedLoc) {
       if ((loc != null) &&
           (getCal().getLocationRefs(loc).size() == 0) &&
           (getCal().deleteLocation(loc))) {
         der.locationDeleted = true;
       }
     }
 
     return der;
   }
 
   public EventUpdateResult addEvent(BwEvent event,
                                     Collection overrides) throws CalFacadeException {
     EventUpdateResult updResult = new EventUpdateResult();
 
     setupSharableEntity(event);
 
     BwLocation loc = event.getLocation();
     BwSponsor sp = event.getSponsor();
 
     if ((sp != null) && (ensureSponsorExists(sp) == null)) {
       updResult.sponsorsAdded++;
     }
 
     if ((loc != null) && (ensureLocationExists(loc) == null)) {
       updResult.locationsAdded++;
     }
 
     /* If no calendar has been assigned to this event set it to the default
      * calendar for non-public events or reject it for public events.
      */
 
     if (event.getCalendar() == null) {
       if (event.getPublick()) {
         throw new CalFacadeException("No calendar assigned");
       }
 
       event.setCalendar(getPreferences().getDefaultCalendar());
     }
 
     if (!event.getCalendar().getCalendarCollection()) {
       throw new CalFacadeAccessException();
     }
 
     event.setDtstamp(new DtStamp(new DateTime(true)).getValue());
     event.setLastmod(new LastModified(new DateTime(true)).getValue());
     event.setCreated(new Created(new DateTime(true)).getValue());
 
     getCal().addEvent(event, overrides);
 
     if (isPublicAdmin()) {
       /* Mail event to any subscribers */
     }
 
     return updResult;
   }
 
   public void updateEvent(BwEvent event) throws CalFacadeException {
     event.setLastmod(new LastModified(new DateTime(true)).getValue());
 
     getCal().updateEvent(event);
 
     if (isPublicAdmin()) {
       /* Mail event to any subscribers */
     }
   }
 
   public boolean deleteSubscribedEvent(BwEvent event) throws CalFacadeException {
     /*
     if (!getCal().fromSubscription(event)) {
       throw new CalFacadeException(
             "Attempt to hide a non-subscribed event: " + event);
     }
     */
     throw new CalFacadeException("Unimplemented: ");
   }
 
   /* ====================================================================
    *                       Caldav support
    * Caldav as it stands at the moment requires that we save the arbitary
    * names clients might assign to events.
    * ==================================================================== */
 
   public Collection findEventsByName(BwCalendar cal, String val)
           throws CalFacadeException {
     return getCal().getEventsByName(cal, val);
   }
 
   /* ====================================================================
    *                   Synchronization
    * ==================================================================== */
 
   public BwSynchInfo getSynchInfo() throws CalFacadeException {
     return getCal().getSynchInfo();
   }
 
   public void addSynchInfo(BwSynchInfo val) throws CalFacadeException {
     getCal().addSynchInfo(val);
   }
 
   public void updateSynchInfo(BwSynchInfo val) throws CalFacadeException {
     getCal().updateSynchInfo(val);
   }
 
   public BwSynchState getSynchState(BwEvent ev)
       throws CalFacadeException {
     return getCal().getSynchState(ev);
   }
 
   public Collection getDeletedSynchStates() throws CalFacadeException {
     return getCal().getDeletedSynchStates();
   }
 
   public void addSynchState(BwSynchState val)
       throws CalFacadeException {
     getCal().addSynchState(val);
   }
 
   public void updateSynchState(BwSynchState val)
       throws CalFacadeException {
     getCal().updateSynchState(val);
   }
 
   public void getSynchData(BwSynchState val) throws CalFacadeException {
     getCal().getSynchData(val);
   }
 
   public void setSynchData(BwSynchState val) throws CalFacadeException {
     getCal().setSynchData(val);
   }
 
   public void updateSynchStates() throws CalFacadeException {
     getCal().updateSynchStates();
   }
 
   /* ====================================================================
    *                       Alarms
    * ==================================================================== */
 
   public Collection getAlarms(BwEvent event, BwUser user) throws CalFacadeException {
     return getCal().getAlarms(event, user);
   }
 
   public void setAlarm(BwEvent event,
                        BwEventAlarm alarm) throws CalFacadeException {
     // Do some sort of validation here.
     alarm.setEvent(event);
     alarm.setUser(getUser());
     getCal().addAlarm(alarm);
   }
 
   public void updateAlarm(BwAlarm val) throws CalFacadeException {
     getCal().updateAlarm(val);
   }
 
   public Collection getUnexpiredAlarms(BwUser user) throws CalFacadeException {
     return getCal().getUnexpiredAlarms(user);
   }
 
   public Collection getUnexpiredAlarms(BwUser user, long triggerTime)
           throws CalFacadeException {
     return getCal().getUnexpiredAlarms(user, triggerTime);
   }
 
   /* ====================================================================
    *                   Access control
    * ==================================================================== */
 
   /* This provides some limits to shareable entity updates for the
    * admin users. It is applied in addition to the normal access checks
    * applied at the lower levels.
    */
   private void updateOK(Object o) throws CalFacadeException {
     if (isGuest()) {
       throw new CalFacadeAccessException();
     }
 
     if (isSuper()) {
       // Always ok
       return;
     }
 
     if (!(o instanceof BwShareableDbentity)) {
       throw new CalFacadeAccessException();
     }
 
     if (!isPublicAdmin()) {
       // Normal access checks apply
       return;
     }
 
     BwShareableDbentity ent = (BwShareableDbentity)o;
 
     if (adminCanEditAllPublicSponsors ||
         ent.getCreator().equals(currentUser())) {
       return;
     }
 
     throw new CalFacadeAccessException();
   }
 
   /* This checks to see if the current user has owner access based on the
    * supplied object. This is used to limit access to objects not normally
    * shared such as preferences and related objects like veiws and subscriptions.
    */
   private void checkOwnerOrSuper(Object o) throws CalFacadeException {
     if (isGuest()) {
       throw new CalFacadeAccessException();
     }
 
     if (isSuper()) {
       // Always ok?
       return;
     }
 
     if (!(o instanceof BwOwnedDbentity)) {
       throw new CalFacadeAccessException();
     }
 
     BwOwnedDbentity ent = (BwOwnedDbentity)o;
 
     BwUser u;
 
     /*if (!isPublicAdmin()) {
       // Expect a different owner - always public-user????
       return;
     }*/
 
     u = getUser();
 
     if (u.equals(ent.getOwner())) {
       return;
     }
 
     throw new CalFacadeAccessException();
   }
 
   /* ====================================================================
    *                   Package and protected methods
    * ==================================================================== */
 
   /** Get the current db session
    *
    * @return Object
    */
   Object getDbSession() throws CalFacadeException {
     return getCal().getDbSession();
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private void deleteOK(Object o) throws CalFacadeException {
     updateOK(o);
   }
 
   /* Get a mailer object which allows the application to mail Message
    * objects.
    *
    * @return MailerIntf    implementation.
    * /
   private MailerIntf getMailer() throws CalFacadeException {
     if (mailer != null) {
       return mailer;
     }
 
     try {
       mailer = (MailerIntf)CalEnv.getGlobalObject("mailerclass",
                                                   MailerIntf.class);
       mailer.init(this, debug);
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     return mailer;
   }*/
 
   private EventInfo postProcess(BwEvent ev, BwSubscription sub)
           throws CalFacadeException {
     if (ev == null) {
       return null;
     }
 
     //trace("ev: " + ev);
 
     /* If the event is an event reference (an alias) implant it in an event
      * proxy and return that object.
      */
     if (ev instanceof BwEventAnnotation) {
       ev = new BwEventProxy((BwEventAnnotation)ev);
     }
 
     EventInfo ei = new EventInfo(ev);
     ei.setSubscription(sub);
     ei.setRecurrenceId(ev.getRecurrence().getRecurrenceId());
 
     return ei;
   }
 
   private Collection postProcess(Collection evs, BwSubscription sub)
           throws CalFacadeException {
     Vector v = new Vector();
 
     Iterator it = evs.iterator();
 
     while (it.hasNext()) {
       BwEvent ev = (BwEvent)it.next();
       EventInfo ei = postProcess(ev, sub);
       v.addElement(ei);
     }
 
     return v;
   }
 
   private BwPreferences getPreferences() throws CalFacadeException {
     return dbi.getPreferences();
   }
 
   /*
   private BwDateTime todaysDateTime() {
     return CalFacadeUtil.getDateTime(new java.util.Date(System.currentTimeMillis()),
                                      true, false);
   }*/
 
   Calintf getCal() throws CalFacadeException {
     if (cali != null) {
       return cali;
     }
 
     try {
       cali = (Calintf)CalEnv.getGlobalObject("calintfclass", Calintf.class);
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     try {
       cali.open(); // Just for the user interactions
       cali.beginTransaction();
 
       boolean userCreated = cali.init(pars.getAuthUser(),
                                       pars.getUser(),
                                       pars.getPublicAdmin(),
                                       pars.isSuperUser(),
                                       getGroups(),
                                       pars.getSynchId(),
                                       debug);
 
       // Prepare for call below.
       publicUserAccount = cali.getSyspars().getPublicUser();
 
       BwUser auth;
// XXX      if (isPublicAdmin() || isGuest()) {
      if (isGuest()) {
         auth = getPublicUser();
       } else {
         auth = cali.getUser(pars.getAuthUser());
       }

      if (debug) {
        trace("Got auth user object " + auth);
      }
       dbi = new CalSvcDb(this, auth);
 
       if (userCreated) {
         // Add preferences
         BwPreferences prefs = new BwPreferences();
 
         BwCalendar cal = cali.getDefaultCalendar();
 
         prefs.setOwner(auth);
         prefs.setDefaultCalendar(cal);
 
         // Add default subscription for default calendar.
         BwSubscription sub = BwSubscription.makeSubscription(cal, cal.getName(), true, true, false);
         setupOwnedEntity(sub);
 
         prefs.addSubscription(sub);
 
         // Add default subscription for trash calendar.
 
         cal = cali.getTrashCalendar();
         sub = BwSubscription.makeSubscription(cal, cal.getName(), false, false, false);
         setupOwnedEntity(sub);
 
         prefs.addSubscription(sub);
 
         dbi.updatePreferences(prefs);
       }
 
       return cali;
     } finally {
       cali.endTransaction();
       cali.close();
     }
   }
 
   private BwUser currentUser() throws CalFacadeException {
     return getUser();
   }
 
   /*
   private BwAuthUser getAdminUser() throws CalFacadeException {
     if (adminUser == null) {
       adminUser = getUserAuth().getUser(pars.getAuthUser());
     }
 
     return adminUser;
   }
   */
 
   /* See if in public admin mode
    */
   private boolean isPublicAdmin() throws CalFacadeException {
     return pars.getPublicAdmin();
   }
 
   /* See if current authorised user has super user access.
    */
   private boolean isSuper() throws CalFacadeException {
     return pars.getPublicAdmin() && pars.isSuperUser();
   }
 
   /* See if current authorised is a guest.
    */
   private boolean isGuest() throws CalFacadeException {
     return pars.isGuest();
   }
 
   /*
   private boolean checkField(String fld1, String fld2) {
     if (fld1 == null) {
       if (fld2 == null) {
         return true;
       }
     } else if (fld1.equals(fld2)) {
       return true;
     }
 
     return false;
   }
   */
 
   private UserAuthCallBack getUserAuthCallBack() {
     if (uacb == null) {
       uacb = new UserAuthCallBack(this);
     }
 
     return (UserAuthCallBack)uacb;
   }
 
   private GroupsCallBack getGroupsCallBack() {
     if (gcb == null) {
       gcb = new GroupsCallBack(this);
     }
 
     return (GroupsCallBack)gcb;
   }
 
   private class IcalCallbackcb implements IcalCallback {
     public BwUser getUser() throws CalFacadeException {
       return CalSvc.this.getUser();
     }
 
     public BwCategory findCategory(BwCategory val) throws CalFacadeException {
       return CalSvc.this.findCategory(val);
     }
 
     public void addCategory(BwCategory val) throws CalFacadeException {
       CalSvc.this.addCategory(val);
     }
 
     public BwLocation ensureLocationExists(String address) throws CalFacadeException {
       BwLocation loc = new BwLocation();
       loc.setAddress(address);
       loc.setOwner(getUser());
 
       BwLocation l = CalSvc.this.ensureLocationExists(loc);
 
       if (l == null) {
         return loc;
       }
 
       return l;
     }
 
     public Collection getEvent(String guid, String rid,
                                Integer seq,
                                int recurRetrieval) throws CalFacadeException {
       return CalSvc.this.getEvent(guid, rid, recurRetrieval);
     }
 
     public URIgen getURIgen() throws CalFacadeException {
       return null;
     }
 
     public CalTimezones getTimezones() throws CalFacadeException {
       return getCal().getTimezones();
     }
 
     public void saveTimeZone(String tzid, VTimeZone vtz
                              ) throws CalFacadeException {
       timezones.saveTimeZone(tzid, vtz);
     }
 
     public void registerTimeZone(String id, TimeZone timezone)
         throws CalFacadeException {
       timezones.registerTimeZone(id, timezone);
     }
 
     public TimeZone getTimeZone(final String id) throws CalFacadeException {
       return timezones.getTimeZone(id);
     }
 
     public VTimeZone findTimeZone(final String id, BwUser owner) throws CalFacadeException {
       return timezones.findTimeZone(id, owner);
     }
   }
 
   /** Set the owner and creator on a shareable entity.
    *
    * @param entity
    * @throws CalFacadeException
    */
   private void setupSharableEntity(BwShareableDbentity entity)
           throws CalFacadeException {
     if (entity.getCreator() == null) {
       entity.setCreator(getUser());
     }
 
     setupOwnedEntity(entity);
   }
 
   /** Set the owner and publick on an owned entity.
    *
    * @param entity
    * @throws CalFacadeException
    */
   private void setupOwnedEntity(BwOwnedDbentity entity)
           throws CalFacadeException {
     entity.setPublick(isPublicAdmin());
 
     if (entity.getOwner() == null) {
       BwUser owner;
 
       if (entity.getPublick()) {
         owner = getPublicUser();
       } else {
         owner = getUser();
       }
 
       entity.setOwner(owner);
     }
   }
 
   /* Get a logger for messages
    */
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void logIt(String msg) {
     getLogger().info(msg);
   }
 
   private void trace(String msg) {
     getLogger().debug(msg);
   }
 }
 
