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
 
 package org.bedework.icalendar;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.CalFacadeException;
 import org.bedework.calfacade.svc.EventInfo;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.CalendarOutputter;
 import net.fortuna.ical4j.data.CalendarParserImpl;
 import net.fortuna.ical4j.data.ParserException;
 //import net.fortuna.ical4j.data.UnfoldingReader;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.ComponentList;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.property.ProdId;
 import net.fortuna.ical4j.model.property.TzId;
 import net.fortuna.ical4j.model.property.Version;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 /** Object to provide translation between an EventVO and an Icalendar format.
  *
  * NOTES: Check out how we deal with all the possible combinations of dtstart,
  * dtend and durations. We probably need to redo date/time stuff in the back end
  * to preserve what was intended. We shouldn't send a different form of an event
  * back if possible.
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public class IcalTranslator implements Serializable {
   protected boolean debug;
 
   /** We'll use this to parameterize some of the behaviour
    */
   public static class Pars {
     /** Support simple location only. Many iCalendar-aware
      * applications only support a simple string valued location.
      *
      * <p>If this value is true we only pass the address part of the location
      * and provide an altrep which will allow (some) clients to retrieve the
      * full form of a location.
      */
     public boolean simpleLocation = true;
 
     /** Support simple contacts only.
      */
     public boolean simpleContact = true;
   }
 
   /* This needs to come from a property updated with each release.
    */
   protected static final String prodId = "BedeWork V3.0";
 
   protected transient Logger log;
 
   protected IcalCallback cb;
 
   protected Pars pars = new Pars();
 
   /** Constructor:
    *
    * @param cb     IcalCallback object for retrieval of entities
    */
   public IcalTranslator(IcalCallback cb) {
     this(cb, false);
   }
 
   /** Constructor:
    *
    * @param cb     IcalCallback object for retrieval of entities
    * @param debug
    */
   public IcalTranslator(IcalCallback cb, boolean debug) {
     this.cb = cb;
     this.debug = debug;
   }
 
   /* ====================================================================
    *                     Translation methods
    * ==================================================================== */
 
   /** Make a new Calendar with default properties
    *
    * @return Calendar
    * @throws CalFacadeException
    */
   public Calendar newIcal() throws CalFacadeException {
     Calendar cal = new Calendar();
 
     cal.getProperties().add(new ProdId(prodId));
     cal.getProperties().add(Version.VERSION_2_0);
 
     return cal;
   }
 
   /** turn a single event into a calendar
    *
    * @param val
    * @return Calendar
    * @throws CalFacadeException
    */
   public Calendar toIcal(BwEvent val) throws CalFacadeException {
     if (val == null) {
       return null;
     }
     
     HashMap added = new HashMap();
 
     try {
       Calendar cal = newIcal();
 
       /* Add referenced timezones to the calendar */
       addIcalTimezones(cal, val, added);
 
       /* Add it to the calendar */
       cal.getComponents().add(toIcalEvent(val));
 
       return cal;
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** turn a collection of events into a calendar
    *
    * @param vals
    * @return Calendar
    * @throws CalFacadeException
    */
   public Calendar toIcal(Collection vals) throws CalFacadeException {
     if ((vals == null) || (vals.size() == 0)) {
       return null;
     }
     
     HashMap added = new HashMap();
 
     try {
       Calendar cal = newIcal();
 
       Iterator it = vals.iterator();
       while (it.hasNext()) {
         BwEvent val = (BwEvent)it.next();
 
         /* Add referenced timezones to the calendar */
         addIcalTimezones(cal, val, added);
 
         /* Add it to the calendar */
         cal.getComponents().add(toIcalEvent(val));
       }
 
       return cal;
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** Make a VEvent object from an EventVO.
    *
    * @param val
    * @return VEvent
    * @throws CalFacadeException
    */
   public VEvent toIcalEvent(BwEvent val) throws CalFacadeException {
     return VEventUtil.toIcalEvent(val, cb.getURIgen());
   }
 
   /** Convert the EventVO object to a String representation
    *
    * @param val
    * @return String
    * @throws CalFacadeException
    */
   public String toStringIcal(BwEvent val) throws CalFacadeException {
     Calendar ical = toIcal(val);
 
     CalendarOutputter calOut = new CalendarOutputter(true);
 
     StringWriter sw = new StringWriter();
 
     try {
       calOut.output(ical, sw);
 
       return sw.toString();
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** Convert the given string representation of an Icalendar object to an EventVO
    *
    * <p>Because an icalendar object can contain 0 or more VEvents we return
    * a collection of events which may be empty.
    *
    * @param cal       calendar
    * @param val
    * @return Collection
    * @throws CalFacadeException
    */
   public Collection fromIcal(BwCalendar cal, String val) throws CalFacadeException {
     try {
       CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
 
       //return fromIcal(cal, bldr.build(new UnfoldingReader(new StringReader(val))));
       return fromIcal(cal, bldr.build(new StringReader(val), true));
     } catch (ParserException pe) {
       if (debug) {
         error(pe);
       }
       throw new IcalMalformedException(pe.getMessage());
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** Convert the Icalendar reader to a Collection of Calendar objects
    *
    * @param cal       calendar
    * @param rdr
    * @return Collection
    * @throws CalFacadeException
    */
   public Collection fromIcal(BwCalendar cal, Reader rdr) throws CalFacadeException {
     try {
       //System.setProperty("ical4j.unfolding.relaxed", "true");
       CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
 
       //return fromIcal(cal, bldr.build(new UnfoldingReader(rdr)));
       return fromIcal(cal, bldr.build(rdr, true));
     } catch (ParserException pe) {
       if (debug) {
         error(pe);
       }
       throw new IcalMalformedException(pe.getMessage());
     } catch (CalFacadeException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** Convert the Calendar to a Collection of calendar objects.
    *
    * @param cal       calendar
    * @param val
    * @return Collection
    * @throws CalFacadeException
    */
   public Collection fromIcal(BwCalendar cal, Calendar val) throws CalFacadeException {
     Vector objs = new Vector();
 
     if (val == null) {
       return objs;
     }
 
     Collection clist = orderedComponents(val.getComponents());
 
     Iterator it = clist.iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof VEvent) {
         EventInfo ev = BwEventUtil.toEvent(cb, cal, objs, (VEvent)o, debug);
 
         if (ev != null) {
           objs.add(ev);
         }
       } else if (o instanceof VTimeZone) {
         doTimeZone((VTimeZone)o);
       }
     }
 
     return objs;
   }
 
   /** Convert the given string representation of an Icalendar object to a
    * Calendar object
    *
    * @param val
    * @return Calendar
    * @throws CalFacadeException
    */
   public static Calendar getCalendar(String val) throws CalFacadeException {
     try {
       CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
 
      return bldr.build(new StringReader(val));
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** Convert the given string representation of an Icalendar object to a
    * collection of VEvent
    *
    * <p>Because an icalendar object can contain 0 or more VEvents we return
    * a collection of vevents which may be empty.
    *
    * @param val
    * @return Collection
    * @throws CalFacadeException
    */
   public Collection toVEvent(String val) throws CalFacadeException {
     try {
       CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
 
      Calendar cal = bldr.build(new StringReader(val));
       Vector evs = new Vector();
 
       if (cal == null) {
         return evs;
       }
 
       ComponentList clist = cal.getComponents();
 
       Iterator it = clist.iterator();
 
       while (it.hasNext()) {
         Object o = it.next();
 
         if (o instanceof VEvent) {
           evs.add(o);
         }
       }
 
       return evs;
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /* ====================================================================
                       Private methods
      ==================================================================== */
 
   /* Order all the components so that they are in the following order:
    *
    * XXX should use a TreeSet with a wrapper round the object and sort them including
    * dates etc.
    *
    * 1. Timezones
    * 2. Events without recurrence id
    * 3. Events with recurrence id
    * 4. Anything else
    */
   private static Collection orderedComponents(ComponentList clist) {
     SubList tzs = new SubList();
     SubList evs = new SubList();
     SubList evsWithrid = new SubList();
     SubList theRest = new SubList();
 
     Iterator it = clist.iterator();
 
     while (it.hasNext()) {
       Component c = (Component)it.next();
 
       if (c instanceof VEvent) {
         if (IcalUtil.getProperty(c, Property.RECURRENCE_ID) != null) {
           evsWithrid.add(c);
         } else {
           evs.add(c);
         }
       } else if (c instanceof VTimeZone) {
         tzs.add(c);
       } else {
         theRest.add(c);
       }
     }
 
     Vector all = new Vector();
 
     tzs.appendTo(all);
     evs.appendTo(all);
     evsWithrid.appendTo(all);
     theRest.appendTo(all);
 
     return all;
   }
 
   private static class SubList {
     Vector list;
 
     void add(Object o) {
       if (list == null) {
         list = new Vector();
       }
       list.add(o);
     }
 
     void appendTo(Collection c) {
       if (list != null) {
         c.addAll(list);
       }
     }
   }
 
   private void doTimeZone(VTimeZone vtz) throws CalFacadeException {
     TzId tzid = (TzId)IcalUtil.getProperty(vtz, Property.TZID);
 
     if (tzid == null) {
       throw new CalFacadeException("Missing tzid property");
     }
 
     String id = tzid.getValue();
 
     if (debug) {
       debugMsg("Got timezone: \n" + vtz.toString() + " with id " + id);
     }
 
     if (cb.findTimeZone(id, null) != null) {
       if (debug) {
         debugMsg("Timezone already in db");
       }
       return; // We know this one
     }
 
     cb.saveTimeZone(tzid.getValue(), vtz);
   }
 
   /* If the start or end date references a timezone, we retrieve the timezone definition
    * and add it to the calendar.
    */
   private void addIcalTimezones(Calendar cal, BwEvent ev, 
                                 HashMap added) throws CalFacadeException {
     BwUser owner = ev.getOwner();
 
     addIcalTimezone(cal, ev.getDtstart().getTzid(), owner, added);
 
     if (ev.getEndType() == BwEvent.endTypeDate) {
       addIcalTimezone(cal, ev.getDtend().getTzid(), owner, added);
     }
   }
   
   private void addIcalTimezone(Calendar cal, String tzid, 
                                BwUser owner, 
                                HashMap added) throws CalFacadeException {
     VTimeZone vtz;
     
     if ((tzid == null) || added.containsKey(tzid)) {
       return;
     }
 
     if (debug) {
       debugMsg("Look for timezone with id " + tzid);
     }
     
     vtz = cb.findTimeZone(tzid, owner);
     if (vtz != null) {
       if (debug) {
         debugMsg("found timezone with id " + tzid);
       }
       cal.getComponents().add(vtz);
     } else if (debug) {
       debugMsg("Didn't find timezone with id " + tzid);
     }
     
     added.put(tzid, null);
   }
 
   private Logger getLog() {
     if (log != null) {
       return log;
     }
 
     log = Logger.getLogger(this.getClass());
     return log;
   }
 
   private void error(Throwable t) {
     getLog().error(this, t);
   }
 
   //private void warn(String msg) {
   //  getLog().warn(msg);
   //}
 
   private void debugMsg(String msg) {
     getLog().debug(msg);
   }
 }
 
