 /*
  * Copyright 2005 Open Source Applications Foundation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.fortuna.ical4j.model;
 
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 
 import net.fortuna.ical4j.model.parameter.Range;
 import net.fortuna.ical4j.model.parameter.Value;
 import net.fortuna.ical4j.model.property.DtEnd;
 import net.fortuna.ical4j.model.property.DtStart;
 import net.fortuna.ical4j.model.property.Duration;
 import net.fortuna.ical4j.model.property.ExDate;
 import net.fortuna.ical4j.model.property.ExRule;
 import net.fortuna.ical4j.model.property.RDate;
 import net.fortuna.ical4j.model.property.RRule;
 import net.fortuna.ical4j.model.property.RecurrenceId;
 import net.fortuna.ical4j.util.Dates;
 
 /**
  * @author cyrusdaboo
  * 
  * A list of instances . Instances are created by adding a component, either the
  * master recurrence component or an overridden instance of one. Its is the
  * responsibility of the caller to ensure all the components added are for the
  * same event (i.e. UIDs are all the same). Also, the master instance MUST be
  * added first.
  */
 
 public class InstanceList extends HashMap {
 
     private static final long serialVersionUID = 1838360990532590681L;
     private boolean isUTC = false;
     private TimeZone timezone = null;
     
     public InstanceList() {
         super();
     }
 
     /**
      * Add a component (either master or override instance) if it falls within
      * the specified time range.
      * 
      * @param comp
      * @param rangeStart
      * @param rangeEnd
      */
     public void addComponent(Component comp, Date rangeStart, Date rangeEnd) {
 
         // See if it contains a recurrence ID
         if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null) {
             addMaster(comp, rangeStart, rangeEnd);
         } else {
             addOverride(comp);
         }
     }
     
     /**
      * @return if the InstanceList generates instances in UTC format.
      */
     public boolean isUTC() {
         return isUTC;
     }
 
     /**
      * Instruct the InstanceList to generate instances in UTC time periods.
      * If set to false, InstanceList will generate floating time instances
      * for events with floating date/times.
      * @param isUTC
      */
     public void setUTC(boolean isUTC) {
         this.isUTC = isUTC;
     }
     
     /**
      * @return timezone used to convert floating times to UTC.  Only
      * used if isUTC is set to true.
      */
     public TimeZone getTimezone() {
         return timezone;
     }
 
     /**
      * Set the timezone to use when converting floating times to
      * UTC.  Only used if isUTC is set to true.
      * @param timezone
      */
     public void setTimezone(TimeZone timezone) {
         this.timezone = timezone;
     }
 
     /**
      * Add a master component if it falls within the specified time range.
      * 
      * @param comp
      * @param rangeStart
      * @param rangeEnd
      */
     protected void addMaster(Component comp, Date rangeStart, Date rangeEnd) {
 
         Date start = getStartDate(comp);
         
         if (start == null) {
             return;
         }
         
         // Convert to UTC if needed
         if(start instanceof DateTime)
             start = convertToUTCIfNecessary((DateTime) start);
         
         
         Dur duration = null;
         Date end = getEndDate(comp);
         if (end == null) {
             if (start instanceof DateTime) {
                 // Its an timed event with no duration
                 duration = new Dur(0, 0, 0, 0);
             } else {
                 // Its an all day event so duration is one day
                 duration = new Dur(1, 0, 0, 0);
             }
             end = Dates.getInstance(duration.getTime(start), start);
         } else {
             // Convert to UTC if needed
             if(end instanceof DateTime)
                 end = convertToUTCIfNecessary((DateTime) end);
             duration = new Dur(start, end);
         }
         
         // Always add first instance if included in range..
        if (start.before(rangeEnd) && end.after(rangeStart)) {
             Instance instance = new Instance(comp, start, end);
             put(instance.getRid().toString(), instance);
         }
 
         // recurrence dates..
         PropertyList rDates = comp.getProperties()
                 .getProperties(Property.RDATE);
         for (Iterator i = rDates.iterator(); i.hasNext();) {
             RDate rdate = (RDate) i.next();
             // Both PERIOD and DATE/DATE-TIME values allowed
             if (Value.PERIOD.equals(rdate.getParameters().getParameter(
                     Parameter.VALUE))) {
                 for (Iterator j = rdate.getPeriods().iterator(); j.hasNext();) {
                     Period period = (Period) j.next();
                     if (period.getStart().before(rangeEnd)
                             && period.getEnd().after(rangeStart)) {
                         Instance instance = new Instance(comp, period
                                 .getStart(), period.getEnd());
                         put(instance.getRid().toString(), instance);
                     }
                 }
             } else {
                 for (Iterator j = rdate.getDates().iterator(); j.hasNext();) {
                     Date startDate = (Date) j.next();
                     Date endDate = Dates.getInstance(duration
                             .getTime(startDate), start);
                     Instance instance = new Instance(comp, startDate, endDate);
                     put(instance.getRid().toString(), instance);
                 }
             }
         }
 
         // recurrence rules..
         PropertyList rRules = comp.getProperties()
                 .getProperties(Property.RRULE);
         for (Iterator i = rRules.iterator(); i.hasNext();) {
             RRule rrule = (RRule) i.next();
             // DateList startDates = rrule.getRecur().getDates(start.getDate(),
             // adjustedRangeStart, rangeEnd, (Value)
             // start.getParameters().getParameter(Parameter.VALUE));
             DateList startDates = rrule.getRecur().getDates(start, rangeStart,
                     rangeEnd,
                     (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
             for (int j = 0; j < startDates.size(); j++) {
                 Date startDate = Dates.getInstance((Date) startDates.get(j),
                         start);
                 Date endDate = Dates.getInstance(duration.getTime(startDate),
                         start);
                 Instance instance = new Instance(comp, startDate, endDate);
                 put(instance.getRid().toString(), instance);
             }
         }
         // exception dates..
         PropertyList exDates = comp.getProperties().getProperties(
                 Property.EXDATE);
         for (Iterator i = exDates.iterator(); i.hasNext();) {
             ExDate exDate = (ExDate) i.next();
             for (Iterator j = exDate.getDates().iterator(); j.hasNext();) {
                 Date startDate = Dates.getInstance((Date) j.next(), start);
                 Instance instance = new Instance(comp, startDate, startDate);
                 remove(instance.getStart().toString());
             }
         }
         // exception rules..
         PropertyList exRules = comp.getProperties().getProperties(
                 Property.EXRULE);
         for (Iterator i = exRules.iterator(); i.hasNext();) {
             ExRule exrule = (ExRule) i.next();
             // DateList startDates = exrule.getRecur().getDates(start.getDate(),
             // adjustedRangeStart, rangeEnd, (Value)
             // start.getParameters().getParameter(Parameter.VALUE));
             DateList startDates = exrule.getRecur().getDates(start, rangeStart,
                     rangeEnd,
                     (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
             for (Iterator j = startDates.iterator(); j.hasNext();) {
                 Date startDate = Dates.getInstance((Date) j.next(), start);
                 Instance instance = new Instance(comp, startDate, startDate);
                 remove(instance.getStart().toString());
             }
         }
     }
 
     /**
      * Add an override component if it falls within the specified time range.
      * 
      * @param comp
      * @return true if the override component modifies instance list and false
      *         if the override component has no effect on instance list
      */
     public boolean addOverride(Component comp) {
 
         boolean modified = false;
 
         // Verify if component is an override
         if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null)
             return false;
         
         // First check to see that the appropriate properties are present.
 
         // We need a DTSTART.
         Date dtstart = getStartDate(comp);
         if (dtstart == null)
             return false;
 
         // Convert to UTC if needed
         if(dtstart instanceof DateTime)
             dtstart = convertToUTCIfNecessary((DateTime) dtstart);
         
         // We need either DTEND or DURATION.
         Date dtend = getEndDate(comp);
         if (dtend == null) {
             Dur duration;
             if (dtstart instanceof DateTime) {
                 // Its an timed event with no duration
                 duration = new Dur(0, 0, 0, 0);
             } else {
                 // Its an all day event so duration is one day
                 duration = new Dur(1, 0, 0, 0);
             }
             dtend = Dates.getInstance(duration.getTime(dtstart), dtstart);
         } else {
             // Convert to UTC if needed
             if(dtend instanceof DateTime)
                 dtend = convertToUTCIfNecessary((DateTime) dtend);
         }
 
         // Now create the map entry
         Date riddt = getReccurrenceId(comp);
         if(riddt instanceof DateTime)
             riddt = convertToUTCIfNecessary((DateTime) riddt);
         
         boolean future = getRange(comp);
 
         Instance instance = new Instance(comp, dtstart, dtend, riddt, true,
                 future);
         String key = instance.getRid().toString();
 
         // Replace the master instance by adding this one
         // only if it exists.
         if(containsKey(key)) {
             put(key, instance);
             modified = true;
         }
 
         // Handle THISANDFUTURE if present
         Range range = (Range) comp.getProperties().getProperty(
                 Property.RECURRENCE_ID).getParameters().getParameter(
                 Parameter.RANGE);
 
         // TODO Ignoring THISANDPRIOR
         if ((range != null) && Range.THISANDFUTURE.equals(range)) {
 
             // Policy - iterate over all the instances after this one, replacing
             // the original instance withg a version adjusted to match the
             // override component
 
             // We need to account for a time shift in the overridden component
             // by applying the same shift to the future instances
             boolean timeShift = (dtstart.compareTo(riddt) != 0);
             Dur offsetTime = (timeShift ? new Dur(riddt, dtstart) : null);
             Dur newDuration = (timeShift ? new Dur(dtstart, dtend) : null);
 
             // Get a sorted list rids so we can identify the starting location
             // for the override.  The starting position will be the rid after
             // the current rid, or in the case of no matching rid, the first
             // rid that is greater than the current rid.
             boolean containsKey = containsKey(key);
             TreeSet sortedKeys = new TreeSet(keySet());
             for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
                 String ikey = (String) iter.next();
                 if (ikey.equals(key) || (!containsKey && ikey.compareTo(key)>0)) {
                     
                     if(containsKey && !iter.hasNext())
                         continue;
                     else if(containsKey)
                         ikey = (String) iter.next();
                     
                     boolean moreKeys = true;
                     boolean firstMatch = true;
                     while(moreKeys==true) {
                         
                         // The target key is already set for the first
                         // iteration, so for all other iterations
                         // get the next target key.
                         if(firstMatch)
                             firstMatch = false;
                         else
                             ikey = (String) iter.next();
                         
                         Instance oldinstance = (Instance) get(ikey);
 
                         // Do not override an already overridden instance
                         if (oldinstance.isOverridden())
                             continue;
 
                         // Determine start/end for new instance which may need
                         // to be offset by the start/end offset and adjusted for
                         // a new duration from the overridden component
                         Date originalstart = oldinstance.getRid();
                         Date start = oldinstance.getStart();
                         Date end = oldinstance.getEnd();
 
                         if (timeShift) {
                             // Handling of overlapping overridden THISANDFUTURE
                             // components is not defined in 2445. The policy
                             // here is that a THISANDFUTURE override should
                             // override any previous THISANDFUTURE overrides. So
                             // we need to use the original start time for the
                             // instance being adjusted as the time that is
                             // shifted, and the original start time is geiven by
                             // its recurrence-id.
                             start = Dates.getInstance(offsetTime
                                     .getTime(originalstart), originalstart);
                             end = Dates.getInstance(newDuration.getTime(start),
                                     start);
                         }
 
                         // Replace with new instance
                         Instance newinstance = new Instance(comp, start, end,
                                 originalstart, false, false);
                         remove(ikey);
                         put(newinstance.getRid().toString(), newinstance);
                         modified = true;
                         
                         if(!iter.hasNext())
                             moreKeys = false;
                     }
                 }
             }
         }
         
         return modified;
     }
 
     private Date getStartDate(Component comp) {
         DtStart prop = (DtStart) comp.getProperties().getProperty(
                 Property.DTSTART);
         return (prop != null) ? prop.getDate() : null;
     }
 
     private Date getEndDate(Component comp) {
         DtEnd dtEnd = (DtEnd) comp.getProperties().getProperty(Property.DTEND);
         // No DTEND? No problem, we'll use the DURATION if present.
         if (dtEnd == null) {
             Date dtStart = getStartDate(comp);
             Duration duration = (Duration) comp.getProperties().getProperty(
                     Property.DURATION);
             if (duration != null) {
                 dtEnd = new DtEnd(Dates.getInstance(duration.getDuration()
                         .getTime(dtStart), dtStart));
             }
         }
         return (dtEnd != null) ? dtEnd.getDate() : null;
     }
 
     private final Date getReccurrenceId(Component comp) {
         RecurrenceId rid = (RecurrenceId) comp.getProperties().getProperty(
                 Property.RECURRENCE_ID);
         return (rid != null) ? rid.getDate() : null;
     }
 
     private final boolean getRange(Component comp) {
         RecurrenceId rid = (RecurrenceId) comp.getProperties().getProperty(
                 Property.RECURRENCE_ID);
         if (rid == null)
             return false;
         Parameter range = rid.getParameters().getParameter(Parameter.RANGE);
         return (range != null) && "THISANDFUTURE".equals(range.getValue());
     }
     
     private DateTime convertToUTCIfNecessary(DateTime date) {
         // If date is not UTC and InstanceList is configured
         // to convert to UTC, then convert time to UTC
         if(!date.isUtc() && isUTC) {
             // If there is a timezone in the time, then use that
             if(date.getTimeZone()!=null) {
                 try {
                     DateTime newDT = new DateTime(date.toString(),date.getTimeZone());
                     newDT.setUtc(true);
                     return newDT;
                 } catch (ParseException e) {
                     // shouldnt occur
                 }
             }
             // Otherwise use timezone that InstanceList is configured
             // with, or use the system default.
             else {
                 try {
                     DateTime newDT = null;
                     if(timezone == null)
                         newDT = new DateTime(date.toString());
                     else
                         newDT = new DateTime(date.toString(),timezone);
                     newDT.setUtc(true);
                     return newDT;
                 } catch (ParseException e) {
                    // shouldn't occur
                 }
             }    
         }
         return date;
     }
 }
