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
 
 package com.janrain.backplane2.server.dao;
 
 import com.janrain.backplane2.server.BackplaneMessage;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.backplane2.server.config.BusConfig2;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 import static com.janrain.backplane2.server.BackplaneMessage.Field.BUS;
 import static com.janrain.backplane2.server.BackplaneMessage.Field.ID;
 import static com.janrain.backplane2.server.BackplaneMessage.Field.STICKY;
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_MESSAGES;
 import static com.janrain.backplane2.server.config.BusConfig2.Field.BUS_NAME;
 import static com.janrain.backplane2.server.config.BusConfig2.Field.RETENTION_STICKY_TIME_SECONDS;
 import static com.janrain.backplane2.server.config.BusConfig2.Field.RETENTION_TIME_SECONDS;
 
 /**
  * @author Tom Raney
  */
 public class BackplaneMessageDAO extends DAO {
 
     BackplaneMessageDAO(SuperSimpleDB superSimpleDB, Backplane2Config bpConfig, DaoFactory daoFactory) {
         super(superSimpleDB, bpConfig);
         this.daoFactory = daoFactory;
     }
 
     public void persistBackplaneMessage(BackplaneMessage message) throws SimpleDBException {
         superSimpleDB.store(bpConfig.getTableName(BP_MESSAGES), BackplaneMessage.class, message);
     }
 
     public BackplaneMessage retrieveBackplaneMessage(String messageId) throws SimpleDBException {
         return superSimpleDB.retrieve(bpConfig.getTableName(BP_MESSAGES), BackplaneMessage.class, messageId);
     }
 
     public List<BackplaneMessage> retrieveAllMesssagesPerScope(Scope scope, String sinceMessageId) throws SimpleDBException {
 
         ArrayList<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
 
         // If the scope is complex, the risk is that we over-run SDB's query size restrictions.
         // So, here we break the query into chunks to run against SDB and build the result set
         // up incrementally.
         // TODO: there is a risk that messages arrive and are lost with this approach
         // because we are building the query results up from pieces.
 
         List<String> queries = scope.buildQueriesFromScope();
 
         for (String query : queries) {
 
             assert(StringUtils.isNotEmpty(query));
 
             if (StringUtils.isNotEmpty(sinceMessageId)) {
                 query += " AND id > '" + sinceMessageId + "'";
             }
 
            query += " AND id IS NOT NULL ORDER BY id";
 
             logger.info("message query => " + query);
 
            messages.addAll(superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_MESSAGES),
                    BackplaneMessage.class, query, true));
         }
 
         // we need to sort the results, because they are built up from individual queries and
        // may not be in the correct order when merged.
         Collections.sort(messages, new Comparator<BackplaneMessage>() {
             @Override
             public int compare(BackplaneMessage msg1, BackplaneMessage msg2) {
                 return msg1.getIdValue().compareTo(msg1.getIdValue());
             }
         });
 
         return messages;
     }
     
     public void deleteExpiredMessages() {
         try {
             logger.info("Backplane message cleanup task started.");
             String messagesTable = bpConfig.getTableName(BP_MESSAGES);
             for(BusConfig2 busConfig : daoFactory.getBusDao().retrieveBuses()) {
                 try {
                     // non-sticky
                     superSimpleDB.deleteWhere(messagesTable, getExpiredMessagesClause(busConfig.get(BUS_NAME), false, busConfig.get(RETENTION_TIME_SECONDS)));
                     // sticky
                     superSimpleDB.deleteWhere(messagesTable, getExpiredMessagesClause(busConfig.get(BUS_NAME), true, busConfig.get(RETENTION_STICKY_TIME_SECONDS)));
 
                 } catch (SimpleDBException sdbe) {
                     logger.error("Error cleaning up expired messages on bus "  + busConfig.get(BUS_NAME) + ", " + sdbe.getMessage(), sdbe);
                 }
             }
 
         } catch (Exception e) {
             // catch-all, else cleanup thread stops
             logger.error("Backplane messages cleanup task error: " + e.getMessage(), e);
         } finally {
             logger.info("Backplane messages cleanup task finished.");
         }
 
     }
     
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(BackplaneMessageDAO.class);
 
     private DaoFactory daoFactory;
 
     private String getExpiredMessagesClause(String busId, boolean sticky, String retentionTimeSeconds) {
         return BUS.getFieldName() + " = '" + busId + "' AND " +
                 // "is (not) null" is low-performance on simpledb apparently
                 // http://practicalcloudcomputing.com/post/722621724/simpledb-essentials-for-high-performance-users-part-2
                 STICKY.getFieldName() + " = '" + Boolean.toString(sticky) + "' AND " +
                 ID.getFieldName() + " < '" +
                 Backplane2Config.ISO8601.format(new Date(System.currentTimeMillis() - Long.valueOf(retentionTimeSeconds) * 1000))
                 + "'";
     }
 }
