 package edu.osu.picola.dataobjects;
 
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 /**
  * This class models the assignments
  *
  * @author akers.79
  */
 public class Assignment implements Serializable {
 
     private int assignment_id;
     private String assignment_descr;
     private Timestamp individual_start_date;
     private Timestamp individual_end_date;
     private Timestamp BP_start_date;
     private Timestamp BP_end_date;
     private Timestamp MP_start_date;
     private Timestamp MP_end_date;
     private int user_id;
     private int course_id;
     private int assignment_number;
     private String assignment_name;
 
     public Assignment(ResultSet rs) {
         try {
             if (rs.isBeforeFirst()) {
                 rs.next();
             }
             assignment_id = rs.getInt("assignment_id");
             System.out.println("Assignment ID: " + assignment_id);
             assignment_descr = rs.getString("assignment_descr");
             individual_start_date = rs.getTimestamp("individual_start_date");
             individual_end_date = rs.getTimestamp("individual_end_date");
             BP_start_date = rs.getTimestamp("BP_start_date");
             BP_end_date = rs.getTimestamp("BP_end_date");
             MP_start_date = rs.getTimestamp("MP_start_date");
             MP_end_date = rs.getTimestamp("MP_end_date");
             user_id = rs.getInt("user_id");
             course_id = rs.getInt("course_id");
             assignment_number = rs.getInt("assignment_number");
             assignment_name = rs.getString("assignment_name");
         } catch (SQLException e) {
             System.out.println("Failed to load Assignment");
             e.printStackTrace();
         }
     }
 
     public Assignment(String assignment_descr,
             Timestamp individual_start_date, Timestamp individual_end_date,
             Timestamp BP_start_date, Timestamp BP_end_date,
             Timestamp MP_start_date, Timestamp MP_end_date,
             int user_id, int course_id, int assignment_number, String assignment_name) {
         this.assignment_id = -1;//negative one means assignment is not in the database.
         this.assignment_descr = assignment_descr;
         this.individual_start_date = individual_start_date;
         this.individual_end_date = individual_end_date;
         this.BP_start_date = BP_start_date;
         this.BP_end_date = BP_end_date;
         this.MP_start_date = MP_start_date;
         this.MP_end_date = MP_end_date;
         this.user_id = user_id;
         this.course_id = course_id;
         this.assignment_number = assignment_number;
     }
 
     public int getAssignment_number() {
         return assignment_number;
    }

     public void setAssignment_number(int assignment_number) {
         this.assignment_number = assignment_number;
     }
 
     public int getAssignment_id() {
         return assignment_id;
     }
 
     public void setAssignment_id(int assignment_id) {
         this.assignment_id = assignment_id;
     }
 
     public String getAssignment_descr() {
         return assignment_descr;
     }
 
     public void setAssignment_descr(String assignment_descr) {
         this.assignment_descr = assignment_descr;
     }
 
     public Timestamp getIndividual_start_date() {
         return individual_start_date;
     }
 
     public void setIndividual_start_date(Timestamp individual_start_date) {
         this.individual_start_date = individual_start_date;
     }
 
     public Timestamp getIndividual_end_date() {
         return individual_end_date;
     }
 
     public void setIndividual_end_date(Timestamp individual_end_date) {
         this.individual_end_date = individual_end_date;
     }
 
     public Timestamp getBP_start_date() {
         return BP_start_date;
     }
 
     public void setBP_start_date(Timestamp bP_start_date) {
         BP_start_date = bP_start_date;
     }
 
     public Timestamp getBP_end_date() {
         return BP_end_date;
     }
 
     public void setBP_end_date(Timestamp bP_end_date) {
         BP_end_date = bP_end_date;
     }
 
     public Timestamp getMP_start_date() {
         return MP_start_date;
     }
 
     public void setMP_start_date(Timestamp mP_start_date) {
         MP_start_date = mP_start_date;
     }
 
     public Timestamp getMP_end_date() {
         return MP_end_date;
     }
 
     public void setMP_end_date(Timestamp mP_end_date) {
         MP_end_date = mP_end_date;
     }
 
     public int getUser_id() {
         return user_id;
     }
 
     public void setUser_id(int user_id) {
         this.user_id = user_id;
     }
 
     public int getCourse_id() {
         return course_id;
     }
 
     public void setCourse_id(int course_id) {
         this.course_id = course_id;
     }
 
     public String getAssignment_name() {
         return assignment_name;
     }
 
     public void setAssignment_name(String assignment_name) {
         this.assignment_name = assignment_name;
     }
 }
