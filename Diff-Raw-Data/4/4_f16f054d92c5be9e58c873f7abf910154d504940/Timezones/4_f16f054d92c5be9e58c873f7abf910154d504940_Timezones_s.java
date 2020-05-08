 /* **********************************************************************
     Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 package edu.rpi.cmt.timezones;
 
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.TimeZoneRegistry;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 /** Handle caching, retrieval and registration of timezones. Timezones are
  * generally retrieved from some outside source, that is a timezone service.
  *
  * @author Mike Douglass
  *
  */
 public abstract class Timezones implements Serializable {
   /** Identify our exceptions.
    * @author douglm
    *
    */
   public static class TimezonesException extends Throwable {
     /** */
     public static String unknownTimezone = "edu.rpi.cmt.timezones.exc.unknownTimezone";
 
     /** */
     public static String badDate = "edu.rpi.cmt.timezones.exc.baddate";
 
     private String extra;
 
     /**
      */
     public TimezonesException() {
       super();
     }
 
     /**
      * @param t
      */
     public TimezonesException(final Throwable t) {
       super(t);
     }
 
     /**
      * @param msg
      */
     public TimezonesException(final String msg) {
       super(msg);
     }
 
     /**
      * @param msg
      * @param extra
      */
     public TimezonesException(final String msg, final String extra) {
       super(msg);
       this.extra = extra;
     }
 
     /**
      * @return String
      */
     public String getExtra() {
       return extra;
     }
 
     @Override
     public String toString() {
       StringBuilder sb = new StringBuilder(super.toString());
       if (extra != null) {
         sb.append(": ");
         sb.append(extra);
       }
 
       return sb.toString();
     }
   }
 
   private static ThreadLocal<String> threadTzid =
     new ThreadLocal<String>();
 
   private static Timezones tzs;
 
   private static class TzRegistry implements TimeZoneRegistry {
     public void register(final TimeZone timezone) {
       // Do nothing
     }
 
     public void clear() {
       // Do nothing
     }
 
     public TimeZone getTimeZone(final String id) {
       try {
         return Timezones.getTimezones().getTimeZone(id);
       } catch (Throwable t) {
         throw new RuntimeException(t);
       }
     }
   }
 
   private static TimeZoneRegistry tzRegistry = new TzRegistry();
 
   /**
    * @return a registry for fetching timezones.
    */
   public static TimeZoneRegistry getTzRegistry() {
     return tzRegistry;
   }
 
   /** Printable (language specific) name + internal id
    */
   public static class TimeZoneName implements Comparable<TimeZoneName>, Serializable {
     /** Name for timezone */
     public String name;
     /** Id for timezone */
     public String id;
 
     /**
      * @param name
      */
     public TimeZoneName(final String name) {
       this.name = name;
       id = name;
     }
 
     /**
      * @return tz name
      */
     public String getName() {
       return name;
     }
 
     /**
      * @return tz id
      */
     public String getId() {
       return id;
     }
 
     public int compareTo(final TimeZoneName that) {
       if (that == this) {
         return 0;
       }
 
       return name.compareTo(that.name);
     }
 
     @Override
     public boolean equals(final Object o) {
       return compareTo((TimeZoneName)o) == 0;
     }
 
     @Override
     public int hashCode() {
       return name.hashCode();
     }
   }
   /* ====================================================================
    *                   Static methods
    * ==================================================================== */
 
   /** Initialize the timezones system.
    *
    * @param serverUrl
    * @throws TimezonesException
    */
   public static void initTimezones(final String serverUrl) throws TimezonesException {
     try {
       if (tzs == null) {
         tzs = (Timezones)Class.forName("edu.rpi.cmt.timezones.TimezonesImpl").newInstance();
       }
 
       tzs.init(serverUrl);
     } catch (Throwable t) {
       throw new TimezonesException(t);
     }
   }
 
   /**
    * @return a Timezones object
    */
   public static Timezones getTimezones() {
     if (tzs == null) {
       throw new RuntimeException("Timezones not initialized");
     }
 
     return tzs;
   }
 
   /** Set the default timezone id for this system.
    *
    * @param id
    * @throws TimezonesException
    */
   public static void setSystemDefaultTzid(final String id) throws TimezonesException {
     getTimezones().setDefaultTimeZoneId(id);
   }
 
   /** Get the default timezone id for this system.
    *
    * @return String id
    * @throws TimezonesException
    */
   public static String getSystemDefaultTzid() throws TimezonesException {
     return getTimezones().getDefaultTimeZoneId();
   }
 
   /** Set the default timezone id for this thread.
    *
    * @param id
    * @throws TimezonesException
    */
   public static void setThreadDefaultTzid(final String id) throws TimezonesException {
     threadTzid.set(id);
   }
 
   /** Get the default timezone id for this thread.
    *
    * @return String id
    * @throws TimezonesException
    */
   public static String getThreadDefaultTzid() throws TimezonesException {
     String id = threadTzid.get();
 
     if (id != null) {
       return id;
     }
     return getSystemDefaultTzid();
   }
 
   /** Get the default timezone for this system.
    *
    * @return default TimeZone or null for none set.
    * @throws TimezonesException
    */
   public static TimeZone getDefaultTz() throws TimezonesException {
     return getTz(getThreadDefaultTzid());
   }
 
   /** Get a timezone object given the id. This will return transient objects
    * registered in the timezone directory
    *
    * @param id
    * @return TimeZone with id or null
    * @throws TimezonesException
    */
    public static TimeZone getTz(final String id) throws TimezonesException {
      return getTimezones().getTimeZone(id);
    }
 
   /** Given a String time value and a possibly null tzid,
    *  will return a UTC formatted value. The supplied time should be of the
    *  form yyyyMMdd or yyyyMMddThhmmss or yyyyMMddThhmmssZ
    *
    *  <p>The last form will be returned untouched, it's already UTC.
    *
    *  <p>the first will have T000000 appended to the parameter value then the
    *  first and second will be converted to the equivalent UTC time.
    *
    *  <p>The returned value is used internally as a value for indexes and
    *  recurrence ids.
    *
    *  <p>tz null mean this is local or floating time
    *
    * @param time  String time to convert.
    * @param tzid  String tzid.
    * @return String always of form yyyyMMddThhmmssZ
    * @throws TimezonesException for bad parameters or timezone
    */
   public static String getUtc(final String time,
                               final String tzid) throws TimezonesException {
     return getTimezones().getUtc(time, tzid, null);
   }
 
   /** Register a timezone object in the current session.
    *
    * @param id
    * @param timezone
    * @throws TimezonesException
    */
   public static void registerTz(final String id, final TimeZone timezone)
           throws TimezonesException {
     /* We don't allow ical to change our timezones. */
     //getTimezones().registerTimeZone(id, timezone);
   }
 
   /** Get an unmodifiable list of timezone names
    *
    * @return Collection of timezone names
    * @throws TimezonesException
    */
   public static Collection<TimeZoneName> getTzNames() throws TimezonesException {
     return getTimezones().getTimeZoneNames();
   }
 
   /** Refresh the timezone table - presumably after a call to clearPublicTimezones.
    * and many calls to saveTimeZone.
    *
    * @throws TimezonesException
    */
   public static void refreshTzs() throws TimezonesException {
     getTimezones().refreshTimezones();
   }
 
   /* ====================================================================
    *                   Abstract methods
    * ==================================================================== */
 
   /** Initialise the object supplying the url of the timezones server.
    *
    * @param serverUrl
    */
   public abstract void init(String serverUrl);
 
   /** Get a timezone object given the id. This method will attempt to retrieve
    * a cached timezone and if that fails a will try to fetch the tz from the
    * store by calling fetchTimeZone.
    *
    * @param id
    * @return TimeZone with id or null
    * @throws TimezonesException
    */
   public abstract TimeZone getTimeZone(String id) throws TimezonesException;
 
   /** Get an unmodifiable list of timezone names
    *
    * @return Collection of timezone names
    * @throws TimezonesException
    */
   public abstract Collection<TimeZoneName> getTimeZoneNames()
       throws TimezonesException;
 
   /** Refresh the timezone table - usually after timezones have changed..
    *
    * @throws TimezonesException
    */
   public abstract void refreshTimezones() throws TimezonesException;
 
   /**
    * @param tzid
    * @return String
    * @throws TimezonesException
    */
   public abstract String unalias(String tzid) throws TimezonesException;
 
   /**
    * @param id
    * @throws TimezonesException
    */
   public abstract void setDefaultTimeZoneId(String id) throws TimezonesException;
 
   /** Get the default timezone id for this system.
    *
    * @return String id
    * @throws TimezonesException
    */
   public abstract String getDefaultTimeZoneId() throws TimezonesException;
 
   /** Get the default timezone for this system.
    *
    * @return default TimeZone or null for none set.
    * @throws TimezonesException
    */
   public abstract TimeZone getDefaultTimeZone() throws TimezonesException;
 
   /** Register a timezone object in the current session.
    *
    * @param id
    * @param timezone
    * @throws TimezonesException
    */
   public abstract void register(String id,
                                 TimeZone timezone)
            throws TimezonesException;
 
   /** Given a String time value and a possibly null tzid and/or timezone
    *  will return a UTC formatted value. The supplied time should be of the
    *  form yyyyMMdd or yyyyMMddThhmmss or yyyyMMddThhmmssZ
    *
    *  <p>The last form will be returned untouched, it's already UTC.
    *
    *  <p>the first will have T000000 appended to the parameter value then the
    *  first and second will be converted to the equivalent UTC time.
    *
    *  <p>The returned value is used internally as a value for indexes and
    *  recurrence ids.
    *
    *  <p>Both tzid and tz null mean this is local or floating time
    *
    * @param time  String time to convert.
    * @param tzid  String tzid.
    * @param tz    If set used in preference to tzid.
    * @return String always of form yyyyMMddThhmmssZ
    * @throws TimezonesException for bad parameters or timezone
    */
   public abstract String getUtc(String time, String tzid,
                                 TimeZone tz) throws TimezonesException;
 
   /**
    * @return Number of utc values cached
    */
   public abstract long getDatesCached();
 
   /**
    * @return date cache hits
    */
   public abstract long getDateCacheHits();
 
   /**
    * @return data cache misses.
    */
   public abstract long getDateCacheMisses();
 }
