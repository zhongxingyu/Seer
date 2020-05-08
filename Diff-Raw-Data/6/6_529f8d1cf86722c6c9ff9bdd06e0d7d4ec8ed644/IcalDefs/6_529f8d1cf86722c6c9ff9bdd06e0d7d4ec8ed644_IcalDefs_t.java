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
 
 /** Possibly temporary home for definitions that need to be global.
  *
  * @author douglm
  *
  */
 public class IcalDefs {
   /* Integer values associated with each type of calendar entity. Used as a
   code for db entries.
    */
 
   /** */
   public static final int entityTypeEvent = 0;
 
   /** */
   public static final int entityTypeAlarm = 1;
 
   /** */
   public static final int entityTypeTodo = 2;
 
   /** */
   public static final int entityTypeJournal = 3;
 
   /** */
   public static final int entityTypeFreeAndBusy = 4;
 
   /** */
   public static final int entityTypeVavailability = 5;
 
   /** */
   public static final int entityTypeAvailable = 6;
 
   /** */
   public static final String[] entityTypeNames = {"event",
                                                   "alarm",
                                                   "todo",
                                                   "journal",
                                                   "freeAndBusy",
                                                   "vavailability",
                                                   "available"
   };
 
   /** Used to identify types of components within a calendar object */
   public enum IcalComponentType {
     /** */
     none,
 
     /** */
     event,
 
     /** */
     todo,
 
     /** */
     journal,
 
     /** */
     freebusy,
 
     /** */
     vavailability,
 
     /** */
     mixed}
 
   /* ----------------------------------------------------------------------
    *        status we can set for scheduling requests
    * ---------------------------------------------------------------------- */
 
   /** Deferred till later e.g. needs mailing */
   public static final String requestStatusDeferred = "1.0;Deferred";
 
   /**  */
   public static final String requestStatusOK = "2.0;Success";
 
   /** Append property name */
   public static final String requestStatusInvalidProperty =
            "3.0;Invalid Property Name:";
 
   /**  */
   public static final String requestStatusUnsupportedCapability =
            "3.14;Unsupported capability";
 
   /** */
   public static final String requestStatusNoAccess =
            "4.2;No Access";
 
   /* ====================================================================
    *                      Attendee partstat
    * ==================================================================== */
 
   /** none set */
   public static final int partstatNone = -2;
 
   /** x-name or iana-token */
   public static final int partstatOther = -1;
 
   /** Event, to-do, journal */
   public static final int partstatNeedsAction = 0;
   /** Event, to-do, journal */
   public static final int partstatAccepted = 1;
   /** Event, to-do, journal */
   public static final int partstatDeclined = 2;
   /** Event, to-do */
   public static final int partstatTentative = 3;
   /** Event, to-do */
   public static final int partstatdelegated = 4;
   /** to-do */
   public static final int partstatcompleted = 5;
   /** to-do */
   public static final int partstatInProcess = 6;
 
   /** Defined partstat values */
   public final static String[] partstats = {
     "NEEDS-ACTION",
     "ACCEPTED",
     "DECLINED",
     "TENTATIVE",
     "DELEGATED",
     "COMPLETED",
     "IN-PROCESS"
   };
 
   /** Return an index for the partstat
    *
    *  @param  val   String partstat
    *  @return int index, <0 for undefined
    */
  public static int checkPartstat(final String val) {
     if (val == null) {
       return partstatNone;
     }
 
     for (int i = 0; i < partstats.length; i++) {
       if (partstats[i].equals(val)) {
         return i;
       }
     }
 
     return partstatOther;
   }
 
   /*    Parameter Name:  SCHEDULE-AGENT
 
    Purpose:  Indicates what agent is expected to handle scheduling for
       the corresponding Attendee.
 
    Format Definition:  This property parameter is defined by the
       following notation:
 
       scheduleagentparam = "SCHEDULE-AGENT" "="
                         ("SERVER"       ; The server handles scheduling
                        / "CLIENT"       ; The client handles scheduling
                        / "NONE"         ; No automatic scheduling
                        / x-name         ; Experimental type
                        / iana-token)    ; Other IANA registered type
                                         ; Default is SERVER
   */
 
   /** x-name or iana-token */
   public static final int scheduleAgentOther = -1;
 
   /** Server handles it - default */
   public static final int scheduleAgentServer = 0;
 
   /** Client handles attendee */
   public static final int scheduleAgentClient = 1;
 
   /** Nobody handles this one */
   public static final int scheduleAgentNone = 2;
 
   /** */
   public static final String[] scheduleAgents = {"SERVER",
     "CLIENT",
     "NONE"};
 
   /* Delivery status - set in attendee SCHEDULE-STATUS parameter
    +----------+--------------------------------------------------------+
    | Delivery | Description                                            |
    | Status   |                                                        |
    | Code     |                                                        |
    +----------+--------------------------------------------------------+
    | 1.0      | The scheduling message is pending. i.e. the server is  |
    |          | still in the process of sending the message.  The      |
    |          | status code value can be expected to change once the   |
    |          | server has completed its sending and delivery          |
    |          | attempts.                                              |
    |          |                                                        |
    | 1.1      | The scheduling message has been successfully sent.     |
    |          | However, the server does not have explicit information |
    |          | about whether the scheduling message was successfully  |
    |          | delivered to the recipient.  This state can occur with |
    |          | "store and forward" style scheduling protocols such as |
    |          | iMIP [RFC2447] (iTIP using email).                     |
    |          |                                                        |
    | 1.2      | The scheduling message has been successfully           |
    |          | delivered.                                             |
    |          |                                                        |
    | 3.7      | The scheduling message was not delivered because the   |
    |          | server did not recognize the calendar user address of  |
    |          | the recipient as being a supported URI.                |
    |          |                                                        |
    | 3.8      | The scheduling message was not delivered because       |
    |          | access privileges do not allow it.                     |
    |          |                                                        |
    | 5.1      | The scheduling message was not delivered because the   |
    |          | server could not complete delivery of the message.     |
    |          | This is likely due to a temporary failure, and the     |
    |          | originator can try to send the message again at a      |
    |          | later time.                                            |
    |          |                                                        |
    | 5.2      | The scheduling message was not delivered because the   |
    |          | server was not able to find a suitable way to deliver  |
    |          | the message.  This is likely a permanent failure, and  |
    |          | the originator should not try to send the message      |
    |          | again, at least without verifying/correcting the       |
    |          | calendar user address of the recipient.                |
    |          |                                                        |
    | 5.3      | The scheduling message was not delivered and was       |
    |          | rejected because scheduling with that recipient is not |
    |          | allowed.  This is likely a permanent failure, and the  |
    |          | originator should not try to send the message again.   |
    +----------+--------------------------------------------------------+
    */
 
   /** */
   public static final String deliveryStatusPending = "1.0";
 
   /** */
   public static final String deliveryStatusSent = "1.1";
 
   /** */
   public static final String deliveryStatusDelivered = "1.2";
 
   /** */
  public static final String deliveryStatusSuccess = "2.0";

  /** */
   public static final String deliveryStatusInvalidCUA = "3.7";
 
   /** */
   public static final String deliveryStatusNoAccess = "3.8";
 
   /** */
   public static final String deliveryStatusTempFailure = "5.1";
 
   /** */
   public static final String deliveryStatusFailed = "5.2";
 
   /** */
   public static final String deliveryStatusRejected = "5.3";
 }
