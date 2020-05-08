 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geogit.storage.bdbje;
 
 import static com.sleepycat.je.OperationStatus.NOTFOUND;
 import static com.sleepycat.je.OperationStatus.SUCCESS;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geogit.api.ObjectId;
 import org.geogit.storage.AbstractObjectDatabase;
 import org.geogit.storage.ObjectDatabase;
 import org.geotools.util.logging.Logging;
 
 import com.google.common.base.Preconditions;
 import com.sleepycat.collections.CurrentTransaction;
 import com.sleepycat.je.Cursor;
 import com.sleepycat.je.CursorConfig;
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.LockMode;
 import com.sleepycat.je.OperationStatus;
 import com.sleepycat.je.Transaction;
 
 /**
  * @TODO: extract interface
  */
 public class JEObjectDatabase extends AbstractObjectDatabase implements ObjectDatabase {
 
     private static final Logger LOGGER = Logging.getLogger(JEObjectDatabase.class);
 
     private final Environment env;
 
     private Database objectDb;
 
     private CurrentTransaction txn;
 
     public JEObjectDatabase(final Environment env) {
         super();
         this.env = env;
     }
 
     /**
      * @see org.geogit.storage.ObjectDatabase#close()
      */
     @Override
     public void close() {
         objectDb.close();
     }
 
     /**
      * @see org.geogit.storage.ObjectDatabase#create()
      */
     @Override
     public void create() {
         txn = CurrentTransaction.getInstance(env);
         DatabaseConfig dbConfig = new DatabaseConfig();
         dbConfig.setAllowCreate(true);
         dbConfig.setTransactional(env.getConfig().getTransactional());
         this.objectDb = env.openDatabase(null, "BlobStore", dbConfig);
     }
 
     @Override
     protected List<ObjectId> lookUpInternal(final byte[] partialId) {
 
         DatabaseEntry key;
         {
             byte[] keyData = partialId.clone();
             key = new DatabaseEntry(keyData);
         }
 
         DatabaseEntry data = new DatabaseEntry();
         data.setPartial(0, 0, true);// do not retrieve data
 
         List<ObjectId> matches;
 
         CursorConfig cursorConfig = new CursorConfig();
         cursorConfig.setReadCommitted(true);
         cursorConfig.setReadUncommitted(false);
 
         Cursor cursor = objectDb.openCursor(txn.getTransaction(), cursorConfig);
         try {
             // position cursor at the first closest key to the one looked up
            OperationStatus status = cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
             if (SUCCESS.equals(status)) {
                 matches = new ArrayList<ObjectId>(2);
                 final byte[] compKey = new byte[partialId.length];
                while (SUCCESS.equals(status)) {
                     byte[] keyData = key.getData();
                     System.arraycopy(keyData, 0, compKey, 0, compKey.length);
                     if (Arrays.equals(partialId, compKey)) {
                         matches.add(new ObjectId(keyData));
                     } else {
                         break;
                     }
                    status = cursor.getNext(key, data, LockMode.DEFAULT);
                 }
             } else {
                 matches = Collections.emptyList();
             }
             return matches;
         } finally {
             cursor.close();
         }
     }
 
     /**
      * @see org.geogit.storage.ObjectDatabase#exists(org.geogit.api.ObjectId)
      */
     @Override
     public boolean exists(final ObjectId id) {
         Preconditions.checkNotNull(id, "id");
 
         DatabaseEntry key = new DatabaseEntry(id.getRawValue());
         DatabaseEntry data = new DatabaseEntry();
         // tell db not to retrieve data
         data.setPartial(0, 0, true);
 
         final LockMode lockMode = LockMode.DEFAULT;
         CurrentTransaction.getInstance(env);
         OperationStatus status = objectDb.get(txn.getTransaction(), key, data, lockMode);
         return SUCCESS == status;
     }
 
     /**
      * @see org.geogit.storage.ObjectDatabase#getRaw(org.geogit.api.ObjectId)
      */
     @Override
     protected InputStream getRawInternal(final ObjectId id) throws IOException {
         Preconditions.checkNotNull(id, "id");
         DatabaseEntry key = new DatabaseEntry(id.getRawValue());
         DatabaseEntry data = new DatabaseEntry();
 
         final LockMode lockMode = LockMode.READ_COMMITTED;
         Transaction transaction = txn.getTransaction();
         OperationStatus operationStatus = objectDb.get(transaction, key, data, lockMode);
         if (NOTFOUND.equals(operationStatus)) {
             throw new IllegalArgumentException("Object does not exist: " + id.toString());
         }
         final byte[] cData = data.getData();
 
         return new ByteArrayInputStream(cData);
     }
 
     /**
      * @see org.geogit.storage.ObjectDatabase#put(org.geogit.storage.ObjectWriter)
      */
     @Override
     protected boolean putInternal(final ObjectId id, final byte[] rawData, final boolean override)
             throws IOException {
         final byte[] rawKey = id.getRawValue();
         DatabaseEntry key = new DatabaseEntry(rawKey);
         DatabaseEntry data = new DatabaseEntry(rawData);
 
         OperationStatus status;
         if (override) {
             status = objectDb.put(txn.getTransaction(), key, data);
         } else {
             status = objectDb.putNoOverwrite(txn.getTransaction(), key, data);
         }
         final boolean didntExist = SUCCESS.equals(status);
 
         if (LOGGER.isLoggable(Level.FINER)) {
             if (didntExist) {
                 LOGGER.finer("Key already exists in blob store, blob reused for id: " + id);
             }
         }
         return didntExist;
     }
 
     @Override
     public boolean delete(final ObjectId id) {
         final byte[] rawKey = id.getRawValue();
         final DatabaseEntry key = new DatabaseEntry(rawKey);
 
         final OperationStatus status = objectDb.delete(txn.getTransaction(), key);
 
         return SUCCESS.equals(status);
     }
 }
