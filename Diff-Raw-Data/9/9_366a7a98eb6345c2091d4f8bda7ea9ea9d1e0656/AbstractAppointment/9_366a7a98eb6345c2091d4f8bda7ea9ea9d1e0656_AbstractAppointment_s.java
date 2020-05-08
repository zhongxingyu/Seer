 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author lycantrophe
  */
 public abstract class AbstractAppointment implements Appointment {
 
     protected Date start;
     protected Date end;
     protected Person owner;
     protected String description;
     protected Location location;
     protected ArrayList<String> participants;
 
     protected AbstractAppointment(Person owner, Date start, Date end, String description, ArrayList<String> participants) {
         this.owner = owner;
         this.start = start;
         this.end = end;
         this.description = description;
         this.participants = participants;
     }
 
     public void updateAppointment(Appointment newAppointment) {
         if (newAppointment.getStart() != null) {
             start = newAppointment.getStart();
         }
         if (newAppointment.getEnd() != null) {
             end = newAppointment.getEnd();
         }
         if (newAppointment.getOwner() != null) {
             owner = newAppointment.getOwner();
         }
         if (newAppointment.getDescription() != null) {
             description = newAppointment.getDescription();
         }
         if (newAppointment.getLocation() != null) {
             location = newAppointment.getLocation();
         }
         if (newAppointment.getParticipants() != null) {
             participants = newAppointment.getParticipants();
         }
     }
 
     protected void updateSQLAppointment(Appointment appointment) throws SQLException {
 
         Connection con;
         PreparedStatement sql = null;
 
         try {
             con = DriverManager.getConnection(
                     "jdbc:default:connection");
 
             sql = con.prepareStatement("UPDATE appointment SET start=?, end=?, owner=?, description=?, setLocation=?");
             sql.setDate(1, new java.sql.Date(appointment.getStart().getTime()));
             sql.setDate(2, new java.sql.Date(appointment.getEnd().getTime()));
             sql.setString(3, appointment.getOwner().getUsername());
             sql.setString(4, appointment.getDescription());
         } finally {
             if (sql != null) {
                 sql.close();
             }
         }
     }
 
     public Date getStart() {
         return start;
     }
 
     public Date getEnd() {
         return end;
     }
 
     public Person getOwner() {
         return owner;
     }
 
     public String getDescription() {
         return description;
     }
 
     public Location getLocation() {
         return location;
     }
 
     public ArrayList<String> getParticipants() {
         return participants;
     }
 }
