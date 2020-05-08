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
 package org.bedework.dumprestore.restore;
 
 
 import org.bedework.calfacade.BwAlarm;
 import org.bedework.calfacade.BwAttendee;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwOrganizer;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwSponsor;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.BwTimeZone;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.BwUserInfo;
 import org.bedework.calfacade.CalFacadeDefs;
 import org.bedework.calfacade.filter.BwFilter;
 import org.bedework.calfacade.svc.BwAdminGroup;
 import org.bedework.calfacade.svc.BwAdminGroupEntry;
 import org.bedework.calfacade.svc.BwAuthUser;
 import org.bedework.calfacade.svc.BwCalSuite;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.dumprestore.BwDbLastmod;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.Types;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.hibernate.FlushMode;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.Session;
 import org.hibernate.StatelessSession;
 import org.hibernate.cfg.Configuration;
 
 /** Class which restores via jdbc.
  *
  * @author Mike Douglass   douglm@rpi.edu
  * @version 1.0
  */
 public class HibRestore implements RestoreIntf {
 //  private boolean debug;
 
   private RestoreGlobals globals;
 
   //private HibSession sess;
   private SessionFactory sessFactory;
 
 
   private Session hibSess;
   private StatelessSession sess;
 
   private int adminGroupId = 1;
 
   private transient Logger log;
 
   /**
    * @param debug
    */
   public HibRestore(boolean debug) {
     //this.debug = debug;
     log = Logger.getLogger(getClass());
     try {
       sessFactory = new Configuration().configure().buildSessionFactory();
     } catch (Throwable t) {
       log.error("Failed to get session factory", t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#init(org.bedework.dumprestore.restore.RestoreGlobals)
    */
   public void init(RestoreGlobals globals) throws Throwable {
     this.globals = globals;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#open()
    */
   public void open() throws Throwable {
   }
 
   public void startTransaction() throws Throwable {
     // Open delayed till retore method called
   }
 
   public void endTransaction() throws Throwable {
     if (sess != null) {
       closeSession();
     }
 
     if (hibSess != null) {
       closeHibSession();
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#close()
    */
   public void close() throws Throwable {
     if (sess == null) {
       return;
     }
 
     try {
       sess.close();
     } catch (Throwable t) {
     }
   }
 
   public void restoreSyspars(BwSystem o) throws Throwable {
     openSess();
 
     save(o);
 
     closeSess();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#restoreUser(org.bedework.calfacade.BwUser)
    */
   public void restoreUser(BwUser o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     try {
       openSess();
 
       //sess.save(o, new Integer(o.getId()));
       save(o);
 
       closeSess();
     } catch (Throwable t) {
       log.error("Exception restoring user " + o);
       throw t;
     }
   }
 
   /** Restore user info
    *
    * @param o
    * @throws Throwable
    */
   public void restoreUserInfo(BwUserInfo o) throws Throwable {
     if (!globals.onlyUsersMap.check(o.getUser())) {
       return;
     }
 
     openHibSess();
 
     hibSave(o);
 
     closeHibSess();
   }
 
   public void restoreTimezone(BwTimeZone o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openSess();
 
     save(o);
 
     closeSess();
   }
 
   public void restoreAttendee(BwAttendee o) throws Throwable {
     // Ensure id not set
     o.setId(CalFacadeDefs.unsavedItemKey);
 
     openHibSess();
 
     hibSess.save(o);
 
     closeHibSess();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#restoreAdminGroup(org.bedework.calfacade.svc.BwAdminGroup)
    */
   public void restoreAdminGroup(BwAdminGroup o) throws Throwable {
     openSess();
 
     if (globals.config.getFrom2p3px()) {
       // No id assigned
       o.setId(adminGroupId);
       adminGroupId++;
     }
 
     if (!globals.onlyUsersMap.check(o.getGroupOwner())) {
       o.setGroupOwner(globals.getPublicUser());
     }
 
     save(o);
 
     log.debug("Saved admin group " + o);
 
     closeSess();
 
     /* Save members. */
 
     Collection c = o.getGroupMembers();
     Iterator it = c.iterator();
     while (it.hasNext()) {
       BwPrincipal pr = (BwPrincipal)it.next();
 
       if ((pr instanceof BwUser) && !globals.onlyUsersMap.check(pr)) {
         continue;
       }
 
       openSess();
 
       BwAdminGroupEntry entry = new BwAdminGroupEntry();
 
       entry.setGrp(o);
       entry.setMember(pr);
 
       log.debug("About to save " + entry);
 
       save(entry);
 
       closeSess();
     }
   }
 
   public BwAdminGroup getAdminGroup(String name) throws Throwable {
     openHibSess();
 
     Query q = hibSess.createQuery("from org.bedework.calfacade.svc.BwAdminGroup ag" +
         " where ag.name=:name");
     q.setString("name", name);
     return (BwAdminGroup)q.uniqueResult();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#restoreAuthUser(org.bedework.calfacade.svc.BwAuthUser)
    */
   public void restoreAuthUser(BwAuthUser o) throws Throwable {
     if (!globals.onlyUsersMap.check(o.getUser())) {
       return;
     }
 
     openHibSess();
 
 //    if (o.getId() <= 0) {
 //      o.setId(o.getUser().getId());
 //    }
 
     hibSave(o);
 
     closeHibSess();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#restoreEvent(org.bedework.calfacade.BwEvent)
    */
   public void restoreEvent(BwEvent o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openHibSess();
     hibSave(o);
     closeHibSess();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#update(org.bedework.calfacade.BwEvent)
    */
   public void update(BwEvent o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openHibSess();
 
     hibSess.update(o);
 
     closeHibSess();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#restoreCategory(org.bedework.calfacade.BwCategory)
    */
   public void restoreCategory(BwCategory o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openSess();
 
     save(o);
 
     closeSess();
   }
 
   public void restoreCalSuite(BwCalSuite o) throws Throwable {
     openHibSess();
     hibSave(o);
     closeHibSess();
   }
 
 
   /* (non-Javadoc)
    * @see org.bedework.dumprestore.restore.RestoreIntf#restoreLocation(org.bedework.calfacade.BwLocation)
    */
   public Integer restoreLocation(BwLocation o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return null;
     }
 
     openSess();
 
     StringBuffer sb = new StringBuffer();
     sb.append("select ent.id from ");
     sb.append(BwLocation.class.getName());
     sb.append(" ent where ent.address=:address and ent.owner=:owner");
 
     Query q = sess.createQuery(sb.toString());
     q.setString("address", o.getAddress());
     q.setEntity("owner", o.getOwner());
 
     Integer i = (Integer)q.uniqueResult();
 
     if (i == null) {
       // no entry with that key
 
       save(o);
     }
 
     closeSess();
 
     return i;
   }
 
   public Integer restoreSponsor(BwSponsor o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return null;
     }
 
     openSess();
 
     StringBuffer sb = new StringBuffer();
     sb.append("select ent.id from ");
     sb.append(BwSponsor.class.getName());
     sb.append(" ent where ent.name=:name and ent.owner=:owner");
 
     Query q = sess.createQuery(sb.toString());
     q.setString("name", o.getName());
     q.setEntity("owner", o.getOwner());
 
     Integer i = (Integer)q.uniqueResult();
 
     if (i == null) {
       // no entry with that key
 
       save(o);
     }
 
     closeSess();
 
     return i;
   }
 
   public void restoreOrganizer(BwOrganizer o) throws Throwable {
     openHibSess();
 
     hibSave(o);
 
     closeHibSess();
   }
 
   public void restoreFilter(BwFilter o) throws Throwable {
     if (false) {
       // XXX need fixing and we're not using them yet
       openHibSess();
 
       hibSave(o);
 
       closeHibSess();
     }
   }
 
   public void restoreUserPrefs(BwPreferences o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openHibSess();
 
     /* Unset the subscription id - hibernate cascades cause an error
      * We'll just have to go with a new id
      */
     Iterator it = o.iterateSubscriptions();
     while (it.hasNext()) {
       BwSubscription sub = (BwSubscription)it.next();
       sub.setId(CalFacadeDefs.unsavedItemKey);
 
       globals.subscriptions++;
     }
 
     /* Same for views */
     it = o.iterateViews();
     while (it.hasNext()) {
       BwView view = (BwView)it.next();
       view.setId(CalFacadeDefs.unsavedItemKey);
 
       globals.views++;
     }
 
    o.setId(CalFacadeDefs.unsavedItemKey);
     hibSave(o);
 
     closeHibSess();
   }
 
   public void restoreAlarm(BwAlarm o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openHibSess();
 
     hibSave(o);
 
     closeHibSess();
   }
 
   public void update(BwUser user) throws Throwable {
     if (!globals.onlyUsersMap.check(user)) {
       return;
     }
 
     openSess();
 
     sess.update(user);
 
     closeSess();
   }
 
   public void restoreDbLastmod(BwDbLastmod o) throws Throwable {
     openSess();
 
     save(o);
 
     closeSess();
   }
 
   public int fixUserEventsCal(BwUser u, BwCalendar cal) throws Throwable {
     /*
     PreparedStatement ps = null;
 
     try {
       ps = conn.prepareStatement(
             "UPDATE events SET calendarid=? where publicf='F' and creatorid=?");
       ps.setInt(1, cal.getId());
       ps.setInt(2, u.getId());
 
       int numUpdated = ps.executeUpdate();
 
       ps = conn.prepareStatement(
             "UPDATE users SET default_calendarid=? where userid=?");
       ps.setInt(1, cal.getId());
       ps.setInt(2, u.getId());
 
       if (ps.executeUpdate() != 1) {
         throw new Exception("Failed update of user default calendar " + u);
       }
 
       return numUpdated;
     } finally {
       if (ps != null) {
         ps.close();
       }
     }*/
     return 1;
   }
 
   /* We cannot use hibernate to set the db id here as the save will cascade
    * down all the children.
    *
    * <p>We save a skeleton copy of the calendar structure using direct jdbc
    * calls then update the structure with hibernate.
    */
   public void restoreCalendars(BwCalendar o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     openSess();
 
     restoreCalendars(o, sess.connection());
 
     sess.update(o);
 
     closeSess();
   }
 
   public BwCalendar getCalendar(String path) throws Throwable {
     openHibSess();
 
     Query q = hibSess.createQuery("from org.bedework.calfacade.BwCalendar cal where " +
                         "cal.path=:path");
     q.setString("path", path);
     BwCalendar cal = (BwCalendar)q.uniqueResult();
 
     return cal;
   }
 
   public void saveRootCalendar(BwCalendar val) throws Throwable {
     if (!globals.onlyUsersMap.check(val)) {
       return;
     }
 
     // Ensure id not set
     val.setId(CalFacadeDefs.unsavedItemKey);
 
     openHibSess();
 
     hibSess.save(val);
 
     closeHibSess();
   }
 
   /* We cannot use hibernate to set the db id here as the save will cascade
    * down all the children.
    *
    * <p>We save a skeleton copy of the calendar structure using direct jdbc
    * calls then update the structure with hibernate.
    */
   public void addCalendar(BwCalendar o) throws Throwable {
     if (!globals.onlyUsersMap.check(o)) {
       return;
     }
 
     // Ensure id not set
     o.setId(CalFacadeDefs.unsavedItemKey);
 
     openHibSess();
 
     BwCalendar parent = o.getCalendar();
 
     parent.addChild(o);
 
 //    hibSess.update(parent);
 
     closeHibSess();
   }
 
   /* ====================================================================
    *                       Private methods
    * ==================================================================== */
 
   private void restoreCalendars(BwCalendar val, Connection conn) throws Throwable {
     if (!globals.onlyUsersMap.check(val)) {
       return;
     }
 
     restoreCalendar(val, conn);
 
     Collection cals = val.getChildren();
     if (cals == null) {
       return;
     }
 
     Iterator it = cals.iterator();
 
     while (it.hasNext()) {
       BwCalendar cal = (BwCalendar)it.next();
 
       restoreCalendars(cal, conn);
     }
   }
 
   /* The only reason for this is the need to preserve the calendar id.
    * From 3.x on this need will not exist. Just use hibernate.
    *
    * Restore a single calendar. Don't restore children
    */
   private void restoreCalendar(BwCalendar val, Connection conn) throws Throwable {
     if (!globals.onlyUsersMap.check(val)) {
       return;
     }
 
     PreparedStatement ps = null;
 
     try {
       ps = conn.prepareStatement(
             "INSERT INTO calendars " +
                 "(id, seq, creatorid, ownerid, bwaccess, " +
                  "publick, calname, path, summary, description," +
                  " mail_list_id, calendar_collection, parent, caltype) " +
                "VALUES (?,?,?,?,?," +
                        "?,?,?,?,?," +
                        "?,?,?,?)");
 
       ps.setInt(1, val.getId());
       ps.setInt(2, val.getSeq());
       ps.setInt(3, val.getCreator().getId());
       ps.setInt(4, val.getOwner().getId());
       ps.setString(5, val.getAccess());
 
       ps.setString(6, boolVal(val.getPublick()));
       ps.setString(7, val.getName());
       ps.setString(8, val.getPath());
       ps.setString(9, val.getSummary());
       ps.setString(10, val.getDescription());
 
       ps.setString(11, val.getMailListId());
       ps.setString(12, boolVal(val.getCalendarCollection()));
 
       if (val.getCalendar() == null) {
         ps.setNull(13, Types.INTEGER);
       } else {
         ps.setInt(13, val.getCalendar().getId());
       }
       ps.setInt(14, val.getCalType());
 
       ps.executeUpdate();
     } catch (Throwable t) {
       log.error("Exception restoring " + val);
       throw t;
     } finally {
       if (ps != null) {
         ps.close();
       }
     }
   }
 
   private synchronized void openSess() throws Throwable {
     if (sess == null) {
       sess = sessFactory.openStatelessSession();
       sess.beginTransaction();
     }
   }
 
   private synchronized void openHibSess() throws Throwable {
     if (hibSess == null) {
       hibSess = sessFactory.openSession();
       hibSess.setFlushMode(FlushMode.COMMIT);
       hibSess.beginTransaction();
     }
   }
 
   private void closeHibSess() throws Throwable {
     endTransaction();
   }
 
   private synchronized void closeHibSession() throws Throwable {
     hibSess.getTransaction().commit();
     try {
       if (hibSess != null) {
         hibSess.close();
         //hibSess.disconnect();
       }
    // } catch (Throwable t) {
       // Discard session if we get errors on close.
 //      sess = null;
     } finally {
       hibSess = null;
     }
   }
 
   private synchronized void closeSess() throws Throwable {
     endTransaction();
   }
 
   private synchronized void closeSession() throws Throwable {
     if (sess.getTransaction() != null) {
       sess.getTransaction().commit();
     }
     try {
       if (sess != null) {
         sess.close();
   //      sess.disconnect();
       }
    // } catch (Throwable t) {
       // Discard session if we get errors on close.
 //      sess = null;
     } finally {
       sess = null;
     }
   }
 
   private void hibSave(Object o) throws Throwable {
     hibSess.save(o);
   }
 
   private void save(Object o) throws Throwable {
     sess.insert(o);
   }
 
   /* Start a (possibly long-running) transaction. In the web environment
    * this might do nothing. The endTransaction method should in some way
    * check version numbers to detect concurrent updates and fail with an
    * exception.
    * /
   private void beginTransaction() throws Throwable {
     sess.beginTransaction();
   }
 
   / * End a (possibly long-running) transaction. In the web environment
    * this should in some way check version numbers to detect concurrent updates
    * and fail with an exception.
    * /
   public void endTransaction() throws Throwable {
     /* Just commit * /
     sess.commit();
   }*/
 
   private String boolVal(boolean val) {
     if (val) {
       return "T";
     }
 
     return "F";
   }
 }
