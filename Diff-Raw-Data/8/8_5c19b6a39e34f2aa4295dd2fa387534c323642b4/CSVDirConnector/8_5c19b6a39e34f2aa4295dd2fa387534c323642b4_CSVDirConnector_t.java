 /*
  * ====================
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2011 Tirasa. All rights reserved.     
  * 
  * The contents of this file are subject to the terms of the Common Development 
  * and Distribution License("CDDL") (the "License").  You may not use this file 
  * except in compliance with the License.
  * 
  * You can obtain a copy of the License at 
  * http://IdentityConnectors.dev.java.net/legal/license.txt
  * See the License for the specific language governing permissions and limitations 
  * under the License. 
  * 
  * When distributing the Covered Code, include this CDDL Header Notice in each file
  * and include the License file at identityconnectors/legal/license.txt.
  * If applicable, add the following below this CDDL Header, with the fields 
  * enclosed by brackets [] replaced by your own identifying information: 
  * "Portions Copyrighted [year] [name of copyright owner]"
  * ====================
  */
 package org.connid.csvdir;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.identityconnectors.common.logging.Log;
 import org.identityconnectors.dbcommon.FilterWhereBuilder;
 import org.identityconnectors.dbcommon.SQLParam;
 import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
 import org.identityconnectors.framework.common.objects.AttributeBuilder;
 import org.identityconnectors.framework.common.objects.AttributeInfo;
 import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
 import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
 import org.identityconnectors.framework.common.objects.ObjectClass;
 import org.identityconnectors.framework.common.objects.OperationOptions;
 import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
 import org.identityconnectors.framework.common.objects.ResultsHandler;
 import org.identityconnectors.framework.common.objects.Schema;
 import org.identityconnectors.framework.common.objects.SchemaBuilder;
 import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
 import org.identityconnectors.framework.common.objects.SyncDeltaType;
 import org.identityconnectors.framework.common.objects.SyncResultsHandler;
 import org.identityconnectors.framework.common.objects.SyncToken;
 import org.identityconnectors.framework.common.objects.Uid;
 import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
 import org.identityconnectors.framework.spi.Configuration;
 import org.identityconnectors.framework.spi.Connector;
 import org.identityconnectors.framework.spi.ConnectorClass;
 import org.identityconnectors.framework.spi.operations.SchemaOp;
 import org.identityconnectors.framework.spi.operations.SearchOp;
 import org.identityconnectors.framework.spi.operations.SyncOp;
 
 /**
  * Only implements search since this connector is only used to do sync.
  */
 @ConnectorClass(configurationClass = CSVDirConfiguration.class,
 displayNameKey = "FlatFile")
 public class CSVDirConnector implements
         Connector, SearchOp<FilterWhereBuilder>, SchemaOp, SyncOp {
 
     /**
      * Setup {@link Connector} based logging.
      */
     private static final Log LOG = Log.getLog(CSVDirConnector.class);
 
     /**
      * Configuration information passed back to the {@link Connector}
      * by the method
      * {@link Connector#init(Configuration)}.
      */
     private CSVDirConfiguration configuration;
 
     private static long token = 0L;
 
     @Override
     public final Configuration getConfiguration() {
         return configuration;
     }
 
     /**
      * @param cfg
      * Saves the configuration for use in later calls.
      * @see org.identityconnectors.framework.Connector#init(
      * org.identityconnectors.framework.Configuration)
      */
     @Override
     public final void init(final Configuration cfg) {
         configuration = (CSVDirConfiguration) cfg;
     }
 
     @Override
     public final Schema schema() {
         final SchemaBuilder bld = new SchemaBuilder(getClass());
         final String[] fields = configuration.getFields();
 
         final String deletedColumn = configuration.getDeleteColumnName();
         final String passwordColumn = configuration.getPasswordColumnName();
        final String[] keyColumns = configuration.getKeyColumnNames();
 
         final Set<AttributeInfo> attrInfos = new HashSet<AttributeInfo>();
         AttributeInfoBuilder abld = null;
 
         for (String fieldName : fields) {
             if (!fieldName.equals(deletedColumn)) {
                 if (fieldName.equalsIgnoreCase(passwordColumn)) {
                     abld.setName(OperationalAttributeInfos.PASSWORD.getName());
                } else if (keyColumns != null
                        && keyColumns.length == 1
                        && fieldName.equalsIgnoreCase(keyColumns[0])) {
                    abld.setName(Name.NAME);
                 } else {
                     abld.setName(fieldName.trim());
                 }
 
                 abld = new AttributeInfoBuilder();
                 abld.setCreateable(false);
                 abld.setUpdateable(false);
                 attrInfos.add(abld.build());
             }
         }
 
         // set it to object class account..
         bld.defineObjectClass(ObjectClass.ACCOUNT_NAME, attrInfos);
 
         // return the new schema object..
         return bld.build();
     }
 
     @Override
     public final FilterTranslator<FilterWhereBuilder> createFilterTranslator(
             final ObjectClass oclass, final OperationOptions options) {
         if (oclass == null || (!oclass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
         return new CSVDirFilterTranslator(this, oclass, options);
     }
 
     @Override
     public void executeQuery(
             final ObjectClass oclass,
             final FilterWhereBuilder where,
             final ResultsHandler handler,
             final OperationOptions options) {
         LOG.info("check the ObjectClass and result handler");
 
         // Contract tests
         if (oclass == null || (!oclass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Object class required");
         }
 
         if (handler == null) {
             throw new IllegalArgumentException("Result handler required");
         }
 
         LOG.ok("The ObjectClass and result handler is ok");
 
         final Set<String> columnNamesToGet = resolveColumnNamesToGet(options);
 
         LOG.ok("Column Names {0} To Get", columnNamesToGet);
 
         final String whereClause =
                 where != null ? where.getWhereClause() : null;
 
         LOG.ok("Where Clause {0}", whereClause);
 
         final List<SQLParam> params =
                 where != null ? where.getParams() : null;
 
         LOG.ok("Where Params {0}", params);
 
         ResultSet rs = null;
         CSVDirConnection connection = null;
 
         try {
             connection = CSVDirConnection.openConnection(configuration);
 
             rs = connection.allCsvFiles(whereClause, params);
 
             ConnectorObjectBuilder bld = null;
             String name = null;
             String value = null;
 
             Boolean handled = Boolean.TRUE;
 
             while (rs.next() && handled) {
                 bld = new ConnectorObjectBuilder();
                 for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                     name = rs.getMetaData().getColumnName(i);
                     value = rs.getString(name);
 
                     if (name.equalsIgnoreCase(
                             configuration.getPasswordColumnName())) {
                         bld.addAttribute(AttributeBuilder.buildPassword(
                                 value.toCharArray()));
                     } else {
                         bld.addAttribute(name, value);
                     }
                 }
 
                 final String uid =
                         createUid(configuration.getKeyColumnNames(), rs);
 
                 bld.setUid(uid);
                 bld.setName(uid);
 
                 // create the connector object..
                 handled = handler.handle(bld.build());
             }
         } catch (Exception e) {
             LOG.error(e, "Search query failed");
             throw new ConnectorIOException(e);
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
 
                 if (connection != null) {
                     connection.closeConnection();
                 }
             } catch (SQLException e) {
                 LOG.error(e, "Error closing connections");
             }
         }
         LOG.ok("Query Account commited");
     }
 
     @Override
     public final void sync(
             final ObjectClass objectClass,
             SyncToken syncToken,
             final SyncResultsHandler handler,
             final OperationOptions opetions) {
 
         // check objectclass
         if (objectClass == null || (!objectClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check objectclass
         if (handler == null) {
             throw new IllegalArgumentException("Invalid handler");
         }
 
         //check syncToken
         if ((syncToken == null) || syncToken.getValue() == null) {
             syncToken = new SyncToken(0);
         }
 
         CSVDirConnection connection = null;
 
         try {
             connection = CSVDirConnection.openConnection(configuration);
 
             buildSyncDelta(connection.modifiedCsvFiles(
                     Long.valueOf(syncToken.getValue().toString())), handler);
 
             token = connection.getFileSystem().getHighestTimeStamp();
         } catch (Exception e) {
             LOG.error(e, "error during syncronizatiion");
             throw new ConnectorIOException(e);
         } finally {
             try {
                 if (connection != null) {
                     connection.closeConnection();
                 }
             } catch (SQLException e) {
                 LOG.error("Error closing connections", e);
             }
         }
     }
 
     private void buildSyncDelta(final ResultSet rs,
             final SyncResultsHandler handler)
             throws SQLException {
 
         ConnectorObjectBuilder bld = null;
         SyncDeltaBuilder syncDeltaBuilder = null;
 
         Boolean handled = Boolean.TRUE;
         String name, value;
 
         while (rs.next() && handled) {
             bld = new ConnectorObjectBuilder();
             for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                 name = rs.getMetaData().getColumnName(i);
                 value = rs.getString(name);
 
                 if (name.equalsIgnoreCase(
                         configuration.getPasswordColumnName())) {
                     bld.addAttribute(AttributeBuilder.buildPassword(
                             value.toCharArray()));
                 } else {
                     bld.addAttribute(name, value);
                 }
             }
 
             final Uid uid = new Uid(
                     createUid(configuration.getKeyColumnNames(), rs));
 
             bld.setUid(uid);
             bld.setName(uid.getUidValue());
 
             syncDeltaBuilder = createSyncDelta(bld, uid);
             choiseRightDeltaType(rs, syncDeltaBuilder);
             handler.handle(syncDeltaBuilder.build());
         }
     }
 
     private String getValueFromColumnName(final ResultSet rs,
             final String columnName)
             throws SQLException {
         return rs.getString(rs.findColumn(columnName));
     }
 
     private void choiseRightDeltaType(final ResultSet rs,
             final SyncDeltaBuilder syncDeltaBuilder)
             throws SQLException {
         if (Boolean.valueOf(getValueFromColumnName(rs,
                 configuration.getDeleteColumnName()))) {
             syncDeltaBuilder.setDeltaType(SyncDeltaType.DELETE);
         } else {
             syncDeltaBuilder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
         }
     }
 
     private SyncDeltaBuilder createSyncDelta(
             final ConnectorObjectBuilder connObjectBuilder, final Uid uid) {
         final SyncDeltaBuilder syncDeltaBuilder = new SyncDeltaBuilder();
         syncDeltaBuilder.setObject(connObjectBuilder.build());
         syncDeltaBuilder.setUid(uid);
         syncDeltaBuilder.setToken(getLatestSyncToken(ObjectClass.ACCOUNT));
         return syncDeltaBuilder;
     }
 
     @Override
     public SyncToken getLatestSyncToken(final ObjectClass objectClass) {
         return new SyncToken(token);
     }
 
     @Override
     public void dispose() {
         // no actions
     }
 
     private Set<String> resolveColumnNamesToGet(OperationOptions options) {
 
         final Set<String> attributesToGet = new HashSet<String>();
         attributesToGet.add(Uid.NAME);
 
         String[] attributes = null;
 
         if (options != null && options.getAttributesToGet() != null) {
             attributes = options.getAttributesToGet();
         } else {
             attributes = configuration.getFields();
         }
 
         attributesToGet.addAll(Arrays.asList(attributes));
         return attributesToGet;
     }
 
     private String createUid(final String[] keys, final ResultSet rs)
             throws SQLException {
         final StringBuilder uid = new StringBuilder();
 
         if (keys != null && keys.length > 0) {
             for (String field : keys) {
                 if (uid.length() > 0) {
                     uid.append(configuration.getKeyseparator());
                 }
                 uid.append(rs.getString(field));
             }
         }
 
         return uid.toString();
     }
 }
