 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.search;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.TypeStorageMode;
 import com.flexive.sqlParser.FxStatement;
 import com.flexive.sqlParser.Property;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.util.*;
 
 /**
  * A resolver for properties
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class PropertyResolver {
     public static enum Table {
         T_CONTENT(DatabaseConst.TBL_CONTENT),
         T_CONTENT_DATA(DatabaseConst.TBL_CONTENT_DATA);
 
         private final String tableName;
 
         Table(String tableName) {
             this.tableName = tableName;
         }
 
         public String getTableName() {
             return tableName;
         }
     }
 
     private static final Log LOG = LogFactory.getLog(PropertyResolver.class);
     private static Map<String, FxDataType> CONTENT_PROPS = null;
 
     private ContentStorage hierarchicalStorage;
     private Map<String, PropertyEntry> cache = new HashMap<String, PropertyEntry>(50);
     private List<PropertyEntry> resultSetColumns;
     private int resultSetPos = DataSelector.INTERNAL_RESULTCOLS.length + 1; // start after internal properties
     private int resultSetColumnCount = 0;
 
     protected PropertyResolver(Connection con) throws FxSqlSearchException {
         try {
             this.hierarchicalStorage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
             this.resultSetColumns = new ArrayList<PropertyEntry>(50);
         } catch (FxNotFoundException e) {
             throw new FxSqlSearchException(LOG, e);
         } catch (Exception e) {
             throw new FxSqlSearchException(LOG, "Init error:" + e.getMessage());
         }
         initColumnInformations(con);
     }
 
     public void addResultSetColumn(PropertyEntry e) {
         // Store actuall resultset position
         e.setPositionInResultSet(resultSetPos);
         this.resultSetColumns.add(e);
         // compute next position to use
         resultSetPos += e.getReadColumns().length;
         if (e.getTableType() == Table.T_CONTENT_DATA) {
             // also select XPATH 
             resultSetPos += 1;
         }
         // compute total result set columns
         resultSetColumnCount += e.getReadColumns().length;
     }
 
 
     public int getResultSetColumnCount() {
         return resultSetColumnCount;
     }
 
     public List<PropertyEntry> getResultSetColumns() {
         return resultSetColumns;
     }
 
     public PropertyEntry get(FxStatement fx_stmt, Property prop) throws FxSqlSearchException {
         final com.flexive.sqlParser.Table tbl = fx_stmt.getTableByAlias(prop.getTableAlias());
         switch (tbl.getType()) {
             case CONTENT:
                 return getByName(fx_stmt, prop);
             default:
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.table.typeNotSupported", tbl.getType());
         }
     }
 
     private PropertyEntry getByName(final FxStatement fx_stmt, final Property property) throws FxSqlSearchException {
         final String key = fx_stmt.getIgnoreCase() + "_" + property.getPropertyName() +
                 (property.hasField() ? "_" + property.getField() : "");
         PropertyEntry entry = cache.get(key);
         if (entry == null) {
             // first check hardcoded virtual properties like @pk and @path
            for (PropertyEntry.Type type : PropertyEntry.Type.values()) {
                 if (type.matchesProperty(property.getPropertyName())) {
                     entry = type.createEntry();
                     break;
                 }
             }
             if (entry == null) {
                 entry = new PropertyEntry(property, hierarchicalStorage, fx_stmt.getIgnoreCase());
             }
             cache.put(key, entry);
         }
         return entry;
     }
 
     /**
      * Initializes the column informations.
      *
     * @param con an existing connection
      * @throws FxSqlSearchException if the init fails
      */
     private synchronized void initColumnInformations(Connection con) throws FxSqlSearchException {
         if (CONTENT_PROPS != null) {
             return;
         }
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             final ResultSet rs = stmt.executeQuery("select * from " + DatabaseConst.TBL_CONTENT + " where 1=2");
             final ResultSetMetaData rsmd = rs.getMetaData();
             CONTENT_PROPS = new Hashtable<String, FxDataType>(10);
             for (int i = 0; i < rsmd.getColumnCount(); i++) {
                 final String columnName = rsmd.getColumnName(i + 1).trim().toUpperCase();
                 final int type = rsmd.getColumnType(i + 1);
                 final FxDataType dt;
                 switch (type) {
                     case java.sql.Types.BIGINT:
                        if ("CREATED_AT".equals(columnName) || "MODIFIED_AT".equals(columnName)) {
                             dt = FxDataType.DateTime;
                             break;
                         }
                     case java.sql.Types.NUMERIC:
                         dt = FxDataType.LargeNumber;
                         break;
                     case java.sql.Types.INTEGER:
                     case java.sql.Types.SMALLINT:
                     case java.sql.Types.TINYINT:
                         dt = FxDataType.Number;
                         break;
                     case java.sql.Types.TIMESTAMP:
                         dt = FxDataType.DateTime;
                         break;
                     case java.sql.Types.VARCHAR:
                         dt = FxDataType.String1024;
                         break;
                     case java.sql.Types.BIT:
                     case java.sql.Types.BOOLEAN:
                         dt = FxDataType.Boolean;
                         break;
                     default:
                         throw new FxSqlSearchException(LOG, "ex.sqlSearch.init.unknowColumnType", columnName,
                                 DatabaseConst.TBL_CONTENT, type);
                 }
                 CONTENT_PROPS.put(columnName, dt);
             }
         } catch (FxSqlSearchException exc) {
             throw exc;
         } catch (Exception exc) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.init.failedToReadTableMetadata", DatabaseConst.TBL_CONTENT);
         } finally {
             Database.closeObjects(PropertyResolver.class, null, stmt);
         }
     }
 }
