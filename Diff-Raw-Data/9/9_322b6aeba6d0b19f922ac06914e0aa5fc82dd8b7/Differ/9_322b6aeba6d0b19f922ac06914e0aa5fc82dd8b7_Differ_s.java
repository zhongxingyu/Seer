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
 package org.bedework.timezones.common;
 
 import net.fortuna.ical4j.model.TimeZone;
 
 import org.apache.log4j.Logger;
 import org.oasis_open.docs.ns.wscal.calws_soap.ComponentSelectionType;
 
 import ietf.params.xml.ns.icalendar_2.IcalendarType;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import edu.rpi.cmt.calendar.diff.XmlIcalCompare;
 
 /** This class provides support for diffing timezone data to determine if
  * updates need to be made to stored data.
  *
  * @author douglm
  */
 public class Differ {
   private boolean debug;
 
   private transient Logger log;
 
   /**
    */
   public static class DiffListEntry {
     /** Tzid changed
      */
     public String tzid;
 
     /** If not add or delete then update.
      */
     public boolean add;
 
     /**  We should never delete even though we check for it here */
     public boolean deleted;
 
     /** The spec
      */
     public TimeZone tzSpec;
 
     /** The XML spec
      */
     public IcalendarType xcal;
 
     /** True if only aliases changed
      */
     public boolean aliasChangeOnly;
     /**
      */
     public SortedSet<String> aliases = new TreeSet<String>();
 
     /** Add our stuff to the StringBuilder
      *
      * @param sb    StringBuilder for result
      */
     protected void toStringSegment(final StringBuilder sb,
                                    final boolean full,
                                    final String indent) {
       sb.append("tzid = ");
       sb.append(tzid);
 
       if (add) {
         sb.append(", add");
       } else if (deleted) {
         sb.append(", deleted");
       } else {
         sb.append(", update");
       }
 
       if (aliasChangeOnly) {
         sb.append(", aliasChangeOnly");
       }
     }
 
     /**
      * @return short form.
      */
     public String toShortString() {
       StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("{");
 
       toStringSegment(sb, false, "  ");
 
       sb.append("}");
       return sb.toString();
     }
   }
 
   /**
    */
   public Differ() {
     debug = getLogger().isDebugEnabled();
   }
 
   /** Compares the zipped data specified by the url with the supplied set
    * of data.
    *
    * @param newTzdataUrl
    * @param currentTzdata
    * @return possibly empty list - never null.
    * @throws TzException
    */
   public List<DiffListEntry> compare(final String newTzdataUrl,
                                      final CachedData currentTzdata) throws TzException {
     return compare(new ZipCachedData(newTzdataUrl), currentTzdata);
   }
 
   /** Compares the new set of data with the supplied current set of data.
    *
    * @param newTzdata
    * @param currentTzdata
    * @return possibly empty list - never null.
    * @throws TzException
    */
   public List<DiffListEntry> compare(final CachedData newTzdata,
                                      final CachedData currentTzdata) throws TzException {
     List<DiffListEntry> res = new ArrayList<DiffListEntry>();
 
     SortedSet<String> newNames = newTzdata.getNameList();
 
     NameChanges nc = getNameChanges(newNames, currentTzdata.getNameList());
 
     if (!nc.deletedNames.isEmpty()) {
       warn("Following ids appear to have been deleted");
       for (String id: nc.deletedNames) {
         warn("   " + id);
       }
     }
 
     if (debug) {
       trace("Following ids appear to have been added");
       for (String id: nc.addedNames) {
         trace("   " + id);
       }
     }
 
     /* Get each timezone that exists in new and current and compare to see if
      * it's changed.
      */
 
    XmlIcalCompare comp = new XmlIcalCompare(XmlIcalCompare.defaultSkipList);
 
     for (String tzid: newNames) {
       if (nc.addedNames.contains(tzid)) {
         DiffListEntry dle = new DiffListEntry();
 
         dle.tzid = tzid;
         dle.add = true;
         dle.tzSpec = newTzdata.getTimeZone(tzid);
         dle.aliases = newTzdata.findAliases(tzid);
 
         res.add(dle);
         continue;
       }
 
       if (nc.deletedNames.contains(tzid)) {
         DiffListEntry dle = new DiffListEntry();
 
         dle.tzid = tzid;
         dle.deleted = true;
         dle.tzSpec = currentTzdata.getTimeZone(tzid);
         dle.aliases = currentTzdata.findAliases(tzid);
 
         res.add(dle);
         continue;
       }
 
       /* compare */
 
       IcalendarType newXcal = newTzdata.getXTimeZone(tzid);
       IcalendarType currentXcal = currentTzdata.getXTimeZone(tzid);
 
       ComponentSelectionType cst = comp.diff(newXcal, currentXcal);
 
       if (cst == null) {
         continue;
       }
 
       if (debug) {
         trace("Adding " + tzid);
       }
 
       DiffListEntry dle = new DiffListEntry();
 
       dle.tzid = tzid;
       dle.tzSpec = newTzdata.getTimeZone(tzid);
       dle.aliases = newTzdata.findAliases(tzid);
       dle.xcal = newXcal;
 
       res.add(dle);
     }
 
     return res;
   }
 
   private static class NameChanges {
     SortedSet<String> addedNames = new TreeSet<String>();
     SortedSet<String> deletedNames = new TreeSet<String>();
   }
 
   private NameChanges getNameChanges(final SortedSet<String> newList,
                                      final SortedSet<String> currentList) {
     NameChanges nc = new NameChanges();
 
     Iterator<String> nit = newList.iterator();
     Iterator<String> cit = currentList.iterator();
     String nid = nit.next();
     String cid = cit.next();
 
     while ((nid != null) || (cid != null)) {
       boolean advanceNew = false;
       boolean advanceCur = false;
 
       test: {
         if (cid == null) {
           nc.addedNames.add(nid);
           advanceNew = true;
           break test;
         }
 
         if (nid == null) {
           nc.deletedNames.add(cid);
           advanceCur = true;
           break test;
         }
 
         int cmp = nid.compareTo(cid);
 
         if (cmp == 0) {
           advanceNew = true;
           advanceCur = true;
 
           break test;
         }
 
         if (cmp < 0) {
           nc.addedNames.add(nid);
           advanceNew = true;
 
           break test;
         }
 
         nc.deletedNames.add(cid);
         advanceCur = true;
       } // test
 
       if (advanceCur) {
         if (cit.hasNext()) {
           cid = cit.next();
         } else {
           cid = null;
         }
       }
 
       if (advanceNew) {
         if (nit.hasNext()) {
           nid = nit.next();
         } else {
           nid = null;
         }
       }
     }
 
     return nc;
   }
 
   /**
    * @return Logger
    */
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   /**
    * @param t
    */
   protected void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
   /**
    * @param msg
    */
   protected void error(final String msg) {
     getLogger().error(msg);
   }
 
   /**
    * @param msg
    */
   protected void warn(final String msg) {
     getLogger().warn(msg);
   }
 
   /**
    * @param msg
    */
   protected void info(final String msg) {
     getLogger().info(msg);
   }
 
   /**
    * @param msg
    */
   protected void trace(final String msg) {
     getLogger().debug(msg);
   }
 }
