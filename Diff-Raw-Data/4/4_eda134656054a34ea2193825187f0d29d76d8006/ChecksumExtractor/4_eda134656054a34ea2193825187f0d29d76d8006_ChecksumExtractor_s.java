 /*
  * #%L
  * Bitrepository Reference Pillar
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
 package org.bitrepository.pillar.cache.database;
 
 import static org.bitrepository.pillar.cache.database.DatabaseConstants.CHECKSUM_TABLE;
 import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_CHECKSUM;
 import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_DATE;
 import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_FILE_ID;
 import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_COLLECTION_ID;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.pillar.cache.ChecksumEntry;
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.database.DatabaseUtils;
 
 /**
  * Extracts data from the checksum database.
  */
 public class ChecksumExtractor {
     /** The connector for the database.*/
     private final DBConnector connector;
     
     /**
      * Constructor.
      * @param connector The connector for the database.
      */
     public ChecksumExtractor(DBConnector connector) {
         ArgumentValidator.checkNotNull(connector, "DBConnector connector");
         
         this.connector = connector;
     }
     
     /**
      * Extracts the date for a given file.
      * @param fileId The id of the file to extract the date for.
      * @param collectionId The collection id for the file.
      * @return The date for the given file.
      */
     public Date extractDateForFile(String fileId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         
         String sql = "SELECT " + CS_DATE + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND " 
                 + CS_COLLECTION_ID + " = ?";
         return DatabaseUtils.selectDateValue(connector, sql, fileId, collectionId);
     }
     
     /**
      * Extracts the checksum for a given file.
      * @param fileId The id of the file to extract the checksum for.
      * @param collectionId The collection id for the file.
      * @return The checksum for the given file.
      */
     public String extractChecksumForFile(String fileId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
         
         String sql = "SELECT " + CS_CHECKSUM + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND "
                 + CS_COLLECTION_ID + " = ?";
         return DatabaseUtils.selectStringValue(connector, sql, fileId, collectionId);
     }
     
     /**
      * Extracts whether a given file exists.
      * @param fileId The id of the file to extract whose existence is in question.
      * @param collectionId The collection id for the file.
      * @return Whether the given file exists.
      */
     public boolean hasFile(String fileId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
 
         String sql = "SELECT COUNT(*) FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND "
                 + CS_COLLECTION_ID + " = ?";
         return DatabaseUtils.selectIntValue(connector, sql, fileId, collectionId) != 0;
     }
     
     /**
      * Extracts the checksum entry for a single file.
      * @param fileId The id of the file whose checksum entry should be extracted.
      * @param collectionId The collection id for the extraction.
      * @return The checksum entry for the file.
      */
     public ChecksumEntry extractSingleEntry(String fileId, String collectionId) {
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
 
         String sql = "SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE 
                 + " WHERE " + CS_FILE_ID + " = ? AND " + CS_COLLECTION_ID + " = ?";
         try {
             PreparedStatement ps = null;
             ResultSet res = null;
             Connection conn = null;
             try {
                 conn = connector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql, fileId, collectionId);
                 res = ps.executeQuery();
                 if(!res.next()) {
                     throw new IllegalStateException("No entry for the file '" + fileId + "'.");
                 }
                 return extractChecksumEntry(res);
             } finally {
                 if(res != null) {
                     res.close();
                 }
                 if(ps != null) {
                     ps.close();
                 }
                 if(conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException e) {
             throw new IllegalStateException("Cannot extract the ChecksumEntry for '" + fileId + "'", e);
         }
     }
 
     /**
      * Extracts the file ids within the given optional limitations.
      * 
      * @param minTimeStamp The minimum date for the timestamp of the extracted file ids.
      * @param maxTimeStamp The maximum date for the timestamp of the extracted file ids.
      * @param maxNumberOfResults The maximum number of results.
      * @param collectionId The collection id for the extraction.
      * @return The requested collection of file ids.
      */
     public ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
             Long maxNumberOfResults, String collectionId) {
         List<Object> args = new ArrayList<Object>(); 
         StringBuilder sql = new StringBuilder();
         sql.append("SELECT " + CS_FILE_ID + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_COLLECTION_ID
                 + " = ?");
         args.add(collectionId);
         
         if(minTimeStamp != null) {
             sql.append(" AND " + CS_DATE + " > ? ");
             args.add(CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp));
         }
         if(maxTimeStamp != null) {
             sql.append(" AND " + CS_DATE + " <= ? ");
             args.add(CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp));
         }
         sql.append(" ORDER BY " + CS_DATE + " ASC ");
         
         ExtractedFileIDsResultSet results = new ExtractedFileIDsResultSet();
         try {
             PreparedStatement ps = null;
             ResultSet res = null;
             Connection conn = null;
             try {
                 conn = connector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql.toString(), args.toArray());
                 conn.setAutoCommit(false);
                 ps.setFetchSize(100);
                 res = ps.executeQuery();
                 
                 int i = 0;
                 while(res.next() && (maxNumberOfResults == null || i < maxNumberOfResults)) {
                     results.insertFileID(res.getString(1), res.getTimestamp(2));
                     i++;
                 }
                 
                 if(maxNumberOfResults != null && i >= maxNumberOfResults) {
                     results.reportMoreEntriesFound();
                 }
             } finally {
                 if(res != null) {
                     res.close();
                 }
                 if(ps != null) {
                     ps.close();
                 }
                 if(conn != null) {
                     conn.setAutoCommit(true);
                     conn.close();
                 }
             }
         } catch (SQLException e) {
             throw new IllegalStateException("Cannot extract the file ids with the arguments, minTimestamp = '" 
                     + minTimeStamp + "', maxTimestamp = '"+ maxTimeStamp + "', maxNumberOfResults = '" 
                     + maxNumberOfResults + "'", e);
         }
         
         return results;
     }
     
     /**
      * Retrieves all the file ids for a given collection id within the database.
      * @param collectionId The collection id for the extraction.
      * @return The list of file ids extracted from the database.
      */
     public List<String> extractAllFileIDs(String collectionId) {
         String sql = "SELECT " + CS_FILE_ID + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_COLLECTION_ID + " = ?";
         return DatabaseUtils.selectStringList(connector, sql, collectionId);
     }
     
     /**
      * Extracts the checksum entries within the given optional limitations.
      * 
      * @param minTimeStamp The minimum date for the timestamp of the extracted checksum entries.
      * @param maxTimeStamp The maximum date for the timestamp of the extracted checksum entries.
      * @param maxNumberOfResults The maximum number of results.
      * @param collectionId The collection id for the extraction.
      * @return The requested collection of file ids.
      */
     public ExtractedChecksumResultSet extractEntries(XMLGregorianCalendar minTimeStamp, 
             XMLGregorianCalendar maxTimeStamp, Long maxNumberOfResults, String collectionId) {
         List<Object> args = new ArrayList<Object>(); 
         StringBuilder sql = new StringBuilder();
         sql.append("SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE 
                 + " WHERE " + CS_COLLECTION_ID + " = ?");
         args.add(collectionId);
         
         if(minTimeStamp != null) {
             sql.append(" AND " + CS_DATE + " >= ? ");
             args.add(CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp));
         }
         if(maxTimeStamp != null) {
             sql.append(" AND " + CS_DATE + " <= ? ");
             args.add(CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp));
         }
         sql.append(" ORDER BY " + CS_DATE + " ASC ");
         
         ExtractedChecksumResultSet results = new ExtractedChecksumResultSet();
         try {
             PreparedStatement ps = null;
             ResultSet res = null;
             Connection conn = null;
             try {
                 conn = connector.getConnection();
                 ps = DatabaseUtils.createPreparedStatement(conn, sql.toString(), args.toArray());
                 res = ps.executeQuery();
                 
                 int i = 0;
                 while(res.next() && (maxNumberOfResults == null || i < maxNumberOfResults)) {
                     results.insertChecksumEntry(extractChecksumEntry(res));
                     i++;
                 }
                 
                 if(maxNumberOfResults != null && i >= maxNumberOfResults) {
                     results.reportMoreEntriesFound();
                 }
             } finally {
                 if(res != null) {
                     res.close();
                 }
                 if(ps != null) {
                     ps.close();
                 }
                 if(conn != null) {
                     conn.close();
                 }
             }
         } catch (SQLException e) {
             throw new IllegalStateException("Cannot extract the checksum entries with the arguments, minTimestamp = '" 
                     + minTimeStamp + "', maxTimestamp = '"+ maxTimeStamp + "', maxNumberOfResults = '" 
                     + maxNumberOfResults + "'", e);
         }
         
         return results;
     }
     
     /**
      * Extracts a checksum entry from a result set. 
      * The result set needs to have requested the elements in the right order:
      *  - File id.
      *  - Checksum.
      *  - Date.
      * 
      * @param resSet The resultset from the database.
      * @return The checksum entry extracted from the result set.
      */
     private ChecksumEntry extractChecksumEntry(ResultSet resSet) throws SQLException {
         String fileId = resSet.getString(1);
         String checksum = resSet.getString(2);
         Date date = resSet.getTimestamp(3);
         return new ChecksumEntry(fileId, checksum, date);
     }
 }
