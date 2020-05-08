 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package trampoline;
 import java.sql.*;
 
 /**
  *
  * @author Andreas
  */
 public class DBConnect {
     Connection conn_;
     Statement stat_;
     ResultSet rs_;
     
     DBConnect() {
         try {
            Class.forName("SQLite.JDBCDriver");

             // connect to the database
            Connection conn_ = DriverManager.getConnection("jdbc:data/database");
             Statement stat_ = conn_.createStatement();
 
             ResultSet rs_ = stat_.executeQuery("SELECT * FROM jumps");
 
             while (rs_.next()) {
                 System.out.println("word = " + rs_.getString("jid"));
             }
 
             
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     boolean addClub(String name) {
         return executeQuery("INSERT INTO clubs (name) VALUES ('"+name+"')");
     }
     
     boolean addGymnast(int cid, String name) {
         return executeQuery("INSERT INTO gymnasts (cid, name) VALUES ('"+cid+"', '"+name+"')");
     }
     
     boolean addJump(int routineid, int jumpnumber, double b1, double en, double b2, double tof, double ton, double total, String location) {
         return executeQuery("INSERT INTO jumps (routineid, jumpnumber, break1, engage, break2, tof, ton, total, location) "
                 + "VALUES ('"+routineid+"', '"+jumpnumber+"', '"+b1+"', '"+en+"', '"+b2+"', '"+tof+"', '"+ton+"', '"+total+"', '"+location+"')");
     }
     
     boolean addRoutine(Routine r, int gid, String datetime) {
         return executeQuery("INSERT INTO routines (gymnastid, totaltof, totalton, totaltime, datetime, numberofjumps) "
                 + "VALUES ('"+gid+"', '"+r.getTotalTof()+"', '"+r.getTotalTon()+"', '"+r.getTotalTime()+"', '"+datetime+"', '"+r.getNumberOfJumps()+"')");
     }
     
     Jump getJump(int jid) {
         executeQuery("SELECT * FROM jumps WHERE jid = '"+jid+"'");
         
         return new Jump(resultGetInt("break1"), resultGetInt("engage"), resultGetInt("break2"));
     }
     
     //If we know we've already got the jump row loaded into "rs_" then use this. 
     Jump getJump() {
         return new Jump(resultGetInt("break1"), resultGetInt("engage"), resultGetInt("break2"));
     }
     
     Routine getRoutine(int rid) {
         executeQuery("SELECT * FROM routine WHERE rid = '"+rid+"'");
         int numberOfJumps = resultGetInt("numberofjumps");
 
         Routine r = new Routine(numberOfJumps);
         executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"' ORDER BY jumpnumber ASC");
         
         try {
             while (rs_.next()) {
                 r.addJump(getJump());
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         
         return r;
     }
     
     //We run this, then just use the class variable "rs_" to get our result set. 
     boolean executeQuery(String s) {
         try {
             ResultSet rs_ = stat_.executeQuery(s);
         }
 
         catch (Exception e) {
             e.printStackTrace();
         }
 
         return true;
     }
     
     int resultGetInt(String recordName) {
         int i = 0;
         
         try {
             i = rs_.getInt(recordName);
         }
 
         catch (Exception e) {
             e.printStackTrace();
         }
 
         return i;
     }
 }
