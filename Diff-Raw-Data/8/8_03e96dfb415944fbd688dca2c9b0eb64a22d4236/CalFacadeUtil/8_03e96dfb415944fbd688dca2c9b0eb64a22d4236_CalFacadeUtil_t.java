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
 package org.bedework.calfacade;
 
 import org.bedework.calfacade.ifs.CalTimezones;
 import org.bedework.calfacade.svc.EventInfo;
 
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.ParameterList;
 import net.fortuna.ical4j.model.Period;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.property.DateProperty;
 
 import java.io.Serializable;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 
 /** A few helpers
  *
  * @author Mike Douglass     douglm@rpi.edu
  *  @version 1.0
  */
 public class CalFacadeUtil implements Serializable {
   private static final DateFormat isoDateFormat =
       new SimpleDateFormat("yyyyMMdd");
 
   private static final DateFormat isoDateTimeFormat =
       new SimpleDateFormat("yyyyMMdd'T'HHmmss");
 
   private static final DateFormat isoDateTimeUTCFormat =
       new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
 
   private CalFacadeUtil() {
   }
 
   /** Check for a valid transparency - null is invalid
    *
    * @param val
    * @return boolean true = it's OK
    */
   public static boolean validTransparency(String val) {
     if (val == null) {
       /* We could argue that's valid as the default but I think that leads to
        * problems.
        */
       return false;
     }
 
     if (BwEvent.transparencyOpaque.equals(val)) {
       return true;
     }
 
     return BwEvent.transparencyTransparent.equals(val);
   }
 
   /** Update the to Collection with from elements. This is used to
    * add or remove members from a Collection managed by hibernate for example
    * where a replacement of the Collection is not allowed.
    *
    * @param from
    * @param to
    */
   public static void updateCollection(Collection from, Collection to) {
     Iterator it = from.iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
 
       if (!to.contains(o)) {
         to.add(o);
       }
     }
 
     it = to.iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
 
       if (!from.contains(o)) {
         to.remove(o);
       }
     }
   }
 
   /** Turn Date into "yyyyMMdd"
    *
    * @param val date
    * @return String "yyyyMMdd"
    */
   public static String isoDate(Date val) {
     synchronized (isoDateFormat) {
       return isoDateFormat.format(val);
     }
   }
 
   /** Turn Date into "yyyyMMddTHHmmss"
    *
    * @param val date
    * @return String "yyyyMMddTHHmmss"
    */
   public static String isoDateTime(Date val) {
     synchronized (isoDateTimeFormat) {
       return isoDateTimeFormat.format(val);
     }
   }
 
   /** Turn Date into "yyyyMMddTHHmmssZ"
    *
    * @param val date
    * @return String "yyyyMMddTHHmmssZ"
    */
   public static String isoDateTimeUTC(Date val) {
     synchronized (isoDateTimeUTCFormat) {
       return isoDateTimeUTCFormat.format(val);
     }
   }
 
   /** Get Date from "yyyyMMdd"
    *
    * @param val String "yyyyMMdd"
    * @return Date
    * @throws CalFacadeException
    */
   public static Date fromISODate(String val) throws CalFacadeException {
     try {
       synchronized (isoDateFormat) {
         return isoDateFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new CalFacadeBadDateException();
     }
   }
 
   /** Get Date from "yyyyMMddThhmmss"
    *
    * @param val String "yyyyMMddThhmmss"
    * @return Date
    * @throws CalFacadeException
    */
   public static Date fromISODateTime(String val) throws CalFacadeException {
     try {
       synchronized (isoDateTimeFormat) {
         return isoDateTimeFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new CalFacadeBadDateException();
     }
   }
 
   /** Get Date from "yyyyMMddThhmmssZ"
    *
    * @param val String "yyyyMMddThhmmssZ"
    * @return Date
    * @throws CalFacadeException
    */
   public static Date fromISODateTimeUTC(String val) throws CalFacadeException {
     try {
       synchronized (isoDateTimeUTCFormat) {
         return isoDateTimeUTCFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new CalFacadeBadDateException();
     }
   }
 
   /** Check Date is "yyyyMMdd"
    *
    * @param val String to check
    * @return boolean
    * @throws CalFacadeException
    */
   public static boolean isISODate(String val) throws CalFacadeException {
     try {
       fromISODate(val);
       return true;
     } catch (Throwable t) {
       return false;
     }
   }
 
   /** Check Date is "yyyyMMddThhmmddZ"
    *
    * @param val String to check
    * @return boolean
    * @throws CalFacadeException
    */
   public static boolean isISODateTimeUTC(String val) throws CalFacadeException {
     try {
       fromISODateTimeUTC(val);
       return true;
     } catch (Throwable t) {
       return false;
     }
   }
 
   /** Check Date is "yyyyMMddThhmmdd"
    *
    * @param val String to check
    * @return boolean
    * @throws CalFacadeException
    */
   public static boolean isISODateTime(String val) throws CalFacadeException {
     try {
       fromISODateTime(val);
       return true;
     } catch (Throwable t) {
       return false;
     }
   }
 
   /** Get a java.util.Date object from the value
    * XXX - this will neeed to be supplied with a tz repository
    *
    * @param val
    * @return Date object representing the date
    * @throws CalFacadeException
    */
   public static Date getDate(BwDateTime val) throws CalFacadeException {
     String dtval = val.getDtval();
 
     try {
       if (val.getDateType()) {
         return fromISODate(dtval);
       }
 
       if (dtval.endsWith("Z")) {
         return fromISODateTimeUTC(dtval);
       }
 
       return fromISODateTime(dtval);
     } catch (Throwable t) {
       throw new CalFacadeBadDateException();
     }
   }
 
   /** Get a date object representing the given date and flags
    *
    * @param dt
    * @param dateOnly
    * @param UTC
    * @param timezones
    * @return Date object representing the date
    * @throws CalFacadeException
    */
   public static BwDateTime getDateTime(Date dt, boolean dateOnly, boolean UTC,
                                        CalTimezones timezones) throws CalFacadeException {
     BwDateTime dtm = new BwDateTime();
 
     String date;
 
     if (dateOnly) {
       date = isoDate(dt);
     } else if (UTC) {
       date = isoDateTimeUTC(dt);
     } else {
       date = isoDateTime(dt);
     }
 
     dtm.init(dateOnly, date, timezones.getDefaultTimeZoneId(), timezones);
 
     return dtm;
   }
 
   /** Get a date object representing the given String UTC date/time
    *
    * @param date
    * @return BwDateTime object representing the date
    * @throws CalFacadeException
    */
   public static BwDateTime getDateTimeUTC(String date) throws CalFacadeException {
     BwDateTime dtm = new BwDateTime();
 
     dtm.initUTC(false, date);
 
     return dtm;
   }
 
   /** Get a date object from a string
    *
    * @param val
    * @param timezones
    * @return Date object representing the date
    * @throws CalFacadeException
    */
   public static BwDateTime getDateTime(String val,
                               CalTimezones timezones) throws CalFacadeException {
     Date dt;
     boolean UTC = false;
     boolean dateOnly = false;
 
     try {
       dt = fromISODateTimeUTC(val);
       UTC = true;
     } catch (CalFacadeBadDateException bde1) {
       try {
         dt = fromISODateTime(val);
       } catch (CalFacadeBadDateException bde2) {
         try {
           dt = fromISODate(val);
           dateOnly = true;
         } catch (CalFacadeException ce) {
           throw ce;
         } catch (Throwable t) {
           throw new CalFacadeException(t);
         }
       }
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
 
     return getDateTime(dt, dateOnly, UTC, timezones);
   }
 
   /** Compare two strings. null is less than any non-null string.
    *
    * @param s1       first string.
    * @param s2       second string.
    * @return int     0 if the s1 is equal to s2;
    *                 <0 if s1 is lexicographically less than s2;
    *                 >0 if s1 is lexicographically greater than s2.
    */
   public static int compareStrings(String s1, String s2) {
     if (s1 == null) {
       if (s2 != null) {
         return -1;
       }
 
       return 0;
     }
 
     if (s2 == null) {
       return 1;
     }
 
     return s1.compareTo(s2);
   }
 
   /** Compare two possibly null objects for equality
    *
    * @param thisone
    * @param thatone
    * @return boolean true if both null or equal
    */
   public static boolean eqObjval(Object thisone, Object thatone) {
     if (thisone == null) {
       return thatone == null;
     }
 
     return thisone.equals(thatone);
   }
 
   /** Compare two possibly null objects
    *
    * @param thisone
    * @param thatone
    * @return int -1, 0, 1,
    */
   public static int cmpObjval(Comparable thisone, Comparable thatone) {
     if (thisone == null) {
       if (thatone == null) {
         return 0;
       }
 
       return -1;
     }
 
     if (thatone == null) {
       return 1;
     }
 
     return thisone.compareTo(thatone);
   }
 
   /** Compare two boolean objects
   *
   * @param thisone
   * @param thatone
   * @return int -1, 0, 1,
   */
   public static int cmpBoolval(boolean thisone, boolean thatone) {
     if (thisone == thatone) {
       return 0;
     }
 
     if (!thisone) {
       return -1;
     }
 
     return 1;
   }
 
   /** Compare two int objects
   *
   * @param thisone
   * @param thatone
   * @return int -1, 0, 1,
   */
   public static int cmpIntval(int thisone, int thatone) {
     if (thisone == thatone) {
       return 0;
     }
 
     if (thisone < thatone) {
       return -1;
     }
 
     return 1;
   }
 
   /** Return a String representing the given String array, achieved by
    * URLEncoding the individual String elements then concatenating with
    *intervening blanks.
    *
    * @param  val    String[] value to encode
    * @return String encoded value
    */
   public static String encodeArray(String[] val){
     if (val == null) {
       return null;
     }
 
     int len = val.length;
 
     if (len == 0) {
       return "";
     }
 
     StringBuffer sb = new StringBuffer();
 
     for (int i = 0; i < len; i++) {
       if (i > 0) {
         sb.append(" ");
       }
 
       String s = val[i];
 
       try {
         if (s == null) {
           sb.append("\t");
         } else {
           sb.append(URLEncoder.encode(s, "UTF-8"));
         }
       } catch (Throwable t) {
         throw new RuntimeException(t);
       }
     }
 
     return sb.toString();
   }
 
   /** Return a StringArray resulting from decoding the given String which
    * should have been encoded by encodeArray
    *
    * @param  val      String value encoded by encodeArray
    * @return String[] decoded value
    */
   public static String[] decodeArray(String val){
     if (val == null) {
       return null;
     }
 
     int len = val.length();
 
     if (len == 0) {
       return new String[0];
     }
 
     ArrayList al = new ArrayList();
     int i = 0;
 
     while (i < len) {
       int end = val.indexOf(" ", i);
 
       String s;
       if (end < 0) {
         s = val.substring(i);
         i = len;
       } else {
         s = val.substring(i, end);
         i = end + 1;
       }
 
       try {
         if (s.equals("\t")) {
           al.add(null);
         } else {
           al.add(URLDecoder.decode(s, "UTF-8"));
         }
       } catch (Throwable t) {
         throw new RuntimeException(t);
       }
     }
 
     return (String[])al.toArray(new String[al.size()]);
   }
 
   /** Given a class name return an object of that class.
    * The class parameter is used to check that the
    * named class is an instance of that class.
    *
    * @param className String class name
    * @param cl   Class expected
    * @return     Object checked to be an instance of that class
    * @throws CalFacadeException
    */
   public static Object getObject(String className, Class cl) throws CalFacadeException {
     try {
       Object o = Class.forName(className).newInstance();
 
       if (o == null) {
         throw new CalFacadeException("Class " + className + " not found");
       }
 
       if (!cl.isInstance(o)) {
         throw new CalFacadeException("Class " + className +
                                      " is not a subclass of " +
                                      cl.getName());
       }
 
       return o;
     } catch (CalFacadeException ce) {
       throw ce;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /* ====================================================================
              ical utilities
      ==================================================================== */
 
   /** Add val to prop
    *
    * @param prop
    * @param val
    */
   public static void addIcalParameter(Property prop, Parameter val) {
     ParameterList parl =  prop.getParameters();
 
     parl.add(val);
   }
 
   /** Get named parameter from prop
    *
    * @param prop
    * @param name
    * @return Parameter
    */
   public static Parameter getIcalParameter(Property prop, String name) {
     ParameterList parl =  prop.getParameters();
 
     if (parl == null) {
       return null;
     }
 
     return parl.getParameter(name);
   }
 
   /** Return the timezone id if it is set for the property.
    *
    * @param val
    * @return String tzid or null.
    */
   public static String getTzid(DateProperty val) {
     Parameter tzidPar = getIcalParameter(val, "TZID");
 
     String tzid = null;
     if (tzidPar != null) {
       tzid = tzidPar.getValue();
     }
 
     return tzid;
   }
 
   /** This class defines the entities which occupy time and the period of
    * interest and can be passed repeatedly to getPeriodsEvents.
    *
    * <p>The end datetime will be updated ready for the next call. If endDt is
    * non-null on entry it will be used to set the startDt.
    */
   public static class GetPeriodsPars {
     /** Event Info or EventPeriod or Period objects to extract from */
     public Collection periods;
     /** Start of period - updated at each call from endDt */
     public BwDateTime startDt;
     /** Duration of period */
     public BwDuration dur;
     /** */
     public CalTimezones tzcache;
 
     /** On return has the end date of the period. */
     public BwDateTime endDt;
   }
 
   /** Select the events in the collection which fall within the period
    * defined by the start and duration.
    *
    * @param   pars      GetPeriodsPars object
    * @return  Collection of EventInfo being one days events or empty for no events.
    * @throws CalFacadeException
    */
   public static Collection getPeriodsEvents(GetPeriodsPars pars) throws CalFacadeException {
     ArrayList al = new ArrayList();
     //long millis = System.currentTimeMillis();
 
     if (pars.endDt != null) {
       pars.startDt = pars.endDt.copy(pars.tzcache);
     }
     pars.endDt = pars.startDt.addDuration(pars.dur, pars.tzcache);
     String start = pars.startDt.getDate();
     String end = pars.endDt.getDate();
 
     //if (debug) {
     //  debugMsg("Did UTC stuff in " + (System.currentTimeMillis() - millis));
     //}
 
     EntityRange er = new EntityRange();
     Iterator it = pars.periods.iterator();
     while (it.hasNext()) {
       er.setEntity(it.next());
 
       /* Period is within range if:
              ((evstart < end) and ((evend > start) or
                  ((evstart = evend) and (evend >= start))))
        */
 
       int evstSt = er.start.compareTo(end);
 
       //debugMsg("                   event " + evStart + " to " + evEnd);
 
       if (evstSt < 0) {
         int evendSt = er.end.compareTo(start);
 
         if ((evendSt > 0) ||
             (er.start.equals(er.end) && (evendSt >= 0))) {
           // Passed the tests.
           //if (debug) {
           //  debugMsg("Event passed range " + start + "-" + end +
           //           " with dates " + evStart + "-" + evEnd +
           //           ": " + ev.getSummary());
           //}
           al.add(er.entity);
         }
       }
     }
 
     return al;
   }
 
   /** Turn the int minutes into a 4 digit String hours and minutes value
    *
    * @param minutes  int
    * @return String 4 digit hours + minutes
    */
   public static String getTimeFromMinutes(int minutes) {
     return pad2(minutes / 60) + pad2(minutes % 60);
   }
 
   /** Return String value of par padded to 2 digits.
    * @param val
    * @return String
    */
   public static String pad2(int val) {
     if (val > 9) {
       return String.valueOf(val);
     }
 
     return "0" + String.valueOf(val);
   }
 
   private static class EntityRange {
     Object entity;
 
     String start;
     String end;
 
     void setEntity(Object o) throws CalFacadeException {
       entity = o;
 
       if (o instanceof EventInfo) {
         EventInfo ei = (EventInfo)o;
         BwEvent ev = ei.getEvent();
 
         start = ev.getDtstart().getDate();
        end = ev.getDtend().getDate();
 
         return;
       }
 
       if (o instanceof EventPeriod) {
         EventPeriod ep = (EventPeriod)o;
 
         start = String.valueOf(ep.getStart());
        end = String.valueOf(ep.getEnd());
 
         return;
       }
 
       if (o instanceof Period) {
         Period p = (Period)o;
 
         start = String.valueOf(p.getStart());
        end = String.valueOf(p.getEnd());
 
         return;
       }
 
       start = null;
       end = null;
     }
   }
 
   /**
    *
    */
   public static class EventPeriod implements Comparable {
     private DateTime start;
     private DateTime end;
     private int type;  // from BwFreeBusyComponent
 
     /** Constructor
      *
      * @param start
      * @param end
      * @param type
      */
     public EventPeriod(DateTime start, DateTime end, int type) {
       this.start = start;
       this.end = end;
       this.type = type;
     }
 
     /**
      * @return DateTime
      */
     public DateTime getStart() {
       return start;
     }
 
     /**
      * @return DateTime
      */
     public DateTime getEnd() {
       return end;
     }
 
     /**
      * @return int
      */
     public int getType() {
       return type;
     }
 
     public int compareTo(Object o) {
       if (!(o instanceof EventPeriod)) {
         return -1;
       }
 
       EventPeriod that = (EventPeriod)o;
 
       /* Sort by type first */
       if (type < that.type) {
         return -1;
       }
 
       if (type > that.type) {
         return 1;
       }
 
       int res = start.compareTo(that.start);
       if (res != 0) {
         return res;
       }
 
       return end.compareTo(that.end);
     }
 
     public boolean equals(Object o) {
       return compareTo(o) == 0;
     }
 
     public int hashCode() {
       return 7 * (type + 1) * (start.hashCode() + 1) * (end.hashCode() + 1);
     }
 
     public String toString() {
       StringBuffer sb = new StringBuffer("EventPeriod{start=");
 
       sb.append(start);
       sb.append(", end=");
       sb.append(end);
       sb.append(", type=");
       sb.append(type);
       sb.append("}");
 
       return sb.toString();
     }
   }
 }
