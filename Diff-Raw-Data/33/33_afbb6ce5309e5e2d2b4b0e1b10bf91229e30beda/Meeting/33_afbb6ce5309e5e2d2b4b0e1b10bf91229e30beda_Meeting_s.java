 package sql;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import helper.Settings;
 import db.DBAccess;
 
 /**
  * For the parameters, please use DB column names
  * Example: setNickName(String bu_id, String bu_nick) 
  *          bu_id and bu_nick are both DB column names
  * <p>
  * SQL Method prefix Legend:<p>
  * 1. (get): simple query<p>
  * 2. (is): query that use SELECT 1 to check existence<p>
  * 3. (default): UPDATE statement that set targeted data back to default values<p>
  * 4. (set): normal UPDATE statement, single column<p>
  * 5. (setMul): UPDATE statement, multi column<p>
  * 6. (update): UPDATE multiple tables using MySQL Stored Procedure (SP) or complex SQL statements
  *    if the method needs to be changed, edit would like be done in SQL script: bbb_db_init.sql<p>
  * 7. (create): INSERT INTO<p>
  * 8. (remove): DELETE<p>
  * @author Kelan (Bo) Li
  *
  */
 public class Meeting extends Sql {
 
     public Meeting(DBAccess source) {
         super(source);
     }
 
     /**
      * Get data on a particular Meeting<p>
      * Fields:<p>
      * (0)ms_id (1)m_id (2)m_inidatetime (3)m_duration (4)m_iscancel (5)m_description (6)m_modpass (7)m_userpass
      * (8)m_setting(meeting)
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingInfo(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT meeting.* "
                 + "FROM meeting "
                 + "WHERE meeting.ms_id = " + ms_id + " "
                 + "AND meeting.m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get data on meetings in a particular Meeting Schedule<p>
      * Fields:<p>
      * (0)ms_id (1)m_id (2)m_inidatetime (3)m_duration (4)m_iscancel (5)m_description (6)m_modpass (7)m_userpass
      * (8)m_setting(meeting) 
      * @param result
      * @return
      */
     public boolean getMeetingInfo(ArrayList<ArrayList<String>> result, int ms_id) {
         _sql = "SELECT meeting.* "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields:<p>
      * (0)ms_id (1)ms_title (2)ms_inidatetime (3)ms_intervals (4)ms_repeats (5)ms_duration (6)bu_id
      * @param result
      * @return
      */
     public boolean getMeetingScheduleInfo(ArrayList<ArrayList<String>> result) {
         _sql = "SELECT * "
                 + "FROM meeting_schedule";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields:<p>
      * (0)ms_id (1)ms_title (2)ms_inidatetime (3)ms_intervals (4)ms_repeats (5)ms_duration (6)bu_id 
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingScheduleInfo(ArrayList<ArrayList<String>> result, int ms_id) {
         _sql = "SELECT * "
                 + "FROM meeting_schedule "
                 + "WHERE ms_id = " + ms_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_description
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingDescription(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT m_description "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_inidatetime
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingInitialDatetime(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT m_inidatetime "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_duration
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingDuration(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT m_duration "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     } 
     
     /**
      * (0)m_iscancel
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getIsMeetingCancelled(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT m_iscancel "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_modpass
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingModPass(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT m_modpass "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_userpass
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingUserPass(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT m_userpass "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields<p>
      * (0)mp_title
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingPresentation(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT mp_title "
                 + "FROM meeting_presentation "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)ma_ismod
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingAttendee(ArrayList<ArrayList<String>> result, int ms_id) {
         _sql = "SELECT bu_id, ma_ismod "
                 + "FROM meeting_attendee "
                 + "WHERE ms_id = " + ms_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)ma_ismod
      * @param result
      * @param ms_id
      * @param bu_id
      * @return
      */
     public boolean getMeetingAttendee(ArrayList<ArrayList<String>> result, int ms_id, String bu_id) {
         _sql = "SELECT bu_id, ma_ismod "
                 + "FROM meeting_attendee "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mac_isattend
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingAttendance(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT bu_id, mac_isattend "
                 + "FROM meeting_attendance "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mac_isattend
      * @param result
      * @param ms_id
      * @param bu_id
      * @return
      */
     public boolean getMeetingAttendance(ArrayList<ArrayList<String>> result, int ms_id, int m_id, String bu_id) {
         _sql = "SELECT bu_id, mac_isattend "
                 + "FROM meeting_attendance "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mg_ismod
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingGuest(ArrayList<ArrayList<String>> result, int ms_id, int m_id) {
         _sql = "SELECT bu_id, mg_ismod "
                 + "FROM meeting_guest "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mg_ismod
      * @param result
      * @param ms_id
      * @param m_id
      * @param bu_id
      * @return
      */
     public boolean getMeetingGuest(ArrayList<ArrayList<String>> result, int ms_id, int m_id, String bu_id) {
         _sql = "SELECT bu_id, mg_ismod "
                 + "FROM meeting_guest "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     public boolean getMeetingSetting(HashMap<String, Integer> result, int ms_id, int m_id) {
         _sql = "SELECT m_setting "
                 + "FROM meeting "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.meeting_setting[0], (value & (1<<6)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[1], (value & (1<<5)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[2], (value & (1<<4)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[3], (value & (1<<3)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[4], (value & (1<<2)) + (value & (1<<1)) + (value & 1));
         }
         return flag;
     }
     
     public boolean defaultMeetingSetting(int ms_id, int m_id) {
         _sql = "UPDATE meeting as a "
                 + "CROSS JOIN (SELECT key_value FROM bbb_admin WHERE key_name='default_meeting') as b "
                 + "SET a.m_setting = b.key_value "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.updateDB(_sql);   
     }
     
     public boolean setMeetingSetting(HashMap<String, Integer> map, int ms_id, int m_id) {
         int value;
         value = (map.get(Settings.meeting_setting[0]) << 6)
                 + (map.get(Settings.meeting_setting[1]) << 5)
                 + (map.get(Settings.meeting_setting[2]) << 4)
                 + (map.get(Settings.meeting_setting[3]) << 3)
                 + (map.get(Settings.meeting_setting[4]));
         _sql = "UPDATE meeting "
                 + "SET m_setting = " + value + " "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingDescription(int ms_id, int m_id, String m_description) {
         _sql = "UPDATE meeting "
                 + "SET m_description = '" + m_description + "' "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.updateDB(_sql);
     }
       
     public boolean setMeetingIsCancel(int ms_id, int m_id, boolean m_iscancel) {
         int flag = (m_iscancel == true) ? 1 : 0;
         _sql = "UPDATE meeting "
                 + "SET m_iscancel = " + flag + " "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id;
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingAttendance(String bu_id, int ms_id, int m_id, boolean mac_isattend) {
         int flag = (mac_isattend == true) ? 1 : 0;
         _sql = "UPDATE meeting_attendance "
                 + "SET mac_isattend = " + flag + " "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingGuestIsMod(String bu_id, int ms_id, int m_id, boolean mg_ismod) {
         int flag = (mg_ismod == true) ? 1 : 0;
         _sql = "UPDATE meeting_guest "
                 + "SET mg_ismod = " + flag + " "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingAttendeeIsMod(String bu_id, int ms_id, boolean ma_ismod) {
         int flag = (ma_ismod == true) ? 1 : 0;
         _sql = "UPDATE meeting_attendee "
                 + "SET ma_ismod = " + flag + " "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param num (1, 2, 3)<p>
      * (1) change the current meeting only<p>
        (2) change all sessions after and including the current one<p>
        (3) change all sessions not yet passed (reference to sysdate())
      * @param ms_id
      * @param m_id
      * @param m_duration
      * @return
      */
     public boolean updateMeetingDuration(int num, int ms_id, int m_id, int m_duration) {
         _sql = "CALL sp_update_m_duration("
                 + num + ", "
                 + ms_id + ", "
                 + m_id + ", "
                 + m_duration + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param num (1, 2, 3)<p>
      * (1) change the current meeting only<p>
        (2) change all sessions after and including the current one<p>
        (3) change all sessions not yet passed (reference to sysdate())
      * @param ms_id
      * @param m_id
      * @param time (format HH:MM:SS)
      * @return
      */
     public boolean updateMeetingTime(int num, int ms_id, int m_id, String time) {
         _sql = "CALL sp_update_m_time("
                 + num + ", "
                 + ms_id + ", "
                 + m_id + ", '"
                 + time + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param ms_id
      * @param ms_repeats
      * @return
      */
     public boolean updateMeetingRepeats(int ms_id, int ms_repeats) {
         _sql = "CALL sp_update_ms_repeats("
                 + ms_id + ", "
                 + ms_repeats + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Format of datetime must be exactly as specified<p>
      * Check to make sure datetime is greatly than current time<p>
      * If method updateMeetingTime has been to alter time of day,
      * calling this method will reset all custom changes
      * @param ms_id
      * @param datetime (format: 'YYYY-MM-DD HH:MM:SS')
      * @return
      */
     public boolean updateMeetingScheduleInitialTime(int ms_id, String datetime) {
         _sql = "CALL sp_update_ms_inidatetime("
                 + ms_id + ", '"
                 + datetime + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingPresentation(String mp_title, int ms_id, int m_id) {
         _sql = "INSERT INTO meeting_presentation VALUES ('"
                 + mp_title + "', " + ms_id + ", " + m_id + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingAttendee(String bu_id, int ms_id, boolean ma_ismod) {
         int flag = (ma_ismod == true) ? 1 : 0;
         _sql = "INSERT INTO meeting_attendee VALUES ('"
                 + bu_id + "', " + ms_id + ", " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingAttendance(String bu_id, int ms_id, int m_id, boolean mac_attend) {
         int flag = (mac_attend == true) ? 1 : 0;
         _sql = "INSERT INTO meeting_attendance VALUES ('"
                 + bu_id + "', " + ms_id + ", " + m_id + ", " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingGuest(String bu_id, int ms_id, int m_id, boolean mg_ismod) {
         int flag = (mg_ismod == true) ? 1 : 0;
         _sql = "INSERT INTO meeting_guest VALUES ('"
                 + bu_id + "', " + ms_id + ", " + m_id + ", " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingAttendance(String bu_id, int ms_id, int m_id) {
         _sql = "DELETE FROM meeting_attendance "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingAttendee(String bu_id, int ms_id) {
         _sql = "DELETE FROM meeting_attendee "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingGuest(String bu_id, int ms_id, int m_id) {
         _sql = "DELETE FROM meeting_guest "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingPresentation(String mp_title, int ms_id, int m_id) {
         _sql = "DELETE FROM meeting_presentation "
                 + "WHERE ms_id = " + ms_id + " "
                 + "AND m_id = " + m_id + " "
                 + "AND mp_title = '" + mp_title + "'";
         return _dbAccess.updateDB(_sql);
     }
 }
