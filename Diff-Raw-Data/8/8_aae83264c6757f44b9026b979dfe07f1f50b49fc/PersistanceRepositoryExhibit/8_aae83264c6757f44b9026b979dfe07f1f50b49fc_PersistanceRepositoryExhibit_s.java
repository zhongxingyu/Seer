 package persistance;
 
 import businessDomainObjects.Exhibit;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import utility.InputValidator;
 
 /**
  *
  * @author Oliver Brooks <oliver2.brooks@live.uwe.ac.uk>
  */
 public class PersistanceRepositoryExhibit {
 
     private DatabaseQueryExecutor db;
 
     public PersistanceRepositoryExhibit(DatabaseQueryExecutor db) {
         this.db = db;
     }
 
     public ArrayList<Exhibit> getAllExhibits() {
<<<<<<< HEAD
<<<<<<< HEAD
=======
        String sql = "SELECT * FROM exhibits;";
>>>>>>> branch 'master' of https://github.com/esd-soft-eng/server.git
=======
        String sql = "SELECT * FROM exhibits;";
>>>>>>> 08568e5381b548396b237a12f1021e312ffa1c44
 
         String sql = "SELECT * FROM exhibits;";
         ResultSet rs = db.executeStatement(sql);
         return mapResultSetToArrayList(rs);
     }
 
     private ArrayList<Exhibit> mapResultSetToArrayList(ResultSet rs) {
         ArrayList<Exhibit> listOfExhibits = new ArrayList<Exhibit>();
 
         try { 
             while (rs.next()) {
                 String exhibitName = rs.getString("Name");
                 String exhibitDescription = rs.getString("Description");
                 int exhibitID = rs.getInt("ExhibitID");
                 int audioLevel1ID = rs.getInt("AudioLevel1ID");
                 int audioLevel2ID = rs.getInt("AudioLevel2ID");
                 int audioLevel3ID = rs.getInt("AudioLevel3ID");
                 int audioLevel4ID = rs.getInt("AudioLevel4ID");
                 Exhibit tempExhibit = new Exhibit(exhibitID, exhibitName, exhibitDescription, audioLevel1ID, audioLevel2ID, audioLevel3ID, audioLevel4ID);
                 listOfExhibits.add(tempExhibit);
             }
 
         } catch (SQLException ex) {
             Logger.getLogger(PersistanceRepositoryUser.class.getName()).log(Level.SEVERE, null, ex);
         }
         return listOfExhibits;
     }
 
     public synchronized boolean addExhibit(String name, String description, int audioLevel1ID, int audioLevel2ID, int audioLevel3ID, int audioLevel4ID) throws SQLException {
         name = InputValidator.clean(name);
         description = InputValidator.clean(description);
         String insertionSQL = "INSERT INTO exhibits (Name, Description, AudioLevel1ID, AudioLevel2ID, AudioLevel3ID, AudioLevel4ID) VALUES ('" + name + "','" + description + "'," + audioLevel1ID + "," + audioLevel2ID + "," + "'," + audioLevel3ID + "," + audioLevel4ID + ");";
         return db.executeUpdate(insertionSQL);
     }
 
     public synchronized boolean removeExhibit(int ID) throws SQLException {
         String deletionSQL = "DELETE FROM exhibits WHERE ExhibitID=" + ID + ";";
         return db.executeUpdate(deletionSQL);
     }
 
     public synchronized boolean modifyExhibit(int ID, String name, String description, int audioLevel1ID, int audioLevel2ID, int audioLevel3ID, int audioLevel4ID) throws SQLException {
         name = InputValidator.clean(name);
         description = InputValidator.clean(description);
 
         String modificationSQL = "UPDATE exhibits SET name='" + name + "', description='" + description + "', AudioLevel1ID=" + audioLevel1ID + ", AudioLevel2ID=" + audioLevel2ID + ", AudioLevel3ID=" + audioLevel3ID + ", AudioLevel4ID=" + audioLevel4ID + " WHERE ExhibitID=" + ID + ";";
         return db.executeUpdate(modificationSQL);
     }
 }
