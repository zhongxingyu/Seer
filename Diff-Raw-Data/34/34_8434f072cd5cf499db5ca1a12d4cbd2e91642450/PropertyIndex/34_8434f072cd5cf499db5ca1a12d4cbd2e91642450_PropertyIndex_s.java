 /* **********************************************************************
     Copyright 2009 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 package edu.rpi.cmt.calendar;
 
 import java.io.Serializable;
 import java.util.HashMap;
 
 /** Define an (arbitrary) index associated with calendar properties
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public class PropertyIndex implements Serializable {
   private PropertyIndex() {};
 
   static class ComponentFlags {
     private boolean eventProperty;
     private boolean todoProperty;
     private boolean journalProperty;
     private boolean freeBusyProperty;
     private boolean timezoneProperty;
     private boolean alarmProperty;
 
     ComponentFlags(final boolean eventProperty,
                    final boolean todoProperty,
                    final boolean journalProperty,
                    final boolean freeBusyProperty,
                    final boolean timezoneProperty,
                    final boolean alarmProperty) {
       this.eventProperty = eventProperty;
       this.todoProperty = todoProperty;
       this.journalProperty = journalProperty;
       this.freeBusyProperty = freeBusyProperty;
       this.timezoneProperty = timezoneProperty;
       this.alarmProperty = alarmProperty;
     }
   }
 
   static final ComponentFlags noComponent =
      new ComponentFlags(false, false, false, false, false, false);
 
   static final ComponentFlags eventOnly =
      new ComponentFlags(true, false, false, false, false, false);
 
   static final ComponentFlags todoOnly =
      new ComponentFlags(false, true, false, false, false, false);
 
   static final ComponentFlags freebusyOnly =
      new ComponentFlags(false, false, false, true, false, false);
 
   static final ComponentFlags timezoneOnly =
      new ComponentFlags(false, false, false, false, true, false);
 
   static final ComponentFlags alarmOnly =
      new ComponentFlags(false, false, false, false, false, true);
 
   static final ComponentFlags event_Todo =
      new ComponentFlags(true, true, false, false, false, false);
 
   static final ComponentFlags event_Todo_Journal =
      new ComponentFlags(true, true, true, false, false, false);
 
   static final ComponentFlags event_Todo_Freebusy =
      new ComponentFlags(true, true, false, true, false, false);
 
   static final ComponentFlags event_Freebusy =
      new ComponentFlags(true, false, false, true, false, false);
 
   static final ComponentFlags event_Todo_Journal_Freebusy =
      new ComponentFlags(true, true, true, true, false, false);
 
   static final ComponentFlags event_Todo_Journal_Timezone =
      new ComponentFlags(true, true, true, false, true, false);
 
   static final ComponentFlags event_Todo_Journal_Alarm =
      new ComponentFlags(true, true, true, false, false, true);
 
   static final ComponentFlags notTimezone =
      new ComponentFlags(true, true, true, true, false, true);
 
   static final ComponentFlags notAlarm =
      new ComponentFlags(true, true, true, true, true, false);
 
   static final ComponentFlags allComponents =
      new ComponentFlags(true, true, true, true, true, true);
 
   private static boolean multi = true;
 
   private static boolean single = false;
 
   /** */
   public static enum PropertyInfoIndex {
     /** */
     UNKNOWN_PROPERTY(null, single, noComponent),
 
     /** */
     CLASS("CLASS", single, event_Todo_Journal),
 
     /** */
     CREATED("CREATED", single, event_Todo_Journal_Freebusy),
 
     /** */
    DESCRIPTION("DESCRIPTION", single, event_Todo_Journal_Alarm),
 
     /** */
     DTSTAMP("DTSTAMP", single, event_Todo_Journal_Freebusy),
 
     /** */
     DTSTART("DTSTART", single, notAlarm),
 
     /** */
     DURATION("DURATION", single, event_Todo_Freebusy),
 
     /** */
     GEO("GEO", single, event_Todo),
 
     /** */
     LAST_MODIFIED("LAST-MODIFIED", single, event_Todo_Journal_Timezone),
 
     /** */
     LOCATION("LOCATION", single, event_Todo),
 
     /** */
     ORGANIZER("ORGANIZER", single, event_Todo_Journal_Freebusy),
 
     /** */
     PRIORITY("PRIORITY", single, event_Todo),
 
     /** */
     RECURRENCE_ID("RECURRENCE-ID", single, event_Todo_Journal_Freebusy),
 
     /** */
     SEQUENCE("SEQUENCE", single, event_Todo_Journal),
 
     /** */
     STATUS("STATUS", single, event_Todo_Journal),
 
     /** */
    SUMMARY("SUMMARY", single, event_Todo_Journal_Alarm),
 
     /** */
     UID("UID", single, event_Todo_Journal_Freebusy),
 
     /** */
     URL("URL", single, event_Todo_Journal_Freebusy),
 
     /* Event only */
 
     /** */
     DTEND("DTEND", single, event_Freebusy),
 
     /** */
     TRANSP("TRANSP", single, eventOnly),
 
     /* Todo only */
 
     /** */
     COMPLETED("COMPLETED", single, todoOnly),
 
     /** */
     DUE("DUE", single, todoOnly),
 
     /** */
     PERCENT_COMPLETE("PERCENT-COMPLETE", single, todoOnly),
 
     /* ---------------------------- Multi valued --------------- */
 
     /* Event and Todo */
 
     /** */
     ATTACH("ATTACH", multi, event_Todo_Journal_Alarm),
 
     /** */
     ATTENDEE ("ATTENDEE", multi, notTimezone),
 
     /** */
     CATEGORIES("CATEGORIES", multi, event_Todo_Journal_Alarm),
 
     /** */
     COMMENT("COMMENT", multi, notAlarm),
 
     /** */
     CONTACT("COMMENT", multi, event_Todo_Journal_Freebusy),
 
     /** */
     EXDATE("EXDATE", multi, event_Todo_Journal_Timezone),
 
     /** */
     EXRULE("EXRULE", multi, event_Todo_Journal_Timezone),
 
     /** */
     REQUEST_STATUS("", multi, event_Todo_Journal_Freebusy),
 
     /** */
     RELATED_TO("", multi, event_Todo_Journal),
 
     /** */
     RESOURCES("RESOURCES", multi, event_Todo),
 
     /** */
     RDATE("RDATE", multi, event_Todo_Journal_Timezone),
 
     /** */
     RRULE ("RRULE", multi, event_Todo_Journal_Timezone),
 
     /* -------------- Other non-event, non-todo ---------------- */
 
     /** */
     FREEBUSY("FREEBUSY", single, freebusyOnly),
 
     /** */
     TZID("TZID", single, timezoneOnly),
 
     /** */
     TZNAME("TZNAME", single, timezoneOnly),
 
     /** */
     TZOFFSETFROM("TZOFFSETFROM", single, timezoneOnly),
 
     /** */
     TZOFFSETTO("TZOFFSETTO", single, timezoneOnly),
 
     /** */
     TZURL("TZURL", single, timezoneOnly),
 
     /** */
     ACTION("ACTION", single, alarmOnly),
 
     /** */
     REPEAT("REPEAT", single, alarmOnly),
 
     /** */
     TRIGGER("TRIGGER", single, alarmOnly),
 
     /** non ical */
     CREATOR("CREATOR", single, event_Todo_Journal),
 
     /** non ical */
     OWNER("OWNER", single, event_Todo_Journal),
 
     /** non ical */
     COST("COST", single, event_Todo),
 
     /** non ical */
     CTAG("CTAG", single, noComponent),
 
     /** non ical */
     ETAG("ETAG", single, noComponent),
 
     /** non ical */
     COLLECTION("COLLECTION", single, event_Todo_Journal),
 
     /** non ical */
     ENTITY_TYPE("ENTITY_TYPE", single, event_Todo_Journal),
 
     /** treat VALARM sub-component as a property */
     VALARM("VALARM", multi, notAlarm),
 
     /** treat x-properties as a single multi-valued property */
     XPROP("XPROP", multi, allComponents),
 
     /** ----------------------------- Following are parameters ----------- */
 
     /** */
     LANG("LANGUAGE", single, noComponent,
          true),                // param
 
     /** */
     TZIDPAR("TZID", single, noComponent,
             true)             // param
             ;
 
     private String pname;
 
     private boolean multiValued;
 
     private boolean param; /* It's a parameter   */
 
     private ComponentFlags components;
 
     private static HashMap<String, PropertyInfoIndex> pnameLookup =
       new HashMap<String, PropertyInfoIndex>();
 
     static {
       for (PropertyInfoIndex pii: values()) {
         String pname = pii.getPname();
 
         if (pname != null) {
           pname = pname.toLowerCase();
         }
         pnameLookup.put(pname, pii);
       }
     }
 
     PropertyInfoIndex(final String pname, final boolean multiValued,
                       final ComponentFlags components) {
       this.pname = pname;
       this.components = components;
       this.multiValued = multiValued;
     }
 
     PropertyInfoIndex(final String pname, final boolean multiValued,
                       final ComponentFlags components,
                       final boolean param) {
       this(pname, multiValued, components);
       this.param = param;
     }
 
     /** get the property name
      *
      * @return property name
      */
     public String getPname() {
       return pname;
     }
 
    /** May need some elaboration
      *
      * @return boolean
      */
     public boolean getMultiValued() {
       return multiValued;
     }
 
     /** True if it's a parameter
      *
      * @return boolean
      */
     public boolean getParam() {
       return param;
     }
 
     /** True if it's an event property
      *
      * @return boolean
      */
     public boolean getEventProperty() {
       return components.eventProperty;
     }
 
     /** True if it's a todo property
      *
      * @return boolean
      */
     public boolean getTodoProperty() {
       return components.todoProperty;
     }
 
     /** True if it's a journal property
      *
      * @return boolean
      */
     public boolean getJournalProperty() {
       return components.journalProperty;
     }
 
     /** True if it's a freebusy property
      *
      * @return boolean
      */
     public boolean getFreeBusyProperty() {
       return components.freeBusyProperty;
     }
 
     /** True if it's a timezone property
      *
      * @return boolean
      */
     public boolean getTimezoneProperty() {
       return components.timezoneProperty;
     }
 
     /** True if it's an alarm property
      *
      * @return boolean
      */
     public boolean getAlarmProperty() {
       return components.alarmProperty;
     }
 
     /** get the index given the property name
      *
      * @param val
      * @return PropertyInfoIndex
      */
     public static PropertyInfoIndex lookupPname(final String val) {
       return pnameLookup.get(val.toLowerCase());
     }
   }
 }
