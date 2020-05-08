 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 
 import com.flexive.shared.structure.*;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.value.*;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.Pair;
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.search.FxPaths;
 import com.flexive.shared.search.FxSQLFunction;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.core.DatabaseConst;
 import static com.flexive.core.search.DataSelector.getBinaryValue;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.sqlParser.Property;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import static java.lang.Long.parseLong;
 import static java.lang.Integer.parseInt;
 
 /**
  * <p>
  * A single entry of the property resolver, i.e. a selected property. A new entry is instantiated for every
  * column of a search query, it also stores context information like the column index in the SQL result set.
  * </p>
  * <p>
  * To create a new property entry type (e.g. a new virtual property), you have to:
  * <ol>
  * <li>Register a new {@link PropertyEntry.Type} that matches the property name</li>
  * <li>Create a new subclass of {@link PropertyEntry} that selects the columns and specifies
  * a method to read the value from the result set</li>
  * <li>Extend the factory method {@link PropertyEntry.Type#createEntry()}</li>
  * <li>Only if you need database-specific procedure calls: extend the {@link DataSelector}
  * implementation</li>
  * </ol>
  * </p>
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class PropertyEntry {
     private static final Log LOG = LogFactory.getLog(PropertyEntry.class);
 
     /**
      * Property entry types. A type is either a generic property selector (e.g. {@link #PROPERTY_REF}),
      * or a custom resolver like the primary key or tree path "virtual" properties.
      */
     public static enum Type {
         /**
          * A common property reference, e.g. co.caption.
          */
         PROPERTY_REF(null),
         /**
          * A primary key (@pk) column.
          */
         PK("@pk"),
 
         /**
          * A tree node path (@path) column.
          */
         PATH("@path"),
 
         /**
          * A tree node position (@node_position) column.
          */
         NODE_POSITION("@node_position"),
 
         /**
          * A property permission (@permissions) column.
          */
         PERMISSIONS("@permissions");
 
         private final String propertyName;
 
         Type(String propertyName) {
             this.propertyName = propertyName;
         }
 
         /**
          * Returns the property name this type applies for. If it is a generic type
          * (i.e. {@link #PROPERTY_REF}, this method returns null.
          *
          * @return the property name this type applies for
          */
         public String getPropertyName() {
             return propertyName;
         }
 
         /**
          * Returns true if this type matches the given property name (e.g. "@pk").
          * For generic types (i.e. {@link #PROPERTY_REF}), this method always returns false.
          *
          * @param name the property name to be matched
          * @return true if this type matches the given property name (e.g. "@pk").
          */
         public boolean matchesProperty(String name) {
             return StringUtils.equalsIgnoreCase(propertyName, name);
         }
 
         /**
          * Create a new {@link PropertyEntry} instance for this property type. Does not
          * work for generic entries (i.e. {@link #PROPERTY_REF}), these have to be created
          * manually by creating a new instance of the generic {@link PropertyEntry} class.
          *
          * @return a new {@link PropertyEntry} instance for this property type.
          */
         public PropertyEntry createEntry() {
             switch (this) {
                 case PK:
                     return new PkEntry();
                 case NODE_POSITION:
                     return new NodePositionEntry();
                 case PATH:
                     return new PathEntry();
                 case PERMISSIONS:
                     return new PermissionsEntry();
                 case PROPERTY_REF:
                     throw new FxSqlSearchException(LOG, "ex.sqlSearch.entry.virtual").asRuntimeException();
                 default:
                     throw new IllegalStateException();
             }
         }
     }
 
     /**
      * The primary key resolver (@pk)
      */
     private static class PkEntry extends PropertyEntry {
         private PkEntry() {
             super(Type.PK,
                     PropertyResolver.Table.T_CONTENT,
                     new String[]{"ID", "VERSION"},
                     null, false, null, null);
         }
 
         @Override
         public Object getResultValue(ResultSet rs, long languageId, boolean xpathAvailable) throws FxSqlSearchException {
             try {
                 final long id = rs.getLong(positionInResultSet);
                 final int ver = rs.getInt(positionInResultSet + 1);
                 return new FxPK(id, ver);
             } catch (SQLException e) {
                 throw new FxSqlSearchException(LOG, e);
             }
         }
     }
 
     /**
      * The tree path resolver (@path)
      */
     private static class PathEntry extends PropertyEntry {
         private PathEntry() {
             super(Type.PATH,
                     PropertyResolver.Table.T_CONTENT,
                     new String[]{""},    // select one column (function will be inserted by DB adapter)
                     null, false, null, null);
         }
 
         @Override
         public Object getResultValue(ResultSet rs, long languageId, boolean xpathAvailable) throws FxSqlSearchException {
             try {
                 return new FxPaths(rs.getString(positionInResultSet));
             } catch (SQLException e) {
                 throw new FxSqlSearchException(LOG, e);
             }
         }
     }
 
     /**
      * The tree node position resolver (@node_position)
      */
     private static class NodePositionEntry extends PropertyEntry {
         private NodePositionEntry() {
             super(Type.NODE_POSITION, PropertyResolver.Table.T_CONTENT,
                     new String[]{""},    // select one column (function will be inserted by DB adapter)
                     null, false, FxDataType.Number, null);
         }
 
         @Override
         public Object getResultValue(ResultSet rs, long languageId, boolean xpathAvailable) throws FxSqlSearchException {
             try {
                 return rs.getLong(positionInResultSet);
             } catch (SQLException e) {
                 throw new FxSqlSearchException(LOG, e);
             }
         }
     }
 
     private static class PermissionsEntry extends PropertyEntry {
         private static final String[] READ_COLUMNS = new String[] { "acl", "created_by", "step", "tdef", "mandator" };
 
         private final FxEnvironment environment;
 
         private PermissionsEntry() {
             super(Type.PERMISSIONS, PropertyResolver.Table.T_CONTENT, READ_COLUMNS,
                     null, false, null, null);
             this.environment = CacheAdmin.getEnvironment();
         }
 
         @Override
         public Object getResultValue(ResultSet rs, long languageId, boolean xpathAvailable) throws FxSqlSearchException {
             try {
                 final long aclId = rs.getLong(positionInResultSet + getIndex("acl"));
                 final long createdBy = rs.getLong(positionInResultSet + getIndex("created_by"));
                 final long stepId = rs.getLong(positionInResultSet + getIndex("step"));
                 final long typeId = rs.getLong(positionInResultSet + getIndex("tdef"));
                 final long mandatorId = rs.getLong(positionInResultSet + getIndex("mandator"));
                 return FxPermissionUtils.getPermissions(aclId, environment.getType(typeId),
                         environment.getStep(stepId).getAclId(), createdBy, mandatorId);
             } catch (SQLException e) {
                 throw new FxSqlSearchException(e);
             } catch (FxNoAccessException e) {
                 // search should never have returned an object without read permissions
                 LOG.error("Search returned an object without read permissions");
                 throw e.asRuntimeException();
             }
         }
 
         private int getIndex(String name) {
             return ArrayUtils.indexOf(READ_COLUMNS, name);
         }
     }
 
     protected final String[] readColumns;
     protected final String filterColumn;
     protected final String tableName;
     protected final FxProperty property;
     protected final PropertyResolver.Table tbl;
     protected final Type type;
     protected final boolean multilanguage;
     protected final List<FxSQLFunction> functions = new ArrayList<FxSQLFunction>();
     protected int positionInResultSet = -1;
 
     /**
      * Create a new instance based on the given (search) property.
      *
      * @param searchProperty the search property
      * @param storage        the storage instance
      * @param ignoreCase     whether case should be ignored for this column
      * @throws FxSqlSearchException if the entry could not be created
      */
     public PropertyEntry(Property searchProperty, ContentStorage storage, boolean ignoreCase) throws FxSqlSearchException {
         this.type = Type.PROPERTY_REF;
 
         if (searchProperty.isAssignment()) {
             try {
                 if (StringUtils.isNumeric(searchProperty.getPropertyName())) {
                     //#<id>
                     this.assignment = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(Long.valueOf(searchProperty.getPropertyName()));
                 } else {
                     //XPath
                     this.assignment = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(searchProperty.getPropertyName());
                 }
             } catch (ClassCastException ce) {
                 throw new FxSqlSearchException(LOG, ce, "ex.sqlSearch.query.unknownAssignment",
                         searchProperty.getPropertyName());
             } catch (FxRuntimeException e) {
                 if (e.getConverted() instanceof FxNotFoundException) {
                     throw new FxSqlSearchException(LOG, e, "ex.sqlSearch.query.unknownAssignment",
                             searchProperty.getPropertyName());
                 } else {
                     throw new FxSqlSearchException(LOG, e, "ex.sqlSearch.query.failedToResolveAssignment",
                             searchProperty.getPropertyName(), e.getMessage());
                 }
             }
             this.property = assignment.getProperty();
         } else {
             this.assignment = null;
             this.property = CacheAdmin.getEnvironment().getProperty(searchProperty.getPropertyName());
         }
         this.readColumns = getReadColumns(storage, property);
         String fcol = ignoreCase ? storage.getQueryUppercaseColumn(this.property) : this.readColumns[0];
         if (fcol == null) {
             fcol = this.readColumns == null ? null : this.readColumns[0];
         }
         this.filterColumn = fcol;
 
         if (this.filterColumn == null) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.init.propertyDoesNotHaveColumnMapping",
                     searchProperty.getPropertyName());
         }
 
         this.tableName = storage.getTableName(this.property);
         if (this.tableName.equalsIgnoreCase(DatabaseConst.TBL_CONTENT)) {
             this.tbl = PropertyResolver.Table.T_CONTENT;
         } else if (this.tableName.equalsIgnoreCase(DatabaseConst.TBL_CONTENT_DATA)) {
             this.tbl = PropertyResolver.Table.T_CONTENT_DATA;
         } else {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.err.unknownPropertyTable", searchProperty, this.tableName);
         }
         this.multilanguage = this.property.isMultiLang();
         this.functions.addAll(searchProperty.getFunctions());
         if (this.functions.size() > 0) {
             // use outmost function result type
             this.overrideDataType = this.functions.get(0).getOverrideDataType();
         }
     }
 
     protected FxPropertyAssignment assignment;
 
     protected FxDataType overrideDataType;
 
     public PropertyEntry(Type type, PropertyResolver.Table tbl, String[] readColumns, String filterColumn, boolean multilanguage, FxDataType overrideDataType, FxProperty property) {
         this.readColumns = readColumns;
         this.filterColumn = filterColumn;
         this.tbl = tbl;
         this.type = type;
         this.multilanguage = multilanguage;
         this.overrideDataType = overrideDataType;
         this.property = property;
         this.tableName = tbl != null ? tbl.getTableName() : null;
     }
 
     public static String[] getReadColumns(ContentStorage storage, FxProperty property) {
         if (FxDataType.Date.equals(property.getDataType()) || FxDataType.DateTime.equals(property.getDataType())
                 || FxDataType.HTML.equals(property.getDataType())) {
             // date values: use only first column, otherwise date functions cannot be appliaed
             // HTML values: don't need boolean and upper case columns for search result
             return new String[] { storage.getColumns(property)[0] };
         } else {
             return storage.getColumns(property);
         }
     }
     /**
      * Return the result value of this property entry in a given result set.
      *
      * @param rs       the SQL result set
      * @param languageId
      * @param xpathAvailable if the XPath was selected as an additional column for content table entries
      * @return the value of this property (column) in the result set
      * @throws FxSqlSearchException if the database cannot read the value
      */
     public Object getResultValue(ResultSet rs, long languageId, boolean xpathAvailable) throws FxSqlSearchException {
         final FxValue result;
         final int pos = positionInResultSet;
         // Handle by type
         try {
             switch (overrideDataType == null ? property.getDataType() : overrideDataType) {
                 case DateTime:
                     if (rs.getMetaData().getColumnType(pos) == java.sql.Types.BIGINT) {
                         result = new FxDateTime(multilanguage, FxLanguage.SYSTEM_ID, new Date(rs.getLong(pos)));
                         break;
                     }
                     Timestamp dttstp = rs.getTimestamp(pos);
                     Date _dtdate = dttstp != null ? new Date(dttstp.getTime()) : null;
                     result = new FxDateTime(multilanguage, FxLanguage.SYSTEM_ID, _dtdate);
                     if (dttstp == null) {
                         result.setEmpty(FxLanguage.SYSTEM_ID);
                     }
                     break;
                 case Date:
                     Timestamp tstp = rs.getTimestamp(pos);
                     result = new FxDate(multilanguage, FxLanguage.SYSTEM_ID, tstp != null ? new Date(tstp.getTime()) : null);
                     if (tstp == null) {
                         result.setEmpty();
                     }
                     break;
                 case DateRange:
                    final Pair<Date, Date> pair = decodeDateRange(rs, pos, 1);
                     if (pair.getFirst() == null || pair.getSecond() == null) {
                         result = new FxDateRange(multilanguage, FxLanguage.SYSTEM_ID, FxDateRange.EMPTY);
                         result.setEmpty(FxLanguage.SYSTEM_ID);
                     } else {
                         result = new FxDateRange(multilanguage, FxLanguage.SYSTEM_ID, new DateRange(pair.getFirst(), pair.getSecond()));
                     }
                     break;
                 case DateTimeRange:
                    final Pair<Date, Date> pair2 = decodeDateRange(rs, pos, 1);
                     if (pair2.getFirst() == null || pair2.getSecond() == null) {
                         result = new FxDateTimeRange(multilanguage, FxLanguage.SYSTEM_ID, FxDateRange.EMPTY);
                         result.setEmpty(FxLanguage.SYSTEM_ID);
                     } else {
                         result = new FxDateTimeRange(new DateRange(pair2.getFirst(), pair2.getSecond()));
                     }
                     break;
                 case HTML:
                     result = new FxHTML(multilanguage, FxLanguage.SYSTEM_ID, rs.getString(pos));
                     break;
                 case String1024:
                 case Text:
                     result = new FxString(multilanguage, FxLanguage.SYSTEM_ID, rs.getString(pos));
                     break;
                 case LargeNumber:
                     result = new FxLargeNumber(multilanguage, FxLanguage.SYSTEM_ID, rs.getLong(pos));
                     break;
                 case Number:
                     result = new FxNumber(multilanguage, FxLanguage.SYSTEM_ID, rs.getInt(pos));
                     break;
                 case Float:
                     result = new FxFloat(multilanguage, FxLanguage.SYSTEM_ID, rs.getFloat(pos));
                     break;
                 case Boolean:
                     result = new FxBoolean(multilanguage, FxLanguage.SYSTEM_ID, rs.getBoolean(pos));
                     break;
                 case Double:
                     result = new FxDouble(multilanguage, FxLanguage.SYSTEM_ID, rs.getDouble(pos));
                     break;
                 case Reference:
                     result = new FxReference(new ReferencedContent(new FxPK(rs.getLong(pos), FxPK.MAX)));  // TODO!!
                     break;
                 case SelectOne:
                     FxSelectListItem oneItem = CacheAdmin.getEnvironment().getSelectListItem(rs.getLong(pos));
                     result = new FxSelectOne(multilanguage, FxLanguage.SYSTEM_ID, oneItem);
                     break;
                 case SelectMany:
                     FxSelectListItem manyItem = CacheAdmin.getEnvironment().getSelectListItem(rs.getLong(pos));
                     SelectMany valueMany = new SelectMany(manyItem.getList());
                     valueMany.selectFromList(rs.getString(pos + 1));
                     result = new FxSelectMany(multilanguage, FxLanguage.SYSTEM_ID, valueMany);
                     break;
                 case Binary:
                     result = new FxBinary(multilanguage, FxLanguage.SYSTEM_ID, decodeBinary(rs, pos));
                     break;
                 default:
                     throw new FxSqlSearchException(LOG, "ex.sqlSearch.reader.UnknownColumnType",
                             String.valueOf(getProperty().getDataType()));
             }
 
             if (rs.wasNull()) {
                 result.setEmpty(languageId);
             }
 
 
             if (xpathAvailable && getTableType() == PropertyResolver.Table.T_CONTENT_DATA) {
                 // Get the XPATH if we are reading from the content data table
                 result.setXPath(rs.getString(positionInResultSet + getReadColumns().length));
             } else if (xpathAvailable && getTableType() == PropertyResolver.Table.T_CONTENT && property != null) {
                 // set XPath for system-internal properties
                 result.setXPath("ROOT/" + property.getName());
             }
             return result;
         } catch (SQLException e) {
             throw new FxSqlSearchException(e);
         }
     }
 
     /**
      * Decodes a daterange result value consisting of two date columns.
      * @param rs    the result set
      * @param pos   the position of the first date column
      * @param secondDateOffset  the offset for the second date column
      * @return      the decoded dates. If a column is null, the corresponding entry is also null.
      * @throws SQLException if the column datatypes don't match
      */
     private Pair<Date, Date> decodeDateRange(ResultSet rs, int pos, int secondDateOffset) throws SQLException {
         final Timestamp fromTimestamp = rs.getTimestamp(pos);                   // FDATE1
         final Timestamp toTimestamp = rs.getTimestamp(pos + secondDateOffset);  // FDATE2
         final Date from = fromTimestamp != null ? new Date(fromTimestamp.getTime()) : null;
         final Date to = toTimestamp != null ? new Date(toTimestamp.getTime()) : null;
         return new Pair<Date, Date>(from, to);
     }
 
     private BinaryDescriptor decodeBinary(ResultSet rs, int pos) throws SQLException {
         final String encodedBinary = rs.getString(pos);
         if (rs.wasNull()) {
             return new BinaryDescriptor();
         }
         final String[] values = StringUtils.split(encodedBinary, DataSelector.BINARY_DELIM);
         return new BinaryDescriptor(CacheAdmin.getStreamServers(),
                 parseLong(getBinaryValue(values, "ID")), 1, 1,
                 parseLong(getBinaryValue(values, "CREATED_AT")),
                 getBinaryValue(values, "NAME"),
                 parseLong(getBinaryValue(values, "BLOBSIZE")),
                 getBinaryValue(values, "XMLMETA"),
                 getBinaryValue(values, "MIMETYPE"),
                 "1".equals(getBinaryValue(values, "ISIMAGE")),
                 Double.parseDouble(getBinaryValue(values, "RESOLUTION")),
                 parseInt(getBinaryValue(values, "WIDTH")),
                 parseInt(getBinaryValue(values, "HEIGHT"))
         );
     }
 
     /**
      * Return the entry type.
      *
      * @return  the entry type.
      */
     public Type getType() {
         return type;
     }
 
 
     /**
      * Overrides the data type this entry represents, e.g. by selectors that load property
      * fields from an external table like the {@link com.flexive.core.search.genericSQL.GenericSQLACLSelector}.
      *
      * @param type  the data type of this entry
      */
     public void overrideDataType(FxDataType type) {
         this.overrideDataType = type;
     }
 
     /**
      * Set the entry's result set index while the query is being built.
      *
      * @param positionInResultSet   the entry's result set index
      */
     public void setPositionInResultSet(int positionInResultSet) {
         this.positionInResultSet = positionInResultSet;
     }
 
     /**
      * Return the table type if it's a predefined table (like FX_CONTENT), null otherwise.
      *
      * @return  the table type if it's a predefined table (like FX_CONTENT), null otherwise.
      */
     public PropertyResolver.Table getTableType() {
         return tbl;
     }
 
     /**
      * The column(s) to read the result from.
      *
      * @return the column(s) to read the result from
      */
     public String[] getReadColumns() {
         return readColumns;
     }
 
     /**
      * Return the column name to be used for filtering (i.e. in the 'WHERE' clause of the query).
      *
      * @return  the column name to be used for filtering
      */
     public String getFilterColumn() {
         return filterColumn;
     }
 
     /**
      * Return the database table name to be used for selecting/filtering, e.g.
      * FX_CONTENT_DATA.
      *
      * @return  the database table name to be used for selecting/filtering
      */
     public String getTableName() {
         return StringUtils.defaultString(tableName, tbl.getTableName());
     }
 
     /**
      * Returns the structure property, may be null if this entry does not actually represent
      * a structure element.
      *
      * @return  the structure property
      */
     public FxProperty getProperty() {
         return property;
     }
 
     /**
      * Returns true if this entry represents a structure (property) assignment.
      *
      * @return  true if this entry represents a structure (property) assignment.
      */
     public boolean isAssignment() {
         return assignment != null;
     }
 
     /**
      * Returns the (property) assignment. May be null if this entry does not represent
      * a structure element.
      *
      * @return  the property assignment
      */
     public FxPropertyAssignment getAssignment() {
         return assignment;
     }
 
     /**
      * Returns true if the given column is a date column with millisecond precision
      * (i.e. a 'long' SQL type instead of a native date).
      *
      * @param columnName    the database column name
      * @return  true if the given column is a date column with millisecond precision
      */
     public static boolean isDateMillisColumn(String columnName) {
         return "CREATED_AT".equalsIgnoreCase(columnName)
                 || "MODIFIED_AT".equalsIgnoreCase(columnName);
     }
 
     /**
      * Returns a (column, value) pair for a SQL comparison condition against the given constant value.
      *
      * @param constantValue the value to be compared, as returned by the SQL parser
      * @return              (column, value) for the comparison condition. The value is already escaped
      *                      for use in a SQL query.
      */
     public Pair<String, String> getComparisonCondition(String constantValue) {
         String column = getFilterColumn();
         String value = null;
         switch (getProperty().getDataType()) {
             case String1024:
             case Text:
             case HTML:
                 value = constantValue;
                 if (value == null) {
                     value = "NULL";
                 } else {
                     // First remove surrounding "'" characters
                     value = FxFormatUtils.unquote(value);
                     // single quotes ("'") should be quoted already, otherwise the
                     // parser would have failed
 
                     // Convert back to an SQL string
                     value = "'" + value + "'";
                 }
                 break;
             case LargeNumber:
                 if ("STEP".equals(column)) {
                     // filter by workflow step definition, not internal step ID
                     column = "(SELECT sd.stepdef FROM " + DatabaseConst.TBL_STEP + " sd " +
                             " WHERE sd.id=cd." + column + ")";
                 }
                 value = "" + FxFormatUtils.toLong(constantValue);
                 break;
             case Number:
                 value = "" + FxFormatUtils.toInteger(constantValue);
                 break;
             case Double:
                 value = "" + FxFormatUtils.toDouble(constantValue);
                 break;
             case Float:
                 value = "" + FxFormatUtils.toFloat(constantValue);
                 break;
             case SelectOne:
                 if (StringUtils.isNumeric(constantValue)) {
                     //list item id, nothing to lookup
                     value = constantValue;
                 } else {
                     //list item data (of first matching item)
                     value = "" + getProperty().getReferencedList().
                             getItemByData(FxFormatUtils.unquote(constantValue)).getId();
                 }
                 break;
             case Boolean:
                 value = FxFormatUtils.toBoolean(constantValue) ? "1" : "0";
                 break;
             case Date:
             case DateRange:
                 if (constantValue == null) {
                     value = "NULL";
                 } else {
                     value = "'" + FxFormatUtils.getDateTimeFormat().format(FxFormatUtils.toDate(constantValue)) + "'";
                 }
                 break;
             case DateTime:
             case DateTimeRange:
                 // CREATED_AT and MODIFIED_AT store the date in a "long" column with millisecond precision
 
                 if (constantValue == null) {
                     value = "NULL";
                 } else {
                     final Date date = FxFormatUtils.toDateTime(constantValue);
                     if (isDateMillisColumn(getFilterColumn())) {
                         value = String.valueOf(date.getTime());
                     } else {
                         value = "'" + FxFormatUtils.getDateTimeFormat().format(date) + "'";
                     }
                 }
                 break;
             case Reference:
                 value = String.valueOf(FxPK.fromString(constantValue).getId());
                 break;
         }
         return value != null ? new Pair<String, String>(column, value) : null;
     }
 
 
 }
