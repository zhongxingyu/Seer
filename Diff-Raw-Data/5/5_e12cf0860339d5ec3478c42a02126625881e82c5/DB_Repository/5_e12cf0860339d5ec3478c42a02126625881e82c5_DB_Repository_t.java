 /*
  * Copyright 2006-2010 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.repo.sleepycat;
 
 import java.io.File;
 
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import com.sleepycat.db.CheckpointConfig;
 import com.sleepycat.db.DatabaseException;
 import com.sleepycat.db.Environment;
 import com.sleepycat.db.EnvironmentConfig;
 import com.sleepycat.db.LockDetectMode;
 import com.sleepycat.db.Transaction;
 import com.sleepycat.db.TransactionConfig;
 
 import com.amazon.carbonado.ConfigurationException;
 import com.amazon.carbonado.IsolationLevel;
 import com.amazon.carbonado.PersistDeniedException;
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.RepositoryException;
 import com.amazon.carbonado.Storable;
 
 import com.amazon.carbonado.spi.ExceptionTransformer;
 
 /**
  * Repository implementation backed by a Berkeley DB. Data is encoded in the DB
  * in a specialized format, and so this repository should not be used to open
  * arbitrary Berkeley databases. DBRepository has total schema ownership, and
  * so it updates type definitions in the storage layer automatically.
  *
  * @author Brian S O'Neill
  * @author Vidya Iyer
  * @author Olga Kuznetsova
  */
 class DB_Repository extends BDBRepository<Transaction> implements CompactionCapability {
     private static final TransactionConfig
         TXN_READ_UNCOMMITTED,        TXN_READ_COMMITTED,        TXN_REPEATABLE_READ,
         TXN_READ_UNCOMMITTED_NOWAIT, TXN_READ_COMMITTED_NOWAIT, TXN_REPEATABLE_READ_NOWAIT;
 
     private static final TransactionConfig TXN_SNAPSHOT;
 
     static {
         TXN_READ_UNCOMMITTED = new TransactionConfig();
         TXN_READ_UNCOMMITTED.setReadUncommitted(true);
 
         TXN_READ_COMMITTED = new TransactionConfig();
         TXN_READ_COMMITTED.setReadCommitted(true);
 
         TXN_REPEATABLE_READ = TransactionConfig.DEFAULT;
 
         TXN_READ_UNCOMMITTED_NOWAIT = new TransactionConfig();
         TXN_READ_UNCOMMITTED_NOWAIT.setReadUncommitted(true);
         TXN_READ_UNCOMMITTED_NOWAIT.setNoWait(true);
 
         TXN_READ_COMMITTED_NOWAIT = new TransactionConfig();
         TXN_READ_COMMITTED_NOWAIT.setReadCommitted(true);
         TXN_READ_COMMITTED_NOWAIT.setNoWait(true);
 
         TXN_REPEATABLE_READ_NOWAIT = new TransactionConfig();
         TXN_REPEATABLE_READ_NOWAIT.setNoWait(true);
 
         TXN_SNAPSHOT = new TransactionConfig();
         try {
             TXN_SNAPSHOT.setSnapshot(true);
         } catch (NoSuchMethodError e) {
             // Must be older BDB version.
         }
     }
 
     private static Map<String, Integer> cRegisterCountMap;
 
     /**
      * @return true if BDB environment should be opened with register option.
      */
     private synchronized static boolean register(String envHome) {
         if (cRegisterCountMap == null) {
             cRegisterCountMap = new HashMap<String, Integer>();
         }
         Integer count = cRegisterCountMap.get(envHome);
         count = (count == null) ? 1 : (count + 1);
         cRegisterCountMap.put(envHome, count);
         return count == 1;
     }
 
     private synchronized static void unregister(String envHome) {
         if (cRegisterCountMap != null) {
             Integer count = cRegisterCountMap.get(envHome);
             if (count != null) {
                 count -= 1;
                 if (count <= 0) {
                     cRegisterCountMap.remove(envHome);
                 } else {
                     cRegisterCountMap.put(envHome, count);
                 }
             }
         }
     }
 
     private static EnvironmentConfig createEnvConfig(BDBRepositoryBuilder builder)
         throws ConfigurationException
     {
         EnvironmentConfig envConfig;
         try {
             envConfig = (EnvironmentConfig) builder.getInitialEnvironmentConfig();
         } catch (ClassCastException e) {
             throw new ConfigurationException
                 ("Unsupported initial environment config. Must be instance of " +
                  EnvironmentConfig.class.getName(), e);
         }
 
         if (envConfig == null) {
             envConfig = new EnvironmentConfig();
             envConfig.setTransactional(true);
             envConfig.setAllowCreate(!builder.getReadOnly());
             envConfig.setTxnNoSync(builder.getTransactionNoSync());
             envConfig.setTxnWriteNoSync(builder.getTransactionWriteNoSync());
             envConfig.setPrivate(builder.isPrivate());
             envConfig.setRunRecovery(builder.isPrivate() && !builder.getReadOnly());
             if (builder.isMultiversion()) {
                 try {
                     envConfig.setMultiversion(true);
                 } catch (NoSuchMethodError e) {
                     throw new ConfigurationException
                         ("BDB product and version does not support MVCC");
                 }
             }
             envConfig.setLogInMemory(builder.getLogInMemory());
 
             try {
                 Integer maxSize = builder.getLogFileMaxSize();
                 if (maxSize != null) {
                     envConfig.setMaxLogFileSize(maxSize);
                 }
             } catch (NoSuchMethodError e) {
                 // Carbonado package might be older.
             }
 
             envConfig.setInitializeCache(true);
             envConfig.setInitializeLocking(true);
 
             // This class used to create the environment without initializing
             // the logging subsystem. When opening an existing environment the
             // requested subsystems must be a subset of the ones it was created
             // with. Initialize logging only when creating a new environment to
             // be backward compatible.
             if (!new File(builder.getEnvironmentHomeFile(), "__db.001").exists()) {
                 envConfig.setInitializeLogging(true);
             }
 
             Long cacheSize = builder.getCacheSize();
             envConfig.setCacheSize(cacheSize != null ? cacheSize : DEFAULT_CACHE_SIZE);
 
             envConfig.setMaxLocks(10000);
             envConfig.setMaxLockObjects(10000);
 
             envConfig.setLockTimeout(builder.getLockTimeoutInMicroseconds());
             envConfig.setTxnTimeout(builder.getTransactionTimeoutInMicroseconds());
         } else {
             if (!envConfig.getTransactional()) {
                 throw new IllegalArgumentException("EnvironmentConfig: getTransactional is false");
             }
 
             if (!envConfig.getInitializeCache()) {
                 throw new IllegalArgumentException
                     ("EnvironmentConfig: getInitializeCache is false");
             }
 
             if (envConfig.getCacheSize() <= 0) {
                 throw new IllegalArgumentException("EnvironmentConfig: invalid cache size");
             }
 
             if (!envConfig.getInitializeLocking()) {
                 throw new IllegalArgumentException
                     ("EnvironmentConfig: getInitializeLocking is false");
             }
         }
 
         return envConfig;
     }
 
     // Default cache size, in bytes.
     private static final long DEFAULT_CACHE_SIZE = 60 * 1024 * 1024;
 
     private final BDBProduct mProduct;
 
     final Environment mEnv;
     final boolean mMVCC;
     final boolean mReadOnly;
     final boolean mDatabasesTransactional;
     final Boolean mChecksum;
     volatile String mRegisteredHome;
 
     /**
      * Open the repository using the given BDB repository configuration.
      *
      * @throws IllegalArgumentException if name or environment home is null
      * @throws RepositoryException if there is a problem opening the environment
      */
     DB_Repository(AtomicReference<Repository> rootRef, BDBRepositoryBuilder builder)
         throws RepositoryException
     {
         this(rootRef, builder, DB_ExceptionTransformer.getInstance());
     }
 
     /**
      * Open the repository using the given BDB repository configuration.
      *
      * @throws IllegalArgumentException if name or environment home is null
      * @throws RepositoryException if there is a problem opening the environment
      */
     DB_Repository(AtomicReference<Repository> rootRef, BDBRepositoryBuilder builder,
                   ExceptionTransformer exTransformer)
         throws RepositoryException
     {
         super(rootRef, builder, exTransformer);
 
         mProduct = builder.getBDBProduct();
 
         if (builder.getRunFullRecovery() && !builder.getReadOnly()) {
             // Open with recovery, close, and then re-open.
             EnvironmentConfig envConfig = createEnvConfig(builder);
             envConfig.setRunRecovery(false);
             envConfig.setRunFatalRecovery(true);
 
             try {
                 new Environment(builder.getEnvironmentHomeFile(), envConfig).close();
             } catch (DatabaseException e) {
                 throw DB_ExceptionTransformer.getInstance().toRepositoryException(e);
             } catch (Throwable e) {
                 String message = "Unable to recover environment";
                 if (e.getMessage() != null) {
                     message += ": " + e.getMessage();
                 }
                 throw new RepositoryException(message, e);
             }
         }
 
         EnvironmentConfig envConfig = createEnvConfig(builder);
 
         // BDB 4.4 feature to check if any process exited uncleanly. If so, run
         // recovery. If any other processes are attached to the environment,
         // they will get recovery exceptions. They just need to exit and
         // restart. The current process can register at most once to the BDB
         // environment.
         try {
             if (!builder.isPrivate()) {
                 mRegisteredHome = builder.getEnvironmentHome();
                 if (register(mRegisteredHome)) {
                     envConfig.setRegister(true);
                     if (!builder.getReadOnly()) {
                         envConfig.setRunRecovery(true);
                     }
                 }
             }
         } catch (NoSuchMethodError e) {
             // Must be older BDB version.
         }
 
         boolean mvcc;
         try {
             mvcc = envConfig.getMultiversion();
         } catch (NoSuchMethodError e) {
             mvcc = false;
         }
         mMVCC = mvcc;
 
         boolean databasesTransactional = envConfig.getTransactional();
         if (builder.getDatabasesTransactional() != null) {
             databasesTransactional = builder.getDatabasesTransactional();
         }
         mDatabasesTransactional = databasesTransactional;
 
         mChecksum = builder.getChecksumEnabled();
 
         try {
             mEnv = new Environment(builder.getEnvironmentHomeFile(), envConfig);
         } catch (DatabaseException e) {
             throw DB_ExceptionTransformer.getInstance().toRepositoryException(e);
         } catch (Throwable e) {
             if (mRegisteredHome != null) {
                 unregister(mRegisteredHome);
             }
             String message = "Unable to open environment";
             if (e.getMessage() != null) {
                 message += ": " + e.getMessage();
             }
             throw new RepositoryException(message, e);
         }
 
         boolean readOnly = builder.getReadOnly();
         if (!readOnly && !builder.getDataHomeFile().canWrite()) {
             // Allow environment to be created, but databases are read-only.
             // This is only significant if data home differs from environment home.
             readOnly = true;
         }
         mReadOnly = readOnly;
 
         long lockTimeout = envConfig.getLockTimeout();
         long txnTimeout = envConfig.getTxnTimeout();
 
         long deadlockInterval = Math.min(lockTimeout, txnTimeout);
         // Make sure interval is no smaller than 0.5 seconds.
         deadlockInterval = Math.max(500000, deadlockInterval) / 1000;
 
         start(builder.getCheckpointInterval(), deadlockInterval, builder);
     }
 
     public Object getEnvironment() {
         return mEnv;
     }
 
     public BDBProduct getBDBProduct() {
         return mProduct;
     }
 
     public int[] getVersion() {
         return new int[] {mEnv.getVersionMajor(), mEnv.getVersionMinor(), mEnv.getVersionPatch()};
     }
 
     public File getHome() {
         return mEnvHome;
     }
 
     public File getDataHome() {
         return mDataHome == null ? mEnvHome : mDataHome;
     }
 
     public <S extends Storable> Result<S> compact(Class<S> storableType)
         throws RepositoryException
     {
         return ((BDBStorage) storageFor(storableType)).compact();
     }
 
     @Override
     IsolationLevel selectIsolationLevel(com.amazon.carbonado.Transaction parent,
                                         IsolationLevel level)
     {
         if (level == null) {
             if (parent == null) {
                 return IsolationLevel.REPEATABLE_READ;
             }
             return parent.getIsolationLevel();
         }
 
         if (level == IsolationLevel.SNAPSHOT) {
             if (!mMVCC) {
                 // Not supported.
                 return null;
             }
         } else if (level == IsolationLevel.SERIALIZABLE) {
             // Not supported.
             return null;
         }
 
         return level;
     }
 
     @Override
     protected Transaction txn_begin(Transaction parent, IsolationLevel level) throws Exception {
         TransactionConfig config;
 
         if (!mDatabasesTransactional) {
             return null;
         }
 
         switch (level) {
         case READ_UNCOMMITTED:
             config = TXN_READ_UNCOMMITTED;
             break;
         case READ_COMMITTED:
             config = TXN_READ_COMMITTED;
             break;
         case SNAPSHOT:
             config = TXN_SNAPSHOT;
             break;
         default:
             config = TXN_REPEATABLE_READ;
             break;
         }
 
         return mEnv.beginTransaction(parent, config);
     }
 
     @Override
     protected Transaction txn_begin(Transaction parent, IsolationLevel level,
                                     int timeout, TimeUnit unit)
         throws Exception
     {
         Transaction txn = txn_begin(parent, level);
         txn.setLockTimeout(unit.toMicros(timeout));
         return txn;
     }
 
     @Override
     protected Transaction txn_begin_nowait(Transaction parent, IsolationLevel level)
         throws Exception
     {
         TransactionConfig config;
 
         if (!mDatabasesTransactional) {
             return null;
         }
 
         switch (level) {
         case READ_UNCOMMITTED:
             config = TXN_READ_UNCOMMITTED_NOWAIT;
             break;
         case READ_COMMITTED:
             config = TXN_READ_COMMITTED_NOWAIT;
             break;
         case SNAPSHOT:
             config = TXN_SNAPSHOT;
             break;
         default:
             config = TXN_REPEATABLE_READ_NOWAIT;
             break;
         }
 
         return mEnv.beginTransaction(parent, config);
     }
 
     @Override
     protected void txn_commit(Transaction txn) throws Exception {
         if (txn == null) return;
 
         txn.commit();
     }
 
     @Override
     protected void txn_abort(Transaction txn) throws Exception {
         if (txn == null) return;
 
         txn.abort();
     }
 
     @Override
     protected void env_checkpoint() throws Exception {
         // Disable checkpoints during hot backup. BDB documentation indicates that
         // checkpoints can run during backup, but testing indicates otherwise.
         synchronized (mBackupLock) {
             if (mBackupCount == 0) {
                 CheckpointConfig cc = new CheckpointConfig();
                 cc.setForce(true);
                 mEnv.checkpoint(cc);
                 removeOldLogFiles();
             } else {
                 throw new PersistDeniedException("Hot backup in progress");
             }
         }
     }
 
     @Override
     protected void env_checkpoint(int kBytes, int minutes) throws Exception {
         synchronized (mBackupLock) {
             if (mBackupCount == 0) {
                 CheckpointConfig cc = new CheckpointConfig();
                 cc.setKBytes(kBytes);
                 cc.setMinutes(minutes);
                 mEnv.checkpoint(cc);
                 removeOldLogFiles();
             }
         }
     }
 
     private void removeOldLogFiles() throws Exception {
         try {
             if (mKeepOldLogFiles) {
                 return;
             }
         } catch (NoSuchFieldError e) {
             // Carbonado package might be older.
         }
 
         mEnv.removeOldLogFiles();
     }
 
     @Override
     protected void env_detectDeadlocks() throws Exception {
         mEnv.detectDeadlocks(LockDetectMode.DEFAULT);
     }
 
     @Override
     protected void env_close() throws Exception {
         if (mEnv != null) {
             mEnv.close();
             String registeredHome = mRegisteredHome;
             if (registeredHome != null) {
                 mRegisteredHome = null;
                 unregister(registeredHome);
             }
         }
     }
 
     @Override
     protected <S extends Storable> BDBStorage<Transaction, S> createBDBStorage(Class<S> type)
         throws Exception
     {
         return new DB_Storage<S>(this, type);
     }
 
     @Override
     void enterBackupMode(boolean deleteOldLogFiles) throws Exception {
         forceCheckpoint();
         if (deleteOldLogFiles && mBackupCount == 0 && mIncrementalBackupCount == 0) {
             // if we are not auto-deleting old log files delete files if prompted
             deleteOldLogFiles(-1); // to delete all
         }
     }
 
     @Override
     void exitBackupMode() throws Exception {
         // Nothing special to do.
     }
 
     @Override
     void enterIncrementalBackupMode(long lastLogNumber, boolean deleteOldLogFiles)
         throws Exception
     {
         if (!mKeepOldLogFiles) {
             throw new IllegalStateException
                 ("Incremental backup requires old log files to be kept");
         }
         mEnv.logFlush(null);
         if (deleteOldLogFiles && lastLogNumber > 0 &&
             mBackupCount == 0 && mIncrementalBackupCount == 0)
         {
             deleteOldLogFiles(lastLogNumber);
         }
     }
 
     @Override
     void exitIncrementalBackupMode() throws Exception {
         // Nothing special to do.
     }
 
     @Override
     File[] backupFiles(long[] newLastLogNum) throws Exception {
         Set<File> dbFileSet = new LinkedHashSet<File>();
 
         for (String dbName : getAllDatabaseNames()) {
             File file = new File(getDatabaseFileName(dbName));
             if (!file.isAbsolute()) {
                file = new File(mDataHome, file.getPath());
             }
             if (!dbFileSet.contains(file) && file.exists()) {
                 dbFileSet.add(file);
             }
         }
 
         // Find highest log number - all logs before this can be removed if 
         // user specifies so in the future.
         long maxLogNum = 0;
         for (File file : mEnv.getArchiveLogFiles(true)) {
            long currLogNum = getLogFileNum(file.getName());
             if (!file.isAbsolute()) {
                 file = new File(mEnvHome, file.getPath());
             }
             if (!dbFileSet.contains(file) && file.exists()) {
                 dbFileSet.add(file);
                 if (currLogNum > maxLogNum) {
                     maxLogNum = currLogNum;
                 }
             }
         }
 
         newLastLogNum[0] = maxLogNum;
 
         return dbFileSet.toArray(new File[dbFileSet.size()]);
     }
 
     @Override
     File[] incrementalBackup(long lastLogNum, long[] newLastLogNum) throws Exception {
         Set<File> dbFileSet = new LinkedHashSet<File>();        
         long maxLogNum = 0;
         for (File file : mEnv.getArchiveLogFiles(true)) {
             long currLogNum = getLogFileNum(file.getName());
             if (currLogNum >= lastLogNum) { // only copy new files
                 if (!file.isAbsolute()) {
                     file = new File(mEnvHome, file.getPath());
                 }
                 if (!dbFileSet.contains(file) && file.exists()) {
                     dbFileSet.add(file);           
                     if (currLogNum > maxLogNum) {
                         maxLogNum = currLogNum;
                     }
                 }
             } 
         }
 
         newLastLogNum[0] = maxLogNum;
 
         return dbFileSet.toArray(new File[dbFileSet.size()]);
     }
 
     private void deleteOldLogFiles(long maxLogNum) throws Exception {
         for (File file : mEnv.getArchiveLogFiles(false)) {
             long currLogNum = getLogFileNum(file.getName());
             if (currLogNum < maxLogNum) {
                 // file no longer in use so delete it
                 file.delete();
             }
         }
     }
 
     private long getLogFileNum(String fileName) {
         int ix = fileName.indexOf(".");
         return Long.parseLong(fileName.substring(ix + 1));
     }
 }
