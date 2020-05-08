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
 package com.flexive.core.search.mysql;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.search.DataFilter;
 import com.flexive.core.search.PropertyEntry;
 import com.flexive.core.search.SqlSearch;
 import com.flexive.core.storage.FxTreeNodeInfo;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.genericSQL.GenericTreeStorage;
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.search.FxFoundType;
 import com.flexive.shared.search.query.VersionFilter;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.sqlParser.*;
 import static com.flexive.sqlParser.Condition.Comparator;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 // NVL --> IFNULL((select sub.id from FX_CONTENT_DATA sub where sub.id=filter.id),1)
 
 /**
  * Builds a select statement selecting all id's and versions that the
  * search should return as resultset.
  * <p/>
  * Example result statement:<br>
  * SELECT tbl6.id,tbl6.ver from <br>
  * (SELECT DISTINCT ID,VER,LANG FROM FX_CONTENT_DATA CO WHERE CO.id=1 and ISMAX_VER=true)  tbl6<br>
  * ,(SELECT * from (<br>
  * (SELECT DISTINCT ID,VER,LANG FROM FX_CONTENT_DATA CO WHERE CO.id=1 and ISMAX_VER=true)<br>
  * UNION<br>
  * (SELECT DISTINCT ID,VER,LANG FROM FX_CONTENT_DATA CO WHERE CO.id=2 and ISMAX_VER=true) ) unInner7) tbl7<br>
  * WHERE  tbl6.id=tbl7.id and tbl6.ver=tbl7.ver and tbl6.lang=tbl7.lang
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class MySQLDataFilter extends DataFilter {
     private static final transient Log LOG = LogFactory.getLog(MySQLDataFilter.class);
 
 //    private static final String NO_MATCH = "(SELECT DISTINCT null id,null ver,null lang FROM dual where 1=2)";
 
     // The maximum rows returned by a subquery (property or assignement search)
     // May be -1 to use all found rows (not recommended, since MySQL will be VERY slow in this case).
     private static final int SUBQUERY_LIMIT = 10000;
 
     private final String tableMain;
     private final String tableContentData;
     private final String tableFulltext;
     private final List<FxFoundType> contentTypes = new ArrayList<FxFoundType>(10);
 
     private int foundEntryCount;
     private boolean truncated;
     private Connection con;
 
     public MySQLDataFilter(Connection con, SqlSearch search) throws FxSqlSearchException {
         super(con, search);
         this.con = con;
         final long[] briefcaseIds = getStatement().getBriefcaseFilter();
         if (briefcaseIds.length == 0) {
             tableMain = DatabaseConst.TBL_CONTENT;
             tableContentData = DatabaseConst.TBL_CONTENT_DATA;
             tableFulltext = DatabaseConst.TBL_CONTENT_DATA_FT;
         } else {
             // TODO: Add briefcase security (may the user access the specified briefcases?)
             // TODO: Filter out undesired / desired languages
             final String briefcaseIdFilter = "SELECT id from " + DatabaseConst.TBL_BRIEFCASE_DATA +
                     " WHERE briefcase_id " +
                     (getStatement().getBriefcaseFilter().length == 1 ? ("=" + briefcaseIds[0]) : "IN (" +
                             FxArrayUtils.toSeparatedList(briefcaseIds, ',') + ")");
             tableContentData = "(SELECT * FROM " + DatabaseConst.TBL_CONTENT_DATA +
                     " WHERE id IN (" + briefcaseIdFilter + ") " + getVersionFilter(null) + ")";
             tableMain = "(SELECT * FROM " + DatabaseConst.TBL_CONTENT +
                     " WHERE id IN (" + briefcaseIdFilter + ") " + getVersionFilter(null) +
                     getTypeFilter(null) + getMandatorFilter(null) + ")";
             tableFulltext = "(SELECT * FROM " + DatabaseConst.TBL_CONTENT_DATA_FT +
                     " WHERE id IN (" + briefcaseIdFilter + "))"; //versions are matched against content_data table
         }
     }
 
     /**
      * Cleanup all temporary data used for the search.
      *
      * @throws FxSqlSearchException if the cleanup failed
      */
     @Override
     public void cleanup() throws FxSqlSearchException {
         if (search.getCacheTable().equals(DatabaseConst.TBL_SEARCHCACHE_MEMORY)) {
             Statement stmt = null;
             try {
                 stmt = getConnection().createStatement();
                 stmt.executeUpdate("DELETE FROM " + DatabaseConst.TBL_SEARCHCACHE_MEMORY +
                         " WHERE search_id=" + search.getSearchId());
             } catch (Throwable t) {
                 throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.err.failedToClearTempSearchResult", search.getSearchId());
             } finally {
                 Database.closeObjects(MySQLDataSelector.class, null, stmt);
             }
         }
     }
 
     /**
      * Builds the filter.
      *
      * @throws FxSqlSearchException if the build failed
      */
     @Override
     public void build() throws FxSqlSearchException {
         final UserTicket ticket = FxContext.get().getTicket();
         final long maxRows = search.getFxStatement().getMaxResultRows() + 1;
         Statement stmt = null;
         String sql = null;
         try {
             final String dataSelect;
             final String securityFilter = ticket.isGlobalSupervisor() ? "" :
                     "WHERE mayReadInstance2(data2.id,data2.ver," + ticket.getUserId() + "," +
                             ticket.getMandatorId() + "," + ticket.isMandatorSupervisor() + "," + ticket.isGlobalSupervisor() + ")\n";
             if (getStatement().getType() == FxStatement.Type.ALL) {
                 // The statement will not filter the data
                 dataSelect = "SELECT " + search.getSearchId() + " search_id,id,ver,tdef FROM " + tableMain + " data2 " +
                         securityFilter + " LIMIT " + maxRows;
             } else {
                 // The statement filters the data
                 StringBuilder result = new StringBuilder(5000);
                 build(result, getStatement().getRootBrace());
                 // Remove leading and ending brace
                 result.deleteCharAt(0);
                 result.deleteCharAt(result.length() - 1);
                 // Finalize the select
                 dataSelect = "SELECT * FROM (SELECT DISTINCT " + search.getSearchId() +
                         " search_id,data.id,data.ver,main.tdef \n" +
                         "FROM (" + result.toString() + ") data, " + DatabaseConst.TBL_CONTENT + " main\n" +
                         "WHERE data.ver=main.ver AND data.id=main.id) data2\n" + securityFilter +
                         // Limit by the specified max items
                         "LIMIT " + maxRows;
             }
             // Find all matching data enties and store them
             if (LOG.isDebugEnabled()) {
                 LOG.debug("SQL getResult: \n" + dataSelect + "\n");
             }
             sql = "INSERT INTO " + search.getCacheTable() + " (" + dataSelect + ")";
             stmt = getConnection().createStatement();
             stmt.setQueryTimeout(search.getParams().getQueryTimeout());
             stmt.executeUpdate(sql);
             analyzeResult();
         } catch (FxSqlSearchException exc) {
             throw exc;
         } catch (SQLException e) {
             throw new FxSqlSearchException(LOG, e, "ex.sqlSearch.failedToBuildDataFilter.sql", e.getMessage(), search.getQuery(), sql);
         } catch (Throwable t) {
             throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.failedToBuildDataFilter", t.getMessage(), search.getQuery());
         } finally {
             Database.closeObjects(MySQLDataFilter.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc} *
      */
     @Override
     public int getFoundEntries() {
         return foundEntryCount;
     }
 
     /**
      * {@inheritDoc} *
      */
     @Override
     public boolean isTruncated() {
         return truncated;
     }
 
     /**
      * Analyzes the result and stores the info in object variables.
      *
      * @throws FxSqlSearchException if the function fails
      */
     private void analyzeResult() throws FxSqlSearchException {
         Statement stmt = null;
         try {
             stmt = getConnection().createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT count(*), tdef FROM " + search.getCacheTable() +
                     " WHERE search_id=" + search.getSearchId() + " GROUP BY tdef");
             foundEntryCount = 0;
             while (rs != null && rs.next()) {
                 final FxFoundType type = new FxFoundType(rs.getLong(2), rs.getInt(1));
                 foundEntryCount += type.getFoundEntries();
                 contentTypes.add(type);
             }
 
             if (foundEntryCount > search.getFxStatement().getMaxResultRows()) {
                 foundEntryCount = search.getFxStatement().getMaxResultRows();
                 truncated = true;
             } else {
                 truncated = false;
             }
             /*rs = stmt.executeQuery("SELECT COUNT(*) FROM "+search.getCacheTable()+" WHERE SEARCH_ID="+search.getSearchId());
             long totalFiltered = 0;
             if( rs != null && rs.next() )
                 totalFiltered = rs.getLong(1);
             System.out.println("Total filtered: "+totalFiltered);*/
         } catch (Throwable t) {
             throw new FxSqlSearchException(LOG, t, "ex.sqlSearch.failedToCountFoundEntries", search.getSearchId());
         } finally {
             Database.closeObjects(MySQLDataFilter.class, null, stmt);
         }
     }
 
     /**
      * Returns a list of all content types that are part of the resultset.
      *
      * @return a list of all content types that are part of the resultset
      */
     @Override
     public List<FxFoundType> getContentTypes() {
         return this.contentTypes;
     }
 
     private void build(StringBuilder sb, Brace br) throws FxSqlSearchException {
         if (br.isAnd()) {
             buildAnd(sb, br);
         } else {
             buildOr(sb, br);
         }
     }
 
     /**
      * Builds an 'OR' condition.
      *
      * @param sb the string buffer to use
      * @param br the brace
      * @throws FxSqlSearchException if the build failed
      */
     private void buildOr(StringBuilder sb, Brace br) throws FxSqlSearchException {
         // Start OR
         sb.append("(SELECT * FROM (\n");
         int pos = 0;
         for (BraceElement be : br.getElements()) {
             if (pos > 0) {
                 sb.append("\nUNION\n");
             }
             if (be instanceof Condition) {
                 sb.append(getConditionSubQuery(br.getStatement(), (Condition) be));
             } else if (be instanceof Brace) {
                 build(sb, (Brace) be);
             } else {
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.filter.invalidBrace", be);
             }
             pos++;
         }
         sb.append(")");
         // Add virtual table name (need by mysql)
         sb.append((" unInner" + br.getId()));
         // Close OR
         sb.append(")");
     }
 
     /**
      * Builds an 'AND' condition.
      *
      * @param sb the StringBuilder to use
      * @param br the brace
      * @throws FxSqlSearchException if the build failed
      */
     private void buildAnd(StringBuilder sb, Brace br) throws FxSqlSearchException {
         // Start AND
         int pos = 0;
         final StringBuilder combinedConditions = new StringBuilder();
         int firstId = -1;
         for (BraceElement be : br.getElements()) {
             if (pos == 0) {
                 firstId = be.getId();
                 sb.append(("(SELECT tbl" + firstId + ".id,tbl" + firstId + ".ver,tbl" + firstId + ".lang FROM\n"));
             } else {
                 sb.append(",");
                 combinedConditions.append((pos > 1) ? " AND " : " ")
                         .append("tbl").append(firstId).append(".id=tbl").append(be.getId()).append(".id AND ")
                         .append("tbl").append(firstId).append(".ver=tbl").append(be.getId()).append(".ver AND ")
                         .append("(tbl").append(firstId).append(".lang=0 or tbl").append(firstId).append(".lang IS NULL OR ")
                         .append("tbl").append(be.getId()).append(".lang=0 OR tbl").append(be.getId()).append(".lang IS NULL OR ")
                         .append("tbl").append(firstId).append(".lang=tbl").append(be.getId()).append(".lang)");
             }
 
             if (be instanceof Condition) {
                 sb.append(getConditionSubQuery(br.getStatement(), (Condition) be));
             } else if (be instanceof Brace) {
                 build(sb, (Brace) be);
             } else {
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.filter.invalidBrace", be);
             }
             sb.append(" tbl").append(be.getId()).append("\n");
             pos++;
         }
         // Where links the tables together
         sb.append(" WHERE ");
         sb.append(combinedConditions);
         // Close AND
         sb.append(")");
     }
 
     /**
      * Helper function for getConditionSubQuery(__).
      *
      * @param cond the condition
      * @param mode tree mode
      * @return the filter sub-statement
      * @throws com.flexive.shared.exceptions.FxSqlSearchException
      *          if the tree node was invalid or could not be found
      */
     public String getTreeFilter(Condition cond, FxTreeMode mode) throws FxSqlSearchException {
         boolean direct = cond.getComperator() == Comparator.IS_DIRECT_CHILD_OF;
 //        String table = DatabaseConst.TBL_TREE+(mode == FxTreeMode.Live ?"L":"");
 
         long parentNode;
         final String value = cond.getConstant().getValue();
         try {
             parentNode = Long.valueOf(value);
         } catch (NumberFormatException e) {
             final String path = FxSharedUtils.stripQuotes(value, '\'');
             try {
                 // Lookup the path in the tree
                 parentNode = search.getTreeEngine().getIdByPath(mode, path);
             } catch (FxApplicationException e2) {
                 throw new FxSqlSearchException(LOG, e2, "ex.sqlSearch.filter.invalidTreePath", path);
             }
             if (parentNode == -1) {
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.filter.invalidTreePath", path);
             }
         }
         if (parentNode < 0) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.filter.invalidTreeId", parentNode);
         }
 
         final FxTreeNodeInfo nodeInfo;
         try {
             nodeInfo = StorageManager.getTreeStorage().getTreeNodeInfo(con, mode, parentNode);
         } catch (FxApplicationException e) {
             throw new FxSqlSearchException(LOG, e, "ex.sqlSearch.filter.loadTreeNode", parentNode, e.getMessage());
         }
         String mandators = CacheAdmin.getEnvironment().getInactiveMandatorList();
         final String mandatorFilter;
         if (mandators.length() > 0)
             mandatorFilter = " AND cd.mandator NOT IN(" + mandators + ")";
         else
             mandatorFilter = mandators; //empty
         String types = CacheAdmin.getEnvironment().getDeactivatedTypesList();
         final String typeFilter;
         if (types.length() > 0)
             typeFilter = " AND cd.tdef NOT IN(" + types + ")";
         else
             typeFilter = types; //empty
         return "(SELECT DISTINCT cd.id,cd.ver,null lang FROM " + tableMain + " cd WHERE " +
                 "cd.id IN (SELECT ref FROM " + GenericTreeStorage.getTable(mode) + " WHERE " +
                 "LFT>" + nodeInfo.getLeft() + " AND RGT<" + nodeInfo.getRight() + " AND ref IS NOT NULL " +
                 (direct ? " AND depth=" + (nodeInfo.getDepth() + 1) : "") +
                 ")" + mandatorFilter + typeFilter + ")";
 
     }
 
     /**
      * Builds a condition sub-query.
      *
      * @param stmt the statement
      * @param cond the condition to build
      * @return the sub query
      * @throws com.flexive.shared.exceptions.FxSqlSearchException
      *          if the function failed
      */
     private String getConditionSubQuery(FxStatement stmt, Condition cond) throws FxSqlSearchException {
         final Table tblContent = stmt.getTableByType(Table.TYPE.CONTENT);
         if (tblContent == null) {
             throw new FxSqlSearchException(LOG, "ex.sqlSearch.contentTableMissing");
         }
         final Property prop = cond.getProperty();
         final Constant constant = cond.getConstant();
 
 
         if (cond.getComperator() == Comparator.IS_CHILD_OF ||
                 cond.getComperator() == Comparator.IS_DIRECT_CHILD_OF) {
             // In case of VERSION.ALL we will look up the LIVE and EDIT tree
             String result = "";
             int count = 0;
             if (stmt.getVersionFilter() == VersionFilter.LIVE ||
                     stmt.getVersionFilter() == VersionFilter.ALL) {
                 result += getTreeFilter(cond, FxTreeMode.Live);
                 count++;
             }
             if (stmt.getVersionFilter() == VersionFilter.MAX ||
                     stmt.getVersionFilter() == VersionFilter.ALL) {
                 if (result.length() > 0) {
                     result += "\n UNION \n";
                 }
                 result += getTreeFilter(cond, FxTreeMode.Edit);
                 count++;
             }
             return (count < 2) ? result : "(" + result + ")";
         } else if (prop.getPropertyName().equals("*")) {
             // Fulltext search
             return "(SELECT DISTINCT ft.id,ft.ver,ft.lang " +
                     "FROM " + tableFulltext + " ft," + DatabaseConst.TBL_CONTENT + " cd WHERE\n" +
                     "cd.ver=ft.ver AND cd.id=ft.id and\n" +
                     "MATCH (value) AGAINST (" + constant.getValue() + ")\n" +
                     getLanguageFilter() +
                     getVersionFilter("cd") +
                     getTypeFilter("cd") +
                     getMandatorFilter("cd") +
                     getSubQueryLimit() +
                     ")";
             /*} else if (prop.getPropertyName().charAt(0)=='#') {
                 long assignmentId = Long.valueOf(prop.getPropertyName().substring(1));
                 FxAssignment fx_ass;
                 try {
                     fx_ass = CacheAdmin.getEnvironment().getAssignment(assignmentId);
                 } catch(Throwable t) {
                     boolean notFound = ((FxRuntimeException)t).getConverted() instanceof FxNotFoundException;
                     if (notFound) {
                         throw new FxSqlSearchException(t,"ex.sqlSearch.query.unknownAssignment",assignmentId);
                     } else {
                         throw new FxSqlSearchException(t,"ex.sqlSearch.query.failedToResolveAssignment",
                                 assignmentId,t.getMessage());
                     }
                 }
                 if (!(fx_ass instanceof FxPropertyAssignment)) {
                     // Return no match for group assignments
                     return NO_MATCH;
                 }
                 FxProperty fx_prop = ((FxPropertyAssignment)fx_ass).getProperty();
                 Property _prop = new Property(prop.getTableAlias(),fx_prop.getName(),prop.getField());
                 return buildPropertyCondition(stmt,cond,_prop,(FxPropertyAssignment)fx_ass);
             */
         } else {
             return buildPropertyCondition(stmt, cond, cond.getProperty());
         }
     }
 
     /**
      * Helper function.
      *
      * @param stmt the statement
      * @param cond the condition
      * @param prop the property
      * @return the subquery
      * @throws FxSqlSearchException if a error occured
      */
     private String buildPropertyCondition(FxStatement stmt, Condition cond, Property prop)
             throws FxSqlSearchException {
 
         final Constant constant = cond.getConstant();
         final PropertyEntry entry = getPropertyResolver().get(stmt, prop);
         final String filter = prop.isAssignment()
                 ? "ASSIGN=" + entry.getAssignment().getId()
                 : "TPROP=" + entry.getProperty().getId();
 
         if (entry.getTableName().equals(DatabaseConst.TBL_CONTENT_DATA) &&
                 cond.getComperator().equals(Condition.Comparator.IS) &&
                 cond.getConstant().isNull()) {
             // IS NULL is a specical case for the content data table:
             // a property is null if no entry is present in the table, which means we must
             // find the entries by using a join on the main table
 
             //mp: wrong properties selected for a join/union and braces missing for limit statement
             //    was:        return "select distinct da.assign,da.tprop,ct.id,ct.ver from "+tableMain+" ct\n" +
             return "(SELECT DISTINCT da.id,da.ver,da.lang FROM " + tableMain + " ct\n" +
                     "LEFT JOIN " + tableContentData + " da ON (ct.id=da.id AND ct.ver=da.ver AND da." + filter + ")\n" +
                     "WHERE da.tprop IS NULL " +
                     getVersionFilter("ct") +
                     getTypeFilter("ct") +
                     getMandatorFilter("ct") +
                     // Assignment search: we are only interrested in the type that the assignment belongs to
                     (prop.isAssignment()
                             ? "AND ct.TDEF=" + entry.getAssignment().getAssignedType().getId() + " "
                             : "") +
                     getSubQueryLimit() + ")";
         }
 
         String column = prop.getFunctionsStart() +
                 entry.getFilterColumn() +
                 prop.getFunctionsEnd();
         String value;
         switch (entry.getProperty().getDataType()) {
             case String1024:
             case Text:
             case HTML:
                 value = constant.getValue();
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
                 value = "" + FxFormatUtils.toLong(constant.getValue());
                 break;
             case Number:
                 value = "" + FxFormatUtils.toInteger(constant.getValue());
                 break;
             case Double:
                 value = "" + FxFormatUtils.toDouble(constant.getValue());
                 break;
             case Float:
                 value = "" + FxFormatUtils.toFloat(constant.getValue());
                 break;
             case SelectOne:
                 if (StringUtils.isNumeric(constant.getValue())) {
                     //list item id, nothing to lookup
                     value = constant.getValue();
                 } else {
                     //list item data (of first matching item)
                     value = "" + entry.getProperty().getReferencedList().
                             getItemByData(FxFormatUtils.unquote(constant.getValue())).getId();
                 }
                 break;
             case SelectMany:
                 if (Comparator.EQUAL.equals(cond.getComperator()) || Comparator.NOT_EQUAL.equals(cond.getComperator())) {
                     // exact match, so we use the text column that stores the comma-separated list of selected items
                     column = "FTEXT1024";
                     value = "'" + StringUtils.join(constant.iterator(), ',') + "'";
                 } else {
                     throw new FxSqlSearchException(LOG, "ex.sqlSearch.reader.type.invalidOperator",
                             entry.getProperty().getDataType(), cond.getComperator());
                 }
                 break;
             case Boolean:
                 value = FxFormatUtils.toBoolean(constant.getValue()) ? "1" : "0";
                 break;
             case Date:
             case DateRange:
                 value = constant.getValue() == null ? "NULL" : "'" + FxFormatUtils.getDateFormat().format(FxFormatUtils.toDate(constant.getValue())) + "'";
                 break;
             case DateTime:
             case DateTimeRange:
                value = constant.getValue() == null ? "NULL" : "'" + FxFormatUtils.getDateTimeFormat().format(FxFormatUtils.toDateTime(constant.getValue())) + "'";
                 break;
             case Binary:
                 if (cond.getComperator().equals(Comparator.IS_NOT) && constant.isNull()) {
                     value = "NULL";
                     break;
                 }
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.reader.type.invalidOperator",
                         entry.getProperty().getDataType(), cond.getComperator());
             case Reference:
                 value = String.valueOf(FxPK.fromString(constant.getValue()).getId());
                 break;
             default:
                 throw new FxSqlSearchException(LOG, "ex.sqlSearch.reader.unknownPropertyColumnType",
                         entry.getProperty().getDataType(), prop.getPropertyName());
         }
 
         // IGNORE_CASE Filter
         if (stmt.getIgnoreCase()) {
             value = value.toUpperCase();
         }
 
         // Apply all Functions
         value = constant.getFunctionsStart() + value + constant.getFunctionsEnd();
 
         // Build the final filter statement
         if (entry.getTableName().equals(DatabaseConst.TBL_CONTENT)) {
             return (" (SELECT DISTINCT cd.id,cd.ver,null lang FROM " + tableMain + " cd WHERE " +
                     column +
                     cond.getSqlComperator() +
                     value +
                     getVersionFilter("cd") +
                     getTypeFilter("cd") +
                     getMandatorFilter("cd") +
                     getSubQueryLimit() +
                     ") ");
         } else {
             return " (SELECT DISTINCT cd.id,cd.ver,cd.lang FROM " + tableContentData + " cd WHERE " +
                     column +
                     cond.getSqlComperator() +
                     value +
                     " AND " + filter +
                     getVersionFilter("cd") +
                     getLanguageFilter() +
                     getSubQueryLimit() +
                     ") ";
         }
     }
 
     /**
      * Get a filter for the version
      *
      * @param tblAlias table alias
      * @return filter statement
      */
     private String getVersionFilter(String tblAlias) {
         String tbl = (tblAlias == null || tblAlias.length() == 0) ? "" : tblAlias + ".";
         if (getStatement().getVersionFilter() == VersionFilter.ALL) {
             return "";
         } else if (getStatement().getVersionFilter() == VersionFilter.LIVE) {
             return " AND " + tbl + "islive_ver=TRUE ";
         } else {
             return " AND " + tbl + "ismax_ver=TRUE ";
         }
     }
 
     /**
      * Get a filter for (in)active mandators
      *
      * @param tblAlias table alias
      * @return filter statement
      */
     private String getMandatorFilter(String tblAlias) {
         String tbl = (tblAlias == null || tblAlias.length() == 0) ? "" : tblAlias + ".";
         String mandators = CacheAdmin.getEnvironment().getInactiveMandatorList();
         if (mandators.length() > 0)
             return " AND " + tbl + "mandator NOT IN(" + mandators + ")";
         else
             return ""; //empty
     }
 
     /**
      * Get a filter for deactivated FxType's
      *
      * @param tblAlias table alias
      * @return filter statement
      */
     private String getTypeFilter(String tblAlias) {
         String tbl = (tblAlias == null || tblAlias.length() == 0) ? "" : tblAlias + ".";
         String types = CacheAdmin.getEnvironment().getDeactivatedTypesList();
         if (types.length() > 0)
             return " AND " + tbl + "tdef NOT IN(" + types + ")";
         else
             return ""; //empty
     }
 
     private String getSubQueryLimit() {
         return SUBQUERY_LIMIT == -1 ? "" : " LIMIT " + SUBQUERY_LIMIT + " ";
     }
 
     private String getLanguageFilter() {
         return search.getLanguage() == null ? "" : " AND lang IN (0," + search.getLanguage().getId() + ") ";
     }
 
 
 }
