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
 package org.bedework.calfacade.svc;
 
 import org.bedework.calfacade.base.BwOwnedDbentity;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.CalFacadeDefs;
 
 /** A subscription in Bedework. We represent subscriptions as urls to the
  * calendar.
  *
  * <p>Internally handled subscriptions for the moment take the form:<br/>
  *   bwcal:///public/cal-path  or <br/>
  *   bwcal:///user/user-name/cal-path
  *
  * <p>External subscriptions are not yet supported.
  *
  *   @author Mike Douglass douglm@rpi.edu
  *  @version 1.0
  */
 public class BwSubscription extends BwOwnedDbentity {
   /** A printable name for the subscription
    */
   private String name;
 
   /** The subscription uri
    */
   private String uri;
 
   /** Should this subscription take part in free/busy calculations?
    */
   private boolean affectsFreeBusy;
 
   /** Should this subscription be displayed by default?
    */
   private boolean display;
 
   /** The display style
    */
   private String style;
 
   /** This is a subscription to an internal calendar.
    */
   private boolean internalSubscription;
 
   /** True if we want email notification of changes. This may only work for
    * internal calendars.
    */
   private boolean emailNotifications;
 
   /** True if the calendar no longer exists.
    */
   private boolean calendarDeleted;
 
   /** Mark this subscription as unremovable by the user.
    */
   private boolean unremoveable;
 
   /* Non-db fields */
 
   /** If an internal subscription this is the calendar.
    */
   private BwCalendar calendar;
 
   /** Constructor
    *
    */
   public BwSubscription() {
   }
 
   /* ====================================================================
    *                   Bean methods
    * ==================================================================== */
 
   /** Set the name
    *
    * @param val    String name
    */
   public void setName(String val) {
     name = val;
   }
 
   /** Get the name
    *
    * @return String   name
    */
   public String getName() {
     return name;
   }
 
   /** Set the uri
    *
    * @param val    String uri
    */
   public void setUri(String val) {
     uri = val;
   }
 
   /** Get the uri
    *
    * @return String   uri
    */
   public String getUri() {
     return uri;
   }
 
   /** Set the affectsFreeBusy flag
    *
    *  @param val    true if the subscription takes part in free/busy calculations
    */
   public void setAffectsFreeBusy(boolean val) {
     affectsFreeBusy = val;
   }
 
   /** Doesthe subscription take part in free/busy calculations?
    *
    *  @return boolean    true if the subscription takes part in free/busy calculations
    */
   public boolean getAffectsFreeBusy() {
     return affectsFreeBusy;
   }
 
   /** Should the subscription be displayed?
    *
    * @param val   boolean true if the subscription is to be displayed
    */
   public void setDisplay(boolean val) {
     display = val;
   }
 
   /** Should the subscription be displayed?
    *
    * @return boolean  true if the subscription is to be displayed
    */
   public boolean getDisplay() {
     return display;
   }
 
   /** Set the style
    *
    * @param val    String style
    */
   public void setStyle(String val) {
     style = val;
   }
 
   /** Get the style
    *
    * @return String   style
    */
   public String getStyle() {
     return style;
   }
 
   /** true if this is a subscription to an internal calendar.
    *
    * @param val   boolean true if this is a subscription to an internal calendar.
    */
   public void setInternalSubscription(boolean val) {
     internalSubscription = val;
   }
 
   /** true if this is a subscription to an internal calendar.
    *
    * @return boolean  true if this is a subscription to an internal calendar.
    */
   public boolean getInternalSubscription() {
     return internalSubscription;
   }
 
   /** Should we send email notifications?
    *
    * @param val   boolean true if we want email notifications
    */
   public void setEmailNotifications(boolean val) {
     emailNotifications = val;
   }
 
   /** Should we send email notifications?
    *
    * @return boolean  true for email notifications
    */
   public boolean getEmailNotifications() {
     return emailNotifications;
   }
 
   /** Is the internal target calendar deleted?
    *
    * @param val   boolean true if the internal target calendar is deleted
    */
   public void setCalendarDeleted(boolean val) {
     calendarDeleted = val;
   }
 
   /** Is the internal target calendar deleted?
    *
    * @return boolean  true if the internal target calendar is deleted
    */
   public boolean getCalendarDeleted() {
     return calendarDeleted;
   }
 
   /** Is the subscription unremoveable?
    *
    * @param val   boolean true if the subscription is unremoveable
    */
   public void setUnremoveable(boolean val) {
     unremoveable = val;
   }
 
   /** Is the subscription unremoveable?
    *
    * @return boolean  true if the subscription is unremoveable
    */
   public boolean getUnremoveable() {
     return unremoveable;
   }
 
   /* ====================================================================
    *                   Non-db methods
    * ==================================================================== */
 
   /** Set the calendar
    *
    *  @param val    BwCalendar calendar
    */
   public void setCalendar(BwCalendar val) {
     calendar = val;
   }
 
   /** Get the calendar
    *
    *  @return BwCalendar      the calendar
    */
   public BwCalendar getCalendar() {
     return calendar;
   }
 
   /* ====================================================================
    *                   Factory methods
    * ==================================================================== */
 
   /** Make a subscription to the calendar object defaulting most fields
    *
    * @param  val            BwCalendar the calendar to subscribe to
    * @return BwSubscription a new subscription object
    */
   public static BwSubscription makeSubscription(BwCalendar val) {
     BwSubscription sub = new BwSubscription();
 
     sub.setName(val.getName());
     sub.setUri(CalFacadeDefs.bwUriPrefix + val.getPath());
     sub.setDisplay(true);
     sub.setAffectsFreeBusy(true);
     sub.setInternalSubscription(true);
     sub.setCalendar(val);
     sub.setInternalSubscription(true);
     sub.setEmailNotifications(false);
 
     return sub;
   }
 
   /** Make a subscription to the calendar object
    *
    * @param  val            BwCalendar the calendar to subscribe to
    * @param name
    * @param display
    * @param affectsFreeBusy
    * @param emailNotification
    * @return BwSubscription a new subscription object
    */
   public static BwSubscription makeSubscription(BwCalendar val,
                                                 String name,
                                                 boolean display,
                                                 boolean affectsFreeBusy,
                                                 boolean emailNotification) {
     BwSubscription sub = new BwSubscription();
 
     sub.setName(name);
     sub.setUri(CalFacadeDefs.bwUriPrefix + val.getPath());
     sub.setInternalSubscription(true);
     sub.setDisplay(display);
     sub.setAffectsFreeBusy(affectsFreeBusy);
     sub.setEmailNotifications(emailNotification);
 
     sub.setCalendar(val);
 
     return sub;
   }
 
   /** Make a subscription to the calendar url
    *
    * @param  url            URL to external calendar
    * @param name
    * @param display
    * @param affectsFreeBusy
    * @param emailNotification
    * @return BwSubscription a new subscription object
    */
   public static BwSubscription makeSubscription(String url,
                                                 String name,
                                                 boolean display,
                                                 boolean affectsFreeBusy,
                                                 boolean emailNotification) {
     BwSubscription sub = new BwSubscription();
 
     sub.setName(name);
     sub.setUri(url);
     sub.setInternalSubscription(false);
     sub.setDisplay(display);
     sub.setAffectsFreeBusy(affectsFreeBusy);
     sub.setEmailNotifications(emailNotification);
 
     return sub;
   }
 
   /** Copy values
    *
    * @param sub
    */
   public void copyTo(BwSubscription sub) {
     super.copyTo(sub);
     sub.setName(getName());
     sub.setUri(getUri());
     sub.setInternalSubscription(getInternalSubscription());
     sub.setDisplay(getDisplay());
     sub.setAffectsFreeBusy(getAffectsFreeBusy());
     sub.setEmailNotifications(getEmailNotifications());
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   /** Compare this subscription and an object. Equal if the owner and name
    * are equal
    *
    * @param  o    object to compare.
    * @return int -1, 0, 1
    */
   public int compareTo(Object o) {
     if (o == this) {
       return 0;
     }
 
     if (o == null) {
       return -1;
     }
 
     if (!(o instanceof BwSubscription)) {
       return -1;
     }
 
     BwSubscription that = (BwSubscription)o;
 
     int res = getOwner().compareTo(that.getOwner());
     if(res != 0) {
       return res;
     }
 
     return getName().compareTo(that.getName());
   }
 
   public int hashCode() {
     return getName().hashCode();
   }
 
   public boolean equals(Object obj) {
     return compareTo(obj) == 0;
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer("BwSubscription(");
 
     toStringSegment(sb);
     sb.append(", name=");
     sb.append(String.valueOf(getName()));
     sb.append(", uri=");
     sb.append(String.valueOf(getUri()));
     sb.append(", unremoveable=");
     sb.append(getUnremoveable());
     sb.append(")");
 
     return sb.toString();
   }
 
   public Object clone() {
     BwSubscription sub = new BwSubscription();
 
     copyTo(sub);
 
     return sub;
   }
 }
