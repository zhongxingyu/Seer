 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server.config;
 
 import com.janrain.backplane.server.config.BpServerConfig;
 import com.janrain.backplane2.server.BackplaneServerException;
 import com.janrain.backplane2.server.dao.DAOFactory;
 import com.janrain.cache.CachedL1;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.util.AwsUtility;
 import com.janrain.commons.util.InitSystemProps;
 import com.janrain.crypto.HmacHashUtils;
 import com.yammer.metrics.reporting.ConsoleReporter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.context.annotation.Scope;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Properties;
 import java.util.TimeZone;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 
 /**
  * Holds configuration settings for the Backplane server
  * 
  * @author Jason Cowley, Johnny Bufu
  */
 @Scope(value="singleton")
 public class Backplane2Config {
 
     // - PUBLIC
 
     // http://fahdshariff.blogspot.ca/2010/08/dateformat-with-multiple-threads.html
     public static final ThreadLocal<DateFormat> ISO8601 = new ThreadLocal<DateFormat>() {
         @Override
         protected DateFormat initialValue() {
             return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") {{
                 setTimeZone(TimeZone.getTimeZone("GMT"));
             }};
         }
     };
 
     public void checkAdminAuth(String user, String password) throws AuthException {
         checkAdminAuth(getAdminAuthTableName(), user, password);
     }
 
     public String getTableName(SimpleDBTables table) {
         return Backplane2Config.this.bpInstanceId + table.getTableSuffix();
     }
 
     public enum SimpleDBTables {
 
         BP_SERVER_CONFIG("_bpserverconfig"),
         BP_ADMIN_AUTH("_Admin"),
         BP_BUS_CONFIG("_v2_busconfig"),
         BP_BUS_OWNERS("_v2_bus_owners"),
         BP_CLIENTS("_v2_clients"),
         BP_MESSAGES("_v2_messages"),
         BP_SAMPLES("_samples"),
         BP_METRICS("_metrics"),
         BP_METRIC_AUTH("_bpMetricAuth"),
         BP_GRANT("_v2_grants"),
         BP_ACCESS_TOKEN("_v2_accessTokens"),
         BP_AUTH_SESSION("_v2_authSessions"),
         BP_AUTHORIZATION_REQUEST("_v2_authorizationRequests"),
         BP_AUTHORIZATION_DECISION_KEY("_v2_authorizationDecisions");
 
         public String getTableSuffix() {
             return tableSuffix;
         }
 
         // - PRIVATE
 
         private String tableSuffix;
 
         private SimpleDBTables(String tableSuffix) {
             this.tableSuffix = tableSuffix;
         }
     }
 
 
 
     /**
 	 * @return the debugMode
 	 */
 	public boolean isDebugMode() {
         return Boolean.valueOf(cachedGet(BpServerConfig.Field.DEBUG_MODE));
 	}
 
     /**
 	 * @return the encryptionKey
 	 */
 	//public String getEncryptionKey() throws SimpleDBException {
 	//	return cachedGet(BpServerProperty.ENCRYPTION_KEY);
 	//}
 
     /**
      * @return the server default max message value per channel
      * @throws SimpleDBException
      */
     public long getDefaultMaxMessageLimit() {
         Long max = Long.valueOf(cachedGet(BpServerConfig.Field.DEFAULT_MESSAGES_MAX));
         return max == null ? Backplane2Config.BP_MAX_MESSAGES_DEFAULT : max;
     }
 
     public Exception getDebugException(Exception e) {
         return isDebugMode() ? e: null;
     }
 
     public String getInstanceId() {
         return bpInstanceId;
     }
 
     public String getBuildVersion() {
         return buildProperties.getProperty(BUILD_VERSION_PROPERTY);
     }
 
     /**
      * Retrieve the server instance id Amazon assigned
      * @return
      */
 
     public static String getEC2InstanceId() {
         return EC2InstanceId;
     }
 
     // - PACKAGE
 
 
     Backplane2Config(String instanceId) {
         this.bpInstanceId = instanceId;
     }
 
     /**
      * Load system property
      * @param propParamName
      * @return
      */
 
     static String getAwsProp(String propParamName) {
         String result = System.getProperty(propParamName);
         if (StringUtils.isBlank(result)) {
             throw new RuntimeException("Required system property configuration missing: " + propParamName);
         }
         return result;
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(Backplane2Config.class);
 
     private static final String BUILD_PROPERTIES = "/build.properties";
     private static final String BUILD_VERSION_PROPERTY = "build.version";
     private static final Properties buildProperties = new Properties();
 
     private static final String BP_CONFIG_ENTRY_NAME = "bpserverconfig";
     private static final long BP_MAX_MESSAGES_DEFAULT = 100;
     private static final long CACHE_UPDATER_INTERVAL_MILLISECONDS = 300;
 
     private final String bpInstanceId;
     private ScheduledExecutorService cleanup;
     private ExecutorService cacheUpdater;
 
 
     // Amazon specific instance-id value
     private static String EC2InstanceId = AwsUtility.retrieveEC2InstanceId();
 
     private final com.yammer.metrics.core.Timer v2CleanupTimer =
         com.yammer.metrics.Metrics.newTimer(Backplane2Config.class, "cleanup_messages_time", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
 
     @SuppressWarnings({"UnusedDeclaration"})
     private Backplane2Config() {
         this.bpInstanceId = getAwsProp(InitSystemProps.AWS_INSTANCE_ID);
 
         //TODO: remove at some point...
         ConsoleReporter.enable(5, TimeUnit.MINUTES);
 
         try {
             buildProperties.load(Backplane2Config.class.getResourceAsStream(BUILD_PROPERTIES));
             //assert(StringUtils.isNotBlank(getEncryptionKey()));
         } catch (Exception e) {
             String err = "Error loading build properties from " + BUILD_PROPERTIES;
             logger.error(err, e);
             throw new RuntimeException(err, e);
         }
 
         logger.info("Configured Backplane Server instance: " + bpInstanceId);
     }
 
     private ScheduledExecutorService createCleanupTask() {
         long cleanupIntervalMinutes;
         logger.info("calling createCleanupTask()");
         cleanupIntervalMinutes = Long.valueOf(cachedGet(BpServerConfig.Field.CLEANUP_INTERVAL_MINUTES));
 
         ScheduledExecutorService cleanupTask = Executors.newScheduledThreadPool(1);
         cleanupTask.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
 
                 try {
                     deleteExpiredMessages();
                 } catch (Exception e) {
                     logger.error("Error while cleaning up expired messages, " + e.getMessage(), e);
                 }
 
             }
 
         }, cleanupIntervalMinutes, cleanupIntervalMinutes, TimeUnit.MINUTES);
 
         return cleanupTask;
     }
 
     private void deleteExpiredMessages() {
         try {
             logger.info("Backplane message cleanup task started.");
 
             //TODO: configure for redis
 
         } catch (Exception e) {
             // catch-all, else cleanup thread stops
             logger.error("Backplane messages cleanup task error: " + e.getMessage(), e);
         } finally {
             logger.info("Backplane messages cleanup task finished.");
         }
     }
 
     /*
     private ExecutorService createCacheUpdaterTask() {
         ScheduledExecutorService cacheUpdater = Executors.newScheduledThreadPool(1);
         cacheUpdater.scheduleWithFixedDelay(new Runnable() {
             @Override
             public void run() {
                 long start = System.currentTimeMillis();
                 try {
                     MessageCache<BackplaneMessage> cache = daoFactory.getMessageCache();
                     long cacheMaxBytes = getMaxMessageCacheBytes();
                     if (cacheMaxBytes <= 0) return;
                     cache.setMaxCacheSizeBytes(cacheMaxBytes);
                     BackplaneMessage lastCached = cache.getLastMessage();
                     String lastCachedId = lastCached != null ? lastCached.getIdValue() : "";
                     cache.add(daoFactory.getBackplaneMessageDAO().retrieveMessagesNoScope(lastCachedId));
                 } catch (Exception e) {
                     logger.error("Error updating message cache: " + e.getMessage(), e);
                 }
                 logger.info("Cache updated in " + (System.currentTimeMillis() - start) + " ms");
             }
         }, CACHE_UPDATER_INTERVAL_MILLISECONDS, CACHE_UPDATER_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
         return cacheUpdater;
     }
     */
 
     @PostConstruct
     private void init() {
         this.cleanup = createCleanupTask();
         //this.cacheUpdater = createCacheUpdaterTask();
 
         /*for(SimpleDBTables table : EnumSet.allOf(SimpleDBTables.class)) {
             superSimpleDb.checkDomain(getTableName(table));
         }*/
     }
 
     @PreDestroy
     private void cleanup() {
         shutdownExecutor(cleanup);
         shutdownExecutor(cacheUpdater);
     }
 
     private void shutdownExecutor(ExecutorService executor) {
         try {
             executor.shutdown();
             if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                 logger.info("Background thread shutdown properly");
             } else {
                 executor.shutdownNow();
                 if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                     logger.error("Background thread did not terminate");
                 }
             }
         } catch (InterruptedException e) {
             logger.error("cleanup() threw an exception", e);
             executor.shutdownNow();
             Thread.currentThread().interrupt();
         }
     }
 
  //   @Inject
     @SuppressWarnings({"UnusedDeclaration"})
    // private SuperSimpleDB superSimpleDb;
 
     @Inject
    private DAOFactory daoFactory;
 
     private String cachedGet(BpServerConfig.Field property) {
 
         BpServerConfig bpServerConfigCache = (BpServerConfig) CachedL1.getInstance().getObject(BpServerConfig.BPSERVER_CONFIG_KEY);
         if (bpServerConfigCache == null) {
             // pull from db if not found in cache
            bpServerConfigCache = daoFactory.getConfigDAO().get(BpServerConfig.BPSERVER_CONFIG_KEY);

             if (bpServerConfigCache == null) {
                 // no instance found in cache or the db, so let's use the default record
                 bpServerConfigCache = new BpServerConfig();
             }
             // add it to the L1 cache
             CachedL1.getInstance().setObject(BpServerConfig.BPSERVER_CONFIG_KEY, -1, bpServerConfigCache);
         }
 
         return bpServerConfigCache.get(property);
 
     }
 
     private String getBpServerConfigTableName() {
         return bpInstanceId + SimpleDBTables.BP_SERVER_CONFIG.getTableSuffix();
     }
 
     private String getAdminAuthTableName() {
         return bpInstanceId + SimpleDBTables.BP_ADMIN_AUTH.getTableSuffix();
     }
 
     private String getMetricAuthTableName() {
         return bpInstanceId + SimpleDBTables.BP_METRIC_AUTH.getTableSuffix();
     }
 
     private Long getMaxCacheAge() {
         return Long.valueOf(cachedGet(BpServerConfig.Field.CONFIG_CACHE_AGE_SECONDS));
     }
 
     public void checkAdminAuth(String authTable, String user, String password) throws AuthException {
         try {
             //User userEntry = superSimpleDb.retrieve(authTable, User.class, user);
            User userEntry = daoFactory.getAdminDAO().get(user);
             String authKey = userEntry == null ? null : userEntry.get(User.Field.PWDHASH);
             if ( ! HmacHashUtils.checkHmacHash(password, authKey) ) {
                 throw new AuthException("User " + user + " not authorized in " + authTable);
             }
         } catch (BackplaneServerException e) {
             throw new AuthException("User " + user + " not authorized in " + authTable + " , " + e.getMessage(), e);
         }
     }
 
 
 }
