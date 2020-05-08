 /*
  * #%L
  * Bitrepository Audit Trail Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.audittrails.store;
 
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_CONTRIBUTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_FILE_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_ID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_COLLECTION_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_COLLECTION_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_CONTRIBUTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_SEQ;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_TABLE;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
 import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.database.DatabaseUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Audit trail storages backed by a database for preserving
  */
 public class AuditTrailServiceDAO implements AuditTrailStore {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The connection to the database.*/
     private DBConnector dbConnector;
     
     /** 
      * Constructor.
      * @param settings The settings.
      */
     public AuditTrailServiceDAO(Settings settings) {
         ArgumentValidator.checkNotNull(settings, "settings");
         
         dbConnector = new DBConnector(
                 settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
     }
     
     @Override
     public List<AuditTrailEvent> getAuditTrails(String fileId, String collectionID, String contributorId, 
             Long minSeqNumber, Long maxSeqNumber, String actorName, FileAction operation, Date startDate, 
             Date endDate, Integer maxResults) {
         ExtractModel model = new ExtractModel();
         model.setFileId(fileId);
         model.setCollectionId(collectionID);
         model.setContributorId(contributorId);
         model.setMinSeqNumber(minSeqNumber);
         model.setMaxSeqNumber(maxSeqNumber);
         model.setActorName(actorName);
         model.setOperation(operation);
         model.setStartDate(startDate);
         model.setEndDate(endDate);
         if(maxResults != null) {
             model.setMaxCount(maxResults);
         }
 
         AuditDatabaseExtractor extractor = new AuditDatabaseExtractor(model, dbConnector);
         return extractor.extractAuditEvents();
     }
     
     @Override
     public AuditEventIterator getAuditTrailsByIterator(String fileId, String collectionID, String contributorId, 
             Long minSeqNumber, Long maxSeqNumber, String actorName, FileAction operation, Date startDate, 
             Date endDate, Integer maxResults) {
         ExtractModel model = new ExtractModel();
         model.setFileId(fileId);
         model.setCollectionId(collectionID);
         model.setContributorId(contributorId);
         model.setMinSeqNumber(minSeqNumber);
         model.setMaxSeqNumber(maxSeqNumber);
         model.setActorName(actorName);
         model.setOperation(operation);
         model.setStartDate(startDate);
         model.setEndDate(endDate);
         if(maxResults != null) {
             model.setMaxCount(maxResults);
         }
 
         AuditDatabaseExtractor extractor = new AuditDatabaseExtractor(model, dbConnector);
         return extractor.extractAuditEventsByIterator();
     }
     
     
     
     @Override
     public void addAuditTrails(AuditTrailEvents newAuditTrails, String collectionId) {
         ArgumentValidator.checkNotNull(newAuditTrails, "AuditTrailEvents newAuditTrails");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         
         AuditDatabaseIngestor ingestor = new AuditDatabaseIngestor(dbConnector);
         for(AuditTrailEvent event : newAuditTrails.getAuditTrailEvent()) {
             ingestor.ingestAuditEvents(event, collectionId);
         }
     }
     
     @Override
     public int largestSequenceNumber(String contributorId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         
         // Indirectly extracts contributor and collection keys, and joins with the file table where the query can be
         // limited by collection
         String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + " FROM " + AUDITTRAIL_TABLE 
                 + " JOIN " + FILE_TABLE 
                 + " ON " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_FILE_KEY + " = " + FILE_TABLE + "." + FILE_KEY
                 + " WHERE "  + AUDITTRAIL_TABLE + "." + AUDITTRAIL_CONTRIBUTOR_KEY + " = ("
                     + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                     + " WHERE " + CONTRIBUTOR_ID + " = ? )"
                 + " AND " + FILE_TABLE + "." + FILE_COLLECTION_KEY + " = (" 
                     + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE 
                     + " WHERE " + COLLECTION_ID + " = ? )"
                 + " ORDER BY " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_SEQUENCE_NUMBER + " DESC";
         
         Long seq = DatabaseUtils.selectFirstLongValue(dbConnector, sql, contributorId, collectionId);
         if(seq != null) {
             return seq.intValue();
         }
         return 0;
     }    
 
     @Override
     public long getPreservationSequenceNumber(String contributorId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
         ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
         
         String sql = "SELECT " + PRESERVATION_SEQ + " FROM " + PRESERVATION_TABLE 
                 + " WHERE " + PRESERVATION_CONTRIBUTOR_KEY + " = (" 
                     + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                     + " WHERE " + CONTRIBUTOR_ID + " = ? )"
                 + "AND " + PRESERVATION_COLLECTION_KEY + " = (" 
                     + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE 
                     + " WHERE " + COLLECTION_ID + " = ? )";
         
         Long seq = DatabaseUtils.selectLongValue(dbConnector, sql, contributorId, collectionId);
         if(seq != null) {
             return seq.intValue();
         }
         return 0;
     }
 
     @Override
     public void setPreservationSequenceNumber(String contributorId, String collectionId, long seqNumber) {
         ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
         ArgumentValidator.checkNotNegative(seqNumber, "int seqNumber");
         long preservationKey = retrievePreservationKey(contributorId, collectionId);
        log.debug("Updateing preservation sequence number for contributor: " + contributorId 
                 + " in collection: " + collectionId + " to seq: " + seqNumber);
         String sqlUpdate = "UPDATE " + PRESERVATION_TABLE + " SET " + PRESERVATION_SEQ + " = ?"
                 + " WHERE " + PRESERVATION_KEY + " = ? ";
         DatabaseUtils.executeStatement(dbConnector, sqlUpdate, seqNumber, preservationKey);
     }
     
     public boolean haveContributor(String contributorID) {
         String sql = "SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE
                         + " WHERE " + CONTRIBUTOR_ID + " = ?";
         Long contributorKey = DatabaseUtils.selectLongValue(dbConnector, sql, contributorID);
         if(contributorKey == null) {
             return false;    
         } else {
             return true;
         }
         
     }
     
     /**
      * Retrieves the key of the preservation table entry for the given collection and contributor.
      * 
      * @param contributorId The contributor of the preservation table entry.
      * @param collectionId The collection of the preservation table entry.
      * @return The key of the entry in the preservation table.
      */
     private Long retrievePreservationKey(String contributorId, String collectionId) {
         String sqlRetrieve = "SELECT " + PRESERVATION_KEY + " FROM " + PRESERVATION_TABLE 
                 + " WHERE " + PRESERVATION_CONTRIBUTOR_KEY + " = (" 
                     + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                     + " WHERE " + CONTRIBUTOR_ID + " = ? ) "
                 + "AND " + PRESERVATION_COLLECTION_KEY + " = ("
                     + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE
                     + " WHERE " + COLLECTION_ID + " = ? )";
         Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId, collectionId);
         
         if(guid == null) {
             log.debug("Inserting preservation entry for contributor '" + contributorId + "' and collection '" 
                     + collectionId + "' into the preservation table.");
             String sqlInsert = "INSERT INTO " + PRESERVATION_TABLE + " ( " + PRESERVATION_CONTRIBUTOR_KEY + " , " 
                     + PRESERVATION_COLLECTION_KEY + ") VALUES ( "
                         + "(SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE + " WHERE " 
                         + CONTRIBUTOR_ID + " = ?)"
                     + ", "
                         + "( SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE + " WHERE " 
                         + COLLECTION_ID + " = ? )"
                     + ")";
             DatabaseUtils.executeStatement(dbConnector, sqlInsert, contributorId, collectionId);
             
             guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId, collectionId);
         }
         
         return guid;
     }
 
     @Override
     public void close() {
         try {
             dbConnector.getConnection().close();
         } catch (SQLException e) {
             log.warn("Cannot close the database properly.", e);
         }
     }
 
 }
