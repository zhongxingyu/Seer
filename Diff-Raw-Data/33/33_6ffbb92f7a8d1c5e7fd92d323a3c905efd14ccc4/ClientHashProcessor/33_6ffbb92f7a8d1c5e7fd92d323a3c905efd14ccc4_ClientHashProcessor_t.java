 package de.consistec.syncframework.common.client;
 
 /*
  * #%L
  * Project - doppelganger
  * File - ClientHashProcessor.java
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
 import static de.consistec.syncframework.common.MdTableDefaultValues.FLAG_PROCESSED;
 import static de.consistec.syncframework.common.i18n.MessageReader.read;
 import static de.consistec.syncframework.common.util.CollectionsUtil.newArrayList;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.IConflictListener;
 import de.consistec.syncframework.common.SyncData;
 import de.consistec.syncframework.common.SyncDataHolder;
 import de.consistec.syncframework.common.SyncDirection;
 import de.consistec.syncframework.common.TableSyncStrategies;
 import de.consistec.syncframework.common.TableSyncStrategy;
 import de.consistec.syncframework.common.adapter.DatabaseAdapterCallback;
 import de.consistec.syncframework.common.adapter.IDatabaseAdapter;
 import de.consistec.syncframework.common.conflict.ConflictStrategy;
 import de.consistec.syncframework.common.conflict.ConflictStrategyFactory;
 import de.consistec.syncframework.common.conflict.IConflictStrategy;
 import de.consistec.syncframework.common.data.Change;
 import de.consistec.syncframework.common.data.MDEntry;
 import de.consistec.syncframework.common.exception.SyncException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.i18n.Errors;
 import de.consistec.syncframework.common.i18n.Warnings;
 import de.consistec.syncframework.common.util.LoggingUtil;
 
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.cal10n.LocLogger;
 
 /**
  * The class {@code ClientHashProcessor} is responsible for applying changes from server
  * on the client. Additionally this class updates the revision for each server change
  * in the meta table.
  * <br/>
  * <p/>
  * To apply the server changes the {@code ClientHashProcessor} looks for each server change
  * in the client's meta and data table and inserts, modifies or deletes the corresponding client entries,
  * depending on the configured conflict strategies CLIENT_WINS or SERVER_WINS and detected
  * possible conflict types which are defined in {@link de.consistec.syncframework.common.conflict.ConflictType}
  * <br/>
  *
  * @author Markus Backes
  * @company consistec Engineering and Consulting GmbH
  * @date unknown
  * @since 0.0.1-SNAPSHOT
  */
 public class ClientHashProcessor {
 
     //<editor-fold defaultstate="expanded" desc=" Class fields " >
     private static final LocLogger LOGGER = LoggingUtil.createLogger(ClientHashProcessor.class.getCanonicalName());
     private static final Config CONF = Config.getInstance();
     private IConflictListener conflictListener;
     private IDatabaseAdapter adapter;
     private TableSyncStrategies strategies;
 
     //</editor-fold>
     //<editor-fold defaultstate="collapsed" desc=" Class constructors " >
     /**
      * Instantiates a new client hash processor.
      *
      * @param adapter Database adapter object.
      * @param conflictListener ConflictListener for resolving conflicts when
      * @param strategies Special synchronization strategies for tables.
      * {@link de.consistec.syncframework.common.conflict.ConflictStrategy ConflictStrategy} is FIRE_EVENT.
      */
     public ClientHashProcessor(IDatabaseAdapter adapter, TableSyncStrategies strategies,
         IConflictListener conflictListener) {
         this.adapter = adapter;
         this.strategies = strategies;
         this.conflictListener = conflictListener;
         LOGGER.debug("HashProcessor Constructor finished");
     }
 
     //</editor-fold>
     //<editor-fold defaultstate="expanded" desc=" Class methods " >
     /**
      * Resolves the conflicts between the client changes and the server changes.
      *
      * @param clientData detected client changes to free from conflicts
      * @param serverData detected server changes to free from conflicts
      * @return a sync data holder container which contains cleaned client and server changes.
      * @throws SyncException
      */
    public SyncDataHolder resolveConflicts(SyncData clientData, SyncData serverData) throws SyncException {
         LOGGER.debug("applyChangesFromServerOnClient called");
 
 
         Collections.sort(serverData.getChanges(), Change.getPrimaryKeyComparator());
         Collections.sort(clientData.getChanges(), Change.getPrimaryKeyComparator());
 
         SyncData copiedClientSyncData = new SyncData(clientData);
         SyncData copiedServerSyncData = new SyncData(serverData);
 
         // we have to copy the lists to remove items from it.
         List<Change> newClientList = newArrayList(clientData.getChanges());
         List<Change> newServerList = newArrayList(serverData.getChanges());
 
         copiedClientSyncData.setChanges(newClientList);
         copiedServerSyncData.setChanges(newServerList);
 
 
         SyncDataHolder dataHolder = new SyncDataHolder(copiedClientSyncData, copiedServerSyncData);
         try {
             for (final Change remoteChange : serverData.getChanges()) {
 
                 int foundIndex = Collections.binarySearch(clientData.getChanges(), remoteChange,
                     Change.getPrimaryKeyComparator());
 
                 final MDEntry remoteEntry = remoteChange.getMdEntry();
                 LOGGER.debug("processing: {}", remoteEntry.toString());
 
                 if (isConflict(foundIndex)) {
                     Change clientChange = clientData.getChanges().get(foundIndex);
                     resolveConflict(remoteChange, clientChange, dataHolder);
                 }
             }
         } catch (DatabaseAdapterException e) {
             throw new SyncException(e);
         } catch (NoSuchAlgorithmException e) {
             throw new SyncException(e);
         } catch (SQLException e) {
             throw new SyncException(e);
         }
         LOGGER.debug("applyChangesFromServerOnClient finished");
 
         return dataHolder;
     }
 
     /**
      * Apply changes from server on client.
      *
      * @param serverChanges the server changes
      * @throws SyncException the sync exception
      */
     public void applyChangesFromServerOnClient(List<Change> serverChanges)
         throws SyncException, DatabaseAdapterException, NoSuchAlgorithmException {
 
         LOGGER.debug("applyChangesFromServerOnClient called");
 
         for (final Change remoteChange : serverChanges) {
 
             final MDEntry remoteEntry = remoteChange.getMdEntry();
             LOGGER.debug("processing: {}", remoteEntry.toString());
 
             applyServerChange(remoteChange);
         }
         LOGGER.debug("applyChangesFromServerOnClient finished");
     }
 
     private void applyServerChange(final Change serverChange) throws DatabaseAdapterException,
         NoSuchAlgorithmException {
 
         final MDEntry remoteEntry = serverChange.getMdEntry();
         final Map<String, Object> remoteRowData = serverChange.getRowData();
 
         // SERVER ADD, MOD OR DEL
         if (remoteEntry.dataRowExists()) {
             adapter.getRowForPrimaryKey(remoteEntry.getPrimaryKey(), remoteEntry.getTableName(),
                 new DatabaseAdapterCallback<ResultSet>() {
                     @Override
                     public void onSuccess(final ResultSet result) throws DatabaseAdapterException, SQLException {
                         String hash = null;
                         try {
                             hash = serverChange.calculateHash();
                         } catch (NoSuchAlgorithmException e) {
                             throw new DatabaseAdapterException(e);
                         }
                         if (result.next()) {
                             // SERVER MOD
                             adapter.updateDataRow(remoteRowData, remoteEntry.getPrimaryKey(), remoteEntry.getTableName());
                             adapter.updateMdRow(remoteEntry.getRevision(), FLAG_PROCESSED, remoteEntry.getPrimaryKey(),
                                 hash, remoteEntry.getTableName());
                         } else {
                             // SERVER ADD
                             // on initialization everything is a server add, but deleted items no longer need to be added
                             adapter.insertDataRow(remoteRowData, remoteEntry.getTableName());
                             adapter.insertMdRow(remoteEntry.getRevision(), FLAG_PROCESSED, remoteEntry.getPrimaryKey(),
                                 hash, remoteEntry.getTableName());
                         }
                     }
                 });
         } else {
             // SERVER DEL
             adapter.getRowForPrimaryKey(remoteEntry.getPrimaryKey(), remoteEntry.getTableName(),
                 new DatabaseAdapterCallback<ResultSet>() {
                     @Override
                     public void onSuccess(final ResultSet result) throws DatabaseAdapterException, SQLException {
                         if (result.next()) {
                             adapter.deleteRow(remoteEntry.getPrimaryKey(), remoteEntry.getTableName());
                             adapter.updateMdRow(remoteEntry.getRevision(), FLAG_PROCESSED, remoteEntry.getPrimaryKey(),
                                 null, remoteEntry.getTableName());
                         } else {
                             adapter.insertMdRow(remoteEntry.getRevision(), FLAG_PROCESSED, remoteEntry.getPrimaryKey(),
                                 null, remoteEntry.getTableName());
                         }
                     }
                 });
         }
     }
 
     private boolean isConflict(int foundIndex) {
         return foundIndex >= 0;
     }
 
     private void resolveConflict(final Change serverChange, final Change clientChange, SyncDataHolder dataHolder)
         throws DatabaseAdapterException, NoSuchAlgorithmException, SQLException, SyncException {
 
         MDEntry remoteEntry = serverChange.getMdEntry();
 
         ConflictHandlingData data = new ConflictHandlingData(clientChange, serverChange);
 
         TableSyncStrategy tableSyncStrategy = strategies.getSyncStrategyForTable(remoteEntry.getTableName());
 
         ConflictStrategy conflictStrategy = tableSyncStrategy.getConflictStrategy();
         LOGGER.info("Conflict Action: {}", conflictStrategy.name());
 
         SyncDirection syncDirection = tableSyncStrategy.getDirection();
         LOGGER.info("Sync Direction: {}", syncDirection.name());
 
         IConflictStrategy conflictHandlingStrategy = ConflictStrategyFactory.newInstance(syncDirection);
 
         switch (conflictStrategy) {
             case CLIENT_WINS:
                 conflictHandlingStrategy.resolveByClientWinsStrategy(adapter, data);
                 dataHolder.getServerSyncData().getChanges().remove(serverChange);
                 break;
             case SERVER_WINS:
                 conflictHandlingStrategy.resolveByServerWinsStrategy(adapter, data);
                 // remove client change from change list if syncdirection is server_to_client
                 dataHolder.getClientSyncData().getChanges().remove(clientChange);
                 dataHolder.getServerSyncData().getChanges().remove(serverChange);
                 break;
             case FIRE_EVENT:
                 resolveConflictsFireEvent(data, conflictHandlingStrategy);
                 // remove client change from change list if syncdirection is server_to_client
                 dataHolder.getClientSyncData().getChanges().remove(clientChange);
                 dataHolder.getServerSyncData().getChanges().remove(serverChange);
                 break;
             default:
                throw new IllegalStateException(String.format("Unknown conflict strategy %s", conflictStrategy.name()));
         }
     }
 
     /**
      * Update client revision.
      *
      * @param clientData data which contains the server revision after sync and the client changes applied on server.
      * @throws DatabaseAdapterException When update fails.
      */
    public void updateClientRevision(SyncData clientData) throws DatabaseAdapterException {
 
         LOGGER.debug("Updating client revisions on hashtable");
         for (Change change : clientData.getChanges()) {
            MDEntry tmpMDEntry = change.getMdEntry();
             int result = adapter.updateRevision(clientData.getRevision(),
                 tmpMDEntry.getTableName() + CONF.getMdTableSuffix(),
                 tmpMDEntry.getPrimaryKey());
             if (result != 1) {
                 LOGGER.warn(read(Warnings.COMMON_CANT_UPDATE_CLIENT_REV));
             }
         }
 
     }
 
    private void resolveConflictsFireEvent(ConflictHandlingData data, IConflictStrategy conflictHandlingStrategy)
         throws SQLException, SyncException, NoSuchAlgorithmException, DatabaseAdapterException {
 
         if (null == conflictListener) {
             throw new SyncException(read(Errors.COMMON_NO_CONFLICT_LISTENER_FOUND));
         } else {
             conflictHandlingStrategy.resolveByFireEvent(adapter, data, data.getLocalData(), conflictListener);
         }
     }
     //</editor-fold>
 }
