 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vgr;
 
 import java.sql.*;
 
 /**
  *
 * @author Adam Abenius & Emil Bengtsson
  */
 public class VGR {
 
     static Connection conn;
     static Statement stmt;
     public static void main(String[] args) throws SQLException {
         dbAnslut();
         ResultSet r = stmt.executeQuery("SELECT * FROM data WHERE modality == 'CR';");
         while (r.next()){
             System.out.println("TJENA");
             System.out.println("ID: " + r.getInt("Patient_ID"));
             System.out.println("Namn: " + r.getString("Patient_Name"));
         }
     }
     
     public static void dbAnslut() throws SQLException{
         boolean adam = false;
         //setup
         try {
 			Class.forName("org.sqlite.JDBC");
 			System.out.print("Ansluter till databas...");
 			conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Emil/Dropbox/VGR/Teknik/vgrdataDB.sqlite");
                         //conn = DriverManager.getConnection("jdbc:sqlite:vgrdataDB.sqlite");
                         System.out.println("  OK /db\n");
 			stmt = conn.createStatement();
 
 		} catch (ClassNotFoundException e) {
                         System.out.println("Drivrutinen kunde inte laddas");
 		} catch (SQLException e) {
                         adam = true;
 			System.out.println("Databasen kunde inte anslutas till, provar adams path");
 		}
         if(adam)
             try {
 			Class.forName("org.sqlite.JDBC");
 			System.out.print("Ansluter till databas...");
 			conn = DriverManager.getConnection("jdbc:sqlite:/Users/AdamAbenius/Dropbox/VGR/Teknik/vgrdataDB.sqlite");
                         //conn = DriverManager.getConnection("jdbc:sqlite:vgrdataDB.sqlite");
                         System.out.println("  OK /db\n");
 			stmt = conn.createStatement();
 
 		} catch (ClassNotFoundException e) {
 			adam = true;
                         System.out.println("Drivrutinen kunde inte laddas.");
 		} catch (SQLException e) {
			System.out.println("Databasen kunde inte anslutas till (adams path).");
 
             }
       }
 }
