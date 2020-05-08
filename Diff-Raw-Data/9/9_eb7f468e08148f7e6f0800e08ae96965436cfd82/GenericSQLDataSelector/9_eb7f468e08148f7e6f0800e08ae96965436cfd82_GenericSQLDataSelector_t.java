 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
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
 package com.flexive.core.search.genericSQL;
 
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.search.*;
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.Pair;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.search.SortDirection;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxFlatStorageMapping;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.sqlParser.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.util.*;
 
 import static com.flexive.core.DatabaseConst.TBL_CONTENT;
 import static com.flexive.core.DatabaseConst.TBL_CONTENT_ACLS;
 
 /**
  * Generic SQL data selector
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class GenericSQLDataSelector extends DataSelector {
     private static final Log LOG = LogFactory.getLog(GenericSQLDataSelector.class);
 
     /**
      * All field selectors supported by this implementation
      */
     protected static final Map<String, FieldSelector> SELECTORS = new HashMap<String, FieldSelector>();
 
     protected static FieldSelector SELECTLIST_ITEM_SELECTOR;
 
     private static final Object UPDATE_LOCK = new Object();
 
     protected static final String[] CONTENT_DIRECT_SELECT = {"ID", "VERSION"};
     protected static final String[] CONTENT_DIRECT_SELECT_PROP = {"ID", "VER"};
     protected static final String FILTER_ALIAS = "filter";
     protected static final String SUBSEL_ALIAS = "sub";
 
     protected SqlSearch search;
 
 
     /**
      * Constructor.
      *
      * @param search the search object
      */
     public GenericSQLDataSelector(SqlSearch search) {
         this.search = search;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Map<String, FieldSelector> getSelectors() {
         synchronized(UPDATE_LOCK) {
             if (SELECTORS.isEmpty()) {
                 SELECTORS.put("MANDATOR", new GenericSQLForeignTableSelector("mandator", DatabaseConst.TBL_MANDATORS, "id", false, null));
                 SELECTORS.put("CREATED_BY", new GenericSQLForeignTableSelector("created_by", DatabaseConst.TBL_ACCOUNTS, "id", false, null));
                 SELECTORS.put("MODIFIED_BY", new GenericSQLForeignTableSelector("modified_by", DatabaseConst.TBL_ACCOUNTS, "id", false, null));
                 SELECTORS.put("ACL", new GenericSQLForeignTableSelector("acl", DatabaseConst.TBL_ACLS, "id", true, "label"));
                 SELECTORS.put("STEP", new GenericSQLStepSelector());
                 SELECTORS.put("TYPEDEF", new GenericSQLForeignTableSelector("tdef", DatabaseConst.TBL_STRUCT_TYPES, "id", true, "description"));
             }
         }
         return SELECTORS;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String build(final Connection con) throws FxSqlSearchException {
         StringBuffer select = new StringBuffer();
         buildColumnSelectList(select);
         if (LOG.isTraceEnabled()) {
             LOG.trace(search.getFxStatement().printDebug());
         }
         return select.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void cleanup(Connection con) throws FxSqlSearchException {
         // nothing to do
     }
 
     public FieldSelector getSelectListItemSelectorInstance() {
         synchronized(UPDATE_LOCK) {
             if (SELECTLIST_ITEM_SELECTOR == null) {
                 SELECTLIST_ITEM_SELECTOR = new GenericSQLForeignTableSelector(
                         null, DatabaseConst.TBL_STRUCT_SELECTLIST_ITEM, "ID", false, null
                 );
             }
         }
         return SELECTLIST_ITEM_SELECTOR;
     }
 
     /**
      * Builds the column select.
      * <p/>
      * Example result: "x.oid,x.ver,(select ..xx.. from ...),..."
      *
      * @param select the stringbuffer to write to
      * @throws FxSqlSearchException if the function fails
      */
     private void buildColumnSelectList(final StringBuffer select) throws FxSqlSearchException {
         // Build all value selectors
         int pos = 0;
         final SubSelectValues values[] = new SubSelectValues[search.getFxStatement().getSelectedValues().size()];
         for (SelectedValue selectedValue : search.getFxStatement().getSelectedValues()) {
             // Prepare all values
             final Value value = selectedValue.getValue();
             PropertyEntry entry = null;
             if (value instanceof Property) {
                 final PropertyResolver pr = search.getPropertyResolver();
                 entry = pr.get(search.getFxStatement(), (Property) value);
                 pr.addResultSetColumn(search, entry);
             }
             values[pos] = selectFromTbl(entry, value, pos);
             pos++;
         }
         // need to check if any order bys from a wildcard selector have not been found
         for (OrderByValue orderBy : search.getFxStatement().getOrderByValues()) {
             if (orderBy.getColumnIndex() < 0 && Math.abs(orderBy.getColumnIndex()) >= pos) {
                 // column has not been selected, thus it is not present in the "ORDER BY" clause
                 throw new FxSqlSearchException("ex.sqlSearch.invalidOrderByIndex", orderBy.getColumnIndex(), pos);
             }
         }
 
         // Build the final select statement
 
         select.append("SELECT \n")
                 .append(filterProperties(INTERNAL_RESULTCOLS.toArray(new String[INTERNAL_RESULTCOLS.size()])))
                 .append(' ');
         for (SubSelectValues ssv : values) {
             for (SubSelectValues.Item item : ssv.getItems()) {
                 if (ssv.isSorted() && item.isOrderBy()) {
                     select.append(",").append(FILTER_ALIAS).append(".").append(item.getAlias()).append("\n");
                 } else {
                     select.append(",").append(item.getSelect()).append(" AS ").append(item.getAlias()).append("\n");
                 }
             }
         }
         select.append("FROM (");
         select.append(buildSourceTable(values));
         select.append(") ").append(FILTER_ALIAS);
         if (supportsRowNr()) {
             select.append(" ORDER BY 1");
         }
     }
 
     /**
      * Builds the source table.
      *
      * @param values the selected values
      * @return a select statement which builds the source table
      */
     private String buildSourceTable(final SubSelectValues[] values) {
         // Prepare type filter
         final String typeFilter = search.getTypeFilter() == null ? ""
                 : " AND " + FILTER_ALIAS + ".TDEF=" + search.getTypeFilter().getId() + " ";
 
         final Map<Integer, String> orderByNumbers = new HashMap<Integer, String>(); // order by index --> column select
         final List<String> columns = new ArrayList<String>();
         final boolean noSort = orderByNumbers.isEmpty() && search.getParams().isNoInternalSort();
 
         // create select columns
         for (String column : INTERNAL_RESULTCOLS) {
             if ("rownr".equals(column)) {
                 if (supportsCounterAfterOrderBy() || noSort) {
                     columns.add(getCounterStatement(column));
                 }
             } else {
                 columns.add(filterProperties(column));
             }
         }
         columns.add(search.getStorage().concat("t.name", "'[@pk='", filterProperties("id"), "'.'", filterProperties("ver"), "']'") + " AS xpathPref");
         // order by starts after the internal selected columns
         int orderByPos = columns.size() + 1;
 
         // create SQL statement
         final StringBuilder sql = new StringBuilder();
         sql.append("select ").append(StringUtils.join(columns, ',')).append(' ');
 
         // add order by indices
         for (SubSelectValues ssv : values) {
             if (ssv.isSorted()) {
                 for (SubSelectValues.Item item : ssv.getItems()) {
                     if (item.isOrderBy()) {
                         // select item from order by
                         sql.append(",").append(item.getSelect()).append(" AS ").append(item.getAlias()).append("\n");
                         // add index for first selected column
                         if (!orderByNumbers.containsKey(ssv.getSortPos())) {
                             orderByNumbers.put(ssv.getSortPos(), orderByPos + " " + (ssv.isSortedAscending() ? "asc" : "desc"));
                         }
                     } else {
                         sql.append(",null AS ").append(item.getAlias()).append('\n');
                     }
                     orderByPos++;
                 }
             }
         }
         sql.append(("FROM " + search.getCacheTable() + " filter, " + DatabaseConst.TBL_STRUCT_TYPES + " t " +
                 "WHERE search_id=" + search.getSearchId() + " AND " + FILTER_ALIAS + ".tdef=t.id " +
                 typeFilter + " "));
 
         final boolean usePaging = search.getStartIndex() > 0 || search.getFetchRows() < Integer.MAX_VALUE;
 
         if (noSort) {
             if (usePaging) {
                 LOG.warn("Paging is disabled because noInternalSort was specified");
             }
         } else {
             if (orderByNumbers.size() == 0) {
                 // No order by specified = order by id and version
                 int idCol = supportsCounterAfterOrderBy() ? 2 : 1;
                 orderByNumbers.put(1, idCol + " asc");
                 orderByNumbers.put(2, (idCol + 1) + " asc");
             }
             sql.append("ORDER BY ");
             // insert order by columns in the order they were defined in the FxSQL query
             for (Integer index : new TreeSet<Integer>(orderByNumbers.keySet())) {
                 sql.append(orderByNumbers.get(index)).append(',');
             }
             sql.setCharAt(sql.length() - 1, ' ');   // replace last separator
 
             if (!supportsCounterAfterOrderBy()) {
                 // insert outer SELECT
                 sql.insert(0, "SELECT " + getCounterStatement("rownr") + ", x.* FROM (");
                 sql.append(") x ");
             }
 
             // Evaluate the order by, then limit the result by the desired range if needed
             if (usePaging) {
                 return "SELECT * FROM (" + sql + ") tmp " + search.getStorage().getLimitOffsetVar("rownr", false, search.getFetchRows(), search.getStartIndex());
             }
         }
 
         return sql.toString();
 
     }
 
     /**
      * Returns a comma-separated list of the given property names after adding FILTER_ALIAS to every property.
      *
      * @param names the property name(s)
      * @return a comma-separated list of the given property names after adding FILTER_ALIAS to every property.
      */
     private String filterProperties(String... names) {
         return FILTER_ALIAS + "." + StringUtils.join(names, "," + FILTER_ALIAS + ".");
     }
 
     /**
      * Build the subselect query needed to get any data from the CONTENT table.
      *
      * @param entry     the entry to select
      * @param prop      the property
      * @param resultPos the position of the property in the select statement
      * @return the SubSelectValues
      * @throws FxSqlSearchException if anything goes wrong
      */
     protected SubSelectValues selectFromTbl(PropertyEntry entry, Value prop, int resultPos) throws FxSqlSearchException {
         final Pair<Integer, SortDirection> sortInfo = getSortInfo(resultPos);
         final SubSelectValues result = new SubSelectValues(resultPos, sortInfo.getFirst(), sortInfo.getSecond());
 
         // disable XPath processing if corresponding hint is set
         entry.setProcessXPath(!FxContext.getUserTicket().isGlobalSupervisor() && !search.getParams().isHintIgnoreXPath());
 
         entry.setProcessData(search.getParams().isHintSelectData());
 
         final FxTreeMode treeMode = search.getParams() != null ? search.getParams().getTreeMode() : FxTreeMode.Edit;
 
         if (prop instanceof Constant || entry == null) {
             result.addItem(prop.getValue().toString(), resultPos, false);
         } else if (entry.getType() == PropertyEntry.Type.NODE_POSITION) {
 
            long root = search.getParams() != null ? search.getParams().getTreeRootId() :  FxTreeNode.ROOT_NODE;
             final String sel = "(select tree_nodeIndex(" + root + "," + FILTER_ALIAS + ".id," +
                     search.getStorage().getBooleanExpression(
                             treeMode == FxTreeMode.Live
                     )
                     + ")" + search.getStorage().getFromDual() + ")";
             result.addItem(sel, resultPos, false);
         } else if (entry.getType() == PropertyEntry.Type.PATH) {
             final long propertyId = search.getEnvironment().getProperty("CAPTION").getId();
             final String sel = "(select tree_FTEXT1024_Paths(" + FILTER_ALIAS + ".id," +
                     search.getLanguage().getId() + "," + propertyId + "," +
                     search.getStorage().getBooleanExpression(
                             treeMode == FxTreeMode.Live
                     )
                     + ")" + search.getStorage().getFromDual() + ")";
             result.addItem(sel, resultPos, false);
         } else if (entry.getType() == PropertyEntry.Type.METADATA) {
             // TODO: support for tree node metadata?
 
             // check if the metadata can be selected unambiguously
             final long[] briefcaseFilter = search.getFxStatement().getBriefcaseFilter();
             if (briefcaseFilter.length == 0) {
                 throw new FxSqlSearchException("ex.sqlSearch.briefcase.metadata.empty");
             } else if (briefcaseFilter.length > 1) {
                 throw new FxSqlSearchException("ex.sqlSearch.briefcase.metadata.ambiguous");
             }
             // select metadata (if structured properly, you can also sort based on metadata)
             final String sel = "(SELECT metadata FROM " + DatabaseConst.TBL_BRIEFCASE_DATA + " WHERE briefcase_id=" + briefcaseFilter[0] + " AND id=" + FILTER_ALIAS + ".id)";
             result.addItem(sel, resultPos, false);
         } else if (entry.getType() == PropertyEntry.Type.LOCK) {
             for (String readColumn : entry.getReadColumns()) {
                 final String sel = "(SELECT " + readColumn
                         + " FROM " + DatabaseConst.TBL_LOCKS
                         + " WHERE lock_id=" + FILTER_ALIAS + ".id AND lock_ver=" + FILTER_ALIAS + ".ver)";
                 result.addItem(sel, resultPos, false);
             }
         } else {
             switch (entry.getTableType()) {
                 case T_CONTENT:
                     for (String column : entry.getReadColumns()) {
                         if ("ACL".equalsIgnoreCase(column)) {
                             result.addItem(
                                     "(SELECT acl FROM " + TBL_CONTENT + " subc" +
                                             " WHERE subc.id = " + FILTER_ALIAS + ".id " +
                                             " AND subc.ver = " + FILTER_ALIAS + ".ver " +
                                             " AND subc.acl != " + ACL.NULL_ACL_ID +
                                             " UNION " +
                                             "SELECT acl FROM " + TBL_CONTENT_ACLS + " suba" +
                                             " WHERE suba.id = " + FILTER_ALIAS + ".id AND suba.ver = " + FILTER_ALIAS + ".ver" +
                                             search.getStorage().getLimit(true, 1) +
                                             ")",
                                     resultPos,
                                     false
                             );
                         } else {
                             final int directSelect = FxArrayUtils.indexOf(CONTENT_DIRECT_SELECT, column, true);
                             if (directSelect > -1) {
                                 final String val = FILTER_ALIAS + "." + CONTENT_DIRECT_SELECT_PROP[directSelect];
                                 result.addItem(val, resultPos, false);
                             } else {
                                 String expr = SUBSEL_ALIAS + "." + column;
                                 if (!prop.getSqlFunctions().isEmpty() && PropertyEntry.isDateMillisColumn(column)) {
                                     // need to convert to date before applying a date function
                                     expr = toDBTime(expr);
                                 }
                                 final String val = "(SELECT " + expr + " FROM " + TBL_CONTENT +
                                         " " + SUBSEL_ALIAS + " WHERE " + SUBSEL_ALIAS + ".id=" + FILTER_ALIAS + ".id AND " +
                                         SUBSEL_ALIAS + ".ver=" + FILTER_ALIAS + ".ver)";
                                 result.addItem(val, resultPos, false);
                             }
                         }
                     }
                     break;
                 case T_CONTENT_DATA:
                     for (String column : entry.getReadColumns()) {
                         final String val = getContentDataSubselect(column, entry, false);
                         result.addItem(val, resultPos, false);
                     }
 //                    String xpath = "concat(filter.xpathPref," + getContentDataSubselect("XPATHMULT", entry, true) + ")";
                     if (!search.getParams().isHintIgnoreXPath() || (entry.isPropertyPermsEnabled() && !FxContext.getUserTicket().isGlobalSupervisor())) {
                         entry.setProcessXPath(true);    // re-enable flag if ignoreXPath hint was set
                         String xpath = search.getStorage().concat("filter.xpathPref", "\'-\'",
                                 entry.isAssignment() ? '\'' + entry.getAssignment().getXPath() + '\'' : getContentDataSubselect("ASSIGN", entry, true),
                                 "\'-\'",
                                 getContentDataSubselect("XMULT", entry, true));
                         result.addItem(xpath, resultPos, true);
                     }
                     if (search.getParams().isHintSelectData()) {
                         if (entry.getDataColumn() != null) {
                             final String select = getContentDataSubselect(entry.getDataColumn(), entry, true);
                             result.addItem(select, resultPos, true);
                         }
                     }
                     break;
                 case T_CONTENT_DATA_FLAT:
                     final FxFlatStorageMapping mapping = entry.getAssignment().getFlatStorageMapping();
                     final String sel = getFlatStorageColumnSelect(entry, mapping, mapping.getColumn());
                     result.addItem(sel, resultPos, false);
                     if (search.getParams().isHintSelectData()) {
                         // straight-forward but slow implementation. When multiple flat storage columns are selected,
                         // we may select the same valuedata column many times
                         final String select = getFlatStorageColumnSelect(entry, mapping, "VALUEDATA");
                         result.addItem(select, resultPos, true);
                     }
 
                     break;
                 default:
                     throw new FxSqlSearchException(LOG, "ex.sqlSearch.table.typeNotSupported", entry.getTableName());
             }
         }
 
         return result.prepare(this, prop, entry);
     }
 
     /**
      * Get a column select statement for a flatstorage column
      *
      * @param entry   entry to select
      * @param mapping flatstorage mapping
      * @param column  the column name
      * @return select statement
      */
     protected String getFlatStorageColumnSelect(PropertyEntry entry, FxFlatStorageMapping mapping, String column) {
         return "(SELECT " + column + " FROM " + mapping.getStorage()
                 + " WHERE id=" + FILTER_ALIAS + ".id AND ver=" + FILTER_ALIAS + ".ver"
                 + " AND "
                 + SearchUtils.getFlatStorageAssignmentFilter(
                 search.getEnvironment(),
                 "",
                 entry.getAssignment()
         )
                 + (entry.getAssignment().isMultiLang()
                 ? " AND " + mapping.getColumn() + " IS NOT NULL"
                 + " AND (lang=" + search.getLanguage().getId()
                 + " OR " + mapping.getColumn() + "_mld=true)"
                 + " ORDER BY " + mapping.getColumn() + "_mld"
                 : "")
                 + search.getStorage().getLimit(true, 1)
                 + ")";
     }
 
     /**
      * Convert a column with a timestamp in long format to a database understandable timestamp
      *
      * @param expr long column to convert
      * @return database time compliant expression
      */
     protected String toDBTime(String expr) {
         return "FROM_UNIXTIME(" + expr + "/1000)";
     }
 
     /**
      * Returns the position in the user's "ORDER BY" clause and the {@link SortDirection} for the given column index.
      *
      * @param pos the position to check
      * @return the position in the "ORDER BY" clause and the {@link SortDirection} for the given column index.
      */
     private Pair<Integer, SortDirection> getSortInfo(int pos) {
         final List<OrderByValue> obvs = search.getFxStatement().getOrderByValues();
         int index = 0;
         for (OrderByValue obv : obvs) {
             if (Math.abs(obv.getColumnIndex()) == pos) {    // also check for negative (=wildcard) order bys
                 return Pair.newPair(index, obv.isAscending() ? SortDirection.ASCENDING : SortDirection.DESCENDING);
             }
             index++;
         }
         return Pair.newPair(-1, SortDirection.UNSORTED);
     }
 
     /**
      * Returns the subselect for FX_CONTENT_DATA.
      *
      * @param column the column that is being procesed
      * @param entry  the entry
      * @param xpath  if true, the XPath (and not the actual value column(s)) will be selected
      * @return the subselect for FX_CONTENT_DATA.
      */
     protected String getContentDataSubselect(String column, PropertyEntry entry, boolean xpath) {
         String select = "(SELECT " + SUBSEL_ALIAS + "." + column +
                 " FROM " + DatabaseConst.TBL_CONTENT_DATA + " " +
                 SUBSEL_ALIAS + " WHERE " +
                 SUBSEL_ALIAS + ".id=" +
                 FILTER_ALIAS + ".id AND " +
                 SUBSEL_ALIAS + ".ver=" +
                 FILTER_ALIAS + ".ver AND " +
                 (entry.isAssignment()
                         ? "ASSIGN IN ("
                         + StringUtils.join(FxSharedUtils.getSelectableObjectIdList(entry.getAssignmentWithDerived()), ',')
                         + ")"
                         : "TPROP=" + entry.getProperty().getId()) + " AND " +
                 "(" + SUBSEL_ALIAS + ".lang=" + search.getLanguage().getId() +
                 " OR " + SUBSEL_ALIAS + ".ismldef=" + search.getStorage().getBooleanTrueExpression() + ")" +
                 // fetch exact language match before default, sort by position to get first entry
                 " ORDER BY " + SUBSEL_ALIAS + ".ismldef, " + SUBSEL_ALIAS + ".pos " +
                 search.getStorage().getLimit(true, 1) + ")";
         if (!xpath && entry.getProperty().getDataType() == FxDataType.Binary) {
             // select string-coded form of the BLOB properties
             select = selectBinary(select);
         }
         return select;
     }
 
     /**
      * <p>Indicate whether the database row index returned by {@link #getCounterStatement(String)}
      * is applied before or after an "order by" for the statement.</p>
      * <p/>
      * <p>If "true", the row index expression will be included in the select clause of the actual statement. For example:
      * {@code SELECT @rowNum=@rowNum+1, id, caption FROM ... ORDER BY caption}
      * </p>
      * <p>If "false", the row index expression will be added in an outer select clause, for example:
      * {@code SELECT @rowNum=@rowNum+1, * FROM (SELECT id, caption FROM ... ORDER BY caption) }
      * </p>
      *
      * @return true if the counter statement is evaluated after the "order by" clause, false otherwise.
      */
     protected boolean supportsCounterAfterOrderBy() {
         return true;
     }
 
     /**
      * @return  true when the database supports rownr variables to preserve the ordering
      * @since 3.2.0
      */
     protected boolean supportsRowNr() {
         return true;
     }
 }
