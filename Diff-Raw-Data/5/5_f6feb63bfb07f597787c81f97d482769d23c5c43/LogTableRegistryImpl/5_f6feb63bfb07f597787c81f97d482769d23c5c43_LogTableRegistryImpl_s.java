 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage.engine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.Lock;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigCollection;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigIterator;
 import org.araqne.confdb.ConfigService;
 import org.araqne.confdb.ConfigTransaction;
 import org.araqne.confdb.Predicates;
 import org.araqne.logstorage.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logstorage-table-registry")
 @Provides
 public class LogTableRegistryImpl implements LogTableRegistry {
 
 	private final Logger logger = LoggerFactory.getLogger(LogTableRegistryImpl.class.getName());
 
 	@Requires
 	private ConfigService conf;
 
 	@Requires
 	private LogFileServiceRegistry lfsRegistry;
 
 	/**
 	 * table id generator
 	 */
 	private AtomicInteger nextTableId;
 
 	/**
 	 * table name to id mappings
 	 */
 	private ConcurrentMap<String, Integer> tableIDs;
 
 	/**
 	 * table name to schema mappings
 	 */
 	private ConcurrentMap<Integer, TableSchema> tableSchemas;
 
 	private CopyOnWriteArraySet<TableEventListener> callbacks;
 
 	public LogTableRegistryImpl() {
 		tableSchemas = new ConcurrentHashMap<Integer, TableSchema>();
 		tableIDs = new ConcurrentHashMap<String, Integer>();
 		callbacks = new CopyOnWriteArraySet<TableEventListener>();
 
 		// migrate _filetype metadata to configs
 		migrateMetadata();
 
 		// load table id mappings
 		loadTableMappings();
 	}
 
 	private void migrateMetadata() {
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 
 		ConfigIterator it = null;
 		try {
 			it = db.findAll(TableSchema.class);
 			while (it.hasNext()) {
 				Config c = it.next();
 				TableSchema schema = c.getDocument(TableSchema.class);
 				if (schema.getPrimaryStorage() != null)
 					continue;
 
 				// do not remove _filetype immediately
 				String fileType = schema.getMetadata().get("_filetype");
 				if (fileType == null)
 					fileType = "v2";
 
 				StorageConfig primaryStorage = new StorageConfig(fileType, schema.getMetadata().get("base_path"));
 
 				String compression = schema.getMetadata().get("compression");
 				if (compression != null)
 					primaryStorage.getConfigs().add(new TableConfig("compression", compression));
 
 				String crypto = schema.getMetadata().get("crypto");
 				if (crypto != null)
 					primaryStorage.getConfigs().add(new TableConfig("crypto", crypto));
 
 				schema.setPrimaryStorage(primaryStorage);
 
 				db.update(c, schema);
 				logger.info("araqne logstorage: migrated table [{}] _filetype [{}] metadata to schema", schema.getName(),
 						fileType);
 			}
 		} finally {
 			if (it != null)
 				it.close();
 		}
 	}
 
 	private void loadTableMappings() {
 		int maxId = 0;
 
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		ConfigCollection col = db.getCollection(TableSchema.class);
 		if (col == null) {
 			col = db.ensureCollection(TableSchema.class);
 			ConfigCollection before = db.getCollection("org.araqne.logstorage.engine.LogTableSchema");
 			if (before != null) {
 				ConfigTransaction xact = db.beginTransaction();
 				try {
 					ConfigIterator it = before.findAll();
 					while (it.hasNext())
 						col.add(xact, it.next().getDocument());
 					xact.commit("araqne-logstorage", "migration from collection org.araqne.logstorage.engine.LogTableSchema");
 					db.dropCollection("org.araqne.logstorage.engine.LogTableSchema");
 				} catch (Throwable e) {
 					xact.rollback();
 					throw new IllegalStateException("migration failed");
 				}
 			}
 		}
 
 		ConfigIterator it = col.findAll();
 		for (TableSchema t : it.getDocuments(TableSchema.class)) {
 			// tableNames.put(t.getId(), t.getName());
 			tableSchemas.put(t.getId(), t);
 			tableIDs.put(t.getName(), t.getId());
 			tableLocks.put(t.getId(), new TableLockImpl(t.getId()));
 			if (maxId < t.getId())
 				maxId = t.getId();
 		}
 
 		nextTableId = new AtomicInteger(maxId);
 	}
 
 	@Override
 	public boolean exists(String tableName) {
 		Integer tid = tableIDs.get(tableName);
 		if (tid == null)
 			return false;
 
 		return tableSchemas.containsKey(tid);
 	}
 
 	@Override
 	public List<String> getTableNames() {
 		return new ArrayList<String>(tableIDs.keySet());
 	}
 
 	@Override
 	public List<TableSchema> getTableSchemas() {
 		List<TableSchema> l = new ArrayList<TableSchema>();
 		for (TableSchema s : tableSchemas.values())
 			l.add(s.clone());
 		return l;
 	}
 
 	@Override
 	public void createTable(TableSchema schema) {
 		if (tableIDs.containsKey(schema.getName()))
 			throw new IllegalStateException("table already exists: " + schema.getName());
 
 		verifyInputSchema(schema);
 		String tableName = schema.getName();
 
 		// set unique id
 		int newId = nextTableId.incrementAndGet();
 		TableSchema newSchema = schema.clone();
 		newSchema.setId(newId);
 
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		db.add(newSchema, "araqne-logstorage", "created " + tableName + " table");
 
 		// tableNames.put(newSchema.getId(), tableName);
 		tableIDs.put(tableName, newSchema.getId());
 		tableSchemas.put(newSchema.getId(), newSchema);
 		tableLocks.put(newSchema.getId(), new TableLockImpl(newSchema.getId()));
 
 		// invoke callbacks
 		for (TableEventListener callback : callbacks) {
 			try {
 				callback.onCreate(newSchema.clone());
 			} catch (Exception e) {
 				logger.warn("araqne logstorage: table event listener should not throw any exception", e);
 			}
 		}
 
 		logger.info("araqne logstorage: created table [{}] ", tableName);
 	}
 
 	@Override
 	public void alterTable(String tableName, TableSchema schema) {
 		schema = schema.clone();
 
 		TableSchema oldSchema = getTableSchema(tableName);
 		if (oldSchema == null)
 			throw new TableNotFoundException(tableName);
 
 		// validates all options
 		verifyInputSchema(schema);
 
 		if (!oldSchema.getName().equals(schema.getName()))
 			throw new IllegalArgumentException("rename is not supported: " + tableName + " => " + schema.getName());
 
 		LogFileService lfs = lfsRegistry.getLogFileService(schema.getPrimaryStorage().getType());
 		List<TableConfigSpec> specs = lfs.getConfigSpecs();
 
 		// check add, update, delete is accepted
 		Map<String, TableConfig> oldConfigs = toMap(oldSchema.getPrimaryStorage().getConfigs());
 		Map<String, TableConfig> newConfigs = toMap(schema.getPrimaryStorage().getConfigs());
 
 		Set<String> deleted = new HashSet<String>(oldConfigs.keySet());
 		deleted.removeAll(newConfigs.keySet());
 
 		Set<String> added = new HashSet<String>(newConfigs.keySet());
 		added.removeAll(oldConfigs.keySet());
 
 		Set<String> updated = new HashSet<String>();
 		for (String key : oldConfigs.keySet()) {
 			if (!newConfigs.containsKey(key))
 				continue;
 
 			updated.add(key);
 		}
 
 		for (String key : added) {
 			TableConfigSpec spec = find(specs, key);
 			TableConfig newConfig = newConfigs.get(key);
 
 			if (!spec.isOptional())
 				throw new IllegalArgumentException("table config [" + spec.getKey() + "] cannot be added, required config");
 
 			logger.debug("araqne logstorage: alter table [{}] added {}={}", new Object[] { tableName, key, newConfig });
 		}
 		;
 
 		for (String key : updated) {
 			TableConfigSpec spec = find(specs, key);
 			TableConfig oldConfig = oldConfigs.get(key);
 			TableConfig newConfig = newConfigs.get(key);
 
 			if (!spec.isUpdatable() && !oldConfig.equals(newConfig))
 				throw new IllegalArgumentException("table config [" + spec.getKey() + "] is not updatable");
 
 			logger.debug("araqne logstorage: alter table [{}] updated key [{}] value [{} -> {}]", new Object[] { tableName, key,
 					oldConfig, newConfig });
 		}
 
 		for (String key : deleted) {
 			TableConfigSpec spec = find(specs, key);
 
 			if (!spec.isUpdatable())
 				throw new IllegalArgumentException("table config [" + spec.getKey() + "] is not updatable");
 		}
 
 		// update schema
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 
 		Config c = db.findOne(TableSchema.class, Predicates.field("name", tableName));
 		if (c == null)
 			throw new TableNotFoundException(tableName);
 
 		db.update(c, schema, true, "araqne-logstorage", "altered " + tableName + " table");
 
 		tableSchemas.put(schema.getId(), schema);
 
 		// invoke callbacks
 		for (TableEventListener callback : callbacks) {
 			try {
 				callback.onAlter(oldSchema.clone(), schema.clone());
 			} catch (Exception e) {
 				logger.warn("araqne logstorage: table event listener should not throw any exception", e);
 			}
 		}
 	}
 
 	private TableConfigSpec find(List<TableConfigSpec> specs, String key) {
 		for (TableConfigSpec spec : specs)
 			if (spec.getKey().equals(key))
 				return spec;
 
 		throw new IllegalStateException("unsupported storage config: " + key);
 	}
 
 	/**
 	 * verifier for both create and alter table
 	 */
 	private void verifyInputSchema(TableSchema schema) {
 		verifyNotNull(schema.getName(), "table name is missing");
 		verifyNotNull(schema.getPrimaryStorage(), "file type is missing");
 
 		// verify file service readiness
 		verifyFileService(schema.getPrimaryStorage());
 
 		if (schema.getReplicaStorage() != null)
 			verifyFileService(schema.getReplicaStorage());
 
 		// config validation
 		LogFileService lfs = lfsRegistry.getLogFileService(schema.getPrimaryStorage().getType());
 		Map<String, TableConfig> configMap = toMap(schema.getPrimaryStorage().getConfigs());
 		validateTableConfigs(lfs.getConfigSpecs(), configMap);
 	}
 
 	private void validateTableConfigs(List<TableConfigSpec> specs, Map<String, TableConfig> configs) {
 		for (TableConfigSpec spec : specs) {
 			TableConfig c = configs.get(spec.getKey());
 			if (c == null && !spec.isOptional())
 				throw new IllegalArgumentException(spec.getKey() + " is missing");
 
 			if (c != null && spec.getValidator() != null)
 				spec.getValidator().validate(spec.getKey(), c.getValues());
 		}
 	}
 
 	private Map<String, TableConfig> toMap(List<TableConfig> l) {
 		Map<String, TableConfig> m = new HashMap<String, TableConfig>();
 		for (TableConfig c : l)
 			m.put(c.getKey(), c);
 		return m;
 	}
 
 	private void verifyNotNull(Object value, String msg) {
 		if (value == null)
 			throw new IllegalStateException(msg);
 	}
 
 	private void verifyFileService(StorageConfig storage) {
 		if (storage == null)
 			throw new IllegalArgumentException("storage engine is missing");
 
 		String[] installedTypes = lfsRegistry.getInstalledTypes();
 		for (String t : installedTypes)
 			if (t.equals(storage.getType()))
 				return;
 
 		throw new UnsupportedLogFileTypeException(storage.getType());
 	}
 
 	@Override
 	public void dropTable(String tableName) {
 		// check if table exists
 		TableSchema schema = getTableSchema(tableName, true);
 
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		Config c = db.findOne(TableSchema.class, Predicates.field("name", tableName));
 		if (c == null)
 			return;
 
 		db.remove(c, false, "araqne-logstorage", "dropped " + tableName + " table");
 
 		// invoke callbacks
 		for (TableEventListener callback : callbacks) {
 			try {
 				callback.onDrop(schema.clone());
 			} catch (Exception e) {
 				logger.warn("araqne logstorage: table event listener should not throw any exception", e);
 			}
 		}
 
 		TableLockImpl tableLock = tableLocks.remove(schema.getId());
 		// XXX : maybe we should invalidate lock.
 		TableSchema t = tableSchemas.remove(schema.getId());
 		// if (t != null)
 		// tableNames.remove(t.getId());
 		if (t != null)
 			tableIDs.remove(t.getName());
 	}
 
 	@Override
 	public TableSchema getTableSchema(String tableName) {
 		Integer tableID = tableIDs.get(tableName);
 		if (tableID != null) {
 			return getTableSchema(tableID, false);
 		} else
 			return null;
 	}
 
 	@Override
 	public TableSchema getTableSchema(String tableName, boolean required) {
 		Integer tableID = tableIDs.get(tableName);
 		if (required && tableID == null)
 			throw new TableNotFoundException(tableName);
 		return getTableSchema(tableID, required);
 	}
 
 	@Override
 	public TableSchema getTableSchema(int tableId) {
 		TableSchema schema = tableSchemas.get(tableId);
 		return schema == null ? null : schema.clone();
 	}
 
 	@Override
 	public TableSchema getTableSchema(int tableId, boolean required) {
 		TableSchema schema = tableSchemas.get(tableId);
 		if (required && schema == null)
 			throw new TableIDNotFoundException(tableId);
 
 		return schema == null ? null : schema.clone();
 	}
 
 	@Override
 	public void addListener(TableEventListener listener) {
 		callbacks.add(listener);
 	}
 
 	@Override
 	public void removeListener(TableEventListener listener) {
 		callbacks.remove(listener);
 	}
 
 	@Override
 	public TableLock getExclusiveTableLock(String tableName, String owner, String purpose) {
		TableLockImpl tableLock = tableLocks.get(getTableSchema(tableName).getId());
 		if (tableLock == null)
 			throw new TableNotFoundException(tableName);
 
 		return tableLock.writeLock(owner, purpose);
 	}
 
 	@Override
 	public TableLock getSharedTableLock(String tableName) {
		TableLockImpl tableLock = tableLocks.get(getTableSchema(tableName).getId());
 		if (tableLock == null)
 			throw new TableNotFoundException(tableName);
 
 		return tableLock.readLock();
 	}
 
 	private ConcurrentHashMap<Integer, TableLockImpl> tableLocks = new ConcurrentHashMap<Integer, TableLockImpl>();
 
 	@Override
 	public LockStatus getTableLockStatus(String tableName) {
 		TableLockImpl tableLock = tableLocks.get(tableIDs.get(tableName));
 		if (tableLock != null) {
 			String owner = tableLock.getOwner();
 			if (owner != null)
 				return new LockStatus(owner, tableLock.availableShared(), tableLock.getReentrantCount(), tableLock.getPurposes());
 			else
 				return new LockStatus(tableLock.availableShared());
 		} else {
 			return new LockStatus(-1);
 		}
 	}
 
 	@Override
 	public String getTableName(int tableId) {
 		return getTableSchema(tableId).getName();
 	}
 
 }
