 package org.h2o.db;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 import org.h2o.autonomic.decision.ranker.metric.Metric;
 import org.h2o.autonomic.numonic.ranking.MachineMonitoringData;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.h2o.db.manager.SystemTableProxy;
 import org.h2o.db.manager.TableManagerProxy;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.query.TableProxy;
 import org.h2o.db.query.asynchronous.CommitResult;
 import org.h2o.db.query.locking.LockRequest;
 import org.h2o.db.query.locking.LockType;
 import org.h2o.db.replication.ReplicaManager;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.db.wrappers.TableManagerWrapper;
 import org.json.JSONException;
 import org.json.JSONWriter;
 
 import uk.ac.standrews.cs.nds.rpc.DeserializationException;
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;
 import uk.ac.standrews.cs.nds.rpc.stream.Marshaller;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.numonic.data.FileSystemData;
 import uk.ac.standrews.cs.numonic.data.MachineUtilisationData;
 import uk.ac.standrews.cs.numonic.data.SystemInfoData;
 import uk.ac.standrews.cs.stachord.impl.ChordRemoteMarshaller;
 import uk.ac.standrews.cs.stachord.interfaces.IChordRemoteReference;
 
 public class H2OMarshaller extends Marshaller {
 
     private static final String EXPECTED_UPDATE_ID = "expectedUpdateID";
     private static final String COMMIT = "commit";
     private static final String UPDATE_ID = "updateID";
     private static final String REQUESTING_DATABASE = "requestingDatabase";
     private static final String TABLE_MANAGER = "tableManager";
     private static final String LOCK_REQUESTED = "lockRequested";
     private static final String LOCK_GRANTED = "lockGranted";
     private static final String TABLE_MANAGER_URL = "tableManagerURL";
     private static final String TABLE_INFO = "tableInfo";
     private static final String DISK = "disk";
     private static final String NETWORK = "network";
     private static final String MEMORY = "memory";
     private static final String CPU = "cpu";
     private static final String IMMEDIATE_DISK_SPACE = "immediateDiskSpace";
     private static final String EXPECTED_TIME_TO_COMPLETION = "expectedTimeToCompletion";
     private static final String ACTIVE = "active";
     private static final String DATABASE_INSTANCE = "databaseInstance";
     private static final String PRIMARY_LOCATION = "primaryLocation";
     private static final String ACTIVE_REPLICAS = "activeReplicas";
     private static final String ALL_REPLICAS = "allReplicas";
     private static final String SESSION_ID = "sessionID";
     private static final String DATABASE_MAKING_REQUEST = "databaseMakingRequest";
     private static final String TABLE_TYPE = "tableType";
     private static final String TABLE_SET = "tableSet";
     private static final String MODIFICATION_ID = "modificationID";
     private static final String SCHEMA_NAME = "schemaName";
     private static final String TABLE_NAME = "tableName";
     private static final String DATABASE_URL = "databaseURL";
     private static final String DATABASE_ID = "databaseID";
     private static final String DATABASE_LOCATION = "dbLocation";
     private static final String KEYS = "keys";
     private static final String VALUES = "values";
     private static final String WRAPPER = "wrapper";
     private static final String TABLE_MANAGER_ADDRESS = "tableManagerAddress";
     private static final String MACHINE_UTILIZATION_DATA = "machineUtilData";
     private static final String FS_DATA = "fsData";
     private static final String MEASUREMENTS_BEFORE_SUMMARY = "measurements_before_summary";
     private static final String CPU_USER = "cpu_user_total";
     private static final String CPU_SYS = "cpu_sys_total";
     private static final String CPU_IDLE = "cpu_idle_total";
     private static final String CPU_WAIT = "cpu_wait_total";
     private static final String CPU_NICE = "cpu_nice_total";
     private static final String MEMORY_USED = "memory_used";
     private static final String MEMORY_FREE = "memory_free";
     private static final String SWAP_USED = "swap_used";
     private static final String SWAP_FREE = "swap_free";
     private static final String FS_LOCATION = "file_system_location";
     private static final String FS_NAME = "file_system_name";
     private static final String FS_TYPE = "file_system_type";
     private static final String FS_SPACE_USED = "fs_space_used";
     private static final String FS_SPACE_FREE = "fs_space_free";
     private static final String FS_SIZE = "fs_size";
     private static final String FS_NUMBER_OF_FILES = "fs_number_of_files";
     private static final String FS_DISK_READS = "fs_disk_reads";
     private static final String FS_BYTES_READ = "fs_bytes_read";
     private static final String FS_DISK_WRITES = "fs_disk_writes";
     private static final String FS_BYTES_WRITTEN = "fs_bytes_written";
     private static final String SYSTEM_INFO_DATA = "systInfoData";
     private static final String OS_NAME = "osName";
     private static final String OS_VERSION = "os_version";
     private static final String HOSTNAME = "hostname";
     private static final String PRIMARY_IP = "primary_ip";
     private static final String DEFAULT_GATEWAY = "default_gateway";
     private static final String CPU_VENDOR = "cpu_vendor";
     private static final String CPU_MODEL = "cpu_model";
     private static final String NUMBER_OF_CORES = "number_of_cores";
     private static final String NUMBER_OF_CPUS = "number_of_cpus";
     private static final String CPU_CLOCK_SPEED = "cpu_clock_speed";
     private static final String CPU_CACHE_SIZE = "cpu_cache_size";
     private static final String MEMORY_TOTAL = "memory_total";
     private static final String SWAP_TOTAL = "swap_total";
     private static final String MACHINE_ID = "machine_id";
 
     private final ChordRemoteMarshaller chord_marshaller;
 
     public H2OMarshaller() {
 
         chord_marshaller = new ChordRemoteMarshaller();
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeChordRemoteReference(final IChordRemoteReference source, final JSONWriter writer) throws JSONException, RPCException {
 
         chord_marshaller.serializeChordRemoteReference(source, writer);
     }
 
     public IChordRemoteReference deserializeChordRemoteReference(final JSONReader reader) throws DeserializationException {
 
         return chord_marshaller.deserializeChordRemoteReference(reader);
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeITableManagerRemote(final ITableManagerRemote source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
             try {
 
                 writer.object();
                 writer.key(TABLE_NAME);
                 writer.value(source.getFullTableName());
                 writer.key(TABLE_MANAGER_ADDRESS);
                 serializeInetSocketAddress(source.getAddress(), writer);
                 writer.endObject();
             }
             catch (final RPCException e) {
                 ErrorHandling.exceptionError(e, "Failed when serializing ITableManagerRemote instance.");
             }
         }
 
     }
 
     public ITableManagerRemote deserializeITableManagerRemote(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(TABLE_NAME);
             final String tableName = reader.stringValue();
             reader.key(TABLE_MANAGER_ADDRESS);
             final InetSocketAddress socketAddress = deserializeInetSocketAddress(reader);
 
             reader.endObject();
 
             return TableManagerProxy.getProxy(socketAddress, tableName);
         }
         catch (final JSONException e) {
             ErrorHandling.exceptionError(e, "Failed when deserializing ITableManagerRemote instance.");
 
             throw new DeserializationException(e);
         }
 
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeISystemTableMigratable(final ISystemTableMigratable source, final JSONWriter writer) throws JSONException, RPCException {
 
         serializeInetSocketAddress(source.getAddress(), writer);
 
     }
 
     public ISystemTableMigratable deserializeISystemTableMigratable(final JSONReader reader) throws DeserializationException, uk.ac.standrews.cs.nds.rpc.DeserializationException {
 
         final InetSocketAddress address = deserializeInetSocketAddress(reader);
 
         return SystemTableProxy.getProxy(address);
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeIDatabaseInstanceRemote(final IDatabaseInstanceRemote source, final JSONWriter writer) throws JSONException, RPCException {
 
         serializeInetSocketAddress(source.getAddress(), writer);
 
     }
 
     public IDatabaseInstanceRemote deserializeIDatabaseInstanceRemote(final JSONReader reader) throws DeserializationException {
 
         final InetSocketAddress address = deserializeInetSocketAddress(reader);
 
         return DatabaseInstanceProxy.getProxy(address);
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeDatabaseID(final DatabaseID source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
 
         }
         else {
             writer.object();
 
             writer.key(DATABASE_URL);
             writer.value(source.getURLwithRMIPort());
 
             writer.endObject();
         }
     }
 
     public DatabaseID deserializeDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(DATABASE_URL);
             final DatabaseID url = DatabaseID.parseURL(reader.stringValue());
 
             reader.endObject();
             return url;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeTableInfo(final TableInfo source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
             writer.key(TABLE_NAME);
             writer.value(source.getTableName());
             writer.key(SCHEMA_NAME);
             writer.value(source.getSchemaName());
             writer.key(MODIFICATION_ID);
             writer.value(source.getModificationID());
             writer.key(TABLE_SET);
             writer.value(source.getTableSet());
             writer.key(TABLE_TYPE);
             writer.value(source.getTableType());
             writer.key(DATABASE_LOCATION);
             serializeDatabaseID(source.getDatabaseID(), writer);
             writer.endObject();
         }
     }
 
     public TableInfo deserializeTableInfo(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(TABLE_NAME);
             final String tableName = reader.stringValue();
 
             reader.key(SCHEMA_NAME);
             final String schemaName = reader.stringValue();
 
             reader.key(MODIFICATION_ID);
             final long modificationID = reader.intValue();
 
             reader.key(TABLE_SET);
             final int tableSet = reader.intValue();
 
             reader.key(TABLE_TYPE);
             final String tableType = null;
             if (!reader.checkNull()) {
                 reader.stringValue();
             }
 
             reader.key(DATABASE_LOCATION);
             final DatabaseID dbLocation = deserializeDatabaseID(reader);
 
             reader.endObject();
 
             return new TableInfo(tableName, schemaName, modificationID, tableSet, tableType, dbLocation);
         }
         catch (final Exception e) {
             ErrorHandling.exceptionError(e, "Couldn't deserialize TableInfo");
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeLockType(final LockType source, final JSONWriter writer) throws JSONException {
 
         writer.value(source.toString());
     }
 
     public LockType deserializeLockType(final String s) throws DeserializationException {
 
         try {
             return LockType.valueOf(LockType.class, s);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeLockRequest(final LockRequest source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(DATABASE_MAKING_REQUEST);
             serializeDatabaseInstanceWrapper(source.getRequestLocation(), writer);
             writer.key(SESSION_ID);
             writer.value(source.getSessionID());
             writer.endObject();
         }
     }
 
     public LockRequest deserializeLockRequest(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
             reader.object();
             reader.key(DATABASE_MAKING_REQUEST);
             final DatabaseInstanceWrapper databaseMakingRequest = deserializeDatabaseInstanceWrapper(reader);
 
             reader.key(SESSION_ID);
             final int sessionID = reader.intValue();
 
             reader.endObject();
 
             return new LockRequest(databaseMakingRequest, sessionID);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeReplicaManager(final ReplicaManager source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(ALL_REPLICAS);
             serializeMapDatabaseInstanceWrapperInteger(source.getActiveReplicas(), writer);
             writer.key(ACTIVE_REPLICAS);
             serializeMapDatabaseInstanceWrapperInteger(source.getAllReplicas(), writer);
             writer.key(PRIMARY_LOCATION);
             serializeDatabaseInstanceWrapper(source.getPrimaryLocation(), writer);
             writer.endObject();
         }
     }
 
     public ReplicaManager deserializeReplicaManager(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
             reader.key(ALL_REPLICAS);
 
             final Map<DatabaseInstanceWrapper, Integer> allReplicas = deserializeMapDatabaseInstanceWrapperInteger(reader);
 
             reader.key(ACTIVE_REPLICAS);
 
             final Map<DatabaseInstanceWrapper, Integer> activeReplicas = deserializeMapDatabaseInstanceWrapperInteger(reader);
 
             reader.key(PRIMARY_LOCATION);
 
             final DatabaseInstanceWrapper primaryLocation = deserializeDatabaseInstanceWrapper(reader);
 
             reader.endObject();
 
             return new ReplicaManager(allReplicas, activeReplicas, primaryLocation);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeDatabaseInstanceWrapper(final DatabaseInstanceWrapper source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(DATABASE_URL);
             serializeDatabaseID(source.getURL(), writer);
             writer.key(DATABASE_INSTANCE);
             serializeIDatabaseInstanceRemote(source.getDatabaseInstance(), writer);
             writer.key(ACTIVE);
             writer.value(source.getActive());
             writer.endObject();
         }
     }
 
     public DatabaseInstanceWrapper deserializeDatabaseInstanceWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(DATABASE_URL);
             final DatabaseID databaseURL = deserializeDatabaseID(reader);
 
             reader.key(DATABASE_INSTANCE);
             final IDatabaseInstanceRemote databaseInstance = deserializeIDatabaseInstanceRemote(reader);
 
             reader.key(ACTIVE);
             final boolean active = reader.booleanValue();
 
             reader.endObject();
 
             return new DatabaseInstanceWrapper(databaseURL, databaseInstance, active);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeTableManagerWrapper(final TableManagerWrapper source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(TABLE_INFO);
             serializeTableInfo(source.getTableInfo(), writer);
             writer.key(TABLE_MANAGER);
             serializeITableManagerRemote(source.getTableManager(), writer);
             writer.key(TABLE_MANAGER_URL);
             serializeDatabaseID(source.getURL(), writer);
             writer.endObject();
         }
     }
 
     public TableManagerWrapper deserializeTableManagerWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(TABLE_INFO);
             final TableInfo tableInfo = deserializeTableInfo(reader);
 
             reader.key(TABLE_MANAGER);
             final ITableManagerRemote tableManager = deserializeITableManagerRemote(reader);
             reader.key(TABLE_MANAGER_URL);
             final DatabaseID tableManagerURL = deserializeDatabaseID(reader);
 
             reader.endObject();
 
             return new TableManagerWrapper(tableInfo, tableManager, tableManagerURL);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeTableProxy(final TableProxy source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(LOCK_GRANTED);
             serializeLockType(source.getLockGranted(), writer);
             writer.key(LOCK_REQUESTED);
             serializeLockType(source.getLockRequested(), writer);
             writer.key(TABLE_NAME);
             serializeTableInfo(source.getTableName(), writer);
             writer.key(ALL_REPLICAS);
             serializeMapDatabaseInstanceWrapperInteger(source.getAllReplicas(), writer);
             writer.key(TABLE_MANAGER);
             serializeITableManagerRemote(source.getTableManager(), writer);
             writer.key(REQUESTING_DATABASE);
             serializeLockRequest(source.getRequestingDatabase(), writer);
             writer.key(UPDATE_ID);
             writer.value(source.getUpdateID());
             writer.endObject();
         }
     }
 
     public TableProxy deserializeTableProxy(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
             reader.object();
             reader.key(LOCK_GRANTED);
             final LockType lockGranted = deserializeLockType(reader.stringValue());
             reader.key(LOCK_REQUESTED);
             final LockType lockRequested = deserializeLockType(reader.stringValue());
             reader.key(TABLE_NAME);
             final TableInfo tableName = deserializeTableInfo(reader);
             reader.key(ALL_REPLICAS);
             final Map<DatabaseInstanceWrapper, Integer> allReplicas = deserializeMapDatabaseInstanceWrapperInteger(reader);
             reader.key(TABLE_MANAGER);
             final ITableManagerRemote tableManager = deserializeITableManagerRemote(reader);
             reader.key(REQUESTING_DATABASE);
             final LockRequest requestingDatabase = deserializeLockRequest(reader);
             reader.key(UPDATE_ID);
             final int updateID = reader.intValue();
 
             reader.endObject();
 
             return new TableProxy(lockGranted, tableName, allReplicas, tableManager, requestingDatabase, updateID, lockRequested);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeCollectionDatabaseInstanceWrapper(final Collection<DatabaseInstanceWrapper> source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final DatabaseInstanceWrapper instance : source) {
                 serializeDatabaseInstanceWrapper(instance, writer);
             }
             writer.endArray();
         }
 
     }
 
     public Set<DatabaseInstanceWrapper> deserializeSetDatabaseInstanceWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
 
             reader.array();
             final Set<DatabaseInstanceWrapper> result = new HashSet<DatabaseInstanceWrapper>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeDatabaseInstanceWrapper(reader));
             }
 
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     public Collection<DatabaseInstanceWrapper> deserializeCollectionDatabaseInstanceWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
 
             reader.array();
 
             final Collection<DatabaseInstanceWrapper> result = new ArrayList<DatabaseInstanceWrapper>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeDatabaseInstanceWrapper(reader));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeMapDatabaseIDDatabaseInstanceWrapper(final Map<DatabaseID, DatabaseInstanceWrapper> source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(KEYS);
             serializeSetDatabaseID(source.keySet(), writer);
             writer.key(VALUES);
             serializeCollectionDatabaseInstanceWrapper(source.values(), writer);
             writer.endObject();
         }
     }
 
     public Map<DatabaseID, DatabaseInstanceWrapper> deserializeMapDatabaseIDDatabaseInstanceWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
             reader.object();
             reader.key(KEYS);
             final Set<DatabaseID> keys = deserializeSetDatabaseID(reader);
             reader.key(VALUES);
             final Collection<DatabaseInstanceWrapper> values = deserializeCollectionDatabaseInstanceWrapper(reader);
 
             final HashMap<DatabaseID, DatabaseInstanceWrapper> result = new HashMap<DatabaseID, DatabaseInstanceWrapper>();
 
             final Iterator<DatabaseID> it1 = keys.iterator();
             final Iterator<DatabaseInstanceWrapper> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final DatabaseID arg0 = it1.next();
                 final DatabaseInstanceWrapper arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             reader.endObject();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeMapTableInfoTableManagerWrapper(final Map<TableInfo, TableManagerWrapper> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(KEYS);
             serializeSetTableInfo(source.keySet(), writer);
             writer.key(VALUES);
             serializeCollectionTableManagerWrapper(source.values(), writer);
             writer.endObject();
         }
     }
 
     public Map<TableInfo, TableManagerWrapper> deserializeMapTableInfoTableManagerWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(KEYS);
 
             final Set<TableInfo> keys = deserializeSetTableInfo(reader);
 
             reader.key(VALUES);
 
             final Collection<TableManagerWrapper> values = deserializeCollectionTableManagerWrapper(reader);
 
             final HashMap<TableInfo, TableManagerWrapper> result = new HashMap<TableInfo, TableManagerWrapper>();
 
             final Iterator<TableInfo> it1 = keys.iterator();
             final Iterator<TableManagerWrapper> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final TableInfo arg0 = it1.next();
                 final TableManagerWrapper arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             reader.endObject();
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeCollectionTableManagerWrapper(final Collection<TableManagerWrapper> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final TableManagerWrapper instance : source) {
                 serializeTableManagerWrapper(instance, writer);
             }
             writer.endArray();
         }
     }
 
     public Set<TableManagerWrapper> deserializeSetTableManagerWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Set<TableManagerWrapper> result = new HashSet<TableManagerWrapper>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeTableManagerWrapper(reader));
             }
 
             reader.endArray();
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeMapTableInfoSetDatabaseID(final Map<TableInfo, Set<DatabaseID>> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(KEYS);
             serializeSetTableInfo(source.keySet(), writer);
             writer.key(VALUES);
             serializeCollectionSetDatabaseID(source.values(), writer);
             writer.endObject();
         }
     }
 
     public Map<TableInfo, Set<DatabaseID>> deserializeMapTableInfoSetDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(KEYS);
 
             final Set<TableInfo> keys = deserializeSetTableInfo(reader);
 
             reader.key(VALUES);
             final Collection<Set<DatabaseID>> values = deserializeCollectionSetDatabaseID(reader);
 
             final HashMap<TableInfo, Set<DatabaseID>> result = new HashMap<TableInfo, Set<DatabaseID>>();
 
             final Iterator<TableInfo> it1 = keys.iterator();
             final Iterator<Set<DatabaseID>> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final TableInfo arg0 = it1.next();
                 final Set<DatabaseID> arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             reader.endObject();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeSetTableManagerWrapper(final Set<TableManagerWrapper> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final TableManagerWrapper instance : source) {
                 serializeTableManagerWrapper(instance, writer);
             }
             writer.endArray();
         }
     }
 
     public Collection<TableManagerWrapper> deserializeCollectionTableManagerWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Collection<TableManagerWrapper> result = new ArrayList<TableManagerWrapper>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeTableManagerWrapper(reader));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeMapTableInfoDatabaseID(final Map<TableInfo, DatabaseID> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(KEYS);
             serializeSetTableInfo(source.keySet(), writer);
             writer.key(VALUES);
             serializeCollectionDatabaseID(source.values(), writer);
             writer.endObject();
         }
     }
 
     public Map<TableInfo, DatabaseID> deserializeMapTableInfoDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(KEYS);
 
             final Set<TableInfo> keys = deserializeSetTableInfo(reader);
 
             reader.key(VALUES);
             final Collection<DatabaseID> values = deserializeCollectionDatabaseID(reader);
 
             final HashMap<TableInfo, DatabaseID> result = new HashMap<TableInfo, DatabaseID>();
 
             final Iterator<TableInfo> it1 = keys.iterator();
             final Iterator<DatabaseID> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final TableInfo arg0 = it1.next();
                 final DatabaseID arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             reader.endObject();
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeCollectionCommitResult(final Collection<CommitResult> source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final CommitResult instance : source) {
                 serializeCommitResult(instance, writer);
             }
             writer.endArray();
         }
     }
 
     public Set<CommitResult> deserializeCollectionCommitResult(final JSONReader reader) throws DeserializationException {
 
         try {
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Set<CommitResult> result = new HashSet<CommitResult>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeCommitResult(reader));
             }
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeSetString(final Set<String> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final String s : source) {
                 writer.value(s);
             }
             writer.endArray();
         }
     }
 
     public Set<String> deserializeSetString(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Set<String> result = new HashSet<String>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(reader.stringValue());
             }
 
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeQueueDatabaseInstanceWrapper(final Queue<DatabaseInstanceWrapper> source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final DatabaseInstanceWrapper instance : source) {
                 serializeDatabaseInstanceWrapper(instance, writer);
             }
             writer.endArray();
         }
     }
 
     public Queue<DatabaseInstanceWrapper> deserializeQueueDatabaseInstanceWrapper(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Queue<DatabaseInstanceWrapper> result = new PriorityQueue<DatabaseInstanceWrapper>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeDatabaseInstanceWrapper(reader));
             }
 
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             ErrorHandling.exceptionErrorNoEvent(e, "Failure on database instance wrapper queue deserialization.");
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeMapDatabaseInstanceWrapperInteger(final Map<DatabaseInstanceWrapper, Integer> source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(KEYS);
             serializeCollectionDatabaseInstanceWrapper(source.keySet(), writer);
             writer.key(VALUES);
             serializeCollectionInteger(source.values(), writer);
             writer.endObject();
         }
     }
 
     public Map<DatabaseInstanceWrapper, Integer> deserializeMapDatabaseInstanceWrapperInteger(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(KEYS);
             final Set<DatabaseInstanceWrapper> keys = deserializeSetDatabaseInstanceWrapper(reader);
 
             reader.key(VALUES);
             final Collection<Integer> values = deserializeCollectionInteger(reader);
 
             final HashMap<DatabaseInstanceWrapper, Integer> result = new HashMap<DatabaseInstanceWrapper, Integer>();
 
             final Iterator<DatabaseInstanceWrapper> it1 = keys.iterator();
             final Iterator<Integer> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final DatabaseInstanceWrapper arg0 = it1.next();
                 final Integer arg1 = it2.next();
                 result.put(arg0, arg1);
             }
             reader.endObject();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeCollectionInteger(final Collection<Integer> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final Integer instance : source) {
                 writer.value(instance);
             }
             writer.endArray();
         }
     }
 
     private Collection<Integer> deserializeCollectionInteger(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
             final Collection<Integer> result = new ArrayList<Integer>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(reader.intValue());
             }
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeSetDatabaseID(final Set<DatabaseID> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final DatabaseID instance : source) {
                 serializeDatabaseID(instance, writer);
             }
             writer.endArray();
         }
     }
 
     private Set<DatabaseID> deserializeSetDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Set<DatabaseID> result = new HashSet<DatabaseID>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeDatabaseID(reader));
             }
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeSetTableInfo(final Set<TableInfo> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final TableInfo instance : source) {
                 serializeTableInfo(instance, writer);
             }
             writer.endArray();
         }
     }
 
     private Set<TableInfo> deserializeSetTableInfo(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Set<TableInfo> result = new HashSet<TableInfo>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeTableInfo(reader));
             }
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeCollectionSetDatabaseID(final Collection<Set<DatabaseID>> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final Set<DatabaseID> instance : source) {
                 serializeSetDatabaseID(instance, writer);
             }
             writer.endArray();
         }
     }
 
     private Set<Set<DatabaseID>> deserializeSetSetDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final HashSet<Set<DatabaseID>> result = new HashSet<Set<DatabaseID>>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeSetDatabaseID(reader));
             }
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     private Collection<Set<DatabaseID>> deserializeCollectionSetDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Collection<Set<DatabaseID>> result = new ArrayList<Set<DatabaseID>>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeSetDatabaseID(reader));
             }
 
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeCollectionDatabaseID(final Collection<DatabaseID> source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.array();
             for (final DatabaseID instance : source) {
                 serializeDatabaseID(instance, writer);
             }
             writer.endArray();
         }
 
     }
 
     private Collection<DatabaseID> deserializeCollectionDatabaseID(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.array();
 
             final Collection<DatabaseID> result = new ArrayList<DatabaseID>();
             while (!reader.have(JSONReader.ENDARRAY)) {
                 result.add(deserializeDatabaseID(reader));
             }
             reader.endArray();
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeCommitResult(final CommitResult source, final JSONWriter writer) throws JSONException, RPCException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(COMMIT);
             writer.value(source.isCommit());
             writer.key(WRAPPER);
             serializeDatabaseInstanceWrapper(source.getDatabaseInstanceWrapper(), writer);
             writer.key(UPDATE_ID);
             writer.value(source.getUpdateID());
             writer.key(EXPECTED_UPDATE_ID);
             writer.value(source.getExpectedUpdateID());
             writer.key(TABLE_NAME);
             serializeTableInfo(source.getTable(), writer);
             writer.endObject();
         }
     }
 
     private CommitResult deserializeCommitResult(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(COMMIT);
 
             final boolean commit = reader.booleanValue();
             reader.key(WRAPPER);
             final DatabaseInstanceWrapper wrapper = deserializeDatabaseInstanceWrapper(reader);
             reader.key(UPDATE_ID);
             final int updateID = reader.intValue();
             reader.key(EXPECTED_UPDATE_ID);
             final int expectedUpdateID = reader.intValue();
             reader.key(TABLE_NAME);
             final TableInfo tableName = deserializeTableInfo(reader);
 
             reader.endObject();
             return new CommitResult(commit, wrapper, updateID, expectedUpdateID, tableName);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public MachineMonitoringData deserializeMachineMonitoringData(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(DATABASE_ID);
 
             final DatabaseID databaseID = deserializeDatabaseID(reader);
            reader.key(DATABASE_ID);
            final MachineUtilisationData machineUtilData = deserializeMachineUtilizationData(reader);
            reader.key(DATABASE_ID);
             final SystemInfoData sysInfoData = deserializeSystemInfoData(reader);
 
            reader.key(DATABASE_ID);
             final FileSystemData fsData = deserializeFileSystemData(reader);
            reader.key(DATABASE_ID);
             final int measurements_before_summary = reader.intValue();
             reader.endObject();
 
             return new MachineMonitoringData(databaseID, sysInfoData, machineUtilData, fsData, measurements_before_summary);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     public void serializeMachineMonitoringData(final MachineMonitoringData source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(DATABASE_ID);
             serializeDatabaseID(source.getDatabaseID(), writer);
             writer.key(SYSTEM_INFO_DATA);
             serializeSystemInfoData(source.getSystemInfoData(), writer);
             writer.key(MACHINE_UTILIZATION_DATA);
             serializeMachineUtilizationData(source.getMachineUtilData(), writer);
             writer.key(FS_DATA);
             serializeFileSystemData(source.getFsData(), writer);
             writer.key(MEASUREMENTS_BEFORE_SUMMARY);
             writer.value(source.getMeasurementsBeforeSummary());
             writer.endObject();
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeMachineUtilizationData(final MachineUtilisationData source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(CPU_USER);
             writer.value(source.getCpuUserTotal());
             writer.key(CPU_SYS);
             writer.value(source.getCpuSysTotal());
             writer.key(CPU_IDLE);
             writer.value(source.getCpuIdleTotal());
             writer.key(CPU_WAIT);
             writer.value(source.getCpuWaitTotal());
             writer.key(CPU_NICE);
             writer.value(source.getCpuNiceTotal());
 
             writer.key(MEMORY_USED);
             writer.value(source.getMemoryUsed());
             writer.key(MEMORY_FREE);
             writer.value(source.getMemoryFree());
             writer.key(SWAP_USED);
             writer.value(source.getSwapUsed());
             writer.key(SWAP_FREE);
             writer.value(source.getSwapFree());
             writer.endObject();
         }
     }
 
     private MachineUtilisationData deserializeMachineUtilizationData(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(CPU_USER);
             final double cpu_user_total = reader.doubleValue();
             reader.key(CPU_SYS);
             final double cpu_sys_total = reader.doubleValue();
             reader.key(CPU_IDLE);
             final double cpu_idle_total = reader.doubleValue();
             reader.key(CPU_WAIT);
             final double cpu_wait_total = reader.doubleValue();
             reader.key(CPU_NICE);
             final double cpu_nice_total = reader.doubleValue();
 
             reader.key(MEMORY_USED);
             final long memory_used = reader.longValue();
             reader.key(MEMORY_FREE);
             final long memory_free = reader.longValue();
             reader.key(SWAP_USED);
             final long swap_used = reader.longValue();
             reader.key(SWAP_FREE);
             final long swap_free = reader.longValue();
 
             reader.endObject();
 
             final MachineUtilisationData mud = new MachineUtilisationData();
 
             mud.cpu_user_total = cpu_user_total;
             mud.cpu_sys_total = cpu_sys_total;
             mud.cpu_idle_total = cpu_idle_total;
             mud.cpu_idle_total = cpu_wait_total;
             mud.cpu_nice_total = cpu_nice_total;
 
             mud.memory_used = memory_used;
             mud.memory_free = memory_free;
             mud.swap_used = swap_used;
             mud.swap_free = swap_free;
 
             return mud;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeFileSystemData(final FileSystemData source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
 
             writer.key(FS_LOCATION);
             writer.value(source.getFileSystemLocation());
             writer.key(FS_TYPE);
             writer.value(source.getFileSystemType());
             writer.key(FS_NAME);
             writer.value(source.getFileSystemName());
             writer.key(FS_SPACE_USED);
             writer.value(source.getSpaceUsed());
             writer.key(FS_SPACE_FREE);
             writer.value(source.getSpaceFree());
 
             writer.key(FS_SIZE);
             writer.value(source.getSize());
             writer.key(FS_NUMBER_OF_FILES);
             writer.value(source.getNumberOfFiles());
             writer.key(FS_DISK_READS);
             writer.value(source.getNumberOfDiskReads());
             writer.key(FS_BYTES_READ);
             writer.value(source.getBytesRead());
             writer.key(FS_DISK_WRITES);
             writer.value(source.getNumberOfDiskWrites());
             writer.key(FS_BYTES_WRITTEN);
             writer.value(source.getBytesWritten());
             writer.endObject();
         }
     }
 
     private FileSystemData deserializeFileSystemData(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(FS_LOCATION);
             final String file_system_location = reader.stringValue();
             reader.key(FS_TYPE);
             final String file_system_type = reader.stringValue();
             reader.key(FS_NAME);
             final String file_system_name = reader.stringValue();
 
             reader.key(FS_SPACE_USED);
             final long fs_space_used = reader.longValue();
             reader.key(FS_SPACE_FREE);
             final long fs_space_free = reader.longValue();
             reader.key(FS_SIZE);
             final long fs_size = reader.longValue();
             reader.key(FS_NUMBER_OF_FILES);
             final long fs_number_of_files = reader.longValue();
             reader.key(FS_DISK_READS);
             final long fs_disk_reads = reader.longValue();
             reader.key(FS_BYTES_READ);
             final long fs_bytes_read = reader.longValue();
             reader.key(FS_DISK_WRITES);
             final long fs_disk_writes = reader.longValue();
             reader.key(FS_BYTES_WRITTEN);
             final long fs_bytes_written = reader.longValue();
 
             reader.endObject();
 
             final FileSystemData fsd = new FileSystemData();
 
             fsd.file_system_location = file_system_location;
             fsd.file_system_type = file_system_type;
             fsd.file_system_name = file_system_name;
 
             fsd.fs_space_used = fs_space_used;
             fsd.fs_space_free = fs_space_free;
             fsd.fs_size = fs_size;
             fsd.fs_number_of_files = fs_number_of_files;
             fsd.fs_disk_reads = fs_disk_reads;
             fsd.fs_bytes_read = fs_bytes_read;
             fsd.fs_disk_writes = fs_disk_writes;
             fsd.fs_bytes_written = fs_bytes_written;
 
             return fsd;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private void serializeSystemInfoData(final SystemInfoData source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
             writer.key(MACHINE_ID);
             writer.value(source.machine_id);
             writer.key(OS_NAME);
             writer.value(source.os_name);
             writer.key(OS_VERSION);
             writer.value(source.os_version);
             writer.key(HOSTNAME);
             writer.value(source.hostname);
             writer.key(PRIMARY_IP);
             writer.value(source.primary_ip);
             writer.key(DEFAULT_GATEWAY);
             writer.value(source.default_gateway);
             writer.key(CPU_VENDOR);
             writer.value(source.cpu_vendor);
             writer.key(CPU_MODEL);
             writer.value(source.cpu_model);
             writer.key(NUMBER_OF_CORES);
             writer.value(source.number_of_cores);
             writer.key(NUMBER_OF_CPUS);
             writer.value(source.number_of_cpus);
             writer.key(CPU_CLOCK_SPEED);
             writer.value(source.cpu_clock_speed);
             writer.key(CPU_CACHE_SIZE);
             writer.value(source.cpu_cache_size);
             writer.key(MEMORY_TOTAL);
             writer.value(source.memory_total);
             writer.key(SWAP_TOTAL);
             writer.value(source.swap_total);
             writer.endObject();
         }
     }
 
     private SystemInfoData deserializeSystemInfoData(final JSONReader reader) throws DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
             reader.object();
 
             reader.key(MACHINE_ID);
             final String machine_id = reader.stringValue();
 
             // OS Info.
             reader.key(OS_NAME);
             final String os_name = reader.stringValue();
             reader.key(OS_VERSION);
             final String os_version = reader.stringValue();
 
             // Network Info.
             reader.key(HOSTNAME);
             final String hostname = reader.stringValue();
             reader.key(PRIMARY_IP);
             final String primary_ip = reader.stringValue();
             reader.key(DEFAULT_GATEWAY);
             final String default_gateway = reader.stringValue();
 
             // CPU Info.
             reader.key(CPU_VENDOR);
             final String cpu_vendor = reader.stringValue();
             reader.key(CPU_MODEL);
             final String cpu_model = reader.stringValue();
             reader.key(NUMBER_OF_CORES);
             final int number_of_cores = reader.intValue();
             reader.key(NUMBER_OF_CPUS);
             final int number_of_cpus = reader.intValue();
             reader.key(CPU_CLOCK_SPEED);
             final int cpu_clock_speed = reader.intValue();
             reader.key(CPU_CACHE_SIZE);
             final long cpu_cache_size = reader.longValue();
 
             // Memory Info.
             reader.key(MEMORY_TOTAL);
             final long memory_total = reader.longValue();
             reader.key(SWAP_TOTAL);
             final long swap_total = reader.longValue();
 
             reader.endObject();
 
             final SystemInfoData fsd = new SystemInfoData(machine_id, os_name, os_version, hostname, primary_ip, default_gateway, cpu_vendor, cpu_model, number_of_cores, number_of_cpus, cpu_clock_speed, cpu_cache_size, memory_total, swap_total);
 
             return fsd;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public void serializeActionRequest(final Metric source, final JSONWriter writer) throws JSONException {
 
         if (source == null) {
             writer.value(null);
         }
         else {
 
             writer.object();
             writer.key(CPU);
             writer.value(source.cpu);
             writer.key(DISK);
             writer.value(source.disk);
             writer.key(MEMORY);
             writer.value(source.memory);
             writer.key(NETWORK);
             writer.value(source.network);
             writer.key(EXPECTED_TIME_TO_COMPLETION);
             writer.value(source.expectedTimeToCompletion);
             writer.key(IMMEDIATE_DISK_SPACE);
             writer.value(source.immediateDiskSpace);
 
             writer.endObject();
         }
     }
 
     public Metric deserializeActionRequest(final JSONReader reader) throws JSONException, DeserializationException {
 
         try {
 
             if (reader.checkNull()) { return null; }
 
             reader.object();
 
             reader.key(CPU);
 
             double cpu = 0;
             if (reader.have(JSONReader.DOUBLE)) {
                 cpu = reader.doubleValue();
             }
             else {
                 cpu = reader.intValue();
             }
 
             reader.key(DISK);
             final double disk = reader.doubleValue();
             reader.key(MEMORY);
             final double memory = reader.doubleValue();
 
             reader.key(NETWORK);
             final double network = reader.doubleValue();
             reader.key(EXPECTED_TIME_TO_COMPLETION);
             long expectedTimeToCompletion = 0;
             if (reader.have(JSONReader.LONG)) {
                 expectedTimeToCompletion = reader.longValue();
             }
             else if (reader.have(JSONReader.DOUBLE)) {
                 expectedTimeToCompletion = new Double(reader.doubleValue()).longValue();
             }
             else {
                 expectedTimeToCompletion = reader.intValue();
             }
             reader.key(IMMEDIATE_DISK_SPACE);
 
             long immediateDiskSpace = 0;
             if (reader.have(JSONReader.LONG)) {
                 immediateDiskSpace = reader.longValue();
             }
             else if (reader.have(JSONReader.DOUBLE)) {
                 immediateDiskSpace = new Double(reader.doubleValue()).longValue();
             }
             else {
                 immediateDiskSpace = reader.intValue();
             }
 
             reader.endObject();
 
             return new Metric(expectedTimeToCompletion, immediateDiskSpace, cpu, memory, network, disk);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 }
