 package se.umu.cs.umume.persistance;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import se.umu.cs.umume.PersonBean;
 
 public class PersistanceLayer {
 
     private static Logger logger = LoggerFactory.getLogger(PersistanceLayer.class);
     private static String databasePath = "jdbc:sqlite:/" + PersistanceLayer.class.getResource("/thebrain.rsd").getFile();
 
     //    public static void testURI() {
     //        logger.info("DB: " + databasePath);
     //        logger.info("DB: " + PersistanceLayer.class.getResource("/thebrain.rsd"));
     //        logger.info("DB: " + PersistanceLayer.class.getResource("/thebrain.rsd").getPath());
     //        logger.info("DB: " + PersistanceLayer.class.getResource("/thebrain.rsd").getFile());
     //    }
 
     public static void addDatabaseInfo(List<PersonBean> persons) {
         try {
             Class.forName("org.sqlite.JDBC");
             Connection conn = DriverManager.getConnection(databasePath);
             Statement stat = conn.createStatement();
             
             for (PersonBean person : persons) {
                 ResultSet rs = stat
                 .executeQuery("select * from Persons where UserName = \""
                         + person.getUid() + "\"");
                 while (rs.next()) {
                     person.setDescription(rs.getString("Description"));
                     person.setTwitterName(rs.getString("TwitterName"));
                     person.setLongitude(rs.getDouble("Longitude"));
                     person.setLatitude(rs.getDouble("Latitude"));
                 }
                 rs.close();
             }
             conn.close();
         } catch (SQLException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     public static void updateInfo(PersonBean person) {
         try {
             Class.forName("org.sqlite.JDBC");
             Connection conn = DriverManager.getConnection(databasePath);
             Statement stat = conn.createStatement();
             ResultSet rs = stat
             .executeQuery("select * from Persons where UserName = '"
                     + person.getUid() + "'");
             if (rs.next()) {
                 
                 String sql = "UPDATE Persons SET "
                     + "TwitterName = '"
                     + person.getTwitterName() + "', "
                     + "Longitude = "
                     + person.getLongitude() + ", "
                     + "Latitude = "
                     + person.getLatitude() + ", "
                     + "Description = '"
                     + person.getDescription() + "'"
                     + " WHERE UserName = '" + person.getUid() + "'";
                 conn.createStatement().executeUpdate(sql);
                 /*
                 String sql = "UPDATE Persons SET "
                     + "TwitterName = ?, "
                     + "Longitude = ?, "
                     + "Latitude = ?,"
                     + "Description = ?"
                     + " WHERE UserName = '" + person.getUid() + "'";
                 
                 PreparedStatement prepStmt = conn.prepareStatement(sql);
                 prepStmt.setString(1, person.getTwitterName());
                 prepStmt.setLong(2, (long) person.getLongitude());
                 prepStmt.setLong(3, (long) person.getLatitude());
                 prepStmt.setString(4, person.getDescription());
 
                 logger.info("Running query: {}", sql);
                 
                 prepStmt.executeUpdate();
                 */
             } else {
                 /*
                 String sql = "INSERT INTO Persons " 
                     + "(UserName, TwitterName, Longitude, Latitude, Description) VALUES ("
                     + "'"+ person.getUid() + "',"  
                     + "'"+ person.getTwitterName() + "',"
                     + "'"+ person.getLongitude() + "'," 
                     + "'"+ person.getLatitude() + "'," 
                     + "'" + person.getDescription() + "')";
                 */
                 String sql = "INSERT INTO Persons " 
                    + "(UserName, TwitterName, Longitude, Latitude, Description) VALUES ("
                     + "(?,?,?,?,?)";
                 
                 PreparedStatement prepStmt = conn.prepareStatement(sql);
                 prepStmt.setString(1, person.getTwitterName());
                 prepStmt.setDouble(2, person.getLongitude());
                 prepStmt.setDouble(3, person.getLatitude());
                 prepStmt.setString(4, person.getDescription());
 
                logger.info("Running query: {}", sql);
                 
                 prepStmt.executeUpdate();
                 
                 logger.info("Doing sql: {}", sql);
                 //stat.executeUpdate(sql);
             }
             rs.close();
             conn.close();
         } catch (SQLException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         // TODO: Close connection and resultset
     }
 }
