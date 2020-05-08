 package test;
 
 import sql.*;
 
 import db.DBAccess;
 
 public class Test_Meeting extends Test_Helper {
     static Meeting _meeting = null;
 
     public Test_Meeting(DBAccess source) {
         super();
         _meeting = new Meeting(source);
         _test = _meeting;
         _counter = 1;
         
         _test.resetErrorFlag();
 
         display(_meeting.getMeetingInfo(_result, 1, 1));
 
         display(_meeting.getMeetingDescription(_result, 1, 1));
 
         display(_meeting.getMeetingInitialDatetime(_result, 1, 1));
 
         display(_meeting.getMeetingDuration(_result, 1, 1));
 
         display(_meeting.getMeetingModPass(_result, 1, 1));
 
         display(_meeting.getMeetingUserPass(_result, 1, 1));
 
         display(_meeting.getIsMeetingCancelled(_result, 1, 1));
         
         display(_meeting.getMeetingPresentation(_result, 1, 1));
         
         display(_meeting.getMeetingAttendee(_result, 1));
         
         display(_meeting.getMeetingAttendance(_result, 1, 1));
         
         display(_meeting.getMeetingSetting(_hm, 1, 1));
         
         display(_meeting.getMeetingSetting(_hm, 1, 2));
         
         display(_meeting.setMeetingSetting(_meeting_map, 1, 1));
         
         display(_meeting.getMeetingSetting(_hm, 1, 1));
         
         display(_meeting.defaultMeetingSetting(1, 1));
         
         display(_meeting.getMeetingSetting(_hm, 1, 1));
         
         display(_meeting.getMeetingAttendee(_result, 1, "non_ldap2"));
         
         display(_meeting.getMeetingAttendance(_result, 1, 1, "bli64"));
         
         display(_meeting.getMeetingGuest(_result, 1, 1));
         
         display(_meeting.getMeetingGuest(_result, 1, 1, "fardad.soleimanloo"));
         
         display(_meeting.setMeetingDescription(1, 1, "Test"));
         
         display(_meeting.setMeetingIsCancel(1, 1, true));
         
         display(_meeting.setMeetingAttendance("capilkey", 2, 1, true));
         
         display(_meeting.setMeetingAttendeeIsMod("capilkey", 1, true));
         
         display(_meeting.setMeetingGuestIsMod("fardad.soleimanloo", 2, 1, false));
         
         //display(_meeting.createMeetingPresentation("mp_title1", 1, 5));
         
         //display(_meeting.createMeetingGuest("bli64", 1, 2, true));
         
         //display(_meeting.createMeetingGuest("bo.li", 1, 2, false));
         
         //display(_meeting.createMeetingAttendee("bli64", 2, true));
         
         //display(_meeting.createMeetingAttendee("bo.li", 2, false));
         
         //display(_meeting.createMeetingAttendance("bo.li", 1, 1, true));
         
         //display(_meeting.createMeetingAttendance("xdeng7", 1, 1, false));
         
         //display(_meeting.removeMeetingAttendance("bli64", 1, 1));
         
         //display(_meeting.removeMeetingAttendee("bli64", 1));
         
         //display(_meeting.removeMeetingGuest("bli64", 1, 1));
         
         //display(_meeting.removeMeetingPresentation("mp_title", 1, 1));
         
         display(_meeting.updateMeetingDuration(3, 1, 8, 150));
         
         display(_meeting.updateMeetingTime(3, 1, 8, "05:05:05"));
         
         display(_meeting.updateMeetingScheduleInitialTime(1, "2013-08-01 10:20:30"));
     }
 }
