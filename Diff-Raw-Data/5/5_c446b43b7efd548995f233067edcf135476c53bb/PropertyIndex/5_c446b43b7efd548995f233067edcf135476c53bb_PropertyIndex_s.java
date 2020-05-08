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
 
 import edu.rpi.sss.util.xml.tagdefs.XcalTags;
 
 import java.io.Serializable;
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 
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
 
   private static boolean IS_MULTI = true;
 
   private static boolean IS_SINGLE = false;
 
   private static boolean IS_PARAM = true;
 
   private static boolean NOT_PARAM = false;
 
   private static boolean IS_IMMUTABLE = true;
 
   private static boolean NOT_IMMUTABLE = false;
 
   /** */
   public static enum DataType {
     /** */
     BINARY(XcalTags.binary),
 
     /** */
     BOOLEAN(XcalTags._boolean),
 
     /** */
     CUA(XcalTags.calAddress),
 
     /** */
     DATE(XcalTags.date),
 
     /** */
     DATE_TIME(XcalTags.dateTime),
 
     /** */
     DURATION(XcalTags.duration),
 
     /** */
     FLOAT(XcalTags._float),
 
     /** */
     INTEGER(XcalTags.integer),
 
     /** */
     PERIOD(XcalTags.period),
 
     /** */
     RECUR(XcalTags.recur),
 
     /** */
     TEXT(XcalTags.text),
 
     /** */
     TIME(XcalTags.time),
 
     /** */
     URI(XcalTags.uri),
 
     /** */
     UTC_OFFSET(XcalTags.utcOffset),
 
     /** More work */
     SPECIAL(null),
 
     /** Non-ical */
     HREF(null);
 
     private QName xcalType;
 
     DataType(final QName xcalType) {
       this.xcalType = xcalType;
     }
 
     /**
      * @return type or null
      */
     public QName getXcalType() {
       return xcalType;
     }
   };
 
   /** */
   public static enum ParameterInfoIndex {
     /** */
     UNKNOWN_PARAMETER(null),
 
     /**
      * Region abbreviation.
      */
     ABBREV("ABBREV"),
 
     /**
      * Alternate text representation.
      */
     ALTREP("ALTREP"),
 
     /**
      * Common name.
      */
     CN("CN"),
 
     /**
      * Calendar user type.
      */
     CUTYPE("CUTYPE"),
 
     /**
      * Delegator.
      */
     DELEGATED_FROM("DELEGATED-FROM"),
 
     /**
      * Delegatee.
      */
     DELEGATED_TO("DELEGATED-TO"),
 
     /**
      * Directory entry.
      */
     DIR("DIR"),
 
     /**
      * Inline encoding.
      */
     ENCODING("ENCODING"),
 
     /**
      * Format type.
      */
     FMTTYPE("FMTTYPE"),
 
     /**
      * Free/busy time type.
      */
     FBTYPE("FBTYPE"),
 
     /**
      * Language for text.
      */
     LANGUAGE("LANGUAGE"),
 
     /**
      * Group or list membership.
      */
     MEMBER("MEMBER"),
 
     /**
      * Participation status.
      */
     PARTSTAT("PARTSTAT"),
 
     /**
      * Recurrence identifier range.
      */
     RANGE("RANGE"),
 
     /**
      * Alarm trigger relationship.
      */
     RELATED("RELATED"),
 
     /**
      * Relationship type.
      */
     RELTYPE("RELTYPE"),
 
     /**
      * Participation role.
      */
     ROLE("ROLE"),
 
     /**
      * RSVP expectation.
      */
     RSVP("RSVP"),
 
     /**
      * Schedule agent.
      */
     SCHEDULE_AGENT("SCHEDULE-AGENT"),
 
     /**
      * Schedule status.
      */
     SCHEDULE_STATUS("SCHEDULE-STATUS"),
 
     /**
      * Sent by.
      */
     SENT_BY("SENT-BY"),
 
     /**
      * Type.
      */
     TYPE("TYPE"),
 
     /**
      * Reference to time zone object.
      */
     TZID("TZID"),
 
     /**
      * Property value data type.
      */
     VALUE("VALUE");
 
     private String pname;
 
     private DataType ptype;
 
     private static HashMap<String, ParameterInfoIndex> pnameLookup =
       new HashMap<String, ParameterInfoIndex>();
 
     static {
       for (ParameterInfoIndex pii: values()) {
         String pname = pii.getPname();
 
         if (pname != null) {
           pname = pname.toLowerCase();
         }
         pnameLookup.put(pname, pii);
       }
     }
 
     ParameterInfoIndex(final String pname) {
       this(pname, DataType.TEXT);
     }
 
     ParameterInfoIndex(final String pname,
                       final DataType ptype) {
       this.pname = pname;
       this.ptype = ptype;
     }
 
     /** get the parameter name
      *
      * @return parameter name
      */
     public String getPname() {
       return pname;
     }
 
     /** get the parameter type
      *
      * @return parameter type
      */
     public DataType getPtype() {
       return ptype;
     }
 
     /** get the index given the parameter name
      *
      * @param val
      * @return ParameterInfoIndex
      */
     public static ParameterInfoIndex lookupPname(final String val) {
       return pnameLookup.get(val.toLowerCase());
     }
   }
 
   /** */
   public static enum PropertyInfoIndex {
     /** */
     UNKNOWN_PROPERTY(null, IS_SINGLE, noComponent),
 
     /** */
     CLASS("CLASS", IS_SINGLE, event_Todo_Journal),
 
     /** */
     CREATED("CREATED", DataType.DATE_TIME,
             IS_SINGLE, event_Todo_Journal_Freebusy),
 
     /** */
     DESCRIPTION("DESCRIPTION", IS_SINGLE, IS_MULTI, event_Todo_Journal_Alarm),
 
     /** */
     DTSTAMP("DTSTAMP", DataType.DATE_TIME,
             IS_SINGLE, event_Todo_Journal_Freebusy,
             NOT_PARAM, NOT_IMMUTABLE),
 
     /** */
     DTSTART("DTSTART", DataType.DATE_TIME,
             IS_SINGLE, notAlarm),
 
     /** */
     DURATION("DURATION", DataType.DURATION,
              IS_SINGLE, event_Todo_Freebusy),
 
     /** */
     GEO("GEO", IS_SINGLE, event_Todo),
 
     /** */
     LAST_MODIFIED("LAST-MODIFIED", DataType.DATE_TIME,
                   IS_SINGLE, event_Todo_Journal_Timezone,
                   NOT_PARAM, NOT_IMMUTABLE),
 
     /** */
     LOCATION("LOCATION", IS_SINGLE, event_Todo),
 
     /** */
     ORGANIZER("ORGANIZER", DataType.CUA,
               IS_SINGLE, event_Todo_Journal_Freebusy),
 
     /** */
     PRIORITY("PRIORITY", DataType.INTEGER,
              IS_SINGLE, event_Todo),
 
     /** */
     RECURRENCE_ID("RECURRENCE-ID", DataType.DATE_TIME,
                   IS_SINGLE, event_Todo_Journal_Freebusy),
 
     /** */
     SEQUENCE("SEQUENCE", DataType.INTEGER,
              IS_SINGLE, event_Todo_Journal,
              NOT_PARAM, NOT_IMMUTABLE),
 
     /** */
     STATUS("STATUS", IS_SINGLE, event_Todo_Journal),
 
     /** */
     SUMMARY("SUMMARY", IS_SINGLE, IS_MULTI, event_Todo_Journal_Alarm),
 
     /** */
     UID("UID", IS_SINGLE, event_Todo_Journal_Freebusy),
 
     /** */
     URL("URL", DataType.URI,
         IS_SINGLE, event_Todo_Journal_Freebusy),
 
     /* Event only */
 
     /** */
     DTEND("DTEND", DataType.DATE_TIME,
           IS_SINGLE, event_Freebusy),
 
     /** */
     TRANSP("TRANSP", IS_SINGLE, eventOnly),
 
     /* Todo only */
 
     /** */
     COMPLETED("COMPLETED", DataType.DATE_TIME,
               IS_SINGLE, todoOnly),
 
     /** */
     DUE("DUE", DataType.DATE_TIME,
         IS_SINGLE, todoOnly),
 
     /** */
     PERCENT_COMPLETE("PERCENT-COMPLETE", IS_SINGLE, todoOnly),
 
     /* ---------------------------- Multi valued --------------- */
 
     /* Event and Todo */
 
     /** */
     ATTACH("ATTACH", DataType.SPECIAL,
            IS_MULTI, event_Todo_Journal_Alarm),
 
     /** */
     ATTENDEE("ATTENDEE", DataType.CUA,
              IS_MULTI, notTimezone),
 
     /** */
     CATEGORIES("CATEGORIES", IS_MULTI, event_Todo_Journal_Alarm),
 
     /** */
     COMMENT("COMMENT", IS_MULTI, notAlarm),
 
     /** */
     CONTACT("CONTACT", IS_MULTI, event_Todo_Journal_Freebusy),
 
     /** */
     EXDATE("EXDATE", DataType.DATE_TIME,
            IS_MULTI, event_Todo_Journal_Timezone),
 
     /** */
    EXRULE("EXRULE", DataType.SPECIAL,
            IS_MULTI, event_Todo_Journal_Timezone),
 
     /** */
     REQUEST_STATUS("REQUEST-STATUS", IS_MULTI, event_Todo_Journal_Freebusy),
 
     /** */
     RELATED_TO("RELATED-TO", IS_MULTI, event_Todo_Journal),
 
     /** */
     RESOURCES("RESOURCES", IS_MULTI, event_Todo),
 
     /** */
     RDATE("RDATE", DataType.DATE_TIME,
           IS_MULTI, event_Todo_Journal_Timezone),
 
     /** */
    RRULE ("RRULE", DataType.SPECIAL,
            IS_MULTI, event_Todo_Journal_Timezone),
 
     /* -------------- Other non-event, non-todo ---------------- */
 
     /** */
     FREEBUSY("FREEBUSY", DataType.PERIOD,
              IS_SINGLE, freebusyOnly),
 
     /** */
     TZID("TZID", IS_SINGLE, timezoneOnly),
 
     /** */
     TZNAME("TZNAME", IS_SINGLE, timezoneOnly),
 
     /** */
     TZOFFSETFROM("TZOFFSETFROM", DataType.UTC_OFFSET,
                  IS_SINGLE, timezoneOnly),
 
     /** */
     TZOFFSETTO("TZOFFSETTO", DataType.UTC_OFFSET,
                IS_SINGLE, timezoneOnly),
 
     /** */
     TZURL("TZURL", DataType.URI,
           IS_SINGLE, timezoneOnly),
 
     /** */
     ACTION("ACTION", IS_SINGLE, alarmOnly),
 
     /** */
     REPEAT("REPEAT", DataType.INTEGER,
            IS_SINGLE, alarmOnly),
 
     /** */
     TRIGGER("TRIGGER", DataType.SPECIAL,
             IS_SINGLE, alarmOnly),
 
     /* -------------- Non-ical ---------------- */
 
     /** non ical */
     CREATOR("CREATOR", DataType.HREF, IS_SINGLE, event_Todo_Journal,
             NOT_PARAM, IS_IMMUTABLE),
 
     /** non ical */
     OWNER("OWNER", DataType.HREF, IS_SINGLE, event_Todo_Journal,
           NOT_PARAM, IS_IMMUTABLE),
 
     /** non ical */
     END_TYPE("END-TYPE", IS_SINGLE, event_Todo_Journal),
 
     /** non ical */
     COST("COST", IS_SINGLE, event_Todo),
 
     /** non ical */
     CTAG("CTAG", DataType.TEXT, IS_SINGLE, noComponent,
          NOT_PARAM, IS_IMMUTABLE),
 
     /** non ical */
     DELETED("DELETED", IS_SINGLE, event_Todo),
 
     /** non ical */
     ETAG("ETAG", DataType.TEXT, IS_SINGLE, noComponent,
          NOT_PARAM, IS_IMMUTABLE),
 
     /** non ical */
     COLLECTION("COLLECTION", IS_SINGLE, event_Todo_Journal),
 
     /** non ical */
     ENTITY_TYPE("ENTITY_TYPE", DataType.INTEGER,
                 IS_SINGLE, event_Todo_Journal,
                 NOT_PARAM, IS_IMMUTABLE),
 
     /** treat VALARM sub-component as a property */
     VALARM("VALARM", IS_MULTI, notAlarm),
 
     /** treat x-properties as a single multi-valued property */
     XPROP("XPROP", IS_MULTI, allComponents),
 
     /** ----------------------------- Following are parameters ----------- */
 
     /** */
     LANG("LANGUAGE", DataType.TEXT, IS_SINGLE, noComponent,
          IS_PARAM, NOT_IMMUTABLE),
 
     /** */
     TZIDPAR("TZID", DataType.TEXT, IS_SINGLE, noComponent,
             IS_PARAM, NOT_IMMUTABLE),
             ;
 
     private String pname;
 
     private DataType ptype;
 
     /* true if the standard says it's multi */
     private boolean multiValued;
 
     /* true if we store multi - e.g. multi-language */
     private boolean dbMultiValued;
 
     private boolean param; /* It's a parameter   */
 
     private boolean immutable;
 
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
       dbMultiValued = multiValued;
     }
 
     PropertyInfoIndex(final String pname,
                       final DataType ptype, final boolean multiValued,
                       final ComponentFlags components) {
       this(pname, multiValued, components);
       this.ptype = ptype;
     }
 
     PropertyInfoIndex(final String pname, final boolean multiValued,
                       final boolean dbMultiValued,
                       final ComponentFlags components) {
       this(pname, DataType.TEXT, multiValued, components,
            NOT_PARAM, NOT_IMMUTABLE);
       this.dbMultiValued = dbMultiValued;
     }
 
     PropertyInfoIndex(final String pname,
                       final DataType ptype,
                       final boolean multiValued,
                       final ComponentFlags components,
                       final boolean param,
                       final boolean immutable) {
       this(pname, multiValued, components);
       this.ptype = ptype;
       this.param = param;
       this.immutable = immutable;
     }
 
     /** get the property name
      *
      * @return property name
      */
     public String getPname() {
       return pname;
     }
 
     /** get the property type
      *
      * @return property type
      */
     public DataType getPtype() {
       return ptype;
     }
 
     /** May need some elaboration - this is for the standard
      *
      * @return boolean
      */
     public boolean getMultiValued() {
       return multiValued;
     }
 
     /** May need some elaboration - this is for the db
      *
      * @return boolean
      */
     public boolean getDbMultiValued() {
       return dbMultiValued;
     }
 
     /** True if it's a parameter
      *
      * @return boolean
      */
     public boolean getParam() {
       return param;
     }
 
     /** True if it's immutable
      *
      * @return boolean
      */
     public boolean getImmutable() {
       return immutable;
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
