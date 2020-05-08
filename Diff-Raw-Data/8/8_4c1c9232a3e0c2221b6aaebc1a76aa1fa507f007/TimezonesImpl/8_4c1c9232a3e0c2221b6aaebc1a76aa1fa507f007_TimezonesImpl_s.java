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
 package edu.rpi.cmt.timezones;
 
 import edu.rpi.sss.util.DateTimeUtil;
 import edu.rpi.sss.util.DateTimeUtil.BadDateException;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.UnfoldingReader;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.util.TimeZones;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 import ietf.params.xml.ns.timezone_service.ObjectFactory;
 import ietf.params.xml.ns.timezone_service.SummaryType;
 import ietf.params.xml.ns.timezone_service.TimezoneListType;
 
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 
 /** Handle caching, retrieval and registration of timezones. There are possibly
  * two sets of timezones, public or system - shared across a system, and user owned,
  * private to the current user.
  *
  * <p>System timezones are typically initialised once per system while user
  * timezones are initialised once per session.
  *
  * <p>As there is a limited set of timezones (currently around 350) it makes sense
  * to hold all the system timezones in memory. Thus, there will be no updates to
  * the pool of system timezones. Any updates therefore are assumed to be to the
  * set of user timezones.
  *
  * @author Mike Douglass
  *
  */
 public class TimezonesImpl extends Timezones {
   private transient Logger log;
 
   protected boolean debug;
 
   private String serverUrl;
 
   protected String defaultTimeZoneId;
   protected transient TimeZone defaultTimeZone;
 
   /* Map of user TimezoneInfo */
   protected HashMap<String, TimeZone> timezones = new HashMap<String, TimeZone>();
 
   protected static volatile Collection<TimeZoneName> timezoneNames;
 
   /* Cache date only UTC values - we do a lot of those but the number of
    * different dates should be limited.
    *
    * We have one cache per timezone
    */
   private HashMap<String, HashMap<String, String>> dateCaches =
     new HashMap<String, HashMap<String, String>>();
 
   private HashMap<String, String> defaultDateCache = new HashMap<String, String>();
 
   private static Properties aliases;
 
   private long datesCached;
   private long dateCacheHits;
   private long dateCacheMisses;
 
   /**
    *
    */
   public TimezonesImpl() {
   }
 
   @Override
   public void init(final String serverUrl) {
     this.serverUrl = serverUrl;
     debug = getLogger().isDebugEnabled();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getTimeZone(java.lang.String)
    */
   @Override
   public TimeZone getTimeZone(final String id) throws TimezonesException {
     //id = unalias(id);
 
     TimeZone tz = timezones.get(id);
     if (tz != null) {
       return tz;
     }
 
     tz = fetchTimeZone(id);
     register(id, tz);
 
     return tz;
   }
 
   @Override
   public TaggedTimeZone getTimeZone(final String id,
                                     final String etag) throws TimezonesException {
     return fetchTimeZone(id, etag);
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getTimeZoneNames()
    */
   @Override
   public Collection<TimeZoneName> getTimeZoneNames() throws TimezonesException {
     if (timezoneNames != null) {
       return Collections.unmodifiableCollection(timezoneNames);
     }
 
     TzServer server = new TzServer(serverUrl);
 
     try {
       TimezoneListType tzlist = server.getList(null);
 
       Collection<TimeZoneName> ids = new TreeSet<TimeZoneName>();
 
       for (SummaryType s: tzlist.getSummary()) {
         ids.add(new TimeZoneName(s.getTzid()));
       }
 
       timezoneNames = ids;
 
       return Collections.unmodifiableCollection(timezoneNames);
     } finally {
       server.close();
     }
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cmt.timezones.Timezones#getList(java.lang.String)
    */
   @Override
   public TimezoneListType getList(final String changedSince) throws TimezonesException {
     TzServer server = new TzServer(serverUrl);
 
     try {
       return server.getList(changedSince);
     } finally {
       server.close();
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#refreshTimezones()
    */
   @Override
   public synchronized void refreshTimezones() throws TimezonesException {
     timezoneNames = null;
     timezones = new HashMap<String, TimeZone>();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#unalias(java.lang.String)
    */
   @Override
   public String unalias(String tzid) throws TimezonesException {
     /* First transform the name if it follows a known pattern, for example
      * we used to get     /mozilla.org/20070129_1/America/New_York
      */
 
     tzid = transformTzid(tzid);
 
     // Allow chains of aliases
 
     String target = tzid;
 
     if (aliases == null) {
       loadAliases();
     }
 
     for (int i = 0; i < 100; i++) {   // Just in case we get a circular chain
       String unaliased = aliases.getProperty(target);
 
       if (unaliased == null) {
         return target;
       }
 
       if (unaliased.equals(tzid)) {
         break;
       }
 
       target = unaliased;
     }
 
     error("Possible circular alias chain looking for " + tzid);
 
     return null;
   }
 
 //  private static DateFormat formatTd  = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
   private static Calendar cal = Calendar.getInstance();
   private static java.util.TimeZone utctz;
   private static java.util.TimeZone lasttz;
   private static String lasttzid;
   static {
     try {
       utctz = TimeZone.getTimeZone(TimeZones.UTC_ID);
     } catch (Throwable t) {
       throw new RuntimeException("Unable to initialise UTC timezone");
     }
     cal.setTimeZone(utctz);
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#setDefaultTimeZoneId(java.lang.String)
    */
   @Override
   public void setDefaultTimeZoneId(final String id) throws TimezonesException {
     defaultTimeZone = null;
     defaultTimeZoneId = id;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getDefaultTimeZoneId()
    */
   @Override
   public String getDefaultTimeZoneId() throws TimezonesException {
     return defaultTimeZoneId;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getDefaultTimeZone()
    */
   @Override
   public TimeZone getDefaultTimeZone() throws TimezonesException {
     if ((defaultTimeZone == null) && (defaultTimeZoneId != null)) {
       defaultTimeZone = getTimeZone(defaultTimeZoneId);
     }
 
     return defaultTimeZone;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getUtc(java.lang.String, java.lang.String, net.fortuna.ical4j.model.TimeZone)
    */
   @Override
   public synchronized String getUtc(String time, String tzid,
                                     final TimeZone tz) throws TimezonesException {
     try {
       //if (debug) {
       //  trace("Get utc for " + time + " tzid=" + tzid + " tz =" + tz);
       //}
       if (DateTimeUtil.isISODateTimeUTC(time)) {
         // Already UTC
         return time;
       }
 
       String dateKey = null;
       HashMap<String, String> cache = null;
 
       if ((time.length() == 8) && DateTimeUtil.isISODate(time)) {
         /* See if we have it cached */
 
         if (tzid == null) {
           cache = defaultDateCache;
         } else if (tzid.equals(getDefaultTimeZoneId())) {
           cache = defaultDateCache;
         } else {
           cache = dateCaches.get(tzid);
           if (cache == null) {
             cache = new HashMap<String, String>();
             dateCaches.put(tzid, cache);
           }
         }
 
         String utc = cache.get(time);
 
         if (utc != null) {
           dateCacheHits++;
           return utc;
         }
 
         /* Not in the cache - calculate it */
 
         dateCacheMisses++;
         dateKey = time;
         time += "T000000";
       } else if (!DateTimeUtil.isISODateTime(time)) {
         throw new BadDateException(time);
       }
 
       boolean tzchanged = false;
 
       /* If we get a null timezone and id we are being asked for the default.
        * If we get a null tz and the tzid is the default id same again.
        *
        * Otherwise we are asked for something other than the default.
        *
        * So lasttzid is either
        *    1. null - never been called
        *    2. the default tzid
        *    3. Some other tzid.
        */
 
       if (tz == null) {
         if (tzid == null) {
           tzid = getDefaultTimeZoneId();
         }
 
         if ((lasttzid == null) || (!lasttzid.equals(tzid))) {
           if (tzid.equals(getDefaultTimeZoneId())) {
             lasttz = getDefaultTimeZone();
           } else {
             lasttz = getTimeZone(tzid);
           }
 
           if (lasttz == null) {
             lasttzid = null;
             throw new TimezonesException(TimezonesException.unknownTimezone, tzid);
           }
           tzchanged = true;
           lasttzid = tzid;
         }
       } else {
         // tz supplied
         if (tz != lasttz) {
           /* Yes, that's a !=. I'm looking for it being the same object.
            * If I were sure that equals were correct and fast I'd use
            * that.
            */
           tzchanged = true;
           tzid = tz.getID();
           lasttz = tz;
         }
       }
 
 
       if (tzchanged) {
         if (debug) {
           trace("**********tzchanged for tzid " + tzid);
         }
 //XX        formatTd.setTimeZone(lasttz);
         lasttzid = tzid;
       }
       DateFormat formatTd  = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
       formatTd.setTimeZone(lasttz);
 
       java.util.Date date = formatTd.parse(time);
 
       cal.clear();
       cal.setTime(date);
 
       //formatTd.setTimeZone(utctz);
       //trace("formatTd with utc: " + formatTd.format(date));
 
       StringBuilder sb = new StringBuilder();
       digit4(sb, cal.get(Calendar.YEAR));
       digit2(sb, cal.get(Calendar.MONTH) + 1); // Month starts at 0
       digit2(sb, cal.get(Calendar.DAY_OF_MONTH));
       sb.append('T');
       digit2(sb, cal.get(Calendar.HOUR_OF_DAY));
       digit2(sb, cal.get(Calendar.MINUTE));
       digit2(sb, cal.get(Calendar.SECOND));
       sb.append('Z');
 
       String utc = sb.toString();
 
       if (dateKey != null) {
         cache.put(dateKey, utc);
         datesCached++;
       }
 
       return utc;
     } catch (TimezonesException cfe) {
       throw cfe;
     } catch (BadDateException bde) {
       throw new TimezonesException(TimezonesException.badDate, time);
     } catch (Throwable t) {
       //t.printStackTrace();
       throw new TimezonesException(t);
     }
   }
 
   /**
    * @return Number of utc values cached
    */
   @Override
   public long getDatesCached() {
     return datesCached;
   }
 
   /**
    * @return date cache hits
    */
   @Override
   public long getDateCacheHits() {
     return dateCacheHits;
   }
 
   /**
    * @return data cache misses.
    */
   @Override
   public long getDateCacheMisses() {
     return dateCacheMisses;
   }
 
   /* ====================================================================
    *                   Protected methods
    * ==================================================================== */
 
   /** Fetch a timezone object from the server given the id.
    *
    * @param id
    * @return TimeZone with id or null
    * @throws TimezonesException
    */
   protected TimeZone fetchTimeZone(final String id) throws TimezonesException {
     TaggedTimeZone ttz = fetchTimeZone(id, null);
 
     if (ttz == null) {
       return null;
     }
 
     register(id, ttz.tz);
 
     return ttz.tz;
   }
 
   protected TaggedTimeZone fetchTimeZone(final String id,
                                          final String etag) throws TimezonesException {
     TzServer server = new TzServer(serverUrl);
 
     try {
       TaggedTimeZone ttz = server.getTz(id, etag);
 
       if (ttz == null) {
         return null;
       }
 
       CalendarBuilder cb = new CalendarBuilder();
 
       UnfoldingReader ufrdr = new UnfoldingReader(new StringReader(ttz.vtz),
                                                   true);
 
       net.fortuna.ical4j.model.Calendar cal = cb.build(ufrdr);
       VTimeZone vtz = (VTimeZone)cal.getComponents().getComponent(Component.VTIMEZONE);
       if (vtz == null) {
         throw new TimezonesException("Incorrectly stored timezone");
       }
 
       TimeZone tz = new TimeZone(vtz);
 
       ttz.tz = tz;
 
       return ttz;
     } catch (Throwable t) {
       throw new TimezonesException(t);
     } finally {
       server.close();
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#register(java.lang.String, net.fortuna.ical4j.model.TimeZone)
    */
   @Override
   public synchronized void register(final String id,
                                                final TimeZone timezone)
           throws TimezonesException {
     timezones.put(id, timezone);
   }
 
   /* ====================================================================
    *                   private methods
    * ==================================================================== */
 
   private static String transformTzid(final String tzid) {
     int len = tzid.length();
 
     if ((len > 13) && (tzid.startsWith("/mozilla.org/"))) {
       int pos = tzid.indexOf('/', 13);
 
       if ((pos < 0) || (pos == (len - 1))) {
         return tzid;
       }
       return tzid.substring(pos + 1);
     }
 
     /* Special to get James Andrewartha going */
     String ss = "/softwarestudio.org/Tzfile/";
 
     if ((len > ss.length()) &&
         (tzid.startsWith(ss))) {
       return tzid.substring(ss.length());
     }
 
     return tzid;
   }
 
   private void loadAliases() throws TimezonesException {
     TzServer server = new TzServer(serverUrl);
     InputStream is = null;
 
     try {
       Properties a = new Properties();
 
       is = server.getAliases();
       a.load(is);
 
       aliases = a;
     } catch (Throwable t) {
       error("loadTimezones error: " + t.getMessage());
       t.printStackTrace();
     } finally {
       if (is != null) {
         try {
           is.close();
         } catch (Throwable t1) {}
       }
 
       server.close();
     }
   }
 
   /** CLass to allow us to call the server
    */
   private static class TzServer {
     private static String tzserverUri;
 
     private static JAXBContext jc;
 
     private HttpGet getter;
     int status;
     HttpResponse response;
 
     TzServer(final String uri) {
       tzserverUri = uri;
     }
 
     public TaggedTimeZone getTz(final String id,
                                 final String etag) throws TimezonesException {
       try {
        doCall("action=get&tzid=" + id, etag);
 
         int status = response.getStatusLine().getStatusCode();
 
         if (status == HttpServletResponse.SC_NO_CONTENT) {
           return new TaggedTimeZone(etag);
         }
 
         if (status != HttpServletResponse.SC_OK) {
           return null;
         }
 
         return new TaggedTimeZone(response.getFirstHeader("Etag").getValue(),
                                   EntityUtils.toString(response.getEntity()));
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
     /* Not used - remove from server
     public String getNames() throws TimezonesException {
       return call("names");
     }*/
 
     public TimezoneListType getList(final String changedSince) throws TimezonesException {
       String req = "action=list";
 
       if (changedSince != null) {
         req = req + "&changedsince=" + changedSince;
       }
 
       JAXBElement jel = getXml(req);
 
       if (jel == null) {
         return null;
       }
 
       return (TimezoneListType)jel.getValue();
     }
 
     public InputStream getAliases() throws TimezonesException {
       return callForStream("aliases");
     }
 
     public JAXBElement getXml(final String req) throws TimezonesException {
       InputStream is = null;
       try {
         is = callForStream(req);
 
         if ((is == null) || (status != HttpServletResponse.SC_OK)) {
           return null;
         }
 
         if (jc == null) {
           synchronized (this) {
             if (jc == null) {
               jc = JAXBContext.newInstance(ObjectFactory.class);
             }
           }
         }
 
         Unmarshaller u = jc.createUnmarshaller();
 
         JAXBElement jel = (JAXBElement)u.unmarshal(is);
         return jel;
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       } finally {
         if (is != null) {
           try {
             is.close();
           } catch (Throwable t) {}
         }
       }
     }
 
     public InputStream callForStream(final String req) throws TimezonesException {
       try {
         doCall(req, null);
 
         if (status != HttpServletResponse.SC_OK) {
           return null;
         }
 
         HttpEntity ent = response.getEntity();
 
         return ent.getContent();
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
     public void close() throws TimezonesException {
       try {
         if (response == null) {
           return;
         }
 
         HttpEntity ent = response.getEntity();
 
         if (ent != null) {
           InputStream is = ent.getContent();
           is.close();
         }
 
         getter = null;
         response = null;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
     private void doCall(final String req,
                         final String etag) throws TimezonesException {
       try {
         if (tzserverUri == null) {
           throw new TimezonesException("No timezones server URI defined");
         }
 
         HttpClient client = new DefaultHttpClient();
 
        getter = new HttpGet(URLEncoder.encode(tzserverUri + "?" + req,
                                               HTTP.DEFAULT_CONTENT_CHARSET));
 
         if (etag != null) {
           getter.addHeader(new BasicHeader("If-None-Match", etag));
         }
 
         response = client.execute(getter);
         status = response.getStatusLine().getStatusCode();
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (UnknownHostException uhe) {
         throw new TzUnknownHostException(tzserverUri);
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
   }
 
   private void digit2(final StringBuilder sb, final int val) throws BadDateException {
     if (val > 99) {
       throw new BadDateException();
     }
     if (val < 10) {
       sb.append("0");
     }
     sb.append(val);
   }
 
   private void digit4(final StringBuilder sb, final int val) throws BadDateException {
     if (val > 9999) {
       throw new BadDateException();
     }
     if (val < 10) {
       sb.append("000");
     } else if (val < 100) {
       sb.append("00");
     } else if (val < 1000) {
       sb.append("0");
     }
     sb.append(val);
   }
 
   /* Get a logger for messages
    */
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void error(final String msg) {
     getLogger().error(msg);
   }
 
   private void trace(final String msg) {
     getLogger().debug(msg);
   }
 }
