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
 package edu.rpi.sss.util.xml.tagdefs;
 
 import javax.xml.namespace.QName;
 
 /** Define Caldav tags for XMlEmit.
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public class CaldavTags implements CaldavDefs {
   /**   */
   public static final QName allcomp = new QName(caldavNamespace,
                                                 "allcomp");
 
   /**   */
   public static final QName allprop = new QName(caldavNamespace,
                                                 "allprop");
 
   /**   */
   public static final QName attendeeAllowed = new QName(caldavNamespace,
                                                 "attendee-allowed");
 
   /** Specifies the resource type of a calendar collection.  */
   public static final QName calendar = new QName(caldavNamespace,
                                                  "calendar");
 
   /**   */
   public static final QName calendarCollectionLocationOk = new QName(caldavNamespace,
                                                      "calendar-collection-location-ok");
 
   /**   */
   public static final QName calendarData = new QName(caldavNamespace,
                                                      "calendar-data");
 
   /** Provides a human-readable description of the calendar collection.
    * NOT ALLPROP
    */
   public static final QName calendarDescription = new QName(caldavNamespace,
                                                             "calendar-description");
 
   /**   */
   public static final QName calendarFreeBusySet = new QName(caldavNamespace,
                                                             "calendar-free-busy-set");
 
   /**   */
   public static final QName calendarHomeSet = new QName(caldavNamespace,
                                                         "calendar-home-set");
 
   /**   */
   public static final QName calendarHomeURL = new QName(caldavNamespace,
                                                         "calendar-home-URL");
 
   /**   */
   public static final QName calendarMultiget = new QName(caldavNamespace,
                                                          "calendar-multiget");
 
   /**   */
   public static final QName calendarQuery = new QName(caldavNamespace,
                                                       "calendar-query");
 
   /**   */
   public static final QName calendarTimezone = new QName(caldavNamespace,
                                                          "calendar-timezone");
 
   /**   */
   public static final QName calendarUserAddressSet = new QName(caldavNamespace,
                                                                "calendar-user-address-set");
 
   /**   */
   public static final QName comp = new QName(caldavNamespace,
                                              "comp");
 
   /**   */
   public static final QName compFilter = new QName(caldavNamespace,
                                                    "comp-filter");
 
   /**   */
   public static final QName expand = new QName(caldavNamespace,
                                                "expand");
 
   /**   */
   public static final QName filter = new QName(caldavNamespace,
                                                "filter");
 
   /**   */
   public static final QName freeBusyQuery = new QName(caldavNamespace,
                                                       "free-busy-query");
 
   /**   */
   public static final QName isNotDefined = new QName(caldavNamespace,
                                                      "is-not-defined");
 
   /**   */
   public static final QName limitFreebusySet = new QName(caldavNamespace,
                                                          "limit-freebusy-set");
 
   /**   */
   public static final QName limitRecurrenceSet = new QName(caldavNamespace,
                                                            "limit-recurrence-set");
 
   /**   */
   public static final QName mkcalendar = new QName(caldavNamespace,
                                                    "mkcalendar");
 
   /**   */
   public static final QName maxAttendeesPerInstance = new QName(caldavNamespace,
                                                     "max-attendees-per-instance");
 
   /**   */
   public static final QName maxDateTime = new QName(caldavNamespace,
                                                     "max-date-time");
 
   /**   */
   public static final QName maxInstances = new QName(caldavNamespace,
                                                      "max-instances");
 
   /**   */
   public static final QName maxResourceSize = new QName(caldavNamespace,
                                                         "max-resource-size");
 
   /**   */
   public static final QName minDateTime = new QName(caldavNamespace,
                                                     "min-date-time");
 
   /**   */
   public static final QName notProcessed = new QName(caldavNamespace,
                                                      "not-processed");
 
   /**   */
   public static final QName noUidConflict = new QName(caldavNamespace,
                                                      "no-uid-conflict");
 
   /**   */
   public static final QName organizerAllowed = new QName(caldavNamespace,
                                                    "organizer-allowed");
 
   /**   */
   public static final QName originator = new QName(caldavNamespace,
                                                    "originator");
 
   /**   */
   public static final QName originatorAllowed = new QName(caldavNamespace,
                                                    "originator-allowed");
 
   /**   */
   public static final QName originatorSpecified = new QName(caldavNamespace,
                                                    "originator-specified");
 
   /**   */
   public static final QName paramFilter = new QName(caldavNamespace,
                                                     "param-filter");
 
   /**   */
   public static final QName processed = new QName(caldavNamespace,
                                                   "processed");
 
   /**   */
   public static final QName prop = new QName(caldavNamespace,
                                              "prop");
 
   /**   */
   public static final QName propFilter = new QName(caldavNamespace,
                                                    "prop-filter");
 
   /**   */
   public static final QName readFreeBusy = new QName(caldavNamespace,
                                                      "read-free-busy");
 
   /**   */
   public static final QName recipient = new QName(caldavNamespace,
                                                   "recipient");
 
   /**   */
   public static final QName recipientPermissions = new QName(caldavNamespace,
                                                   "recipient-permissions");
 
   /**   */
   public static final QName recipientSpecified = new QName(caldavNamespace,
                                                   "recipient-specified");
 
   /**   */
   public static final QName requestStatus = new QName(caldavNamespace,
                                                       "request-status");
 
   /**   */
   public static final QName response = new QName(caldavNamespace,
                                                  "response");
 
   /**   */
   public static final QName returnContentType = new QName(caldavNamespace,
                                                           "return-content-type");
 
   /**   */
   public static final QName schedule = new QName(caldavNamespace,
                                                  "schedule");
 
   /**   */
   public static final QName scheduleFreeBusy = new QName(caldavNamespace,
                                                          "schedule-free-busy");
 
   /**   */
   public static final QName scheduleInbox = new QName(caldavNamespace,
                                                       "schedule-inbox");
 
   /**   */
   public static final QName scheduleInboxURL = new QName(caldavNamespace,
                                                          "schedule-inbox-URL");
 
   /**   */
   public static final QName scheduleOutbox = new QName(caldavNamespace,
                                                        "schedule-outbox");
 
   /**   */
   public static final QName scheduleOutboxURL = new QName(caldavNamespace,
                                                           "schedule-outbox-URL");
 
   /**   */
   public static final QName scheduleReply = new QName(caldavNamespace,
                                                       "schedule-reply");
 
   /**   */
   public static final QName scheduleRequest = new QName(caldavNamespace,
                                                         "schedule-request");
 
   /**   */
   public static final QName scheduleResponse = new QName(caldavNamespace,
                                                          "schedule-response");
 
   /**   */
   public static final QName scheduleState = new QName(caldavNamespace,
                                                       "schedule-state");
 
   /**   */
   public static final QName supportedCalendarComponentSet = new QName(caldavNamespace,
                                             "supported-calendar-component-set");
 
   /**   */
   public static final QName supportedCalendarData = new QName(caldavNamespace,
                                                       "supported-calendar-data");
 
   /**   */
   public static final QName supportedFilter = new QName(caldavNamespace,
                                                         "supported-filter");
 
   /**   */
   public static final QName textMatch = new QName(caldavNamespace,
                                                   "text-match");
 
   /**   */
   public static final QName timeRange = new QName(caldavNamespace,
                                                   "time-range");
 
   /**   */
   public static final QName timezone = new QName(caldavNamespace, "timezone");
 
   /**   */
   public static final QName validCalendarData = new QName(caldavNamespace,
                                                           "valid-calendar-data");
 }
 
