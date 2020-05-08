 package dk.statsbiblioteket.newspaper.mfpakintegration.database;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.Event;
 import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Handles all calls to the actual database backend.
  */
 public class MfPakDAO {
     private static Logger log = LoggerFactory.getLogger(MfPakDAO.class);
     protected DBConnector connector = null;
     private final MfPakConfiguration configuration;
     public static final String GET_BATCH_ID = "SELECT rowId FROM batch WHERE batchId=?";
     public static final String GET_EVENTS = "SELECT name, batchstatus.created FROM batchstatus, status WHERE batchstatus.statusrowId=status.rowId AND batchstatus.batchrowId=?";
     public static final String GET_ALL_BARCODES = "SELECT  batchId, rowId FROM batch";
     public static final String GET_ALL_EVENTS = "SELECT batchrowId, name, batchstatus.created from batchstatus, status where statusrowId = status.rowId ";
     public static final String GET_NEWSPAPER_ID = "SELECT NewsPaperId FROM NewsPaper WHERE RowId = (SELECT NewsPaperRowId FROM Batch WHERE BatchId = ?) ";
 
     public MfPakDAO(MfPakConfiguration configuration) {
         this.configuration = configuration;
         try {
             Class.forName(configuration.getDatabaseDriver());
         } catch (ClassNotFoundException e) {
             throw new RuntimeException("Unable to find database driver '" + configuration.getDatabaseDriver() +
                     "' in classpath.", e);
         }
     }
 
     private synchronized Connection getConnection() {
         if (connector == null) {
             connector = new DBConnector(configuration);
         }
         return connector.getConnection();
     }
 
     /**
      * Returns a list of all batches with their associated events from mfpak.
      *
      * @return The list of batches.
      */
     public List<Batch> getAllBatches() throws SQLException {
         Map<String, Batch> batchesById = new HashMap<>();
         try (Connection con = getConnection();
              PreparedStatement statement = con.prepareStatement(GET_ALL_BARCODES);
              PreparedStatement statement2 = con.prepareStatement(GET_ALL_EVENTS)) {
             try (ResultSet rs = statement.executeQuery()) {
                 while (rs.next()) {
                     Long barcode = rs.getLong("batchId");
                     Batch batch = new Batch();
                     batch.setBatchID(barcode.toString());
                     batch.setEventList(new ArrayList<Event>());
                     batchesById.put(rs.getString("rowId"), batch);
                 }
             }
             try (ResultSet rs = statement2.executeQuery()) {
                 while (rs.next()) {
                     String batchId = rs.getString("batchrowId");
                     Timestamp createdTimestamp = rs.getTimestamp("created"); 
                     String status = rs.getString("name");
                     Batch batch = batchesById.get(batchId);
                     if (batch != null) {
                         try {
                             batch.getEventList().add(createEvent(status, createdTimestamp));
                         } catch (IllegalArgumentException e) {
                             log.warn(e.getMessage());
                         }
                     } else {
                         log.warn("Found an event '" + status + "' attached to an unknown batch with id '" + batchId);
                     }
                 }
             }
         }
 
         return new ArrayList<>(batchesById.values());
     }
 
     /**
      * Returns a batch with the given id (barcode) and all its relevant statuses, or null if no such batch exists.
      *
      * @param barcode
      * @return the batch.
      */
     public Batch getBatchByBarcode(String barcode) throws SQLException {
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(GET_BATCH_ID)) {
             stmt.setLong(1, Long.parseLong(barcode));//TODO parse errors?
             try (ResultSet rs = stmt.executeQuery()) {
                 boolean batchExists = rs.next();
                 if (!batchExists) {
                     log.warn("No such batch found: '" + barcode + "'");
                     return null;
                 } else {
                     Batch batch = new Batch();
                     batch.setBatchID(barcode);
                     int id = rs.getInt("rowId");
                     batch.setEventList(new ArrayList<Event>());
                     try (PreparedStatement stmt2 = con.prepareStatement(GET_EVENTS)) {
                         stmt2.setInt(1, id);
                         try (ResultSet rs2 = stmt2.executeQuery()) {
                             while (rs2.next()) {
                                 String status = rs2.getString("name");
                                 Timestamp created = rs2.getTimestamp("created");
                                 try {
                                     batch.getEventList().add(createEvent(status, created));
                                 } catch (IllegalArgumentException e) {
                                     log.warn(e.getMessage());
                                 }
                             }
                         }
                     }
                     if (rs.next()) {
                         log.warn("Found more than one batch with the barcode '" + barcode + "'");
                     }
                     return batch;
                 }
             }
         }
     }
     
     /**
      * Returns the newspaper id for a batch with the given id (barcode), or null if no newspaper id is found.
      *
      * @param barcode for the batch
      * @return The id of the news paper found for batch with barcode.
      * @throws InconsistentDatabaseException if more than one newspaperID is found.
      */
     public String getNewspaperID(String barcode) throws SQLException, InconsistentDatabaseException {
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(GET_NEWSPAPER_ID)) {
             stmt.setLong(1, Long.parseLong(barcode));
             try (ResultSet rs = stmt.executeQuery()) {
                 boolean newspaperIDExists = rs.next();
                 if (!newspaperIDExists) {
                     log.warn("No newspaper ID for batch: '" + barcode + "' found!");
                     return null;
                 } else {
                     String newspaperID = rs.getString("NewsPaperId");
                     if (rs.next()) {
                         throw new InconsistentDatabaseException("Found more than one batch with the barcode '" + barcode + "'");
                     }
                     return newspaperID;
                 }
             }
         }
     }
     
     /**
      * Method to look-up a newspaper entity information based on the newspaperID and a date.
      * @param newspaperID the ID of the newspaper
      * @param date the date for which the entity information is wanted. 
      * @return NewspaperEntity the entity information about the newspaper. Null if no entity information can be
      *      found for a newspaperID on the given date 
      * @throws InconsistentDatabaseException if more than one NewspaperEntity is found
      */
     public NewspaperEntity getNewspaperEntity(String newspaperID, Date date) throws SQLException, InconsistentDatabaseException {
         final String selectSql = "SELECT Name, PublicationLocation, FromDate, ToDate FROM NewsPaperTitle"
                 + " WHERE NewsPaperRowId = (SELECT RowId FROM NewsPaper WHERE NewsPaperId = ?)"
                 + " AND FromDate <= ?" 
                 + " AND ToDate >= ?";
         NewspaperEntity entity = null;
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(selectSql)) {
             stmt.setString(1, newspaperID);
             stmt.setDate(2, new java.sql.Date(date.getTime()));
             stmt.setDate(3, new java.sql.Date(date.getTime()));
             try (ResultSet rs = stmt.executeQuery()) {
                 boolean newspaperEntityExists = rs.next();
                 if (newspaperEntityExists) {
                     entity = new NewspaperEntity();
                     entity.setNewspaperID(newspaperID);
                     entity.setNewspaperTitle(rs.getString("Name"));
                     entity.setPublicationLocation(rs.getString("PublicationLocation"));
                     NewspaperDateRange range = new NewspaperDateRange(rs.getDate("FromDate"), rs.getDate("ToDate"));
                     entity.setNewspaperDateRange(range);
                     if (rs.next()) {
                         throw new InconsistentDatabaseException("Found more than one newspaper entity for newspaperID '" 
                                 + newspaperID + "' on date '" + date + "'");
                     }
                 } else {
                     log.warn("No newspaper entity for newspaperID '" + newspaperID + "' on date '" + date + "' found!");
                 }
             }
         }
         return entity;
     }
     
     /**
      * Method to get the list of valid NewspaperDateRanges for a given batchID. 
      * The date ranges is sorted by from date ascending. The number of items in the list is the same 
      * as the number of films in the given batch.
      * @param batchID The id of the batch to get the NewspaperDateRanges for.  
      * @return The list of NewspaperDateRanges if any could be found, or null if none could be found.
      *  I.e. the list will never be empty.
      */
     public List<NewspaperDateRange> getBatchDateRanges(String batchID) throws SQLException {
         final String selectSql = "SELECT FromDate, ToDate FROM Film" 
                 + " WHERE BatchRowId = (SELECT RowId FROM Batch WHERE BatchId = ?)"
                 + " ORDER BY FromDate ASC";
         List<NewspaperDateRange> ranges;
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(selectSql)) {
             stmt.setLong(1, Long.parseLong(batchID));
             try(ResultSet rs = stmt.executeQuery()) {
                 ranges = new ArrayList<NewspaperDateRange>();
                 while(rs.next()) {
                     Date from = rs.getDate("FromDate");
                     Date to = rs.getDate("ToDate");
                     NewspaperDateRange range = new NewspaperDateRange(from, to);
                     ranges.add(range);
                 }
                 if(ranges.isEmpty()) {
                     ranges = null;
                 }
             }
         }
         
         return ranges;
     }
 
     /**
      * Method to get the list of valid NewspaperEntities. The list of entities are all entities covered from
      * the earliest date on the films that the batch's is comprised of and to the last date. 
      * @param batchID The ID of the batch to get titles for.
      * @return The list of NewspaperEntity if any could be found, or null if none could be found.
      *  I.e. the list will never be empty.  
      */
     public List<NewspaperEntity> getBatchNewspaperEntities(String batchID) throws SQLException {
         List<NewspaperEntity> entities = null;
         
         final String selectSql = "SELECT Name, FromDate, ToDate, PublicationLocation, NewsPaper.NewsPaperId"  
                 + " FROM NewsPaperTitle"
                 + " JOIN NewsPaper ON NewsPaperTitle.NewsPaperRowId = NewsPaper.RowId" 
                 + " WHERE NewsPaperRowId = (SELECT NewsPaperRowId FROM Batch WHERE BatchId = ?)"
                + " AND (ToDate IS NULL OR ToDate >= (SELECT FromDate FROM Film"
                     + " WHERE BatchRowId = (SELECT RowId FROM Batch WHERE BatchId = ?)"
                    + " ORDER BY FromDate ASC LIMIT 1))"
                 + " AND FromDate <= (SELECT ToDate FROM Film"
                     + " WHERE BatchRowId = (SELECT RowId FROM Batch WHERE BatchId = ?)"
                     + " ORDER BY ToDate DESC LIMIT 1)"
                 + " ORDER BY FromDate ASC";
         
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(selectSql)) {
             stmt.setLong(1, Long.parseLong(batchID));
             stmt.setLong(2, Long.parseLong(batchID));
             stmt.setLong(3, Long.parseLong(batchID));
             try(ResultSet rs = stmt.executeQuery()) {
                 entities = new ArrayList<NewspaperEntity>();
                 while(rs.next()) {
                     Date from = rs.getDate("FromDate");
                     Date to = rs.getDate("ToDate");
                     if(to == null) {
                         to = new Date(Long.MAX_VALUE);
                     }
                     NewspaperDateRange range = new NewspaperDateRange(from, to);
                     NewspaperEntity entity = new NewspaperEntity();
                     entity.setNewspaperTitle(rs.getString("Name"));
                     entity.setNewspaperDateRange(range);
                     entity.setNewspaperID(rs.getString("NewsPaperId"));
                     entity.setPublicationLocation(rs.getString("PublicationLocation"));
                     entities.add(entity);
                 }
                 if(entities.isEmpty()) {
                     entities = null;
                 }
             }
         }
         
         return entities;
     }
     
     /**
      * Method to get the options ordered for the given batch
      * @param batchID the ID of the batch
      * @return {@link NewspaperBatchOptions} The options for the given batch.
      * @throws SQLException 
      */
     public NewspaperBatchOptions getBatchOptions(String batchID) throws SQLException, InconsistentDatabaseException {
         NewspaperBatchOptions options = null;
         final String selectSql = "SELECT OptionB1, OptionB2, OptionB3, OptionB4,"
                 + " OptionB5, OptionB6, OptionB7, OptionB8, OptionB9 FROM OrderLine"
                 + " WHERE RowId = ("
                     + " SELECT OrderLineRowId FROM OrderBatch"
                     + " WHERE BatchRowID = ("
                         + " SELECT RowId FROM Batch where BatchId = ?))";
         
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(selectSql)) {
             stmt.setLong(1, Long.parseLong(batchID));
             try (ResultSet rs = stmt.executeQuery()) {
                 boolean newspaperOptionsExists = rs.next();
                 if (!newspaperOptionsExists) {
                     log.warn("No batch options entry for batch: '" + batchID + "' found!");
                     return null;
                 } else {
                     options = new NewspaperBatchOptions();
                     options.setOptionB1(rs.getBoolean("OptionB1"));
                     options.setOptionB2(rs.getBoolean("OptionB2"));
                     options.setOptionB3(rs.getBoolean("OptionB3"));
                     options.setOptionB4(rs.getBoolean("OptionB4"));
                     options.setOptionB5(rs.getBoolean("OptionB5"));
                     options.setOptionB6(rs.getBoolean("OptionB6"));
                     options.setOptionB7(rs.getBoolean("OptionB7"));
                     options.setOptionB8(rs.getBoolean("OptionB8"));
                     options.setOptionB9(rs.getBoolean("OptionB9"));
                     if (rs.next()) {
                         throw new InconsistentDatabaseException("Found more than one set of batch options for batch '" 
                                 + batchID + "'");
                     }
                 }
             }
         }
         
         return options;
     }
     
     /**
      * Retrieve the date that a batch was shipped from SB
      * @param batchID The ID of the batch
      * @return Date the date that the batch was shipped, null if no shipment has been registered
      * @throws InconsistentDatabaseException if more that one shipment date is found for the batch
      */
     public Date getBatchShipmentDate(String batchID) throws SQLException {
         Date shipmentDate = null;
         
         final String selectSql = "SELECT Created FROM BatchStatus"
                 + " WHERE BatchRowId = (SELECT RowId FROM Batch"
                     + " WHERE BatchId = ?)"
                 + " AND StatusRowId = (SELECT RowId FROM Status"
                     + " WHERE Name = 'Batch shipped to supplier');";
         
         try (Connection con = getConnection(); PreparedStatement stmt = con.prepareStatement(selectSql)) {
             stmt.setLong(1, Long.parseLong(batchID));
             try (ResultSet rs = stmt.executeQuery()) {
                 boolean shipmentDateFound = rs.next();
                 if (!shipmentDateFound) {
                     log.warn("No shipment date for batch: '" + batchID + "' found!");
                 } else {
                     shipmentDate = rs.getDate("Created");
                     if (rs.next()) {
                         throw new InconsistentDatabaseException(
                                 "Found more than one shipment date for batch '" + batchID + "'");
                     }
                 }
             }
         }
         
         return shipmentDate;
     }
     
     /**
      * Creates a event object based on the status in the MfPak DB.
      *
      * @param status           The name in the status table for this batch.
      * @param createdTimestamp
      * @return The corresponding event object.
      */
     private static Event createEvent(String status, Date createdTimestamp) {
         Event event = new Event();
         event.setSuccess(true);
         event.setDate(createdTimestamp);
         switch (status) {
             case "Initial":
                 event.setEventID("Initial");
                 break;
             case "Batch added to shipping container":
                 event.setEventID("Added_to_shipping_container");
                 break;
             case "Batch shipped to supplier":
                 event.setEventID("Shipped_to_supplier");
                 break;
             case "Batch shipped from supplier":
                 event.setEventID("Shipped_from_supplier");
                 break;
             case "Batch received from supplier":
                 event.setEventID("Received_from_supplier");
                 break;
             case "Batch follow-up":
                 event.setEventID("FollowUp");
                 break;
             case "Batch approved":
                 event.setEventID("Approved");
                 break;
             default:
                 throw new IllegalArgumentException("Unknown batch status " + status);
         }
         return event;
     }
 
     /**
      * Returns a specific event specified by the barcode and eventStatus, or null if no such event has occurred.
      *
      * @param batchBarcode
      * @param eventStatus
      * @return the event.
      */
     public Event getEvent(String batchBarcode, String eventStatus) throws SQLException {
         Batch batch = getBatchByBarcode(batchBarcode);
         for (Event event : batch.getEventList()) {
             if (event.getEventID().equals(eventStatus)) {
                 return event;
             }
         }
         return null;
     }
 }
