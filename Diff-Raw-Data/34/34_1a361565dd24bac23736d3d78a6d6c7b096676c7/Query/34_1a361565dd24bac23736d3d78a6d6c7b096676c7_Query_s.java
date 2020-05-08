 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author lycantrophe
  */
 public class Query {
 
     private Connection con;
     PreparedStatement statement;
 
     public Query() {
         try {
             con = DriverManager.getConnection(
                     "jdbc:default:connection");
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void updateAppointment(Appointment appointment, ArrayList<Person> newParticipants, ArrayList<Person> oldParticipants) {
 
         try {
             statement = con.prepareStatement("UPDATE appointment SET start=?, end=?, owner=?, description=?, setLocation=?");
             statement.setDate(1, new java.sql.Date(appointment.getStart().getTime()));
             statement.setDate(2, new java.sql.Date(appointment.getEnd().getTime()));
             statement.setString(3, appointment.getOwner().getUsername());
             statement.setString(4, appointment.getDescription());
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
 
         addAppointment(appointment, newParticipants);
 
         try {
             statement = con.prepareStatement("DELETE FROM appointmentRel WHERE username=? AND appointmentid=? )");
 
             for (Person other : oldParticipants) {
                 statement.setString(1, other.getUsername());
                 statement.setString(2, appointment.getId());
                 statement.executeUpdate();
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void deleteAppointment(Appointment appointment) {
         try {
             statement = con.prepareStatement("DELETE FROM appointmentRel WHERE  appointmentid = ?");
             statement.setString(1, appointment.getId());
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
     }
 
     public void updateStatus(String status, Appointment appointment, Person person) {
 
         try {
            statement = con.prepareStatement("UPDATE appointmentRel SET status = ? WHERE appointmentId = ? AND username = ? ");
             statement.setString(1, status);
             statement.setString(2, appointment.getId());
            statement.setString(3, person.getUsername());
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void addAppointment(Appointment appointment, ArrayList<Person> persons) {
 
         try {
             con.prepareStatement("INSERT INTO appointmentRel (username, appointmentid, status ) VALUES ( ?, ?, ?)");
 
             for (Person other : persons) {
                 statement.setString(1, other.getUsername());
                 statement.setString(2, appointment.getId());
                 statement.setString(3, "PENDING");
                 statement.executeUpdate();
             }
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
     }
 
     public boolean authorize(String username, String password) {
 
         try {
             statement = con.prepareStatement("SELECT COUNT(*) FROM users WHERE username=? AND password=?");
             statement.setString(1, username);
             statement.setString(2, password);
             ResultSet rs = statement.executeQuery();
             return rs.getInt("COUNT") == 1 ? true : false;
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         return false;
     }
 
     public ArrayList<Person> getPersons() {
         ArrayList<Person> persons = new ArrayList<Person>();
         try {
             statement = con.prepareStatement("SELECT * FROM persons");
             ResultSet rs = statement.executeQuery();
             while (rs.next()) {
                 persons.add(new Person(rs.getString("Username"), rs.getString("Firstname"), rs.getString("Surname"), rs.getString("Email"), rs.getString("Phonenumber")));
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         return persons;
     }
 
     public void createAppointments(Map<String, Person> persons, Map<String, Location> locations) {
 
        HashMap<String, ArrayList<Person>> appointments = new HashMap<String, ArrayList<Person>>();
         try {
             // Get the relations
             statement = con.prepareStatement("SELECT * FROM appointmentRel");
             ResultSet rs = statement.executeQuery();
             // Builds the map of all the appointment participants
             while (rs.next()) {
                 String key = rs.getString("ID");
                 if (!appointments.containsKey(key)) {
                    appointments.put(key, new ArrayList<Person>());
                 }
                appointments.get(key).add(persons.get(key));
                
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         // Starts the work
         try {
             statement = con.prepareStatement("SELECT * FROM appointments");
             ResultSet rs = statement.executeQuery();
 
             while (rs.next()) {
                 // Builds the participant arraylist
                 ArrayList<String> participants = new ArrayList<String>(Arrays.asList(rs.getString("Participants").split(",")));
 
                 String id = rs.getString("ID");
                 Person owner = persons.get(rs.getString("Owner"));
 
                 // Constructs the appointment
                 Appointment appointment = new AppointmentImpl(owner, rs.getDate("Start"), rs.getDate("End"), rs.getString("Description"), appointments.get(id), participants, locations.get(rs.getString("Location")));
 
                 // Adds this appointment to everyone invited
                for (Person person : appointments.get(id)) {
                    person.addAppointment(appointment);
                 }
             }
 
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public ArrayList<Location> getLocations() {
 
         ArrayList<Location> locations = new ArrayList<Location>();
         try {
             statement = con.prepareStatement("SELECT * FROM Locations");
             ResultSet rs = statement.executeQuery();
             // Send in hash or arraylist to map up persons?
             while (rs.next()) {
                 if (rs.getString("Type") != null) {
                     locations.add(new Room(rs.getInt("ID"), rs.getString("Name"), rs.getInt("Capacity"), AbstractLocation.Roomtype.valueOf(rs.getString("Type"))));
                 } else {
                     locations.add(new OtherLocation(rs.getString("Name")));
                 }
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         return locations;
     }
 
     public void close() {
         try {
             con.close();
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 }
