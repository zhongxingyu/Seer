 /**
  * 
  */
 package org.nttext.commentary.persist.mysql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 
 import org.apache.log4j.Logger;
 import org.idch.bible.ref.Passage;
 import org.idch.bible.ref.VerseRange;
 import org.nttext.commentary.EntryInstance;
 import org.nttext.commentary.InstanceRepository;
 import org.nttext.commentary.VariationUnit;
 
 /**
  * @author Neal Audenaert
  */
 public class MySQLEntryInstanceRepository implements InstanceRepository {
     private static final Logger LOGGER = Logger.getLogger(MySQLEntryInstanceRepository.class);
 
     private static final int PASSAGE = 1;
     private static final int OVERVIEW = 2;
     private static final int CREATED = 3;
     private static final int MODIFIED = 4;
     
     private static final int INSTANCE_ID = 5;
     
     private static final String FIELDS =  
             "passage, overview, date_created, last_updated "; 
     
     private MySQLCommentaryModule repo = null;
     
     MySQLEntryInstanceRepository(MySQLCommentaryModule repo) {
         this.repo = repo;
     }
     
     /* (non-Javadoc)
      */
     @Override
     public EntryInstance create(Passage passage) {
         return this.create(new EntryInstance(passage));
     }
    
     /**
      * 
      * @param instance
      * @return
      */
     public EntryInstance create(EntryInstance instance) {
         assert (instance.getId() == null) : "This instance has already been created.";
         if (instance.getId() != null)
             return null;
         
         String sql = "INSERT INTO NTTEXTComm_Instances (" + FIELDS +") " +
                      "VALUES (?, ?, now(), now())";
         Connection conn = null;
         try {
             // build the statement
             conn = repo.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, 
                     PreparedStatement.RETURN_GENERATED_KEYS);
             
             stmt.setString(PASSAGE, instance.getPassage().toString());
             stmt.setString(OVERVIEW, instance.getOverview());
            
             // execute the query
             int numRowsChanged = stmt.executeUpdate();
             ResultSet results = stmt.getGeneratedKeys();
             if (numRowsChanged == 1 && results.next()) {
                 long id = results.getLong(1);
                 instance.setId(id);
             } else {
                 instance = null;
             }
             
             conn.commit();
         } catch (Exception ex) {
             repo.rollbackConnection(conn);
             
             String msg = "Could not create instance: " + instance.getPassage() + ". " + ex.getMessage();
             LOGGER.warn(msg, ex);
             instance = null;
         } finally {
             repo.closeConnection(conn);
         }
         
         return instance;
     }
 
     private EntryInstance restore(ResultSet results) throws SQLException {
         EntryInstance instance = null;
         
         long id = results.getLong(INSTANCE_ID);
         // TODO add caching
         String ref = results.getString(PASSAGE);
         String overview = results.getString(OVERVIEW);
         Date created = results.getDate(CREATED);
         Date modified = results.getDate(MODIFIED);
         
         Passage passage = new VerseRange(ref);
         instance = new EntryInstance(id, passage, overview, created, modified);
         
         Connection conn = results.getStatement().getConnection();
         Set<VariationUnit> variationUnits = this.getVU(conn, instance);
         instance.setVariationUnits(variationUnits);
         return instance;
     }
     
     
     /* (non-Javadoc)
      */
     @Override
     public EntryInstance find(long id) {
         String sql = "SELECT " + FIELDS + ", instance_id " +
                      "  FROM NTTEXTComm_Instances " +
         		     " WHERE instance_id = ?";
         
         EntryInstance instance = null;;
         Connection conn = null;
         try {
             conn = repo.openReadOnlyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             stmt.setLong(1, id);
             
             ResultSet results = stmt.executeQuery();
             if (results.next())
                 instance = restore(results);
         } catch (Exception ex) {
             String msg = "Could not retrieve instance (" + id + "): " + ex.getMessage();
             LOGGER.warn(msg, ex);
             instance = null;
         } finally {
             repo.closeConnection(conn);
         }
         
         return instance;
     }
     
     
     
     /* (non-Javadoc)
      */
     @Override
     public EntryInstance find(Passage passage) {
         // TODO this will need to be reworked when we allow multiple instances per Entry
         String sql = "SELECT " + FIELDS + ", instance_id " +
                      "  FROM NTTEXTComm_Instances " +
                      " WHERE passage = ?";
        
         EntryInstance instance = null;;
         Connection conn = null;
         try {
             conn = repo.openReadOnlyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             stmt.setString(1, passage.toString());
             
             ResultSet results = stmt.executeQuery();
             if (results.next())
                 instance = restore(results);
         } catch (Exception ex) {
             String msg = "Could not retrieve instance (" + passage + "): " + ex.getMessage();
             LOGGER.warn(msg, ex);
             instance = null;
         } finally {
             repo.closeConnection(conn);
         }
        
         return instance;
     }
 
     /* (non-Javadoc)
      */
     @Override
     public boolean save(EntryInstance instance) {
         assert (instance.getId() != null) : "This instance has not been created.";
         if (instance.getId() == null)
             return false;
         
         int ID = 2, OVERVIEW = 1;
         String sql = "UPDATE NTTEXTComm_Instances SET " +
         		     "  overview = ?, " +
         		     "  last_updated = now()" +
         		     "WHERE instance_id = ?";
         
         boolean success = false;
         Connection conn = null;
         try {
             // build the statement
             conn = repo.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             stmt.setString(OVERVIEW, instance.getOverview());
             stmt.setLong(ID, instance.getId());
            
             // execute the query
             int numRowsChanged = stmt.executeUpdate();
             success = (numRowsChanged == 1);
             if (numRowsChanged > 1) {
                 LOGGER.warn("Bizarre number of rows changed (" + numRowsChanged + ") " + 
                             "while saving an instance (" + instance.getId() + "). Expected 1.");
                 repo.rollbackConnection(conn);
             } else {
                 conn.commit();
             }
         } catch (Exception ex) {
             repo.rollbackConnection(conn);
             
             String msg = "Could not create instance: " + instance.getPassage() + ". " + ex.getMessage();
             LOGGER.warn(msg, ex);
             success = false;
         } finally {
             repo.closeConnection(conn);
         }
         
         return success;
     }
 
     /* (non-Javadoc)
      */
     @Override
     public boolean remove(EntryInstance instance) {
         assert (instance.getId() != null) : "This instance has not been created.";
         if (instance.getId() == null)
             return false;
         
         int ID = 1;
         String sql = "DELETE FROM NTTEXTComm_Instances " +
                      " WHERE instance_id = ?";
         
         boolean success = false;
         Connection conn = null;
         try {
             // build the statement
             conn = repo.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             stmt.setLong(ID, instance.getId());
            
             // execute the query
             int numRowsChanged = stmt.executeUpdate();
             success = (numRowsChanged == 1);
             if (numRowsChanged > 1) {
                 LOGGER.warn("Bizarre number of rows changed (" + numRowsChanged + ") " + 
                             "while removing an instance (" + instance.getId() + "). Expected 1.");
                 repo.rollbackConnection(conn);
             } else {
                 conn.commit();
             }
         } catch (Exception ex) {
             repo.rollbackConnection(conn);
             
             String msg = "Could not remove instance: " + instance.getPassage() + ". " + ex.getMessage();
             LOGGER.warn(msg, ex);
             success = false;
         } finally {
             repo.closeConnection(conn);
         }
         
         return success;
     }
 
     /* (non-Javadoc)
      */
     @Override
     public boolean associate(EntryInstance instance, VariationUnit vu) {
         int INSTANCE_ID = 1, VU_ID = 2;
         String sql = "INSERT INTO NTTEXTComm_EntryVUs (instance_id, vu_id)" +
         		     "VALUES (?, ?)";
         
         boolean success = false;
         Connection conn = null;
         try {
             // build the statement
             conn = repo.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             stmt.setLong(INSTANCE_ID, instance.getId());
             stmt.setLong(VU_ID, vu.getId());
            
             // execute the query
             int numRowsChanged = stmt.executeUpdate();
             success = numRowsChanged == 1;
             if (numRowsChanged > 1) {
                 LOGGER.warn("Bizarre number of rows changed (" + numRowsChanged + ") " + 
                             "while associating a variation unit (" + vu.getId() + ") " +
                             "with and instance (" + instance.getId() + "). Expected 1.");
                 repo.rollbackConnection(conn);
             } else {
                 if (success) instance.addVariationUnit(vu);
                 conn.commit();
             }
         } catch (Exception ex) {
             repo.rollbackConnection(conn);
             
            String msg = "Could not associate a variation unit (" + vu.getId() + ") " +
                         "with an instance (" + instance.getId() + "): " + ex.getMessage();
             LOGGER.warn(msg, ex);
             instance = null;
         } finally {
             repo.closeConnection(conn);
         }
         
         return success;
     }
 
     /* (non-Javadoc)
      */
     @Override
     public boolean disassociate(EntryInstance instance, VariationUnit vu) {
         int INSTANCE_ID = 1, VU_ID = 2;
         String sql = "DELETE FROM NTTEXTComm_EntryVUs " +
         		     " WHERE instance_id = ? AND vu_id = ?";
         
         boolean success = false;
         Connection conn = null;
         try {
             // build the statement
             conn = repo.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             
             stmt.setLong(INSTANCE_ID, instance.getId());
             stmt.setLong(VU_ID, vu.getId());
            
             // execute the query
             int numRowsChanged = stmt.executeUpdate();
             success = numRowsChanged == 1;
             if (numRowsChanged > 1) {
                 LOGGER.warn("Bizarre number of rows changed (" + numRowsChanged + ") " + 
                             "while disassociating a variation unit (" + vu.getId() + ") " +
                             "with and instance (" + instance.getId() + "). Expected 1.");
                 repo.rollbackConnection(conn);
             } else {
                 if (success) instance.removeVariationUnit(vu);
                 conn.commit();
             }
         } catch (Exception ex) {
             repo.rollbackConnection(conn);
             
             String msg = "Could not disassociate a variation unit (" + vu.getId() + ") " +
                          "with an instance (" + instance.getId() + "): " + ex.getMessage();
             LOGGER.warn(msg, ex);
             instance = null;
         } finally {
             repo.closeConnection(conn);
         }
         
         return success;
     }
     
     Set<VariationUnit> getVU(Connection conn, EntryInstance instance) throws SQLException {
         int INSTANCE_ID = 1, VU_ID = 1;
         String sql = "SELECT vu_id FROM NTTEXTComm_EntryVUs WHERE instance_id = ? ";
         
         MySQLVariationUnitRepository vuRepo = 
                 (MySQLVariationUnitRepository)repo.getVURepository();
         Set<VariationUnit> VUs = new HashSet<VariationUnit>();
         
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setLong(INSTANCE_ID, instance.getId());
         
         ResultSet results = stmt.executeQuery();
         while (results.next()) {
             long vuId = results.getLong(VU_ID);
             VUs.add(vuRepo.find(conn, vuId));
         }
        
         return VUs;
     }
 
     /* (non-Javadoc)
      */
     @Override
     public Set<VariationUnit> getVU(EntryInstance instance) {
         Set<VariationUnit> VUs = new HashSet<VariationUnit>();
         Connection conn = null;
         try {
             conn = repo.openReadOnlyConnection();
             VUs = getVU(conn, instance);
             
         } catch (Exception ex) {
             String msg = "Could not retrieve associated VUs for  (" + instance.getId()+ "): " + ex.getMessage();
             LOGGER.warn(msg, ex);
             instance = null;
         } finally {
             repo.closeConnection(conn);
         }
        
         return VUs;
     }
 }
