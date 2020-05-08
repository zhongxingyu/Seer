 package de.consistec.syncframework.common.client;
 
 /*
  * #%L
  * Project - doppelganger
  * File - ClientTableSynchronizer.java
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import static de.consistec.syncframework.common.MdTableDefaultValues.CLIENT_INIT_REVISION;
 import static de.consistec.syncframework.common.MdTableDefaultValues.FLAG_MODIFIED;
 import static de.consistec.syncframework.common.MdTableDefaultValues.MDV_DELETED_VALUE;
 import static de.consistec.syncframework.common.MdTableDefaultValues.PK_COLUMN_NAME;
 import static de.consistec.syncframework.common.MdTableDefaultValues.REV_COLUMN_NAME;
 import static de.consistec.syncframework.common.util.CollectionsUtil.newHashMap;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.adapter.DatabaseAdapterCallback;
 import de.consistec.syncframework.common.adapter.IDatabaseAdapter;
 import de.consistec.syncframework.common.data.Change;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.i18n.Infos;
 import de.consistec.syncframework.common.util.DBMapperUtil;
 import de.consistec.syncframework.common.util.HashCalculator;
 import de.consistec.syncframework.common.util.LoggingUtil;
 
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.cal10n.LocLogger;
 
 /**
  * The {@code ClientTableSynchronizer} class looks for changed / deleted data rows for each
  * sync table defined in the configuration file.
  * <br/>
  * <p>If changed rows are detected then the {@code ClientTableSynchronizer } calculates
  * a new hash entry for each of them and updates it in the md-table. For new rows the
  * {@code ClientTableSynchronizer } creates a new md table entry. If deleted rows are
  * detected and an md table entry exists then the {@code ClientTableSynchronizer }
  * updates the md table entry with an empty hash value and marks it so as deleted.</p>
  *
  * @author Markus
  * @company consistec Engineering and Consulting GmbH
  * @date unknown
  * @since 0.0.1-SNAPSHOT
  */
 public class ClientTableSynchronizer {
 
     private static final LocLogger LOGGER = LoggingUtil.createLogger(ClientTableSynchronizer.class.getCanonicalName());
     private static final Config CONF = Config.getInstance();
     private IDatabaseAdapter adapter;
 
     /**
      * Instantiates a new client table synchronizer.
      *
      * @param adapter Database adapter
      */
     public ClientTableSynchronizer(IDatabaseAdapter adapter) {
         this.adapter = adapter;
         LOGGER.debug("ClientTableSynchronizer Constructor finished");
     }
 
     /**
     * Synchronize client tables and marks new, modified or deleted rows in the database.
      *
      * @throws DatabaseAdapterException
      * @throws SQLException
      */
     public void synchronizeClientTables() throws DatabaseAdapterException {
 
         LOGGER.debug("synchronizeClientTables called");
 
         LOGGER.debug("Searching for modifications and updating metadata accordingly.");
 
         for (final String table : CONF.getSyncTables()) {
             LOGGER.debug("Processing table {}", table);
             searchAndProcessChangedRows(table);
             searchAndProcessDeletedRows(table);
         }
 
         LOGGER.debug("synchronizeClientTables finished");
     }
 
     private void searchAndProcessChangedRows(final String table) throws DatabaseAdapterException {
 
         final List<String> columns = adapter.getColumnNamesFromTable(table);
         Collections.sort(columns);
 
         adapter.getAllRowsFromTable(table, new DatabaseAdapterCallback<ResultSet>() {
             @Override
             public void onSuccess(ResultSet allRows) throws DatabaseAdapterException {
 
                 try {
                     String mdTable = table + CONF.getMdTableSuffix();
 
                     while (allRows.next()) {
 
                         final Map<String, Object> rowData = newHashMap();
 
                         for (String s : columns) {
                             rowData.put(s, allRows.getObject(s));
                         }
 
                         final Object primaryKey = allRows.getObject(adapter.getPrimaryKeyColumn(table).getName());
                         final String hash = new HashCalculator().getHash(rowData);
 
                         adapter.getRowForPrimaryKey(primaryKey, mdTable, new DatabaseAdapterCallback<ResultSet>() {
                             @Override
                             public void onSuccess(ResultSet result) throws DatabaseAdapterException {
                                 Change change = new Change();
                                 try {
                                     if (result.next()) {
                                         if (!DBMapperUtil.rowHasSameHash(result, hash)) {
                                             LOGGER.info(Infos.COMMON_UPDATING_CLIENT_HASH_ENTRY);
                                             adapter.updateMdRow(result.getInt(REV_COLUMN_NAME), FLAG_MODIFIED,
                                                 primaryKey, hash, table);
                                         }
                                     } else {
                                         // create new entry
                                         LOGGER.info(Infos.COMMON_CREATING_NEW_CLIENT_HASH_ENTRY);
                                         adapter.insertMdRow(CLIENT_INIT_REVISION, FLAG_MODIFIED, primaryKey, hash,
                                             table);
                                     }
 
                                 } catch (SQLException e) {
                                     throw new DatabaseAdapterException(e);
                                 }
                             }
                         });
 
                     }
 
                 } catch (SQLException e) {
                     throw new DatabaseAdapterException(e);
                 } catch (NoSuchAlgorithmException e) {
                     throw new DatabaseAdapterException(e);
                 }
             }
         });
     }
 
     private void searchAndProcessDeletedRows(final String table) throws DatabaseAdapterException {
 
         LOGGER.debug("searching for deleted rows");
 
         // Update deleted rows
         // http://www.cryer.co.uk/brian/sql/sql_crib_sheet.htm
         adapter.getDeletedRowsForTable(table, new DatabaseAdapterCallback<ResultSet>() {
             @Override
             public void onSuccess(ResultSet deletedRows) throws DatabaseAdapterException {
                 try {
                     while (deletedRows.next()) {
                         if (DBMapperUtil.rowIsAlreadyDeleted(deletedRows)) {
                             continue;
                         }
 
                         LOGGER.info(Infos.COMMON_FOUND_DELETED_ROW_ON_CLIENT);
                         adapter.updateMdRow(deletedRows.getInt(REV_COLUMN_NAME), FLAG_MODIFIED,
                             deletedRows.getObject(PK_COLUMN_NAME), MDV_DELETED_VALUE, table);
                     }
                 } catch (SQLException e) {
                     throw new DatabaseAdapterException(e);
                 }
             }
         });
     }
 }
