 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.storage;
 
 import com.google.common.collect.ImmutableSet;
 
 import org.apache.commons.pool.PoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.lite.types.LongString;
 import org.sakaiproject.nakamura.lite.types.StringType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 import java.util.Set;
 
 @Component(componentAbstract = true)
 public abstract class AbstractClientConnectionPool implements StorageClientPool {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(AbstractClientConnectionPool.class);
 
     @Property(intValue = 200)
     private static final String MAX_ACTIVE = "max-active";
     @Property(longValue = 10)
     private static final String MAX_WAIT = "max-wait";
     @Property(intValue = 5)
     private static final String MAX_IDLE = "max-idle";
     @Property(boolValue = true)
     private static final String TEST_ON_BORROW = "test-on-borrow";
     @Property(boolValue = true)
     private static final String TEST_ON_RETURN = "test-on-return";
     @Property(longValue = 60000)
     private static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "time-between-eviction-run";
     @Property(intValue = 1000)
     private static final String NUM_TESTS_PER_EVICTION_RUN = "num-tests-per-eviction-run";
     @Property(longValue = 10000)
     private static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "min-evictable-idle-time-millis";
     @Property(boolValue = false)
     private static final String TEST_WHILE_IDLE = "test-while-idle";
     @Property(value = "grow")
     private static final String WHEN_EHAUSTED = "when-exhausted-action";

    private static final int DEFAULT_LONG_STRING_SIZE = 16*1024;
    @Property(intValue = DEFAULT_LONG_STRING_SIZE)
     private static final String LONG_STRING_SIZE = "long-string-size";
 
     public  static final String DEFAULT_FILE_STORE = "store";
     @Property(value = DEFAULT_FILE_STORE)
     public static final String FS_STORE_BASE_DIR = "store-base-dir";
 
     /**
      * Is ok to store content and long strings in the same location as they are
      * identified by sha1 hashes and so unique. If a deplorer decides they want different
      * locations, they can configure.
      */
     @Property(value = DEFAULT_FILE_STORE)
     private static final String LONG_STRING_STORE_BASE = "long-string-base-dir";
 
 
     @Reference
     private Configuration configuration;
 
     private Set<String> indexColumns;
 
 
 
     private GenericObjectPool pool;
 
     private Set<String> indexColumnsTypes;
 
 
     public AbstractClientConnectionPool() {
     }
 
     @Activate
     public void activate(Map<String, Object> properties) throws ClassNotFoundException {
         // for testing purposes
         if ( configuration == null ) {
             configuration = (Configuration) properties.get(Configuration.class.getName());
         }
         indexColumns = ImmutableSet.of(configuration.getIndexColumnNames());
         indexColumnsTypes = ImmutableSet.of(configuration.getIndexColumnTypes());
         int maxActive = StorageClientUtils.getSetting(properties.get(MAX_ACTIVE), 200);
         byte whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
         String whenExhausted = (String) properties.get(WHEN_EHAUSTED);
         if ("fail".equals(whenExhausted)) {
             whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
         } else if ("grow".equals(whenExhausted)) {
             whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
         } else if ("block".equals(whenExhausted)) {
             whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
         }
         long maxWait = StorageClientUtils.getSetting(properties.get(MAX_WAIT), 10L);
         int maxIdle = StorageClientUtils.getSetting(properties.get(MAX_IDLE), 5);
         boolean testOnBorrow = StorageClientUtils.getSetting(properties.get(TEST_ON_BORROW), true);
         boolean testOnReturn = StorageClientUtils.getSetting(properties.get(TEST_ON_RETURN), true);
         long timeBetweenEvictionRunsMillis = StorageClientUtils.getSetting(
                 properties.get(TIME_BETWEEN_EVICTION_RUNS_MILLIS), 60000L);
         int numTestsPerEvictionRun = StorageClientUtils.getSetting(properties.get(NUM_TESTS_PER_EVICTION_RUN), 1000);
         long minEvictableIdleTimeMillis = StorageClientUtils.getSetting(
                 properties.get(MIN_EVICTABLE_IDLE_TIME_MILLIS), 10000L);
         boolean testWhileIdle = StorageClientUtils.getSetting(properties.get(TEST_WHILE_IDLE), false);
 
         pool = new GenericObjectPool(getConnectionPoolFactory(), maxActive, whenExhaustedAction,
                 maxWait, maxIdle, testOnBorrow, testOnReturn, timeBetweenEvictionRunsMillis,
                 numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle);
         
         // set the maximum size of a string, if this is not 0, strings over this size will become files.
        StringType.setLengthLimit(StorageClientUtils.getSetting(properties.get(LONG_STRING_SIZE),DEFAULT_LONG_STRING_SIZE));
         // location of the long string store.
         LongString
                 .setBase(StorageClientUtils.getSetting(properties.get(LONG_STRING_STORE_BASE),
                         StorageClientUtils.getSetting(properties.get(FS_STORE_BASE_DIR),
                                 DEFAULT_FILE_STORE)));
 
     }
 
 
     protected abstract PoolableObjectFactory getConnectionPoolFactory();
 
     @Deactivate
     public void deactivate(Map<String, Object> properties) {
         try {
             pool.clear();
             pool.close();
             LOGGER.debug("Sparse Map Content client pool closed ");
         } catch (Exception e) {
             LOGGER.error("Failed to close pool ", e);
         }
     }
 
     public Set<String> getIndexColumns() {
         return indexColumns;
     }
 
     public Set<String> getIndexColumnsTypes() {
         return indexColumnsTypes;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.sakaiproject.nakamura.lite.cassandra.ConnectionPool#openConnection()
      */
     public StorageClient getClient() throws ClientPoolException {
         try {
             StorageClient client = (StorageClient) pool.borrowObject();
             LOGGER.debug("Borrowed storage client pool client:" + client);
             return client;
         } catch (Exception e) {
             LOGGER.warn("Failed To Borrow connection from pool {} ",e.getMessage());
             throw new ClientPoolException("Failed To Borrow connection from pool ", e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.sakaiproject.nakamura.lite.cassandra.ConnectionPool#closeConnection()
      */
     public void releaseClient(StorageClient client) {
         try {
             if (client != null) {
                 pool.returnObject(client);
                 LOGGER.debug("Released storage client pool client:" + client);
             }
         } catch (Exception e) {
             LOGGER.warn("Failed to close connection " + e.getMessage(), e);
         }
     }
 
 }
