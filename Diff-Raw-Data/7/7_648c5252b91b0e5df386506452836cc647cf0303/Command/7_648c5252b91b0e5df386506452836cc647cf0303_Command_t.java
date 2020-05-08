 package org.vikenpedia.fellesprosjekt.shared;
 
 public class Command {
     /**
      * LOGIN:
      * 		strarg1: username
      * 		strarg2: password
      * 
      * SAVE_MEETING:
      * 		data: Meeting
      * 
      * 
      * SAVE_MEETINGPARTICIPANT:
      * 		data: MeetingParticipant
      * 
      * GET_MY_CALENDARS:
      * 		
      * CALENDAR
      * 
      * CALENDARS
      * 
      * @author CVi
      */
     public static enum Commands {
         OK, ERROR, LOGIN, LOGIN_COMPLETE, LOGIN_FAILED, SAVE_MEETING, SAVE_MEETINGPARTICIPANT,
             GET_MY_CALENDARS, CALENDAR, CALENDARS
     }
 
     public Commands command;
     public String data;
     public String strarg1;
     public String strarg2;
     public int intarg1;
     public int intarg2;
 }
