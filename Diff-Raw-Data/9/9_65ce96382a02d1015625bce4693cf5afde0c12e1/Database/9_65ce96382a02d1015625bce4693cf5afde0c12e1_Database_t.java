 package CS247;
 
 import java.sql.*;
 import java.net.URLDecoder;
 import java.io.File;
 
 public class Database {
 
     private Connection conn;
     
     private final String create_alerts = 
 	"CREATE TABLE IF NOT EXISTS `android_alerts` ("
     + "`alert_id` INTEGER PRIMARY KEY," 
     + "`title` VARCHAR( 500 ) NOT NULL,"
     + "`link` VARCHAR( 500 ) NOT NULL,"
     + "`description` TEXT( 10000 ) NOT NULL,"
     + "`suggestions` TEXT( 10000 ) NOT NULL,"
     + "`importance` TINYINT NOT NULL,"
     + "`time_stamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
     
     private final String create_android_devices =
     "CREATE TABLE IF NOT EXISTS `android_devices` ("
     + "`registration_id` VARCHAR( 500 ) NOT NULL"
 	+ ");";
     
     // Connect to the database.
     Database() {
         this.conn = null;
         
 		// get the path of server.jar in order to store the db in the same directory.
 		String path = Server.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 		try {
 			path = URLDecoder.decode(path, "UTF-8");
 			path = (new File(path)).getParentFile().getPath() + File.separator + "cs247.db";
 		} catch (Exception e){
 			path = "cs247.db";
 		}
 		
         // Actually connect to the database
         // you will need to download http://files.zentus.com/sqlitejdbc/sqlitejdbc-v056.jar
         // and place it in the bin/ directory.
         try {
 			Class.forName("org.sqlite.JDBC").newInstance();
 			conn = DriverManager.getConnection("jdbc:sqlite:" + path);
 			conn.setAutoCommit(true);
 			Statement stmnt = conn.createStatement();
 			stmnt.executeUpdate(create_alerts);
 			stmnt.executeUpdate(create_android_devices);
 			stmnt.close();
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("You need to get http://files.zentus.com/sqlitejdbc/sqlitejdbc-v056.jar and place it in the bin/ folder!");
         }
     }
     
     // Not sure we need this, as we want to reuse connections ?
     public void closeConnection(Connection x) {
         try {
             x.close();
         }
         
         catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /* Inserts an alert into the android_alerts table
      * @param title The title of the alert
      * @param link The link to the source of the alert
      * @param importance A integer representing how important this alert is, pass in -1 if you do not want to use this parameter
      */
     public void insertAlert(String title, String link, int importance) {
         
         PreparedStatement addAlert = null;
         
         String insertAlert = "INSERT INTO android_alerts (alert_id, title, link, importance) values(NULL, ?, ?, ?)";
         
         try {
             addAlert = conn.prepareStatement(insertAlert);
             addAlert.setString(1, title);
             addAlert.setString(2, link);
             addAlert.setInt(3, importance);
             addAlert.executeUpdate();
         }
         
         catch (Exception e) {
            e.printStackTrace();
         }
     
     }
     
     /* Inserts an alert into the android_alerts table
      * @param title The title of the alert
      * @param link The link to the source of the alert
      * @param description The main body of text about this alert.
      * @param suggestions What we think they should do based on this alert
      * @param importance A integer representing how important this alert is, pass in -1 if you do not want to use this parameter
      * @return the id of the alert that was inserted.
      */
     public void insertAlert(String title, String link, String description, String suggestions, int importance) {
         
         PreparedStatement addAlert = null;
         
         String insertAlert = "INSERT INTO android_alerts (alert_id, title, link, description, suggestions, importance) values(NULL, ?, ?, ?, ? , ?)";
         
         try {
             // can't return the value of the alert_id column with sqlite :/
             addAlert = conn.prepareStatement(insertAlert);
             addAlert.setString(1, title);
             addAlert.setString(2, link);
             addAlert.setString(3, description);
             addAlert.setString(4, suggestions);
             addAlert.setInt(5, importance);
             addAlert.executeUpdate();
             //we can select last insert id
         }
         
         catch (Exception e) {
            e.printStackTrace();
         }
         
        // return alert_id;
 
     }
     
     public boolean isAlertPresent(String link){
		boolean res = false;
 		try {
	   		PreparedStatement ps = conn.prepareStatement("SELECT alert_id FROM android_alerts WHERE link = ?");
    		ps.setString(1, link);
    		ResultSet r = ps.executeQuery();
 			res = r.next();
 			r.close();
 		} catch(Exception e){
 			res = false;
 		}
 		return res;
     }
     
     /* Gets an alert from the android_alerts table by its ID as an array
      * @param id The id of the alert you want to get
      * @return array representing part of the row returned 0 = Title, 1 = Link, 2 = Description, 3 = Suggestions
      */
    public String[] getAlertByIDAsArray(int alert_id) {
         
         PreparedStatement getAlert = null;
         ResultSet rs = null;
         String[] Results = new String[4];
         
         String alertToGet = "SELECT * FROM android_alerts WHERE alert_id = ? ";
         
         try {
             getAlert = conn.prepareStatement(alertToGet);
             getAlert.setInt(1, alert_id);
             rs = getAlert.executeQuery();
             // Move the pointer to the start of the results
             rs.next();
             Results[0] = rs.getString("title");
             Results[1] = rs.getString("link");
             Results[2] = rs.getString("description");
             Results[3] = rs.getString("suggestions");
             rs.close();
         }
         
         catch (Exception e) {
            e.printStackTrace();
         }
         
         return Results;        
 
     } 
     
     /* Gets an alert from the android_alerts table by its ID as a result set object
      * @param id The id of the alert you want to get
      * @return ResultSet representing all the rows returned 1 = Alert ID, 2 = Title, 3 = Link, 4 = Description, 5 = Suggestions
      */
     public ResultSet getAlertByIDAsResultSetObject(int alert_id) {
         
         PreparedStatement getAlert = null;
         ResultSet rs = null;
         
         String alertToGet = "SELECT * FROM android_alerts WHERE alert_id = ? ";
         
         try {
             getAlert = conn.prepareStatement(alertToGet);
             getAlert.setInt(1, alert_id);
             rs = getAlert.executeQuery();
         }
         
         catch (Exception e) {
            e.printStackTrace();
         }
         
         return rs;    
 
     }
     
     /* Gets all alerts from the android_alerts table that were created after the timestamp passed in
      * @param timestamp Timestamp in the format YYYY-MM-DD HH:MM:SS  
      * @return ResultSet representing the rows returned 1 = Alert ID, 2 = Title, 3 = Link, 4 = Description, 5 = Suggestions
      */
     public ResultSet getAllAlertsSinceXAsResultSetObject(String timestamp) {
         
         PreparedStatement getAlerts = null;
         ResultSet rs = null;
         
         String alertsToGet = "SELECT * FROM android_alerts WHERE time_stamp >= ? ";
         
         try {
             getAlerts = conn.prepareStatement(alertsToGet);
             getAlerts.setString(1, timestamp);
             rs = getAlerts.executeQuery();
         }
         
         catch (Exception e) {
            e.printStackTrace();
         }
         
         return rs;    
 
     }
     
     public boolean insertRegistrationID(String reg)
     {
         PreparedStatement addID = null;
         try
         {
             addID = conn.prepareStatement("INSERT INTO android_devices (registration_id) values (?)");
             addID.setString(1, reg);
             addID.executeUpdate();
         }
         catch (Exception e)
         {return false;}
         
         return true;
     }
     
     public boolean removeRegistrationID(String reg)
     {
         PreparedStatement delID = null;
         try
         {
             delID = conn.prepareStatement("DELETE FROM android_devices WHERE `registration_id` = ?");
             delID.setString(1, reg);
             delID.executeUpdate();
         }
         catch (Exception e)
         {return false;}
         
         return true;
     }
 }
